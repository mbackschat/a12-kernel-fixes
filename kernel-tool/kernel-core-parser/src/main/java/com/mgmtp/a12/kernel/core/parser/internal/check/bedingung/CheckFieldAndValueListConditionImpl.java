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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldOderStringContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldUndStringListenSpezifierContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FieldAndValueListConditionContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationDataTable;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

import jakarta.validation.constraints.NotNull;

/**
 * Checks the parameters for the ValueNotConsistent condition.
 * <p>
 * The first parameter must be an enumeration field with table reference.
 * The other parameters need to match the dependent columns defined in the first parameter.
 * These parameter may be
 * <ul>
 * <li>string constants that are contained in the dependent column, or</li>
 * <li>enumeration fields, where all the values are contained in the respective column.</li>
 * </ul>
 * <p>
 * The checks of the values are only performed, if the table of the first parameter
 * {@link IEnumerationType} is present, i.e it contains a value for
 * {@link IEnumerationType#getTable()}.
 */
public class CheckFieldAndValueListConditionImpl extends CheckImplAbstractClass {

	private final CheckEntityUtils entityChecker;

	public CheckFieldAndValueListConditionImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
		this.entityChecker = new CheckEntityUtils(parser);
	}

	public void check(FieldAndValueListConditionContext ctx) {
		String opName = ctx.fieldAndValueListConditionName().getText();

		FeldUndStringListenSpezifierContext valueListSpecifier = ctx.feldUndStringListenSpezifier();
		EntitySpecContext ectx = ctx.entitySpec();
		IField f = checkField(ectx, opName);
		IEnumerationType enumType = (IEnumerationType) f.getType();

		List<FeldOderStringContext> values = valueListSpecifier.feldOderString();
		if (values.size() != enumType.getDependentColumns().size()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_DEPENDENT_COLUMN_SIZE_MISMATCH, opName);
			throw createMVKParserError(ctx, errMessage);
		}

		values.forEach(v -> checkParameterType(v, opName));

		enumType.getTable().ifPresent(table -> {
			for (int i = 0; i < values.size(); i++) {
				checkValue(values.get(i), table, enumType.getDependentColumns().get(i));
			}
		});
	}

	/**
	 * Checks that the given entity specified is one single field of type enumeration with table
	 * reference.
	 *
	 * @param ectx
	 * 		context of the entity - used for errorMessaging only
	 * @param opName
	 * 		name of the operation, used for error messages
	 * @return field referenced in the condition
	 */
	private IField checkField(EntitySpecContext ectx, String opName) {
		EntityRef ref = getEntityRef(ectx);

		IField f = entityChecker.checkIsField(ref, opName, ectx);
		entityChecker.checkNoWildCards(ref, opName, ectx);
		entityChecker.checkEnumWithTableReference(f, opName, ectx);
		return f;
	}

	private void checkParameterType(FeldOderStringContext ctx, String opName) {
		if (ctx.entitySpec() == null) {
			return;
		}
		EntityRef ref = getEntityRef(ctx.entitySpec());
		IField field = entityChecker.checkIsField(ref, opName, ctx);
		entityChecker.checkNoWildCards(ref, opName, ctx);
		if (!FieldUtils.isEnum(field)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_ONLY_ENUMS_ALLOWED, opName);
			throw createMVKParserError(ctx, errMessage);
		}
	}

	/**
	 * Check the value of one parameter against the corresponding column of the table.
	 *
	 * @param ctx
	 * 		the {@link ParserRuleContext} containing the parameter.
	 * @param table
	 * 		the table against which to check. The method should only be called if the table is
	 * 		present.
	 * @param dependentColumnName
	 * 		the columnName of the dependent column.
	 */
	private void checkValue(FeldOderStringContext ctx, IEnumerationDataTable table, String dependentColumnName) {
		Set<String> allowedValues = table.getDistinctValues(dependentColumnName);
		if (allowedValues.isEmpty()) {
			// Column does not exist -> enum is not valid -> we won't check further
			return;
		}

		if (ctx.StringLiteral() != null) {
			String value = CommonUtils.removeAndUnescapeQuotesAndRemoveCrFromCrLf(ctx.StringLiteral().getText());
			if (!allowedValues.contains(value)) {
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_STRING_VALUE_NOT_IN_COLUMN, value,
						dependentColumnName);
				throw createMVKParserError(ctx, errMessage);
			}
		} else {
			EntityRef ref = getEntityRef(ctx.entitySpec());
			// Enumeration check was already performed
			IEnumerationType fieldType = (IEnumerationType) ((IField) ref.getEntity()).getType();
			Set<String> possibleValues = new HashSet<>(FieldUtils.getDistinctEnumValues(fieldType));
			possibleValues.removeAll(allowedValues);
			if (!possibleValues.isEmpty()) {
				String disallowedValuesString = possibleValues.stream().collect(Collectors.joining(", "));
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_ENUM_VALUES_NOT_IN_COLUMN,
						ref.getEntity().getFullName(), dependentColumnName, disallowedValuesString);
				throw createMVKParserError(ctx, errMessage);
			}
		}
	}
}
