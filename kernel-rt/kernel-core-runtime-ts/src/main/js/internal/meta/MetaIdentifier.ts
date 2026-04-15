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
import {
	arraysEqual,
	hashFuncNumberArray,
	IIdentifier,
	stringconv
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { FeldUtil } from "../util/FeldUtil.js";
import { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";

/**
 * Klasse dient der Repräsentation eines Identifiers für die Metadatenschnittstelle.
 */
export class MetaIdentifier {
	private _name: string;
	private _indices: number[];
	private _u: number;

	private constructor(name: string, idx: number[], u: number) {
		this._name = name;
		this._indices = idx;
		this._u = u;
	}

	public hashCode(): number {
		return (
			(37 * stringconv(this._name.toUpperCase()) + hashFuncNumberArray(this._indices)) * 37 +
			(this._u ^ (this._u >>> 32))
		);
	}

	public equals(other: any): boolean {
		if (this === other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!(other instanceof MetaIdentifier)) {
			return false;
		}

		const o: MetaIdentifier = other;
		return this._name === o._name && arraysEqual(this._indices, o._indices) && this._u === o._u;
	}

	/**
	 * Stringdarstellung analog zu der in parse erwarteten Beschreibung
	 * &lt;name&gt;[u][index1,index2,..index3], wobei [u] optional ist.
	 */
	public toString(): string {
		let result: string = this._name;
		if (this._u !== IIdentifier.ALL_INDICES) {
			result += MetaIdentifier._addBrackets(this._u);
		}
		result += MetaIdentifier._addBracketsByString(
			MetaIdentifier._addBracketsByString(this._indices.toString())
		);
		return result;
	}

	/**
	 * Erzeugt einen {@link MetaIdentifier} aus einer StringDarstellung der Form
	 * &lt;name&gt;[u][index1,index2,..index3], wobei [u] optional ist.
	 */
	public static parse(beschreibung: string): MetaIdentifier {
		const teile: string[] = beschreibung.split("[");
		const index: string = MetaIdentifier._removeTrailingBracket(teile[teile.length - 1]);
		const u: number =
			teile.length === 2 ? 1 : Number(MetaIdentifier._removeTrailingBracket(teile[1]));
		return new MetaIdentifier(teile[0], FeldUtil.erzeugeLongArray(index), u);
	}

	/**
	 * Gebe den String ohne die hintere eckige Klammer zurück. Die vordere eckige
	 * Klammer ist bereits durch den split-Befehl entfernt worden.
	 */
	private static _removeTrailingBracket(s: string): string {
		return s.substring(0, s.length - 1);
	}

	/**
	 * Füge eckige Klammern hinzu für die String-Darstellung
	 */
	private static _addBrackets(i: number): string {
		return "[" + i + "]";
	}

	/**
	 * Füge eckige Klammern hinzu für die String-Darstellung
	 */
	private static _addBracketsByString(s: string): string {
		return "[" + s + "]";
	}

	/**
	 * Erzeugt einen neuen Identifier mit dem Namen dieses MetaIdentifiers.
	 * Alle Ebenen, die eine Iteration spezifizieren werden mit den konkreten Werten
	 * des übergebenen index initialisiert.
	 *
	 */
	createIdentifier(indexArray: number[]): RtInternalIdentifier {
		const idx: number[] = [];
		for (const index of this._indices) {
			idx.push(index);
		}
		const result: RtInternalIdentifier = new RtInternalIdentifier(this._name, idx);
		for (const i of this.getEbenenMitIteration()) {
			if (i < indexArray.length) {
				idx[i] = indexArray[i];
			}
		}
		return result;
	}

	/**
	 * Liefert die Menge aller Wiederholungsebenen, für die dieser {@link MetaIdentifier}
	 * Iteration spezifiziert
	 */
	getEbenenMitIteration(): Set<number> {
		const result: Set<number> = new Set<number>();
		for (let i = 0; i < this._indices.length; i++) {
			if (this._isIteration(i)) {
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * Gibt true zurück, wenn die Ebene i eine Iteration spezifiziert
	 */
	private _isIteration(i: number): boolean {
		return this._indices[i] === IIdentifier.ITERATION;
	}
}
