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

package com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;

import lombok.NonNull;

/***
 * allows more fine-grained control over {@link GroupInstanceV2#withMerged(GroupInstanceV2, IDocumentMergeConfig)} and
 * {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2#withMerged(GroupInstanceV2, IDocumentMergeConfig)}.
 * <p>
 * A minimal implementation only needs to define {@link #shouldUseCustomMergeForRepetitions(DocumentPointer)} and can be implemented
 * e.g. by just a lambda function. In that case, the repetitions that get selected by shouldUseCustomMergeForRepetitions will
 * be concatenated, rather than merged point-wise recursively.
 * <p>
 * Additional customization is possible by overriding {@link #customMergeForRepetitions(DocumentPointer, RepetitionsV2, RepetitionsV2)}
 * (The default implementation performs concatenation, as mentioned above).
 */
@FunctionalInterface
public interface IDocumentMergeConfig {

	/**
	 * Will be called with a document pointer for every subGroup of the other group or document.
	 * The pointers that get passed to this method will always have a last repetition index of 0 (wildcard), because the pointer
	 * identifies the full repetitions instance, not a particular repetition group.
	 */
	boolean shouldUseCustomMergeForRepetitions(@NonNull DocumentPointer pointer);

	/**
	 * Allows to apply a custom update for the repetitions that were selected by
	 * {@link #shouldUseCustomMergeForRepetitions(DocumentPointer)}.
	 * The default implementation concatenates the repetitions from the other document or group to the repetitions from the document
	 * or group on which
	 * {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2#withMerged(GroupInstanceV2, IDocumentMergeConfig)}
	 * was called.
	 */
	default @NonNull Collection<UpdateAction> customMergeForRepetitions(
			@NonNull DocumentPointer ptr, @NonNull RepetitionsV2 repCurrent, @NonNull RepetitionsV2 repOther) {
		// Notes about this implementation of repetitions concatenation:
		// it would be nice to have putAllRepetitions, but that might not be worth the impl effort for now
		// RepetitionsV2 concatenatedRep = concatRepetitions(g1.groupAllRepetitions(pointerRelativeToBase), rep);
		// updates.add(UpdateAction.putAllRepetitions(pointerRelativeToBase, concatenatedRep));
		// instead, we just put each repetition individually, for now:
		int preexistingSize = repCurrent.size();
		List<UpdateAction> updates = new ArrayList<>();
		for (int iOneBased = 1; iOneBased <= repOther.size(); iOneBased++) {
			updates.add(UpdateAction.putGroup(
					ptr.withLastRepetitionReplaced(preexistingSize + iOneBased),
					repOther.get(iOneBased)));
		}
		return updates;
	}
}
