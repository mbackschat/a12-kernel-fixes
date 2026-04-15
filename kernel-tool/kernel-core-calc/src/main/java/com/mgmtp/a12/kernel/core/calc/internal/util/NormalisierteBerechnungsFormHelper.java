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

package com.mgmtp.a12.kernel.core.calc.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.calc.internal.util.compOp.CalcOperationRoundDownWithCompOp;
import com.mgmtp.a12.kernel.core.calc.internal.util.compOp.CalcOperationWithCompOp;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckVergleichsBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityListenUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserError;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorHandler;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.CompositeConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parser.internal.util.MissingEntityEC;
import com.mgmtp.a12.kernel.core.parser.internal.util.OperationResultType;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IApplicationCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IGroupOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.FieldIdOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.GroupOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FindAnyAst;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.INumberType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService.RoundingMode;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.EnumBasedErrorLookup;

/**
 * Die Klasse <code>NormalisierteBerechnungsFormHelper</code> stellt Methoden zur Prüfung der
 * "normalisierten Form" für (serverseitige) Berechnungen und Extraktion der Informationen aus der
 * "normalisierten Form" zur Verfügung.
 * <p>
 * Die "normalisierte Form" wird im AHB im Kapitel zu den serverseitigen Berechnungsinformationen
 * beschrieben.
 */
public class NormalisierteBerechnungsFormHelper {
	private final EnumBasedErrorLookup<CalculationErrorType> errorLookup;
	private final IRule rule;
	private final IProblemReporter pm;
	private final IEC ec;
	private final boolean isHModel;
	private final IModelSpecificSettings modelSpecificSettings;
	private final IProjectSpecificSettings pss;
	private final IServiceConfiguration.ParserStrictness strictness;
	private boolean wasGeneralReferenceErrorReported = false;
	private final Set<EntityIdImpl> reportedReferenceErrorEntities = new HashSet<>();

	/**
	 * @param errorLookup
	 * 		the object used to lookup the error messages
	 * @param pm
	 * 		the problem reporter, the error messages should be given to
	 * @param rule
	 * 		the rule, the calculation is derived from
	 * @param ec
	 * 		the entity container used to determine max iterations for fields.
	 * @param isHModel
	 * 		determines, whether special treatment of iterations is in use.
	 * @param modelSpecificSettings
	 * @param pss
	 * 		used for the specification of the language for language dependent error messages as well as the language of condition
	 * 		and error texts of rules.
	 * @param strictness
	 * 		specifies the strictness of the checks (also for the parse tree).
	 */
	public NormalisierteBerechnungsFormHelper(EnumBasedErrorLookup<CalculationErrorType> errorLookup, IProblemReporter pm,
			IRule rule, IEC ec, boolean isHModel, IModelSpecificSettings modelSpecificSettings, IProjectSpecificSettings pss,
			IServiceConfiguration.ParserStrictness strictness) {
		this.errorLookup = errorLookup;
		this.rule = rule;
		if (pm == null) {
			this.pm = p -> {
			};
		} else {
			this.pm = pm;
		}
		if (strictness == IServiceConfiguration.ParserStrictness.LENIENT) {
			this.ec = MissingEntityEC.wrapEC(ec);
		} else {
			this.ec = ec;
		}
		this.isHModel = isHModel;
		this.modelSpecificSettings = modelSpecificSettings;
		this.pss = pss;
		this.strictness = strictness;
	}

	/**
	 * Prüft, ob die Bedingung der "normalisierten Form" entspricht und liefert eine Fehlermeldung
	 * zurück.
	 *
	 * @param bedingung
	 * 		die zu prüfende Bedingung. Dabei dürfen die Kontexte nicht expandiert worden sein.
	 * @param errorField
	 * 		das Fehlerfeld.
	 * @return <code>true</code>, falls die Bedingung der normaliserten Form entspricht, sonst
	 * 		<code>false</code>.
	 */
	public boolean checkNormalisierteForm(ICondition bedingung, IField errorField) {
		// 1. Feld-Gate-Bedingung: FeldAngegeben(bF) Und ...
		if (!(bedingung instanceof ICompositeCondition zgb)) {
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_FIELD_GATE);
			return false;
		}
		if (zgb.getLogOp() != LogOp.And) {
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_FIELD_GATE);
			return false;
		}
		List<ICondition> bedingungen = zgb.getConditionList();
		if (!checkFeldGate(bedingungen.get(0), errorField.getFullName())) {
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_FIELD_GATE);
			return false;
		}

		// 2. AnzahlAngegebeneUSB, WiederholungNichtEindeutig und Applikationsbedingungen dürfen nicht vorkommen
		if (AnalyseService.beinhaltetAnzahlAngegebenerUsb(bedingung)) {
			ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(
					modelSpecificSettings.getLanguageUsedInConditionText(),
					pss);
			String tokenName = mlh.getTerminalName(GrammarOnly_ConditionParser.ANZAHL_ANGEGEBENE_USB);
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_LANGUAGE_FEATURE, tokenName);
			return false;
		} else if (FindAnyAst.findeInBedingung(bedingung, IApplicationCondition.class::isInstance, true).isPresent()) {
			ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(
					modelSpecificSettings.getLanguageUsedInConditionText(),
					pss);
			String tokenName = mlh.getTerminalName(GrammarOnly_ConditionParser.APPLIKATIONS_BEDINGUNG);
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_LANGUAGE_FEATURE, tokenName);
			return false;
		} else if (AnalyseService.beinhaltetWiederholungNichtEindeutig(bedingung)) {
			ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(
					modelSpecificSettings.getLanguageUsedInConditionText(),
					pss);
			String tokenName = mlh.getTerminalName(GrammarOnly_ConditionParser.WIEDERHOLUNG_NICHT_EINDEUTIG);
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_LANGUAGE_FEATURE, tokenName);
			return false;
		}

		// 3. Teilbedingungen extrahieren und prüfen
		// a. komplexe Bedingung => letzte Teilbedingung ist ZusGesetzteBedingung der Teilbedingungen mit Oder
		boolean isWiederholbar = EntityUtils.isRepeatableOnOneLevel(errorField, isHModel);
		if (isComplexCalculation(bedingungen)) {
			// Common precondition
			List<ICondition> commonPreconditionParts = bedingungen.subList(1, bedingungen.size() - 1);
			ICompositeCondition lastCondition = (ICompositeCondition) bedingungen.get(bedingungen.size() - 1);
			List<ICondition> teilBerechnungen = lastCondition.getConditionList();
			return Java8Utils.allMatchEvaluateAll(teilBerechnungen.stream(),
					b -> checkTeilBerechnung(b, errorField, isWiederholbar, false, commonPreconditionParts));
		} else if (bedingungen.size() > 2) { // b. mehr als eine Teilbedingung => einfache Form
			CompositeConditionImpl teilBer = new CompositeConditionImpl(zgb, bedingungen.subList(1, bedingungen.size()));
			return checkTeilBerechnung(teilBer, errorField, isWiederholbar, true, null);
		} else { // c. 2 Teilbedingungen, aber 2. nicht ZusGesetzteBedingung mit Oder
			return checkTeilBerechnung(bedingungen.get(1), errorField, isWiederholbar, true, null);
		}
	}

	/**
	 * Prüft eine einzelne Teilberechnung ([&lt;vorBedingung&gt; Und]
	 * [berechnetesFeld]!=&lt;berechnungsFunktion&gt;).
	 *
	 * @param b
	 * 		die zu prüfende Teilberechnung.
	 * @param errorField
	 * 		das Fehlerfeld.
	 * @param isIterable
	 * 		gibt an, ob das berechnete Feld wiederholbar ist.
	 * @param leereVorBedingungErlaubt
	 * 		gibt an, ob die Vorbedingung entfallen kann.
	 * @return <code>true</code>, falls die Teilberechnung valide ist, sonst <code>false</code>.
	 */
	private boolean checkTeilBerechnung(ICondition b, IField errorField, boolean isIterable, boolean leereVorBedingungErlaubt,
			List<ICondition> commonPreconditionParts) {
		// Leere Vorbedingung?
		if (!(b instanceof ICompositeCondition zgb)) {
			if (!leereVorBedingungErlaubt) {
				reportProblem(CalculationErrorType.MVK_ERROR_EMPTY_PRECONDITION);
				return false;
			} else {
				if (isHModel && isIterable) { // for A12 DVs, the iteration check precondition is added automatically/implicity
					reportProblem(CalculationErrorType.MVK_ERROR_ITERATION_UNCHECKED);
					return false;
				}
				return checkBerechnungsVergleich(b, errorField);
			}
		}
		// TeilBerechnung muss immer mit Und verknüpft sein.
		if (zgb.getLogOp() != LogOp.And) {
			reportProblem(CalculationErrorType.MVK_ERROR_COMPARISON_WITH_OR);
			return false;
		}
		List<ICondition> bedingungen = zgb.getConditionList();
		boolean success = checkBerechnungsVergleich(bedingungen.get(bedingungen.size() - 1), errorField);
		List<ICondition> preConditionParts = new ArrayList<>(bedingungen.subList(0, bedingungen.size() - 1));
		if (commonPreconditionParts != null) {
			preConditionParts.addAll(commonPreconditionParts);
		}
		return checkVorbedingungen(preConditionParts, errorField, isIterable) && success;
	}

	private Optional<Pair<ICondition, IOperation>> extractTeilBerechnung(ICondition b, RoundingMode roundingMode,
			IField calcField) {
		// Leere Vorbedingung?
		if (!(b instanceof ICompositeCondition zgb)) {
			return extractBerechnungsFeldOp(b, roundingMode, calcField).map(bv -> Pair.of(null, bv));
		}
		List<ICondition> bedingungen = zgb.getConditionList();
		ICondition vorBedingung;
		if (bedingungen.size() > 2) {
			vorBedingung = new CompositeConditionImpl(zgb, bedingungen.subList(0, bedingungen.size() - 1), false);
		} else {
			vorBedingung = bedingungen.get(0);
		}
		Optional<IOperation> berechnungsFunktion = extractBerechnungsFeldOp(bedingungen.get(bedingungen.size() - 1), roundingMode,
				calcField);
		return berechnungsFunktion.map(bf -> Pair.of(vorBedingung, bf));
	}

	private boolean checkBerechnungsVergleich(ICondition b, IField errorField) {
		if (!(b instanceof IComparisonCondition vb)) {
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_COMPARISON);
			return false;
		}
		// Nur Ungleichoperatoren erlaubt
		switch (vb.getCmpOp()) {
			case NE:
			case NE1:
			case NE2:
			case NE5:
			case NE10:
				break;
			default:
				reportProblem(CalculationErrorType.MVK_ERROR_INVALID_COMPARISON);
				return false;
		}
		// [berechnetesFeld] ...
		if (!(vb.getOperation1() instanceof IFieldIdOperation berFeldWert)) {
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_COMPARISON);
			return false;
		}
		// Semantic indexes and categories are checked in CalculationService#testRuleStructure
		if (berFeldWert.getOperatorName() != FieldIdOperationName.FieldValue ||
				!checkErrorFieldName(berFeldWert.getField().getFullName(), errorField.getFullName())) {
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_COMPARISON);
			return false;
		}

		return checkBerechnungsFunktion(vb.getOperation2(), errorField);
	}

	private Optional<IOperation> extractBerechnungsFeldOp(ICondition b, RoundingMode roundingMode, IField calcField) {
		if (!(b instanceof IComparisonCondition vb)) {
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_COMPARISON);
			return Optional.empty();
		}
		if (vb.getCmpOp() == ComparisonOperation.NE && roundingMode != RoundingMode.EXACT) {
			reportProblem(CalculationErrorType.MVK_ERROR_WRONG_ROUNDING_MODE);
			return Optional.empty();
		}
		/*
		 * Sonderfall Toleranzvergleich und exakte Berechnung: es wird abgerundet auf so viele
		 * Nachkommastellen, wie das berechnete Feld maximal haben darf
		 */
		if (isToleranzVergleich(vb.getCmpOp()) && roundingMode == RoundingMode.EXACT) {
			int fractionalDigits = ((INumberType) calcField.getType()).getMaxFractionalDigits();
			return Optional.of(
					new CalcOperationRoundDownWithCompOp(vb.getOperation2(),
							fractionalDigits, vb.getCmpOp()));
		}
		return Optional.of(new CalcOperationWithCompOp(vb.getOperation2(), vb.getCmpOp()));
	}

	private boolean isToleranzVergleich(ComparisonOperation vglOp) {
		return vglOp == ComparisonOperation.NE1 || vglOp == ComparisonOperation.NE2
				|| vglOp == ComparisonOperation.NE5 || vglOp == ComparisonOperation.NE10;
	}

	private boolean checkBerechnungsFunktion(IOperation feldOp, IField errorField) {
		// Keine Referenzen auf das berechnete (=Fehler-) Feld
		FieldReferenceResult errorResult = feldReferenziertOhneKontextAngegeben(feldOp, errorField);
		if (errorResult.isErroneous) {
			reportReferenceError(errorResult);
			return false;
		}

		if (strictness == IServiceConfiguration.ParserStrictness.STRICT && FieldUtils.isEnum(errorField)) {
			IEnumerationType fieldType = (IEnumerationType) errorField.getType();
			if (!isEnumValueCompatible(feldOp, fieldType)) {
				reportProblem(CalculationErrorType.MVK_ERROR_INVALID_ENUM_CALCULATION);
				return false;
			}
		}
		return true;
	}

	/**
	 * Tests that the operation is
	 * <ul>
	 * <li>a string constant contained in the xml values of the calculated field.</li>
	 * <li>a field operation or the language construct <code>FirstFilledValue</code> with the following allowed parameter(s):</li>
	 * <ul>
	 * <li>enumerations containing only valid enumeration values and/or</li>
	 * <li>categories, where all category values are valid.</li>
	 * </ul>
	 * </ul>
	 * <b>Note:</b> The restrictions for enumerations are stricter than for other field types, because if an invalid enumeration
	 * value would be calculated, the display value could not be determined.
	 *
	 * @param op
	 * 		the operation to test.
	 * @param errorFieldType
	 * 		the field type of the calculated field.
	 * @return <code>true</code> if the operation is valid, else <code>false</code>.
	 */
	private boolean isEnumValueCompatible(IOperation op, IEnumerationType errorFieldType) {
		Set<String> xmlValues = FieldUtils.getDistinctEnumValues(errorFieldType);
		if (op instanceof IConstantOperation constOp) {
			String value = constOp.getValue();
			return xmlValues.contains(value);
		}
		if ((op instanceof IFieldIdOperation fieldIdOp)) {
			if (fieldIdOp.getOperatorName() != FieldIdOperationName.FieldValue) {
				return false;
			}

			return isEntityIdCompatibleWithEnumValues(fieldIdOp.getField(), errorFieldType, xmlValues);
		}
		if ((op instanceof IEntityListOperation entityListOp)) {
			if (entityListOp.getOperatorName() != EntityListOperationName.FirstFilledValue) {
				return false;
			}
			return entityListOp.getEntityList().getEntityIdList().stream()
					.allMatch(entityId -> isEntityIdCompatibleWithEnumValues(entityId, errorFieldType, xmlValues));
		}
		return false;
	}

	private boolean isEntityIdCompatibleWithEnumValues(IEntityId fieldId, IEnumerationType errorFieldType, Set<String> xmlValues) {
		String category = fieldId.getCategoryName();
		IField field = (IField) ec.get(fieldId.getFullName());
		if (!FieldUtils.isEnum(field)) {
			return false;
		}

		IEnumerationType enumType = (IEnumerationType) field.getType();
		Set<String> values;
		if (StringUtils.isEmpty(category)) {
			values = FieldUtils.getDistinctEnumValues(enumType);
		} else {
			values = FieldUtils.getCategoryValues(enumType, category);
		}
		if (!xmlValues.containsAll(values)) {
			return false;
		}
		if (StringUtils.isEmpty(category)) {
			return checkDisplayValues(enumType, errorFieldType);
		}
		return true;
	}

	/**
	 * For comparisons of field operations with enumerations, this check is already done by the parser, see
	 * {@link CheckVergleichsBedingungImpl#checkEnumValuesWithoutCategories(OperationResultType,
	 * OperationResultType, GrammarOnly_ConditionParser.OperationContext, GrammarOnly_ConditionParser.OperationContext)}, but for
	 * comparisions with <code>FirstFilledValue</code> we do this check only for calculations at the moment.
	 */
	private boolean checkDisplayValues(IEnumerationType operandType, IEnumerationType errorFieldType) {
		if (FieldUtils.isEnumWithDisplayValues(operandType) != FieldUtils.isEnumWithDisplayValues(errorFieldType)) {
			return false;
		}
		if (!FieldUtils.isEnumWithDisplayValues(errorFieldType)) {
			return true;
		}
		Optional<String> errMsgOpt = CheckEntityListenUtils.checkDisplayValues(operandType, errorFieldType,
				new ParserErrorHandler(errorLookup.getLocale()));
		return errMsgOpt.isEmpty();
	}

	private boolean checkVorbedingungen(List<ICondition> teilBedingungen, IField errorField, boolean isIterable) {

		// Keine Referenz auf das berechnete (=Fehler-) Feld
		FieldReferenceResult errorResult = teilBedingungen.stream()
				.map(b -> feldReferenziertOhneKontextAngegeben(b, errorField))
				.reduce((a, b) -> a.combine(b))
				.orElse(new FieldReferenceResult(false, Collections.emptyList()));
		if (errorResult.isErroneous) {
			reportReferenceError(errorResult);
			return false;
		}
		// Es muss bei wiederholbarem Fehlerfeld sichergestellt sein, dass mindestens ein Operandenfeld der Wiederholung angegeben ist.
		// only relevant for H-DVs now, as for A12 DVs, the iteration check precondition is added automatically/implicity
		if (isHModel && isIterable && teilBedingungen.stream().noneMatch(b -> iterationChecked(b, errorField, isHModel))) {
			reportProblem(CalculationErrorType.MVK_ERROR_ITERATION_UNCHECKED);
			return false;
		}
		// Applikationsbedingung und AnzahlAngegebeneUsb wird zentral in #checkNormalisierteForm geprüft.
		return true;
	}

	private void reportReferenceError(FieldReferenceResult errorResults) {
		List<EntityIdImpl> entities = errorResults.erroneousEntities().stream()
				.filter(EntityIdImpl.class::isInstance)
				.map(e -> (EntityIdImpl) e)
				.toList();
		assert entities.size() == errorResults.erroneousEntities().size() : "no other impls than EntityIdImpl are expected";
		if (entities.isEmpty()) {
			if (!wasGeneralReferenceErrorReported) {
				reportProblem(CalculationErrorType.MVK_ERROR_REFERENCE_TO_CALCULATED_FIELD);
				wasGeneralReferenceErrorReported = true;
			}
		} else {
			entities.forEach(e -> reportProblem(e, CalculationErrorType.MVK_ERROR_REFERENCE_TO_CALCULATED_FIELD));
		}
	}

	private static class IterationChecker extends ASTVisitor {
		private final Predicate<IEntityId> checker;
		private boolean result = false;
		private final IField errorField;
		private final IEC ec;

		public IterationChecker(Predicate<IEntityId> checker, IField errorField, IEC ec) {
			this.checker = checker;
			this.errorField = errorField;
			this.ec = ec;
		}

		@Override
		public boolean visit(ICompositeCondition p) {
			if (result) {
				return false;
			}
			if (p.getLogOp() == LogOp.Or) {
				result = p.getConditionList().stream().allMatch(b -> isIterationChecked(b, checker, errorField, ec));
				return false;
			}
			return true;
		}

		@Override
		public boolean visit(IEntityListCondition elb) {
			if (!result) {
				checkConditionName(elb);
			}
			return false;
		}

		protected void checkConditionName(IEntityListCondition elb) {
			switch (elb.getConditionName()) {
				case AtLeastOneGroupFilled, AtLeastOneFieldFilled, MoreThanOneFieldFilled ->
						result = elb.getEntityList().getEntityIdList().stream().allMatch(checker);
				case AllGroupsFilled, AllFieldsFilled -> result = elb.getEntityList().getEntityIdList().stream().anyMatch(checker);
				default -> {
				}
			}
		}

		@Override
		public boolean visit(IRootGroupCondition rgc) {
			if (!result && rgc.getConditionName() == RootGroupConditionName.MindestensEinVordruckAngegeben) {
				result = checksIteration(rgc);
			}
			return false;
		}

		@Override
		public boolean visitGeneral(IParseTreeEntity e) {
			// stop criterion
			return !result;
		}

		public boolean getResult() {
			return result;
		}

		public static boolean isIterationChecked(ICondition bedingung, Predicate<IEntityId> checker, IField errorField, IEC ec) {
			IterationChecker itChecker = new IterationChecker(checker, errorField, ec);
			bedingung.traverse(itChecker);
			return itChecker.getResult();
		}

		private boolean checksIteration(IRootGroupCondition rgc) {
			// For the new world, we can use the entity id to check the iteration analogously to groups
			if (rgc.getEntityId() != null) {
				return iterationCheckedA12Model(errorField, rgc.getEntityId());
			}
			// For H models we must go the stony path alone
			int iterationLevels = EntityUtils.getMaxIterationLvl(errorField);
			if (iterationLevels > 0) {
				return false;
			}
			if (iterationLevels == 0 && !isIteration(rgc.getRootGroupIndex())) {
				return false;
			}
			return !EntityUtils.hasMaxUsb(errorField) || isIteration(rgc.getUsb());
		}
	}

	private boolean iterationChecked(ICondition bedingung, IField errorField, boolean isHModel) {
		Predicate<IEntityId> checker;
		if (isHModel) {
			checker = eid -> iterationCheckedHModel(errorField, eid);
		} else {
			checker = eid -> iterationCheckedA12Model(errorField, eid);
		}

		return IterationChecker.isIterationChecked(bedingung, checker, errorField, ec);
	}

	/**
	 * In H models, iterations are checked by field references that lie on a root group with
	 * same maximum iteration for root group, line and usb. The field has to be repeatable on all
	 * the levels, the error field is. It may also be repeatable on levels, the error field is
	 * <em>not</em>.
	 *
	 * @param errorField
	 * 		the error field.
	 * @param entityRef
	 * 		the candidate for checking the iterations.
	 * @return <code>true</code> if the field secures the iteration of the error field.
	 */
	private boolean iterationCheckedHModel(IField errorField, IEntityId entityRef) {
		IEntity entity = ec.get(entityRef.getFullName());
		// Für H-DVs haben wir immer zwei Wiederholungsebenen und potentiell den USB
		// Wenn an einer Stelle das Fehlerfeld wiederholbar ist, muss auch das zugehörige Feld mit der gleichen Wiederholung wiederholbar sein.
		long[] errorFieldMax = EntityUtils.getMax(errorField, true);

		// Groups that contain the error field must have all iterations equal to the error field and therefore check the iteration
		// .#Zeile is obligatory for the group in this case
		if (!entityRef.isField() && EntityUtils.isDescendantOrEqual(errorField.getFullName(), entityRef.getFullName())) {
			return true;
		}
		long[] entityMax = entityRef.getMaxValues();
		long[] entityIndices = entityRef.getIndices();
		long errorFieldMaxUsb = EntityUtils.getMaxUsb(errorField);
		long entityMaxUsb = EntityUtils.getMaxUsb(entity);
		long entityUsb = entityRef.getUsb();
		assert (errorFieldMax.length == 2);
		assert (entityMax.length == 2);

		for (int i = 0; i < errorFieldMax.length; i++) {
			if (errorFieldMax[i] > 1 && (errorFieldMax[i] != entityMax[i] || !isIteration(entityIndices[i]))) {
				return false;
			}
		}
		return errorFieldMaxUsb <= 1 ||
				(entityMaxUsb == errorFieldMaxUsb && isIteration(entityUsb));
	}

	/**
	 * In A12 models, iterations are checked by entity references that lie in the same repeatable
	 * context and specify iteration on all these levels.
	 *
	 * @param errorField
	 * 		the error field.
	 * @param entity
	 * 		the candidate for checking the iterations.
	 * @return <code>true</code> if the field secures the iteration of the error field.
	 */
	private static boolean iterationCheckedA12Model(IField errorField, IEntityId entity) {
		int maxIterationLevel = EntityUtils.getMaxIterationLvl(errorField);
		if (maxIterationLevel < 0) {
			// The method should be called only for repeatable error fields. So this should never happen but returning true is safe in this case.
			return true;
		}

		if (entity.getSemanticIndex() != null && !entity.getSemanticIndex().isEmpty()) {
			for (Object semIdx : entity.getSemanticIndex()) {
				// If the iteration is checked by a semantic index, this suffices
				// If a semantic index is not specified, also no value can be acquired.
				if (semIdx instanceof IEntityId semIdxEntity && iterationCheckedA12Model(errorField, semIdxEntity)) {
					return true;
				}
			}
		}

		String iterationLevelPathInErrorFieldPath = EntityUtils.getPathUpToLevel(maxIterationLevel, errorField.getFullName());
		if (!EntityUtils.isDescendantOrEqual(entity.getFullName(), iterationLevelPathInErrorFieldPath)) {
			// This is true for fields and for groups
			return false;
		}

		long[] entityIndices = entity.getIndices();
		long[] max = errorField.getMax();
		for (int i = 0; i <= maxIterationLevel; i++) {
			if (max[i] > 1 && !isIteration(entityIndices[i])) {
				return false;
			}
		}
		return true;
	}

	private record FieldReferenceResult(boolean isErroneous, List<IEntityId> erroneousEntities) {
		private FieldReferenceResult combine(FieldReferenceResult other) {
			boolean erroneous = this.isErroneous || other.isErroneous;
			List<IEntityId> entities = new ArrayList<>(this.erroneousEntities);
			entities.addAll(other.erroneousEntities);
			return new FieldReferenceResult(erroneous, entities);
		}
	}

	private static FieldReferenceResult feldReferenziertOhneKontextAngegeben(IParseTreeEntity e, IField calculatedField) {
		final boolean[] result = new boolean[] { false };
		final List<IEntityId> erroneousEntities = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(IEntityId e) {
				if (EntityUtils.isDescendantOrEqual(calculatedField.getFullName(), e.getFullName())) {
					erroneousEntities.add(e);
					result[0] = true;
				}
				if (e.getFilterCondition() != null) {
					e.getFilterCondition().traverse(this);
				}
				return true;
			}

			@Override
			public boolean visit(IRootGroupCondition rc) {
				if (!feldLiegtAufVordruck(calculatedField.getFullName(), rc.getRootGroupName())) {
					return false;
				}
				// 'VordruckAngegeben' for the calculated field is allowed
				if (rc.getConditionName() == RootGroupConditionName.MindestensEinVordruckAngegeben &&
						rc.getUsb() != IParseTreeEntity.WILDCARD && // if there is a USB, the USB must specify iteration
						(EntityUtils.getMaxLfdNr(calculatedField) == 1 || isIteration(rc.getRootGroupIndex()))) {
					// The spec must not contain an other iteration of the calculated field.
					return false;
				}
				IEntityId entity = rc.getEntityId();
				if (entity != null) {
					// For old world, the entity is not set
					erroneousEntities.add(entity);
				}
				result[0] = true;
				return false;
			}

			@Override
			public void endVisit(INumberOfRootGroupsOperation norgo) {
				if (feldLiegtAufVordruck(calculatedField.getFullName(), norgo.getRootGroupName())) {
					// Here we have no entity (with start/stop index in condition) to return.
					result[0] = true;
				}
			}

			@Override
			public boolean visit(IEntityListCondition elc) {
				// 'KontextAngegeben' for the calculated field is allowed
				if (elc.getConditionName() == EntityListConditionName.AllGroupsFilled &&
						elc.getEntityList().getEntityIdList().size() == 1 &&
						EntityUtils.isDescendantOrEqual(calculatedField.getFullName(),
								elc.getEntityList().getEntityIdList().get(0).getFullName())) {
					return false;
				}
				return true;
			}

			@Override
			public boolean visit(IGroupOperation gop) {
				assert gop.getOperatorName() == GroupOperationName.CurrentRepetition;
				return false;
			}
		};
		e.traverse(visitor);
		return new FieldReferenceResult(result[0], erroneousEntities);
	}

	private static boolean feldLiegtAufVordruck(String feldFullName, String vordruckName) {
		String vordruckPfad = EntityUtils.createRootGroupPath(vordruckName);
		return EntityUtils.isDescendantOrEqual(feldFullName, vordruckPfad);
	}

	/**
	 * @return <code>true</code> iff the last condition is a composite condition with or.
	 */
	private static boolean isComplexCalculation(List<ICondition> conditions) {
		ICondition lastCondition = conditions.get(conditions.size() - 1);
		if (conditions.size() < 2 || !(lastCondition instanceof ICompositeCondition)) {
			return false;
		}
		return ((ICompositeCondition) lastCondition).getLogOp() == LogOp.Or;
	}

	private boolean checkFeldGate(ICondition b, String fehlerFeldFullName) {
		if (!(b instanceof IEntityListCondition feldGate)) {
			return false;
		}
		return feldGate.getConditionName() == EntityListConditionName.AllFieldsFilled &&
				feldGate.getEntityList().getEntityIdList().size() == 1 &&
				checkErrorFieldName(feldGate.getEntityList().getEntityIdList().get(0).getFullName(), fehlerFeldFullName) &&
				(strictness == IServiceConfiguration.ParserStrictness.LENIENT ||
						checkIterationOnAllLevels(feldGate.getEntityList().getEntityIdList().get(0)));
	}

	private boolean checkErrorFieldName(String errorFieldFullName, String expectedName) {
		return strictness == IServiceConfiguration.ParserStrictness.LENIENT ?
				EntityUtils.getShortName(errorFieldFullName).equals(EntityUtils.getShortName(expectedName)) :
				errorFieldFullName.equals(expectedName);
	}

	private boolean checkIterationOnAllLevels(IEntityId id) {
		long[] max = id.getMaxValues();
		long[] indices = id.getIndices();

		assert (max.length == indices.length);

		for (int i = 0; i < max.length; i++) {
			if (!isIteration(indices[i]) && max[i] > 1) {
				return false;
			}
		}
		return true;
	}

	private Optional<IEntityId> extractBerechnetesFeld(ICondition b) {
		if (!(b instanceof IEntityListCondition feldGate)) {
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_FIELD_GATE);
			return Optional.empty();
		}
		return Optional.of(feldGate.getEntityList().getEntityIdList().get(0));
	}

	/**
	 * Liefert die CalculationInfo für die übergebene Regelbedingung. Sollte nur für
	 * {@link ICondition}en aufgerufen werden, für die
	 * {@link #checkNormalisierteForm(ICondition, IField)} <code>true</code> liefert.
	 *
	 * @param bedingung
	 * 		die zu analysierende Regelbedingung. Die zurückgegebene
	 *        {@link ICalculationInfo}-Instanz enthält die Teilberechnungen in der Expansion wie
	 * 		diese Bedingung.
	 * @param calcField
	 * 		das berechnete Feld
	 * @return die aus der Regelbedingung extrahierten CalculationInfo.
	 */
	public Optional<ICalculationInfo> extractCalculationInfo(ICondition bedingung, RoundingMode rundungsArt, IField calcField) {
		// 1. Feld-Gate-Bedingung: FeldAngegeben(bF) Und ...
		if (!(bedingung instanceof ICompositeCondition compositeCondition)) {
			reportProblem(CalculationErrorType.MVK_ERROR_INVALID_FIELD_GATE);
			return Optional.empty();
		}
		List<ICondition> bedingungen = compositeCondition.getConditionList();
		Optional<IEntityId> berechnetesFeld = extractBerechnetesFeld(bedingungen.get(0));
		if (berechnetesFeld.isEmpty()) {
			return Optional.empty();
		}

		// 3. Teilbedingungen extrahieren und prüfen
		// a. komplexe Bedingung => 2. von 2 Teilbedingungen ist ZusGesetzteBedingung der Teilbedingungen mit Oder
		Optional<List<Pair<ICondition, IOperation>>> teilberechnungsListe;
		ICondition commonPrecondition;
		if (isComplexCalculation(bedingungen)) {
			ICompositeCondition lastCondition = (ICompositeCondition) bedingungen.get(bedingungen.size() - 1);
			List<ICondition> teilBerechnungen = lastCondition.getConditionList();
			teilberechnungsListe = checkList(teilBerechnungen.stream()
					.map(b -> extractTeilBerechnung(b, rundungsArt, calcField))
					.toList());
			List<ICondition> commonPreconditionParts = bedingungen.subList(1, bedingungen.size() - 1);
			commonPrecondition = createAndOpFromList(commonPreconditionParts);
		} else if (bedingungen.size() > 2) { // b. mehr als eine Teilbedingung => einfache Form
			CompositeConditionImpl teilBer = new CompositeConditionImpl(compositeCondition, bedingungen.subList(1,
					bedingungen.size()), false);
			teilberechnungsListe = extractTeilBerechnung(teilBer, rundungsArt, calcField).map(Collections::singletonList);
			commonPrecondition = null;
		} else { // c. 2 Teilbedingungen, aber 2. nicht ZusGesetzteBedingung mit Oder
			teilberechnungsListe = extractTeilBerechnung(bedingungen.get(1), rundungsArt, calcField).map(
					Collections::singletonList);
			commonPrecondition = null;
		}

		return teilberechnungsListe.map(tbl -> new CalculationInfo(berechnetesFeld.get(), commonPrecondition, tbl,
				rundungsArt, this.rule.getFullName()));
	}

	private static ICondition createAndOpFromList(List<ICondition> parts) {
		if (parts.isEmpty()) {
			return null;
		}
		if (parts.size() == 1) {
			return parts.get(0);
		}
		return new CompositeConditionImpl(LogOp.And, parts);
	}

	/**
	 * Pull the Optional up.
	 *
	 * @param list
	 * 		the list, the optionals should be pulled up from.
	 * @return the list stripped by the optionals surrounded by {@link Optional#of(Object)} if all
	 * 		elements are {@link Optional#isPresent()} or {@link Optional#empty()}, if one of the
	 * 		elements is {@link Optional#empty()}.
	 */
	private static <T> Optional<List<T>> checkList(List<Optional<T>> list) {
		if (list.stream().allMatch(Optional::isPresent)) {
			return Optional.of(list.stream().map(Optional::get).toList());
		} else {
			return Optional.empty();
		}
	}

	private void reportProblem(CalculationErrorType errorType, Object... args) {
		this.errorLookup.reportError(pm, rule, errorType, args);
	}

	private void reportProblem(EntityIdImpl entity, CalculationErrorType errorType, Object... args) {
		if (!reportedReferenceErrorEntities.add(entity)) {
			// We already reported the erroneous entity (e.g. for a common precondition)
			return;
		}
		String message = this.errorLookup.getErrorMessage(errorType, args);
		int startIdx = entity.getStartIdxInCondition();
		int endIdx = entity.getStopIdxInCondition();
		MVKParserError error = new MVKParserError(startIdx, endIdx, 1, 1, rule, message);
		pm.reportProblem(error);
	}

	/**
	 * @param index
	 * @return true if <code>index</code> specifies {@link IParseTreeEntity#ITERATION} or
	 *        {@link IParseTreeEntity#PARALLEL_ITERATION}, false otherwise
	 */
	private static boolean isIteration(long index) {
		return index == IParseTreeEntity.ITERATION || index == IParseTreeEntity.PARALLEL_ITERATION;
	}
}
