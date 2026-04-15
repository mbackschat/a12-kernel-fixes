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

public class TypedGroupPointerImpl<
		    S extends TypedGroupView<S>,
		    T extends TypedGroupView<T>>
	extends AbstractTypedPointer<S, T>
{
	private final Function<GroupInstanceV2, T> wrapEnd;

	protected TypedGroupPointerImpl(DocumentPointer untypedPointer,
			Function<GroupInstanceV2, S> wrapStart,
			Function<GroupInstanceV2, T> wrapEnd
	) {
		super(untypedPointer, wrapStart);
		this.wrapEnd = wrapEnd;
	}

	@Override public T __internal_get(S typedStart) {
		GroupInstanceV2 start = typedStart._unwrap();
		GroupInstanceV2 end = start.group(this.untypedPointer);
		return this.wrapEnd.apply(end);
	}

	@Override public S __internal_with(S typedStart, T typedNewSubgroup) {
		GroupInstanceV2 start = typedStart._unwrap();
		GroupInstanceV2 updatedStart;
		if(typedNewSubgroup != null) {
			GroupInstanceV2 newValueUntyped = typedNewSubgroup._unwrap();
			updatedStart = start.withGroup(untypedPointer, newValueUntyped);
		} else {
			updatedStart = start.withGroupRemoved(untypedPointer);
		}
		// NOTE this internal method in pointer doesn't know if orig is a group or document, but it only gets called from
		// the typed view that knows the specific subtype and casts accordingly
		return this.wrapStart.apply(updatedStart);
	}

	/**
	 *
	 * @deprecated this method is now redundant, use {@link #_unwrap()} instead.
	 * It may be removed in some future release but is kept here for compatibility with generated
	 * typed accessor classes from previous versions.
	 */
	@Deprecated
	public DocumentPointer __internal_untypedPointer() {
		return untypedPointer;
	}
	public Function<GroupInstanceV2, S> __internal_wrapStart() {
		return wrapStart;
	}

	protected static Object __unwrapAndCheckConfirmFieldValue(Boolean value) {
		if (Boolean.FALSE.equals(value)) { // This check is a bit unusual for the typed overlays, because it does a check at
			// runtime, whereas the typed-overlays are otherwise mostly compile-time only checks. But currently it seems
			// like it is nonetheless good to check this here, as it is quite likely to alert users to incorrect usage in cases
			// where the static type checks give a false sense of security, and seems unlikely to break something that is not
			// already broken (and there is an escape hatch, see the exception message below).
			throw new IllegalArgumentException("Boolean.FALSE is not a valid value for confirm fields. " +
					"If you absolutely must set Boolean.FALSE to a field that has type 'Confirm' in the document model, a possible " +
					"workaround would be to _unwrap() the group or document, set the value, and re-wrap it with a typed overlay.");
		}
		return value;
	}
}
