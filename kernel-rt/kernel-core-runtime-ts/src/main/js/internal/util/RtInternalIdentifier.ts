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


import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import { ArrayUtils } from "../util/ArrayUtils.js";
import { Constants } from "../util/Constants.js";
import type { IterationState } from "../util/IterationState.js";
import { RuntimeUtils } from "../util/RuntimeUtils.js";
import { SemanticIndexSpec } from "../util/SemanticIndexSpec.js";

import { EntityUtil } from "./EntityUtil.js";
import type { RtIdentifierTemplate } from "./RtIdentifierTemplate.js";
import type { RtIdentifierTemplateLayerSpec } from "./RtIdentifierTemplateLayerSpec.js";

/**
 * The internal representation of an [IIdentifier](../../kernel-core-runtime-api-ts/modules/
 * _validation_iidentifier_.iidentifier.html). It does not implement [IIdentifier](../../
 * kernel-core-runtime-api-ts/modules/_validation_iidentifier_.iidentifier.html)
 * to ensure that no internal identifier is given to users of the kernel-core-runtime-api-ts.
 *
 * It is used to handle the data access in the internal {@link ValidationCache} and contains
 * additional information for categories and semantic indexes used to extract specific values.
 *
 */
export class RtInternalIdentifier {
	private _name: string;
	private _indices: number[] = [];

	private _category: string | undefined;
	private _semIndex: SemanticIndexSpec | undefined;
	private _hash: number;

	constructor(
		name: string,
		indices: number[],
		category?: string,
		semIndex?: SemanticIndexSpec
	) {
		this._name = name;
		this._indices = indices;
		this._category = category ? category : undefined;
		this._semIndex = semIndex ? semIndex : undefined;
		this._hash = this._hashNumber();
	}

	public cloneWithSemanticIndexReplaced(
		lvl: number,
		semIdxReplacement: number
	): RtInternalIdentifier {
		const result: RtInternalIdentifier = this.clone();
		result._indices[lvl] = semIdxReplacement;
		result.resetSemanticIndex();
		return result;
	}

	public getSemanticIndex(): SemanticIndexSpec | undefined {
		return this._semIndex;
	}

	public resetSemanticIndex(): void {
		this._semIndex = undefined;
		this._hash = this._hashNumber();
	}

	public resetCategory(): void {
		this._category = undefined;
	}

	public clone(): RtInternalIdentifier {
		const idx: number[] = this._indices.slice(0);
		const result: RtInternalIdentifier = new RtInternalIdentifier(
			this._name,
			idx,
			this._category,
			this._semIndex
		);
		return result;
	}

	/**
	 * Erzeugt ein {@link RtInternalIdentifier} Objekt mit dem Namen &lt;name&gt;, dessen USB das
	 * erste Element aus den Indices aus &lt;idx&gt; ist, die Indices die restlichen Elemente. Die
	 * parallele Iteration wird durch den semantischen Index mit dem Wert aus dem
	 * {@link IterationState} ersetzt.
	 */
	public static createFromIterationState(
		name: string,
		idx: IterationState,
		category?: string,
		semIndex?: SemanticIndexSpec
	): RtInternalIdentifier {
		const indices: number[] = idx.getIndexes();
		const parallelIterationLevel: number = ArrayUtils.indexOf(
			indices,
			IIdentifier.PARALLEL_ITERATION
		);
		let semIdxSpec: SemanticIndexSpec | undefined;
		if (parallelIterationLevel >= 0) {
			indices[parallelIterationLevel] = IIdentifier.SEMANTIC_INDEX;
			semIdxSpec = new SemanticIndexSpec(idx.getValue());
		} else {
			semIdxSpec = undefined;
		}
		return new RtInternalIdentifier(
			name,
			indices,
			category,
			semIndex ? semIndex : semIdxSpec
		);
	}

	/**
	 * Creates an {@link RtInternalIdentifier} instance with information given by a
	 * {@link RtIdentifierTemplate}. The iteration layers (simple iteration, semantic index,
	 * parallel iteration) are replaced by the corresponding iteration state.
	 */
	public static createFromTemplate(
		template: RtIdentifierTemplate,
		iterState: IterationState
	): RtInternalIdentifier {
		const indices: number[] = new Array<number>(template.getLayerSpec().length);
		let hasSemanticIndex = false;

		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const usedIterationState = template.isOuter() ? iterState.getOuterState()! : iterState;
		for (let i = 0; i < indices.length; i++) {
			const layerSpec = template.getSingleLayerSpec(i);
			if (layerSpec.getIdxEnd() !== layerSpec.getIdxStart()) {
				throw new Error("Internal error: the method must not be called for repeatable templates!");
			}
			indices[i] = RtInternalIdentifier._calculateIdxFromLayerSpec(layerSpec, () =>
				usedIterationState.getIndex(i)
			);
			if (indices[i] === IIdentifier.SEMANTIC_INDEX) {
				hasSemanticIndex = true;
			}
		}
		let semIdx = template.getSemanticIndex().createSemanticIndex(usedIterationState);
		// If we have parallel iteration, there is no spec for semantic index
		if (hasSemanticIndex && !semIdx) {
			semIdx = new SemanticIndexSpec(usedIterationState.getValue());
		}
		return new RtInternalIdentifier(
			template.getUniqueName(),
			indices,
			template.getCategory(),
			semIdx
		);
	}

	private static _calculateIdxFromLayerSpec(
		layerSpec: RtIdentifierTemplateLayerSpec,
		outerIdx: () => number
	): number {
		if (layerSpec.getIdxStart() === IIdentifier.PARALLEL_ITERATION) {
			return IIdentifier.SEMANTIC_INDEX;
		} else if (layerSpec.getIdxStart() === IIdentifier.ITERATION) {
			return outerIdx();
		} else {
			return layerSpec.getIdxStart();
		}
	}

	/**
	 * This implementation is important since the access to a map in typescript is done with this
	 * string representation.
	 *
	 * @return
	 * 		the string representation of this object.
	 */
	toString(): string {
		if (!this._name.includes("/")) {
			// Intern identifier with unique name
			const sb1: string[] = [this._name];
			for (const i of this._indices) {
				sb1.push(Constants.FIELD_LAYER_SEPARATOR);
				sb1.push(String(i));
			}
			if (this._category) {
				sb1.push(Constants.CATEGORY_VALUE_ACCESS);
				sb1.push(this._category);
			}
			if (this._semIndex) {
				sb1.push(Constants.SEMANTIC_INDEX_SEPARATOR);
				sb1.push(this._semIndex.toString());
			}
			return sb1.join();
		}

		const teile: string[] = this._name.split("/");
		const sb2: string[] = [];
		for (let i = 1; i < teile.length; i++) {
			sb2.push("/" + teile[i] + "[" + String(this._indices[i - 1]) + "]");
		}
		if (this._semIndex) {
			sb2.push(" für ");
			sb2.push(this._semIndex.toString());
		}
		return sb2.join();
	}

	private _hashNumber(): number {
		const prime = 31;
		let result = 1;
		result = prime * result + hashFuncNumberArray(this._indices);
		result = prime * result + (this._name == null ? 0 : stringconv(this._name));

		const semIdxHashCode: number =
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			this.getSemanticIndex() === undefined ? 0 : this.getSemanticIndex()!.hashCode();
		result = result + prime * semIdxHashCode;
		return result;
	}

	public hashCode(): number {
		return this._hash;
	}

	public equals(obj: any): boolean {
		if (this === obj) {
			return true;
		}
		if (obj === undefined) {
			return false;
		}
		if (!RuntimeUtils.isIIdentifier(obj)) {
			return false;
		}

		const other: RtInternalIdentifier = <RtInternalIdentifier>obj;
		if (other._hash !== this._hash) {
			return false;
		}
		if (this._name === undefined) {
			if (other._name != undefined) {
				return false;
			}
		} else if (this._name !== other._name) {
			return false;
		}

		if (!arraysEqual(other._indices, this._indices)) {
			return false;
		}

		return this.getSemanticIndex()
			? // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			this.getSemanticIndex()!.equals(other.getSemanticIndex())
			: other.getSemanticIndex() == null;
	}

	getName(): string {
		return this._name;
	}

	setName(name: string) {
		this._name = name;
		this._hash = this._hashNumber();
	}

	getIndices(): number[] {
		return this._indices;
	}

	setIndices(indices: number[]): void {
		this._indices = indices;
		this._hash = this._hashNumber();
	}

	getUsb(): number {
		throw new Error("Internal error: RtInternalIdentifier: method getUsb() must not be called!");
	}

	getCategory(): string | undefined {
		return this._category;
	}

	/**
	 * <p>
	 * Diese Methode wird nur für die Pflichtfeld-Ermittlung aufgerufen.
	 * Die Methode muss auch berücksichtigen, dass - für nicht echt wiederholbare Kontexte -
	 * eine Spezifikation 1 eine Spezifikation mit Wildcard ([IIdentifier.ALL_INDICES](../../
	 * kernel-core-runtime-api-ts/modules/_validation_iidentifier_.iidentifier.html#all_indices)) umfasst.
	 * </p>
	 * <p>
	 * Ist die Behandlung von diesem Spezialfall nicht notwendig, soll die sehr viel schnellere
	 * Methode {@link umfasst umfasst(RtInternalIdentifier)} verwendet werden.
	 * </p>
	 * <p>
	 * Gibt <code>true</code> zurück, wenn dieser Identifier den anderen enthält unter
	 * Berücksichtigung der Sonderbedeutung des Index ([IIdentifier.ALL_INDICES](../../
	 * kernel-core-runtime-api-ts/modules/_validation_iidentifier_.iidentifier.html#all_indices)) <br>
	 * Z.B. umfasst der Identifier /[]/V[1]/K[0]/F[0] den Identifier /[]/V[1]/K[3]/F[2].<br>
	 * Hat der Kontext /V/Ctx max. Wiederholbarkeit 1 und das Feld /V/Ctx/F max. Zeilenindex 1, so
	 * umfasst /[]/V[3]/Ctx[1]/F[1] auch /[]/V[3]/Ctx[0]/F[0]
	 * </p>
	 */
	umfasstFuerPflichtangabenPruefung(
		id: RtInternalIdentifier,
		metaData: IMetaDataValidierung
	): boolean {
		if (this._name !== id.getName()) {
			return false;
		}
		if (this._indices.length !== id.getIndices().length) {
			return false;
		}
		const maxIndices: number[] = metaData.getMaxFieldIndices(this._name);
		for (const i in this._indices) {
			if (this._indices[i] === IIdentifier.ALL_INDICES || this._indices[i] === id.getIndices()[i]) {
				continue;
			}
			if (
				maxIndices[i] === 1 &&
				this._indices[i] === 1 &&
				id.getIndices()[i] === IIdentifier.ALL_INDICES
			) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * Gibt <code>true</code> zurück, wenn dieser Identifier den anderen enthält unter
	 * Berücksichtigung der Sonderbedeutung des Index [IIdentifier.ALL_INDICES](../../
	 * kernel-core-runtime-api-ts/modules/_validation_iidentifier_.iidentifier.html#all_indices).<br>
	 * Z.B. umfasst der Identifier /[]/V[1]/K[0]/F[0] die Identifier
	 * <ul>
	 * <li>/[]/V[1]/K[3]/F[2]</li>
	 * <li>/[]/V[1]/K[3]/F[0]</li>
	 * <li>/[]/V[1]/K[0]/F[0]</li>
	 * <li>/[]/V[1]/K[0]/F[2]</li>
	 * </ul>
	 * Spezifiziert der übergebene Identifier für eine Ebene [IIdentifier.ALL_INDICES](../../
	 * kernel-core-runtime-api-ts/modules/_validation_iidentifier_.iidentifier.html#all_indices), so
	 * kann die Methode nur dann <code>true</code> liefern, wenn der aufrufende Identifier für diese
	 * Ebene ebenfalls [IIdentifier.ALL_INDICES](../../kernel-core-runtime-api-ts/modules/
	 * _validation_iidentifier_.iidentifier.html#all_indices) spezifiziert.<br>
	 * Die Methode sollte also nicht mit [IIdentifier.ALL_INDICES](../../kernel-core-runtime-api-ts/modules/
	 * _validation_iidentifier_.iidentifier.html#all_indices) für eine nicht
	 * wiederholbare Ebene aufgerufen werden, denn in diesem Fall wird die umfasst-Relation u.U.
	 * nicht richtig erkannt - für den Fall sollte
	 * {@link umfasstFuerPflichtangabenPruefung umfasstFuerPflichtangabenPruefung(
	 * RtInternalIdentifier, IMetaDataValidierung)}
	 * verwendet werden.
	 */
	umfasst(id: RtInternalIdentifier): boolean {
		if (this._name !== id.getName()) {
			return false;
		}
		if (this._indices.length !== id.getIndices().length) {
			return false;
		}
		for (let i = 0; i < this._indices.length; i++) {
			if (this._indices[i] !== IIdentifier.ALL_INDICES && this._indices[i] !== id.getIndices()[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Remove the index parts that are not contained in id.
	 *
	 * Attention: the result is not symmetric.
	 *
	 * @param id 	the id to intersect with.
	 */
	intersect(id: RtInternalIdentifier): RtInternalIdentifier | undefined {
		const indexes: number[] = id.getIndices();
		const resultIdx = new Array<number>(this._indices.length);
		let changed = false;

		if (indexes.length < this._indices.length) {
			return undefined;
		}

		for (let i = 0; i < this._indices.length; i++) {
			const idx = this._indices[i];
			const oIdx = indexes[i];

			if (idx === oIdx || oIdx === IIdentifier.ALL_INDICES) {
				resultIdx[i] = idx;
			} else if (idx === IIdentifier.ALL_INDICES) {
				resultIdx[i] = oIdx;
				changed = true;
			} else {
				return undefined;
			}
		}

		if (!changed) {
			return this;
		} else {
			return new RtInternalIdentifier(
				this._name,
				resultIdx,
				this._category,
				this._semIndex
			);
		}
	}

	static createIdx(id: RtInternalIdentifier): number[] {
		return id.getIndices().slice(0);
	}

	/**
	 * Create a {@link RtInternalIdentifier} for the enclosing group for the given indexes.
	 *
	 * @param id 	the id to create the parent for.
	 * @param fullName 	full name of the id
	 */
	static createParentId(id: RtInternalIdentifier, fullName: string): RtInternalIdentifier | null {
		const parentName = EntityUtil.getParentPath(fullName);
		if (parentName.length === 0) {
			return null;
		}
		const parentIndexes = id.getIndices().slice(0, -1);
		return new RtInternalIdentifier(parentName, parentIndexes);
	}

	static normalizeIdentifier(id: RtInternalIdentifier): RtInternalIdentifier {
		return new RtInternalIdentifier(
			id.getName(),
			id.getIndices(),
			id.getCategory(),
			id.getSemanticIndex()
		);
	}
}
