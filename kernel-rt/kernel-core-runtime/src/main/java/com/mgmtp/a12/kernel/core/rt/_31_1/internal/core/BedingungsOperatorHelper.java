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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.core;

import java.math.RoundingMode;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.CollectionWithCheapMergeCopy;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkBigDecimal;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkDate;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.VkDateRange;

/**
 * Klasse dient als Wrapper für eine Menge von Hilfsmethoden für die Auswertung
 * von Bedingungen.
 * Die Klasse wird nur vom MainValidatorController verwendet und
 * dient dazu, Hilfsfunktionalität in eine andere Klasse auszulagern.
 */

public class BedingungsOperatorHelper {

	private static final String ERROR_UNKNOWN_COMPARISON_OPERATOR = "Error during code generation: Unknown comparison operator <%s>.";
	private static final Map<String, Pattern> patternMap = new ConcurrentHashMap<>();

	/**
	 * Lazy Initalisation der verwendeten Patterns. Falls das Pattern noch nicht
	 * in der Map vorhanden, wird es in die Map eingefügt und zurückgegeben.
	 *
	 * @param regex
	 * 		- regulärer Ausdruck
	 * @return Pattern zu der Map.
	 */
	public static Pattern getPattern(String regex) {
		return patternMap.computeIfAbsent(regex, Pattern::compile);
	}

	private BedingungsOperatorHelper() {
		throw new IllegalStateException("Utility class");
	}

	public enum VglOp {
		GT,
		NE,
		LT,
		EQ,
		GE,
		LE,
		NE10,
		NE5,
		NE2,
		NE1,
		NM,
		WM;
	}

	/**
	 * Vergleich zwischen zwei Datumsangaben.
	 *
	 * @param op
	 * 		Vergleichsoperation
	 * @return true, falls die Bedingung zutrifft, in allen anderen Fällen (auch
	 * 		Fehlerfällen) wird false zurückgegeben. Andere Fälle sind
	 * 		insbesondere: kein Wert für Feld, oder UNKNOWN als Wert.
	 */
	public static boolean vergleicheDATUM(VglOp op, Date date1, Date date2) {
		// Wenn eine der Datumsangaben null ist -> gib immer false zurück
		if (date1 == null || date2 == null) {
			return false;
		}

		return switch (op) {
			case GT -> date1.after(date2);
			case NE -> !date1.equals(date2);
			case LT -> date2.after(date1);
			case EQ -> date1.equals(date2);
			case GE -> !date2.after(date1);
			case LE -> !date1.after(date2);
			default -> throw new IllegalArgumentException(String.format(ERROR_UNKNOWN_COMPARISON_OPERATOR, op));
		};
	}

	public static boolean vergleicheDATERANGE(VglOp op, VkDateRange d1, VkDateRange d2) {
		boolean vergleichStart = BedingungsOperatorHelper.vergleicheDATUM(op, d1.getStart(), d2.getStart());
		boolean vergleichEnd = BedingungsOperatorHelper.vergleicheDATUM(op, d1.getEnd(), d2.getEnd());

		return switch (op) {
			case NE -> vergleichStart || vergleichEnd;
			case EQ -> vergleichStart && vergleichEnd;
			case GT, LT, GE, LE -> throw new IllegalArgumentException(
					"Error during code generation: For DateRanges only the comparison operators '==' and '!=' are allowed.");
			case NE10, NE5, NE2, NE1 -> throw new IllegalArgumentException(
					"Error during code generation: Invocation of a comparison with tolerance on DateRange parameters.");
			default -> throw new IllegalArgumentException(String.format(ERROR_UNKNOWN_COMPARISON_OPERATOR, op));
		};
	}

	public static boolean vergleicheSTRING(VglOp op, String s1, String s2) {
		if (StringUtils.isEmpty(s1) || StringUtils.isEmpty(s2)) {
			return false;
		}

		//Cobertura:/alle VlgOp-Elemente werten getestet (NM geht auf default)
		return switch (op) {
			case NE -> !s1.equals(s2);
			case EQ -> s1.equals(s2);
			case GT, LT, GE, LE -> throw new IllegalArgumentException(
					"Error during code generation: For Strings only the comparison operators '==' and '!=' are allowed.");
			case NE10, NE5, NE2, NE1 -> throw new IllegalArgumentException(
					"Error during code generation: Invocation of a comparison with tolerance on String parameters.");
			default -> throw new IllegalArgumentException(String.format(ERROR_UNKNOWN_COMPARISON_OPERATOR, op));
		};
	}

	public static ValidierungsErgebnis vergleiche(VglOp op, VkBigDecimal s1, VkBigDecimal s2) {

		if (s1 == null || s2 == null || s1.wertIstUngueltig() || s2.wertIstUngueltig()
				|| s1.isNichtPruefungsrelevant() || s2.isNichtPruefungsrelevant()) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		VkBigDecimal value1WithDefaultScale = s1.setScale(VkBigDecimal.DEFAULT_SCALE, RoundingMode.HALF_UP);
		VkBigDecimal value2WithDefaultScale = s2.setScale(VkBigDecimal.DEFAULT_SCALE, RoundingMode.HALF_UP);

		int result = value1WithDefaultScale.compareTo(value2WithDefaultScale);
		switch (op) {
			case GT:
				if (result > 0) {
					if (s1.kannKleinerWerden() || s2.kannGroesserWerden()) {
						return ValidierungsErgebnis.TRUE_AF;
					}
					return ValidierungsErgebnis.TRUE_WF;
				}
				break;
			case NE:
				if (result != 0) {
					return getFehlerFuerNE(result, s1, s2);
				}
				break;
			case LT:
				if (result < 0) {
					if (s1.kannGroesserWerden() || s2.kannKleinerWerden()) {
						return ValidierungsErgebnis.TRUE_AF;
					}
					return ValidierungsErgebnis.TRUE_WF;
				}
				break;
			case EQ:
				if (result == 0) {
					if (s1.kannKleinerWerden() || s2.kannKleinerWerden() || s1.kannGroesserWerden() || s2.kannGroesserWerden()) {
						return ValidierungsErgebnis.TRUE_AF;
					}
					return ValidierungsErgebnis.TRUE_WF;
				}
				break;
			case GE:
				if (result >= 0) {
					if (s1.kannKleinerWerden() || s2.kannGroesserWerden()) {
						return ValidierungsErgebnis.TRUE_AF;
					}
					return ValidierungsErgebnis.TRUE_WF;
				}
				break;
			case LE:
				if (result <= 0) {
					if (s1.kannGroesserWerden() || s2.kannKleinerWerden()) {
						return ValidierungsErgebnis.TRUE_AF;
					}
					return ValidierungsErgebnis.TRUE_WF;
				}
				break;
			case NE10:
				if (value1WithDefaultScale.subtract(value2WithDefaultScale).abs().compareTo(new VkBigDecimal(10)) > 0) {
					return getFehlerFuerNE(result, s1, s2);
				}
				break;
			case NE5:
				if (value1WithDefaultScale.subtract(value2WithDefaultScale).abs().compareTo(new VkBigDecimal(5)) > 0) {
					return getFehlerFuerNE(result, s1, s2);
				}
				break;
			case NE2:
				if (value1WithDefaultScale.subtract(value2WithDefaultScale).abs().compareTo(new VkBigDecimal(2)) > 0) {
					return getFehlerFuerNE(result, s1, s2);
				}
				break;
			case NE1:
				if (value1WithDefaultScale.subtract(value2WithDefaultScale).abs().compareTo(new VkBigDecimal(1)) > 0) {
					return getFehlerFuerNE(result, s1, s2);
				}
				break;
			default:
				throw new IllegalArgumentException(String.format(ERROR_UNKNOWN_COMPARISON_OPERATOR, op));
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	private static ValidierungsErgebnis getFehlerFuerNE(int result, VkBigDecimal s1, VkBigDecimal s2) {
		if (result < 0) {
			if (s1.kannGroesserWerden() || s2.kannKleinerWerden()) {
				return ValidierungsErgebnis.TRUE_AF;
			}
			return ValidierungsErgebnis.TRUE_WF;
		} else { // result > 0
			if (s1.kannKleinerWerden() || s2.kannGroesserWerden()) {
				return ValidierungsErgebnis.TRUE_AF;
			}
			return ValidierungsErgebnis.TRUE_WF;
		}
	}

	/**
	 * Gibt die Differenz zwischen zwei Datumsangaben zurück. Die Differenz wird berechnet entweder
	 * <li>in Jahren, oder
	 * <li>in Monaten, oder
	 * <li>in Tagen
	 *
	 * <br>
	 * Kleinere Zeitangaben (Minuten, Sekunden etc.) bleiben bei der Differenzbildung
	 * unberücksichtigt. Liegt das erste Datum nach dem zweiten Datum, so wird die negative
	 * Differenz zurückgegeben.
	 *
	 * @param date1
	 * 		- erstes Datum
	 * @param date2
	 * 		- zweites Datum
	 * @param unit
	 * 		- eine der {@link ChronoUnit}s YEARS, MONTHS, DATES, HOURS, MINUTES, SECONDS.
	 * @return - die Differenz
	 */
	public static VkBigDecimal getDatumsDiff(VkDate date1, VkDate date2, ChronoUnit unit, TimeZone timeZone) {
		boolean nichtPruefungsrelevant = date1.isNichtPruefungsrelevant() || date2.isNichtPruefungsrelevant();
		boolean kannGroesserWerden = date1.isNichtAngegeben() || date2.isNichtAngegeben();
		boolean kannKleinerWerden = date1.isNichtAngegeben() || date2.isNichtAngegeben();

		if (nichtPruefungsrelevant) {
			return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
		}

		long value;
		if (!date1.hasValue() || !date2.hasValue()) {
			value = VkBigDecimal.DEFAULT_VALUE;
		} else {
			ZonedDateTime dateTime1 = date1.getDate().toInstant().atZone(timeZone.toZoneId());
			ZonedDateTime dateTime2 = date2.getDate().toInstant().atZone(timeZone.toZoneId());
			value = getDateDiff(dateTime1, dateTime2, unit);
		}

		return new VkBigDecimal(value, nichtPruefungsrelevant, kannGroesserWerden, kannKleinerWerden,
				date1.getOrigins().merge(date2.getOrigins()));
	}

	// Hilfsmethode für die Berechnung von Datumsdifferenzen
	private static long getDateDiff(ZonedDateTime dateTime1, ZonedDateTime dateTime2, ChronoUnit unit) {
		ZonedDateTime firstDate;
		ZonedDateTime secondDate;

		int multiplikator = 1;
		int comparison = dateTime1.compareTo(dateTime2);
		if (comparison < 0) {
			firstDate = dateTime1;
			secondDate = dateTime2;
		} else {
			firstDate = dateTime2;
			secondDate = dateTime1;
			multiplikator = -1;
		}

		if (unit == ChronoUnit.MONTHS) {
			return multiplikator * (long) getDifferenzInMonaten(firstDate, secondDate);
		}
		if (unit == ChronoUnit.YEARS) {
			return multiplikator * (long) getDifferenzInJahren(firstDate, secondDate);
		}
		return multiplikator * getStandardDiffInUnits(firstDate, secondDate, unit);
	}

	private static long getStandardDiffInUnits(ZonedDateTime firstDate, ZonedDateTime secondDate, ChronoUnit unit) {
		return unit.between(firstDate, secondDate);
	}

	/**
	 * Gibt die Differenz zwischen zwei Datumswerten in Jahren zurück.
	 * Dabei wird angenommen, daß datum1 nicht nach datum2 liegt.
	 */
	private static long getDifferenzInJahren(ZonedDateTime datum1, ZonedDateTime datum2) {
		//berechne zunächst die grobe Jahresdifferenz; da datum2 nicht mehr vor datum1 liegen kann,
		//ist dieser Wert immer nicht-negativ
		long diff = datum2.getYear() - datum1.getYear();

		ZonedDateTime tmp = addiereJahre(datum1, diff);

		if (tmp.compareTo(datum2) > 0) {//eins zu weit gezählt
			return (diff - 1);
		}

		return diff;
	}

	private static ZonedDateTime addiereJahre(ZonedDateTime datum, long jahre) {
		ZonedDateTime result = datum.plusYears(jahre);

		if (istAchtUndZwanzigsterFebruar(datum) && istAchtUndZwanzigsterFebruar(result)) {
			if (!istInSchaltjahr(datum) && istInSchaltjahr(result)) {
				/*
				 * Wir wollen 28.2.1999 + 1 Jahr = 29.2.2000, die Addition liefert aber 28.2.2000; das
				 * wird hier korrigiert
				 */
				result = result.plusDays(1);
			} else if (istInSchaltjahr(datum) && !istInSchaltjahr(result)) {
				/*
				 * Da 2000 ein Schaltjahr ist, wollen wir 28.2.2000T00:00:01 + 1 Jahr =
				 * 28.2.2001T00:00:00,
				 * Die Addition liefert aber 28.2.2001T00:00:01; das wird hier korrigiert
				 */
				result = result.toLocalDate().atStartOfDay(datum.getZone());
			}
		}
		return result;
	}

	private static boolean istAchtUndZwanzigsterFebruar(ZonedDateTime dateTime) {
		return dateTime.getMonth() == Month.FEBRUARY && dateTime.getDayOfMonth() == 28;
	}

	private static boolean istInSchaltjahr(ZonedDateTime datum) {
		return datum.toLocalDate().isLeapYear();
	}

	/**
	 * Gibt die Differenz zwischen zwei Datumswerten in Monaten zurück.
	 * Dabei wird angenommen, daß datum1 nicht nach datum2 liegt.
	 */
	private static long getDifferenzInMonaten(ZonedDateTime datum1, ZonedDateTime datum2) {

		//Die Monatsdifferenz wird über Jahre*12 berechnet
		long jahreDiff = getDifferenzInJahren(datum1, datum2);
		long monthDiff = 12 * jahreDiff;
		while (true) {
			// addiere zu datum1 so viele Monate, daß es später als datum2 ist
			//Monate sollen nicht einzeln hochgezählt werden, da es zu Fehlern führt:
			// (z.B. 31.1.2008 + 1 Monat + 1 Monat = 29.2.2008 + 1 Monat = 29.3.2008, aber 31.1.2008 + 2 Monate = 31.3.2008)
			//deshalb wird immer auf einer "frischen" Kopie von datum1 gerechnet und die bisher berechnete
			//Mindestdifferenz dazuaddiert
			ZonedDateTime tmp = addiereMonate(datum1, monthDiff);
			if (tmp.compareTo(datum2) > 0) {
				return (monthDiff - 1); //eins zu weit gezählt, ziehe es wieder ab
			}
			monthDiff++;
		}
	}

	private static ZonedDateTime addiereMonate(ZonedDateTime datum, long monate) {
		ZonedDateTime result = datum.plusMonths(monate);
		if (istAchtUndZwanzigsterFebruar(datum) && istInSchaltjahr(datum)
				&& istAchtUndZwanzigsterFebruar(result) && !istInSchaltjahr(result)) {
			/*
			 * Da 2000 ein Schaltjahr ist, wollen wir 28.2.2000T00:00:01 + 12 Monate =
			 * 28.2.2001T00:00:00,
			 * Die Addition liefert aber 28.2.2001T00:00:01; das wird hier korrigiert
			 */
			result = result.toLocalDate().atStartOfDay(datum.getZone());
		}
		return result;
	}

	public static VkDate addTime(VkDate date, VkBigDecimal amount, ChronoUnit unit, TimeZone timeZone) {
		boolean nichtPruefungsrelevant = date.isNichtPruefungsrelevant() || amount.isNichtPruefungsrelevant();
		boolean nichtAngegeben = date.isNichtAngegeben() || amount.kannGroesserWerden() || amount.kannKleinerWerden();

		if (nichtPruefungsrelevant) {
			return VkDate.NON_RELEVANT_DATE;
		} else if (!date.hasValue() || amount.wertIstUngueltig()) {
			//return new VkDate(null, nichtAngegeben, !nichtAngegeben); // warum !nichtAngegeben???
			return new VkDate(null, nichtAngegeben, nichtPruefungsrelevant,
					CollectionWithCheapMergeCopy.empty());
		} else {
			ZonedDateTime dateTime = date.getDate().toInstant().atZone(timeZone.toZoneId());
			ZonedDateTime result = addTimeInternal(dateTime, amount.intValue(), unit);
			return new VkDate(Date.from(result.toInstant()), nichtAngegeben, nichtPruefungsrelevant,
					date.getOrigins().merge(amount.getOrigins()));
		}
	}

	private static ZonedDateTime addTimeInternal(ZonedDateTime dateTime, int amount, ChronoUnit unit) {
		return switch (unit) {
			case ChronoUnit.YEARS ->  addiereJahre(dateTime, amount);
			case ChronoUnit.MONTHS -> addiereMonate(dateTime, amount);
			default -> dateTime.plus(amount, unit);
		};
	}

	/**
	 * Gibt Tag, Monat, Jahr, Stunden, Minuten oder Sekunden eines Datums zurück.
	 *
	 * @param datum
	 * 		- das Datum, aus dem ein Teil extrahiert wird
	 * @param dateField
	 * 		- einer der Werte ChronoField.YEAR_OF_ERA, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH,
	 * 		ChronoField.HOUR_OF_DAY, ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE
	 * @return - der gesuchte Datumsteil
	 */
	public static VkBigDecimal getDatumsTeil(VkDate datum, TemporalField dateField, TimeZone timeZone) {
		boolean nichtPruefungsrelevant = datum.isNichtPruefungsrelevant();
		boolean kannGroesserWerden = datum.isNichtAngegeben();
		boolean kannKleinerWerden = datum.isNichtAngegeben();

		if (nichtPruefungsrelevant) {
			return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
		}

		long value;
		CollectionWithCheapMergeCopy<RtInternalIdentifier> origins;
		if (!datum.hasValue()) {
			value = VkBigDecimal.DEFAULT_VALUE;
			origins = CollectionWithCheapMergeCopy.empty();
		} else {
			ZonedDateTime dateTime = datum.getDate().toInstant().atZone(timeZone.toZoneId());
			value = dateTime.get(dateField);
			origins = datum.getOrigins();
		}
		return new VkBigDecimal(value, nichtPruefungsrelevant, kannGroesserWerden, kannKleinerWerden, origins);
	}
}
