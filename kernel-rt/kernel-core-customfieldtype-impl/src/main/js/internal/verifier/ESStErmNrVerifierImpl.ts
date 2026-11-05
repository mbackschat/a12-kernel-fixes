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

import type {
    ICustomFieldTypeCheckError,
    ICustomFieldTypeConversionResult,
    ICustomFieldTypeValidationParam,
    ICustomFieldValidator
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import {customFieldTypeConversionResult} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type {IFadat} from "../../IFadat.js";

import {CustomFieldTypeErrorEnum} from "../error/CustomFieldTypeErrorEnum.js";
import {CustomFieldTypeErrorKeys} from "../error/CustomFieldTypeErrorKeys.js";
import {CustomFieldTypeErrorUtil} from "../error/CustomFieldTypeErrorUtil.js";

import {CustomFieldTypeValidationParamImpl} from "../CustomFieldTypeValidationParamImpl.js";

import {FadatLookupUtil} from "./FadatLookupUtil.js";

/**
 * Validates the ESSt-Ermittlungsnummer.
 * <p>
 * <b>Internal format ({@code FFFF8BBBJJJJUUUUUKKKTTTP}, 24 digits):</b>
 * <ul>
 * <li>{@code FFFF} Bundesfinanzamtsnummer (incl. Länderkennung)</li>
 * <li>{@code 8} Merkzeichen (constant {@code 8} at position 5)</li>
 * <li>{@code BBB} Bezirksnummer</li>
 * <li>{@code JJJJ} Jahr</li>
 * <li>{@code UUUUU} Unterscheidungsnummer</li>
 * <li>{@code KKK} Kennzeichnung für zusammen gehörende Fälle</li>
 * <li>{@code TTT} Unterscheidungsnummer für die einzelne Person (Teilfallnummer)</li>
 * <li>{@code P} Prüfziffer</li>
 * </ul>
 * <b>Display format ({@code FFFF/BBB/JJJJ/UUUUU/KKK/000P}):</b> the Merkzeichen {@code 8} is dropped and
 * the {@code TTT} group is always rendered as {@code 000}.
 * <p>
 * The validity check performs, in this order: a formal check of the 24-digit structure (including the
 * fixed Merkzeichen {@code 8}), a validity check of the Bundesfinanzamtsnummer ({@code FFFF}), a check
 * that the Unterscheidungsnummer ({@code UUUUU}) does not consist solely of zeros, and the Prüfziffer
 * ({@code P}) verification. The check digit uses the following procedure: the Ermittlungsnummer is transcoded to
 * a 13-digit number {@code FFF+BBB+ZZ+UUU+TT} (country code, Merkzeichen and {@code KKK} are
 * dropped, {@code JJJJ} becomes a two-digit year reference relative to 1980, only the last three digits
 * of {@code UUUUU} and the last two digits of {@code TTT} are kept), then each digit is multiplied by a
 * fixed weight and the check digit equals the weighted sum modulo 10.
 */
export class ESStErmNrVerifierImpl implements ICustomFieldValidator {
	/**
	 * The internal 24-digit format. Capturing groups: FFFF, BBB, JJJJ, UUUUU, KKK, TTT, P. The Merkzeichen
	 * at position 5 is required to be {@code 8}.
	 */
	private static readonly _INTERNAL_FORMAT =
		/^([0-9]{4})8([0-9]{3})([0-9]{4})([0-9]{5})([0-9]{3})([0-9]{3})([0-9])$/;

	/**
	 * The display format {@code FFFF/BBB/JJJJ/UUUUU/KKK/TTTP}. Capturing groups: FFFF, BBB, JJJJ, UUUUU,
	 * KKK, TTT, P (TTTP is captured in 2 separate groups).
	 */
	private static readonly _DISPLAY_FORMAT =
		/^([0-9]{4})\/([0-9]{3})\/([0-9]{4})\/([0-9]{5})\/([0-9]{3})\/([0-9]{3})([0-9])$/;

	/** The weights of the check-digit procedure (applied to the 13-digit number). */
	private static readonly _CHECK_DIGIT_WEIGHTS: number[] = [4, 8, 9, 1, 2, 3, 6, 7, 3, 2, 1, 0, 0];

	/** The start year of the two-digit year reference used by the check-digit procedure. */
	private static readonly _YEAR_REFERENCE_START: number = 1980;

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static readonly _CONVERSION_ERROR_IDENTIFIER: string = "CONVERSION_ERROR";

	private readonly _fadat: IFadat | undefined;

	public constructor(fadat: IFadat | undefined) {
		this._fadat = fadat;
	}

	validate(
		ermnr: string,
		valParam: ICustomFieldTypeValidationParam,
		isDisplayValue: boolean
	): ICustomFieldTypeCheckError | undefined {
		if (isDisplayValue) {
			// the value must be converted to the internal format before validation
			const _convertResult = this.convertDisplay2Internal(ermnr);
			if (_convertResult.getErrorMessage()) {
				// conversion error
				return CustomFieldTypeErrorUtil.getResult(
					CustomFieldTypeErrorEnum.ESSTERMNR_FORMAT_FEHLER,
					CustomFieldTypeErrorKeys.UNGUELTIGE_ESSTERMNR
				);
			}
			ermnr = _convertResult.getConvertedValue();
		}
		const error = this._checkErmNr(ermnr);
		return CustomFieldTypeErrorUtil.getResult(error, CustomFieldTypeErrorKeys.UNGUELTIGE_ESSTERMNR);
	}

	/**
	 * Checks the given Ermittlungsnummer (internal format) and returns the matching error, or
	 * {@link CustomFieldTypeErrorEnum#KEIN_FEHLER} if it is valid.
	 */
	private _checkErmNr(ermnr: string): CustomFieldTypeErrorEnum {
		const matcher = ESStErmNrVerifierImpl._INTERNAL_FORMAT.exec(ermnr);
		if (!matcher) {
			return CustomFieldTypeErrorEnum.ESSTERMNR_FORMAT_FEHLER;
		}

		const federalTaxOfficeNumber = Number(matcher[1]);
		if (!FadatLookupUtil.existsFederalTaxOfficeNumber(this._fadat, federalTaxOfficeNumber)) {
			return CustomFieldTypeErrorEnum.ESSTERMNR_FINANZAMT_FEHLER;
		}

		const unterscheidungsnummer = matcher[4];
		if (unterscheidungsnummer === "00000") {
			return CustomFieldTypeErrorEnum.ESSTERMNR_UNTERSCHEIDUNGSNUMMER_FEHLER;
		}

		if (!ESStErmNrVerifierImpl._isCheckDigitCorrect(matcher)) {
			return CustomFieldTypeErrorEnum.ESSTERMNR_PRUEFZIFFER_FEHLER;
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Verifies the check digit ({@code P}) of a matched internal Ermittlungsnummer.
	 */
	private static _isCheckDigitCorrect(matcher: RegExpExecArray): boolean {
		const ffff = matcher[1];
		const bbb = matcher[2];
		const jjjj = matcher[3];
		const uuuuu = matcher[4];
		const ttt = matcher[6];
		const checkDigit = Number(matcher[7]);

		// Drop the country code (first digit of FFFF), keep BBB, encode the year
		// as a two-digit reference relative to 1980, keep only the last three digits of UUUUU and the
		// last two digits of TTT. The Merkzeichen and KKK do not contribute to the check digit.
		const fff = ffff.substring(1);
		const year = Number(jjjj);
		const yearReference = Math.max(0, year - ESStErmNrVerifierImpl._YEAR_REFERENCE_START);
		// The year reference is two digits; years before 1980 map to 0. Years from 2080 onwards are
		// outside the specified range and wrap around (kept two-digit so the number stays 13 digits).
		const zz = String(yearReference % 100).padStart(2, "0");
		const uuu = uuuuu.substring(2);
		const tt = ttt.substring(1);

		const nrw = fff + bbb + zz + uuu + tt;

		let sum = 0;
		for (let i = 0; i < ESStErmNrVerifierImpl._CHECK_DIGIT_WEIGHTS.length; i++) {
			sum += ESStErmNrVerifierImpl._CHECK_DIGIT_WEIGHTS[i] * Number(nrw.charAt(i));
		}
		return checkDigit === sum % 10;
	}

	convertDisplay2Internal(displayValue: string): ICustomFieldTypeConversionResult {
		const matcher = ESStErmNrVerifierImpl._DISPLAY_FORMAT.exec(displayValue);
		if (!matcher) {
			return customFieldTypeConversionResult(
				displayValue,
				ESStErmNrVerifierImpl._CONVERSION_ERROR_IDENTIFIER
			);
		}
		// Reinsert the Merkzeichen "8" after FFFF; the remaining groups (incl. KKK and TTTP) keep their order.
		const internalValue =
			matcher[1] + "8" + matcher[2] + matcher[3] + matcher[4] + matcher[5] + matcher[6] + matcher[7];
		return customFieldTypeConversionResult(internalValue);
	}

	convertInternal2Display(internalValue: string): ICustomFieldTypeConversionResult {
		const valParam: ICustomFieldTypeValidationParam = new CustomFieldTypeValidationParamImpl(
			CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH,
			CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH
		);
		const _checkResult = this.validate(internalValue, valParam, false);
		if (_checkResult) {
			return customFieldTypeConversionResult(
				internalValue,
				"Konvertiere nur gültige Ermittlungsnummern. " + _checkResult.getErrorMessage()
			);
		}
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const matcher = ESStErmNrVerifierImpl._INTERNAL_FORMAT.exec(internalValue)!;
		// Drop the Merkzeichen "8" and always render the TTT group as "000".
		return customFieldTypeConversionResult(
			matcher[1] + "/" + matcher[2] + "/" + matcher[3] + "/" + matcher[4] + "/" + matcher[5] + "/000" + matcher[7]
		);
	}
}
