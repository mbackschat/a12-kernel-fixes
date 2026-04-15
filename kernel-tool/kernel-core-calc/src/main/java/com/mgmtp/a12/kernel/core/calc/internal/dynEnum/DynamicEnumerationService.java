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
package com.mgmtp.a12.kernel.core.calc.internal.dynEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.calc.internal.util.DynEnumErrorLookup;
import com.mgmtp.a12.kernel.core.calc.internal.util.DynEnumErrorType;
import com.mgmtp.a12.kernel.core.calc.internal.util.DynamicEnumerationInfo;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.ParserService;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdListImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.CompositeConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListAndValueListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.FieldIdOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FindAnyAst;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.INumberType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IDynamicEnumerationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;

public class DynamicEnumerationService implements IDynamicEnumerationServiceVK {

	private final IServiceConfiguration serviceConfig;
	private final ParserService parserService;
	private final DynEnumErrorLookup errorLookup;

	public DynamicEnumerationService(IServiceConfiguration serviceConfiguration) {
		this.serviceConfig = serviceConfiguration;
		this.parserService = new ParserService(serviceConfig);
		this.errorLookup = new DynEnumErrorLookup(
				serviceConfig.getModelSpecificSettings().getLanguageUsedInConditionText());
	}

	/**
	 * Checks, whether the given rule is valid as a dynamic enumeration rule.
	 * Convenience method to omit double parsing, when the parsing was already done.
	 *
	 * @param condition
	 * 		the already parsed rule condition. The condition has to be parsed with
	 *        {@link ExpandMode#NO_EXPAND}. If the condition is <code>null</code>, the rule will
	 * 		be parsed and possible errors will be reported.
	 * @param rule
	 * 		the rule to be used as source for error messages. The rule error condition is not
	 * 		used in this call.
	 * @param pr
	 * 		the {@link IProblemReporter} taking occurring {@link IProblem}s.
	 * @return <code>true</code>, if the rule specifies a correct calculation for dynamic
	 * 		enumerations or <code>false</code> otherwise.
	 */
	@Override
	public boolean checkDynamicEnumerationRule(ICondition condition, IRule rule, IProblemReporter pr) {
		if (!checkRuleParams(rule, pr)) {
			return false;
		}
		if (condition == null) {
			condition = parserService.parseAndCheckCondition(rule, rule.getErrorConditionText(), pr, ExpandMode.NO_EXPAND);
		}
		if (condition == null) {
			return false;
		}
		if (!checkConditionGlobally(condition, rule, pr)) {
			return false;
		}
		return checkDynEnumForm(condition, rule, pr);
	}

	@Override
	public boolean checkDynamicEnumerationRule(IRule rule, IProblemReporter pm) {
		return checkDynamicEnumerationRule(null, rule, pm);
	}

	@Override
	public Optional<List<IDynamicEnumerationInfo>> checkAndExtractAllDynEnumInfo(List<IRule> rules, IProblemReporter pr) {
		return Java8Utils.reduceToOptionalList(rules.stream()
				.map(r -> checkAndExtractDynEnumInfo(r, pr)));
	}

	public Optional<IDynamicEnumerationInfo> checkAndExtractDynEnumInfo(IRule r, IProblemReporter pr) {
		ICondition condition = parserService.parseAndCheckCondition(r, r.getErrorConditionText(), pr, ExpandMode.NO_EXPAND);
		if (!checkDynamicEnumerationRule(condition, r, pr)) {
			return Optional.empty();
		}
		return extractDynEnumInfo(condition, r, pr);
	}

	private boolean checkConditionGlobally(ICondition condition, IRule rule, IProblemReporter pm) {
		if (AnalyseService.isParallelIteration(condition, true)) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_PARALLEL_ITERATION);
			return false;
		}
		if (AnalyseService.beinhaltetWiederholungNichtEindeutig(condition)) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_REPETITION_NOT_UNIQUE);
			return false;
		}
		return true;
	}

	/**
	 * Class for
	 * {@link DynamicEnumerationService#checkDynEnumForm(ICondition, IRule, IProblemReporter)}
	 * to split out
	 * {@link DynamicEnumerationService#checkOrCondition(IRule, IProblemReporter, DynEnumForm)}.
	 */
	private static class DynEnumForm {
		private ICompositeCondition compCond;
		private List<ICondition> subConditions;
		private HashSet<String> operandNames = new HashSet<>();

		public DynEnumForm(ICompositeCondition compCond) {
			this.compCond = compCond;
			this.subConditions = new ArrayList<>(compCond.getConditionList());
		}
	}

	private boolean checkDynEnumForm(ICondition condition, IRule rule, IProblemReporter pm) {
		if (!(condition instanceof ICompositeCondition)) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return false;
		}
		DynEnumForm def = new DynEnumForm((ICompositeCondition) condition);
		if (def.compCond.getLogOp() == LogOp.Or && !checkOrCondition(rule, pm, def)) {
			return false;
		}

		// compCond may be changed in checkOrCondition
		if (def.compCond.getLogOp() != LogOp.And) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return false;
		}

		Optional<IEntityId> dynEnumField = checkAndExtractDynEnumField(def.subConditions.get(0), rule, pm);
		if (!dynEnumField.isPresent()) {
			// errors already reported
			return false;
		}

		return def.subConditions.stream()
				.skip(1)
				.allMatch(c -> checkEnumValueCalc(c, pm, dynEnumField.get(), rule, def.operandNames,
						def.subConditions.size() - 1 > 1));
	}

	private boolean checkOrCondition(IRule rule, IProblemReporter pm, DynEnumForm def) {
		// with safeGuard
		if (def.subConditions.size() != 2) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return false;
		}
		if (!checkSafeGuard(def.subConditions.get(0), rule, pm, rule.getErrorEntity().getFullName())) {
			// Error message already created
			return false;
		}
		def.operandNames.addAll(getOperandNameFromSafeGuard(def.subConditions.get(0)));

		ICondition lastCondition = def.subConditions.get(1);
		if (!(lastCondition instanceof ICompositeCondition)) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return false;
		}
		def.compCond = (ICompositeCondition) lastCondition;
		def.subConditions.clear();
		def.subConditions.addAll(def.compCond.getConditionList());
		return true;
	}

	private boolean checkRuleParams(IRule rule, IProblemReporter pm) {
		if (ExtmpUtils.isHModel(serviceConfig.getModelSpecificSettings())) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_OLD_WORLD);
			return false;
		}
		if (!checkFieldType(rule, pm)) {
			return false;
		}
		if (EntityUtils.isIndexField((IField) rule.getErrorEntity(), serviceConfig.getIEC())) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INDEX_FIELD);
			return false;
		}
		return true;
	}

	private boolean checkSafeGuard(ICondition safeGuard, IRule rule, IProblemReporter pm, String dynEnumFieldName) {
		if (!(safeGuard instanceof ICompositeCondition)) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return false;
		}
		ICompositeCondition compCond = (ICompositeCondition) safeGuard;
		if (compCond.getLogOp() != LogOp.And ||
				compCond.getConditionList().size() != 2) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return false;
		}
		ICondition fieldTestCond = compCond.getConditionList().get(0);
		ICondition opTestCond = compCond.getConditionList().get(1);
		if (!(fieldTestCond instanceof IEntityListCondition) ||
				!(opTestCond instanceof IEntityListCondition)) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return false;
		}

		IEntityListCondition fieldTest = (IEntityListCondition) fieldTestCond;
		IEntityListCondition opTest = (IEntityListCondition) opTestCond;

		if (!isFieldTestCorrect(dynEnumFieldName, fieldTest) ||
				!isOpTestCorrect(dynEnumFieldName, opTest)) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return false;
		}
		return true;
	}

	private boolean isFieldTestCorrect(String dynEnumFieldName, IEntityListCondition fieldTest) {
		return fieldTest.getConditionName() == EntityListConditionName.AllFieldsFilled &&
				fieldTest.getEntityList().getEntityIdList().size() == 1 &&
				fieldTest.getEntityList().getEntityIdList().get(0).getFullName().equals(dynEnumFieldName);
	}

	private boolean isOpTestCorrect(String dynEnumFieldName, IEntityListCondition opTest) {
		boolean correctConstruct = ((opTest.getConditionName() == EntityListConditionName.NoFieldFilled && opTest.getEntityList()
				.getEntityIdList().size() == 1) ||
				opTest.getConditionName() == EntityListConditionName.NotAllFieldsFilled);
		boolean correctFields = opTest.getEntityList().getEntityIdList().stream()
				.noneMatch(e -> e.getFullName().equals(dynEnumFieldName) || !e.isField());
		return correctConstruct && correctFields;
	}

	private Set<String> getOperandNameFromSafeGuard(ICondition safeGuard) {
		IEntityListCondition opTest = (IEntityListCondition) ((ICompositeCondition) safeGuard).getConditionList().get(1);
		return opTest.getEntityList().getEntityIdList().stream().map(IEntityId::getFullName).collect(Collectors.toSet());
	}

	private boolean checkFieldType(IRule rule, IProblemReporter pm) {
		if (!rule.getErrorEntity().isField()) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_TYPE);
			return false;
		}
		IFieldType type = ((IField) rule.getErrorEntity()).getType();
		if (type instanceof IStringType strType && !strType.isExtensibleEnum()) {
			if (strType.getMaxLength() > 0 ||
					strType.getMinLength() != IFieldType.UNDEFINED ||
					!StringUtils.isEmpty(strType.getPattern()) ||
					strType.isNoValueValidation()) {
				errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_TYPE);
				return false;
			}
		} else if (type instanceof IEnumerationType) {
			// this is OK
		} else if (type instanceof INumberType) {
			INumberType numType = (INumberType) type;
			if (numType.leadingZerosAllowed() ||
					numType.getMaxFractionalDigits() > 0 ||
					numType.getMinFractionalDigits() > 0 ||
					numType.getMaxValue().isPresent() ||
					numType.getMinValue().isPresent()) {
				errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_TYPE);
				return false;
			}
		} else {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_TYPE);
			return false;
		}
		return true;
	}

	private boolean checkEnumValueCalc(ICondition c, IProblemReporter pm, IEntityId dynEnumField, IRule rule,
			HashSet<String> operandNames, boolean multipleOperations) {
		if (c instanceof ICompositeCondition) {
			// With precondition
			ICompositeCondition compCond = (ICompositeCondition) c;
			Optional<ICondition> calcCondition = checkEnumValueCalcWithPreCondition(pm, rule, operandNames, compCond);
			if (!calcCondition.isPresent()) {
				// Error was reported already.
				return false;
			}
			c = calcCondition.get();
		}
		// Now c must be the comparison condition...
		if (c instanceof IComparisonCondition) {
			IComparisonCondition cc = (IComparisonCondition) c;
			return checkEnumValueComparisonCalc(pm, dynEnumField, rule, operandNames, cc);
		} else if (c instanceof IEntityListAndValueListCondition) {
			return checkEnumValueEntityListAndValueListConditionCalc(c, pm, dynEnumField, rule, operandNames, multipleOperations);
		} else {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP);
			return false;
		}
	}

	private boolean checkEnumValueEntityListAndValueListConditionCalc(ICondition c, IProblemReporter pm, IEntityId dynEnumField,
			IRule rule, HashSet<String> operandNames, boolean multipleOperations) {
		if (multipleOperations) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP_OP);
			return false;
		}
		IEntityListAndValueListCondition consistencyCheck = (IEntityListAndValueListCondition) c;
		if (consistencyCheck.getConditionName() != EntityListAndValueListConditionName.ValueNotConsistent ||
				consistencyCheck.getEntityList().getEntityIdList().size() != 1) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP_OP);
			return false;
		}
		IEntityId entity = consistencyCheck.getEntityList().getEntityIdList().get(0);
		if (!checkTargetField(entity, pm, rule, dynEnumField)) {
			// error already reported
			return false;
		}
		List<IEntityId> ids = consistencyCheck.getValueListAsMixedList().stream()
				.filter(e -> e instanceof IEntityId)
				.map(IEntityId.class::cast)
				.collect(Collectors.toList());
		return checkEntitiesAreOperands(pm, rule, operandNames, new EntityIdListImpl(ids));
	}

	private boolean checkEnumValueComparisonCalc(IProblemReporter pm, IEntityId dynEnumField, IRule rule,
			HashSet<String> operandNames, IComparisonCondition cc) {
		// ... with !=
		if (cc.getCmpOp() != ComparisonOperation.NE) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP_OP);
			return false;
		}
		if (!checkLeftSide(cc.getOperation1(), dynEnumField, pm, rule)) {
			// error already reported
			return false;
		}
		return checkEntitiesAreOperands(pm, rule, operandNames, cc.getOperation2());
	}

	private Optional<ICondition> checkEnumValueCalcWithPreCondition(IProblemReporter pm, IRule rule, HashSet<String> operandNames,
			ICompositeCondition compCond) {
		ICondition c;
		if (compCond.getLogOp() != LogOp.Or) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP);
			return Optional.empty();
		}
		ICondition preCondition = extractPreCondition(compCond);
		Optional<IParseTreeEntity> invalidReference = FindAnyAst.findeInBedingung(preCondition,
				p -> p instanceof IEntityId && !operandNames.contains(((IEntityId) p).getFullName()), true);
		if (invalidReference.isPresent()) {
			String fieldName = ((IEntityId) invalidReference.get()).getFullName();
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_FIELD_REFERENCE, fieldName);
			return Optional.empty();
		}
		List<ICondition> parts = compCond.getConditionList();
		c = parts.get(parts.size() - 1);
		return Optional.of(c);
	}

	private boolean checkEntitiesAreOperands(IProblemReporter pm, IRule rule, HashSet<String> operandNames,
			IParseTreeEntity subTree) {
		Optional<IParseTreeEntity> invalidReference = FindAnyAst.findeInParseBaumEntity(subTree,
				p -> p instanceof IEntityId && !operandNames.contains(((IEntityId) p).getFullName()), true);
		if (invalidReference.isPresent()) {
			String fieldName = ((IEntityId) invalidReference.get()).getFullName();
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_FIELD_REFERENCE, fieldName);
			return false;
		}
		return true;
	}

	private boolean checkLeftSide(IOperation leftSide, IEntityId dynEnumField, IProblemReporter pr, IRule rule) {
		if (!(leftSide instanceof IFieldIdOperation)) {
			errorLookup.reportError(pr, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP);
			return false;
		}
		IFieldIdOperation fvo = (IFieldIdOperation) leftSide;
		if (fvo.getOperatorName() != FieldIdOperationName.FieldValue) {
			errorLookup.reportError(pr, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP);
			return false;
		}
		IEntityId field = fvo.getField();
		return checkTargetField(field, pr, rule, dynEnumField);
	}

	private boolean checkTargetField(IEntityId field, IProblemReporter pr, IRule rule, IEntityId dynEnumField) {
		if (!field.getFullName().equals(dynEnumField.getFullName())) {
			errorLookup.reportError(pr, rule, DynEnumErrorType.MVK_ERROR_WRONG_DYNENUM_FIELD, field.getFullName(),
					dynEnumField.getFullName());
			return false;
		}
		if (field.getCategoryName() != null) {
			errorLookup.reportError(pr, rule, DynEnumErrorType.MVK_ERROR_DYNENUM_FIELD_WITH_CATEGORY, field.getFullName(),
					dynEnumField.getFullName());
			return false;
		}
		if (!field.getSemanticIndex().isEmpty()) {
			errorLookup.reportError(pr, rule, DynEnumErrorType.MVK_INVALID_ITERATION);
			return false;
		}
		// Iterations must be equal
		if (!Arrays.equals(field.getIndices(), dynEnumField.getIndices())) {
			errorLookup.reportError(pr, rule, DynEnumErrorType.MVK_INVALID_ITERATION);
			return false;
		}
		return true;
	}

	private Optional<IDynamicEnumerationInfo> extractDynEnumInfo(ICondition condition, IRule rule, IProblemReporter pm) {
		Objects.requireNonNull(rule, "The given rule my not be null.");
		Objects.requireNonNull(pm, "Problem reporter may not be null.");

		if (condition == null) {
			condition = parserService.parseAndCheckCondition(rule, rule.getErrorConditionText(), pm, ExpandMode.NO_EXPAND);
		}
		if (!(condition instanceof ICompositeCondition)) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return Optional.empty();
		}
		ICompositeCondition compositeCondition = (ICompositeCondition) condition;
		List<ICondition> bedingungen = compositeCondition.getConditionList();
		ICondition safeGuard = null;
		if (compositeCondition.getLogOp() == LogOp.Or) {
			// With safeGuard
			ICompositeCondition completeSafeGuard = (ICompositeCondition) bedingungen.get(0);
			safeGuard = completeSafeGuard.getConditionList().get(1);

			ICondition calcCondition = bedingungen.get(bedingungen.size() - 1);
			if (!(calcCondition instanceof ICompositeCondition)) {
				errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
				return Optional.empty();
			}
			compositeCondition = (ICompositeCondition) calcCondition;
			bedingungen = compositeCondition.getConditionList();
		}

		if (compositeCondition.getLogOp() != LogOp.And) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP);
			return Optional.empty();
		}

		Optional<IEntityId> dynEnumField = checkAndExtractDynEnumField(bedingungen.get(0), rule, pm);
		if (!dynEnumField.isPresent()) {
			return Optional.empty();
		}

		// 3. Teilbedingungen extrahieren und prüfen
		List<Pair<ICondition, IOperation>> dynEnumValueCalcs = bedingungen.stream()
				.skip(1)
				.map(tb -> extractDynEnumValueCalc(tb, rule, pm))
				.collect(Collectors.toList());

		return Optional.of(new DynamicEnumerationInfo(dynEnumField.get(), safeGuard, dynEnumValueCalcs, rule.getFullName()));
	}

	private Pair<ICondition, IOperation> extractDynEnumValueCalc(ICondition dynEnumValCalc, IRule rule, IProblemReporter pm) {
		if (dynEnumValCalc instanceof ICompositeCondition) {
			ICompositeCondition cc = (ICompositeCondition) dynEnumValCalc;
			int ccLen = cc.getConditionList().size();
			IOperation op = extractDynEnumValueOp(cc.getConditionList().get(ccLen - 1), rule, pm).orElse(null);
			ICondition preCondition = extractPreCondition(cc);
			return Pair.of(preCondition, op);
		} else {
			IOperation op = extractDynEnumValueOp(dynEnumValCalc, rule, pm).orElse(null);
			return Pair.of(null, op);
		}
	}

	private ICondition extractPreCondition(ICompositeCondition cc) {
		int ccLen = cc.getConditionList().size();
		if (ccLen == 2) {
			return cc.getConditionList().get(0);
		} else {
			return new CompositeConditionImpl(cc, cc.getConditionList().subList(0, ccLen - 1));
		}
	}

	private Optional<IOperation> extractDynEnumValueOp(ICondition dynEnumValCalc, IRule rule, IProblemReporter pm) {
		if (dynEnumValCalc instanceof IComparisonCondition) {
			IComparisonCondition cc = (IComparisonCondition) dynEnumValCalc;
			if (cc.getCmpOp() != ComparisonOperation.NE) {
				errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP_OP);
				return Optional.empty();
			}
			return Optional.of(cc.getOperation2());
		} else if (dynEnumValCalc instanceof IEntityListAndValueListCondition) {
			IEntityListAndValueListCondition cond = (IEntityListAndValueListCondition) dynEnumValCalc;
			if (cond.getConditionName() != EntityListAndValueListConditionName.ValueNotConsistent
					|| cond.getEntityList().getEntityIdList().size() != 1) {
				errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP_OP);
				return Optional.empty();
			}
			return Optional.of(new ConsistentValueOpImpl(cond));
		} else {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_CALCOP);
			return Optional.empty();
		}
	}

	private Optional<IEntityId> checkAndExtractDynEnumField(ICondition b, IRule rule, IProblemReporter pm) {
		if (!(b instanceof IEntityListCondition)) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return Optional.empty();
		}
		IEntityListCondition feldGate = (IEntityListCondition) b;
		if (feldGate.getEntityList().getEntityIdList().size() != 1 ||
				feldGate.getConditionName() != EntityListConditionName.AllFieldsFilled) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_DYNENUM_INVALID_GATE);
			return Optional.empty();
		}
		Optional<IEntityId> result = Optional.of(feldGate.getEntityList().getEntityIdList().get(0));
		if (!checkDynEnumField(rule, pm, result)) {
			return Optional.empty();
		}
		return result;
	}

	private boolean checkDynEnumField(IRule rule, IProblemReporter pm, Optional<IEntityId> dynEnumField) {
		if (!dynEnumField.isPresent()) {
			// Error already reported
			return false;
		}
		if (!dynEnumField.get().getFullName().equals(rule.getErrorEntity().getFullName())) {
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_ERROR_WRONG_DYNENUM_FIELD, dynEnumField.get().getFullName(),
					rule.getErrorEntity().getFullName());
			return false;
		}
		if (Arrays.stream(dynEnumField.get().getIndices()).anyMatch(i -> i != -1 && i != 1)) {
			// Only iteration is allowed for repeatable layers
			errorLookup.reportError(pm, rule, DynEnumErrorType.MVK_INVALID_ITERATION);
			return false;
		}
		return true;
	}
}
