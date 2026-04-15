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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer.mappingmodel.IPrecompModelDataMapper;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;

public class SplittingDelegatingResultForFormalValidationErrors implements IResult {
	private final IPrecompModelDataMapper mapperWithMapBackState;
	private final IResult targetResult;
	private final IResult precomputationConsistencyResult;
	private final Function<SourceIdentifier, String> customResultChooser;

	/**
	 * @param customResultChooser allows to redirect a received message to a different result, by picking an alternative sourceId
	 *                              for the mapped-back error field (the returned sourceId must be one of
	 *                              {@link SourceIdentifier#ID_FOR_TARGET}, {@link SourceIdentifier#ID_FOR_PRECOMPUTATION_DM} or
	 *                              one of the source's sourceIds. The default impl is {@link SourceIdentifier::sourceId}
	 */
	public SplittingDelegatingResultForFormalValidationErrors(IPrecompModelDataMapper mapperWithMapBackState, IResult targetResult,
			IResult precomputationConsistencyResult,
			Function<SourceIdentifier, String> customResultChooser) {
		this.mapperWithMapBackState = mapperWithMapBackState;
		this.targetResult = targetResult;
		this.precomputationConsistencyResult = precomputationConsistencyResult;
		this.customResultChooser = customResultChooser;
	}

	@Override public void addMessageV2(String ruleName, String errorCode, String messageStr, ErrorType type,
			SeverityType severity, ICause possiblyCausingEntities) {
		IIdentifier errorField = findErrorFieldForFormalValidationError(possiblyCausingEntities);
		IResult matchingResult = findMatchingResult(errorField, mapperWithMapBackState, targetResult,
				precomputationConsistencyResult);
		if (matchingResult != null) {
			matchingResult.addMessageV2(ruleName, errorCode, messageStr, type, severity,
				possiblyCausingEntities.withMappedIdentifiers(mapperWithMapBackState::mapIdentifierBack)
			);
		}
	}

	@Override public void addInfo(String ruleName, String infoCode, IIdentifier infoField, String infoMessage,
			ErrorType type, Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible,
			Set<IIdentifier> unknowns) {
		IResult matchingResult = findMatchingResult(infoField, mapperWithMapBackState, targetResult,
				precomputationConsistencyResult);

		addMessageV1IfResultNotNull(matchingResult, V1MessageType.INFO, ruleName, infoCode, infoField, infoMessage, type,
				referencedFields, refOmissionErrorResponsible, unknowns);
	}

	@Override public void addHint(String ruleName, String hintCode, IIdentifier hintField, String hintMessage,
			ErrorType type, Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible,
			Set<IIdentifier> unknowns) {
		IResult matchingResult = findMatchingResult(hintField, mapperWithMapBackState, targetResult,
				precomputationConsistencyResult);

		addMessageV1IfResultNotNull(matchingResult, V1MessageType.HINT, ruleName, hintCode, hintField, hintMessage, type,
				referencedFields, refOmissionErrorResponsible, unknowns);
	}

	@Override public void addError(String ruleName, String errorCode, IIdentifier errorField, String errorMessage,
			ErrorType type, Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible,
			Set<IIdentifier> unknowns) {
		IResult matchingResult = findMatchingResult(errorField, mapperWithMapBackState, targetResult,
				precomputationConsistencyResult);

		addMessageV1IfResultNotNull(matchingResult, V1MessageType.ERROR, ruleName, errorCode, errorField, errorMessage, type,
				referencedFields, refOmissionErrorResponsible, unknowns);
	}

	private enum V1MessageType {HINT, INFO, ERROR}
	private void addMessageV1IfResultNotNull(IResult result, V1MessageType whatMethodToCall, String ruleName, String msgCode, IIdentifier msgField, String message,
			ErrorType type, Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible,
			Set<IIdentifier> unknowns) {
		if(result == null) {
			return;
		}
		// NOTE: in principle, the referenced fields (incl error field) could be from more than one origin (multiple sources, target or precomp
		// fragment), but we don't expect this to actually happen in the context of the datatransfer:
		// - validation rules are only evaluated for individual sources - those rules can't reference fields from outside the source
		// - computations shouldn't report validation errors
		// - formal validation (performed as a first step during precomputation) looks at one field at a time.
		// If for some reason this would nonetheless happen, our code here should deal with it juts fine, but the caller would have
		// to interpret the mapped SourceIdentifiers correctly without assuming that they all have the same sourceId.
		msgField = mapperWithMapBackState.mapIdentifierBack(msgField);
		referencedFields = referencedFields.stream().map(mapperWithMapBackState::mapIdentifierBack).collect(Collectors.toSet());
		refOmissionErrorResponsible = refOmissionErrorResponsible.stream().map(mapperWithMapBackState::mapIdentifierBack).collect(Collectors.toSet());
		unknowns = unknowns.stream().map(mapperWithMapBackState::mapIdentifierBack).collect(Collectors.toSet());
		switch (whatMethodToCall) {
			case HINT: result.addHint(ruleName, msgCode, msgField, message, type, referencedFields, refOmissionErrorResponsible, unknowns);
				break;
			case INFO: result.addInfo(ruleName, msgCode, msgField, message, type, referencedFields, refOmissionErrorResponsible, unknowns);
				break;
			case ERROR: result.addError(ruleName, msgCode, msgField, message, type, referencedFields, refOmissionErrorResponsible, unknowns);
				break;
			default: throw new RuntimeException("unexpected case: " + whatMethodToCall);
		}
	}

	@Override public int getCountOf(ErrorType type) { // we need this impl to use getCountOf(ErrorType) directly (instead of the
		// default impl) until all relevant implementations have implemented getCount(ErrorType, SeverityType) - otherwise,
		// DataTransferServiceImpl#getIResultCountWithFallback will not work
		return targetResult.getCountOf(type) + precomputationConsistencyResult.getCountOf(type);
	}

	@Override public int getCount(ErrorType type, SeverityType severityThreshold) {
		return Stream.of(targetResult, precomputationConsistencyResult)
				.mapToInt(r -> r.getCount(type, severityThreshold))
				.sum();
	}

	/**
	 * currently, only the target and precomputation result are suppored. If customResultChooser chooses
	 * the id of a source for the mapped-back sourceIdentifier, then null will be returned.
	 */
	private IResult findMatchingResult(
			IIdentifier errorField, IPrecompModelDataMapper mapperWithMapBackState,
			IResult targetResult, IResult precomputationConsistencyResult
	) {
		var mappedBack = mapperWithMapBackState.mapIdentifierBack(errorField);
		String effectiveSourceId = this.customResultChooser.apply(mappedBack);
		if (effectiveSourceId.equals(SourceIdentifier.ID_FOR_PRECOMPUTATION_DM)) {
			return precomputationConsistencyResult;
		} else if (effectiveSourceId.equals(SourceIdentifier.ID_FOR_TARGET)) {
			return targetResult;
		} else {
			assert false : "expected customResultChooser to always choose either target or precomp, " +
					"but apparently a source-data sourceId was chosen: " + effectiveSourceId
					+ ". This is not currently suppored by SplittingDelegatingResultForFormalValidationErrors";
			return null;
		}
	}

	private static IIdentifier findErrorFieldForFormalValidationError(ICause possiblyCausingEntities) {
		List<IIdentifier> referencedIds = new ArrayList<>();
		for (var ids : possiblyCausingEntities.getCausingEntityGroups()) {
			referencedIds.addAll(ids);
		}
		assert referencedIds.size() == 1 : "only errors from formal validation are expected here, which should have exactly one possiblyCausingEntity";
		return referencedIds.get(0);
	}
}
