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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The base class for lookups of error messages.
 * It provides a fallback mechanism for internal resources when no resource is given at the specific path.
 * The internal path is {@code "internal/<VERSION>/<BUNDLE_NAME>"}.
 * <p>
 * Is similar to {@link com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.utils.InternalFallbackErrorLookup}.
 */
public abstract class InternalFallbackErrorLookup {
	public static final String INTERNAL_BUNDLE_LOCATION = "internal";
	public static final String INTERNAL_VERSION = "_31_1";

	private InternalFallbackErrorLookup() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Transforms the given baseName to the (potential) name of the internal bundle.
	 * This is used as a fallback if the application does not provide a key in the
	 * resource bundle.
	 *
	 * @param baseName
	 * 		the location and name of the bundle
	 * @return the name of the internal bundle ({@link #INTERNAL_BUNDLE_LOCATION} + baseName)
	 */
	public static String toInternalBundleName(String baseName) {
		Path p = Paths.get(baseName);
		if (p.getRoot() == null || !INTERNAL_BUNDLE_LOCATION.equals(p.getRoot().toString())) {
			// add internal and version to the baseName so that the internal bundle will be used
			baseName = INTERNAL_BUNDLE_LOCATION + "/" + INTERNAL_VERSION + "/" + baseName;
		}
		return baseName;
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
	 * @param bundleName
	 * 		the bundle in which the error message will be looked up.
	 * @param locale
	 * 		the locale, the error message should be formulated in.
	 * @param control
	 * 		the control for the resolving the resource bundle.
	 */
	public static String getBareErrorMessageUnformatted(String errorKey, String bundleName, Locale locale,
			ResourceBundle.Control control) {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, control);
			return bundle.getString(errorKey);
		} catch (MissingResourceException _e) {
			// if the errorKey could not be found in the provided bundles,
			// search in the internal bundles (this is the fallback for missing keys)
			ResourceBundle bundle = ResourceBundle.getBundle(toInternalBundleName(bundleName), locale, control);
			return bundle.getString(errorKey);
		}
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
	 * @param bundleName
	 * 		the bundle in which the error message will be looked up.
	 * @param locale
	 * 		the locale, the error message should be formulated in.
	 */
	public static String getBareErrorMessageUnformatted(String errorKey, String bundleName, Locale locale) {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
			return bundle.getString(errorKey);
		} catch (MissingResourceException _e) {
			// if the errorKey could not be found in the provided bundles,
			// search in the internal bundles (this is the fallback for missing keys)
			ResourceBundle bundle = ResourceBundle.getBundle(toInternalBundleName(bundleName), locale);
			return bundle.getString(errorKey);
		}
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
	 * Returns the bundle for the corresponding internal resource.
	 *
	 * @param bundleName
	 * 		the bundle in which the error message will be looked up.
	 * @param locale
	 * 		the locale, the error message should be formulated in.
	 * @param control
	 * 		the control for the resolving the resource bundle.
	 * @return the internal {@link ResourceBundle} that corresponds to the provided bundleName
	 */
	public static ResourceBundle getInternalResourceBundle(String bundleName, Locale locale, ResourceBundle.Control control) {
		return ResourceBundle.getBundle(toInternalBundleName(bundleName), locale, control);
	}
}
