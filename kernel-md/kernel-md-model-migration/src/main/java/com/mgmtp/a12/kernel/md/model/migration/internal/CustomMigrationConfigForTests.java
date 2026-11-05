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

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationCode;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;

import lombok.Builder;
import lombok.NonNull;

/**
 * allow for convenient partial implementations of ICustomMigrationConfig in testing using builder pattern
 * only used in tests (a productive ICustomMigrationConfig may be implemented by users)
 */
@Builder
public record CustomMigrationConfigForTests(
		Function<String,Optional<ICustomMigrationCode>> determineCustomCodeBefore,
		Function<String, Optional<ICustomMigrationCode>> determineCustomCodeAfter,
		Function<String, TimeZone> determineTimeZone,
		Function<String, Locale> determineConditionLanguage,
		BiFunction<String, String, String> determineRequirednessMode,
		@NonNull Map<String, String> typeDefUuidsByUniqueIdForTest

) implements ICustomMigrationConfig {
	public static CustomMigrationConfigForTestsBuilder builder() {
		return new CustomMigrationConfigForTestsBuilder().typeDefUuidsByUniqueIdForTest(Map.of());
	}

	@Override public Optional<ICustomMigrationCode> getCustomCodeBefore(@NonNull String targetVersion) {
		return  Optional.ofNullable(determineCustomCodeBefore)
				.orElse( ICustomMigrationConfig.super::getCustomCodeBefore)
				.apply(targetVersion);
	}

	@Override public Optional<ICustomMigrationCode> getCustomCodeAfter(@NonNull String targetVersion) {
		return Optional.ofNullable(determineCustomCodeAfter)
				.orElse(ICustomMigrationConfig.super::getCustomCodeAfter )
				.apply(targetVersion);
	}

	@Override public @NonNull TimeZone getTimezone(@NonNull String modelId) {
		return Optional.ofNullable(determineTimeZone).orElse(ICustomMigrationConfig.super::getTimezone)
				.apply(modelId);
	}

	@Override public @NonNull Locale getConditionLanguage(@NonNull String modelId) {
		return Optional.ofNullable( determineConditionLanguage)
				.orElse( ICustomMigrationConfig.super::getConditionLanguage)
				.apply(modelId);
	}

	@Override public @NonNull String getRequirednessMode(@NonNull String modelId, @NonNull String fieldPath) {
		return Optional.ofNullable(determineRequirednessMode).orElse(ICustomMigrationConfig.super::getRequirednessMode)
				.apply(modelId, fieldPath);
	}

}
