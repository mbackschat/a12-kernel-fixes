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
package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.verifier;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeConversionResult;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeConversionResultImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberCheckDigitDefinition;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberFormatting;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl.DefdataStnrAccessHelper;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl.GermanTaxNumberDefiningData;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.utils.PruefzifferVerfahren;

import lombok.NonNull;

/**
 * Shared validation and conversion logic for Steuernummer and Bundessteuernummer verification.
 * This class is parameterized by a regex pattern and does not depend on IFadat directly.
 * Instead, BUFA validation is provided as an {@link IntPredicate} by the caller.
 */
final class SteuernummerValidationHelper {

	// Formale Prüfung: 13-stellige Zahl, wobei die 5. Stelle den Wert 0 hat.
	static final Pattern VALID_STNR = Pattern.compile("([0-9]{2})([0-9]{2})0[0-9]{8}");

	private static final int STELLEN_BUFANR = 4;
	private static final String TRENNZEICHEN_STNR = "@";

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static final String CONVERSION_ERROR_IDENTIFIER = "CONVERSION_ERROR";

	private final Pattern validPattern;

	/*
	 * Key kann hier nicht die Bundeslandnummer sein (obwohl das Pattern bundeslandspezifisch ist),
	 * denn die Daten, die das Pattern bestimmen, werden in der Schnittstelle
	 * {@link IDefdatenSteuernummer} übergeben. Diese Daten sind im Portal reloadable, können sich
	 * somit zur Laufzeit ändern.
	 */
	private final Map<String, Pattern> mapPatternForOflToXml = new ConcurrentHashMap<>();

	SteuernummerValidationHelper(Pattern validPattern) {
		this.validPattern = validPattern;
	}

	// =========================== Validation ==========================

	Matcher getMatcher(final String xmlSteuernummer) {
		return validPattern.matcher(xmlSteuernummer);
	}

	/**
	 * Prüft die angegebene Steuernummer und liefert den Key der passenden Fehlermeldung
	 * bzw. {@link CustomFieldTypeErrorEnum#KEIN_FEHLER}, wenn die Steuernummer gültig ist.
	 *
	 * @param steuernummer
	 * 		the tax number to check
	 * @param bufaValidator
	 * 		predicate to validate the federal tax office number,
	 * 		or {@code null} to skip BUFA validation
	 */
	CustomFieldTypeErrorEnum checkSteuernummer(@NonNull String steuernummer, @NonNull IntPredicate bufaValidator) {
		Matcher matcher = getMatcher(steuernummer);
		if (!matcher.matches()) {
			return CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER;
		}
		String bundeslandnummerStr = matcher.group(1);
		int bundesfinanzamtsnummer = Integer.parseInt(bundeslandnummerStr.concat(matcher.group(2)));
		if (!bufaValidator.test(bundesfinanzamtsnummer)) {
			return CustomFieldTypeErrorEnum.STNR_FINANZAMT_FEHLER;
		}
		// Steuernummer in die verschiedenen Teile zerstückeln.
		int bundeslandnummer = Integer.parseInt(bundeslandnummerStr);
		int idxBezirkEnd = DefdataStnrAccessHelper.getIdxBezirkEnd(GermanTaxNumberDefiningData.getInstance(), bundeslandnummer);
		int bezirksnummer = Integer.parseInt(steuernummer.substring(
				DefdataStnrAccessHelper.BEZIRKSNUMMER_BEGIN_IDX, idxBezirkEnd));
		if (bezirksnummer == 0) {
			return CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER_BEZIRK;
		}
		// Nach der formalen Prüfung ist muss die Steuernummer 13 Stellen besitzen.
		int pruefziffer = Integer.parseInt(steuernummer.substring(12));

		if (anyNegativePatternValid(steuernummer, bundeslandnummer, GermanTaxNumberDefiningData.getInstance())) {
			return CustomFieldTypeErrorEnum.STNR_FORMALER_NEGATIVMUSTER;
		}

		IGermanTaxNumberCheckDigitDefinition verfahrenDef = DefdataStnrAccessHelper.getPruefziffernverfahrenDefinition(
				GermanTaxNumberDefiningData.getInstance(), bundeslandnummer, bundesfinanzamtsnummer, bezirksnummer);

		if (!istPruefzifferRichtig(steuernummer, pruefziffer, verfahrenDef)) {
			return CustomFieldTypeErrorEnum.STNR_PRUEFZIFFER_FEHLER;
		}

		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Prüft die angegebene EWAz (eine Steuernummer für die Nordländer SH, HB, HH und BE) und
	 * liefert den Key der passenden Fehlermeldung bzw. {@link CustomFieldTypeErrorEnum#KEIN_FEHLER},
	 * wenn die Steuernummer gültig ist.
	 * Die Bundesfinanzamtsnummer im Einheitswert-Aktenzeichens wurde bereits geprüft.
	 */
	CustomFieldTypeErrorEnum checkEWAzAlsSteuernummer(@NonNull String steuernummer) {
		return checkSteuernummer(steuernummer, bufaNr -> true);
	}

	/**
	 * Prüft, ob die Steuernummer zu Negativmustern passt.
	 *
	 * @return <code>false</code> falls kein Negative-Muster passt, ansonsten <code>true</code>
	 */
	boolean anyNegativePatternValid(final String steuernummer, final int bundeslandnummer, IGermanTaxNumberDefiningData defdaten) {
		Set<String> negativMusterSet = defdaten.getNegativePattern(bundeslandnummer);
		if (negativMusterSet != null) {
			Iterator<String> it = negativMusterSet.iterator();
			while (it.hasNext()) {
				String negativMuster = it.next();
				try {
					Pattern p = Pattern.compile(negativMuster);
					if (p.matcher(steuernummer).matches()) {
						return true;
					}
				} catch (Exception e) {
					// Integrationsproblem
					throw new IllegalArgumentException("Fehler in den defininierenden Daten. " +
							"Das Negativmuster \"" + negativMuster + "\" besitzt eine ungültige Syntax.", e);
				}
			}
		}
		return false;
	}

	boolean istPruefzifferRichtig(final String steuernummer, final int pruefziffer,
			final IGermanTaxNumberCheckDigitDefinition verfahrenDef) {
		int[] faktoren = DefdataStnrAccessHelper.getFaktoren(verfahrenDef);
		String bezeichnung = verfahrenDef.getDescription();
		if ("11er-Verfahren".equals(bezeichnung)) {
			return PruefzifferVerfahren.pruefeMitElferVerfahren(steuernummer, pruefziffer, faktoren);
		} else if ("2er-Verfahren".equals(bezeichnung)) {
			int[] summanden = DefdataStnrAccessHelper.getSummanden(verfahrenDef);
			return PruefzifferVerfahren.pruefeMitZweierVerfahren(steuernummer, pruefziffer, faktoren, summanden);
		} else if ("modifiziertes 11er-Verfahren".equals(bezeichnung)) {
			return pruefeMitModElferVerfahren(steuernummer, pruefziffer, faktoren);
		} else if ("11er-Verfahren-NRW".equals(bezeichnung)) {
			return pruefeMitElferVerfahrenNRW(steuernummer, pruefziffer, faktoren);
		} else {
			// Integrationsproblem
			throw new IllegalArgumentException("Fehler in den defininierenden Daten. " +
					"Es wird kein Prüfziffernverfahren mit der Bezeichnung \"" + bezeichnung + "\" unterstützt.");
		}
	}

	private boolean pruefeMitElferVerfahrenNRW(final String steuernummer, final int pruefziffer,
			final int[] faktoren) {
		int sum = 0;
		for (int i = 0; i < faktoren.length; i++) {
			sum += (faktoren[i] * Integer.parseInt(String.valueOf(steuernummer.charAt(i))));
		}
		double lower = Math.floor(sum / 11d) * 11;
		return valuesAreEqual((sum - lower), pruefziffer);
	}

	boolean pruefeMitModElferVerfahren(final String steuernummer, final int pruefziffer,
			final int[] faktoren) {
		int sum = 0;
		for (int i = 0; i < faktoren.length; i++) {
			int produkt = (faktoren[i] * Integer.parseInt(String.valueOf(steuernummer.charAt(i))));
			String s = String.valueOf(produkt);
			int produktEinstellig = 0;
			if (s.length() == 1) {
				produktEinstellig = produkt;
			} else if (s.length() == 2) {
				produktEinstellig = Integer.parseInt(s.substring(s.length() - 1)) + 1;
			} else {
				// Integrationsproblem
				throw new IllegalArgumentException(
						"Fehler in den defininierenden Daten. Im modifizierten 11er Verfahren für die Steuernummer " +
								steuernummer + " hat das Produkt aus der " + i + ".ten Stelle der Steuernummer " +
								"und dem Faktor " + faktoren[i] + " nicht wie erwartet ein oder zwei Stellen.");
			}
			if (String.valueOf(produktEinstellig).length() != 1) {
				// Integrationsproblem
				throw new IllegalArgumentException(
						"Fehler in den defininierenden Daten. Im modifizierten 11er Verfahren für die Steuernummer " +
								steuernummer + " ist das einstellige Produkt \"" + produktEinstellig + "\" aus der " + i +
								".ten Stelle der Steuernummer und dem Faktor " + faktoren[i] +
								" nicht - wie erwartet - einstellig.");
			}
			sum += produktEinstellig;
		}
		double upper = Math.ceil(sum / 10d) * 10;
		return valuesAreEqual((upper - sum), pruefziffer);
	}

	private boolean valuesAreEqual(final double a, final int b) {
		return (Double.compare(a, b) == 0);
	}

	// =========================== Conversion ==========================

	/**
	 * Konvertiert die Steuernummer im XML-Format (gleichbedeutend mit dem
	 * ELSTER-Steuernummerformat) in die Oberflächendarstellung.
	 * Assumes the input has already been validated.
	 *
	 * @return Steuernummer in Oberflächendarstellung
	 */
	ICustomFieldTypeConversionResult convertValidInternal2Display(String xmlStNr) {
		Matcher matcher = getMatcher(xmlStNr);
		matcher.matches(); // Wir wissen schon, dass die Steuernummer gültig ist!!!
		int bundeslandNr = Integer.parseInt(matcher.group(1));
		IGermanTaxNumberFormatting formatierung = GermanTaxNumberDefiningData.getInstance().getFormatting(bundeslandNr);

		int idxStartBezirk = STELLEN_BUFANR + 1;
		int idxEndBezirk = idxStartBezirk + formatierung.getNumPositionsDistrict();
		int idxStartUNr = idxEndBezirk;
		int idxEndUNr = idxStartUNr + formatierung.getNumPositionsDifferentiationNumber();
		int idxStartPruefziffer = idxEndUNr;
		return new CustomFieldTypeConversionResultImpl(formatierung.getPrefix()
				+ xmlStNr.substring(STELLEN_BUFANR - formatierung.getNumPositionsTaxOffice(), STELLEN_BUFANR) // Finanzamtsnummer
				+ formatierung.getDelimiter1()
				+ xmlStNr.substring(idxStartBezirk, idxEndBezirk) // Bezirksnummer
				+ formatierung.getDelimiter2()
				+ xmlStNr.substring(idxStartUNr, idxEndUNr) // Unterscheidungsnummer
				+ formatierung.getDelimiter3()
				+ xmlStNr.substring(idxStartPruefziffer) // Prüfziffer
				+ TRENNZEICHEN_STNR
				+ bundeslandNr, null);
	}

	/**
	 * Konvertiert die Steuernummer in Oberflächendarstellung in das XML-Format (gleichbedeutend mit
	 * dem ELSTER-Steuernummerformat).
	 *
	 * @param oflStNrMitBuLaNr
	 * 		- der String enthält neben der Steuernummer in Oberflächendarstellung
	 * 		auch die
	 * 		Bundeslandnummer getrennt durch das Zeichen '@'.
	 */
	ICustomFieldTypeConversionResult convertDisplay2Internal(String oflStNrMitBuLaNr) {
		// Bundeslandnummer bestimmen
		if (!oflStNrMitBuLaNr.contains(TRENNZEICHEN_STNR)) {
			throw new IllegalArgumentException(
					"Der übergebene String \"" + oflStNrMitBuLaNr + "\" muss neben der Steuernummer in Oberflächendarstellung, " +
							"getrennt durch das Zeichen '@', auch die Bundeslandnummer enthalten.");
		}
		int idxTrz = oflStNrMitBuLaNr.indexOf(TRENNZEICHEN_STNR);
		if ((idxTrz + 1) >= oflStNrMitBuLaNr.length()) {
			return new CustomFieldTypeConversionResultImpl(oflStNrMitBuLaNr, CONVERSION_ERROR_IDENTIFIER);
		}
		String bundeslandStr = oflStNrMitBuLaNr.substring(idxTrz + 1);
		int bundeslandNr = -1;
		try {
			// Der Substring kann nicht null sein. Das wird bereits mit "if((idx+1) >= oflStNrMitBuLaNr.length())" abgefangen.
			bundeslandNr = Integer.parseInt(bundeslandStr);
		} catch (NumberFormatException e) {
			return new CustomFieldTypeConversionResultImpl(oflStNrMitBuLaNr, CONVERSION_ERROR_IDENTIFIER);
		}
		// Infos zur bundeslandspezifischen Formatierung bestimmen
		IGermanTaxNumberFormatting formatierung = null;
		try {
			formatierung = GermanTaxNumberDefiningData.getInstance().getFormatting(bundeslandNr);
		} catch (IllegalArgumentException e) {
			return new CustomFieldTypeConversionResultImpl(oflStNrMitBuLaNr, CONVERSION_ERROR_IDENTIFIER);
		}
		// Formale Prüfung der Oberflächendarstellung
		Pattern validOflFormat = getPatternToConvertOflToXml(formatierung);
		Matcher matcher = validOflFormat.matcher(oflStNrMitBuLaNr);
		if (!matcher.matches()) {
			return new CustomFieldTypeConversionResultImpl(oflStNrMitBuLaNr, CONVERSION_ERROR_IDENTIFIER);
		}

		String finanzamt = matcher.group(1);
		String bezirk = matcher.group(2);
		String unterscheidungsnr = matcher.group(3);
		String pruefziffer = matcher.group(4);

		String bufaNr = getBundesfinanzamtsnummer(bundeslandNr, finanzamt, formatierung.getNumPositionsTaxOffice());
		if (bufaNr.length() < 2 || !bufaNr.substring(0, 2).equals(bundeslandStr)) {
			return new CustomFieldTypeConversionResultImpl(oflStNrMitBuLaNr, CONVERSION_ERROR_IDENTIFIER);
		}
		return new CustomFieldTypeConversionResultImpl(bufaNr + "0" + bezirk + unterscheidungsnr + pruefziffer, null);
	}

	private Pattern getPatternToConvertOflToXml(IGermanTaxNumberFormatting formatierung) {
		String muster = formatierung.getPrefix() // Der Prefix sollte immer eine konkrete Ziffenrfolge sein und kann damit 1:1 eingefügt werden.
				+ "([0-9]{" + formatierung.getNumPositionsTaxOffice() + "})"
				+ formatierung.getDelimiter1()
				+ "([0-9]{" + formatierung.getNumPositionsDistrict() + "})"
				+ formatierung.getDelimiter2()
				+ "([0-9]{" + formatierung.getNumPositionsDifferentiationNumber() + "})"
				+ formatierung.getDelimiter3()
				+ "([0-9]{1})@([0-9]{2})";

		return getPatternToConvertOflToXml(muster);
	}

	Pattern getPatternToConvertOflToXml(String muster) {
		return mapPatternForOflToXml.computeIfAbsent(muster, Pattern::compile);
	}

	private String getBundesfinanzamtsnummer(Integer bundeslandnr, String finanzamtsnr, int stellenFinanzamt) {
		String strBulaNr = String.valueOf(bundeslandnr);
		return strBulaNr.substring(0, STELLEN_BUFANR - stellenFinanzamt) + finanzamtsnr;
	}
}
