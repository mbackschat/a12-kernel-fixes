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

package com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services;

import java.util.Optional;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.md.structuralmapping.internal.generator.SMMGeneratorForMigrationConfigImpl;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

@OnlyForUsage
public interface SMMGeneratorConfig {
	/**
	 * The id to give to the generated SMM. Must not be empty.
	 */
	@NonNull String idForGeneratedSMM();

	/**
	 * Specifies the name of the annotation for excluding target fields from the generation of field mappings. All fields where
	 * either the field itself or one of its ancestors is annotated with this annotation are not considered as potential target
	 * fields. Must not be empty when provided.
	 */
	@NonNull Optional<String> targetAnnotationForNoDataTransfer();

	/**
	 * Specifies the suffix to use for "overrides". If a source field name ends with this suffix, it is treated as an override for
	 * the field whose name is equal to the part up to the suffix. In the generator, the override field will be used instead of the
	 * overridden field. Must not be empty when provided.
	 */
	@NonNull Optional<String> sourceFieldNameSuffixForOverride();

	/**
	 * When set, the generator will consider fields whose short name matches the pattern as having a unique short name in the
	 * source or target model. When generating the field mappings, such fields will be matched by their short name instead of their
	 * full name.
	 */
	@NonNull Optional<Pattern> patternForUniqueShortNames();

	static @NonNull SMMGeneratorConfig of(
			@NonNull String idForGeneratedSMM,
			String targetAnnotationForNoDataTransfer,
			String sourceFieldNameSuffixForOverride
	) {
		return of(
				idForGeneratedSMM,
				targetAnnotationForNoDataTransfer,
				sourceFieldNameSuffixForOverride,
				null);
	}

	static @NonNull SMMGeneratorConfig of(
			@NonNull String idForGeneratedSMM,
			String targetAnnotationForNoDataTransfer,
			String sourceFieldNameSuffixForOverride,
			Pattern patternForUniqueShortNames
	) {
		return new SMMGeneratorForMigrationConfigImpl(
				idForGeneratedSMM,
				Optional.ofNullable(targetAnnotationForNoDataTransfer),
				Optional.ofNullable(sourceFieldNameSuffixForOverride),
				Optional.ofNullable(patternForUniqueShortNames));
	}

}
