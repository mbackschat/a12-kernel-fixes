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
package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hilfsklasse zur Optimierung der Kontextiteration.<br>
 * Die Optimierung, dass pro Wiederholungsebene bis zum maximalen Index iteriert wird, ist nicht
 * ausreichend, vgl. EXT-8419.<br>
 * Zu jeder "Iterationstiefe" werden alle Index-Arrays gespeichert, für die ein Feld
 * angegeben ist. Die Index-Arrays enthalten an erster Stelle immer den USB, passend zu den
 * Iterationsarrays im generierten Code.<br>
 * <b>Beispiel:</b><br>
 * Feld /A/k1/k2/F mit Indices [7,5,3,1] und usb = -2 (Wert der Konstante
 * IIdentifier#USB_NICHT_ANGEGEBEN) ist angegeben.<br>
 * Dann wird
 * <ul>
 * <li>für die Ebene 0 das Array [-2] gespeichert</li>
 * <li>für die Ebene 1 das Array [-2, 7]</li>
 * <li>für die Ebene 2 das Array [-2, 7, 5]</li>
 * <li>etc.</li>
 * </ul>
 */
public class GesetzteWiederholungen {
	/**
	 * Alle gesetzten Indices werden pro Ebene (= Verschachtelungstiefe) gespeichert,
	 * Position in der Liste entspricht der Verschachtelungstiefe.
	 */
	private List<Set<LongArray>> gesetzteIndicesProEbene = new ArrayList<>();

	private Set<Integer> addedSemanticIndices = new HashSet<>();

	/**
	 * Liefert <code>true</code>, wenn ein Feld für die durch das Index-Array gegebene
	 * Wiederholung angegeben ist. Das als Parameter übergebene Index-Array enthält an erster
	 * Stelle den USB, wie im generierten Code für die Regeliteration.
	 * <p>
	 * <b>Beispiel:</b> Wenn das Feld /A/k1/k2/F, mit indices=[7, 2, 3, 1] und usb=-2 angegeben ist,
	 * sind folgende Wiederholungen gesetzt:
	 * <ul>
	 * <li>[-2]</li>
	 * <li>[-2, 7]</li>
	 * <li>[-2, 7, 2]</li>
	 * <li>[-2, 7, 2, 3]</li>
	 * <li>[-2, 7, 2, 3, 1]</li>
	 * </ul>
	 */
	public boolean istWiederholungGesetzt(long[] indicesWithUsb) {
		if (gesetzteIndicesProEbene.size() < indicesWithUsb.length) {
			return false;
		}
		Set<LongArray> gesetzteIndices = gesetzteIndicesProEbene.get(indicesWithUsb.length - 1);
		return gesetzteIndices.contains(new LongArray(indicesWithUsb));
	}

	/**
	 * Checks whether there is any field specified in the repetition specified by the given indices.
	 * If a semantic index is used on any of the repetition levels, the repetitions for that layer
	 * are combined (see {@link #addSemanticIndexValues(int)} for an example). The combination is
	 * cached for further use.
	 *
	 * @param indices
	 * 		the indices to query.
	 * @param semanticIndexLevelWithoutUSB
	 * 		the semantic index level not respecting the additional USB. <code>-1</code> if no
	 * 		semantic index is used.
	 * @return
	 */
	public boolean istWiederholungGesetzt(long[] indices, int semanticIndexLevelWithoutUSB) {
		if (semanticIndexLevelWithoutUSB < 0) {
			return istWiederholungGesetzt(indices);
		}

		int semanticIndexLevel = semanticIndexLevelWithoutUSB + 1;

		long[] semIdxIndices = indices.clone();
		if (semanticIndexLevel >= 0 && semIdxIndices.length > semanticIndexLevel + 1) {
			semIdxIndices[semanticIndexLevel] = Constants.SEMANTIC_INDEX;
		}

		if (!addedSemanticIndices.contains(semanticIndexLevel)) {
			addSemanticIndexValues(semanticIndexLevel);
			addedSemanticIndices.add(semanticIndexLevel);
		}
		return istWiederholungGesetzt(semIdxIndices);
	}

	/**
	 * Add additional values to the indices merging all sub levels for one level for semantic
	 * indices.
	 *
	 * <b>Example:</b> If the field /A/k1/k2/F, with indices=[7, 2, 3, 1] and usb=-2 is given and
	 * the semanticIndexLevel is 2, then following repetitions are added (c.f.
	 * {@link #istWiederholungGesetzt(long[])}):
	 * <ul>
	 * <li>[-2, 7,-4, 3]</li>
	 * <li>[-2, 7,-4, 3, 1]</li>
	 * </ul>
	 *
	 * @param semanticIndexLevel
	 * 		the level, on which the semantic index occurs in the array.
	 */
	private void addSemanticIndexValues(int semanticIndexLevel) {
		for (int i = semanticIndexLevel; i < gesetzteIndicesProEbene.size(); i++) {
			Set<LongArray> indicesToAdd = new HashSet<>();
			for (LongArray indicesSet : gesetzteIndicesProEbene.get(i)) {
				long[] newIndices = indicesSet.getOriginClone();
				newIndices[semanticIndexLevel] = Constants.SEMANTIC_INDEX;
				indicesToAdd.add(new LongArray(newIndices));
			}
			gesetzteIndicesProEbene.get(i).addAll(indicesToAdd);
		}
	}

	public void update(RtInternalIdentifier id) {
		for (int i = 0; i <= id.getIndices().length; i++) {
			long[] ind = createSubArrayWithUsb(id, i + 1);

			if (gesetzteIndicesProEbene.size() <= i) {
				gesetzteIndicesProEbene.add(i, new HashSet<LongArray>());
			}
			gesetzteIndicesProEbene.get(i).add(new LongArray(ind));
		}
	}

	private long[] createSubArrayWithUsb(RtInternalIdentifier id, int laengeDesSubArrays) {
		long[] result = new long[laengeDesSubArrays];
		result[0] = id.getUsb();
		for (int j = 1; j < laengeDesSubArrays; j++) {
			result[j] = id.getIndices()[j - 1];
		}
		return result;

	}

}
