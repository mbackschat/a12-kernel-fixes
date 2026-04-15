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
	ICustomFieldTypeValidationParam
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { ICustomFieldValidator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { IFadat } from "../../IFadat.js";

import { CustomFieldTypeErrorEnum } from "../error/CustomFieldTypeErrorEnum.js";
import { CustomFieldTypeErrorKeys } from "../error/CustomFieldTypeErrorKeys.js";
import { CustomFieldTypeErrorUtil } from "../error/CustomFieldTypeErrorUtil.js";

import { SteuernummerVerifierImpl } from "./SteuernummerVerifierImpl.js";

/**
 * Checks the Bundessteuernummer.
 *
 * The validation algorithm for the Bundessteuernummer is largely identical to the algorithm for the
 * Steuernummern in Bavaria. To use the implementation of the Steuernummer check the string "@91" is
 * appended to the Bundessteuernummer. This suffix is used to use the corresponding validation check
 * of the Bavarian Steuernummer.
 * <p>
 * The validation of the BZSt-Steuernummer in Elster format consists of the following parts:<br>
 * <ul>
 * <li>Is the BZSt-Steuernummer formally valid?</li>
 * <li>Does the validation calculate the proper verification code?</li>
 * </ul>
 * </p>
 * <p>
 * At the formal validation the BZSt-Steuernummer has to match the following pattern:
 * <code>91160[0-9]{8}</code>
 * </p>
 * <p>
 * Additionally, the BZSt-Steuernummer will be validated by a verification code check.
 * </p>
 */
export class BundessteuernummerVerifierImpl extends SteuernummerVerifierImpl implements ICustomFieldValidator {
	// Formale Prüfung: 13-stellige Zahl, die mit den Ziffern 91160 beginnt.
	private static readonly _VALID_BUSTNR = /^(91)(16)0[0-9]{8}$/;

	public constructor(fadat: IFadat | undefined) {
		super(fadat);
	}

	/**
	 * Checks the provided Bundessteuernummer.<br>
	 * As additional parameter the defining data of the
	 * Steuernummerprüfung is needed. If none are provided then an {@link !Error Error} is thrown.
	 */
	validate(
		value: string,
		valParam: ICustomFieldTypeValidationParam,
		isDisplayValue: boolean
	): ICustomFieldTypeCheckError | undefined {
		const result: ICustomFieldTypeCheckError | undefined = super.validate(
			value,
			valParam,
			isDisplayValue
		);
		return this._checkResult(result);
	}

	private _checkResult(result: ICustomFieldTypeCheckError | undefined): ICustomFieldTypeCheckError | undefined {
		if (result != undefined) {
			return CustomFieldTypeErrorUtil.getResult(
				CustomFieldTypeErrorEnum.BUNDESSTNR_UNGUELTIG,
				CustomFieldTypeErrorKeys.UNGUELTIGE_BUSTNR
			);
		}
		return result;
	}

	getMatcher(xmlSteuernummer: string): RegExpExecArray {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return BundessteuernummerVerifierImpl._VALID_BUSTNR.exec(xmlSteuernummer)!;
	}

	istBundesfinanzamtsnummerGueltig(_bundesfinanzamtsnummer: number): boolean {
		return true;
	}
}
