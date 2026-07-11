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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeConversionResult;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeConversionResultImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeValidationParamImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl.GermanTaxNumberDefiningData;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorKeys;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorUtil;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.utils.EWAzProperties;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.utils.PruefzifferVerfahren;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.utils.PruefzifferVerfahren.Verfahren;

import lombok.NonNull;

/**
 * Prüft das Einheitswert-Aktenzeichen.
 * Die Prüfung des Einheitswert-Aktenzeichen besteht aus den folgenden Teilen:
 * <ul>
 * <li>Ist das Einheitswert-Aktenzeichen formal gültig?</li>
 * <li>Existiert die in dem Einheitswert-Aktenzeichen enthaltene Bundesfinanzamtsnummer?</li>
 * <li>Liefert das Prüfziffernberechnungsverfahren die passende Prüfziffer?</li>
 * </ul>
 *
 * <p>
 * Bei der formalen Prüfung muss das Einheitswert-Aktenzeichen dem folgenden regulären Ausdruck
 * entsprechen:
 * <code>[0-9]{4}[045][0-9]{8,14}</code>
 * </p>
 *
 * <p>
 * Die ersten vier Zeichen des Einheitswert-Aktenzeichens bilden die Bundesfinanzamtsnummer. Es wird
 * geprüft, ob diese BUFA-Nr. laut der verbindlichen Liste im technischen Anforderungskonzept (V35,
 * Kapitel 1.2.2) zulässig ist.
 * </p>
 * <p>
 * An 5. Stelle steht der Formatschlüssel. Er hat beim Ordnungsbegriff "Einheitswertaktenzeichen"
 * der Grundsteuer in verschiedenen Ländern den Wert 0, 4, oder 5.
 *
 * <p>
 * Auch besondere Formatregeln für Schleswig-Holstein und Nordrhein-Westfalen werden geprüft.
 * </p>
 * <p>
 * Die Schnittstelle {@link IGermanTaxNumberDefiningData} wird vom aufrufenden System implementiert.
 *
 * <p>
 * Abschließend wird das Einheitswert-Aktenzeichen noch anhand eines
 * Prüfziffernberechnungsverfahrens geprüft.
 * </p>
 */
public class EWAzVerifierImpl implements ICustomFieldValidator {

	// Formale Prüfung: 12- bis 18-stellige Zahl.
	private static final Pattern VALID_EWAZ = Pattern.compile("([0-9]{2})([0-9]{2})([045])[0-9]{8,14}");
	private static final Pattern SPLIT_DISPLAY_VALUE = Pattern.compile("(.*)(@)([A-Z]{2})");
	private static final int STELLEN_BUFANR = 4;
	private static final Map<String, Integer> LAENDERKUERZEL_NUMMERN;

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static final String CONVERSION_ERROR_IDENTIFIER = "CONVERSION_ERROR";

	private static final String LAENDERKUERZEL_NW = "NW";
	private static final String LAENDERKUERZEL_SH = "SH";

	static {
		Map<String, Integer> laenderKuerzelNummer = new HashMap<>();
		laenderKuerzelNummer.put("BW", 28); // Baden-Württemberg
		laenderKuerzelNummer.put("BY", 9); // Bayern
		laenderKuerzelNummer.put("BE", 11); // Berlin
		laenderKuerzelNummer.put("BB", 30); // Brandenburg
		laenderKuerzelNummer.put("HB", 24); // Bremen
		laenderKuerzelNummer.put("HH", 22); // Hamburg
		laenderKuerzelNummer.put("HE", 26); // Hessen
		laenderKuerzelNummer.put("MV", 40); // Mecklenburg-Vorpommern
		laenderKuerzelNummer.put("NI", 23); // Niedersachsen
		laenderKuerzelNummer.put(LAENDERKUERZEL_NW, 5); // Nordrhein-Westfalen
		laenderKuerzelNummer.put("RP", 27); // Rheinland-Pfalz
		laenderKuerzelNummer.put("SL", 10); // Saarland
		laenderKuerzelNummer.put("SN", 32); // Sachsen
		laenderKuerzelNummer.put("ST", 31); // Sachsen-Anhalt
		laenderKuerzelNummer.put(LAENDERKUERZEL_SH, 21); // Schleswig-Holstein
		laenderKuerzelNummer.put("TH", 41); // Thüringen
		LAENDERKUERZEL_NUMMERN = Collections.unmodifiableMap(laenderKuerzelNummer);
	}

	private static String getLaenderKuerzel(Integer buLaNr) {
		return LAENDERKUERZEL_NUMMERN
				.entrySet()
				.stream()
				.filter(entry -> buLaNr.equals(entry.getValue()))
				.map(Map.Entry::getKey)
				.findFirst().orElseThrow(() -> new NoSuchElementException("No value present"));
	}

	private static int getLaenderNummer(String buLaString) {
		return LAENDERKUERZEL_NUMMERN.get(buLaString);
	}

	private final @NonNull IFadat fadat;

	public EWAzVerifierImpl(@NonNull IFadat fadat) {
		this.fadat = fadat;
	}

	/**
	 * Prüft das übergebene Einheitswert-Aktenzeichen.<br>
	 * Als zusätzliche Parameter werden die definierenden Daten der Steuernummer-Prüfung
	 * ({@link IGermanTaxNumberDefiningData}) und ein boolean benötigt. Die definierenden Daten der
	 * Steuernummer-Prüfung werden nicht dazu verwendet, um zu prüfen, ob die Bundesfinanzamtsnummer
	 * existiert - im technischen Anforderungskonzept (V35, Kapitel 1.2.2) steht eine verbindliche
	 * Liste der erlaubten BUFA-Nummern. Sie werden hier nur verwendet um zu prüfen, ob das vom
	 * Benutzer eingegebene Bundeslandkürzel gültig ist. Die definierenden Daten der
	 * Steuernummer-Prüfung werden außerdem an den SteuernummerVerifier weitergegeben falls es sich
	 * um eine ein Einheitswert-Aktenzeichen der sogenannten Nordländer (SH, HH, HB, BE) handelt.
	 * Der Wert des booleschen Parameters bestimmt, ob der Wert vor der Validierung ins XML-Format
	 * konvertiert werden muss.
	 * Die bundeslandspezifischen Eigenschaften eines Einheitswert-Aktenzeichen werden entweder hier
	 * oder bei der Konvertierung der Oberflächendarstellung ins XML-Format gesetzt.
	 * Werden die Parameter nicht oder falsche Parameter übergeben, wird eine
	 * {@link IllegalArgumentException} geworfen.
	 */
	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String value,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		Locale errorMsgLocale = valParam.getErrorMsgLocale();
		if (isDisplayValue) {// der Wert muss vor der Validierung in XML-Format konvertiert werden
			ICustomFieldTypeConversionResult convertResult = convertDisplay2Internal(value);

			if (convertResult.getErrorMessage().isPresent()) {// Fehler bei der Konvertierung
				return CustomFieldTypeErrorUtil.getResult(CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER, errorMsgLocale,
						CustomFieldTypeErrorKeys.UNGUELTIGES_EWAZ);
			}
			// nehmen wir die XML-Darstellung
			value = convertResult.getConvertedValue();
		}
		int buLaNummer = 0;
		try {
			buLaNummer = getBuLaNrFromString(value);
			getLaenderKuerzel(buLaNummer);
		} catch (Exception e) {
			return CustomFieldTypeErrorUtil.getResult(CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER, errorMsgLocale,
					CustomFieldTypeErrorKeys.UNGUELTIGES_EWAZ);
		}
		CustomFieldTypeErrorEnum error = checkEinheitswertAktenzeichen(value, buLaNummer);
		return CustomFieldTypeErrorUtil.getResult(error, errorMsgLocale,
				CustomFieldTypeErrorKeys.UNGUELTIGES_EWAZ);
	}

	/**
	 * Prüft die angegebene Einheitswert-Aktenzeichen und liefert den Key der passenden
	 * Fehlermeldung bzw. {@link CustomFieldTypeErrorEnum#KEIN_FEHLER}, wenn das
	 * Einheitswert-Aktenzeichen gültig ist.
	 *
	 * @param ewaz
	 * 		Einheitswert-Aktenzeichen
	 * @param buLaNummer
	 * 		Bundeslandnummer
	 */
	private CustomFieldTypeErrorEnum checkEinheitswertAktenzeichen(@NonNull String ewaz, int buLaNummer) {
		// Nach der formalen Prüfung muss das Einheitswert-Aktenzeichen je nach Bundesland 12-18 Stellen besitzen.
		Matcher matcher = VALID_EWAZ.matcher(ewaz);
		int stelligkeit = EWAzProperties.getStelligkeit(buLaNummer);
		if (!matcher.matches() || (ewaz.length() != stelligkeit + 1)) {
			return CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER;
		}
		if (buLaNummer == LAENDERKUERZEL_NUMMERN.get(LAENDERKUERZEL_SH) && !istSHFormatGueltig(ewaz)) {
			return CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER;
		}
		if (buLaNummer == LAENDERKUERZEL_NUMMERN.get(LAENDERKUERZEL_NW) && !istNWFormatGueltig(ewaz)) {
			return CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER;
		}
		String bundeslandnummerStr = matcher.group(1);
		int bundeslandnummer2st = Integer.parseInt(bundeslandnummerStr);
		int bundesfinanzamtsnummer = Integer.parseInt(bundeslandnummerStr.concat(matcher.group(2)));
		if (!EWAzProperties.getBundesfinanzamtsnummern(bundeslandnummer2st).contains(bundesfinanzamtsnummer) &&
				!fadat.getEWAzTestFederalTaxOfficeNumber().contains(bundesfinanzamtsnummer)) {
			return CustomFieldTypeErrorEnum.EWAZ_FINANZAMT_FEHLER;
		}
		String formatschluesselStr = matcher.group(3);
		int formatschluessel = Integer.parseInt(formatschluesselStr);
		if (EWAzProperties.getFormatschluessel(buLaNummer) != formatschluessel) {
			return CustomFieldTypeErrorEnum.EWAZ_FORMATSCHLUESSEL_FEHLER;
		}
		Verfahren verfahren = EWAzProperties.getVerfahrenDef(buLaNummer);
		if (verfahren == Verfahren.STNR) {
			SteuernummerValidationHelper stnrHelper = new SteuernummerValidationHelper(SteuernummerValidationHelper.VALID_STNR);
			if (stnrHelper.checkEWAzAlsSteuernummer(ewaz) != CustomFieldTypeErrorEnum.KEIN_FEHLER) {
				return CustomFieldTypeErrorEnum.EWAZ_PRUEFZIFFER_FEHLER;
			}
		} else {
			String ewazOhneFormatschluessel = ewaz.substring(0, 4) + ewaz.substring(5, stelligkeit + 1);
			if (!istPruefzifferRichtig(ewazOhneFormatschluessel, stelligkeit, verfahren)) {
				return CustomFieldTypeErrorEnum.EWAZ_PRUEFZIFFER_FEHLER;
			}
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Prüfe, dass beim Einheitswert-Aktenzeichen in Schleswig-Holstein nur die Bezirknummern
	 * 001-898
	 * [3.-5. (Ofl) bzw. 5.-7. Stelle (XML-Format)] zulässig sind.
	 *
	 * @param ewaz
	 * 		Einheitswert-Aktenzeichen
	 */
	private boolean istSHFormatGueltig(String ewaz) {
		int bezirksnummer = Integer.parseInt(ewaz.substring(5, 8));
		return bezirksnummer >= 1 && bezirksnummer <= 898;
	}

	/**
	 * Prüfe, dass beim Einheitswert-Aktenzeichen in Nordrhein-Westfalen an der 7. (Ofl), bzw. 8.
	 * Stelle (XML-Format) nur die Grundbesitzart 1 oder 3 angegeben werden kann.
	 *
	 * @param ewaz
	 * 		Einheitswert-Aktenzeichen
	 */
	private boolean istNWFormatGueltig(String ewaz) {
		String grundbesitzart = ewaz.substring(8, 9);
		return grundbesitzart.equals("1") || grundbesitzart.equals("3");
	}

	private boolean istPruefzifferRichtig(final String ewaz,
			int stelligkeit, final Verfahren verfahren) {
		int pruefziffer = Integer.parseInt(ewaz.substring(stelligkeit - 1));
		switch (verfahren) {
			case A1: {
				int[] faktoren = { 0, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };
				return pruefeMitA1Verfahren(ewaz, pruefziffer, faktoren);
			}
			case A2: {
				int[] faktoren = { 0, 0, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2 };
				return pruefeMitA2Verfahren(ewaz, pruefziffer, faktoren);
			}
			case A3: {
				int[] faktoren = { 0, 0, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7 };
				return pruefeMitA1Verfahren(ewaz, pruefziffer, faktoren);
			}
			case NRW: {
				int[] faktoren = { 0, 1, 2, 3, 4, 7, 8, 9, 2, 3, 4, 7, 8 };
				return pruefeMitA1Verfahren(ewaz, pruefziffer, faktoren);
			}
			default:
				// Integrationsproblem
				throw new IllegalArgumentException(
						"Es wird kein Prüfziffernverfahren mit der Bezeichnung \"" + verfahren + "\" unterstützt.");
		}
	}

	private boolean pruefeMitA1Verfahren(final String ewaz, final int pruefziffer, final int[] faktoren) {
		int sum = 0;
		for (int i = 0; i < faktoren.length; i++) {
			sum += (faktoren[i] * Integer.parseInt(String.valueOf(ewaz.charAt(i))));
		}
		return PruefzifferVerfahren.valuesAreEqual(PruefzifferVerfahren.einerstelle(sum), pruefziffer);
	}

	private boolean pruefeMitA2Verfahren(final String ewaz, final int pruefziffer, final int[] faktoren) {
		int sum = 0;
		for (int i = 0; i < faktoren.length; i++) {
			sum += PruefzifferVerfahren.quersumme(faktoren[i] * Integer.parseInt(String.valueOf(ewaz.charAt(i))));
		}
		return PruefzifferVerfahren.pruefePruefzifferAusDifferenz(sum, 10, pruefziffer);
	}

	/**
	 * Konvertiert das Einheitswert-Aktenzeichen im XML-Format in die Oberflächendarstellung.
	 *
	 * @return Einheitswert-Aktenzeichen in Oberflächendarstellung
	 */
	@Override
	public @NonNull ICustomFieldTypeConversionResult convertInternal2Display(@NonNull String xmlEwAz) {
		ICustomFieldTypeValidationParam valParam = new CustomFieldTypeValidationParamImpl(Locale.GERMANY,
				CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH, CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH);
		// Gültigkeitsprüfung und Setzen der bundeslandspezifischen Eigenschaften
		Optional<ICustomFieldTypeCheckError> checkResult = validate(xmlEwAz, valParam, false);
		if (checkResult.isPresent()) {
			return new CustomFieldTypeConversionResultImpl(xmlEwAz,
					"Konvertiere nur gültige Aktenzeichen. " + checkResult.get().getErrorMessage());
		}

		int bundeslandNr = getBuLaNrFromString(xmlEwAz);
		return new CustomFieldTypeConversionResultImpl(
				xmlEwAz.substring(STELLEN_BUFANR - EWAzProperties.getFANrStellen(bundeslandNr), STELLEN_BUFANR) // Finanzamtsnummer
						+ xmlEwAz.substring(STELLEN_BUFANR + 1,
						EWAzProperties.getStelligkeit(bundeslandNr) + 1) //EWAz ohne Finanzamtsnummer und Formatschlüssel
						+ "@" // Trennzeichen vor Bundeslandkürzel
						+ getLaenderKuerzel(bundeslandNr), null);
	}

	/**
	 * Konvertiert das Einheitswert-Aktenzeichen in Oberflächendarstellung in das XML-Format.
	 *
	 * @param ewAzDisplayValueWithLaenderkuerzel
	 * 		- der String enthält neben dem Einheitswert-Aktenzeichen in Oberflächendarstellung
	 * 		auch das Bundeslandkürzel getrennt durch das Zeichen '@'.
	 */
	@Override
	public @NonNull ICustomFieldTypeConversionResult convertDisplay2Internal(@NonNull String ewAzDisplayValueWithLaenderkuerzel) {
		Matcher mToSplitDisplayValue = SPLIT_DISPLAY_VALUE.matcher(ewAzDisplayValueWithLaenderkuerzel);
		if (!mToSplitDisplayValue.matches()) {
			throw new IllegalArgumentException(
					String.format("Das EW-Az in Oberflächendarstellung '%s' entspricht nicht dem geforderten Pattern '%s'.",
							ewAzDisplayValueWithLaenderkuerzel, SPLIT_DISPLAY_VALUE.pattern()));
		}
		String landerkuerzel = mToSplitDisplayValue.group(3);
		if (!GermanTaxNumberDefiningData.getInstance().existsFederalStateCode(landerkuerzel)) {
			return new CustomFieldTypeConversionResultImpl(ewAzDisplayValueWithLaenderkuerzel, CONVERSION_ERROR_IDENTIFIER);
		}
		int bundeslandNr = getLaenderNummer(landerkuerzel);

		String ewAzDisplayValue = mToSplitDisplayValue.group(1);

		// Formale Prüfung der Oberflächendarstellung
		Matcher matcher = EWAzProperties.getMuster(bundeslandNr).matcher(ewAzDisplayValue);
		if (!matcher.matches()) {
			return new CustomFieldTypeConversionResultImpl(ewAzDisplayValueWithLaenderkuerzel, CONVERSION_ERROR_IDENTIFIER);
		}
		int finanzamtNrAnzStellen = EWAzProperties.getFANrStellen(bundeslandNr);

		String formattedString = ewAzDisplayValue.replaceAll("[ \\/\\.-]", "");
		String finanzamtNr = formattedString.substring(0, finanzamtNrAnzStellen);
		String nummer = formattedString.substring(finanzamtNrAnzStellen);

		String bufaNr = getBundesfinanzamtsnummer(bundeslandNr, finanzamtNr, finanzamtNrAnzStellen);
		String bundesLandNrStr = String.valueOf(bundeslandNr);
		if (!bufaNr.startsWith(bundesLandNrStr)) {
			return new CustomFieldTypeConversionResultImpl(ewAzDisplayValueWithLaenderkuerzel, CONVERSION_ERROR_IDENTIFIER);
		}
		String formatschluesselStr = Integer.toString(EWAzProperties.getFormatschluessel(bundeslandNr));
		return new CustomFieldTypeConversionResultImpl(bufaNr + formatschluesselStr + nummer, null);
	}

	private String getBundesfinanzamtsnummer(Integer bundeslandNr, String finanzamtNr, int finanzamtNrAnzStellen) {
		String strBulaNr = String.valueOf(bundeslandNr);
		return strBulaNr.substring(0, STELLEN_BUFANR - finanzamtNrAnzStellen) + finanzamtNr;
	}

	private int getBuLaNrFromString(String ewaz) {
		if (!ewaz.isEmpty() && (ewaz.charAt(0) == '5' || ewaz.charAt(0) == '9')) {
			return Integer.parseInt(ewaz.substring(0, 1));
		} else {
			return Integer.parseInt(ewaz.substring(0, 2));
		}
	}
}
