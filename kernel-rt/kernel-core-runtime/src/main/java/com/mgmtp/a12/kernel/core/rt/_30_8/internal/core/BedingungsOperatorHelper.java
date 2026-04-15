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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.core;

import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.CollectionWithCheapMergeCopy;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DateUtil;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.VkBigDecimal;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.VkDate;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.VkDateRange;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtInternalIdentifier;

/**
 * Klasse dient als Wrapper für eine Menge von Hilfsmethoden für die Auswertung
 * von Bedingungen.
 * Die Klasse wird nur vom MainValidatorController verwendet und
 * dient dazu, Hilfsfunktionalität in eine andere Klasse auszulagern.
 */

public class BedingungsOperatorHelper {

	private static final String ERROR_UNKNOWN_COMPARISON_OPERATOR = "Error during code generation: Unknown comparison operator <%s>.";
	private static final long MILLISECONDS_PER_SECOND = 1000L;
	private static final long MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * 60;
	private static final long MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * 60;
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
	 * @param calendarField
	 * 		- eine der Konstanten Calendar.YEAR, Calendar.MONTH,
	 * 		Calendar.DAY_OF_MONTH
	 * @return - die Differenz
	 */
	public static VkBigDecimal getDatumsDiff(VkDate date1, VkDate date2, int calendarField, TimeZone timeZone) {
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
			Calendar cal1 = getCalendarFromDatum(date1.getDate(), timeZone);
			Calendar cal2 = getCalendarFromDatum(date2.getDate(), timeZone);
			value = getCalenderDiff(cal1, cal2, calendarField);
		}

		return new VkBigDecimal(value, nichtPruefungsrelevant, kannGroesserWerden, kannKleinerWerden,
				date1.getOrigins().merge(date2.getOrigins()));
	}

	// Hilfsmethode für die Berechnung von Datumsdifferenzen
	private static long getCalenderDiff(Calendar cal1, Calendar cal2, int calendarField) {
		Calendar datum1;
		Calendar datum2;
		int multiplikator = 1;
		if (cal1.before(cal2)) {
			datum1 = (Calendar) cal1.clone();
			datum2 = (Calendar) cal2.clone();
		} else {
			datum1 = (Calendar) cal2.clone();
			datum2 = (Calendar) cal1.clone();
			multiplikator = -1;
		}

		if (calendarField == Calendar.MONTH) {
			return multiplikator * (long) getDifferenzInMonaten(datum1, datum2);
		}
		if (calendarField == Calendar.YEAR) {
			return multiplikator * (long) getDifferenzInJahren(datum1, datum2);
		}
		if (calendarField == Calendar.HOUR_OF_DAY) {
			return multiplikator * getDifferenceInHours(datum1, datum2);
		}
		if (calendarField == Calendar.MINUTE) {
			return multiplikator * getDifferenceInMinutes(datum1, datum2);
		}
		if (calendarField == Calendar.SECOND) {
			return multiplikator * getDifferenceInSeconds(datum1, datum2);
		}
		return multiplikator * (long) getDifferenzInTagen(datum1, datum2);
	}

	/**
	 * Gibt die Differenz zwischen zwei Datumswerten in Jahren zurück.
	 * Dabei wird angenommen, daß datum1 nicht nach datum2 liegt.
	 */
	private static int getDifferenzInJahren(Calendar datum1, Calendar datum2) {
		//berechne zunächst die grobe Jahresdifferenz; da datum2 nicht mehr vor datum1 liegen kann,
		//ist dieser Wert immer nicht-negativ
		int diff = datum2.get(Calendar.YEAR) - datum1.get(Calendar.YEAR);

		Calendar tmp = addiereJahre(datum1, diff);

		if (tmp.after(datum2)) {//eins zu weit gezählt
			return (diff - 1);
		}

		return diff;
	}

	private static Calendar addiereJahre(Calendar datum, int jahre) {
		Calendar result = createCalendarCopy(datum);
		result.add(Calendar.YEAR, jahre);

		if (istAchtUndZwanzigsterFebruar(datum) && istAchtUndZwanzigsterFebruar(result)) {
			if (!istInSchaltjahr(datum) && istInSchaltjahr(result)) {
				/*
				 * Wir wollen 28.2.1999 + 1 Jahr = 29.2.2000, Calendar macht aber 28.2.2000; das
				 * wird hier
				 * korrigiert
				 */
				result.add(Calendar.DAY_OF_MONTH, 1);
			} else if (istInSchaltjahr(datum) && !istInSchaltjahr(result)) {
				/*
				 * Da 2000 ein Schaltjahr ist, wollen wir 28.2.2000T00:00:01 + 1 Jahr =
				 * 28.2.2001T00:00:00,
				 * Calendar macht aber 28.2.2001T00:00:01; das wird hier korrigiert
				 */
				DateUtil.clearTime(result);
			}
		}
		return result;
	}

	private static boolean istAchtUndZwanzigsterFebruar(Calendar cal) {
		return cal.get(Calendar.MONTH) == Calendar.FEBRUARY && cal.get(Calendar.DAY_OF_MONTH) == 28;
	}

	private static boolean istInSchaltjahr(Calendar datum) {
		int jahr = datum.get(Calendar.YEAR);
		if ((jahr & 3) != 0) {
			return false;
		}

		return (jahr % 100 != 0) || (jahr % 400 == 0);
	}

	/**
	 * Gibt die Differenz zwischen zwei Datumswerten in Monaten zurück.
	 * Dabei wird angenommen, daß datum1 nicht nach datum2 liegt.
	 */
	private static int getDifferenzInMonaten(Calendar datum1, Calendar datum2) {

		//Die Monatsdifferenz wird über Jahre*12 berechnet
		int jahreDiff = getDifferenzInJahren(datum1, datum2);
		int monthDiff = 12 * jahreDiff;
		while (true) {
			// addiere zu datum1 so viele Monate, daß es später als datum2 ist
			//Monate sollen nicht einzeln hochgezählt werden, da es zu Fehlern führt:
			// (z.B. 31.1.2008 + 1 Monat + 1 Monat = 29.2.2008 + 1 Monat = 29.3.2008, aber 31.1.2008 + 2 Monate = 31.3.2008)
			//deshalb wird immer auf einer "frischen" Kopie von datum1 gerechnet und die bisher berechnete
			//Mindestdifferenz dazuaddiert
			Calendar tmp = addiereMonate(datum1, monthDiff);
			if (tmp.after(datum2)) {
				return (monthDiff - 1); //eins zu weit gezählt, ziehe es wieder ab
			}
			monthDiff++;
		}
	}

	private static Calendar addiereMonate(Calendar datum, int monate) {
		Calendar result = createCalendarCopy(datum);
		result.add(Calendar.MONTH, monate);
		if (istAchtUndZwanzigsterFebruar(datum) && istInSchaltjahr(datum)
				&& istAchtUndZwanzigsterFebruar(result) && !istInSchaltjahr(result)) {
			/*
			 * Da 2000 ein Schaltjahr ist, wollen wir 28.2.2000T00:00:01 + 12 Monate =
			 * 28.2.2001T00:00:00,
			 * Calendar macht aber 28.2.2001T00:00:01; das wird hier korrigiert
			 */
			DateUtil.clearTime(result);
		}
		return result;
	}

	/**
	 * Gibt die Differenz zwischen zwei Datumswerten in Tagen zurück.
	 * Dabei wird angenommen, daß datum1 nicht nach datum2 liegt.
	 */
	private static int getDifferenzInTagen(Calendar datum1, Calendar datum2) {
		int jahreDiff = getDifferenzInJahren(datum1, datum2);
		// Initialize with some lower bound. Lap years don't matter for that.
		int tageDiff = jahreDiff * 365;
		datum1.add(Calendar.DAY_OF_MONTH, tageDiff);
		while (!datum1.after(datum2)) {
			datum1.add(Calendar.DAY_OF_MONTH, 1);
			tageDiff++;
		}
		return (tageDiff - 1);//1 zu weit gezählt, da beim Schleifenabbruch
		//datum1 zum ersten mal nach datum2 lag
	}

	private static long getDifferenceInHours(Calendar datum1, Calendar datum2) {
		return (datum2.getTimeInMillis() - datum1.getTimeInMillis()) / MILLISECONDS_PER_HOUR;
	}

	private static long getDifferenceInMinutes(Calendar datum1, Calendar datum2) {
		return (datum2.getTimeInMillis() - datum1.getTimeInMillis()) / MILLISECONDS_PER_MINUTE;
	}

	private static long getDifferenceInSeconds(Calendar datum1, Calendar datum2) {
		return (datum2.getTimeInMillis() - datum1.getTimeInMillis()) / MILLISECONDS_PER_SECOND;
	}

	private static Calendar createCalendarCopy(Calendar datum1) {
		return (Calendar) datum1.clone();
	}

	public static VkDate addTime(VkDate date, VkBigDecimal amount, int calendarField, TimeZone timeZone) {
		boolean nichtPruefungsrelevant = date.isNichtPruefungsrelevant() || amount.isNichtPruefungsrelevant();
		boolean nichtAngegeben = date.isNichtAngegeben() || amount.kannGroesserWerden() || amount.kannKleinerWerden();

		if (nichtPruefungsrelevant) {
			return VkDate.NON_RELEVANT_DATE;
		} else if (!date.hasValue() || amount.wertIstUngueltig()) {
			//return new VkDate(null, nichtAngegeben, !nichtAngegeben); // warum !nichtAngegeben???
			return new VkDate(null, nichtAngegeben, nichtPruefungsrelevant,
					CollectionWithCheapMergeCopy.empty());
		} else {
			Calendar cal = getCalendarFromDatum(date.getDate(), timeZone);
			Calendar result = addTimeInternal(cal, amount.intValue(), calendarField);
			return new VkDate(result.getTime(), nichtAngegeben, nichtPruefungsrelevant,
					date.getOrigins().merge(amount.getOrigins()));
		}
	}

	private static Calendar addTimeInternal(Calendar cal, int amount, int calendarField) {
		if (calendarField == Calendar.YEAR) {
			return addiereJahre(cal, amount);
		} else if (calendarField == Calendar.MONTH) {
			return addiereMonate(cal, amount);
		} else {
			cal.add(calendarField, amount);
			return cal;
		}
	}

	/**
	 * Gibt Tag, Monat, Jahr, Stunden, Minuten oder Sekunden eines Datums zurück.
	 *
	 * @param datum
	 * 		- das Datum, aus dem ein Teil extrahiert wird
	 * @param calendarField
	 * 		- einer der Werte Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
	 * 		Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND
	 * @return - der gesuchte Datumsteil
	 */
	public static VkBigDecimal getDatumsTeil(VkDate datum, int calendarField, TimeZone timeZone) {
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
			Calendar cal = getCalendarFromDatum(datum.getDate(), timeZone);
			if (calendarField == Calendar.MONTH) {
				value = (long) cal.get(Calendar.MONTH) + 1;
			} else {
				value = cal.get(calendarField);
			}
			origins = datum.getOrigins();
		}
		return new VkBigDecimal(value, nichtPruefungsrelevant, kannGroesserWerden, kannKleinerWerden, origins);
	}

	private static Calendar getCalendarFromDatum(Date datum, TimeZone timeZone) {
		Calendar cal = new GregorianCalendar(timeZone);
		cal.setTime(datum);
		return cal;
	}

}
