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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import lombok.NonNull;

class DocumentPointerImpl implements DocumentPointer {
	private final List<PathPart> pathParts;
	private static final Pattern pathPartPattern = Pattern.compile("([\\w\\-]+)(\\[(\\d+)?])?");

	private DocumentPointerImpl(List<PathPart> pathParts) {
		pathParts = Collections.unmodifiableList(new ArrayList<>(pathParts));
		assert pathParts.stream().noneMatch(
				pp -> pp.repetitionIndex() < 0) : "PathPart is responsible for repetition indexes >= 0, but contains index < 0";

		// PathParts already guarantees index >= 0
		for (int i = 0; i < pathParts.size() - 1; i++) {
			if (pathParts.get(i).repetitionIndex() <= DocumentMultiPointer.WILDCARD) {
				throw new IllegalArgumentException("All repetition indexes must be >= 1, the last repetition can also be 0");
			}
		}
		this.pathParts = pathParts;
	}

	static DocumentPointerImpl of(@NonNull List<PathPart> pathParts) {
		return new DocumentPointerImpl(pathParts);
	}

	static DocumentPointerImpl of(@NonNull String pathWithRepetitions) {
		return new DocumentPointerImpl(parsePathParts(pathWithRepetitions));
	}

	static List<PathPart> parsePathParts(@NonNull String pathWithRepetitions) {
		List<String> pathParts = parseFullName(pathWithRepetitions);
		List<PathPart> pathPartsPairs = new ArrayList<>(pathParts.size());
		for (String pathPart : pathParts) {
			var m = pathPartPattern.matcher(pathPart);
			var _matches = m.matches();
			int repetition = m.group(3) != null ? Integer.parseInt(m.group(3)) : 1;
			if (repetition < 0) {
				throw new IllegalArgumentException("repetition indices must be integers >= 0 (1-based, 0 for wildcards) but found "
						+ repetition + " in path string " + pathWithRepetitions);
			}
			pathPartsPairs.add(new PathPartImpl(m.group(1), repetition));
		}
		return pathPartsPairs;
	}

	static List<String> parseFullName(@NonNull String fullName) {
		if (!"/".equals(fullName) && fullName.endsWith("/")) {
			throw new IllegalArgumentException("path must not end with a slash but was " + fullName);
		}
		if (fullName.startsWith("/")) { // the leading "/" is redundant - documentPointers are always relative to the
			// document or group to which they are applied - it can be a root document or subgroup, but the pointer is the same
			fullName = fullName.substring(1);
		}
		if (fullName.isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.asList(fullName.split("/"));
	}

	static List<PathPart> parsePathParts(@NonNull String fullName, List<Integer> repetitions) {
		List<String> fullNameParts = parseFullName(fullName);

		if (fullNameParts.size() != repetitions.size()) {
			throw new IllegalArgumentException("fullName and repetition have different length");
		}

		return IntStream.range(0, fullNameParts.size())
				.mapToObj(i -> (PathPart) new PathPartImpl(fullNameParts.get(i), repetitions.get(i)))
				.toList();
	}

	static DocumentPointerImpl of(@NonNull String fullName, List<Integer> repetitions) {
		return of(parsePathParts(fullName, repetitions));
	}

	@Override public PathPart getPartAtLevel(int level) {
		return pathParts.get(level);
	}

	@Override public List<PathPart> getPathParts() {
		return pathParts;
	}

	@Override public int size() {
		return pathParts.size();
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DocumentPointer otherPointer))
			return false;
		return Objects.equals(getPathParts(), otherPointer.getPathParts());
	}

	@Override public int hashCode() {
		return Objects.hash(getPathParts());
	}

	@Override public int compareTo(@NonNull DocumentPointer o) {
		int sharedSize = Math.min(size(), o.size());
		for (int i = 0; i < sharedSize; i++) {
			int levelCompare = getPartAtLevel(i).compareTo(o.getPartAtLevel(i));
			if (levelCompare != 0) {
				return levelCompare;
			}
		}
		return Integer.compare(size(), o.size());
	}

	@Override public String toString() {
		var str = PathPartImpl.pathPartListToString(getPathParts());
		return "".equals(str) ? "(empty DocumentPointer)" : str;
	}
}