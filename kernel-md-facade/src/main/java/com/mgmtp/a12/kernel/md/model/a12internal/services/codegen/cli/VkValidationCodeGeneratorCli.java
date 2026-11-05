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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.cli.help.TextHelpAppendable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationModelService;
import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationModelService.CombinationModelExpandParams;
import com.mgmtp.a12.kernel.md.combination.a12internal.DirectoryBasedUnexpandedModelResolver;
import com.mgmtp.a12.kernel.md.combination.a12internal.IUnexpandedModelResolver;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig.JsCodeGenConfig;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig.ProgrammingLanguage;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DMProblemUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.cli.ArchiveExtractor;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.model.header.ModelReference;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Provides a command-line interface to the generator of validation code.
 */
@OnlyForUsage
public class VkValidationCodeGeneratorCli {

	private static final Logger LOGGER = LoggerFactory.getLogger(VkValidationCodeGeneratorCli.class);
	protected static final String WRAP_JS_ARG_NAME = "-wjs";
	protected static final String WRAP_JS_EXT_ARG_NAME = "-wjse";
	protected static final String WRAP_JS_USAGE = "if set, the generated javascript code will be wrapped in an UMD-wrapper "
			+ "(see https://github.com/umdjs/umd/blob/master/templates/returnExports.js).";
	protected static final String WRAP_JS_EXT_USAGE = "if set, the generated javascript code will be wrapped in an UMD-wrapper "
			+ "(see https://github.com/umdjs/umd/blob/master/templates/returnExports.js)  "
			+ "and an additional file with the same name but extension .d.ts will be generated with the needed typescript description code.";
	private static final Set<String> SUPPORTED_MODEL_REFS = Set.of("include", "typeDefinitions");
	private static final Options OPTIONS = new Options();

	private static IUnexpandedModelResolver resolver;

	static {
		OPTIONS.addOption("w", "workspace-path", true, "directory in which the included/imported document models can be found. " +
				"It is required if the given document model (in-file) contains includes/imports.");
		OPTIONS.addOption("i", "in-file", true, "the document model from which the validation code should be generated (required). "
				+ "NOTE: the document model will be automatically expanded.");
		OPTIONS.addOption("o", "out-path", true, "the file/directory to generate into, whether it is a file or a " +
				"directory is determined by option "
				+ "-l (required), for JAVA code the path will be used as base dir and the java packages will be expanded into it.");
		OPTIONS.addOption("m", "mapping-props-path", true, "the path to the properties file to generate the model name -> " +
				"validation package mapping into\n"
				+ "should be used only for JAVA code generation (optional)");
		OPTIONS.addOption("p", "mapping-props-path", true, "the path to the properties file to generate the model name -> " +
				"validation package mapping into\n"
				+ "should be used only for JAVA code generation (optional)");
		OPTIONS.addOption("l", "programming-language", true,
				"the programming language to generate: JAVA, JAVASCRIPT or GROOVY (required)");
		OPTIONS.addOption("wjs", "wrap-js-code-in-function", false, WRAP_JS_USAGE);
		OPTIONS.addOption("wjse", "wrap-js-code-in-function-extended", false, WRAP_JS_EXT_USAGE);
	}

	public static void setDocModelResolver(IUnexpandedModelResolver docModelResolver) {
		resolver = docModelResolver;
	}

	private String workspacePath;
	private String documentModelPath;
	private String destPath;
	private String propsPath;
	private String packageName;
	private ProgrammingLanguage programmingLanguage;
	private boolean wrapJsCodeInFunction;
	private boolean wrapJsCodeInFunctionExtended;

	private IDocumentModel documentModel;
	private Path outputPath;
	private IValidationCodeGeneratorConfig codegenConfig;
	private Path mappingPropertiesPath;
	private Properties mappingProperties;

	public static void main(final String[] args) throws IOException {
		try {
			int result = execute(args);
			if (result != 0) {
				// only exit if the process was not successful
				System.exit(result);
			}
		} catch (CodegenFailedException e) {
			// error details already logged by handleNotifications
			System.exit(-1);
		}
	}

	/**
	 * Parses CLI arguments, runs the validation-code generator and extracts the result.
	 *
	 * @return {@code 0} on success, {@code -1} if argument parsing or parameter
	 *         validation failed (help is printed to stdout in that case).
	 * @throws CodegenFailedException if the generator reported {@link Severity#ERROR}
	 *         notifications (the JVM is NOT terminated — callers may inspect the
	 *         exception's message for the error details).
	 * @throws IOException on I/O errors while reading the input model or writing the
	 *         mapping-properties file.
	 */
	public static int execute(final String[] args) throws IOException {
		final VkValidationCodeGeneratorCli cli = new VkValidationCodeGeneratorCli();
		final CommandLineParser parser = new DefaultParser();
		final CommandLine cmd;
		try {
			cmd = parser.parse(OPTIONS, args, false);
		} catch (final ParseException e) {
			showHelp(e.getMessage());
			return -1;
		}

		if (cmd.hasOption("w")) {
			cli.workspacePath = cmd.getOptionValue("w");
		}
		if (cmd.hasOption("i")) {
			cli.documentModelPath = cmd.getOptionValue("i");
		}
		if (cmd.hasOption("o")) {
			cli.destPath = cmd.getOptionValue("o");
		}
		if (cmd.hasOption("m")) {
			cli.propsPath = cmd.getOptionValue("m");
		}
		if (cmd.hasOption("p")) {
			cli.packageName = cmd.getOptionValue("p");
		}
		if (cmd.hasOption("l")) {
			try {
				cli.programmingLanguage = ProgrammingLanguage.valueOf(cmd.getOptionValue("l"));
			} catch (IllegalArgumentException e) {
				showHelp(String.format("\"%s\" is not a valid value for \"-l\"", cmd.getOptionValue("l")));
				return -1;
			}
		}
		if (cmd.hasOption("wjs")) {
			cli.wrapJsCodeInFunction = true;
		}
		if (cmd.hasOption("wjse")) {
			cli.wrapJsCodeInFunctionExtended = true;
		}
		if (cli.wrapJsCodeInFunction && cli.wrapJsCodeInFunctionExtended) {
			showHelp(String.format("Option %s cannot be used with option %s", WRAP_JS_ARG_NAME, WRAP_JS_EXT_ARG_NAME));
			return -1;
		}

		if (!cli.processParameters()) {
			return -1;
		}

		cli.generate();

		cli.finishUp();

		return 0;

	}

	private static void showHelp(final String error) {
		final TextHelpAppendable helpAppendable = new TextHelpAppendable(System.out);
		helpAppendable.setMaxWidth(150);
		final HelpFormatter helpFormatter = HelpFormatter.builder()
				.setHelpAppendable(helpAppendable)
				.setShowSince(false)
				.get();

		final StringBuilder header = new StringBuilder();
		if (error != null) {
			header.append("\terror: ");
			header.append(error);
		}
		header.append("\noptions:");

		try {
			helpFormatter.printHelp(
					VkValidationCodeGeneratorCli.class.getSimpleName(),
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

	private void printHelp() {
		HelpFormatter formatter = HelpFormatter.builder()
				.setShowSince(false)
				.get();

		try {
			formatter.printHelp(
					VkValidationCodeGeneratorCli.class.getSimpleName(),
					"",
					OPTIONS,
					"",
					true
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean processParameters() throws IOException {
		if (documentModelPath == null
				|| destPath == null
				|| documentModelPath.isEmpty()
				|| destPath.isEmpty()
				|| programmingLanguage == null
				|| ((wrapJsCodeInFunction || wrapJsCodeInFunctionExtended) && programmingLanguage != ProgrammingLanguage.JAVASCRIPT)) {
			printHelp();
			return false;
		}

		if (!handleOutputPath() || !handleDocumentModelPath()) {
			return false;
		}

		handleMappingPropertiesPath();
		createCodegenConfig();
		return true;
	}

	/**
	 * Runs the code generator and writes the result archive to disk. If the generator
	 * reports any {@link Severity#ERROR} notifications, aborts before extraction by
	 * throwing {@link CodegenFailedException}.
	 *
	 * @throws CodegenFailedException if at least one notification has severity
	 *         {@link Severity#ERROR}
	 */
	private void generate() {
		IDocumentModelService service = new ExternalDocumentModelService();
		LOGGER.info("Starting code generation...");
		final Collection<RankedNotification> notifications = new ArrayList<>();
		final byte[] zipArchive = service.generateValidationCode(documentModel, codegenConfig, mappingProperties,
				notifications::add);
		handleNotifications(notifications);
		new ArchiveExtractor(true).extractToDisk(zipArchive, outputPath,
				programmingLanguage == ProgrammingLanguage.GROOVY || programmingLanguage == ProgrammingLanguage.JAVASCRIPT);
	}

	/**
	 * Logs the given notifications grouped by severity. If any ERROR-severity
	 * notifications are present, throws {@link CodegenFailedException} with the
	 * joined error texts as message — so the abort reason is visible to programmatic
	 * callers (e.g. tests) via the exception's stack trace, in addition to the
	 * logged output.
	 *
	 * @throws CodegenFailedException if at least one notification has severity
	 *         {@link Severity#ERROR}
	 */
	private void handleNotifications(Collection<RankedNotification> notifications) {
		String infos = DMProblemUtils.join(notifications, Severity.INFO);
		if (StringUtils.isNotBlank(infos)) {
			LOGGER.info("=================================================");
			LOGGER.info("Following infos were found while generating the code:");
			LOGGER.info(infos);
			LOGGER.info("=================================================");
		}
		String warnings = DMProblemUtils.join(notifications, Severity.WARNING);
		if (StringUtils.isNotBlank(warnings)) {
			LOGGER.info("=================================================");
			LOGGER.info("Following warnings were found while generating the code:");
			LOGGER.info(warnings);
			LOGGER.info("=================================================");
		}
		String errors = DMProblemUtils.join(notifications, Severity.ERROR);
		if (StringUtils.isNotBlank(errors)) {
			LOGGER.info("=================================================");
			LOGGER.error("Following errors were found while generating the code:");
			LOGGER.error(errors);
			LOGGER.info("=================================================");
			throw new CodegenFailedException(errors);
		}
	}

	private void finishUp() throws IOException {
		if (mappingProperties == null) {
			return;
		}
		// write properties
		try (OutputStream output = Files.newOutputStream(mappingPropertiesPath)) {
			mappingProperties.store(output, "Mapping between generated code identifier and java package name was written.");
		}
	}

	private boolean handleOutputPath() throws IOException {
		outputPath = Paths.get(destPath);
		String errorFormat = "For {} code generation the out-path parameter must point to a {}.";
		if (programmingLanguage == ProgrammingLanguage.JAVASCRIPT) {
			if (outputPath.toFile().isDirectory()) {
				LOGGER.error(errorFormat, "JAVASCRIPT", "file");
				return false;
			}
			Files.createDirectories(outputPath.toAbsolutePath().getParent());
		}
		if (programmingLanguage == ProgrammingLanguage.JAVA) {
			if (outputPath.toFile().isFile()) {
				LOGGER.error(errorFormat, "JAVA", "directory");
				return false;
			}
			Files.createDirectories(outputPath);
		}
		return true;
	}

	private boolean handleDocumentModelPath() throws IOException {
		final Path pathToDocumentModel = Paths.get(documentModelPath);
		if (!pathToDocumentModel.toFile().exists()) {
			LOGGER.error("The path to the document model does not point to a file. Path: {}", documentModelPath);
			return false;
		}
		final IDocumentModelSerializer serializer = new DocumentModelSerializerImpl();
		final Path absolutePath = pathToDocumentModel.toAbsolutePath();
		try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(absolutePath), StandardCharsets.UTF_8)) {
			documentModel = serializer.deserialize(reader);
		}

		if (containsIncludesImports()) {
			if (StringUtils.isBlank(workspacePath) && resolver == null) {
				LOGGER.error("The given document model contains includes/imports and the path to the " +
						"workspace was not set. Path: {}", documentModelPath);
				return false;
			}
			final IUnexpandedModelResolver currentResolver = resolver == null
					? DirectoryBasedUnexpandedModelResolver.ofDir(Paths.get(workspacePath).toAbsolutePath().toString())
					: resolver;
			documentModel = CombinationModelService.expand(documentModel, currentResolver,
							CombinationModelExpandParams.builder().build())
					.orElseThrow(() -> new RuntimeException("Expansion failed for " + documentModelPath));
		}
		return true;
	}

	private void handleMappingPropertiesPath() throws IOException {
		if (propsPath == null) {
			mappingProperties = null;
			return;
		}
		mappingPropertiesPath = Paths.get(propsPath).toAbsolutePath();
		mappingProperties = new Properties();
		if (!mappingPropertiesPath.toFile().exists()) {
			Files.createDirectories(mappingPropertiesPath.getParent());
			Files.createFile(mappingPropertiesPath);
			return;
		}
		try (Reader input = new InputStreamReader(Files.newInputStream(mappingPropertiesPath), StandardCharsets.UTF_8)) {
			mappingProperties.load(input);
		}
	}

	private boolean containsIncludesImports() {
		List<ModelReference> modelReferences = documentModel.getHeader().getModelReferences();
		if (modelReferences == null) {
			return false;
		}
		return modelReferences.stream()
				.filter(mr -> DocumentModel.MODEL_TYPE_DOCUMENT.equals(mr.getModelType()))
				.anyMatch(mr -> SUPPORTED_MODEL_REFS.contains(mr.getPurpose()));
	}

	private void createCodegenConfig() {
		codegenConfig = new IValidationCodeGeneratorConfig() {

			@Override
			public @NonNull ProgrammingLanguage getProgrammingLanguage() {
				return programmingLanguage;
			}

			@Override
			public Optional<String> getPackageName() {
				return Optional.ofNullable(packageName);
			}

			@Override
			public @NonNull JsCodeGenConfig getJsCodeGenConfig() {
				return computeJsCodeGenCofig();
			}
		};
	}

	private JsCodeGenConfig computeJsCodeGenCofig() {
		if (wrapJsCodeInFunction) {
			return JsCodeGenConfig.WRAPPED_IN_FUNCTION;
		} else if (wrapJsCodeInFunctionExtended) {
			return JsCodeGenConfig.WRAPPED_IN_FUNCTION_EXTENDED;
		} else {
			return JsCodeGenConfig.STANDARD;
		}
	}
}
