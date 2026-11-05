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

package com.mgmtp.a12.kernel.md.model.migration.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.model.api.services.migration.IDocumentModelMigrator;
import com.mgmtp.a12.kernel.md.model.api.services.migration.IDocumentModelMigratorResult;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationCode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.api.services.migration.exceptions.DocumentModelMigratorException;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.InternalStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigratorConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.DocumentModelMigratorResultImpl;
import com.mgmtp.a12.kernel.md.model.migration.internal.step.MigrationStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import lombok.NonNull;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

public class DocumentModelMigratorImpl implements IDocumentModelMigrator {
	private final List<String> supportedTargetVersions;
	private final String firstSupportedSourceVersion;
	private final MigrationConfig mergedConfig;

	public static DocumentModelMigratorImpl createForwardMigrator(ICustomMigrationConfig customConfig) {
		return new DocumentModelMigratorImpl(MigratorConfig.internalSteps(), customConfig, true);
	}

	public static DocumentModelMigratorImpl createBackwardMigrator(ICustomMigrationConfig customConfig) {
		return new DocumentModelMigratorImpl(MigratorConfig.internalSteps(), customConfig, false);
	}

	DocumentModelMigratorImpl(
			@NonNull List<InternalStep> steps,
			ICustomMigrationConfig customConfig,
			boolean isForward
	) {
		this.mergedConfig = MigrationConfig.create(steps, customConfig, isForward);
		List<MigrationStep> migrationSteps = mergedConfig.getMigrationSteps();
		List<String> targetVersions = migrationSteps.stream()
				.filter(m -> !m.getConfig().targetVersionIsInternal())
				.map(MigrationStep::getTargetModelVersion)
				.collect(Collectors.toList());//wa want modifiable list here

		var firstInList = mergedConfig.getMigrationSteps().getFirst();
		this.firstSupportedSourceVersion = firstInList.getSourceModelVersion();

		targetVersions.addFirst(firstSupportedSourceVersion);
		this.supportedTargetVersions = Collections.unmodifiableList(targetVersions);
	}

	@Override
	public @NonNull Collection<IDocumentModelMigratorResult> migrate(
			@NonNull Collection<String> docModelsToMigrate,
			String targetVersionNullable
	) {
		return List.copyOf(migrateInternal(docModelsToMigrate, targetVersionNullable));
	}

	public @NonNull List<DocumentModelMigratorResultImpl> migrateInternal(
			@NonNull Collection<String> inputStrings,
			String targetVersionNullable
	) {
		DmParseResult parseResult = parseDms(inputStrings);
		migrateParseableDms(parseResult.dmsToMigrate(), targetVersionNullable);
		return Stream.concat(
				parseResult.resultsForUnparseableDms().stream(),
				parseResult.dmsToMigrate().stream().map(DocumentModelMigratorImpl::extractMigrationResult)
		).toList();
	}

	public @NonNull Map<Path, DmToMigrate> migrateForCli(
			@NonNull Map<Path, JsonNode> dmsByPath
	) {
		Map<Path, DmToMigrate> dmsToMigrateByPath = Java8Utils.mapMapValues(dmsByPath,
				dmJsonNode -> DmToMigrate.create(dmJsonNode, null));
		migrateParseableDms(List.copyOf(dmsToMigrateByPath.values()), getEffectiveTargetVersion(null));
		return dmsToMigrateByPath;
	}

	private record DmParseResult(List<DmToMigrate> dmsToMigrate, List<DocumentModelMigratorResultImpl> resultsForUnparseableDms) {
	}

	private static @NonNull DmParseResult parseDms(@NonNull Collection<String> inputStrings) {
		List<DmToMigrate> dmsToMigrate = new ArrayList<>();
		List<DocumentModelMigratorResultImpl> resultsForUnparseableDms = new ArrayList<>();

		int i = 0;
		for (String inputString : inputStrings) {
			i++;
			JsonNode dmJsonNode;
			try {
				dmJsonNode = ModelJsonUtil.parseModel(inputString);
			} catch (JacksonException e) {
				resultsForUnparseableDms.add(DocumentModelMigratorResultImpl.failed("inputModel" + i, inputString,
						new DocumentModelMigratorException("Could not read JSON representation of DocumentModel.", e)));
				continue;
			}
			if (!ModelJsonUtil.isValidDocumentModel(dmJsonNode)) {
				resultsForUnparseableDms.add(DocumentModelMigratorResultImpl.failed("inputModel" + i, inputString,
						new DocumentModelMigratorException("The model does not seem to be a valid document model.")));
				continue;
			}
			dmsToMigrate.add(DmToMigrate.create(dmJsonNode, inputString));
		}
		return new DmParseResult(dmsToMigrate, resultsForUnparseableDms);
	}

	private void migrateParseableDms(@NonNull List<DmToMigrate> dmsToMigrate, String targetVersionNullable) {
		checkTargetVersionSupported(targetVersionNullable);
		String effectiveTargetVersion = getEffectiveTargetVersion(targetVersionNullable);
		List<MigrationStep> migrationSteps = getMigrationStepsUntilTargetVersion(effectiveTargetVersion);
		checkSourceVersionSupported(dmsToMigrate, effectiveTargetVersion, migrationSteps);
		for (MigrationStep migrationStep : migrationSteps) {
			List<DmToMigrate> dmsToMigrateInStep = dmsToMigrate.stream()
					.filter(dmToMigrate -> dmToMigrate.currentModelVersion().equals(migrationStep.getSourceModelVersion()))
					.filter(dmToMigrate -> !dmToMigrate.hasFailedMigration())
					.toList();
			executeMigrationStep(migrationStep, dmsToMigrateInStep);
		}
	}

	private void executeMigrationStep(MigrationStep migrationStep, List<DmToMigrate> dmsToMigrateInStep) {
		// Execute custom code before migration
		executeCustomCode(dmsToMigrateInStep, migrationStep.getConfig().customCodeBefore());

		// Migrate DM
		migrationStep.execute(dmsToMigrateInStep);

		// Update DM version
		dmsToMigrateInStep.forEach(dmToMigrate -> {
			dmToMigrate.currentModelVersion(migrationStep.getTargetModelVersion());
			ModelJsonUtil.setVersion(dmToMigrate.jsonNode(), migrationStep.getTargetModelVersion());
			if (dmToMigrate.migrationResult().getType().equals(MigrationResultType.NOT_REQUIRED)) {
				dmToMigrate.migrationResult().setType(MigrationResultType.SUCCESSFUL);
			}
		});

		// Execute custom code after migration
		executeCustomCode(dmsToMigrateInStep, migrationStep.getConfig().customCodeAfter());
	}

	private static void executeCustomCode(List<DmToMigrate> dmsToMigrate, ICustomMigrationCode customCode) {
		if (customCode != null) {
			dmsToMigrate.forEach(dmToMigrate -> customCode.execute(dmToMigrate.jsonNode()));
		}
	}

	private static DocumentModelMigratorResultImpl extractMigrationResult(DmToMigrate dmToMigrate) {
		MigrationResultType resultType = dmToMigrate.migrationResult().getType();
		return switch (resultType) {
			case SUCCESSFUL, NOT_REQUIRED -> DocumentModelMigratorResultImpl.successfulOrNotRequired(dmToMigrate.dmId(),
					dmToMigrate.originalDmString(), resultType, ModelJsonUtil.serializeJson(dmToMigrate.jsonNode()),
					dmToMigrate.migrationResult());
			case FAILED -> DocumentModelMigratorResultImpl.failed(dmToMigrate.dmId(), dmToMigrate.originalDmString(),
					new DocumentModelMigratorException(createErrorMessage(dmToMigrate.migrationResult())));
		};
	}

	private static String createErrorMessage(MigrationResult result) {
		StringBuilder sb = new StringBuilder("Migration failed!");
		if (!result.getErrors().isEmpty()) {
			sb.append("\nErrors:");
			for (final MigrationError error : result.getErrors()) {
				sb.append("\n" + error.toString());
			}
		}
		if (!result.getInfos().isEmpty()) {
			sb.append("\nInfos:");
			for (final MigrationInfo info : result.getInfos()) {
				sb.append("\n" + info);
			}
		}
		return sb.toString();
	}

	private String getEffectiveTargetVersion(String targetVersionNullable) {
		return StringUtils.isNotBlank(targetVersionNullable)
				? targetVersionNullable
				: mergedConfig.getMigrationSteps().getLast().getTargetModelVersion();
	}

	private void checkTargetVersionSupported(String targetVersion) {
		if (StringUtils.isNotBlank(targetVersion) && !supportedTargetVersions.contains(targetVersion)) {
			throw new IllegalArgumentException(
					String.format("Target version '%s' is not supported. Please use one of the following values: '%s'",
							targetVersion, String.join(", ", supportedTargetVersions)));
		}
	}

	private List<MigrationStep> getMigrationStepsUntilTargetVersion(@NonNull String targetVersion) {
		if (mergedConfig.getMigrationSteps().isEmpty()
				|| mergedConfig.getMigrationSteps().getFirst().getSourceModelVersion().equals(targetVersion)) {
			return List.of();
		}
		List<MigrationStep> stepsUntilTargetVersion = new ArrayList<>();
		for (MigrationStep migrationStep : mergedConfig.getMigrationSteps()) {
			stepsUntilTargetVersion.add(migrationStep);
			if (migrationStep.getTargetModelVersion().equals(targetVersion)) {
				break;
			}
		}
		return stepsUntilTargetVersion;
	}

	private void checkSourceVersionSupported(
			@NonNull List<DmToMigrate> dmsToMigrate,
			@NonNull String targetVersion,
			@NonNull List<MigrationStep> migrationSteps
	) {
		Set<String> supportedSourceVersions = Stream.concat(
				Stream.of(targetVersion),
				migrationSteps.stream().map(MigrationStep::getSourceModelVersion)
		).collect(Collectors.toSet());
		dmsToMigrate.forEach(dmToMigrate -> {
			if (!supportedSourceVersions.contains(dmToMigrate.sourceModelVersion())) {
				dmToMigrate.migrationResult().addError(new MigrationError(MigrationErrorType.UNSUPPORTED_SOURCE_MODEL_VERSION,
						"Source model version: '%s' could not be found in the given migration configuration".formatted(
								dmToMigrate.sourceModelVersion())));
			}
		});
	}

	@Override
	public @NonNull List<String> getSupportedTargetVersions() {
		return supportedTargetVersions;
	}

	@Override
	public String getFirstSupportedSourceVersion() {
		return firstSupportedSourceVersion;
	}
}
