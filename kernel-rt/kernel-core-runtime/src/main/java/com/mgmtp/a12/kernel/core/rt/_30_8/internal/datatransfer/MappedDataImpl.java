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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer;

import java.util.List;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.MappedData;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.MappedField;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

public record MappedDataImpl(
		boolean success,
		Set<IIdentifier> removedEntities,
		List<MappedField> mappedFields,
		Set<String> selectedSourceIds
) implements MappedData {
		// since A12K-3395, the mapping is always considered successful - if there are some
		// failures during precomputation,
		// unaffected fields should still be mapped. But lets not make a breaking change
		// to the API just yet (more stability, and maybe we discover new failure
		// cases as the mapping topic stabilizes) - instead, simply set success always to true.

	public static MappedDataImpl success(
			Set<IIdentifier> removedEntities, List<MappedField> mappedFields, Set<String> includedSourceIds) {
		return new MappedDataImpl(true, removedEntities, mappedFields, includedSourceIds);
	}
}
