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

package com.mgmtp.a12.kernel.md.document.apiV2.typed;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * @param <S>
 * 		the self-type of the typed view class that implements TypedGroupView
 */
@OnlyForUsage // must only be implemented by kernel-provided or kernel-generated typed accessor classes
public interface TypedGroupView<S extends TypedGroupView<S>> {
	GroupInstanceV2 _unwrap();

	/**
	 * @param <T>
	 * 		the target type of the pointer, e.g. if the pointer points to /a/b/c,
	 * 		then T will be the TypedGroupView subclass for c.
	 */
	<T> T _at(TypedPointer<S, T> pointer);

	/**
	 * @param <T>
	 * 		see param T of {@link #_at}
	 */
	<T> S _with(TypedPointer<S, T> pointer, T update);

	/**
	 * @param <T>
	 * 		see param T of {@link #_at}
	 */
	<T> S _withInsertedRepetition(TypedRepeatableGroupPointer<S, T> pointer, T update);

	/**
	 * @param <T>
	 * 		see param T of {@link #_at}
	 */
	<T> S _withAppendedRepetition(TypedRepetitionsPointer<S, T, ?> pointer, T update);

	/**
	 * Equals compares both the view class and the wrapped group or document
	 */
	@Override boolean equals(Object o);

	/**
	 * see {@link #equals(Object)}
	 */
	@Override int hashCode();
}
