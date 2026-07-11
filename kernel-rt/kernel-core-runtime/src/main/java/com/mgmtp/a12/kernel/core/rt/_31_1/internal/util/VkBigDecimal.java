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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.FormatDefinitionZahl;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.IFormatDefinition;

/**
 * Wrapper Klasse für BigDecimal. Dieser erlaubt auch einen "ungültigen" Wert, wenn z.B.
 * durch 0 dividiert wird. Alle Berechnungen mit ungültigen Werten führen wieder zu ungültigen
 * Werten.
 */
public class VkBigDecimal extends VkValue {
	/**
	 * Before comparing two values the scale is set to this default value with
	 * {@link RoundingMode#HALF_UP}.
	 */
	public static final int DEFAULT_SCALE = 19;

	/**
	 * Precision used for internal calculations.
	 */
	public static final int INTERNAL_PRECISION = 50;
	private static final MathContext MATH_CONTEXT = new MathContext(INTERNAL_PRECISION, RoundingMode.HALF_UP);

	public static final VkBigDecimal ZAHL_0 = createZahl0();
	public static final VkBigDecimal NICHT_PRUEF_REL_ZAHL = createNichtPruefungsrelevanteZahl();
	public static final VkBigDecimal NICHT_ANGEGEBENE_ZAHL = createNichtAngegebeneZahl();
	public static final VkBigDecimal INVALID_NUMBER = createUngueltigenWert();

	public static final long DEFAULT_VALUE = 0;

	private final BigDecimal zahl;
	private final boolean wertUngueltig;
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
	private boolean isEmpty = false;

	// Zeigt an, dass bei der Berechnung der Zahl ein nicht prüfungsrelevantes Feld gefunden wurde.
	private final boolean nichtPruefungsrelevant;

	// Die beiden folgenden Attribute werden benötigt, um zwischen Auslassungs- und Wertefehlern zu unterscheiden.
	private final boolean kannGroesserWerden;
	private final boolean kannKleinerWerden;

	private VkBigDecimal(BigDecimal bigDec, boolean nichtPruefungsrelevant, boolean kannGroesserWerden, boolean kannKleinerWerden,
			boolean isEmpty, CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		super(origins);
		this.zahl = bigDec;
		this.nichtPruefungsrelevant = nichtPruefungsrelevant;
		this.kannGroesserWerden = kannGroesserWerden;
		this.kannKleinerWerden = kannKleinerWerden;
		this.isEmpty = isEmpty;
		this.wertUngueltig = false;
	}

	private static VkBigDecimal createUngueltigenWert() {
		return new VkBigDecimal(true);
	}

	private static VkBigDecimal createZahl0() {
		return new VkBigDecimal(0, CollectionWithCheapMergeCopy.empty());
	}

	private static VkBigDecimal createNichtPruefungsrelevanteZahl() {
		return new VkBigDecimal(DEFAULT_VALUE, true, false, false, CollectionWithCheapMergeCopy.empty());
	}

	private static VkBigDecimal createNichtAngegebeneZahl() {
		return new VkBigDecimal(DEFAULT_VALUE, false, true, true, true, CollectionWithCheapMergeCopy.empty());
	}

	/**
	 * Creates a valid or invalid {@link VkBigDecimal} with the {@link #DEFAULT_VALUE}.
	 *
	 * @param invalid
	 * 		specifies if the value is invalid (i.e. the value is the result of a division by
	 * 		zero) or valid
	 */
	private VkBigDecimal(boolean invalid) {
		super(CollectionWithCheapMergeCopy.empty());
		zahl = new BigDecimal(DEFAULT_VALUE, MATH_CONTEXT);
		nichtPruefungsrelevant = false;
		kannGroesserWerden = false;
		kannKleinerWerden = false;
		wertUngueltig = invalid;
	}

	public VkBigDecimal(long l) {
		this(l, CollectionWithCheapMergeCopy.empty());
	}

	/**
	 * Erzeugt einen VkBigDecimal mit Wert l, Rundungsmodus
	 * {@link java.math.RoundingMode#HALF_UP} und Präzision {@link #INTERNAL_PRECISION}.
	 */
	public VkBigDecimal(long l, CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		super(origins);
		this.zahl = new BigDecimal(l, MATH_CONTEXT);
		nichtPruefungsrelevant = false;
		kannGroesserWerden = false;
		kannKleinerWerden = false;
		wertUngueltig = false;
	}

	/**
	 * Erzeugt einen VkBigDecimal mit Wert gegeben durch die String-Repräsentation wert,
	 * Rundungsmodus {@link java.math.RoundingMode#HALF_UP} und Präzision
	 * {@link #INTERNAL_PRECISION}.
	 */
	public VkBigDecimal(String wert, CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		super(origins);
		this.zahl = new BigDecimal(wert, MATH_CONTEXT);
		nichtPruefungsrelevant = false;
		kannGroesserWerden = false;
		kannKleinerWerden = false;
		wertUngueltig = false;
	}

	/**
	 * Creates a VkBigDecimal with value given by the string representation <code>value</code>,
	 * rounding mode {@link java.math.RoundingMode#HALF_UP} and precision.
	 * {@link #INTERNAL_PRECISION} determining whether it can get bigger or smaller by canChange and the formatDefinition.
	 */
	public VkBigDecimal(VkString value, IFormatDefinition formatDefinition) {
		super(value.getOrigins());
		this.zahl = new BigDecimal(value.getWert().replace(",", "."), MATH_CONTEXT);
		nichtPruefungsrelevant = false;
		if (value.isNichtAngegeben()) {
			kannGroesserWerden = true;
			kannKleinerWerden = this.zahl.compareTo(BigDecimal.ZERO) > 0 ||
					(formatDefinition instanceof FormatDefinitionZahl fdz && fdz.isSignAllowed());
		} else {
			kannGroesserWerden = false;
			kannKleinerWerden = false;
		}
		wertUngueltig = false;
	}

	public VkBigDecimal(String wert) {
		this(wert, CollectionWithCheapMergeCopy.empty());
	}

	public VkBigDecimal(long l, boolean nichtPruefungsrelevant, boolean kannGroesserWerden, boolean kannKleinerWerden) {
		this(l, nichtPruefungsrelevant, kannGroesserWerden, kannKleinerWerden, CollectionWithCheapMergeCopy.empty());
	}

	/**
	 * Erzeugt einen VkBigDecimal mit Wert l, Rundungsmodus
	 * {@link java.math.RoundingMode#HALF_UP} und Präzision {@link #INTERNAL_PRECISION}.
	 * <p>
	 * nichtPruefungsrelevant, kannGroesserWerden und kannKleinerWerden werden auf die
	 * übergebenen Werte gesetzt.
	 */
	public VkBigDecimal(long l, boolean nichtPruefungsrelevant, boolean kannGroesserWerden, boolean kannKleinerWerden,
			CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		this(l, nichtPruefungsrelevant, kannGroesserWerden, kannKleinerWerden, false, origins);
	}

	public VkBigDecimal(long l, boolean nichtPruefungsrelevant, boolean kannGroesserWerden, boolean kannKleinerWerden,
			boolean isEmpty) {
		this(l, nichtPruefungsrelevant, kannGroesserWerden, kannKleinerWerden, isEmpty, CollectionWithCheapMergeCopy.empty());
	}

	public VkBigDecimal(long l, boolean nichtPruefungsrelevant, boolean kannGroesserWerden, boolean kannKleinerWerden,
			boolean isEmpty, CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		super(origins);
		this.zahl = new BigDecimal(l, MATH_CONTEXT);
		this.nichtPruefungsrelevant = nichtPruefungsrelevant;
		this.kannGroesserWerden = kannGroesserWerden;
		this.kannKleinerWerden = kannKleinerWerden;
		this.wertUngueltig = false;
		this.isEmpty = isEmpty;
	}

	/**
	 * Erzeugt einen VkBigDecimal mit Wert bigDec.
	 * <p>
	 * nichtPruefungsrelevant, kannGroesserWerden und kannKleinerWerden werden auf die
	 * übergebenen Werte gesetzt.
	 */
	public VkBigDecimal(BigDecimal bigDec, boolean nichtPruefungsrelevant, boolean kannGroesserWerden, boolean kannKleinerWerden,
			CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		super(origins);
		this.zahl = bigDec;
		this.nichtPruefungsrelevant = nichtPruefungsrelevant;
		this.kannGroesserWerden = kannGroesserWerden;
		this.kannKleinerWerden = kannKleinerWerden;
		this.wertUngueltig = false;
	}

	/**
	 * Liefert -1, 0, bzw. 1 wenn dieses VkBigDecimal kleiner, gleich bzw. größer ist als
	 * other. Ist mindestens einer der beiden Werte ungültig, so wird immmer -1 zurückgegeben.
	 * ACHTUNG: die Methode soll nicht auf einem ungültigen oder mit einem ungültigen Wert
	 * aufgerufen werden!
	 */
	public int compareTo(VkBigDecimal other) {
		assert !wertUngueltig && !other.wertIstUngueltig() : "The method 'compareTo' in VkBigDecimal was called with an invalid value.";
		if (wertUngueltig || other.wertIstUngueltig()) {
			return -1;
		}
		return this.zahl.compareTo(other.getBigDecimalValue());
	}

	/**
	 * Liefert den BigDecimal-Wert von diesem VkBigDecimal.
	 */
	public BigDecimal getBigDecimalValue() {
		return zahl;
	}

	/**
	 * Liefert ein VkBigDecimal, dessen Wert der absolut Betrag von diesem
	 * VkBigDecimal ist.
	 */
	public VkBigDecimal abs() {
		if (this.wertIstUngueltig()) {
			return INVALID_NUMBER;
		}
		BigDecimal erg = zahl.abs();
		boolean ergNichtPruefungsrelevant = nichtPruefungsrelevant;
		boolean ergKannGroesserWerden = kannGroesserWerden || kannKleinerWerden;
		boolean ergKannKleinerWerden = (istPositiv() && kannKleinerWerden) || (istNegativ() && kannGroesserWerden);
		boolean isAbsEmpty = isEmpty;
		return new VkBigDecimal(erg, ergNichtPruefungsrelevant, ergKannGroesserWerden, ergKannKleinerWerden, isAbsEmpty,
				getOrigins());
	}

	/**
	 * Liefert das Minimum von diesem VkBigDecimal und other, ermittelt durch compareTo.
	 */
	public VkBigDecimal min(VkBigDecimal other) {
		if (this.wertIstUngueltig() || other.wertIstUngueltig()) {
			return INVALID_NUMBER;
		}
		VkBigDecimal min = compareTo(other) > 0 ? other : this;
		boolean ergNichtPruefungsrelevant = this.nichtPruefungsrelevant || other.nichtPruefungsrelevant;
		boolean ergKannKleinerWerden = this.kannKleinerWerden || other.kannKleinerWerden;
		boolean ergKannGroesserWerden = compareTo(other) == 0 ?
				// if they are equal, it only can get bigger if both can get bigger, else the other would dominate
				this.kannGroesserWerden && other.kannGroesserWerden :
				min.kannGroesserWerden;
		boolean isMinEmpty = isEmpty && other.isEmpty;
		return new VkBigDecimal(min.zahl, ergNichtPruefungsrelevant, ergKannGroesserWerden, ergKannKleinerWerden, isMinEmpty,
				min.getOrigins());
	}

	/**
	 * Liefert das Maximum von diesem VkBigDecimal und other, ermittelt durch {@link #compareTo}.
	 */
	public VkBigDecimal max(VkBigDecimal other) {
		if (this.wertIstUngueltig() || other.wertIstUngueltig()) {
			return INVALID_NUMBER;
		}
		VkBigDecimal max = compareTo(other) > 0 ? this : other;
		boolean ergNichtPruefungsrelevant = this.nichtPruefungsrelevant || other.nichtPruefungsrelevant;
		boolean ergKannGroesserWerden = this.kannGroesserWerden || other.kannGroesserWerden;
		boolean ergKannKleinerWerden = compareTo(other) == 0 ?
				// if they are equal, it only can get smaller if both can get smaller, else the other would dominate
				this.kannKleinerWerden && other.kannKleinerWerden :
				max.kannKleinerWerden;
		boolean isMaxEmpty = isEmpty && other.isEmpty;
		return new VkBigDecimal(max.zahl, ergNichtPruefungsrelevant, ergKannGroesserWerden, ergKannKleinerWerden, isMaxEmpty,
				max.getOrigins());
	}

	/**
	 * Liefert 0 falls die Zahl ungültig ist und sonst die Zahl als int
	 */
	public int intValue() {
		return this.zahl.intValue();
	}

	/**
	 * Liefert den Wert von diesem VkBigDecimal plus dem Wert von augend.
	 */
	public VkBigDecimal add(VkBigDecimal augend) {
		if (this.wertIstUngueltig() || augend.wertIstUngueltig()) {
			return INVALID_NUMBER;
		}

		BigDecimal erg = this.zahl.add(augend.getBigDecimalValue(), MATH_CONTEXT);
		boolean ergNichtPruefungsrelevant = this.nichtPruefungsrelevant || augend.nichtPruefungsrelevant;
		boolean ergKannGroesserWerden = this.kannGroesserWerden || augend.kannGroesserWerden;
		boolean ergKannKleinerWerden = this.kannKleinerWerden || augend.kannKleinerWerden;
		boolean isSumEmpty = isEmpty && augend.isEmpty;
		return new VkBigDecimal(erg, ergNichtPruefungsrelevant, ergKannGroesserWerden, ergKannKleinerWerden, isSumEmpty,
				this.getOrigins().merge(augend.getOrigins()));
	}

	/**
	 * Liefert den Wert von diesem VkBigDecimal multipliziert mit dem Wert von multiplicand.
	 */
	public VkBigDecimal multiply(VkBigDecimal multiplicand) {
		if (this.wertIstUngueltig() || multiplicand.wertIstUngueltig()) {
			return INVALID_NUMBER;
		}
		BigDecimal erg = this.zahl.multiply(multiplicand.getBigDecimalValue(), MATH_CONTEXT);
		boolean ergNichtPruefungsrelevant = this.nichtPruefungsrelevant || multiplicand.nichtPruefungsrelevant;
		boolean ergKannGroesserWerden = produktKannGroesserWerden(this, multiplicand);
		boolean ergKannKleinerWerden = produktKannKleinerWerden(this, multiplicand);
		boolean isProductEmpty = isEmpty && multiplicand.isEmpty;
		return new VkBigDecimal(erg, ergNichtPruefungsrelevant, ergKannGroesserWerden, ergKannKleinerWerden, isProductEmpty,
				this.getOrigins().merge(multiplicand.getOrigins()));
	}

	private boolean produktKannGroesserWerden(VkBigDecimal faktor1, VkBigDecimal faktor2) {
		return faktor1.kannGroesserWerden && faktor2.kannGroesserWerden || faktor1.kannKleinerWerden && faktor2.kannKleinerWerden
				|| produktKannGroesserWerdenBezogenAufErstenFaktor(faktor1, faktor2)
				|| produktKannGroesserWerdenBezogenAufErstenFaktor(faktor2, faktor1);
	}

	private boolean produktKannGroesserWerdenBezogenAufErstenFaktor(VkBigDecimal faktor1, VkBigDecimal faktor2) {
		return faktor2.istPositiv() && faktor1.kannGroesserWerden || faktor2.istNegativ() && faktor1.kannKleinerWerden;
	}

	private boolean produktKannKleinerWerden(VkBigDecimal faktor1, VkBigDecimal faktor2) {
		return faktor1.kannGroesserWerden && faktor2.kannKleinerWerden || faktor1.kannKleinerWerden && faktor2.kannGroesserWerden
				|| produktKannKleinerWerdenBezogenAufErstenFaktor(faktor1, faktor2)
				|| produktKannKleinerWerdenBezogenAufErstenFaktor(faktor2, faktor1);
	}

	private boolean produktKannKleinerWerdenBezogenAufErstenFaktor(VkBigDecimal faktor1, VkBigDecimal faktor2) {
		return faktor2.istPositiv() && faktor1.kannKleinerWerden || faktor2.istNegativ() && faktor1.kannGroesserWerden;
	}

	/**
	 * Liefert den Wert von diesem VkBigDecimal minus dem Wert von subtrahend.
	 */
	public VkBigDecimal subtract(VkBigDecimal subtrahend) {
		if (this.wertIstUngueltig() || subtrahend.wertIstUngueltig()) {
			return INVALID_NUMBER;
		}

		BigDecimal erg = this.zahl.subtract(subtrahend.getBigDecimalValue(), MATH_CONTEXT);
		boolean ergNichtPruefungsrelevant = this.nichtPruefungsrelevant || subtrahend.nichtPruefungsrelevant;
		boolean ergKannGroesserWerden = this.kannGroesserWerden || subtrahend.kannKleinerWerden;
		boolean ergKannKleinerWerden = this.kannKleinerWerden || subtrahend.kannGroesserWerden;
		boolean isResultEmpty = isEmpty && subtrahend.isEmpty;
		return new VkBigDecimal(erg, ergNichtPruefungsrelevant, ergKannGroesserWerden, ergKannKleinerWerden, isResultEmpty,
				this.getOrigins().merge(subtrahend.getOrigins()));
	}

	/**
	 * Liefert den Wert von diesem VkBigDecimal dividiert durch den Wert von divisor.
	 */
	public VkBigDecimal divide(VkBigDecimal divisor) {
		if (this.wertIstUngueltig() || divisor.wertIstUngueltig()) {
			return INVALID_NUMBER;
		}
		if (divisor.getBigDecimalValue().signum() == 0) {
			return INVALID_NUMBER;
		}
		BigDecimal erg = this.zahl.divide(divisor.getBigDecimalValue(), MATH_CONTEXT);
		boolean ergNichtPruefungsrelevant = this.nichtPruefungsrelevant || divisor.nichtPruefungsrelevant;
		boolean ergKannGroesserWerden = produktKannGroesserWerden(this, getKehrwert(divisor));
		boolean ergKannKleinerWerden = produktKannKleinerWerden(this, getKehrwert(divisor));
		boolean isFractionEmpty = isEmpty && divisor.isEmpty;
		return new VkBigDecimal(erg, ergNichtPruefungsrelevant, ergKannGroesserWerden, ergKannKleinerWerden, isFractionEmpty,
				this.getOrigins().merge(divisor.getOrigins()));
	}

	/**
	 * Vorbedingung: Zahl ist ungleich 0.
	 */
	private VkBigDecimal getKehrwert(VkBigDecimal d) {
		BigDecimal erg = BigDecimal.ONE.divide(d.getBigDecimalValue(), MATH_CONTEXT);
		boolean ergNichtPruefungsrelevant = d.nichtPruefungsrelevant;
		boolean ergKannGroesserWerden = d.kannKleinerWerden || d.istNegativ() && d.kannGroesserWerden;
		boolean ergKannKleinerWerden = d.kannGroesserWerden || d.istPositiv() && d.kannKleinerWerden;
		boolean isKehrwertEmpty = d.isEmpty;
		return new VkBigDecimal(erg, ergNichtPruefungsrelevant, ergKannGroesserWerden, ergKannKleinerWerden, isKehrwertEmpty,
				getOrigins());
	}

	public VkBigDecimal power(VkBigDecimal exponent) {
		if (this.wertIstUngueltig() || exponent.wertIstUngueltig()) {
			return INVALID_NUMBER;
		}
		if (exponent.zahl.signum() < 0) {
			if (zahl.signum() == 0) {
				// 0 ^ -n is invalid
				return INVALID_NUMBER;
			} else {
				// a^(-b) = (1/a)^b if a != 0
				// We shouldn't use abs because canGetBigger is true for |a| even if a cannot get smaller.
				// But if a can get bigger, -a can get smaller and may become < 0
				// 	and therefore there should be no difference in the final behaviour, though.
				return getKehrwert(this).power(exponent.multiply(new VkBigDecimal(-1L)));
			}
		}
		// exponent >= 0 from here on
		if (exponent.zahl.compareTo(new BigDecimal(1000)) > 0) {
			// Exponents much bigger than that may be slow in typescript
			return INVALID_NUMBER;
		}
		if (exponent.zahl.stripTrailingZeros().scale() > 0) {
			// We cannot handle fractional digits
			return INVALID_NUMBER;
		}
		BigDecimal erg = this.zahl.pow(exponent.zahl.intValue(), MATH_CONTEXT);

		boolean ergNichtPruefungsrelevant = this.nichtPruefungsrelevant || exponent.nichtPruefungsrelevant;
		CanChange canChange = powCanChange(this, exponent);
		boolean resultCanShrink = canChange.canShrink();
		boolean resultCanGrow = canChange.canGrow();
		boolean isResultEmpty = this.isEmpty && erg.signum() == 0;
		return new VkBigDecimal(erg, ergNichtPruefungsrelevant, resultCanGrow, resultCanShrink,
				isResultEmpty, this.getOrigins().merge(exponent.getOrigins()));
	}

	record CanChange(boolean canShrink, boolean canGrow) {
	}

	private CanChange powCanChange(VkBigDecimal base, VkBigDecimal exponent) {
		if (!exponent.kannKleinerWerden && !exponent.kannGroesserWerden) {
			return powCanChangeWithFixedExponent(base, exponent);
		}
		if (!base.kannKleinerWerden && !base.kannGroesserWerden) {
			return powCanChangeWithFixedBase(base, exponent);
		}
		return powCanChangeWithEverythingVariable(base, exponent);
	}

	private CanChange powCanChangeWithEverythingVariable(VkBigDecimal base, VkBigDecimal exponent) {
		// Handle only the simplest case of all explicitly
		if (base.zahl.compareTo(BigDecimal.ONE) > 0 && !exponent.kannKleinerWerden && !base.kannKleinerWerden) {
			// a > 1 => a^n strictly monotonously rising wrt. a and n
			return new CanChange(false, true);
		}
		// 1. if base can traverse from <1 to >1 or from >-1 to <-1 in either direction,
		//    then the change regarding the exponent inverses and (true, true) should be correct.
		// 2. if base <-1 and only falling,
		//    then (true, true) should also be correct, since we can always choose an uneven exponent.
		return new CanChange(true, true);
	}

	private CanChange powCanChangeWithFixedBase(VkBigDecimal base, VkBigDecimal exponent) {
		// Precondition (exponent.kannKleinerWerden || exponent.kannGroesserWerden) && exponent >= 0 &&
		// 	!base.kannGroesserWerden && !base.kannKleinerWerden
		//  and result is valid
		if (base.zahl.compareTo(BigDecimal.ONE) > 0) {
			// a>1 => a^n monotonously rising
			return new CanChange(exponent.kannKleinerWerden, exponent.kannGroesserWerden);
		} else if (base.zahl.compareTo(BigDecimal.ONE) == 0) {
			// 1^n = 1
			return new CanChange(false, false);
		} else if (base.zahl.signum() > 0) {
			// 0<a<1 => a^n monotonously falling
			return new CanChange(exponent.kannGroesserWerden, exponent.kannKleinerWerden);
		}
		if (base.zahl.signum() == 0) {
			return powCanChangeWithFixedZeroBase(exponent);
		}
		return powCanChangeWithFixedNegativeBase(base, exponent);
	}

	private static CanChange powCanChangeWithFixedNegativeBase(VkBigDecimal base, VkBigDecimal exponent) {
		// base.zahl.signum() < 0
		// exponent alternates between even and odd and result between - and +
		boolean squared = isDivisibleBy2(exponent.zahl);
		if (base.zahl.compareTo(new BigDecimal(-1)) > 0) {
			// -1<a<0 => |a^n| monotonous falling
			if (squared && !exponent.kannKleinerWerden) {
				return new CanChange(true, false);
			} else if (!squared && !exponent.kannKleinerWerden) {
				return new CanChange(false, true);
			} else {
				return new CanChange(true, true);
			}
		}
		if (base.zahl.compareTo(new BigDecimal(-1)) == 0) {
			// -1 ^ n alternates between -1 and +1
			if (squared) {
				return new CanChange(true, false);
			} else {
				return new CanChange(false, true);
			}
		}
		// a < -1 => |a^n| monotonously rising
		if (squared && !exponent.kannGroesserWerden) {
			return new CanChange(true, false);
		} else if (!squared && !exponent.kannGroesserWerden) {
			return new CanChange(false, true);
		} else {
			return new CanChange(true, true);
		}
	}

	private static CanChange powCanChangeWithFixedZeroBase(VkBigDecimal exponent) {
		if (!exponent.kannKleinerWerden && exponent.zahl.signum() > 0) {
			// 0^n = 0 if n > 0
			return new CanChange(false, false);
		} else if (!exponent.kannKleinerWerden() && exponent.zahl.signum() == 0) {
			// 0^n = 1 for n = 0 and 0 for bigger n
			return new CanChange(true, false);
		} else {
			// exponent.zahl < 0 can't reach here because INVALID_NUMBER is returned already
			assert (exponent.kannKleinerWerden);
			// 0^n = 0 -> 1 -> inf für n -> -inf
			return new CanChange(false, true);
		}
	}

	private static boolean isDivisibleBy2(BigDecimal number) {
		return number.stripTrailingZeros().remainder(BigDecimal.TWO).equals(BigDecimal.ZERO);
	}

	private static CanChange powCanChangeWithFixedExponent(VkBigDecimal base, VkBigDecimal exponent) {
		// Precondition: exponent >= 0
		if (exponent.zahl.signum() == 0) {
			// x^0 = 1 kann nichts größer oder kleiner werden (immer 1)
			return new CanChange(false, false);
		}
		if (!base.kannGroesserWerden && !base.kannKleinerWerden) {
			// no change in source -> no change in value
			return new CanChange(false, false);
		}
		boolean squared = isDivisibleBy2(exponent.zahl);
		boolean canGrow =
				// base -> inf
				base.kannGroesserWerden ||
						// base -> -inf
						/* base.kannKleinerWerden && */ squared;
		boolean canShrink =
				(base.kannKleinerWerden && (!squared || base.istPositiv())) ||
						(squared && base.istNegativ() && base.kannGroesserWerden);
		return new CanChange(canShrink, canGrow);
	}

	/**
	 * Liefert ein VkBigDecimal, dessen BigDecimal-Wert sich gemäß dem Aufruf von
	 * {@link BigDecimal#setScale(int anzStellen, RoundingMode mode)} auf dem
	 * BigDecimal dieses VkBigDecimals berechnet.
	 */
	public VkBigDecimal setScale(int anzStellen, RoundingMode mode) {
		if (this.wertIstUngueltig()) {
			return INVALID_NUMBER;
		}
		BigDecimal erg = zahl.setScale(anzStellen, mode);
		return new VkBigDecimal(erg, nichtPruefungsrelevant, kannGroesserWerden, kannKleinerWerden, isEmpty, getOrigins());
	}

	/**
	 * Gibt true zurück, wenn dieses VkBigDecimal ungültig ist.
	 */
	public boolean wertIstUngueltig() {
		return wertUngueltig;
	}

	public boolean isNichtPruefungsrelevant() {
		return nichtPruefungsrelevant;
	}

	public boolean kannGroesserWerden() {
		return kannGroesserWerden;
	}

	public boolean kannKleinerWerden() {
		return kannKleinerWerden;
	}

	public boolean isNichtAngegeben() {
		return kannGroesserWerden() || kannKleinerWerden();
	}

	private boolean istPositiv() {
		return zahl.signum() > 0;
	}

	private boolean istNegativ() {
		return zahl.signum() < 0;
	}

	public boolean equals(Object other) {
		if (other == null || (other.getClass() != getClass())) {
			return false;
		}
		if (this == other) {
			return true;
		}
		VkBigDecimal o = (VkBigDecimal) other;
		if (this.wertUngueltig != o.wertUngueltig) {
			return false;
		}
		if (this.isEmpty != o.isEmpty) {
			return false;
		}
		// For equality, we do not care about the scale but BigDecimal#equals does. So set to the same scale before checking equality
		// we avoid setScale when possible, because it seems to be expensive (5x execution time for VieleUSBs_negativ #1 without optimization)
		BigDecimal a = this.getBigDecimalValue();
		BigDecimal b = o.getBigDecimalValue();
		int scaleA = a.scale();
		int scaleB = b.scale();
		int scaleCommon = Math.min(Math.max(scaleA, scaleB), DEFAULT_SCALE);
		a = scaleA == scaleCommon ? a : a.setScale(scaleCommon, RoundingMode.HALF_UP);
		b = scaleB == scaleCommon ? b : b.setScale(scaleCommon, RoundingMode.HALF_UP);
		return a.equals(b);
	}

	public int hashCode() {
		final int prime = 37;
		int result = 1;
		result = prime * result + (wertUngueltig ? 1 : 0);
		result = prime * result + (isEmpty ? 1 : 0);
		// here we have to use the same scale as in the equals method
		BigDecimal bigDecimalWithDefaultScale = this.getBigDecimalValue().setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
		result = prime * result + bigDecimalWithDefaultScale.hashCode();

		return result;
	}

	public String toString() {
		if (wertUngueltig) {
			return "Ungültiger Wert";
		}
		return zahl.toPlainString() + (isEmpty ? "(Empty)" : "");
	}

	@Override
	public boolean hasValue() {
		// the below condition should be true, but we currently
		// don't want new assertions in the runtime:
		// assert !isEmpty || (kannGroesserWerden && kannKleinerWerden);
		return !isEmpty;
	}
}
