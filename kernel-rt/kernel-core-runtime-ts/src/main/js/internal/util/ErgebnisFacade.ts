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

import type { ErrorType, IIdentifier, IResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { ErgebnisElement, ErgebnisTyp } from "./ErgebnisElement.js";

/**
 * Implementation of [IResult](../../kernel-core-runtime-api-ts/modules/_validation_iresult_.iresult.html).
 * Referenced fields, referenced fields which cause omission errors and unknown fields are not cached.
 */
export class ErgebnisFacade implements IResult {
	elemente: ErgebnisElement[] = [];

	public getCountOf(typ: ErrorType): number {
		if (typ == undefined) {
			return this.elemente.length;
		}
		let result = 0;
		for (const pe of this.elemente) {
			if (pe.ergebnisFehlerTyp === typ) {
				result++;
			}
		}
		return result;
	}

	public get elements(): ErgebnisElement[] {
		return this.elemente;
	}

	public addInfo(
		ruleName: string,
		infoCode: string,
		infoField: IIdentifier,
		infoMessage: Localizable[],
		type: ErrorType,
		// signature should be the same as in java
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		referencedFields?: Set<IIdentifier>,
		// signature should be the same as in java
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		refOmissionErrorResponsible?: Set<IIdentifier>,
		// signature should be the same as in java
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		unknowns?: Set<IIdentifier>
	): void {
		const pe: ErgebnisElement = new ErgebnisElement(
			infoCode,
			ErgebnisTyp.Info,
			ruleName,
			infoField,
			infoMessage,
			type
		);
		this.elemente.push(pe);
	}

	public addHint(
		ruleName: string,
		hintCode: string,
		hintField: IIdentifier,
		hintMessage: Localizable[],
		type: ErrorType,
		// signature should be the same as in java
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		referencedFields?: Set<IIdentifier>,
		// signature should be the same as in java
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		refOmissionErrorResponsible?: Set<IIdentifier>,
		// signature should be the same as in java
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		unknowns?: Set<IIdentifier>
	): void {
		const pe: ErgebnisElement = new ErgebnisElement(
			hintCode,
			ErgebnisTyp.Hinweis,
			ruleName,
			hintField,
			hintMessage,
			type
		);
		this.elemente.push(pe);
	}

	public addError(
		ruleName: string,
		errorCode: string,
		errorField: IIdentifier,
		errorMessage: Localizable[],
		type: ErrorType,
		// signature should be the same as in java
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		referencedFields?: Set<IIdentifier>,
		// signature should be the same as in java
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		refOmissionErrorResponsible?: Set<IIdentifier>,
		// signature should be the same as in java
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		unknowns?: Set<IIdentifier>
	): void {
		const pe: ErgebnisElement = new ErgebnisElement(
			errorCode,
			ErgebnisTyp.Fehler,
			ruleName,
			errorField,
			errorMessage,
			type
		);
		this.elemente.push(pe);
	}
}
