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
package com.mgmtp.a12.kernel.tool.migration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.core.utils.internal.TopologicalSortHelper;
import com.mgmtp.a12.kernel.md.model.api.services.migration.exceptions.DocumentModelMigratorException;
import com.mgmtp.a12.kernel.md.model.migration.internal.DocumentModelMigratorImpl;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationInfo;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationResult;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.DmsTopologicalSorter;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ReferencedDmsChecker;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.ObjectMapperProvider;

/**
 * Command line based document model migration tool.
 */
public class MigratorCli {

	private static final int SUCCESS = 0;
	private static final int FAILED = 1;
	private static final Options OPTIONS = new Options();
	private static final Logger LOGGER = LoggerFactory.getLogger(MigratorCli.class);

	static {
		OPTIONS.addOption("b", "backup", false, "create a backup before migrating a model");
		OPTIONS.addOption("n", "no-validation", false, "(not recommended) do not validate the document model against the "
				+ "corresponding xml-schema after migration, this option is not applied for the migration in json format");
		OPTIONS.addOption("d", "debug", false, "enable debug mode");
		OPTIONS.addOption("t", "test", false, "do not write migrated model to file, but log the output " +
				"(default: print to console)");
		OPTIONS.addOption("o", "optional-boolean-migration", false, "also migrate optional boolean steps " +
				"(RequiredBooleanMigrationStep.java)");
		OPTIONS.addOption("skipUniqueIdCheck", "intended for test models only: if set, the check that all models must have " +
				"unique ids won't be executed");
		OPTIONS.addOption("h", "help", false, "display this help");
	}

	/**
	 * The entry point of application.
	 *
	 * @param args
	 * 		the input arguments
	 */
	public static void main(final String[] args) {
		final int migrationResult = new MigratorCli().migrate(args);
		if (migrationResult != 0) {
			// only exit if the process was not successful
			System.exit(migrationResult);
		}
	}

	private final Map<String, String> customTypeDefUuidsByUniqueId;

	public MigratorCli() {
		this(new HashMap<>());
	}

	/**
	 * @param customTypeDefUuidsByUniqueId
	 * 		Intended to be used in tests to get deterministic results.
	 */
	public MigratorCli(Map<String, String> customTypeDefUuidsByUniqueId) {
		this.customTypeDefUuidsByUniqueId = customTypeDefUuidsByUniqueId;
	}

	private final Map<Path, JsonNode> dmsToMigrate = new HashMap<>();

	/**
	 * Conduct the actual migration.
	 *
	 * @param args
	 * 		the input arguments
	 */
	public int migrate(final String[] args) {

		final CommandLineParser parser = new DefaultParser();
		final CommandLine commandLine;
		try {
			commandLine = parser.parse(OPTIONS, args, false);
		} catch (final ParseException e) {
			showHelp(e.getMessage());
			return FAILED;
		}

		if (commandLine.hasOption('h')) {
			showHelp();
			return SUCCESS;
		}

		if (commandLine.getArgs().length != 1) {
			showHelp("Please specify a file or directory!");
			return FAILED;
		}

		final Path inputPath;
		try {
			inputPath = Paths.get(commandLine.getArgs()[0]);
		} catch (final InvalidPathException e) {
			showHelp("Invalid path: " + commandLine.getArgs()[0]);
			return FAILED;
		}

		if (!inputPath.toFile().isFile() && !inputPath.toFile().isDirectory()) {
			showHelp("Not a file or directory: " + commandLine.getArgs()[0]);
			return FAILED;
		}

		if (!checkPreconditionsAndInitDmsToMigrate(commandLine, inputPath)) {
			// error was already logged
			return FAILED;
		}

		if (!copyOriginalIfRequired(commandLine)) {
			// error was already logged
			return FAILED;
		}

		LOGGER.info("\n********************************************************************************");
		LOGGER.info("Starting execution of all migration steps for all document models in json format");
		LOGGER.info("********************************************************************************\n");

		return migrateAllDocumentModels(commandLine);
	}

	private boolean checkPreconditionsAndInitDmsToMigrate(final CommandLine commandLine, final Path inputPath) {
		// ids of all kind of models, not only document models
		// the uniqueness of the id should be checked over all kind of models -> the Kernel migration tool is one of the first tools
		// (if not the first one at all) which is executed to migrate A12 models and that's why we do it
		Map<String, Set<String>> modelPathsById = new HashMap<>();
		try (Stream<Path> walk = Files.walk(inputPath)) {
			walk.filter(f -> f.toFile().isFile())
					.filter(path -> path.getFileName().toString().endsWith(ModelJsonUtil.JSON_EXTENSION))
					.forEach(path -> {
						Optional<JsonNode> modelOpt = ModelJsonUtil.parseValidA12Model(path);
						if (modelOpt.isEmpty()) {
							LOGGER.warn("WARNING: Json at " + inputPath + " does not appear to be a valid model, skipping it...");
							return;
						}
						JsonNode model = modelOpt.get();
						String id = ModelJsonUtil.extractModelId(model);
						if (ModelJsonUtil.isValidDocumentModel(model)) {
							// we collect here the document models only
							dmsToMigrate.put(path, model);
						}
						modelPathsById.computeIfAbsent(id, key -> new HashSet<>()).add(path.toString());
					});
		} catch (IOException e) {
			LOGGER.error("Failed to traverse directory '{}'.\nReason: {}", inputPath, e.getMessage());
			return false;
		}
		String modelsWithSameIdStr = modelPathsById.entrySet().stream()
				.filter(e -> e.getValue().size() > 1)
				.map(e -> e.getKey() + "\n\t" + e.getValue().stream().collect(Collectors.joining("\n\t")))
				.collect(Collectors.joining("\n"));
		if (!commandLine.hasOption("skipUniqueIdCheck") && !StringUtils.isEmpty(modelsWithSameIdStr)) {
			LOGGER.error(
					"""
							Following models have the same id:
							%s
							
							Please ensure following constraints:
								1. Model ids must be unique among all model types (document model, form model, overview model, etc).
								2. Referenced document models in other document models must be resolvable and the model id must be equal the file name.
							""".formatted(modelsWithSameIdStr));
			return false;
		}
		// in SME, it is checked that the file name and the model id are equal for all models
		// we check this for the document models which are referenced by another document model in order to have an easier
		// implementation of the corresponding migration step
		ReferencedDmsChecker checker = new ReferencedDmsChecker(dmsToMigrate);
		if (checker.areRdmsWithDifferentIdAndFileNamePresent()) {
			LOGGER.error(
					"Following document models are referenced by another document model and have different file name and model id" +
							". Please make sure that the file name matches the model id:\n\t"
							+ checker.getRdmsWithDifferentIdAndFileNameAsString());
			return false;
		}
		return true;
	}

	private void printProblems(final Map<Path, Integer> resultMap) {
		LOGGER.error("");
		resultMap.entrySet()
				.stream()
				.filter(e -> e.getValue().equals(FAILED))
				.forEach(e -> LOGGER.error("Migration of '{}' failed!", e.getKey()));
	}

	private boolean copyOriginalIfRequired(final CommandLine commandLine) {
		boolean result = true;
		if (commandLine.hasOption('b')) {
			for (Path dmPath : dmsToMigrate.keySet()) {
				try {
					Files.copy(dmPath, Paths.get(dmPath + ".bak"));
				} catch (final IOException ioex) {
					LOGGER.error("Could not create backup file: {}", ioex.getMessage());
					result = false;
				}
			}
		}
		return result;
	}

	private int migrateAllDocumentModels(final CommandLine commandLine) {
		Map<String, List<Path>> docModelPathsByIdPreserveAmbiguous = new HashMap<>();
		dmsToMigrate.entrySet().stream()
				.forEach(entry -> {
					Path path = entry.getKey();
					JsonNode dm = entry.getValue();
					String docModelId = ModelJsonUtil.extractModelId(dm);
					docModelPathsByIdPreserveAmbiguous.computeIfAbsent(docModelId, k -> new ArrayList<>()).add(path);
				});
		DmsTopologicalSorter sorter = DmsTopologicalSorter.ofDMsWithAmbiguousIds(dmsToMigrate.values());
		TopologicalSortHelper.TopologicalSortResult<String> result = sorter.sort();
		if (!result.isSuccessful()) {
			LOGGER.error(
					"Following document models create a cycle: " + result.getCycle().stream().collect(Collectors.joining(" -> ")));
			var ambiguousIds = docModelPathsByIdPreserveAmbiguous.entrySet().stream()
					.filter(e -> e.getValue().size() > 1)
					.map(Map.Entry::getKey).toList();
			if (ambiguousIds.size() > 0) {
				LOGGER.info("This cycle may be related to document models with non-unique ids: " + ambiguousIds);
			}
			return FAILED;
		}

		final Map<Path, Integer> resultMap = new HashMap<>();
		MigrationCache migrationCache = new MigrationCache(customTypeDefUuidsByUniqueId);
		for (String docModelId : result.getSort()) {
			for (Path path : docModelPathsByIdPreserveAmbiguous.get(docModelId)) {
				JsonNode docModel = dmsToMigrate.get(path);
				resultMap.put(path, migrateDocumentModel(commandLine, path, docModel, migrationCache));
			}
		}

		if ((resultMap.values().stream().anyMatch(i1 -> i1 == FAILED))) {
			printProblems(resultMap);
			return FAILED;
		}
		return SUCCESS;
	}

	private int migrateDocumentModel(final CommandLine commandLine, final Path inputPath, final JsonNode jsonDm,
			MigrationCache migrationCache) {
		MigrationResult migrationResult;

		try {
			migrationResult = new DocumentModelMigratorImpl(migrationCache, null).migrateToLatestVersion(jsonDm);
		} catch (DocumentModelMigratorException e) {
			LOGGER.error(e.getMessage());
			return FAILED;
		}

		switch (migrationResult.getType()) {
			case SUCCESSFUL:
				printSuccess(commandLine, inputPath, migrationResult, "");
				if (commandLine.hasOption('t')) {
					try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
						ObjectMapperProvider.createPreconfiguredObjectMapper().writeValue(outputStream, jsonDm);
						LOGGER.info(outputStream.toString());
					} catch (final IOException ioex) {
						LOGGER.error("Unable to write model to log: {}", ioex.getMessage());
						return FAILED;
					}
				} else {
					try (OutputStream outputStream = Files.newOutputStream(inputPath)) {
						ObjectMapperProvider.createPreconfiguredObjectMapper().writeValue(outputStream, jsonDm);
					} catch (final IOException ioex) {
						LOGGER.error("Unable to write model to disk: {}", ioex.getMessage());
						return FAILED;
					}
				}
				return SUCCESS;
			case FAILED:
				printFailed(inputPath, migrationResult);
				return FAILED;
			case NOT_REQUIRED:
				LOGGER.info("Migration for '{}' not required.", inputPath);
				break;
		}
		return SUCCESS;
	}

	private void printSuccess(final CommandLine commandLine, final Path inputPath, final MigrationResult migrationResult,
			final String additionalInfo) {
		LOGGER.info("Successfully migrated {} file '{}' from version {} to {}.",
				additionalInfo,
				inputPath,
				migrationResult.getSourceModelVersion(),
				migrationResult.getTargetModelVersion());
		if (commandLine.hasOption('d')) {
			printMigrationInfos(migrationResult.getInfos());
		}
	}

	private void printFailed(final Path inputPath, final MigrationResult migrationResult) {
		LOGGER.error("Migration for '{}' failed!", inputPath);
		printMigrationErrors(migrationResult.getErrors());
		printMigrationInfos(migrationResult.getInfos());
	}

	private static void showHelp() {
		showHelp(null);
	}

	private static void showHelp(final String error) {
		final TextHelpAppendable helpAppendable = new TextHelpAppendable(System.out);
		helpAppendable.setMaxWidth(120);
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
			try {
				final String jarName = new File(
						MigratorCli.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName();

				helpFormatter.printHelp(
						"java -jar " + jarName + " [OPTIONS] DOC_MODEL_FILE|DIRECTORY",
						header.toString(),
						OPTIONS,
						null,
						false
				);
			} catch (URISyntaxException e) {
				helpFormatter.printHelp(
						"java -jar migration.jar [OPTIONS] DOC_MODEL_FILE|DIRECTORY",
						header.toString(),
						OPTIONS,
						null,
						false
				);
			}
		} catch (IOException e) {
			// Should never happen with TextHelpAppendable(System.out)
			throw new RuntimeException(e);
		}
	}

	private static void printMigrationInfos(final Collection<MigrationInfo> infos) {
		if (!infos.isEmpty()) {
			LOGGER.info("Infos:");
			for (final MigrationInfo info : infos) {
				LOGGER.info("\t{}", info);
			}
		}
	}

	private static void printMigrationErrors(final Collection<MigrationError> errors) {
		if (!errors.isEmpty()) {
			LOGGER.error("Errors:");
			for (final MigrationError error : errors) {
				LOGGER.error("\t{}", error);
			}
		}
	}
}
