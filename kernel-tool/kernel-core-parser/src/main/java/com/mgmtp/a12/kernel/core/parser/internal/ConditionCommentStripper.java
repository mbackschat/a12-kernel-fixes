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
package com.mgmtp.a12.kernel.core.parser.internal;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Full_ConditionLexer;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;

/**
 * Removes {@code ;;} comments from condition / operation expression texts of the A12 condition language.
 * <p>
 * The comment stripping combines the {@link Full_ConditionLexer} (which knows the real {@code Comment} token spans, and
 * therefore correctly ignores {@code ;;} occurring inside string literals) with a regex that handles the surrounding
 * whitespace nicely. This is the same logic that {@link ConditionFormatter#stripComments} uses; it is factored out here so
 * that callers that only have the raw expression text (plus its condition language) can strip comments without needing a
 * full {@code IServiceConfiguration} / {@code IRuleParser} / rule object.
 */
public final class ConditionCommentStripper {

	/* This pattern matches sub-comments. We split the comments at each ;; into sub-comments because we want each potential comment
	 * individually in case we have a mismatch regarding lexer terminals, e.g. because of ;; in string literals.
	 * See #getAllSubCommentPositions for further details.
	 * For multiline comments the intra comment line breaks are removed as start of the next part.
	 *
	 * The trivial regex would be "\s*;;[^\r\n]*", which does not split at occurrences of ;;.
	 * The negative lookahead "(?!\s*;;)" does the remaining splitting. The "\s*" in it is used to be able to remove the whitespace
	 * in front of any sub-comment even if the previous sub-comment was ignored.
	 */
	private static final Pattern COMMENT_PATTERN = Pattern.compile("\\s*(;;((?!\\s*;;)[^\\r\\n])*)");

	private ConditionCommentStripper() {}

	/**
	 * Removes all {@code ;;} comments from the given condition / operation expression text.
	 *
	 * @param conditionText
	 * 		the expression text to strip comments from. May be {@code null}, in which case {@code null} is returned.
	 * @param conditionLanguage
	 * 		the language the condition text is written in (needed to lex it correctly).
	 * @param pss
	 * 		the project specific settings used while lexing.
	 * @return the expression text without comments, trimmed; or {@code null} if {@code conditionText} was {@code null}.
	 */
	public static String stripComments(String conditionText, Locale conditionLanguage, IProjectSpecificSettings pss) {
		if (conditionText == null) {
			return null;
		}

		Set<Integer> commentPositions = getAllSubCommentPositions(conditionText, conditionLanguage, pss);

		// We combine pattern matching with lexer terminals to have nice whitespace handling.
		Matcher m = COMMENT_PATTERN.matcher(conditionText);
		// Only replace the regex, if the ;; is in a lexer comment.
		return m.replaceAll(r -> commentPositions.contains(r.start(1)) ? "" : r.group()).trim();
	}

	/**
	 * To be able to remove comments after string literals containing <code>;;</code>, the COMMENT_PATTERN splits the comments at
	 * every occurrence of <code>;;</code> to be able to throw away all mismatched <code>;;</code> occurrences separately if
	 * necessary.
	 * Get the comment positions as defined by the lexer and all positions of ;; inside (sub comment positions). These are exactly
	 * the positions, we want to remove comment parts found by the pattern.
	 */
	private static Set<Integer> getAllSubCommentPositions(String conditionText, Locale conditionLanguage,
			IProjectSpecificSettings pss) {
		CharStream input = CharStreams.fromString(conditionText);
		Full_ConditionLexer lexer = new Full_ConditionLexer(input);
		lexer.initLexer(conditionText, conditionLanguage, pss);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);

		tokenStream.seek(0);
		Set<Integer> commentPositions = new HashSet<>();
		Token token = tokenStream.LT(1);
		while (token.getType() != Token.EOF) {
			if (token.getType() == Base_ConditionLexer.Comment) {
				commentPositions.addAll(getSubCommentStartPositions(token));
			}
			tokenStream.consume();
			token = tokenStream.LT(1);
		}
		return commentPositions;
	}

	private static Set<Integer> getSubCommentStartPositions(Token token) {
		Set<Integer> result = new HashSet<>();
		int tokenPos = token.getStartIndex();
		String tokenText = token.getText();
		int idx = -1;
		while ((idx = tokenText.indexOf(";;", idx + 1)) >= 0) {
			result.add(tokenPos + idx);
		}
		return result;
	}
}
