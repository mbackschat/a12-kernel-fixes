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

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CodeGenCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.CodeGenOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;

/**
 * Modelliert eine Berechnungsalternative für die Berechnungsfunktion eines Feldes für die
 * Codegenerierung. Eine Berechnungsalternative besteht aus drei Teilen:
 * <ul>
 * <li>dem berechneten Feld, für das die Berechnung durchgeführt wird,</li>
 * <li>der Vorbedingung, die angibt, ob dieser Zweig der Berechnung ausgeführt werden soll und</li>
 * <li>der Berechnungsfunktion</li>.
 * <p>
 * In dieser Klasse werden die allgemeinen Repräsentationen in spezielle CodeGen-Objekte verpackt,
 * um sie im zugehörigen Template verwenden zu können.
 */
@StringTemplateGroup("calc")
public class CodeGenCalculationAlternative extends STParameterClassBase {
	// Die ursprünglichen Objekte.
	private ICalculationInfo calcInfo;

	private CodeGenCondition codeGenPreCondition;

	private CodeGenOperation codeGenCalcFunction;

	private EntityId codeGenCalculatedField;

	/**
	 * Gibt die maximale Anzahl der Stellen einer Zahl zur Ausgabe von fehlerhaften Werten an. Wird
	 * vom MVK-Service hier durchgeschleift.
	 */
	private long maxDigitsGlobal;

	private final int maxFractionalDigits;

	private final int minFractionalDigits;

	private int parallelIterationLevel;

	// Groovy only
	public boolean useClosureForCalculationCode = false;

	public CodeGenCalculationAlternative(ICalculationInfo ci, CodeGenCondition preCondition, CodeGenOperation calcFunction,
			EntityId calculatedField, long maxDigitsGlobal,
			int minFractionalDigits, int maxFractionalDigits, int parallelIterationLevel) {
		assert ci.getCommonPrecondition().isEmpty() : "The common precondition is expected to have been expanded and distributed " +
				"to the individual computation alternatives before codegen";
		this.calcInfo = ci;
		this.maxDigitsGlobal = maxDigitsGlobal;
		this.codeGenPreCondition = preCondition;
		this.codeGenCalcFunction = calcFunction;
		this.codeGenCalculatedField = calculatedField;
		this.maxFractionalDigits = maxFractionalDigits;
		this.minFractionalDigits = minFractionalDigits;
		this.parallelIterationLevel = parallelIterationLevel;
	}

	public String getDescription() {
		return String.format("Berechnungsalternative aus Regel '%s'", calcInfo.getName());
	}

	/**
	 * @return den Inhalt des if-Statements für die Vorbedingung.
	 */
	public String getPreConditionBooleanCode() {
		if (codeGenPreCondition == null) {
			// Nicht vorhandene Vorbedingung bedeutet das Gleiche wie immer wahre Vorbedingung
			return "true";
		}
		return codeGenPreCondition.getBooleanCode();
	}

	public String getCalculatedFieldCode() {
		return codeGenCalculatedField.toCode();
	}

	public String getCalcFunctionCode() {
		return codeGenCalcFunction.toCode();
	}

	public String getMaxFractionalDigits() {
		return Integer.toString(maxFractionalDigits);
	}

	public String getMinFractionalDigits() {
		return Integer.toString(minFractionalDigits);
	}

	/**
	 * @return die max. Anzahl Stellen, die im Fehlerfall ausgegeben werden sollen (1 + Anzahl
	 * 		erlaubte Stellen).
	 */
	public String getMaxDigitsGlobal() {
		if (maxDigitsGlobal != -1) {
			return Long.toString(maxDigitsGlobal + 1);
		} else {
			return "-1";
		}
	}

	IParseTreeEntity getCodeGenCalcFunction() {
		return codeGenCalcFunction;
	}

	IParseTreeEntity getCodeGenPreCondition() {
		return codeGenPreCondition;
	}

	public boolean isParallelIteration() {
		return parallelIterationLevel >= 0;
	}

	public String getParallelIterationLevel() {
		return Integer.toString(parallelIterationLevel);

	}

	// Groovy only
	public boolean getUseClosureForCalculationCode() {
		return useClosureForCalculationCode;
	}

	// Groovy only
	public void setUseClosureForCalculationCode(boolean newVal) {
		useClosureForCalculationCode = newVal;
	}
}
