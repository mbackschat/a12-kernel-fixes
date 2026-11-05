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

package com.mgmtp.a12.kernel.mapping.rt.api;

import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceIdentifier;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

@OnlyForUsage
public interface SourceField {
	String ID_FOR_TARGET = SourceIdentifier.ID_FOR_TARGET;
	String ID_FOR_PRECOMPUTATION_DM = SourceIdentifier.ID_FOR_PRECOMPUTATION_DM;

	/**
	 * @return If the field is from one of the source documents, then this method returns the id that was given
	 *        {@link SourceDocument#sourceInstanceId()} for that source document. If the field is from the target
	 * 		document, {@link #ID_FOR_TARGET} will be returned and if the field only exists in the precomputation document (i.e. it is
	 * 		a precomputation helper field), then {@link #ID_FOR_PRECOMPUTATION_DM} is returned.
	 */
	@NonNull String sourceInstanceId();

	@NonNull DocumentPointer field();

	static @NonNull SourceField of(
			@NonNull String sourceInstanceId,
			@NonNull DocumentPointer field
	) {
		return new SourceFieldImpl(sourceInstanceId, field);
	}
}
