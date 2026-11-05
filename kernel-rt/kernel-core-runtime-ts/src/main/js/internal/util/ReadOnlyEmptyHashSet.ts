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
import { HashSet } from "@com.mgmtp.a12.utils/utils-collections";

/**
 * An immutable set that is always empty.
 */
export class ReadOnlyEmptyHashSet<T> extends HashSet<T> {
	private static readonly _INSTANCE = new ReadOnlyEmptyHashSet<never>();

	/**
	 * @return the shared empty set. The element type is irrelevant at runtime, as the set never
	 * 		contains an element.
	 */
	public static instance<T>(): ReadOnlyEmptyHashSet<T> {
		return ReadOnlyEmptyHashSet._INSTANCE as ReadOnlyEmptyHashSet<T>;
	}

	public override add(_value: T): this {
		throw new Error("Internal error: ReadOnlyEmptyHashSet must not be modified.");
	}

	public override tryAdd(_value: T): boolean {
		throw new Error("Internal error: ReadOnlyEmptyHashSet must not be modified.");
	}

	public override delete(_value: T): boolean {
		return false;
	}

	public override clear(): void {
		// nothing to do, the set is always empty
	}
}
