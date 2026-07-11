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
package com.mgmtp.a12.kernel.md.model.api.fieldtypes;

import java.util.Date;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.utils.NotInD;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * For {@link IField}s containing {@link Date} values.
 */
@OnlyForUsage
public interface IDateType extends IFieldType {

	/**
	 * Defines which date parts (day, month and/or year) may be optional/unknown. Following inputs
	 * are allowed for unknown date parts:
	 * <ul>
	 * <li>{@code 00} for day and month</li>
	 * <li>{@code 0000} for year</li>
	 * </ul>
	 */
	public enum DatePrecision {
		/**
		 * This is the default value. In this case, all the three date parts must be specified.
		 */
		FULL,
		/**
		 * Allows that the day part may be unknown, e.g. {@code 2021-02-00} would be a valid input
		 * (fully known dates are also valid).
		 */
		DAY_OPTIONAL,
		/**
		 * Allows that the month part may be unknown. If the month is unknown, the day must be
		 * unknown as well, e.g. {@code 2021-00-00} and {@code 2021-02-00} would be valid inputs
		 * (fully known dates are also valid).
		 */
		MONTH_OPTIONAL,
		/**
		 * Allows that all three date parts may be unknown. If the year is unknown, the month and
		 * the day must be unknown as well, the logic of {@link #MONTH_OPTIONAL} applies also in
		 * this case, e.g. {@code 0000-00-00}, {@code 2021-00-00} and {@code 2021-02-00} would be
		 * valid inputs (fully known dates are also valid).
		 */
		YEAR_OPTIONAL
	}

	/**
	 * @return the format of the date in string format. Never {@code null}.
	 */
	@NonNull String getFormat();

	/**
	 * @return {@code true} if a date before {@code 1900-01-01} should be considered as a wrong
	 * 		input, otherwise {@code false}. The default value is {@code false}.
	 */
	boolean isYoungerThan1900Check();

	/**
	 * @return precision of the date which is allowed for the field. Never {@code null}.
	 */
	@NonNull DatePrecision getDatePrecision();

	/**
	 * @return {@code true} if the {@link DatePrecision} of {@code this} is other than
	 *        {@link DatePrecision#FULL}. Otherwise {@code false}.
	 */
	default boolean arePartiallyKnownDatesAllowed() {
		return getDatePrecision() != DatePrecision.FULL;
	}

	/**
	 * @return an empty optional or a custom date format which specifies all three date fragments
	 * 		but is not the standard format 'yyyy-MM-dd'. Examples of such formats in ExtMP are
	 * 		dd.MM.yyyy and yyyyMMdd.
	 */
	@NotInD
	Optional<String> getNotInDCustomFormat();

}
