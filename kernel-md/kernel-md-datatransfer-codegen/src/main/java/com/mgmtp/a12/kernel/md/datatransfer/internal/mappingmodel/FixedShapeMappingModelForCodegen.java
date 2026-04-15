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

package com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel;

import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.cascadeNulls;
import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.mapList;
import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.setDifference;
import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.setIntersection;
import static com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer.mappingmodel.FixedShapePrecompModelDataMapper.mountPointRgPathForInput;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.DIFFERENT_TIMEZONES;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.INFO_NO_PRECOMPUTATION_FRAGMENT_SPECIFIED;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.NONERROR_MESSAGE_DURING_PRECOMPUTATION_DM_CREATION;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.NO_COMMON_LOCALES_BASE;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.NO_COMMON_LOCALES_PRECOMP;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.PRECOMPUTATION_FIELDS_IN_TARGET;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.PRECOMP_FRAGMENT_OVERLAPS_TARGET;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.PROBLEM_DURING_PRECOMPUTATION_DM_CREATION;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.SOURCE_ALLOWS_UNSUPPORTED_CHARACTERS;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.SOURCE_NAME_CLASHES_WITH_TARGET_ROOTGROUP;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.WARN_NOT_ALL_TARGET_LOCALES_SUPPORTED_BASEDM;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.WARN_NOT_ALL_TARGET_LOCALES_SUPPORTED_PRECOMP_DM;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum.WARN_PRECOMP_FRAGMENT_HAS_DIFFERENT_DECIMAL_SEPARATOR;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.FixedShapeMappingModelForCodegen.BaseOrPrecomp.CASE_BASE_DM;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.FixedShapeMappingModelForCodegen.BaseOrPrecomp.CASE_PRECOMP_DM;
import static com.mgmtp.a12.kernel.md.model.a12internal.services.join.JoiningType.ADDITIVE_MODEL;
import static com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.DocumentModelJoiningItem.ElementOrderForAddition.ANCHOR_BASED_ORDERING;
import static com.mgmtp.a12.kernel.md.model.api.IGroup.IIncludeDetails.IncludeLevel.MODEL_ROOT;
import static com.mgmtp.a12.kernel.md.model.api.IGroup.IIncludeDetails.IncludeLevel.SINGLE_RG;
import static com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils.getAllComputations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.base.utils.internal.CharsetUtil;
import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer.mappingmodel.FixedShapePrecompModelDataMapper;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer.mappingmodel.IPrecompModelDataMapper;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel.SourceAndTargetDMForSMM;
import com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferCodegenException;
import com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum;
import com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.LocaleIndependentNotification;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedPointer;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelConfig;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.DocumentModelJoiningItem;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.DocumentModelBuilder;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandService;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.utils.DmJoinUtils;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.kernel.md.rt.internal.service.Identifier;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAwareDocServiceImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.MMDocumentModelLoader;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.pointers._mm_mappingmodel_2.PContent;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views._mm_mappingmodel_2._content.Source;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views._mm_mappingmodel_2._content._source.includeLevel;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen_mm_mappingmodel__0032.Factory_codegen_mm_mappingmodel__0032;

import lombok.NonNull;

public class FixedShapeMappingModelForCodegen implements MappingModelForCodegen {
	public static final String PRECOMP_DM_ID = "_com_mgmtp_a12_kernel_internal_precomp_DM";
	public static final String BASE_DM_ID = "_com_mgmtp_a12_kernel_internal_base_DM_for_precomp";
	private static final String MM_CODEGEN = "extmpinternal/codegen-mm_mappingmodel-2.json";
	private static final PContent<MM_MappingModel_2> P_RG = MM_MappingModel_2._pointer().content();
	private static final TypedPointer<MM_MappingModel_2, List<Source>> P_SOURCES = P_RG.source();
	private static final TypedPointer<MM_MappingModel_2, String> P_TARGET_DM_ID = P_RG.target().dmId();
	private static final TypedPointer<MM_MappingModel_2, String> P_PRECOMP_FRAGMENT_DM_ID_IF_GIVEN = P_RG.preComputationFragment()
			.dmId();
	private static final TypedPointer<MM_MappingModel_2, String> P_STRUCTURALMAPPING_MODEL_ID__NULLABLE_IN_EXTMP = P_RG.structuralMappingModel()
			.id();
	private static final TypedPointer<MM_MappingModel_2, String> P_MAPPING_TYPE = P_RG.mappingType();

	private final MM_MappingModel_2 mappingModelDocument;
	private final Map<String, SourceSpec4Codegen> sourcesByName;
	private final List<SourceSpec4Codegen> inputSpecs;

	/**
	 * @return the FixedShapeMappingModelForCodegen, or null if model is invalid
	 */
	public static FixedShapeMappingModelForCodegen of(
			MM_MappingModel_2 model,
			Consumer<IMessage> notificationReceiverForModelValidation,
			Locale localeForModelValidationMessages) {
		boolean isValid = validateMappingModel(model, notificationReceiverForModelValidation, localeForModelValidationMessages);
		if (!isValid) {
			return null;
		}
		return new FixedShapeMappingModelForCodegen(model);
	}

	private FixedShapeMappingModelForCodegen(
			MM_MappingModel_2 prevalidatedMappingModel
	) {
		this.mappingModelDocument = prevalidatedMappingModel;
		this.inputSpecs = prevalidatedMappingModel._at(P_SOURCES).stream().map(SourceSpec4Codegen::of).toList();
		this.sourcesByName = inputSpecs.stream().collect(
				Collectors.toMap(SourceSpec4Codegen::name, Function.identity()));
	}

	/**
	 * An adapter for {@link Source}, in order to better decouple from the generated class
	 */
	private record SourceSpec4Codegen(String name, String validationDmId, int maxRepetitions,
									  IGroup.IIncludeDetails.IncludeLevel includeLevel, boolean noSourceValidation) {
		private static SourceSpec4Codegen of(Source source) {
			return new SourceSpec4Codegen(source.name(), source.dmId(),
					source.maxRepeat() != null ? source.maxRepeat().intValue() : 1,
					switch (nonNullIncludeLevel(source.includeLevel())) {
						case MODEL_ROOT -> MODEL_ROOT;
						case SINGLE_RG -> SINGLE_RG;
					},
					Boolean.TRUE.equals(source.noSourceValidation()));
		}
	}

	// the includeLevel is defined so that if the field is not set, i.e. null, then it is to be treated like SINGLE_RG
	private static includeLevel nonNullIncludeLevel(includeLevel levelOrNull) {
		return levelOrNull == null ? includeLevel.SINGLE_RG : levelOrNull;
	}

	@Override public List<String> sourceNames() {
		return mappingModelDocument._at(P_SOURCES).stream().map(Source::name).toList();
	}

	@Override public String getSourceValidationModelId(String sourceName) {
		return cascadeNulls(sourcesByName.get(sourceName), SourceSpec4Codegen::validationDmId);
	}

	@Override public boolean isNoSourceValidation(String sourceName) {
		return Optional.ofNullable(sourcesByName.get(sourceName)).map(SourceSpec4Codegen::noSourceValidation).orElse(false);
	}

	@Override public String getTargetModelId() {
		return this.mappingModelDocument._at(P_TARGET_DM_ID);
	}

	@Override public IDocumentModel createBaseDMForPrecomputationFragment(
			IDocumentModelResolver resolver,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications
	) {
		return new DocumentModelWrapper(
				createBaseDMForPrecomputationFragmentInternal(resolver, receiverForNonCriticalNotifications).baseDm());
	}

	private record BaseDmAndInfoForCheckMessages(DocumentModel baseDm, InfosForCheckMessages infosForCheckMessages) {
	}

	/**
	 * for now, the plan is to use the cached DMs only for the _messages_ of warning/error/info messages, but not
	 * for anything that can change other behavior of the code. This is because DMs can be mutated in place in
	 * various possibly-unexpected ways, and therefore it seems a bit risky to rely on "reused" DMs too much.
	 * If we only use them in order to fetch error message parameters, the worst that can happen is a confusing error message,
	 * but no otherwise incorrect behavior.
	 */
	private record InfosForCheckMessages(Map<String, DocumentModel> sourceDmsByIdMaybeModifiedOnlyForMetadataChecks,
										 DocumentModel targetDmMaybeModifiedOnlyForMetadataChecks,
										 DocumentModel precompFragmentNullable) {
		InfosForCheckMessages withPrecompFragment(DocumentModel precompFragment) {
			return new InfosForCheckMessages(sourceDmsByIdMaybeModifiedOnlyForMetadataChecks,
					targetDmMaybeModifiedOnlyForMetadataChecks,
					precompFragment);
		}
	}

	private BaseDmAndInfoForCheckMessages createBaseDMForPrecomputationFragmentInternal(IDocumentModelResolver resolver,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications) {
		// Warning: the dms are mutable and some operations (e.g. include expansion) may change them in place.
		//   (even IDocumentModel (vs DocumentModel), because it is implemented with a wrapper around a DocumentModel)
		var targetDM = resolveInternalAndRemoveMeta(resolver, this.mappingModelDocument._at(P_TARGET_DM_ID));
		Map<String, DocumentModel> sourceDmsById = this.inputSpecs.stream()
				.map(SourceSpec4Codegen::validationDmId)
				.distinct().collect(Collectors.toMap(id -> id, id -> resolveInternalAndRemoveMeta(resolver, id)));
		var allDMs = Stream.concat(Stream.of(targetDM), sourceDmsById.values().stream()).toList();

		// we can safely reuse the resolved DMs between checks (but not during operations that might modify them in place, like expansion)
		// report proper error in case of sources that don't use MODEL_ROOT includes but have DMs with repeatable or multiple RGs,
		// _before_ expansion
		checkAndGetRgPathsForNonModelRootSources(this.mappingModelDocument, sourceDmsById::get);
		var cachedInfosForCheckMessages = new InfosForCheckMessages(sourceDmsById, targetDM, null);
		checkAllDMsHaveSameTimeZone(allDMs, cachedInfosForCheckMessages);
		checkNoSourceNameCollidesWithTargetRg(targetDM);
		List<Locale> supportedLocales = checkAndGetSupportedLocalesIntersectionNonEmptyForBase(allDMs, cachedInfosForCheckMessages);
		warnIfSupportedLocalesLessThanTargetLocales(CASE_BASE_DM, supportedLocales, targetDM, receiverForNonCriticalNotifications);
		warnIfSourceCharsetIsLargerThanTarget(sourceDmsById, targetDM, receiverForNonCriticalNotifications);

		DocumentModel empty = DocumentModelBuilder.builder(BASE_DM_ID, supportedLocales).build();
		setModelConfigForBaseDm(empty, targetDM);
		// modelInfo seems not relevant here at the moment, but with A12K-3686 we might set the baseYear in modelInfo
		// iff all source/target dms have the same explicit baseYear. this would enable precomputations that
		// compare e.g. MMDD fields with YYYYMMDD or that use JahrAusDatum(f_MMDD)
		DocumentModel withInputsMounted = addMountpointsAndExpandIncludesInplace(
				empty, this.inputSpecs, id -> resolveInternalAndRemoveMeta(resolver, id), receiverForNonCriticalNotifications);
		copySourceRGAnnotations(withInputsMounted, sourceDmsById);
		DocumentModel withTargetAdded = addDMs(
				withInputsMounted, withoutRulesAndComputations(targetDM), receiverForNonCriticalNotifications);
		return new BaseDmAndInfoForCheckMessages(withTargetAdded, cachedInfosForCheckMessages);
	}

	@Override public IDocumentModel createPrecomputationDm(
			IDocumentModelResolver resolver,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications
	) {
		BaseDmAndInfoForCheckMessages baseDmAndInfosForChecks = createBaseDMForPrecomputationFragmentInternal(resolver,
				receiverForNonCriticalNotifications);
		DocumentModel baseDmWithSourcesAndTarget = baseDmAndInfosForChecks.baseDm();
		DocumentModel precomputationModel;
		String precompFragmentId = this.mappingModelDocument._at(P_PRECOMP_FRAGMENT_DM_ID_IF_GIVEN);
		if (precompFragmentId != null) {
			var precompFragmentDM = resolveInternalAndRemoveMeta(resolver, precompFragmentId);
			var allDMs = List.of(baseDmWithSourcesAndTarget, precompFragmentDM);
			InfosForCheckMessages infosForCheckMessages = baseDmAndInfosForChecks.infosForCheckMessages()
					.withPrecompFragment(precompFragmentDM);

			checkAllDMsHaveSameTimeZone(allDMs, infosForCheckMessages);
			// maybe we could get targetDm from infosForCheckMessages instead, but then we'd use the cached dm
			// for more than just the _messages_ of checks (but for the actual check condition too). This
			// might be problematic if some of the other operations modify targetDm (or start to modify it in some
			// future revision of the code). Given that the resolver caches the dm anyway (incl defensive copying)
			// in ExtMP, it seems cheap enough and safer to fetch targetDm again here and use the fresh instance for
			// the checks.
			var targetDm = resolveInternalAndRemoveMeta(resolver, this.mappingModelDocument._at(P_TARGET_DM_ID));
			checkDoesNotComputeIntoFieldsBelowTargetGroups(precompFragmentDM, targetDm);
			checkPrunedPrecompDoesNotAddIntoTargetGroups(precompFragmentDM, targetDm);
			List<Locale> supportedLocales = checkAndGetSupportedLocalesIntersectionNonEmptyForPrecomp(allDMs,
					infosForCheckMessages);
			warnIfSupportedLocalesLessThanTargetLocales(CASE_PRECOMP_DM, supportedLocales, baseDmWithSourcesAndTarget,
					receiverForNonCriticalNotifications);
			warnIfPrecompFragmentHasDifferentDecimalSeparator(precompFragmentDM, baseDmWithSourcesAndTarget,
					receiverForNonCriticalNotifications);

			// we set the model config before addition, in case the addition logic depends on some of the config
			DocumentModelBuilder.setSupportedLocales(baseDmWithSourcesAndTarget, supportedLocales);
			// the config for the condition language (incl shortnames) can be taken from the fragment (config from baseDmWithSourcesAndTarget can be ignored), because no rules or computations from baseDmWithSourcesAndTarget are present in the precomputationModel.
			cfg(baseDmWithSourcesAndTarget).setConditionLanguage(cfg(precompFragmentDM).getConditionLanguage());
			cfg(baseDmWithSourcesAndTarget).setFieldRefByShortNameAllowed(cfg(precompFragmentDM).isFieldRefByShortNameAllowed());
			// modelInfo seems not relevant here at the moment, but with A12K-3686 we might set the baseYear in modelInfo
			// iff baseDm (based on sources and target) and precomp dms have the same explicit baseYear. this would enable precomputations that
			// compare e.g. MMDD fields with YYYYMMDD or that use JahrAusDatum(f_MMDD)
			precomputationModel = addDMs(baseDmWithSourcesAndTarget, precompFragmentDM, receiverForNonCriticalNotifications);
		} else {
			receiverForNonCriticalNotifications.accept(
					LocaleIndependentNotification.of(Severity.INFO, INFO_NO_PRECOMPUTATION_FRAGMENT_SPECIFIED));
			precomputationModel = baseDmWithSourcesAndTarget;
		}
		// dm addition currently doesn't apply a new id for the result -> we set the id here manually
		// (this is done _after_ addition here, because otherwise the joinedModelsInfo will specify PRECOMP_DM_ID as one of the
		// _sources_ of the join, which looks odd. But I don't think anyone should really care about the joinedModelsInfo of
		// the precomputation DM, so maybe this doesn't matter)
		DocumentModelBuilder.setIdAndName(precomputationModel, PRECOMP_DM_ID);
		return new DocumentModelWrapper(precomputationModel);
	}

	/**
	 * For now, we assume that precomputations do not compute into fields that are below a target root group, because that would
	 * raise a bunch of questions and complexities that we might not need to support for now (would the computed values be expected
	 * to be included in the mapping result, even if the structural mapping doesn't touch them? Could the structural mapping use
	 * them as _source_ fields (if they are not actually target fields but just helper fields inside target groups?, ...)
	 * <p/>
	 * This method checks this assumption and throws a {@link DataTransferCodegenException} if the assumption does not hold
	 *
	 * @throws DataTransferCodegenException
	 * 		if the precomputation dm overlaps with the target
	 */
	private static void checkDoesNotComputeIntoFieldsBelowTargetGroups(DocumentModel precompFragmentDM, DocumentModel targetDm) {
		Set<String> targetRgNames = new HashSet<>(getRootGroupNamesWithoutMeta(targetDm));
		List<Computation> computationsInPrecompDm = getAllComputations(precompFragmentDM);
		Predicate<Computation> hasComputedFieldInTarget = comp -> targetRgNames.contains(
				ElementUtils.resolveRelativePath(comp, comp.getComputedField().getRelativePath()).split("/")[1]);
		List<String> fullnamesOfDisallowedComputations = computationsInPrecompDm.stream()
				.filter(hasComputedFieldInTarget)
				.map(ElementUtils::getPath)
				.sorted().toList();
		if (!fullnamesOfDisallowedComputations.isEmpty()) {
			throw new DataTransferCodegenException(PRECOMPUTATION_FIELDS_IN_TARGET,
					String.join(", ", fullnamesOfDisallowedComputations));
		}
	}

	/**
	 * NOTE: currently, the check is done on a pruned version of the precomp DM, that has all empty groups removed (recursively).
	 * <p/>
	 * Note: the instructions for creating the fragment in ExtMP actually do seem like the resulting fragment
	 * will not contain any (empty) target root groups - so maybe the pruning is only currently necessary for our old exported
	 * fragment model and will become unnecessary as soon as the fragment is re-exported;
	 * see ExtMP Wiki Shortlink 04VjH Section C.1.a.
	 */
	private static void checkPrunedPrecompDoesNotAddIntoTargetGroups(DocumentModel precompFragmentDM, DocumentModel targetDm) {
		precompFragmentDM = withEmptyGroupsPruned(precompFragmentDM);
		Set<String> precompRgs = new HashSet<>(getRootGroupNamesWithoutMeta(precompFragmentDM));
		Set<String> targetRgs = new HashSet<>(getRootGroupNamesWithoutMeta(targetDm));
		Set<String> overlapping = setIntersection(precompRgs, targetRgs);
		if (!overlapping.isEmpty()) {
			throw new DataTransferCodegenException(PRECOMP_FRAGMENT_OVERLAPS_TARGET, String.join(", ", overlapping));
		}
	}

	/**
	 * returns a dm that is like the given dm, but with all empty groups removed (recursively). The original dm is guaranteed to be
	 * kept unchanged.
	 */
	private static DocumentModel withEmptyGroupsPruned(DocumentModel dm) {
		// NOTE: there is some code for removing empty groups in kernel-md-join, but for this temporary workaround it seems easier
		//   to implement this here again
		dm = DocumentModelCopyService.copy(dm);
		pruneEmptySubgroupsInternal(dm.getContent().getModelRoot());
		return dm;
	}

	private static void pruneEmptySubgroupsInternal(Group group) {
		// we create a new elements list, because the existing one might be unmodifiable
		List<Element> newElements = new ArrayList<>();
		group.getElements().forEach(e -> {
			if (!(e instanceof Group subGroup)) {
				newElements.add(e);
			} else {
				pruneEmptySubgroupsInternal(subGroup);
				if (!subGroup.getElements().isEmpty()) {
					newElements.add(subGroup);
				}
			}
		});
		group.setElements(newElements);
	}

	private static DocumentModel resolveInternalAndRemoveMeta(IDocumentModelResolver externalResolver, String dmId) {
		return removeMetaGroupInPlace( // addition and include expansion don't currently support the special __meta root group,
				// so we remove it here in case it is present, as a workaround (A12K-3202, A12K-3203)
				unwrapIDocumentModel(externalResolver.getDocumentModelById(dmId)));
	}

	/**
	 * workaround for non-rootpreserving includes: include expansion does not currently annotate the mountpoint group with the
	 * annotations from the root group of the included DM, so those annotations would be lost.
	 * This method copies these annotations to the mountpoint group. If we at some point change include expansion
	 * so that it does this out of the box, then this method would become unnecessary
	 */
	// A12K-3335 can be discarded when A12K-3335 is implemented
	private void copySourceRGAnnotations(DocumentModel withInputsMounted, Map<String, DocumentModel> sourceDmsById) {
		var dmss = new DocumentModelSearchService(withInputsMounted);
		this.inputSpecs.stream()
				.filter(inp -> inp.includeLevel() == SINGLE_RG)
				.forEach(inputSpec -> {
					Group mountPoint = (Group) dmss.getByPath(mountPointRgPathForInput(inputSpec.name()))
							.filter(e -> e instanceof Group).orElseThrow();
					List<Group> sourceRgs = getRootGroupsWithoutMeta(sourceDmsById.get(inputSpec.validationDmId));
					assert sourceRgs.size() <= 1 : "only intended for single-RG dms in SINGLE_RG includes (not MODEL_ROOT), but got RGs: "
							+ sourceRgs.stream().map(Element::getName).toList();
					Group sourceRg = sourceRgs.getFirst();
					mountPoint.setAnnotations(
							sourceRg.getAnnotations()); // we assume the annotations are not changed (no deepcopy needed)
				});
	}

	private static void setModelConfigForBaseDm(DocumentModel baseDm, DocumentModel targetDM) {
		var cfg = cfg(baseDm);
		cfg.setTimeZone(cfg(targetDM).getTimeZone());
		cfg.setDecimalSeparator(cfg(targetDM).getDecimalSeparator());
		cfg.setSupportedCharacters(List.of("\u0000-\uFFFF"));
		// Note: the base dm contains no rules and computations, so the conditionLanguage and shortName settings probably
		// don't matter much. But using the settings from the target seems better than setting an arbitrary model-independent
		// default. Also, this may actually be relevant for the additiv-fragment editor, in case the editor does not allow
		// to  set a different condition language (in the case where the base contains no rules)
		cfg.setConditionLanguage(cfg(targetDM).getConditionLanguage());
		cfg.setFieldRefByShortNameAllowed(cfg(targetDM).isFieldRefByShortNameAllowed());
	}

	private void warnIfSourceCharsetIsLargerThanTarget(Map<String, DocumentModel> sourceDmsById, DocumentModel targetDM,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications) {
		sourceDmsById.forEach((sourceDmId, sourceDm) -> {
			if (!CharsetUtil.isCharsetContained(cfg(targetDM).getSupportedCharacters(),
					cfg(sourceDm).getSupportedCharacters())) { // NOTE: we don't care about the annotation
				// MODEL_DEACTIVATE_LEGALCHARCHECK, which can allow charsets larger than 0x0000-0xFFFF, and which is not
				// taken into account by getSupportedCharacters(). That annotation is a special hack (for TMT?) and not generally
				// supported
				receiverForNonCriticalNotifications.accept(LocaleIndependentNotification.warning(
						SOURCE_ALLOWS_UNSUPPORTED_CHARACTERS, sourceDmId, id(targetDM)
				));
			}
		});
	}

	enum BaseOrPrecomp {CASE_BASE_DM, CASE_PRECOMP_DM}

	private void warnIfSupportedLocalesLessThanTargetLocales(BaseOrPrecomp affectedDm, List<Locale> supportedLocales,
			DocumentModel targetOrBaseDm,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications) {
		Set<Locale> unsupportedTargetLocales = setDifference(targetOrBaseDm.getHeader().getLocales(), supportedLocales);
		if (!unsupportedTargetLocales.isEmpty()) {
			DataTransferErrorEnum warningCode = switch (affectedDm) {
				case CASE_BASE_DM -> WARN_NOT_ALL_TARGET_LOCALES_SUPPORTED_BASEDM;
				case CASE_PRECOMP_DM -> WARN_NOT_ALL_TARGET_LOCALES_SUPPORTED_PRECOMP_DM;
			};
			receiverForNonCriticalNotifications.accept(LocaleIndependentNotification.warning(warningCode,
					String.join(", ", unsupportedTargetLocales.stream().map(Locale::toString).sorted().toList())));
		}
	}

	private void warnIfPrecompFragmentHasDifferentDecimalSeparator(DocumentModel precompFragmentDM, DocumentModel targetDM,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications) {
		String precompFragmentDS = cfg(precompFragmentDM).getDecimalSeparator();
		String targetDS = cfg(targetDM).getDecimalSeparator();
		if (!precompFragmentDS.equals(targetDS)) {
			receiverForNonCriticalNotifications.accept(
					LocaleIndependentNotification.warning(WARN_PRECOMP_FRAGMENT_HAS_DIFFERENT_DECIMAL_SEPARATOR, precompFragmentDS,
							targetDS));
		}
	}

	/**
	 * Warnung: do not rely on this id in critical cases: dm resolvers may return a DM where the id in the dm's header does not
	 * match the id for which it was resolved.
	 */
	private static String id(DocumentModel dm) {
		return dm.getHeader().getId();
	}

	/**
	 * @return the DocumentModelConfig of the dm
	 */
	private static DocumentModelConfig cfg(DocumentModel dm) {
		return dm.getContent().getModelConfig();
	}

	/**
	 * Note: locales are currently compared with simple equality. Therefore, e.g. the intersection of {@code {de}} and
	 * {@code {de_DE}} will be the empty set, rather than {@code {de}}.
	 */
	private List<Locale> supportedLocalesIntersection(Collection<DocumentModel> dms) {
		return dms.stream().map(dm -> dm.getHeader().getLocales())
				.collect(Java8Utils.setIntersectionCollector(
						null)) // maybe this is stricter than necessary, because Locales might be unequal and
				// nonetheless "overlapping", e.g. "de_DE" and "de" or so?
				.stream().sorted(Comparator.comparing(Locale::toString)).toList();
	}

	@Override public SourceAndTargetDMForSMM createSourceAndTargetDMForSMM(IDocumentModelResolver resolver,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications) {
		DocumentModel precomputationDM = unwrapIDocumentModel(
				createPrecomputationDm(resolver, receiverForNonCriticalNotifications));
		DocumentModel precomputationDMWithoutComputations = withoutRulesAndComputations(precomputationDM);
		var rawTargetDM = resolveInternalAndRemoveMeta(resolver, this.mappingModelDocument._at(P_TARGET_DM_ID));
		Set<String> targetRootGroupNames = Set.copyOf(getRootGroupNamesWithoutMeta(rawTargetDM));
		DocumentModel targetDM = withRootGroupsFiltered(precomputationDMWithoutComputations,
				rootGroup -> targetRootGroupNames.contains(rootGroup.getName()));
		DocumentModel sourceDM = withRootGroupsFiltered(precomputationDMWithoutComputations,
				rootGroup -> !targetRootGroupNames.contains(rootGroup.getName()));
		return new SourceAndTargetDMForSMM(sourceDM, targetDM);
	}

	private static DocumentModel withRootGroupsFiltered(DocumentModel dm, Predicate<Group> rootGroupPredicate) {
		DocumentModel filteredCopy = DocumentModelCopyService.copy(dm);
		List<Element> rootGroups = new ArrayList<>(filteredCopy.getContent().getModelRoot().getElements());
		rootGroups.removeIf(element -> !(element instanceof Group group && rootGroupPredicate.test(group)));
		filteredCopy.getContent().getModelRoot().setElements(rootGroups);
		return filteredCopy;
	}

	@Override public String getStructuralMappingModelId() {
		return this.mappingModelDocument._at(P_STRUCTURALMAPPING_MODEL_ID__NULLABLE_IN_EXTMP);
	}

	@Override public String getMappingType() {
		return this.mappingModelDocument._at(P_MAPPING_TYPE);
	}

	@Override public @NonNull Map<String, String> getOriginalRootGroupNamesBySourceName(@NonNull IDocumentModelResolver resolver) {
		Map<String, String> rgPathsByDmId = checkAndGetRgPathsForNonModelRootSources(
				this.mappingModelDocument, dmId -> unwrapIDocumentModel(resolver.getDocumentModelById(dmId)));
		return this.mappingModelDocument._at(P_SOURCES).stream()
				.collect(Collectors.toMap(
						Source::name,
						source -> switch (nonNullIncludeLevel(source.includeLevel())) {
							case SINGLE_RG -> rgPathsByDmId.get(source.dmId()).substring(1);
							case MODEL_ROOT -> "";
						}));
	}

	@Override public IPrecompModelDataMapper rtPrecompModelDataMapper(IDocumentModelResolver resolver) {
		Map<String, String> rgPathsByDmId = checkAndGetRgPathsForNonModelRootSources(
				this.mappingModelDocument, dmId -> unwrapIDocumentModel(resolver.getDocumentModelById(dmId)));
		String precompFragmentId = this.mappingModelDocument._at(P_PRECOMP_FRAGMENT_DM_ID_IF_GIVEN);
		return FixedShapePrecompModelDataMapper.of(
				mapList(this.inputSpecs, idef -> inputDefToRtSpec(idef, rgPathsByDmId)),
				precompFragmentId != null
						? fieldFullNamesInDm(resolver.getDocumentModelById(precompFragmentId))
						: Set.of()
		);
	}

	private Set<String> fieldFullNamesInDm(IDocumentModel documentModelById) {
		Set<String> fieldFullNames = new HashSet<>();
		class FieldFullnamesCollector extends DocumentModelVisitor {
			@Override public DocumentModelWalker.VisitProcess visitField(IField field) {
				fieldFullNames.add(ElementUtils.getPath(field));
				return super.visitField(field);
			}
		}
		new DocumentModelWalker().acceptDocumentModel(documentModelById, new FieldFullnamesCollector());
		return fieldFullNames;
	}

	private static DocumentModel unwrapIDocumentModel(IDocumentModel documentModel) {
		return ((DocumentModelWrapper) documentModel).getDocumentModel(); // we don't support other impls anyway
	}

	private static DocumentModel addMountpointsAndExpandIncludesInplace(
			DocumentModel rootDm,
			List<SourceSpec4Codegen> includes,
			Function<String, DocumentModel> resolver,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications
	) {
		for (var inputSpec : includes) {
			List<String> mountPath = ElementPathUtils.splitAbsPath(
					mountPointRgPathForInput(inputSpec.name()));
			DocumentModelBuilder.addInclude(
					rootDm, inputSpec.validationDmId(), mountPath, inputSpec.maxRepetitions(), inputSpec.includeLevel());
		}
		expandIncludesAndRemoveRulesAndComputationsFromIncludedDms(rootDm, resolver::apply, receiverForNonCriticalNotifications);
		return rootDm;
	}

	/**
	 * remove the optional __meta root group that has recently been introduces in A12, gets sometimes automatically added by SME
	 * (e.g. when copying DMs), and which is currently not supported by our include expansion (the included DM will be rejected
	 * because
	 * it has multiple root groups. Most likely, __meta is also not properly supported by dm addition: It probably gets treated
	 * like
	 * a regular group, but we don't want the precomptutaion DM to end up with some meta attributes from target or from the
	 * precomputation fragment.
	 * <p/>
	 * A12K-3202, A12K-3203: when A12K-3202 and A12K-3203 are fixed _and_ if DM-addition is changed to discard __meta root groups
	 * from the added DM (here the
	 * precomp fragment and the target), then we can remove this workaround method
	 */
	private static DocumentModel removeMetaGroupInPlace(DocumentModel dm) {
		dm.getContent().getModelRoot().setElements(
				dm.getContent().getModelRoot().getElements().stream().filter(e -> !"__meta".equals(e.getName())).toList());
		return dm;
	}

	private static void expandIncludesAndRemoveRulesAndComputationsFromIncludedDms(
			DocumentModel baseDm,
			Function<String, DocumentModel> resolver,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications
	) {
		DocumentModelExpandService.expand(baseDm,
				// NOTE: we could alternatively use the removeRules flag of the includes, but I believe we should avoid depending on
				// that, because it seems like an unnecessary complexity in the include-expansion logic, that we might want to remove
				// in the future.
				dmId -> withoutRulesAndComputations(resolver.apply(dmId)),
				throwOnError_ReportIfNonError(receiverForNonCriticalNotifications));
	}

	public static DocumentModel withoutRulesAndComputations(DocumentModel dm) {
		// NOTE: the source DMs can't really have computations (unless the computation happens to compute the same value that is
		// manually given), because a validation rule will be generated for the computation, which otherwise fails and results in
		// the input being excluded from the mapping.
		// Or do we want to run computations for the individual inputs, for some reason?
		return DmJoinUtils.withoutRulesAndComputations( // this discards groups that are mountpoints of includes :/
				dm,
				p -> {
					if (p.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) { // this can probably not happen
						throw new DataTransferCodegenException(p, PROBLEM_DURING_PRECOMPUTATION_DM_CREATION, p.getMessage());
					}
					assert false : "we do not expect warnings or infos to occur during deselection " +
							"of rules and computations, but got: " + p;
					// we don't report such cases for now, because this would complicate usage, in particular in
					// AbstractDataTransferCodeGenerator, and we don't currently expect that _any_ relevant issues could occur
					// during deselection of rules and computations
				}
		).orElseThrow();
	}

	private void checkNoSourceNameCollidesWithTargetRg(DocumentModel targetDm) {
		Set<String> rootGroupNamesInTarget = new HashSet<>(getRootGroupNamesWithoutMeta(targetDm));
		this.sourceNames().forEach(sourceName -> {
			if (rootGroupNamesInTarget.contains(sourceName)) {
				throw new DataTransferCodegenException(SOURCE_NAME_CLASHES_WITH_TARGET_ROOTGROUP, sourceName);
			}
		});
	}

	/**
	 * checks that, for all sources that have {@link Source#includeLevel()} {@link includeLevel#SINGLE_RG} (or null),
	 * the dm has exactly one root group, and that root group is non-repeatable.
	 *
	 * @throws DataTransferCodegenException
	 * 		see {@link #checkAndGetSingleNonRepeatableRootGroupPath(DocumentModel)} for the
	 * 		exceptions that are raised if the  requirements are not satisfied
	 */
	private static Map<String, String> checkAndGetRgPathsForNonModelRootSources(
			MM_MappingModel_2 mappingModelDocument,
			Function<String, DocumentModel> getDmById
	) {
		return fn2Map(
				dmIdsOfNonModelRootSources(mappingModelDocument),
				dmId -> checkAndGetSingleNonRepeatableRootGroupPath(getDmById.apply(dmId)));
	}

	private static <K, V> Map<K, V> fn2Map(Stream<K> domain, Function<K, V> fn) {
		return domain.distinct().collect(Collectors.toMap(k -> k, fn));
	}

	private static Stream<String> dmIdsOfNonModelRootSources(MM_MappingModel_2 mappingModel) {
		return mappingModel._at(P_SOURCES).stream()
				.filter(s -> nonNullIncludeLevel(s.includeLevel()) != includeLevel.MODEL_ROOT)
				.map(Source::dmId)
				.distinct();
	}

	/**
	 * @throws DataTransferCodegenException
	 * 		with {@link DataTransferCodegenException#getErrorType()} <ul>
	 * 		<li>{@link DataTransferErrorEnum#SOURCE_HAS_MULTIPLE_ROOTGROUPS} if the DM has more than one root group.</li>
	 * 		<li>{@link DataTransferErrorEnum#SOURCE_ROOTGROUP_REPEATABLE} if the DM's root group is repeatable.</li>
	 * 		</ul>
	 */
	private static String checkAndGetSingleNonRepeatableRootGroupPath(DocumentModel dm) {
		List<Group> rootGroups = getRootGroupsWithoutMeta(dm);
		if (rootGroups.size() != 1) {
			throw new DataTransferCodegenException(DataTransferErrorEnum.SOURCE_HAS_MULTIPLE_ROOTGROUPS,
					id(dm), rootGroups.stream().map(Group::getName).sorted().collect(Collectors.toList()));
		}
		if (rootGroups.get(0).getRepeatability() != 1) {
			throw new DataTransferCodegenException(DataTransferErrorEnum.SOURCE_ROOTGROUP_REPEATABLE,
					id(dm), "/" + rootGroups.get(0).getName());
		}
		return "/" + rootGroups.get(0).getName();
	}

	/**
	 * Reason for this check:<br>
	 * We can only support the intersection of the supported locales of all DMs:
	 * Fields from all part DMs (sources, precomp, target) can be inputs to precomputations and will therefore be formally
	 * validated. If the formal validation fails, the field's error text for the codegen locale will be needed to report
	 * the error. (at least for fields with enum type, the error text is probably needed)
	 */
	private List<Locale> checkAndGetSupportedLocalesIntersectionNonEmptyForPrecomp(
			Collection<DocumentModel> dms,
			InfosForCheckMessages infosForCheckMessages
	) {
		List<Locale> supportedLocales = supportedLocalesIntersection(dms);
		if (supportedLocales.isEmpty()) {
			String targetLocales = localesString(infosForCheckMessages.targetDmMaybeModifiedOnlyForMetadataChecks());
			String precompFragmentLocales = localesString(infosForCheckMessages.precompFragmentNullable());
			String sourcesLocales = getSourcesLocales(infosForCheckMessages);
			throw new DataTransferCodegenException(NO_COMMON_LOCALES_PRECOMP,
					targetLocales,
					precompFragmentLocales,
					sourcesLocales);
		}
		return supportedLocales;
	}

	/**
	 * see {@link #checkAndGetSupportedLocalesIntersectionNonEmptyForPrecomp(Collection, InfosForCheckMessages)}
	 */
	private List<Locale> checkAndGetSupportedLocalesIntersectionNonEmptyForBase(
			Collection<DocumentModel> dms,
			InfosForCheckMessages infosForCheckMessages
	) {
		List<Locale> supportedLocales = supportedLocalesIntersection(dms);
		if (supportedLocales.isEmpty()) {
			String targetLocales = localesString(infosForCheckMessages.targetDmMaybeModifiedOnlyForMetadataChecks());
			String sourcesLocales = getSourcesLocales(infosForCheckMessages);
			throw new DataTransferCodegenException(NO_COMMON_LOCALES_BASE,
					targetLocales,
					sourcesLocales);
		}
		return supportedLocales;
	}

	private static String getSourcesLocales(InfosForCheckMessages infosForCheckMessages) {
		String sourcesLocales = infosForCheckMessages.sourceDmsByIdMaybeModifiedOnlyForMetadataChecks().entrySet().stream()
				.map(e -> e.getKey() + ": " + localesString(e.getValue()))
				.sorted().collect(Collectors.joining("; "));
		return sourcesLocales;
	}

	private static String localesString(DocumentModel dm) {
		return dm.getHeader().getLocales().stream().map(Locale::toString).sorted().collect(Collectors.joining(", "));
	}

	/**
	 * @return the names of the dm's root groups (without leading "/")
	 */
	private static List<String> getRootGroupNamesWithoutMeta(DocumentModel dm) {
		return getRootGroupsWithoutMeta(dm).stream()
				.map(Element::getName)
				.toList();
	}

	private static List<Group> getRootGroupsWithoutMeta(DocumentModel dm) {
		return dm.getContent().getModelRoot().getElements().stream()
				.filter(e -> e instanceof Group)
				// should the __meta filter be extracted to a general util method (other code locations might also need the "non-meta" root groups, and are maybe broken since the introduction of __meta (e.g. include expansoin seems broken!
				// (this filter is currently redundant to removeMetaGroupInPlace, but that method is only a temporary workaround. when it gets removed, we'll _need_ it again)
				.filter(e -> !"__meta".equals(e.getName()))
				.map(e -> (Group) e).toList();
	}

	private void checkAllDMsHaveSameTimeZone(List<DocumentModel> dms, InfosForCheckMessages infosForCheckMessages) {
		if (dms.stream().map(dm -> cfg(dm).getTimeZone()).distinct().count() > 1) {
			// for the error message, we report the ids as referenced in the mapping model (they may be different than the ids contained in the resolved dms)
			// and we should report for the sources and target separately, instead of reporting the timezone of the base dm, which is not well-visible for the user.
			List<String> inputDmIdsAndTimezones = new ArrayList<>();
			if (infosForCheckMessages.precompFragmentNullable() != null) { // put the fragment first if present: in this case, it is the most likely problem because target and source probably already match
				inputDmIdsAndTimezones.add(this.mappingModelDocument._at(P_PRECOMP_FRAGMENT_DM_ID_IF_GIVEN) + ": " + cfg(
						infosForCheckMessages.precompFragmentNullable()).getTimeZone());
			}
			inputDmIdsAndTimezones.add(this.mappingModelDocument._at(P_TARGET_DM_ID) + ": " +
					cfg(infosForCheckMessages.targetDmMaybeModifiedOnlyForMetadataChecks()).getTimeZone());
			infosForCheckMessages.sourceDmsByIdMaybeModifiedOnlyForMetadataChecks().forEach((id, dm) -> {
				inputDmIdsAndTimezones.add(id + ": " + cfg(dm).getTimeZone());
			});
			throw new DataTransferCodegenException(DIFFERENT_TIMEZONES, String.join(", ", inputDmIdsAndTimezones));
		}
	}

	private static FixedShapePrecompModelDataMapper.SourceSpec4Runtime inputDefToRtSpec(SourceSpec4Codegen inputDef,
			Map<String, String> singleRootGroupNamesByDmIdForNonModelRootSources) {
		return FixedShapePrecompModelDataMapper.SourceSpec4Runtime.of(
				inputDef.name(), inputDef.maxRepetitions(),
				switch (inputDef.includeLevel) {// NOTE: we must check this flag and not rely on the value from
					// singleRootGroupNamesByDmIdForNonModelRootSources, because the same dm could theoretically be used in
					// multiple sources, both with and without includeLevel()
					case MODEL_ROOT -> "";
					case SINGLE_RG -> singleRootGroupNamesByDmIdForNonModelRootSources.get(inputDef.validationDmId());
				}
		);
	}

	private static DocumentModel addDMs(DocumentModel a, DocumentModel b,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications) {
		return DocumentModelJoiningService.INSTANCE.join(
				a,
				List.of(new DocumentModelJoiningItem(b, ADDITIVE_MODEL, ANCHOR_BASED_ORDERING)),
				Locale.US /*error reporting should not matter?*/,
				throwOnError_ReportIfNonError(receiverForNonCriticalNotifications)
		).orElseThrow(/* this should never happen, because join() promises to only return empty when critical issues (ERROR
		severity) have occurred, and in that case throwOnError_ReportIfNonError would have already aborted the join operation */);
	}

	/**
	 * Returns an {@link IProblemReporter} that behaves as follows:
	 * <ul>
	 *   <li>If a problem with <em>error</em> severity (or higher) occurs:<br/>
	 *     <em>Throws</em> a DataTransferCodegenException with type {@link DataTransferErrorEnum#PROBLEM_DURING_PRECOMPUTATION_DM_CREATION}.
	 *   </li>
	 *   <li>For problems with lower severity (warning/info):<br/>
	 *     <em>Reports</em> to the notification receiver a message with type {@link DataTransferErrorEnum#NONERROR_MESSAGE_DURING_PRECOMPUTATION_DM_CREATION}.
	 *   </li>
	 * </ul>
	 */
	private static IProblemReporter throwOnError_ReportIfNonError(
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications) {
		return p -> {
			if (p.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) {
				throw new DataTransferCodegenException(p, PROBLEM_DURING_PRECOMPUTATION_DM_CREATION, p.getMessage());
			} else {
				receiverForNonCriticalNotifications.accept(
						LocaleIndependentNotification.of(p.getSeverity(), NONERROR_MESSAGE_DURING_PRECOMPUTATION_DM_CREATION,
								p.getMessage()));
			}
		};
	}

	private static boolean validateMappingModel(
			MM_MappingModel_2 mappingModelConfig,
			Consumer<IMessage> notificationReceiver,
			Locale localeForValidationMessages
	) {
		var mmGenCodeFactory = Factory_codegen_mm_mappingmodel__0032.getInstance();
		IDocumentModelResolver modelResolver = MMDocumentModelLoader.createResolver(MM_CODEGEN);
		DocumentV2 codegenDm = DmAwareDocServiceImpl.ofResolver(MM_CODEGEN, modelResolver)
				.removeUnknowns(mappingModelConfig._unwrap());

		IDocumentValidationResult validationResult = LowKernelApiUtils.callValidator(
				LowKernelApiUtils.ValidationMethod.FULL_VALIDATION,
				mmGenCodeFactory.getValidator(),
				mmGenCodeFactory.getMetaObject(),
				codegenDm,
				null,
				DocumentProcessingConfig.builder(localeForValidationMessages).build(),
				id -> new Identifier(id.getName(), id.getIndices()).toString());
		validationResult.getMessages().forEach(notificationReceiver); // transfer errors _and_ warnings (warnings must be
		// transferred even if no error occurred, i.e. even if the model is valid)
		return validationResult.noErrorOccurred();
	}
}
