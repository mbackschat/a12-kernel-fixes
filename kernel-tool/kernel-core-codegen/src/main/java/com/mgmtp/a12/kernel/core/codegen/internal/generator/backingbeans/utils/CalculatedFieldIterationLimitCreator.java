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

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;

/**
 * Klasse kapselt alle Methoden, um die Iterationsgrenzen eines berechneten Feldes zu ermitteln. Bei
 * Berechnungen gilt - im Unterschied zu den Regelbedingungen:
 * <ul>
 * <li>es kann immer optimiert werden</li>
 * <li>die Iteration kann nicht eingeschränkt werden</li>
 * </ul>
 * dadurch wird die Ermittlung der Optimierung vereinfacht - wenn eine Wiederholungsebene echt
 * wiederholbar ist, kann optimiert werden.
 */
public class CalculatedFieldIterationLimitCreator extends IterationLimitCreatorBase {

	IEntityId calculatedField;
	int parallelIterationLevel;

	public CalculatedFieldIterationLimitCreator(IEntityId calculatedField, CodeGenData data, int parallelIterationLevel) {
		super(data);
		this.calculatedField = calculatedField;
		this.parallelIterationLevel = parallelIterationLevel;
	}

	@Override
	public boolean[] getEbenenArrayFuerIterationsOptimierungsOption() {
		//Bei Berechnungen kann immer optimiert werden, da bereits
		//durch die Optimierbarkeit bereits durch die  kanonische Form sichergestellt ist
		//Um im Generierten Code das Abfragen im Falle von nicht-wiederholbaren Ebenen
		//zum vermeiden,  wird lediglich auf echte Wiederholbarkeit (d.h. max > 1 geprüft.
		boolean[] result = new boolean[getSizeOfIterationsArray()];

		if (isA12Model()) {
			long[] indices = getEbenenArrayFuerIterationsEnde();

			for (int i = 0; i < indices.length; i++) {
				result[i] = indices[i] > 1; // optimierbar, wenn wiederholbar
			}
		} else {
			result[0] = calculatedField.getMaxUsb() > 1;

			//bei Berechnungen ist sichergestellt,  dass optimiert werden kann
			long[] max = calculatedField.getMaxValues();
			result[1] = max[0] > 1;
			result[2] = max[max.length - 1] > 1;
		}

		return result;
	}

	@Override
	public long[] getEbenenArrayFuerIterationsEnde() {
		long[] result = new long[getSizeOfIterationsArray()];
		result[0] = calculatedField.getMaxUsb();
		long[] indices = calculatedField.getMaxValues();
		System.arraycopy(indices, 0, result, 1, indices.length);

		if (parallelIterationLevel >= 0) {
			result[parallelIterationLevel + 1] = IParseTreeEntity.PARALLEL_ITERATION;
		}

		return result;
	}

	@Override
	public long[] getEbenenArrayFuerIterationsStart() {
		long[] result = new long[getSizeOfIterationsArray()];
		Arrays.fill(result, 1L); // da für Berechnungen die Iteration nicht eingeschränkt sein darf, immer 1
		result[0] = isUsbMuster() ? 1 : IParseTreeEntity.UNSPECIFIED;

		if (parallelIterationLevel >= 0) {
			result[parallelIterationLevel + 1] = IParseTreeEntity.PARALLEL_ITERATION;
		}

		return result;
	}

	private boolean isUsbMuster() {
		return calculatedField.getMaxUsb() >= 0;
	}

	private int getSizeOfIterationsArray() {
		return isA12Model()
				// [USB (always -2), index of root group, index of group below root group, ..., index of field (always 1)]
				? EntityUtils.countHierarchyLevels(calculatedField.getFullName()) + 1
				// always 3 [USB, lfdNr, Zeile]
				: 3;
	}

}
