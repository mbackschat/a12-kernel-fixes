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
 * Represents the concrete indices of an iteration. In case of parallel iteration, one of the
 * indices can be defined by the value of an index field ({@link _indexValue}).
 * When inside a filter conditions, also stores the {@link IterationState} of the (outer) rule iteration.
 */
export class IterationState {
	private readonly _indices: number[];
	private readonly _indexValue: string | undefined;
	private readonly _outerState?: IterationState;

	/**
	 * @param indices			Indices of the iteration. May contain
	 * 							[IIdentifier.PARALLEL_ITERATION](../../kernel-core-runtime-api-ts/modules/
	 * 							_validation_iidentifier_.iidentifier.html#parallel_iteration). In this case,
	 *            				the concrete repetition is defined by {@link _indexValue}.
	 * @param indexValue	Value of the index field that defines the concrete repetition for the indexed layer.
	 */
	constructor(indices: number[], indexValue: string | undefined, outerState?: IterationState) {
		this._indices = indices;
		this._indexValue = indexValue;
		this._outerState = outerState;
	}

	public getIndex(level: number): number {
		return this._indices[level];
	}

	public getValue(): string | undefined {
		return this._indexValue;
	}

	public getIndexes(): number[] {
		return this._indices;
	}

	/**
	 * @returns the state of the (outer) rule iteration when inside a filter condition.
	 * 		Outside of filter conditions undefined.
	 */
	public getOuterState(): IterationState | undefined {
		return this._outerState;
	}

	public toString(): string {
		const indexValueStr: string = this._indexValue ? ", indexValue=" + this._indexValue : "";
		return "IterationState [indices=" + this._indices.toString() + indexValueStr + "]";
	}
}
