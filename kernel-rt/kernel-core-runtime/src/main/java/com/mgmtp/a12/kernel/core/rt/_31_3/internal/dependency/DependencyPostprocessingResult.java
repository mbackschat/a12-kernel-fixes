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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IndexFieldCache;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ErrorCause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifierWithSemanticIndex;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;

import lombok.NonNull;

/**
 * Decorating {@link IResult} that removes hidden entities from each message's causes and, when that changes
 * a cause, swaps in the rule's alternative message. A message is <b>never</b> dropped.
 *
 * <p>Only {@link IResult#addMessageV2} is post-processed (Stage 1); the old {@code add*} API and counters
 * are forwarded unchanged.
 */
final class DependencyPostprocessingResult implements IResult {

	private final IResult delegate;
	private final Set<IIdentifier> hiddenEntities;
	private final DependencyModelAccessor modelAccessor;
	private final String language;
	private final IMetaDataValidierung metaData;

	DependencyPostprocessingResult(@NonNull IResult delegate, @NonNull Set<IIdentifier> hiddenEntities,
			@NonNull DependencyModelAccessor modelAccessor, @NonNull String language,
			@NonNull IMetaDataValidierung metaData) {
		this.delegate = delegate;
		this.hiddenEntities = hiddenEntities;
		this.modelAccessor = modelAccessor;
		this.language = language;
		this.metaData = metaData;
	}

	@Override
	public void addMessageV2(String ruleName, String errorCode, String messageStr, ErrorType type, SeverityType severity,
			ICause possiblyCausingEntities) {
		ICause cause = filterHiddenCauses(possiblyCausingEntities);
		// a filtered cause gets the rule's alternative message (if one is defined for the language); an unchanged one keeps its text
		String message = cause == possiblyCausingEntities ? messageStr
				: modelAccessor.alternativeMessage(ruleName, language).orElse(messageStr);
		delegate.addMessageV2(ruleName, errorCode, message, type, severity, cause);
	}

	/** Removes hidden entities from each group of {@code cause}; returns {@code cause} itself when nothing was removed. */
	private ICause filterHiddenCauses(ICause cause) {
		if (cause == null || hiddenEntities.isEmpty()) {
			return cause;
		}
		List<Set<IIdentifier>> groups = cause.getCausingEntityGroups();
		List<Set<IIdentifier>> newGroups = new ArrayList<>(groups.size());
		boolean changed = false;
		for (Set<IIdentifier> group : groups) {
			Set<IIdentifier> kept = group.stream()
					.filter(causing -> !isHidden(causing))
					.collect(Collectors.toSet());
			changed |= kept.size() != group.size();
			newGroups.add(kept);
		}
		return changed ? new ErrorCause(cause.getIterationEntity(), newGroups) : cause;
	}

	/** A causing entity is hidden if some hidden entry is an ancestor-or-self of it under the index rules below. */
	private boolean isHidden(IIdentifier causing) {
		return hiddenEntities.stream().anyMatch(hidden -> covers(hidden, causing));
	}

	/// A hidden entry `hidden` covers a causing entity `causing` if `hidden`'s path is an ancestor of (or equal to) `causing`'s
	/// path and on every layer of `hidden` one of the following applies:
	/// - index is [IIdentifier#ALL_INDICES] (matches every repetition)
	/// - index equals `causing`'s concrete index
	/// - index is an unresolved semantic index ([IIdentifier#UNKNOWN]) whose index-field value equals causing`'s (matched by value,
	///   not position).
	private boolean covers(IIdentifier hidden, IIdentifier causing) {
		if (!isAncestorOrSelf(hidden.getName(), causing.getName())) {
			return false;
		}
		long[] hiddenIndices = hidden.getIndices();
		long[] causingIndices = causing.getIndices();
		// hidden is an ancestor-or-self of causing, so it never has more layers than causing.
		for (int layer = 0; layer < hiddenIndices.length; layer++) {
			long hiddenIndex = hiddenIndices[layer];
			long causingIndex = causingIndices[layer];
			if (hiddenIndex == IIdentifier.ALL_INDICES) {
				continue;
			}
			if (hiddenIndex >= 1 && hiddenIndex == causingIndex) {
				continue;
			}
			if (hiddenIndex == IIdentifier.UNKNOWN && causingIndex == IIdentifier.UNKNOWN
					&& sameIndexValue(hidden, causing)) {
				continue;
			}
			return false;
		}
		return true;
	}

	private static boolean isAncestorOrSelf(String ancestor, String entity) {
		return ancestor.equals(entity) || EntityUtil.isAncestor(ancestor, entity);
	}

	/// Both index values are normalized through the index field's format definition (as in [IndexFieldCache]) before
	/// comparison, so e.g. a numeric index matches regardless of trailing zeros.
	private boolean sameIndexValue(IIdentifier hidden, IIdentifier causing) {
		if (!(hidden instanceof IIdentifierWithSemanticIndex hiddenSemantic)
				|| !(causing instanceof IIdentifierWithSemanticIndex causingSemantic)) {
			return false;
		}
		Optional<String> hiddenValue = hiddenSemantic.getIndexValue();
		Optional<String> causingValue = causingSemantic.getIndexValue();
		if (hiddenValue.isEmpty() || causingValue.isEmpty()) {
			return hiddenValue.equals(causingValue);
		}
		String uniqueFieldName = metaData.getUniqueName(hiddenSemantic.getIndexField().getName());
		return IndexFieldCache.normalizeValue(metaData, uniqueFieldName, hiddenValue.get())
				.equals(IndexFieldCache.normalizeValue(metaData, uniqueFieldName, causingValue.get()));
	}

	// --- old API and counters: forwarded unchanged (Stage 1 post-processes the new API only) ---

	@Override
	public void addInfo(String ruleName, String infoCode, IIdentifier infoField, String infoMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		delegate.addInfo(ruleName, infoCode, infoField, infoMessage, type, referencedFields, refOmissionErrorResponsible,
				unknowns);
	}

	@Override
	public void addHint(String ruleName, String hintCode, IIdentifier hintField, String hintMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		delegate.addHint(ruleName, hintCode, hintField, hintMessage, type, referencedFields, refOmissionErrorResponsible,
				unknowns);
	}

	@Override
	public void addError(String ruleName, String errorCode, IIdentifier errorField, String errorMessage, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		delegate.addError(ruleName, errorCode, errorField, errorMessage, type, referencedFields, refOmissionErrorResponsible,
				unknowns);
	}

	@Override
	public int getCountOf(ErrorType type) {
		return delegate.getCountOf(type);
	}

	@Override
	public int getCount(ErrorType type, SeverityType severityThreshold) {
		return delegate.getCount(type, severityThreshold);
	}
}
