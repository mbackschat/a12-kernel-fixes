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

import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV2InternalUtils.assertLastRepIsWildcard;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedGroupView;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedRepetitionsPointer;

public class TypedRepetitionsPointerImpl<
		S extends TypedGroupView<S>,
		T extends TypedGroupView<T>,
		P extends TypedPointer<S, T>
		>
		extends AbstractTypedPointer<S, List<T>>
		implements TypedRepetitionsPointer<S, T, P> {
	private final BiFunction<Function<GroupInstanceV2, S>, DocumentPointer, P> mkSingleRepPointer;
	private final Function<GroupInstanceV2, T> wrapEnd;

	protected TypedRepetitionsPointerImpl(DocumentPointer untypedPointer, // last idx is ignored
			BiFunction<Function<GroupInstanceV2, S>, DocumentPointer, P> mkSingleRepPointer, // TODO minimize abstraction
			Function<GroupInstanceV2, S> wrapStart,
			Function<GroupInstanceV2, T> wrapEnd
	) {
		super(untypedPointer, wrapStart);
		assertLastRepIsWildcard(untypedPointer);
		this.mkSingleRepPointer = mkSingleRepPointer;
		this.wrapEnd = wrapEnd;
	}

	@Override public P get(int oneBasedRepetition) {
		DocumentPointer repetitionPointer = this.untypedPointer.withLastRepetitionReplaced(oneBasedRepetition);
		return this.mkSingleRepPointer.apply(this.wrapStart, repetitionPointer);
	}

	 public static <
	 	S extends TypedGroupView<S>,
	 	T extends TypedGroupView<T>,
	 	P extends TypedPointer<S, T>
	 	> TypedRepetitionsPointerImpl<S, T, P> __internal_derive(
				DocumentPointer parentPointer,
			 	String name,
			 	Function<GroupInstanceV2, S> wrapStart,
			 	Function<GroupInstanceV2, T> wrapEnd,
			 	BiFunction<Function<GroupInstanceV2, S>, DocumentPointer, P> mkSingleRepPointer
	 ) {
		 int wildCardRep = 0;
		 return new TypedRepetitionsPointerImpl<>(
				 parentPointer.withAppended(name, wildCardRep),
				 mkSingleRepPointer,
				 wrapStart,
				 wrapEnd
		);
	}

	@Override public List<T> __internal_get(S typedStart) {
		GroupInstanceV2 start = typedStart._unwrap();
		RepetitionsV2 end = start.groupAllRepetitions(this.untypedPointer);
		return end.stream().map(this.wrapEnd).toList();
	}

	@Override public S __internal_with(S typedStart, List<T> typedNewSubgroupRepetitions) {
		GroupInstanceV2 start = typedStart._unwrap();
		GroupInstanceV2 updatedStart;
		if (typedNewSubgroupRepetitions != null) {
			RepetitionsV2 newValueUntyped = RepetitionsV2.of(typedNewSubgroupRepetitions.stream().map(T::_unwrap).toList());
			updatedStart = start.withGroupAllRepetitions(untypedPointer, newValueUntyped);
		} else {
			updatedStart = start.withGroupAllRepetitionsRemoved(untypedPointer);
		}
		// NOTE this internal method in pointer doesn't know if orig is a group or document, but it only gets called from
		// the typed view that knows the specific subtype and casts accordingly
		return this.wrapStart.apply(updatedStart);
	}

	@Override public S __internal_withAppendedRepetition(S typedStart, T typedNewSubgroup) {
		if (typedNewSubgroup == null) {
			throw new NullPointerException("typedNewSubgroup must be non-null");
		}
		GroupInstanceV2 start = typedStart._unwrap();
		GroupInstanceV2 newValueUntyped = typedNewSubgroup._unwrap();
		GroupInstanceV2 updatedStart = start.withGroupRepetitionAppended(this.untypedPointer, newValueUntyped);
		return this.wrapStart.apply(updatedStart);
	}
}
