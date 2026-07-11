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

import static com.mgmtp.a12.kernel.md.model.a12internal.ExpansionPurposes.DM_MANAGED_PURPOSES;
import static com.mgmtp.a12.kernel.md.model.a12internal.ExpansionPurposes.DM_PURPOSE_INCLUDE;
import static com.mgmtp.a12.kernel.md.model.a12internal.ExpansionPurposes.DM_PURPOSE_TYPEDEF;
import static com.mgmtp.a12.kernel.md.model.api.IGroup.IIncludeConfig.IncludeLevel.MODEL_ROOT;
import static com.mgmtp.a12.kernel.md.model.api.IGroup.IIncludeConfig.IncludeLevel.SINGLE_RG;
import static com.mgmtp.a12.kernel.md.model.internal.utils.ExpansionUtils.canAddNewTypeDef;
import static com.mgmtp.a12.kernel.md.model.internal.utils.ExpansionUtils.createError;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.parser.internal.ParserService;
import com.mgmtp.a12.kernel.core.service.internal.EntityPathsRelativizer;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.string.sourcemap.StringWithFragmentsOriginMapEntry;
import com.mgmtp.a12.kernel.md.combination.a12internal.ExpandingDmResolver;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelRoot;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdge;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdgeLabel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionNode;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.LocalCoordinates;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.ElementContainer;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DMWithExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil;
import com.mgmtp.a12.kernel.md.model.internal.expansioninfo.ExpansionInfoUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandError;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.IRuleFromComputation;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ComputationAlternativesJoiner;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelServiceUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.WrappedProblemWithSeverityLimitAndOrigin;
import com.mgmtp.a12.kernel.md.model.internal.utils.ExpansionUtils;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.ElementWrapper;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.ModelReference;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

/**
 * A service that replaces included contents by their include elements.
 */
public class DocumentModelExpandService {
	private final DocumentModel baseDocumentModel; // updated in-place
	private final Map<LocalCoordinates, ExpansionNode> origins; // updated in-place
	private final boolean createExpansionInfo;
	private final ExpandingDmResolver modelResolver;
	private final Consumer<RankedNotification> notificationReceiver;

	/**
	 * *Note*: nested {@code CombinationModel} not handled, use {@code CombinationModelService.expand} instead.
	 */
	public static void expand(DocumentModel baseDocumentModel,
			final ExpandingDmResolver modelResolver, Consumer<RankedNotification> notificationReceiver) {
		var unusedEI = expand(baseDocumentModel, modelResolver, notificationReceiver, false);
		assert unusedEI.isEmpty() : "passed createExpansionInfo=false, did not expect to get an expansionInfo back";
	}

	/**
	 * *Note*: nested {@code CombinationModel} not handled, use {@code CombinationModelService.expand} instead.
	 */
	public static @NonNull Optional<ExpansionInfo> expand(DocumentModel baseDocumentModel, final ExpandingDmResolver modelResolver,
			Consumer<RankedNotification> notificationReceiver, boolean createExpansionInfo) {
		return new DocumentModelExpandService(baseDocumentModel, modelResolver, notificationReceiver, createExpansionInfo)
				.expandInternal();
	}

	private DocumentModelExpandService(final DocumentModel baseDocumentModel, final ExpandingDmResolver modelResolver,
			Consumer<RankedNotification> notificationReceiver, boolean createExpansionInfo) {
		this.baseDocumentModel = baseDocumentModel;
		this.origins = new HashMap<>();
		this.createExpansionInfo = createExpansionInfo;
		this.modelResolver = modelResolver;
		this.notificationReceiver = notificationReceiver;
	}

	private @NonNull Optional<ExpansionInfo> expandInternal() {
		List<Group> includeGroups = collectIncludeMountPoints(baseDocumentModel);
		List<ModelReference> imports = collectImports(baseDocumentModel.getHeader());

		if (includeGroups.isEmpty() && imports.isEmpty() && collectIncludeModelRefs().isEmpty()) {
			// If there are no imports and no includes, i.e. nothing to expand, then don't touch anything
			// leave the baseDm unchanged, and just return the all-local expansionInfo.
			// (note: for consistent models, includeGroups.isEmpty() and collectIncludeModelRefs().isEmpty() should be equal, but
			//        let's be robust against models with inconsistent model refs here, and only leave the model untouched if there
			//        are also no unused include model refs in the DM)
			if (createExpansionInfo) {
				// add origin info for stuff that comes from baseDocumentModel, as the model is not adapted,
				// we can have it as "self" origin
				ExpansionInfoUtils.mergeInto(origins, LocalCoordinatesModelVisitor.getAllAsLocal(baseDocumentModel));
				return Optional.of(ExpansionInfo.of(baseDocumentModel.getHeader().getId(), origins));
			} else {
				return Optional.empty();
			}
		}
		// (now we know there is actually an expand to be done)

		if (createExpansionInfo) { // add origin info for stuff that comes from baseDocumentModel
			ExpansionInfoUtils.mergeInto(origins, LocalCoordinatesModelVisitor.getAllAsIncludeBase(baseDocumentModel));
		}

		handleImports(imports);
		for (var group : includeGroups) {
			handleInclude(group);
		}

		// remove header modelReferences corresponding to the now-expanded includes and imports (but also any potentially (wierdly) leftover modelReferences from other expansion-related things like combination models, hypothetically):
		ExpansionUtils.removeAllManagedModelReferencesInPlace(baseDocumentModel);
		normalizeTypedefsList(baseDocumentModel);
		HeaderBuilderUtil.normalizeModelRefsList(baseDocumentModel);
		// Resolve/update within-dm reference pointers (error fields, computed fields, sort fields, typedefs, etc)
		// We ignore errors during reference resolving, as the DM might not be fully valid by itself (e.g. additive documents)
		// The user has to validate the result in their context (e.g. after joining)
		DocumentModelUtils.resolveAndUpdateReferences(baseDocumentModel, p -> {
		});
		return createExpansionInfo
				? Optional.of(ExpansionInfo.of(baseDocumentModel.getHeader().getId(), origins))
				: Optional.empty();
	}

	private List<Group> collectIncludeMountPoints(DocumentModel dm) {
		List<Group> result = new ArrayList<>();
		new DocumentModelWalker().acceptDocumentModel(dm, new DocumentModelVisitor() {
			@Override
			public VisitProcess visitGroup(Group group) {
				if (group.getIncludeConfig().isPresent()) {
					result.add(group);
				}
				return VisitProcess.CONTINUE_TRAVERSAL;
			}
		});
		return result;
	}

	private List<ModelReference> collectImports(final Header header) {
		return CollectionUtils.emptyIfNull(header.getModelReferences())
				.stream()
				.filter(mr -> DM_PURPOSE_TYPEDEF.equals(mr.getPurpose()))
				.toList();
	}

	private List<ModelReference> collectIncludeModelRefs() {
		return ListUtils.emptyIfNull(baseDocumentModel.getHeader().getModelReferences()).stream()
				.filter(mr -> DM_PURPOSE_INCLUDE.equals(mr.getPurpose()) && "document".equals(mr.getModelType()))
				.toList();
	}

	private void handleImports(List<ModelReference> imports) {
		imports.forEach(this::handleImport);
	}

	private void handleImport(ModelReference modelRef) {
		assert DM_PURPOSE_TYPEDEF.equals(modelRef.getPurpose());

		DMWithExpansionInfo dmWithExpansionInfo = modelResolver.a12internal_resolveById(modelRef.getReference(),
				createExpansionInfo);
		List<FieldTypeDefinition> importedTDs = dmWithExpansionInfo.dm().getContent().getTypeDefinitions();
		ExpansionInfo nestedExpansionInfo = dmWithExpansionInfo.ei().orElse(null);
		assert createExpansionInfo == (null != nestedExpansionInfo);

		importedTDs.forEach(
				td -> addTypeDefToDmAndRejectIdCollisions(baseDocumentModel, td, notificationReceiver));
		addModelRefsFromImportedModelToBaseDm(dmWithExpansionInfo.dm().getHeader().getModelReferences(), dmWithExpansionInfo.ei());
		// neither model annotations nor elements are transferred during an import - nothing else to transfer except TDs and
		// (unmanaged) modelRefs

		if (nestedExpansionInfo != null) {
			String currentModelId = baseDocumentModel.getHeader().getId();
			ExpansionNode modelDetailsOrigin = ExpansionNode.of(
					LocalCoordinates.ModelItself.of(), currentModelId, ExpansionEdge.of(
							ExpansionEdgeLabel.Dm.Import.of(),
							nestedExpansionInfo.getOrigin(LocalCoordinates.ModelItself.of())
					));
			ExpansionInfoUtils.mergeInto(origins, modelDetailsOrigin);

			importedTDs.forEach(td -> {
				var localCoords = LocalCoordinates.Typedef.of(
						td.getId()); // td id doesn't change, so those coords are valid both in the import DM and in the base DM
				ExpansionNode typeDefOrigin = ExpansionNode.of(localCoords,
						currentModelId,
						ExpansionEdge.of(
								ExpansionEdgeLabel.Dm.Import.of(),
								nestedExpansionInfo.getOrigin(localCoords)
						));

				ExpansionInfoUtils.mergeInto(origins, typeDefOrigin);
			});
		}
	}

	private void handleInclude(final Group group) {
		final String includedDmId = checkAndGetIncludedDmId(group);
		if (includedDmId == null) {
			return; // tolerate some likely-inconsistent states and simply leave them untouched.
		}

		DMWithExpansionInfo dmWithExpansionInfo = modelResolver.a12internal_resolveById(includedDmId, createExpansionInfo);
		DocumentModel includedDM = dmWithExpansionInfo.dm();
		Optional<ExpansionInfo> includedDmExpansionInfo = dmWithExpansionInfo.ei();
		Group.IncludeConfig includeConfig = group.getIncludeConfig().orElseThrow();
		if (!checkAndReportResolvedModelNotNull(includedDM, includedDmId, notificationReceiver)
				|| !checkIncludedModelForCompatibility(includeConfig, includedDM, includedDmId)) {
			// no document model or not compatible, no expansion
			return;
		}

		removeRulesAndComputationsIfNecessary(includedDM, includeConfig.isExcludeRules(),
				includeConfig.isExcludeComputations());
		// assign new element ids, so we are safe against id collisions even if the same model is included in multiple places within
		// the same baseDocumentModel
		ElementIdUpdaterVisitor idUpdaterVisitor = new ElementIdUpdaterVisitor(group.getId() + "_");
		new DocumentModelWalker().acceptDocumentModel(includedDM, idUpdaterVisitor);
		// resolve references in includedDM (e.g. for error fields) that have just now become resolveable, because
		// includedDM has now been expanded (sub-includes have been resolved).
		// This is necessary because parsing in ConditionPathRelativizerVisitor will otherwise fail for rules
		// that reference an error field from a sub-include of includedDM (those references have not been resolved
		// initially)
		DocumentModelUtils.resolveAndUpdateReferences(includedDM,
				p -> {/* problems ignored, resolving is best-effort at this step*/});
		// update rule and/or computation conditions regarding field references which are defined through their absolute
		// path or relative with the root group name as explicit turning point in including model
		new DocumentModelWalker().acceptDocumentModel(includedDM,
				new ConditionPathRelativizerVisitor(includedDM));

		integrateIncludedDocModelIntoGroup(group, includedDM);

		addTypeDefsToBaseDm(includedDM.getContent().getTypeDefinitions(), includedDmExpansionInfo, group.getId());
		addModelRefsFromIncludedModelToBaseDm(ListUtils.emptyIfNull(includedDM.getHeader().getModelReferences()),
				includedDmExpansionInfo, group.getId());

		assert createExpansionInfo == (includedDmExpansionInfo.isPresent());
		if (includedDmExpansionInfo.isPresent()) {
			final ExpansionInfo finalIncludedDmExpansionInfo = includedDmExpansionInfo.get();

			// add origin info for _content_ that was transferred from the included dm (content as opposed to header info like
			// typedefs and modelRefs which were handled separately)
			Set<LocalCoordinates> newCoordsFromInclude = LocalCoordinatesModelVisitor.getLocalCoordinates(group.getElements());
			Map<LocalCoordinates, ExpansionNode> includedContentOrigins = newCoordsFromInclude.stream().collect(Collectors.toMap(
					Function.identity(),
					newCoords -> {
						LocalCoordinates oldCoordsInIncludedDm = switch (newCoords) {
							case LocalCoordinates.Element ec ->
									LocalCoordinates.Element.of(idUpdaterVisitor.newToOldElementIdMap.get(ec.id()));
							case LocalCoordinates.Annotation ac -> {
								String annotationName = ac.annotationName();
								yield LocalCoordinates.Annotation.of(annotationName,
										idUpdaterVisitor.newToOldElementIdMap.get(ac.annotatedElementId()));
							}
							// we only ever look at the include group, it has only elements and their annotations
							// anything else should not be retrieved from getLocalCoordinates
							default -> throw new IllegalArgumentException(
									"The group should only contain coordinates for elements and annotations.");
						};
						return ExpansionNode.of(newCoords,
								baseDocumentModel.getHeader().getId(),
								Set.of(ExpansionEdge.of(
										ExpansionEdgeLabel.Dm.Include.of(group.getId()),
										finalIncludedDmExpansionInfo.getOrigin(oldCoordsInIncludedDm)
								))
						);
					})
			);

			// add the includedModelOrigin of the included model
			ExpansionNode includedModelOrigin = ExpansionNode.of(LocalCoordinates.ModelItself.of(),
					baseDocumentModel.getHeader().getId(),
					ExpansionEdge.of(
							ExpansionEdgeLabel.Dm.Include.of(group.getId()),
							finalIncludedDmExpansionInfo.getOrigin(LocalCoordinates.ModelItself.of())
					));

			ExpansionInfoUtils.mergeInto(origins, includedContentOrigins);
			ExpansionInfoUtils.mergeInto(origins, includedModelOrigin);
		}

		//remove previous includeConfig after include handling of group is finished.
		group.setIncludeConfig(null);
	}

	private void addTypeDefsToBaseDm(List<FieldTypeDefinition> typeDefs,
			Optional<ExpansionInfo> nestedExpansionInfo,
			String includeGroupId) {
		for (FieldTypeDefinition td : typeDefs) {
			addTypeDefToDmAndRejectIdCollisions(baseDocumentModel, td, notificationReceiver);
			nestedExpansionInfo.ifPresent(nestedEI -> {
				LocalCoordinates.Typedef coords = LocalCoordinates.Typedef.of(td.getId());
				ExpansionNode typeDefOrigin = ExpansionNode.of(coords,
						baseDocumentModel.getHeader().getId(),
						ExpansionEdge.of(
								ExpansionEdgeLabel.Dm.Include.of(includeGroupId),
								nestedEI.getOrigin(coords)
						));
				ExpansionInfoUtils.mergeInto(origins, typeDefOrigin);
			});
		}
	}

	/**
	 * handles deduplication (by id) and validates that for the same id, only equal typedefs may exist
	 */
	private static void addTypeDefToDmAndRejectIdCollisions(DocumentModel dm, FieldTypeDefinition td,
			Consumer<RankedNotification> notificationReceiver) {
		if (canAddNewTypeDef(dm.getContent(), td, notificationReceiver)) {
			dm.getContent().getTypeDefinitions().add(td);
		}
	}

	/**
	 * DocumentModelContent currently represents typedefs as a list, even though conceptually they are rather a set.
	 * This method normalizes their order to avoid meaningless differences between semantically equal DMs.
	 */
	private static void normalizeTypedefsList(DocumentModel dm) {
		dm.getContent()
				.setTypeDefinitions(
						new ArrayList<>(dm.getContent().getTypeDefinitions().stream()
								.sorted(Comparator.comparing(FieldTypeDefinition::getId))
								.toList()));

		List<FieldTypeDefinition> tds = dm.getContent().getTypeDefinitions();
		assert (tds.size() == tds.stream().map(FieldTypeDefinition::getId).distinct().toList().size())
				: "typedefs should not contain duplicates, got " + tds;
	}

	/**
	 *
	 * @param modelRefsFromIncludedDm
	 * 		the model references of the included DM (managed refs already stripped by resolver)
	 * @param nestedExpansionInfo
	 * 		the ExpansionInfo of the included DM
	 * @param includeElementId
	 * 		the (possibly adapted) id of the new included element
	 */
	private void addModelRefsFromIncludedModelToBaseDm(
			@NonNull List<ModelReference> modelRefsFromIncludedDm, @NonNull Optional<ExpansionInfo> nestedExpansionInfo,
			@NonNull String includeElementId) {
		addModelRefsFromIncludeOrImportModelToBaseDm(modelRefsFromIncludedDm, nestedExpansionInfo,
				ExpansionEdgeLabel.Dm.Include.of(includeElementId));
	}

	private void addModelRefsFromImportedModelToBaseDm(
			@NonNull List<ModelReference> modelRefsFromIncludedDm, @NonNull Optional<ExpansionInfo> nestedExpansionInfo) {
		addModelRefsFromIncludeOrImportModelToBaseDm(modelRefsFromIncludedDm, nestedExpansionInfo,
				ExpansionEdgeLabel.Dm.Import.of());
	}

	private void addModelRefsFromIncludeOrImportModelToBaseDm(
			@NonNull List<ModelReference> modelRefsFromIncludedDm, @NonNull Optional<ExpansionInfo> nestedExpansionInfo,
			@NonNull ExpansionEdgeLabel expansionEdgeLabel) {
		assert modelRefsFromIncludedDm.stream()
				.noneMatch(mr -> DM_MANAGED_PURPOSES.contains(mr.getPurpose())) : modelRefsFromIncludedDm;
		Set<String> existingModelReferencesInBase = CollectionUtils.emptyIfNull(baseDocumentModel.getHeader().getModelReferences())
				.stream()
				.map(com.mgmtp.a12.model.header.ModelReference::getReference)
				.collect(Collectors.toSet());

		nestedExpansionInfo.ifPresent(nestedEI ->
				modelRefsFromIncludedDm.forEach(mrFromInclude -> {
					LocalCoordinates coords = LocalCoordinates.ModelReference.of(
							mrFromInclude); // coords are unchanged between included DM and baseDm here
					ExpansionNode modelRefOrigin = ExpansionNode.of(coords,
							baseDocumentModel.getHeader().getId(),
							ExpansionEdge.of(
									expansionEdgeLabel,
									nestedEI.getOrigin(coords)
							));
					ExpansionInfoUtils.mergeInto(origins, modelRefOrigin);
				})
		);

		modelRefsFromIncludedDm.stream()
				.filter(r -> !existingModelReferencesInBase.contains(r.getReference()))
				.forEach(baseDocumentModel.getHeader().getModelReferences()::add);
	}

	private String checkAndGetIncludedDmId(final Group group) {
		assert group.getIncludeConfig().isPresent() : "expected to only get called on groups with includeConfig";
		@SuppressWarnings("java:S2637") // passing null as parameter, this is allowed in Optional#orElse
		String dmId = group.getIncludeConfig().map(Group.IncludeConfig::getReference).orElse(null);
		if (StringUtils.isBlank(dmId)) {
			String groupPath = ElementUtils.getPath(group);
			createError(notificationReceiver,
					DocumentModelExpandError.NO_MODEL_REFERENCE.getMessage(groupPath,
							baseDocumentModel.getHeader().getId()));

			return null;
		}
		return dmId;
	}

	private boolean checkIncludedModelForCompatibility(Group.IncludeConfig includeConfig,
			final DocumentModel includedDocumentModel, final String includedModelReference) {
		DocumentModelRoot modelRoot = includedDocumentModel.getContent().getModelRoot();
		boolean isModelRootInclude = includeConfig.getIncludeLevel() == MODEL_ROOT;
		if (!isModelRootInclude) {
			if (modelRoot.getElements().size() != 1 || !(modelRoot.getElements().getFirst() instanceof Group)) {
				createError(notificationReceiver,
						DocumentModelExpandError.NOT_EXACTLY_ONE_ROOT_GROUP.getMessage(includedModelReference));
				return false;
			}
			if (((Group) modelRoot.getElements().getFirst()).getRepeatability() > 1) {
				createError(notificationReceiver,
						DocumentModelExpandError.REPEATABLE_ROOT_GROUP.getMessage(includedModelReference));
				return false;
			}
		}
		return checkLocaleCompatibility(includedDocumentModel, includedModelReference,
				baseDocumentModel.getHeader().getLocales(), notificationReceiver);
	}

	/**
	 * @return the updated included elements:
	 * 		- id: adapted id after inclusion
	 */
	private void integrateIncludedDocModelIntoGroup(final Group group,
			final DocumentModel includedDocumentModel) {
		Group includedDocumentModelRoot = includedDocumentModel.getContent().getModelRoot();
		assert group.getIncludeConfig().isPresent() : group.getName();
		IGroup.IIncludeConfig.IncludeLevel includeLevel = group.getIncludeConfig().map(Group.IncludeConfig::getIncludeLevel)
				.orElse(SINGLE_RG); // fallback case should never happen, see assertion above
		Group includedRootGroup = switch (includeLevel) {
			case MODEL_ROOT -> includedDocumentModelRoot;
			case SINGLE_RG -> (Group) includedDocumentModelRoot.getElements().getFirst();
		};
		List<Element> newChildren = includedRootGroup.getElements();

		if (!group.getElements().isEmpty()) {
			// can only be the case in inconsistent models
			group.setElements(new ArrayList<>());
		}
		group.setIncludeConfig(null);
		newChildren.forEach(group::addElement);
	}

	private static class ElementIdUpdaterVisitor extends DocumentModelVisitor {
		private final String idPrefix;
		final Map<String, String> newToOldElementIdMap = new HashMap<>();

		ElementIdUpdaterVisitor(String idPrefix) {
			this.idPrefix = idPrefix;
		}

		@Override
		public void leaveElement(Element element) {
			String newId = idPrefix + element.getId();
			newToOldElementIdMap.put(newId, element.getId());
			element.setId(newId);
			super.leaveElement(element);
		}
	}

	protected class ConditionPathRelativizerVisitor extends DocumentModelVisitor {
		protected final DocumentModel model;
		private final ParserService parserService;
		private final Map<String, IRule> rule2IRuleMap;
		private final Map<String, IBaseCalculationService.ICalculationRule> comp2ICompMap;

		public ConditionPathRelativizerVisitor(final DocumentModel model) {
			this.model = model;
			IDocumentModel extModel = new DocumentModelService().convertToExternal(model);
			ElementContainer elementContainer = DocumentModelServiceUtils.convert(extModel, false,
					getProblemReporterForEcConversion());
			IServiceConfiguration serviceConfiguration = DocumentModelServiceUtils.createServiceConfiguration(extModel,
					elementContainer);
			this.parserService = new ParserService(serviceConfiguration);
			this.rule2IRuleMap = initRule2IRuleMap(elementContainer);
			this.comp2ICompMap = initComp2ICompMap(elementContainer);
		}

		private Map<String, IRule> initRule2IRuleMap(ElementContainer elementContainer) {
			return elementContainer.getAllRules().stream().collect(
					Collectors.toMap(IRule::getFullName, r -> r));
		}

		private Map<String, IBaseCalculationService.ICalculationRule> initComp2ICompMap(ElementContainer elementContainer) {
			return elementContainer.getAllCalculations().stream().collect(
					Collectors.toMap(c -> c.getRule().getFullName(), c -> c));
		}

		private IRule getIRuleForRule(Rule rule) {
			return rule2IRuleMap.get(ElementUtils.getPath(rule));
		}

		private IBaseCalculationService.ICalculationRule getICompForComp(Computation comp) {
			return comp2ICompMap.get(ElementUtils.getPath(comp));
		}

		@Override
		public VisitProcess visitComputation(Computation computation) {
			IBaseCalculationService.ICalculationRule calculationRule = getICompForComp(computation);

			// if the computation is invalid, there is no ICalculationRule for it
			if (calculationRule != null) {
				List<EntityPathsRelativizer.TextUpdate> updates = EntityPathsRelativizer.determineUpdatesForPathsInConditionWithRootGroupReference(
						calculationRule.getRule(), parserService, getProblemReporterForParser(computation));
				applyConditionUpdatesToComputation(updates, (IRuleFromComputation) calculationRule.getRule(), computation);
			}

			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitRule(Rule rule) {
			List<EntityPathsRelativizer.TextUpdate> updates =
					EntityPathsRelativizer.determineUpdatesForPathsInConditionWithRootGroupReference(
							getIRuleForRule(rule), parserService, getProblemReporterForParser(rule));
			applyConditionUpdatesToRule(updates, rule);

			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		/**
		 * @param element
		 *        {@link Computation} or {@link Rule}
		 */
		private IProblemReporter getProblemReporterForParser(Element element) {
			return p -> notificationReceiver.accept(new WrappedProblemWithSeverityLimitAndOrigin(
					p,
					Severity.WARNING,
					element));
		}

		private IProblemReporter getProblemReporterForEcConversion() {
			return p -> {
				if (p.getSource() instanceof ElementWrapper elementWrapper) {
					// to make our error consistent we extract the internal part of the object
					notificationReceiver.accept(new WrappedProblemWithSeverityLimitAndOrigin(
							p,
							Severity.WARNING,
							elementWrapper.getElement()));
				} else {
					// this should not happen. and if so, we want to know it
					throw new IllegalArgumentException("The class " + p.getSource().getClass().getName() + " is not supported as " +
							"origin for a problem.");
				}
			};
		}

		private void applyConditionUpdatesToRule(List<EntityPathsRelativizer.TextUpdate> updates, Rule rule) {
			String updatedConditionText = EntityPathsRelativizer.TextUpdate.applyUpdatesTo(updates, rule.getErrorCondition());
			rule.setErrorCondition(updatedConditionText);
		}

		private void applyConditionUpdatesToComputation(
				List<EntityPathsRelativizer.TextUpdate> updates,
				IRuleFromComputation ruleFromComputation,
				Computation computation) {

			for (var update : EntityPathsRelativizer.TextUpdate.sortedBackwards(updates)) {
				StringWithFragmentsOriginMapEntry originMapEntry =
						ruleFromComputation.mapPositionInConditionTextToOriginInComputationAlternative(update.getBeginIdx());
				// Note: originMapEntry.translateIdx will implicitly validate that update.getEndIdx is within the fragment too
				Object originRef = originMapEntry.getOriginRef();
				if (originRef instanceof ComputationAlternativesJoiner.ComputationAlternativeFragmentOriginRef) {
					var originRefCa = (ComputationAlternativesJoiner.ComputationAlternativeFragmentOriginRef)
							originMapEntry.getOriginRef();
					var updateInFragment = update.withBeginAndEndIdx(
							originMapEntry.translateIdx(update.getBeginIdx()),
							originMapEntry.translateIdx(update.getEndIdx()));

					boolean isPrecond = originRefCa.isPrecondition();
					int alternativeIdx = originRefCa.alternativeIdx();
					if (alternativeIdx == -1 && isPrecond) {
						assert computation.getCommonPrecondition()
								.isPresent() : "as it is a precondition we assume the precondition is present";
						@SuppressWarnings("java:S3655") // optional without isPresent check -> false positive
						StringBuilder origCommonPrecondition = new StringBuilder(computation.getCommonPrecondition().get());
						updateInFragment.applyTo(origCommonPrecondition);
						computation.setCommonPrecondition(origCommonPrecondition.toString());
					} else if (isPrecond) {
						Computation.ComputationAlternative alt = computation.getComputationAlternatives().get(alternativeIdx);
						assert alt.getPrecondition().isPresent() : "as it is a alternative we assume the alternative is present";
						@SuppressWarnings("java:S3655") // optional without isPresent check -> false positive
						StringBuilder origPrecondition = new StringBuilder(alt.getPrecondition().get());
						updateInFragment.applyTo(origPrecondition);
						alt.setPrecondition(origPrecondition.toString());
					} else {
						Computation.ComputationAlternative alt = computation.getComputationAlternatives().get(alternativeIdx);
						StringBuilder origOperation = new StringBuilder(alt.getOperation());
						updateInFragment.applyTo(origOperation);
						alt.setOperation(origOperation.toString());
					}
				} else {
					if (!(originRef instanceof String)) {
						// note: the computed field is referenced by path in the (dynamically derived RuleFromComputation, but is
						// referenced by id in the DocumentModel
						// -> for the computed field, an update is computed, but it does not correspond to a source in any
						//    computation alternative / fragment, instead, the origin object is the string constant itself
						//    (see ComputationAlternativesJoiner)
						// -> we do nothing for the updates that do not target a ComputationAlternativeFragmentOriginRef
						throw new IllegalArgumentException("Unexpected condition-update origin: " + originRef);
					}
				}
			}
		}
	}

	/**
	 * If requested, removes rules / computations from the documentModel (in place); except for rules/computations that
	 * are direct children of attachment groups.
	 */
	private static void removeRulesAndComputationsIfNecessary(DocumentModel documentModel, boolean excludeRules,
			boolean excludeComputations) {
		if (excludeRules) {
			removeAllMatchingElementsExceptDirectChildsOfAttachementGroups(documentModel, Rule.class::isInstance);
		}
		if (excludeComputations) {
			removeAllMatchingElementsExceptDirectChildsOfAttachementGroups(documentModel, Computation.class::isInstance);
		}
	}

	private static void removeAllMatchingElementsExceptDirectChildsOfAttachementGroups(DocumentModel documentModel,
			Predicate<Element> shouldBeRemoved) {
		new DocumentModelWalker().acceptDocumentModel(documentModel, new DocumentModelVisitor() {
			// override leaveGroup instead visitGroup or enterGroup, so we don't mess with the traverse process when we modify the
			// group's elements
			@Override public void leaveGroup(final Group group) {
				if (!isAttachmentGroup(group)) {
					group.setElements(new ArrayList<>( // wrap in ArrayList in case mutability is expected during later handling
							group.getElements().stream()
									.filter(e -> !shouldBeRemoved.test(e)).toList()));
				}
			}
		});
	}

	private static boolean isAttachmentGroup(Group group) {
		return group.getUsageType().map(ElementUtils.A12_ATTACHMENT::equals).orElse(false);
	}

	private static boolean checkAndReportResolvedModelNotNull(DocumentModel importedDocumentModel, String importedModelReference,
			Consumer<RankedNotification> notificationReceiver) {
		if (importedDocumentModel == null) {
			createError(notificationReceiver,
					DocumentModelExpandError.REFERENCED_MODEL_NOT_RESOLVED.getMessage(importedModelReference));
			return false;
		}
		return true;
	}

	/**
	 * The imported document model must contain all supported locales.
	 */
	private static boolean checkLocaleCompatibility(final DocumentModel importedDocumentModel, final String includedModelReference,
			List<Locale> supportedLocales, Consumer<RankedNotification> notificationReceiver) {
		List<Locale> localesNotInIncludedModel = new ArrayList<>(supportedLocales);
		localesNotInIncludedModel.removeAll(importedDocumentModel.getHeader().getLocales());
		if (!localesNotInIncludedModel.isEmpty()) {
			createError(notificationReceiver,
					DocumentModelExpandError.SUPPORTED_LOCALES_DONT_FIT.getMessage(includedModelReference));
			return false;
		}
		return true;
	}
}
