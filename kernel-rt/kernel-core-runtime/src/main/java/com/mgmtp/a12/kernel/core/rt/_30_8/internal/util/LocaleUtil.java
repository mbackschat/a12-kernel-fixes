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

import java.util.List;
import java.util.Locale;

/**
 * Diese Klasse wird verwendet um aus beliebigen Parametern die darin enthaltene Locale zu
 * extrahieren.
 */
public class LocaleUtil {

	private static final String LOCALE_NOT_SUPPORTED = "The locale '%s' is not supported.";

	private final List<Locale> supportedLocales;

	public LocaleUtil(List<Locale> supportedLocales) {
		this.supportedLocales = supportedLocales;
	}

	/**
	 * @param param
	 * 		für dieses Objekt wird geprüt ob es sich um eine vom Generat unterstützte Locale
	 * 		handelt.
	 * @param msgNoLocale
	 * 		Text der {@link IllegalArgumentException} falls das param-Objekt keine Locale ist.
	 * @return falls es sich beim param-Objekt um eine Locale handelt w
	 *
	 * @throws {@link
	 *        IllegalArgumentException} falls es sich beim param-Objekt um keine Locale handelt
	 * 		oder falls die Locale nicht in der Menge der vom Generat unterstützten Locales
	 * 		ist.
	 */
	public Locale deriveLocaleIfPossible(Object param, String msgNoLocale) {
		if (!(param instanceof Locale)) {
			throw new IllegalArgumentException(msgNoLocale);
		} else {
			Locale locale = (Locale) param;
			if (!isSupportedLocale(locale)) {
				throw new IllegalArgumentException(String.format(LOCALE_NOT_SUPPORTED, param));
			}
			return locale;
		}
	}

	public Locale deriveLocaleFromFirstParam(Object... params) {
		// Locale.GERMANY ExtMP fallback
		Locale sprache = Locale.GERMANY; // default ist deutsch (falls kein Sprachparameter angegeben)

		if (params != null && params.length != 0) {
			if (!(params[0] instanceof Locale)) {
				return null; // Error handling needs to happen at the caller!
			}
			if (!isSupportedLocale((Locale) params[0])) {
				throw new IllegalArgumentException(String.format(LOCALE_NOT_SUPPORTED, params[0]));
			}
			sprache = (Locale) params[0];
		}
		return sprache;
	}

	public void checkLocale(Locale sprache) {
		checkLocale(sprache, String.format(LOCALE_NOT_SUPPORTED, sprache));
	}

	public void checkLocale(Locale sprache, String errMsg) {
		if (!isSupportedLocale(sprache)) {
			throw new IllegalArgumentException(errMsg);
		}
	}

	public boolean isSupportedLocale(Locale sprache) {
		return supportedLocales.contains(sprache);
	}
}
