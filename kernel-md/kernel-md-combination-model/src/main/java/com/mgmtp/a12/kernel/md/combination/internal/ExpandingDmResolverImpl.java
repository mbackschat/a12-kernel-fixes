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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.JoiningType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.AnnotationsOrigins;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.DocumentModelJoiningItem;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.JoiningItem;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.SelectionJoiningItem;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.DocumentModelBuilder;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil;
import com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandService;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ModelHeaderUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelJoinUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.NameCollisionHandler;
import com.mgmtp.a12.kernel.mmtypings.ct_model_header_1.views._ct_model_header_1._header.Locales;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.pointers._mm_combinationmodel_1._content.PCombinationSteps;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views.MM_CombinationModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views._mm_combinationmodel_1._content.CombinationSteps;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views._mm_combinationmodel_1._content._combinationsteps.type;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.Experimental;

import lombok.NonNull;

@Experimental
public class ExpandingDmResolverImpl implements ExpandingDmResolver {
	public static final String SUPPORTED_LOCALES_DONT_FIT = "The model %s (%s) does not support all locales of the combination model (missing [%s] of [%s]).";
	public static final String BASE_MODEL_MISSING_PREVIEW_EXPANSION_ONLY =
			"Base model not specified. Preview-only mode was used for expansion of combination model '%s', " +
					"and subsequent consistency checks may be inaccurate or misleading.";
	public static final TypedRepetitionsPointer<MM_CombinationModel_1, CombinationSteps, PCombinationSteps<MM_CombinationModel_1>> P_STEPS = MM_CombinationModel_1._pointer()
			.content().combinationSteps();

	private final IUnexpandedModelResolver internalResolver;
	private final Consumer<? super RankedNotification> notificationReceiver;
	private final List<String> visitedIdsStacks;
	private final Locale locale;

	public static ExpandingDmResolverImpl of(
			@NonNull IUnexpandedModelResolver internalResolver,
			@NonNull Locale locale,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			@NonNull List<String> visitedIdsStacks
	) {
		return new ExpandingDmResolverImpl(internalResolver, locale, notificationReceiver, visitedIdsStacks);
	}

	private ExpandingDmResolverImpl(
			@NonNull IUnexpandedModelResolver internalResolver,
			@NonNull Locale locale,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			@NonNull List<String> visitedIdsStacks) {
		this.internalResolver = internalResolver;
		this.notificationReceiver = notificationReceiver;
		this.locale = locale;
		this.visitedIdsStacks = visitedIdsStacks;
	}

	@Override public IDocumentModel getDocumentModelById(@NonNull String dmId) {
		DMLike resolved;
		try {
			resolved = internalResolver.resolve(dmId);
		} catch (CombinationException e) {
			throw new CombinationException(String.format("%s (%s)", e.getMessage(), formatVisitedIds()), e);
		}
		ExpandingDmResolverImpl resolverForRecursion = this.withVisitedIdAppended(dmId);

		return switch (resolved) {
			case DMWrapper(IDocumentModel dm) -> {
				IProblemReporter problemReporterWithVisitedIdsStack = notification -> {
					RankedNotificationImpl adaptedNotificationWithVisitedIds = RankedNotificationImpl.of(
							notification.getMessage() + " (" + resolverForRecursion.formatVisitedIds() + ")",
							notification.getSeverity(),
							notification.getSource());
					notificationReceiver.accept(adaptedNotificationWithVisitedIds);
				};

				// works similar to CombinationModelService.expand(IDocumentModel, ...)
				// we pass the recursively-expanding, combination-model-aware, resolver instead of the
				// DocumentModelReferenceResolver, in order to support recursive expansion of combination models too, even
				// though DocumentModelExpandService does not know how to handle combination models
				DocumentModelExpandService.expand(new DocumentModelService().convertFromExternal(dm),
						resolverForRecursion::getDocumentModelByIdAsA12internalDM,
						problemReporterWithVisitedIdsStack);
				yield dm;
			}
			case CMWrapper(MM_CombinationModel_1 cm) -> {
				// we cannot use the expand method of CombinationModelService, as we need to preserve the visitedIdsStack
				Optional<IDocumentModel> expanded = resolverForRecursion.expand(cm, notificationReceiver);
				yield expanded.orElseThrow(CombinationException::new);
			}
		};
	}

	public DocumentModel getDocumentModelByIdAsA12internalDM(@NonNull String dmId) {
		return new DocumentModelService().convertFromExternal(getDocumentModelById(dmId));
	}

	/**
	 * Recursively expand the given combinationModel (including nested combination models and includes)
	 */
	public @NonNull Optional<IDocumentModel> expand(@NonNull MM_CombinationModel_1 combinationModel,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		if (!CombinationModelService.checkStructuralConsistency(combinationModel, locale, notificationReceiver)) {
			return Optional.empty();
		}

		DocumentModel baseDM;

		String baseModelId = combinationModel._at(MM_CombinationModel_1._pointer().content().baseModelId());
		if (baseModelId != null) {
			// copy _expanded_ DM
			// recursion for baseDm happens via getDocumentModelByIdAsA12internalDM
			baseDM = DocumentModelCopyService.copy(this.getDocumentModelByIdAsA12internalDM(baseModelId));
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
			baseDM = DocumentModelBuilder.builder(combinationModel._at(MM_CombinationModel_1._pointer().header().id()), locales)
					.build();
		}

		// collect all the joining items
		List<JoiningItem> joiningItems = new ArrayList<>(combinationModel._at(P_STEPS).stream()
				.flatMap(this::toJoiningItems) // toJoiningItem handles recursion (via  getDocumentModelByIdAsA12internalDM)
				.toList());

		checkAndNotifyCompatibility(
				combinationModel._at(MM_CombinationModel_1._pointer().header().locales())
						.stream().map(Locales::code).toList(),

				// combine DMs from the joining items (steps) and the baseDM
				Java8Utils.appendList(joiningItems.stream()
								.filter(DocumentModelJoiningItem.class::isInstance)
								.map(ji -> ((DocumentModelJoiningItem) ji).getDocumentModel()).toList(),
						baseDM
				),
				notificationReceiver);

		// set header after, to we can use the original header for locale comparison
		com.mgmtp.a12.model.header.Header cmHeader = ModelHeaderUtils.fromGroupInstance(combinationModel.header()._unwrap());
		Header originalDMHeader = baseDM.getHeader();

		baseDM.setHeader(HeaderBuilderUtil
				.toBuilder(cmHeader)
				.withModelType(originalDMHeader.getModelType())
				.withModelVersion(originalDMHeader.getModelVersion())
				.build());

		mergeHeader(notificationReceiver, baseDM, originalDMHeader);

		IProblemReporter problemReporterWithVisitedIdsStack = combineNotificationReceiverWithVisitedIds(notificationReceiver);

		Optional<IDocumentModel> result = DocumentModelJoiningService.INSTANCE.join(baseDM, joiningItems, locale,
				problemReporterWithVisitedIdsStack).map(new DocumentModelService()::convertToExternal);
		// modelInfo/name typically holds the same string as the model id, so we should probably set it too.
		// note that we do this _after_ the join in order to not affect the joinedModelsInfo (it should still mention the baseDms name)
		result.ifPresent(
				dm -> new DocumentModelService().convertFromExternal(dm).getContent().getModelInfo().setName(cmHeader.getId()));
		return result;
	}

	private void mergeHeader(Consumer<? super RankedNotification> notificationReceiver, DocumentModel baseDM,
			Header originalDMHeader) {
		IProblemReporter p = n -> notificationReceiver.accept(RankedNotificationImpl.of(
				n.getMessage() + " (" + this.formatVisitedIds() + ")",
				n.getSeverity(),
				n.getSource()
		));

		JoiningProblemReporter problemReporter = new JoiningProblemReporter(p);
		NameCollisionHandler nameCollisionHandler = new NameCollisionHandler(DocumentModelJoinUtil.getModelString(baseDM), locale,
				problemReporter);

		DocumentModelJoinUtil.mergeModelReferences(baseDM, originalDMHeader);
		DocumentModelJoinUtil.determineAndSetJoinOfAnnotations(baseDM.getHeader().getAnnotations(),
				originalDMHeader.getAnnotations(),
				annotations -> baseDM.setHeader(
						HeaderBuilderUtil.toBuilder(baseDM.getHeader()).withAnnotations(annotations).build()),
				() -> nameCollisionHandler.handleInconsistentAnnotations(Header.class, null),
				new AnnotationsOrigins(),
				problemReporter
		);
	}

	public String formatVisitedIds() {
		return String.join(" -> ", visitedIdsStacks);
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

	public ExpandingDmResolverImpl withVisitedIdAppended(String id) {
		if (visitedIdsStacks.contains(id)) {
			throw new CombinationException(String.format("Cycle detected while expanding, %s was already visited (%s)", id,
					formatVisitedIds()));
		}

		var extendedVisitedIds = Java8Utils.appendList(this.visitedIdsStacks, id);
		return ExpandingDmResolverImpl.of(internalResolver, locale, notificationReceiver, extendedVisitedIds);
	}

	/**
	 * (handles recursive expansion of any referenced DMs or CMs)
	 */
	private Stream<JoiningItem> toJoiningItems(@NonNull CombinationSteps step) {
		return switch (step.type()) {
			case Addition ->
					Stream.of(new DocumentModelJoiningItem(this.getDocumentModelByIdAsA12internalDM(step.additiveModel().dmId()),
							JoiningType.ADDITIVE_MODEL, DocumentModelJoiningItem.ElementOrderForAddition.ANCHOR_BASED_ORDERING));
			case Selection -> Stream.of(
					new SelectionJoiningItem(this.internalResolver.resolveSelectionModel(step.selectionModel().smId())._unwrap(),
							JoiningType.SELECTION));
			case DecorationForFields, DecorationForGroups -> Stream.of(
					new SelectionJoiningItem(this.internalResolver.resolveSelectionModel(step.selectionModel().smId())._unwrap(),
							JoiningType.SELECTION_DECORATION),
					new DocumentModelJoiningItem(this.getDocumentModelByIdAsA12internalDM(step.decorationModel().dmId()),
							convertType(step.type()),
							DocumentModelJoiningItem.ElementOrderForAddition.ANCHOR_BASED_ORDERING)
			);
		};
	}

	private JoiningType convertType(type type) {
		return switch (type) {
			case Addition -> JoiningType.ADDITIVE_MODEL;
			case Selection -> JoiningType.SELECTION;
			case DecorationForFields -> JoiningType.DECORATION_MODEL_FIELD;
			case DecorationForGroups -> JoiningType.DECORATION_MODEL_GROUP;
		};
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
