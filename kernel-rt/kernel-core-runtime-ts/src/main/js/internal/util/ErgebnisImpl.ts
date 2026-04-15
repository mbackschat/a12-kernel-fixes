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

/**
 * Hilfsklasse für ein Ergebnis. Wird zum Validieren benötigt, bevor ein Wert von der
 * Oberflächendarstellung in die XML Darstellung konvertiert werden kann.
 */
export class ErgebnisImpl implements IResult {
	private _fehlerText: Localizable[] | undefined;

	public addInfo(
		ruleName: string,
		infoCode: string,
		infoField: IIdentifier,
		infoMessage: Localizable[],
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		type: ErrorType,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		referencedFields?: Set<IIdentifier>,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		refOmissionErrorResponsible?: Set<IIdentifier>,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		unknowns?: Set<IIdentifier>
	): void {
		this._fehlerText = infoMessage;
	}

	public addHint(
		ruleName: string,
		hintCode: string,
		hintField: IIdentifier,
		hintMessage: Localizable[],
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		type: ErrorType,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		referencedFields?: Set<IIdentifier>,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		refOmissionErrorResponsible?: Set<IIdentifier>,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		unknowns?: Set<IIdentifier>
	): void {
		this._fehlerText = hintMessage;
	}

	public addError(
		ruleName: string,
		errorCode: string,
		errorField: IIdentifier,
		errorMessage: Localizable[],
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		type: ErrorType,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		referencedFields?: Set<IIdentifier>,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		refOmissionErrorResponsible?: Set<IIdentifier>,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		unknowns?: Set<IIdentifier>
	): void {
		this._fehlerText = errorMessage;
	}

	// function signature should be the same as in java
	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	public getCountOf(typ: ErrorType): number {
		return this.hasError() ? 1 : 0;
	}

	public getAnzahlFehler(dummy: string, typ: ErrorType): number {
		return this.getCountOf(typ);
	}

	public hasError(): boolean {
		return this._fehlerText != undefined;
	}

	public get fehlerText(): Localizable[] | undefined {
		return this._fehlerText;
	}

	public set fehlerText(fehlerText: Localizable[] | undefined) {
		this._fehlerText = fehlerText;
	}
}
