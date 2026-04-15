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

package com.mgmtp.a12.kernel.core.codegen.internal.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.ToIntFunction;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeFragmentContainer;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeOutput;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 *
 */
public class SplitFunctionsHelper {

	public static final int ELEMENTS_PER_SPLIT_METHOD_DEFAULT = 50;

	private SplitFunctionsHelper() {
		throw new IllegalStateException("Utility class");
	}

	public static String deriveSplitFunctionCode(
			Collection<? extends ICodeOutput> codeGenObjects,
			String methodSigTemplate, int elementsPerMethod, ProgrammingLanguage lang) {
		return deriveSplitFunctionCode(codeGenObjects, methodSigTemplate, elementsPerMethod, lang, "", "");
	}

	/**
	 * Diese Methode splittet den Code evtl. in mehrere Methoden.
	 * <p>
	 * Aufgrund einer Java-Einschränkung kann in Java, im Gegensatz zu C++, die Initialisierung
	 * nicht über lange Initialisierungslisten erfolgen. In Java existiert eine Begrenzung
	 * (65000Bytes) der statitischen Initialisierer, sowie der einzelnen Methoden (inklusive des
	 * Konstruktors).
	 * <p>
	 * Aus diesem Grund werden die diversen Aufrufe in mehrere private Teilmethoden zerlegt. Dabei
	 * bedarf es keiner besonderen Reihenfolge.
	 */
	public static String deriveSplitFunctionCode(
			Collection<? extends ICodeOutput> codeGenObjects,
			String methodSigTemplate, int elementsPerMethod, ProgrammingLanguage lang,
			String prefix, String suffix) {
		return deriveSplitFunctionCode(codeGenObjects, methodSigTemplate, elementsPerMethod, lang, prefix, suffix, "");
	}

	/**
	 * This code splits method calls into different methods.
	 * <p>
	 * In Java, there exists the restriction that methods and initializers (including constructors)
	 * each must not be bigger than a certain threshold (64kiB).
	 * For this reason, the different calls are separated into multiple partial methods.
	 *
	 * @param codeGenObjects
	 * 		the objects that should be split into separate methods. The body of the method.
	 * @param methodSigTemplate
	 * 		the template for the method signature.
	 * @param elementsPerMethod
	 * 		the number of elements per method.
	 * @param lang
	 * 		the programming language, the code is generated for.
	 * @param prefix
	 * 		the prefix for each of the generated methods. May be empty but not
	 * 		<code>null</code>.
	 * @param suffix
	 * 		the suffix for each of the generated methods. May be empty but not
	 * 		<code>null</code>.
	 * @param param
	 * 		the parameter used in the {@link ICodeOutput#toCode(String)} method. May
	 * 		be empty or <code>null</code> if the base template should be used.
	 */
	public static String deriveSplitFunctionCode(
			Collection<? extends ICodeOutput> codeGenObjects,
			String methodSigTemplate, int elementsPerMethod, ProgrammingLanguage lang,
			String prefix, String suffix, String param) {
		// alle notwendigen StringBuffer erzeugen
		int numSplits = computeNumSplits(codeGenObjects.size(), elementsPerMethod);
		List<StringBuilder> lBuf = new ArrayList<>(numSplits);

		for (int i = 0; i < numSplits; i++) {
			lBuf.add(new StringBuilder());
		}
		// die StringBuffer mit dem Code befüllen
		int objCounter = 1;
		for (ICodeOutput obj : codeGenObjects) {
			int numBuf = computeNumSplits(objCounter, elementsPerMethod) - 1;
			if (numBuf < 0 || numBuf >= lBuf.size()) {
				throw new IllegalArgumentException(String.format(
						"Fehler beim Splitten des Codes in mehrere Einheiten (Objekt #%d wurde bei %d Elementen pro Einheit Einheit #%d zugewiesen).",
						objCounter, elementsPerMethod, numBuf));
			}
			lBuf.get(numBuf).append(obj.toCode(param));
			lBuf.get(numBuf).append("\n");
			++objCounter;
		}

		return concatStringBuilders(methodSigTemplate, lang, lBuf, prefix, suffix);
	}

	/**
	 * @return the code containing the split function based on a given size function for
	 * 		<code>codeGenObjects</code>.
	 */
	public static <T extends ICodeOutput> String deriveSplitFunctionCodeBasedOnSize(
			Collection<T> codeGenObjects, String methodSigTemplate, int sizePerUnit, ProgrammingLanguage lang,
			ToIntFunction<T> sizeFunction) {
		List<StringBuilder> allSBs = new ArrayList<>();
		StringBuilder actualSB = new StringBuilder();
		int accumulatedSize = 0;
		for (T obj : codeGenObjects) {
			int actualSize = sizeFunction.applyAsInt(obj);
			if (shouldBeAppendedToActualSplit(accumulatedSize, actualSize, sizePerUnit)) {
				actualSB.append(obj.toCode());
				actualSB.append("\n");
				accumulatedSize += actualSize;
			} else {
				// new split -> add actual split to the list
				allSBs.add(actualSB);
				actualSB = new StringBuilder();
				actualSB.append(obj.toCode());
				actualSB.append("\n");
				accumulatedSize = actualSize;
			}
		}
		// the last created string builder must be added
		allSBs.add(actualSB);

		return concatStringBuilders(methodSigTemplate, lang, allSBs, "", "");
	}

	private static String concatStringBuilders(String methodSigTemplate, ProgrammingLanguage lang, List<StringBuilder> builders,
			String prefix, String suffix) {
		// die Methoden konkatenieren
		int bufCounter = 0;
		StringBuilder buf = new StringBuilder();
		for (StringBuilder innerSB : builders) {
			buf.append(String.format(methodSigTemplate, bufCounter));
			if (lang == ProgrammingLanguage.VBA) {
				buf.append(String.format("%s%s%sEnd Sub%n", prefix, innerSB.toString(), suffix));
			} else {
				buf.append(String.format("{%n%s%s%s}%n", prefix, innerSB.toString(), suffix));
			}
			++bufCounter;
		}

		return buf.toString();
	}

	private static boolean shouldBeAppendedToActualSplit(int accumulatedSize, int actualSize, int sizePerUnit) {
		// in case the first element contains more than the number of sub elements per unit
		return (accumulatedSize + actualSize) <= sizePerUnit || accumulatedSize == 0;
	}

	public static String deriveSplitFunctionCodeCalls(int collectionSize, String methodName, int elementsPerMethod,
			ProgrammingLanguage lang) {
		return deriveSplitFunctionCodeCalls(collectionSize, methodName, elementsPerMethod, lang, null, "");
	}

	/**
	 * Liefert den notwendigen Code um die in der Methode 'deriveSplitFunctionCode' erzeugten
	 * Teilmethoden aufzurufen. Die Aufrufe der Teilmethoden erfolgen einfach nacheinander. Die
	 * Teilmethoden bekommen keine Argumente.
	 */
	public static String deriveSplitFunctionCodeCalls(int collectionSize, String methodName, int elementsPerMethod,
			ProgrammingLanguage lang, String separator, String code4EmptyCollection) {
		if (collectionSize == 0) {
			return code4EmptyCollection;
		}
		int numCalls = computeNumSplits(collectionSize, elementsPerMethod);
		return deriveSplitFunctionCallsCode(numCalls, methodName, lang, separator);
	}

	/**
	 * @return the code containing the split function calls.
	 */
	public static String deriveSplitFunctionCallsCode(int numberOfCalls, String methodNameTemplate, ProgrammingLanguage lang,
			String separator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numberOfCalls; i++) {
			sb.append(String.format(methodNameTemplate, i));
			if (separator != null && i != numberOfCalls - 1) {
				sb.append(separator);
			} else if (lang != ProgrammingLanguage.VBA) {
				sb.append(";\n");
			} else {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Liefert die Anzahl der Einheiten, in die die übergebene Anzahl von Elementen gesplittet wird.
	 *
	 * @param numElems
	 * @param elemsPerUnit
	 * @return Anzahl der Einheiten
	 */
	public static int computeNumSplits(int numElems, int elemsPerUnit) {
		return (int) Math.ceil((double) numElems / elemsPerUnit);
	}

	/**
	 * @return the number of splits based on the sub elements contained in <code>elements</code>
	 * 		(see also {@link ICodeFragmentContainer#getNumberOfFragments()}).
	 */
	public static <T> int computeNumSplitsBasedOnSize(Collection<T> elements, int maxSizePerUnit, ToIntFunction<T> sizeFunction) {
		if (elements.isEmpty()) {
			return 0;
		}
		// more than 0 elements means at least one split
		int numSplits = 1;
		int accumulatedSize = 0;
		for (T element : elements) {
			int actualSize = sizeFunction.applyAsInt(element);
			if (shouldBeAppendedToActualSplit(accumulatedSize, actualSize, maxSizePerUnit)) {
				accumulatedSize += actualSize;
			} else {
				// here we generate a new split
				numSplits++;
				accumulatedSize = actualSize;
			}
		}
		return numSplits;
	}
}
