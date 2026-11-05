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

package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util;

import static com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig.Type.ABSOLUTE;
import static com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig.Type.RELATIVE;
import static com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig.Type.SHORTNAME;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckRelUpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.common.grammar.languages.AbstractMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;

public class EntityIdFormatterUtil {
	private EntityIdFormatterUtil() {
		throw new RuntimeException("static utility class");
	}

	/**
	 * Formats the entityId as a path for a condition text.
	 * The path notation variant is determined by several factors<br/>
	 * <ul>
	 * <li>The {@link IEntityId#getPathNotationConfig()} (which typically represents either
	 * the original path type, if the entityId has been parsed from a condition text,
	 * and/or may additionally contain some overridden preference supplied by a formatter)</li>
	 * <li>Constraints on legal notation types by the grammar, not all path types are allowed in the condition grammar in all contexts:<br/>
	 *   <ul><li>E.g. absolute paths into the rule group are currently not allowed unless they contain stars at levels above the rule
	 *   group (or even only at the root group level)</li>
	 *   <li>shortNames are only allowed if the name is unique in the DVL and if shortnames are allowed in the DVL at all</li>
	 *   <li>explicit turning points for relative paths are only allowed if the turning point is not above the root group level.</li>
	 *   </ul></li>
	 * </ul>
	 * Note that even if an entityId has been parsed from a condition text and its pathNotationConfig has not been modified and the
	 * entity has not been renamed or moved, the formatted path does still not need to equal its original string representation
	 * in the condition text, because e.g. rendundant line-breaks or spaces will not be included in the formatted path.<br/>
	 * This method has no knowledge of the original condition text, and is indeed used, in part, for entities that do not originate
	 * from a parsed condition text.
	 * <p/>
	 * NOT included in the formatted result:
	 * <ul>
	 *     <li>a semantic index specification that may exist on the entityId</li>
	 *     <li>a filter condition that may exist on the entityId</li>
	 *     <li>any categoryName that may exist on the entityId</li>
	 * </ul>
	 *
	 * @param entityId
	 * @param ruleGroupFullName
	 * 		if the path is formatted as a relative path, it will be relative to the rule group path (the parent of the rule path)
	 * @param isShortNameAllowedAndUnique
	 * 		a predicate that tells if shortNames are allowed in the DVL, and if a given shortName is unique.
	 * @param mlh
	 * @return the formatted path (absolute, relative or shortname, quoted as necessary by mlh). Without semantic index or filter
	 * 		condition
	 */
	public static String formatEntityIdForConditionText(
			IEntityId entityId,
			String ruleGroupFullName,
			Predicate<String> isShortNameAllowedAndUnique,
			ConditionMultiLanguageHandler mlh
	) {
		if (entityId.isHmodel()) { // don't refactor H-DV stuff, that is not worth the effort. We delegate to the H-DV part of the old
			// asString method instead.
			//
			// NOTE: isOuter is not supported for H-DVs (see CheckEntitySpecImpl#checkOuter MVK_INVALID_OPERATION_IN_H_MODEL),
			// so we don't need to handle it here, even after the responsibility for
			// the $-sign in outer references has been moved to this method.
			return FeldIdUtil.asStringHDVOnly(entityId, ruleGroupFullName + "/rulenameplaceholder", mlh);
		}
		return formatEntityIdForConditionTextA12DV(
				entityId.getFullName(), entityId.getIndices(), entityId.isOuter(),
				entityId.getPathNotationConfig(),
				ruleGroupFullName,
				isShortNameAllowedAndUnique,
				mlh,
				false,
				false
		);
	}

	/**
	 * see {@link #formatEntityIdForConditionText(IEntityId, String, Predicate, ConditionMultiLanguageHandler)}
	 *
	 * <em>Note</em> this method does something similar as MoveSupportInternal#updatedReferenceString (see
	 * comment in that method)
	 */
	public static String formatEntityIdForConditionTextA12DV(
			String entityFullName,
			long[] entityIndices,
			boolean isOuter,
			PathNotationConfig pathNotationPreferences,
			String ruleGroupFullName,
			Predicate<String> isShortNameAllowedAndUnique,
			ConditionMultiLanguageHandler mlh,
			boolean suppressStars,
			// only for compatibility with previous behavior (note that upward steps that are required for stars are still inserted, only the actual stars are suppressed
			boolean suppressPathNotationFiltering
	) {
		return formatEntityIdForConditionTextOrErrTextParamA12DV(
				entityFullName, entityIndices, isOuter, pathNotationPreferences, ruleGroupFullName,
				isShortNameAllowedAndUnique,
				mlh,
				GrammarOnly_ConditionParser.REGEL_KONTEXT,
				suppressStars,
				suppressPathNotationFiltering
		);
	}

	/**
	 * Note: the focus of the implementation is currently the condition grammar - incorrectly handled edge-cases may be more likely
	 * for the error text parameter grammar.
	 * For the condition grammar, all path-formatting should delegate to this method, as a single source of truth.
	 * However, this is not the case for the error text parameter formatting (which was not refactored) - only
	 * MoveSupportInternal#updatedReferenceString delegates to this method, because it already used common code for both grammars
	 * before the refactoring.
	 */
	private static String formatEntityIdForConditionTextOrErrTextParamA12DV(
			String entityFullName,
			long[] entityIndices,
			boolean isOuter,
			PathNotationConfig pathNotationPreferences,
			String ruleGroupFullName,
			Predicate<String> isShortNameAllowedAndUnique,
			AbstractMultiLanguageHandler mlh,
			final int RULE_GROUP_PARSER_CONSTANT,
			boolean suppressStars,
			// only for compatibility with previous behavior (note that upward steps that are required for stars are still inserted, only the actual stars are suppressed
			boolean suppressPathNotationFiltering
	) {
		pathNotationPreferences = suppressPathNotationFiltering ?
				pathNotationPreferences // for parser error messages, illegal notation types do occur and must be formatted (e.g.
				// their illegality may be the very reason for the condition parser error message)
				: pathNotationPreferences.withFilteredPreferences(notationType -> isPathNotationTypeLegalInConditionOrErrTxtParam(
				notationType, entityFullName, entityIndices, ruleGroupFullName, isShortNameAllowedAndUnique));

		PathWithoutStarsWithLevelInfo unquotedPathWithoutStars = switch (pathNotationPreferences.firstChoice()) {
			case SHORTNAME -> mkShortnameWithoutStars(entityFullName);
			case RELATIVE -> mkRelativePathWithoutStars(entityFullName, entityIndices, ruleGroupFullName, pathNotationPreferences);
			case ABSOLUTE -> mkAbsolutePathWithoutStars(entityFullName);
		};

		String reconstructedPath = FeldIdUtil.quoteNameIfNecessary(unquotedPathWithoutStars.path, mlh);

		// NOTE: the entity names that are used in condition parser error messages do not currently contain stars, even if the
		// entity is referenced with wildcards in the condition.
		// [EXTP-3740] NOTE: we could consider removing this special-case and always add stars for wildcards, including in
		// parser error messages. This would make some messages easier to understand and others maybe more confusing. Such a change
		// would also affect automated tests, because the expected messagse would slightly change.
		if (!suppressStars) {
			reconstructedPath = addStars4WildcardsA12DV(
					reconstructedPath,
					entityIndices,
					unquotedPathWithoutStars.indexOfFirstDownwardStepInPath,
					unquotedPathWithoutStars.levelOfFirstDownwardStepInPath);
		}

		if ("".equals(reconstructedPath)) { // RegelKontext/RuleGroup is basically a special notation for the emtpy relative path
			reconstructedPath = mlh.getTerminalName(RULE_GROUP_PARSER_CONSTANT);
		}

		return (isOuter ? "$" : "") + reconstructedPath;
	}

	/**
	 * like
	 * {@link #formatEntityForCondationParserMessageA12DV(String, String, List, PathNotationConfig, ConditionMultiLanguageHandler)}
	 * but assumes that all indices are 1 (0 indices can otherwise result in some paths beeing formatted as absolute that would
	 * otherwise be formatted as relative)
	 */
	public static String formatEntityForCondationParserMessageA12DV(
			String rulePath,
			String entityPath,
			PathNotationConfig pathNotationConfig,
			ConditionMultiLanguageHandler mlh
	) {
		List<Long> noStarIndices = Collections.nCopies(EntityUtils.getLevel(entityPath) + 1, 1L);
		return formatEntityForCondationParserMessageA12DV(rulePath, entityPath, noStarIndices, pathNotationConfig, mlh);
	}

	/**
	 * Creates a path-like string representation for an Entity (rathen an entity than an entityId) that is suitable for condition
	 * parser error messages.
	 * The path will be absolute or relative depending on simple criteria (mostly <em>not</em> controlled by
	 * origEntityIdPathNotation).
	 * The path will not contain any stars that may occur in the actual condition text.
	 */
	public static String formatEntityForCondationParserMessageA12DV(
			String rulePath,
			String entityPath,
			List<Long> entityIndices,
			PathNotationConfig origEntityIdPathNotation,
			ConditionMultiLanguageHandler mlh
	) {
		PathNotationConfig pathNotation = pathNotationForConditionParserMessageA12DV(
				entityPath, entityIndices, rulePath, origEntityIdPathNotation);
		return formatEntityIdForConditionTextA12DV(
				entityPath,
				Java8Utils.toLongArray(entityIndices),
				false,
				pathNotation,
				EntityUtils.getParentPath(rulePath),
				(shortName) -> false,
				mlh,
				true,
				true
		);
	}

	/**
	 * Attempts to format an entity id (path) in a way that is valid for an error text parameter reference.
	 * <br>
	 * Works similar to {@link #formatEntityIdForConditionText(IEntityId, String, Predicate, ConditionMultiLanguageHandler)}
	 */
	public static String formatEntityIdErrTextParamA12DV(
			String entityFullName,
			long[] entityIndices,
			PathNotationConfig pathNotationPreferences,
			String ruleGroupFullName,
			Predicate<String> isShortNameAllowedAndUnique,
			AbstractMultiLanguageHandler mlh,
			final int RULE_GROUP_PARSER_CONSTANT
	) {
		return formatEntityIdForConditionTextOrErrTextParamA12DV(
				entityFullName,
				entityIndices,
				false,
				pathNotationPreferences,
				ruleGroupFullName,
				isShortNameAllowedAndUnique,
				mlh,
				RULE_GROUP_PARSER_CONSTANT,
				false,
				false
		);
	}

	/**
	 * determines a PathNotationConfig for formatting of an entityId for a condition-parser error message. Note that it does not
	 * in general make sense to preserve the PathNotationConfig of some original entity, because for error messages it is sometimes
	 * required to format entityIds that do not exist explicitly in the condition.<br/>
	 * Example:
	 * Rule group: /A/B/C
	 * Referenced entity: D/f1 (relative reference below the rule group)
	 * Parser Error: D/f1 lacks a star on /A/B (at the root group level)
	 * Here, a relative reference to B ("..") makes no sense in the error message (because ".." doesn't even occur in the
	 * condition),
	 * "/A/B" is more readable.
	 */
	private static PathNotationConfig pathNotationForConditionParserMessageA12DV(
			String entityPath,
			List<Long> entityIndices,
			String rulePath,
			PathNotationConfig origEntityIdPathNotation
	) {
		Set<Integer> wildcardLevelsOnRulePath = EntityUtils.getWildcardLevelsOnRulePathA12Model(
				entityPath, entityIndices, rulePath);
		boolean hasWildcardOnOrAboveRuleGroupLevel = wildcardLevelsOnRulePath.stream()
				.anyMatch(i -> i <= EntityUtils.getLevel(rulePath) - 1);
		String rulePathWithSlash = EntityUtils.getParentPathWithSlash(rulePath);

		// [EXTP-3740] NOTE: the special-case treatment of relative upward paths seems somewhat arbitrary.
		// We could consider removing the fist if branch here. This would slightly change the text of some parser error messages,
		// therefore it could break tests. For the user, the messages may become less confusing in some cases, and less helpful in
		// other cases.
		if (origEntityIdPathNotation.isLabeledAsRelativeUpwardReference()) {
			return origEntityIdPathNotation.withPreferRelPath(); // preserve any additional relative-path specific info (and make sure that RELATIVE is the preferred notation type)
		} else if (entityPath.startsWith(rulePathWithSlash) && !hasWildcardOnOrAboveRuleGroupLevel) {
			// this can happen e.g. for the error field
			return new PathNotationConfig(RELATIVE, false, false);
		} else {
			// Note: this override produces maybe-surprising messages like:
			// Fehler für '/Lx_Ux' in 'Kx/zahlen/zahl_1': in Filterbedingungen darf nur über die Wiederholungsebenen iteriert werden, auf denen das gefilterte Feld Iteration oder Stern spezifiziert. [MVK_INVALID_ITERATION_IN_FILTER_CONDITION]
			// Note: a relative path would *not* be better here, because /Lx_Ux simply never occurs in the relevant part of the condition text
			return PathNotationConfig.ABSPATH;
		}
	}

	/**
	 * Note: accuracy for the error text parameter grammar may be limited, this method is primarily intended for the condition
	 * grammar,
	 * but is probably better than most ad-hoc approximations for the errortext parameter grammar that could be replaced by this
	 */
	private static boolean isPathNotationTypeLegalInConditionOrErrTxtParam(
			PathNotationConfig.Type notationType,
			String entityFullName,
			long[] entityIndices,
			String ruleGroupFullName,
			Predicate<String> isShortNameAllowedAndUnique
	) {
		if (notationType == SHORTNAME) {
			return isShortNameAllowedAndUnique.test(EntityUtils.getShortName(entityFullName))
					&& Arrays.stream(entityIndices).noneMatch(l -> l == IParseTreeEntity.WILDCARD);
		}
		RequiredRelUpLevels relPathConstraints = getRequiredRelativeUpwardReferenceLevels(entityFullName, entityIndices,
				ruleGroupFullName);
		if (notationType == ABSOLUTE) {
			return true;
		} else {
			assert notationType == RELATIVE : notationType;
			// A12K-4276: the turning point may climb up to the technical DocumentModelRoot (level -1) so a relative path can reach a
			// sibling root group ("../../RG2/x"); see CheckRelUpImpl.MIN_LEVEL_FOR_UPWARD_PATHS.
			return relPathConstraints.turingPointLevel() >= CheckRelUpImpl.MIN_LEVEL_FOR_UPWARD_PATHS;
		}
	}

	/**
	 * @param path
	 * 		abspath, relative path or shortname
	 * @param indexOfFirstDownwardStepInPath
	 * 		-1 if not applicable or the index in the "/"-separated parts of path that
	 * 		represents the first downward step (0 for abspaths and relative
	 * 		downward paths, > 0 for relative paths with upward steps)
	 * @param levelOfFirstDownwardStepInPath
	 * 		the level of the entity that corresponds to the first downward step
	 */
	private static record PathWithoutStarsWithLevelInfo(
			String path,
			int indexOfFirstDownwardStepInPath,
			int levelOfFirstDownwardStepInPath) {
	}

	private static PathWithoutStarsWithLevelInfo mkShortnameWithoutStars(String fullName) {
		return new PathWithoutStarsWithLevelInfo(
				EntityUtils.getShortName(fullName),
				-1, -1);
	}

	private static PathWithoutStarsWithLevelInfo mkRelativePathWithoutStars(
			String entityFullName,
			long[] entityIndices,
			String ruleGroupFullName,
			PathNotationConfig notationOptions
	) {
		RequiredRelUpLevels relPathConstraints = getRequiredRelativeUpwardReferenceLevels(entityFullName, entityIndices,
				ruleGroupFullName);
		// this should work for both relative downward and relative up&down paths
		int relativeUpLevels = relPathConstraints.relUpLevels();
		int commonAncestorLevel = relPathConstraints.turingPointLevel();
		// commonAncestorLevel will usually be >= 0,
		// but for paths in parser messages, this may not be the case (because those may be illegal paths)

		// In general, a turning point at root group level is allowed to be explicit (only in the entitypathsrelativizer we require
		// commonAncestorLevel (=turning point level) > 0)
		boolean canBeRelpathWithExplTurningPoint = commonAncestorLevel >= 0;

		var pathSpilt = EntityUtils.splitPathBelowLevel(commonAncestorLevel, entityFullName);
		String ancestorPath = pathSpilt.getLeft();
		String restName = pathSpilt.getRight();
		String turningPointNameOrEmpty = (notationOptions.hasExplicitTurningPoint() && canBeRelpathWithExplTurningPoint && relativeUpLevels > 0)
				? EntityUtils.getShortName(ancestorPath)
				: "";
		String relUpPath = makeUpwardPath(relativeUpLevels) + turningPointNameOrEmpty;

		return new PathWithoutStarsWithLevelInfo(
				joinNonEmpty(relUpPath, restName, "/"),
				relativeUpLevels,
				commonAncestorLevel + 1
		);
	}

	private static PathWithoutStarsWithLevelInfo mkAbsolutePathWithoutStars(String fullName) {
		return new PathWithoutStarsWithLevelInfo(fullName, 0, 0);
	}

	/**
	 * see {@link #getRequiredRelativeUpwardReferenceLevels(String, long[], String)}
	 *
	 * @param entityId
	 * @param ruleGroupFullName
	 * @return
	 */
	public static RequiredRelUpLevels getRequiredRelativeUpwardReferenceLevels(IEntityId entityId, String ruleGroupFullName) {
		return getRequiredRelativeUpwardReferenceLevels(entityId.getFullName(), entityId.getIndices(), ruleGroupFullName);
	}

	/**
	 * Computes the number of upward steps required for a relative path, and the level of the resulting turning point.
	 * Upward steps can be necessary for two reasons:
	 * <ul>
	 *     <li>in order to reach the nearest common ancestor of ruleGroupFullName and entityFullName</li>
	 *     <li>in order to get above the first wildcard level, so that there is a downward path step for the wildcard level to which
	 *         a "*" can be added to represent the wildcard</li>
	 * </ul>
	 */
	public static RequiredRelUpLevels getRequiredRelativeUpwardReferenceLevels(String entityFullName, long[] entityIndices,
			String ruleGroupFullName) {
		int ruleGroupLevel = EntityUtils.getLevel(ruleGroupFullName);
		// the path needs to go up above the level of the first wildcard, so that the wildcard can be specified (in the first
		// downward step of the path). (Wildcards cannot be specified in upward steps of paths).
		int levelBeforeFirstWilcard = EntityUtils.getLevelBeforeFirstWildcard(entityIndices); // wrong result
		int commonPrefixLevel = EntityUtils.getCommonAncestorLevel(entityFullName, ruleGroupFullName);
		int turningPointLevel = Integer.min(levelBeforeFirstWilcard, commonPrefixLevel);
		int relUpLevels = ruleGroupLevel - Integer.min(turningPointLevel, ruleGroupLevel);
		return new RequiredRelUpLevels(relUpLevels, turningPointLevel);
	}

	/**
	 * @param relUpLevels
	 * 		the number of upward steps in the relative path (can be 0)
	 * @param turingPointLevel
	 * 		the level of the turning point or common ancestor (0-based, i.e. 0 corresponds to a root group)
	 */
	public record RequiredRelUpLevels(int relUpLevels, int turingPointLevel) {
	}

	public static String makeUpwardPath(int levels) {
		assert levels >= 0;
		if (levels <= 0) {
			return "";
		}
		return StringUtils.repeat(EntityUtils.PATH_UP + "/", levels - 1) + EntityUtils.PATH_UP;
	}

	private static String addStars4WildcardsA12DV(String pathWithoutStars, long[] repetitions, int indexOfFirstDownwardStepInPath,
			int levelOfFirstDownwardStepInPath) {
		if (indexOfFirstDownwardStepInPath == -1 || levelOfFirstDownwardStepInPath == -1) {
			// special parameter values that indicate that the path has *no* downward steps where stars could be added
			assert !Java8Utils.contains(repetitions, IParseTreeEntity.WILDCARD);
			return pathWithoutStars;
		}
		if (!Java8Utils.contains(repetitions, IParseTreeEntity.WILDCARD)) {
			return pathWithoutStars;
		}

		String[] teile = EntityUtils.getPathParts(pathWithoutStars);
		boolean isAbsPath = pathWithoutStars.startsWith("/");
		for (int i = indexOfFirstDownwardStepInPath; i < teile.length; i++) {
			if (repetitions[levelOfFirstDownwardStepInPath - indexOfFirstDownwardStepInPath + i] == IParseTreeEntity.WILDCARD) {
				teile[i] = teile[i] + "*";
			}
		}
		return (isAbsPath ? "/" : "") + StringUtils.join(teile, "/");
	}

	private static String joinNonEmpty(String path1, String path2, String delimiter) {
		assert !path1.endsWith(delimiter);
		assert !path2.startsWith(delimiter);
		if (StringUtils.isEmpty(path1)) {
			return path2;
		}
		if (StringUtils.isEmpty(path2)) {
			return path1;
		}
		return path1 + delimiter + path2;
	}
}
