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
import { IIdentifier, NumberSubArraySet } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { RtInternalIdentifier } from "./RtInternalIdentifier.js";

/**
 * Hilfsklasse zur Optimierung der Kontextiteration.<br>
 * Die Optimierung, dass pro Wiederholungsebene bis zum maximalen Index iteriert wird, ist nicht
 * ausreichend, vgl. EXT-8419.<br>
 * Zu jeder "Iterationstiefe" werden alle Index-Arrays gespeichert, für die ein Feld
 * angegeben ist. <br>
 * <b>Beispiel:</b><br>
 * Feld /A/k1/k2/F mit Indices [7,5,3,1] ist angegeben.<br>
 * Dann wird
 * <ul>
 * <li>für die Ebene 0 das Array [7] gespeichert</li>
 * <li>für die Ebene 1 das Array [7, 5]</li>
 * <li>etc.</li>
 * </ul>
 *
 */
export class GesetzteWiederholungen {
	/**
	 * Alle gesetzten Indices werden pro Ebene (= Verschachtelungstiefe) gespeichert,
	 * Position in der Liste entspricht der Verschachtelungstiefe.
	 */
	private _gesetzteIndicesProEbene: NumberSubArraySet[] = [];

	private _addedSemanticIndices: Set<number> = new Set<number>();

	/**
	 * Checks whether there is any field instance whose indices match the given indices up to the given level.
	 * 
	 * If a semantic index is used on any of the repetition levels, the repetitions for that layer
	 * are combined (see {@link _addSemanticIndexValues} for an example). The combination is
	 * cached for further use.
	 * 
	 * **Example:** If the field /A/k1/k2/F with indices=[7, 2, 3, 1] is set, the following repetitions are set:
	 * <ul>
	 * <li>[7]</li>
	 * <li>[7, 2]</li>
	 * <li>[7, 2, 3]</li>
	 * <li>[7, 2, 3, 1]</li>
	 * </ul>
	 *
	 * @param indices the indices to query.
	 * @param level the level up to which the indices should be checked (inclusive)
	 * @param semanticIndexLevel 	the semantic index level.
	 * 								<code>-1</code> if no semantic index is used.
	 */
	public istWiederholungGesetzt(indices: number[], level: number, semanticIndexLevel?: number): boolean {
		if (this._gesetzteIndicesProEbene.length <= level) {
			return false;
		}

		if (semanticIndexLevel !== undefined && semanticIndexLevel >= 0 && semanticIndexLevel < level) {
			indices = indices.slice(0, level + 1);
			indices[semanticIndexLevel] = IIdentifier.SEMANTIC_INDEX;

			if (!this._addedSemanticIndices.has(semanticIndexLevel)) {
				this._addSemanticIndexValues(semanticIndexLevel);
				this._addedSemanticIndices.add(semanticIndexLevel);
			}
		}
		// We can query the complete indices array because equals and hashCode of the set regard
		// only the first i elements (see NumberSubArraySet)
		return this._gesetzteIndicesProEbene[level].has(indices);
	}

	/**
	 * Add additional values to the indices merging all sub levels for one level for semantic
	 * indices.
	 *
	 * <b>Example:</b> If the field /A/k1/k2/F, with indices=[7, 2, 3, 1] is given and
	 * the semanticIndexLevel is 2, then following repetitions are added (c.f.
	 * {@link istWiederholungGesetzt}):
	 * <ul>
	 * <li>[7,-4, 3]</li>
	 * <li>[7,-4, 3, 1]</li>
	 * </ul>
	 *
	 * @param semanticIndexLevel 	the level, on which the semantic index occurs in the array.
	 */
	private _addSemanticIndexValues(semanticIndexLevel: number): void {
		for (let i = semanticIndexLevel; i < this._gesetzteIndicesProEbene.length; i++) {
			const indicesToAdd: number[][] = [];
			for (const indices of this._gesetzteIndicesProEbene[i]) {
				const newIndices = indices.slice(0);
				newIndices[semanticIndexLevel] = IIdentifier.SEMANTIC_INDEX;
				indicesToAdd.push(newIndices);
			}
			for (const indices of indicesToAdd) {
				this._gesetzteIndicesProEbene[i].add(indices);
			}
		}
	}

	public update(id: RtInternalIdentifier): void {
		const gesetzteIndicesProEbene = this._gesetzteIndicesProEbene;
		const indices = id.getIndices();
		for (let i = 0; i < indices.length; i++) {
			if (gesetzteIndicesProEbene.length <= i) {
				gesetzteIndicesProEbene[i] = new NumberSubArraySet(i);
			}
			// We can add the complete indices array because equals and hashCode of the set regard
			// only the first i elements (see NumberSubArraySet)
			gesetzteIndicesProEbene[i].add(indices);
		}
	}
}
