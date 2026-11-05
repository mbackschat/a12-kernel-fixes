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

package com.mgmtp.a12.utils.conversion.a12internal;

import java.time.Instant;
import java.util.Date;

import com.mgmtp.a12.utils.conversion.InstantRange;

/**
 * Functionality for converting between field values of basic type V1 (e.g. {@link Date} for dates,
 * {@code Date[]} for date ranges) and V2 (e.g. {@link Instant} for dates, {@link InstantRange} for date ranges).
 * V1 types are used by the core-runtime layer ({@code IMetaField}); V2 types are used by {@code DocumentV2}.
 *
 * @deprecated Use methods that expect {@code DocumentV2} instead of V1 types where possible. This class will be removed with A12K-4179.
 */
@Deprecated(since = "30.0.0")
public class BasicTypeV1V2Conversion {
	public static Object basicTypeV1ToV2(Object value) {
		Object converted = switch (BasicTypeV1.tagOrThrow(value)) {
			case NULL, STRING, BOOLEAN, BIG_DECIMAL -> value;
			case DATE -> dateToInstant((Date) value);
			case DATERANGE_AS_ARRAY -> dateRangeArrayToInstantRange((Date[]) value);
		};
		assert BasicTypeV2.hasValidType(converted);
		return converted;
	}

	public static Object basicTypeV2ToV1(Object value) {
		Object converted = switch (BasicTypeV2.tagOrThrow(value)) {
			case NULL, STRING, BOOLEAN, BIG_DECIMAL -> value;
			case INSTANT -> instantToDate((Instant) value);
			case INSTANT_RANGE -> instantRangeToDateRangeArray((InstantRange) value);
		};
		assert BasicTypeV1.hasValidType(converted);
		return converted;
	}

	public static InstantRange dateRangeArrayToInstantRange(Date[] dateRange) {
		if (dateRange == null) {
			return null;
		}
		return InstantRange.of(dateToInstant(dateRange[0]), dateToInstant(dateRange[1]));
	}

	public static Date[] instantRangeToDateRangeArray(InstantRange instantRange) {
		if (instantRange == null) {
			return null;
		}
		return new Date[] { instantToDate(instantRange.start()), instantToDate(instantRange.end()) };
	}

	public static Instant dateToInstant(Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant();
	}

	public static Date instantToDate(Instant instant) {
		if (instant == null) {
			return null;
		}
		return Date.from(instant);
	}
}
