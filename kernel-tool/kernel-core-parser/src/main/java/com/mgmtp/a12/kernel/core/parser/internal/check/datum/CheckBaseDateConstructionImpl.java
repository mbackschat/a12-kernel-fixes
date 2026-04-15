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

package com.mgmtp.a12.kernel.core.parser.internal.check.datum;

import java.util.Optional;

import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.DateUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.DatumParamContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.INumberType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

public abstract class CheckBaseDateConstructionImpl extends CheckImplAbstractClass {

	private static final int MAX_VALUE_YEAR = 9999;
	protected static final int MAX_VALUE_CENTURY = 99;

	protected CheckBaseDateConstructionImpl(FullChecks_ConditionParser parser) {
		super(parser);
	}

	protected void checkConstant(DatumParamContext dp, int minIncl, int maxExcl, int length) {
		String s = dp.s.getText();
		String nu = CommonUtils.removeAndUnescapeQuotesAndRemoveCrFromCrLf(s);
		if (lengthsEqualOrUnspecified(length, nu.length()) && !nu.isEmpty() && StringUtils.isNumeric(nu)) {
			int i = Integer.parseInt(nu);
			if (i >= minIncl && i < maxExcl) {
				return;
			}
		}
		String opName = dp.getParent().getStart().getText();
		String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_WRONG_DATE_FORMAT_FOR_OP, opName, nu);
		throw createMVKParserError(dp, errMessage);
	}

	/**
	 * Checks whether the datumExtractOp is allowed at the position it was specified.
	 *
	 * @param dp
	 * 		context of the provided datumExtractOp
	 * @param expectedDatumExtractOp
	 * 		expected datumExtractOp
	 */
	protected void checkCorrectDatumExtractOp(DatumParamContext dp, int expectedDatumExtractOp) {
		int providedDatumExtractOp = dp.getStart().getType();

		if (providedDatumExtractOp == expectedDatumExtractOp) {
			return;
		}

		Token constructOp = dp.getParent().getStart(); // Differentiate between date and time construct
		String constructOpName = constructOp.getText();
		String providedDatumExtractOpName = dp.getStart().getText();
		String expectedDatumExtractOpName =
				new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
						fullParser.getPss()).getTerminalName(
						expectedDatumExtractOp);
		String errMessage;

		switch (constructOp.getType()) {
			case GrammarOnly_ConditionParser.DATUM:
				errMessage = getErrorMessageForDateConstructWithExtractionOp(providedDatumExtractOp, constructOpName,
						expectedDatumExtractOpName, providedDatumExtractOpName);
				throw createMVKParserError(dp, errMessage);
			case GrammarOnly_ConditionParser.TIME:
				errMessage = getErrorMessageForTimeConstructWithExtractionOp(providedDatumExtractOp, constructOpName,
						expectedDatumExtractOpName, providedDatumExtractOpName);
				throw createMVKParserError(dp, errMessage);
			default:
				String invalidConstructOpType =
						new ConditionMultiLanguageHandler(fullParser.getLanguageUsedInConditionText(),
								fullParser.getPss()).getTerminalName(
								constructOp.getType());
				throw new IllegalArgumentException(
						"Unexpected construction type using date extraction parameter: " + invalidConstructOpType);
		}
	}

	private String getErrorMessageForDateConstructWithExtractionOp(int providedDatumExtractOp, String constructOpName,
			String expectedDatumExtractOpName, String providedDatumExtractOpName) {
		return switch (providedDatumExtractOp) {
			case GrammarOnly_ConditionParser.TAG_AUS_DATUM, GrammarOnly_ConditionParser.MONAT_AUS_DATUM,
				 GrammarOnly_ConditionParser.JAHR_AUS_DATUM ->
					getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_DATE_OR_TIME_EXTRACT_OP, constructOpName,
							expectedDatumExtractOpName);
			default -> getErrorMessage(ParserErrorTypesEnum.MVK_DATE_OR_TIME_EXTRACT_OP_NOT_ALLOWED, constructOpName,
					providedDatumExtractOpName);
		};
	}

	private String getErrorMessageForTimeConstructWithExtractionOp(int providedDatumExtractOp, String constructOpName,
			String expectedDatumExtractOpName, String providedDatumExtractOpName) {
		return switch (providedDatumExtractOp) {
			case GrammarOnly_ConditionParser.STUNDEN_AUS_ZEIT, GrammarOnly_ConditionParser.MINUTEN_AUS_ZEIT,
				 GrammarOnly_ConditionParser.SEKUNDEN_AUS_ZEIT ->
					getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_DATE_OR_TIME_EXTRACT_OP, constructOpName,
							expectedDatumExtractOpName);
			default -> getErrorMessage(ParserErrorTypesEnum.MVK_DATE_OR_TIME_EXTRACT_OP_NOT_ALLOWED, constructOpName,
					providedDatumExtractOpName);
		};
	}

	/**
	 * checks the param if it is an entity
	 *
	 * @param ctx
	 * 		context
	 * @param expectedMaxLength
	 * 		max length allowed for the date part to check
	 * @param expectedMaxValue
	 * 		max value allowed for the date part to check
	 * @param isMaxValueFlexible
	 * 		for the complete year and the century parameter the max value in the field
	 * 		definition is allowed to be smaller than expectedMaxValue. Otherwise max. value
	 * 		and expected max. value have to be equal, if the restriction is specified by a
	 * 		max. value values and not bey max. length.
	 */
	protected void checkParamIfEntity(DatumParamContext ctx, int expectedMaxLength, int expectedMaxValue,
			boolean isMaxValueFlexible) {
		String opName = ctx.getParent().getStart().getText();
		EntitySpecContext ectx = ctx.entitySpec();
		EntityRef ei = getEntityRef(ectx);
		CheckEntityUtils checker = new CheckEntityUtils(fullParser);

		IField f = checker.checkIsField(ei, opName, ectx);
		checker.checkNoWildCards(ei, opName, ectx);

		IFieldType type = f.getType();

		String eiNameForErrorOutput = ei.getNameBuiltFromConditionText();
		checkPatternIfString(type, eiNameForErrorOutput, ectx, opName);

		checker.checkNoCategories(ectx, opName);

		if (checkNumberType(type, expectedMaxLength, expectedMaxValue, isMaxValueFlexible) ||
				checkStringTypeWithExpectedLength(type, expectedMaxLength) ||
				(expectedMaxLength == 4 && checkYearType(type))) {
			return;
		}
		String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_WRONG_DATE_FORMAT_FOR_OP, opName, eiNameForErrorOutput);
		throw createMVKParserError(ectx.getParent(), errMessage);
	}

	private void checkPatternIfString(IFieldType type, String eiNameForErrorOutput, EntitySpecContext ctx, String opName) {
		if (type instanceof IStringType sType && !sType.isExtensibleEnum() && !regExDescribesOnlyDigits(sType)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_NO_DIGIT_PATTERN, opName, eiNameForErrorOutput);
			throw createMVKParserError(ctx.getParent(), errMessage);
		}
	}

	/**
	 * we know, that we do not consider all patterns describing only digits.
	 * As the pattern [0-9]+ is a predefined pattern, we take this.
	 *
	 * @param sType
	 * 		the string type to be checked
	 * @return true, if the pattern describes only digits.
	 */
	private boolean regExDescribesOnlyDigits(IStringType sType) {
		String pattern = sType.getPattern();
		if (StringUtils.isEmpty(pattern)) {
			return false;
		}
		int maxLength = sType.getMaxLength();
		if (pattern.equals("[0-9]+") || pattern.equals("[0-9]*")) {
			return true;
		}
		if (pattern.equals("\\d+") || pattern.equals("\\d*")) {
			return true;
		}

		if (maxLength != IFieldType.UNDEFINED) {
			String suffix = String.format("{%d}", maxLength);
			return pattern.equals("[0-9]" + suffix) || pattern.equals("\\d" + suffix);
		}
		return false;
	}

	private boolean checkYearType(IFieldType type) {
		return (type instanceof IDateType dateType && DateUtils.YEAR_FORMAT.equals(dateType.getFormat()));
	}

	/**
	 * checks that the type is a number, has no fractional digits, matches the given length or the
	 * given max value and has no sign.
	 *
	 * @param type
	 * 		type to be checked
	 * @param expectedMaxLength
	 * 		maxLength
	 * @param expectedMaxValue
	 * 		expected max value
	 * @param isMaxValueFlexible
	 * 		if true, the field type is allowed to specify a max. value between 1000 and
	 * 		expectedMaxValue
	 * @return true, if the fieldtype matches the requirements.
	 */
	private boolean checkNumberType(IFieldType type, int expectedMaxLength, double expectedMaxValue, boolean isMaxValueFlexible) {
		if (type instanceof INumberType numberType) {
			int maxLength = numberType.getMaxLength().orElse(-1);
			double maxValue = numberType.getMaxValue().orElse(-1d);
			return !hasFractionalDigits(numberType) &&
					(maxLength == expectedMaxLength || checkMaxValue(maxValue, expectedMaxValue, isMaxValueFlexible)) &&
					positivesOnly(numberType);
		} else {
			return false;
		}
	}

	private boolean positivesOnly(INumberType numberType) {
		// coreNumberType.hasSign() is mapped to !a12NumerType.positivesOnly()
		// since the default value for a12NumerType.positivesOnly() is 'false' (therefore 'true' for coreNumberType.hasSign())
		// and the a12 user cannot change this value with the data modeler
		// we need to deactivate this check for a12
		// Currently, in A12 it is ok not to have set positivesOnly, s. comment above. But in this case
		// a non negative minimal value has to be set (checkNonNegativeMinValuePresent).
		// For positive values, checkNonNegativeMinValuePresent is always true in ExtMP, s.t. this additional check is redundant but ok.
		return checkNonNegativeMinValuePresent(numberType) || !numberType.hasSign();
	}

	private boolean checkNonNegativeMinValuePresent(INumberType numberType) {
		Optional<Double> minValue = numberType.getMinValue();
		return minValue.isPresent() && minValue.get() >= 0;
	}

	private boolean checkMaxValue(double maxValue, double expectedMaxValue, boolean isMaxValueFlexible) {
		return maxValue == expectedMaxValue ||
				(isMaxValueFlexible && maxValue < expectedMaxValue &&
						//complete year
						((expectedMaxValue == MAX_VALUE_YEAR && maxValue >= 1000) ||
								//century
								(expectedMaxValue == MAX_VALUE_CENTURY && maxValue >= 10)));
	}

	private boolean checkStringTypeWithExpectedLength(IFieldType type, int expectedMaxLength) {
		if (type instanceof IStringType stringType) {
			return stringType.getMaxLength() == expectedMaxLength;
		}
		return false;
	}

	private boolean lengthsEqualOrUnspecified(int length1, int length2) {
		return length1 == IFieldType.UNDEFINED || length2 == IFieldType.UNDEFINED || length1 == length2;
	}

	private boolean hasFractionalDigits(INumberType t) {
		return (t.getMinFractionalDigits() != 0 || (t.getMaxFractionalDigits() != 0 && t.getMaxFractionalDigits() != IFieldType.UNDEFINED));
	}

}