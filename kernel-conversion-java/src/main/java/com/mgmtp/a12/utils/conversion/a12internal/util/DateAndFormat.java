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

package com.mgmtp.a12.utils.conversion.a12internal.util;

import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.model.utils.Experimental;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil.DateInterval;

import lombok.Getter;
import lombok.NonNull;

/**
 * The class bundles the common use of the value and format of a date.
 */
@Experimental
public class DateAndFormat {

	@NonNull private String dateString;
	@NonNull private String dateFormat;
	@Getter private final TimeZone timeZone;

	/**
	 * It is assumed that the value and format match.
	 * This means in particular that all parameters are not empty and not undefined.
	 */
	public DateAndFormat(@NonNull String value, @NonNull String format, TimeZone timeZone) {
		this.dateString = value;
		this.dateFormat = format;
		this.timeZone = timeZone;
	}

	/**
	 * If the date format does not contain the year part, the format is supplemented with the year format
	 * and the date string with the specified year. Otherwise the object remains unchanged.
	 */
	public void completeWithYear(int year) {
		if (DatePart.YEAR.isInFormat(dateFormat) || (
				(DatePart.HOUR.isInFormat(dateFormat) || DatePart.MINUTE.isInFormat(dateFormat)) &&
						!DatePart.MONTH.isInFormat(dateFormat) && !DatePart.DAY.isInFormat(dateFormat))) {
			return;
		}

		String dateSeparator = DateUtil.getDateSeparator(dateFormat);
		// We should always add a date separator if we can to make parsing easier
		if (StringUtils.isEmpty(dateSeparator)) {
			dateSeparator = ".";
		}
		if (!dateFormat.endsWith(dateSeparator)) {
			dateFormat += dateSeparator;
			dateString += dateSeparator;
		}
		dateFormat += DateUtil.YEAR_FORMAT;
		dateString += String.format("%04d", year);
	}

	/**
	 * If the date format does not contain the month or day part, the format is supplemented with the month or day format
	 * and the date string with the specified month or day from the given date interval. Otherwise the object remains unchanged.
	 *
	 * @param intervalSpec
	 * 		specifies the start or the end of a date interval (earliest or latest possible date)
	 */
	public void addMonthAndDay(DateInterval intervalSpec) {
		if (DatePart.HOUR.isInFormat(dateFormat)) {
			return;
		}
		String dateSeparator = DateUtil.getDateSeparator(dateFormat);
		if (!DatePart.MONTH.isInFormat(dateFormat)) {
			dateFormat = String.format("%s%s%s", "MM", dateSeparator, dateFormat);
			dateString = String.format("%s%s%s", intervalSpec.getGivenMonth(), dateSeparator, dateString);
		}
		if (!DatePart.DAY.isInFormat(dateFormat)) {
			dateFormat = String.format("%s%s%s", "dd", dateSeparator, dateFormat);
			String tag = (intervalSpec == DateInterval.INTERVAL_START) ?
					"01" :
					DateUtil.getLastDayOfMonth(new DateAndFormat("01" + dateSeparator + dateString, dateFormat, timeZone));
			dateString = String.format("%s%s%s", tag, dateSeparator, dateString);
		}
	}

	/**
	 * Unify the last date separator in the value and the format of a date with the date separator specified in the format.
	 */
	public void unifyLastSeparator() {
		String dateSeparator = DateUtil.getDateSeparator(dateFormat);
		if (dateString.endsWith(dateSeparator) && !dateFormat.endsWith(dateSeparator)) {
			dateFormat += dateSeparator;
		}
		if (!dateString.endsWith(dateSeparator) && dateFormat.endsWith(dateSeparator)) {
			dateString += dateSeparator;
		}
	}

	public String getDate() {
		return dateString;
	}

	public String getFormat() {
		return dateFormat;
	}

	@Override
	public String toString() {
		return "Date = '" + dateString + "', Format = '" + dateFormat + "'";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateFormat == null) ? 0 : dateFormat.hashCode());
		result = prime * result + ((dateString == null) ? 0 : dateString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DateAndFormat other = (DateAndFormat) obj;
		if (dateFormat == null) {
			if (other.dateFormat != null) {
				return false;
			}
		} else if (!dateFormat.equals(other.dateFormat)) {
			return false;
		}
		if (dateString == null) {
			if (other.dateString != null) {
				return false;
			}
		} else if (!dateString.equals(other.dateString)) {
			return false;
		}
		return true;
	}

	public void setDate(String newDate) {
		this.dateString = newDate;
	}
}
