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

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.service.internal.errortext.FieldErrortextChecker;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IFieldErrorTextParser;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;

/**
 * Implements the interface {@link IFieldErrorTextParser} for parsing field error texts.
 */
public class FieldErrorTextParser implements IFieldErrorTextParser, IErrorTextParser {

	private final IModelSpecificSettings modelSpecificSettings;

	public FieldErrorTextParser(IServiceConfiguration serviceConfiguration) {
		this.modelSpecificSettings = serviceConfiguration.getModelSpecificSettings();
	}

	@Override
	public boolean hasValidErrorText(IField field, Collection<Locale> locales, IProblemReporter pr) {
		Objects.requireNonNull(field, HAS_VALID_ERROR_TEXT_FIELD_NULL);
		Objects.requireNonNull(locales, HAS_VALID_ERROR_TEXT_LOCALES_NULL);
		Objects.requireNonNull(pr, HAS_VALID_ERROR_TEXT_PR_NULL);

		Function<Locale, String> localeToErrorText = l -> null;
		if (field.getType() instanceof IEnumerationType et) {
			localeToErrorText = l -> et.getErrorMessage(l);
		} else if (field.getType() instanceof IStringType st) {
			localeToErrorText = l -> st.getErrorMessage(l);
		}

		return hasValidErrorText(field, locales, localeToErrorText, pr);
	}

	@Override
	public boolean hasValidErrorTextInRequirednessConfig(IField field, Collection<Locale> locales, IProblemReporter pr) {
		Objects.requireNonNull(field, HAS_VALID_ERROR_TEXT_FIELD_NULL);
		Objects.requireNonNull(locales, HAS_VALID_ERROR_TEXT_LOCALES_NULL);
		Objects.requireNonNull(pr, HAS_VALID_ERROR_TEXT_PR_NULL);

		Function<Locale, String> localeToErrorText = l -> field.getRequirednessConfig()
				.map(rc -> rc.getErrorMessage(l).orElse(null)).orElse(null);
		return hasValidErrorText(field, locales, localeToErrorText, pr);
	}

	private boolean hasValidErrorText(IField field, Collection<Locale> locales,
			Function<Locale, String> localeToErrorText, IProblemReporter pr) {
		Objects.requireNonNull(field, HAS_VALID_ERROR_TEXT_FIELD_NULL);
		Objects.requireNonNull(localeToErrorText, HAS_VALID_ERROR_TEXT_ERROR_TEXTS_NULL);
		Objects.requireNonNull(locales, HAS_VALID_ERROR_TEXT_LOCALES_NULL);
		Objects.requireNonNull(pr, HAS_VALID_ERROR_TEXT_PR_NULL);

		FieldErrortextChecker fec = new FieldErrortextChecker(modelSpecificSettings, pr);
		return locales.stream()
				.map(l -> fec.checkErrorText(localeToErrorText.apply(l), field))
				.reduce(true, (a, b) -> a && b);
	}
}
