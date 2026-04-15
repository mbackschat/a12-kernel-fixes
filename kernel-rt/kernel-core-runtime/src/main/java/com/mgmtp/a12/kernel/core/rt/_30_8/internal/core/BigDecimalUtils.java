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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.CollectionWithCheapMergeCopy;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.VkBigDecimal;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.VkString;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtInternalIdentifier;

public class BigDecimalUtils {

	private BigDecimalUtils() {
	}

	/**
	 * Standard Angaben für eine BigDecimal Zahl.
	 */

	public static VkBigDecimal makeDecimal(long l) {
		return new VkBigDecimal(l);
	}

	/**
	 * see {@link #makeDecimal(String, CollectionWithCheapMergeCopy)}
	 */
	public static VkBigDecimal makeDecimal(String wert) {
		return makeDecimal(wert, CollectionWithCheapMergeCopy.empty());
	}

	/**
	 * Methode darf nur mit korrekten Zahlstrings (oder dem Leerstring) aufgerufen werden,
	 * sonst fliegt eine NumberFormatException.
	 */
	public static VkBigDecimal makeDecimal(String wert, CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		if (StringUtils.isEmpty(wert)) {
			return makeDecimal(0);
		}
		return new VkBigDecimal(wert.replace(",", "."), origins);
	}

	public static VkBigDecimal abrunden(VkBigDecimal wert, int anzStellen) {
		return runden(wert, anzStellen, RoundingMode.FLOOR);
	}

	public static VkBigDecimal aufrunden(VkBigDecimal wert, int anzStellen) {
		return runden(wert, anzStellen, RoundingMode.CEILING);
	}

	public static VkBigDecimal runden(VkBigDecimal wert, int anzStellen) {
		return runden(wert, anzStellen, RoundingMode.HALF_UP);
	}

	private static VkBigDecimal runden(VkBigDecimal d, int anzStellen, RoundingMode mode) {
		/*
		 * note: for internal calculations, we use an internal precision, s.
		 * VkBigDecimal#INTERNAL_PRECISION
		 * before rounding down to a number of decimal digits, make a rounding using the default
		 * scale
		 * in this way we avoid rounding errors like "Abrunden(1/3 + 2/3) = 0"
		 */
		VkBigDecimal valueWithDefaultScale = d.setScale(VkBigDecimal.DEFAULT_SCALE, RoundingMode.HALF_UP);
		return valueWithDefaultScale.setScale(anzStellen, mode);
	}

	// Kann nicht über single Feld abgehandelt werden, weil im falle von isEmpty
	// ein 0 zurückgegeben
	// wird und nicht ein unknown.
	static VkBigDecimal laenge(VkString wert) {
		boolean kannGroesserWerden = wert.isNichtAngegeben();
		boolean kannKleinerWerden = false;
		return new VkBigDecimal(wert.length(), wert.isNichtPruefungsrelevant(), kannGroesserWerden, kannKleinerWerden,
				wert.getOrigins());
	}

	static VkBigDecimal absOp(VkBigDecimal operator) {
		return operator.abs();
	}

	/**
	 * Erzeugt für einen {@link VkBigDecimal}-Wert einen Wert mit maximal maxPrecision Stellen.
	 * Dient zur Ausgabe eines fehlerhaften Werts.
	 *
	 * @param wert
	 * 		der Wert.
	 * @param maxPrecision
	 * 		die maximale Zahl von Stellen.
	 * @return eine Stringrepräsentation des Wertes.
	 */
	public static String toStringWertLaengenBeschraenkt(VkBigDecimal wert, int maxPrecision) {
		BigDecimal minScaleWert = wert.getBigDecimalValue().stripTrailingZeros();

		// Wenn keine maxPrecision spezifiziert, gib Wert bis zur maximalen Genauigkeit an
		if (maxPrecision == -1) {
			if (minScaleWert.scale() < VkBigDecimal.DEFAULT_SCALE) {
				return minScaleWert.toPlainString();
			} else {
				return minScaleWert.setScale(VkBigDecimal.DEFAULT_SCALE, RoundingMode.HALF_UP).toPlainString();
			}
		}

		if (minScaleWert.precision() <= maxPrecision) {
			// Wenn der Wert in die max. Größe passt -> so ausgeben.
			return minScaleWert.toPlainString();
		} else {
			// scale = maxPrecision - # Vorkommastellen, aber Vorkommastellen zu eliminieren bringt nichts => min. 0
			// Vorkommastellen berechnen sich aus Anzahl Stellen insgesamt minus Stellen rechts vom Dezimaltrenner
			int scale = Math.max(0, maxPrecision - (minScaleWert.precision() - minScaleWert.scale()));
			return minScaleWert.setScale(scale, RoundingMode.HALF_UP).toPlainString();
		}
	}
}
