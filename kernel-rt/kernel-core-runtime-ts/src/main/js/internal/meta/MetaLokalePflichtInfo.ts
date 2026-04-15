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
	HashSet,
	SortedSet
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import { EbenenIterator } from "../util/EbenenIterator.js";
import type { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";


import type { MetaIdentifier } from "./MetaIdentifier.js";

export const NICHT_RELEVANT = 0;

/**
 * Modelliert die PflichtInformationen
 * Jede PflichtInformation hat eine _additivMenge und eine _pMenge (jeweils Menge von MetaIdentifier)
 * mit folgender Bedeutung:<br>
 * wenn ein Feld aus '_additivMenge' angegeben ist, muss jedes Feld aus '_pMenge' angegeben sein.
 */
export class MetaLokalePflichtInfo {
	private _additivMenge: HashSet<MetaIdentifier> = new HashSet<MetaIdentifier>();
	private _pMenge: HashSet<MetaIdentifier> = new HashSet<MetaIdentifier>();

	private _metaDataValidierung: IMetaDataValidierung;

	/**
	 * Alle Wiederholungsebenen, für die eine Iteration spezifiziert wurde.
	 */
	private _ebenenMitIteration: SortedSet<number> = new SortedSet<number>();

	/**
	 * Erzeugt eine lokale Pflichtfeldinfo mit Additiv a und Pflichtmenge p.
	 */
	public static createLokalePflichtFeldInfo(
		metaDataValidierung: IMetaDataValidierung,
		a: HashSet<MetaIdentifier>,
		p: HashSet<MetaIdentifier>
	): MetaLokalePflichtInfo {
		return new MetaLokalePflichtInfo(metaDataValidierung, a, p);
	}

	/**
	 * Erzeugt das Objekt aus additiv und pflichtmenge 
	 *
	 * @param additiv 	Additiv Menge
	 * @param pflichtmenge 	P-Menge
	 */
	constructor(
		metaDataValidierung: IMetaDataValidierung,
		additiv: HashSet<MetaIdentifier>,
		pflichtmenge: HashSet<MetaIdentifier>
	) {
		this._metaDataValidierung = metaDataValidierung;
		this._additivMenge = additiv;
		this._pMenge = pflichtmenge;

		for (const feld of additiv) {
			for (const num of feld.getEbenenMitIteration()) {
				this._ebenenMitIteration.add(num);
			}
		}

		for (const feld of pflichtmenge) {
			for (const num of feld.getEbenenMitIteration()) {
				this._ebenenMitIteration.add(num);
			}
		}
	}

	/**
	 * Methode zur Optimierung der Iteration bei der Entscheidung, ob diese Pflichtinformation für
	 * die übergebene Menge relevant ist.<br>
	 * Ein Index ist für eine Wiederholungsebene relevant:
	 * <ul>
	 * <li>spezifiziert die Pflichtinfo Iteration für die Ebene, so muss der Index in einem
	 * IIdentifier der übergebenen Menge vorkommen</li>
	 * <li>spezifiziert die Pflichtinfo keine Iteration für die Ebene, so ist nur der Index 1
	 * relevant</li>
	 * </ul>
	 */
	private _getRelevanteEbenen(ids: HashSet<RtInternalIdentifier>): EbenenIterator {
		if (this._ebenenMitIteration.size <= 0) {
			return new EbenenIterator([1], [1]);
		}

		// Der Ebeneniterator betrachtet nur relevante Ebenen,
		// Untersuche nur bis zur höchsten Ebene mit Iteration
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const startIndices: number[] = new Array<number>(this._ebenenMitIteration.maximum()! + 1);
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const endeIndices: number[] = new Array<number>(this._ebenenMitIteration.maximum()! + 1);

		const NOCH_KEIN_WERT_GESETZT = -9;

		// Alle Ebenen ohne Iteration sind 1. Die anderen Ebenen werden im
		// folgenden noch gesetzt.
		for (let i = 0; i < startIndices.length; i++) {
			startIndices[i] = this._ebenenMitIteration.has(i) ? NOCH_KEIN_WERT_GESETZT : 1;
			endeIndices[i] = this._ebenenMitIteration.has(i) ? NOCH_KEIN_WERT_GESETZT : 1;
		}

		// Für jede Ebene, die eine Iteration ist, soll das maximale Iterationsintervall bestimmt werden.
		// Dazu werden alle Idententifier auf diesen Ebenen geprüft.

		ids.forEach(id =>
			this._ebenenMitIteration.forEach(i => {
				if (this._isEbeneRelevant(i, id)) {
					const idx: number = id.getIndices()[i];
					if (startIndices[i] === NOCH_KEIN_WERT_GESETZT) {
						startIndices[i] = idx;
						endeIndices[i] = idx;
					} else {
						startIndices[i] = Math.min(idx, startIndices[i]);
						endeIndices[i] = Math.max(idx, endeIndices[i]);
					}
				}
			})
		);
		return new EbenenIterator(startIndices, endeIndices);
	}

	/**
	 * Eine Ebene ist relevant wenn der Identifier diese Ebene umfasst.
	 *
	 * @param i 	zu prüfende Ebene
	 * @param id Identifier, dessen Index-Array geprüft wird
	 * @return <code>true</code>, falls Ebene relevant ist.
	 */
	private _isEbeneRelevant(i: number, id: RtInternalIdentifier): boolean {
		// nicht relevant,  wenn Identifier die hier betrachtete Ebene gar nicht hat.
		return id.getIndices().length > i;
	}

	/**
	 * Konvertiert die {@link _pMenge} dieser Info in eine Menge von IIdentifiern, wenn
	 * {@link _additivMenge} die übergebene Menge von IIdentifiern enthält, wenn die
	 * Iterationsangabe in {@link _additivMenge} durch konkrete Werte ersetzt werden. <br>
	 * Für die Konvertierung werden in {@link _pMenge} die Iterationsangabe durch die gleichen
	 * konkreten Werte ersetzt.
	 */
	getPMengeWennMetaAdditivUmfasstMenge(
		menge: HashSet<RtInternalIdentifier>
	): HashSet<RtInternalIdentifier> {
		const result: HashSet<RtInternalIdentifier> = new HashSet<RtInternalIdentifier>();
		for (const idx of this._getRelevanteEbenen(menge)) {
			const fsMenge: HashSet<RtInternalIdentifier> = this._createIdMengeFromMetaFSMenge(
				this._additivMenge,
				idx.getIndexes()
			);
			if (this._umfasst(fsMenge, menge)) {
				const pflichtMenge: HashSet<RtInternalIdentifier> = this._createIdMengeFromMetaFSMenge(
					this._pMenge,
					idx.getIndexes()
				);
				for (const id of pflichtMenge) {
					result.add(id);
				}
			}
		}
		return result;
	}

	/**
	 * Überprüft, ob eine Menge von [IIdentifier](../../kernel-core-runtime-api-ts/modules/
	 * _validation_iidentifier_.iidentifier.html) eine andere umfasst unter
	 * Berücksichtigung der Sondersemantik von 0 als Abkürzung für "alle Ebenen".
	 *
	 * @param set1 	Menge von Identifiern
	 * @param set2 	Menge von Identifiern
	 * @return <code>true</code>, wenn die zweite Menge von IIdentifiern eine Teilmenge der ersten
	 *         ist im obigen Sinne
	 */
	private _umfasst(
		set1: HashSet<RtInternalIdentifier>,
		set2: HashSet<RtInternalIdentifier>
	): boolean {
		for (const id2 of set2) {
			let found = false;
			for (const id1 of set1) {
				if (id1.umfasstFuerPflichtangabenPruefung(id2, this._metaDataValidierung)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Liefert zur Menge metaFSMenge von MetaIdentifiern eine Menge von IIdentifiern.
	 * Zu jedem MetaIdentifier wird ein IIdentifier mit dem gleichen Namen erzeugt.<br>
	 * Wiederholungsebenen in einem {@link MetaIdentifier}, für die Iteration spezifiziert
	 * wurde, werden durch den entsprechenden Index aus den übergebenen Indices ersetzt.<br>
	 */
	private _createIdMengeFromMetaFSMenge(
		metaFSMenge: HashSet<MetaIdentifier>,
		indices: number[]
	): HashSet<RtInternalIdentifier> {
		const result: HashSet<RtInternalIdentifier> = new HashSet<RtInternalIdentifier>();
		for (const mf of metaFSMenge) {
			result.add(mf.createIdentifier(indices));
		}
		return result;
	}
}
