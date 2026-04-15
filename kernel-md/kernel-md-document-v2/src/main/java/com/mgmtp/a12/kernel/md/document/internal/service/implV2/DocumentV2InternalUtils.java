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

package com.mgmtp.a12.kernel.md.document.internal.service.implV2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;

import lombok.NonNull;

public class DocumentV2InternalUtils {
	private DocumentV2InternalUtils() {
		throw new UnsupportedOperationException("Static utility class");
	}

	// TODO cleanup those static util methods, where reasonable
	public static int hashField(FieldInstanceV2 field) {
		return fieldEqualityKey(field).hashCode();
	}

	public static boolean equalsField(@NonNull FieldInstanceV2 field, Object obj) {
		if (!(obj instanceof FieldInstanceV2 otherField)) {
			return false;
		}
		return fieldEqualityKey(field).equals(fieldEqualityKey(otherField));
	}

	private static List<Object> fieldEqualityKey(FieldInstanceV2 field) {
		return listOfAllowNulls(field.value());
	}

	public static int hashRepetitions(RepetitionsV2 repetitions) {
		return repetitionsEqualityKey(repetitions).hashCode();
	}

	public static boolean equalsRepetitions(@NonNull RepetitionsV2 repetitions, Object obj) {
		if (!(obj instanceof RepetitionsV2 otherRepetitions)) {
			return false;
		}
		return repetitionsEqualityKey(repetitions).equals(repetitionsEqualityKey(otherRepetitions));
	}

	private static List<?> repetitionsEqualityKey(RepetitionsV2 repetitions) {
		return repetitions.toList();
	}

	public static void checkOneBasedIndex(int index) {
		if (index <= 0) {
			throw new IndexOutOfBoundsException("index is 1-based and must therefore must be >= 1 but was " + index);
		}
	}

	public static String docToString(@NonNull DocumentV2 doc) {
		GroupInstanceV2 asGroup = doc.toRootRootGroup();
		return "DocumentV2: " + groupToStringInternal(asGroup.withDirectFieldValue("documentModelId", doc.getDocumentModelId()));
	}

	public static String groupToString(@NonNull GroupInstanceV2 group) {
		return "GroupInstanceV2: " + groupToStringInternal(group);
	}

	public static String groupToStringInternal(@NonNull GroupInstanceV2 group) {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n").append(indentWithoutAppendingTrailingNewline(groupContentsToString(group), 2)).append("\n}");
		return builder.toString();
	}

	private static String groupContentsToString(GroupInstanceV2 group) {
		return Stream.concat(
				group.directSubgroups().stream().sorted(Map.Entry.comparingByKey()).map(e ->
						"\"" + e.getKey() + "\": " + repetitionsToStringInternal(e.getValue())
				),
				group.directFields().stream().sorted(Map.Entry.comparingByKey()).map(e ->
						"\"" + e.getKey() + "\": " + fieldToString(e.getValue())
				)
		).collect(Collectors.joining(",\n"));
	}

	public static String repetitionsToString(@NonNull RepetitionsV2 repetitions) {
		return "RepetitionsV2: " + repetitionsToStringInternal(repetitions);
	}

	private static String repetitionsToStringInternal(RepetitionsV2 repetitions) {
		if (repetitions.size() == 0) {
			return "[]";
		}
		return repetitions.stream()
				.map(g -> indentWithoutAppendingTrailingNewline(groupContentsToString(g), 2))
				.collect(Collectors.joining("\n},{\n", "[{\n", "\n}]"));
	}

	/**
	 * {@link String#indent} indents the string, but also appends a newline at the end. This method indents and then removes
	 * the added newline again
	 */
	private static String indentWithoutAppendingTrailingNewline(String string, int n) {
		var indented = string.indent(n);
		if (indented.endsWith("\n")) {
			indented = indented.substring(0, indented.length() - 1);
		} else {
			assert string.isEmpty() : "indent is expected to always add a newline, except if the input string was empty";
		}
		return indented;
	}

	public static String fieldToString(@NonNull FieldInstanceV2 field) {
		Object value = field.value();
		return value instanceof String ? "\"" + value + "\"" : Objects.toString(value);
	}

	@SafeVarargs private static <T> List<T> listOfAllowNulls(T... elems) {
		// Note: this must work even if some elems are null
		return new ArrayList<>(Arrays.asList(elems));
	}

	// NOTE copy form Java8Utils...
	public static <A, B> B cascadeNulls(A a, @NonNull Function<A, B> fn) {
		return a != null ? fn.apply(a) : null;
	}

	// NOTE copy from Java8Utils
	public static <K, V> Map<K, V> filterMapValues(@NonNull Map<K, V> map, @NonNull Predicate<V> filter) {
		Map<K, V> newMap = new HashMap<>();
		for (var e : map.entrySet()) {
			if (filter.test(e.getValue())) {
				newMap.put(e.getKey(), e.getValue());
			}
		}
		return newMap;
	}

	public static void validateLastRepIsWildcard(@NonNull DocumentPointer pointer) {
		if (!lastRepIsWildcard(pointer)) {
			throw new IllegalArgumentException("expected the last repetition of the pointer to be 0 (wildcard), " +
					"and the pointer to have at least one path part, but got " + pointer);
		}
	}

	public static void assertLastRepIsWildcard(@NonNull DocumentPointer pointer) {
		assert lastRepIsWildcard(pointer) : "expected the last repetition of the pointer to be 0 (wildcard), " +
				"and the pointer to have at least one path part, but got " + pointer;
	}

	/**
	 * checks if the pointer has at least one element <em>and</em> the last element has repetitionIndex 0 (for wildcard). i.e.
	 * checks if the pointer can reference a RepetitionsV2.
	 */
	private static boolean lastRepIsWildcard(@NonNull DocumentPointer pointer) {
		return pointer.size() > 0 && pointer.getPartAtLevel(pointer.size() - 1).repetitionIndex() == 0;
	}

	public static void validateLastRepIs1(@NonNull DocumentPointer pointer) {
		if (!lastRepIs1(pointer)) {
			throw new IllegalArgumentException("expected the last repetition of the pointer to be 1, " +
					"and the pointer to have at least one path part, but got " + pointer);
		}
	}

	public static void assertLastRepIs1(@NonNull DocumentPointer pointer) {
		assert lastRepIs1(pointer) : "expected the last repetition of the pointer to be 1, " +
				"and the pointer to have at least one path part, but got " + pointer;
	}

	/**
	 * checks if the pointer has at least one element <em>and</em> the last element has repetitionIndex 1. i.e.
	 * checks if the pointer can reference a Field (or first repetition of a group).
	 */
	private static boolean lastRepIs1(@NonNull DocumentPointer pointer) {
		return pointer.size() > 0 && pointer.getPartAtLevel(pointer.size() - 1).repetitionIndex() == 1;
	}

	public static void assertHasNoTrailingWildcardRep(@NonNull DocumentPointer pointer) {
		// also fine for the root pointer
		assert pointer.size() == 0 || pointer.getPartAtLevel(pointer.size() - 1).repetitionIndex() > 0 :
				"the pointer was expected to be empty or have a last repetition >= 1 (concrete index) but got "
						+ pointer.getPartAtLevel(pointer.size() - 1).repetitionIndex();
	}
}
