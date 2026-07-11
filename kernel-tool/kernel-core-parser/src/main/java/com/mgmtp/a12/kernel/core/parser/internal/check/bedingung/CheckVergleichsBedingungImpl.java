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
package com.mgmtp.a12.kernel.core.parser.internal.check.bedingung;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckOpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EqualityVglContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.OperationContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.SINGLE_OPContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.VergleichsBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.DateFormat;
import com.mgmtp.a12.kernel.core.parser.internal.util.OperationResultType;
import com.mgmtp.a12.kernel.core.parser.internal.util.OperationResultType.OperationResultBaseType;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;
import com.mgmtp.a12.kernel.core.utils.internal.PatternUtils;

import jakarta.validation.constraints.NotNull;

/**
 * Stellt die Prüfungen für <code>VergleichsBedingung</code> bereit. Folgende Einschränkungen für
 * die <b>Vergleichsoperatoren</b> werden geprüft:
 * * <ul>
 * <li><b>WieMuster/NichtWieMuster</b>: auf der linken Seite des Vergleichs eine Feld-Operation
 * verwendet
 * wird, die vom Typ String ist und auf der rechten Seite des Vergleichs ein gültiger, nicht leerer
 * Regulärer Ausdruck. Für die regulären Ausdrücke gelten die Einschränkungen {@literal ->} siehe
 * AHB. Dies ist notwendig, da das pattern-Matching für C++ und Java gleich interpretiert werden
 * muss und für die verwendeten Bibliotheken gibt es sprachabhängige Einschränkungen.
 * {@link ParserErrorTypesEnum#MVK_MISSING_CONST_IN_PATTERN_COMP},
 * {@link ParserErrorTypesEnum#MVK_INVALID_PATTERN},
 * {@link ParserErrorTypesEnum#MVK_INVALID_TYPE_FOR_PATTERN_COMPARISON}
 * <li><b>Toleranz-Vergleich</b>:Es wird geprüft, dass nur Zahlen bzw. Geldbeträge verglichen
 * werden. {@link ParserErrorTypesEnum#MVK_INVALID_TYPE_FOR_TOLERANCE_OP},
 * <li><b>Kleiner/Größer</b>:Vergleiche dürfen nur für Zahlen, Geldbeträge oder Datumsangaben
 * verwendet werden. Der Vergleich auf kleiner /größer für Strings ist zwar definiert, macht aber
 * für eine Validierung keinen Sinn – ebenso ist ein Vergleich auf kleiner oder größer für
 * vordefinierte Typen nicht sinnvoll.{@link ParserErrorTypesEnum#MVK_INVALID_TYPE_FOR_COMPARISON}
 * </li>
 * </ul>
 * <p>
 * *
 * <p>
 * Eine Konstante darf nicht mit einer Konstante verglichen werden, außer es handelt sich um die
 * Datumskonstanten Referenzjahr bzw. HEUTE. Solche Vergleiche machen keinen Sinn, da sie ja
 * immer konstant falsch oder wahr sind. Daher werden sie verboten. Insbesondere wird so eine
 * Fehleingabe erkannt, wenn vergessen wurde, um ein Feld mit einer Ziffernfolge als Namen, eckige
 * Klammern zu notieren {@link ParserErrorTypesEnum#MVK_INVALID_COMPARE_TO_CONST}
 * <p>
 * <p>
 * <p>
 * Folgende Einschränkungen werden für die verschiedenen Typen geprüft:
 * <ul>
 * <li><b>Datumsbereiche</b> dürfen nur mit == und != mit passenden Datumsbereichen verglichen werden
 * {@link ParserErrorTypesEnum#MVK_INVALID_COMPARE_TO_DATE_RANGE}
 * <li><b>JaNein Felder</b> dürfen nur mit einem JaNein-Feld oder den Konstanten WAHR bzw. FALSCH
 * verglichen werden. Als Vergleichs-Operator sind nur == oder != zulässig
 * {@link ParserErrorTypesEnum#MVK_INVALID_COMPARE_TO_YESNO}
 * <li><b>Ja Felder</b> dürfen nur mit einem Ja-Feld oder den Konstanten WAHR verglichen werden. Als
 * Vergleichs-Operator sind nur == oder != zulässig
 * {@link ParserErrorTypesEnum#MVK_INVALID_COMPARE_TO_YES}
 * <li><b>Enumeration/String</b> darf nur mit einer Enumeration oder mit einem String verglichen
 * werden. Falls der String als String-Konstante spezifiziert ist, muss der String ein gültiger Wert
 * in der Enumerations sein. {@link ParserErrorTypesEnum#MVK_INVALID_COMPARE_TO_ENUM_OR_STRING} Wenn
 * zwei Enumerationsfelder verglichen werden, wird nicht geprüft, dass die Enumerationsfelder von
 * den erlaubten Werten zusammenpassen. Ein String darf nur mit einer Enumeration oder einem String
 * verglichen werden. {@link ParserErrorTypesEnum#MVK_INVALID_STRING_CONSTANT_FOR_ENUM_COMPARISON}
 * <li><b>Datum Felder</b> dürfen nur mit einem Datum oder einer String-Konstante verglichen werden.
 * Das Datum kann aus einer Operation konstruiert worden sein (z.B. StartAusDatumBereich). Die
 * String-Konstante muss von der Form TT.MM. oder TT.MM.JJJJ sein – dies gilt unabhängig von der
 * Projekt-Sprache. Die Datumsformate der zu vergleichenden Datumsangaben müssen entweder beide eine
 * Jahresangabe enthalten oder keine. Ausnahme bei Modellen mit Referenzjahr (ExtMP-Welt) hier kann
 * das Referenzjahr als Jahresangabe verwendet werden. Felder mit Format HH:mm:ss dürfen nur mit Feldern
 * desselben Formats verglichen werden. Ein '==' oder '!=' Vergleich von Formaten mit Zeitangabe mit
 * Formaten ohne Zeitangabe ist nicht sinnvoll und ist verboten um Fehleingaben zu
 * vermeiden.{@link ParserErrorTypesEnum#MVK_INVALID_COMPARE_TO_DATE}
 * <li><b>Zahlend/Geldbeträge</b> darf nur mit einer Zahl verglichen werden. Entsprechende Fehler
 * sind bereits durch die Überprüfungen der anderen Feldtypen gemeldet worden. Eine Zahl darf auch
 * mit einem Referenzjahr verglichen werden (ExtMP).
 * Die maximalen Nachkommastellen der Zahlen der linken und rechten Seite müssen übereinstimmen.
 * Dabei werden für multiplikative Konstanten beliebige zusätzliche Nachkommastellen ergänzt, wenn
 * nötig. Die Prüfung kann durch <code>@UnterdrueckeWarnung(MVK_INVALID_COMPARE_DEC_PLACES)</code>
 * in der Regelbedingung unterdrückt werden.
 * </ul>
 */
public class CheckVergleichsBedingungImpl extends CheckImplAbstractClass {

	public CheckVergleichsBedingungImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	/**
	 * checks if the comparison between the two operations are valid.
	 * <ol>
	 * <li>comparison is not allowed for date ranges
	 * <li>do not compare two constants, except date constants
	 * <li>checks if the comparison operation is correct.
	 * <ul>
	 * <li>tolerance operation is only allowed for numbers
	 * <li>pattern compare is only allowed for strings, and correct patterns
	 * <li>ordering operation is only allowed for numbers or dates
	 * </ul>
	 * <li>special checks if operations are yes / yesno
	 * <li>special checks if operations are dates
	 * <li>special checks if operations are enums or strings
	 * </ul>
	 *
	 * @param ctx
	 * 		context specifying the comparison
	 */
	public void check(@NotNull VergleichsBedingungContext ctx) {
		OperationContext o1Ctx = ctx.lhs;
		OperationContext o2Ctx = ctx.rhs;

		OperationResultType rt1 = getOpType(o1Ctx);
		OperationResultType rt2 = getOpType(o2Ctx);

		//2. do not compare two constants
		if (!isValidConstantComparison(o1Ctx, o2Ctx, rt1)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_CONST);
			throw createMVKParserError(ctx, errMessage);
		}

		//3. in case of a tolerance operation:  only numbers are allowed
		if (isToleranceOp(ctx)) {
			if (!CheckOpUtils.isOpLikeNumber(rt1) || !CheckOpUtils.isOpLikeNumber(rt2)) {
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_TYPE_FOR_TOLERANCE_OP);
				throw createMVKParserError(ctx, errMessage);
			}

			// no further checks necessary, we can return.
			return;
		}

		//special handling of the pattern-operation.
		if (isPatternOp(ctx)) {
			checkPatternOp(o1Ctx, o2Ctx, ctx.vglOp().patternVgl().getText());
			// no further checks necessary, we can return.
			return;
		}

		//ordering is  allowed only for numbers and dates
		//this checks only if ordering op is allowed. Checks if a date is compared to a number is handled
		//later within checkDateType.
		if (isOrderingOp(ctx) && (!CheckOpUtils.isOpLikeNumber(rt1) && !isTypeADate(rt1) || !CheckOpUtils.isOpLikeNumber(
				rt2) && !isTypeADate(rt2))) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_TYPE_FOR_COMPARISON);
			throw createMVKParserError(ctx, errMessage);
		}
		// ordering operation is o.k., but we need further checks.  Thus, do not return here.

		//if one of the operations is a yes / yesno operation, the other one has to be one as well.
		checkYesAndYesNoOperations(o1Ctx, o2Ctx);

		//check date comparison
		checkDateType(ctx, o1Ctx, o2Ctx);

		// check date range comparison
		checkDateRangeType(ctx, o1Ctx, o2Ctx);

		//check comparison for enums or strings.
		checkStringOrEnumType(o1Ctx, o2Ctx, true);
		checkStringOrEnumType(o2Ctx, o1Ctx, false);

		//number
		checkNumberComparison(o1Ctx, o2Ctx, isEqualsOrUnequalsOp(ctx));

		//comparison between BaseYear and boolean const is not allowed
		checkBaseYearAndBooleanConst(o1Ctx, o2Ctx);

		//comparison BaseYear and BaseYear is not allowed
		checkBaseYearAndBaseYear(o1Ctx, o2Ctx);
	}

	private boolean isValidConstantComparison(OperationContext o1Ctx, OperationContext o2Ctx, OperationResultType rt1) {
		if (!CheckOpUtils.isConstant(o1Ctx) || !CheckOpUtils.isConstant(o2Ctx)) {
			return true;
		}
		if (rt1.getBaseType() != OperationResultBaseType.DATETIME) {
			return false;
		}
		// do not compare Heute/Jetzt with Heute/Jetzt and date strings with date strings
		return CheckOpUtils.isHeuteOrJetzt(o1Ctx) != CheckOpUtils.isHeuteOrJetzt(o2Ctx);
	}

	private void checkBaseYearAndBooleanConst(OperationContext o1Ctx, OperationContext o2Ctx) {
		OperationResultType rt1 = getOpType(o1Ctx);
		OperationResultType rt2 = getOpType(o2Ctx);

		if ((isTypeABooleanConst(rt1) && rt2.getBaseType() == OperationResultBaseType.BASE_YEAR)
				|| (isTypeABooleanConst(rt2) && rt1.getBaseType() == OperationResultBaseType.BASE_YEAR)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INCONSISTENT_TYPES_COMPARED);
			throw createMVKParserError(o1Ctx, errMessage);
		}
	}

	private void checkBaseYearAndBaseYear(OperationContext o1Ctx, OperationContext o2Ctx) {
		OperationResultType rt1 = getOpType(o1Ctx);
		OperationResultType rt2 = getOpType(o2Ctx);

		if (rt1.getBaseType() == OperationResultBaseType.BASE_YEAR && rt2.getBaseType() == OperationResultBaseType.BASE_YEAR) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INCONSISTENT_TYPES_COMPARED);
			throw createMVKParserError(o1Ctx, errMessage);
		}
	}

	private void checkNumberComparison(OperationContext o1Ctx, OperationContext o2Ctx, boolean isUnEqualsComparison) {
		OperationResultType rt1 = getOpType(o1Ctx);
		OperationResultType rt2 = getOpType(o2Ctx);
		if ((isTypeANumber(rt1) && !CheckOpUtils.isOpLikeNumber(rt2)) || (isTypeANumber(rt2) && !CheckOpUtils.isOpLikeNumber(
				rt1)) && !fullParser.isLenient()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INCONSISTENT_TYPES_COMPARED);
			throw createMVKParserError(o1Ctx, errMessage);
		}
		if (CheckOpUtils.isOpLikeNumber(rt1) &&
				CheckOpUtils.isOpLikeNumber(rt2) && isUnEqualsComparison &&
				!fullParser.isWarningSuppressed(
						ParserErrorTypesEnum.MVK_INVALID_COMPARE_DEC_PLACES.name()) && !fullParser.isLenient()) {
			Integer lhs = getMaxFractionalDigits(rt1);
			Integer rhs = getMaxFractionalDigits(rt2);
			if (lhs == null || rhs == null ||
					(lhs < rhs && !rt1.hasMultiplicativeConstant()) ||
					(lhs > rhs && !rt2.hasMultiplicativeConstant())) {
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_DEC_PLACES, lhs != null ? lhs : "?",
						rhs != null ? rhs : "?");
				throw createMVKParserError(o1Ctx, errMessage);
			}
		}
	}

	/**
	 * check the pattern operation.
	 * <ul>
	 * <li>both operations must match a string type (i.e. strings, predefined types and extensible
	 * enumerations)</li>
	 * <li>the second operation must be a constant operation specifying a valid regular expression
	 * </li>
	 * <li>the first operation must be a field value operation</li>
	 * </ul>
	 *
	 * @param o1Ctx
	 * 		context specifying the first operation
	 * @param o2Ctx
	 * 		context specifying the second operation
	 */
	private void checkPatternOp(OperationContext o1Ctx, OperationContext o2Ctx, String opName) {

		// Make sure the first parameter is the operation field value with a String inside.

		// (1) Make sure the operation is 'field value'
		if (!(o1Ctx instanceof SINGLE_OPContext singleOpContext) || singleOpContext.einfacheOperation().feldWertOp() == null) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_PARAMETER_FOR_PATTERN_COMPARISON, opName);
			throw createMVKParserError(o1Ctx, errMessage);
		}

		// In case of a pattern comparison, both contexts must match a string.
		OperationResultType rt1 = getOpType(o1Ctx);
		OperationResultType rt2 = getOpType(o2Ctx);

		if (!isTypeAString(rt1)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_TYPE_FOR_PATTERN_COMPARISON, opName);
			throw createMVKParserError(o1Ctx, errMessage);
		}
		if (!isTypeAString(rt2)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_TYPE_FOR_PATTERN_COMPARISON, opName);
			throw createMVKParserError(o2Ctx, errMessage);
		}

		//the second context must be a constant.
		if (!CheckOpUtils.isConstant(o2Ctx)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_MISSING_CONST_IN_PATTERN_COMP, opName);
			throw createMVKParserError(o2Ctx, errMessage);
		}

		//constant must be a valid pattern
		String pattern = CheckOpUtils.getConstantValue(o2Ctx);

		// Only do more testing if we are in ExtMP World.
		if (fullParser.isExtmp()) {
			if (!StringUtils.isEmpty(pattern) && CommonUtils.isRegExValid(pattern)) {
				return;
			}
		} else {
			try {
				Pattern.compile(pattern);
				if (PatternUtils.isPatternValid(pattern)) {
					return;
				}
			} catch (PatternSyntaxException e) {
				// We don't need to handle the error, a Parser error is thrown if we don't return anyways.
			}
		}

		String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_PATTERN, pattern, opName);
		throw createMVKParserError(o2Ctx, errMessage);
	}

	//check for strings and enums.
	//if an enum is compared to a string constant,  the constant must be a valid value of the enumeration.
	private void checkStringOrEnumType(OperationContext o1Ctx, OperationContext o2Ctx, boolean isForwardCheck) {
		if (fullParser.isLenient()) {
			return;
		}

		OperationResultType rt1 = getOpType(o1Ctx);
		OperationResultType rt2 = getOpType(o2Ctx);

		if (isTypeAnEnum(rt1) && isTypeAnEnum(rt2) && !isForwardCheck) {
			return;
		}

		//both have the same base type --> o.k.
		if (rt1.getBaseType().equals(rt2.getBaseType())) {
			return;
		}

		//in case of a string,  an enumeration type is allowed.
		if (isTypeAString(rt1)) {
			if (isTypeAnEnum(rt2)) {
				checkEnumVsStringType(rt2, o1Ctx, o2Ctx);
				return;
			}

			//other type is no string and no enumeration -> error
			createStringOrEnumError(o2Ctx);
		}

		//in case of an enumeration
		if (isTypeAnEnum(rt1) && !isTypeAString(rt2)) {
			//other type is no string and no enumeration -> error
			createStringOrEnumError(o1Ctx);
		}

		//type is no string and no enumeration: no further checks.
	}

	private void checkEnumVsStringType(OperationResultType enumResultType, OperationContext stringCtx, OperationContext enumCtx) {
		//check enumeration values in case of a string constant.
		if (CheckOpUtils.isConstant(stringCtx)) {
			checkEnumValue(stringCtx, enumResultType);
		}
	}

	private void createStringOrEnumError(OperationContext errorCtx) {
		String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_ENUM_OR_STRING);
		throw createMVKParserError(errorCtx, errMessage);
	}

	/**
	 * checks if the constant value is a valid enumeration value.
	 *
	 * @param ctxOfString
	 * 		specifying the string constant
	 * @param enumType
	 * 		specifying the enumeration type.
	 */
	private void checkEnumValue(OperationContext ctxOfString, OperationResultType enumType) {
		if (FieldUtils.isTableEnumWithoutTable(enumType.getFieldType())) {
			// No check for table enums without given table
			return;
		}
		String value = CheckOpUtils.getConstantValue(ctxOfString);
		assert value != null;
		Set<String> validValues = getValidEnumValues(enumType);
		if (!validValues.contains(value)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_STRING_CONSTANT_FOR_ENUM_COMPARISON, value);
			throw createMVKParserError(ctxOfString, errMessage);
		}
	}

	private Set<String> getValidEnumValues(OperationResultType enumType) {
		String category = enumType.getCategory();
		if (StringUtils.isNotEmpty(category)) {
			return enumType.getEnumCategoryValues(category);
		} else {
			return enumType.getEnumValues();
		}
	}

	/**
	 * In case of dates, we have to check if either both dates have a year specified or not.
	 *
	 * @param o1Ctx
	 * 		context specifying the first operation
	 * @param o2Ctx
	 * 		context specifying the second operation
	 */
	private void checkDateType(VergleichsBedingungContext ctx, OperationContext o1Ctx, OperationContext o2Ctx) {
		OperationResultType rt1 = getOpType(o1Ctx);
		OperationResultType rt2 = getOpType(o2Ctx);
		if (!isTypeADate(rt1) && !isTypeADate(rt2)) {
			// No date -> not relevant.
			return;
		}
		if (isTypeADate(rt1) && !isTypeComparableToDate(rt2)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_DATE);
			throw createMVKParserError(o2Ctx, errMessage);
		}
		if (isTypeADate(rt2) && !isTypeComparableToDate(rt1)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_DATE);
			throw createMVKParserError(o1Ctx, errMessage);
		}
		if ((rt1.getBaseType() == OperationResultBaseType.BASE_YEAR && !DateFormat.isFormatWithDateSpecification(
				rt2.getDateFormat())) ||
				(rt2.getBaseType() == OperationResultBaseType.BASE_YEAR && !DateFormat.isFormatWithDateSpecification(
						rt1.getDateFormat()))) {
			// Cannot compare BaseYear to time
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_DATE);
			throw createMVKParserError(o1Ctx, errMessage);
		}
		if (!DateFormat.formatsConsistentRegardingFormat(rt1.getDateFormat(), rt2.getDateFormat(), getParser().hasBaseYear())) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_DATE);
			throw createMVKParserError(o1Ctx, errMessage);
		}
		// '=='- and '!='-operations are not allowed if a datetime is compared to the constant 'today' or to a non-datetime field
		if (isEqualsOrUnequalsOp(ctx) && DateFormat.isFormatWithTimeSpecification(
				rt1.getDateFormat()) != DateFormat.isFormatWithTimeSpecification(rt2.getDateFormat())) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_DATE);
			throw createMVKParserError(o1Ctx, errMessage);
		}
	}

	private void checkDateRangeType(VergleichsBedingungContext ctx, OperationContext o1Ctx, OperationContext o2Ctx) {
		OperationResultType rt1 = getOpType(o1Ctx);
		OperationResultType rt2 = getOpType(o2Ctx);
		if (!isTypeADateRange(rt1) && !isTypeADateRange(rt2)) {
			// No date range -> not relevant.
			return;
		}
		if (isTypeADateRange(rt1) != isTypeADateRange(rt2)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_DATE_RANGE);
			throw createMVKParserError(o2Ctx, errMessage);
		}
		// We don't want to include the year addition semantics, therefore we set hasBaseYear always to false
		if (!rt1.getDateFormat().hasSameComponents(rt2.getDateFormat(), false)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_DATE_RANGE);
			throw createMVKParserError(o1Ctx, errMessage);
		}
		// only '=='- and '!='-operations are allowed
		if (!isEqualsOrUnequalsOp(ctx)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_DATE_RANGE);
			throw createMVKParserError(o1Ctx, errMessage);
		}
	}

	private boolean isTypeABooleanConst(OperationResultType rt) {
		return rt.getBaseType() == OperationResultBaseType.CONSTN || rt.getBaseType() == OperationResultBaseType.CONSTY;
	}

	/**
	 * checks if the comparison is valid if the given result type is a Yes-Type or a YesNo-type
	 *
	 * @param o1Ctx
	 * 		context specifying the first operation
	 * @param o2Ctx
	 * 		context specifying the second operation
	 */
	private void checkYesAndYesNoOperations(OperationContext o1Ctx, OperationContext o2Ctx) {
		OperationResultType rt1 = getOpType(o1Ctx);
		OperationResultType rt2 = getOpType(o2Ctx);

		//in case of a YesType:  comparison only with (un-)equal
		if (isTypeAYes(rt1) || isTypeAYes(rt2)) {
			EqualityVglContext eq = ((VergleichsBedingungContext) o1Ctx.getParent()).vglOp().eVgl;
			if (eq == null) { // for Yes-Fields only (un-)equal-Comparison is allowed.
				throw createMVKParserError(o1Ctx.getParent(), getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_YES));
			}
			if (isTypeAYes(rt1) && isTypeAYes(rt2) && eq.ne == null) { // for two yes fields, only unequal is allowed
				throw createMVKParserError(o1Ctx.getParent(), getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_YES));
			}
		}

		if ((isTypeAYes(rt1) && !isTypeComparableToYes(rt2)) ||
				(isTypeAYes(rt2) && !isTypeComparableToYes(rt1))) {
			throw createMVKParserError(o1Ctx.getParent(), getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_YES));
		}

		if (isTypeAYes(rt1)
				&& isTypeAYes(rt2)
				&& !Objects.equals(rt1.getYesValue(), rt2.getYesValue())) {
			throw createMVKParserError(o1Ctx.getParent(), getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_YES));
		}

		if ((isTypeAYesNo(rt1) && !isTypeComparableToYesNo(rt2)) ||
				(isTypeAYesNo(rt2) && !isTypeComparableToYesNo(rt1))) {
			throw createMVKParserError(o1Ctx.getParent(), getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_YESNO));
		}

		if (isTypeAYesNo(rt1) && isTypeAYesNo(rt2)
				&& (!Objects.equals(rt1.getYesValue(), rt2.getYesValue()) || !Objects.equals(rt1.getNoValue(), rt2.getNoValue()))) {
			throw createMVKParserError(o1Ctx.getParent(), getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_COMPARE_TO_YESNO));
		}
	}

	private boolean isOrderingOp(VergleichsBedingungContext ctx) {
		return ctx.vglOp().oVgl != null;
	}

	private boolean isPatternOp(VergleichsBedingungContext ctx) {
		return ctx.vglOp().pVgl != null;
	}

	private boolean isToleranceOp(VergleichsBedingungContext ctx) {
		return ctx.vglOp().tVgl != null;
	}

	private boolean isEqualsOrUnequalsOp(VergleichsBedingungContext ctx) {
		return ctx.vglOp().eVgl != null;
	}

	/**
	 * returns true, if the given type can be compared to a YesType
	 *
	 * @param rt
	 * 		result type
	 * @return true if a comparison to a YesType is valid.
	 */
	private boolean isTypeComparableToYes(OperationResultType rt) {
		return rt.getBaseType() == OperationResultBaseType.BOOLY ||
				rt.getBaseType() == OperationResultBaseType.CONSTY;
	}

	/**
	 * returns true, if the given type can be compared to a Date
	 *
	 * @param rt
	 * 		result type
	 * @return true if a comparison to a DateType is valid.
	 */
	private boolean isTypeComparableToDate(OperationResultType rt) {
		return rt.getBaseType() == OperationResultBaseType.DATETIME || rt.getBaseType() == OperationResultBaseType.BASE_YEAR;
	}

	/**
	 * returns true, if the given type can be compared to a YesNoType
	 *
	 * @param rt
	 * 		result type
	 * @return true if a comparison to a YesNoType is valid.
	 */
	private boolean isTypeComparableToYesNo(OperationResultType rt) {
		return isTypeAYesNo(rt) ||
				rt.getBaseType() == OperationResultBaseType.CONSTY ||
				rt.getBaseType() == OperationResultBaseType.CONSTN;
	}

	//********************************************************************************************
	private boolean isTypeAString(OperationResultType rt) {
		return rt.getBaseType() == OperationResultBaseType.STRING;
	}

	private boolean isTypeANumber(OperationResultType rt) {
		return rt.getBaseType() == OperationResultBaseType.NUMBER;
	}

	private boolean isTypeAnEnum(OperationResultType rt) {
		return rt.getBaseType() == OperationResultBaseType.ENUM;
	}

	private boolean isTypeADate(OperationResultType rt) {
		return rt.getBaseType() == OperationResultBaseType.DATETIME;
	}

	private boolean isTypeADateRange(OperationResultType rt) {
		return rt.getBaseType() == OperationResultBaseType.DATE_RANGE;
	}

	private boolean isTypeAYesNo(OperationResultType rt) {
		return rt.getBaseType() == OperationResultBaseType.BOOLYN;
	}

	private boolean isTypeAYes(OperationResultType rt) {
		return rt.getBaseType() == OperationResultBaseType.BOOLY;
	}

}