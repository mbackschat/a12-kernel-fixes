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

package com.mgmtp.a12.kernel.mapping.rt.api;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

@OnlyForUsage
public interface MappingResult {
	/**
	 * @return true if the mapping was successful, false otherwise.
	 * 		<p>
	 * 		Related but different: if some or all <em>source</em> data is deselected in the source-selection step, the result is
	 * 		considered neither failed not incomplete, as this deselection is considered to be regular modeled behavior.
	 * 		<p>
	 * 		Note: currently, {@link #success()} always returns true, because in case of precomputation errors, the mapping will
	 * 		nonetheless continue (but some mapped values might be omitted).
	 * 		There is currently no flag that explicitly identifies <em>incomplete</em> mapping
	 * 		results that are caused by unsuccessful precomputations. If this information is needed please contact the kernel team. (It
	 * 		can be <em>approximated</em> by checking {@code targetMessages} and {@code precomputationConsistencyMessages} for errors)
	 * 		(but the presence of errors does not guarantee that some values have indeed been omitted in the mapping, as
	 * 		invalidly-precomputed values that are <em>not</em> operands of further precomputations will result in reported errors,
	 * 		but will not themselves be omitted).
	 */
	// NOTE the javadoc of this method should be kept in sync with the javadoc of
	// com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.MappedData#success
	boolean success();

	/**
	 * The concrete fields that were transferred from source documents to the target document.
	 * <p>
	 * The mapped values are not validated against the target model and may be invalid.
	 */
	@NonNull List<MappedField> mappedFields();

	/**
	 * The entities which should be removed from the target document.
	 */
	@NonNull Set<DocumentPointer> removedEntities();

	/**
	 * {@link SourceEligibilityResult}s for all {@link SourceDocument}s; the keys are
	 * the {@link SourceDocument#sourceInstanceId()}.
	 */
	@NonNull Map<String, SourceEligibilityResult> sourceEligibilityResults();

	/**
	 * Error, warning or info messages that concern fields in the target document.
	 * e.g. if a field from the target is an input to a precomputation and is formally invalid,
	 * or if the structural mapping reports an error or warning for a target field.
	 */
	// see the javadoc of IDataTransferService#getTransferredData for details about what messages
	// are reported to targetResult
	@NonNull List<IMessage> targetMessages();

	/**
	 * Error, warning or info messages from the precomputation step. If errors occur here, they
	 * may indicate a modeling or integration problem.
	 */
	// see the javadoc of IDataTransferService#getTransferredData for details about what messages
	// are reported to precomputationConsistencyResult
	@NonNull List<IMessage> precomputationConsistencyMessages();

	/**
	 * @param document
	 * 		typically the document that was passed to the mapping as the target document
	 * @return the updated document with the transfer result applied
	 */
	default @NonNull DocumentV2 applyTo(@NonNull DocumentV2 document) {
		// sort the removed entities in reverse order (higher repetition first), since after the removal of a group the higher
		// repetitions are shifted downward to not leave a gap
		DocumentV2 resultDoc = document;
		List<DocumentPointer> removedEntitiesSorted = removedEntities().stream().sorted(Comparator.reverseOrder()).toList();
		for (DocumentPointer removedEntity : removedEntitiesSorted) {
			resultDoc = resultDoc.withGroupRemoved(removedEntity);
		}
		List<UpdateAction> updateActions = mappedFields().stream()
				.map(mappedField -> UpdateAction.putFieldValue(mappedField.targetField(), mappedField.value()))
				.toList();
		return resultDoc.withBatchUpdates(updateActions);
	}

	static @NonNull MappingResult of(
			boolean success,
			@NonNull List<MappedField> mappedFields,
			@NonNull Set<DocumentPointer> removedEntities,
			@NonNull Map<String, SourceEligibilityResult> sourceEligibilityResults,
			@NonNull List<IMessage> targetMessages,
			@NonNull List<IMessage> precomputationConsistencyMessages
	) {
		return new MappingResultImpl(success,
				mappedFields, removedEntities,
				sourceEligibilityResults,
				targetMessages, precomputationConsistencyMessages);
	}
}
