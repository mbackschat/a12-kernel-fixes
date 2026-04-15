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
package com.mgmtp.a12.kernel.md.model.internal.service;

import static com.mgmtp.a12.kernel.md.model.api.IGroup.IIncludeDetails.IncludeLevel.MODEL_ROOT;
import static com.mgmtp.a12.kernel.md.model.api.IGroup.IIncludeDetails.IncludeLevel.SINGLE_RG;
import static com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandSanityChecks.PURPOSE_4_ONLY_TYPE_DEFS;
import static com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandSanityChecks.checkLocaleCompatibility;
import static com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandSanityChecks.checkResolvedDocumentModel;
import static com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandSanityChecks.createError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.parser.internal.ParserService;
import com.mgmtp.a12.kernel.core.service.internal.EntityPathsRelativizer;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.string.sourcemap.StringWithFragmentsOriginMapEntry;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelRoot;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.IIdNamed;
import com.mgmtp.a12.kernel.md.model.a12internal.ILabeled;
import com.mgmtp.a12.kernel.md.model.a12internal.LocalizedTextMapBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.ElementContainer;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.expansion.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.internal.service.expansion.TypeDefCache;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.IRuleFromComputation;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ComputationAlternativesJoiner;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelServiceUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.WrappedProblemWithSeverityLimitAndOrigin;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.ElementWrapper;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderFactory;
import com.mgmtp.a12.model.header.ModelReference;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

/**
 * A visitor that replaces included contents by their include elements.
 */
public class DocumentModelExpandService {

	static final String INCLUDE = "include";
	static final String TRANSITIVE_INCLUDE = "transitiveInclude";
	private static final Set<String> includePurposes =
			// we need a set that won't crash on .contains(null), so Set.of() alone won't do
			Collections.unmodifiableSet(new HashSet<>(Set.of(INCLUDE, TRANSITIVE_INCLUDE)));

	private final DocumentModel baseDocumentModel;
	private final List<Locale> supportedLocales;
	private final DocumentModelReferenceResolver modelResolver;
	private final IProblemReporter problemReporter;

	/**
	 * *Note*: nested {@code CombinationModel} not handled, use {@code CombinationModelService.expand} instead.
	 */
	public static void expand(DocumentModel baseDocumentModel, final DocumentModelReferenceResolver modelResolver,
			IProblemReporter problemReporter) {
		new DocumentModelExpandService(baseDocumentModel, modelResolver, problemReporter).expandInternal();
	}

	private DocumentModelExpandService(final DocumentModel baseDocumentModel, final DocumentModelReferenceResolver modelResolver,
			IProblemReporter problemReporter) {
		this.baseDocumentModel = baseDocumentModel;
		this.supportedLocales = baseDocumentModel.getHeader().getLocales();
		this.modelResolver = modelResolver;
		this.problemReporter = problemReporter;
	}

	private void expandInternal() {
		new DocumentModelWalker().acceptDocumentModel(baseDocumentModel,
				getExpandVisitor(baseDocumentModel, new ExpansionInfo(), null));

		// we ignore errors during reference resolving, as the DM might not be fully valid by itself (e.g. additive documents)
		// the user has to validate the result in their context (e.g. after joining)
		DocumentModelUtils.resolveAndUpdateReferences(baseDocumentModel, p -> {
		});
	}

	private DocumentModelVisitor getExpandVisitor(DocumentModel documentModel, ExpansionInfo expansionInfo,
			Group.IncludeDetails includeDetailsInInludingDm) {
		return new IncludeExpandVisitor(documentModel, expansionInfo, includeDetailsInInludingDm);
	}

	private static String createPath(Element element) {
		return ElementUtils.getPath(element);
	}

	private class IncludeExpandVisitor extends DocumentModelVisitor {

		protected final DocumentModel includingDocModel;
		protected final ExpansionInfo expansionInfo;
		protected final TypeDefCache typeDefCache = new TypeDefCache();
		/**
		 * null if we are at the root model (i.e. the current model is not included)
		 */
		protected final Group.IncludeDetails includeDetailsInInludingDm;

		public IncludeExpandVisitor(
				final DocumentModel includingDocModel,
				final ExpansionInfo expansionInfo,
				Group.IncludeDetails includeDetailsInInludingDm
		) {
			this.includingDocModel = includingDocModel;
			this.expansionInfo = expansionInfo;
			this.includeDetailsInInludingDm = includeDetailsInInludingDm;
		}

		@Override
		public VisitProcess visitElement(Element element) {
			element.setInternalDescription(extractSupportedLocalizedTextMap(element.getInternalDescription()));
			element.setExternalDescription(extractSupportedLocalizedTextMap(element.getExternalDescription()));
			if (element instanceof ILabeled labeled) {
				labeled.setLabel(extractSupportedLocalizedTextMap(labeled.getLabel()));
			}
			return super.visitElement(element);
		}

		@Override
		public void leaveElement(Element element) {
			updateId(element);
			super.leaveElement(element);
		}

		@Override
		public VisitProcess visitComputation(Computation computation) {
			computation.setErrorMessage(extractSupportedLocalizedTextMap(computation.getErrorMessage()));
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitRule(Rule rule) {
			rule.setErrorMessage(extractSupportedLocalizedTextMap(rule.getErrorMessage()));
			updateVariantErrorMessages(rule);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private void updateVariantErrorMessages(Rule rule) {
			Map<String, ILocalizedTextMap> updated = new LinkedHashMap<>();
			rule.getVariantErrorMessages()
					.forEach((key, value) -> updated.put(key, extractSupportedLocalizedTextMap(value)));
			rule.setVariantErrorMessages(updated);
		}

		/**
		 * @return a {@link ILocalizedTextMap} instance only with the supported locales.
		 */
		private ILocalizedTextMap extractSupportedLocalizedTextMap(ILocalizedTextMap original) {
			if (original.isEmpty()) {
				return original;
			}
			LocalizedTextMapBuilder builder = new LocalizedTextMapBuilder();
			for (Entry<Locale, String> entry : original.entrySet()) {
				if (!supportedLocales.contains(entry.getKey())) {
					continue;
				}
				builder.add(entry.getKey(), entry.getValue());
			}
			return builder.build();
		}

		private void updateId(final IIdNamed entity) {
			entity.setId(expansionInfo.getUpdatedId(entity.getId()));
		}

		@Override
		public VisitProcess visitGroup(final Group group) {
			if (group.getIncludeDetails().isPresent()) {
				if (group.getElements().isEmpty()) {
					//include has not already been expanded in a previous expand process
					handleInclude(group);
				}
				return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
			} else {
				return VisitProcess.CONTINUE_TRAVERSAL;
			}
		}

		private DocumentModelVisitor getRuleAndComputationConditionUpdaterVisitor(DocumentModel documentModel) {
			return new RuleAndComputationConditionUpdaterVisitor(documentModel);
		}

		private void handleInclude(final Group group) {
			if (!checkModelReferenceExistance(group)) {
				return;
			}
			ModelReference modelRefToIncludedDmFromIncludingGroup = getModelReferenceToIncludedDm(group);

			final String modelRefString = modelRefToIncludedDmFromIncludingGroup.getReference();
			if (!checkIncludeCycle(modelRefString)) {
				// break the cycle, the error is already reported
				return;
			}

			DocumentModel includedDocModel = modelResolver.getDocumentModel(modelRefString);
			if (!checkResolvedDocumentModel(includedDocModel, modelRefString, problemReporter)
					|| !checkIncludedModelForCompatibility(group, includedDocModel, modelRefString)) {
				// no document model or not compatible, no expansion
				return;
			}

			// recursively expand included model
			assert group.getIncludeDetails()
					.isPresent() : "expected group.getIncludeDetails() to be present when in handleInclude()";
			ExpansionInfo newExpansionInfo = new ExpansionInfo(expansionInfo, group);
			removeRulesAndComputationsIfNecessary(includedDocModel, newExpansionInfo); // note: does not remove rules in
			// (not-yet-resolved) nested includes yet, this will be done during the recursive call to handleInclude,
			// new ExpansionInfo(ExpansionInfo, Group) propagates the exclude flags.
			// We choose to nonetheless discard the rules/computations _early_, to avoid potential error cases that might occur
			// during expansion if we deferred the remove-rules step until _after_ recursive expansion has been done

			new DocumentModelWalker().acceptDocumentModel(includedDocModel,
					getExpandVisitor(includedDocModel, newExpansionInfo, group.getIncludeDetails().orElse(null)));

			typeDefCache.addTypeDefs(includedDocModel.getContent().getTypeDefinitions());

			addTransitiveModelReferencesFromIncludedModel(includedDocModel);

			// resolve references in includedDocModel (e.g. for error fields) that have just now become resolveable, because
			// includedDocModel has now been expanded (sub-includes have been resolved).
			// This is necessary because parsing in RuleAndComputationConditionUpdaterVisitor will otherwise fail for rules
			// that reference an error field from a sub-include of includedDocModel (those references have not been resolved
			// initially)
			DocumentModelUtils.resolveAndUpdateReferences(includedDocModel,
					p -> {/* problems ignored, resolving is best-effort at this step*/});

			// update rule and/or computation conditions regarding field references which are defined through their absolute
			// path or relative with the root group name as explicit turning point in including model
			new DocumentModelWalker().acceptDocumentModel(includedDocModel,
					getRuleAndComputationConditionUpdaterVisitor(includedDocModel));

			this.integrateIncludedDocModelIntoGroup(group, includedDocModel);
		}

		/**
		 * For this method to work, we assume that the model references do not need to be changed.
		 * This is true, if the alias names are unique, e.g. if they are the model ids, and the models are referenced by id, or they
		 * are referenced by path and all models reside in the same path.
		 *
		 * @param includedDocumentModel
		 * 		the document model to extract the model references from.
		 */
		private void addTransitiveModelReferencesFromIncludedModel(
				DocumentModel includedDocumentModel) {
			Set<String> existingModelAliases = this.includingDocModel.getHeader().getModelReferences().stream()
					.map(ModelReference::getAlias)
					.collect(Collectors.toSet());

			// map from original reference in the included Model to corresponding references for the including model
			// (direct includes become transitive includes)
			Map<ModelReference, ModelReference> modelReferencesToAdd =
					includedDocumentModel.getHeader().getModelReferences().stream()
							.filter(r -> includePurposes.contains(r.getPurpose()))
							.filter(r -> !existingModelAliases.contains(r.getAlias()))
							.collect(Collectors.toMap(Function.identity(), this::createTransitiveReference, (x, y) -> x,
									LinkedHashMap::new));

			this.includingDocModel.getHeader().getModelReferences().addAll(modelReferencesToAdd.values());

			// as we also have to update the ModelReference in `IncludeDetails` of a group, we have to change the object there as well
			// this should not really be necessary, as the IncludeDetails are only used by kernel, but for DocumentModel comparison
			// this should have a consistent state
			if (!modelReferencesToAdd.isEmpty()) {
				DocumentModelVisitor modelReferenceUpdater = new DocumentModelVisitor() {
					@Override public VisitProcess visitGroup(Group group) {
						if (group.getIncludeDetails().isPresent()) {
							ModelReference modelReference = group.getIncludeDetails().get().getModelReference();
							if (modelReferencesToAdd.containsKey(modelReference)) {
								group.getIncludeDetails().get().setModelReference(modelReferencesToAdd.get(modelReference));
							}
						}

						return super.visitGroup(group);
					}
				};
				new DocumentModelWalker().acceptDocumentModel(includedDocumentModel, modelReferenceUpdater);
			}
		}

		private ModelReference createTransitiveReference(ModelReference reference) {
			if (INCLUDE.equals(reference.getPurpose())) {
				return HeaderFactory.createModelReference(TRANSITIVE_INCLUDE, reference.getModelType(), reference.getAlias(),
						reference.getReference());
			} else {
				return reference;
			}
		}

		private boolean checkModelReferenceExistance(final Group group) {
			if (StringUtils.isBlank(getModelReferenceToIncludedDm(group).getReference())) {
				String groupPath = createPath(group);
				createError(problemReporter,
						DocumentModelExpandError.NO_MODEL_REFERENCE.getMessage(groupPath,
								this.includingDocModel.getHeader().getId()));
				return false;
			}
			return true;
		}

		private boolean checkIncludeCycle(final String modelReference) {
			if (this.expansionInfo.getModelReferences().contains(modelReference)) {
				createError(problemReporter, DocumentModelExpandError.SELF_RECURSION.getMessage());
				return false;
			}
			return true;
		}

		private boolean checkIncludedModelForCompatibility(Group mountPointGroup, final DocumentModel includedDocumentModel,
				final String includedModelReference) {
			DocumentModelRoot modelRoot = includedDocumentModel.getContent().getModelRoot();
			assert mountPointGroup.getIncludeDetails().isPresent() : mountPointGroup.getName();
			boolean isModelRootInclude = mountPointGroup.getIncludeDetails()
					.map(i -> i.getIncludeLevel() == MODEL_ROOT).orElse(false);
			if (!isModelRootInclude) {
				if (modelRoot.getElements().size() != 1 || !(modelRoot.getElements().getFirst() instanceof Group)) {
					createError(problemReporter,
							DocumentModelExpandError.MULTIPLE_ROOT_GROUPS.getMessage(includedModelReference));
					return false;
				}
				if (((Group) modelRoot.getElements().getFirst()).getRepeatability() > 1) {
					createError(problemReporter,
							DocumentModelExpandError.REPEATABLE_ROOT_GROUP.getMessage(includedModelReference));
					return false;
				}
				if (((Group) modelRoot.getElements().getFirst()).getElements().isEmpty() &&
						((Group) modelRoot.getElements().getFirst()).getIncludeDetails().isEmpty()) {
					createError(problemReporter,
							DocumentModelExpandError.ROOT_GROUP_WITHOUT_CHILDREN.getMessage(includedModelReference));
					return false;
				}
			} else {
				// empty included dms don't work, because the consistency check / code generation will mistake them for
				// unexpanded includes and fail with something like
				// "Cannot process document models with unexpanded includes. Found unexpanded include with path '/RG/IncludeMultiEmpty'."
				// We could in principle allow this during expansion, but then, users would be confronted with confusing subsequent
				// error messages if the expanded DM gets validated. We don't need to support this anyway, so it seems reasonable
				// to simply reject those cases:
				if (modelRoot.getElements().isEmpty()) { // we don't check for && modelRoot.getIncludeDetails().isEmpty(), because a
					// modelRoot (not root group) that is itself the mountpoint of an include probably doesn't make sense and can't
					// be created (at least not in SME)
					createError(problemReporter,
							DocumentModelExpandError.EMPTY_INCLUDED_DM.getMessage(includedModelReference));
					return false;
				}

				// Are any other specific checks needed for MODEL_ROOT includes?
			}
			return checkLocaleCompatibility(includedDocumentModel, includedModelReference,
					baseDocumentModel.getHeader().getLocales(), problemReporter);
		}

		private void integrateIncludedDocModelIntoGroup(final Group group, final DocumentModel includedDocumentModel) {
			Group includedDocumentModelRoot = includedDocumentModel.getContent().getModelRoot();
			assert group.getIncludeDetails().isPresent() : group.getName();
			IGroup.IIncludeDetails.IncludeLevel includeLevel = group.getIncludeDetails().map(Group.IncludeDetails::getIncludeLevel)
					.orElse(SINGLE_RG); // fallback case should never happen, see assertion above
			Group includedRootGroup = switch (includeLevel) {
				case MODEL_ROOT -> includedDocumentModelRoot;
				case SINGLE_RG -> (Group) includedDocumentModelRoot.getElements().getFirst();
			};
			List<Element> newChildren = includedRootGroup.getElements();
			newChildren.forEach(group::addElement);

			assert includeLevel != MODEL_ROOT || includedRootGroup.getUsageType()
					.isEmpty() : "usageType not expected for rootPreserving include";
			group.setUsageType(includedRootGroup.getUsageType().orElse(null));

			integrationCallback(group, includedRootGroup);
		}

		private @NonNull ModelReference getModelReferenceToIncludedDm(final Group group) {
			return group.getIncludeDetails()
					.orElseThrow(
							() -> new IllegalArgumentException("Expected include details not present for group " + group.getId()))
					.getModelReference();
		}

		protected void integrationCallback(final Group group, final Group includedRootGroup) {
			// callback for subclasses. Especially for migration
		}

		@Override
		public VisitProcess visitField(final Field field) {
			field.setHelperText(extractSupportedLocalizedTextMap(field.getHelperText()));
			FieldType type = field.getFieldType();
			updateFieldType(type);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private void updateFieldType(FieldType fieldType) {
			if (fieldType instanceof StringType stringType) {
				stringType.setErrorMessage(extractSupportedLocalizedTextMap(stringType.getErrorMessage()));
			}
			if (fieldType instanceof EnumerationType enumType) {
				enumType.getValues().forEach(ev -> ev.setLabel(extractSupportedLocalizedTextMap(ev.getLabel())));
				enumType.setErrorMessage(extractSupportedLocalizedTextMap(enumType.getErrorMessage()));
			}
		}

		@Override
		public VisitProcess visitFieldTypeDefinition(final FieldTypeDefinition fieldTypeDefinition) {
			fieldTypeDefinition.setInternalDescription(
					extractSupportedLocalizedTextMap(fieldTypeDefinition.getInternalDescription()));
			fieldTypeDefinition.setExternalDescription(
					extractSupportedLocalizedTextMap(fieldTypeDefinition.getExternalDescription()));
			updateFieldType(fieldTypeDefinition.getFieldType());
			fieldTypeDefinition.addModelReferencePath(expansionInfo.getModelReferencePath());
			typeDefCache.addUniqueTypeDef(fieldTypeDefinition);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitHeader(final Header header) {
			handleImports(header);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private void handleImports(final Header header) {
			/*
			 * In case an include is defined through the header to import only type definitions,
			 * these will be added if and only if the expanded document model does not have already
			 * type definitions (a mixture of definition ways is not allowed).
			 */
			if (!baseDocumentModel.getContent().getTypeDefinitions().isEmpty()) {
				return;
			}

			CollectionUtils.emptyIfNull(header.getModelReferences())
					.stream()
					.filter(mr -> PURPOSE_4_ONLY_TYPE_DEFS.equals(mr.getPurpose()))
					.forEach(this::handleImport);
		}

		private void handleImport(ModelReference modelRef) {
			DocumentModelExpandImportService importService = new DocumentModelExpandImportService(modelResolver,
					baseDocumentModel.getHeader().getLocales(), problemReporter);
			importService.extractImportedTypeDefinitions(modelRef, expansionInfo)
					.forEach(typeDefCache::addTypeDef);
		}

		@Override
		public void leave() {
			// at the end of the visit process, we override the type definitions
			// all the type definitions in includingDocModel were already visited and gathered in typeDefCache in the method visitFieldTypeDefinition
			includingDocModel.getContent().setTypeDefinitions(new ArrayList<>(typeDefCache.getOrderedTypeDefs()));
		}
	}

	protected class RuleAndComputationConditionUpdaterVisitor extends DocumentModelVisitor {
		protected final DocumentModel model;
		private final ParserService parserService;
		private final Map<String, IRule> rule2IRuleMap;
		private final Map<String, IBaseCalculationService.ICalculationRule> comp2ICompMap;

		public RuleAndComputationConditionUpdaterVisitor(final DocumentModel model) {
			this.model = model;
			String variant = null;
			IDocumentModel extModel = new DocumentModelService().convertToExternal(model);
			ElementContainer elementContainer = DocumentModelServiceUtils.convert(extModel, variant,
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
			return rule2IRuleMap.get(createPath(rule));
		}

		private IBaseCalculationService.ICalculationRule getICompForComp(Computation comp) {
			return comp2ICompMap.get(createPath(comp));
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
			return p -> problemReporter.reportProblem(new WrappedProblemWithSeverityLimitAndOrigin(
					p,
					Severity.WARNING,
					element));
		}

		private IProblemReporter getProblemReporterForEcConversion() {
			return p -> {
				if (p.getSource() instanceof ElementWrapper elementWrapper) {
					// to make our error consistent we extract the internal part of the object
					problemReporter.reportProblem(new WrappedProblemWithSeverityLimitAndOrigin(
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
						StringBuilder origCommonPrecondition = new StringBuilder(computation.getCommonPrecondition().get());
						updateInFragment.applyTo(origCommonPrecondition);
						computation.setCommonPrecondition(origCommonPrecondition.toString());
					} else if (isPrecond) {
						Computation.ComputationAlternative alt = computation.getComputationAlternatives().get(alternativeIdx);
						assert alt.getPrecondition().isPresent() : "as it is a alternative we assume the alternative is present";
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
	 * if the expansionInfo specifies {@link ExpansionInfo#isExcludeRules()}/{@link ExpansionInfo#isExcludeComputations()},
	 * removes the rules / computations from the documentModel (in place); except for rules/computations that are direct child
	 * elements of attachment groups
	 */
	private static void removeRulesAndComputationsIfNecessary(DocumentModel documentModel, ExpansionInfo expansionInfo) {
		if (expansionInfo.isExcludeRules()) {
			removeAllMatchingElementsExceptDirectChildsOfAttachementGroups(documentModel, Rule.class::isInstance);
		}
		if (expansionInfo.isExcludeComputations()) {
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
}
