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
import type {IFadat} from "../../IFadat.js";

import {CustomFieldTypeErrorEnum} from "../error/CustomFieldTypeErrorEnum.js";
import {CustomFieldTypeErrorKeys} from "../error/CustomFieldTypeErrorKeys.js";

import {FadatLookupUtil} from "./FadatLookupUtil.js";
import {AbstractSteuernummerVerifier} from "./AbstractSteuernummerVerifier.js";
import {SteuernummerValidationHelper} from "./SteuernummerValidationHelper.js";

/**
 * Checks the tax number.
 * The check of the tax number in Elster format consists of the following parts:
 * <ul>
 * <li>Is the tax number formally valid?</li>
 * <li>Does the federal tax office number contained in the tax number exist?</li>
 * <li>Is the district number contained in the tax number unequal to 0?</li>
 * <li>Is no state-specific negative pattern met?</li>
 * <li>Is the district number unequal to 0000, 0998, 0999 for NRW or not equal to 000, 998, 999 otherwise
 * (was implemented as a state-specific negative pattern)?</li>
 * <li>Does the check digit calculation procedure provide the appropriate check digit?</li>
 * </ul>
 */
export class SteuernummerVerifierImpl extends AbstractSteuernummerVerifier {
	private _fadat: IFadat | undefined;

	public constructor(fadat: IFadat | undefined) {
		super(
			new SteuernummerValidationHelper(SteuernummerValidationHelper.VALID_STNR),
			CustomFieldTypeErrorKeys.UNGUELTIGE_STNR
		);
		this._fadat = fadat;
	}

	protected getBufaValidator(): (bufaNr: number) => boolean {
		return (bufaNr) => {
			return FadatLookupUtil.existsFederalTaxOfficeNumber(this._fadat, bufaNr);
		};
	}

	protected mapValidationError(error: CustomFieldTypeErrorEnum): CustomFieldTypeErrorEnum {
		return error; // preserve specific error codes from checkSteuernummer
	}

	protected createValidationError(): CustomFieldTypeErrorEnum {
		return CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER;
	}
}
