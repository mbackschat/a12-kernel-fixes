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
package com.mgmtp.a12.kernel.md.model.internal.services.join;

import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DMWithExpansionInfo;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.addition.AdditiveModelJoiner;
import com.mgmtp.a12.kernel.md.model.internal.services.join.decoration.DecorationFieldModelJoiner;
import com.mgmtp.a12.kernel.md.model.internal.services.join.decoration.DecorationGroupModelJoiner;
import com.mgmtp.a12.kernel.md.model.internal.services.join.item.DocumentModelJoiningItem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.selection.SelectionModelV2Wrapper;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;

import lombok.NonNull;

/**
 * Helper class to execute the join of {@link DocumentModel}s for additive or decoration models. This class is not immutable since
 * {@link JoiningProblemReporter} and ResultModel can be changed during runtime.
 */
public class DocumentModelJoinExecutor {
	private final DocumentModel referenceModelMutatedInPlaceToResultModel;
	private final Optional<ExpansionInfo> referenceModelExpansionInfo;
	private final DocumentModelJoiningItem documentModelJoiningItem;
	private final Locale locale;
	private final JoiningProblemReporter problemReporter;
	private final int stepNumber;

	public DocumentModelJoinExecutor(
			int stepNumber,
			DMWithExpansionInfo referenceModelMutatedInPlaceToResultModel,
			DocumentModelJoiningItem documentModelJoiningItem,
			Locale locale,
			JoiningProblemReporter problemReporter
	) {
		this.stepNumber = stepNumber;
		this.referenceModelMutatedInPlaceToResultModel = referenceModelMutatedInPlaceToResultModel.dm();
		this.referenceModelExpansionInfo = referenceModelMutatedInPlaceToResultModel.ei();
		this.locale = locale;
		this.problemReporter = problemReporter;
		this.documentModelJoiningItem = documentModelJoiningItem;
	}

	public DMWithExpansionInfo join(SelectionModelV2Wrapper selectionModelWrapper) {
		Optional<ExpansionInfo> expansionInfo = executeJoin(selectionModelWrapper);
		return DMWithExpansionInfo.of(problemReporter.hasCriticalErrorOccurred() ? null :
						referenceModelMutatedInPlaceToResultModel,
				expansionInfo);
	}

	private @NonNull Optional<ExpansionInfo> executeJoin(SelectionModelV2Wrapper selectionModelWrapper) {
		//We copy the document model to join as we ensure that the original joinItem will never be manipulated
		DocumentModel dmToJoin = DocumentModelCopyService.copy(documentModelJoiningItem.getDocumentModel());
		// Default selection includes all application data fields and fields not contained in appendix groups
		selectionModelWrapper = selectionModelWrapper != null ?
				selectionModelWrapper :
				SelectionModelV2Wrapper.getDefault(JoiningType.SELECTION_DECORATION);
		JoiningType joiningType = documentModelJoiningItem.getJoiningType();

		DMWithExpansionInfo refDmWithEI = DMWithExpansionInfo.of(
				referenceModelMutatedInPlaceToResultModel, referenceModelExpansionInfo);
		DMWithExpansionInfo dmToJoinWithEI = DMWithExpansionInfo.of(dmToJoin, documentModelJoiningItem.getExpansionInfo());
		Optional<ExpansionInfo> expansionInfo = switch (joiningType) {
			case ADDITIVE_MODEL:
				AdditiveModelJoiner additiveModelJoiner = new AdditiveModelJoiner(
						refDmWithEI,
						dmToJoinWithEI,
						stepNumber,
						locale,
						problemReporter);
				yield additiveModelJoiner.addElements();
			case DECORATION_MODEL_FIELD:
				DecorationFieldModelJoiner decorationFieldModelJoiner = new DecorationFieldModelJoiner(
						refDmWithEI,
						dmToJoinWithEI, stepNumber,
						selectionModelWrapper, locale, problemReporter);
				yield decorationFieldModelJoiner.decorateElements();
			case DECORATION_MODEL_GROUP:
				DecorationGroupModelJoiner decorationGroupModelJoiner = new DecorationGroupModelJoiner(
						refDmWithEI,
						dmToJoinWithEI, stepNumber,
						selectionModelWrapper, locale, problemReporter);
				yield decorationGroupModelJoiner.decorateElements();
			default:
				throw new IllegalArgumentException(
						String.format("Unknown joining type '%s' for document model join.", joiningType));
		};

		// update references in new rules and computations
		// this can only be done after all groups and fields in the result model have been determined
		DocumentModelUtils.resolveAndUpdateReferences(referenceModelMutatedInPlaceToResultModel,
				p -> {
				} // ignore reference-resolve errors/warnings here: we do not know the overall context and therefore
				// cannot decide if they should be warnings or errors -> it seems simplest to require the caller to use the consistency check if necessary
		);

		return expansionInfo;
	}
}
