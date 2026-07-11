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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef;

/**
 * Represents meta information for fields referenced by rules.
 */
public class Identifier_t {
	public enum ReferenzTyp {
		OPERAND,
		OPERAND_UEBERTRAG,
		BERECHNETES_FELD,
		BERECHNETES_FELD_UEBERTRAG,
		KEINE_BERECHNUNG
	}

	public Identifier_t(int feldIndex, long[] indices, long usb, ReferenzTyp refTyp, String semanticIndexValue,
			Identifier_t semanticIndex, String fullName) {
		this.feldIndex = feldIndex;
		this.indices = indices;
		this.usb = usb;
		this.refTyp = refTyp;
		this.semanticIndexValue = semanticIndexValue;
		this.semanticIndex = semanticIndex;
		this.fullName = fullName;
	}

	public int getFeldIndex() {
		return feldIndex;
	}

	public long[] getIndices() {
		return indices;
	}

	public long getUsb() {
		return usb;
	}

	public boolean isBerechnetesFeldUebertrag() {
		return refTyp == ReferenzTyp.BERECHNETES_FELD_UEBERTRAG;
	}

	public boolean isBerechnetesFeld() {
		return refTyp == ReferenzTyp.BERECHNETES_FELD ||
				refTyp == ReferenzTyp.BERECHNETES_FELD_UEBERTRAG;
	}

	public boolean isOperand() {
		return refTyp == ReferenzTyp.OPERAND ||
				refTyp == ReferenzTyp.OPERAND_UEBERTRAG;
	}

	public boolean isOperandUebertrag() {
		return refTyp == ReferenzTyp.OPERAND_UEBERTRAG;
	}

	public String getSemanticIndexValue() {
		return semanticIndexValue;
	}

	public Identifier_t getSemanticIndex() {
		return semanticIndex;
	}

	public String getFullName() {
		return fullName;
	}

	public boolean isGroup() {
		return feldIndex == -1;
	}

	int feldIndex;
	long[] indices;
	long usb;
	ReferenzTyp refTyp;
	String semanticIndexValue;
	Identifier_t semanticIndex;
	String fullName;

}
