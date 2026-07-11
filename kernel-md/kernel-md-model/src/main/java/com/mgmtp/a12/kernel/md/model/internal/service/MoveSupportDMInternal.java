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

package com.mgmtp.a12.kernel.md.model.internal.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.service.internal.MoveSupportInternal;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextUtil;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.IDocumentModelObjectReference;

public class MoveSupportDMInternal extends MoveSupportInternal {

	// NOTE: high-level api: see MoveSupportDM

	// make sure that nothing depends on the constructor directly, in case we need to extract an interface later
	protected MoveSupportDMInternal() {
		super();
	}

	public static MoveSupportDMInternal getInstance() {
		return new MoveSupportDMInternal();
	}

	/**
	 * See {@link MoveSupportInternal#changeConditionSnippetForMove(Function, String, String, Map, Locale)}
	 */
	public Computation.ComputationPreconditionsAndOperations changeComputationAlternativesForMove(
			Computation.ComputationPreconditionsAndOperations computationFragments,
			String currentFullName,
			Map<String, String> entityMoves,
			Locale conditionLanguage
	) {
		String newCommonPrecond = null;
		if (computationFragments.getCommonPrecondition().isPresent()) {
			newCommonPrecond = updateBedingungSnippet(
					computationFragments.getCommonPrecondition().get(), currentFullName, entityMoves, conditionLanguage
			);
		}
		List<Computation.ComputationAlternative> newAlternatives = computationFragments.getAlternatives().stream().map(ca -> {
			String newCaPrecond = null;
			if (ca.getPrecondition().isPresent()) {
				newCaPrecond = updateBedingungSnippet(
						ca.getPrecondition().get(), currentFullName, entityMoves, conditionLanguage);
			}
			String newOperation = updateOperation(ca.getOperation(), currentFullName, entityMoves, conditionLanguage
			);
			return new Computation.ComputationAlternative(newCaPrecond, newOperation, ca.getToleranceRangeOp().orElse(null));
		}).toList();
		return new Computation.ComputationPreconditionsAndOperations(newCommonPrecond, newAlternatives);
	}

	public void updateRelPathInFieldRef(
			IDocumentModelObjectReference<?> elementRef,
			String oldBasePath,
			Map<String, String> entityMoves
	) {
		String oldRelPath = elementRef.getRelativePath();
		String oldFullName = ElementPathUtils.absPath(oldBasePath, oldRelPath);
		String newFullName = findNewFullname(oldFullName, entityMoves);
		String newBasePath = findNewFullname(oldBasePath, entityMoves);
		String newRelPath = ElementPathUtils.relPath(newFullName, newBasePath);
		elementRef.setRelativePath(newRelPath);
	}

	/**
	 * Similar to {@link #changeErrorTextsForMove(Map, String, Map, Locale)} but for Document Uniqueness Criterion error texts,
	 * which use simple absolute-path parameters ({@code $fieldFullName$} and {@code $fieldFullName.value$}) rather than the
	 * rule error text grammar.
	 */
	public Map<Locale, String> changeDocumentUniquenessCriterionErrorTextsForMove(
			Map<Locale, String> errorTexts,
			Map<String, String> entityMoves
	) {
		return Java8Utils.mapMapValuesPreserveOrder(errorTexts, errorText ->
				FehlertextUtil.mapParamsInFehlertext(errorText, param -> {
					if (!EntityUtils.isAbsolutePath(param)) {
						return param; // not a valid absolute path — leave unchanged
					}
					String suffix = "";
					String basePath = param;
					if (param.endsWith(".value")) {
						suffix = ".value";
						basePath = param.substring(0, param.length() - ".value".length());
					}
					return findNewFullname(basePath, entityMoves) + suffix;
				}));
	}

	private String updateBedingungSnippet(
			String teilbedingungSnippet,
			String currentFullName,
			Map<String, String> entityMoves,
			Locale conditionLanguage) {
		return changeConditionSnippetForMove(
				GrammarOnly_ConditionParser::bedingungSnippet,
				teilbedingungSnippet,
				currentFullName,
				entityMoves,
				conditionLanguage
		);
	}

	private String updateOperation(
			String operation,
			String currentFullName,
			Map<String, String> entityMoves,
			Locale conditionLanguage) {
		return changeConditionSnippetForMove(
				GrammarOnly_ConditionParser::operationSnippet,
				operation,
				currentFullName,
				entityMoves,
				conditionLanguage
		);
	}
}
