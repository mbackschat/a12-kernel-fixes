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

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedGroupView;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedRepeatableGroupPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedRepetitionsPointer;

/**
 * @param <R>
 * 		the wrapped raw (untyped) document node type (either GroupInstanceV2 or DocumentV2)
 * @param <S>
 * 		the self-type of the typed view class that extends AbstractTypedView
 */
public abstract class AbstractTypedView<R extends GroupInstanceV2, S extends AbstractTypedView<R, S>>
		implements TypedGroupView<S> // Note: complies to TypedDocumentView too if R extends DocumentV2
{
	private final R wrappedUntyped;

	protected AbstractTypedView(R wrappedUntyped) {
		this.wrappedUntyped = wrappedUntyped;
	}

	@Override public R _unwrap() {
		return wrappedUntyped;
	}

	@Override public <T> T _at(TypedPointer<S, T> pointer) {
		return pointer.__internal_get(__internal_self());
	}

	@Override public <T> S _with(TypedPointer<S, T> pointer, T update) {
		return pointer.__internal_with(__internal_self(), update);
	}

	@Override public <T> S _withInsertedRepetition(TypedRepeatableGroupPointer<S, T> pointer, T update) {
		return pointer.__internal_withInsertedRepetition(__internal_self(), update);
	}

	@Override public <T> S _withAppendedRepetition(TypedRepetitionsPointer<S, T, ?> pointer, T update) {
		return pointer.__internal_withAppendedRepetition(__internal_self(), update);
	}

	/**
	 * This method is just a workaround for a limitation of the java type system: it allows superclass methods to access "this"
	 * _typed as an instance of the implementing subclass_
	 */
	abstract protected S __internal_self();

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AbstractTypedView<?, ?> that = (AbstractTypedView<?, ?>) o;
		return Objects.equals(wrappedUntyped, that.wrappedUntyped);
	}

	@Override public int hashCode() {
		return Objects.hash(getClass(), wrappedUntyped);
	}

	@Override public String toString() {
		return this.getClass().getName() + ": " + _unwrap().toString();
	}
}
