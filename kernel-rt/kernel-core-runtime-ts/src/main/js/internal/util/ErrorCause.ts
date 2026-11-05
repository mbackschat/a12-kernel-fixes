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

import type { ICause, IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { HashSet } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

/**
 * The default implementation for the interface {@link ICause}.
 */
export class ErrorCause implements ICause {

	constructor(
        private readonly _iterationGroup: IIdentifier,
        private readonly _causingEntityGroups: Set<IIdentifier>[]
    ) {
	}

	public getIterationEntity(): IIdentifier {
		return this._iterationGroup;
	}

	public getCausingEntityGroups(): Set<IIdentifier>[] {
		return this._causingEntityGroups;
	}

	public withMappedIdentifiers(mapIdentifiers: (id: IIdentifier) => IIdentifier): ErrorCause {
		return new ErrorCause(
			mapIdentifiers(this._iterationGroup),
			this._causingEntityGroups.map(set => {
				const mapped: HashSet<IIdentifier> = new HashSet<IIdentifier>();
				for (const id of set) {
					mapped.add(mapIdentifiers(id));
				}
				return mapped;
			})
		);
	}

	/**
	 * @return an {@link ICause} for a single entity `id`: a single group containing only `id`, with
	 * `id` as the iteration entity.
	 */
	public static createSingleFieldCause(id: IIdentifier): ErrorCause {
		return new ErrorCause(id, [new HashSet<IIdentifier>([id])]);
	}
}
