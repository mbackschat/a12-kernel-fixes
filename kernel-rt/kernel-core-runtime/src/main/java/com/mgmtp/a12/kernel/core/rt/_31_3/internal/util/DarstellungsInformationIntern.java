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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.internal.DataFormatsBuilder;

import lombok.Getter;
import lombok.NonNull;

/**
 * Die Klasse kapselt die Prüfungen für Darstellungsinformation
 * Stellt die Information für die Darstellungsspezifischen Formate bereit.
 */
public class DarstellungsInformationIntern {

	//Liste der gültigen DezimalTrenner
	private static final List<String> VALID_TRENNER = Collections.unmodifiableList(Arrays.asList(".", ","));

	@Getter private final String wahrWert;
	@Getter private final String falschWert;
	private final boolean doConvertCustomFieldTypesAlways;
	private final Set<String> doConvertCustomFieldTypes;
	private final boolean doConvertEnumerationen;
	private final boolean decimalPlacesOptional;
	private final DarstellungsInfoDatum infoDatum;
	@Getter private final int baseYear;
	@Getter private final TimeZone timeZone;
	private final String thousandsSeparator;
	private final String thousandsSeparatorApplyToTrait;

	private final char[] dezimalTrenner = new char[2];

	/**
	 * Der Konstruktor überprüft die Darstellungsinformation
	 *
	 * @param dezimalTrennerIntern
	 * 		Dezimaltrenner in XML-Darstellung
	 * @param infoOrNull
	 * 		übergebene Darstellungsinformation
	 * @param baseYear
	 * 		base year of the model
	 * @param defaultValuesOrNull
	 * 		the default values for the current language. Used to replace parameters specifying
	 *        {@link PresentationInformation#LANGUAGE_DEFINED}.
	 * @param timeZone
	 * 		the time zone, date values are interpreted in.
	 */
	public DarstellungsInformationIntern(
			char dezimalTrennerIntern,
			PresentationInformation infoOrNull,
			int baseYear,
			PresentationInformationValues defaultValuesOrNull,
			TimeZone timeZone
	) {
		this.dezimalTrenner[0] = dezimalTrennerIntern;
		this.dezimalTrenner[1] = dezimalTrennerIntern;
		this.baseYear = baseYear;
		this.timeZone = timeZone;
		if (infoOrNull == null) {//Es gibt nichts zu tun
			this.infoDatum = new DarstellungsInfoDatum();
			this.doConvertEnumerationen = false;
			this.doConvertCustomFieldTypesAlways = false;
			this.doConvertCustomFieldTypes = Collections.emptySet();
			this.falschWert = null;
			this.wahrWert = null;
			this.decimalPlacesOptional = false;
			this.thousandsSeparator = "";
			this.thousandsSeparatorApplyToTrait = null;
			return;
		}

		// we need to extract the thousands separator first, since we need this information for future checks
		this.thousandsSeparator = getInfoKey(infoOrNull, PresentationInformation.THOUSANDS_SEPARATOR, defaultValuesOrNull);
		String thousandsSeparatorApplyToTrait = getInfoKey(infoOrNull, PresentationInformation.THOUSANDS_SEPARATOR_APPLY_TO_TRAIT,
				defaultValuesOrNull);
		this.thousandsSeparatorApplyToTrait = !StringUtils.isEmpty(thousandsSeparatorApplyToTrait) ?
				thousandsSeparatorApplyToTrait :
				null;

		// check decimal separator
		String dezTrenner = getInfoKey(infoOrNull, PresentationInformation.DECIMAL_SEPARATOR, defaultValuesOrNull);
		if (dezTrenner != null) {
			this.dezimalTrenner[1] = dezTrenner.charAt(0);
		}
		checkSeparators(dezTrenner);

		this.wahrWert = getInfoKey(infoOrNull, PresentationInformation.TRUE_VALUE, defaultValuesOrNull);
		this.falschWert = getInfoKey(infoOrNull, PresentationInformation.FALSE_VALUE, defaultValuesOrNull);

		String convertCustomFieldTypes = getInfoKey(infoOrNull, PresentationInformation.CONVERT_CUSTOM_FIELD_TYPES_LIST,
				defaultValuesOrNull);
		if (convertCustomFieldTypes != null) {
			this.doConvertCustomFieldTypes = Arrays.stream(StringUtils.split(convertCustomFieldTypes, ","))
					.collect(Collectors.toSet());
		} else {
			this.doConvertCustomFieldTypes = Collections.emptySet();
		}
		String convertCustomFieldAlways = getInfoKey(infoOrNull, PresentationInformation.CONVERT_CUSTOM_FIELD_TYPES_ALL,
				defaultValuesOrNull);
		this.doConvertCustomFieldTypesAlways = PresentationInformation.TRUE.equals(convertCustomFieldAlways);
		checkCustomFieldTypeConversion();

		String convertEnums = getInfoKey(infoOrNull, PresentationInformation.CONVERT_ENUMERATIONS, defaultValuesOrNull);
		this.doConvertEnumerationen = PresentationInformation.TRUE.equals(convertEnums);

		String decimalPlacesOpt = getInfoKey(infoOrNull, PresentationInformation.DECIMAL_PLACES_OPTIONAL, defaultValuesOrNull);
		this.decimalPlacesOptional = PresentationInformation.TRUE.equals(decimalPlacesOpt);

		//Konvertierungsinformation zu Datumsdarstellungen wird in eine eigene Klasse ausgelagert.
		infoDatum = new DarstellungsInfoDatum(infoOrNull, defaultValuesOrNull);
	}

	public static @NonNull DarstellungsInformationIntern createInternalRepresentation(@NonNull IMetaDataValidierung metaData) {
		return new DarstellungsInformationIntern(
				metaData.getDezimalTrenner(), null, metaData.getBaseYearOrDefault(), null, metaData.getTimeZone());
	}

	private void checkCustomFieldTypeConversion() {
		if (doConvertCustomFieldTypesAlways && !doConvertCustomFieldTypes.isEmpty()) {
			throw new IllegalArgumentException(
					"When defining custom field types to always be converted, it is invalid to additionally specify a concrete list of custom field types to be converted.");
		}
	}

	private void checkSeparators(String dezTrenner) {
		if (dezTrenner != null && !VALID_TRENNER.contains(dezTrenner)) {
			throw new IllegalArgumentException(
					String.format("Invalid presentation information: The decimal separator '%s' is invalid.", dezTrenner));
		}
		if (!StringUtils.isEmpty(thousandsSeparator) && StringUtils.isEmpty(dezTrenner)) {
			throw new IllegalArgumentException("If a thousands separator was defined, a decimal separator must be defined.");
		}
		if (thousandsSeparator != null && thousandsSeparator.equals(String.valueOf(this.getDezimalTrenner()))) {
			throw new IllegalArgumentException("Decimal separator and thousands separator must not be the same!");
		}
		if (thousandsSeparatorApplyToTrait != null) {
			switch (thousandsSeparatorApplyToTrait) {
				case PresentationInformation.TRAIT_AMOUNT, PresentationInformation.TRAIT_NONE:
					break;
				default:
					throw new IllegalArgumentException(
							String.format(
									"Invalid presentation information: The thousandsSeparatorApplyToTrait '%s' is invalid.",
									thousandsSeparatorApplyToTrait));
			}
		}
	}

	public @NonNull String convertDatumFormatXml2Ofl(@NonNull String xmlFormat) {
		return infoDatum.convertDatumFormatXml2Ofl(xmlFormat);
	}

	public String convertDatumBereichTrennerXML2Ofl(String xmlDatumBereichTrenner) {
		return infoDatum.convertDatumBereichTrennerXML2Ofl(xmlDatumBereichTrenner);
	}

	public DataFormats getDateDisplayDataFormats(DataFormats defaults) {
		return infoDatum.getDateDisplayDataFormats(defaults);
	}

	/**
	 * The {@link DataFormats} describing the internal (XML) representation of dates. Mirrors the TypeScript
	 * {@code DarstellungsInformationIntern.getInternalDataFormats}.
	 */
	public static DataFormats getInternalDataFormats(char decimalSeparator) {
		return new DataFormatsBuilder().decimalSeparator(decimalSeparator).build();
	}

	// gib immer die Oberflächenvalidierung zurück. Falls es keine spezifische
	//Oberflächenvalidierung gibt, ist die Oberflächenvalidierung bereits im
	//Konstruktor auf die XML Darstellung gesetzt.  Daher muss hier nicht mehr
	// unterschieden werden.
	public char getDezimalTrenner() {
		return dezimalTrenner[FeldDarstellungsTyp.OBERFLAECHEN_DARSTELLUNG.ordinal()];
	}

	public char getDezimalTrennerIntern() {
		return dezimalTrenner[FeldDarstellungsTyp.XML_DARSTELLUNG.ordinal()];
	}

	public static String getInfoKey(PresentationInformation info, String key, PresentationInformationValues defaultWerte) {
		String result = info.getPresentationInformation().get(key);
		if (PresentationInformation.LANGUAGE_DEFINED.equals(result)) {
			result = SprachenHelper.getSprachspezifischeDarstellungsinfo(key, defaultWerte);
		}
		return result;
	}

	public int getMinLaengeDatum(String xmlFormat) {
		return infoDatum.getMinLaengeDatum(xmlFormat);
	}

	public int getMaxLaengeDatum(String xmlFormat) {
		return infoDatum.getMaxLaengeDatum(xmlFormat);
	}

	public int getMinLaengeDatumBereich(String xmlDatumFormat, String xmlDatumBereichTrenner) {
		return infoDatum.getMinLaengeDatumBereich(xmlDatumFormat, xmlDatumBereichTrenner);
	}

	public int getMaxLaengeDatumBereich(String xmlDatumFormat, String xmlDatumBereichTrenner) {
		return infoDatum.getMaxLaengeDatumBereich(xmlDatumFormat, xmlDatumBereichTrenner);
	}

	/**
	 * Gebe den Index (Punkt oder Komma) für die Validierung zurück
	 * In den Arrays ist immer die Punktnotation in Wert 0 gespeichert und
	 * die Kommanotation in Wert 1
	 *
	 * @return index Index der in dem Array die relevante Darstellung zurückgibt.
	 */
	public int getDezimalIndexFuerValidierung() {
		return (getDezimalTrenner() == '.') ? 0 : 1;
	}

	/**
	 * Zur Verwendung des Dezimaltrenners in einem Pattern
	 * muss der Punkt (nicht das Komma) escaped werden.
	 */
	public static String getDezimalTrennerForPattern(int index) {
		String result = VALID_TRENNER.get(index);
		if (result.equals(".")) {
			return "\\.";
		}
		return result;
	}

	/**
	 * Liefert <code>true</code>, wenn für das übergebene Format bzgl. der Darstellungsinformation
	 * ein Datumstrenner hinten erlaubt ist, aber optional. Dies ist der Fall, wenn
	 * <ul>
	 * <li>in der Darstellungsinformation der Punkt als Datumstrenner spezifziert wurde</li>
	 * <li>in der Darstellungsinformation eine Datumsreihenfolge spezifiziert wurde, in der Tag vor
	 * Monat kommt</li>
	 * <li>das Format spezifiziert Tag und Monat, kein Jahr</li>
	 * </ul>
	 */
	public boolean datumTrennerHintenOptional(String datumsFormat) {
		return infoDatum.datumTrennerHintenOptional(datumsFormat);
	}

	public boolean doConvertCustomFieldType(String customFieldTypeName) {
		return doConvertCustomFieldTypesAlways || doConvertCustomFieldTypes.contains(customFieldTypeName);
	}

	public boolean doConvertEnumerationen() {
		return doConvertEnumerationen;
	}

	public boolean areDecimalPlacesOptional() {
		return decimalPlacesOptional;
	}

	public String getThousandsSeparatorForTrait(String trait) {
		if (thousandsSeparatorApplyToTrait == null
				|| thousandsSeparatorApplyToTrait.equals(trait)) {
			return thousandsSeparator;
		} else {
			return null;
		}
	}
}
