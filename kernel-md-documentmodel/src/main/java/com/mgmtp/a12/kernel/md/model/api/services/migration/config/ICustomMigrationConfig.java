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
package com.mgmtp.a12.kernel.md.model.api.services.migration.config;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import com.mgmtp.a12.kernel.md.model.api.services.migration.IDocumentModelMigrator;
import com.mgmtp.a12.kernel.md.utils.UseOnlyAfterCloseConsultation;

import lombok.NonNull;

/**
 * This interface can be implemented to customize migration.
 */
public interface ICustomMigrationConfig {

	Set<Locale> SUPPORTED_CONDITION_LANGUAGES = Set.of(Locale.GERMANY, Locale.US);
	String DEFAULT_REQUIREDNESS_MODE = "absoluteOrRelativeToNextRepAncestor";
	String REQUIREDNESS_MODE_RELATIVE_TO_PARENT = "relativeToParent";

	/**
	 * Before the migration steps belonging to the given {@code targetVersion} are executed, the
	 * returned custom code is executed.
	 *
	 * @param targetVersion
	 * 		a document model version from
	 *        {@link IDocumentModelMigrator#getSupportedTargetVersions()}. Never
	 *        {@code null} or empty.
	 * @return an instance of the project-specific class for custom migration or
	 *        {@code Optional#empty()} if there is no custom code needed for this
	 *        {@code targetVersion}.
	 * 		Migration to the desired {@code targetVersion} generally requires migration across
	 * 		multiple {@code targetVersion}s. It is recommended to return specific implementations
	 * 		of {@link ICustomMigrationCode} for a {@code targetVersion}. This ensures that the
	 * 		custom code is only executed when migrating to the specified {@code targetVersion}.
	 * 		Warning: If this is not taken into account, if the custom code is migrated across
	 * 		multiple {@code targetVersion}, it will be executed for each {@code targetVersion}.
	 */
	@UseOnlyAfterCloseConsultation
	default Optional<ICustomMigrationCode> getCustomCodeBefore(@NonNull String targetVersion) {
		return Optional.empty();
	}

	/**
	 * After the migration steps belonging to the given {@code targetVersion} are executed, the
	 * returned custom code is executed.
	 *
	 * @param targetVersion
	 * 		a document model version from
	 *        {@link IDocumentModelMigrator#getSupportedTargetVersions()}. Never
	 *        {@code null} or empty.
	 * @return an instance of the project-specific class for custom migration or
	 *        {@code Optional#empty()} if there is no custom code needed for this
	 *        {@code targetVersion}.
	 * 		Migration to the desired {@code targetVersion} generally requires migration across
	 * 		multiple {@code targetVersion}s. It is recommended to return specific implementations
	 * 		of {@link ICustomMigrationCode} for a {@code targetVersion}. This ensures that the
	 * 		custom code is only executed when migrating to the specified {@code targetVersion}.
	 * 		Warning: If this is not taken into account, if the custom code is migrated across
	 * 		multiple {@code targetVersion}, it will be executed for each {@code targetVersion}.
	 */
	@UseOnlyAfterCloseConsultation
	default Optional<ICustomMigrationCode> getCustomCodeAfter(@NonNull String targetVersion) {
		return Optional.empty();
	}

	/**
	 * Migration specifies a time zone in the DocumentModel. This method is used to determine the
	 * time zone to be set.
	 *
	 * @param modelId
	 * 		If, for example, a model-specific time zone is to be returned, the modelId can
	 * 		be checked. Never {@code null} or empty.
	 * @return the desired {@link TimeZone}. Never {@code null}.
	 */
	default @NonNull TimeZone getTimezone(@NonNull String modelId) {
		return TimeZone.getTimeZone("UTC");
	}

	/**
	 * Migration specifies the language used in the rule condition text (comments are not affected) and also for the reserved
	 * terms when defining parameters in the rule error message, e.g. ".value", in the DocumentModel.<br/>
	 * <b>Note: </b>Currently, only {@link Locale#US} and {@link Locale#GERMANY} are supported. If the DocumentModel
	 * specifies a language for the condition and error texts that is not supported, {@link Locale#US} is set by default.<br/>
	 * This method is used to determine the condition language to be set.
	 *
	 * @param modelId
	 * 		If, for example, a model-specific condition language is to be returned, the modelId can
	 * 		be checked. Never {@code null} or empty.
	 * @return the desired {@link Locale}. Never {@code null}.
	 */
	default @NonNull Locale getConditionLanguage(@NonNull String modelId) {
		return Locale.US;
	}

	/**
	 * Migration to specify a specific mode for the requiredness configuration of a required field.
	 *
	 * @param modelId
	 * 		If, for example, a model-specific requiredness mode is to be returned, the modelId can be checked.
	 * 		Never {@code null} or empty.
	 * @param fieldPath
	 * 		path of the required field which is being migrated. If, for example, a specific requiredness mode should be set to certain
	 * 		fields, the fieldPath can be checked. Never {@code null} or empty.
	 * @return {@value DEFAULT_REQUIREDNESS_MODE} (default in A12) or {@value REQUIREDNESS_MODE_RELATIVE_TO_PARENT}.
	 */
	default @NonNull String getRequirednessMode(@NonNull String modelId, @NonNull String fieldPath) {
		return DEFAULT_REQUIREDNESS_MODE;
	}
}
