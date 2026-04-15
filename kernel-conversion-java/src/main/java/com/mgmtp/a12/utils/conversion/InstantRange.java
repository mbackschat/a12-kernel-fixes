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

// NOTE: this class is meant to be eventually moved out ouf kernel into the utils (conversion?) repository, so we already
// use a kernel-independent package name. This way, a later move to the separate repo can be non-breaking for projects that
// interact with data from documents (InstantRange is one of the valid types for field vaules in DocumentV2).
package com.mgmtp.a12.utils.conversion;

import java.time.Instant;

import com.mgmtp.a12.utils.conversion.internal.InstantRangeImpl;

/**
 * implementations must be immutable
 * <p/>
 * <em>NOTE</em> the caller must not implement the marked target. Kernel provides the implementation. If kernel
 * adds new methods, this will not be considered as a breaking change. (@OnlyForUsage)
 */
public interface InstantRange {
	Instant start();
	Instant end();
	InstantRange withStart(Instant start);
	InstantRange withEnd(Instant end);
	static InstantRange of(Instant start, Instant end) {
		return InstantRangeImpl.ofInternal(start, end);
	}
}