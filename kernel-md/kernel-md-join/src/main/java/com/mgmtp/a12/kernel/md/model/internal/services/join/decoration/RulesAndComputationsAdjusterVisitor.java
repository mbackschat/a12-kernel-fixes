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
package com.mgmtp.a12.kernel.md.model.internal.services.join.decoration;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.service.internal.MoveSupportInternal;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporterProxy;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelObjectReferenceBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.IEntity;
import com.mgmtp.a12.kernel.md.model.a12internal.LocalizedTextMapBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.ElementContainer;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.impl.LocalizedTextMapImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.MoveSupportDMInternal;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelServiceUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;

public class RulesAndComputationsAdjusterVisitor extends DocumentModelVisitor {

	private final Map<String, EntityFullName> formerToNewFullNamesMapper;
	private final Map<String, EntityFullName> newToFormerFullNamesMapper;
	private final Map<String, IRule> rulesInOriginalEc;
	private final Map<String, IBaseCalculationService.ICalculationRule> calculationsInOriginalEc;
	private final Map<String, String> formerToNewFullNameWithoutRulesInDecoModelMapper;
	private final DocumentModelService documentModelService;
	private final DocumentModelSearchService searchDecoModel;
	private final JoiningProblemReporter problemReporter;
	private final Locale conditionLanguage;

	public RulesAndComputationsAdjusterVisitor(DocumentModel decorationModel, DocumentModel decorationModelCopy,
			Map<String, EntityFullName> formerToNewFullNamesMapper, JoiningProblemReporter problemReporter) {
		this.documentModelService = new DocumentModelService();
		String variant = decorationModelCopy.getContent().getModelInfo().getVariant().orElse(null);
		this.searchDecoModel = new DocumentModelSearchService(decorationModelCopy);
		IDocumentModel extDecorationModel = documentModelService.convertToExternal(decorationModel);
		ElementContainer ecFromOriginal = DocumentModelServiceUtils.convert(extDecorationModel, variant);

		this.formerToNewFullNamesMapper = formerToNewFullNamesMapper;
		this.newToFormerFullNamesMapper = formerToNewFullNamesMapper.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getValue().getFullName(),
						e -> EntityFullName.builder().fullName(e.getKey()).type(e.getValue().getType()).build()));
		this.rulesInOriginalEc = ecFromOriginal.getAllRules().stream()
				.collect(Collectors.toMap(IRule::getFullName, Function.identity()));
		this.calculationsInOriginalEc = ecFromOriginal.getAllCalculations().stream()
				.collect(Collectors.toMap(cr -> cr.getRule().getFullName(), Function.identity()));
		this.problemReporter = problemReporter;
		this.formerToNewFullNameWithoutRulesInDecoModelMapper = formerToNewFullNamesMapper.entrySet().stream()
				.filter(e -> e.getValue().getType() != EntityFullName.EntityType.RULE)
				.filter(e -> e.getValue().getType() != EntityFullName.EntityType.COMPUTATION)
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFullName()));

		this.conditionLanguage = extDecorationModel.getContent().getDocumentModelConfig().getConditionLanguage();
	}

	@Override
	public VisitProcess visitRule(final Rule rule) {
		String formerFullName = determineFormerFullName(rule);
		IRule originalRule = rulesInOriginalEc.get(formerFullName);

		if (originalRule != null) {
			adjustRule(rule, originalRule);
		} else {
			throw new IllegalArgumentException(String.format("Rule '%s' in entity container not found.", formerFullName));
		}
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitComputation(final Computation computation) {
		String formerFullName = determineFormerFullName(computation);
		IBaseCalculationService.ICalculationRule originalCalculation = calculationsInOriginalEc.get(formerFullName);

		if (originalCalculation != null) {
			adjustComputation(computation, originalCalculation);
		} else {
			throw new IllegalArgumentException(
					String.format("Calculation rule '%s' in entity container not found.", formerFullName));
		}
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	private String determineFormerFullName(Element element) {
		String fullName = documentModelService.getPath(element);
		return newToFormerFullNamesMapper.get(fullName).getFullName();
	}

	private void adjustRule(Rule rule, IRule originalRule) {
		rule.setErrorCondition(MoveSupportInternal.getInstance()
				.changeConditionTextForMove(originalRule.getErrorConditionText(), originalRule.getFullName(),
						formerToNewFullNameWithoutRulesInDecoModelMapper, // note: moveSupport expects this to also contain rule name changes, but those don't matter (only the changes of the rule group do, and those ARE contained here
						conditionLanguage));
		rule.setErrorMessage(convertErrorMessages(MoveSupportInternal.getInstance()
				.changeErrorTextsForMove(originalRule.getErrorMessages(), originalRule.getFullName(),
						formerToNewFullNameWithoutRulesInDecoModelMapper, conditionLanguage)));

		String newFullNameOfErrorEntity =
				formerToNewFullNameWithoutRulesInDecoModelMapper.get(originalRule.getErrorEntity().getFullName());
		if (newFullNameOfErrorEntity != null) {
			String newRuleFullName = documentModelService.getPath(rule);
			String errorEntityRelPath = ElementPathUtils.relPath(newFullNameOfErrorEntity, newRuleFullName);

			Optional<Field> optionalField = searchDecoModel.getByPath(newFullNameOfErrorEntity).map(Field.class::cast);
			rule.setErrorEntity(new DocumentModelObjectReferenceBuilder<IEntity>().relativePath(errorEntityRelPath)
					.dmo(optionalField.orElse(null)).build());
		}
	}

	private void adjustComputation(Computation computation, IBaseCalculationService.ICalculationRule originalCalculation) {
		computation.setComputationPreconditionsAndOperations(MoveSupportDMInternal.getInstance()
				.changeComputationAlternativesForMove(
						computation.getComputationPreconditionsAndOperations(),
						originalCalculation.getRule().getFullName(),
						formerToNewFullNameWithoutRulesInDecoModelMapper, conditionLanguage
				));
		computation.setErrorMessage(new LocalizedTextMapImpl(MoveSupportDMInternal.getInstance().changeErrorTextsForMove(
				computation.getErrorMessage(),
				originalCalculation.getRule().getFullName(),
				formerToNewFullNameWithoutRulesInDecoModelMapper,
				conditionLanguage
		)));

		String newErrorEntityFullName =
				Optional.ofNullable(formerToNewFullNamesMapper.get(originalCalculation.getRule().getErrorEntity().getFullName()))
						.map(EntityFullName::getFullName)
						.orElse(null);

		String newComputationFullName = documentModelService.getPath(computation);
		assert newComputationFullName != null : "We expect to always find the computation";

		if (newErrorEntityFullName != null) {
			Optional<Field> optionalField = searchDecoModel.getByPath(newErrorEntityFullName).map(Field.class::cast);
			String newErrorEntityRelPath = ElementPathUtils.relPath(newErrorEntityFullName, newComputationFullName);

			computation.setComputedField(
					new DocumentModelObjectReferenceBuilder<Field>().relativePath(newErrorEntityRelPath)
							.dmo(optionalField.orElse(null)).build());
		}
	}

	private ILocalizedTextMap convertErrorMessages(Map<Locale, String> errorMessages) {
		LocalizedTextMapBuilder errMap = new LocalizedTextMapBuilder();
		errorMessages.forEach(errMap::add);
		return errMap.build();
	}
}
