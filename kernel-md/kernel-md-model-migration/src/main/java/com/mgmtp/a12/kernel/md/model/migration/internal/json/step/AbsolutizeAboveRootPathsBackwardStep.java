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

package com.mgmtp.a12.kernel.md.model.migration.internal.json.step;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.EntityIdFormatterUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.service.internal.MoveSupportParseTreeListener;
import com.mgmtp.a12.kernel.core.service.internal.MoveSupportParseTreeListener.EntityReference;
import com.mgmtp.a12.kernel.core.service.internal.MoveSupportParseTreeListener.Range;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationInfo;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * A12K-4276: Backward migration step. Rewrites relative paths in conditions that climb above the root groups - up to
 * the technical document model root - into their equivalent absolute form, because older document model versions
 * reject them with {@code MVK_TOO_MANY_PARENT_GROUPS}. The absolute notation means the same thing and is valid in old
 * and new versions alike.
 * <p>
 * References are collected with the grammar-only condition parser and {@link MoveSupportParseTreeListener}, as in the
 * move support, and rewritten by re-rendering them with {@link PathNotationConfig#ABSPATH} - compare
 * {@code MoveSupportInternal#derivePathNotationConfig}.
 * <p>
 * Error texts are deliberately not touched: the A12 export does not produce above-root relative paths in error text
 * parameters.
 */
public final class AbsolutizeAboveRootPathsBackwardStep {

	private static final String ERROR_CONDITION = "errorCondition";
	private static final String COMMON_PRECONDITION = "commonPrecondition";
	private static final String COMPUTATION_ALTERNATIVES = "computationAlternatives";
	private static final String PRECONDITION = "precondition";
	private static final String OPERATION = "operation";

	private AbsolutizeAboveRootPathsBackwardStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, ICustomMigrationConfig customMigConfig) {
		JsonNode content = model.path(ModelJsonUtil.CONTENT);
		JsonNode modelRoot = content == null ? null : content.get(ModelJsonUtil.MODEL_ROOT);
		JsonNode rootGroups = modelRoot == null ? null : modelRoot.get(ModelJsonUtil.ROOT_GROUPS);
		Locale conditionLanguage = ModelJsonUtil.extractConditionLanguage(model).orElse(null);
		if (rootGroups == null || !rootGroups.isArray() || conditionLanguage == null) {
			return;
		}
		ConditionMultiLanguageHandler mlh =
				new ConditionMultiLanguageHandler(conditionLanguage);
		for (JsonNode rootGroup : rootGroups) {
			visitGroup(rootGroup, "", migrationLogger, mlh);
		}
	}

	private static void visitGroup(JsonNode groupElement, String parentPath, MigrationLogger migrationLogger,
			ConditionMultiLanguageHandler mlh) {
		JsonNode group = groupElement.get(ModelJsonUtil.TYPE_GROUP);
		if (group == null || !group.has(ModelJsonUtil.GROUP_ELEMENTS)) {
			return;
		}
		String groupFullName = parentPath + EntityUtils.SEPARATOR + elementName(groupElement);
		for (JsonNode element : group.get(ModelJsonUtil.GROUP_ELEMENTS)) {
			String type = elementType(element);
			if (ModelJsonUtil.TYPE_GROUP.equals(type)) {
				visitGroup(element, groupFullName, migrationLogger, mlh);
			} else if (ModelJsonUtil.TYPE_RULE.equals(type)) {
				migrateRule(element, groupFullName, migrationLogger, mlh);
			} else if (ModelJsonUtil.TYPE_COMPUTATION.equals(type)) {
				migrateComputation(element, groupFullName, migrationLogger, mlh);
			}
		}
	}

	private static void migrateRule(JsonNode ruleElement, String groupFullName, MigrationLogger migrationLogger,
			ConditionMultiLanguageHandler mlh) {
		if (ruleElement.get(ModelJsonUtil.TYPE_RULE) instanceof ObjectNode ruleAttributesObject) {
			ElementCtx ctx = ElementCtx.of(ruleElement, groupFullName, mlh, migrationLogger);
			rewriteCondition(ruleAttributesObject, ERROR_CONDITION, GrammarOnly_ConditionParser::bedingung_start, ctx);
		}
	}

	private static void migrateComputation(JsonNode computationElement, String groupFullName, MigrationLogger migrationLogger,
			ConditionMultiLanguageHandler mlh) {
		if (computationElement.get(ModelJsonUtil.TYPE_COMPUTATION) instanceof ObjectNode computationAttributesObject) {
			ElementCtx ctx = ElementCtx.of(computationElement, groupFullName, mlh, migrationLogger);
			rewriteCondition(computationAttributesObject, COMMON_PRECONDITION, GrammarOnly_ConditionParser::bedingungSnippet, ctx);
			JsonNode alternatives = computationAttributesObject.get(COMPUTATION_ALTERNATIVES);
			if (alternatives != null && alternatives.isArray()) {
				for (JsonNode alternative : alternatives) {
					if (alternative instanceof ObjectNode alternativeObj) {
						rewriteCondition(alternativeObj, PRECONDITION, GrammarOnly_ConditionParser::bedingungSnippet, ctx);
						rewriteCondition(alternativeObj, OPERATION, GrammarOnly_ConditionParser::operationSnippet, ctx);
					}
				}
			}
		}
	}

	private static void rewriteCondition(ObjectNode parent, String fieldName,
			Function<GrammarOnly_ConditionParser, ParseTree> grammarRule, ElementCtx ctx) {
		if (parent.get(fieldName) instanceof StringNode textNode) {
			String oldText = textNode.stringValue();
			String newText = absolutizeAboveRootPaths(oldText, grammarRule, fieldName, ctx);
			if (!newText.equals(oldText)) {
				parent.put(fieldName, newText);
			}
		}
	}

	private static String absolutizeAboveRootPaths(String snippet,
			Function<GrammarOnly_ConditionParser, ParseTree> grammarRule, String fieldNameForLogging, ElementCtx ctx) {
		if (StringUtils.isEmpty(snippet)) {
			return snippet;
		}
		int ruleGroupLevel = EntityUtils.getLevel(ctx.groupFullName());
		// a reference reaching the model root needs ruleGroupLevel + 1 upward steps, and the count over the whole
		// snippet is an upper bound for any single reference in it - so this skips the parse for most conditions
		if (StringUtils.countMatches(snippet, EntityUtils.PATH_UP) < ruleGroupLevel + 1) {
			return snippet;
		}
		StringBuilder newSnippet = new StringBuilder(snippet);
		findEntityReferences(snippet, grammarRule, fieldNameForLogging, ctx).stream()
				.filter(reference -> !reference.isAbsolute())
				// replace from the back, so that the ranges of the references still to come stay valid
				.sorted(Comparator.comparingInt((EntityReference r) -> r.rangeInCondition().start()).reversed())
				.forEachOrdered(reference -> {
					Range range = reference.rangeInCondition();
					String referenceText = snippet.substring(range.start(), range.endExclusive());
					// counted in the reference's own text, so a non-minimal hand-written path is judged by what it
					// spells out; inside an entity reference '..' can only be an upward step, as an idName needs a
					// name part after every '.'
					int upwardSteps = StringUtils.countMatches(referenceText, EntityUtils.PATH_UP);
					if (upwardSteps >= ruleGroupLevel + 1) {
						// the turning point is the technical model root: exactly the notation A12K-4276 added
						newSnippet.replace(range.start(), range.endExclusive(), ctx.formatAbsolute(reference));
					}
					// no else: more upward steps than that already fail in the listener while collecting
				});
		return newSnippet.toString();
	}

	private static List<EntityReference> findEntityReferences(String snippet,
			Function<GrammarOnly_ConditionParser, ParseTree> grammarRule, String fieldNameForLogging, ElementCtx ctx) {
		try {
			return parseEntityReferences(snippet, grammarRule, ctx);
		} catch (Exception e) {
			ctx.warn("the references in the '" + fieldNameForLogging + "' could not be collected (" + e.getMessage()
					+ "), so relative paths in it were left unchanged");
			return List.of();
		}
	}

	/**
	 * Note: near-copy of the private {@code MoveSupportInternal#findEntityReferencesWithConditionGrammar}
	 */
	private static List<EntityReference> parseEntityReferences(String snippet,
			Function<GrammarOnly_ConditionParser, ParseTree> grammarRule, ElementCtx ctx) {
		CharStream input = CharStreams.fromString(snippet);
		Base_ConditionLexer lexer = new Base_ConditionLexer(input);
		lexer.initLexer(ctx.mlh());

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		GrammarOnly_ConditionParser parser = new GrammarOnly_ConditionParser(tokens);
		parser.initParser(false, false, null);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE); // don't print syntax errors to stderr

		ParseTree parsed = grammarRule.apply(parser);

		ParseTreeWalker walker = new ParseTreeWalker();
		MoveSupportParseTreeListener listener = new MoveSupportParseTreeListener(ctx.groupFullName());
		walker.walk(listener, parsed);
		return listener.getCollectedReferences();
	}

	/**
	 * Analogous to {@code MoveSupportInternal#computeStarsArray}: an index of 0 marks a starred (iterated) ancestor,
	 * 1 a plain one.
	 */
	private static long[] computeStarsArray(String entityFullName, Set<String> starredGroupFullNames) {
		int numberOfPathParts = ElementPathUtils.getNumberOfPathParts(entityFullName);
		long[] starIndices = new long[numberOfPathParts];
		List<String> pathParts = ElementPathUtils.getAncestorPaths(entityFullName);
		pathParts.add(entityFullName);
		for (int i = 0; i < numberOfPathParts; i++) {
			starIndices[i] = starredGroupFullNames.contains(pathParts.get(i)) ? 0L : 1L;
		}
		return starIndices;
	}

	private static String elementType(JsonNode element) {
		JsonNode type = element.get(ModelJsonUtil.ELEMENT_TYPE);
		return type != null && type.isString() ? type.asString() : "";
	}

	private static String elementName(JsonNode element) {
		JsonNode name = element.get(ModelJsonUtil.NAME);
		return name != null && name.isString() ? name.asString() : "";
	}

	private record ElementCtx(String idForLogging, String nameForLogging, String groupFullName, ConditionMultiLanguageHandler mlh,
	                          MigrationLogger migrationLogger) {

		static ElementCtx of(JsonNode elementForLogging, String groupFullName, ConditionMultiLanguageHandler mlh,
				MigrationLogger migrationLogger) {
			JsonNode idForLogging = elementForLogging.get(ModelJsonUtil.FIELD_ID);
			JsonNode nameForLogging = elementForLogging.get(ModelJsonUtil.NAME);
			return new ElementCtx(idForLogging != null && idForLogging.isString() ? idForLogging.asString() : "unknown",
					nameForLogging != null && nameForLogging.isString() ? nameForLogging.asString() : "unknown", groupFullName, mlh,
					migrationLogger);
		}

		// similar to MoveSupportInternal#updatedReferenceString4ConditionGrammar
		String formatAbsolute(EntityReference entityReference) {
			return EntityIdFormatterUtil.formatEntityIdForConditionTextA12DV(
					entityReference.fullName(),
					computeStarsArray(entityReference.fullName(), entityReference.starredGroupFullNames()),
					false, // the listener's range never includes the '$' of an outer reference
					PathNotationConfig.ABSPATH,
					groupFullName,
					shortName -> false,
					mlh,
					false,
					false);
		}

		void warn(String what) {
			migrationLogger.addInfo(new MigrationInfo(
					"WARNING: In rule or computation '" + nameForLogging + "', " + what
							+ ". It may not be parseable in older document model versions.",
					idForLogging));
		}
	}
}
