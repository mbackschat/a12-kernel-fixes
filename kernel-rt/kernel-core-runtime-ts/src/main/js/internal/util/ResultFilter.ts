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

export type MessageFilter = (ruleName: string, errorCode: string, errorField: IIdentifier, errorType: ErrorType) => boolean;

export class ResultFilter implements IResult {
	constructor(
		private readonly _wrappedResult: IResult,
		private readonly _messageFilter: MessageFilter,
	) { }

	addInfo(
		ruleName: string,
		infoCode: string,
		infoField: IIdentifier,
		infoMessage: Localizable[],
		type: ErrorType,
		referencedFields: Set<IIdentifier>,
		refOmissionErrorResponsible: Set<IIdentifier>,
		unknowns: Set<IIdentifier>
	): void {
		if (this._messageFilter(ruleName, infoCode, infoField, type)) {
			this._wrappedResult.addInfo(
				ruleName,
				infoCode,
				infoField,
				infoMessage,
				type,
				referencedFields,
				refOmissionErrorResponsible,
				unknowns
			);
		}
	}

	addHint(
		ruleName: string,
		hintCode: string,
		hintField: IIdentifier,
		hintMessage: Localizable[],
		type: ErrorType,
		referencedFields: Set<IIdentifier>,
		refOmissionErrorResponsible: Set<IIdentifier>,
		unknowns: Set<IIdentifier>
	): void {
		if (this._messageFilter(ruleName, hintCode, hintField, type)) {
			this._wrappedResult.addHint(
				ruleName,
				hintCode,
				hintField,
				hintMessage,
				type,
				referencedFields,
				refOmissionErrorResponsible,
				unknowns
			);
		}
	}

	addError(
		ruleName: string,
		errorCode: string,
		errorField: IIdentifier,
		errorMessage: Localizable[],
		type: ErrorType,
		referencedFields: Set<IIdentifier>,
		refOmissionErrorResponsible: Set<IIdentifier>,
		unknowns: Set<IIdentifier>
	): void {
		if (this._messageFilter(ruleName, errorCode, errorField, type)) {
			this._wrappedResult.addError(
				ruleName,
				errorCode,
				errorField,
				errorMessage,
				type,
				referencedFields,
				refOmissionErrorResponsible,
				unknowns
			);
		}
	}

	getCountOf(type: ErrorType): number {
		return this._wrappedResult.getCountOf(type);
	}
}
