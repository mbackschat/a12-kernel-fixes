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

package com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckRelUpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserError;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.IndexArt;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorLookup;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorType;
import com.mgmtp.a12.kernel.core.service.internal.errortext.RuleErrorTextIterationParamVisitor;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.checker.CheckIdNameImpl;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.checker.CheckRelativeUpImpl;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.languages.ErrortextParameterMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType.EnumerationValue;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.internal.utils.ECUtils;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

/**
 * Checks the parameter between two $-signs in a rule error text.
 * <p/>
 * This class has the full implementation of the check* methods, the base class {@link GrammarOnly_ErrorTextParameterParser} has
 * only emtpy
 * placeholder implementations inherited from {@link AbstractErrorTextParameterParser}.
 * <p/>
 * Valid parameters are:
 *
 * <ul>
 * <li>empty parameter</li>
 * <li>Referenzjahr, Referenzjahr +/- constant for models with a base year {@link #checkReferenzjahr()}</li>
 * <li>#LfdNr, checks for correct usage in {@link #checkFehlerEnum(LfdNrContext)}</li>
 * <li>#Zeile, checks for correct usage in {@link #checkZeile()})</li>
 * <li>#Usb, checks for correct usage in {@link #checkUSB()}</li>
 * <li>#group for models with not unique names, checks for correct usage in
 * {@link #checkLevelParam}</li>
 * <li>field reference</li>
 * <li>&lt;field reference&gt;.Wert</li>
 * <li>&lt;field reference&gt;.Vordruckzeile, checks for correct usage in
 * {@link #checkVordruckzeile}</li>
 * </ul>
 */
public class FullChecks_ErrorTextParameterParser extends GrammarOnly_ErrorTextParameterParser {

	private IRule rule;
	private String rulePath;
	private boolean hasBaseYear;
	private boolean isUIteration;
	private boolean isZIteration;

	private IEntity errorEntity;

	private FehlertextErrorLookup errorHandler;
	private ErrortextParameterMultiLanguageHandler multiLanguageHandler;

	private IEC ec;
	private EntityResolver entityResolver;
	/**
	 * Specifies whether the condition is valid. If the condition is not valid, it is null and no
	 * checks can be performed, that require the condition, so these checks are skipped in this
	 * case. This is not a problem because in the case of an invalid condition, this error is
	 * already reported at another place.
	 */
	private boolean conditionChecks;
	private ICondition condition;
	private String errorTextParameter;

	/**
	 * map of a parsed EntitySpecContext to its resolved entity
	 */
	private final Map<EntitySpecContext, EntityRef> referencedEntities = new HashMap<>();

	private RefEntities refEntities;

	/**
	 * Some referenced entities are not automatically created during the checks. E.g. the indirectly referenced index fields. If
	 * one would like to put the latter into referencedEntities, one would have to use different ParserRuleContexts for the
	 * different index fields. This is not easy to do.
	 * Therefore, to have these entities in the result of {@link #getReferencedEntities()}, we store them in this set.
	 */
	private final Set<IEntity> additionallyReferencedEntities = new HashSet<>();

	public static class EntityRef {

		private final String nameUsedInErrorText;
		private final IEntity entity;

		public EntityRef(String nameUsedInErrorText, IEntity entity) {
			this.nameUsedInErrorText = nameUsedInErrorText;
			this.entity = entity;
		}

		public String getNameUsedInErrorText() {
			return nameUsedInErrorText;
		}

		public IEntity getEntity() {
			return entity;
		}

	}

	@FunctionalInterface
	public interface ErrorComposer {
		MVKParserError composeError(FehlertextErrorType errorType, Object... args);
	}

	public FullChecks_ErrorTextParameterParser(TokenStream input) {
		super(input);
	}

	/**
	 * @param condition
	 * 		<b>expanded</b> condition or null (if null, checks that require the
	 * 		condition are
	 * 		not performed)
	 */
	public void initParser(IRule rule, ICondition condition, String errorTextParameter,
			IEC ec, IModelSpecificSettings modelSpecificSettings) {
		super.initParser(ExtmpUtils.isHModel(modelSpecificSettings), this::isGroupFromHModelTest);
		this.rule = rule;
		this.rulePath = EntityUtils.getRuleGroupName(rule);
		this.hasBaseYear = ECUtils.isBaseYearDefined(modelSpecificSettings);
		this.ec = ec;
		this.entityResolver = new EntityResolver(rule, ec);
		this.errorTextParameter = errorTextParameter;
		this.condition = condition;
		this.errorHandler = new FehlertextErrorLookup(modelSpecificSettings.getLanguageUsedInConditionText());
		this.multiLanguageHandler = new ErrortextParameterMultiLanguageHandler(
				modelSpecificSettings.getLanguageUsedInConditionText());

		if (condition != null) {
			this.isUIteration = AnalyseService.referenziertIteration(condition, IndexArt.USB);
			this.isZIteration = AnalyseService.referenziertIteration(condition, IndexArt.ZEILE);
			this.refEntities = new RefEntities(condition, ec, rulePath, isHModel());
			this.conditionChecks = true;
		} else {
			this.isUIteration = false;
			this.isZIteration = false;
			this.refEntities = null;
			this.conditionChecks = false;
		}

		this.errorEntity = rule.getErrorEntity();

	}

	/**
	 * Checks the error text parameters $&lt;field&gt;...$ where ... may be '.value' ('.Wert') or a
	 * category reference ('->&lt;categoryname&gt;') and/or a semantic index.
	 */
	@Override protected void checkFieldRef(FieldContext ctx) {
		if (ctx.entitySpec().regelKontextEntity() != null) {
			throw composeError(FehlertextErrorType.INVALID_PARAMETER, errorTextParameter);
		}

		// check existence of category also if no condition was passed to avoid errors in ErrorTextParameterCreator
		String categoryName = ctx.valueOrCategory() instanceof CategoryContext catCtxt ? checkAndGetCategoryName(catCtxt,
				ctx.id.getText()) : null;

		/*
		 * Es wurden keine Felddefinitionen gesetzt, d.h. Prüfung kann hier aufhören. Eine Prüfung,
		 * ob Angaben der Zeilen bzw. laufenden Nr. zu den Felddefinitionen passen kann nicht
		 * erfolgen. Ebenfalls kann nicht geprüft werden, ob das Feld in der Menge der
		 * referenzierten Felder liegt.
		 */
		if (refEntities == null) {
			return;
		}

		String entityNameInErrorText = "";
		if (ctx.entitySpec().entity() != null) {
			entityNameInErrorText = ctx.entitySpec().entity().getText();
		} else if (ctx.entitySpec().entityHModel() != null) {
			entityNameInErrorText = ctx.entitySpec().entityHModel().pathHModel().getText();
		}
		if (isHModel() && entityNameInErrorText.contains("/")) {
			throw composeError(FehlertextErrorType.INVALID_PATH_SPEC, errorTextParameter);
		}

		String fullName = getEntityRef(ctx.entitySpec()).getEntity().getFullName();
		RefEntities.FullName refEntitiesFullName = new RefEntities.FullName(fullName);
		IEntityId firstId = checkFieldReferencedInConditionAndGetFirstId(refEntitiesFullName, entityNameInErrorText, ctx);

		if (ctx.valueOrCategory() instanceof ValueContext) {
			checkDotValue(entityNameInErrorText, firstId, refEntitiesFullName);
		}

		// Prüfe Lfd-Angabe, falls eine gemacht wurde.
		SpezifierHModelContext spezifierAlteWelt = ctx.entitySpec().entityHModel() != null ?
				ctx.entitySpec().entityHModel().spezifierHModel() :
				null;
		String lStr = spezifierAlteWelt != null && spezifierAlteWelt.lfdNrSpezifier() != null ?
				spezifierAlteWelt.lfdNrSpezifier().lkonkret.getText() :
				null;
		if (lStr != null) {
			checkLfdNrSpecifier(lStr, entityNameInErrorText, firstId);
		}

		// Prüfe Zeilenangabe,  falls eine gemacht wurde.
		String zStr = spezifierAlteWelt != null && spezifierAlteWelt.zeileSpezifier() != null ?
				spezifierAlteWelt.zeileSpezifier().zkonkret.getText() :
				null;
		if (zStr != null) {
			checkZeileSpecifier(zStr, entityNameInErrorText, firstId);
		}

		// check access to category values
		if (categoryName != null) {
			checkAccessToCategoryValue(fullName, categoryName);
		}
		// kein Fehler entdeckt
	}

	private IEntityId checkFieldReferencedInConditionAndGetFirstId(RefEntities.FullName fullName,
			String entityNameInErrorText,
			FieldContext ctx) {

		RefEntities.SemanticIndexSpec semanticIndexSpec = null;
		if (ctx.semanticIndex() != null) {
			semanticIndexSpec = getSemanticIndexSpec(ctx.semanticIndex());
		}

		List<IEntityId> ids = refEntities.tryGetFor(fullName, semanticIndexSpec);
		if (ids == null || ids.isEmpty()) {
			if (ctx.semanticIndex() != null) {
				throw composeError(FehlertextErrorType.MVK_INDEX_FOR_ERROR_TEXT_INVALID, errorTextParameter);
			} else {
				throw composeError(FehlertextErrorType.INVALID_FIELD, entityNameInErrorText);
			}
		}
		return ids.getFirst();
	}

	/**
	 * @return either the full path of the entity specified in SemanticIndexContext or an unescaped version of the constant
	 */
	private RefEntities.SemanticIndexSpec getSemanticIndexSpec(SemanticIndexContext semanticIndex) {
		String semanticIndexSpec = semanticIndex.getText();
		if (semanticIndex.konst != null) {
			return RefEntities.SemanticIndexSpec.of(
					"\"" + CommonUtils.removeAndUnescapeQuotesAndRemoveCrFromCrLf(semanticIndexSpec) + "\"");
		}
		var entitySpecCtx = semanticIndex.id;
		var entityRef = getEntityRef(entitySpecCtx);
		return RefEntities.SemanticIndexSpec.of(entityRef.entity.getFullName());
	}

	private void checkDotValue(String fieldRef, IEntityId firstId, RefEntities.FullName fullName) {
		// .Wert / .value nur erlaubt, wenn das Feld ohne Stern referenziert wurde
		// wird zurzeit nur für A12-Modelle geprüft. s. EXT-10438

		if (!isHModel() && refEntities.isNotReferencedWithoutWildcard(fullName)) {
			throw composeError(FehlertextErrorType.PARAM_VALUE_WITH_STARREFERENCE, errorTextParameter, fieldRef);
		}
		// it is not allowed to access to a field value if 'no value validation' is set
		if (isNoValueValidation(firstId.getFullName())) {
			throw composeError(FehlertextErrorType.INVALID_NO_VALUE_VALIDATION, fieldRef);
		}
	}

	private void checkLfdNrSpecifier(String lStr, String fieldRef, IEntityId firstId) {
		//Bei allen Angaben ungleich 1 muss das Feld auf einem Mehrfachvordruck liegen.
		if (!"1".equals(lStr)) {
			long maxL = firstId.getMaxValues()[0];
			//ist das Feld auch auf einem Mehrfachvordruck
			if (maxL == 1) {
				throw composeError(FehlertextErrorType.INVALID_FIELD_L, errorTextParameter, maxL);
			} else {
				// es wurde eine konkrete laufende Nummer angegeben -> diese muss kleiner als die maximale lfd Nr sein.
				if (StringUtils.isNumeric(lStr) && Long.parseLong(lStr) > maxL) {
					throw composeError(FehlertextErrorType.INVALID_FIELD_L, fieldRef, maxL);
				}
			}
		}
	}

	private void checkZeileSpecifier(String zStr, String fieldRef, IEntityId firstId) {
		//Bei allen Angaben ungleich 1 muss das Feld mehrzeilig sein.
		if (!"1".equals(zStr)) {
			//ist das Feld mehrzeilig
			long maxZ = firstId.getMaxValues()[1];
			if (maxZ == 1) {
				throw composeError(FehlertextErrorType.INVALID_FIELD_Z, errorTextParameter, maxZ);
			} else {
				// es wurde eine konkrete Zeile angegeben -> diese muss kleiner als die maximalen Zeilenangabe sein.
				if (StringUtils.isNumeric(zStr) && Long.parseLong(zStr) > maxZ) {
					throw composeError(FehlertextErrorType.INVALID_FIELD_Z, fieldRef, maxZ);
				}
			}
		}
	}

	private String checkAndGetCategoryName(CategoryContext categoryCtx, String fieldRef) {
		NameWithoutDotContext categoryNameCtx = categoryCtx.categoryRef().categoryName;
		if (categoryNameCtx == null) {
			throw composeError(FehlertextErrorType.NO_CATEGORY, fieldRef);
		}
		return categoryNameCtx.getText();
	}

	@Override protected void checkIndexFieldRef(IndexFieldContext ctx) {
		// check existence of category also if no condition was passed to avoid errors in ErrorTextParameterCreator
		String categoryName = ctx.valueOrCategory() instanceof CategoryContext catCtxt ? checkAndGetCategoryName(catCtxt,
				ctx.id.getText()) : null;

		if (refEntities == null) {
			return;
		}

		Set<IField> indexFieldsOnErrorEntityPath = AnalyseService.getIndexFields(errorEntity, ec);

		String fieldNameRef = ctx.entitySpec().getText();

		List<IField> relevantIndexFields = indexFieldsOnErrorEntityPath.stream()
				.filter(f -> EntityUtils.getShortName(f.getFullName()).equals(fieldNameRef))
				.toList();

		if (relevantIndexFields.isEmpty()) {
			// the path of the referenced index field is not on the error entity path
			throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD, "$" + errorTextParameter + "$");
		}
		if (relevantIndexFields.size() > 1) {
			// more than 1 index field with short name 'fieldNameRef' on the path of the error field
			throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_AMBIGUOUS, "$" + errorTextParameter + "$", fieldNameRef);
		}

		IField refIndexFieldOnErrorEntityPath = relevantIndexFields.getFirst();

		IGroup indexedGroup = EntityUtils.getParentGroup(refIndexFieldOnErrorEntityPath, ec);
		if (indexedGroup.getIndexFieldNames().size() > 1) {
			throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_COMPOSED_INDEX, "$" + errorTextParameter + "$",
					fieldNameRef);
		}

		List<IEntityId> allRefIndexFieldsInParam = refEntities.getAllRefIndexFields().stream()
				.filter(f -> EntityUtils.getShortName(f.getFullName()).equals(fieldNameRef))
				.sorted((o1, o2) -> o1.getFullName().compareTo(o2.getFullName())) // sort to get deterministic ordering for tests
				.toList();
		checkIndexFieldRefRepeatability(refIndexFieldOnErrorEntityPath, allRefIndexFieldsInParam);

		if (ctx.valueOrCategory() != null) {
			if (ctx.valueOrCategory() instanceof CategoryContext) {
				// ->category reference
				checkIndexFieldRefCategory(allRefIndexFieldsInParam, categoryName);
			} else if (ctx.valueOrCategory() instanceof ValueContext) {
				// .value reference
				checkIndexFieldRefValue(allRefIndexFieldsInParam);
			}
		} else {
			// neither ->category reference nor .value reference are given -> makes no sense
			throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_PARAMETER, "$" + errorTextParameter + "$");
		}
		//add refIndexFieldOnErrorEntityPath to referencedEntities
		var entityRef = new EntityRef(fieldNameRef, refIndexFieldOnErrorEntityPath);
		this.referencedEntities.put(ctx.id, entityRef);

		allRefIndexFieldsInParam.forEach(e -> additionallyReferencedEntities.add(ec.get(e.getFullName())));
	}

	private void checkIndexFieldRefRepeatability(IField indexFieldOnErrorFieldPath, List<IEntityId> allRefIndexFieldsInParam) {
		if (indexFieldOnErrorFieldPath.getMax()[indexFieldOnErrorFieldPath.getMax().length - 2] == 1) {
			// The containing group is not repeatable
			checkIndexFieldRefRepeatability4nonRepeatableGroup(indexFieldOnErrorFieldPath, allRefIndexFieldsInParam);
		} else {
			checkIndexFieldRefRepeatability4repeatableGroup(indexFieldOnErrorFieldPath, allRefIndexFieldsInParam);
		}
		// no error found
	}

	private void checkIndexFieldRefRepeatability4nonRepeatableGroup(IField indexFieldOnErrorFieldPath,
			List<IEntityId> allRefIndexFieldsInParam) {
		for (IEntityId entityId : allRefIndexFieldsInParam) {
			if (entityId.getMaxValues()[entityId.getMaxValues().length - 2] != 1) {
				// neither all referenced index fields are non-repeatable nor are all repeatable and specify (parallel) iteration
				throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_REP, indexFieldOnErrorFieldPath.getFullName(),
						entityId.getFullName());
			}
			// all referenced index fields have to be of type Enum, have exactly one XML-Value and an S-Value
			if (!checkNonRepIndexFieldType(entityId)) {
				throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_NONREP_ENUM, entityId.getFullName());
			}
		}
	}

	private void checkIndexFieldRefRepeatability4repeatableGroup(IField indexFieldOnErrorFieldPath,
			List<IEntityId> allRefIndexFieldsInParam) {
		for (IEntityId entityId : allRefIndexFieldsInParam) {
			if (entityId.getMaxValues()[entityId.getMaxValues().length - 2] == 1) {
				// neither all referenced index fields are non-repeatable nor are all repeatable
				throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_REP, indexFieldOnErrorFieldPath.getFullName(),
						entityId.getFullName());
			}
			if (entityId.getIndices()[entityId.getIndices().length - 2] != IParseTreeEntity.ITERATION
					&& entityId.getIndices()[entityId.getIndices().length - 2] != IParseTreeEntity.PARALLEL_ITERATION) {
				// the referenced index field is repeatable but does not specify (parallel) iteration
				throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_REP_ITER, entityId.getFullName());
			}
		}
	}

	private boolean checkNonRepIndexFieldType(IEntityId entityId) {
		IFieldType fieldType = ((IField) ec.get(entityId.getFullName())).getType();
		if (fieldType instanceof IEnumerationType enumerationType) {
			return enumerationType.getXMLValues().size() == 1
					&& !StringUtils.isEmpty(enumerationType.getsValue());
		} else {
			return false;
		}
	}

	private void checkIndexFieldRefCategory(List<IEntityId> allRefIndexFieldsInParam, String categoryName) {
		List<String> comparisonXMLValues = null;
		List<String> comparisonCategoryValues = null;
		String comparisonEntityName = null;
		for (IEntityId entityId : allRefIndexFieldsInParam) {
			IFieldType fieldType = ((IField) ec.get(entityId.getFullName())).getType();
			if (!(fieldType instanceof IEnumerationType enumType)) {
				// not all referenced index fields are of type Enum
				throw composeError(FehlertextErrorType.FIELD_NO_ENUM, entityId.getFullName());
			}
			if (!enumType.getCategoryValues().containsKey(categoryName)) {
				// not all referenced index fields have the referenced category
				throw composeError(FehlertextErrorType.INVALID_CATEGORY, categoryName, entityId.getFullName());
			}
			if (comparisonXMLValues != null && !comparisonXMLValues.equals(enumType.getXMLValues())) {
				// not all referenced index fields have the same xml values and ordering thereof
				throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_ENUM_XML_VALUES, comparisonEntityName,
						entityId.getFullName());
			}
			if (comparisonCategoryValues != null && !comparisonCategoryValues.equals(
					enumType.getCategoryValues().get(categoryName))) {
				// not all referenced index fields have the same xml <-> category value mapping
				throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_ENUM_XML_TO_CAT, comparisonEntityName,
						entityId.getFullName());
			}
			comparisonXMLValues = enumType.getXMLValues();
			comparisonCategoryValues = enumType.getCategoryValues().get(categoryName);
			comparisonEntityName = entityId.getFullName();
		}
		// no error found
	}

	private void checkIndexFieldRefValue(List<IEntityId> allRefIndexFieldsInParam) {
		if (allRefIndexFieldsInParam.isEmpty()) {
			return;
		}
		String firstFieldName = allRefIndexFieldsInParam.getFirst().getFullName();
		IFieldType firstFieldType = ((IField) ec.get(firstFieldName)).getType();

		for (IEntityId otherEntityId : allRefIndexFieldsInParam) {
			IFieldType otherFieldType = ((IField) ec.get(otherEntityId.getFullName())).getType();

			if ((firstFieldType instanceof IEnumerationType) != (otherFieldType instanceof IEnumerationType)) { // currently this is already checked in the rule parser, cf. comment for INVALID_INDEX_FIELD_INCONSISTENT_TYPES in test cases
				// neither all referenced index fields are of type enum nor none
				throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_INCONSISTENT_TYPES, firstFieldName,
						otherEntityId.getFullName());
			}

			if ((firstFieldType instanceof IEnumerationType firstFieldEnumType)) { // i.e. otherFieldType instanceof IEnumerationType
				checkEnumFieldTypeConsistency(firstFieldName, otherEntityId.getFullName(), firstFieldEnumType,
						(IEnumerationType) otherFieldType);
			}
		}
		// no error found
	}

	private void checkEnumFieldTypeConsistency(String firstFieldName, String otherFieldName, IEnumerationType firstFieldEnumType,
			IEnumerationType otherEnumType) {
		if (!firstFieldEnumType.getXMLValues().equals(otherEnumType.getXMLValues())) {
			// not all referenced index fields have the same xml values and ordering thereof
			throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_ENUM_XML_VALUES, firstFieldName, otherFieldName);
		}
		if (!firstFieldEnumType.getSupportedDisplayLocales().equals(otherEnumType.getSupportedDisplayLocales())) {
			// not all referenced index fields have the same supported locales for display values
			throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_ENUM_XML_TO_DISPLAY, firstFieldName, otherFieldName);
		}
		for (Locale loc : firstFieldEnumType.getSupportedDisplayLocales()) {
			List<EnumerationValue> firstFieldEnumValues = firstFieldEnumType.getDisplayValues(loc);
			List<EnumerationValue> otherFieldEnumValues = otherEnumType.getDisplayValues(loc);
			if ((firstFieldEnumValues == null && otherFieldEnumValues != null)
					|| (firstFieldEnumValues != null && !firstFieldEnumValues.equals(otherFieldEnumValues))) {
				// not all referenced index fields have the same xml <-> display value mapping
				throw composeError(FehlertextErrorType.INVALID_INDEX_FIELD_ENUM_XML_TO_DISPLAY, firstFieldName, otherFieldName);
			}
		}
	}

	/**
	 * @return a string representation of the semantic index, how it is typically displayed in the error text
	 */
	String buildSemanticIndexStringForErrorMessage(String nameUsedInErrorText, String semanticIndex) {
		return String.format("%s %s %s", nameUsedInErrorText,
				this.multiLanguageHandler.getTerminalName(BaseErrorTextParameterLexer.FOR),
				semanticIndex);
	}

	/**
	 * Checks for the Vordruckzeile-parameter '&lt;field&gt;.Vordruckzeile' if
	 * <ul>
	 * <li>the field is referenced in a correct way, i.e. with the short name for H-Models and with the full name for A12-Models</li>
	 * <li>the field exists</li>
	 * </ul>
	 * It is <b>not</b> checked here if the field specifies a Vordruckzeile but it is checked
	 * during code generation.
	 */
	@Override protected void checkVordruckzeile(VordruckzeileContext ctx) {
		checkValidFieldName(ctx.id);
	}

	private void checkValidFieldName(EntitySpecContext id) {
		String fName = id.getText();
		EntitySpecContext entitySpecCtx = ParserUtils.getEntitySpecContext(id);

		if (isHModel() && fName.contains("/")) {
			throw composeError(FehlertextErrorType.INVALID_PATH_SPEC_IN_PARAMETER, "$" + errorTextParameter + "$");
		}

		if (!isHModel()) {
			var entityRef = getEntityRef(entitySpecCtx);
			fName = entityRef.entity.getFullName();
		}

		if (!(ec.get(fName) instanceof IField)) {
			throw composeError(FehlertextErrorType.INVALID_FIELD_IN_PARAMETER, "$" + errorTextParameter + "$");
		}
	}

	/**
	 * Checks the error text parameter $#groupPath$.
	 */
	@Override protected void checkLevelParam(GroupIndexContext ctx) {

		String pathToDisplayInErrorMessage = ctx.entitySpec().getText();
		String absolutePath;
		if (ctx.entitySpec().regelKontextEntity() != null) {
			if (StringUtils.countMatches(rulePath, "/") == 1) {
				throw composeError(FehlertextErrorType.RULEGROUP_IMMEDIATELY_BELOW_ROOT);
			}
			absolutePath = rulePath;
			pathToDisplayInErrorMessage = rulePath;
		} else {
			var entityRef = getEntityRef(ctx.entitySpec());
			absolutePath = entityRef.entity.getFullName();
		}

		/*
		 * gültiger Kontextpfad?
		 */
		if (refEntities == null) {
			return;
		}

		IEntity entity = EntityUtils.getEntityInGroup(ec, absolutePath, rulePath);
		if (entity == null || !entity.isGroup()) {
			throw composeError(FehlertextErrorType.INVALID_GROUP, errorTextParameter, pathToDisplayInErrorMessage);
		}
		IGroup group = (IGroup) entity;
		boolean isIndexGroup = (group.getIndexFieldNames() != null && !group.getIndexFieldNames().isEmpty());

		var fullName = new RefEntities.FullName(absolutePath);

		RefEntities.SemanticIndexSpec semanticIndex = ctx.semanticIndex() != null ?
				getSemanticIndexSpec(ctx.semanticIndex()) :
				null;

		if (!refEntities.hasValidGroupReference(fullName, semanticIndex, isIndexGroup)) {
			throw composeError(FehlertextErrorType.INVALID_GROUP, errorTextParameter, pathToDisplayInErrorMessage);
		}

		//bei #Kontext: wird der Kontext in der Bedingung ohne Stern referenziert?
		if (refEntities.isNotReferencedWithoutWildcard(fullName)) {
			throw composeError(FehlertextErrorType.ITER_PARAM_WITH_STARREFERENCE, errorTextParameter, pathToDisplayInErrorMessage);
		}

		if (!checkSemanticIndexAndParallelIteration(absolutePath, semanticIndex)) {
			if (semanticIndex != null) {
				throw composeError(FehlertextErrorType.INVALID_USE_OF_SEMANTIC_INDEX, errorTextParameter);
			}
			throw composeError(FehlertextErrorType.INVALID_USE_OF_ITERATION_PARAM, errorTextParameter);
		}
	}

	/**
	 * precondition: {@code entitySpecContext} has already been parsed
	 *
	 */
	private EntityRef getEntityRef(EntitySpecContext entitySpecContext) {
		if (referencedEntities.containsKey(entitySpecContext)) {
			return referencedEntities.get(entitySpecContext);
		} else { // should not happen - referenced entities are put to the map while they are parsed
			throw new IllegalArgumentException(
					String.format("Referenced entity for EntitySpecContext %s not found.", entitySpecContext.getText()));
		}
	}

	private boolean checkSemanticIndexAndParallelIteration(String grpParamFullname,
			RefEntities.SemanticIndexSpec semanticIndexSpec) {
		if (semanticIndexSpec == null && !isParallelIteration(grpParamFullname)) {
			return true;
		}

		var semanticIndexParam = semanticIndexSpec == null ? null : semanticIndexSpec.specification();
		RuleErrorTextIterationParamVisitor visitor = new RuleErrorTextIterationParamVisitor(grpParamFullname, semanticIndexParam,
				rulePath);
		condition.traverse(visitor);
		return visitor.isParameterAllowed();
	}

	/**
	 * Überprüft eine Referenzjahr Angabe: nur erlaubt in Modellen mit Referenzjahr.
	 * Entweder es wird nur ein Referenzjahr angegeben oder eine Berechnung (Referenzjahr-n oder Referenzjahr+n) -> dies wird
	 * geprüft durch die Grammatik.
	 */
	@Override protected void checkReferenzjahr() {
		if (!hasBaseYear) {
			throw composeError(FehlertextErrorType.NO_BASE_YEAR);
		}
	}

	/**
	 * Überprüft die Angabe von #Zeile:
	 * <ul>
	 * <li>Darf nur in H-Modellen angegeben werden.</li>
	 * <li>Darf nur bei Iteration über die Zeile angegeben werden.</li>
	 * <li>Kann von einer Addition oder Subtraktion gefolgt sein. (Prüfung durch Grammatik)</li>
	 * </ul>
	 */
	@Override protected void checkZeile() {
		if (!isHModel()) {
			throw composeError(FehlertextErrorType.PARAM_INVALID_IN_NEW_WORLD, errorTextParameter);
		}
		if (conditionChecks && !isZIteration) {
			throw composeError(FehlertextErrorType.NO_Z_ITERATION, errorTextParameter);
		}
	}

	@Override protected void checkRelativeUp(RelativeUpContext context) {
		var checker = new CheckRelativeUpImpl(this.rule, this::composeError, this.ec, this.referencedEntities);
		checker.check(context);
	}

	/**
	 * checks only that the path does not climb above the root group
	 */
	@Override protected void checkRelUp(RelUpContext ctx) {
		String ruleName = this.rule.getFullName();
		int ruleGroupLevel = EntityUtils.getLevel(ruleName) - 1;
		int totalNumUpInRelativeUp = ctx.oneUp().size() + 1; // one '..' is hardcoded in relativeUp outside of relUp
		if (ruleGroupLevel - totalNumUpInRelativeUp < CheckRelUpImpl.MIN_LEVEL_FOR_UPWARD_PATHS) {
			String nameInErrorText = ctx.getText() + EntityUtils.PATH_UP;//the .. not yet parsed in grammar definition of relative up
			throw composeError(FehlertextErrorType.ERROR_TEXT_PARAMETER_TOO_MANY_PARENT_GROUPS, nameInErrorText);
		}
	}

	/**
	 * Überprüft die Angabe von #Usb:
	 * <ul>
	 * <li>Darf nur in H-Modellen angegeben werden.</li>
	 * <li>Darf nur bei Iteration über den USB angegeben werden.</li>
	 * <li>Darf nicht von einer Addition, Subtraktion oder anderem gefolgt sein. (über Grammatik
	 * sichergestellt)</li>
	 * </ul>
	 */
	@Override protected void checkUSB() {
		if (!isHModel()) {
			throw composeError(FehlertextErrorType.PARAM_INVALID_IN_NEW_WORLD, errorTextParameter);
		}
		if (conditionChecks && !isUIteration) {
			throw composeError(FehlertextErrorType.NO_U_ITERATION, errorTextParameter);
		}
	}

	/**
	 * Überprüft ob eine korrekte FehlerEnumeration für #LfdNr verwendet wurde.
	 * <p>
	 * Anmerkung: Die Verwendung kann nicht auf Iteration über die LfdNr eingeschränkt werden, da dies Probleme bei Regeln in
	 * abgeleiteten Modellen verursacht, wenn die Wiederholung des Vordrucks dort auf 1 eingeschränkt wurde.
	 */
	@Override protected void checkFehlerEnum(LfdNrContext ctx) {
		if (!conditionChecks) { // if the condition is erroneous this error is reported at another place -> no additional checks
			return;
		}
		if (AnalyseService.isParallelIteration(condition, true)) {
			String rootGroupFullName = EntityUtils.getPathUpToLevel(0, rulePath);
			throw composeError(FehlertextErrorType.INVALID_HASH_LFDNR_FOR_PARALLEL_ITERATION, errorTextParameter,
					"#" + rootGroupFullName);
		}
	}

	private boolean isParallelIteration(String path) {
		return AnalyseService.getParallelIterationGroups(condition).contains(path);
	}

	/**
	 * @return <code>true</code> if the field is a string with the property 'no value validation',
	 * 		<code>false</code> otherwise.
	 */
	private boolean isNoValueValidation(String fieldName) {
		IField field = (IField) ec.get(fieldName);
		if (!FieldUtils.isString(field)) {
			return false;
		}
		IStringType type = (IStringType) field.getType();
		return type.isNoValueValidation();
	}

	/**
	 * Check the parameter with the form $FIELD_NAME-&gt;CATEGORY_NAME$:
	 * <ul>
	 * <li>FIELD_NAME must be a field of type enum.</li>
	 * <li>CATEGORY_NAME must be a valid category name of the enum field.</li>
	 * <li></li>
	 * </ul>
	 */
	private void checkAccessToCategoryValue(String fieldName, String categoryName) {
		IField field = (IField) ec.get(fieldName);
		if (!FieldUtils.isEnum(field)) {
			throw composeError(FehlertextErrorType.FIELD_NO_ENUM, fieldName);
		}
		IEnumerationType type = (IEnumerationType) field.getType();
		if (!type.getCategoryValues().containsKey(categoryName)) {
			throw composeError(FehlertextErrorType.INVALID_CATEGORY, categoryName, fieldName);
		}
	}

	@Override protected void checkSemanticIndex(SemanticIndexContext ctx) {
		SemanticIndexContext nestedSemanticIndex = ctx.semanticIndex();
		if (nestedSemanticIndex != null) {
			String semanticIndexSpec = buildSemanticIndexStringForErrorMessage(ctx.entitySpec().getText(),
					nestedSemanticIndex.getText());
			throw composeError(FehlertextErrorType.ILLEGAL_SEMANTIC_INDEX, semanticIndexSpec);
		}
	}

	@Override protected void checkIdName(IdNameContext ctx) {
		var checker = new CheckIdNameImpl(rule, this::composeError, ec, entityResolver, errorTextParameter,
				referencedEntities);
		checker.check(ctx);
	}

	private MVKParserError composeError(FehlertextErrorType errorType, Object... args) {
		String msg =
				errorHandler.getErrorMessage(errorType, args);
		return new MVKParserError(rule, msg);
	}

	public FehlertextErrorLookup getErrorLookup() {
		return errorHandler;
	}

	public ErrortextParameterMultiLanguageHandler getMultiLanguageHandler() {
		return multiLanguageHandler;
	}

	private boolean isGroupFromHModelTest(ParserRuleContext parserContext) {
		EntitySpecContext entitySpecContext = ParserUtils.getEntitySpecContext(parserContext.getParent());
		return referencedEntities.get(entitySpecContext).getEntity() instanceof IGroup;
	}

	/**
	 * This returns the referenced entities found in the error text parameter.
	 * <p>
	 * ATTENTION: This should only be called after the error text parameter was parsed completely.
	 *
	 * @return the {@link IEntity}s referenced in the error text parameter.
	 */
	public Set<IEntity> getReferencedEntities() {
		Set<IEntity> result = new HashSet<>(additionallyReferencedEntities);
		result.addAll(referencedEntities.values().stream()
				.map(EntityRef::getEntity)
				.collect(Collectors.toSet()));
		return result;
	}

	/**
	 * @return a map of the respecive error text parameter context to the parsed entityRef,
	 * 		does not contain indirectly referenced entities in contrast to {@link #getReferencedEntities}
	 */
	public Map<EntitySpecContext, EntityRef> getReferencedEntitiesMap() {
		return referencedEntities;
	}

	@Override
	protected void addRegelKontextEntityToReferencedEntities(RegelKontextEntityContext ctx) {
		EntitySpecContext entitySpecContext = ParserUtils.getEntitySpecContext(ctx);
		IEntity entity = ec.get(rulePath);
		if (entity == null) { // should not happen - this would be an integration error
			throw new IllegalArgumentException(String.format("Rule group %s not found in entity container", rulePath));
		}
		EntityRef entityRef = new EntityRef(ctx.getText(), entity);

		referencedEntities.put(entitySpecContext, entityRef);
	}
}
