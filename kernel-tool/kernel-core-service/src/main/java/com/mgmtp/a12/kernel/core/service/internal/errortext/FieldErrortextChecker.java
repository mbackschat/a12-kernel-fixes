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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;

import lombok.NonNull;

/**
 * Checks the error text of a {@link IFieldType}.
 */
public class FieldErrortextChecker {

	private final @NonNull IField field;
	private final @NonNull FehlertextErrorLookup errorHandler;
	private final @NonNull IModelSpecificSettings modelSpecificSettings;
	private final Locale localeOfProblemMessages;
	private final boolean extendProblemMessages;

	/**
	 * @param field
	 * 		the error text's {@link IField}
	 * @param extendProblemMessages
	 * 		specifies, whether a message regarding the field should be prepended.
	 */
	public FieldErrortextChecker(
			@NonNull IField field,
			@NonNull IModelSpecificSettings modelSpecificSettings,
			boolean extendProblemMessages
	) {
		this.field = field;
		this.localeOfProblemMessages = modelSpecificSettings.getLanguageUsedInConditionText();
		this.errorHandler = new FehlertextErrorLookup(localeOfProblemMessages);
		this.modelSpecificSettings = modelSpecificSettings;
		this.extendProblemMessages = extendProblemMessages;
	}

	// Valid parameter(s) in a field's error text between two $-signs
	private static final Map<Locale, Set<String>> localeToValidParameterText = Map.of(
			Locale.GERMANY, Set.of("Feld", "Feld.Wert"),
			Locale.US, Set.of("field", "field.value"));

	/**
	 * Checks the error text of a field (type).
	 */
	public void checkErrorText(String errorText, Locale locale, IProblemReporter problemReporter) {
		IProblemReporter pr = prependProblemMessageIfNecessary(problemReporter, locale);

		List<String> parameters = FehlertextUtil.checkErrorTextAndExtractParameters(
				errorText, errorHandler, pr, field, ExtmpUtils.isExtmp(modelSpecificSettings));
		parameters.stream()
				.filter(parameterText -> !isValidParameterText(parameterText))
				.findFirst()
				.ifPresent(parameterText -> pr.reportProblem(
						new MVKCoreError(errorHandler.getErrorMessage(FehlertextErrorType.INVALID_PARAMETER, parameterText),
								field)));
	}

	private boolean isValidParameterText(@NonNull String parameterText) {
		return localeToValidParameterText.get(modelSpecificSettings.getLanguageUsedInConditionText()).contains(parameterText);
	}

	private IProblemReporter prependProblemMessageIfNecessary(IProblemReporter problemReporter, Locale locale) {
		if (!extendProblemMessages) {
			return problemReporter;
		}
		String errMsgPrefix = errorHandler.getBareErrorMessage(
				FehlertextErrorType.FIELD_ERRORTEXT_WRONG, field.getFullName(), locale.getDisplayLanguage(localeOfProblemMessages));
		return problem -> problemReporter.reportProblem(
				new MVKCoreError(errMsgPrefix + " " + problem.getMessage(), problem.getSource()));
	}
}
