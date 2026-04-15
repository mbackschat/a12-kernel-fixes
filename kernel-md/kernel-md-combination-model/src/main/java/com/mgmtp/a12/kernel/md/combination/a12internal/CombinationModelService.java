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

package com.mgmtp.a12.kernel.md.combination.a12internal;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.core.base.utils.internal.Consumer2ProblemReporter;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.combination.internal.ExpandingDmResolverImpl;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedRepetitionsPointer;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil;
import com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandService;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.internal.service.Identifier;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAwareDocServiceImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.MMDocumentModelLoader;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.pointers._mm_combinationmodel_1._content.PCombinationSteps;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views.MM_CombinationModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views._mm_combinationmodel_1._content.CombinationSteps;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.model.utils.Experimental;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen_mm_combinationmodel__0031.Factory_codegen_mm_combinationmodel__0031;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Provides functionality for using the {@link MM_CombinationModel_1 CombinationModel}.
 * <p>
 * This is a service class and provides static methods, no instance can be created.
 */
@Accessors(fluent = true)
@Experimental
@OnlyForUsage
public class CombinationModelService {
	public static final String MODEL_TYPE = "combination";
	public static final int ALL_STEPS = -1;
	public static final String MODEL_VERSION = "1.0.0";
	protected static final String ADDITIVE_MODEL_TYPE = "additive-document";

	private static final TypedRepetitionsPointer<MM_CombinationModel_1, CombinationSteps, PCombinationSteps<MM_CombinationModel_1>> P_STEPS =
			MM_CombinationModel_1._pointer().content().combinationSteps();
	// the MM does not contain fields that are restricted by variants (only rules)
	// for A12 (SME) there is a CustomCondition "InvalidReference" which we don't have/use
	private static final String META_MODEL_DM_NAME = "extmpinternal/none-mm_combinationmodel-1.json";
	private static final String META_MODEL_CODEGEN_DM_NAME = "extmpinternal/codegen-mm_combinationmodel-1.json";

	@Getter(value = AccessLevel.PRIVATE, lazy = true)
	private static final IDocumentModel metaModel = MMDocumentModelLoader.deserializeMMDocumentModel(META_MODEL_DM_NAME);
	@Getter(value = AccessLevel.PRIVATE)
	private static final IDocumentModelResolver metaModelResolver = MMDocumentModelLoader.createResolver(metaModel());
	private static final IDocumentV2Serializer documentSerializer = new DocumentSerializerImpl(metaModelResolver());

	private CombinationModelService() {
		throw new IllegalStateException("This service class provides static methods and should not get instantiated.");
	}

	/**
	 * Expand all includes of the provided {@link IDocumentModel}.
	 * <p>
	 * The includes can be either {@link IDocumentModel}s or {@link MM_CombinationModel_1 CombinationModel}s, both are resolved by
	 * the provided resolver (also see {@link DMLike}).
	 * <p>
	 * <i>Note</i>: the returned {@link IDocumentModel} may only be partial, the caller has to check the
	 * {@code notificationReceiver} for notifications.
	 */
	public static @NonNull Optional<IDocumentModel> expand(@NonNull IDocumentModel documentModel,
			@NonNull IUnexpandedModelResolver dmResolver, @NonNull Locale locale,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		DocumentModel dm = DocumentModelCopyService.copy(new DocumentModelService().convertFromExternal(documentModel));

		ExpandingDmResolverImpl expandingDmResolver = ExpandingDmResolverImpl.of(dmResolver, locale, notificationReceiver,
				List.of(dm.getHeader().getId()));

		IProblemReporter problemReporterWithVisitedIdsStack = expandingDmResolver.combineNotificationReceiverWithVisitedIds(
				notificationReceiver);

		try {
			DocumentModelExpandService.expand(dm,
					// works similar to ExpandingDmResolverImpl.getDocumentModelById
					// we pass the recursively-expanding, combination-model-aware, resolver instead of the
					// DocumentModelReferenceResolver, in order to support recursive expansion of combination models too, even
					// though DocumentModelExpandService does not know how to handle combination models
					expandingDmResolver::getDocumentModelByIdAsA12internalDM,
					problemReporterWithVisitedIdsStack);
			return Optional.of(new DocumentModelService().convertToExternal(dm));
		} catch (CombinationException e) {
			notificationReceiver.accept(RankedNotificationImpl.error(e.getMessage()));
			return Optional.empty();
		}
	}

	/**
	 * <em>Note:</em> This method is only intended for use in modeling tools (e.g. SME) and must not be used to create final
	 * production-ready models.
	 * Outside the work-in-progress modeling use case, models should not be <em>partially</em> expanded, therefore
	 * {@link #expand(MM_CombinationModel_1, IUnexpandedModelResolver, Locale, Consumer)} should be used to <em>fully</em> expand
	 * them instead.
	 * <p>
	 * Expand the {@link MM_CombinationModel_1 CombinationModel} but only apply the first n combination steps.
	 * <p>
	 * Only the first n combination steps (at most all existing steps) are evaluated. Additional steps are ignored.
	 * <p>
	 * The basemodel and all the steps can reference other {@link IDocumentModel}s or
	 * {@link MM_CombinationModel_1 CombinationModel}s, both are resolved by the provided resolver (also see {@link DMLike}).
	 * <p>
	 * <i>Note</i>: the returned {@link IDocumentModel} may only be partial, the caller has to check the
	 * {@code notificationReceiver} for notifications.
	 */
	// NOTE keep this method a12internal even when CombinationModelService or some of its methods become public-api. This
	// Method should only be used by modeling tools (e.g. SME). We don't want partually expanded Models to be used in production,
	// for example because such a partially expanded model would have the same id as the fully expanded one, so the id would be
	// no longer unambiguous.
	public static @NonNull Optional<IDocumentModel> expand(@NonNull MM_CombinationModel_1 combinationModel, int applyNSteps,
			@NonNull IUnexpandedModelResolver dmResolver, @NonNull Locale locale,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		if (applyNSteps != ALL_STEPS) {
			combinationModel = combinationModel._with(
					P_STEPS, Java8Utils.subList(combinationModel._at(P_STEPS), 0, applyNSteps));
		}
		return expand(combinationModel, dmResolver, locale, notificationReceiver);
	}

	/**
	 * Expand a combination model with all combination steps.
	 * <p>
	 * The basemodel and all the steps can reference other {@link IDocumentModel}s or
	 * {@link MM_CombinationModel_1 CombinationModel}s, both are resolved by the provided resolver (also see {@link DMLike}).
	 * <p>
	 * <i>Note</i>: the returned {@link IDocumentModel} may only be partial, the caller has to check the
	 * {@code notificationReceiver} for notifications.
	 */
	public static @NonNull Optional<IDocumentModel> expand(@NonNull MM_CombinationModel_1 combinationModel,
			@NonNull IUnexpandedModelResolver dmResolver, @NonNull Locale locale,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		ExpandingDmResolverImpl expandingDmResolver = ExpandingDmResolverImpl.of(dmResolver, locale, notificationReceiver,
				List.of(combinationModel.header().id()));
		try {
			return expandingDmResolver.expand(combinationModel, notificationReceiver);
		} catch (CombinationException e) {
			notificationReceiver.accept(RankedNotificationImpl.error(e.getMessage()));
			return Optional.empty();
		}
	}

	/**
	 * Check to see if the structure of the combination model is valid.
	 * <p>
	 * This executes the validation rules of the {@link MM_CombinationModel_1 CombinationModel}.
	 */
	public static boolean checkStructuralConsistency(@NonNull MM_CombinationModel_1 combinationModel,
			@NonNull Locale locale, @NonNull Consumer<? super RankedNotification> notificationReceiver) {
		IDocumentModelResolver modelResolver = MMDocumentModelLoader.createResolver(META_MODEL_CODEGEN_DM_NAME);
		DocumentV2 codegenDm = DmAwareDocServiceImpl.ofResolver(META_MODEL_CODEGEN_DM_NAME, modelResolver)
				.removeUnknowns(combinationModel._unwrap());

		IDocumentValidationResult documentValidationResult = LowKernelApiUtils.callValidator(
				LowKernelApiUtils.ValidationMethod.FULL_VALIDATION,
				Factory_codegen_mm_combinationmodel__0031.getInstance().getValidator(),
				Factory_codegen_mm_combinationmodel__0031.getInstance().getMetaObject(),
				codegenDm,
				null,
				DocumentProcessingConfig.builder(locale).build(),
				id -> new Identifier(id.getName(), id.getIndices()).toString());

		documentValidationResult.getMessages()
				.forEach(m -> notificationReceiver.accept(
						RankedNotificationImpl.of(m.getErrorText(), m.getSeverity().toBaseSeverity(), m)));

		return documentValidationResult.noErrorOccurred();
	}

	/**
	 * Checks all included models and combination steps to see if the resulting {@link IDocumentModel} is valid.
	 *
	 * @return {@code false} if there are errors in the expanded {@link MM_CombinationModel_1 CombinationModel},
	 * 		the {@code notificationReceiver} received the occurred messages, {@code true} otherwise.
	 */
	public static boolean checkConsistencyComplete(@NonNull MM_CombinationModel_1 combinationModel,
			@NonNull IUnexpandedModelResolver dmResolver, @NonNull Locale locale,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		AtomicBoolean result = new AtomicBoolean(true);
		Consumer<RankedNotification> errorRecordingNotificationReceiver = n -> {
			notificationReceiver.accept(n);
			if (n.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) {
				result.set(false);
			}
		};

		// "all locales are available" and "typeDefinition mode is the same" will be checked during expansion
		Optional<IDocumentModel> expanded = expand(combinationModel, dmResolver, locale, errorRecordingNotificationReceiver);

		// check if the resulting IDocumentModel is valid
		expanded.ifPresent(
				dm -> new DocumentModelService().checkConsistency(new DocumentModelService().convertFromExternal(dm),
						new Consumer2ProblemReporter(errorRecordingNotificationReceiver)));

		return result.get();
	}

	/**
	 * Remove all model references from additive and selection models.
	 *
	 * @param documentModel
	 * 		the DM where to remove the model references from.
	 * @return a _new_ DM with the model references removed.
	 *
	 * @deprecated this is only a temporary workaround for kernel 30.7.x
	 */
	@Deprecated(since = "30.7.0")
	public static @NonNull IDocumentModel removeAdditiveAndSelectionModelReferences(@NonNull IDocumentModel documentModel) {
		List<String> modelReferencesToRemove = List.of(ADDITIVE_MODEL_TYPE, SelectionModelService.MODEL_TYPE);

		Header header = documentModel.getHeader();
		Header newHeader = HeaderBuilderUtil.toBuilder(header)
				.withModelReferences(
						header.getModelReferences().stream()
								.filter(mr -> !modelReferencesToRemove.contains(mr.getModelType()))
								.toList()
				)
				.build();
		DocumentModel internalDM = new DocumentModelService().convertFromExternal(documentModel);
		DocumentModel dmCopy = DocumentModelCopyService.copy(internalDM);
		dmCopy.setHeader(newHeader);
		return new DocumentModelService().convertToExternal(dmCopy);
	}

	/**
	 * Deserialize a {@link MM_CombinationModel_1 CombinationModel} using the provided reader.
	 * <p>
	 * This method does not expand the {@link MM_CombinationModel_1 CombinationModel}, for this use
	 * {@link #expand(MM_CombinationModel_1, IUnexpandedModelResolver, Locale, Consumer)}.
	 */
	public static @NonNull MM_CombinationModel_1 deserialize(@NonNull Reader reader,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		DocumentDeserializationConfig config = DocumentDeserializationConfig.builder().build();
		DocumentV2 documentV2 = documentSerializer.deserializeV2(reader, metaModel().getHeader().getId(), config,
				notificationReceiver::accept);
		return MM_CombinationModel_1._viewOfCheckDmId(documentV2);
	}

	/**
	 * Deserialize a {@link MM_CombinationModel_1 CombinationModel} from a {@link JsonNode}.
	 * <p>
	 * This method does not expand the {@link MM_CombinationModel_1 CombinationModel}, for this use
	 * {@link #expand(MM_CombinationModel_1, IUnexpandedModelResolver, Locale, Consumer)}.
	 */
	public static @NonNull MM_CombinationModel_1 deserialize(@NonNull JsonNode jsonNode,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		DocumentDeserializationConfig config = DocumentDeserializationConfig.builder().build();
		DocumentV2 documentV2 = documentSerializer.deserializeV2(jsonNode, metaModel().getHeader().getId(), config,
				notificationReceiver::accept);
		return MM_CombinationModel_1._viewOfCheckDmId(documentV2);
	}

	/**
	 * Fills all computable fields of the provided {@link MM_CombinationModel_1 CombinationModel}.
	 */
	public static @NonNull MM_CombinationModel_1 fillComputableFields(@NonNull MM_CombinationModel_1 combinationModel) {
		return combinationModel
				._with(MM_CombinationModel_1._pointer().header().modelType(), MODEL_TYPE)
				._with(MM_CombinationModel_1._pointer().header().modelVersion(), MODEL_VERSION);
	}

	/**
	 * Serialize the {@link MM_CombinationModel_1 CombinationModel} to Json.
	 */
	public static void serialize(@NonNull MM_CombinationModel_1 combinationModel, @NonNull Writer writer) {
		DocumentSerializationConfig config = DocumentSerializationConfig.builder().build();
		DocumentV2 document = combinationModel._unwrap();

		documentSerializer.serializeV2(document, writer, config);
	}
}
