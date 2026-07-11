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
package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BooleanParameter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.INumberType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;

/**
 * Diese Klasse enthält die notwendigen Informationen zum Generieren eines Zahl-Feldes.
 * <p>
 * Alle Methoden mit der Namenskonvention get....Code werden von dem CodeTemplates aus aufgerufen
 * und dienen der Codeerzeugung.
 * <p>
 * Achtung: die Verwendungen dieser Methoden sind somit nicht werkzeuggestützt durch "References"
 * Aufrufe nachvollziehbar. Die Stringtemplate Engine verlangt die Verwendung von public getter
 * Methode. D.h. diese Methode nicht einfach umbenennen und auch nicht löschen, weil ja scheinbar
 * nicht verwendet !
 */
public class CodeGenNumberTypeImpl extends CodeGenFeldTypBasis {

	private final boolean hatVorzeichen;
	private int vorkomma;
	private int nachkomma;
	private int nachkommaBis;
	private int maxLaenge;
	// maxLaengeDisp = der im ExtMP Editor angezeigte Wert für maxLaenge
	private final int maxLaengeDisp;
	private int minLaenge;
	// minLaengeDisp = der im ExtMP Editor angezeigte Wert für minLaenge
	private final int minLaengeDisp;
	private boolean nullErlaubt;
	private boolean fuehrendeNullenErlaubt;
	private final Optional<Double> minWert;
	private final Optional<Double> maxWert;

	private final long maxStellen;
	private Locale aktuelleSprache;
	private boolean isAmountOfMoney;

	public CodeGenNumberTypeImpl(IFieldType baseType) {
		INumberType numberType = (INumberType) baseType;

		this.maxStellen = IProjectSpecificSettings.MAXDIGITS_IN_NUMBERS;

		hatVorzeichen = numberType.hasSign();

		vorkomma = numberType.getMaxNumberOfDigitsBeforeDecimalPlace().orElse(-1);
		nachkomma = numberType.getMinFractionalDigits();
		nachkommaBis = numberType.getMaxFractionalDigits();

		minLaenge = numberType.getMinLength().orElse(-1);
		maxLaenge = numberType.getMaxLength().orElse(-1);

		minLaengeDisp = getMinLaengePrintable();
		maxLaengeDisp = getMaxLaengePrintable();

		nullErlaubt = numberType.isZeroAllowed();
		fuehrendeNullenErlaubt = numberType.leadingZerosAllowed();

		minWert = numberType.getMinValue();
		maxWert = numberType.getMaxValue();

		isAmountOfMoney = numberType.isNumberTraitAmount();
	}

	public String getIsAmountOfMoney() {
		return new BooleanParameter(isAmountOfMoney).toCode();
	}

	public int getMinLaenge() {
		return minLaenge;
	}

	/**
	 * @return der im ExtMP Editor angezeigte Wert für maxLaenge
	 */
	public int getMaxLaengeDisp() {
		return maxLaengeDisp;
	}

	/**
	 * @return der im ExtMP Editor angezeigte Wert für minLaenge
	 */
	public int getMinLaengeDisp() {
		return minLaengeDisp;
	}

	public String getMinWert() {
		if (!minWert.isPresent()) {
			return "";
		} else {
			return convertToPrintable(minWert.get());
		}
	}

	public String getMaxWert() {
		if (!maxWert.isPresent()) {
			return "";
		} else {
			return convertToPrintable(maxWert.get());
		}
	}

	private String convertToPrintable(double d) {
		if (nachkomma == nachkommaBis && nachkomma > -1) {
			return String.format(Locale.US, "%." + nachkomma + "f", d);
		} else {
			return new BigDecimal(Double.toString(d)).stripTrailingZeros().toPlainString();
		}
	}

	/**
	 * Für die CodeErzeugung: wird zur Zeit nur in JAVA Skript verwendet. TODO:
	 * vordefinierte Strings auch in den anderen Sprachen bereits bei der
	 * Codegenerierung erzeugen und nicht erst zur Laufzeit.
	 *
	 * @return String mit soviel Nullen wie nachkommastellen definiert sind,
	 */
	public String getNullenString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < nachkomma; i++) {
			builder.append("0");
		}
		return builder.toString();
	}

	/**
	 * Für die CodeErzeugung: wird zur Zeit nur in JAVA Skript verwendet. TODO:
	 * vordefinierte Strings auch in den anderen Sprachen bereits bei der
	 * Codegenerierung erzeugen und nicht erst zur Laufzeit.
	 *
	 * @return regulärer Ausdruck mit dem die Anzahl der Vorkommastellen geprüft
	 * 		werden kann.
	 */
	public String getVKPattern() {
		StringBuilder builder = new StringBuilder();
		if (vorkomma > 0) {
			builder.append("[+|-]?[0-9]{1,");
			builder.append(vorkomma);
			builder.append("}");
		}
		return builder.toString();
	}

	public void setNachkomma(int nachkomma) {
		this.nachkomma = nachkomma;
	}

	/**
	 * Für die CodeErzeugung: wird zur Zeit nur in JavaScript verwendet. In
	 * JavaScript wird für jede Sprache ein fester Dezimaltrenner verwendet.
	 *
	 * @return regulärer Ausdruck mit dem die Anzahl der Nachkommastellen
	 * 		geprüft werden kann.
	 */
	public String getNKPattern() {
		char dezimalTrenner = JavaScriptLanguageHelper.getJsDezimalTrenner(aktuelleSprache);
		StringBuilder buf = new StringBuilder();

		if (nachkomma > 0 && nachkommaBis == -1) {
			buf.append((dezimalTrenner == ',') ? "[-]?[0-9]+,[0-9]{" : "[-]?[0-9]+\\.[0-9]{");
			buf.append(nachkomma);
			buf.append("}");
		}

		if (nachkommaBis > 0 && nachkomma == 0) {
			buf.append((dezimalTrenner == ',') ? "[-]?[0-9]+(,[0-9]{" : "[-]?[0-9]+(\\.[0-9]{");
			buf.append(0);
			buf.append(",");
			buf.append(nachkommaBis);
			buf.append("})?");
		}

		if (nachkommaBis > 0 && nachkomma > 0) {
			buf.append((dezimalTrenner == ',') ? "[-]?[0-9]+,[0-9]{" : "[-]?[0-9]+\\.[0-9]{");
			buf.append(nachkomma);
			buf.append(",");
			buf.append(nachkommaBis);
			buf.append("}");
		}

		return buf.toString();
	}

	public int getVorkomma() {
		return vorkomma;
	}

	public int getNachkomma() {
		return nachkomma;
	}

	public int getNachkommaBis() {
		return nachkommaBis;
	}

	public int getMaxLaenge() {
		return maxLaenge;
	}

	public boolean isNullErlaubt() {
		return nullErlaubt;
	}

	public boolean isFuehrendeNullenErlaubt() {
		return fuehrendeNullenErlaubt;
	}

	public boolean getHatVorzeichen() {
		return hatVorzeichen;
	}

	public String getVorzeichenCode() {
		BooleanParameter p = new BooleanParameter(hatVorzeichen);
		return p.toCode();
	}

	public Integer getMaxLaengePrintable() {
		if (maxLaenge > 0) {
			return maxLaenge;
		}
		/*
		 * max. Länge nicht explizit spezifiziert -> berechne sie aus den anderen Attributen
		 * min. und max. Wert werden dabei nicht berücksichtigt, vgl. EXT-10057
		 */
		int maxNachkomma = berechneMaxNachkomma();
		boolean vorzeichenErlaubt = hatVorzeichen;
		int result = 0;
		if (vorkomma > 0) {
			result = vorkomma;
			if (maxNachkomma > 0) {
				result += 1 + maxNachkomma;
			}
		} else {
			result = (int) maxStellen;
			if (maxNachkomma > 0) {//Nachkomma erlaubt -> 1 Stelle für den Dezimaltrenner
				result++;
			}
		}
		if (vorzeichenErlaubt) {
			result++;
		}
		return result;
	}

	public Integer getMinLaengePrintable() {
		if (getMinLaenge() > 0) {
			return getMinLaenge();
		}
		/*
		 * min. Länge nicht explizit spezifiziert -> berechne sie aus den anderen Attributen
		 * min. und max. Wert werden dabei nicht berücksichtigt, vgl. EXT-10057
		 */
		int cMinLength = 1;
		if (getNachkomma() > 0) {
			cMinLength += 1 + getNachkomma();
		}
		return cMinLength;
	}

	/**
	 * Die maximal erlaubte Anzahl der Nachkommastellen berechnet sich aus den Attributen Nachkomma
	 * und NachkommaBis: es ist das max. der beiden Werte<br>
	 * Eines der beiden Attribute muss immer angegeben, insb. größer als -1 sein - vgl. EXT-5094
	 */
	private int berechneMaxNachkomma() {
		return Math.max(nachkommaBis, nachkomma);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (fuehrendeNullenErlaubt ? 1231 : 1237);
		result = prime * result + (hatVorzeichen ? 1231 : 1237);
		result = prime * result + maxLaenge;
		result = prime * result + maxLaengeDisp;
		result = prime * result + maxWert.hashCode();
		result = prime * result + minLaenge;
		result = prime * result + minLaengeDisp;
		result = prime * result + minWert.hashCode();
		result = prime * result + nachkomma;
		result = prime * result + nachkommaBis;
		result = prime * result + (nullErlaubt ? 1231 : 1237);
		result = prime * result + vorkomma;
		result = prime * result + (isAmountOfMoney ? 1231 : 1237);
		return result;
	}

	/**
	 * evaluiert, ob zwei Feldschlüssel gleich sind.
	 *
	 * @return wahr, falls beide Felschlüssel gleich sind, false sonst.
	 */
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

		CodeGenNumberTypeImpl o = (CodeGenNumberTypeImpl) other;

		return (hatVorzeichen == o.hatVorzeichen && vorkomma == o.vorkomma
				&& nachkomma == o.nachkomma && nachkommaBis == o.nachkommaBis
				&& nullErlaubt == o.nullErlaubt
				&& fuehrendeNullenErlaubt == o.fuehrendeNullenErlaubt
				&& minLaenge == o.minLaenge && maxLaenge == o.maxLaenge
				&& minLaengeDisp == o.minLaengeDisp && maxLaengeDisp == o.maxLaengeDisp
				&& minWert.equals(o.minWert)
				&& maxWert.equals(o.maxWert))
				&& isAmountOfMoney == o.isAmountOfMoney;
	}

	public void setSprache(Locale aktuelleSprache) {
		this.aktuelleSprache = aktuelleSprache;
	}

	public String getClassnameSuffix() {
		return "Zahl";
	}

	public boolean isAmountOfMoney() {
		return isAmountOfMoney;
	}

	public boolean isAmountOfMoneyWithCents() {
		return isAmountOfMoney && getNachkomma() > 0;
	}

	public boolean isAmountOfMoneyWithoutCents() {
		return isAmountOfMoney && getNachkomma() == 0;
	}
}
