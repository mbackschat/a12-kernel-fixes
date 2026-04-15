/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
package com.mgmtp.a12.kernel.md.model.a12internal.services.codegen.cli;

import static com.mgmtp.a12.kernel.md.model.a12internal.services.codegen.cli.VkValidationCodeGeneratorCli.WRAP_JS_ARG_NAME;
import static com.mgmtp.a12.kernel.md.model.a12internal.services.codegen.cli.VkValidationCodeGeneratorCli.WRAP_JS_EXT_ARG_NAME;
import static com.mgmtp.a12.kernel.md.model.a12internal.services.codegen.cli.VkValidationCodeGeneratorCli.WRAP_JS_EXT_USAGE;
import static com.mgmtp.a12.kernel.md.model.a12internal.services.codegen.cli.VkValidationCodeGeneratorCli.WRAP_JS_USAGE;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.cli.help.TextHelpAppendable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig.ProgrammingLanguage;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.FilePathIncludedDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelReferenceResolverWrapper;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Batch generator for javascript vk validation code
 */
@OnlyForUsage
public class BatchVkValidationCodeGeneratorJs {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchVkValidationCodeGeneratorJs.class);
	private static final Options OPTIONS = new Options();

	static {
		OPTIONS.addOption("w", "workspace-path", true, "directory in which the included/imported document models can be found.");
		OPTIONS.addOption("wjs", "wrap-js-code-in-function", false, WRAP_JS_USAGE);
		OPTIONS.addOption("wjse", "wrap-js-code-in-function-extended", false, WRAP_JS_EXT_USAGE);
	}

	private Path workspacePath;
	private boolean wrapJsCodeInFunction;
	private boolean wrapJsCodeInFunctionExtended;

	private List<String> arguments;

	public static void main(final String[] args) {
		if (!batchCodeGenerationSuccessful(args)) {
			System.exit(-1);
		}
	}

	public static boolean batchCodeGenerationSuccessful(String[] args) {
		final BatchVkValidationCodeGeneratorJs cli = new BatchVkValidationCodeGeneratorJs();
		final CommandLineParser parser = new DefaultParser();
		final CommandLine cmd;
		try {
			cmd = parser.parse(OPTIONS, args, false);
			if (cmd.hasOption("w")) {
				cli.workspacePath = Paths.get(cmd.getOptionValue("w"));
			}
			if (cmd.hasOption("wjs")) {
				cli.wrapJsCodeInFunction = true;
			}
			if (cmd.hasOption("wjse")) {
				cli.wrapJsCodeInFunctionExtended = true;
			}
			cli.arguments = cmd.getArgList();
		} catch (final ParseException e) {
			showHelp(e.getMessage());
			return false;
		}

		if (cli.workspacePath != null) {
			if (!cli.workspacePath.toFile().exists() || !cli.workspacePath.toFile().isDirectory()) {
				showHelp(String.format("The given path to the workspace '%s' does not exist or does not point to a directory.",
						cli.workspacePath.toString()));
				return false;
			}
			VkValidationCodeGeneratorCli.setDocModelResolver(new DocumentModelReferenceResolverWrapper(
					new FilePathIncludedDocumentModelResolver(cli.workspacePath)));
		}

		if (cli.wrapJsCodeInFunction && cli.wrapJsCodeInFunctionExtended) {
			showHelp(String.format("Option %s cannot be used with option %s", WRAP_JS_ARG_NAME, WRAP_JS_EXT_ARG_NAME));
			return false;
		}

		return cli.generateCodeWithArgs(ProgrammingLanguage.JAVASCRIPT, cli.arguments);
	}

	private static void showHelp(final String error) {
		final TextHelpAppendable helpAppendable = new TextHelpAppendable(System.out);
		helpAppendable.setMaxWidth(125);
		final HelpFormatter helpFormatter = HelpFormatter.builder()
				.setHelpAppendable(helpAppendable)
				.setShowSince(false)
				.get();

		helpFormatter.setSyntaxPrefix("usage:");

		final StringBuilder header = new StringBuilder();
		if (error != null) {
			header.append("\terror: ");
			header.append(error);
		}
		header.append("\noptions:");

		try {
			helpFormatter.printHelp(
					BatchVkValidationCodeGeneratorJs.class.getSimpleName()
							+ " (" + WRAP_JS_ARG_NAME + " | " + WRAP_JS_EXT_ARG_NAME + ")? {<infile> <outfile>}+",
					header.toString(),
					OPTIONS,
					null,
					false

			);
		} catch (IOException e) {
			// Should never happen with TextHelpAppendable(System.out)
			throw new RuntimeException(e);
		}
	}

	private boolean generateCodeWithArgs(final ProgrammingLanguage programmingLanguage,
			List<String> args) {
		if (args == null || args.isEmpty() || args.size() % 2 != 0) {
			showHelp("Argument \"{<infile> <outfile>}+\" is required");
			return false;
		}
		LOGGER.info("Starting code generation of {} document models.", args.size() / 2);
		final long startTime = System.currentTimeMillis();
		boolean result = true;
		try {
			for (int i = 0; i < args.size(); i += 2) {
				final Path inFile = Paths.get(args.get(i));
				final Path outFile = Paths.get(args.get(i + 1));
				result &= generateCode(programmingLanguage, inFile, outFile);
			}
		} catch (Exception e) {
			LOGGER.error("Error in code generation: {}", e.getMessage());
			result = false;
		} finally {
			VkValidationCodeGeneratorCli.setDocModelResolver(null);
		}
		LOGGER.info("Code generation took {} ms.", (System.currentTimeMillis() - startTime));
		return result;
	}

	private boolean generateCode(
			final ProgrammingLanguage programmingLanguage,
			final Path inFile,
			final Path outFile) throws IOException {
		LOGGER.info("Generating code from {} and writing it to {}", inFile, outFile);
		List<String> args = new ArrayList<>();
		if (wrapJsCodeInFunction) {
			args.add(WRAP_JS_ARG_NAME);
		} else if (wrapJsCodeInFunctionExtended) {
			args.add(WRAP_JS_EXT_ARG_NAME);
		}
		args.add("--in-file");
		args.add(inFile.toString());
		args.add("--out-path");
		args.add(outFile.toString());
		args.add("--programming-language");
		args.add(programmingLanguage.name());
		return VkValidationCodeGeneratorCli.execute(args.toArray(new String[0])) == 0;
	}

}
