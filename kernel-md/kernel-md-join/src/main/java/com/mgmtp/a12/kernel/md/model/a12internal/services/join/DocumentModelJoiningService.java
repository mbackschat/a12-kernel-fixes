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
package com.mgmtp.a12.kernel.md.model.a12internal.services.join;

import static com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.DocumentModelJoiningItem.ElementOrderForAddition.ANCHOR_BASED_ORDERING;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.IAdditionOrigins;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.DocumentModelJoiningItem;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.JoiningItem;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.SelectionJoiningItem;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.utils.VersionChecker;
import com.mgmtp.a12.kernel.md.model.internal.services.join.DocumentModelJoinExecutor;
import com.mgmtp.a12.kernel.md.model.internal.services.join.DocumentModelSelectionExecutor;
import com.mgmtp.a12.kernel.md.model.internal.services.join.addition.DummyAdditionOrigins;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCode;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCodeUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.JoinProblem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.selection.SelectionModelV2Wrapper;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelJoinUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.internal.service.Identifier;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.kernel.mmtypings.mm_selectionmodel_2.views.MM_SelectionModel_2;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderFactory;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen_mm_selectionmodel__0032.Factory_codegen_mm_selectionmodel__0032;

import lombok.NonNull;

/**
 * Service class for handling joins of document models.
 * @deprecated use {@link com.mgmtp.a12.kernel.md.combination.a12internal.CombinationModelService} instead
 */
@Deprecated(since = "30.6.0")
@OnlyForUsage
public class DocumentModelJoiningService {

	public static final DocumentModelJoiningService INSTANCE = new DocumentModelJoiningService();

	/**
	 * @deprecated use {@link #INSTANCE} instead.
	 */
	@Deprecated(since = "30.2.0")
	public DocumentModelJoiningService() {
	}

	/**
	 * exactly like {@link #join(DocumentModel, List, Locale, IProblemReporter)},
	 * the lenient flag will be ignored.
	 *
	 * @deprecated use {@link #join(DocumentModel, List, Locale, IProblemReporter)} instead.
	 */
	@Deprecated(since = "30.6.0")
	public Optional<DocumentModel> join(DocumentModel rootModel, List<JoiningItem> joiningItems,
			boolean lenient, Locale locale, IProblemReporter problemReporter) {
		return join(rootModel, joiningItems, locale, problemReporter);
	}

	/**
	 * Executes the join on the given root model for all {@link JoiningItem}s according to their {@link JoiningType}.
	 * If an error occurs, it is added to the given {@link IProblemReporter} but a partial {@link DocumentModel} might also be returned.
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
	public Optional<DocumentModel> join(DocumentModel rootModel, List<JoiningItem> joiningItems,
			Locale locale, IProblemReporter problemReporter) {
		JoiningProblemReporter joiningProblemReporter = new JoiningProblemReporter(problemReporter);
		return joinInternal(rootModel, joiningItems, locale, new DummyAdditionOrigins(), joiningProblemReporter);
	}

	/**
	 * like
	 * {@link #join(DocumentModel, DocumentModel, Locale, IProblemReporter, IProblemReporter, IAdditionOrigins,
	 * DocumentModelJoiningItem.ElementOrderForAddition)},
	 * but with implicit {@link DocumentModelJoiningItem.ElementOrderForAddition#ANCHOR_BASED_ORDERING}
	 */
	public Optional<DocumentModel> join(
			DocumentModel rootModel,
			DocumentModel additiveModel,
			Locale locale,
			IProblemReporter problemReporter,
			IProblemReporter criticalIssues,
			IAdditionOrigins additionOrigins
	) {
		return join(rootModel, additiveModel, locale, problemReporter, criticalIssues, additionOrigins, ANCHOR_BASED_ORDERING);
	}

	/**
	 * Executes the join on the given root model for the given additive model, for more details see also
	 * {@link #join(DocumentModel, List, Locale, IProblemReporter)}. The main differences between the two methods are:
	 * <ul>
	 *     <li>A single additive model is joined to the root model.</li>
	 *     <li>In this method, there is a distinction between critical (see param {@code criticalIssues}) and non-critical errors (see param {@code problemReporter}).</li>
	 *     <li>The information regarding the origin of some aspects of the {@link DocumentModel} is gathered.</li>
	 * </ul>
	 *
	 * @param rootModel
	 * 		the root model
	 * @param additiveModel
	 * 		the additive model
	 * @param locale
	 * 		to define the language of the error messages
	 * @param problemReporter
	 * 		this {@link IProblemReporter} gathers all the notifications (errors, warnings, infos) which were found during the
	 * 		join-process. A non-empty result can be expected even if errors were found. Critical errors are not reported in this
	 *        {@link IProblemReporter}.
	 * @param criticalIssues
	 * 		this {@link IProblemReporter} gathers all the critical issues which don't allow the process to continue. If critical
	 * 		issues were found, the method returns {@link Optional#empty()}. Other kind of notifications are not reported in this
	 *        {@link IProblemReporter}.
	 * @param additionOrigins
	 * 		gathers the information related to the origin of different aspects of a {@link DocumentModel}, e.g. if an element comes
	 * 		from the root model or from the additive model. Must not be {@code null}.
	 * @return {@link Optional} containing the joined {@link DocumentModel} or {@link Optional#empty()} if critical issues occurred.
	 * 		<b>Note:</b> In case that a name collision is present (non-critical error), the order of the elements in the affected
	 * 		parent (group) does not correspond to the order which would be expected in a normal join process, since both duplicates
	 * 		are present in the result and listed near to each other. In general, the result is invalid when having non-critical
	 * 		errors.
	 */
	public Optional<DocumentModel> join(
			DocumentModel rootModel,
			DocumentModel additiveModel,
			Locale locale,
			IProblemReporter problemReporter,
			IProblemReporter criticalIssues,
			IAdditionOrigins additionOrigins,
			DocumentModelJoiningItem.ElementOrderForAddition elementOrderForAddition
	) {
		JoiningProblemReporter joiningProblemReporter = new JoiningProblemReporter(problemReporter, criticalIssues);
		List<JoiningItem> joiningItems = List.of(
				new DocumentModelJoiningItem(additiveModel, JoiningType.ADDITIVE_MODEL, elementOrderForAddition));
		return joinInternal(rootModel, joiningItems, locale, additionOrigins, joiningProblemReporter);
	}

	private static Optional<DocumentModel> joinInternal(DocumentModel rootModel, List<JoiningItem> joiningItems,
			Locale locale, IAdditionOrigins additionOrigins, JoiningProblemReporter joiningProblemReporter) {
		DocumentModel currentResultModel = DocumentModelCopyService.copy(rootModel);

		updateModelVersionIfNecessary(currentResultModel, joiningItems, locale, joiningProblemReporter);
		if (joiningProblemReporter.hasCriticalProblemOccurred()) {
			return Optional.empty();
		}

		SelectionModelV2Wrapper selectionModelWrapper = null;
		for (JoiningItem joiningItem : joiningItems) {
			JoiningType joiningType = joiningItem.getJoiningType();

			if (joiningItem instanceof SelectionJoiningItem selectionJoiningItem) {
				validateSelectionModel(selectionJoiningItem, locale, joiningProblemReporter);
				if (joiningProblemReporter.hasCriticalProblemOccurred()) {
					return Optional.empty();
				}

				selectionModelWrapper = new SelectionModelV2Wrapper(joiningType,
						MM_SelectionModel_2._viewOfCheckDmId(selectionJoiningItem.getSelection()));

				if (JoiningType.SELECTION.equals(joiningType)) {
					DocumentModel currentResultModelCopyForSelection = DocumentModelCopyService.copy(currentResultModel);
					DocumentModelSelectionExecutor documentModelSelectionExecutor = new DocumentModelSelectionExecutor(
							currentResultModelCopyForSelection, selectionModelWrapper, joiningProblemReporter);
					currentResultModel = documentModelSelectionExecutor.select();
				}
			} else if (joiningItem instanceof DocumentModelJoiningItem documentModelJoiningItem) {
				DocumentModelJoinExecutor documentModelJoinExecutor = new DocumentModelJoinExecutor(
						rootModel,
						currentResultModel,
						documentModelJoiningItem,
						locale,
						additionOrigins,
						joiningProblemReporter);
				currentResultModel = documentModelJoinExecutor.join(selectionModelWrapper);

				selectionModelWrapper = null;
			} else {
				throw new IllegalArgumentException("Unknown JoiningItem " + joiningItem.getClass().getName());
			}

			if (currentResultModel == null || joiningProblemReporter.hasCriticalProblemOccurred()) {
				return Optional.empty();
			}
		}

		String joinedDmInfo = DocumentModelJoinUtil.deriveDmJoinInfoFrom(rootModel, joiningItems);
		currentResultModel.getContent().getModelInfo().setJoinedModelsInfo(joinedDmInfo);

		return Optional.of(currentResultModel);
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

		if (problemReporter.hasCriticalProblemOccurred()) {
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
