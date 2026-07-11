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

package com.mgmtp.a12.kernel.md.model.migration.internal.config;

import java.util.List;

import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.AddConditionLanguageStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.AddRequirednessConfigStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.AddTimezoneStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.ChangeAnnotationStructureStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.ConvertEnumValuesMapToListStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.CopyAnnotationsForCDMStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.CorrectCustomFieldTypeStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.FillGapsInEnumDisplayValuesStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.LocalizedTextToListStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.NoMigrationNecessaryStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.ReferenceDmsByIdStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.ReferenceViaPathStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.RemoveAnnotationFromFieldtype;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.RemoveModelAliasSuffix;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.RemoveSavedAtAnnotationStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.RemoveUniquenessCriteriaBackwardStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.RemoveUnspecifiedTypeStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.RemoveVariantPropertiesStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.RestructureIncludeConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.RestructureIncludeConfigBackward;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.RewriteToReferenzjahrStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.SimplifyCharsetStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.json.step.typeDefs.UseUuidInTypeDefsStep;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

public class MigratorConfig {

	private MigratorConfig() {

	}

	public static List<InternalStep> internalSteps() {
		// @formatter:off
		return List.of(
				InternalStep.builder("24.0.0", "24.1.0")
						.forwardMigration(NoMigrationNecessaryStep::execute)
						.build(),
				InternalStep.builder("24.1.0", "24.2.0")
						.forwardMigration(NoMigrationNecessaryStep::execute)
						.build(),
				InternalStep.builder("24.2.0", "25.0.0")
						.description("A12K-1571: Convert enum values from map to list representation")
						.forwardMigration(ConvertEnumValuesMapToListStep::execute)
						.build(),
				InternalStep.builder("25.0.0", "25.1.0")
						.description("A12K-1571: Add timeZone field to model config")
						.newVersionIsInternal(true)
						.forwardMigration(AddTimezoneStep::execute)
						.build(),
				InternalStep.builder("25.1.0", "25.2.0")
						.description("A12K-1571: Remove deprecated properties from CustomFieldType nodes")
						.newVersionIsInternal(true)
						.forwardMigration(CorrectCustomFieldTypeStep::execute)
						.build(),
				InternalStep.builder("25.2.0", "26.0.0")
						.description("A12K-1571: Convert localized text from map to list representation")
						.forwardMigration(LocalizedTextToListStep::execute)
						.build(),
				InternalStep.builder("26.0.0", "26.1.0")
						.description("A12K-2040: Bug fix")
						.forwardMigration(NoMigrationNecessaryStep::execute)
						.build(),
				InternalStep.builder("26.1.0", "26.1.1")
						.description("A12K-2186: Remove erroneous .json suffix from model aliases in included model references")
						.forwardMigration(RemoveModelAliasSuffix::execute)
						.build(),
				InternalStep.builder("26.1.1", "26.2.0")
						.description("A12K-1778: Remove the savedAt annotation from model header")
						.newVersionIsInternal(true)
						.forwardMigration(RemoveSavedAtAnnotationStep::execute)
						.build(),
				InternalStep.builder("26.2.0", "26.2.1")
						.description("A12K-2096: Add conditionLanguage field to model config")
						.newVersionIsInternal(true)
						.forwardMigration(AddConditionLanguageStep::execute)
						.build(),
				InternalStep.builder("26.2.1", "27.0.0")
						.description("A12K-2117: Convert model references from name-based to UUID-based IDs")
						.newVersionIsInternal(true)
						.forwardMigration(ReferenceDmsByIdStep::execute)
						.build(),
				InternalStep.builder("27.0.0", "27.0.1")
						.description("A12K-2111: Fill missing locale-specific display values in enum fields using internal value as fallback")
						.newVersionIsInternal(true)
						.forwardMigration(FillGapsInEnumDisplayValuesStep::execute)
						.build(),
				InternalStep.builder("27.0.1", "27.0.2")
						.description("A12K-2116: Replace typeDef IDs with UUIDs and update all references")
						.forwardMigration(UseUuidInTypeDefsStep::execute)
						.build(),
				InternalStep.builder("27.0.2", "27.1.0")
						.description("A12K-2388: Extend A12 export for computations with tolerance range operations")
						.forwardMigration(NoMigrationNecessaryStep::execute)
						.build(),
				InternalStep.builder("27.1.0", "28.0.0")
						.description("A12K-2741: Convert annotations from map structure to array of objects with name and value")
						.newVersionIsInternal(true)
						.forwardMigration(ChangeAnnotationStructureStep::execute)
						.build(),
				InternalStep.builder("28.0.0", "28.0.1")
						.description("A12K-2711: Convert field references from ID-based to path-based references")
						.newVersionIsInternal(true)
						.forwardMigration(ReferenceViaPathStep::execute)
						.build(),
				InternalStep.builder("28.0.1", "28.0.2")
						.description("A12K-2729: Replace required property on groups and fields with requirednessConfig")
						.forwardMigration(AddRequirednessConfigStep::execute)
						.build(),
				InternalStep.builder("28.0.2", "28.1.0")
						.description("A12K-2985: Add hint lists for string fields")
						.forwardMigration(NoMigrationNecessaryStep::execute)
						.build(),
				InternalStep.builder("28.1.0", "28.1.1")
						.description("A12K-3107: Simplify restricted character set definitions")
						.newVersionIsInternal(true)
						.forwardMigration(SimplifyCharsetStep::execute)
						.build(),
				InternalStep.builder("28.1.1", "28.2.0")
						.description("A12K-3078: Add min- and max-length to custom field type")
						.forwardMigration(NoMigrationNecessaryStep::execute)
						.build(),
				InternalStep.builder("28.2.0", "28.3.0")
						.description("A12K-3234: Increment DM version")
						.forwardMigration(NoMigrationNecessaryStep::execute)
						.build(),
				InternalStep.builder("28.3.0", "28.4.0")
						.description("A12K-3770: For models with German condition language, rewrite base year references to Referenzjahr in conditions, computations, and error texts")
						.forwardMigration(RewriteToReferenzjahrStep::execute)
						.build(),
				InternalStep.builder("28.4.0", "28.5.0")
						.description("A12K-4118: Add new string field 'packet' to DocumentModelInfo")
						.forwardMigration(NoMigrationNecessaryStep::execute)
						.backwardMigration( (model,logger,cache,config)
								-> ModelJsonUtil.tryRemoveElementFromObject(  model, "/content/modelInfo/packet"))
						.build(),
				InternalStep.builder("28.5.0", "28.6.0")
						.description("A12K-3995: Add list of Document Uniqueness Criteria to DocumentModelContent")
						.forwardMigration(NoMigrationNecessaryStep::execute)
						.backwardMigration(RemoveUniquenessCriteriaBackwardStep::execute)
						.build(),
				InternalStep.builder("28.6.0", "29.0.0")
						.description("A12K-4102: Move modelAlias, excludeRules, excludeComputations and includeLevel to includeConfig child-object")
						.forwardMigration(RestructureIncludeConfig::execute)
						.backwardMigration(RestructureIncludeConfigBackward::execute)
						.build(),
				InternalStep.builder("29.0.0", "29.1.0")
						.description("A12K-3986: Remove variantFilter and variantErrorMessages properties from Rules, Fields, and Computations")
						.forwardMigration(RemoveVariantPropertiesStep::execute)
						.backwardMigration(NoMigrationNecessaryStep::execute)
						.build(),
				InternalStep.builder("29.1.0", "29.2.0")
						.description("A12K-3981: Remove UnspecifiedType field type, replace with StringType and _take_reference_configuration annotation")
						.forwardMigration(RemoveUnspecifiedTypeStep::execute)
						.backwardMigration(NoMigrationNecessaryStep::execute)
						.build(),
				InternalStep.builder("29.2.0", "29.3.0")
						.description("[A12K-4187] - Migration for CDM query root Document Models. If the document model header contains an " +
								"annotation named `cdm.queryRoot`, all model references with purpose `include` are copied with purpose `composedData` and " +
								"appended to the existing model references.")
						.forwardMigration(CopyAnnotationsForCDMStep::execute)
						.backwardMigration(NoMigrationNecessaryStep::execute)
						.build(),
				InternalStep.builder("29.3.0", "29.4.0")
						.description("[A12K-4250] - Migration to remove fieldtype annotations from Document Models.")
						.forwardMigration(RemoveAnnotationFromFieldtype::execute)
						.backwardMigration(NoMigrationNecessaryStep::execute)
						.build()
		);
		// @formatter:on
	}
}
