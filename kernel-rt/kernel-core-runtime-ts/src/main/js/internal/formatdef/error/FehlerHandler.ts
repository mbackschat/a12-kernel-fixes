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
import type { Localizable } from "@com.mgmtp.a12.utils/utils-localization";

import { defaultResourceProvider } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { IIdentifier, IResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
	ErrorType
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { RuntimeFormalErrorEnum } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { LocalizableDefaults } from "../../util/LocalizableUtil.js";
import { LocalizableUtil } from "../../util/LocalizableUtil.js";
import { ValidType } from "../../util/ValidType.js";

import { IFormaleFehlerConstants } from "../../util/IFormaleFehlerConstants.js";

import type { IValidierungParam } from "../IValidierungParam.js";

export abstract class FehlerHandler {

	public static addFehler(
		errorKey: string,
		fehlerTextWithParam: Localizable[],
		param: IValidierungParam
	): ValidType;
	public static addFehler(
		erg: IResult,
		errorKey: string,
		id: IIdentifier,
		fehlertext: Localizable[],
		nurPruefen: boolean
	): ValidType;
	public static addFehler(
		ergORerrorKey: string | IResult,
		errorKeyORfehlerTextWithParam: string | Localizable[],
		idORparam: IIdentifier | IValidierungParam,
		fehler?: Localizable[],
		pruefen?: boolean
	): ValidType {
		let erg: IResult;
		let errorKey: string;
		let id: IIdentifier;
		let fehlertext: Localizable[];
		let nurPruefen = false;

		if (typeof ergORerrorKey === "string") {
			const param: IValidierungParam = <IValidierungParam>idORparam;
			erg = param.getErgebnis();
			id = param.getIdentifier();
			nurPruefen = param.nurPruefen();
			errorKey = ergORerrorKey;
			const fieldReplacementArg = LocalizableUtil.createFieldReplacementArg(id, param.getMetaData());
			fehlertext = (<Localizable[]>errorKeyORfehlerTextWithParam).map(
				f => LocalizableUtil.addLocalizableArgs(f, fieldReplacementArg)
			);
		} else {
			erg = ergORerrorKey;
			id = <IIdentifier>idORparam;
			errorKey = <string>errorKeyORfehlerTextWithParam;
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			fehlertext = fehler!;
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			nurPruefen = pruefen!;
		}

		if (!nurPruefen) {
			const referencedFields: Set<IIdentifier> = new Set<IIdentifier>();
			referencedFields.add(id);
			erg.addError(
				IFormaleFehlerConstants.FORMALE_PRUEFUNG,
				errorKey,
				id,
				fehlertext,
				ErrorType.VALUE_ERROR,
				referencedFields,
				new Set<IIdentifier>(),
				new Set<IIdentifier>()
			);
		}
		return ValidType.Invalid;
	}

	public static getSingleErrorText(
		fehlerId: RuntimeFormalErrorEnum,
		locale: string
	): string | undefined {
		return defaultResourceProvider.gettext(RuntimeFormalErrorEnum[fehlerId], locale)
	}

	public static getAllErrorTexts(
		fehlerId: RuntimeFormalErrorEnum
	): LocalizableDefaults {
		return this.getAllValues(RuntimeFormalErrorEnum[fehlerId]);
	}

	public static getAllValues(key: string): LocalizableDefaults {
		return defaultResourceProvider.getAllTextsForKey(key);
	}
}
