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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.meta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.ArrayUtils;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * Klasse dient der Repräsentation eines Identifiers für die Metadatenschnittstelle.
 */
public class MetaIdentifier {

	private String name;
	private long[] indices;
	private long u;

	private MetaIdentifier(String name, long[] idx, long u) {
		this.name = name;
		this.indices = idx;
		this.u = u;
	}

	public int hashCode() {
		long result = (37 * name.toUpperCase().hashCode() + Arrays.hashCode(indices))
				* 37 + (u ^ (u >>> 32));
		return (int) result;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (other.getClass() != getClass()) {
			return false;
		}

		MetaIdentifier o = (MetaIdentifier) other;
		return (name.equals(o.name)
				&& Arrays.equals(indices, o.indices)
				&& u == o.u);
	}

	/**
	 * Stringdarstellung analog zu der in parse erwarteten Beschreibung
	 * &lt;name&gt;[u][index1,index2,..index3], wobei [u] optional ist.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(name);
		if (u != IIdentifier.ALL_INDICES) {
			sb.append(addBrackets(u));
		}
		sb.append(addBrackets(Arrays.toString(indices)));
		return sb.toString();
	}

	/**
	 * Erzeugt einen {@link MetaIdentifier} aus einer StringDarstellung der Form
	 * &lt;name&gt;[u][index1,index2,..index3], wobei [u] optional ist.
	 */
	public static MetaIdentifier parse(String beschreibung) {
		String[] teile = beschreibung.split("\\[");
		assert (teile.length == 2 || teile.length == 3);
		String index = removeTrailingBracket(teile[teile.length - 1]);
		long u = (teile.length == 2) ? 1 : Long.parseLong(removeTrailingBracket(teile[1]));
		return new MetaIdentifier(teile[0], ArrayUtils.erzeugeLongArray(index), u);
	}

	/**
	 * Gebe den String ohne die hintere eckige Klammer zurück. Die vordere eckige
	 * Klammer ist bereits durch den split-Befehl entfernt worden.
	 */
	private static String removeTrailingBracket(String s) {
		return s.substring(0, s.length() - 1);
	}

	/**
	 * Füge eckige Klammern hinzu für die String-Darstellung
	 */
	private static String addBrackets(long i) {
		return "[" + i + "]";
	}

	/**
	 * Füge eckige Klammern hinzu für die String-Darstellung
	 */
	private static String addBrackets(String s) {
		return "[" + s + "]";
	}

	/**
	 * Erzeugt einen neuen Identifier mit dem Namen dieses MetaIdentifiers.
	 * Alle Ebenen, die eine Iteration spezifizieren werden mit den konkreten Werten
	 * des übergebenen index initialisiert.
	 */
	RtInternalIdentifier createIdentifier(long[] indexArray, long usb) {
		long[] idx = indices.clone();
		RtInternalIdentifier result = new RtInternalIdentifier(name, idx);

		for (int i : getEbenenMitIteration()) {
			if (i < indexArray.length) {
				idx[i] = indexArray[i];
			}
		}
		if (u == IIdentifier.ITERATION &&
				usb != IIdentifier.USB_NOT_SPECIFIED && usb != IIdentifier.ALL_INDICES) {
			result.setUsb(usb);
		}
		return result;
	}

	/**
	 * Liefert die Menge aller Wiederholungsebenen, für die dieser {@link MetaIdentifier}
	 * Iteration spezifiziert
	 */
	Set<Integer> getEbenenMitIteration() {
		Set<Integer> result = new HashSet<>();
		for (int i = 0; i < indices.length; i++) {
			if (isIteration(i)) {
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * Gibt true zurück, wenn die Ebene i eine Iteration spezifiziert
	 */
	private boolean isIteration(int i) {
		return indices[i] == IIdentifier.ITERATION;
	}
}
