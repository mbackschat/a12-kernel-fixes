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

import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * see {@link IDateFragmentType}
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DateFragmentType extends FieldType {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return the date fragment format. Is never {@code null}.
	 * @param formatOfFragment
	 * 		the new date fragment format. May not be {@code null}.
	 */
	private @NonNull String formatOfFragment;

	/**
	 * @return {@code true} if a date fragment containing the year part before {@code 1900} should
	 * 		be considered as a wrong input, otherwise {@code false}. The default value is
	 *        {@code false}.
	 * @param youngerThan1900Check
	 * 		use {@code true} if a date fragment containing the year part before {@code 1900}
	 * 		should be considered as a wrong input, otherwise {@code false}.
	 */
	private boolean youngerThan1900Check = false;

	/**
	 * @param notInDCustomFormat
	 * 		a custom date fragment format which differs to the supported ones. See
	 *        {@link IDateFragmentType#getNotInDCustomFormat()}.
	 */
	private String notInDCustomFormat;

	@Builder(builderClassName = "DateFragmentTypeBuilder")
	private DateFragmentType(@NonNull String formatOfFragment, boolean youngerThan1900Check, String notInDCustomFormat) {
		this.formatOfFragment = formatOfFragment;
		this.youngerThan1900Check = youngerThan1900Check;
		this.notInDCustomFormat = notInDCustomFormat;
	}

	public static class DateFragmentTypeBuilder {
		private boolean youngerThan1900Check = false; // we set explicit the default values
	}

	/**
	 * @return a custom date fragment format which differs to the supported ones. See
	 *        {@link IDateFragmentType#getNotInDCustomFormat()}.
	 */
	public Optional<String> getNotInDCustomFormat() {
		return Optional.ofNullable(notInDCustomFormat);
	}

}
