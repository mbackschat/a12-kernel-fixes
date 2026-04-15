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

/**
 * This class models the boundaries of a repetition layer.
 * This class is outside {@link RtIdentifierTemplate} because otherwise we have difficulties to
 * translate the class to typescript.
 */
export class RtIdentifierTemplateLayerSpec {
	private readonly _idxStart: number;
	private readonly _idxEnd: number;

	constructor(idxStart: number, idxEnd?: number) {
		this._idxStart = idxStart;
		this._idxEnd = idxEnd ? idxEnd : idxStart;
	}

	getIdxStart(): number {
		return this._idxStart;
	}

	getIdxEnd(): number {
		return this._idxEnd;
	}

	public toString(): string {
		return "[" + this._idxStart + " - " + this._idxEnd + "]";
	}

	public static layerSpecsToStartEndIndices(
		ll: Array<RtIdentifierTemplateLayerSpec>
	): [number[], number[]] {
		const startIdx: number[] = [];
		const endIdx: number[] = [];
		for (let i = 0; i < ll.length; i++) {
			startIdx[i] = ll[i].getIdxStart();
			endIdx[i] = ll[i].getIdxEnd();
		}
		return [startIdx, endIdx];
	}

	public static startEndIndicesToLayerSpecs(
		startIndices: number[],
		endIndices: number[]
	): Array<RtIdentifierTemplateLayerSpec> {
		if (startIndices.length != endIndices.length) {
			throw new Error("invalid argument");
		}
		const ls: Array<RtIdentifierTemplateLayerSpec> = [];
		for (let i = 0; i < startIndices.length; i++) {
			ls.push(new RtIdentifierTemplateLayerSpec(startIndices[i], endIndices[i]));
		}
		return ls;
	}
}
