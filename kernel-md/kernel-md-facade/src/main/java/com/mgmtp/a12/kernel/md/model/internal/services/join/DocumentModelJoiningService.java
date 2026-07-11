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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.combination.a12internal.SelectionModelService;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionNode;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.LocalCoordinates;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DMWithExpansionInfo;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.utils.VersionChecker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ProblemReporter2Consumer;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCode;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCodeUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.JoinProblem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.item.DocumentModelJoiningItem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.item.JoiningItem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.item.SelectionJoiningItem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.selection.SelectionModelV2Wrapper;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelJoinUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.internal.service.Identifier;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.kernel.mmtypings.mm_selectionmodel_2.views.MM_SelectionModel_2;
import com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen_mm_selectionmodel__0032.Factory_codegen_mm_selectionmodel__0032;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Service class for handling joins of document models.
 */
@OnlyForUsage
public class DocumentModelJoiningService {
	private DocumentModelJoiningService() {
	}

	/**
	 * Executes the join on the given root model for all {@link JoiningItem}s according to their {@link JoiningType}.
	 * If an error occurs, it is added to the given {@link IProblemReporter} but a partial {@link DocumentModel} might also be
	 * returned.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>The result document model is not validated. This can be done afterward using the method
	 * {@link IDocumentModelService#checkConsistency}</li>
	 * <li>In the models to be joined, fields should NOT be referenced via a short name, if the field is not contained in the rule group.
	 * If fields outside the rule group are referenced by shortname, rules may get invalid or change the semantics</li>
	 * <li>The document models handed to this method have to be expanded. A valid behavior for document models with includes (not expanded) is
	 * not ensured</li>
	 * <li>All provided {@link DocumentModel}s must specify the same major model version number. The result model contains the
	 * highest version number of all provided models. {@link DocumentModel}s that were generated from ExtMP must also specify
	 * the same variant.</li>
	 * <li>Change of index fields - An index field from the {@link DocumentModelJoiningItem} is not transferred to the resulting
	 * {@link DocumentModel} as such, but only as a normal field if it does not yet exist in the respective group.</li>
	 * <li>Multilingualism - It is not checked whether all provided models support the same
	 * locale(s). This can lead to inconsistencies in the result model.</li>
	 * <li>Typedefs are merged from all {@link DocumentModelJoiningItem}.</li>
	 * <li>Document Uniqueness Criteria are joined from the root model and all additive {@link DocumentModelJoiningItem}s.
	 * Criteria are added as-is. Document Uniqueness Criteria of included/decoration/selection/typedefinition DMs are ignored.</li>
	 * <li>Annotations of the document models or its elements are merged. The merge is done as follows:
	 * <ul>
	 *     <li>if the root model contains no annotations, all annotations of the joined model are added</li>
	 *     <li>if the root model contains annotations, all additional annotations of joined model are added. Additional annotations means the annotation key does not exist in the root model</li>
	 *     <li>if the joined model contains annotations with annotation keys that exists in the root model, the annotation key and value must be equal. Otherwise, an error is reported</li>
	 * </ul>
	 * <p>
	 * <b>Note</b>: {@link com.mgmtp.a12.kernel.md.model.internal.services.join.util.UnjoinableAnnotations} is used to exclude some annotations from the joining items that cannot be joined.
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param rootModel
	 * 		the root model
	 * @param joiningItems
	 * 		the {@link JoiningItem}s
	 * @param locale
	 * 		to define the language of the error messages
	 * @param problemReporter
	 *        {@link IProblemReporter} receiving the occurred problems
	 * @return {@link Optional} containing the joined {@link DocumentModel} or {@link Optional#empty()} if critical problems occurred
	 */
	public static Optional<DocumentModel> join(DocumentModel rootModel, List<JoiningItem> joiningItems,
			Locale locale, IProblemReporter problemReporter) {
		return join(DMWithExpansionInfo.of(rootModel), joiningItems, locale, new JoiningProblemReporter(problemReporter), null);
	}

	public static Optional<DocumentModel> join(DocumentModel rootModel, List<JoiningItem> joiningItems,
			Locale locale, JoiningProblemReporter problemReporter) {
		return join(DMWithExpansionInfo.of(rootModel), joiningItems, locale, problemReporter, null);
	}

	/**
	 * like
	 * {@link #join(DocumentModel, DocumentModel, Locale, IProblemReporter, IProblemReporter,
	 * DocumentModelJoiningItem.ElementOrderForAddition)},
	 * with {@link Consumer<ExpansionInfo>}.
	 * <p>
	 * If no {@code expansionInfoConsumer} is provided, the ExpansionInfo will not be collected.
	 */
	public static Optional<DocumentModel> join(DMWithExpansionInfo rootModel, List<JoiningItem> joiningItems,
			Locale locale, JoiningProblemReporter problemReporter, Consumer<ExpansionInfo> expansionInfoConsumer) {
		return joinInternal(rootModel, joiningItems, locale, expansionInfoConsumer, problemReporter);
	}

	private static Optional<DocumentModel> joinInternal(DMWithExpansionInfo rootModelWithExpansionInfo,
			List<JoiningItem> joiningItems, Locale locale, Consumer<ExpansionInfo> expansionInfoConsumer,
			JoiningProblemReporter joiningProblemReporter) {
		DocumentModel rootModel = rootModelWithExpansionInfo.dm();
		// starts with the ExpansionInfo from the root model
		DMWithExpansionInfo currentResult = DMWithExpansionInfo.of(DocumentModelCopyService.copy(rootModel),
				rootModelWithExpansionInfo.ei());

		updateModelVersionIfNecessary(currentResult.dm(), joiningItems, locale, joiningProblemReporter);
		if (joiningProblemReporter.hasCriticalErrorOccurred()) {
			return Optional.empty();
		}

		SelectionModelV2Wrapper selectionModelWrapper = null;

		// as the selection and decoration are two separate joiningItems, we have to "combine" them to get the correct step number
		int cleanedStepNumber = 0;
		for (int i = 0; i < joiningItems.size(); i++) {
			JoiningItem joiningItem = joiningItems.get(i);
			JoiningType joiningType = joiningItem.getJoiningType();

			switch (joiningItem) {
				case SelectionJoiningItem selectionJoiningItem -> {
					validateSelectionModel(selectionJoiningItem, locale, joiningProblemReporter);
					if (joiningProblemReporter.hasCriticalErrorOccurred()) {
						return Optional.empty();
					}

					validateSelectionPathsAgainstReferenceDm(selectionJoiningItem, currentResult.dm(), locale,
							joiningProblemReporter);

					selectionModelWrapper = new SelectionModelV2Wrapper(joiningType,
							MM_SelectionModel_2._viewOfCheckDmId(selectionJoiningItem.getSelection()));

					if (JoiningType.SELECTION.equals(joiningType)) {
						DocumentModelSelectionExecutor documentModelSelectionExecutor = new DocumentModelSelectionExecutor(
								currentResult, selectionModelWrapper, joiningProblemReporter);

						DMWithExpansionInfo selectionResult = documentModelSelectionExecutor.select(cleanedStepNumber);
						currentResult = selectionResult;
					} else {
						if (currentResult.ei().isPresent()) {
							@SuppressWarnings("java:S3655") // optional without isPresent check
							ExpansionNode node = DocumentModelSelectionExecutor.asModelItself(
									rootModel.getHeader().getId(),
									selectionModelWrapper.getSelectionModelId(),
									cleanedStepNumber,
									currentResult.ei()
							).get();
							currentResult = new DMWithExpansionInfo(currentResult.dm(), currentResult.ei()
									.map(ei -> ei.mergeWithNestedOrigins(Map.of(LocalCoordinates.ModelItself.of(), node))));
						}

						// when we have a decoration (has to have a selection item and a decoration item) we have 2 items for a
						// single combination step, so if it is no "standalone" selection, we have to correct the stepNumber
						cleanedStepNumber -= 1;
					}
				}
				case DocumentModelJoiningItem documentModelJoiningItem -> {
					DocumentModelJoinExecutor documentModelJoinExecutor = new DocumentModelJoinExecutor(
							cleanedStepNumber,
							currentResult,
							documentModelJoiningItem,
							locale,
							joiningProblemReporter);

					currentResult = documentModelJoinExecutor.join(selectionModelWrapper);
					selectionModelWrapper = null;
				}
				default -> throw new IllegalArgumentException("Unknown JoiningItem " + joiningItem.getClass().getName());
			}

			if (currentResult.dm() == null || joiningProblemReporter.hasCriticalErrorOccurred()) {
				return Optional.empty();
			}

			cleanedStepNumber += 1;
		}

		if (expansionInfoConsumer != null && currentResult.ei().isPresent()) {
			expansionInfoConsumer.accept(currentResult.ei().get());
		}
		return Optional.of(currentResult.dm());
	}

	private static void updateModelVersionIfNecessary(DocumentModel rootModel,
			List<JoiningItem> documentModelJoiningItems,
			Locale locale, JoiningProblemReporter problemReporter) {
		String rootModelVersion = rootModel.getHeader().getModelVersion();
		String resultModelVersion = rootModelVersion;

		for (DocumentModelJoiningItem documentModelJoiningItem : documentModelJoiningItems.stream()
				.filter(DocumentModelJoiningItem.class::isInstance).map(DocumentModelJoiningItem.class::cast).toList()) {
			Header joinHeader = documentModelJoiningItem.getDocumentModel().getHeader();
			if (!VersionChecker.isMajorVersionEqual(rootModelVersion, joinHeader.getModelVersion())) {
				String additionalModelId = DocumentModelJoinUtil.getModelString(documentModelJoiningItem.getDocumentModel());
				problemReporter.reportCriticalProblem(JoinProblem.createError(documentModelJoiningItem,
						ErrorCodeUtil.getMessageWithCode(ErrorCode.WrongModelVersion, locale,
								joinHeader.getModelVersion(), additionalModelId, rootModelVersion)));
			} else {
				resultModelVersion = VersionChecker.getMaxVersion(rootModelVersion, joinHeader.getModelVersion());
			}
		}

		if (problemReporter.hasCriticalErrorOccurred()) {
			return;
		}

		if (!resultModelVersion.equals(rootModelVersion)) {
			Header headerWithNewVersion = HeaderBuilderUtil
					.toBuilder(rootModel.getHeader())
					.withModelVersion(resultModelVersion)
					.build();
			rootModel.setHeader(headerWithNewVersion);
		}
	}

	private static void validateSelectionPathsAgainstReferenceDm(SelectionJoiningItem selectionJoiningItem,
			DocumentModel referenceDm, Locale locale, JoiningProblemReporter problemReporter) {
		MM_SelectionModel_2 selectionModel = MM_SelectionModel_2._viewOf(selectionJoiningItem.getSelection());

		SelectionModelService.checkConsistencyAgainstReferenceDm(selectionModel,
				new DocumentModelService().convertToExternal(referenceDm), locale, new ProblemReporter2Consumer(problemReporter));
	}

	private static void validateSelectionModel(SelectionJoiningItem selectionJoiningItem, Locale locale,
			JoiningProblemReporter problemReporter) {
		DocumentV2 doc = selectionJoiningItem.getSelection();

		IDocumentValidationResult validationResult = validateSelection(doc, locale);

		if (!validationResult.noErrorOccurred()) {
			validationResult.getMessages().stream()
					.map(m -> new JoinProblem(Severity.valueOf(m.getSeverity().name()), m.getErrorFieldPointer(),
							ErrorCodeUtil.getMessageWithCode(ErrorCode.InvalidSelection, locale, m.getErrorText()),
							new Date()))
					.forEach(problemReporter::reportCriticalProblem);
		}
	}

	private static IDocumentValidationResult validateSelection(@NonNull DocumentV2 selectionDocument, @NonNull Locale locale) {
		return LowKernelApiUtils.callValidator(
				LowKernelApiUtils.ValidationMethod.FULL_VALIDATION,
				Factory_codegen_mm_selectionmodel__0032.getInstance().getValidator(),
				Factory_codegen_mm_selectionmodel__0032.getInstance().getMetaObject(),
				selectionDocument,
				null,
				DocumentProcessingConfig.builder(locale).build(),
				id -> new Identifier(id.getName(), id.getIndices()).toString());
	}
}
