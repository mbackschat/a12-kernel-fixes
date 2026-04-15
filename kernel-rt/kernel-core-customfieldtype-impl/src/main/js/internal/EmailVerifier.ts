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
	ICustomFieldTypeValidationParam
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { customFieldTypeConversionResult, type ICustomFieldValidator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import { CustomFieldTypeErrorEnum } from "./error/CustomFieldTypeErrorEnum.js";
import { CustomFieldTypeErrorKeys } from "./error/CustomFieldTypeErrorKeys.js";
import { CustomFieldTypeErrorUtil } from "./error/CustomFieldTypeErrorUtil.js";
import { EmailVerifierUtils } from "./utils/EmailVerifierUtils.js";

/**
 * Prüft die Email-Adresse.
 * Als regulärer Ausdruck wird zur Prüfung folgender Ausdruck verwendet:
 * <code>[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@([a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)(?:\\.([a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?))*</code>
 */
export class EmailVerifier implements ICustomFieldValidator {
	private static readonly _INSTANCE: EmailVerifier = new EmailVerifier();
	private static readonly RE_VALID_EMAIL = new RegExp(
		"[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@([a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)(?:\\.([a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?))*"
	);
	private legalTlds: Set<string>;

	private constructor() {
		this.legalTlds = new EmailVerifierUtils().getLegalTlds();
	}

	public static getInstance(): EmailVerifier {
		return EmailVerifier._INSTANCE;
	}

	validate(
		email: string,
		valParam: ICustomFieldTypeValidationParam,
		_isDisplayValue = false
	): ICustomFieldTypeCheckError | undefined {
		if (this._isValid(email)) {
			return this._checkLaenge(email, valParam);
		}
		return CustomFieldTypeErrorUtil.getResult(
			CustomFieldTypeErrorEnum.EMAIL_UNGUELTIG,
			CustomFieldTypeErrorKeys.UNGUELTIGE_EMAIL
		);
	}

	/**
	 *
	 * @param email
	 * @return true, falls die EMail-Adresse valide ist
	 */
	private _isValid(email: string): boolean {
		if (!EmailVerifier.RE_VALID_EMAIL.test(email)) {
			return false;
		}
		let _tld: string = email.replace(EmailVerifier.RE_VALID_EMAIL, "$2");
		if (!_tld || _tld.length === 0) {
			// there is no sub-domain, so group 1 contains the TLD
			_tld = email.replace(EmailVerifier.RE_VALID_EMAIL, "$1");
		}
		return this.legalTlds.has(_tld.toLowerCase());
	}

	private _checkLaenge(
		email: string,
		valParam: ICustomFieldTypeValidationParam
	): ICustomFieldTypeCheckError | undefined {
		const _minLaenge: number | undefined = valParam.getMinLength();
		if (_minLaenge && email.length < _minLaenge) {
			const args = {
				minLength: CustomFieldTypeErrorUtil.createPlainPlaceholder(_minLaenge.toString())
			}
			return CustomFieldTypeErrorUtil.getResult(
				CustomFieldTypeErrorEnum.STRING_ZU_KURZ,
				CustomFieldTypeErrorKeys.EMAIL_ZU_KURZ,
				args
			);
		}

		const _maxLaenge: number | undefined = valParam.getMaxLength();
		if (_maxLaenge && email.length > _maxLaenge) {
			const args = {
				maxLength: CustomFieldTypeErrorUtil.createPlainPlaceholder(_maxLaenge.toString())
			}
			return CustomFieldTypeErrorUtil.getResult(
				CustomFieldTypeErrorEnum.STRING_ZU_LANG,
				CustomFieldTypeErrorKeys.EMAIL_ZU_LANG,
				args
			);
		}

		return undefined;
	}

	convertDisplay2Internal(displayValue: string): ICustomFieldTypeConversionResult {
		return customFieldTypeConversionResult(displayValue);
	}

	convertInternal2Display(internalValue: string): ICustomFieldTypeConversionResult {
		return customFieldTypeConversionResult(internalValue);
	}
}
