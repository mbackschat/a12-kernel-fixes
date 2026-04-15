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
package com.mgmtp.a12.kernel.md.model.a12internal.services.codegen;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.base.utils.internal.FieldTypeConstants;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectLanguageSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.A12LanguageSettings;

/**
 * Default implementation of {@link IProjectSpecificSettings}.
 */
public class A12SpecificSettings implements IProjectSpecificSettings {

	private static final String DATE_FORMAT = FieldTypeConstants.DEFAULT_DATE_FORMAT;
	private static final String TIME_FORMAT = "HH:mm";
	private static final Map<Locale, A12LanguageSettings> LOCALE_TO_LANGUAGE_SETTINGS = new HashMap<>();

	private final Map<Locale, IProjectLanguageSettings> languageSettings;

	public A12SpecificSettings(final List<Locale> supportedLocales) {
		languageSettings = supportedLocales
				.stream()
				.collect(Collectors.toMap(
						locale -> locale,
						locale -> {
							if (LOCALE_TO_LANGUAGE_SETTINGS.containsKey(locale)) {
								return LOCALE_TO_LANGUAGE_SETTINGS.get(locale);
							} else {
								return new A12LanguageSettings(locale.getLanguage(),
										locale,
										'.',
										",",
										DATE_FORMAT,
										false,
										"yyyy-MM-dd HH:mm",
										TIME_FORMAT);
							}
						}));
	}

	@Override
	public Map<Locale, IProjectLanguageSettings> getProjectLanguageSettings() {
		return languageSettings;
	}

	static {
		LOCALE_TO_LANGUAGE_SETTINGS.put(Locale.ENGLISH, new A12LanguageSettings(Locale.ENGLISH.getLanguage(),
				Locale.ENGLISH,
				'.',
				",",
				DATE_FORMAT,
				false,
				"MM/dd/yyyy HH:mm",
				TIME_FORMAT));

		final Locale gb = Locale.of("gb");
		LOCALE_TO_LANGUAGE_SETTINGS.put(gb, new A12LanguageSettings(gb.getLanguage(),
				gb,
				'.',
				",",
				DATE_FORMAT,
				false,
				"dd/MM/yyyy HH:mm",
				TIME_FORMAT));

		final Locale us = Locale.of("us");
		LOCALE_TO_LANGUAGE_SETTINGS.put(us, new A12LanguageSettings(us.getLanguage(),
				us,
				'.',
				",",
				DATE_FORMAT,
				false,
				"MM/dd/yyyy HH:mm",
				TIME_FORMAT));

		LOCALE_TO_LANGUAGE_SETTINGS.put(Locale.GERMAN, new A12LanguageSettings(Locale.GERMAN.getLanguage(),
				Locale.GERMAN,
				'.',
				",",
				DATE_FORMAT,
				false,
				"dd.MM.yyyy HH:mm",
				TIME_FORMAT));

		LOCALE_TO_LANGUAGE_SETTINGS.put(Locale.FRENCH, new A12LanguageSettings(Locale.FRENCH.getLanguage(),
				Locale.FRENCH,
				'.',
				",",
				DATE_FORMAT,
				false,
				"dd/MM/yyyy HH:mm",
				TIME_FORMAT));

		final Locale nl = Locale.of("nl");
		LOCALE_TO_LANGUAGE_SETTINGS.put(nl, new A12LanguageSettings(nl.getLanguage(),
				nl,
				'.',
				",",
				DATE_FORMAT,
				false,
				"dd-MM-yyyy HH:mm",
				TIME_FORMAT));

		final Locale ch = Locale.of("ch");
		LOCALE_TO_LANGUAGE_SETTINGS.put(ch, new A12LanguageSettings(ch.getLanguage(),
				ch,
				'.',
				",",
				DATE_FORMAT,
				false,
				"dd.MM.yyyy HH:mm",
				TIME_FORMAT));
	}

}
