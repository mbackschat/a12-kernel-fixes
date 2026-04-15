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

import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModelInfo;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.utils.NotInD;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * For {@link IField}s containing a date range.
 */
@OnlyForUsage
public interface IDateRangeType extends IFieldType {

	public enum InterpretationOfYear {
		FROM, // baseYear is set to start and baseYear+1 is set to end (SUBSEQUENT/Folgejahr/TT.MM-TT.MMvonReferenzjahr)
		TO // baseYear-1 is set to start and baseYear is set to end (PREVIOUS/Vorjahr/TT.MM-TT.MMbisReferenzjahr)
	}

	/**
	 * @return the separator of the two dates spanning the date range. In A12 it is always "/".
	 */
	@NonNull String getRangeSeparator();

	/**
	 * Following date formats are supported:
	 * <ul>
	 * <li>MM</li>
	 * <li>yyyy</li>
	 * <li>yyyy-MM</li>
	 * <li>MM-dd</li>
	 * <li>yyyy-MM-dd</li>
	 * </ul>
	 *
	 * @return the date format for both parts of the range. Is never {@code null} or empty.
	 */
	@NonNull String getFormat();

	/**
	 * @return {@code true} if ({@code from} and/or {@code to}) dates before {@code 1900-01-01}
	 * 		should be considered as a wrong input, otherwise {@code false}. The default value is
	 *        {@code false}.
	 */
	boolean isYoungerThan1900Check();

	/**
	 * This property has an effect only if the format 'MM-dd' is set at {@link #getFormat()} and a
	 * base year is defined (see {@link IDocumentModelInfo#getBaseYear()}).
	 *
	 * @return how the year part of the {@code from} and {@code to} dates should be interpreted (for
	 * 		more details see the validation language documentation).
	 */
	Optional<InterpretationOfYear> getInterpretationOfYear();

	/**
	 * @return an empty optional or a custom date format for both {@code from} and {@code to} dates
	 * 		of the range which differs to the supported formats in {@link #getFormat()}. Examples
	 * 		of such formats in ExtMP are dd.MM.yyyy-dd.MM.yyyy and yyyyMMdd-yyyyMMdd.
	 */
	@NotInD
	Optional<String> getNotInDCustomFormat();

	/**
	 * @return an empty optional when the range separator equals {@link #getRangeSeparator()},
	 * 		otherwise contains the custom range separator.
	 */
	@NotInD
	Optional<String> getNotInDCustomRangeSeparator();

}
