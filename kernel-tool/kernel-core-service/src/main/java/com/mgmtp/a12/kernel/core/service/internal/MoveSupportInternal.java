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

package com.mgmtp.a12.kernel.core.service.internal;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.EntityIdFormatterUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextUtil;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.BaseErrorTextParameterLexer;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.languages.ErrortextParameterMultiLanguageHandler;

import lombok.NonNull;

/**
 * Support for updating rules (and computations?) when fields, groups or rules are moved or renamed.
 * Aims to do sensible updates even when rules are not fully valid, and does not guarantee that resulting rules are fully valid even
 * if they were valid before the move, e.g. when repeatabilities don't match afterwards.
 */
public class MoveSupportInternal {

	// NOTE: lets see if we can keep this pseudo-static

	// NOTE: computations: see MoveSupportMDImplInternal
	// NOTE: high-level api: see MoveSupportDM

	// make sure that nothing depends on the constructor directly, in case we need to extract an interface later
	protected MoveSupportInternal() {
	}

	public static MoveSupportInternal getInstance() {
		return new MoveSupportInternal();
	}

	/**
	 * Scope & Limitations</br>
	 * <ol>
	 *   <li>absolute vs relative path variant will be preserved if possible</li>
	 *   <li>validity before or after is <em>not</em> checked or guaranteed. If appropriate, tools should use the existing
	 *   kernel api to inform the user about potentially invalid DMs after the move and before saving, if desired.</li>
	 *   <li>We do not check if a referenced field, group or rule exists, only if the path format is correct.</li>
	 *   <li>We do not detect whether a name is a single-element relative path or a shortname. All such references are treated as
	 *   single-element relative paths. Note: shortnames do not exist in A12 (as of 2023-03-29) (except in error text parameters,
	 *   e.g. for the 'index(shortname)' construct !)
	 *   and ExtMP's A12-Export converts shortnames to regular paths.</li>
	 *   <li>The parser assumes that we are in an A12 DV (not H-DV). Note: even if the parser is reconfigured, it will not
	 *   currently detect slash-less references to "Vordrucke"</li>
	 *   <li>Entity path specifications containing starred ('*') groups are adjusted as well,
	 *   	if each starred group of the current path is also contained in the resulting full path specification of
	 *   	the entity after the move. Otherwise, they remain untouched.
	 *   </li>
	 *   <li>If a group is specified to be moved, the move applies to all groups and fields within that group too, unless there
	 *   is a more specific move that applies to the subgroup or field.
	 *   </li>
	 * </ol>
	 *
	 * @param ruleConditionText
	 * 		the rule condition text before the move
	 * @param ruleCurrentFullname
	 * 		the full path and name of the rule before the move
	 * @param entityMoves
	 * 		a map String -> String before & after fullNames of the moved rules, fields and groups
	 * @return the condition text with entity references updated
	 */
	public String changeConditionTextForMove(
			String ruleConditionText,
			@NonNull String ruleCurrentFullname,
			@NonNull Map<String, String> entityMoves,
			@NonNull Locale conditionLanguage
	) {
		return changeConditionSnippetForMove(
				GrammarOnly_ConditionParser::bedingung_start,
				ruleConditionText,
				ruleCurrentFullname,
				entityMoves,
				conditionLanguage
		);
	}

	protected String changeConditionSnippetForMove(
			@NonNull Function<GrammarOnly_ConditionParser, ParseTree> grammarRule,
			String snippet,
			@NonNull String ruleCurrentFullname,
			@NonNull Map<String, String> entityMoves,
			@NonNull Locale conditionLanguage
	) {

		if (StringUtils.isEmpty(snippet)) {
			return snippet;
		}
		if (StringUtils.isEmpty(ruleCurrentFullname)) {
			throw new IllegalArgumentException("Expected nonempty rule current fullname (but was empty).");
		}
		ConditionMultiLanguageHandler mlh =
				new ConditionMultiLanguageHandler(conditionLanguage);
		List<MoveSupportParseTreeListener.EntityReference> entityReferences;
		try {
			entityReferences = findEntityReferencesWithConditionGrammar(grammarRule, snippet, ruleCurrentFullname, mlh);
		} catch (Exception e) {
			assert false : "The lenient parser is not expected to generate errors, but we got " + e;
			entityReferences = List.of();
		}
		return updateEntityPathsIn(
				snippet,
				ruleCurrentFullname,
				entityReferences,
				entityMoves,
				(eRef, newEntityFullName, newRuleFullName, starredGroupsNewFullnames)
						-> updatedReferenceString4ConditionGrammar(eRef, newEntityFullName, newRuleFullName,
						starredGroupsNewFullnames, mlh)
		);
	}

	/**
	 * Similar to {@link #changeConditionTextForMove(String, String, Map, Locale)}
	 */
	public Map<Locale, String> changeErrorTextsForMove(
			@NonNull Map<Locale, String> errorTexts,
			@NonNull String ruleCurrentFullname,
			@NonNull Map<String, String> entityMoves,
			@NonNull Locale conditionLanguage
	) {
		return Java8Utils.mapMapValuesPreserveOrder(errorTexts, (String errorText) ->
				FehlertextUtil.mapParamsInFehlertext(errorText, (String param) -> {

					ErrortextParameterMultiLanguageHandler mlh = new ErrortextParameterMultiLanguageHandler(conditionLanguage);

					List<MoveSupportParseTreeListener.EntityReference> entityReferences;
					try {
						entityReferences = findEntityReferencesWithErrorTextParamGrammar(param, ruleCurrentFullname, mlh);
					} catch (Exception e) {
						assert false : "The lenient parser is not expected to generate errors, but we got " + e;
						entityReferences = List.of();
					}

					return updateEntityPathsIn(
							param,
							ruleCurrentFullname,
							entityReferences,
							entityMoves,
							(eRef, newEntityFullName, newRuleFullName, starredGroupsNewFullnames) ->
									updatedReferenceString4ErrTexGrammar(eRef,
											newEntityFullName,
											newRuleFullName, starredGroupsNewFullnames, mlh)
					);
				})
		);
	}

	private interface MkUpdatedReferenceString {
		String apply(MoveSupportParseTreeListener.EntityReference e, String newEntityFullName, String newRuleFullName,
				Set<String> starredGroupNewFullnames);
	}

	private boolean fullNameHasAllStarredGroupFullnamesAsAncestors(String newEntityFullName, Set<String> starredGroupNewFullnames) {
		var pathParts = new HashSet<>(getPathPartsAsFullNames(newEntityFullName));
		return pathParts.containsAll(starredGroupNewFullnames);
	}

	private String updateEntityPathsIn(
			String snippet,
			@NonNull String ruleCurrentFullname,
			@NonNull List<MoveSupportParseTreeListener.EntityReference> entityReferences,
			@NonNull Map<String, String> entityMoves,
			MkUpdatedReferenceString mkNewReferenceString
	) {
		String newRuleFullname = findNewFullname(ruleCurrentFullname, entityMoves);
		StringBuilder newSnippet = new StringBuilder(snippet);

		entityReferences.stream()
				.sorted((a, b) -> Integer.compare(b.rangeInCondition().start(), a.rangeInCondition().start()))
				.forEachOrdered(eRef -> {

					String newEntityFullName = findNewFullname(eRef.fullName(), entityMoves);
					if (!eRef.fullName().equals(newEntityFullName) || !ruleCurrentFullname.equals(newRuleFullname)) {
						Set<String> starredGroupNewFullnames = findNewFullNames(eRef.starredGroupFullNames(), entityMoves);
						if (fullNameHasAllStarredGroupFullnamesAsAncestors(newEntityFullName,
								starredGroupNewFullnames)) {
							String newEntityRefString = mkNewReferenceString.apply(eRef, newEntityFullName, newRuleFullname,
									starredGroupNewFullnames);

							String normalizedOldRefString = mkNewReferenceString.apply(eRef, eRef.fullName(), ruleCurrentFullname,
									eRef.starredGroupFullNames());

							// we only update the reference if new (normalized) new reference String differs from the normalized
							// previous reference string:
							//   Background:
							//   the update may be not-lossless, e.g. if the reference String in the condition had a non-standard
							//   format (e.g. line-breaks between elements of the path), or implicit vs explicit turning points
							// tested in "Paths that can be unchanged are not touched (relpaths & abspaths)" (TestMoveSupportDMInternal)
							if (!newEntityRefString.equals(normalizedOldRefString)) {
								int stopIndex = eRef.rangeInCondition()
										.endExclusive();

								newSnippet.replace(
										eRef.rangeInCondition().start(), stopIndex,
										newEntityRefString);
							}
						}//else: some starredGroupNewFullname is no ancestor of  newEntityFullName: we cannot safely update the
						// reference and leave it untouched
					}
				});

		return newSnippet.toString();
	}

	private List<MoveSupportParseTreeListener.EntityReference> findEntityReferencesWithConditionGrammar(
			Function<GrammarOnly_ConditionParser, ParseTree> grammarRule,
			String snippet,
			String ruleCurrentFullname,
			ConditionMultiLanguageHandler mlh
	) {
		CharStream input = CharStreams.fromString(snippet);
		Base_ConditionLexer lexer = new Base_ConditionLexer(input);

		lexer.initLexer(mlh);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		GrammarOnly_ConditionParser baseParser = new GrammarOnly_ConditionParser(tokens);
		baseParser.initParser(false, false, null);
		baseParser.removeErrorListener(ConsoleErrorListener.INSTANCE); // don't print syntax errors to stderr

		ParseTree parsed = grammarRule.apply(baseParser);

		ParseTreeWalker walker = new ParseTreeWalker();
		MoveSupportParseTreeListener tc = new MoveSupportParseTreeListener(EntityUtils.getParentPath(ruleCurrentFullname));
		walker.walk(tc, parsed);

		return tc.getCollectedReferences();
	}

	private List<MoveSupportParseTreeListener.EntityReference> findEntityReferencesWithErrorTextParamGrammar(
			String param,
			String ruleCurrentFullname,
			ErrortextParameterMultiLanguageHandler mlh
	) {
		CharStream input = CharStreams.fromString(param);
		BaseErrorTextParameterLexer lexer = new BaseErrorTextParameterLexer(input);
		lexer.initLexer(mlh);

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		GrammarOnly_ErrorTextParameterParser baseParser = new GrammarOnly_ErrorTextParameterParser(tokens);
		baseParser.initParser(false, null);
		baseParser.removeErrorListener(ConsoleErrorListener.INSTANCE); // don't print syntax errors to stderr

		GrammarOnly_ErrorTextParameterParser.ParameterContext parsed = baseParser.parameter();

		ParseTreeWalker walker = new ParseTreeWalker();
		MoveSupportErrorTextParseTreeListener listener = new MoveSupportErrorTextParseTreeListener(EntityUtils.getParentPath(
				ruleCurrentFullname));
		walker.walk(listener, parsed);

		return listener.getCollectedReferences();
	}

	/**
	 * Checks if the entity at fullName, or any of its ancestors (parent groups, grandparent groups, etc.) occurs in entityMoves
	 * and returns the updated fullName with the most specific matching move applied.
	 */
	protected static String findNewFullname(@NonNull String fullName, @NonNull Map<String, String> entityMoves) {
		Validate.isTrue(fullName.startsWith("/"));
		String prefix = fullName;
		String newPrefix;
		String suffix;
		while (!prefix.isEmpty()) {
			if (entityMoves.containsKey(prefix)) {
				newPrefix = entityMoves.get(prefix);
				suffix = fullName.substring(prefix.length());
				return newPrefix + suffix;
			}
			prefix = prefix.substring(0, prefix.lastIndexOf(EntityUtils.SEPARATOR));
		}
		return fullName;
	}

	public static SequencedSet<String> findNewFullNames(@NonNull Set<String> oldFullNames,
			@NonNull Map<String, String> entityMoves) {
		var retVal = new LinkedHashSet<String>();
		for (var oldFullName : oldFullNames) {
			var oldOrNewFullName = findNewFullname(oldFullName, entityMoves);

			if (!retVal.add(oldOrNewFullName)) {
				assert false : "MoveSupport found conflicting entity move, more than one entity moved to  " + oldOrNewFullName;
			}
		}
		return retVal;
	}

	/**
	 * @return an array starIndices encoding what subpaths are starred, starIndices[i] == 1L => no star at path part i
	 * 		,starIndices[i] ==  0 => star at path part i
	 * 		(encoded as 0 and 1 rather than boolean (and 0 == starred rather than the other way round) for historic reasons)
	 */
	private static long[] computeStarsArray(String newEntityFullName, Set<String> starredGroupNewFullnames) {
		var nStarsToSpecify = ElementPathUtils.getNumberOfPathParts(newEntityFullName);
		var retVal = new long[nStarsToSpecify];

		List<String> pathParts = getPathPartsAsFullNames(newEntityFullName);

		for (int i = 0; i < nStarsToSpecify; ++i) {
			var subPath = pathParts.get(i);
			if (starredGroupNewFullnames.contains(subPath))
				retVal[i] = 0L;
			else
				retVal[i] = 1L;
		}
		return retVal;
	}

	private static List<String> getPathPartsAsFullNames(String newEntityFullName) {
		List<String> pathParts = ElementPathUtils.getAncestorPaths(newEntityFullName);
		pathParts.add(newEntityFullName);
		return pathParts;
	}

	private static String updatedReferenceString4ConditionGrammar(MoveSupportParseTreeListener.EntityReference entityRef,
			String newEntityFullName,
			String newRuleFullName,
			Set<String> starredGroupNewFullnames,
			ConditionMultiLanguageHandler mlh
	) {

		long[] starIndices = computeStarsArray(newEntityFullName, starredGroupNewFullnames);
		PathNotationConfig pathNotation = derivePathNotationConfig(entityRef);
		boolean isOuter = false;  // we can set isOuter to false here, because MoveSupportParseTreeListener and
		// MoveSupportErrorTextParseTreeListener listen not for entitySpec, but for regelKontextEntity and entity, which
		// do not include outer ("$") -> the computed replacement range will not include the dollar symbol
		return EntityIdFormatterUtil.formatEntityIdForConditionTextA12DV(
				newEntityFullName,
				starIndices,
				isOuter,
				pathNotation,
				EntityUtils.getParentPath(newRuleFullName),
				shortName -> false,
				mlh,
				false,
				false
		);
	}

	private static String updatedReferenceString4ErrTexGrammar(
			MoveSupportParseTreeListener.EntityReference entityRef,
			String newEntityFullName,
			String newRuleFullName,
			Set<String> starredGroupsNewFullnames,
			ErrortextParameterMultiLanguageHandler mlh
	) {
		assert starredGroupsNewFullnames.isEmpty() : "stars are not possible in error text grammar";
		long[] starIndices = computeStarsArray(newEntityFullName, starredGroupsNewFullnames);
		PathNotationConfig pathNotation = derivePathNotationConfig(entityRef);
		return EntityIdFormatterUtil.formatEntityIdErrTextParamA12DV(
				newEntityFullName,
				starIndices,
				pathNotation,
				EntityUtils.getParentPath(newRuleFullName),
				shortName -> false,
				mlh,
				GrammarOnly_ErrorTextParameterParser.RULE_GROUP
		);
	}

	private static PathNotationConfig derivePathNotationConfig(MoveSupportParseTreeListener.EntityReference e) {
		PathNotationConfig pathNotation = e.isAbsolute() ? PathNotationConfig.ABSPATH : PathNotationConfig.RELATIVE;
		return pathNotation.withHasExplicitTurningPoint(e.hasExplicitTurningPoint());
	}
}
