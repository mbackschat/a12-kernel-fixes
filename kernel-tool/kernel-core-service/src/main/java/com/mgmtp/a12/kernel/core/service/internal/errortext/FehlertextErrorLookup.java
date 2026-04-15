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

package com.mgmtp.a12.kernel.core.service.internal.errortext;

import java.util.Locale;

import com.mgmtp.a12.kernel.core.utils.internal.EnumBasedErrorLookup;

/**
 * An instance of the class <code>FehlertextErrorLookup</code> defines the lookup of the error
 * messages that can occur during checking the error messages defined for individual rules. The
 * messages may depend on the language used in the editor or equivalently the condition text of the
 * rule. So this language has to be specified.
 */
public class FehlertextErrorLookup extends EnumBasedErrorLookup<FehlertextErrorType> {
	static final String ERRORTEXT_ERROR_MESSAGES_RESOURCE_NAME = "internal/vk_service_errortext_error_messages/error_messages";

	/**
	 * @param locale
	 * 		the locale used for the lookup of error messages that may occur during checking
	 * 		the "Fehlertext".
	 */
	public FehlertextErrorLookup(Locale locale) {
		super(locale);
	}

	@Override
	protected String getErrorBundle() {
		return ERRORTEXT_ERROR_MESSAGES_RESOURCE_NAME;
	}
}
