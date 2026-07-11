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
package com.mgmtp.a12.kernel.md.facade.internal.cli;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.md.combination.a12internal.CMWrapper;
import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationException;
import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationModelService;
import com.mgmtp.a12.kernel.md.combination.a12internal.DMLike;
import com.mgmtp.a12.kernel.md.combination.a12internal.DMWrapper;
import com.mgmtp.a12.kernel.md.combination.a12internal.DirectoryBasedUnexpandedModelResolver;
import com.mgmtp.a12.kernel.md.combination.a12internal.IUnexpandedModelResolver;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.internal.expansioninfo.ExpansionInfoUtils;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views.MM_CombinationModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.Experimental;

import lombok.NonNull;

/**
 * Batch generator for document model expansion. The model references related to the referenced (transitive) includes and/or imports
 * are removed from the expanded document model.
 */
@Experimental
public class BatchDocumentModelExpander {
	public static final int EXIT_ERROR = -1;
	public static final int EXIT_OK = 0;
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchDocumentModelExpander.class);
	private static final Options OPTIONS;

	static {
		OPTIONS = new Options();
	}

	@FunctionalInterface
	protected interface ErrorHandler {
		/**
		 * @param message
		 * 		a mandatory message string
		 * @param e
		 * 		an optional exception
		 * @return the error code of the cli
		 */
		int apply(@NonNull String message, Exception e);
	}

	public static void main(final String[] args) {
		// For test reasons, we do only System.exit in main.
		// We test only #expandModelsWithArgs, so don't modify this method.
		System.exit(expandModelsWithArgs(args));
	}

	public static int expandModelsWithArgs(final String[] args) {
		Options helpOnlyOptions = new Options();
		helpOnlyOptions.addOption("h", "help", false, "Display help information");

		CommandLineParser parser = new DefaultParser();
		try { // first try to check if --help was passed and not complain about mandatory options in that case
			CommandLine helpCmd = parser.parse(helpOnlyOptions, args);
			if (helpCmd.hasOption("help")) {
				printHelp();
				return EXIT_OK;
			}
		} catch (ParseException e) {
			LOGGER.error("Error while parsing cli arguments", e);
			return EXIT_ERROR;
		}

		ErrorHandler errorHandler = (String errorMessage, Exception e) -> {
			LOGGER.error(errorMessage, e);
			if (e != null) {
				printHelp("%s: %s".formatted(errorMessage, e.getMessage()));
			} else {
				printHelp(errorMessage);
			}
			return EXIT_ERROR;
		};

		return execute(args, errorHandler);
	}

	protected static int execute(final String[] args,
			final ErrorHandler errorHandler) {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(OPTIONS, args);
		} catch (ParseException e) {
			String errorMessage = "unrecognized command line arguments: " + e.getMessage();
			return errorHandler.apply(errorMessage, null);
		}

		String[] providedPositionalArgs = cmd.getArgs();

		if (providedPositionalArgs.length < 3) {
			String errorMessage = "Please provide a path and at least a pair of <infile> <outfile>";
			return errorHandler.apply(errorMessage, null);
		}

		if ((providedPositionalArgs.length - 1) % 2 != 0) {
			String errorMessage = "For each infile, an outfile must be given! The first parameter is the workspace directory.";
			return errorHandler.apply(errorMessage, null);
		}

		Path workspacePath = Paths.get(args[0]);
		List<? extends Pair<String, String>> inOutPairs = IntStream.iterate(1, i -> i + 2).limit(providedPositionalArgs.length / 2)
				.mapToObj(i -> new ImmutablePair<>(providedPositionalArgs[i], providedPositionalArgs[i + 1]))
				.toList();

		return expandModels(workspacePath, inOutPairs, errorHandler);
	}

	protected static int expandModels(Path workspacePath, List<? extends Pair<String, String>> inOutPairs,
			ErrorHandler errorHandler) {
		if (!workspacePath.toFile().exists() || !workspacePath.toFile().isDirectory()) {
			String errorMessage = "The path to the workspace does not exist or does not point to a directory. Path: " + workspacePath;
			return errorHandler.apply(errorMessage, null);
		}

		LOGGER.debug("Using working directory {}", workspacePath);

		IUnexpandedModelResolver resolver = DirectoryBasedUnexpandedModelResolver.ofDir(workspacePath.toString());
		for (Pair<String, String> inOutPair : inOutPairs) {
			int result = expandModel(resolver, inOutPair, errorHandler);
			if (result != 0) {
				return result;
			}
		}

		return EXIT_OK;
	}

	private static void printHelp() {
		printHelp("");
	}

	private static void printHelp(String header) {
		String[][] positionalArguments = {
				{ "<workspacePath>", "path to the directory in wich the included/imported document models can be found." },
				{ "<modelId>",
						"the id of the corresponding document model which will be expanded (filename without .json extension)." },
				{ "<outfile>", "absolute path to the file to which the expanded document model will be written." }
		};
		StringBuilder text = new StringBuilder();
		text.append(String.format("Usage: %s %s {%s %s}+%n", BatchDocumentModelExpander.class.getSimpleName(),
				positionalArguments[0][0], positionalArguments[1][0], positionalArguments[2][0]));
		text.append("Where:").append(System.lineSeparator());
		for (String[] arg : positionalArguments) {
			text.append(String.format("  %-15s %s%n", arg[0], arg[1]));
		}
		System.err.println(header);
		System.out.println(text);
	}

	protected static int expandModel(
			final IUnexpandedModelResolver resolver,
			final Pair<String, String> inOutPair,
			final ErrorHandler errorHandler) {
		String modelId = inOutPair.getLeft();
		Path outFile = Path.of(inOutPair.getRight());

		LOGGER.debug("====================");
		LOGGER.debug("Loading the model {}", modelId);
		DMLike resolvedModel;
		try {
			resolvedModel = resolver.resolve(modelId);
		} catch (CombinationException e) {
			return errorHandler.apply("Resolver error", e);
		}

		LOGGER.debug("Trying to expand the model {}", modelId);

		List<RankedNotification> notifications = new ArrayList<>();
		ExpansionInfo[] expansionInfo = { null };
		CombinationModelService.CombinationModelExpandParams expandParams = CombinationModelService.CombinationModelExpandParams.builder()
				.locale(Locale.US).notificationReceiver(notifications::add)
				.a12Internal_expansionInfoReceiver(ei -> expansionInfo[0] = ei)
				.build();

		Optional<IDocumentModel> expandedModel = switch (resolvedModel) {
			case DMWrapper(IDocumentModel dm) -> CombinationModelService.expand(dm, resolver, expandParams);
			case CMWrapper(MM_CombinationModel_1 cm) -> CombinationModelService.expand(cm, resolver, expandParams);
		};

		if (expandedModel.isEmpty()) {
			Supplier<String> errorMessage = () -> String.format("Errors during expansion: %n%s",
					String.join(System.lineSeparator(), notifications.stream().map(RankedNotification::getMessage).toList())
			);
			return errorHandler.apply(errorMessage.get(), null);
		}

		final Path outAbsolutePath = outFile.toAbsolutePath();
		try {
			Files.createDirectories(outAbsolutePath.getParent());
		} catch (IOException e) {
			return errorHandler.apply("The path " + outAbsolutePath.getParent() + " to the output file could not be created", e);
		}

		IDocumentModel documentModel = expandedModel.get();
		try (Writer writer = Files.newBufferedWriter(outAbsolutePath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.CREATE)) {
			final IDocumentModelSerializer serializer = new DocumentModelSerializerImpl();
			serializer.serialize(documentModel, writer, problem -> {
			});
		} catch (IOException e) {
			return errorHandler.apply("The file could not be written to " + outFile.toAbsolutePath(), e);
		}

		String baseName = FilenameUtils.getBaseName(outAbsolutePath.getFileName().toString());
		Path expansionInfoPath = outAbsolutePath.resolveSibling(baseName + "_ei.json");
		try (Writer writer = Files.newBufferedWriter(expansionInfoPath, StandardCharsets.UTF_8,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.CREATE)) {
			ExpansionInfoUtils.serializeEI(writer, expansionInfo[0]);
		} catch (IOException e) {
			return errorHandler.apply("The expansionInfo file could not be written to " + expansionInfoPath.toAbsolutePath(), e);
		}

		LOGGER.info("Successfully expanded {} to {} ({}).", modelId, outFile.toAbsolutePath(), expansionInfoPath);
		if (!notifications.isEmpty()) {
			Supplier<String> messageString = () -> String.join(System.lineSeparator(),
					notifications.stream().map(RankedNotification::getMessage).toList());
			LOGGER.info("Warnings during expansion: {}{}",
					System.lineSeparator(),
					messageString.get()
			);
		}

		return EXIT_OK;
	}
}
