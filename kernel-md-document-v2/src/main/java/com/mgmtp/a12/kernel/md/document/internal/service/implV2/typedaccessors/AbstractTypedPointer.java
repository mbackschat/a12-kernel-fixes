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

import java.util.Objects;
import java.util.function.Function;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedGroupView;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedPointer;

public abstract class AbstractTypedPointer<S extends TypedGroupView<S>, T> implements TypedPointer<S, T> {
	protected final DocumentPointer untypedPointer;
	protected final Function<GroupInstanceV2, S> wrapStart;

	public AbstractTypedPointer(DocumentPointer untypedPointer, Function<GroupInstanceV2, S> wrapStart) {
		this.untypedPointer = untypedPointer;
		this.wrapStart = wrapStart;
	}

	@Override public DocumentPointer _unwrap() {
		return untypedPointer;
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AbstractTypedPointer<?, ?> that = (AbstractTypedPointer<?, ?>) o;
		return Objects.equals(untypedPointer, that.untypedPointer);
	}

	@Override public int hashCode() {
		return Objects.hash(getClass(), untypedPointer);
	}

	@Override public String toString() {
		return this.getClass().getName() + ": " + untypedPointer;
	}
}
