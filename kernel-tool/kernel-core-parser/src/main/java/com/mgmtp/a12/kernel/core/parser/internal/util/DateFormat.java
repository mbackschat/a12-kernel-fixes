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

package com.mgmtp.a12.kernel.core.parser.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Order- and separator-independent date format that represents which of the components
 * (day, month, year, hour, minute, second) are present in a date, but not concrete format strings;
 * ie yyyyMMdd and dd-MM-yyyy would be equivalent.
 * <p>
 * OperationResultType.getDateFormat returns a format string like
 * "dd.MM.yyyy". In most Situations where the format is used, however, we are only interested in
 * the components (d, M, y etc) that are present in the date representation.
 * In other cases, such as comparisons and Max/Min (and MaxWert/MinWert), we would want to treat
 * all dates as comparable that have the same set of components, and not distinguish e.g.
 * dd.MM.yyyy and yyyyMMdd.
 * This class is intended to model this order-indepentent date-format concept.
 * <p>
 * (A separate but related concept is IDateType (extends IFieldType), which models dates with
 * optional fields and
 * explicit concrete-order format string, but not dates-with-fixed-set-of-fields but arbitrary
 * ordering.)
 */
public class DateFormat {
	public enum DatetimeComponent {
		YEAR,
		MONTH,
		DAY,
		HOUR,
		MINUTE,
		SECOND,
	}

	private Set<DatetimeComponent> components = new HashSet<>();
	private String concreteFormat = null;

	public static DateFormat of(String format) {
		return new DateFormat(format);
	}

	private static Map<String, DatetimeComponent> stringCodes;

	static {
		stringCodes = new HashMap<>();
		stringCodes.put("y", DatetimeComponent.YEAR);
		stringCodes.put("M", DatetimeComponent.MONTH);
		stringCodes.put("d", DatetimeComponent.DAY);
		stringCodes.put("H", DatetimeComponent.HOUR);
		stringCodes.put("m", DatetimeComponent.MINUTE);
		stringCodes.put("s", DatetimeComponent.SECOND);

	}

	private DateFormat(String format) {
		if (format == null) {
			throw new IllegalArgumentException();
		}
		concreteFormat = format;

		// können wir irgendwie auf unerwartete/nicht erkannte Bestandteile prüfen?
		components = new HashSet<>();
		for (Map.Entry<String, DatetimeComponent> e : stringCodes.entrySet()) {
			if (format.contains(e.getKey())) {
				components.add(e.getValue());
			}
		}
	}

	private DateFormat(Set<DatetimeComponent> components) {
		this.concreteFormat = null;
		this.components = new HashSet<>(components);
	}

	public boolean hasComponent(DatetimeComponent c) {
		return components.contains(c);
	}

	/**
	 * Tests whether the components of this DateFormat contains all components of the other format or more.
	 *
	 * @param other
	 * 		the date format to be compared.
	 * @return
	 */
	public boolean hasAllComponents(DateFormat other) {
		return components.containsAll(other.components);
	}

	public boolean hasSameComponents(DateFormat other, boolean hasBaseYear) {
		if (hasBaseYear) {
			Set<DatetimeComponent> c1 = new HashSet<>(components);
			Set<DatetimeComponent> c2 = new HashSet<>(other.components);
			c1.add(DatetimeComponent.YEAR);
			c2.add(DatetimeComponent.YEAR);
			return c1.equals(c2);
		} else {
			return components.equals(other.components);
		}
	}

	public boolean hasSameKnownFormatStringIfExists(DateFormat other) {
		Optional<String> f1 = getConcretFormatIfExists();
		Optional<String> f2 = other.getConcretFormatIfExists();
		return f1.isPresent() && f2.isPresent() && f1.get().equals(f2.get());
	}

	public Optional<String> getConcretFormatIfExists() {
		return concreteFormat != null ? Optional.of(concreteFormat) : Optional.empty();
	}

	public String description() {
		// not toString in order to avoid silent wrong behavior in places that still expect date formats to be plain strings
		Optional<String> format = getConcretFormatIfExists();
		if (format.isPresent()) {
			return format.get();
		} else {
			return "DateFormat (unordered, like " + canonicalStringRepr() + ")";
		}
	}

	private String canonicalStringRepr() {
		List<String> cs = new ArrayList<>();
		for (DatetimeComponent c : DatetimeComponent.values()) {
			if (hasComponent(c)) {
				cs.add(c.toString());
			}
		}
		return String.join("-", cs);
	}

	/**
	 * Create a DateFormat with the same order-independent components, but erasing the concrete
	 * format string of this DateFormat (if present).
	 * Intended for cases like MaxWert, where only the order-independent return type can be known,
	 * but not a concrete format string
	 * of one of the inputs.
	 */
	public DateFormat asPureOrderIndependent() {
		return new DateFormat(components);
	}

	/**
	 * Returns true, if the date formats are compatible.
	 * This includes the compatibility regarding year fragments and date/time fragments.
	 */
	public static boolean formatsConsistentRegardingFormat(DateFormat format1, DateFormat format2, boolean hasBaseYear) {
		// TODO prüfen ob die weniger scharfe Bedingung von formatsConsistentRegardingFormat bei bisherigen Verwendungen tatsächlich gewollt ist.
		// (z.B. Vergleichbarkeit yyyyMMdd <-> yyyyMM)
		return formatsConsistentRegardingYearSpec(format1, format2, hasBaseYear)
				&& formatsConsistentRegardingDateTimeFormat(format1, format2);
	}

	/**
	 * returns true, if either both date formats contain the year fragment or no date format
	 * contains the year fragment.
	 * in case of ExtMP World: special handling of Referenzjahr.
	 */
	private static boolean formatsConsistentRegardingYearSpec(DateFormat format1, DateFormat format2, boolean hasBaseYear) {
		//in Modellen mit Referenzjahr kann die Jahresangabe hinzugefügt werden.
		if (hasBaseYear) {
			return true;
		}
		return (format1.hasComponent(DatetimeComponent.YEAR) == format2.hasComponent(DatetimeComponent.YEAR));
	}

	/**
	 * Returns <code>true</code>, if both formats contain date information or none of them.
	 */
	private static boolean formatsConsistentRegardingDateTimeFormat(DateFormat format1, DateFormat format2) {
		boolean format1containsDate = isFormatWithDateSpecification(format1);
		boolean format2containsDate = isFormatWithDateSpecification(format2);

		return format1containsDate == format2containsDate;
	}

	public static boolean isValidDateTimeFormat(DateFormat dateFormat) {
		if (!dateFormat.hasComponent(DatetimeComponent.YEAR)) {
			return false;
		}
		if (!dateFormat.hasComponent(DatetimeComponent.MONTH)) {
			return false;
		}
		if (!dateFormat.hasComponent(DatetimeComponent.DAY)) {
			return false;
		}
		if (!dateFormat.hasComponent(DatetimeComponent.HOUR)) {
			return false;
		}
		if (!dateFormat.hasComponent(DatetimeComponent.MINUTE)) {
			return false;
		}
		return dateFormat.hasComponent(DatetimeComponent.SECOND);
	}

	/**
	 * @return <code>true</code> if the format contains any date specifier (H, m, s).
	 */
	public static boolean isFormatWithTimeSpecification(DateFormat timeFormat) {
		if (timeFormat == null) {
			return false;
		}
		return timeFormat.hasComponent(DatetimeComponent.HOUR) || timeFormat.hasComponent(
				DatetimeComponent.MINUTE) || timeFormat.hasComponent(DatetimeComponent.SECOND);
	}

	/**
	 * @return <code>true</code> if the format contains any date specifier (y, M, d).
	 */
	public static boolean isFormatWithDateSpecification(DateFormat format) {
		if (format == null) {
			return false;
		}
		return format.hasComponent(DatetimeComponent.YEAR) || format.hasComponent(DatetimeComponent.MONTH) || format.hasComponent(
				DatetimeComponent.DAY);
	}

	public static boolean isTimeFragment(DateFormat.DatetimeComponent opFragment) {
		return opFragment == DatetimeComponent.HOUR || opFragment == DatetimeComponent.MINUTE || opFragment == DatetimeComponent.SECOND;
	}

}
