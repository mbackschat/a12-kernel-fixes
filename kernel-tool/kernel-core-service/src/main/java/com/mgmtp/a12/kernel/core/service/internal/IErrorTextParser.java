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
package com.mgmtp.a12.kernel.core.service.internal;

/**
 * Interface to be extended by parser services like {@link RuleParser} which provide methods to control validity of error texts.
 * The interface itself just provides common error messages.
 */
public interface IErrorTextParser {

	public static final String HAS_VALID_ERROR_RULE_NULL = "hasValidErrorText: Parameter 'rule' may not be null.";
	public static final String HAS_VALID_ERROR_TEXT_LOCALES_NULL = "hasValidErrorText: Parameter 'locales' may not be null.";
	public static final String HAS_VALID_ERROR_TEXT_PR_NULL = "hasValidErrorText: Parameter 'pr' may not be null.";
	public static final String HAS_VALID_ERROR_TEXT_ERROR_TEXTS_NULL = "hasValidErrorText: Parameter 'errorTexts' may not be null.";
	public static final String HAS_VALID_ERROR_TEXT_FIELD_NULL = "hasValidErrorText: Parameter 'field' may not be null.";
}
