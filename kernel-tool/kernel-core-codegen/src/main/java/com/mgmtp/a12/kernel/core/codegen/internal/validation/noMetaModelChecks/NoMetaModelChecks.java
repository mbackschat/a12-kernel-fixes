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

package com.mgmtp.a12.kernel.core.codegen.internal.validation.noMetaModelChecks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.SourceVersion;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.CharsetUtil;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.base.utils.internal.charset.LegalCharTester;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BaseCodeGenUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.service.internal.RuleParser;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FieldErrortextChecker;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationDataTable;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IDynamicEnumerationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ISortField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.enumTable.CSVTableChecker;
import com.mgmtp.a12.model.notification.Severity;

public class NoMetaModelChecks {

	private static final Pattern ROOT_GROUP_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_üöäÜÖÄß]+");
	private static final Pattern GROUP_NAME_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_üöäÜÖÄß]*");
	private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_:]+([.][a-zA-Z][a-zA-Z0-9_:]*)*");
	private static final Pattern MODEL_NAME_PATTERN_A12 = Pattern.compile("(?![xX][mM][lL])[a-zA-Z_][a-zA-Z_0-9-.]*");
	private static final Pattern MODEL_NAME_PATTERN_EXTMP = Pattern.compile("[a-zA-Z_0-9]*");

	private final CodeGenData data;
	private final ProblemReporter problemReporter;
	private final NoMetaModelCheckErrorLookup errorLookup;
	private final Collection<Locale> languages;
	private final List<String> restrictedValidGraphemeSet;
	private final Optional<LegalCharTester> legalCharTesterOptional;
	private final ICodeGeneratorParameters.ProgrammingLanguage programmingLanguage;

	public NoMetaModelChecks(CodeGenData data, ProblemReporter problemReporter) {
		this.data = data;
		this.problemReporter = problemReporter;
		this.errorLookup = new NoMetaModelCheckErrorLookup(data.getModelSpecificSettings().getLanguageUsedInConditionText());
		this.languages = data.getParams().getLanguages();
		this.restrictedValidGraphemeSet = data.getParams().restrictedValidGraphemeSet();
		this.legalCharTesterOptional = LegalCharTester.fromGraphemeSet(restrictedValidGraphemeSet);
		this.programmingLanguage = data.getParams().getProgrammingLanguage();

	}

	/**
	 * @return <code>true</code>, if all non-meta-model-checks are OK
	 */
	public boolean check() {
		boolean name4GeneratedCodeValid = checkName4GeneratedCode();
		boolean packageName4GeneratedCodeValid = checkPackageName4GeneratedCode();
		boolean entityContainerValid = checkEntityContainer();
		boolean languagesValid = checkLanguages();
		boolean restrictedValidCharacterSetValid = checkRestrictedValidCharacterSet();
		boolean dynamicEnumerationsValid = checkDynamicEnumerations();
		boolean timeZoneValid = checkTimeZone();
		if (!(name4GeneratedCodeValid && packageName4GeneratedCodeValid && entityContainerValid && languagesValid
				&& restrictedValidCharacterSetValid && dynamicEnumerationsValid && timeZoneValid)) {
			return false;
		}

		boolean enumTablesValid = checkEnumTables(); // Searching fields in the complete IEC should not be done if checkEntityContainer may have errors.
		boolean indexFieldsValid = checkIndexFields();
		boolean sortedFieldsValid = checkSortFields();
		if (!(indexFieldsValid && sortedFieldsValid && enumTablesValid)) {
			return false;
		}
		return checkCustomRefErrorEntities(); // checkCustomRefErrorEntities is checked only after all other checks pass, because
		// otherwise some reported problems may be duplicated (checkCustomRefErrorEntities attempts to parse the rule condition,
		// which can result in problem reports that are not specific to the customRefErrorEntities)
	}

	private boolean checkTimeZone() {
		String timeZoneStr = data.getModelSpecificSettings().getTimeZone();
		if ("GMT".equals(timeZoneStr)) {
			return true;
		}
		TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
		if ("GMT".equals(timeZone.getID())) {
			errorLookup.reportError(problemReporter, null, NoMetaModelCheckErrorType.MVK_INVALID_TIME_ZONE, timeZoneStr);
			return false;
		}
		return true;
	}

	private boolean checkDynamicEnumerations() {
		Map<String, List<IDynamicEnumerationInfo>> dynEnumsByName = data.getParams().getDynamicEnumerationInformation().stream()
				.collect(Collectors.groupingBy(de -> de.getDynamicEnumerationField().getFullName()));
		AtomicBoolean result = new AtomicBoolean(true);
		dynEnumsByName.entrySet().stream()
				.filter(e -> e.getValue().size() > 1)
				.forEach(e -> {
					addDynEnumError(e.getValue());
					result.set(false);
				});
		return result.get();
	}

	private void addDynEnumError(List<IDynamicEnumerationInfo> entries) {
		String name1 = entries.get(0).getName();
		String name2 = entries.get(1).getName();
		String enumFieldName = entries.get(0).getDynamicEnumerationField().getFullName();
		IEntity entity = data.getEc().get(enumFieldName);
		errorLookup.reportError(problemReporter, entity, NoMetaModelCheckErrorType.MVK_DUPLICATE_DYNAMIC_ENUM, enumFieldName, name1,
				name2);
	}

	private boolean checkIndexFields() {
		return Java8Utils.allMatchEvaluateAll(EntityUtils.getAllGroups(data.getEc()).stream(), this::checkIndexFieldsForGroup);
	}

	private Stream<Pair<String, IGroup>> getIndexFieldPairs(IGroup group) {
		return group.getIndexFieldNames().stream().map(fn -> Pair.<String, IGroup>of(fn, group));
	}

	private boolean checkIndexFieldsForGroup(IGroup group) {
		return Java8Utils.allMatchEvaluateAll(group.getIndexFieldNames().stream(),
				fn -> checkIndexField(fn, group));
	}

	private boolean checkIndexField(String fieldName, IGroup group) {
		IEntity indexEntity = EntityUtils.getEntityInGroup(data.getEc(), fieldName, group.getFullName());

		if (indexEntity == null || !indexEntity.isField()) {
			errorLookup.reportError(problemReporter, group, NoMetaModelCheckErrorType.MVK_INVALID_INDEX_FIELD, fieldName,
					group.getFullName());
			return false;
		}
		IField indexField = (IField) indexEntity;
		String indexFieldFullName = String.format("%s/%s", group.getFullName(), fieldName);
		// from here we use the full name of the field,
		// otherwise the error message does not have enough information in order to fix the problem
		if (FieldUtils.isNoValueValidation(indexField)) {
			errorLookup.reportError(problemReporter, indexField, NoMetaModelCheckErrorType.MVK_INDEX_FIELD_NO_VALUE_VALIDATION,
					indexFieldFullName);
			return false;
		}
		if (FieldUtils.isTableEnum(indexField.getType())) {
			errorLookup.reportError(problemReporter, indexField, NoMetaModelCheckErrorType.MVK_INDEX_FIELD_NO_TABLE_ENUM,
					indexFieldFullName);
			return false;
		}

		return true;
	}

	private boolean checkSortFields() {
		return Java8Utils.allMatchEvaluateAll(EntityUtils.getAllGroups(data.getEc()).stream(),
				this::checkSortFieldsForGroup);
	}

	private boolean checkSortFieldsForGroup(IGroup group) {
		List<ISortField> sortFields = group.getSortInfo().get(CodeGenConstants.SORT_CATEGORY_DEFAULT);
		if (sortFields != null) {
			return Java8Utils.allMatchEvaluateAll(sortFields.stream(),
					sf -> checkSortField(sf.getName(), group));
		}
		return true;
	}

	private boolean checkSortField(String fieldName, IGroup group) {
		IEntity sortEntity = EntityUtils.getEntityInGroup(data.getEc(), fieldName, group.getFullName());

		if (sortEntity == null || !sortEntity.isField()) {
			errorLookup.reportError(problemReporter, group, NoMetaModelCheckErrorType.MVK_INVALID_SORT_FIELD, fieldName,
					group.getFullName());
			return false;
		}
		IField sortField = (IField) sortEntity;

		String sortFieldFullName = String.format("%s/%s", group.getFullName(), fieldName);
		// from here we use the full name of the field,
		// otherwise the error message does not have enough information in order to fix the problem
		if (FieldUtils.isNoValueValidation(sortField)) {
			errorLookup.reportError(problemReporter, sortField, NoMetaModelCheckErrorType.MVK_SORT_FIELD_NO_VALUE_VALIDATION,
					sortFieldFullName);
			return false;
		}

		return true;
	}

	private boolean checkCustomRefErrorEntities() {
		RuleParser ruleParser = new RuleParser(
				new ServiceConfiguration(data.getEc(), data.getModelSpecificSettings(), data.getPss()));
		Collection<IRule> rules = new ArrayList<>(data.getRules());

		return Java8Utils.allMatchEvaluateAll(
				rules.stream(),
				(IRule rule) -> {
					if (rule instanceof RuleInfo ruleInfo) {
						// avoid additional parsing if the rule is already a RuleInfo with cached condition
						return ruleParser.hasValidCustomErrorEntities(
								ruleInfo,
								problemReporter,
								RuleInfo::getConditionNoExpandForCustomRefErrorEntities);
					} else {
						return ruleParser.hasValidCustomErrorEntities(
								rule,
								problemReporter);
					}
				});
	}

	private boolean checkRestrictedValidCharacterSet() {
		final CharsetUtil charsetUtil = new CharsetUtil(data.getModelSpecificSettings().getLanguageUsedInConditionText());
		if (restrictedValidGraphemeSet.isEmpty()) {
			return true;
		}

		charsetUtil.validate(restrictedValidGraphemeSet, problemReporter);
		return !problemReporter.hasProblemOccurred(Severity.ERROR);
	}

	private boolean checkLanguages() {
		if (ProgrammingLanguage.JAVA_SCRIPT_VK == programmingLanguage) {
			return true;
		}
		boolean result = true;
		for (Locale language : languages) {
			if (!data.getPss().getProjectLanguageSettings().containsKey(language)) {
				errorLookup.reportError(problemReporter, null, NoMetaModelCheckErrorType.MVK_NO_PROJECT_LANGUAGE_SETTINGS,
						language);
				result = false;
			}
		}

		return result;
	}

	private boolean checkEnumTables() {
		List<IField> tableEnumFields = EntityUtils.getAllFields(data.getEc()).stream()
				.filter(f -> f.getType() instanceof IEnumerationType &&
						((IEnumerationType) f.getType()).getTableName().isPresent())
				.collect(Collectors.toList());
		if (tableEnumFields.isEmpty()) {
			return true;
		}
		if (programmingLanguage == ProgrammingLanguage.GROOVY
				|| programmingLanguage == ProgrammingLanguage.JAVA_SCRIPT_VK) {
			errorLookup.reportError(problemReporter, tableEnumFields.get(0),
					NoMetaModelCheckErrorType.MVK_TABLE_ENUMS_NOT_ALLOWED_FOR_PROGRAMMING_LANGUAGE, programmingLanguage.toString());
			return false;
		}
		Map<String, Set<String>> enumTablesToUsedFields = tableEnumFields.stream()
				.collect(Collectors.groupingBy(
						f -> ((IEnumerationType) f.getType()).getTableName().get(),
						Collectors.mapping(IField::getFullName, Collectors.toSet())));
		boolean result = Java8Utils.allMatchEvaluateAll(enumTablesToUsedFields.entrySet().stream(),
				e -> checkEnumTable(e.getKey(), e.getValue()));
		return result && Java8Utils.allMatchEvaluateAll(tableEnumFields.stream(), this::checkTableEnum);
	}

	private boolean checkTableEnum(IField field) {
		boolean result = true;
		IEnumerationType enumType = (IEnumerationType) field.getType();
		// That there is a table name was checked outside
		String tableName = enumType.getTableName().orElseThrow(() -> new NoSuchElementException("No value present"));
		// That the table exists was checked already
		Optional<IEnumerationDataTable> table = enumType.getTable();
		if (!table.isPresent()) {
			throw new IllegalArgumentException(
					"Existing table was not transferred to enumeration type. Wrong implementation of EnumerationType?");
		}

		Map<String, List<String>> columns = table.get().getColumns();
		// All dependent columns must exist in the table
		for (String depColumn : enumType.getDependentColumns()) {
			if (!columns.containsKey(depColumn)) {
				errorLookup.reportError(problemReporter, field, NoMetaModelCheckErrorType.MVK_TABLE_ENUM_DEP_COLUMN_MISSING,
						depColumn, tableName);
				result = false;
			}
		}

		// The xml column must exist in the table
		Optional<String> columnNameOpt = enumType.getColumnName();
		if (!columnNameOpt.isPresent()) {
			// The error is handled by the meta model
			return false;
		}
		String columnName = columnNameOpt.get();
		if (!columns.containsKey(columnName)) {
			errorLookup.reportError(problemReporter, field, NoMetaModelCheckErrorType.MVK_TABLE_ENUM_COLUMN_MISSING, columnName,
					tableName);
			result = false;
		}

		// Category columns defined in the enum must exactly match the ones specified in the table
		Set<String> categoriesFromType = enumType.getCategoryValues().keySet();
		Set<String> categoriesFromTable = extractCategories(columns, columnName);
		if (!categoriesFromType.equals(categoriesFromTable)) {
			String fromTypeCategoryString = categoriesFromType.stream().collect(Collectors.joining(", "));
			String fromTableCategoryString = categoriesFromTable.stream().collect(Collectors.joining(", "));
			errorLookup.reportError(problemReporter, field, NoMetaModelCheckErrorType.MVK_TABLE_ENUM_DIFF_CATEGORIES,
					fromTypeCategoryString, tableName, fromTableCategoryString);
			result = false;
		}

		// If there is a display value, the model must not be multilingual
		if (columns.containsKey(columnName + CSVTableChecker.DISPLAY_SUFFIX)
				&& data.getParams().getLanguages().size() != 1) {
			errorLookup.reportError(problemReporter, field,
					NoMetaModelCheckErrorType.MVK_TABLE_ENUM_WITH_MULTILINGUAL_DISPLAY_VALUE,
					columnName, tableName);
			result = false;
		}
		return result;
	}

	private Set<String> extractCategories(Map<String, List<String>> columns, String columnName) {
		String categoryPrefix = columnName + ".";
		int prefixLen = categoryPrefix.length();
		return columns.keySet().stream()
				.filter(c -> c.startsWith(categoryPrefix))
				.map(c -> c.substring(prefixLen))
				.collect(Collectors.toSet());
	}

	private boolean checkEnumTable(String tableName, Set<String> fieldNames) {
		if (data.getEc().getEnumerationTables() == null ||
				!data.getEc().getEnumerationTables().containsKey(tableName)) {
			String fieldNamesForErrorMessage = fieldNames.stream().collect(Collectors.joining(", "));
			errorLookup.reportError(problemReporter, null, NoMetaModelCheckErrorType.MVK_ENUM_TABLE_MISSING, tableName,
					fieldNamesForErrorMessage);
			return false;
		}

		byte[] tableRawData = data.getEc().getEnumerationTables().get(tableName).getBytes();
		return CSVTableChecker.checkCsvTable(tableRawData, tableName, restrictedValidGraphemeSet, problemReporter);
	}

	/**
	 * @return <code>true</code>, if entity container is OK
	 */
	private boolean checkEntityContainer() {

		// Iterate over all root groups
		for (IEntity e : data.getEc().getDirectlyContainedEntities(EntityUtils.SEPARATOR)) {
			if (e.isGroup()) {
				checkEntity(e, EntityUtils.SEPARATOR);
			} else {
				// Field on root group level

				// Das Feld '%s' hat keine Eltern-Entität.
				errorLookup.reportError(problemReporter, e, NoMetaModelCheckErrorType.MVK_FIELD_NO_PARENT, e.getFullName());
			}
		}

		return !problemReporter.hasErrorOccurred();
	}

	/**
	 * @return <code>true</code>, if given package name is valid.
	 */
	private boolean checkPackageName4GeneratedCode() {
		boolean result = true;
		// we allow the dot at the end of the package name, but for the check we don't need it
		String packageName4GeneratedCode = BaseCodeGenUtils.getNormalizedPackageNameOrDefault(data.getParams())
				.replaceAll("\\.$", "");
		if (!SourceVersion.isName(packageName4GeneratedCode)) {
			errorLookup.reportError(problemReporter, null, NoMetaModelCheckErrorType.MVK_INVALID_PACKAGE_NAME_4_GENERATED_CODE,
					packageName4GeneratedCode);
			result = false;
		}
		return result;
	}

	/**
	 * @return <code>true</code>, if given name is valid.
	 */
	private boolean checkName4GeneratedCode() {
		boolean result = true;
		String name = data.getParams().getName();
		Pattern namePattern = ExtmpUtils.isExtmp(data.getModelSpecificSettings()) ?
				MODEL_NAME_PATTERN_EXTMP :
				MODEL_NAME_PATTERN_A12;
		if (StringUtils.isEmpty(name) || !namePattern.matcher(name).matches()) {
			errorLookup.reportError(problemReporter, null, NoMetaModelCheckErrorType.MVK_INVALID_NAME_4_GENERATED_CODE,
					data.getParams().getName());
			result = false;
		}
		return result;

	}

	/**
	 * @return <code>false</code>, if group- or field name(s) don't match RegEx (or are empty)
	 */
	private boolean check_MVK_PATH_INVALID(IEntity entity) {
		if (StringUtils.isEmpty(entity.getFullName())) {
			return false;
		}

		Matcher m;
		String[] path = entity.getFullName().split(EntityUtils.SEPARATOR, -1);
		for (int i = 0; i < path.length; i++) {
			if (i == 0) { // First (0th) token is the starting separator
				if (!path[i].isEmpty()) {
					return false;
				}
			} else if (i == 1) { // Second (1st) token is the root group name
				m = ROOT_GROUP_NAME_PATTERN.matcher(path[i]);
				if (!m.matches()) {
					return false;
				}
			} else if (entity.isField() && i == path.length - 1) { // Last token of a field is a field name
				m = FIELD_NAME_PATTERN.matcher(path[i]);
				if (!m.matches() || (!ExtmpUtils.isHModel(data.getModelSpecificSettings()) && Character.isDigit(
						path[i].charAt(0)))) {
					return false;
				}
			} else { // All other tokens are group names
				m = GROUP_NAME_PATTERN.matcher(path[i]);
				if (!m.matches()) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * @return <code>false</code>, if entity's parent path due to full name isn't equal to
	 * 		parentPath (how entity was derived from IEC)
	 */
	private boolean check_MVK_PATHS_INCONSISTENT(IEntity entity, String parentPath) {
		String parentPathDerivedFromFullname = EntityUtils.getParentPath(entity.getFullName());
		if (parentPathDerivedFromFullname.isEmpty() && parentPath.equals(EntityUtils.SEPARATOR)) {
			// A root group's derived parent path is 'empty'
			return true;
		}

		return parentPathDerivedFromFullname.equals(parentPath);
	}

	/**
	 * @return <code>false</code>, if entity is a field containing (other) groups and/or fields
	 */
	private boolean check_MVK_FIELD_WITH_CONTAINED_ENTITIES(IEntity entity) {
		if (!entity.isField()) {
			return true;
		}

		List<IEntity> directlyContainedEntities = data.getEc().getDirectlyContainedEntities(entity.getFullName());
		return directlyContainedEntities.isEmpty();
	}

	/**
	 * @return <code>true</code>, if path to parent is empty (in root groups) or path to parent
	 * 		represents an entity in iec.
	 */
	private boolean check_MVK_PARENT_ENTITY_MISSING(IEntity entity) {
		String parentPathDerivedFromFullname = EntityUtils.getParentPath(entity.getFullName());
		return parentPathDerivedFromFullname.isEmpty() || data.getEc().get(parentPathDerivedFromFullname) != null;
	}

	/**
	 * @return <code>false</code>, if at least one of the checks for max repetition layers
	 * 		determines an error.
	 */
	private boolean check_MVK_MAX_VALUES_INVALID(IEntity entity) {
		long[] maxRep = entity.getMax();
		if (maxRep == null) {
			return false;
		}

		for (long i : maxRep) {
			if (i <= 0) {
				// Max repetition isn't greater than 0
				return false;
			}
		}
		if (ExtmpUtils.isHModel(data.getModelSpecificSettings())) {
			if (entity.isGroup() && maxRep.length != 1) {
				// Length of a group's max rep array is not exactly 1
				return false;
			}
			if (entity.isField() && maxRep.length != 2) {
				// Length of a field's max rep array is not exactly 2
				return false;
			}
		} else {
			if (StringUtils.countMatches(entity.getFullName(), EntityUtils.SEPARATOR_CHAR) != maxRep.length) {
				// Length of max rep array is not equal to amount of slashes in full name
				return false;
			}

			if (entity.isField() && maxRep[maxRep.length - 1] != 1) {
				// Field's last may rep layer is not exactly 1
				return false;
			}
		}

		return true;
	}

	/**
	 * @return <code>false</code>, if a max repetition layer doesn't match its parent's layer
	 */
	private boolean check_MVK_MAX_VALUES_INCONSISTENT(IEntity entity, String parentPath) {
		if (entity.getMax() == null) {
			// This error has also been caught in previous check_MVK_MAX_VALUES_INVALID
			return false;
		}

		if (parentPath.equals(EntityUtils.SEPARATOR)) {
			// For root groups, the return value is true, if its length is 1
			return entity.getMax().length == 1;
		}

		long[] maxRepParent = data.getEc().get(parentPath).getMax();
		if (maxRepParent == null) {
			// We assume the path being inconsistent, if there is no parent max rep layer
			return false;
		}

		if (ExtmpUtils.isHModel(data.getModelSpecificSettings())) {
			return checkMaxValuesConsistentHDV(entity, maxRepParent);
		}

		long[] maxRep = entity.getMax();
		if (maxRep.length != maxRepParent.length + 1) {
			return false;
		}

		for (int i = 0; i < maxRep.length - 1; i++) {
			if (maxRep[i] != maxRepParent[i]) {
				// i-th max rep layer of entity and its parent is not equal
				return false;
			}
		}

		return true;
	}

	private boolean checkMaxValuesConsistentHDV(IEntity entity, long[] maxRepParent) {
		long[] maxRep = entity.getMax();
		if (entity.isField()) {
			// A field always has 2 indices in an H model, and a group 1
			if (maxRep.length != 2 || maxRepParent.length != 1) {
				return false;
			}
		} else {
			// All groups have 1 index
			if (maxRep.length != 1 || maxRepParent.length != 1) {
				return false;
			}
		}
		// max repetition of root group must match.
		return maxRep[0] == maxRepParent[0];
	}

	private void checkFieldErrorText(IField field) {
		FieldErrortextChecker fec = new FieldErrortextChecker(field, data.getModelSpecificSettings(), true);
		for (Locale languageLocale : languages) {
			String errorText = FieldUtils.getErrorText(field.getType(), languageLocale);
			if (errorText != null) {
				fec.checkErrorText(errorText, languageLocale, problemReporter);
			}
		}
	}

	/**
	 * Checks, if entity is OK.
	 * <p>
	 * Errors are stored in problemReporter.
	 */
	private void checkEntity(final IEntity entity, final String parentPath) {
		if (!check_MVK_PATH_INVALID(entity)) {
			// Der Pfad '%s' ist ungültig.
			errorLookup.reportError(problemReporter, entity, NoMetaModelCheckErrorType.MVK_PATH_INVALID, entity.getFullName());
			return; // Stop at invalid path
		}

		if (!check_MVK_PATHS_INCONSISTENT(entity, parentPath)) {
			// Der Pfad '%s' passt nicht mit dem Pfad der Eltern-Entität '%s' zusammen.
			errorLookup.reportError(problemReporter, entity, NoMetaModelCheckErrorType.MVK_PATHS_INCONSISTENT, entity.getFullName(),
					parentPath);
			return; // Stop at inconsistent path
		}

		if (!check_MVK_FIELD_WITH_CONTAINED_ENTITIES(entity)) {
			// '%s' ist ein Feld und kann daher keine Entitäten enthalten.
			errorLookup.reportError(problemReporter, entity, NoMetaModelCheckErrorType.MVK_FIELD_WITH_CONTAINED_ENTITIES,
					entity.getFullName());
			return;
		}

		if (!check_MVK_PARENT_ENTITY_MISSING(entity)) {
			// Für '%s' ist die Eltern-Entität '%s' nicht im Entity-Container enthalten.
			errorLookup.reportError(problemReporter, entity, NoMetaModelCheckErrorType.MVK_PARENT_ENTITY_MISSING,
					entity.getFullName(), EntityUtils.getParentPath(entity.getFullName()));
			return;
		}

		if (!check_MVK_MAX_VALUES_INVALID(entity)) {
			// Die maximalen Indices für '%s' sind ungültig.
			errorLookup.reportError(problemReporter, entity, NoMetaModelCheckErrorType.MVK_MAX_VALUES_INVALID,
					entity.getFullName());
		}

		if (!check_MVK_MAX_VALUES_INCONSISTENT(entity, parentPath)) {
			// Die maximalen Indices für '%s' und '%s' passen nicht zusammen.
			errorLookup.reportError(problemReporter, entity, NoMetaModelCheckErrorType.MVK_MAX_VALUES_INCONSISTENT,
					entity.getFullName(), parentPath);
		}

		if (entity instanceof IField field) {
			checkFieldErrorText(field);
			checkEnumValues(field);
		}

		List<IEntity> directlyContainedEntities = data.getEc().getDirectlyContainedEntities(entity.getFullName());
		for (IEntity e : directlyContainedEntities) {
			checkEntity(e, entity.getFullName());
		}
	}

	private void checkEnumValues(final IField field) {
		if (legalCharTesterOptional.isEmpty() || restrictedValidGraphemeSet.isEmpty()) {
			return;
		}
		if (field.getType() instanceof IEnumerationType) {
			IEnumerationType type = (IEnumerationType) field.getType();
			type.getXMLValues().stream().forEach(value -> checkEnumValue(field, value, legalCharTesterOptional.get()));
			//no check for display values, they can have any characters
		}
		if (field.getType() instanceof IStringType type && type.isExtensibleEnum()) {
			for (Locale language : languages) {
				if (type.getHintList(language) != null) {
					type.getHintList(language).stream()
							.forEach(enumValue -> checkEnumValue(field, enumValue, legalCharTesterOptional.get()));
				}
			}
		}
	}

	private void checkEnumValue(final IField field, final String value, final LegalCharTester legalCharTester) {
		legalCharTester.findErrorIndizes(value)
				.forEach(idx -> {
					errorLookup.reportError(problemReporter, field, NoMetaModelCheckErrorType.MVK_ENUM_VALUE_WITH_INVALID_CHARACTER,
							field.getFullName(), value, String.valueOf(value.charAt(idx)));
				});
	}
}
