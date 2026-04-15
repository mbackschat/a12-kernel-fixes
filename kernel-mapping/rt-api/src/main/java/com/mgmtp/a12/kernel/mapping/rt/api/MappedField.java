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

import java.util.Set;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion;

import lombok.NonNull;

@OnlyForUsage
public interface MappedField {
	/**
	 * @return the field instance in the target document that was mapped into
	 */
	@NonNull DocumentPointer targetField();

	/**
	 * @return the new value for {@link #targetField()}. Note: this is a value suitable for
	 *        {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2}, not for
	 *        {@link com.mgmtp.a12.kernel.md.document.api.IDocument}; see {@link BasicTypeV1V2Conversion} for details.
	 */
	// currently this can probably not be null, but it seems plausible that we might allow null here in a future version, as a way to selectively clear fields in the target?
	Object value();

	/**
	 * @return the field instances that have directly contributed to the mapped field value, excluding additional field instances that may
	 * 		have affected the value indirectly via case distinctions (conditions) in precomputations.
	 */
	@NonNull Set<SourceField> operationSourceFields();

	static @NonNull MappedField of(
			@NonNull DocumentPointer targetField,
			Object value,
			@NonNull Set<SourceField> operationSourceFields
	) {
		return new MappedFieldImpl(targetField, value, operationSourceFields);
	}
}
