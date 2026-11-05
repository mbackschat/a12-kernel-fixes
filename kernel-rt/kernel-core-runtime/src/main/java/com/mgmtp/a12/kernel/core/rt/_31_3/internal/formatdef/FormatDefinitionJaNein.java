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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef;

import java.util.Locale;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.utils.conversion.internal.BooleanConversion;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.internal.DataFormatsBuilder;

import lombok.NonNull;

/**
 * Klasse modelliert zur Laufzeit ein FeldJaNein Feld.
 */
public class FormatDefinitionJaNein implements IFormatDefinition {

	private String jaWert;
	private String neinWert;

	private int minLaenge;
	private int maxLaenge;

	public FormatDefinitionJaNein(String jaWert, String neinWert) {
		this.jaWert = jaWert;
		this.neinWert = neinWert;

		if (jaWert.length() > neinWert.length()) {
			minLaenge = neinWert.length();
			maxLaenge = jaWert.length();
		} else {
			minLaenge = jaWert.length();
			maxLaenge = neinWert.length();
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + jaWert + ", " + neinWert;
	}

	@Override
	public String convert(final String wert,
			DarstellungsInformationIntern darstellungsInfo,
			boolean vonXml, RawParamHandler4Convert additionalParameters) {
		DataFormats internalDataFormats = booleanDataFormats(jaWert, neinWert);
		DataFormats displayDataFormats = booleanDataFormats(
				darstellungsInfo.getWahrWert() != null ? darstellungsInfo.getWahrWert() : jaWert,
				darstellungsInfo.getFalschWert() != null ? darstellungsInfo.getFalschWert() : neinWert);
		if (vonXml) {
			return BooleanConversion.convert(wert, internalDataFormats, displayDataFormats);
		} else {
			return BooleanConversion.convert(wert, displayDataFormats, internalDataFormats);
		}
	}

	/**
	 * Builds a {@link DataFormats} carrying only the true/false value pair the {@link BooleanConversion} reads.
	 */
	private static DataFormats booleanDataFormats(String trueValue, String falseValue) {
		return new DataFormatsBuilder().trueValue(trueValue).falseValue(falseValue).build();
	}

	/**
	 * allgemeine Methode zum Validieren des Eingabewertes wert.
	 */
	@Override
	public ValidType validiereFormat(IValidierungParam param, IMetaDataValidierung metaData, boolean fullCheck) {
		if (!fullCheck && param.getWert().isEmpty()) {
			// this case is for the base type conversion -> an empty value will be converted to null later on
			return ValidType.Valid;
		}

		//Standard: prüfe gegen den im Editor gespeicherten Wert
		String wahrWert = jaWert;
		String falschWert = neinWert;

		DarstellungsInformationIntern info = param.getDarstellungsInfo();
		//Falls Werte in der Darstellungsinformation überschrieben wurden,
		//nehme diese Informationen.
		if (info.getWahrWert() != null) {
			wahrWert = info.getWahrWert();
		}
		if (info.getFalschWert() != null) {
			falschWert = info.getFalschWert();
		}
		String wert = param.getWert();
		if (!wahrWert.equals(wert) && !falschWert.equals(wert)) {
			String fehler = FehlerHandler.getFehlertext(RuntimeFormalErrorEnum.FELD_JA_NEIN,
					param.isExtmp(), param.getSprache()).replace("<trueValue>", wahrWert).replace("<falseValue>", falschWert);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_FELD_JA_NEIN, fehler, param);
		}

		return ValidType.Valid;
	}

	@Override
	public String getDefaultValue(IMetaDataValidierung metaData, PresentationInformation presentationInformation, Locale sprache) {
		return "";
	}

	@Override
	public Object getWert(String key) {
		if (key.equals(IMetaKeys.FIELD_MIN_LENGTH)) {
			return minLaenge;
		}
		if (key.equals(IMetaKeys.FIELD_MAX_LENGTH)) {
			return maxLaenge;
		}
		if (key.equals(IMetaKeys.FIELD_FIELD_TYPE)) {
			return IFormatDefinition.FeldFormatEnumType.ER_FELDJANEIN.toString();
		}
		if (key.equals(IMetaKeys.FIELD_VALUE_TRUE)) {
			return jaWert;
		}
		if (key.equals(IMetaKeys.FIELD_VALUE_FALSE)) {
			return neinWert;
		}
		return null;
	}

	@Override
	public int compare(@NonNull String s1, @NonNull String s2, IMetaDataValidierung metaData) {
		// fields with yes-value come first
		int i1 = s1.equals(jaWert) ? 1 : 0;
		int i2 = s2.equals(jaWert) ? 1 : 0;
		return i2 - i1;
	}

	@Override
	public Boolean convertToBasicType(String internalValue, IMetaDataValidierung metaData) {
		Object value = BooleanConversion.convertStringToBoolean(internalValue, booleanDataFormats(jaWert, neinWert))
				.value().orElse(null);
		return value instanceof Boolean bool ? bool : null;
	}

	@Override
	public String convertFromBasicType(@NonNull Object value, @NonNull IMetaDataValidierung metaDat) {
		if (value instanceof String strVal && strVal.isEmpty()) {
			return null;
		}
		return BooleanConversion.convertBooleanToString(value, booleanDataFormats(jaWert, neinWert)).orElse(null);
	}
}
