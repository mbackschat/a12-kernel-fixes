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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.Feld_t;

/**
 * Hilfsklasse für alle Utility Methoden bezogen auf ein Feld.
 */
public class FeldUtil {

	private FeldUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Prüft, ob das Feld global ist.
	 * ACHTUNG: Die Überprüfung erfolgt in diesem Fall nicht über den Key FELD_GLOBAL,
	 * da in einigen Testfällen der additional key FELD_GLOBAL nicht angegeben wird.
	 *
	 * @param f
	 * 		-zu prüfendes Feld
	 * @return <code>true</code>, falls das Feld global ist
	 */
	public static boolean isGlobal(Feld_t f) {
		return f.getAdditionalValue("GlobalesFeld") != null && (boolean) f.getAdditionalValue("GlobalesFeld");
	}

	/**
	 * Liefert die Liste der Indices von id beginnend mit dem USB und ohne den Zeilenindex.
	 */
	public static List<Long> getIndicesMitUSBUndOhneZeile(RtInternalIdentifier id) {
		List<Long> indicesMitUSBOhneZeile = new ArrayList<>();
		indicesMitUSBOhneZeile.add(id.getUsb());
		long[] indices = id.getIndices();
		Collections.addAll(indicesMitUSBOhneZeile, Arrays.stream(indices, 0, indices.length - 1).boxed().toArray(Long[]::new));
		return indicesMitUSBOhneZeile;
	}

	/**
	 * Liefert das Index-Array des Identifiers inklusive USB, dieser ist an der 0-ten Position.
	 */
	public static long[] getIndicesMitUSB(RtInternalIdentifier id) {
		int laengeMitUSB = id.getIndices().length + 1;
		long[] indicesMitUSB = new long[laengeMitUSB];
		indicesMitUSB[0] = id.getUsb();
		for (int i = 1; i < laengeMitUSB; i++) {
			indicesMitUSB[i] = id.getIndices()[i - 1];
		}
		return indicesMitUSB;
	}

	/**
	 * Gib die Zeilenangabe des Identifiers zurück
	 * Eine Zeile ist immer die letzte Ebene
	 */
	public static long getZeile(RtInternalIdentifier id) {
		return id.getIndices()[id.getIndices().length - 1];
	}

	public static RtInternalIdentifier getAncestorForLevel(RtInternalIdentifier id, String fullName, int level, boolean isHModel) {
		String groupName = EntityUtil.getPathOfHierarchyLevel(fullName, level);
		long[] groupIndices;
		if (isHModel) {
			groupIndices = new long[1];
			groupIndices[0] = id.getIndices()[0];
		} else {
			groupIndices = new long[level + 1];
			System.arraycopy(id.getIndices(), 0, groupIndices, 0, level + 1);
		}
		RtInternalIdentifier groupId = new RtInternalIdentifier(groupName, groupIndices, id.getUsb());
		return groupId;
	}
}
