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

import type { ErrorType, IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

export enum ErgebnisTyp {
	Hinweis,
	Fehler,
	Info
}

/**
 * Klasse modelliert eine einzelne Fehlermeldung für das ErgebnisElement.
 */
export class ErgebnisElement {
	private _key: string;
	private _typ: ErgebnisTyp;
	private _regelName: string;
	private _fehlerText: Localizable[];
	private _fehlerFeld: IIdentifier;
	private _fehlerTyp: ErrorType;

	constructor(
		key: string,
		typ: ErgebnisTyp,
		regelName: string,
		id: IIdentifier,
		fehlerText: Localizable[],
		fehlerTyp: ErrorType
	) {
		this._key = key;
		this._typ = typ;
		this._regelName = regelName;
		this._fehlerFeld = id;
		this._fehlerText = fehlerText;
		this._fehlerTyp = fehlerTyp;
	}

	public get key(): string {
		return this._key;
	}

	public get typ(): ErgebnisTyp {
		return this._typ;
	}

	public get regelName(): string {
		return this._regelName;
	}

	public get fehlerText(): Localizable[] {
		return this._fehlerText;
	}

	public get fehlerFeld(): IIdentifier {
		return this._fehlerFeld;
	}

	public get ergebnisFehlerTyp(): ErrorType {
		return this._fehlerTyp;
	}
}
