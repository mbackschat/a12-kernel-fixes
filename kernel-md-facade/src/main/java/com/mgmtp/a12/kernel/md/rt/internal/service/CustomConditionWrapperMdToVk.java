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

package com.mgmtp.a12.kernel.md.rt.internal.service;

import java.util.Arrays;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class CustomConditionWrapperMdToVk implements ICustomCondition {
	private final @NonNull com.mgmtp.a12.kernel.md.rt.api.ICustomCondition customCondition;

	public static @NonNull ICustomCondition wrap(
			@NonNull com.mgmtp.a12.kernel.md.rt.api.ICustomCondition customCondition
	) {
		return customCondition instanceof CustomConditionWrapperVkToMd wrapper
				? wrapper.unwrap()
				: new CustomConditionWrapperMdToVk(customCondition);
	}

	@Override
	public boolean check(IData data, Set<IIdentifier> relevantEntities, Set<IIdentifier> formallyIncorrectFields,
			IIdentifier errorField) {
		final Set<DocumentMultiPointer> relevantFieldInstances = (relevantEntities == null) ?
				null :
				DocumentPointerConverter.convertToDocumentMultiPointerSet(relevantEntities);
		final Set<DocumentPointer> formallyIncorrectEntityInstances = (formallyIncorrectFields == null) ?
				Set.of() :
				DocumentPointerConverter.convertToDocumentPointerSet(formallyIncorrectFields);

		PartiallyKnownDocumentMultiPointer errorEntityInstance = PartiallyKnownDocumentMultiPointer.of(errorField.getName(),
				Arrays.stream(errorField.getIndices()).mapToInt(i -> (int) i).boxed().toList());
		if (!(data instanceof ValidationData validationData)) {
			throw new IllegalStateException(
					"A custom condition provided through the md-API must not be used by validation calls which bypass the md-API.");
		}

		DocumentV2 documentV2 = validationData.getInternalDocumentV2();
		return customCondition.check(documentV2, relevantFieldInstances,
				formallyIncorrectEntityInstances, errorEntityInstance);
	}
}
