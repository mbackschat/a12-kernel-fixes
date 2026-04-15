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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.calc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenFilterCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdsCollector;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util.MetaSprachUnterschiedeHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.IterationLimitCreatorBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RepetitionNotUniqueCreator;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.IRepetitionNotUniqueHandler;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Modelliert die Berechnung für ein Feld für die Codegenerierung.
 * <p>
 * Hier werden die Ausgangsinformationen bestehend aus Feldentität und {@link CalculationInfo}s in
 * {@link CodeGenCalculationAlternative}n umgewandelt, um sie im zugehörigen StringTemplate
 * auszugeben.
 */
@StringTemplateGroup("calc")
public class CodeGenCalculatedField extends STParameterClassBase implements IRepetitionNotUniqueHandler {
	private final EntityId berechnetesFeld;

	/**
	 * Die gespeicherten Ausgangsdaten.
	 */
	protected final List<ICalculationInfo> calcInfos;

	/**
	 * Zusätzliches Flag zur Weiterreichung.
	 */
	private final boolean isHModel;

	private final boolean isProgLangWithUsb;

	/**
	 * Die konvertierten CodeGenObjekte
	 */
	private final List<CodeGenCalculationAlternative> calculationAlternatives;

	private final IterationLimitCreatorBase iterationLimitCreator;

	private final String modelName;

	private int ebeneAbDerDuplikatGesucht = -1;
	private EntityIdsCollector rtCtrlParamCodeCollector;

	private final int parallelIterationLevel;

	private final EntityIdsCollector filterEntityIdsCollector;
	private final Collection<CodeGenFilterCondition> filterConditions;

	private int calcObjectNum;

	/**
	 * Dieser Konstruktor wird bei der Codegenerierung aufgerufen.
	 */
	CodeGenCalculatedField(CodeGenCalculatedFieldBuilder builder) {
		this.calcInfos = builder.getCalcInfos();
		this.isHModel = builder.isHModel();
		this.isProgLangWithUsb = builder.isProgLangWithUsb();
		this.berechnetesFeld = builder.getCalculatedField();
		this.calculationAlternatives = builder.getCalculationAlternatives();
		this.iterationLimitCreator = builder.getIterationLimitCreator();
		this.modelName = builder.getData().getParams().getName();
		this.rtCtrlParamCodeCollector = builder.getRtControllerParamsCreationCodeCollector();
		this.parallelIterationLevel = builder.getParallelIterationLevel();
		this.filterEntityIdsCollector = builder.getFilterEntityIdsCollector();
		this.filterConditions = builder.getFilterConditions();
	}

	// used for JS_VK
	public String getName() {
		return this.modelName;
	}

	public boolean isHModel() {
		return isHModel;
	}

	@Override
	public boolean isProgLangWithUsb() {
		return isProgLangWithUsb;
	}

	@Override
	public String getFullRuleName() {
		return EntityUtils.getParentPath(calcInfos.get(0).getName());
	}

	public EntityId getBerechnetesFeld() {
		return berechnetesFeld;
	}

	public String getVordruckBerechnetesFeld() {
		return EntityUtils.getRootGroupName(berechnetesFeld.getFullName());
	}

	public String getCalculatedFieldName() {
		return berechnetesFeld.getFullName();
	}

	public String getCalcFuncName() {
		return "calc_" + getUniqueName();
	}

	public String getUniqueName() {
		return berechnetesFeld.getUniqueName();
	}

	public boolean isUsbMuster() {
		return berechnetesFeld.getMaxUsb() >= 0;
	}

	/**
	 * wird vom Java-Template verwendet. Dient dazu, die Behandlung für USB Iteration über
	 * verschiedene Vordrucke hinweg zu behandeln.
	 *
	 * @return true, wenn für das berechnete Feld eine USB Iteration über verschiedene Vordrucke
	 * 		verwendet wird.
	 */
	public boolean isUsbMusterUeberVordrucke() {
		List<IParseTreeEntity> constituents = new ArrayList<>();
		constituents.addAll(getRulePartsForCodeGeneration());
		constituents.add(berechnetesFeld);
		return AnalyseService.isUSBPatternForRootGroups(constituents);
	}

	/**
	 * wird vom Java-Template verwendet.
	 */
	public String getStartIndicesCode() {
		return iterationLimitCreator.getStartIndicesCode();
	}

	/**
	 * wird vom Java-Template verwendet.
	 */
	public String getEndeIndicesCode() {
		return iterationLimitCreator.getEndIndicesCode();
	}

	/**
	 * wird von JAVA Templates verwendet.
	 */
	public String getParameterRepetitionNotUniqueInitCode() {
		return RepetitionNotUniqueCreator.getInitCode(this);
	}

	/**
	 * wird von JAVA Templates verwendet.
	 */
	public String getParameterRepetitionNotUniqueClearCode() {
		return RepetitionNotUniqueCreator.getClearCode(this);
	}

	/**
	 * wird von JAVA Templates verwendet.
	 */
	public String getCalculationCode() {
		StringBuilder sb = new StringBuilder();
		calculationAlternatives.forEach(ba -> sb.append(ba.toCode()));
		return sb.toString();
	}

	// Groovy only
	public boolean getUseClosureForCalculationCode() {
		return calculationAlternatives.size() >= CodeGenConstants.NUM_CALCULATIONS_PER_METHOD_GROOVY;
	}

	// Groovy only
	public String getCalculationSplitFunctionCalls() {
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(calculationAlternatives.size(),
				MetaSprachUnterschiedeHelper.CLOSURE_NAME_CALCULATIONCODE_TEMPLATE + "()",
				CodeGenConstants.NUM_CALCULATIONS_PER_METHOD_GROOVY, ProgrammingLanguage.GROOVY, " || ", "");
	}

	// Groovy only
	public String getCalculationSplitFunctionCode() {
		calculationAlternatives.forEach(ba -> ba.setUseClosureForCalculationCode(true));
		return SplitFunctionsHelper.deriveSplitFunctionCode(calculationAlternatives,
				MetaSprachUnterschiedeHelper.GROOVY_CALCULATIONCODE_CLOSURE_NAME,
				CodeGenConstants.NUM_CALCULATIONS_PER_METHOD_GROOVY, ProgrammingLanguage.GROOVY, "", "return false;\n");
	}

	public void prepareCodeGen() {
		RepetitionNotUniqueCreator.setParamNumberForRuleWiederholungNichtEindeutig(this);
	}

	@Override
	public List<IParseTreeEntity> getRulePartsForCodeGeneration() {
		return calculationAlternatives.stream()
				.flatMap(ba -> Stream.<IParseTreeEntity>of(ba.getCodeGenPreCondition(), ba.getCodeGenCalcFunction()))
				.filter(Objects::nonNull)
				.toList();
	}

	@Override
	public int getEbeneAbDerDuplikatGesucht() {
		return ebeneAbDerDuplikatGesucht;
	}

	@Override
	public void setEbeneAbDerDuplikatGesucht(int level) {
		ebeneAbDerDuplikatGesucht = level;
	}

	public List<String> getRtControllerParamsCreationCode() {
		return rtCtrlParamCodeCollector.getAll().stream()
				.map(co -> co.toCode("varAssignment"))
				.toList();
	}

	public List<String> getFilterParamsCreationCode() {
		return filterEntityIdsCollector.getAll().stream()
				.map(co -> co.toCode("varAssignment"))
				.toList();
	}

	public List<String> getFilterCode() {
		return filterConditions.stream()
				.map(CodeGenFilterCondition::toCode)
				.toList();
	}

	public boolean isParallelIteration() {
		return parallelIterationLevel >= 0;
	}

	public String getParallelIterationGroupsCode() {
		Set<String> codeFragments = calculationAlternatives.stream()
				.flatMap(calcAlt -> Stream.of(calcAlt.getCodeGenPreCondition(), calcAlt.getCodeGenCalcFunction()))
				.flatMap(p -> AnalyseService.getParallelIterationGroups(p).stream())
				.map(s -> new StringParameter(s).toCode())
				.collect(Collectors.toCollection(TreeSet::new));
		String calcFieldName = berechnetesFeld.getFullName();
		String parallelIterationGroupOnCalcFieldPath = EntityUtils.getPathUpToLevel(parallelIterationLevel, calcFieldName);
		codeFragments.add(new StringParameter(parallelIterationGroupOnCalcFieldPath).toCode());
		return StringUtils.join(codeFragments, ", ");
	}

	public List<String> getUsbRootGroupsCode() {
		return calculationAlternatives.stream()
				.flatMap(calcAlt -> Stream.of(calcAlt.getCodeGenPreCondition(), calcAlt.getCodeGenCalcFunction()))
				.flatMap(p -> AnalyseService.getUsbIterationRootGroups(p).stream())
				.map(s -> new StringParameter(s).toCode())
				.toList();
	}

	public int getCalcObjectNum() {
		return calcObjectNum;
	}

	public void setCalcObjectNum(int calcObject) {
		this.calcObjectNum = calcObject;
	}
}
