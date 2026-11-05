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

package javadocsnippets;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.PathPart;

// @formatter:off

class TestJavaDocDocumentMultiPointer {
	@Test
	void containsAncestorOf() {
		// @start region=containsAncestorOf
		// the root pointer is an ancestor of every pointer
		Assertions.assertTrue(DocumentMultiPointer.of("").containsAncestorOf(DocumentPointer.of("a[1]/b[1]/c[1]"))); // @highlight regex='".*?"'
		// a concrete index must match, a wildcard index allows for any index
		Assertions.assertTrue(DocumentMultiPointer.of("a[2]/b[0]").containsAncestorOf(DocumentPointer.of("a[2]/b[1]/c[1]"))); // @highlight regex='".*?"'
		// it is also an ancestor, if the paths have the same length
		Assertions.assertTrue(DocumentMultiPointer.of("a[2]/b[0]").containsAncestorOf(DocumentPointer.of("a[2]/b[3]"))); // @highlight regex='".*?"'
		// it does not contain an ancestor if it contains only strict descendents of the document pointer
		Assertions.assertFalse(DocumentMultiPointer.of("a[2]/b[0]").containsAncestorOf(DocumentPointer.of("a[2]"))); // @highlight regex='".*?"'
		// it does not contain an ancestor if name or repetition index mismatch:
		Assertions.assertFalse(DocumentMultiPointer.of("a[2]").containsAncestorOf(DocumentPointer.of("b[3]"))); // @highlight regex='".*?"'
		// @end region=containsAncestorOf
	}

	@Test
	void containsDescendentOf() {
		// @start region=containsDescendentOf
		// every pointer is a descendent of the root pointer
		Assertions.assertTrue(DocumentMultiPointer.of("a/b/c").containsDescendentOf(DocumentPointer.of(""))); // @highlight regex='".*?"'
		// a concrete index must match
		Assertions.assertTrue(DocumentMultiPointer.of("a[2]/b/c").containsDescendentOf(DocumentPointer.of("a[2]/b"))); // @highlight regex='".*?"'
		// a wildcard index contains a descendant
		Assertions.assertTrue(DocumentMultiPointer.of("a[2]/b[0]/c").containsDescendentOf(DocumentPointer.of("a[2]/b[3]"))); // @highlight regex='".*?"'
		// it is also an ancestor, if the paths have the same length
		Assertions.assertTrue(DocumentMultiPointer.of("a[2]/b[0]").containsDescendentOf(DocumentPointer.of("a[2]/b[3]"))); // @highlight regex='".*?"'

		// it does not contain a descendent if it contains only strict ancestors of the document pointer
		Assertions.assertFalse(DocumentMultiPointer.of("a[2]").containsDescendentOf(DocumentPointer.of("a[2]/b[0]"))); // @highlight regex='".*?"'
		// it does not contain an ancestor if name or repetition index mismatch:
		Assertions.assertFalse(DocumentMultiPointer.of("a[2]").containsAncestorOf(DocumentPointer.of("b[3]"))); // @highlight regex='".*?"'
		// @end region=containsDescendentOf
	}

	@Test
	void createOfPathParts() {
		// @start region=createOfPathParts
		// the list of PathParts defines each level of the path
		Assertions.assertEquals(DocumentMultiPointer.of("/a/b"), DocumentMultiPointer.of(List.of(PathPart.of("a", 1), PathPart.of("b", 1)))); // @highlight regex='".*?"'
		// an empty list is also valid
		Assertions.assertEquals(DocumentMultiPointer.of(""), DocumentMultiPointer.of(List.of())); // @highlight regex='".*?"'
		// @end region=createOfPathParts
	}

	@Test
	void createOfPathWithRepetitions() {
		// @start region=createOfPathWithRepetitions
		// the path can contain a leading '/'
		Assertions.assertEquals(DocumentMultiPointer.of("/a/b"), DocumentMultiPointer.of("a/b")); // @highlight regex='".*?"'
		// a repetition is implicit set to '1'
		Assertions.assertEquals(DocumentMultiPointer.of("a/b[2]/c[1]"), DocumentMultiPointer.of("a[1]/b[2]/c[1]")); // @highlight regex='".*?"'
		// @end region=createOfPathWithRepetitions
	}

	@Test
	void createOfPathListAndRepetitions() {
		// @start region=createOfPathListAndRepetitions
		// the path can contain a leading '/'
		Assertions.assertEquals(DocumentMultiPointer.of("/a/b"), DocumentMultiPointer.of(List.of("a", "b"), List.of(1, 1))); // @highlight regex='".*?"'
		// exception when the list are not the same length
		Assertions.assertThrows(IllegalArgumentException.class, () -> DocumentMultiPointer.of(List.of("a", "b"), List.of(1))); // @highlight regex='".*?"'
		// @end region=createOfPathListAndRepetitions
	}

	@Test
	void createOfPathAndRepetitions() {
		// @start region=createOfPathAndRepetitions
		// the path can contain a leading '/'
		Assertions.assertEquals(DocumentMultiPointer.of("/a/b"), DocumentMultiPointer.of("a/b", List.of(1, 1))); // @highlight regex='".*?"'
		// a repetition is implicit set to '1'
		Assertions.assertThrows(IllegalArgumentException.class, () -> DocumentMultiPointer.of(List.of("a", "b"), List.of(1))); // @highlight regex='".*?"'
		// @end region=createOfPathAndRepetitions
	}

	@Test
	void createOfPathWithAllWildcards() {
		// @start region=createOfPathWithAllWildcards
		// all repetition indexes will be set to WILDCARD
		Assertions.assertEquals(DocumentMultiPointer.of("a[0]/b[0]"), DocumentMultiPointer.ofPathWithAllWildcards("a/b")); // @highlight regex='".*?"'
		// @end region=createOfPathWithAllWildcards
	}
}
