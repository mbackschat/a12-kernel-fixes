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

package com.mgmtp.a12.kernel.md.rt.a12internal.service;

import java.util.Map;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.LocalizableUtil;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * The localization API is currently only implemented and used for TypeScript. This class provides temporary utility methods to
 * provide very limited support for parts of that API to Java already.
 */
@OnlyForUsage
public class LocalizationKeyFactory {

	/**
	 * Maps the error code of a formal error to the {@link RuntimeFormalErrorEnum} whose name is used as the last segment of the
	 * localization key.
	 * <p>
	 * The mapping is not injective: the TypeScript runtime picks the concrete {@link RuntimeFormalErrorEnum} based on the format
	 * definition of the validated field (signed or unsigned, number of decimal places, amount or plain number, start or end of a
	 * date range, ...), and that information is not part of an {@link IMessage}. Where several formal errors share one error
	 * code, the neutral variant is used, i.e. plain numbers without a sign and the non-range date errors.
	 */
	// @formatter:off
	static final Map<String, RuntimeFormalErrorEnum> FORMAL_ERRORS_BY_ERROR_CODE = Map.ofEntries(
			// identifier and character set checks, see FormalChecker
			Map.entry(IFormaleFehlerConstants.FEHLER_FALSCHE_ANZAHL_INDICES, RuntimeFormalErrorEnum.FALSCHE_ANZAHL_INDICES_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_FELD_UNBEKANNT, RuntimeFormalErrorEnum.FEHLER_FELD_UNBEKANNT),
			Map.entry(IFormaleFehlerConstants.ZEICHEN_NICHT_IM_ZEICHENSATZ, RuntimeFormalErrorEnum.ZEICHEN_NICHT_IM_ZEICHENSATZ_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_FUEHRENDES_BLANK, RuntimeFormalErrorEnum.FEHLER_FUEHRENDES_BLANK_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_NACHFOLGENDES_BLANK, RuntimeFormalErrorEnum.FEHLER_NACHFOLGENDES_BLANK_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_WERT_ZU_LANG, RuntimeFormalErrorEnum.FEHLER_WERT_ZU_LANG_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_LAUFENDE_NR_ZU_KLEIN, RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_LAUFENDE_NR_ZU_GROSS, RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_ZU_GROSS_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZEILENNUMMER_ZU_KLEIN, RuntimeFormalErrorEnum.FEHLER_ZEILE_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZEILENNUMMER_ZU_GROSS, RuntimeFormalErrorEnum.FEHLER_ZEILENNUMMER_ZU_GROSS_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_KONTEXTNUMMER_ZU_KLEIN, RuntimeFormalErrorEnum.KONTEXTNUMMER_ZU_KLEIN_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_KONTEXTNUMMER_ZU_GROSS, RuntimeFormalErrorEnum.FEHLER_KONTEXTNUMMER_ZU_GROSS_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_KEIN_USB_SPEZIFIZIERT, RuntimeFormalErrorEnum.FEHLER_KEIN_USB_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_USB_ZU_KLEIN, RuntimeFormalErrorEnum.FEHLER_USB_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_USB_ZU_GROSS, RuntimeFormalErrorEnum.FEHLER_USB_ZU_GROSS_FM_TEXT),
			Map.entry(IFormaleFehlerConstants.FEHLER_USB_SPEZIFIZIERT, RuntimeFormalErrorEnum.FEHLER_USB_ANGEGEBEN_FM_TEXT),

			// number checks, see FormatDefinitionZahl. Ambiguous, see the note above: the runtime additionally distinguishes
			// plain numbers from amounts (GBOC/GBMC) and the number of decimal places (ONK/VNK/FNKOVZ/FNKPVZ, OVZ/MVZ).
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_MIT_UNGUELTIGEN_ZEICHEN, RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_OHNE_DEZIMALTRENNER, RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN, RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_ENTHAELT_PLUS, RuntimeFormalErrorEnum.ZAHL_ENTHAELT_PLUS),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_ENTHAELT_VORZEICHEN, RuntimeFormalErrorEnum.ZAHL_ENTHAELT_VORZEICHEN),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_HAT_DEZIMALTRENNER, RuntimeFormalErrorEnum.ZAHL_HAT_DEZIMALTRENNER),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_ZUVIELE_VORKOMMASTELLEN, RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZ),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_IST_NULL, RuntimeFormalErrorEnum.ZAHL_IST_NULL),
			Map.entry(IFormaleFehlerConstants.FEHLER_NULL_ENTHAELT_VORZEICHEN, RuntimeFormalErrorEnum.ZAHL_NULL_ENTHAELT_VORZEICHEN),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_HAT_FUEHRENDE_NULLEN, RuntimeFormalErrorEnum.ZAHL_HAT_FUEHRENDE_NULLEN),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_ZU_KURZ, RuntimeFormalErrorEnum.ZAHL_ZU_KURZ),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_ZU_LANG, RuntimeFormalErrorEnum.ZAHL_ZU_LANG),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_ZU_KLEIN, RuntimeFormalErrorEnum.ZAHL_ZU_KLEIN),
			Map.entry(IFormaleFehlerConstants.FEHLER_ZAHL_ZU_GROSS, RuntimeFormalErrorEnum.ZAHL_ZU_GROSS),
			Map.entry(IFormaleFehlerConstants.STELLIGKEIT_ZAHL_ZU_LANG, RuntimeFormalErrorEnum.STELLIGKEIT_ZU_LANG),

			// string and boolean checks, see FormatDefinitionString
			Map.entry(IFormaleFehlerConstants.FEHLER_STRING_ZU_KURZ, RuntimeFormalErrorEnum.STRING_ZU_KURZ),
			Map.entry(IFormaleFehlerConstants.FEHLER_STRING_ZU_LANG, RuntimeFormalErrorEnum.STRING_ZU_LANG),
			Map.entry(IFormaleFehlerConstants.FEHLER_STRING_MIT_NEWLINE, RuntimeFormalErrorEnum.STRING_ENTHAELT_NEWLINE),
			Map.entry(IFormaleFehlerConstants.FEHLER_FELD_JA_NEIN, RuntimeFormalErrorEnum.FELD_JA_NEIN),
			Map.entry(IFormaleFehlerConstants.FEHLER_FELD_JA, RuntimeFormalErrorEnum.FELD_JA),

			// date checks, see FormatDefinitionDatum and FormatDefinitionDatumBereich. Ambiguous, see the note above: the
			// runtime additionally distinguishes the erroneous part of the date and the start from the end of a date range.
			Map.entry(IFormaleFehlerConstants.FEHLER_DATUM, RuntimeFormalErrorEnum.DATUM),
			Map.entry(IFormaleFehlerConstants.FEHLER_DATUM_FORMAT, RuntimeFormalErrorEnum.DATUM_FORMAT),
			Map.entry(IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_TRENNER_FEHLT, RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT),
			Map.entry(IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT, RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT),
			Map.entry(IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_NICHT_GUELTIG, RuntimeFormalErrorEnum.DATUM_BEREICH_NICHT_GUELTIG),
			Map.entry(IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_DATUM, RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM),

			// computation errors, see CalculationController
			Map.entry(IFormaleFehlerConstants.BERECHNUNG_WERT_FEHLER, RuntimeFormalErrorEnum.BERECHNUNG_WERT_FEHLER),

			// errors of the rules generated for index fields, see CodeGenRuleDefinition
			Map.entry(IFormaleFehlerConstants.FEHLER_INDEXFELD, RuntimeFormalErrorEnum.INDEX_FELD_FEHLER),
			Map.entry(IFormaleFehlerConstants.FEHLER_MULTI_SELECT, RuntimeFormalErrorEnum.INDEX_FELD_FEHLER),
			Map.entry(IFormaleFehlerConstants.FEHLER_SOLITARY_INDEX, RuntimeFormalErrorEnum.SOLITARY_INDEX_FEHLER));
	// @formatter:on

	private LocalizationKeyFactory() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Derives the localization key of the given message, i.e. the Java equivalent of the key of the first entry of a TypeScript
	 * {@code Message.errorText}. The keys are documented in the chapter <em>Localization</em> of the Kernel developer
	 * documentation.
	 * <p>
	 * Note that this is only an approximation: <ul>
	 * <li>Fallback messages are not considered. For example required field errors always produce two Localizables in TypeScript:
	 * First {@literal "documentModel.fieldRequiredErrorMessage.<dmId>.<dmPath>"}, then
	 * {@literal "kernel.formalErrors.PFLICHT_FELD_FEHLER"}</li>
	 * <li>For some formal errors {@link IMessage#getErrorText()} may differ for the same {@link IMessage#getErrorCode()}
	 * depending on the field's format specification. For example the error code {@literal "zahlZuvieleVorkommastellen"} can have
	 * any of the following messages - each with its own localization key:<ul>
	 * <li>{@literal kernel.formalErrors.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZ -> "Only values with up to
	 * <numberOfDigitsBeforeDecimalPoint> digits before the decimal point are allowed."}</li>
	 * <li>{@literal kernel.formalErrors.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZ -> "Only values with up to
	 * <numberOfDigitsBeforeDecimalPoint> digits and an optional negative sign before the decimal point are allowed."}</li>
	 * <li>{@literal kernel.formalErrors.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZONK -> "Only values with up to
	 * <numberOfDigitsBeforeDecimalPoint> digits are allowed."}</li>
	 * <li>{@literal kernel.formalErrors.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZONK -> "Only values with up to
	 * <numberOfDigitsBeforeDecimalPoint> digits and an optional negative sign are allowed."}</li>
	 * </ul>
	 * In such cases we always return one of these keys.
	 * </li>
	 * </ul>
	 *
	 * @param message
	 * 		the message to derive the localization key for
	 * @param documentModelId
	 * 		the id of the document model that was validated, as specified in the header of the document model. It is not part
	 * 		of the message and therefore has to be provided by the caller.
	 * @return the localization key of the message
	 */
	public static String approximateLocalizationKeyFromMessage(@NonNull IMessage message, @NonNull String documentModelId) {
		String rulePath = message.getRulePath().orElse(null);
		if (rulePath != null && !IResult.FORMAL_VALIDATION_RULE_NAME.equals(rulePath)) {
			return LocalizableUtil.createRuleErrorKey(rulePath, documentModelId);
		}

		return switch (message.getErrorCode()) {
			case IFormaleFehlerConstants.FEHLER_PFLICHTFELD ->
					LocalizableUtil.createFieldRequiredErrorKey(message.getErrorFieldPointer().fullName(), documentModelId);
			// the same error code is used for a violated pattern and for a value that is not part of the enumeration
			case IFormaleFehlerConstants.FEHLER_STRING_FALSCHES_MUSTER ->
					LocalizableUtil.createFieldErrorKey(message.getErrorFieldPointer().fullName(), documentModelId);
			default -> {
				RuntimeFormalErrorEnum formalError = FORMAL_ERRORS_BY_ERROR_CODE.get(message.getErrorCode());
				// an unknown error code is used as it is so that error codes added later degrade gracefully
				yield LocalizableUtil.createFormalErrorKeyFromString(
						formalError == null ? message.getErrorCode() : formalError.name());
			}
		};
	}
}
