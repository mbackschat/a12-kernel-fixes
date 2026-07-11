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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.service.internal.errortext.FieldErrortextChecker;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IFieldErrorTextParser;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

/**
 * Implements the interface {@link IFieldErrorTextParser} for parsing field error texts.
 */
public class FieldErrorTextParser implements IFieldErrorTextParser {

	private final IModelSpecificSettings modelSpecificSettings;

	public FieldErrorTextParser(IServiceConfiguration serviceConfiguration) {
		this.modelSpecificSettings = serviceConfiguration.getModelSpecificSettings();
	}

	@Override
	public boolean hasValidErrorText(
			@NonNull IField field,
			@NonNull Collection<Locale> locales,
			@NonNull IProblemReporter pr
	) {
		Function<Locale, String> localeToErrorText = l -> null;
		if (field.getType() instanceof IEnumerationType et) {
			localeToErrorText = l -> et.getErrorMessage(l);
		} else if (field.getType() instanceof IStringType st) {
			localeToErrorText = l -> st.getErrorMessage(l);
		}

		return hasValidErrorText(field, locales, localeToErrorText, pr);
	}

	@Override
	public boolean hasValidErrorTextInRequirednessConfig(
			@NonNull IField field,
			@NonNull Collection<Locale> locales,
			@NonNull IProblemReporter pr
	) {
		Function<Locale, String> localeToErrorText = l -> field.getRequirednessConfig()
				.flatMap(rc -> rc.getErrorMessage(l)).orElse(null);
		return hasValidErrorText(field, locales, localeToErrorText, pr);
	}

	private boolean hasValidErrorText(
			@NonNull IField field,
			@NonNull Collection<Locale> locales,
			@NonNull Function<Locale, String> localeToErrorText,
			@NonNull IProblemReporter pr
	) {
		AtomicBoolean isValid = new AtomicBoolean(true);
		FieldErrortextChecker fec = new FieldErrortextChecker(field, modelSpecificSettings, false);
		locales.forEach(locale -> fec.checkErrorText(localeToErrorText.apply(locale), locale, problem -> {
			if (problem.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) {
				isValid.set(false);
			}
			pr.reportProblem(problem);
		}));
		return isValid.get();
	}
}
