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
import com.mgmtp.a12.utils.conversion.internal.ConfirmConversion;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.internal.DataFormatsBuilder;

import lombok.NonNull;

/**
 * Klasse modelliert zur Laufzeit ein FeldJa Feld.
 */
public class FormatDefinitionJa implements IFormatDefinition {

	private String jaWert;

	public FormatDefinitionJa(String jaWert) {
		this.jaWert = jaWert;
	}

	@Override
	public String convert(final String wert,
			DarstellungsInformationIntern darstellungsInfo,
			boolean vonXml, RawParamHandler4Convert additionalParameters) {
		DataFormats internalDataFormats = confirmDataFormats(jaWert);
		DataFormats displayDataFormats = confirmDataFormats(
				darstellungsInfo.getWahrWert() != null ? darstellungsInfo.getWahrWert() : jaWert);
		if (vonXml) {
			return ConfirmConversion.convert(wert, internalDataFormats, displayDataFormats);
		} else {
			return ConfirmConversion.convert(wert, displayDataFormats, internalDataFormats);
		}
	}

	/**
	 * Builds a {@link DataFormats} carrying only the true value the {@link ConfirmConversion} reads.
	 */
	private static DataFormats confirmDataFormats(String trueValue) {
		return new DataFormatsBuilder().trueValue(trueValue).build();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + jaWert;
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

		//Standardmäßig wird gegen den im Editor gespeicherten Wert geprüft.
		String wahrWert = jaWert;

		//Falls Wert in der Darstellungsinformation überschrieben ist,
		//nehme diese Information
		DarstellungsInformationIntern info = param.getDarstellungsInfo();
		if (info.getWahrWert() != null) {
			wahrWert = info.getWahrWert();
		}

		String wert = param.getWert();
		if (!wahrWert.equals(wert)) {
			String fehler = FehlerHandler.getFehlertext(RuntimeFormalErrorEnum.FELD_JA, param.isExtmp(),
					param.getSprache()).replace("<trueValue>", wahrWert);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_FELD_JA, fehler, param);
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
			return jaWert.length();
		}
		if (key.equals(IMetaKeys.FIELD_MAX_LENGTH)) {
			return jaWert.length();
		}
		if (key.equals(IMetaKeys.FIELD_FIELD_TYPE)) {
			return IFormatDefinition.FeldFormatEnumType.ER_FELDJA.toString();
		}
		if (key.equals(IMetaKeys.FIELD_VALUE_TRUE)) {
			return jaWert;
		}

		return null;
	}

	@Override
	public int compare(@NonNull String s1, @NonNull String s2, IMetaDataValidierung metaData) {
		// there is nothing to compare
		return 0;
	}

	@Override
	public Boolean convertToBasicType(String internalValue, IMetaDataValidierung metaData) {
		Object value = ConfirmConversion.convertStringToConfirm(internalValue, confirmDataFormats(jaWert))
				.value().orElse(null);
		return value instanceof Boolean bool ? bool : null;
	}

	@Override
	public String convertFromBasicType(@NonNull Object value, @NonNull IMetaDataValidierung metaDat) {
		if (value instanceof String strVal && strVal.isEmpty()) {
			return null;
		}
		return ConfirmConversion.convertConfirmToString(value, confirmDataFormats(jaWert)).orElse(null);
	}
}
