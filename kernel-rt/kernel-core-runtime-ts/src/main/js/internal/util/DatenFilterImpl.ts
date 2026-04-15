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
import type { IData, IIdentifier, IIterator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { FilteringIterator } from "./FilteringIterator.js";
import type { Predicate } from "./Predicate.js";

/**
 * Liefert eine Implementierung des Interfaces [IData](
 * ../../../kernel-core-runtime-api-ts/modules/_validation_idata_.idata.html),
 * das aus einer vorgegebenen Instanz Felder filtern kann.
 */
export class DatenFilterImpl implements IData {
	private _originalData: IData;
	private _filter: Predicate<IIdentifier>;

	/**
	 * @param origin 	die zu wrappende Instanz.
	 * @param filter 	ein Filter, der angibt, ob ein konkreter IIdentifier zurückgeliefert werden soll.
	 */
	constructor(origin: IData, filter: Predicate<IIdentifier>) {
		this._originalData = origin;
		this._filter = filter;
	}

	public getValue(entity: IIdentifier): string | undefined {
		if (!this._filter.test(entity)) {
			return undefined;
		}
		return this._originalData.getValue(entity);
	}

	public iterator(): IIterator<IIdentifier> {
		return new FilteringIterator<IIdentifier>(this._originalData.iterator(), this._filter);
	}

	public addValue(feld: IIdentifier, wert: string): void {
		throw new Error(
			"Unsupported operation in DatenFilterImpl for field [" + feld + "] and value [" + wert + "]."
		);
	}
}
