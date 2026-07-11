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

import {FadatLookupUtil} from "./FadatLookupUtil.js";

/**
 * Checks the Bundesfinanzamtsnummer.
 * The validation of the Bundesfinanzamtsnummer consists of the following parts:
 * <ul>
 * <li>Is the Bundesfinanzamtsnummer formally valid?</li>
 * <li>Is there an existing Bundesfinanzamt with this number?</li>
 * </ul>
 * This formal check verifies that the number is a four-digit number.
 * If the Bundesfinanzamtsnummer does not match the regular expression
 * <code>[0-9]{4}</code> it is a formal error.
 */
export class BundesfinanzamtsnummerVerifierImpl implements ICustomFieldValidator {
	private static readonly _VALID_BUFANR = /^[0-9]{4}$/;

	private readonly _fadat: IFadat | undefined;

	public constructor(fadat: IFadat | undefined) {
		this._fadat = fadat;
	}

	/**
	 * Checks the provided Bundesfinanzamtsnummer. <br>
	 * As additional parameter the defining data of the
	 * Steuernummerprüfung is needed. If none are provided then an {@link !Error Error} is thrown.
	 */
	validate(
		value: string,
		_valParam: ICustomFieldTypeValidationParam,
		_isDisplayValue: boolean,
	): ICustomFieldTypeCheckError | undefined {
		const _error = this._checkBufaNr(value);
		return CustomFieldTypeErrorUtil.getResult(
			_error,
			CustomFieldTypeErrorKeys.UNGUELTIGE_BUFANR
		);
	}

	/**
	 * Prüft die angegebene Bundesfinanzamtsnummer und liefert den Key der passenden Fehlermeldung
	 * bzw. <code>null</code>, wenn die Bundesfinanzamtsnummer gültig ist.
	 */
	private _checkBufaNr(bundesfinanzamtsnummer: string): CustomFieldTypeErrorEnum {
		if (!this._istFormalGueltig(bundesfinanzamtsnummer)) {
			return CustomFieldTypeErrorEnum.BUFANR_FORMALER_FEHLER;
		}
		if (!FadatLookupUtil.existsFederalTaxOfficeNumber(this._fadat, Number(bundesfinanzamtsnummer))) {
			return CustomFieldTypeErrorEnum.BUFANR_UNGUELTIGE_FINANZAMTSNR;
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Ist die Bundesfinanzamtsnummer formal gültig?.
	 */
	private _istFormalGueltig(bufaNr: string): boolean {
		return BundesfinanzamtsnummerVerifierImpl._VALID_BUFANR.test(bufaNr);
	}

	convertDisplay2Internal(displayValue: string): ICustomFieldTypeConversionResult {
		return customFieldTypeConversionResult(displayValue);
	}

	convertInternal2Display(internalValue: string): ICustomFieldTypeConversionResult {
		return customFieldTypeConversionResult(internalValue);
	}
}
