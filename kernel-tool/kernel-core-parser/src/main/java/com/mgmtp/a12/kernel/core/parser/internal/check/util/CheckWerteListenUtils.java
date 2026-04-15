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
package com.mgmtp.a12.kernel.core.parser.internal.check.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

/**
 * Provides helper methods for value lists.
 */
public class CheckWerteListenUtils {

	private CheckWerteListenUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * read a string list provided by the given context.
	 * The method is used for different contextes specifying a string list.
	 * Thus, we must navigate using the children.
	 *
	 * @param ctx
	 * 		specifying the list.
	 * @return list of strings. list is empty, if ctx is null
	 */
	public static List<String> readListe(GrammarOnly_ConditionParser.StringListeContext ctx) {
		return readListeP(ctx);
	}

	public static List<String> readListe(GrammarOnly_ConditionParser.NumberListeContext ctx) {
		return readListeP(ctx);
	}

	private static List<String> readListeP(ParserRuleContext ctx) {
		List<String> result = new ArrayList<>();
		if (ctx != null) {
			ctx.children.stream().forEach(c -> addWertToListe(result, c));
		}
		return result;
	}

	/**
	 * add a value to the given list <code> stringList </code>, if the given parse tree contains a
	 * value.
	 * commas are omited, quotes and surplus backslashes are removed.
	 *
	 * @param stringList
	 * 		result list
	 * @param c
	 * 		parse tree specifying a comma or a string
	 */
	private static void addWertToListe(List<String> stringList, ParseTree c) {
		if (c != null) {//skip commas
			String s = c.getText();
			if (!",".equals(s)) {
				if (s.startsWith("\"")) {
					s = CommonUtils.removeAndUnescapeQuotesAndRemoveCrFromCrLf(s);
				}
				stringList.add(s);
			}
		}
	}

	/**
	 * checks for all strings in the stringList if it is a valid xml or category value.
	 *
	 * @param xmlValues
	 * 		collection specifying the xmlValues of the enumerations referenced without category access
	 * @param categoryValues
	 * 		collection specifying the values of the enum category values for the categories used in the references
	 * @param stringList
	 * 		list of the strings which have to be tested
	 * @param ctx
	 * 		for nice error messaging only
	 * @param obj
	 * 		for error messaging only
	 */
	public static void checkEnumValues(Collection<String> xmlValues, Collection<String> categoryValues,
			Collection<String> stringList, ParserRuleContext ctx, CheckImplAbstractClass obj) {
		for (String s : stringList) {
			if (!xmlValues.contains(s) && !categoryValues.contains(s)) {
				String opName = ctx.getStart().getText();
				String errMessage = obj.getErrorMessage(
						ParserErrorTypesEnum.MVK_INVALID_STRING_CONSTANT_FOR_ENUMERATION_OR_CATEGORY, opName, s);
				throw obj.createMVKParserError(ctx, errMessage);
			}
		}
	}

}
