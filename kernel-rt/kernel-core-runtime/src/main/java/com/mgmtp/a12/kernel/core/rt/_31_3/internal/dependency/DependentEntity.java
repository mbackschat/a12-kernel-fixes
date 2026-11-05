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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.dependency;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

import lombok.NonNull;

/**
 * A single entity that a trigger field hides. This is the computed output shape of the hidden-fields
 * calculation (before each entry is mapped to an {@link IIdentifier}); it has no serialized
 * counterpart in the dependency model, so it is the only record the runtime data model materializes.
 *
 * <p>A dependent is in one of two model-declared states, distinguished by {@link #hasSemanticIndex()}:
 * either it was declared without a semantic index, or it carries one. Turning the entry into an
 * {@link IIdentifier} happens later, and the state only affects the index encoding of the repeatable layers:
 * each repeatable layer below the trigger IRG becomes a wildcard ({@link IIdentifier#ALL_INDICES}), and a semantic index
 * additionally pins its own group's layer to the concrete repetition whose index field matches
 * {@link #semanticIndexValue()} (or to {@link IIdentifier#UNKNOWN} if none does). Repeatable layers
 * below the semantic index therefore stay wildcards.
 *
 * @param entityPath
 * 		the full path of the hidden field or group; never {@code null}.
 * @param semanticIndexValue
 * 		the model-declared index-field value when the dependent carries a semantic index, otherwise
 *        {@code null}.
 */
record DependentEntity(@NonNull String entityPath, String semanticIndexValue) {

	/** The dependent was declared without a semantic index: every repetition is hidden. */
	static DependentEntity ofAllRepetitions(@NonNull String entityPath) {
		return new DependentEntity(entityPath, null);
	}

	/**
	 * The dependent was declared with a semantic index; the concrete repetition is resolved later
	 * against the data, so the entry just carries the index-field value.
	 */
	static DependentEntity ofSemanticIndex(@NonNull String entityPath, @NonNull String semanticIndexValue) {
		return new DependentEntity(entityPath, semanticIndexValue);
	}

	boolean hasSemanticIndex() {
		return semanticIndexValue != null;
	}
}
