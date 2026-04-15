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


import type { IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
	arraysEqual,
	hashFuncNumberArray,
	stringconv
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { RuntimeUtils } from "../util/RuntimeUtils.js";


/**
 * A simple implementation of the interface [IIdentifier](../../
 * kernel-core-runtime-api-ts/modules/_validation_iidentifier_.iidentifier.html).
 */
export class RtExternalIdentifier implements IIdentifier {
	private _name: string;
	private _indices: number[] = [];
	private _hash: number;

	constructor(idOrName: IIdentifier | string, indices?: number[]) {
		if (typeof idOrName === "string") {
			this._name = idOrName;
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			this._indices = indices!;
		} else {
			this._name = idOrName.getName();
			this._indices = idOrName.getIndices();
		}
		this._hash = this._hashNumber();
	}

	public getName(): string {
		return this._name;
	}

	public getIndices(): number[] {
		return this._indices;
	}

	public getUsb(): number {
		throw new Error("Internal error: RtExternalIdentifier: method getUsb() must not be called!");
	}

	public clone(): RtExternalIdentifier {
		const idx: number[] = this._indices.slice(0);
		const id: RtExternalIdentifier = new RtExternalIdentifier(this._name, idx);
		return id;
	}

	/**
	 * Liefert die Stringrepräsentation dieser Klasse.
	 *
	 * @return 	die Stringrepräsentation dieses Objekts
	 */
	public toString(): string {
		return "/" + this._name.split("/").slice(1).map((pathPart, index) => {
			return pathPart + "[" + String(this._indices[index]) + "]"
		}).join("/");
	}

	public equals(obj: any): boolean {
		// We ignore semantic indices here and in hashCode to ease value collection in ValidationCache.
		if (this === obj) {
			return true;
		}
		if (obj == undefined) {
			return false;
		}
		if (!RuntimeUtils.isIIdentifier(obj)) {
			return false;
		}

		const other: IIdentifier = obj;
		if (this._name == undefined) {
			if (other.getName() != undefined) {
				return false;
			}
		} else if (this._name !== other.getName()) {
			return false;
		}
		if (!arraysEqual(other.getIndices(), this._indices)) {
			return false;
		}
		return true;
	}

	public _hashNumber(): number {
		// We ignore semantic indices here and in equals to ease value collection in ValidationCache.
		const prime = 31;
		let result = 1;
		result = prime * result + hashFuncNumberArray(this._indices);
		result = prime * result + (this._name == null ? 0 : stringconv(this._name));
		return result;
	}

	public hashCode(): number {
		return this._hash;
	}

	public static ensureImpl(id: IIdentifier): RtExternalIdentifier {
		if (id instanceof RtExternalIdentifier) {
			return id;
		}
		return new RtExternalIdentifier(id);
	}
}
