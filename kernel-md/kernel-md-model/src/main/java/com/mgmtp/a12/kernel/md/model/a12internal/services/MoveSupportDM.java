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

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.service.internal.MoveSupportInternal;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentUniquenessCriterion;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.impl.LocalizedTextMapImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.MoveSupportDMInternal;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.model.utils.OnlyForUsage;

@OnlyForUsage
public class MoveSupportDM {
	private MoveSupportDM() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Updates references to fields and groups in rule conditions, rule error messages, computations
	 * (preconditions, operations, common precondition), computation error messages and Document Uniqueness Criteria
	 * (field full names and error text parameters) according to entityMoves.
	 * Also updates the error field reference in all rules, the computed field reference in computations and for groups the index
	 * field and sort field (if present).<p/>
	 * entityMoves should contain a mapping of previous vs new absolute paths of groups, fields, rules or computations in the
	 * document model.
	 * </p>
	 * Note: the rules and computations themselves are <em>NOT</em> moved, only the conditions, preconditions, operations and
	 * error texts are updated. Moving the rules and computations must be done by the caller afterward (that move may target
	 * some specific position within the child elements of a parent group, which is not relevant for updating the references here).
	 * </p>
	 * Scope & Limitations: see {@link MoveSupportInternal#changeConditionTextForMove(String, String, Map, Locale)}</br>
	 */
	public static void changePathReferencesForMove(DocumentModel documentModel, Map<String, String> entityMoves) {
		// NOTE: if necessary, we could/should use an index of entities -> referencing rules and computations for performance
		// optimization. This would allow to skip rules & computations that are not affected by the entityMoves.
		Locale conditionLanguage = documentModel.getContent().getModelConfig().getConditionLanguage();
		MoveSupportDMInternal moveSupportInternal = MoveSupportDMInternal.getInstance();
		forEachRuleAndComputationAndGroup(
				documentModel,
				rule -> {
					String ruleFullName = ElementUtils.getPath(rule);
					rule.setErrorCondition(moveSupportInternal.changeConditionTextForMove(
							rule.getErrorCondition(),
							ruleFullName,
							entityMoves,
							conditionLanguage
					));
					rule.setErrorMessage(new LocalizedTextMapImpl(moveSupportInternal.changeErrorTextsForMove(
							rule.getErrorMessage(),
							ruleFullName,
							entityMoves,
							conditionLanguage)));
					moveSupportInternal.updateRelPathInFieldRef(
							rule.getErrorEntity(),
							ruleFullName,
							entityMoves
					);
				},
				computation -> {
					var computationFullName = ElementUtils.getPath(computation);
					computation.setComputationPreconditionsAndOperations(moveSupportInternal.changeComputationAlternativesForMove(
							computation.getComputationPreconditionsAndOperations(),
							computationFullName,
							entityMoves,
							conditionLanguage
					));
					computation.setErrorMessage(new LocalizedTextMapImpl(moveSupportInternal.changeErrorTextsForMove(
							computation.getErrorMessage(),
							computationFullName,
							entityMoves,
							conditionLanguage)));
					moveSupportInternal.updateRelPathInFieldRef(
							computation.getComputedField(),
							computationFullName,
							entityMoves
					);
				},
				group -> {
					String groupFullName = ElementUtils.getPath(group);
					// Note: the result is only valid if it is a single-element relpath (i.e. no slashes),
					// but we shouldn't _need_ to check this here, because if the group was valid before and the moves are legal,
					// this constraint will automatically be satisfied afterwards.
					// Additionally, it seems possible that this constraint could be removed in the future.
					group.getIndexField().ifPresent(idxFieldRef -> moveSupportInternal.updateRelPathInFieldRef(
							idxFieldRef, groupFullName, entityMoves));
					group.getSortFields().forEach(sortField -> moveSupportInternal.updateRelPathInFieldRef(
							sortField.getField(), groupFullName, entityMoves));
				}
		);

		for (DocumentUniquenessCriterion criterion : documentModel.getContent().getDocumentUniquenessCriteria()) {
			criterion.setFieldFullNames(MoveSupportInternal.findNewFullNames(criterion.getFieldFullNames(), entityMoves));
			criterion.setErrorMessage(new LocalizedTextMapImpl(
					moveSupportInternal.changeDocumentUniquenessCriterionErrorTextsForMove(criterion.getErrorMessage(),
							entityMoves)));
		}
	}

	private static void forEachRuleAndComputationAndGroup(
			DocumentModel dm,
			Consumer<Rule> ruleConsumer,
			Consumer<Computation> computationConsumer,
			Consumer<Group> groupConsumer
	) {
		// TODO discuss with SME if we want to continue on potential failures, and maybe report a problem instead of propagating
		//   exceptions.
		new DocumentModelWalker().acceptDocumentModel(
				dm,
				new DocumentModelVisitor() {
					@Override public VisitProcess visitRule(final Rule rule) {
						ruleConsumer.accept(rule);
						return VisitProcess.CONTINUE_TRAVERSAL;
					}

					@Override public VisitProcess visitComputation(final Computation computation) {
						computationConsumer.accept(computation);
						return VisitProcess.CONTINUE_TRAVERSAL;
					}

					@Override public VisitProcess visitGroup(final Group group) {
						groupConsumer.accept(group);
						return VisitProcess.CONTINUE_TRAVERSAL;
					}
				});
	}
}
