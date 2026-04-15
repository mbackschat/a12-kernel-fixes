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

package com.mgmtp.a12.kernel.core.utils.internal;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The base class for lookups of error messages.
 * <p>
 * The derived classes should use the methods specified here to lookup the error messages in a
 * lookup specific resource bundle.
 * If there are different error constant enums with individual lookup bundles you can use
 * the same logic as {@link com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorHandler}.
 * The deriving classes have to implement the {@link #getErrorBundle} method
 * to signal which bundle should be used.
 * The deriving class also has to define a means of converting the error constant to a lookup key.
 * When the lookup key is given by an enum then {@link EnumBasedErrorLookup} should be used.
 */
public abstract class MVKErrorLookup {
	private final Locale locale;

	/**
	 * @param locale
	 * 		the locale used for the lookup of error messages
	 */
	public MVKErrorLookup(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return this.locale;
	}

	/**
	 * Denotes the error bundle to use, has to be implemented by deriving classes.
	 *
	 * @return the error bundle to use for looking up the error messages
	 */
	protected abstract String getErrorBundle();

	/**
	 * Returns the bare error message for the given errorKey and locale. The error key is NOT
	 * appended to the end of the message. This is needed for some
	 * special messages which are actually only parts of an error message, but are included in the
	 * resource bundles because they have to be treated language dependent.
	 * The retrieved error message is not formatted further.
	 *
	 * @param errorKey
	 * 		the key used to lookup the error message.
	 * @param bundleName
	 * 		the bundle in which the error message will be looked up.
	 * @param locale
	 * 		the locale, the error message should be formulated in.
	 */
	public static String getBareErrorMessageUnformatted(String errorKey, String bundleName, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
		return bundle.getString(errorKey);
	}

	/**
	 * Returns the bare error message for the given errorKey and locale. The error key is NOT
	 * appended to the end of the message. This is needed for some
	 * special messages which are actually only parts of an error message, but are included in the
	 * resource bundles because they have to be treated language dependent.
	 *
	 * @param errorKey
	 * 		the key used to lookup the error message.
	 * @param bundleName
	 * 		the bundle in which the error message will be looked up.
	 * @param locale
	 * 		the locale, the error message should be formulated in.
	 * @param args
	 * 		the arguments to replace the placeholders in the error message.
	 */
	public static String getBareErrorMessage(String errorKey, String bundleName, Locale locale, Object... args) {
		return String.format(locale, getBareErrorMessageUnformatted(errorKey, bundleName, locale), args);
	}

	/**
	 * Returns the error message for the given errorKey and locale. The errorKey in square brackets
	 * is appended to the end of the message.
	 *
	 * @param errorKey
	 * 		the key used to lookup the error message.
	 * @param bundleName
	 * 		the bundle in which the error message will be lookup up.
	 * @param locale
	 * 		the locale, the error message should be formulated in.
	 * @param args
	 * 		the arguments to replace the placeholders in the error message.
	 */
	public static String getErrorMessage(String errorKey, String bundleName, Locale locale, Object... args) {
		return getErrorMessage(errorKey, errorKey, bundleName, locale, args);
	}

	/**
	 * Returns the error message for the given errorKey and locale. The errorKey in square brackets
	 * is appended to the end of the message.
	 *
	 * @param errorKey
	 * 		the key used to lookup the error message.
	 * @param displayKey
	 * 		the key that should be displayed with the error message.
	 * @param bundleName
	 * 		the bundle in which the error message will be looked up.
	 * @param locale
	 * 		the locale, the error message should be formulated in.
	 * @param args
	 * 		the arguments to replace the placeholders in the error message.
	 */
	public static String getErrorMessage(String errorKey, String displayKey, String bundleName, Locale locale, Object... args) {
		return getBareErrorMessage(errorKey, bundleName, locale, args) + " [" + displayKey + "]";
	}

	/**
	 * Returns the bare error message for the given errorKey and locale. The error key is NOT
	 * appended to the end of the message. This is needed for some
	 * special messages which are actually only parts of an error message, but are included in the
	 * resource bundles because they have to be treated language dependent.
	 * The retrieved error message is not formatted further.
	 *
	 * @param errorKey
	 * 		the key used to lookup the error message.
	 */
	public String getBareErrorMessageUnformatted(String errorKey) {
		return getBareErrorMessageUnformatted(errorKey, getErrorBundle(), getLocale());
	}

	/**
	 * Returns the bare error message for the given errorKey and locale. The error key is NOT
	 * appended to the end of the message. This is needed for some
	 * special messages which are actually only parts of an error message, but are included in the
	 * resource bundles because they have to be treated language dependent.
	 *
	 * @param errorKey
	 * 		the key used to lookup the error message.
	 * @param args
	 * 		the arguments to replace the placeholders in the error message.
	 */
	public String getBareErrorMessage(String errorKey, Object... args) {
		return getBareErrorMessage(errorKey, getErrorBundle(), this.locale, args);
	}

	/**
	 * Returns the error message for the given errorKey and locale. The errorKey in square brackets
	 * is appended to the end of the message.
	 *
	 * @param errorKey
	 * 		the key used to lookup the error message.
	 * @param args
	 * 		the arguments to replace the placeholders in the error message.
	 */
	public String getErrorMessage(String errorKey, Object... args) {
		return getErrorMessage(errorKey, getErrorBundle(), this.locale, args);
	}

	/**
	 * Returns the error message for the given errorKey and locale. The errorKey in square brackets
	 * is appended to the end of the message.
	 *
	 * @param errorKey
	 * 		the key used to lookup the error message.
	 * @param bundleName
	 * 		the bundle in which the error message will be looked up.
	 * @param args
	 * 		the arguments to replace the placeholders in the error message.
	 */
	protected String getErrorMessage(String errorKey, String bundleName, Object... args) {
		return getErrorMessage(errorKey, errorKey, bundleName, getLocale(), args);
	}
}
