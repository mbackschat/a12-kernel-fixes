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

package com.mgmtp.a12.utils.conversion.internal;

import java.time.Instant;
import java.util.Objects;

import com.mgmtp.a12.utils.conversion.InstantRange;

public class InstantRangeImpl implements InstantRange {
	private final Instant start;
	private final Instant end;
	private InstantRangeImpl(Instant start, Instant end) {
		this.start = start;
		this.end = end;
	}
	public static InstantRangeImpl ofInternal(Instant start, Instant end) {
		return new InstantRangeImpl(start, end);
	}

	@Override public Instant start() {
		return start;
	}

	@Override public Instant end() {
		return end;
	}

	@Override public InstantRange withStart(Instant start) {
		return ofInternal(start, this.end);
	}

	@Override public InstantRange withEnd(Instant end) {
		return ofInternal(this.start, end);
	}

	@Override public boolean equals(Object o) {
		if(!(o instanceof InstantRange oDateRange)) {
			return false;
		}
		return Objects.equals(this.start(), oDateRange.start())
				&& Objects.equals(this.end(), oDateRange.end());
	}

	@Override public int hashCode() {
		return Objects.hash(this.start(), this.end());
	}
	@Override public String toString() {
		return "InstantRange(" + this.start() + ", " + this.end() + ")";
	}
}
