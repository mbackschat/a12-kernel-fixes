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

package com.mgmtp.a12.kernel.md.document.api;

import com.mgmtp.a12.kernel.md.utils.CustomImplementationIsOptional;

/**
 * Defines ways to create a clone of {@link IEntityInstance} but changing a specific property.
 *
 * @deprecated use {@code DocumentPointer} or {@code DocumentMultiPointer} instead.
 */
@CustomImplementationIsOptional
@Deprecated(since = "30.0.0")
public interface IEntityInstanceDerivable extends IEntityInstance {
	/**
	 * Clones {@code this} but with a different path.
	 *
	 * @param newPath
	 * 		the new path which the clone should have. May not be {@code null} or empty.
	 * @return the clone with the new path. May not be {@code null}.
	 */
	IEntityInstanceDerivable withPath(String newPath);

	/**
	 * Clones {@code this} but with different repetitions.
	 *
	 * @param newRepetitions
	 * 		the new repetitions which the clone should have. May not be {@code null}.
	 * @return the clone with the new repetitions. May not be {@code null}.
	 */
	IEntityInstanceDerivable withRepetitions(int[] newRepetitions);
}
