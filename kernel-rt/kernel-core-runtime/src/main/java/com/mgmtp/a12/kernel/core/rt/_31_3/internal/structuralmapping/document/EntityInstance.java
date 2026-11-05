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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.document;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.Named;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.structuralmapping.Parentable;

public interface EntityInstance extends Parentable<EntityInstance, GroupInstance>, Named {
	interface Origin {
		/**
		 * see {@link #isFromInput()}
		 */
		Origin FROM_INPUT = new Origin() {
		};

		Origin UNKNOWN = new Origin() {
		};

		/**
		 * @return {@code true} if the entity instance was part of the initial document provided by the user.
		 */
		default boolean isFromInput() {
			return this == FROM_INPUT;
		}
	}

	/**
	 * @return the entity the receiver is an instance of.
	 */
	Entity entity();

	Origin origin();

	@Override
	default EntityInstance self() {
		return this;
	}

	/**
	 * @return the repetition of this entity instance (1-based).
	 */
	int repetition();

	/**
	 * @return {@code true} if the receiver is present in the document, otherwise {@code false}.
	 */
	boolean isPresent();

	default boolean isAbsent() {
		return !isPresent();
	}

	EntityInstance createIfAbsent(Origin origin);

	/**
	 * @return the short name of this instance's entity (e.g. {@literal "MyField"} for entity with full name
	 *        {@literal "/Root/GroupA/MyField"}).
	 */
	@Override
	default String shortName() {
		return entity().shortName();
	}

	/**
	 * @return the full name of this instance's entity (e.g. {@literal "/Root/GroupA/MyField"}).
	 */
	@Override
	default String fullName() {
		return entity().fullName();
	}

	@Override
	default int level() {
		return entity().level();
	}

	/**
	 * @return an array of the repetitions of the receiver and all ancestors, ordered from lowest to highest level. Does not include
	 * 		root.
	 */
	default long[] repetitions() {
		long[] repetitions = new long[level()];
		EntityInstance ancestor = this;
		while (!ancestor.isRoot()) {
			repetitions[ancestor.levelIndex()] = ancestor.repetition();
			ancestor = ancestor.parent();
		}
		return repetitions;
	}

	/**
	 * Debug utility only.
	 *
	 * @return the JSON representation of the receiver. Not guaranteed to be valid JSON!
	 */
	default String printJson() {
		StringBuilder sb = new StringBuilder();
		printJsonOn(sb);
		return sb.toString();
	}

	/**
	 * Debug utility only.
	 * <p>
	 * Not guaranteed to produce valid JSON!
	 */
	void printJsonOn(StringBuilder sb);
}
