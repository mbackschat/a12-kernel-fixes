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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IErrortextParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IFieldParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IGroupIndexParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IIndexFieldParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IRootgroupNameParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IRootgroupRowParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.ISemanticIndex;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.ISimpleFieldParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.SemanticIndex;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;
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
	 * Replaces entity references in an error text if an entity is renamed or moved.
	 * <p>
	 * Precondition:
	 * The error text has to be correct.
	 *
	 * @param errorText
	 * 		the error text to be changed
	 * @param newRuleFullName
	 * 		new full name of the rule whose error text is changed (used to determine, how the
	 * 		new entity has to be referenced)
	 * @param fullNameUpdater
	 * 		determines the newFullName of a referenced entity from its oldFullName
	 * @param rule
	 * 		rule whose error text is changed
	 * @param condition
	 * 		parsed condition of the rule. Note: the condition has to be expanded
	 *        {@link ExpandMode#EXPAND}
	 * @param isDecoration
	 * 		true if the adjustments are done as part of a decoration, otherwise false
	 * @return the updated error text
	 */
	public static String replaceReferenceInErrorText(String errorText, String newRuleFullName,
			UnaryOperator<String> fullNameUpdater,
			IServiceConfiguration serviceConfiguration, IRule rule, ICondition condition, boolean isDecoration) {
		RuleErrorTextParameterService parameterService = new RuleErrorTextParameterService(serviceConfiguration.getIEC(),
				serviceConfiguration.getModelSpecificSettings());
		String[] parts = errorText.split("\\$", -1);
		ProblemReporter pm = new ProblemReporter();
		for (int i = 1; i < parts.length; i += 2) { // only parts between two dollars are error text parameters
			String param = parts[i];
			if (!StringUtils.isEmpty(param)) {
				IErrortextParameter parsedParam = parseParam(parameterService, rule, condition, param, pm);
				if (errorTextParamContainsEntityReference(parsedParam, isDecoration)) {
					parts[i] = replaceReferenceInErrorTextParameter(param, parsedParam, newRuleFullName,
							fullNameUpdater, serviceConfiguration, rule, isDecoration);
				}
			}
		}
		return StringUtils.join(parts, "$");
	}

	private static IErrortextParameter parseParam(RuleErrorTextParameterService parameterService, IRule rule, ICondition condition,
			String param, ProblemReporter pm) {
		IErrortextParameter parsedParam = parameterService.parseParameter(rule, condition, param, pm);
		if (pm.hasProblemOccurred(Severity.ERROR)) {
			throw new IllegalArgumentException("Error text invalid:\n" + pm.getProblems().stream().map(IProblem::getMessage)
					.collect(Collectors.joining("\n")));
		}
		return parsedParam;
	}

	/**
	 * @param param
	 * 		parsed error text parameter (part between the dollars of an error text parameter)
	 * @return true, if the error text parameter is one of $#&lt;groupPath&gt;...$,
	 * 		$&lt;field&gt;...$, false otherwise
	 */
	private static boolean errorTextParamContainsEntityReference(IErrortextParameter param, boolean isDecoration) {
		return param instanceof IGroupIndexParameter || param instanceof ISimpleFieldParameter
				|| param instanceof IRootgroupNameParameter || param instanceof IRootgroupRowParameter
				/**
				 * For parallel iteration the index field parameter does not reference a concrete
				 * entity (because two index fields are involved) - that is why normally the index
				 * field parameter is not replaced.
				 * In contrast, for decorations the corresponding index field in the parallel group
				 * would be renamed as well and the new name would match, so adapting the parameter
				 * is safe. Additionally, it has to be adapted because otherwise the parameter would
				 * be invalid after the renaming.
				 */
				|| (param instanceof IIndexFieldParameter && isDecoration);
	}

	/**
	 * Replaces a reference of the old entity with a reference of the new entity. If the old entity
	 * was not referenced in the parameter, the unmodified parameter is returned.
	 * <p>
	 * Precondition:
	 * <ol>
	 * <li>The error text parameter is correct.</li>
	 * <li>The error text parameter is one of $#&lt;groupPath&gt;$, $&lt;field&gt;$ or
	 * $&lt;field&gt;.Wert$</li>
	 * </ol>
	 *
	 * @param origStringForNoModifyCase
	 * @param param
	 * 		parsed error text parameter (part between the dollars of an error text parameter)
	 * @param newRuleFullName
	 * 		used to determine, how the new entity has to be referenced
	 * @param fullNameUpdater
	 * 		used for determining the new full name of an entity
	 * @param rule
	 * 		rule whose error text is changed
	 * @param isDecoration
	 * 		true if the adjustment is done as part of a decoration, otherwise false
	 * @return the updated error text parameter
	 */
	private static String replaceReferenceInErrorTextParameter(String origStringForNoModifyCase, IErrortextParameter param,
			String newRuleFullName,
			UnaryOperator<String> fullNameUpdater, IServiceConfiguration serviceConfiguration, IRule rule, boolean isDecoration) {
		String oldRuleFullName = rule.getFullName();
		if (param instanceof IGroupIndexParameter groupIndexParameter) { // parameter $#<groupPath>$
			replaceIterationParameterInErrorText(groupIndexParameter, oldRuleFullName, newRuleFullName, fullNameUpdater,
					serviceConfiguration);
		} else if (param instanceof IRootgroupRowParameter rrParam) {
			rrParam.setField(getReplacement4VordruckzeileOrVordrucknameParam(rrParam.getField(), oldRuleFullName, fullNameUpdater,
					serviceConfiguration));
		} else if (param instanceof IRootgroupNameParameter rnParam) {
			rnParam.setField(getReplacement4VordruckzeileOrVordrucknameParam(rnParam.getField(), oldRuleFullName, fullNameUpdater,
					serviceConfiguration));
		} else if (param instanceof ISimpleFieldParameter fieldParam) {
			fieldParam.setEntityRefInErrorText(
					replaceEntityReference(fieldParam.getEntityRefInErrorText(), oldRuleFullName, newRuleFullName, fullNameUpdater,
							serviceConfiguration));
			if (fieldParam.getSemanticIndex() != null) {
				String newSemanticIndex = replaceSemanticIndexReference(
						fieldParam.getSemanticIndex().getSemanticIndexSpec(),
						oldRuleFullName,
						newRuleFullName, fullNameUpdater, serviceConfiguration);
				var semanticIndexRefFullName = fieldParam.getSemanticIndex().getSemanticIndexRefFullName();
				String newSemanticIndexFullname =
						semanticIndexRefFullName != null ?
								replaceSemanticIndexReference(semanticIndexRefFullName
										, oldRuleFullName, newRuleFullName, fullNameUpdater, serviceConfiguration
								) : null;

				fieldParam.setSemanticIndex(new SemanticIndex(newSemanticIndex, newSemanticIndexFullname));
			}
		} else if (param instanceof IIndexFieldParameter indexFieldParam && isDecoration) {

			IField refIndexField = getRefIndexField(indexFieldParam, rule, serviceConfiguration);
			if (refIndexField != null) {
				String oldFieldFullName = refIndexField.getFullName();
				String newFieldFullName = fullNameUpdater.apply(oldFieldFullName);
				if (!oldFieldFullName.equals(newFieldFullName)) {
					indexFieldParam.setEntityRefInErrorText(EntityUtils.getShortName(newFieldFullName));
				}
			}
		} else {
			return origStringForNoModifyCase; // when we _know_ that nothing can possibly have changed, return the original String,
			// in order to avoid accidental normalizations that might happen (e.g. token types that have aliases)
		}
		Locale lang = serviceConfiguration.getModelSpecificSettings().getLanguageUsedInConditionText();
		return param.format(lang);
	}//TODO[A12K-3890]: clean this method, if obsolete. Background:
	// in the current implementation, the properties of parameters from IErrortextParameter are only adjusted
	// partially, i.e. we set  IIndexFieldParameter.setEntityRefInErrorText but do not update IIndexFieldParameter
	// .getFieldFullName is done.
	// This is (or was) intended, because only the .entityRefInErrorText is process by consumers of replace.. methods in this
	// util class.
	// We expect with A12K-3890 that most of these methods become obsolete, and thereby need no further adjustment until then

	private static String getReplacement4VordruckzeileOrVordrucknameParam(String fieldName, String oldRulePath,
			UnaryOperator<String> pathUpdater, IServiceConfiguration serviceConfiguration) {
		String oldPath = determineOldFullName(fieldName, oldRulePath, serviceConfiguration);
		String newPath = pathUpdater.apply(oldPath);
		if (fieldName.equals(oldPath)) {
			return newPath;
		} else {
			String oldShortName = EntityUtils.getShortName(oldPath);
			if (fieldName.equals(oldShortName)) {
				String newShortName = EntityUtils.getShortName(newPath);
				String oldRuleGroupPath = EntityUtils.getParentPath(oldRulePath);
				boolean useShortName = ExtmpUtils.isHModel(serviceConfiguration.getModelSpecificSettings())
						|| EntityUtils.shouldShortNameBeUsed(oldRuleGroupPath, oldPath, newPath, serviceConfiguration);
				return useShortName ? newShortName : newPath;
			}
		}
		return fieldName;
	}

	private static IField getRefIndexField(IIndexFieldParameter indexFieldParam, IRule rule,
			IServiceConfiguration serviceConfiguration) {
		IEC ec = serviceConfiguration.getIEC();

		IEntity errorEntity = ec.get(rule.getErrorEntity().getFullName());
		Set<IField> indexFieldsOnErrorEntityPath = AnalyseService.getIndexFields(errorEntity, ec);
		List<IField> relevantIndexFields = indexFieldsOnErrorEntityPath.stream()
				.filter(f -> EntityUtils.getShortName(f.getFullName()).equals(indexFieldParam.getEntityRefInErrorText()))
				.toList();
		return relevantIndexFields.size() == 1 ? relevantIndexFields.get(0) : null;
	}

	private static String replaceEntityReference(String entityName, String oldRuleFullName, String newRuleFullName,
			UnaryOperator<String> fullNameUpdater, IServiceConfiguration serviceConfiguration) {
		String oldEntityFullName = determineOldFullName(entityName, oldRuleFullName, serviceConfiguration);
		String newEntityFullName = fullNameUpdater.apply(oldEntityFullName);
		if (oldEntityFullName.equals(newEntityFullName)) {
			return entityName;
		}
		String oldShortName = EntityUtils.getShortName(oldEntityFullName);
		boolean useShortName = entityName.equals(oldShortName) && EntityUtils.shouldShortNameBeUsed(newRuleFullName,
				oldEntityFullName, newEntityFullName, serviceConfiguration);
		IEntity entity = serviceConfiguration.getIEC().get(oldEntityFullName);
		boolean isHModel = ExtmpUtils.isHModel(serviceConfiguration.getModelSpecificSettings());

		if (useShortName) {
			return EntityUtils.getShortName(newEntityFullName);
		}
		return isHModel
				? EntityUtils.getNameBuiltForConditionTextHModel(newRuleFullName, newEntityFullName, entity.isField(), false)
				: EntityUtils.getNameBuiltForErrorTextParameterA12Model(newRuleFullName, newEntityFullName);
	}

	private static void replaceIterationParameterInErrorText(IGroupIndexParameter param, String oldRuleFullName,
			String newRuleFullName, UnaryOperator<String> fullNameUpdater, IServiceConfiguration serviceConfiguration) {
		String groupPath = param.getGroup();
		var semanticIndex = param.getSemanticIndex();

		if (semanticIndex != null) {
			String newSemanticIndexSpec
					= replaceSemanticIndexReference(semanticIndex.getSemanticIndexSpec(), oldRuleFullName, newRuleFullName,
					fullNameUpdater,
					serviceConfiguration);
			if (!semanticIndex.getSemanticIndexSpec().equals(newSemanticIndexSpec)) {

				var semanticIndexRefFullName = semanticIndex.getSemanticIndexRefFullName();
				String newSemanticIndexFullname =
						semanticIndexRefFullName != null ?
								replaceSemanticIndexReference(semanticIndexRefFullName
										, oldRuleFullName, newRuleFullName, fullNameUpdater, serviceConfiguration
								) : null;

				param.setSemanticIndex(new SemanticIndex(newSemanticIndexSpec, newSemanticIndexFullname));
			}
		}

		if (param.getGroup() != null) { // Only change if not rule group
			String newGroupName = replaceEntityReference(groupPath, oldRuleFullName, newRuleFullName, fullNameUpdater,
					serviceConfiguration);
			if (!newGroupName.equals(groupPath)) {
				param.setGroup(newGroupName);
			}
		}
	}

	private static String replaceSemanticIndexReference(String semanticIndex, String oldRuleFullName, String newRuleFullName,
			UnaryOperator<String> fullNameUpdater, IServiceConfiguration serviceConfiguration) {
		if (semanticIndex.startsWith("\"")) {
			return semanticIndex;
		} else {
			return replaceEntityReference(semanticIndex, oldRuleFullName, newRuleFullName, fullNameUpdater, serviceConfiguration);
		}
	}

	private static String determineOldFullName(String entityName, String oldRuleFullName,
			IServiceConfiguration serviceConfiguration) {
		if (EntityUtils.isAbsolutePath(entityName)) {
			return entityName;
		}
		IEntity fieldWithUniqueName = getUniquelyNamedField(entityName, serviceConfiguration.getIEC());
		return fieldWithUniqueName == null ?
				EntityUtils.getParentPathWithSlash(oldRuleFullName) + entityName :
				fieldWithUniqueName.getFullName();
	}//TODO[A12K-3890]: method should become obsolete

	private static IEntity getUniquelyNamedField(String entityName, IEC ec) {
		Set<String> fullNames = ec.shortName2FullName(EntityUtils.getShortName(entityName));
		return fullNames.size() == 1 ? ec.get(fullNames.iterator().next()) : null;
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
	 * specifically for A12 export, because <f1>.Vordruckzeile and <f1>.Vordruckname are not handled (those are replaced during A12 export)
	 */
	public static String translateParamsInRuleErrorMsgAndReplaceShortnamesForA12Export(String errorMsg, IRule rule, IEC ec,
			IModelSpecificSettings modelSpecificSettings
	) {
		return translateParamsInRuleErrorMsg(errorMsg, rule, ec, modelSpecificSettings,
				(IErrortextParameter parsedParam) -> {
					if (parsedParam instanceof IFieldParameter parsedField
							// "indexField" (see grammar) references are always by shortName, and relative or absolute
							// paths are not allowed there. Note: the name of the index field is only necessary at all if
							// there are multiple index fields on the path of the error field, or so.
							// (see checkIndexFieldRef in FullChecks_ErrorTextParameterParser)
							&& !(parsedField instanceof IIndexFieldParameter)
					) {
						replaceShortnameWithAbsOrRelPath(parsedField, rule);
						return parsedField;
					} else if (parsedParam instanceof IGroupIndexParameter groupIndexParam) {
						replaceShortnameWithAbsOrRelPath(groupIndexParam, rule);
						return groupIndexParam;
					} else {
						// Note: <f1>.Vordruckzeile and <f1>.Vordruckname also seem to allow shortname refs for f1, but they are
						// replaced during A12 export by the corresponding constant values, so we don't need to handle them here
						return parsedParam;
					}
				});
	}

	private static void replaceShortnameWithAbsOrRelPath(IFieldParameter parsedField, IRule rule) {
		if(parsedField.getFieldFullName() != null //at the time of implementation the null case could not happen, yet we are defensive in not throwing
			&& !parsedField.getEntityRefInErrorText().contains(EntityUtils.SEPARATOR) //rel paths or absolute paths are not replaced
		) {
			parsedField.setEntityRefInErrorText(makeRelOrAbsPath(parsedField.getFieldFullName(), rule));
		}

		if(parsedField instanceof ISimpleFieldParameter parsedSimpleField) { // the other sub-interface, IIndexFieldParameter, corresponds to the index(...) language construct in the errtextparam grammar that does not currently support non-shortnames
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
		if(semanticIndex == null) {
			return null;
		}
		var semIdxFullName =  semanticIndex.getSemanticIndexRefFullName();
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
		var ruleGroupPath = ElementPathUtils.getParentPath(rule.getFullName());
		var replacedPath = fullName;
		if (EntityUtils.getRootGroupName(ruleGroupPath).equals(EntityUtils.getRootGroupName(fullName))) {
			//use relPath:
			replacedPath = EntityUtils.absPath2RelPathNoValidate(fullName, ruleGroupPath, true);
		}
		return replacedPath;
	}

	/**
	 * Translate the parameter found in the given rule error message to English.
	 *
	 * @param errorMsg
	 * 		error message to be translated, may not be null. The error message has to be
	 * 		correct (especially the error text parameters).
	 * @param rule
	 * 		the rule to that the errorMsg belongs, may not be null
	 * @param mapParsedParamAndMaybeModify
	 * 		optional; skipped if null. The supplied function is allowed to modify or destroy the
	 * 		given parameter as a side effect.
	 * @return the translated rule error message.
	 */
	public static String translateParamsInRuleErrorMsg(String errorMsg, IRule rule, IEC ec,
			IModelSpecificSettings modelSpecificSettings,
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
			param = parsedParam.format(Locale.US);
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