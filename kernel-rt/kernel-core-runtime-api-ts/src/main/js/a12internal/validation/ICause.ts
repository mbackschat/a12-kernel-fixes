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

import type { IIdentifier } from "./IIdentifier.js";

/**
 * Contains the causing entities.
 *
 * This interface should not be implemented outside the kernel.
 */
export interface ICause {
	/**
	 * Returns the causing entities. Groups of entities are gathered to prioritize them.
	 *
	 * The current implementation uses the following group assignments:
	 * - For {@link ErrorType.OMISSION_ERROR} the first group of entities consists of the empty
	 *   entities causing the omission error. The second group consists of the given entities.
	 * - For {@link ErrorType.VALUE_ERROR} all potentially causing entities are returned as a single
	 *   group.
	 *
	 * Entities of the earlier groups are more probable to cause the error from a technical point of
	 * view.
	 */
	getCausingEntityGroups(): Set<IIdentifier>[];

	/**
	 * @return the entity specifying the iteration, the error occurred in, or `null`.
	 */
	getIterationEntity(): IIdentifier | null;

	/**
	 * @return an {@link ICause} that is like this one, but with all identifiers in
	 * {@link getCausingEntityGroups} and {@link getIterationEntity} mapped according to
	 * `mapIdentifiers`.
	 */
	withMappedIdentifiers(mapIdentifiers: (id: IIdentifier) => IIdentifier): ICause;
}
