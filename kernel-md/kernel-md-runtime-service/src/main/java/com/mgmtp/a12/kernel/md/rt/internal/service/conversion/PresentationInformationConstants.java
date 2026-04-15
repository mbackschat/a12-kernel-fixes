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

package com.mgmtp.a12.kernel.md.rt.internal.service.conversion;

import static com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation.DAY_MONTH_YEAR;
import static com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation.MONTH_DAY_YEAR;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.base.utils.internal.FieldTypeConstants;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;

/**
 * Following values about decimal and thousands separator were taken from
 * https://en.wikipedia.org/wiki/Decimal_separator
 */
class PresentationInformationConstants {

	private static final String STANDARD_DE_DATE_FORMAT = "dd.MM.yyyy";
	private static final String STANDARD_EN_DATE_FORMAT = "MM/dd/yyyy";

	private PresentationInformationConstants() {
		throw new IllegalStateException("Utility class");
	}

	private static final String TIME_FORMAT = FieldTypeConstants.DEFAULT_TIME_FORMAT;
	private static final Map<Locale, PresentationInformation> PI_BY_LOCALE = new HashMap<>();

	static {
		PI_BY_LOCALE.put(Locale.ENGLISH, new PresentationInformationBuilder()
				.dateFormat(STANDARD_EN_DATE_FORMAT).dateFragmentOrder(MONTH_DAY_YEAR).dateSeparator("/")
				.decimalSeparator(".").thousandsSeparator(",").build());
		PI_BY_LOCALE.put(Locale.US, new PresentationInformationBuilder()
				.dateFormat(STANDARD_EN_DATE_FORMAT).dateFragmentOrder(MONTH_DAY_YEAR).dateSeparator("/")
				.decimalSeparator(".").thousandsSeparator(",").build()); // ExtMP supports en_US
		PI_BY_LOCALE.put(Locale.GERMAN, new PresentationInformationBuilder()
				.dateFormat(STANDARD_DE_DATE_FORMAT).dateFragmentOrder(DAY_MONTH_YEAR).dateSeparator(".")
				.decimalSeparator(",").thousandsSeparator(".").build());
		PI_BY_LOCALE.put(Locale.GERMANY, new PresentationInformationBuilder()
				.dateFormat(STANDARD_DE_DATE_FORMAT).dateFragmentOrder(DAY_MONTH_YEAR).dateSeparator(".")
				.decimalSeparator(",").thousandsSeparator(".").build()); // ExtMP supports de_DE
		PI_BY_LOCALE.put(Locale.FRENCH, new PresentationInformationBuilder()
				.dateFormat("dd/MM/yyyy").dateFragmentOrder(DAY_MONTH_YEAR).dateSeparator("/")
				.decimalSeparator(",").thousandsSeparator(" ").build());
		PI_BY_LOCALE.put(Locale.of("gb"), new PresentationInformationBuilder()
				.dateFormat("dd/MM/yyyy").dateFragmentOrder(DAY_MONTH_YEAR).dateSeparator("/")
				.decimalSeparator(".").thousandsSeparator(",").build());
		PI_BY_LOCALE.put(Locale.of("us"), new PresentationInformationBuilder()
				.dateFormat(STANDARD_EN_DATE_FORMAT).dateFragmentOrder(MONTH_DAY_YEAR).dateSeparator("/")
				.decimalSeparator(".").thousandsSeparator(",").build());
		PI_BY_LOCALE.put(Locale.of("nl"), new PresentationInformationBuilder()
				.dateFormat("dd-MM-yyyy").dateFragmentOrder(DAY_MONTH_YEAR).dateSeparator("-")
				.decimalSeparator(",").thousandsSeparator(".").build());
		PI_BY_LOCALE.put(Locale.of("ch"), new PresentationInformationBuilder()
				.dateFormat(STANDARD_DE_DATE_FORMAT).dateFragmentOrder(DAY_MONTH_YEAR).dateSeparator(".")
				.decimalSeparator(".").thousandsSeparator("'").build());
	}

	public static PresentationInformation get(Locale locale) {
		return PI_BY_LOCALE.get(locale);
	}

	private static class PresentationInformationBuilder {
		private PresentationInformation pi = new PresentationInformation();

		PresentationInformationBuilder() {
			// following presentation information is fixed at the moment
			pi.add(PresentationInformation.TIME_FORMAT, TIME_FORMAT);
			pi.add(PresentationInformation.DATE_RANGE_SEPARATOR, " ");
			pi.add(PresentationInformation.DATE_ZERO_OPTIONAL, PresentationInformation.TRUE);
			pi.add(PresentationInformation.CONVERT_CUSTOM_FIELD_TYPES_LIST, "");
			pi.add(PresentationInformation.CONVERT_CUSTOM_FIELD_TYPES_ALL, PresentationInformation.FALSE);
			pi.add(PresentationInformation.CONVERT_ENUMERATIONS, PresentationInformation.TRUE);
			pi.add(PresentationInformation.DECIMAL_PLACES_OPTIONAL, PresentationInformation.TRUE);
			pi.add(PresentationInformation.TRUE_VALUE, "true");
			pi.add(PresentationInformation.FALSE_VALUE, "false");
		}

		PresentationInformationBuilder decimalSeparator(String decimalSeparator) {
			pi.add(PresentationInformation.DECIMAL_SEPARATOR, decimalSeparator);
			return this;
		}

		PresentationInformationBuilder thousandsSeparator(String thousandsSeparator) {
			pi.add(PresentationInformation.THOUSANDS_SEPARATOR, thousandsSeparator);
			return this;
		}

		PresentationInformationBuilder dateSeparator(String dateSeparator) {
			pi.add(PresentationInformation.DATE_SEPARATOR, dateSeparator);
			return this;
		}

		PresentationInformationBuilder dateFragmentOrder(String dateFragmentOrder) {
			pi.add(PresentationInformation.DATE_FRAGMENT_ORDERING, dateFragmentOrder);
			return this;
		}

		PresentationInformationBuilder dateFormat(String dateFormat) {
			pi.add(PresentationInformation.DATE_TIME_FORMAT, dateFormat + " " + TIME_FORMAT);
			return this;
		}

		PresentationInformation build() {
			return pi;
		}
	}

}
