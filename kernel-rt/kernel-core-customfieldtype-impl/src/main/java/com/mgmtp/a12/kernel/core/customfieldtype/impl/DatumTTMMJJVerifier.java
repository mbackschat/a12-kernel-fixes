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
package com.mgmtp.a12.kernel.core.customfieldtype.impl;

import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorKeys;

/**
 * Validates a date with a two-digit year in the format DD.MM.YY.
 * It checks whether the entered value matches the pattern DD.MM.YY – the three parts day,
 * month, and year must each be specified with two digits, and the date separator is always a point.
 * Then it checks whether the date is valid (e.g. 31.04.10 or 29.02.01 are invalid - April has only
 * 30 days and February only has 29 days in leap years, but 2001 is not a leap year).
 */
public class DatumTTMMJJVerifier extends DatumVerifier {

	private static final DatumTTMMJJVerifier me = new DatumTTMMJJVerifier();
	private static final Pattern FORMAT_PATTERN = Pattern.compile("^[0-3][0-9]\\.[0-1][0-9]\\.[0-9]{2}$");
	private static final String SDF_PATTERN = "dd.MM.yy";
	private static final String FORMAT_FUER_FEHLERMELDUNG = "TT.MM.JJ";

	private DatumTTMMJJVerifier() {
	}

	public static DatumTTMMJJVerifier getInstance() {
		return me;
	}

	@Override
	protected Pattern getFormatPattern() {
		return FORMAT_PATTERN;
	}

	@Override
	protected String getSimpleDateFormatPattern() {
		return SDF_PATTERN;
	}

	public String getErrorKey() {
		return CustomFieldTypeErrorKeys.UNGUELTIGES_DATUM_TTMMJJ;
	}

	@Override
	protected String getFormatFuerFehlermeldung() {
		return FORMAT_FUER_FEHLERMELDUNG;
	}
}
