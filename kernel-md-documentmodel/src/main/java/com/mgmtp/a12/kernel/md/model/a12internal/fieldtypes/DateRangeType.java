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

package com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes;

import java.util.Optional;

import com.mgmtp.a12.kernel.core.base.utils.internal.FieldTypeConstants;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType.InterpretationOfYear;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * For {@link Field}s containing a date range.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DateRangeType extends FieldType {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @param rangeSeparator
	 * 		the date range separator. May be {@code null} to unset. In A12 is always "/"
	 * 		used. See {@link FieldTypeConstants#A12_DATE_RANGE_SEPARATOR} and
	 *        {@link IDateRangeType#getRangeSeparator()}.
	 */
	private String rangeSeparator = FieldTypeConstants.A12_DATE_RANGE_SEPARATOR;

	/**
	 * @return the date format for both parts of the range. Is never {@code null} or empty. See
	 *        {@link IDateRangeType#getFormat()}
	 * @param format
	 * 		the new date format for both parts of the range. May not be {@code null} or
	 * 		empty.
	 */
	private @NonNull String format;

	/**
	 * @deprecated youngerThan1900Check is no longer evaluated and its value is therefore irrelevant. This attribute  will be
	 * 		removed with A12K-4439.
	 */
	@Deprecated(since = "31.2.0")
	private boolean youngerThan1900Check = false;

	/**
	 * @param interpretationOfYear
	 * 		specifies how a year will be interpreted. See also
	 *        {@link IDateRangeType#getInterpretationOfYear()}.
	 */
	private InterpretationOfYear interpretationOfYear;

	/**
	 * @param a
	 * 		custom date format for both {@code from} and {@code to} dates of the range which
	 * 		differs to the supported formats in {@link #getFormat()}.
	 * 		See{@link IDateRangeType#getNotInDCustomFormat()}.
	 */
	private String notInDCustomFormat;

	/**
	 * @param notInDCustomRangeSeparator
	 * 		the range separator other than the one returned by {@link #getRangeSeparator()}.
	 * 		See also {@link IDateRangeType#getNotInDCustomRangeSeparator()}.
	 */
	private String notInDCustomRangeSeparator;

	/**
	 * @deprecated youngerThan1900Check is no longer evaluated and its value is therefore irrelevant. This attribute  will be
	 * 		removed with A12K-4439.
	 */
	@Deprecated(since = "31.2.0")
	@Builder(builderClassName = "DateRangeTypeBuilder")
	private DateRangeType(
			@NonNull String format,
			String rangeSeparator,
			@Deprecated(since = "31.2.0") // will be removed with A12K-4439
			boolean youngerThan1900Check,
			InterpretationOfYear interpretationOfYear,
			String notInDCustomFormat,
			String notInDCustomRangeSeparator
	) {
		this.format = format;
		this.rangeSeparator = rangeSeparator;
		this.youngerThan1900Check = youngerThan1900Check;
		this.interpretationOfYear = interpretationOfYear;
		this.notInDCustomFormat = notInDCustomFormat;
		this.notInDCustomRangeSeparator = notInDCustomRangeSeparator;
	}

	/**
	 * @return how a year will be interpreted.
	 */
	public Optional<InterpretationOfYear> getInterpretationOfYear() {
		return Optional.ofNullable(interpretationOfYear);
	}

	/**
	 * @return an empty optional or a custom date format for both {@code from} and {@code to} dates
	 * 		of the range which differs to the supported formats in {@link #getFormat()}.
	 * 		See{@link IDateRangeType#getNotInDCustomFormat()}.
	 */
	public Optional<String> getNotInDCustomFormat() {
		return Optional.ofNullable(notInDCustomFormat);
	}

	/**
	 * @return an empty optional or a range separator other than the one returned by
	 *        {@link #getRangeSeparator()}.
	 */
	public Optional<String> getNotInDCustomRangeSeparator() {
		return Optional.ofNullable(notInDCustomRangeSeparator);
	}

	public static class DateRangeTypeBuilder {
		@Deprecated(since = "31.2.0") // will be removed with A12K-4439
		private boolean youngerThan1900Check = false; // We set the default value explicitly.
		private String rangeSeparator = FieldTypeConstants.A12_DATE_RANGE_SEPARATOR; // Default separator
	}
}
