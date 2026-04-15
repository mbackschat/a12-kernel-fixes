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

package com.mgmtp.a12.kernel.md.document.internal.service.implV2.typedaccessors;

import java.util.function.Function;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedGroupView;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedRepeatableGroupPointer;

public class TypedRepeatableGroupPointerImpl<
		S extends TypedGroupView<S>,
		T extends TypedGroupView<T>>
		extends TypedGroupPointerImpl<S, T>
		implements TypedRepeatableGroupPointer<S, T> {
	protected TypedRepeatableGroupPointerImpl(DocumentPointer untypedPointer,
			Function<GroupInstanceV2, S> wrapStart,
			Function<GroupInstanceV2, T> wrapEnd
	) {
		super(untypedPointer, wrapStart, wrapEnd);
	}

	@Override public S __internal_withInsertedRepetition(S typedStart, T typedNewSubgroup) {
		if (typedNewSubgroup == null) {
			throw new NullPointerException("typedNewSubgroup must be non-null");
		}
		GroupInstanceV2 start = typedStart._unwrap();
		GroupInstanceV2 newValueUntyped = typedNewSubgroup._unwrap();
		GroupInstanceV2 updatedStart = start.withGroupRepetitionInserted(_unwrap(), newValueUntyped);
		return this.__internal_wrapStart().apply(updatedStart);
	}
}
