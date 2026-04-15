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
import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import { VkBigDecimal } from "../util/VkBigDecimal.js";

import { FormatDefinitionZahl } from "../formatdef/FormatDefinitionZahl.js";

import type { Combiner } from "./Combiner.js";

/**
 * Abstract implementation of a {@link Combiner} for {@link VkBigDecimal}s.
 * Contains specific implementations for FirstFilledValue, MinValue, MaxValue and Sum.
 */
export abstract class NumberCombiner implements Combiner<VkBigDecimal, VkBigDecimal> {
	protected nichtAngFeldGefunden = false;
	protected kannKleinerWerden = false;
	protected first = true;
	protected value: VkBigDecimal = VkBigDecimal.ZAHL_0;

	public getNonRelevantValue(): VkBigDecimal {
		return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
	}

	public setOmittedTail(fd: IFormatDefinition | undefined): void {
		this.setEmptyValueGiven(fd);
	}

	private setEmptyValueGiven(fd: IFormatDefinition | undefined): void {
		this.nichtAngFeldGefunden = true;
		this.kannKleinerWerden = this.kannKleinerWerden || this.kannKleinerWerdenBeiNichtAngFeld(fd);
	}

	public addValue(
		val: VkBigDecimal,
		fd: IFormatDefinition | undefined,
		testValidity: boolean
	): void {
		if (
			testValidity &&
			val.getBigDecimalValue().eq(VkBigDecimal.NICHT_ANGEGEBENE_ZAHL.getBigDecimalValue()) &&
			(val.isNichtPruefungsrelevant || val.kannGroesserWerden || val.kannKleinerWerden)
		) {
			this.setEmptyValueGiven(fd);
			return;
		}
		this.value = this.combineWith(val);
		this.first = false;
	}

	public getValue(containsFilter: boolean): VkBigDecimal {
		// in presence of a filter condition we assume that the result always can become bigger or smaller
		if (containsFilter) {
			return new VkBigDecimal(this.value.toString(), false, true, true);
		}
		if (this.nichtAngFeldGefunden) {
			/*
			 * Wenn keines der Felder angegeben ist (dann ist first = true), kann die Zahl immer
			 * größer oder kleiner werden (weil dann 0 zurückgegeben wird).
			 */
			const kannGroesserWerden: boolean = this.first || this.kannGroesserWerdenBeiNichtAngFeld();
			this.kannKleinerWerden = this.kannKleinerWerden || this.first;
			return new VkBigDecimal(
				this.value.toString(),
				false,
				kannGroesserWerden,
				this.kannKleinerWerden
			);
		}
		return this.value;
	}

	public canChange(): boolean {
		return true;
	}

	abstract combineWith(d: VkBigDecimal): VkBigDecimal;

	abstract kannGroesserWerdenBeiNichtAngFeld(): boolean;

	abstract kannKleinerWerdenBeiNichtAngFeld(fd: IFormatDefinition | undefined): boolean;
}

export class MinWertCombiner extends NumberCombiner {
	public combineWith(d2: VkBigDecimal): VkBigDecimal {
		return this.first ? d2 : this.value.min(d2);
	}

	public kannGroesserWerdenBeiNichtAngFeld(): boolean {
		return false;
	}

	// TODO: Fix this the next time the file is edited.
	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	public kannKleinerWerdenBeiNichtAngFeld(fd: IFormatDefinition | undefined): boolean {
		return true;
	}
}

export class MaxWertCombiner extends NumberCombiner {
	public combineWith(d2: VkBigDecimal): VkBigDecimal {
		return this.first ? d2 : this.value.max(d2);
	}

	public kannGroesserWerdenBeiNichtAngFeld(): boolean {
		return true;
	}

	// TODO: Fix this the next time the file is edited.
	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	public kannKleinerWerdenBeiNichtAngFeld(fd: IFormatDefinition | undefined): boolean {
		return false;
	}
}

export class SummeCombiner extends NumberCombiner {
	public combineWith(d2: VkBigDecimal): VkBigDecimal {
		return this.value.add(d2);
	}

	public kannGroesserWerdenBeiNichtAngFeld(): boolean {
		return true;
	}

	public kannKleinerWerdenBeiNichtAngFeld(fd: IFormatDefinition | undefined): boolean {
		if (!(fd instanceof FormatDefinitionZahl)) {
			return true;
		}
		return fd.hatVorzeichen;
	}
}
