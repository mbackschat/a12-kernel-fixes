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
import type { ICustomFieldTypeCheckError, ICustomFieldTypeConversionResult, ICustomFieldTypeValidationParam } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { customFieldTypeConversionResult, type ICustomFieldValidator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import { CustomFieldTypeErrorEnum } from "./internal/error/CustomFieldTypeErrorEnum.js";
import { CustomFieldTypeErrorKeys } from "./internal/error/CustomFieldTypeErrorKeys.js";
import { CustomFieldTypeErrorUtil } from "./internal/error/CustomFieldTypeErrorUtil.js";

/**
 * Checks the ID number. From January 1st, 2008, the Federal Ministry of Finance introduced a uniform ID number
 * (identification number) according to § 139 AO. This identification feature is intended to replace the old tax number and is
 * unique for every taxable citizen.
 * <p>
 * <b>Structure of the ID number:</b><br>
 * The identifier is an eleven-digit sequence of numbers, the eleventh digit is a check digit.
 * In addition, it must also meet the following criteria:
 * <ul>
 * <li>No leading zeros allowed unless it is a test identification number (only allowed with valid test flag).</li>
 * <li>Positions 2 to 10 consist of numbers between 0 and 9.</li>
 * <li>The eleventh digit is a check digit and is calculated from digits 1 - 10 according to the check algorithm specified below.</li>
 * <li>In the first 10 digits of the identification number, exactly 1 digit must appear twice, the position of the repetition
 * is arbitrary. From January 1st, 2016 (implemented in ER-23), this one digit can also appear three times instead of just twice.
 * The digits of the remaining 7 digits must not be repeated, analogous to double occurrences.</li>
 * </ul>
 * </p>
 * <p>
 * To distinguish between the previous tax number and the identification number, no identification number with the leading
 * digits 0, 1, 2 or 2 will be assigned for a certain transitional period.
 * </p>
 * <b>Examples:</b>
 * <table style="border: 1px solid black; border-collapse: separate; border-spacing: 0.2em;">
 * <tr>
 * <th style="border: 1px solid black; padding: 0.2em 0.5em;">ID number without a check digit</th>
 * <th style="border: 1px solid black; padding: 0.2em 0.5em;">check digit</th>
 * <th style="border: 1px solid black; padding: 0.2em 0.5em;">Double/triple digit</th>
 * <th style="border: 1px solid black; padding: 0.2em 0.5em;">Simple digits</th>
 * </tr>
 * <tr>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">4123450896</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">7</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">4</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">1,2,3,5,0,8,9,6</td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">5549267083</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">6</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">5</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">4,9,2,6,7,0,8,3</td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">0247629135(TestID)</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">8</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">2</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">0,4,7,6,9,1,3,5</td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">6195362647</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">7</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">6 (triple)</td>
 * <td style="border: 1px solid black; padding: 0.2em 0.5em;">1,9,5,3,2,4,7</td>
 * </tr>
 * </table>
 */
export class IdNrVerifier implements ICustomFieldValidator {
	private static readonly _RE_VALID_IDNR = /\d{11}/;

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static readonly _CONVERSION_ERROR_IDENTIFIER: string = "CONVERSION_ERROR";

	private constructor(
		private _acceptTestIdNrs: boolean,
	) {
	}

	/**
	 * @param acceptTestIdNrs
	 *        {@code true} ensures that a German ID number starting with "0" is considered valid. This is only
	 *        appropriate for testing purposes. {@code false} ensures that such test numbers are not recognized as valid,
	 *        which is the standard behavior.
	 * @return the instance of {@link ICustomFieldValidator}.
	 */
	public static create(acceptTestIdNrs: boolean): ICustomFieldValidator {
		return new IdNrVerifier(acceptTestIdNrs);
	}

	validate(
		idnr: string,
		valParam: ICustomFieldTypeValidationParam,
		isDisplayValue: boolean
	): ICustomFieldTypeCheckError | undefined {
		if (isDisplayValue) {//der Wert muss vor der Validierung in XML-Format konvertiert werden
			const convertResult: ICustomFieldTypeConversionResult = this.convertDisplay2Internal(idnr);

			if (convertResult.getErrorMessage()) {//Fehler bei der Konvertierung
				return CustomFieldTypeErrorUtil.getResult(CustomFieldTypeErrorEnum.IDNR_LEERZEICHEN_UNGUELTIG, CustomFieldTypeErrorKeys.UNGUELTIGE_ID_NUMMER);
			}
			//nehmen wir die XML-Darstellung
			idnr = convertResult.getConvertedValue();
		}
		const _error = this._checkIdNr(idnr);
		return CustomFieldTypeErrorUtil.getResult(_error, CustomFieldTypeErrorKeys.UNGUELTIGE_ID_NUMMER);
	}

	/**
	 * Prüft die angegebene IdNr und liefert den Key der passenden Fehlermeldung bzw.
	 * <code>null</code>, wenn die IdNr gültig ist.
	 */
	private _checkIdNr(idnr: string): CustomFieldTypeErrorEnum {
		if (idnr.length !== 11) {
			return CustomFieldTypeErrorEnum.IDNR_LAENGE_UNGUELTIG;
		}
		if (!IdNrVerifier._RE_VALID_IDNR.test(idnr)) {
			return CustomFieldTypeErrorEnum.IDNR_NICHT_NUMERISCH;
		}
		if (!this._checkZiffernfolge(idnr)) {
			return CustomFieldTypeErrorEnum.IDNR_ZIFFERNFOLGE_FEHLER;
		}
		if (!this._checkKorrektePruefziffer(idnr)) {
			return CustomFieldTypeErrorEnum.IDNR_PRUEFZIFFER_UNGUELTIG;
		}
		if (idnr.startsWith("0") && !this._acceptTestIdNrs) {
			return CustomFieldTypeErrorEnum.IDNR_NUR_FUER_TEST;
		}

		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	private _checkKorrektePruefziffer(idnr: string): boolean {
		const _ten = 10;
		const _eleven = 11;

		const _idnrChars = Array.from(idnr);

		let _remainderModTen = 0;
		let _remainderModEleven = _ten;
		let _digit = 0;
		for (let _counter = 0; _counter < 10; _counter++) {
			_digit = Number(_idnrChars[_counter]);
			_remainderModTen = (_digit + _remainderModEleven) % _ten;
			if (_remainderModTen === 0) {
				_remainderModTen = _ten;
			}
			_remainderModEleven = (2 * _remainderModTen) % _eleven;
		}

		let _checkCipher = _eleven - _remainderModEleven;
		if (_checkCipher === 10) {
			_checkCipher = 0;
		}

		return _checkCipher === Number(_idnrChars[10]);
	}

	private _checkZiffernfolge(idnr: string): boolean {
		const _w = Array.from(idnr);
		// Die Wiederholungen der Ziffern
		const _wiederholungen: number[] = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0];

		// 11. Position ist Prüfziffer und wird nicht geprüft
		for (let _i = 0; _i < 10; _i++) {
			const _ziffer = Number(_w[_i]);
			_wiederholungen[_ziffer]++;
		}

		const _notSet = -1;
		let _wiederholteZiffer = _notSet;
		for (let _i = 0; _i < 10; _i++) {
			if (_wiederholungen[_i] < 2) {
				continue;
			}
			if (_wiederholungen[_i] > 3) {
				// Nur maximal dreifache Wiederholung erlaubt
				return false;
			}
			if (_wiederholteZiffer !== _notSet) {
				// Nur eine Ziffer darf zweimal oder dreimal vorkommen
				return false;
			} else {
				_wiederholteZiffer = _i;
			}
		}
		if (_wiederholteZiffer === _notSet) {
			// Keine wiederholte Ziffer.
			return false;
		}

		const _idnrOhnePruefziffer = idnr.substring(0, idnr.length - 1);
		if (
			_idnrOhnePruefziffer.indexOf(
				[_wiederholteZiffer, _wiederholteZiffer, _wiederholteZiffer].join("")
			) !== -1
		) {
			// Existieren drei gleiche Ziffern an den Positionen 1 bis 10,
			// dürfen diese gleichen Ziffern niemals an direkt aufeinander folgenden Stellen stehen
			// (2 gleiche Ziffern dürfen allerdings direkt aufeinander folgen).
			return false;
		}

		return true;
	}

	convertDisplay2Internal(displayValue: string): ICustomFieldTypeConversionResult {
		let xmlValue = "";

		if (displayValue.indexOf(' ') != -1) {
			for (let i = 0; i < displayValue.length; i++) {
				if (i == 2 || i == 6 || i == 10) {
					if (displayValue.charAt(i) != ' ') {
						return customFieldTypeConversionResult(displayValue, IdNrVerifier._CONVERSION_ERROR_IDENTIFIER);
					}
				} else {
					if (displayValue.charAt(i) == ' ') {
						return customFieldTypeConversionResult(displayValue, IdNrVerifier._CONVERSION_ERROR_IDENTIFIER);
					}
					xmlValue += displayValue.charAt(i);
				}
			}
		} else {
			xmlValue = displayValue;
		}
		return customFieldTypeConversionResult(xmlValue, undefined);
	}

	convertInternal2Display(internalValue: string): ICustomFieldTypeConversionResult {
		const error = this._checkIdNr(internalValue);
		if (error !== CustomFieldTypeErrorEnum.KEIN_FEHLER && error != CustomFieldTypeErrorEnum.IDNR_NUR_FUER_TEST) {
			// can not be null because it is not KEIN_FEHLER
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			const checkResult = CustomFieldTypeErrorUtil.getResult(error, CustomFieldTypeErrorKeys.UNGUELTIGE_ID_NUMMER)!;
			return customFieldTypeConversionResult(internalValue,
				"Konvertiere nur gültige Identifikationsnummern. " + checkResult.getErrorMessage());
		}
		return customFieldTypeConversionResult(
			internalValue.substring(0, 2) + ' ' + internalValue.substring(2, 5) + ' ' + internalValue.substring(5, 8) + ' ' + internalValue.substring(8), undefined);
	}
}
