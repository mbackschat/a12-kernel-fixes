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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Abstract base class for the utilities classes that generate iteration limits.
 * Contains the common functionality for validation and calculation.
 */
public abstract class IterationLimitCreatorBase {

	public static final long MAX_PRUEF_KONTEXT = 5;
	protected final boolean isHModel;
	protected final ProgrammingLanguage progLang;
	protected boolean isProgLangWithUsb;

	public IterationLimitCreatorBase(CodeGenEntityData data) {
		this.isHModel = ExtmpUtils.isHModel(data.getModelSpecificSettings());
		this.progLang = data.getParams().getProgrammingLanguage();
		this.isProgLangWithUsb = this.progLang.isWithUsb();
	}

	public boolean isLevelIterationOptimization() {
		long[] iterationsEnde = getEbenenArrayFuerIterationsEnde();
		int produkt = 1;

		for (int i = 0; i < iterationsEnde.length; i++) {
			if (iterationsEnde[i] > 1) {
				produkt *= iterationsEnde[i];
				if (produkt > MAX_PRUEF_KONTEXT) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isIteration() {
		long[] iterStart = getEbenenArrayFuerIterationsStart();
		long[] iterEnde = getEbenenArrayFuerIterationsEnde();

		/**
		 * We ignore USB iterations and hence start at index 1.
		 */
		for (int i = 1; i < iterStart.length; i++) {
			if (iterEnde[i] > iterStart[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gibt das Codefragment zurück für den Start-Array für die Regel-Ebeneniterator. Das erste
	 * Element des Arrays ist die Angabe für den USB. Die geschweiften Klammern sind Teil des
	 * Templates und werden daher nicht mit erzeugt.
	 * <p>
	 * wird von C++ und JAVA Templates verwendet (delegiert von CodeGenRegeldefinition)
	 *
	 * @return Codefragment für den Start-Array
	 */
	public String getStartIndicesCode() {
		long[] startIndices = getEbenenArrayFuerIterationsStart();

		List<String> codeFragments = new ArrayList<>();
		if (isProgLangWithUsb) {
			codeFragments.add(getUSBCodeFragment(startIndices[0]));
		}

		for (int i = 1; i < startIndices.length; i++) {
			codeFragments.add(Long.toString(startIndices[i]));
		}

		return constructIterationsIndicesCode(codeFragments, StartEndIndex.START);
	}

	/**
	 * Gibt das Codefragment zurück für den End-Array für die Regel-Ebeneniterator. Das erste
	 * Element des Arrays ist die Angabe für den USB. Die geschweiften Klammern sind Teil des
	 * Templates und werden daher nicht mit erzeugt. In der Methode werden bereits die
	 * Optimierungsaufrufe generiert, wenn eine Optimierung für die Regel möglich ist.
	 * <p>
	 * wird von C++ und JAVA Templates verwendet (delegiert von CodeGenRegeldefinition)
	 *
	 * @return Codefragment für den Start-Array
	 */
	public String getEndIndicesCode() {
		List<String> codeFragments = new ArrayList<>();
		long[] endeIndices = getEbenenArrayFuerIterationsEnde();
		boolean[] optimierungsOption = getEbenenArrayFuerIterationsOptimierungsOption();

		if (isProgLangWithUsb) {
			codeFragments.add(getUSBCodeFragment(endeIndices[0]));
		}

		int zeilenIndex = endeIndices.length - 1;

		for (int i = 1; i < zeilenIndex; i++) {
			codeFragments.add(getEndeCodeForKontextUndLfdNr(endeIndices[i], (long) i - 1, optimierungsOption[i]));
		}
		codeFragments.add(getZIterationsEndeCode(endeIndices[zeilenIndex], optimierungsOption[zeilenIndex]));

		return constructIterationsIndicesCode(codeFragments, StartEndIndex.ENDE);
	}

	protected boolean isA12Model() {
		return !isHModel;
	}

	private String getUSBCodeFragment(long u) {
		return new USBCodeFragment(u).toCode();
	}

	/**
	 * Erzeugt die Liste der Anfangs bzw. End-Indices für die Iteration, abhängig von dem Parameter
	 * startEnde. Der USB ist dabei immer das erste Element.
	 */
	private String constructIterationsIndicesCode(List<String> codeFragments, StartEndIndex startEnde) {
		return new IterationIndices(codeFragments, startEnde).toCode();
	}

	private String getEndeCodeForKontextUndLfdNr(long index, long ebenenIndex, boolean isOptimierbar) {
		//TODO[Template]: Put this into a template file
		if (isOptimierbar) {
			String methodNameUndParameter = String.format("getMaxGesetzterKontext(%d)", ebenenIndex);
			return getOptimizationCodeFragment(methodNameUndParameter, index);
		} else {
			return String.valueOf(index);
		}
	}

	private String getOptimizationCodeFragment(String methodNameAndParameter, long commonIterationEnd) {
		return new OptimizationCodeFragment(methodNameAndParameter, commonIterationEnd).toCode();
	}

	/**
	 * Wenn es sich um eine Bedingung handelt, die für eine Zeile nur feuern kann, wenn in dieser
	 * Zeile mindestens ein Feld angegeben wurde, so wird für die Zeilenoptimierung als maximaler
	 * Schleifenwert der maximal gesezte Zeilenwert verwendet.
	 */
	protected String getZIterationsEndeCode(long normalIterationsEnde, boolean isOptimierbar) {
		//TODO[Template]: Put this into a template file
		if (ProgrammingLanguage.VBA.equals(progLang)) {
			return "letzteZeile";
		}
		if (isOptimierbar) {
			return getOptimizationCodeFragment("getMaxGesetzteZeilen()", normalIterationsEnde);
		}
		return Long.toString(normalIterationsEnde);
	}

	protected abstract long[] getEbenenArrayFuerIterationsStart();

	protected abstract boolean[] getEbenenArrayFuerIterationsOptimierungsOption();

	protected abstract long[] getEbenenArrayFuerIterationsEnde();
}
