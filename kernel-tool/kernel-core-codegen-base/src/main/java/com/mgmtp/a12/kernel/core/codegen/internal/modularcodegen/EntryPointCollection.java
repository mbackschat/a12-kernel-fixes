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

package com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen;

import java.util.List;

/**
 * An implementation of EntryPoints that wraps a list of EntryPoints. Intended for
 * cases where it is not statically known what EntryPoints will be present in a codegen result
 * (e.g. packet code generation where the parameters determine the subset of created code modules).
 * <p/>
 * It should probably be avoided to add more than one EntryPoints of the same type, as this may or may not lead to ambiguity.
 */
public class EntryPointCollection implements IEntryPoints {
	private final List<IEntryPoints> entryPoints;
	private EntryPointCollection(final List<IEntryPoints> entryPoints) {
		this.entryPoints = entryPoints;
	}
	public static EntryPointCollection of(final List<IEntryPoints> entryPoints) {
		return new EntryPointCollection(entryPoints);
	}
	public List<IEntryPoints> asList() {
		return entryPoints;
	}

	/**
	 * if there is a single entry point that is assignable to expectedSubtype, return it; if there is none, return null; if there
	 * are more than one, throw a runtime exception.
	 */
	public <E extends IEntryPoints> E getUniqueMatchingEntryPoint(Class<E> expectedSubtype) {
		List<E> matching = this.entryPoints.stream()
				.filter(ep -> expectedSubtype.isAssignableFrom(ep.getClass()))
				.map(expectedSubtype::cast)
				.toList();
		if (matching.size() > 1) {
			throw new RuntimeException("expected exactly one matching entryPoint descriptor but got:  " + matching);
		}
		return matching.isEmpty() ? null : matching.get(0);
	}
}
