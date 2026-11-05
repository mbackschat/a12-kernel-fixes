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

public class TypedFieldValuePointerImpl<S extends TypedGroupView<S>, T>
		extends AbstractTypedPointer<S, T> {
	/**
	 * casts the field value Object to type T. In the case of enum fields,
	 * this parses the raw string value to the corresponding enum constant.
	 */
	private final Function<Object, T> castOrParseFieldValue;
	/**
	 * converts the typed version of the field value to the untyped field value.
	 * for most field types this is simply the identity function, but for enum
	 * fields this function must convert the enum constant to the corresponding
	 * raw string.
	 */
	private final Function<T, Object> unparseFieldValue;

	protected TypedFieldValuePointerImpl(DocumentPointer untypedPointer,
			Function<GroupInstanceV2, S> wrapStart,
			Function<Object, T> castOrParse,
			Function<T, Object> unparse
	) {
		super(untypedPointer, wrapStart);
		this.castOrParseFieldValue = castOrParse;
		this.unparseFieldValue = unparse;
	}

	public static <S extends TypedGroupView<S>, T> TypedFieldValuePointerImpl<S, T> __internal_derive(
			TypedGroupPointerImpl<S, ?> parent,
			String name,
			Function<Object, T> castOrParse,
			Function<T, Object> unparse
	) {
		return new TypedFieldValuePointerImpl<>(
				parent._unwrap().withAppended(name, 1),
				parent.__internal_wrapStart(),
				castOrParse,
				unparse
		);
	}

	@Override public T __internal_get(S typedStart) {
		GroupInstanceV2 start = typedStart._unwrap();
		Object end = start.fieldValue(this.untypedPointer);
		return this.castOrParseFieldValue.apply(end);
	}

	@Override public S __internal_with(S typedStart, T typedNewFieldValue) {
		GroupInstanceV2 start = typedStart._unwrap();
		GroupInstanceV2 updatedStart;
		if (typedNewFieldValue != null) {
			Object newFieldValue = this.unparseFieldValue.apply(typedNewFieldValue);
			updatedStart = start.withFieldValue(untypedPointer, newFieldValue);
		} else {
			updatedStart = start.withFieldRemoved(untypedPointer);
		}
		return this.wrapStart.apply(updatedStart);
	}
}
