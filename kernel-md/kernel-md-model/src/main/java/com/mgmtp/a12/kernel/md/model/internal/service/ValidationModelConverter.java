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

import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.cascadeNulls;

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

import lombok.NonNull;

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

	private final boolean considerNotInD;

	private List<Locale> modelLanguages;
	private ConditionMultiLanguageHandler mlh;
	private Map<String, IFieldTypeDefinition> typeDefs;
	private IProblemReporter problemReporter;

	/**
	 * @param considerNotInD
	 * 		- if {@code true} ExtMP-specific information is considered otherwise it is ignored.
	 */
	public ValidationModelConverter(boolean considerNotInD) {
		this.considerNotInD = considerNotInD;
	}

	/**
	 * Converts the given document to the {@link IEC} representation.
	 *
	 * @param documentModel
	 * 		the document model to convert.
	 * @param problemReporter
	 * 		the problem reporter, error messages are added to. May not be <code>null</code>.
	 * @return an {@link ElementContainer} corresponding to the given {@link DocumentModel}.
	 */
	public @NonNull ElementContainer convert(@NonNull IDocumentModel documentModel, @NonNull IProblemReporter problemReporter) {
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
				problemReporter.reportProblem(new MVKCoreError("Top-level element must be of type %s, but was %s."
						.formatted(IGroup.class.getName(), element.getClass().getName()), element));
				continue;
			}

			if (StringUtils.isBlank(rootGroupType.getName())) {
				problemReporter.reportProblem(new MVKCoreError("Root group must have a name.", rootGroupType));
				continue;
			}

			final Group rootGroup = createGroupFromGroupType(rootGroupType, "", new long[0], result);
			addChildToGroup(result, rootGroup);
		}
	}

	private @NonNull Group createGroupFromGroupType(
			@NonNull IGroup groupType,
			@NonNull String parentPath,
			long[] parentMaxIndices,
			@NonNull ElementContainerImpl resultElementContainer
	) {
		String groupPath = joinPath(groupType, parentPath);
		long[] groupIndices = computeMaxIndices(groupType, parentMaxIndices);

		if (groupType.getIncludeConfig().isPresent() && groupType.getElements().isEmpty()) {
			problemReporter.reportProblem(
					new MVKCoreError("Unexpanded include at '%s'".formatted(groupPath), groupType));
		}

		String indexFieldName = groupType.getIndexField()
				.map(IDocumentModelObjectReference::getRelativePath)
				.map(indexFieldRelPath -> ElementPathUtils.getName(ElementPathUtils.absPath(groupPath, indexFieldRelPath)))
				.orElse(null);
		String usageType = groupType.getUsageType().orElse(null);
		Map<String, List<ISortField>> sortInfo = createSortInfo(groupType, groupPath);
		Group result = new Group(groupType.getName(), groupPath, groupIndices, emptyMapIfNull(groupType.getAnnotations()),
				groupType.getId(),
				indexFieldName,
				usageType, sortInfo);
		resultElementContainer.addToGroupCache(result);

		for (IElement element : groupType.getElements()) {
			if (StringUtils.isBlank(element.getName())) {
				problemReporter.reportProblem(new MVKCoreError("Element must have a name.", groupType));
				continue;
			}

			Entity entity = switch (element) {
				case IField field ->
						createFieldFromFieldType(field, groupPath, groupIndices, ElementUtils.A12_ATTACHMENT.equals(usageType));
				case IComputation computation -> cascadeNulls(
						createRuleFromComputation(computation, groupPath, groupIndices, mlh, problemReporter),
						rule -> createCalculationFromComputationType(rule, computation, groupPath, groupIndices));
				case IRule rule -> createRuleFromRuleType(rule, groupPath, groupIndices);
				case IGroup group -> createGroupFromGroupType(group, groupPath, groupIndices, resultElementContainer);
				default -> {
					problemReporter.reportProblem(new MVKCoreError("Unknown subclass of %s : %s"
							.formatted(IElement.class.getName(), element.getClass().getName()), element));
					// the element will not be added to the model --> might lead to cascading errors
					yield null;
				}
			};
			if (entity != null) {
				addChildToGroup(result, entity);
			}
		}
		return result;
	}

	private void addChildToGroup(Group group, Entity childToAdd) {
		Entity conflictingEntity = group.addChildEntityIfAbsent(childToAdd);
		if (conflictingEntity != null) {
			problemReporter.reportProblem(new MVKCoreError("Cannot have two child elements with the same name [%s] in group '%s'."
					.formatted(childToAdd.getName(), group.getFullName()), childToAdd));
			// all duplicates after the first occurence will not be added to the model --> might lead to cascading errors
		}
	}

	private static List<Annotation> emptyMapIfNull(List<Annotation> annotations) {
		return annotations == null ? List.of() : Collections.unmodifiableList(annotations);
	}

	private Map<String, List<ISortField>> createSortInfo(IGroup group, String groupPath) {
		List<ISortField> resultList = new ArrayList<>();
		int i = 0;
		for (IGroup.ISortField sf : group.getSortFields()) {
			String sortFieldName = ElementPathUtils.getName(ElementPathUtils.absPath(groupPath, sf.getField().getRelativePath()));
			ISortField.SortingType sortingType = ISortField.SortingType.Descending;
			if (IGroup.ISortField.Order.ASC.equals(sf.getOrder())) {
				sortingType = ISortField.SortingType.Ascending;
			}
			resultList.add(new SortField(sortFieldName, ++i, sortingType));
		}
		Map<String, List<ISortField>> resultMap = new HashMap<>();
		resultMap.put("Standard", resultList);
		return resultMap;
	}

	private @NonNull Field createFieldFromFieldType(final IField field,
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

	public static Rule createRuleFromComputation(final IComputation computation,
			final String parentPath,
			final long[] parentMaxIndices,
			Locale conditionLanguage,
			List<Locale> modelLanguages,
			IProblemReporter problemReporter) {
		ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(conditionLanguage,
				new A12SpecificSettings(modelLanguages));
		return createRuleFromComputation(computation, parentPath, parentMaxIndices, mlh, problemReporter);
	}

	private static Rule createRuleFromComputation(final IComputation computation,
			final String parentPath,
			final long[] parentMaxIndices,
			ConditionMultiLanguageHandler mlh,
			IProblemReporter problemReporter) {

		final String rulePath = joinPath(computation, parentPath);
		final long[] ruleIndices = computeMaxIndices(computation, parentMaxIndices);

		StringWithFragmentsOriginMap statement = getComputationStatement(computation, mlh, problemReporter);
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

	private static StringWithFragmentsOriginMap getComputationStatement(final IComputation computation,
			ConditionMultiLanguageHandler mlh, IProblemReporter problemReporter) {
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

	private @NonNull Rule createRuleFromRuleType(final IRule rule,
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

	private static String joinPath(final IElement elementType,
			final String parentPath) {
		return parentPath + IIdentifier.SEPARATOR + elementType.getName();
	}

	private static long[] computeMaxIndices(final IElement elementType,
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
