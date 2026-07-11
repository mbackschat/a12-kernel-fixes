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

package com.mgmtp.a12.kernel.md.combination.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.collections4.ListUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;
import com.mgmtp.a12.kernel.md.combination.a12internal.CMWrapper;
import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationException;
import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationModelService;
import com.mgmtp.a12.kernel.md.combination.a12internal.DMLike;
import com.mgmtp.a12.kernel.md.combination.a12internal.DMWrapper;
import com.mgmtp.a12.kernel.md.combination.a12internal.ExpandingDmResolver;
import com.mgmtp.a12.kernel.md.combination.a12internal.IUnexpandedModelResolver;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedRepetitionsPointer;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.ExpansionPurposes;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdge;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdgeLabel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionNode;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.LocalCoordinates;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DMWithExpansionInfo;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.DocumentModelBuilder;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil;
import com.mgmtp.a12.kernel.md.model.internal.expansioninfo.ExpansionInfoUtils;
import com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandError;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ModelHeaderUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.internal.services.join.JoiningType;
import com.mgmtp.a12.kernel.md.model.internal.services.join.item.DocumentModelJoiningItem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.item.JoiningItem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.item.SelectionJoiningItem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelJoinUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.NameCollisionHandler;
import com.mgmtp.a12.kernel.md.model.internal.utils.ExpansionUtils;
import com.mgmtp.a12.kernel.mmtypings.ct_model_header_1.views._ct_model_header_1._header.Locales;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.pointers._mm_combinationmodel_1._content.PCombinationSteps;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views.MM_CombinationModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views._mm_combinationmodel_1._content.CombinationSteps;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views._mm_combinationmodel_1._content._combinationsteps.Type;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.ModelReference;
import com.mgmtp.a12.model.notification.RankedNotification;
import lombok.NonNull;

public class ExpandingDmResolverImpl implements ExpandingDmResolver {
	public static final String SUPPORTED_LOCALES_DONT_FIT = "The model %s (%s) does not support all locales of the combination model (missing [%s] of [%s]).";
	public static final String BASE_MODEL_MISSING_PREVIEW_EXPANSION_ONLY =
			"Base model not specified. Preview-only mode was used for expansion of combination model '%s', " +
					"and subsequent consistency checks may be inaccurate or misleading.";
	public static final TypedRepetitionsPointer<MM_CombinationModel_1, CombinationSteps, PCombinationSteps<MM_CombinationModel_1>> P_STEPS = MM_CombinationModel_1._pointer()
			.content().combinationSteps();

	final @NonNull IUnexpandedModelResolver internalResolver;
	private final @NonNull Consumer<? super RankedNotification> notificationReceiver;
	private final @NonNull List<String> visitedIdsStack;
	private final @NonNull Locale locale;

	public ExpandingDmResolverImpl(
			@NonNull IUnexpandedModelResolver internalResolver,
			@NonNull Locale locale,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			@NonNull List<String> visitedIdsStack) {
		this.internalResolver = internalResolver;
		this.notificationReceiver = notificationReceiver;
		this.locale = locale;
		this.visitedIdsStack = visitedIdsStack;
	}

	@Override public IDocumentModel getDocumentModelById(@NonNull String dmId) {
		return a12internal_resolveById(dmId, false).dmAsExternal();
	}

	@Override public DMWithExpansionInfo a12internal_resolveById(@NonNull String dmId, boolean createExpansionInfo) {
		DMLike resolved;
		try {
			resolved = internalResolver.resolve(dmId);
		} catch (CombinationException e) {
			// TODO A12K-3237: use context info
			throw new CombinationException(String.format("%s (%s)", e.getMessage(), formatVisitedIds()), e);
		}
		ExpandingDmResolver resolverForRecursion = this.withVisitedIdAppended(dmId);

		return switch (resolved) {
			case DMWrapper(IDocumentModel dm) -> {
				DocumentModel baseDocumentModel = new DocumentModelService().convertFromExternal(dm);
				yield resolverForRecursion.a12internal_expand(baseDocumentModel, createExpansionInfo);
			}
			case CMWrapper(MM_CombinationModel_1 cm) ->
					resolverForRecursion.a12internal_expand(cm, createExpansionInfo).orElseThrow(CombinationException::new);
		};
	}

	@Override
	public DMWithExpansionInfo a12internal_expand(DocumentModel baseDocumentModel, boolean createExpansionInfo) {
		Consumer<RankedNotification> consumerWithVisitedIdsStack = notification -> { // Not sure if this special consumer is really
			// needed except for current test expectations... maybe this is currently a hacky version to get some context info into
			// error messages that might get superseded with A12K-3237?
			RankedNotificationImpl adaptedNotificationWithVisitedIds = RankedNotificationImpl.of(
					// TODO A12K-3237: use context info
					notification.getMessage() + " (" + formatVisitedIds() + ")",
					notification.getSeverity(),
					notification.getSource());
			notificationReceiver.accept(adaptedNotificationWithVisitedIds);
		};
		// the below assertion does not hold for some of our tests, because for some DMs the id in the model does not match the
		// id by which the model gets resolved:
		//assert baseDocumentModel.getHeader().getId().equals(this.visitedIdsStack.getLast()) :
		//		"we expect the caller of expand() to set the current-level entry in the visitedIdsStack";

		// works similar to CombinationModelService.expand(IDocumentModel, ...)
		// we pass the recursively-expanding, combination-model-aware resolver,
		// in order to support recursive expansion of combination models too, even
		// though DocumentModelExpandService does not know how to handle combination models
		Optional<ExpansionInfo> expansionInfo = DocumentModelExpandService.expand(baseDocumentModel,
				this, consumerWithVisitedIdsStack, createExpansionInfo);
		return DMWithExpansionInfo.of(baseDocumentModel, expansionInfo);
	}

	/**
	 * Recursively expand the given combinationModel (including nested combination models and includes)
	 */
	@Override
	public @NonNull Optional<DMWithExpansionInfo> a12internal_expand(@NonNull MM_CombinationModel_1 combinationModel,
			boolean createExpansionInfo) {
		if (!CombinationModelService.checkStructuralConsistency(combinationModel, locale, notificationReceiver)) {
			return Optional.empty();
		}
		assert combinationModel._at(MM_CombinationModel_1._pointer().header().id()).equals(this.visitedIdsStack.getLast()) :
				"we expect the caller of expand() to set the current-level entry in the visitedIdsStack"; //  because expand calls
		// other instance methods, so deriving a resolver with withVisitedIdAppended would be inconvenient

		DMWithExpansionInfo preparedBase = prepareBaseForJoin(combinationModel, notificationReceiver, createExpansionInfo);

		// collect all the joining items
		List<JoiningItem> joiningItems = new ArrayList<>(combinationModel._at(P_STEPS).stream()
				// toJoiningItem handles recursion (via resolveById)
				.flatMap(step -> toJoiningItems(step, createExpansionInfo))
				.toList());

		// locale compatibility of the base DM is already checked inside prepareBaseForJoin;
		// here we only check the DMs from the joining items (steps)
		checkAndNotifyCompatibility(
				combinationModel._at(MM_CombinationModel_1._pointer().header().locales())
						.stream().map(Locales::code).toList(),
				joiningItems.stream()
						.filter(DocumentModelJoiningItem.class::isInstance)
						.map(ji -> ((DocumentModelJoiningItem) ji).getDocumentModel()).toList(),
				notificationReceiver);

		IProblemReporter problemReporterWithVisitedIdsStack = combineNotificationReceiverWithVisitedIds(notificationReceiver);

		ExpansionInfo[] expansionInfoAfterJoinSteps = { null };
		Optional<IDocumentModel> result = DocumentModelJoiningService.join(
						preparedBase, joiningItems, locale,
						new JoiningProblemReporter(problemReporterWithVisitedIdsStack),
						createExpansionInfo ? ei -> expansionInfoAfterJoinSteps[0] = ei : null)
				.map(new DocumentModelService()::convertToExternal);

		Optional<ExpansionInfo> cleanedExpansionInfoAfterJoinSteps = Optional.ofNullable(expansionInfoAfterJoinSteps[0])
				.map(ExpansionEdgeLabel.JoinInternal::removeInternalConnections);

		result.ifPresent(ExpandingDmResolverImpl::removeManagedModelReferencesAndNormalize);

		return result.map(dm -> DMWithExpansionInfo.ofExternalDM(dm, cleanedExpansionInfoAfterJoinSteps));
	}

	/**
	 * Resolves the base document model (or creates an empty placeholder if none is specified),
	 * applies header values from the combination model (keeping modelType/modelVersion from the original base),
	 * and merges annotations and model references. Builds the corresponding expansion info with direct
	 * externally-visible connection types (Base, CmItself).
	 *
	 * @return a DMWithExpansionInfo ready to be passed as root model to DocumentModelJoiningService.join
	 */
	private DMWithExpansionInfo prepareBaseForJoin(
			@NonNull MM_CombinationModel_1 combinationModel,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			boolean createExpansionInfo) {

		String cmId = combinationModel._at(MM_CombinationModel_1._pointer().header().id());
		String baseModelId = combinationModel._at(MM_CombinationModel_1._pointer().content().baseModelId());

		// single map accumulating all origins (analogous to DocumentModelExpandService.origins)
		HashMap<LocalCoordinates, ExpansionNode> origins = new HashMap<>();

		// --- resolve or create baseDM, seed origins from the resolved base model ---

		DocumentModel resolvedBaseDM;

		if (baseModelId != null) {
			DMWithExpansionInfo resolvedBaseWithEI = this.a12internal_resolveById(baseModelId, createExpansionInfo);
			// Copy is needed because resolveById does not guarantee a fresh DM: for the DMWrapper path,
			// convertFromExternal merely unwraps whatever the IUnexpandedModelResolver returned, and the
			// header is mutated below.
			resolvedBaseDM = DocumentModelCopyService.copy(resolvedBaseWithEI.dm());

			if (createExpansionInfo) {
				ExpansionInfoUtils.mergeInto(origins,
						ExpansionInfoUtils.allNestedOriginsNoChange(
								cmId, ExpansionEdgeLabel.Cm.Base.of(), resolvedBaseWithEI.ei().get()));
			}
		} else {
			// If no base model is specified, we do a "preview" expansion for improved convenience for the modeler.
			// Settings like modelInfo and modelConfig that are normally taken from the baseDm may in this case be
			// wrong, and subsequent consistency-check messages can therefore be misleading.
			// The baseModelId == null case should therefore very likely not be used in prod and we should not spend
			// too much effort making it "bug-free".
			notificationReceiver.accept(RankedNotificationImpl.warning(
					String.format(BASE_MODEL_MISSING_PREVIEW_EXPANSION_ONLY, this.formatVisitedIds())
			));

			// create empty DM with default values in the header, the id will be replaced later
			List<Locale> locales = combinationModel._at(MM_CombinationModel_1._pointer().header().locales()).stream()
					.map(l -> CommonUtils.toLocale(l.code())).toList();
			resolvedBaseDM = DocumentModelBuilder.builder(cmId, locales).build();
			// no expansionInfo to set, since base is empty (any modelRefs, annotations, etc in the prepared base dm can only origniate from the _CM_
		}

		// check locale compatibility of the resolved base DM before overwriting its header
		checkAndNotifyCompatibility(
				combinationModel._at(MM_CombinationModel_1._pointer().header().locales())
						.stream().map(Locales::code).toList(),
				List.of(resolvedBaseDM),
				notificationReceiver);

		// --- overwrite header from CM (keeping modelType/modelVersion from the original base) ---
		// set header after the locale compat check, to we can use the original header for locale comparison
		Header cmHeader = ModelHeaderUtils.fromGroupInstance(combinationModel.header()._unwrap());
		Header originalBaseDMHeader = resolvedBaseDM.getHeader();

		// remove all managed purposes of the cm
		List<ModelReference> filteredModelReferences = Java8Utils.filterList(ListUtils.emptyIfNull(cmHeader.getModelReferences()),
				mr -> !ExpansionPurposes.CM_MANAGED_PURPOSES.contains(mr.getModelType()));

		// Note: the below header-merging (+corresponding origin-graph recording) code seems very long and complex vs what aomunt
		// of complexity I'd intuitively expect... maybe it can be simplified further...

		resolvedBaseDM.setHeader(HeaderBuilderUtil
				.toBuilder(cmHeader)
				.withModelType(originalBaseDMHeader.getModelType())
				.withModelVersion(originalBaseDMHeader.getModelVersion())
				.withModelReferences(filteredModelReferences)
				.build());
		// from here on, resolvedBaseDM has the CM's header (except modelType/modelVersion)

		// --- merge model references and annotations from the original base header back in ---
		DocumentModelJoinUtil.mergeModelReferences(resolvedBaseDM, originalBaseDMHeader);

		IProblemReporter problemReporter = n -> notificationReceiver.accept(RankedNotificationImpl.of(
				n.getMessage() + " (" + this.formatVisitedIds() + ")",
				n.getSeverity(),
				n.getSource()
		));
		JoiningProblemReporter joiningProblemReporter = new JoiningProblemReporter(problemReporter);
		NameCollisionHandler nameCollisionHandler = new NameCollisionHandler(
				DocumentModelJoinUtil.getModelString(resolvedBaseDM), locale, joiningProblemReporter);
		DocumentModelJoinUtil.determineAndSetJoinOfAnnotations(
				resolvedBaseDM.getHeader().getAnnotations(),
				originalBaseDMHeader.getAnnotations(),
				annotations -> resolvedBaseDM.setHeader(
						HeaderBuilderUtil.toBuilder(resolvedBaseDM.getHeader()).withAnnotations(annotations).build()),
				() -> nameCollisionHandler.handleInconsistentAnnotations(Header.class, null),
				joiningProblemReporter,
				new DocumentModelJoinUtil.AnnotationSourceCallback() {
				} // origins are seeded upfront (see below) // NOTE this creates a small inconsistency where unjoinable annotations have expansionInfo entries but are not present in the result TODO decide if that is acceptable or should be fixed. Also: are corresponding annotations from additive fragments removed too? (where?)
		);

		// if there is a base model, the name of the model is still from the base model
		// we can only set the name here, otherwise the wrong model name would be used when merging annotations
		resolvedBaseDM.getContent().getModelInfo().setName(combinationModel.header().id());

		// --- add CM-level origins (annotations, model references, model details) ---

		if (!createExpansionInfo) {
			return DMWithExpansionInfo.of(resolvedBaseDM, Optional.empty());
		}

		// Annotation origins: base DM annotation origins are already in the origins map (seeded from
		// allNestedElementOriginsNoChange above). CM annotation origins are added here. Annotation merging
		// (determineAndSetJoinOfAnnotations) never drops annotations — all annotations from both the CM header
		// and the original base DM header end up in the prepared DM — so we can seed both sets of origins
		// upfront without consulting the annotation merge callback.
		// For annotations present in both sources with differing values, both values are kept (as inconsistent
		// duplicates) and both origins are recorded here via mergeInto's ExpansionNode::merge.
		// Note: "unjoinable" base DM annotations (e.g. "roles") are dropped from the DM but their origin
		// entries remain harmlessly in the map — this matches the pre-existing behavior.
		Stream.ofNullable(cmHeader.getAnnotations()).flatMap(List::stream).forEach(annotation -> {
			LocalCoordinates.Annotation coords = LocalCoordinates.Annotation.model(annotation.getName());
			ExpansionInfoUtils.mergeInto(origins, ExpansionNode.of(coords, cmId, Set.of(
					ExpansionEdge.of(ExpansionEdgeLabel.Cm.CmItself.of(),
							ExpansionNode.of(coords, cmId))
			)));
		});

		ExpansionInfoUtils.mergeInto(origins,
				ExpansionInfoUtils.unmanagedModelReferences(cmHeader.getModelReferences()).stream()
						.map(loc -> ExpansionNode.of(loc, cmId, Set.of(
								ExpansionEdge.of(ExpansionEdgeLabel.Cm.CmItself.of(),
										// in the _unexpanded_ CM, both loc and modelId were the same:
										ExpansionNode.of(loc, cmId))
						))).toList());

		ExpansionInfoUtils.mergeInto(origins, ExpansionNode.of(LocalCoordinates.ModelItself.of(),
				cmId,
				ExpansionEdge.of(
						ExpansionEdgeLabel.Cm.CmItself.of(),
						ExpansionNode.of(LocalCoordinates.ModelItself.of(), cmId))
		));

		return DMWithExpansionInfo.of(resolvedBaseDM, Optional.of(ExpansionInfo.of(cmId, origins)));
	}

	public String formatVisitedIds() {
		return String.join(" -> ", visitedIdsStack);
	}

	private void checkAndNotifyCompatibility(@NonNull List<String> locales,
			@NonNull List<DocumentModel> dmsToCheck,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		String allLocalesString = String.join(", ", locales);

		dmsToCheck.forEach(ji -> {
					List<String> missingLocales = new ArrayList<>(locales);
					missingLocales.removeAll(ji.getHeader().getLocales().stream().map(Locale::toString).toList());

					if (!missingLocales.isEmpty()) {
						notificationReceiver.accept(RankedNotificationImpl.error(
								String.format(SUPPORTED_LOCALES_DONT_FIT,
										ji.getHeader().getId(),
										formatVisitedIds(),
										String.join(", ", missingLocales),
										allLocalesString
								)));
					}
				}
		);
	}

	public ExpandingDmResolver withVisitedIdAppended(String id) {
		if (visitedIdsStack.contains(id)) {
			throw new CombinationException(
					DocumentModelExpandError.SELF_RECURSION.getMessage(id, String.format(" (%s)", formatVisitedIds())));
		}

		var extendedVisitedIds = Java8Utils.appendList(this.visitedIdsStack, id);
		return ExpandingDmResolver.internal_withStartingId(internalResolver, locale, notificationReceiver, extendedVisitedIds);
	}

	/**
	 * (handles recursive expansion of any referenced DMs or CMs)
	 */
	private Stream<JoiningItem> toJoiningItems(@NonNull CombinationSteps step, boolean createExpansionInfo) {
		return switch (step.type()) {
			case Addition -> {
				DMWithExpansionInfo dmWithExpansionInfo = this.a12internal_resolveById(step.additiveModel().dmId(),
						createExpansionInfo);

				yield Stream.of(new DocumentModelJoiningItem(dmWithExpansionInfo.dm(),
						JoiningType.ADDITIVE_MODEL, DocumentModelJoiningItem.ElementOrderForAddition.ANCHOR_BASED_ORDERING,
						dmWithExpansionInfo.ei()));
			}
			case Selection -> Stream.of(
					new SelectionJoiningItem(this.internalResolver.resolveSelectionModel(step.selectionModel().smId())._unwrap(),
							JoiningType.SELECTION));
			case DecorationForFields, DecorationForGroups -> {
				DMWithExpansionInfo dmWithExpansionInfo = this.a12internal_resolveById(step.decorationModel().dmId(),
						createExpansionInfo);
				yield Stream.of(
						new SelectionJoiningItem(
								this.internalResolver.resolveSelectionModel(step.selectionModel().smId())._unwrap(),
								JoiningType.SELECTION_DECORATION),
						new DocumentModelJoiningItem(dmWithExpansionInfo.dm(), convertJoiningType(step.type()),
								DocumentModelJoiningItem.ElementOrderForAddition.ANCHOR_BASED_ORDERING,
								dmWithExpansionInfo.ei())
				);
			}
		};
	}

	private JoiningType convertJoiningType(Type type) {
		return switch (type) {
			case Addition -> JoiningType.ADDITIVE_MODEL;
			case Selection -> JoiningType.SELECTION;
			case DecorationForFields -> JoiningType.DECORATION_MODEL_FIELD;
			case DecorationForGroups -> JoiningType.DECORATION_MODEL_GROUP;
		};
	}

	/**
	 * Removes model references with managed purposes (include, typeDefinitions) and normalizes
	 * the model references list. Operates on an external IDocumentModel by converting to internal
	 * representation, modifying, and relying on shared underlying state.
	 */
	static void removeManagedModelReferencesAndNormalize(IDocumentModel dm) {
		DocumentModel internalDM = new DocumentModelService().convertFromExternal(dm);
		// the ModelReference list might be immutable, so we rebuild the header with a filtered list
		Header adaptedHeader = HeaderBuilderUtil.toBuilder(dm.getHeader())
				.withModelReferences(
						ExpansionUtils.removeAllManagedModelReferences(ListUtils.emptyIfNull(dm.getHeader().getModelReferences())))
				.build();
		internalDM.setHeader(adaptedHeader);
		HeaderBuilderUtil.normalizeModelRefsList(internalDM);
	}

	public IProblemReporter combineNotificationReceiverWithVisitedIds(Consumer<? super RankedNotification> notificationReceiver) {
		return notification -> {
			RankedNotificationImpl adaptedNotificationWithVisitedIds = RankedNotificationImpl.of(
					notification.getMessage() + " (" + this.formatVisitedIds() + ")", notification.getSeverity(),
					notification.getSource());
			notificationReceiver.accept(adaptedNotificationWithVisitedIds);
		};
	}
}
