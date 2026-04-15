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

import java.util.Arrays;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.ado.IExtmpRule;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.ado.IExtmpRule.IterationInterval;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.IndexArt;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

/**
 * Helper class to create iteration limits code for the validation.
 */
public class RuleIterationLimitCreator extends IterationLimitCreatorBase {

	private final RuleInfo ruleInfo;
	private final IEC ec;
	private final long[] ebenenArrayFuerIterationsStart;
	private final long[] ebenenArrayFuerIterationsEnde;

	public RuleIterationLimitCreator(RuleInfo ruleInfo, CodeGenData data) {
		super(data);
		this.ruleInfo = ruleInfo;
		this.ec = data.getEc();
		this.ebenenArrayFuerIterationsStart = createEbenenArrayFuerIterationsStart();
		this.ebenenArrayFuerIterationsEnde = createEbenenArrayFuerIterationsEnde();
	}

	@Override
	protected boolean[] getEbenenArrayFuerIterationsOptimierungsOption() {
		boolean[] result = new boolean[getSizeOfIterationsArray()];

		for (int i = 0; i < ebenenArrayFuerIterationsEnde.length; i++) {
			boolean isIteration = this.ebenenArrayFuerIterationsStart[i] < ebenenArrayFuerIterationsEnde[i];
			result[i] = isIteration && ebenenArrayFuerIterationsEnde[i] > IterationLimitCreatorBase.MAX_PRUEF_KONTEXT;
		}

		return result;
	}

	@Override
	public long[] getEbenenArrayFuerIterationsEnde() {
		return this.ebenenArrayFuerIterationsEnde;
	}

	private long[] createEbenenArrayFuerIterationsEnde() {
		long[] result = new long[getSizeOfIterationsArray()];
		result[0] = (ruleInfo.isUSBPattern()) ? EntityUtils.getMaxUsb(ruleInfo.getRule().getErrorEntity()) : getUStart();
		result[1] = getLfdNrIterationsEnde();
		if (isA12Model()) {
			long[] indices = getIterationsEndeFuerKontexte();
			System.arraycopy(indices, 0, result, 2, indices.length);
		}
		result[result.length - 1] = getZeilenIterationsEnde();

		int parallelIterationLevel = AnalyseService.getParallelIterationLevel(ruleInfo.getCondition(), ruleInfo.getErrorEntity(),
				ec);
		if (parallelIterationLevel >= 0) {
			result[parallelIterationLevel + 1] = IParseTreeEntity.PARALLEL_ITERATION;
		}

		return result;
	}

	/**
	 * Gibt einen Array zurück, der für jede Kontextebene des Aktionsfeldes das Ende der Iteration
	 * spezifiziert. Dies ist die maximale Wiederholbarkeit des Kontextes, falls für diese Ebene
	 * eine Iteration spezifiziert ist und 1 sonst.<br>
	 * <b>Precondition:</b> die Bedingung ist expandiert, enthält also keine direkten
	 * Referenzierungen von Kontexten mehr
	 */
	private long[] getIterationsEndeFuerKontexte() {

		long[] preresult = ruleInfo.getRule().getErrorEntity().getMax().clone();
		int end = ruleInfo.getRule().getErrorEntity().isGroup() ? preresult.length : preresult.length - 1;

		ICondition b = ruleInfo.getCondition();
		for (int i = 1; i < end; i++) {
			if (!AnalyseService.isIteration(b, i, true)) {
				preresult[i] = 1;
			}
		}
		return Arrays.copyOfRange(preresult, 1, end);
	}

	@Override
	public long[] getEbenenArrayFuerIterationsStart() {
		return this.ebenenArrayFuerIterationsStart;
	}

	private long[] createEbenenArrayFuerIterationsStart() {
		//bei Regeln kann in der alten Welt die Iteration für
		//lfd Nr bzw. Zeile eingeschränkt sein.
		long[] result = new long[getSizeOfIterationsArray()];
		Arrays.fill(result, 1L);
		result[0] = getUStart();
		result[1] = getLfdNrIterationsStart();
		result[result.length - 1] = getZeilenIterationsStart();

		int parallelIterationLevel = AnalyseService.getParallelIterationLevel(ruleInfo.getCondition(), ruleInfo.getErrorEntity(),
				ec);
		if (parallelIterationLevel >= 0) {
			// +1 wegen USB
			result[parallelIterationLevel + 1] = IParseTreeEntity.PARALLEL_ITERATION;
		}

		return result;
	}

	private long getUStart() {
		return EntityUtils.hasMaxUsb(ruleInfo.getRule().getErrorEntity()) ? 1 : IParseTreeEntity.UNSPECIFIED;
	}

	private int getSizeOfIterationsArray() {
		return isA12Model()
				// [USB (always -2), index of root group, index of group below root group, ..., index of field (always 1)]
				? EntityUtils.countHierarchyLevels(ruleInfo.getRule().getErrorEntity().getFullName()) + 1
				// always 3 [USB, lfdNr, Zeile]
				: 3;
	}

	/**
	 * Falls es sich um ein RegelMuster handelt, wir der Start des Musters zurückgegeben. Wenn es
	 * sich nicht um ein Regelmuster handelt, wird die Bedingung untersucht und das Minimum aller
	 * LfdNummern die im Fehlerfeld referenziert werden genommen.
	 *
	 * @return die laufende Nummer, die für das Fehlerfeld gesetzt wird.
	 */
	private long getLfdNrIterationsStart() {

		if (isLIteration()) {
			return IndexHelper.getStartIndex(getLfdNrIterationInterval());
		}
		return CodeGenHelper.getFehlerFeldLfdNrForCodeGen(ruleInfo.getRule().getErrorEntity().getFullName(),
				ruleInfo.getCondition());
	}

	/**
	 * In der Methode muss berücksichtigt werden, dass die maximale laufende Nummer eines Vordruckes
	 * durch eine Regel der Form
	 * <code>AnzahlAngegebenerVordruck(vordruckName) größer als eine Zahl</code> für eine Anwendung
	 * eingeschränkt ist. Als Ende der Iteration muss dann das Minimum verwendet werden aus der
	 * Angabe in den Basisdaten und dieser, durch eine Regel gesetzten maximalen laufenden Nummer
	 * Vordruck.
	 *
	 * @return Ende der Iteration für die LfdNummer als Long zurück.
	 */
	private long getLfdNrIterationsEnde() {
		if (!isLIteration()) {
			//der Wert kann größer 1 sein, wenn in der Bedingung nur konkrete LfdNummern > 1 referenziert wurden
			return getLfdNrIterationsStart();
		}

		long maxLfdNr = ruleInfo.getRule().getErrorEntity().getMax()[0];
		long iterationsEnde = IndexHelper.getEndeIndex(getLfdNrIterationInterval(), maxLfdNr);

		return Math.min(maxLfdNr, iterationsEnde);
	}

	private boolean isLIteration() {
		return AnalyseService.referenziertIteration(ruleInfo.getCondition(), IndexArt.LFDNUMMER);
	}

	private boolean isZIteration() {
		return AnalyseService.referenziertIteration(ruleInfo.getCondition(), IndexArt.ZEILE);
	}

	private Optional<IterationInterval> getLfdNrIterationInterval() {
		if (ruleInfo.getRule() instanceof IExtmpRule) {
			return ((IExtmpRule) ruleInfo.getRule()).getRootgroupIterationRestriction();
		}
		return Optional.empty();
	}

	private Optional<IterationInterval> getRowStartAndEnd() {
		if (ruleInfo.getRule() instanceof IExtmpRule) {
			return ((IExtmpRule) ruleInfo.getRule()).getRowIterationRestriction();
		}
		return Optional.empty();
	}

	/**
	 * Methode gibt den Zeilenindex des Fehlerfeldes zurück. Der Zeilenindex ist die kleinste Zeile,
	 * der kleinsten laufenden Nummer.
	 *
	 * @return Zeilenindex für die Codegenerierung
	 */
	private long getZeilenIterationsStart() {
		if (isZIteration()) {
			return IndexHelper.getStartIndex(getRowStartAndEnd());
		}
		String fehlerFeld = ruleInfo.getRule().getErrorEntity().getFullName();
		long lfdNr = isLIteration() ?
				IParseTreeEntity.ITERATION :
				CodeGenHelper.getFehlerFeldLfdNrForCodeGen(fehlerFeld, ruleInfo.getCondition());
		return CodeGenHelper.getFehlerFeldZeileForCodeGen(fehlerFeld, ruleInfo.getCondition(), lfdNr);
	}

	/**
	 * In der Methode muss berücksichtigt werden, dass die maximale Zeile eines Felder
	 * durch eine Regel der Form
	 * <code>MaximalAngegebenerZeilenIndex(feldName.*) größer als eine Zahl</code> für eine
	 * Anwendung
	 * eingeschränkt ist. Als Ende der Iteration muss dann das Minimum verwendet werden aus der
	 * Angabe in den Basisdaten und dieser, durch eine Regel gesetzten maximalen Zeile.
	 *
	 * @return Ende der Iteration für die LfdNummer als Long zurück.
	 */
	private long getZeilenIterationsEnde() {
		if (!isZIteration()) {
			//der Wert kann größer als 1 sein, wenn in der Bedingung nur Felder mit konkreten Zeilen > 1 referenziert werden
			return getZeilenIterationsStart();
		}

		long maxZeile = ruleInfo.getRule().getErrorEntity().isGroup() ?
				1 :
				EntityUtils.getMaxZeile((IField) ruleInfo.getRule().getErrorEntity());
		long iterationsEnde = IndexHelper.getEndeIndex(getRowStartAndEnd(), maxZeile);
		return Math.min(iterationsEnde, maxZeile);
	}
}
