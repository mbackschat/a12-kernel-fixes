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

package com.mgmtp.a12.kernel.md.document.apiV2.immutable;

import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.ImmutableFieldInstanceV2Impl;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.utils.conversion.InstantRange;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion;

@OnlyForUsage
public interface FieldInstanceV2 {
	/**
	 * @return the value of the field instance or null if the field instance has no value.<br/>
	 * 		<b>NOTE</b> in contrast to {@code IFieldInstance#getValue()}, this method does <em>not</em> return an optional.
	 * 		<b>NOTE</b> in contrast to {@code IFieldInstance#getValue()}, dates are represented by {@link java.time.Instant} and date
	 * 		ranges are represented by {@link InstantRange}.
	 */
	Object value();

	FieldInstanceV2 withValue(Object value);

	/**
	 * Instances of FieldInstanceV2 are compared for equality based on their current value, independent of the concrete
	 * sub-interface or subclass.
	 */
	@Override boolean equals(Object other);

	/**
	 * see {@link #equals}
	 */
	@Override int hashCode();

	// UTILS =====

	/**
	 * @param value
	 * 		the value of the field instance or null if the field instance has no value.<br/>
	 * 		Note that in v2 documents, some date-related field values are represented by different types that in v1
	 * 		documents, see {@link FieldInstanceV2#value}.
	 */
	static FieldInstanceV2 ofValue(Object value) {
		return ImmutableFieldInstanceV2Impl.ofValueInternal(value);
	}

	/**
	 * @param v1Value
	 * 		the value of a document v1 field value type (e.g. Dates instead of Instant, Date[] instead of Instant pairs),
	 * 		as it is returned by {@code IFieldInstance#getValue().orElse(null)}
	 * @deprecated IDocument (V1) and the associated BasicTypeV1 are deprecated. With a corresponding V2 value,
	 *        {@link #ofValue(Object)} can be used instead.
	 */
	@Deprecated(since = "30.0.0")
	static FieldInstanceV2 ofV1Value(Object v1Value) {
		return ImmutableFieldInstanceV2Impl.ofV1ValueInternal(v1Value);
	}

	/**
	 * @deprecated this method is no longer needed, because IDocument (V1) and the associated BasicTypeV1 are deprecated.
	 */
	@Deprecated(since = "30.0.0")
	default Object toV1Value() {
		return BasicTypeV1V2Conversion.basicTypeV2ToV1(this.value());
	}

	/**
	 * @param v1Value
	 * 		the value of a document v1 field value type (e.g. Dates instead of Instant, Date[] instead of Instant pairs),
	 * 		as it is returned by {@code IFieldInstance#getValue().orElse(null)}
	 * @deprecated IDocument (V1) and the associated BasicTypeV1 are deprecated. With a corresponding V2 value,
	 *        {@link #withValue(Object)} can be used instead.
	 */
	@Deprecated(since = "30.0.0")
	default FieldInstanceV2 withV1Value(Object v1Value) {
		return this.withValue(BasicTypeV1V2Conversion.basicTypeV1ToV2(v1Value));
	}
}
