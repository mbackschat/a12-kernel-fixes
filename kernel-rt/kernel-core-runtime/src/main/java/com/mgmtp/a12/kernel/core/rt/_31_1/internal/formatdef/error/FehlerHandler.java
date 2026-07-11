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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.error;

import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.CheckCommand;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.IValidierungParam;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.InternalFallbackErrorLookup;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;

import lombok.NonNull;

/**
 *
 */
public class FehlerHandler {

	private static final String FIELD_REFERENCE_DE = "$Feld$";
	private static final String FIELD_REFERENCE_EN = "$field$";
	private static final String FIELD_VALUE_REFERENCE_DE = "$Feld.Wert$";
	private static final String FIELD_VALUE_REFERENCE_EN = "$field.value$";

	private FehlerHandler() {
		throw new IllegalStateException("Utility class");
	}

	public static ValidType addFehler(IResult erg, String errorKey, IIdentifier id, String fehlertext, boolean nurPruefen) {
		if (!nurPruefen) {
			erg.addError(IFormaleFehlerConstants.FORMALE_PRUEFUNG, errorKey, id, fehlertext, IResult.ErrorType.VALUE_ERROR,
					Collections.singleton(id), Collections.<IIdentifier>emptySet(), Collections.<IIdentifier>emptySet());
			erg.addMessageV2(IFormaleFehlerConstants.FORMALE_PRUEFUNG, errorKey, fehlertext, IResult.ErrorType.VALUE_ERROR,
					SeverityType.ERROR, CheckCommand.createSingleFieldCause(id));
		}
		return ValidType.Invalid;
	}

	public static ValidType addFehler(String errorKey, String fehlerTextWithParam, IValidierungParam param) {
		IIdentifier id = param.getIdentifier();
		IResult erg = param.getErgebnis();
		boolean nurPruefen = param.isNurPruefen();
		String fehler = FehlerHandler.replaceFeldName(fehlerTextWithParam, param.getDaten(), id);
		return addFehler(erg, errorKey, id, fehler, nurPruefen);
	}

	public static @NonNull String transformFehlerText(@NonNull String fehlerText, @NonNull IValidierungParam param) {
		return transformFehlerText(fehlerText, param.getDaten(), param.getIdentifier(), param.getWert());
	}

	public static @NonNull String transformFehlerText(@NonNull String errorMessage, @NonNull IData data, @NonNull IIdentifier id,
			@NonNull String value) {
		String result = errorMessage;
		if (result.contains(FIELD_REFERENCE_DE) || result.contains(FIELD_REFERENCE_EN)) {
			String stringRepraesentation = getStringRepraesentation(data, id);
			result = result.replace(FIELD_REFERENCE_DE, stringRepraesentation)
					.replace(FIELD_REFERENCE_EN, stringRepraesentation);
		}
		if (result.contains(FIELD_VALUE_REFERENCE_DE) || result.contains(FIELD_VALUE_REFERENCE_EN)) {
			// We have to make sure that ALL field values are replaced
			// We don't use the method replaceAll because the field value to display may contain special characters for a regular expression
			result = result.replace(FIELD_VALUE_REFERENCE_DE, getValueForDisplay(data, id, value))
					.replace(FIELD_VALUE_REFERENCE_EN, getValueForDisplay(data, id, value));
		}
		return result;
	}

	private static String getValueForDisplay(IData data, IIdentifier id, String value) {
		// in the convert method there is no data to get the value from and in this case we use the given value (param.getWert)
		String valueForDisplay = data.getValueForDisplay(id);
		return valueForDisplay == null ? value : valueForDisplay;
	}

	private static String getStringRepraesentation(IData data, IIdentifier id) {
		return data.identifierToString(id);
	}

	public static String replaceFeldName(String fehlerText, IData data, IIdentifier id) {
		return fehlerText.replace("$<fieldName>$", getStringRepraesentation(data, id));
	}

	/**
	 * Returns the error text for the given error id and a Locale. If no error texts are available
	 * for the given Locale a {@link MissingResourceException} will be thrown.
	 *
	 * @param fehlerId
	 * 		The error id.
	 * @param isExtmp
	 * 		If true, ExtMP-specific error texts will be returned.
	 * @param locale
	 * 		The locale for which an error message is needed.
	 * @return The error message for the given error id and language. If no such error message
	 * 		exists, a {@link MissingResourceException} will be thrown.
	 */
	public static String getFehlertext(RuntimeFormalErrorEnum fehlerId, boolean isExtmp, Locale locale) {
		if (fehlerId == RuntimeFormalErrorEnum.KEIN_FEHLER) {
			return "";
		}
		Locale myLocale = getRelevantLocale(locale, isExtmp);

		return InternalFallbackErrorLookup.getBareErrorMessageUnformatted(fehlerId.name(),
				Constants.FORMAL_ERRORS_MESSAGES_RESOURCE_NAME, myLocale,
				ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
	}

	private static Locale getRelevantLocale(Locale srcLocale, boolean isExtmp) {
		if (isExtmp) {
			// custom creation of a locale which would not be valid using commons-lang3:LocaleUtils
			return Locale.of(srcLocale.getLanguage(), "EXTMP");
		}
		return srcLocale;
	}
}
