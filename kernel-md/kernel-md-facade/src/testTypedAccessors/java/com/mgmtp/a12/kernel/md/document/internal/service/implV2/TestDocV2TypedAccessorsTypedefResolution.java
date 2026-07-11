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

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.typedefimported.typedefs.typedef_1a2b3c4d_5e6f_7890_abcd_ef1234567890.Color;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.typedefimported.typedefs.typedef_2b3c4d5e_6f70_8901_bcde_f23456789012.Salutation;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.typedefimported.views.ImportingDm;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.typedefimported.views.SecondImportingDm;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.typedefincluded.views.IncludingDm;

/**
 * Verifies that fields referencing a type definition that is <em>not</em> defined locally in the dm - but originates
 * from an imported typedef model or from a (lower) included model - resolve to their effective type in the generated
 * typed accessors (instead of falling back to {@code Object}).
 * <p>
 * The number typedefs in the test dms wrap a {@code NumberType}, which the generator maps to {@link BigDecimal}. The
 * accessor methods below are assigned to {@code BigDecimal}-typed variables, so if the typedef had <em>not</em> been
 * resolved (i.e. the accessor returned {@code Object}), the generated typings would not compile against this test -
 * which is the actual assertion. The runtime value checks are an additional sanity check.
 * <p>
 * Additionally verifies that a typedef whose generated value-class is shared (the <em>enum</em> class of an enum
 * typedef, or the hint-list <em>constants</em> class of a string typedef with a hint list) yields a single class per
 * typedef id, under {@code <prefix>/typedefs/<typedef-id>/}, reused by every field / model that references it rather
 * than duplicated. On this branch this sharing is always on (the former {@code --use-shared-typedef-classes} flag is
 * obsolete and ignored).
 * <p>
 * The generated typings are produced at build time (see {@code registerTaskForTypingsGen} in {@code build.gradle},
 * entries {@code TypedefImported} / {@code TypedefIncluded}), analogous to the other typed-accessor tests.
 */
public class TestDocV2TypedAccessorsTypedefResolution {

	@Test void importedTypedefResolvesToEffectiveType() {
		DocumentV2 untypedDoc = DocumentV2.empty("ImportingDm").withBatchUpdates(List.of(
				UpdateAction.putField("/rg/amount", FieldInstanceV2.ofValue(BigDecimal.valueOf(42)))));

		ImportingDm view = ImportingDm._viewOf(untypedDoc);

		// resolves to BigDecimal (the imported typedef wraps a NumberType) - would be Object without the fix, which would
		// make this assignment fail to compile
		BigDecimal amount = view.rg().amount();
		Assertions.assertEquals(BigDecimal.valueOf(42), amount);
	}

	@Test void includedTypedefResolvesAndIncludeAwarenessIsPreserved() {
		DocumentV2 untypedDoc = DocumentV2.empty("IncludingDm").withBatchUpdates(List.of(
				UpdateAction.putField("/rg/qty", FieldInstanceV2.ofValue(BigDecimal.valueOf(7))),
				UpdateAction.putField("/rg/included/includedValue", FieldInstanceV2.ofValue(BigDecimal.valueOf(9)))));

		IncludingDm view = IncludingDm._viewOf(untypedDoc);

		// the including dm's own field references a typedef that is only reachable via the lower included dm -> resolves
		BigDecimal qty = view.rg().qty();
		Assertions.assertEquals(BigDecimal.valueOf(7), qty);

		// include-awareness is preserved: the include's accessor returns the included dm's own generated view class.
		// The fully-qualified type below makes that explicit - it lives in the _includeddm package (the included dm's
		// own classes). If the include had been treated as flattened/expanded, the returned type would instead live
		// under the including dm's package and this assignment would not compile. Its field (referencing the locally
		// defined typedef) also resolves to BigDecimal.
		com.mgmtp.a12.kernel.md.facade.test.generated_typings.typedefincluded.views._includeddm.IncludedRg included =
				view.rg().included();
		BigDecimal includedValue = included.includedValue();
		Assertions.assertEquals(BigDecimal.valueOf(9), includedValue);
	}

	@Test void enumTypedefYieldsASingleSharedEnumClass() {
		// favColor and bgColor (same model) and themeColor (a second model generated together) all reference the same
		// enum typedef. They must all resolve to the *same* generated enum class - one class per typedef id, shared
		// rather than duplicated per field or per model. The shared class lives under <prefix>/typedefs/<typedef-id>/,
		// named after the typedef. The id uses the standard typedef_<uuid> format to exercise the escaping in
		// typedefIdAsPackagePart: ...typedefimported.typedefs.typedef__1a2b3c4d_2d5e6f_2d7890_2dabcd_2def1234567890.Color
		// (td_imported_amount is a non-standard-format id and demonstrates that non-standard ids are also handled).
		DocumentV2 doc1 = DocumentV2.empty("ImportingDm")
				.withFieldValue("/rg/favColor", "red")
				.withFieldValue("/rg/bgColor", "green");
		ImportingDm view1 = ImportingDm._viewOf(doc1);

		DocumentV2 doc2 = DocumentV2.empty("SecondImportingDm").withFieldValue("/rg/themeColor", "red");
		SecondImportingDm view2 = SecondImportingDm._viewOf(doc2);

		// compile-time proof of sharing: all three accessors are assigned to the same Color type. A per-field or
		// per-model duplicate would be a distinct class and would not compile here.
		Color favColor = view1.rg().favColor();
		Color bgColor = view1.rg().bgColor();
		Color themeColor = view2.rg().themeColor();

        // some extra sanity check:
		Assertions.assertEquals("red", favColor._stringValue());
		Assertions.assertEquals("green", bgColor._stringValue());
		Assertions.assertEquals("red", themeColor._stringValue());

		// runtime proof that it is literally one shared enum class (across fields and across models)
		Assertions.assertSame(favColor.getClass(), bgColor.getClass());
		Assertions.assertSame(favColor.getClass(), themeColor.getClass());
		Assertions.assertSame(favColor, themeColor); // "red" is the same singleton enum constant
	}

	@Test void hintListTypedefYieldsASingleSharedConstantsClass() {
		// compile-time check (+runtime minimal sanity check) that the Salutation hintlist constants class lives in the
		// typedef-specific package (rather than in a fqn specific to a particular field). (this does not prove that no additional
		// copies live in field-specific fqn's though)
		Assertions.assertEquals("hello", com.mgmtp.a12.kernel.md.facade.test.generated_typings.typedefimported.typedefs.typedef_2b3c4d5e_6f70_8901_bcde_f23456789012.Salutation.En.hello);

		// apart from the existence of this string constants class, we'd ideally check that the javadoc of the field methods like ImportingDm._pointer().rg().favGreeting() references it,
		// but for that, we'd need to examine the sources, which is inconvenient for this test. so for now, we don't check this.

		// a small additional sanity check (but with little signal): type of the field and of the constant are compatible
		// (both are expected to be string) compile time compatibilit check:
		var someDoc = ImportingDm._empty()._with(ImportingDm._pointer().rg().favGreeting(), Salutation.En.hi);
		// extra runtime double-check:
		Assertions.assertEquals("hi", someDoc._at(ImportingDm._pointer().rg().favGreeting()));
	}

	@Test void unreferencedTypedefsStillYieldTheirGeneratedClasses() {
		// UnusedEnum and UnusedHintList are typedefs defined in ImportedTypedefsDm that NO field references. The generator
		// emits the value-class of every typedef in the provided models up front (by its globally unique id), not only of
		// the ones a field happens to use - so their classes exist under <prefix>/typedefs/<typedef-id>/ all the same. The
		// fully-qualified references below are the compile-time proof: without those classes this test would not compile.
		com.mgmtp.a12.kernel.md.facade.test.generated_typings.typedefimported.typedefs
				.typedef_3c4d5e6f_7081_9012_cdef_345678901234.UnusedEnum unusedEnumValue =
				com.mgmtp.a12.kernel.md.facade.test.generated_typings.typedefimported.typedefs
						.typedef_3c4d5e6f_7081_9012_cdef_345678901234.UnusedEnum._of("on");
		Assertions.assertEquals("on", unusedEnumValue._stringValue());

		Assertions.assertEquals("foo", com.mgmtp.a12.kernel.md.facade.test.generated_typings.typedefimported.typedefs
				.typedef_4d5e6f70_8192_0123_defa_456789012345.UnusedHintList.En.foo);
	}
}
