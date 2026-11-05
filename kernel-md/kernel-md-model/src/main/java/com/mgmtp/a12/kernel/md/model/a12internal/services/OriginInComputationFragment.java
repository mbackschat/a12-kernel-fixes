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

package com.mgmtp.a12.kernel.md.model.a12internal.services;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.utils.internal.string.sourcemap.StringWithFragmentsOriginMapEntry;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.IRuleFromComputation;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ComputationAlternativesJoiner;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.Getter;

/**
 * For an {@link IProblem} that originates from a parser problem for a computation's preconditions or operations, this class
 * helps to map the problem to the corresponding computation alternative, precondition/operation and range of
 * {@link TextPosition}s within that precondition or operation.
 */
@OnlyForUsage
public class OriginInComputationFragment {
	private final ComputationAlternativesJoiner.ComputationAlternativeFragmentOriginRef fragmentDescriptor;
	private final TextPosition startInFragment;
	private final TextPosition endInFragment;

	private OriginInComputationFragment(
			ComputationAlternativesJoiner.ComputationAlternativeFragmentOriginRef fragmentDescriptor,
			TextPosition startInFragment,
			TextPosition endInFragment
	) {
		this.fragmentDescriptor = fragmentDescriptor;
		this.startInFragment = startInFragment;
		this.endInFragment = endInFragment;
	}

	public static Optional<OriginInComputationFragment> of(IProblem problem) {
		boolean hasValidPositionInfo = problem.getSourceStart() != -1
				&& problem.getSourceEnd() != -1
				&& problem.getLine() != -1
				&& problem.getLineEnd() != -1;
		Object origin = problem.getSource();
		if (!hasValidPositionInfo) {
			return Optional.empty();
		} else if (origin instanceof IRuleFromComputation) {
			IRuleFromComputation rule = (IRuleFromComputation) problem.getSource();
			return of(rule, problem.getLine(), problem.getSourceStart(), problem.getLineEnd(), problem.getSourceEnd());
		} else if (origin instanceof OriginInComputationFragment computationFragment) {
			return Optional.of(computationFragment);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * line numbers are 1-based and endLineInFragment is inclusive</br>
	 * column numbers are 0-based and endColInFragment is exclusive
	 */
	public static OriginInComputationFragment of(String fragemntText, int alternativeIndex, boolean isPrecondition,
			int startLineInFragment, int startColInFragment,
			int endLineInFragment, int endColInFragment) {
		return new OriginInComputationFragment(
				new ComputationAlternativesJoiner.ComputationAlternativeFragmentOriginRef(alternativeIndex, isPrecondition),
				new OriginInComputationFragment.TextPosition(startLineInFragment, startColInFragment, fragemntText),
				new OriginInComputationFragment.TextPosition(endLineInFragment, endColInFragment, fragemntText));
	}

	/**
	 * Creates an origin within a computation's additional parser information (the preamble, e.g.
	 * <code>@SuppressWarning(...)</code>).
	 *
	 * line numbers are 1-based and endLineInFragment is inclusive</br>
	 * column numbers are 0-based and endColInFragment is exclusive
	 */
	public static OriginInComputationFragment ofAdditionalParserInformation(String fragmentText,
			int startLineInFragment, int startColInFragment,
			int endLineInFragment, int endColInFragment) {
		return new OriginInComputationFragment(
				ComputationAlternativesJoiner.ComputationAlternativeFragmentOriginRef.forAdditionalParserInformation(),
				new OriginInComputationFragment.TextPosition(startLineInFragment, startColInFragment, fragmentText),
				new OriginInComputationFragment.TextPosition(endLineInFragment, endColInFragment, fragmentText));
	}

	private static Optional<OriginInComputationFragment> of(IRuleFromComputation rule, int startLine, int startColumn, int endLine,
			int endColumn) {
		var start = new TextPosition(startLine, startColumn, rule.getErrorConditionText());
		var end = new TextPosition(endLine, endColumn, rule.getErrorConditionText());
		return of(rule, start.getIdx(), end.getIdx());
	}

	private static Optional<OriginInComputationFragment> of(IRuleFromComputation rule, int startIdx, int endIdx) {
		StringWithFragmentsOriginMapEntry fragmentRefWithOffset =
				rule.mapPositionInConditionTextToOriginInComputationAlternative(startIdx);

		assert endIdx >= startIdx :
				"endIdx is exclusive, so it must be >= startIdx but endIdx: " + endIdx + ", startIdx: " + startIdx;

		if (!fragmentRefWithOffset.equals(
				rule.mapPositionInConditionTextToOriginInComputationAlternative(
						// endIdx is exclusive, so we only require endIdx - 1 to map to the same fragment.
						// This code will normally not be reached for zero-length fragments, but in order to be robust even if
						// mapPositionInConditionTextToOriginInComputationAlternative implements some edge-case behavior that
						// may return emtpy fragments (e.g. at the end of a string), we additionally use Math.max here:
						Math.max(startIdx, endIdx - 1)))) {

			// if the range in the rule condition spans multiple fragemnts, it cannot be mapped to
			// a single OriginInComputationFragment
			return Optional.empty();
		}

		if (!(fragmentRefWithOffset.getOriginRef()
				instanceof ComputationAlternativesJoiner.ComputationAlternativeFragmentOriginRef fragmentDescriptor)) {
			// if the range in the rule condition corresponds not to a computation fragemnt (precondition or operation of one
			// of the computation alternatives, but to a template-derived part of the rule, then it cannot be mapped to an
			// OriginInComputationFragment
			return Optional.empty();
		}
		String fragmentText = rule.getErrorConditionText().substring(
				fragmentRefWithOffset.getBeginIdxInCombinedString(),
				fragmentRefWithOffset.getEndIdxInCombinedString());
		TextPosition startInFragment = new TextPosition(fragmentRefWithOffset.translateIdx(startIdx), fragmentText);
		TextPosition endInFragment = new TextPosition(fragmentRefWithOffset.translateIdx(endIdx), fragmentText);
		return Optional.of(new OriginInComputationFragment(fragmentDescriptor, startInFragment, endInFragment));
	}

	/**
	 * @return <code>true</code> iff the parser problem originates from the common precondition field of a computation.
	 */
	public boolean isCommonPrecondition() {
		return fragmentDescriptor.isCommonPrecondition();
	}

	/**
	 * @return <code>true</code> iff the parser problem originates from the additional parser information (the preamble,
	 * 		e.g. <code>@SuppressWarning(...)</code>) of a computation. If so, {@link #getComputationAlternativeIndex()} and
	 *        {@link #isPrecondition()} are not meaningful.
	 */
	public boolean isAdditionalParserInformation() {
		return fragmentDescriptor.isAdditionalParserInformation();
	}

	/**
	 * @return the index (0-based) of the computation alternative that corresponds to the parser problem, or a
	 * 		negative sentinel for problems that do not belong to an alternative: {@code -1} for the common
	 * 		precondition ({@link #isCommonPrecondition()}) and {@code -2} for the additional parser information
	 * 		({@link #isAdditionalParserInformation()}).
	 */
	public int getComputationAlternativeIndex() {
		return fragmentDescriptor.alternativeIdx();
	}

	/**
	 * @return true if the parser problem originates from the <b>precondition</b> of the computation alternative (as reported by
	 *        {@link #getComputationAlternativeIndex()}, or false if it originates from the <b>operation</b>.
	 */
	public boolean isPrecondition() {
		return fragmentDescriptor.isPrecondition();
	}

	/**
	 * @return the start of the parser problem within the computation alternative
	 * 		precondition or operation (as reported by {@link #getComputationAlternativeIndex()} and
	 *        {@link #isPrecondition()}).
	 */
	public TextPosition getStartInFragment() {
		return this.startInFragment;
	}

	/**
	 * @return the end of the parser problem within the computation alternative
	 * 		precondition or operation (as reported by {@link #getComputationAlternativeIndex()} and
	 *        {@link #isPrecondition()}).
	 */
	public TextPosition getEndInFragment() {
		return this.endInFragment;
	}

	public static class TextPosition {
		@Getter private final String text;
		private final int idx;

		private final Pair<Integer, Integer> asLineAndColumn;

		private static final String LINE_SEP_LOOKBEHIND_REGEX = "(?<=\r?\n)"; // ideally we would use the same definition of

		public TextPosition(int idx, String text) {
			this.idx = idx;
			this.text = text;
			this.asLineAndColumn = computeLineAndColumn(idx, text);
		}

		public TextPosition(int line, int col, String text) {
			this.idx = lineAndCol2Idx(line, col, text);
			this.text = text;
			this.asLineAndColumn = Pair.of(line, col);
		}

		/**
		 * @return the 0-based index of the position in the text that is associated with this object
		 */
		public int getIdx() {
			return idx;
		}

		/**
		 * @return the line (1-based) and colum (0-based) that describes the position in the text that is associated with this
		 * 		object
		 */
		public Pair<Integer, Integer> getLineAndColumn() {
			return asLineAndColumn;
		}

		private static Pair<Integer, Integer> computeLineAndColumn(int idx, String text) {
			return idx2LineAndCol(idx, text);
		}

		private static int lineAndCol2Idx(int line, int col, String text) {
			String[] lines = text.split(LINE_SEP_LOOKBEHIND_REGEX);
			int idx = 0;
			for (int i = 1; i < line; i++) { // line numbers are 1-based
				idx += lines[i - 1].length();
			}
			return idx + col;
		}

		private static Pair<Integer, Integer> idx2LineAndCol(int idx, String text) {
			String[] lines = text.split(LINE_SEP_LOOKBEHIND_REGEX);
			int currentLine = 1; // 1-based indexing for lines
			int currentLineStartIdx = 0;
			String line;
			for (int i = 0; i < lines.length; i++) {
				line = lines[i];
				var nextLineStartIdx = currentLineStartIdx + line.length();
				if (idx < nextLineStartIdx || (idx == nextLineStartIdx && i == lines.length - 1)) {
					return Pair.of(currentLine, idx - currentLineStartIdx);
				} else {
					currentLine += 1;
					currentLineStartIdx = nextLineStartIdx;
				}
			}
			throw new IllegalArgumentException("Could not convert idx to line and column in text, likely out-of-bounds: idx = "
					+ idx + ", last index in text = " + currentLineStartIdx + ", text = " + text);
		}
	}
}

