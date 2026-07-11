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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IErrortextParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.FullChecks_ErrorTextParameterParser;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;

import lombok.NonNull;

/**
 * Klasse dient dazu, den eingegebenen Fehlertext und seine Fehlertext-Parameter auf syntaktische Richtigkeit zu checken.
 */
public class RuleErrortextChecker {
	private final @NonNull IModelSpecificSettings modelSpecificSettings;
	private final @NonNull FehlertextErrorLookup errorHandler;
	private final @NonNull IEC ec;
	private final ICondition condition;
	private final @NonNull IRule rule;
	private final @NonNull Locale localeOfProblemMessages;
	private final boolean extendProblemMessages;

	/**
	 * Übergabe der Parameter für die Initialisierung.
	 * <p>
	 * Aus dem Parameter condition werden die RefFields extrahiert und daraus wird eine Map
	 * konstruiert, die als Schlüssel genau die Bezeichnung des Feldes hat, die auch im Fehlertext
	 * erwartet wird. D.h. : bei eindeutigen Feldnamen nur den Feldnamen bei nicht eindeutigen
	 * Feldnamen den Pfad relativ zum RegelPfad, bzw. absolut, wenn der Pfad nicht innerhalb des
	 * Regelkontextes liegt.
	 * </p>
	 *
	 * @param rule
	 * 		- das Regelobjekt. Wird benötigt für die Bestimmung des Regelkontextes (zur
	 * 		Überprüfung der korrekten Referenzierung der Fehlertextparameter)
	 * @param condition
	 * 		- die geparste Regelbedingung. Wird verwendet zur Bestimmung der referenzierten
	 * 		Felder und ob über Zeile/USB iteriert wird.<br>
	 * 		Ist die Bedingung <code>null</code>, werden Prüfungen, die eine Bedingung
	 * 		voraussetzen, nicht durchgeführt
	 * @param ec
	 * 		- EntityContainer, wird verwendet zur Prüfung, dass der Vordruckzeile-Parameter
	 * 		nur für existierende Felder verwendet wird
	 * @param modelSpecificSettings
	 * 		property of the EC. Contains various info about the model (e.g. base year, isHDV, etc.)
	 * @param extendProblemMessages
	 */
	public RuleErrortextChecker(
			@NonNull IRule rule,
			ICondition condition,
			@NonNull IEC ec,
			@NonNull IModelSpecificSettings modelSpecificSettings,
			boolean extendProblemMessages
	) {
		this.ec = ec;
		this.modelSpecificSettings = modelSpecificSettings;
		this.condition = condition;
		this.rule = rule;
		this.localeOfProblemMessages = modelSpecificSettings.getLanguageUsedInConditionText();
		this.errorHandler = new FehlertextErrorLookup(localeOfProblemMessages);
		this.extendProblemMessages = extendProblemMessages;
	}

	/**
	 * Checks if the error text is correct.
	 * An error text is correct, if
	 * <ul>
	 * <li>it is not null and not empty
	 * <li>additional checks: see {@link #checkErrorText}
	 * </ul>
	 *
	 * @param errortext
	 * 		error text to be checked
	 * @param locale
	 * 		used to format the returned error message if the error text is incorrect
	 * @param problemReporter
	 * 		problem reporter, may not be null
	 * @return map that contains the parsed error text parameters for parameters, that could be
	 * 		successfully parsed. The key is the non parsed error text parameter (text between two
	 * 		dollars in the error text). Note that the map may contain entries even if there are
	 * 		problems reported (if there are several error text parameters in one error text and
	 * 		one is valid and another is invalid). If the error text is not valid or if there are
	 * 		no (valid) error text parameters, an empty map is returned.
	 */
	public Map<String, IErrortextParameter> checkErrorMessageNotEmptyAndCorrect(
			String errortext,
			@NonNull Locale locale,
			@NonNull IProblemReporter problemReporter
	) {
		if (StringUtils.isEmpty(errortext)) {
			problemReporter.reportProblem(new MVKCoreError(
					errorHandler.getErrorMessage(FehlertextErrorType.RULE_HAS_NO_ERRORTEXT, rule.getFullName(),
							locale.getDisplayLanguage(localeOfProblemMessages)), rule));
		}
		return checkErrorText(errortext, locale, problemReporter);
	}

	/**
	 * Überprüft den Fehlertext, ob er gültig ist. Folgende Überprüfungen werden gemacht:
	 * <p>
	 * <nl>
	 * <li>kein Zeilenumbruch enthalten</li>
	 * <li>keine Control-Characters enthalten</li>
	 * <li>korrektes encoding {@link FehlertextUtil#isFehlertextCharsetOK(String)}</li>
	 * <li>gerade Anzahl von $ Zeichen</li>
	 * <li>Überprüfung der einzelnen Parameter
	 * {@link FullChecks_ErrorTextParameterParser }</li>
	 * </nl>
	 *
	 * @param errorText
	 * 		zu prüfender Fehlertext
	 * @return map that contains the parsed error text parameters for parameters, that could be
	 * 		successfully parsed. The key is the non parsed error text parameter (text between two
	 * 		dollars in the error text). Note that the map may contain entries even if there are
	 * 		problems reported (if there are several error text parameters in one error text and
	 * 		one is valid and another is invalid). If the error text is not valid or if there are
	 * 		no (valid) error text parameters, an empty map is returned.
	 */
	public Map<String, IErrortextParameter> checkErrorText(
			String errorText,
			@NonNull Locale locale,
			@NonNull IProblemReporter problemReporter
	) {
		IProblemReporter pr = prependProblemMessageIfNecessary(problemReporter, locale);

		List<String> parameters = FehlertextUtil.checkErrorTextAndExtractParameters(
				errorText, errorHandler, pr, rule, ExtmpUtils.isExtmp(modelSpecificSettings));
		if (parameters.isEmpty()) {
			return Map.of();
		}
		// parse parameters
		Map<String, IErrortextParameter> parsedParameters = new HashMap<>();
		for (String parameterText : parameters) {
			RuleErrorTextParameterService service = new RuleErrorTextParameterService(ec, modelSpecificSettings);
			IErrortextParameter parsedParameter = service.parseParameter(rule, condition, parameterText, pr);
			if (parsedParameter != null) {
				parsedParameters.put(parameterText, parsedParameter);
			}
		}
		return parsedParameters;
	}

	private IProblemReporter prependProblemMessageIfNecessary(IProblemReporter problemReporter, Locale locale) {
		if (!extendProblemMessages) {
			return problemReporter;
		}
		String errMsgPrefix = errorHandler.getBareErrorMessage(
				FehlertextErrorType.RULE_ERRORTEXT_WRONG, rule.getFullName(), locale.getDisplayLanguage(localeOfProblemMessages));
		return problem -> problemReporter.reportProblem(
				new MVKCoreError(errMsgPrefix + " " + problem.getMessage(), problem.getSource()));
	}
}
