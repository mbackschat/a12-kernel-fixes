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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IErrortextParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IFieldParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IGroupIndexParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IIndexFieldParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.ISemanticIndex;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.ISimpleFieldParameter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporter;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

/**
 * Hilfsklasse für die Überprüfungen von Fehlertexten mit allen statischen, insb. also Modell-unabhängigen Überprüfungen.
 * Außerdem werden Methoden zur Ersetzung von Fehlertextparametern bei Verschieben bzw. Umbenennen von Feldern/Kontexten angeboten.
 */
public class FehlertextUtil {

	public static final String WERT = ".Wert";
	public static final Pattern CONTROL_CHARACTER_PATTERN = Pattern.compile("\\p{C}");

	private FehlertextUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static List<String> checkErrorTextAndExtractParameters(
			String errorText,
			@NonNull FehlertextErrorLookup problemMessageLookup,
			@NonNull IProblemReporter pr,
			Object problemSource,
			boolean isExtmp
	) {
		// (1) Whether an error message is required should be checked somewhere else
		if (StringUtils.isEmpty(errorText)) {
			return List.of();
		}
		// (2) Line breaks are not allowed
		if (!checkLineSeparators(errorText)) {
			pr.reportProblem(
					new MVKCoreError(problemMessageLookup.getErrorMessage(FehlertextErrorType.INVALID_LINEBREAK), problemSource));
			return List.of();
		}

		// (3) The encoding must be ISO-8859-15 for ExtMP and Unicode for A12
		if (isExtmp) {
			if (!isFehlertextCharsetOK(errorText)) {
				pr.reportProblem(
						new MVKCoreError(problemMessageLookup.getErrorMessage(FehlertextErrorType.WRONG_ENCODING, "ISO-8859-15"),
								problemSource));
				return List.of();
			}
		} else {
			if (!isFehlertextCharsetUnicodeOK(errorText)) {
				pr.reportProblem(
						new MVKCoreError(problemMessageLookup.getErrorMessage(FehlertextErrorType.WRONG_ENCODING, "Unicode"),
								problemSource));
				return List.of();
			}
		}

		// (4) Control characters are not allowed
		if (!checkControlCharacters(errorText)) {
			pr.reportProblem(
					new MVKCoreError(problemMessageLookup.getErrorMessage(FehlertextErrorType.INVALID_CHARACTERS), problemSource));
			return List.of();
		}

		// (5) There must be an even number of $ signs
		if (!checkWrongNumberOfDollars(errorText)) {
			pr.reportProblem(
					new MVKCoreError(problemMessageLookup.getErrorMessage(FehlertextErrorType.WRONG_NUMBER_OF_DOLLARS),
							problemSource));
			return List.of();
		}

		// extract parameters
		String[] parts = errorText.split("\\$");
		List<String> parameters = new ArrayList<>();
		for (int i = 1; i < parts.length; i = i + 2) {
			String value = parts[i];
			if (!StringUtils.isEmpty(value)) {
				parameters.add(value);
			}
		}
		return parameters;
	}

	/**
	 * Überprüft das Encoding des Fehlertextes auf ISO-8859-15.
	 */
	public static boolean isFehlertextCharsetOK(String fehlerText) {
		return CommonUtils.checkEncoding(fehlerText, "ISO-8859-15");
	}

	/**
	 * Überprüft das Encoding des Fehlertextes auf Unicode.
	 */
	public static boolean isFehlertextCharsetUnicodeOK(String fehlerText) {
		return CommonUtils.checkEncoding(fehlerText, "Unicode");
	}

	/**
	 * Checks the given error text for line separators.
	 */
	public static boolean checkLineSeparators(String errorText) {

		return !errorText.contains("\n")
				&& !errorText.contains("\r")
				&& !errorText.contains("\u2028")
				&& !errorText.contains("\u2029");
	}

	/**
	 * Checks the given error text for control characters.
	 */
	public static boolean checkControlCharacters(String errorText) {
		return !FehlertextUtil.CONTROL_CHARACTER_PATTERN.matcher(errorText).find();
	}

	public static boolean checkWrongNumberOfDollars(String errorText) {
		return StringUtils.countMatches(errorText, "$") % 2 == 0;
	}

	/**
	 * specifically for A12 export, because <f1>.Vordruckzeile parameters are not handled (those are replaced during A12
	 * export)
	 */
	public static @NonNull String translateParamsInRuleErrorMsgAndReplaceShortnamesForA12Export(
			@NonNull String errorMsg,
			@NonNull IRule rule,
			@NonNull IEC ec,
			@NonNull IModelSpecificSettings modelSpecificSettings,
			@NonNull Locale targetLocale
	) {
		return translateParamsInRuleErrorMsg(errorMsg, rule, ec, modelSpecificSettings, targetLocale,
				parsedParam -> switch (parsedParam) {
					// "indexField" (see grammar) references are always by shortName, and relative or absolute
					// paths are not allowed there. Note: the name of the index field is only necessary at all if
					// there are multiple index fields on the path of the error field, or so.
					// (see checkIndexFieldRef in FullChecks_ErrorTextParameterParser)
					case IFieldParameter parsedField when !(parsedField instanceof IIndexFieldParameter) -> {
						replaceShortnameWithAbsOrRelPath(parsedField, rule);
						yield parsedField;
					}
					case IGroupIndexParameter groupIndexParam -> {
						replaceShortnameWithAbsOrRelPath(groupIndexParam, rule);
						yield groupIndexParam;
					}
					// Note: <f1>.Vordruckzeile parameters also seem to allow shortname refs for f1, but they are
					// replaced during A12 export by the corresponding constant values, so we don't need to handle them here
					case null, default -> parsedParam;
				});
	}

	private static void replaceShortnameWithAbsOrRelPath(IFieldParameter parsedField, IRule rule) {
		if (parsedField.getFieldFullName() != null //at the time of implementation the null case could not happen, yet we are defensive in not throwing
				&& !parsedField.getEntityRefInErrorText()
				.contains(EntityUtils.SEPARATOR) //rel paths or absolute paths are not replaced
		) {
			parsedField.setEntityRefInErrorText(makeRelOrAbsPath(parsedField.getFieldFullName(), rule));
		}

		if (parsedField instanceof ISimpleFieldParameter parsedSimpleField) { // the other sub-interface, IIndexFieldParameter, corresponds to the index(...) language construct in the errtextparam grammar that does not currently support non-shortnames
			parsedSimpleField.setSemanticIndex(
					replaceShortNameInSemanticIndexSpec(parsedSimpleField.getSemanticIndex(), rule));
		}
	}

	private static void replaceShortnameWithAbsOrRelPath(IGroupIndexParameter groupIndexParam, IRule rule) {
		// the group reference itself cannot be a shortname (no shortnames for groups currently), but if it has a semanticIndex field
		// reference, that field reference could be by shortname
		groupIndexParam.setSemanticIndex(replaceShortNameInSemanticIndexSpec(groupIndexParam.getSemanticIndex(), rule));
	}

	/**
	 * Makes sure semanticIndex does not have a shortname ref.
	 * <ul>
	 *     <li>if semanticIndex is null, null is returned</li>
	 *     <li>if semanticIndex does not have a field reference or if it has a non-shortname field reference, it is returned unchanged</li>
	 *     <li>if semanticIndex has a shortname field reference, it is replaced by a non-shortname ref according to {@link #makeRelOrAbsPath}</li>
	 * </ul>
	 */
	private static ISemanticIndex replaceShortNameInSemanticIndexSpec(ISemanticIndex semanticIndex, IRule rule) {
		if (semanticIndex == null) {
			return null;
		}
		var semIdxFullName = semanticIndex.getSemanticIndexRefFullName();
		String semIdxSpec = semanticIndex.getSemanticIndexSpec();
		if (semIdxFullName != null // if this is null, the ISemanticIndex probably uses a constant rather than a field ref
				&& semIdxSpec != null // this should never actually be null, at least not if semIdxFullName != null
				&& !semIdxSpec.contains(EntityUtils.SEPARATOR) // don't convert relpaths or abspaths
		) {
			return semanticIndex.withSemanticIndexSpec(makeRelOrAbsPath(semIdxFullName, rule));
		} else {
			return semanticIndex;
		}
	}

	private static String makeRelOrAbsPath(@NonNull String fullName, @NonNull IRule rule) {
		var path = ElementPathUtils.splitAbsPath(fullName);
		var rulePath = ElementPathUtils.splitAbsPath(rule.getFullName());
		return Objects.equals(rulePath.getFirst(), path.getFirst()) // same root group?
				? ElementPathUtils.relPath(path, rulePath.subList(0, rulePath.size() - 1)) // -> relative path from rule group
				: fullName; // -> absolute path
	}

	/**
	 * Translate the parameter found in the given rule error message to the provided target locale.
	 *
	 * @param errorMsg
	 * 		error message to be translated, may not be null. The error message has to be
	 * 		correct (especially the error text parameters).
	 * @param rule
	 * 		the rule to that the errorMsg belongs, may not be null
	 * @param mapParsedParamAndMaybeModify
	 * 		optional; skipped if null. The supplied function is allowed to modify or destroy the
	 * 		given parameter as a side effect.
	 * @param targetLocale
	 * 		the locale to translate the parameters to
	 * @return the translated rule error message.
	 */
	public static @NonNull String translateParamsInRuleErrorMsg(
			@NonNull String errorMsg,
			@NonNull IRule rule,
			@NonNull IEC ec,
			@NonNull IModelSpecificSettings modelSpecificSettings,
			@NonNull Locale targetLocale,
			Function<IErrortextParameter, IErrortextParameter> mapParsedParamAndMaybeModify
	) {
		RuleErrorTextParameterService parameterService = new RuleErrorTextParameterService(ec, modelSpecificSettings);
		return mapParamsInFehlertext(errorMsg, param -> {
			ProblemReporter pr = new ProblemReporter();
			IErrortextParameter parsedParam = parameterService.parseParameter(rule, null, param,
					pr); // error text parameters are not checked regarding the condition
			if (pr.hasProblemOccurred(Severity.ERROR)) {
				throw new IllegalArgumentException(
						String.format("Rule %s: Error text parameter '%s' not valid:%n%s", rule.getFullName(), param,
								pr.getProblems().stream().map(IProblem::getMessage).collect(Collectors.joining("%n"))));
			}
			if (mapParsedParamAndMaybeModify != null) {
				parsedParam = mapParsedParamAndMaybeModify.apply(parsedParam);
			}
			param = parsedParam.format(targetLocale);
			return param;
		});
	}

	public static String mapParamsInFehlertext(String errorMsg, Function<String, String> fn) {
		if (StringUtils.isEmpty(errorMsg)) {
			return errorMsg;
		}
		String[] parts = StringUtils.splitPreserveAllTokens(errorMsg, '$');
		// only every second element is a parameter
		for (int i = 1; i < parts.length; i = i + 2) {
			String param = parts[i];
			// only non-blank elements can be parameters. "$$" signifies an escaped $, not an empty parameter
			if (!StringUtils.isEmpty(param)) {
				parts[i] = fn.apply(param);
			}
		}
		return StringUtils.join(parts, "$");
	}
}