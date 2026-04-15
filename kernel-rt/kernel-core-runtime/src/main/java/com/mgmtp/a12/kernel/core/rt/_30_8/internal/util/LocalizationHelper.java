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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Helper class that allows to retrieve different words and signs needed for localization. E.g. in
 * the error messages english and german texts have different words for commas ('commas' and
 * 'Kommata').
 */
public class LocalizationHelper {

	public static final String COMMA_PLURAL = "COMMA_PLURAL";
	public static final String POINT_PLURAL = "POINT_PLURAL";

	public static final String DIVISION_BY_ZERO_INVALID = "DIVISION_BY_ZERO_INVALID";
	public static final String ERROR_DURING_DATA_CHECK_EVAL = "ERROR_DURING_DATA_CHECK_EVAL";

	private LocalizationHelper() {
		throw new IllegalStateException("Utility class");
	}

	public static String getValue(String key, Locale language) {
		return InternalFallbackErrorLookup.getBareErrorMessageUnformatted(key, Constants.LOCALIZATION_HELPER_RESOURCE_NAME,
				language, ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
	}
}
