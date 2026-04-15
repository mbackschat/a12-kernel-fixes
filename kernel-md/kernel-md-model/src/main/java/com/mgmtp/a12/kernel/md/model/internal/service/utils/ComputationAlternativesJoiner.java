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

package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.FeldIdUtil;
import com.mgmtp.a12.kernel.core.utils.internal.string.sourcemap.StringWithFragmentsOriginMap;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation.ComputationAlternative;
import com.mgmtp.a12.kernel.md.model.api.IComputation.IComputationAlternative;
import com.mgmtp.a12.kernel.md.model.api.IField;

import lombok.Getter;
import lombok.NonNull;

/**
 * The class <code>ComputationAlternativesJoiner</code> is used to combine the
 * {@link ComputationAlternative}s from a {@link Computation} to the single calculation statement
 * used in kernel.
 */
public class ComputationAlternativesJoiner {
	private static final String GATE_TEMPLATE_ENGLISH = "FieldFilled(%s) And ";
	private static final String GATE_TEMPLATE_GERMAN = "FeldAngegeben(%s) Und ";
	private static final String PREAMBLE_ENGLISH = "@SuppressWarning(%s)\n";
	private static final String PREAMBLE_GERMAN = "@UnterdrueckeWarnung(%s)\n";
	private static final String AND_OP_ENGLISH = "And";
	private static final String AND_OP_GERMAN = "Und";
	private static final String OR_OP_ENGLISH = "Or";
	private static final String OR_OP_GERMAN = "Oder";

	private final String gateTemplate;
	private final String preamble;
	private final String andOp;
	private final String orOp;

	private final @NonNull ConditionMultiLanguageHandler mlh;
	private final String commonPrecondition;
	private final @NonNull List<IComputationAlternative> alternatives;
	private final @NonNull String computedFieldReference;
	private final @NonNull List<String> errorCodesToSuppress;

	/**
	 * @param alternatives
	 * 		the alternatives to be joined.
	 * @param computedField
	 * 		the computed field.
	 * @param computationParentPath
	 * 		the parent path of the corresponding computation
	 * @param errorCodesToSuppress
	 * 		the error codes that should be suppressed with @SuppressWarning.
	 */
	public ComputationAlternativesJoiner(@NonNull ConditionMultiLanguageHandler mlh, String commonPrecondition,
			@NonNull List<IComputationAlternative> alternatives, @NonNull IField computedField,
			@NonNull String computationParentPath, @NonNull List<String> errorCodesToSuppress) {
		this.mlh = mlh;
		this.commonPrecondition = commonPrecondition;
		this.alternatives = alternatives;
		this.computedFieldReference = determineComputedFieldReference(computedField, computationParentPath);
		this.errorCodesToSuppress = errorCodesToSuppress;

		boolean isConditionLangEnglish = mlh.getLanguageUsedInConditionText().getLanguage().equals(Locale.US.getLanguage());
		this.gateTemplate = isConditionLangEnglish ? GATE_TEMPLATE_ENGLISH : GATE_TEMPLATE_GERMAN;
		this.preamble = isConditionLangEnglish ? PREAMBLE_ENGLISH : PREAMBLE_GERMAN;
		this.andOp = isConditionLangEnglish ? AND_OP_ENGLISH : AND_OP_GERMAN;
		this.orOp = isConditionLangEnglish ? OR_OP_ENGLISH : OR_OP_GERMAN;
	}

	private String determineComputedFieldReference(IField computedField, String computationParentPath) {
		String computedFieldPath = ElementUtils.getPath(computedField);
		if (!ElementPathUtils.isDescendantOrEqual(computedFieldPath, computationParentPath)) {
			return computedFieldPath;
		}
		return FeldIdUtil.quoteNameIfNecessary(computedFieldPath.substring(computationParentPath.length() + 1), mlh);
	}

	/**
	 * Performs the joinMessages.
	 *
	 * @return the combined statement of the computation alternatives containing
	 * 		the @SuppressWarning preamble if some warnings are to be suppressed.
	 */
	public StringWithFragmentsOriginMap join() {
		StringWithFragmentsOriginMap pre = createPreamble();
		if (alternatives.size() > 1) {
			return pre.concat(createFullComputation());
		} else if (alternatives.isEmpty()) {
			return StringWithFragmentsOriginMap.ofConst("");
		} else {
			IComputationAlternative alternative = alternatives.getFirst();
			return pre.concat(createSimpleComputation(alternative));
		}
	}

	private StringWithFragmentsOriginMap createPreamble() {
		String preambleText;
		if (errorCodesToSuppress.isEmpty()) {
			preambleText = "";
		} else {
			preambleText = String.format(preamble, String.join(",", errorCodesToSuppress));
		}
		return StringWithFragmentsOriginMap.ofConst(preambleText);
	}

	private StringWithFragmentsOriginMap createFullComputation() {
		StringWithFragmentsOriginMap result = createFieldGate()
				.concat(createCommonPrecondition())
				.concat("(\n");
		for (int i = 0; i < alternatives.size(); i++) {
			IComputationAlternative a = alternatives.get(i);
			result = result.concat("(")
					.concat(createAlternative(a, i))
					.concat("\n)");
			if (i < alternatives.size() - 1) {
				result = result.concat(String.format(" %s%n", orOp));
			}
		}
		return result.concat("\n)");
	}

	private StringWithFragmentsOriginMap createSimpleComputation(IComputationAlternative alternative) {
		int alternativeIndex = 0;
		return createFieldGate()
				.concat(createCommonPrecondition())
				.concat(createAlternative(alternative, alternativeIndex));
	}

	private StringWithFragmentsOriginMap createCommonPrecondition() {
		if (commonPrecondition == null) {
			return StringWithFragmentsOriginMap.ofConst("");
		} else {
			return StringWithFragmentsOriginMap.ofConst("(")
					.concat(stringWithSourceMapForCommonPrecondition(commonPrecondition))
					.concat(String.format("%n) %s ", andOp));
		}
	}

	private StringWithFragmentsOriginMap createAlternative(IComputationAlternative alternative, int alternativeIndex) {
		StringWithFragmentsOriginMap preCond;
		Optional<String> precond = alternative.getPrecondition();
		if (precond.isPresent()) {
			String preCondText = precond.get();
			boolean isPrecond = true;
			preCond = StringWithFragmentsOriginMap.ofConst("(")
					.concat(stringWithSourceMapForCompAlt(preCondText, alternativeIndex, isPrecond))
					.concat("\n) " + andOp + " ");
		} else {
			preCond = StringWithFragmentsOriginMap.ofConst("");
		}
		return preCond.concat(
				createComparison(alternative.getOperation(), alternativeIndex, alternative.getToleranceRangeOp().orElse(null)));
	}

	private StringWithFragmentsOriginMap createComparison(@NonNull String operationText, int alternativeIndex,
			String toleranceRangeOp) {
		boolean isPrecond = false;
		String compOp = toleranceRangeOp == null ? "!=" : determineLocalizedToleranceRangeOp(toleranceRangeOp);
		return StringWithFragmentsOriginMap.ofConst("[" + computedFieldReference + "] " + compOp + " ")
				.concat(
						stringWithSourceMapForCompAlt(
								operationText,
								alternativeIndex,
								isPrecond));
	}

	private String determineLocalizedToleranceRangeOp(String toleranceRangeOp) {
		return switch (toleranceRangeOp) {
			case Computation.TR1 -> mlh.getTerminalName(Base_ConditionLexer.UNGLEICH_MIT_TOLERANZ1);
			case Computation.TR2 -> mlh.getTerminalName(Base_ConditionLexer.UNGLEICH_MIT_TOLERANZ2);
			case Computation.TR5 -> mlh.getTerminalName(Base_ConditionLexer.UNGLEICH_MIT_TOLERANZ5);
			case Computation.TR10 -> mlh.getTerminalName(Base_ConditionLexer.UNGLEICH_MIT_TOLERANZ10);
			default -> throw new IllegalArgumentException(toleranceRangeOp + " is not a valid tolerance range operation.");
		};
	}

	private StringWithFragmentsOriginMap createFieldGate() {
		return StringWithFragmentsOriginMap.ofConst(String.format(gateTemplate, computedFieldReference));
	}

	private static StringWithFragmentsOriginMap stringWithSourceMapForCommonPrecondition(String srcString) {
		return StringWithFragmentsOriginMap.ofSingleOrigin(srcString, new ComputationAlternativeFragmentOriginRef(-1, true));
	}

	private static StringWithFragmentsOriginMap stringWithSourceMapForCompAlt(
			String srcString,
			int computationAlternativeIndex,
			boolean isPrecond
	) {
		return StringWithFragmentsOriginMap.ofSingleOrigin(
				srcString,
				new ComputationAlternativeFragmentOriginRef(
						computationAlternativeIndex,
						isPrecond));
	}

	public record ComputationAlternativeFragmentOriginRef(@Getter int alternativeIdx, boolean isPrecondition) {
	}
}
