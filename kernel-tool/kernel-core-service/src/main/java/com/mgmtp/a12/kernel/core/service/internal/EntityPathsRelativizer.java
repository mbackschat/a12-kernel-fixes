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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.parser.internal.ParserService;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.EntityIdFormatterUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporterProxy;

public class EntityPathsRelativizer {

	/**
	 * This method computes a list of {@link TextUpdate}s that describe how to convert the entity paths in the rule's condition so
	 * that they are relative and do not contain the root group's name.<br/>
	 * i.e. Absolute paths are converted to relative paths, and relative paths with explicit turning points (e.g. "../..RG1/f")
	 * that reach a root group are converted to relative paths without explicit turning points (e.g. "../../f").
	 * Relative paths that do not reach the root group are not converted.
	 * <p/>
	 * For a consistent input DM the relativized result stays consistent. For a reference that crosses to a sibling root group the
	 * method emits a relative path that reaches above the root group (e.g. "../../RG2/x"), which is valid, too.
	 * <p/>
	 * The given rule is not changed - to apply the conversion, the returned {@link TextUpdate}s must be applied to the rule's
	 * condition by the caller.
	 * <p/>
	 */
	public static List<TextUpdate> determineUpdatesForPathsInConditionWithRootGroupReference(
			IRule iRule,
			ParserService parserService,
			IProblemReporter pr) {
		Objects.requireNonNull(pr);

		ProblemReporterProxy prProxy = new ProblemReporterProxy(pr);
		ICondition condition = parserService.parseAndCheckCondition(iRule, prProxy, ExpandMode.NO_EXPAND_OR_RG_REPLACEMENT);

		if (condition == null) {
			return List.of();
		}

		List<TextUpdate> updates = new ArrayList<>();
		for (IEntityId entityId : getEntitiesFromCondition(condition)) {
			EntityIdImpl entityIdImpl = (EntityIdImpl) entityId;

			String origName = iRule.getErrorConditionText().substring(
					entityIdImpl.getStartIdxInCondition(),
					entityIdImpl.getStopIdxInCondition());

			// no change is needed for relative, downward-only paths. (only for absolute paths and for some relative upward paths).
			// Currently, it is not explicitly tracked whether a path was absolute or a relative-downward-only, so we detect
			// this based on startsWith("/")
			boolean isRelativeDownwardPath = !(origName.startsWith("/") || origName.startsWith("$/"))
					&& !entityIdImpl.getPathNotationConfig().isLabeledAsRelativeUpwardReference()
					&& !entityIdImpl.isReferencedByShortName(); // shortNames *must* be replaced in includes to avoid potential ambiguity
			// Note: referencing a field via its short name is currently not allowed in A12, but to keep this method
			// robust we should check anyway, as it is not inherently DM-only
			if (isRelativeDownwardPath) {
				continue;
			}

			PathNotationConfig newPathConfig = makePathNotationConfigForRelativizedPath(entityIdImpl, iRule.getFullName());
			EntityIdImpl newPath = entityIdImpl.clone();
			newPath.setPathNotationConfig(newPathConfig);

			String newNameInclStars = EntityIdFormatterUtil.formatEntityIdForConditionText(
					newPath, EntityUtils.getParentPath(iRule.getFullName()),
					shortName -> false,
					new ConditionMultiLanguageHandler(
							parserService.getModelSpecificSettings().getLanguageUsedInConditionText(),
							parserService.getProjectSpecificSettings()));

			if (!origName.equals(newNameInclStars)) {
				int oldBegin = entityIdImpl.getStartIdxInCondition();
				int oldEnd = entityIdImpl.getStopIdxInCondition();
				updates.add(new TextUpdate(oldBegin, oldEnd, newNameInclStars));
			}
		}
		return updates;
	}

	public static class TextUpdate {
		private final int beginIdx, endIdx;
		private final String newText;

		public TextUpdate(int beginIdx, int endIdx, String newText) {
			this.beginIdx = beginIdx;
			this.endIdx = endIdx;
			this.newText = newText;
		}

		public static String applyUpdatesTo(Collection<TextUpdate> updates, String string) {
			var stringBuilder = new StringBuilder(string);
			sortedBackwards(updates).forEach(u -> u.applyTo(stringBuilder));
			return stringBuilder.toString();
		}

		public void applyTo(StringBuilder stringBuilder) {
			stringBuilder.replace(this.getBeginIdx(), this.getEndIdx(), this.getNewText());
		}

		/**
		 * sort by reverse occurrence in the text, so that we can safely apply one update after another without invalidating
		 * begin/end positions of the next updates
		 */
		public static List<TextUpdate> sortedBackwards(Collection<TextUpdate> updates) {
			return updates.stream().sorted(
					(o1, o2) -> Integer.compare(o2.getBeginIdx(), o1.getBeginIdx())
			).collect(Collectors.toList());
		}

		public TextUpdate withBeginAndEndIdx(int beginIdx, int endIdx) {
			return new TextUpdate(beginIdx, endIdx, this.newText);
		}

		public int getBeginIdx() {
			return beginIdx;
		}

		public int getEndIdx() {
			return endIdx;
		}

		public String getNewText() {
			return newText;
		}

		@Override public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			TextUpdate that = (TextUpdate) o;
			return beginIdx == that.beginIdx && endIdx == that.endIdx && Objects.equals(newText, that.newText);
		}

		@Override public int hashCode() {
			return Objects.hash(beginIdx, endIdx, newText);
		}

		@Override
		public String toString() {
			return "TextUpdate(" + beginIdx + ", " + endIdx + ", " + newText + ")";
		}
	}

	private static List<IEntityId> getEntitiesFromCondition(ICondition condition) {
		List<IEntityId> entityIds = new ArrayList<>();
		condition.traverse(new ASTVisitor() {
			@Override public boolean visit(IEntityId p) {
				entityIds.add(p);
				if (p.getFilterCondition() != null) {
					p.getFilterCondition().traverse(this);
				}
				return super.visit(p);
			}
		});
		return entityIds;
	}

	private static PathNotationConfig makePathNotationConfigForRelativizedPath(IEntityId p, String ruleFullName) {
		// A12K-4276: a wildcard at the root group level is carried through as a downward segment ("../../RG2*/x");
		// getRequiredRelativeUpwardReferenceLevels places the turning point above it.
		EntityIdFormatterUtil.RequiredRelUpLevels relUpRequirements =
				EntityIdFormatterUtil.getRequiredRelativeUpwardReferenceLevels(p, EntityUtils.getParentPath(ruleFullName));
		// in case the turning point is a non-root group, we can keep the explicit turning point
		// e.g. rule group /G1/G2/G3 (level=2, 0-based) und path ..G2/G3b*/f1 (levels=1)
		// Note that this requirement is stricter than the condition in EntityIdFormatterUtil#isPathNotationTypeLegalInCondition:
		//   while a root-group turning point is ok in general, it is not suitable for models that are to be used as (SINGLE_RG) includes,
		//   because the root group name is overridden after expansion of the include.
		//   (in MODEL_ROOT includes, a root-group level explicit turning point would actually be ok, but
		//    we don't add a case distinction for this here since the potential advantage would be purely
		//    cosmetic and minimal)
		boolean canKeepExplicitTurningPoint = relUpRequirements.turingPointLevel() > 0;

		PathNotationConfig rpiOrig = p.getPathNotationConfig();
		return rpiOrig
				.withPreferRelPath()
				// NOTE: the behavior for converted abspaths is currently to always use no explicit turning point,
				// even if the path does not touch the root group. This appears to be a good default because it results
				// in more uniform behavior.
				// (This is defined via PathNotationConfig.ABSPATH for which hasExplicitTurningPoint() is false.)
				.withHasExplicitTurningPoint(rpiOrig.hasExplicitTurningPoint() && canKeepExplicitTurningPoint);
	}
}
