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

package com.mgmtp.a12.kernel.md.document.apiV2;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.Strings;

import lombok.NonNull;

// (keep the impl package private)
record PathPartImpl(String name, int repetitionIndex) implements PathPart {
	PathPartImpl {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("PathPart name must not be null or empty");
		}
		if (repetitionIndex < 0) {
			throw new IllegalArgumentException("repetition index must be > 0, or 0 for wildcard, but not negative");
		}
	}

	@Override public int compareTo(PathPart o) {
		var a = Strings.CS.compare(name, o.name());
		if (a != 0) {
			return a;
		}
		if ((repetitionIndex == 0) != (o.repetitionIndex() == 0)) {
			throw new IllegalArgumentException("cannot compare PathPart with wildcard repetition index 0 unless the other"
					+ " PathPart has 0 too (got " + this + " and " + o);
		}
		return Integer.compare(repetitionIndex, o.repetitionIndex());
	}

	static List<PathPart> toPathPartList(@NonNull List<String> path, @NonNull List<Integer> repetitions) {
		if (path.size() != repetitions.size()) {
			throw new IllegalArgumentException("path and repetitions have different length");
		}

		return IntStream.range(0, path.size())
				.mapToObj(i -> (PathPart) new PathPartImpl(path.get(i), repetitions.get(i))).toList();
	}

	static String pathPartListToString(@NonNull List<PathPart> pathParts) {
		return pathParts.stream().map(Object::toString).collect(Collectors.joining("/"));
	}

	@Override public String toString() {
		return this.name() + (this.repetitionIndex() != 1 ? ("[" + this.repetitionIndex() + "]") : "");
	}
}
