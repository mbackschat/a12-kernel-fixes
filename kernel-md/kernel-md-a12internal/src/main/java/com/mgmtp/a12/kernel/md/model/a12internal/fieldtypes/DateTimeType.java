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

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.base.utils.internal.FieldTypeConstants;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateTimeType;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;
import com.mgmtp.a12.model.header.Annotation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * see {@link IDateTimeType}
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@OnlyForA12InternalUsage
public class DateTimeType extends FieldType {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_FORMAT = FieldTypeConstants.DEFAULT_DATE_TIME_FORMAT;

	/**
	 * @return the date time format. Is never {@code null}.
	 * @param format
	 * 		the new date time format. May not be {@code null}. Default value is
	 *        {@value #DEFAULT_FORMAT}.
	 */
	private @NonNull String format = DEFAULT_FORMAT;

	@Builder(builderClassName = "DateTimeTypeBuilder")
	private DateTimeType(@NonNull List<Annotation> annotations,
			@NonNull String format) {
		super(annotations);
		this.format = format;
	}

	public static class DateTimeTypeBuilder {
		private List<Annotation> annotations = new ArrayList<>(); // in order to make DateTimeTypeBuilder.annotations() optional
		private String format = DEFAULT_FORMAT; // in order to make DateTimeTypeBuilder.format() optional
	}
}
