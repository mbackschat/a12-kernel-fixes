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
import type { RoundingMode } from "big.js";
import { Big } from "big.js";

import { DefaultHashFunc } from "@com.mgmtp.a12.utils/utils-collections";

import { BigDecimal } from "@com.mgmtp.a12.utils/utils-localization/a12internal";

import type { VkValue } from "./VkValue.js";

interface CanChange {
	canGrow: boolean,
	canShrink: boolean
}

/**
 * Wrapper Klasse für BigDecimal. Dieser erlaubt auch einen "ungültigen" Wert, wenn z.B.
 * durch 0 dividiert wird. Alle Berechnungen mit ungültigen Werten führen wieder zu ungültigen
 * Werten.
 *
 */
export class VkBigDecimal extends BigDecimal implements VkValue {

	public static ZAHL_0: VkBigDecimal = VkBigDecimal._createZahl0();
	public static NICHT_PRUEF_REL_ZAHL: VkBigDecimal =
		VkBigDecimal._createNichtPruefungsrelevanteZahl();
	public static NICHT_ANGEGEBENE_ZAHL: VkBigDecimal = VkBigDecimal._createNichtAngegebeneZahl();
	public static INVALID_NUMBER: VkBigDecimal = VkBigDecimal._createUngueltigenWert();

	private _wertUngueltig: boolean;
	/**
	 * if set to true, we know that the value of the underlying field (or possibly operation) is not
	 * given, as opposed to zero. This explicit flag was introduced because previously a not-given
	 * field resulted in an VkBigDecimal with value 0 and both kannGroesserWerden and
	 * kannKleinerWerden set to true, which cannot reliably be distinguished from a given-but zero
	 * value that also has those two flags set.
	 * <p>
	 * Note that currently, results of operations often don't have this flag set even if their
	 * inputs are empty, because many operations are defined to return zero when no input is given,
	 * and treating those zeros as empty may break existing uses.
	 * <p>
	 * Such a given-but-zero value with both kannGroesserWerden and kannKleinerWerden is possible
	 * e.g. as a result of
	 * <code>Max(Max([fneg1], Summe(fneg2, fempty1)), Min([f0], [fempty2]))</code> with
	 * <code>fneg1 = -1; fneg2 = -2; f0 = 0</code> and the femtpy* fields not-given
	 */
	private _isEmpty = false;

	// Zeigt an, dass bei der Berechnung der Zahl ein nicht prüfungsrelevantes Feld gefunden wurde.
	private _nichtPruefungsrelevant: boolean;

	// Die beiden folgenden Attribute werden benötigt, um zwischen Auslassungs- und Wertefehlern zu unterscheiden.
	private _kannGroesserWerden: boolean;
	private _kannKleinerWerden: boolean;

	constructor(
		wert: number | string | Big | boolean,
		nichtPruefungsrelevant = false,
		kannGroesserWerden = false,
		kannKleinerWerden = false,
		isEmpty = false
	) {
		super(wert);
		if (typeof wert === "boolean") {
			this._wertUngueltig = wert;
		} else {
			this._wertUngueltig = false;
		}
		this._nichtPruefungsrelevant = nichtPruefungsrelevant;
		this._kannGroesserWerden = kannGroesserWerden;
		this._kannKleinerWerden = kannKleinerWerden;
		this._isEmpty = isEmpty;
	}

	private static _createUngueltigenWert(): VkBigDecimal {
		return new VkBigDecimal(true);
	}

	private static _createZahl0(): VkBigDecimal {
		return new VkBigDecimal(0);
	}

	private static _createNichtPruefungsrelevanteZahl(): VkBigDecimal {
		return new VkBigDecimal(VkBigDecimal.DEFAULT_VALUE, true, false, false);
	}

	private static _createNichtAngegebeneZahl(): VkBigDecimal {
		return new VkBigDecimal(VkBigDecimal.DEFAULT_VALUE, false, true, true, true);
	}

	/**
	 * Liefert -1, 0, bzw. 1 wenn dieses VkBigDecimal kleiner, gleich bzw. größer ist als
	 * other. Ist mindestens einer der beiden Werte ungültig, so wird immmer -1 zurückgegeben.
	 * ACHTUNG: die Methode soll nicht auf einem ungültigen oder mit einem ungültigen Wert
	 * aufgerufen werden!
	 */
	public compareTo(other: VkBigDecimal): number {
		if (this._wertUngueltig || other.wertIstUngueltig) {
			return -1;
		}
		return this._zahl.cmp(other._zahl);
	}

	public getBigDecimalValue(): Big {
		return this._zahl;
	}

	/**
	 * Liefert ein VkBigDecimal, dessen Wert der absolut Betrag von diesem VkBigDecimal ist.
	 * Ist dieses VkBigDecimal ungültig, so wird ein als ungültig markierter Wert zurückgegeben.
	 */
	public abs(): VkBigDecimal {
		if (this.wertIstUngueltig) {
			return VkBigDecimal.INVALID_NUMBER;
		}
		const erg: Big = this._zahl.abs();
		const ergNichtPruefungsrelevant: boolean = this._nichtPruefungsrelevant;
		const ergKannGroesserWerden: boolean = this._kannGroesserWerden || this._kannKleinerWerden;
		const ergKannKleinerWerden: boolean =
			(this._istPositiv && this._kannKleinerWerden) ||
			(this._istNegativ && this._kannGroesserWerden);
		const isAbsEmpty = this._isEmpty;
		return new VkBigDecimal(
			erg,
			ergNichtPruefungsrelevant,
			ergKannGroesserWerden,
			ergKannKleinerWerden,
			isAbsEmpty
		);
	}

	/**
	 * Liefert das Minimum von diesem VkBigDecimal und other, ermittelt durch compareTo.
	 * Ist einer der Werte ungültig, so wird ein als ungültig markierter Wert zurückgegeben.
	 */
	public min(other: VkBigDecimal): VkBigDecimal {
		if (this.wertIstUngueltig || other.wertIstUngueltig) {
			return VkBigDecimal.INVALID_NUMBER;
		}
		const min: VkBigDecimal = this.compareTo(other) > 0 ? other : this;
		const ergNichtPruefungsrelevant: boolean =
			this._nichtPruefungsrelevant || other._nichtPruefungsrelevant;
		const ergKannKleinerWerden: boolean = this.kannKleinerWerden || other.kannKleinerWerden;
		const ergKannGroesserWerden: boolean = this.compareTo(other) == 0 ?
			this.kannGroesserWerden && other.kannGroesserWerden :
			min.kannGroesserWerden;
		const isMinEmpty: boolean = this._isEmpty && other._isEmpty;
		return new VkBigDecimal(
			min._zahl,
			ergNichtPruefungsrelevant,
			ergKannGroesserWerden,
			ergKannKleinerWerden,
			isMinEmpty
		);
	}

	/**
	 * Liefert das Maximum von diesem VkBigDecimal und other, ermittelt durch {@link compareTo}.
	 * Ist einer der Werte ungültig, so wird ein als ungültig markierter Wert zurückgegeben.
	 */
	public max(other: VkBigDecimal): VkBigDecimal {
		if (this.wertIstUngueltig || other.wertIstUngueltig) {
			return VkBigDecimal.INVALID_NUMBER;
		}
		const max: VkBigDecimal = this.compareTo(other) > 0 ? this : other;
		const ergNichtPruefungsrelevant: boolean =
			this._nichtPruefungsrelevant || other._nichtPruefungsrelevant;
		const ergKannGroesserWerden: boolean = this.kannGroesserWerden || other.kannGroesserWerden;
		const ergKannKleinerWerden: boolean = this.compareTo(other) == 0 ?
			this.kannKleinerWerden && other.kannKleinerWerden :
			max.kannKleinerWerden;
		const isMaxEmpty: boolean = this._isEmpty && other._isEmpty;
		return new VkBigDecimal(
			max._zahl,
			ergNichtPruefungsrelevant,
			ergKannGroesserWerden,
			ergKannKleinerWerden,
			isMaxEmpty
		);
	}

	/**
	 * Liefert 0 falls die Zahl ungültig ist und sonst die Zahl als int
	 */
	public intValue(): number {
		return Number(this._zahl.round(0, 0)); // Big.RM = 0 > round towards zero
	}

	public number(): number {
		return Number(this._zahl);
	}

	/**
	 * Liefert den Wert von diesem VkBigDecimal plus dem Wert von augend.
	 * Ist dieses VkBigDecimal oder augend ungültig, so wird ein als ungültig markierter Wert
	 * zurückgegeben.
	 */
	public add(augend: VkBigDecimal): VkBigDecimal {
		if (this.wertIstUngueltig || augend.wertIstUngueltig) {
			return VkBigDecimal.INVALID_NUMBER;
		}
		return new VkBigDecimal(
			this._zahl.plus(augend._zahl),
			this._nichtPruefungsrelevant || augend._nichtPruefungsrelevant,
			this.kannGroesserWerden || augend.kannGroesserWerden,
			this.kannKleinerWerden || augend.kannKleinerWerden,
			this._isEmpty && augend._isEmpty
		);
	}

	/**
	 * Liefert den Wert von diesem VkBigDecimal multipliziert mit dem Wert von multiplicand.
	 * Ist dieses VkBigDecimal oder multiplicand ungültig, so wird ein als ungültig markierter Wert
	 * zurückgegeben.
	 */
	public multiply(multiplicand: VkBigDecimal): VkBigDecimal {
		if (this.wertIstUngueltig || multiplicand.wertIstUngueltig) {
			return VkBigDecimal.INVALID_NUMBER;
		}
		return new VkBigDecimal(
			this._zahl.times(multiplicand._zahl),
			this._nichtPruefungsrelevant || multiplicand._nichtPruefungsrelevant,
			this._produktKannGroesserWerden(this, multiplicand),
			this._produktKannKleinerWerden(this, multiplicand),
			this._isEmpty && multiplicand._isEmpty
		);
	}

	private _produktKannGroesserWerden(faktor1: VkBigDecimal, faktor2: VkBigDecimal): boolean {
		return (
			(faktor1.kannGroesserWerden && faktor2.kannGroesserWerden) ||
			(faktor1.kannKleinerWerden && faktor2.kannKleinerWerden) ||
			this._produktKannGroesserWerdenBezogenAufErstenFaktor(faktor1, faktor2) ||
			this._produktKannGroesserWerdenBezogenAufErstenFaktor(faktor2, faktor1)
		);
	}

	private _produktKannGroesserWerdenBezogenAufErstenFaktor(
		faktor1: VkBigDecimal,
		faktor2: VkBigDecimal
	): boolean {
		return (
			(faktor2._istPositiv && faktor1.kannGroesserWerden) ||
			(faktor2._istNegativ && faktor1.kannKleinerWerden)
		);
	}

	private _produktKannKleinerWerden(faktor1: VkBigDecimal, faktor2: VkBigDecimal): boolean {
		return (
			(faktor1.kannGroesserWerden && faktor2.kannKleinerWerden) ||
			(faktor1.kannKleinerWerden && faktor2.kannGroesserWerden) ||
			this._produktKannKleinerWerdenBezogenAufErstenFaktor(faktor1, faktor2) ||
			this._produktKannKleinerWerdenBezogenAufErstenFaktor(faktor2, faktor1)
		);
	}

	private _produktKannKleinerWerdenBezogenAufErstenFaktor(
		faktor1: VkBigDecimal,
		faktor2: VkBigDecimal
	): boolean {
		return (
			(faktor2._istPositiv && faktor1.kannKleinerWerden) ||
			(faktor2._istNegativ && faktor1.kannGroesserWerden)
		);
	}

	/**
	 * Liefert den Wert von diesem VkBigDecimal minus dem Wert von subtrahend.
	 * Ist dieses VkBigDecimal oder subtrahend ungültig, so wird ein als ungültig markierter Wert
	 * zurückgegeben.
	 */
	public subtract(subtrahend: VkBigDecimal): VkBigDecimal {
		if (this.wertIstUngueltig || subtrahend.wertIstUngueltig) {
			return VkBigDecimal.INVALID_NUMBER;
		}

		const erg: Big = this._zahl.minus(subtrahend._zahl);
		const ergNichtPruefungsrelevant: boolean =
			this._nichtPruefungsrelevant || subtrahend._nichtPruefungsrelevant;
		const ergKannGroesserWerden: boolean = this.kannGroesserWerden || subtrahend.kannKleinerWerden;
		const ergKannKleinerWerden: boolean = this.kannKleinerWerden || subtrahend.kannGroesserWerden;
		const isResultEmpty: boolean = this._isEmpty && subtrahend._isEmpty;
		return new VkBigDecimal(
			erg,
			ergNichtPruefungsrelevant,
			ergKannGroesserWerden,
			ergKannKleinerWerden,
			isResultEmpty
		);
	}

	/**
	 * Liefert den Wert von diesem VkBigDecimal dividiert durch den Wert von divisor.
	 * Ist dieses VkBigDecimal ungültig oder divisor gleich 0 oder ungültig, so wird
	 * ein als ungültig markierter Wert zurückgegeben.
	 */
	public divide(divisor: VkBigDecimal): VkBigDecimal {
		if (this.wertIstUngueltig || divisor.wertIstUngueltig) {
			return VkBigDecimal.INVALID_NUMBER;
		}

		if (divisor._zahl.cmp(0) === 0) {
			return VkBigDecimal.INVALID_NUMBER;
		}
		Big.DP = VkBigDecimal.INTERNAL_DECIMAL_PLACES;
		const erg: Big = this._zahl.div(divisor._zahl);
		const ergNichtPruefungsrelevant: boolean =
			this._nichtPruefungsrelevant || divisor._nichtPruefungsrelevant;
		const ergKannGroesserWerden: boolean = this._produktKannGroesserWerden(
			this,
			this._getKehrwert(divisor)
		);
		const ergKannKleinerWerden: boolean = this._produktKannKleinerWerden(
			this,
			this._getKehrwert(divisor)
		);
		const isFractionEmpty: boolean = this._isEmpty && divisor._isEmpty;
		return new VkBigDecimal(
			erg,
			ergNichtPruefungsrelevant,
			ergKannGroesserWerden,
			ergKannKleinerWerden,
			isFractionEmpty
		);
	}

	/**
	 * Vorbedingung: Zahl ist ungleich 0.
	 */
	private _getKehrwert(d: VkBigDecimal): VkBigDecimal {
		const erg: Big = new Big(1).div(d._zahl);
		const ergNichtPruefungsrelevant: boolean = d._nichtPruefungsrelevant;
		const ergKannGroesserWerden: boolean =
			d.kannKleinerWerden || (d._istNegativ && d.kannGroesserWerden);
		const ergKannKleinerWerden: boolean =
			d.kannGroesserWerden || (d._istPositiv && d.kannKleinerWerden);
		const isKehrwertEmpty = d._isEmpty;
		return new VkBigDecimal(
			erg,
			ergNichtPruefungsrelevant,
			ergKannGroesserWerden,
			ergKannKleinerWerden,
			isKehrwertEmpty
		);
	}

	public power(exponent: VkBigDecimal): VkBigDecimal {
		if (this.wertIstUngueltig || exponent.wertIstUngueltig) {
			return VkBigDecimal.INVALID_NUMBER;
		}

		if (exponent._zahl.lt(0)) {
			if (this._zahl.eq(0)) {
				// 0 ^ -n is invalid
				return VkBigDecimal.INVALID_NUMBER;
			} else {
				// a^(-b) = (1/a)^b if a != 0
				return this._getKehrwert(this).power(exponent.multiply(new VkBigDecimal(-1)));
			}
		}

		// Handle non-negative exponents
		if (exponent._zahl.gt(1000)) {
			// Exponents much bigger than that may be slow
			return VkBigDecimal.INVALID_NUMBER;
		}

		if (!exponent._zahl.mod(1).eq(0)) {
			// Fractional exponents are not supported
			return VkBigDecimal.INVALID_NUMBER;
		}

		const erg: Big = this._zahl.pow(exponent._zahl.toNumber());
		const ergNichtPruefungsrelevant = this._nichtPruefungsrelevant || exponent._nichtPruefungsrelevant;

		// Calculate whether the result can grow or shrink
		const canChange = this._powCanChange(this, exponent);
		const ergKannKleinerWerden = canChange.canShrink;
		const ergKannGroesserWerden = canChange.canGrow;

		const isResultEmpty = this._isEmpty && erg.eq(0);
		return new VkBigDecimal(
			erg,
			ergNichtPruefungsrelevant,
			ergKannGroesserWerden,
			ergKannKleinerWerden,
			isResultEmpty
		);
	}

	private _powCanChange(base: VkBigDecimal, exponent: VkBigDecimal): CanChange {
		// Fixed exponent case
		if (!exponent.kannKleinerWerden && !exponent.kannGroesserWerden) {
			return this._powCanChangeWithFixedExponent(base, exponent);
		}

		// Fixed base case
		if (!base.kannKleinerWerden && !base.kannGroesserWerden) {
			return this._powCanChangeWithFixedBase(base, exponent);
		}

		// Variable base and exponent case
		return this._powCanChangeWithEverythingVariable(base, exponent);
	}

	private _powCanChangeWithEverythingVariable(base: VkBigDecimal, exponent: VkBigDecimal): CanChange {
		// Handle the simplest case explicitly
		if (base._zahl.gt(1) && !exponent.kannKleinerWerden && !base.kannKleinerWerden) {
			// a > 1 => a^n is strictly monotonously rising
			return { canShrink: false, canGrow: true };
		}

		// 1. If base can traverse from <1 to >1 or from >-1 to <-1 in either direction,
		//    then the change regarding the exponent inverses and (true, true) should be correct.
		// 2. If base < -1 and only falling,
		//    then (true, true) should also be correct, since we can always choose an uneven exponent.
		return { canShrink: true, canGrow: true };
	}

	private _powCanChangeWithFixedBase(base: VkBigDecimal, exponent: VkBigDecimal): CanChange {
		// Precondition: (exponent.kannKleinerWerden || exponent.kannGroesserWerden) && exponent >= 0 &&
		// !base.kannGroesserWerden && !base.kannKleinerWerden
		if (base._zahl.gt(1)) {
			// a > 1 => a^n is monotonously rising
			return { canShrink: exponent.kannKleinerWerden, canGrow: exponent.kannGroesserWerden };
		} else if (base._zahl.eq(1)) {
			// 1^n = 1
			return { canShrink: false, canGrow: false };
		} else if (base._zahl.gt(0)) {
			// 0 < a < 1 => a^n is monotonously falling
			return { canShrink: exponent.kannGroesserWerden, canGrow: exponent.kannKleinerWerden };
		} else if (base._zahl.eq(0)) {
			return this._powCanChangeWithFixedZeroBase(exponent);
		}

		return this._powCanChangeWithFixedNegativeBase(base, exponent);
	}

	private _powCanChangeWithFixedNegativeBase(base: VkBigDecimal, exponent: VkBigDecimal): CanChange {
		// base._zahl < 0
		// The result alternates between positive and negative for even and odd exponents
		const squared = this._isDivisibleBy2(exponent._zahl);
		if (base._zahl.gt(-1)) {
			// -1 < a < 0 => |a^n| is monotonously falling
			if (squared && !exponent.kannKleinerWerden) {
				return { canShrink: true, canGrow: false };
			} else if (!squared && !exponent.kannKleinerWerden) {
				return { canShrink: false, canGrow: true };
			} else {
				return { canShrink: true, canGrow: true };
			}
		} else if (base._zahl.eq(-1)) {
			// -1 ^ n alternates between -1 and +1
			if (squared) {
				return { canShrink: true, canGrow: false };
			} else {
				return { canShrink: false, canGrow: true };
			}
		} else // if (base._zahl.lt(-1))
			// a < -1 => |a^n| is monotonously rising
			if (squared && !exponent.kannGroesserWerden) {
				return { canShrink: true, canGrow: false };
			} else if (!squared && !exponent.kannGroesserWerden) {
				return { canShrink: false, canGrow: true };
			} else {
				return { canShrink: true, canGrow: true };
			}
	}

	private _powCanChangeWithFixedZeroBase(exponent: VkBigDecimal): CanChange {
		if (!exponent.kannKleinerWerden && exponent._zahl.gt(0)) {
			// 0^n = 0 if n > 0
			return { canShrink: false, canGrow: false };
		} else if (!exponent.kannKleinerWerden && exponent._zahl.eq(0)) {
			// 0^n = 1 for n = 0 and 0 for bigger n
			return { canShrink: true, canGrow: false };
		} else {
			// exponent._zahl < 0 can't reach here because INVALID_NUMBER is returned already
			if (!exponent.kannKleinerWerden) {
				throw new Error("Unexpected invalid state for exponent.");
			}
			// 0^n = 0 -> 1 -> inf für n -> -inf
			return { canShrink: false, canGrow: true };
		}
	}

	private _isDivisibleBy2(number: Big): boolean {
		return number.mod(2).eq(0);
	}

	private _powCanChangeWithFixedExponent(base: VkBigDecimal, exponent: VkBigDecimal): CanChange {
		// Precondition: exponent >= 0
		if (exponent._zahl.eq(0)) {
			// x^0 = 1 cannot grow or shrink (always 1)
			return { canShrink: false, canGrow: false };
		}

		if (!base.kannGroesserWerden && !base.kannKleinerWerden) {
			// No change in base => no change in value
			return { canShrink: false, canGrow: false };
		}

		const squared = this._isDivisibleBy2(exponent._zahl);
		const canGrow = base.kannGroesserWerden || (base.kannKleinerWerden && squared);
		const canShrink =
			base.kannKleinerWerden && (!squared || base._istPositiv) ||
			(squared && base._istNegativ && base.kannGroesserWerden);

		return { canShrink: canShrink, canGrow: canGrow };
	}

	/**
	 * Liefert ein VkBigDecimal, dessen BigJS-Wert sich auf dem
	 * BigJS dieses VkBigDecimals berechnet.
	 * Ist dieses VkBigDecimal ungültig, so wird ein als ungültig markierter Wert zurückgegeben.
	 */
	public setScale(anzStellen: number, modus: RoundingMode): VkBigDecimal {
		if (this.wertIstUngueltig) {
			return VkBigDecimal.INVALID_NUMBER;
		}

		return new VkBigDecimal(
			this._zahl.round(anzStellen, modus),
			this._nichtPruefungsrelevant,
			this._kannGroesserWerden,
			this._kannKleinerWerden,
			this._isEmpty
		);
	}

	/**
	 * Gibt true zurück, wenn dieses VkBigDecimal ungültig ist.
	 */
	public get wertIstUngueltig(): boolean {
		return this._wertUngueltig;
	}

	public get isNichtPruefungsrelevant(): boolean {
		return this._nichtPruefungsrelevant;
	}

	public get kannGroesserWerden(): boolean {
		return this._kannGroesserWerden;
	}

	public get kannKleinerWerden(): boolean {
		return this._kannKleinerWerden;
	}

	public get isNichtAngegeben(): boolean {
		return this.kannKleinerWerden || this.kannGroesserWerden;
	}

	public get hasValue(): boolean {
		// the below condition should be true, but we currently
		// don't want new assertions in the runtime:
		// assert !isEmpty || (kannGroesserWerden && kannKleinerWerden);
		return !this._isEmpty;
	}

	private get _istPositiv(): boolean {
		return this._zahl.gt(0);
	}

	private get _istNegativ(): boolean {
		return this._zahl.lt(0);
	}

	public equals(other: any): boolean {
		if (other === undefined || !(other instanceof VkBigDecimal)) {
			return false;
		}
		if (this === other) {
			return true;
		}
		if (this._wertUngueltig !== other._wertUngueltig ||
			this._isEmpty !== other._isEmpty) {
			return false;
		}
		// imitate setScale/rounding behavior of Java implementation
		return this._zahl
			.round(VkBigDecimal.DEFAULT_SCALE, Big.roundHalfUp)
			.eq(other._zahl.round(VkBigDecimal.DEFAULT_SCALE, Big.roundHalfUp));
	}

	public hash(): number {
		return DefaultHashFunc(
			[this._zahl.round(VkBigDecimal.DEFAULT_SCALE, Big.roundHalfUp).valueOf(), this._wertUngueltig,
			this._isEmpty]
		);
	}

	public toComparableString(): string {
		if (this._wertUngueltig) {
			return "invalid";
		} else if (this._isEmpty) {
			return "empty";
		} else {
			return this._zahl.toFixed(VkBigDecimal.DEFAULT_SCALE, Big.roundHalfUp);
		}
	}

	public toString(): string {
		if (this._wertUngueltig) {
			return "Ungültiger Wert";
		}
		return this._zahl.toString() + (this._isEmpty ? "(Empty)" : "");
	}

	public toStringWithMaxPrecision(maxPrecision: number): string {
		// Wenn keine maxPrecision spezifiziert, gib Wert bis zur maximalen Genauigkeit an
		if (maxPrecision === -1) {
			if (this.scale() < VkBigDecimal.DEFAULT_SCALE) {
				return this._zahl.toFixed();
			} else {
				Big.RM = VkBigDecimal.ROUND_HALF_UP;
				return this._zahl.toFixed(VkBigDecimal.DEFAULT_SCALE);
			}
		}

		if (this.precision() <= maxPrecision) {
			// Wenn der Wert in die max. Größe passt -> so ausgeben.
			return this._zahl.toFixed();
		} else {
			// scale = maxPrecision - # Vorkommastellen, aber Vorkommastellen zu eliminieren bringt nichts => min. 0
			// Vorkommastellen berechnen sich aus Anzahl Stellen insgesamt minus Stellen rechts vom Dezimaltrenner
			const scale: number = Math.max(0, maxPrecision - (this.precision() - this.scale()));
			Big.RM = VkBigDecimal.ROUND_HALF_UP;
			return this._zahl.toFixed(scale);
		}
	}

	/**
	 * Returns the number of significands.
	 */
	public precision(): number {
		return this._zahl.c.length;
	}

	public greaterThanOrEqual(other: number): boolean {
		return this._zahl.gte(other);
	}

	public toFixed(dp: number): string {
		return this._zahl.toFixed(dp);
	}
}
