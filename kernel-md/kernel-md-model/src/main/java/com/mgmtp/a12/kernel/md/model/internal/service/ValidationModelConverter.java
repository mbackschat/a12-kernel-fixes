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

import static com.mgmtp.a12.kernel.md.model.internal.service.mapping.VariantFiltering.computationIsAcceptedByFilter;
import static com.mgmtp.a12.kernel.md.model.internal.service.mapping.VariantFiltering.fieldIsAcceptedByFilter;
import static com.mgmtp.a12.kernel.md.model.internal.service.mapping.VariantFiltering.ruleIsAcceptedByFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ISortField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;
import com.mgmtp.a12.kernel.core.utils.internal.string.sourcemap.StringWithFragmentsOriginMap;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.ElementContainer;
import com.mgmtp.a12.kernel.md.model.a12internal.services.codegen.A12SpecificSettings;
import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelObjectReference;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IRequirednessConfig;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.Calculation;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.ElementContainerImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.Entity;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.Field;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.Group;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.RequirednessConfig;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.Rule;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.RuleFromComputationImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.SortField;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.type.TypeConverter;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ComputationAlternativesJoiner;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.model.header.Annotation;

/**
 * <p>
 * Converts {@link DocumentModel}s to the {@link IEC} representation that is used in the validation
 * kernel.
 * </p>
 * <p>
 * Note: This class is not thread-safe. Parallel conversions should be done by creating multiple
 * instances.
 * </p>
 */
public class ValidationModelConverter {

	private final String variant;
	private final boolean considerNotInD;

	private List<Locale> modelLanguages;
	private ConditionMultiLanguageHandler mlh;
	private Map<String, IFieldTypeDefinition> typeDefs;
	private IProblemReporter problemReporter;

	/**
	 * @param variant
	 * 		- the variant may be {@code null}:
	 * @param considerNotInD
	 * 		- if {@code true} ExtMP-specific information is considered otherwise it is ignored.
	 */
	public ValidationModelConverter(String variant, boolean considerNotInD) {
		this.variant = variant;
		this.considerNotInD = considerNotInD;
	}

	/**
	 * Converts the given document for the given variant to the {@link IEC} representation.
	 *
	 * @param documentModel
	 * 		the document model to convert.
	 * @return an {@link ElementContainer} corresponding to the given {@link DocumentModel}.
	 */
	public ElementContainer convert(IDocumentModel documentModel) {
		return convert(documentModel, p -> {
			throw new IllegalArgumentException(p.getMessage());
		});
	}

	/**
	 * Converts the given document for the given variant to the {@link IEC} representation.
	 *
	 * @param documentModel
	 * 		the document model to convert.
	 * @param problemReporter
	 * 		the problem reporter, error messages are added to. May not be <code>null</code>.
	 * @return an {@link ElementContainer} corresponding to the given {@link DocumentModel}.
	 */
	public ElementContainer convert(IDocumentModel documentModel, IProblemReporter problemReporter) {
		final ElementContainerImpl result = new ElementContainerImpl();
		final Locale conditionLanguage = documentModel.getContent().getDocumentModelConfig().getConditionLanguage();

		this.modelLanguages = documentModel.getHeader().getLocales();
		this.mlh = new ConditionMultiLanguageHandler(conditionLanguage, new A12SpecificSettings(modelLanguages));
		this.typeDefs = documentModel.getContent().getTypeDefinitions().stream()
				.collect(Collectors.toMap(IFieldTypeDefinition::getId, Function.identity()));
		this.problemReporter = problemReporter;

		// Conversion is done in two phases
		// 1. Convert all the entities of the model tree (groups, field, rules) to their mvk equivalents.
		// 2. Recreate the cross references between rules and their error fields.
		// The 2nd step can only be done after the 1st step has finished because when converting a rule to its mvk
		// equivalent the referenced error field could be non-existent in the mvk representation because it was not
		// visited yet.
		convertModelContent(documentModel, result);
		setupErrorFieldReferences(result);

		return result;
	}

	private void convertModelContent(
			final IDocumentModel documentModel,
			final ElementContainerImpl result) {

		for (final IElement element : documentModel.getContent().getDocumentModelRoot().getElements()) {
			if (!(element instanceof IGroup rootGroupType)) {
				String msg = "Cannot process model with top-level element of type "
						+ element.getClass().getSimpleName() + ".";
				problemReporter.reportProblem(new MVKCoreError(msg, null));
				continue;
			}

			final Group rootGroup = createGroupFromGroupType(rootGroupType, "", new long[0], result);
			result.addChildEntity(rootGroup);
		}
	}

	private Group createGroupFromGroupType(final IGroup groupType,
			final String parentPath,
			final long[] parentMaxIndices,
			ElementContainerImpl resultElementContainer) {

		final String groupPath = joinPath(groupType, parentPath);
		final long[] groupIndices = computeMaxIndices(groupType, parentMaxIndices);

		final String indexField = groupType.getIndexField()
				.map(IDocumentModelObjectReference::getDocumentModelObject)
				.map(f -> f.orElseGet(() -> {
					problemReporter.reportProblem(new MVKCoreError(
							"Cannot process document models with unexpanded includes. Found unresolved reference.", groupType));
					return null;
				}))
				.map(IField::getName)
				.orElse(null);
		final String usageType = groupType.getUsageType().orElse(null);
		final Map<String, List<ISortField>> sortInfo = createSortInfo(groupType.getSortFields(), groupType);
		final Group result = new Group(groupType.getName(), groupPath, groupIndices, emptyMapIfNull(groupType.getAnnotations()),
				groupType.getId(),
				indexField,
				usageType, sortInfo);
		resultElementContainer.addToGroupCache(result);

		for (final IElement element : groupType.getElements()) {
			checkForInclude(groupPath, element);

			if (element instanceof IField field) {
				if (fieldIsAcceptedByVariantFilter(field)) {
					result.addChildEntity(
							createFieldFromFieldType(field, groupPath, groupIndices,
									ElementUtils.A12_ATTACHMENT.equals(usageType)));
				}
			} else if (element instanceof IComputation computation) {
				if (computationIsAcceptedByVariantFilter(computation)) {
					final Rule rule = createRuleFromComputation(computation, groupPath, groupIndices);
					if (rule == null) {
						continue;
					}
					result.addChildEntity(
							createCalculationFromComputationType(rule, computation, groupPath, groupIndices));
				}
			} else if (element instanceof IRule rule) {
				if (ruleIsAcceptedByVariantFilter(rule)) {
					result.addChildEntity(
							createRuleFromRuleType(rule,
									groupPath,
									groupIndices));
				}
			} else if (element instanceof IGroup group) {
				result.addChildEntity(createGroupFromGroupType(group,
						groupPath,
						groupIndices,
						resultElementContainer));
			}
		}
		return result;
	}

	private List<Annotation> emptyMapIfNull(List<Annotation> annotations) {
		return annotations == null ? List.of() : Collections.unmodifiableList(annotations);
	}

	protected boolean fieldIsAcceptedByVariantFilter(IField field) {
		return fieldIsAcceptedByFilter(field, variant);
	}

	protected boolean computationIsAcceptedByVariantFilter(IComputation computation) {
		return computationIsAcceptedByFilter(computation, variant);
	}

	protected boolean ruleIsAcceptedByVariantFilter(IRule rule) {
		return ruleIsAcceptedByFilter(rule, variant);
	}

	private Map<String, List<ISortField>> createSortInfo(List<IGroup.ISortField> sortFields, IGroup group) {
		List<ISortField> resultList = new ArrayList<>();
		int i = 0;
		for (IGroup.ISortField sf : sortFields) {
			Optional<IField> documentModelObject = sf.getField().getDocumentModelObject();
			if (documentModelObject.isEmpty()) {
				problemReporter.reportProblem(new MVKCoreError("Cannot process document models with unexpanded includes. Found " +
						"unresolved reference.", group));
				continue;
			}
			String fieldName = documentModelObject.get().getName();
			ISortField.SortingType sortingType = ISortField.SortingType.Descending;
			if (IGroup.ISortField.Order.ASC.equals(sf.getOrder())) {
				sortingType = ISortField.SortingType.Ascending;
			}
			resultList.add(new SortField(fieldName, ++i, sortingType));
		}
		Map<String, List<ISortField>> resultMap = new HashMap<>();
		resultMap.put("Standard", resultList);
		return resultMap;
	}

	private Field createFieldFromFieldType(final IField field,
			final String parentPath,
			final long[] parentMaxIndices,
			final boolean isInAttachment) {
		final String fieldPath = joinPath(field, parentPath);
		final long[] fieldIndices = computeMaxIndices(field, parentMaxIndices);
		final IFieldType fieldType = new TypeConverter(considerNotInD).convert(field.getFieldType(), modelLanguages, typeDefs,
				problemReporter, field);
		var requirednessConfig = createRequirednessConfig(field.getRequirednessConfig().orElse(null));
		return new Field(field.getName(),
				fieldType,
				fieldPath,
				fieldIndices,
				emptyMapIfNull(field.getAnnotations()),
				field.getId(),
				field.isGlobal(),
				field.isTransient(),
				requirednessConfig,
				isInAttachment);
	}

	private com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField.IRequirednessConfig createRequirednessConfig(
			final IRequirednessConfig requirednessConfig) {
		if (requirednessConfig == null) {
			return null;
		}
		var mode = com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField.IRequirednessConfig.Mode.valueOf(
				requirednessConfig.getMode().name());
		return new RequirednessConfig(mode, requirednessConfig.getErrorMessage());
	}

	private Calculation createCalculationFromComputationType(final com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule rule,
			final IComputation computation,
			final String parentPath,
			final long[] parentMaxIndices) {
		final String rulePath = joinPath(computation, parentPath);
		final long[] ruleIndices = computeMaxIndices(computation, parentMaxIndices);
		return new Calculation(computation,
				rulePath,
				ruleIndices,
				rule);
	}

	private Rule createRuleFromComputation(final IComputation computation,
			final String parentPath,
			final long[] parentMaxIndices) {

		final String rulePath = joinPath(computation, parentPath);
		final long[] ruleIndices = computeMaxIndices(computation, parentMaxIndices);

		StringWithFragmentsOriginMap statement = getComputationStatement(computation);
		if (statement == null) {
			return null;
		}

		return new RuleFromComputationImpl(computation.getName(),
				rulePath,
				ruleIndices,
				statement,
				computation.getErrorMessage(),
				computation.getName(),
				// we are sure that computations have severity 'ERROR' (see EXT-18536)
				com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule.RuleSeverityType.ERROR,
				computation.getComputedField().getRelativePath(),
				emptyMapIfNull(computation.getAnnotations()),
				computation);
	}

	protected StringWithFragmentsOriginMap getComputationStatement(final IComputation computation) {
		Optional<IField> computedField = computation.getComputedField().getDocumentModelObject();
		if (computedField.isEmpty()) {
			String relativePath = computation.getComputedField().getRelativePath();
			String message = StringUtils.isEmpty(relativePath)
					? "The computed field was not specified."
					: "The computed field \"%s\" was not found.".formatted(
					ElementUtils.resolveRelativePath(computation, relativePath));
			problemReporter.reportProblem(new MVKCoreError(message, computation));
			return null;
		}

		return new ComputationAlternativesJoiner(mlh,
				computation.getCommonPrecondition().orElse(null),
				computation.getComputationAlternatives(),
				computedField.get(),
				ElementUtils.getPath(computation.getParent()),
				computation.getErrorCodesToSuppress())
				.join();
	}

	private Rule createRuleFromRuleType(final IRule rule,
			final String parentPath,
			final long[] parentMaxIndices) {

		final String rulePath = joinPath(rule, parentPath);
		final long[] ruleIndices = computeMaxIndices(rule, parentMaxIndices);

		return new Rule(rule.getName(),
				rulePath,
				ruleIndices,
				rule.getErrorCondition(),
				rule.getErrorMessage(),
				rule.getErrorCode(),
				com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule.RuleSeverityType.valueOf(rule.getSeverity().name()),
				rule.getErrorEntity().getRelativePath(),
				emptyMapIfNull(rule.getAnnotations()),
				rule.getId());
	}

	private void checkForInclude(final String groupPath, final IElement element) {
		if (element instanceof IGroup group
				&& group.getIncludeDetails().isPresent()
				&& group.getElements().isEmpty()) {
			problemReporter.reportProblem(
					new MVKCoreError("Cannot process document models with unexpanded includes. Found unexpanded include with path '"
							+ groupPath
							+ IIdentifier.SEPARATOR
							+ element.getName()
							+ "'.", element));
		}
	}

	private String joinPath(final IElement elementType,
			final String parentPath) {
		return parentPath + IIdentifier.SEPARATOR + elementType.getName();
	}

	private long[] computeMaxIndices(final IElement elementType,
			final long[] parentMaxIndices) {
		final long[] maxIndices = new long[parentMaxIndices.length + 1];
		System.arraycopy(parentMaxIndices, 0, maxIndices, 0, parentMaxIndices.length);
		if (elementType instanceof IGroup group) {
			maxIndices[maxIndices.length - 1] = group.getRepeatability();
		} else {
			maxIndices[maxIndices.length - 1] = 1L;
		}
		return maxIndices;
	}

	private void setupErrorFieldReferences(final ElementContainerImpl entityContainer) {
		entityContainer.flattenToStream()
				.filter(Rule.class::isInstance)
				.map(Rule.class::cast)
				.forEach(rule -> rule.setErrorEntity(findReferencedEntity(entityContainer, rule, rule.getErrorEntityRelPath())));
		entityContainer.flattenToStream()
				.filter(Calculation.class::isInstance)
				.map(Calculation.class::cast)
				.forEach(calculation -> {
					final Rule rule = (Rule) calculation.getRule();
					rule.setErrorEntity(
							findReferencedEntity(entityContainer, rule, rule.getErrorEntityRelPath()));
					calculation.setCalculatedField((com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField)
							findReferencedEntity(entityContainer, calculation, calculation.getCalculatedFieldRelPath()));
				});
	}

	private com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity findReferencedEntity(
			final ElementContainerImpl entityContainer, Entity baseEntity, final String referencedEntityRelPath) {
		String referencedEntityFullName = ElementPathUtils.absPath(baseEntity.getFullName(), referencedEntityRelPath);
		return entityContainer.get(referencedEntityFullName);
	}
}
