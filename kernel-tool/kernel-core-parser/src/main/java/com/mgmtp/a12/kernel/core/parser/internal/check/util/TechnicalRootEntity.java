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

package com.mgmtp.a12.kernel.core.parser.internal.check.util;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

/**
 * A12K-4276: virtual entity for the technical {@code DocumentModelRoot} (path {@code "/"}, one level above the root groups), used
 * as the turning point of a relative upward path reaching into a sibling root group (e.g. {@code "../../RG2/x"}). It is not an
 * indexable level ({@link #getMax()} is empty, callers add no index); ending on it is rejected by the entity-spec checks.
 */
public final class TechnicalRootEntity implements IEntity {

	public static final TechnicalRootEntity INSTANCE = new TechnicalRootEntity();

	private static final long[] NO_REPETITION_LAYERS = new long[0];

	private TechnicalRootEntity() {
	}

	@Override
	public String getFullName() {
		return EntityUtils.SEPARATOR;
	}

	@Override
	public long[] getMax() {
		return NO_REPETITION_LAYERS.clone();
	}
}
