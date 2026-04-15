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
package com.mgmtp.a12.kernel.core.service.internal.mandatoryinformation;

import java.util.Arrays;
import java.util.Map;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FeldIdUtilities;

/**
 * Die Klasse wird bei der Pflichtfeldermittlung verwendet, um Felder in Additiv- und Pflichtmengen
 * in einer {@link PflichtInformation} darzustellen. <br>
 * Um auszudrücken, dass eine {@link PflichtInformation} für eine Wiederholungsebene für mehrere
 * Indices gültig ist (weil die Info aus einer Regel mit Iteration entstanden ist), wird die
 * Konstante {@link IParseTreeEntity#ITERATION} verwendet. <br>
 * Als Abkürzung für "alle Wiederholungen" (entsprechend dem Stern-Operator bzw. Mengenbildung in
 * der Sprache) wird die Konstante {@link IParseTreeEntity#WILDCARD} verwendet.
 */
public final class PflichtIdentifier {

	private String fullname;
	private long[] indices;

	private long usb = IParseTreeEntity.UNSPECIFIED;
	private String vordruckName;
	private long[] maxWerte;
	private long maxUsb;

	public PflichtIdentifier(IEntityId feld) {
		this.fullname = feld.getFullName();
		this.maxWerte = feld.getMaxValues();
		this.maxUsb = feld.getMaxUsb();
		this.indices = Arrays.copyOf(feld.getIndices(), feld.getIndices().length);
		usb = feld.getUsb();
		vordruckName = FeldIdUtilities.getVordruckName(feld);
	}

	public PflichtIdentifier(PflichtIdentifier pid) {
		this.fullname = pid.fullname;
		this.indices = pid.indices.clone();
		this.usb = pid.usb;
		this.vordruckName = pid.vordruckName;
		this.maxWerte = pid.maxWerte.clone();
		this.maxUsb = pid.maxUsb;
	}

	public String toString() {
		return createStringWithName(fullname);
	}

	/**
	 * Liefert die im generierten Meta-Code verwendete Stringdarstellung des Feldes:<br>
	 * uniqueName[u][e1,...,en], wobei [u] optional ist und nur verwendet wird, wenn das Feld auf
	 * einem USB-Vordruck liegt. Die Indices können neben konkreten Werten auch die Werte
	 * {@link IParseTreeEntity#WILDCARD} und {@link IParseTreeEntity#ITERATION} annehmen.
	 */
	public String toMetaString(Map<String, String> uniqueNames) {
		String uniqueName = uniqueNames.get(fullname);
		return createStringWithName(uniqueName);
	}

	private String createStringWithName(String name) {
		StringBuilder sb = new StringBuilder(name);
		if (usb != IParseTreeEntity.UNSPECIFIED) {
			sb.append("[" + usb + "]");
		}
		sb.append("[");
		for (int i = 0; i < indices.length - 1; i++) {
			sb.append(indices[i]);
			sb.append(",");
		}
		sb.append(indices[indices.length - 1]);
		sb.append("]");

		return sb.toString();
	}

	public int hashCode() {
		assert (fullname != null);
		return (37 * fullname.hashCode() + Arrays.hashCode(indices)) * 37 + +(int) (usb ^ (usb >>> 32));
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

		PflichtIdentifier o = (PflichtIdentifier) other;

		return (fullname.equals(o.fullname)
				&& Arrays.equals(o.indices, indices)
				&& this.usb == o.usb
				&& this.vordruckName.equals(o.vordruckName));
	}

	public String getFullName() {
		return fullname;
	}

	public long getLfdNummer() {
		return indices[0];
	}

	public String getVordruckName() {
		return vordruckName;
	}

	public long getMaxLfdNr() {
		return maxWerte[0];
	}

	public long getMaxZeile() {
		return maxWerte[maxWerte.length - 1];
	}
}
