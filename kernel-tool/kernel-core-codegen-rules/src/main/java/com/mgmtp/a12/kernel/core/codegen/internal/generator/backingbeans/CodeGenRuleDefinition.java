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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenRuleConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CodeGenCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CompositeCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaRegelFehlertexte;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util.MetaSprachUnterschiedeHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.teilvalidierung.RuleType;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.AddValidatorMessage;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeOutput;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.InitEntityListAndValueListConditionParam;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.IterationLimitCreatorBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RepetitionNotUniqueCreator;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.IRepetitionNotUniqueHandler;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule.RuleSeverityType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Defines the backing bean for rule generation.
 */
@StringTemplateGroup("rules/")
public class CodeGenRuleDefinition extends STParameterClassBase implements IRepetitionNotUniqueHandler {
	private static final String UNKNOWN_AUTO_GENERATED_RULE_TYPE = "Unknown auto generated rule type ";

	private static final String ERROR = "Fehler";
	private static final String WARNING = "Hinweis";
	private static final String INFO = "Info";

	private static final String MANDATORY_FIELD_ERROR_ID = "FEHLER_PFLICHTFELD";
	private static final String MANDATORY_FIELD_MESSAGE_ID = "PFLICHT_FELD_FEHLER";

	private static final String UNIQUE_FIELD_ERROR_ID = "FEHLER_INDEXFELD";
	private static final String UNIQUE_FIELD_MESSAGE_ID = "INDEX_FELD_FEHLER";

	private static final String UNIQUE_VALUE_ERROR_ID = "FEHLER_MULTI_SELECT";

	private static final String SOLITARY_INDEX_ERROR_ID = "FEHLER_SOLITARY_INDEX";
	private static final String SOLITARY_INDEX_MESSAGE_ID = "SOLITARY_INDEX_FEHLER";

	private final RuleInfo ruleInfo;
	private final String uniqueRuleName;
	private final String uniqueErrorFieldName;
	private final String mTVA;
	private MetaRegelFehlertexte metaRegelFehlertexte;

	private final boolean withOmissionErrors;
	private final boolean isHModel;
	private final boolean isProgLangWithUsb;

	private final CodeGenCondition codeGenCondition;
	private final Set<String> usbRootGroups;
	private final EntityIdsCollector entityIdsCollector;
	private int conditionCounter = 0;

	private IterationLimitCreatorBase iterationLimitCreator;
	private String combinedExpression = "";

	private List<String> conditionCodeInOrder = new LinkedList<>();
	// Groovy only
	private List<ICodeOutput> conditionCodeOutputInOrder = new LinkedList<>();

	private boolean prepareConditionCodeDone = false;

	private int ebeneAbDerDuplikatGesucht = -1;

	private final int semanticIndexLevel;

	private Collection<CodeGenFilterCondition> filterConditions;
	private Collection<CodeGenSetOperation> setOperations;
	private EntityIdsCollector filterEntityIdsCollector;
	private EntityIdsCollector setOperationEntityIdsCollector;

	private List<InitEntityListAndValueListConditionParam> entityListAndValueListInitCodes;

	private int checkObjectNum;

	public CodeGenRuleDefinition(CodeGenRuleDefinitionBuilder builder) {
		this.ruleInfo = builder.getRuleInfo();

		this.filterEntityIdsCollector = builder.getFilterEnditityIdsCollector();
		this.filterConditions = builder.getFilterConditions();
		this.setOperationEntityIdsCollector = builder.getSetOpEntityIdsCollector();
		this.setOperations = builder.getSetOperations();

		this.entityIdsCollector = builder.getEntityIdsCollector();
		this.codeGenCondition = builder.getCodeGenCondition();
		this.usbRootGroups = builder.getUsbRootGroups();

		this.uniqueRuleName = builder.getUniqueRuleName();
		this.uniqueErrorFieldName = builder.getUniqueErrorFieldName();
		this.mTVA = builder.getMTVA();
		this.metaRegelFehlertexte = builder.getRegelFehlertexte();

		this.iterationLimitCreator = builder.getIterationLimitCreator();
		this.withOmissionErrors = builder.isWithOmissionErrors();

		this.isHModel = builder.isHModel();
		this.isProgLangWithUsb = builder.isProgLangWithUsb();
		this.semanticIndexLevel = builder.getSemanticIndexLevel();

		this.entityListAndValueListInitCodes = builder.getEntityListAndValueListInitCodes();

		RepetitionNotUniqueCreator.setParamNumberForRuleWiederholungNichtEindeutig(this);
	}

	public String getConditionBooleanCode() {
		return codeGenCondition.getBooleanCode();
	}

	/**
	 * Background: Because of compiler problems in C++, we need to generate each partial condition
	 * into a separate line (see EXT-4342).
	 * <p>
	 * Called from the template. Writes each partial condition into a separate line and combines the
	 * partial results into the final results to compute the correct error type.
	 * </p>
	 *
	 * @return String Code that computes the error type.
	 */
	public String getConditionLineCode() {
		callPrepareConditionCode();
		return toCode("line");
	}

	private void callPrepareConditionCode() {
		if (!prepareConditionCodeDone) {
			StringBuilder combinedExpressionBuilder = new StringBuilder();

			conditionCounter = 0;
			prepareConditionCode(codeGenCondition, combinedExpressionBuilder);
			combinedExpression = combinedExpressionBuilder.toString();
			prepareConditionCodeDone = true;
		}
	}

	public List<String> getConditionCodeInOrder() {
		return conditionCodeInOrder;
	}

	public String getCombinedExpression() {
		return combinedExpression;
	}

	/**
	 * @param condition
	 * 		- Condition for which we want to generate code
	 * @param combinedExpressionBuilder
	 * 		- {@link StringBuilder} to combine all expressions
	 */
	private void prepareConditionCode(CodeGenCondition condition, StringBuilder combinedExpressionBuilder) {
		if (condition instanceof CompositeCondition) {
			CompositeCondition zusGesBedingung = (CompositeCondition) condition;
			List<CodeGenCondition> bedingungen = zusGesBedingung.getBedingungen();
			prepareConditionCode(bedingungen.get(0), combinedExpressionBuilder);
			for (int i = 1; i < bedingungen.size(); i++) {
				//TODO[Template]: Figure out a way to put this into a nice template structure.
				combinedExpressionBuilder.append(String.format(".combine%s(", zusGesBedingung.getLogOpCode()));
				prepareConditionCode(bedingungen.get(i), combinedExpressionBuilder);
				combinedExpressionBuilder.append(")");
			}
		} else {
			combinedExpressionBuilder.append("tb" + conditionCounter);
			String conditionCode = condition.toCode();
			conditionCodeInOrder.add(conditionCode);
			// Groovy only
			conditionCodeOutputInOrder.add(toCodeOutput(conditionCounter, conditionCode));
			conditionCounter++;
		}
	}

	// Groovy only
	private ICodeOutput toCodeOutput(int condCounter, String conditionCode) {
		return () -> "\ttb" + condCounter + " = " + conditionCode + ";";
	}

	// Groovy only
	private class ConditionClosure {
		private static final String CONDITIONCLOSURE_PREFIX = "condition_";
		private String closureName;
		private LogOp logOp;
		private List<Object> elements = new LinkedList<>();

		public ConditionClosure(int closureNumber, LogOp logOp) {
			this.closureName = String.format("%s%d", CONDITIONCLOSURE_PREFIX, closureNumber);
			this.logOp = logOp;
		}

		public void add(Object elem) {
			elements.add(elem);
		}

		public String getClosureCall() {
			return String.format("%s()", closureName);
		}

		public String getClosureCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("Closure<Boolean> %s = { ", closureName));
			Iterator<Object> iterator = elements.iterator();
			boolean firstElem = true;
			while (iterator.hasNext()) {
				if (firstElem) {
					firstElem = false;
				} else {
					sb.append(logOp == LogOp.And ? " && " : " || ");
				}
				Object nextElem = iterator.next();
				if (nextElem instanceof ConditionClosure) {
					sb.append(((ConditionClosure) nextElem).getClosureCall());
				} else if (nextElem instanceof CodeGenCondition) {
					sb.append(((CodeGenCondition) nextElem).getBooleanCode());
				}
			}
			sb.append(" }\n");
			return sb.toString();
		}
	}

	// Groovy only
	private List<ConditionClosure> conditionBooleanClosures = new LinkedList<>();
	private boolean prepareConditionBooleanClosuresDone = false;

	// Groovy only
	private void prepareConditionBooleanClosures(CodeGenCondition condition, ConditionClosure outerClosure, Integer startIndex) {
		if (condition instanceof CompositeCondition) {
			CompositeCondition zusGesBedingung = (CompositeCondition) condition;
			List<CodeGenCondition> bedingungen = zusGesBedingung.getBedingungen();
			ConditionClosure innerClosure = new ConditionClosure(conditionBooleanClosures.size(), zusGesBedingung.getLogOp());
			conditionBooleanClosures.add(innerClosure);
			if (outerClosure != null) {
				outerClosure.add(innerClosure);
			}
			if (startIndex != null) {
				prepareConditionBooleanClosures(bedingungen.get(startIndex), innerClosure, null);
				int endIndex = Math.min(startIndex + CodeGenRuleConstants.NUM_MAX_CONDITIONS_PER_CONDITIONBOOLEAN_GROOVY,
						bedingungen.size());
				for (int i = startIndex + 1; i < endIndex; i++) {
					prepareConditionBooleanClosures(bedingungen.get(i), innerClosure, null);
				}
			} else if (bedingungen.size() > CodeGenRuleConstants.NUM_MAX_CONDITIONS_PER_CONDITIONBOOLEAN_GROOVY) {
				for (int i = 0; i * CodeGenRuleConstants.NUM_MAX_CONDITIONS_PER_CONDITIONBOOLEAN_GROOVY < bedingungen.size(); i++) {
					prepareConditionBooleanClosures(
							condition,
							innerClosure,
							i * CodeGenRuleConstants.NUM_MAX_CONDITIONS_PER_CONDITIONBOOLEAN_GROOVY
					);
				}
			} else {
				prepareConditionBooleanClosures(bedingungen.get(0), innerClosure, null);
				for (int i = 1; i < bedingungen.size(); i++) {
					prepareConditionBooleanClosures(bedingungen.get(i), innerClosure, null);
				}
			}
		} else if (outerClosure != null) {
			outerClosure.add(condition);
		}
	}

	// Groovy only
	public boolean getUseSplitClosuresForConditionBooleanCode() {
		if (codeGenCondition instanceof CompositeCondition) {
			callPrepareConditionCode();
			if (conditionCodeOutputInOrder.size() >= CodeGenRuleConstants.NUM_MAX_CONDITIONS_PER_CONDITIONBOOLEAN_GROOVY) {
				if (!prepareConditionBooleanClosuresDone) {
					prepareConditionBooleanClosures(codeGenCondition, null, null);
					prepareConditionBooleanClosuresDone = true;
				}
				return true;
			}
		}
		return false;
	}

	// Groovy only
	public String getSplitClosuresConditionBooleanCode() {
		ListIterator<ConditionClosure> iterator = conditionBooleanClosures.listIterator(conditionBooleanClosures.size());
		StringBuilder sb = new StringBuilder();
		while (iterator.hasPrevious()) {
			sb.append(iterator.previous().getClosureCode());
		}
		return sb.toString();
	}

	// Groovy only
	public String getSplitClosuresConditionBooleanCalls() {
		Iterator<ConditionClosure> iterator = conditionBooleanClosures.iterator();
		return iterator.next().getClosureCall();
	}

	// Groovy only
	public boolean getUseClosureForConditionLineCode() {
		callPrepareConditionCode();
		return conditionCodeOutputInOrder.size() >= CodeGenRuleConstants.NUM_CONDITIONLINES_PER_METHOD_GROOVY;
	}

	// Groovy only
	public String getConditionLineSplitFunctionCalls() {
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(conditionCodeOutputInOrder.size(),
				MetaSprachUnterschiedeHelper.CLOSURE_NAME_CONDITIONLINECODE_TEMPLATE + "()",
				CodeGenRuleConstants.NUM_CONDITIONLINES_PER_METHOD_GROOVY, ProgrammingLanguage.GROOVY);
	}

	// Groovy only
	public String getConditionLineSplitFunctionCode() {
		return SplitFunctionsHelper.deriveSplitFunctionCode(conditionCodeOutputInOrder,
				MetaSprachUnterschiedeHelper.GROOVY_CONDITIONLINECODE_CLOSURE_NAME,
				CodeGenRuleConstants.NUM_CONDITIONLINES_PER_METHOD_GROOVY, ProgrammingLanguage.GROOVY);
	}

	public String getUniqueErrorFieldName() {
		return uniqueErrorFieldName;
	}

	public String getErrorCode() {
		return new StringParameter(ruleInfo.getRule().getErrorCode()).toCode();
	}

	public String getUniqueRuleName() {
		return uniqueRuleName;
	}

	public boolean isUsbPattern() {
		return ruleInfo.isUSBPattern();
	}

	public List<String> getUsbRootGroupsCode() {
		return usbRootGroups.stream()
				.map(s -> new StringParameter(s).toCode())
				.collect(Collectors.toList());
	}

	public String getMTVA() {
		return mTVA;
	}

	public String getFullName() {
		return ruleInfo.getRule().getFullName();
	}

	@Override
	public String getFullRuleName() {
		return ruleInfo.getRule().getFullName();
	}

	public String getRuleType() {
		switch (ruleInfo.getRule().getSeverityType()) {
			case ERROR:
				return ERROR;
			case WARNING:
				return WARNING;
			case INFO:
				return INFO;
			default:
				throw new IllegalArgumentException(
						"The rule type " + ruleInfo.getRule().getSeverityType() + " is not supported (rule" + ruleInfo.getRule()
								.getFullName() + ").");
		}
	}

	// in vba there is no enumeration for the rule type (like in java), but the constants TYP_FEHLER and TYP_HINWEIS
	public String getRuleTypeVbaCode() {
		return getRuleType().toUpperCase();
	}

	public boolean isUsbPatternForRootGroup() {
		return ruleInfo.isUsbPatternForRootGroup();
	}

	public String getStartIndicesCode() {
		return iterationLimitCreator.getStartIndicesCode();
	}

	public String getEndIndicesCode() {
		return iterationLimitCreator.getEndIndicesCode();
	}

	/**
	 * Level of error field for which semantic index is specified in the rule condition or some
	 * negative value if no semantic index is specified for the error field path. <br>
	 * Level 0 means root group repetition, USB is <b>not</b> considered here.
	 */
	public int getSemanticIndexLevel() {
		return semanticIndexLevel;
	}

	public boolean isLevelIterationOptimization() {
		if (isHModel) {
			return false;
		}
		return iterationLimitCreator.isLevelIterationOptimization();
	}

	public boolean isIteration() {
		return iterationLimitCreator.isIteration();
	}

	public String getRepetitionNotUniqueInitCode() {
		return RepetitionNotUniqueCreator.getInitCode(this);
	}

	public String getEntityListAndValueListInitCode() {
		return entityListAndValueListInitCodes.stream()
				.map(InitEntityListAndValueListConditionParam::toCode)
				.collect(Collectors.joining());
	}

	public String getRepetitionNotUniqueClearCode() {
		return RepetitionNotUniqueCreator.getClearCode(this);
	}

	public boolean isRepetitionNotUniqueRule() {
		return AnalyseService.beinhaltetWiederholungNichtEindeutig(codeGenCondition);
	}

	public String getAddValidatorMessageCode() {
		/*
		 * "metaRegelFehlertexte": Used only for C++-Code generation of error messages.
		 */
		if (withOmissionErrors) {
			return new AddValidatorMessage(this, metaRegelFehlertexte).toCode("WithErrorType");
		} else {
			return new AddValidatorMessage(this, metaRegelFehlertexte).toCode("NoErrorType");
		}
	}

	public boolean isWithOmissionErrors() {
		return withOmissionErrors;
	}

	@Override
	public List<IParseTreeEntity> getRulePartsForCodeGeneration() {
		return Collections.singletonList(codeGenCondition);
	}

	@Override
	public boolean isHModel() {
		return isHModel;
	}

	@Override
	public boolean isProgLangWithUsb() {
		return isProgLangWithUsb;
	}

	public boolean isValidForRuleType(RuleType type) {
		switch (type) {
			case ALL:
				return true;
			case INFO:
				return ruleInfo.getRule().getSeverityType() == RuleSeverityType.INFO;
			case WARNING:
				return ruleInfo.getRule().getSeverityType() == RuleSeverityType.WARNING;
			case ERROR:
				return ruleInfo.getRule().getSeverityType() == RuleSeverityType.ERROR;
			default:
				throw new IllegalArgumentException("Regelart " + type + " wird nicht unterstützt");
		}
	}

	@Override
	public int getEbeneAbDerDuplikatGesucht() {
		return ebeneAbDerDuplikatGesucht;
	}

	@Override
	public void setEbeneAbDerDuplikatGesucht(int level) {
		ebeneAbDerDuplikatGesucht = level;
	}

	public boolean isPreliminary() {
		// Currently exactly the auto generated rules are preliminary
		return this.ruleInfo.getAutogenType() != null;
	}

	public String getFormalErrorId() {
		switch (this.ruleInfo.getAutogenType()) {
			case MANDATORY_FIELD:
				return MANDATORY_FIELD_ERROR_ID;
			case UNIQUE_FIELD:
				return UNIQUE_FIELD_ERROR_ID;
			case UNIQUE_VALUE:
				return UNIQUE_VALUE_ERROR_ID;
			case SOLITARY_FIELD:
				return SOLITARY_INDEX_ERROR_ID;
			default:
				throw new IllegalArgumentException(UNKNOWN_AUTO_GENERATED_RULE_TYPE + ruleInfo.getAutogenType());
		}
	}

	public String getFormalMsgId() {
		switch (this.ruleInfo.getAutogenType()) {
			case MANDATORY_FIELD:
				return MANDATORY_FIELD_MESSAGE_ID;
			case UNIQUE_FIELD:
			case UNIQUE_VALUE:
				return UNIQUE_FIELD_MESSAGE_ID;
			case SOLITARY_FIELD:
				return SOLITARY_INDEX_MESSAGE_ID;
			default:
				throw new IllegalArgumentException(UNKNOWN_AUTO_GENERATED_RULE_TYPE + ruleInfo.getAutogenType());
		}
	}

	public boolean isOmissionError() {
		switch (this.ruleInfo.getAutogenType()) {
			case MANDATORY_FIELD:
				return true;
			case UNIQUE_FIELD:
			case UNIQUE_VALUE:
				return false;
			case SOLITARY_FIELD:
				return true;
			default:
				throw new IllegalArgumentException(UNKNOWN_AUTO_GENERATED_RULE_TYPE + ruleInfo.getAutogenType());
		}
	}

	public List<String> getRtControllerParamsCreationCode() {
		return entityIdsCollector.getAll().stream()
				.map(co -> co.toCode("varAssignment"))
				.collect(Collectors.toList());
	}

	public List<String> getFilterParamsCreationCode() {
		return filterEntityIdsCollector.getAll().stream()
				.map(co -> co.toCode("varAssignment"))
				.collect(Collectors.toList());
	}

	public List<String> getFilterCode() {
		return filterConditions.stream()
				.map(CodeGenFilterCondition::toCode)
				.collect(Collectors.toList());
	}

	public List<String> getSetOpParamsCreationCode() {
		return setOperationEntityIdsCollector.getAll().stream()
				.map(co -> co.toCode("varAssignment"))
				.collect(Collectors.toList());
	}

	public List<String> getSetOperationCode() {
		return setOperations.stream()
				.map(CodeGenSetOperation::toCode)
				.collect(Collectors.toList());
	}

	public boolean hasFilterConditions() {
		return !filterConditions.isEmpty();
	}

	public boolean isParallelIteration() {
		return AnalyseService.isParallelIteration(codeGenCondition, true);
	}

	public boolean isErrorEntityAField() {
		return ruleInfo.getErrorEntity().isField();
	}

	public List<String> getParallelIterationGroupsCode() {
		return AnalyseService.getParallelIterationGroups(codeGenCondition).stream()
				.map(s -> new StringParameter(s).toCode())
				.collect(Collectors.toList());
	}

	public boolean hasUniqueSemanticIndex4ErrorField4NotIteratingLevel() {
		return ruleInfo.hasUniqueSemanticIndex4ErrorField4NotIteratingLevel();
	}

	public String getSemanticIndexValueCode() {
		if (!hasUniqueSemanticIndex4ErrorField4NotIteratingLevel()) {
			String errMsg = "This method should only be called if the error field of the rule is referenced with semantic index in the rule condition.";
			throw new IllegalArgumentException(errMsg);
		}
		Optional<EntityId> errorFieldWithSemanticIndexOpt = entityIdsCollector.getEntityIds().stream()
				.filter(this::containsErrorFieldWithSemanticIndex)
				.findAny();
		EntityId errorFieldWithSemanticIndex = errorFieldWithSemanticIndexOpt
				.orElseThrow(() -> new IllegalArgumentException(
						"Unexpected error for rule '" + ruleInfo.getFullName() + "'. Error field with semantic index should be present."));
		boolean isConstant = ruleInfo.isUniqueSemanticIndex4ErrorFieldConstant();
		return new SemanticIndexValue(errorFieldWithSemanticIndex, isConstant).toCode();
	}

	private boolean containsErrorFieldWithSemanticIndex(EntityId eId) {
		if (eId.getSemanticIndex().isEmpty()) {
			return false;
		}
		if (eId.isField()) {
			return eId.getFullName().equals(ruleInfo.getErrorEntity().getFullName());
		}
		return EntityUtils.isDescendantOrEqual(ruleInfo.getErrorEntity().getFullName(), eId.getFullName());
	}

	public String getGroupOnErrorFieldPathWithSemanticIndexFullName() {
		if (!hasUniqueSemanticIndex4ErrorField4NotIteratingLevel()) {
			String errMsg = "This method should only be called if the error field of the rule is referenced with semantic index in the rule condition.";
			throw new IllegalArgumentException(errMsg);
		}
		return EntityUtils.getPathUpToLevel(semanticIndexLevel, ruleInfo.getErrorEntity().getFullName());
	}

	public int getSemanticIndexLevelInIterationState() {
		// the iteration state uses usb as level on the first position so increase the semanticIndexLevel level by one
		return semanticIndexLevel + (isProgLangWithUsb ? 1 : 0);
	}

	public int getCheckObjectNum() {
		return checkObjectNum;
	}

	public void setCheckObjectNum(int checkObject) {
		this.checkObjectNum = checkObject;
	}

	public RuleInfo getRuleInfo() {
		return ruleInfo;
	}
}
