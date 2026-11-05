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
package com.mgmtp.a12.kernel.md.structuralmapping.internal.consistency;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter @Accessors(fluent = true)
@RequiredArgsConstructor
public enum NotificationCode {
	// Fragments
	FRAGMENT_GROUP("fragment.group"),
	FRAGMENT_FIELD("fragment.field"),
	FRAGMENT_RULE("fragment.rule"),
	FRAGMENT_COMPUTATION("fragment.computation"),
	FRAGMENT_SOURCE("fragment.source"),
	FRAGMENT_TARGET("fragment.target"),
	FRAGMENT_UNKNOWN("fragment.unknown"),
	FRAGMENT_FM("fragment.fm"),
	FRAGMENT_RS("fragment.rs"),
	FRAGMENT_RS_UNKNOWN("fragment.rs.unknown"),
	FRAGMENT_RS_SLICE("fragment.rs.slice"),
	FRAGMENT_RS_FOLD("fragment.rs.fold"),

	// Serialization
	DESERIALIZATION_PROBLEM("DESERIALIZATION_PROBLEM"),

	// Consistency
	ENTITY_USED_AS_FIELD_AND_GROUP("ENTITY_USED_AS_FIELD_AND_GROUP"),
	GROUP_CLEAR_ON_FILL_CONFLICT("GROUP_CLEAR_ON_FILL_CONFLICT"),
	MAPPING_BLOCK_NO_FIELD_MAPPINGS("MAPPING_BLOCK_NO_FIELD_MAPPINGS"),
	FIELD_MAPPING_WILL_CAUSE_COLLISION("FIELD_MAPPING_WILL_CAUSE_COLLISION"),
	FIELD_MAPPING_TARGETS_SLICE_TARGET_FIELD("FIELD_MAPPING_TARGETS_SLICE_TARGET_FIELD"),
	RESOLUTION_STRATEGY_NO_APPLICABLE_FIELD_MAPPING("RESOLUTION_STRATEGY_NO_APPLICABLE_FIELD_MAPPING"),
	RESOLUTION_STRATEGY_ALWAYS_APPLIES_TOGETHER("RESOLUTION_STRATEGY_ALWAYS_APPLIES_TOGETHER"),
	SLICE_SOURCE_FIELD_NOT_DESCENDANT_OF_SOURCE_GROUP("SLICE_SOURCE_FIELD_NOT_DESCENDANT_OF_SOURCE_GROUP"),
	SLICE_TARGET_FIELD_NOT_DESCENDANT_OF_TARGET_GROUP("SLICE_TARGET_FIELD_NOT_DESCENDANT_OF_TARGET_GROUP"),

	// Compatibility
	ENTITY_NOT_IN_DM("ENTITY_NOT_IN_DM"),
	ENTITY_NOT_CORRECT_TYPE_IN_DM("ENTITY_NOT_CORRECT_TYPE_IN_DM"),
	FIELD_MAPPING_NO_APPLICABLE_RESOLUTION_STRATEGIES("FIELD_MAPPING_NO_APPLICABLE_RESOLUTION_STRATEGIES"),
	FIELD_MAPPING_INCOMPATIBLE_TYPES("FIELD_MAPPING_INCOMPATIBLE_TYPES"),
	FIELD_MAPPING_UNUSED_SOURCE_IRG("FIELD_MAPPING_UNUSED_SOURCE_IRG"),
	RESOLUTION_STRATEGY_TARGETS_NON_REPEATABLE_GROUP("RESOLUTION_STRATEGY_TARGETS_NON_REPEATABLE_GROUP"),
	RESOLUTION_STRATEGY_CROSS("RESOLUTION_STRATEGY_CROSS"),
	SLICE_TARGET_FIELD_REPEATABLY_REFERENCED("SLICE_TARGET_FIELD_REPEATABLY_REFERENCED"),
	SLICE_SOURCE_FIELD_REPEATABLY_REFERENCED("SLICE_SOURCE_FIELD_REPEATABLY_REFERENCED"),
	SLICE_INCOMPATIBLE_TYPES("SLICE_INCOMPATIBLE_TYPES"),

	// Generation for ExtMP
	AMBIGUOUS_SLICE("AMBIGUOUS_SLICE"),
	AMBIGUOUS_FOLD("AMBIGUOUS_FOLD"),
	AMBIGUOUS_GROUP_CLEAR("AMBIGUOUS_GROUP_CLEAR");

	@NonNull private final String key;
}
