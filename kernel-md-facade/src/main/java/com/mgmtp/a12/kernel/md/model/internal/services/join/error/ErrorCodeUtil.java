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

package com.mgmtp.a12.kernel.md.model.internal.services.join.error;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import com.mgmtp.a12.kernel.md.model.a12internal.Element;

/**
 * Converts the ErrorCode into the message for the specified language.
 */
public abstract class ErrorCodeUtil {
	private static final String DEFAULT_BUNDLE_NAME = "com.mgmtp.a12.kernel.md.model.internal.services.join.i18n.messages";

	private ErrorCodeUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static String getLocalizedMessageFragment(ErrorCode errorKey, Locale locale) {
		return getMessage(errorKey.getMessageKey(), locale);
	}

	public static String getLocalizedMessageFragment(ErrorCode errorKey, Locale locale, Object... args) {
		return getMessage(errorKey, locale, args);
	}

	private static String getMessage(String errorKey, Locale locale) {
		final ResourceBundle bundle = ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, Locale.forLanguageTag(locale.getLanguage()));
		return bundle.getString(errorKey);
	}

	public static String getMessageWithCode(ErrorCode errorKey, Locale locale) {
		return String.format(locale, "%s [%s]", getMessage(errorKey, locale), errorKey.name());
	}

	public static String getMessageWithCode(ErrorCode errorKey, Locale locale, Object... args) {
		return String.format(locale, "%s [%s]", getMessage(errorKey, locale, args), errorKey.name());
	}

	private static String getMessage(ErrorCode errorKey, Locale locale, Object... args) {
		return getMessage(errorKey.getMessageKey(), locale, args);
	}

	private static String getMessage(String errorKey, Locale locale, Object... args) {
		String pattern = getMessage(errorKey, locale);
		return MessageFormat.format(pattern, args);
	}

	public static String getLocalizedMessageFragmentStart(Locale locale, Element element) {
		return getLocalizedMessageFragment(locale, ErrorCode.ErrorMessageFragmentStart, element);
	}

	public static String getLocalizedMessageFragment(Locale locale, ErrorCode error, Element element) {
		return getLocalizedMessageFragment(locale, error, element.getClass().getSimpleName().toLowerCase());
	}

	public static String getLocalizedMessageFragment(Locale locale, ErrorCode error, String elementName) {
		String messageKey = String.format("%s.%s", error.getMessageKey(), elementName);
		return getMessage(messageKey, locale);
	}

}
