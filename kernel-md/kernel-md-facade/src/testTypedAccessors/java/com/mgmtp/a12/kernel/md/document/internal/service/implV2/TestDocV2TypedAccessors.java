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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedPointer;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.collisionswithdefaultimports.views.CollisionsWithDefaultImports;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.collisionswithreservednamesandpackages.views.CollisionsWithReservedNamesAndPackages;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.collisionswithreservednamesandpackages.views._collisionswithreservednamesandpackages._escaped_int._escaped_long.Escaped_return;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontact.pointers._domaincontact.PContact;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontact.pointers._domaincontact._contact.PAddress;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontact.views.DomainContact;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontact.views._domaincontact.Contact;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontact.views._domaincontact._contact.Address;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontact.views._domaincontact._contact.Phones;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontact.views._domaincontact._contact._phones.Type;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.othercollisions.pointers._othercollisions.PRG;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.othercollisions.views.OtherCollisions;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.othercollisions.views._othercollisions._rg._maycollidewithpointerclassname.MayCollideWithPointerClassName;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.testspecialcasesforaccessorgen.pointers._basedm.PRepeatableRg;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.testspecialcasesforaccessorgen.pointers._basedm._rg_1._groupandsubgroupwithsamename.PGroupAndSubgroupWithSameName;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.testspecialcasesforaccessorgen.typedefs.typedef_0c3bc7ac_42b1_452d_bc4a_1d7414b82551.PredefinedEnum;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.testspecialcasesforaccessorgen.views.BaseDm;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.testspecialcasesforaccessorgen.views._basedm._repeatablerg.EnumField;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.testspecialcasesforaccessorgen.views._basedm._repeatablerg.LowerCaseEnumField;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.testspecialcasesforaccessorgen.views._basedm._repeatablerg.StringFieldWithHintList;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.testspecialcasesforaccessorgen.views._basedm._rg_1._groupandsubgroupwithsamename._groupandsubgroupwithsamename.EnumFieldWithNumberAndSpecialCharValue;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.testspecialcasesforaccessorgen.views._basedm._rg_1._groupandsubgroupwithsamename._groupandsubgroupwithsamename.EnumWithUmlautAndSpecialCharInValues;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.withexternalincludes.views.WithExternalIncludes;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.withincludes.views.WithIncludes;
import com.mgmtp.a12.kernel.md.facade.test.generated_typings.withincludes.views._include1.InculdeRG;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.utils.conversion.InstantRange;
import com.mgmtp.a12.kernel.internal.test.utils.annotations.SampleAnnotationWithRuntimeRetention;
import com.mgmtp.a12.kernel.internal.test.utils.annotations.SecondSampleAnnotationWithRuntimeRetention;

class TestDocV2TypedAccessors {

	@Test
	void testTypingsForDomainContactMisc() { // TODO is this test redundant?
		String dmString = dmStringDomainContact();
		String docString = resourceToStringUtf8("documents/json/A12K-1387/tutorial_application-example/ContactWithAddress.json");
		DocumentV2 documentV2 = deserializeDocumentV2(docString, dmString).document();

		String expectedEmail = "johny.smith@mgm-tp.com";
		List<String> expectedAddresses = List.of("""
						Astreet 2
						01234 ACity""",
				"""
						Bstreet 3
						43210 Btown""");
		DomainContact domainContactDoc = DomainContact._viewOf(documentV2);
		Assertions.assertEquals(expectedEmail, domainContactDoc.contact().personalData().email());
		Assertions.assertEquals(
				expectedAddresses,
				domainContactDoc.contact().address().stream()
						.map(a -> a.street() + " " + a.housenumber() + "\n" + a.zip() + " " + a.city()).toList());

		PAddress<DomainContact> addressPointer = DomainContact._pointer().contact().address(2);
		TypedPointer<DomainContact, String> cityPointer = addressPointer.city();
		var zip1Pointer = DomainContact._pointer().contact().address(1).zip();
		String zip1 = domainContactDoc._at(zip1Pointer);
		Assertions.assertEquals("01234", zip1);
		Assertions.assertEquals("Btown", domainContactDoc._at(addressPointer).city());
		Assertions.assertEquals("Btown", domainContactDoc._at(addressPointer.city()));
		Assertions.assertEquals("Btown", domainContactDoc._at(cityPointer));
		PAddress<DomainContact> outOfBoundsPointer = DomainContact._pointer().contact().address(200);
		TypedPointer<DomainContact, String> outOfBoundsCityPointer = DomainContact._pointer().contact().address(200).city();
		Assertions.assertNull(domainContactDoc._at(outOfBoundsPointer)); // should not throw
		Assertions.assertNull(domainContactDoc._at(outOfBoundsCityPointer)); // should not throw if null-chaining works correctly
		DomainContact dc = domainContactDoc;

		DomainContact updatedDoc = domainContactDoc._with(cityPointer, "NewCity");
		String fetched = updatedDoc._at(cityPointer);
		Assertions.assertEquals("NewCity", fetched);
		Assertions.assertEquals("Btown", domainContactDoc._at(cityPointer));

		DocumentV2 unwrappedUpdated = updatedDoc._unwrap();
		Assertions.assertEquals("NewCity",
				unwrappedUpdated.directSubgroup("Contact").get(1).directSubgroup("Address").get(2).directField("City").value());
		Assertions.assertEquals(documentV2.getDocumentModelId(), ((DocumentV2) unwrappedUpdated).getDocumentModelId());

		Assertions.assertEquals(
				List.of("Astreet 2 01234 ACity", "Bstreet 3 43210 Btown"),
				domainContactDoc._at(DomainContact._pointer().contact()).address()
						.stream().map(
								a -> String.join(" ", a.street(), a.housenumber(), a.zip(), a.city()
								)).toList());

		var birthDayPointer = DomainContact._pointer().contact().personalData().dateOfBirth();
		Instant birthDay = domainContactDoc._at(birthDayPointer);
		Assertions.assertEquals(Instant.parse("1987-10-11T00:00:00Z"), birthDay);
		DomainContact docWithNextGenBirthDay = domainContactDoc._with(birthDayPointer, Instant.parse("2000-01-01T00:00:00Z"));
		var nextGenBirthDay = docWithNextGenBirthDay._at(birthDayPointer);
		Assertions.assertEquals(Instant.parse("2000-01-01T00:00:00Z"), nextGenBirthDay);

		var phoneType = domainContactDoc._at(DomainContact._pointer().contact().phones(1).type());
		Assertions.assertSame(Type.WORK, phoneType);
		Assertions.assertEquals(Type.WORK, phoneType);
	}

	@Test void testBasicMethodsAndCompileChecksOfGeneratedTypings() {
		// NOTE this test is based on the DomainContact DM, but it might be
		// simpler if it was based on a purpose-made minimal test dm -> maybe refactor at some point

		// check that  pointer classes and methods exist for some subgroups and fields
		// NOTE: we can't really negative-test the combinations of pointers & values that the compiler _doesn't_ accept, I guess?
		PContact<DomainContact> pContact = DomainContact._pointer().contact();
		var pPersonalData = pContact.personalData();
		var pFirstName = pPersonalData.firstName();
		var pGender = pPersonalData.gender();
		var pDateOfBirth = pPersonalData.dateOfBirth();
		var pPhoto = pPersonalData.photo();
		// skipping other string fields in personalData
		// let's declare the type for pPhotoSize explicitly, as an example (type inference would do the same on hover)
		TypedPointer<DomainContact, BigDecimal> pPhotoSize = pPersonalData.photo().size();
		// (explicit type example continued) for groups, the target type is not a field value type but the typed view of the group:
		com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontact.pointers.
				_domaincontact._contact.PMisc<DomainContact> pMisc = pContact.misc();
		var pMiscType = pMisc.type();
		var pPhones1 = pContact.phones(1);
		var pPhones2 = pContact.phones(2);
		var pPhones3 = pContact.phones(3);
		var pPhone1Number = pPhones1.phoneNumber();
		var pPhone2Type = pContact.phones(2).type();
		var pAddress2 = pContact.address(2);

		// some checks of actual runtime behavior
		DomainContact emptyDcTypedView = DomainContact._viewOfCheckDmId(DocumentV2.empty("DomainContact"));

		// test the _with() method (here, with an enum field):
		DomainContact domainContactWithPhone = emptyDcTypedView._with(pPhone2Type, Type.MOBILE);
		Assertions.assertEquals(deserializeDocumentV2("""
						{ "Contact" : { "Phones" : [{}, { "Type": "MOBILE" }]  }}""",
				dmStringDomainContact()).document(), domainContactWithPhone._unwrap());
		// test the _with() method with a subgroup
		Phones someSubGroup = Phones._empty()._with(Phones._pointer().phoneNumber(), "0123456789");
		DomainContact dcWithSubgroup = emptyDcTypedView._with(pPhones2, someSubGroup);
		Assertions.assertEquals(deserializeDocumentV2("""
						{ "Contact" : { "Phones" : [{}, { "PhoneNumber": "0123456789" }]  }}""",
				dmStringDomainContact()).document(), dcWithSubgroup._unwrap());

		// test the _with() method with null value for subgroup (removal)
		Assertions.assertEquals(deserializeDocumentV2("""
						{ "Contact" : { "Phones" : [{ "PhoneNumber": "0123456789" }]  }}""",
				dmStringDomainContact()).document(), dcWithSubgroup._with(pPhones1, null)._unwrap());
		// test the _with() method with null value for a field (removal of the whole field, not just the field value)
		Assertions.assertEquals(deserializeDocumentV2("""
						{ "Contact" : { "Phones" : [{}, {}]  }}""",
				dmStringDomainContact()).document(), dcWithSubgroup._with(pPhones2.phoneNumber(), null)._unwrap());
		// test the _with() method with null value for not-present subgroup (absent parents must not be added)
		Assertions.assertEquals(emptyDcTypedView._unwrap(), emptyDcTypedView._with(pPhones2, null)._unwrap());
		Assertions.assertEquals(emptyDcTypedView._unwrap(), emptyDcTypedView._with(pPhones2.phoneNumber(), null)._unwrap());

		// test _withInsertedRepetition()
		Assertions.assertEquals(deserializeDocumentV2("""
								{ "Contact" : { "Phones" : [{}, { "PhoneNumber": "0123456789" }]  }}""",
						dmStringDomainContact()).document(),
				emptyDcTypedView._withInsertedRepetition(pPhones1, someSubGroup)._withInsertedRepetition(pPhones1, Phones._empty())
						._unwrap());
		// test _withInsertedRepetition() negative test with null value:
		var npe = Assertions.assertThrows(NullPointerException.class,
				() -> emptyDcTypedView._withInsertedRepetition(pPhones1, someSubGroup)._withInsertedRepetition(pPhones1, null));
		Assertions.assertTrue(npe.getMessage().contains("typedNewSubgroup must be non-null"), npe.getMessage());

		// test _at (for subgroup)
		Phones phone2 = dcWithSubgroup._at(pPhones2);
		Assertions.assertEquals(deserializeDocumentV2("""
						{ "Contact" : { "Phones" : [{}, { "PhoneNumber": "0123456789" }]  }}""",
				dmStringDomainContact()).document().group("Contact/Phones[2]"), phone2._unwrap());
		// test _at (for field value)
		String phone2Number = dcWithSubgroup._at(pPhones2.phoneNumber());
		Assertions.assertEquals(deserializeDocumentV2("""
						{ "Contact" : { "Phones" : [{}, { "PhoneNumber": "0123456789" }]  }}""",
				dmStringDomainContact()).document().fieldValue("Contact/Phones[2]/PhoneNumber"), phone2Number);
		Assertions.assertEquals("0123456789", phone2Number); // sanity check
		// test _at (not-present subgroup)
		Assertions.assertNull(emptyDcTypedView._at(pPhones2));
		Assertions.assertNull(dcWithSubgroup._at(pPhones3.phoneNumber()));

		// note: _withInserted() is not available with pointers to non-repeatable groups, i.e. the following line must not compile:
		// domainContactWithPhotoDateAndPhones._withInserted(pPersonalData, domainContactWithPhone._at(pPersonalData));

		// test that the compiler agrees that _getWrappedUntyped() returns a document (not just a group instance) when a document
		// view is unwrapped
		DocumentV2 unwrappedDoc = domainContactWithPhone._unwrap();

		DomainContact._viewOf(DocumentV2.empty("myDoc")); // no id check -> works
		DomainContact._viewOfCheckDmId(DocumentV2.empty("DomainContact")); // dynamic dm id check successful
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> DomainContact._viewOfCheckDmId(DocumentV2.empty("MismatchingDmId")));
		// DomainContact._viewOf(ImmutableGroupV2Impl.empty()); // must not compile: group cannot be wrapped where document is needed
	}

	@Test void testWithAppendedRepetition() {
		var doc = DomainContact._viewOf(deserializeDocumentV2("""
						{ "Contact" : { "Phones" : [{ "PhoneNumber": "0123456789" }, {}]  }}""",
				dmStringDomainContact()).document());
		Phones phone3 = Phones._empty()._with(Phones._pointer().phoneNumber(), "12341234");
		// a single-repetition poniter must not compile:
		// DomainContact updadedDocInvalid = DomainContact._viewOf(doc)._withAppendedRepetition(DomainContact._pointer().contact().phones(2), phone3);
		var phonesPointer = DomainContact._pointer().contact().phones();
		DomainContact updadedDoc = doc._withAppendedRepetition(phonesPointer, phone3);
		Assertions.assertEquals(
				deserializeDocumentV2("""
								{ "Contact" : { "Phones" : [{ "PhoneNumber": "0123456789" }, {}, { "PhoneNumber": "12341234" }]  }}""",
						dmStringDomainContact()).document(),
				updadedDoc._unwrap()
		);

		var e = Assertions.assertThrows(NullPointerException.class, () -> doc._withAppendedRepetition(phonesPointer, null));
		Assertions.assertTrue(e.getMessage().contains("typedNewSubgroup must be non-null"), e.getMessage());
	}

	@Test void testPointerUnwrap() {
		var emptyTp = DomainContact._pointer();
		var repTp = DomainContact._pointer().contact().address();
		var singleRepOfRepeatableTp = repTp.get(2);
		var fieldTp = DomainContact._pointer().contact().address(2).city();

		Assertions.assertEquals(DocumentPointer.of(""), emptyTp._unwrap());
		Assertions.assertEquals(
				DocumentPointer.of("Contact/Address[0]"),
				repTp._unwrap());
		Assertions.assertEquals(
				DocumentPointer.of("Contact/Address[2]"),
				singleRepOfRepeatableTp._unwrap());
		Assertions.assertEquals(
				DocumentPointer.of("Contact/Address[2]/City"),
				fieldTp._unwrap());
	}

	@Test void testPointerEqualsAndHashCode() {
		PAddress<DomainContact> p1 = DomainContact._pointer().contact().address(1);
		PAddress<DomainContact> p1b = DomainContact._pointer().contact().address(1);
		PAddress<Contact> p2 = Contact._pointer().address(1);
		// it would be nice to also test the case where the untyped pointer is equal but the class is not - but we'd need a
		// suitable test DM for that.
		assertEqualsInclHashcode(p1, p1b,
				"typed pointers must be 'equal' if their untyped pointers and their typed classes are equal");
		Assertions.assertNotSame(p1, p1b, "it would in principle be ok if this was the same object, but then the previous" +
				"assertion would not check what it is supposed to check in this test");
		assertNotEqualsInclHashcode(p1, p2, "same runtime class, as the target is the same, " +
				"but different untyped pointer because the start is different");
	}

	@Test void testViewEqualsAndHashCode() {
		Address typed1 = Address._empty()._with(Address._pointer().city(), "myCity");
		Address typed1b = Address._empty()._with(Address._pointer().city(), "myCity");
		Address typed2 = Address._empty()._with(Address._pointer().city(), "otherCity");
		Phones sameContentOtherWrapper = Phones._viewOf(typed1._unwrap()); // NOTE: this doesn't make much sense, because the
		// wrapped data has no entries that match the shape of Phones - but it is not illegal either: the views are designed to
		// allow additional entries that are not known to the view class (the "city" entry here in the case of Phones),
		// and the wrapped data is obviously allowed to have no given entry for any of the fields that _are_ known to the view)
		assertEqualsInclHashcode(typed1, typed1b,
				"must be equal, because both the view class and the wrapped group are equal");
		Assertions.assertNotSame(typed1, typed1b, "it would in principle be ok if this was the same object, but then the previous" +
				"assertion would not check what it is supposed to check in this test");
		assertNotEqualsInclHashcode(typed1, typed2,
				"must not be equal: view class is the same, but the wrapped group is unequal");
		assertNotEqualsInclHashcode(typed1, sameContentOtherWrapper,
				"must not be equal: the wrapped group is equal, but the view class is different");
		assertEqualsInclHashcode(typed1._unwrap(), sameContentOtherWrapper._unwrap(),
				"sanity check only: if this does not hold, then the previous assertion does not meaningfully check that " +
						"the different view class is indeed taken into account");
	}

	private static <T> void assertEqualsInclHashcode(T a, T b, String message) {
		Assertions.assertEquals(a, b, message);
		Assertions.assertEquals(a.hashCode(), b.hashCode(), message);
	}

	private static <T> void assertNotEqualsInclHashcode(T a, T b, String message) {
		Assertions.assertNotEquals(a, b, message);
		Assertions.assertNotEquals(a.hashCode(), b.hashCode(), message);
	}

	@Test void testSpecialCasesForAccessorGenMinimal() {
		// - access all paths in the dm TestSpecialCasesForAccessorGenMinimal once,
		//   to make sure that the untyped paths in the typed accessors are correct.
		// - implicitly check that the typings were generated and compiled successfully, by referencing the types here
		Instant i1 = Instant.parse("2023-01-01T00:00:00.00Z");
		Instant i2 = Instant.parse("2024-01-01T00:00:00.00Z");
		DocumentV2 untypedDoc = DocumentV2.empty("someId").withBatchUpdates(List.of(
				UpdateAction.putField(
						"/rg_1/GroupAndSubgroupWithSameName/GroupAndSubgroupWithSameName/EnumWithUmlautAndSpecialCharInValues",
						FieldInstanceV2.ofValue("Value_with_Umlauts__and_special_chars_äÖßé' ^ { \"")),
				UpdateAction.putField("/rg_1/GroupAndSubgroupWithSameName/GroupAndSubgroupWithSameName/FieldWithPredefinedType",
						FieldInstanceV2.ofValue("Enum Value A")),
				UpdateAction.putField(
						"/rg_1/GroupAndSubgroupWithSameName/GroupAndSubgroupWithSameName/lowercase_Field_with_underscores",
						FieldInstanceV2.ofValue("someString")),
				UpdateAction.putField(
						"/rg_1/GroupAndSubgroupWithSameName/GroupAndSubgroupWithSameName/EnumFieldWithNumberAndSpecialCharValue",
						FieldInstanceV2.ofValue("{ } [ < \\   \\\\ \\n")),
				UpdateAction.putFieldValue("/RepeatableRg[13]/StringField", "someString"),
				UpdateAction.putField("/RepeatableRg[11]/NumberField", FieldInstanceV2.ofValue(BigDecimal.valueOf(42))),
				UpdateAction.putField("/RepeatableRg[13]/NumberWithLeadingZerosField",
						FieldInstanceV2.ofValue(BigDecimal.valueOf(42))),
				UpdateAction.putField("/RepeatableRg[11]/NumberWithLeadingZerosField", FieldInstanceV2.ofValue("0042")),
				UpdateAction.putField("/RepeatableRg[11]/DateField", FieldInstanceV2.ofValue(i1)),
				UpdateAction.putField("/RepeatableRg[11]/PartiallyKnownDateField", FieldInstanceV2.ofValue(i1)),
				UpdateAction.putField("/RepeatableRg[13]/PartiallyKnownDateField", FieldInstanceV2.ofValue("2023-11")),
				UpdateAction.putField("/RepeatableRg[11]/DateTimeField", FieldInstanceV2.ofValue(i1)),
				UpdateAction.putField("/RepeatableRg[11]/TimeField", FieldInstanceV2.ofValue(i1)),
				UpdateAction.putField("/RepeatableRg[11]/DateFragmentField", FieldInstanceV2.ofValue(i1)),
				// date fragments are dates internally
				UpdateAction.putField("/RepeatableRg[11]/DateRangeField", FieldInstanceV2.ofValue(InstantRange.of(i1, i2))),
				UpdateAction.putField("/RepeatableRg[11]/DateRangeFieldMMDD", FieldInstanceV2.ofValue(InstantRange.of(i1, i2))),
				UpdateAction.putField("/RepeatableRg[11]/ConfirmField", FieldInstanceV2.ofValue(null)),
				UpdateAction.putField("/RepeatableRg[13]/ConfirmField", FieldInstanceV2.ofValue(Boolean.TRUE)),
				UpdateAction.putField("/RepeatableRg[11]/BooleanField", FieldInstanceV2.ofValue(Boolean.FALSE)),
				UpdateAction.putField("/RepeatableRg[13]/BooleanField", FieldInstanceV2.ofValue(Boolean.TRUE)),
				UpdateAction.putField("/RepeatableRg[14]/BooleanField", FieldInstanceV2.ofValue(null)),
				UpdateAction.putField("/RepeatableRg[11]/CustomField", FieldInstanceV2.ofValue("custom fields are strings")),
				UpdateAction.putField("/RepeatableRg[11]/EnumField", FieldInstanceV2.ofValue("val2")),
				UpdateAction.putField("/RepeatableRg[11]/lowerCaseEnumField", FieldInstanceV2.ofValue("optA"))
		));
		BaseDm baseDm = BaseDm._viewOf(untypedDoc);
		PGroupAndSubgroupWithSameName<BaseDm> pG = BaseDm._pointer().rg_1().groupAndSubgroupWithSameName()
				.groupAndSubgroupWithSameName();
		PRepeatableRg<BaseDm> pRep14 = BaseDm._pointer().repeatableRg(14);
		PRepeatableRg<BaseDm> pRep13 = BaseDm._pointer().repeatableRg(13);
		PRepeatableRg<BaseDm> pRep11 = BaseDm._pointer().repeatableRg(11);

		EnumWithUmlautAndSpecialCharInValues enum1Value = baseDm._at(pG.enumWithUmlautAndSpecialCharInValues());
		assertEqualsCheckStaticType(
				EnumWithUmlautAndSpecialCharInValues.Value_with_Umlauts__and_special_chars__u00E4__u00D6__u00DF__u00E9__u0027___u005E___u007B___u0022_,
				enum1Value);
		assertEqualsCheckStaticType("Value_with_Umlauts__and_special_chars_äÖßé' ^ { \"", enum1Value._stringValue());

		PredefinedEnum enum2Value = baseDm._at(pG.fieldWithPredefinedType());
		assertEqualsCheckStaticType(PredefinedEnum.Enum_Value_A, enum2Value);
		assertEqualsCheckStaticType("Enum Value A", enum2Value._stringValue());

		assertEqualsCheckStaticType("someString", baseDm._at(pG.lowercase_Field_with_underscores()));

		EnumFieldWithNumberAndSpecialCharValue enum3Value = baseDm._at(pG.enumFieldWithNumberAndSpecialCharValue());
		assertEqualsCheckStaticType(
				EnumFieldWithNumberAndSpecialCharValue.u007B___u007D___u005B___u003C___u005C_____u005C__u005C___u005C_n,
				enum3Value);
		assertEqualsCheckStaticType("{ } [ < \\   \\\\ \\n", enum3Value._stringValue());

		assertEqualsCheckStaticType("someString", baseDm._at(pRep13.stringField()));
		assertEqualsCheckStaticType(BigDecimal.valueOf(42), baseDm._at(pRep11.numberField()));
		Assertions.assertEquals(BigDecimal.valueOf(42), baseDm._at(pRep13.numberWithLeadingZerosField()));
		Assertions.assertEquals("0042", baseDm._at(pRep11.numberWithLeadingZerosField()));
		baseDm._with(pRep13.numberWithLeadingZerosField(),
				(Object) "00123"); // numbers with leading zeros have type object because they can be either string or BigDecimal
		assertEqualsCheckStaticType(i1, baseDm._at(pRep11.dateField()));
		Assertions.assertEquals(i1, baseDm._at(pRep11.partiallyKnownDateField()));
		Assertions.assertEquals("2023-11", baseDm._at(pRep13.partiallyKnownDateField()));
		baseDm._with(pRep11.partiallyKnownDateField(),
				(Object) "partially known dates are Object because they can be both instant and string, dynamically");
		assertEqualsCheckStaticType(i1, baseDm._at(pRep11.dateTimeField()));
		assertEqualsCheckStaticType(i1, baseDm._at(pRep11.timeField()));
		assertEqualsCheckStaticType(i1, baseDm._at(pRep11.dateFragmentField()));
		assertEqualsCheckStaticType(InstantRange.of(i1, i2), baseDm._at(pRep11.dateRangeField()));
		assertEqualsCheckStaticType(InstantRange.of(i1, i2), baseDm._at(pRep11.dateRangeFieldMMDD()));
		assertEqualsCheckStaticType((Boolean) null, baseDm._at(pRep11.confirmField()));
		assertEqualsCheckStaticType(Boolean.TRUE, baseDm._at(pRep13.confirmField()));
		assertEqualsCheckStaticType(Boolean.FALSE, baseDm._at(pRep11.booleanField()));
		assertEqualsCheckStaticType(Boolean.TRUE, baseDm._at(pRep13.booleanField()));
		assertEqualsCheckStaticType((Boolean) null, baseDm._at(pRep14.booleanField()));
		assertEqualsCheckStaticType("custom fields are strings", baseDm._at(pRep11.customField()));
		EnumField enum4Value = baseDm._at(pRep11.enumField());
		assertEqualsCheckStaticType(EnumField.val2, enum4Value);
		assertEqualsCheckStaticType("val2", enum4Value._stringValue());

		// A12K-3592: enum class names are now capitalized (LowerCaseEnumField, not lowerCaseEnumField)
		LowerCaseEnumField enum5Value = baseDm._at(pRep11.lowerCaseEnumField());
		assertEqualsCheckStaticType(LowerCaseEnumField.optA, enum5Value);
		assertEqualsCheckStaticType("optA", enum5Value._stringValue());
	}

	@Test void testHintListConstantsClass() {
		// A12K-3825: string fields with hintList generate a constants class with per-locale inner classes
		// Verify the constants class exists and has the expected structure
		Assertions.assertEquals("rot", StringFieldWithHintList.De.rot);
		Assertions.assertEquals("grün", StringFieldWithHintList.De.gr_u00FC_n);
		Assertions.assertEquals("red", StringFieldWithHintList.En.red);
		Assertions.assertEquals("green", StringFieldWithHintList.En.green);

		// The field type is still String, not the constants class
		BaseDm doc = BaseDm._empty()._with(BaseDm._pointer().repeatableRg(1).stringFieldWithHintList(), "red");
		assertEqualsCheckStaticType("red", doc._at(BaseDm._pointer().repeatableRg(1).stringFieldWithHintList()));
		// Can also use a value not in the hint list (it's a non-closed enumeration)
		BaseDm doc2 = BaseDm._empty()._with(BaseDm._pointer().repeatableRg(1).stringFieldWithHintList(), "purple");
		assertEqualsCheckStaticType("purple", doc2._at(BaseDm._pointer().repeatableRg(1).stringFieldWithHintList()));
	}

	@Test void testConfirmfieldSpecialCase() {
		BaseDm doc = BaseDm._empty();
		var confirmPointer = BaseDm._pointer().repeatableRg(1).confirmField();
		var err = Assertions.assertThrows(IllegalArgumentException.class, () -> doc._with(confirmPointer, Boolean.FALSE));
		Assertions.assertTrue(err.getMessage().startsWith("Boolean.FALSE is not a valid value for confirm fields"),
				err.getMessage());
	}

	@Test void testOtherCollisions() {
		// - access all paths once, to make sure that the untyped paths in the typed accessors are correct.
		// - implicitly check that the typings were generated and compiled successully, by referencing the types here
		DocumentV2 untypedDoc = DocumentV2.empty("someId").withBatchUpdates(List.of(
				UpdateAction.putField("/RG/pMayCollideWithPointerClassName/f", FieldInstanceV2.ofValue("myValue1")),
				UpdateAction.putField("/RG/PMayAlsoCollideWithPointerClassName/f", FieldInstanceV2.ofValue("myValue2")),
				UpdateAction.putField("/RG/MayAlsoCollideWithPointerClassName/f", FieldInstanceV2.ofValue("myValue3")),
				UpdateAction.putField("/RG/MayAlsoCollideWithPointerClassName/MayAlsoCollideWithPointerClassName",
						FieldInstanceV2.ofValue("myValue4")),
				UpdateAction.putField("/RG/MayCollideWithPointerClassName/f", FieldInstanceV2.ofValue("myValue5")),
				UpdateAction.putField("/RG/MayCollideWithPointerClassName/MayCollideWithPointerClassName",
						FieldInstanceV2.ofValue("v2"))
		));
		OtherCollisions baseDm = OtherCollisions._viewOf(untypedDoc);
		PRG<OtherCollisions> rgPointer = OtherCollisions._pointer().rG();
		assertEqualsCheckStaticType("myValue1", baseDm._at(rgPointer.pMayCollideWithPointerClassName().f()));
		assertEqualsCheckStaticType("myValue2", baseDm._at(rgPointer.pMayAlsoCollideWithPointerClassName().f()));
		assertEqualsCheckStaticType("myValue3", baseDm._at(rgPointer.mayAlsoCollideWithPointerClassName().f()));
		assertEqualsCheckStaticType("myValue4",
				baseDm._at(rgPointer.mayAlsoCollideWithPointerClassName().mayAlsoCollideWithPointerClassName()));
		assertEqualsCheckStaticType("myValue5", baseDm._at(rgPointer.mayCollideWithPointerClassName().f()));
		MayCollideWithPointerClassName enumValue = baseDm._at(
				rgPointer.mayCollideWithPointerClassName().mayCollideWithPointerClassName());
		assertEqualsCheckStaticType(MayCollideWithPointerClassName.v2, enumValue);
		assertEqualsCheckStaticType("v2", enumValue._stringValue());
	}

	static void assertEqualsCheckStaticType(String a, String b) {
		Assertions.assertEquals(a, b);
	}

	static void assertEqualsCheckStaticType(BigDecimal a, BigDecimal b) {
		Assertions.assertEquals(a, b);
	}

	static void assertEqualsCheckStaticType(Instant a, Instant b) {
		Assertions.assertEquals(a, b);
	}

	static void assertEqualsCheckStaticType(InstantRange a, InstantRange b) {
		Assertions.assertEquals(a, b);
	}

	static void assertEqualsCheckStaticType(Boolean a, Boolean b) {
		Assertions.assertEquals(a, b);
	}

	static <T extends Enum<T>> void assertEqualsCheckStaticType(T a, T b) {
		Assertions.assertEquals(a, b);
	}

	@Test void testCollisionsWithDefaultImportsMinimal() {
		// Test that the generated class loads and has basic behavior. As a double-check that compilation was successful
		CollisionsWithDefaultImports empty = CollisionsWithDefaultImports._viewOfCheckDmId(
				DocumentV2.empty("CollisionsWithDefaultImports"));
		var p1 = CollisionsWithDefaultImports._pointer().groupInstanceV2().typedDocumentView(2).date();
		Instant i1 = empty._at(p1);
		Instant i2 = Instant.now();
		CollisionsWithDefaultImports withDate = empty._with(p1, i2);
		Assertions.assertEquals(i2, withDate._at(p1));
		Assertions.assertNull(i1);
		Assertions.assertEquals(i2, withDate._unwrap().fieldValue("/GroupInstanceV2/TypedDocumentView[2]/Date"));
	}

	@Test void testCollisionsWithReservedNamesAndPackagesMinimal() {
		CollisionsWithReservedNamesAndPackages empty = CollisionsWithReservedNamesAndPackages._viewOfCheckDmId(
				DocumentV2.empty("CollisionsWithReservedNamesAndPackages"));
		var p1 = CollisionsWithReservedNamesAndPackages._pointer();
		p1.escaped_new(2).com().mgmtp().f1();
		var p2 = p1.escaped_new(1).java().util(4).instant();
		p1.escaped_int().escaped_long().integer();
		Instant i1 = Instant.now();
		Assertions.assertEquals(i1, empty._with(p2, i1)._at(p2));

		var pEnum = p1.escaped_int().escaped_long().escaped_return();
		CollisionsWithReservedNamesAndPackages withEnum = empty._with(pEnum, Escaped_return.Escaped_package);
		// test with untyped document too, to make sure that the subgroup names in the generated accessors match and are not accidentally escaped too
		DocumentPointer untypedPointer = DocumentPointer.of("/int/long/return");
		Assertions.assertEquals("package", withEnum._unwrap().fieldValue(untypedPointer));
		// also test retrival
		DocumentV2 untypedDocument = DocumentV2.empty("CollisionsWithReservedNamesAndPackages")
				.withFieldValue(untypedPointer, "42");
		Assertions.assertEquals(Escaped_return.Escaped_42,
				CollisionsWithReservedNamesAndPackages._viewOf(untypedDocument)._at(pEnum));

		// test enum name collisions
		var pCollectorsCollectors = p1.escaped_int().escaped_long().collectors();
		CollisionsWithReservedNamesAndPackages withCollectors = empty._with(pCollectorsCollectors,
				com.mgmtp.a12.kernel.md.facade.test.generated_typings.collisionswithreservednamesandpackages.views._collisionswithreservednamesandpackages._escaped_int._escaped_long
						.Collectors.Collectors);
		Assertions.assertEquals(
				com.mgmtp.a12.kernel.md.facade.test.generated_typings.collisionswithreservednamesandpackages.views._collisionswithreservednamesandpackages._escaped_int._escaped_long
						.Collectors.Collectors,
				withCollectors._at(pCollectorsCollectors));
		Assertions.assertEquals("Collectors", withCollectors._at(pCollectorsCollectors)._stringValue());

		TypedPointer<CollisionsWithReservedNamesAndPackages, String> dp = p1.escaped_new(2).com().mgmtp().escaped_Default();
		Assertions.assertEquals("foo", empty._with(dp, "foo")
				.escaped_new().get(1).com().mgmtp().escaped_Default());
	}

	@Test void testEmptyFactoryMethod() {
		// document:
		DomainContact dc = DomainContact._empty();
		Assertions.assertEquals(DocumentV2.empty("DomainContact"), dc._unwrap());
		// group:
		com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontact.views._domaincontact._contact.Address addr =
				com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontact.views._domaincontact._contact.Address._empty();
		Assertions.assertEquals(GroupInstanceV2.empty(), addr._unwrap());
	}

	@Test void testWithIncludesSharedClassesMinimal() {
		WithIncludes base = WithIncludes._empty();

		var pIncludeGroupI_3_18 = WithIncludes._pointer().rG1(3).includeGroup1(18);
		// insert some value for subsequent tests
		base = base._with(pIncludeGroupI_3_18.g1(3).include2MountPoint(3).g1().fBar(), BigDecimal.valueOf(42));

		InculdeRG ic1 = base._at(pIncludeGroupI_3_18); // check that the compiler-derived type matches the expectation

		// check that compiler accepts assignment to other include of same type:
		WithIncludes copyMovedSubtree = base._with(WithIncludes._pointer().rG2().gFooBar().includeGroup1B(2), ic1);
		BigDecimal actualFromCopiedLocation = copyMovedSubtree._at(WithIncludes._pointer().rG2().gFooBar().includeGroup1B(2)
				.g1(3).include2MountPoint(3).g1().fBar());
		Assertions.assertEquals(BigDecimal.valueOf(42), actualFromCopiedLocation);
	}

	@Test void testIncludesExternalModels() {
		// just a smoke test of the generated typings - here we don't checke the general included-dm behavior
		WithExternalIncludes doc = WithExternalIncludes._empty()
				._with(WithExternalIncludes._pointer().rG1_includeLocal().f1(), "f1 in local include")
				._with(WithExternalIncludes._pointer().rG2_includeExternalA().f1(), "f1 in external include a")
				._with(WithExternalIncludes._pointer().rG3_includeExternalB().f1(), "f1 in external include b")
				;
		// put the full FQN here, so we see (and the compiler checks), that the package of the external DM is indeed
		// separate (externalincludeddma) from the WithExternalIncludes dm
		com.mgmtp.a12.kernel.md.facade.test.generated_typings.externalincludeddma.views._externalincludeddma.RgOfExtInclDmA
				includedRg = doc._at(WithExternalIncludes._pointer().rG2_includeExternalA());
		// sanity check
		Assertions.assertEquals(
			DocumentV2.empty("WithExternalIncludes")
				.withFieldValue("RG1_includeLocal/f1",     "f1 in local include")
				.withFieldValue("RG2_includeExternalA/f1", "f1 in external include a")
				.withFieldValue("RG3_includeExternalB/f1", "f1 in external include b"),
			doc._unwrap());
	}

	@Test void testListPointers() {
		TypedPointer<DomainContact, List<Phones>> phonesListPointer = DomainContact._pointer().contact().phones();
		DomainContact dcWithPhones = DomainContact._viewOf(deserializeDocumentV2("""
				{ "Contact" : {
					"PersonalData" : {
					"Photo" : {"size":"1234"},
					"DateOfBirth" : "2023-01-01"
					},
					"Phones" : [{}, {}, { "Type": "MOBILE" }]
				}}""", dmStringDomainContact()).document());
		List<Phones> phonesList = dcWithPhones._at(phonesListPointer);
		Assertions.assertEquals(GroupInstanceV2.empty(), phonesList.get(0)._unwrap());
		Assertions.assertEquals(Type.MOBILE, phonesList.get(2).type());
		Assertions.assertEquals(Type.MOBILE, dcWithPhones._at(DomainContact._pointer().contact().phones().get(3).type()));
		Assertions.assertNull(dcWithPhones._at(DomainContact._pointer().contact().phones().get(2).type()));

		Assertions.assertEquals(deserializeDocumentV2("""
						{ "Contact" : {
							"PersonalData" : {
								"Photo" : {"size":"1234"},
								"DateOfBirth" : "2023-01-01"
							}
						}}""", dmStringDomainContact()).document(),
				dcWithPhones._with(phonesListPointer, List.of())._unwrap()
		);

		Assertions.assertEquals(deserializeDocumentV2("""
						{ "Contact" : {
							"PersonalData" : {
								"Photo" : {"size":"1234"},
								"DateOfBirth" : "2023-01-01"
							},
							"Phones" : [{ "Type": "MOBILE" }]
						}}""", dmStringDomainContact()).document(),
				dcWithPhones._with(phonesListPointer, List.of(dcWithPhones._at(DomainContact._pointer().contact().phones().get(3))))
						._unwrap()
		);
		Assertions.assertEquals(List.of(), dcWithPhones._at(DomainContact._pointer().contact().address()),
				"expect emtpy list instead of null");
		Assertions.assertNull(dcWithPhones._at(DomainContact._pointer().contact().address().get(1)));
		Assertions.assertNull(dcWithPhones._at(DomainContact._pointer().contact().address(1)));
	}

	@Test
	@Disabled("for manual checks only")
	void testPerf() {
		String dmString = dmStringDomainContact();
		String docString = resourceToStringUtf8("documents/json/A12K-1387/tutorial_application-example/ContactWithAddress.json");
		DocumentV2 documentV2 = deserializeDocumentV2(docString, dmString).document();

		DomainContact dc = null;

		int n = 10000;
		Instant t0 = Instant.now();
		for (int i = 0; i < n; i++) {
			dc = DomainContact._viewOf(documentV2);
		}
		Instant t1 = Instant.now();
		System.out.println("dt1 = " + (t1.toEpochMilli() - t0.toEpochMilli()));

		int n2 = 10000;
		Instant t3 = Instant.now();
		for (int i = 0; i < n2; i++) {
			dc.contact().personalData().email();
		}
		Instant t4 = Instant.now();
		System.out.println("dt3 = " + (t4.toEpochMilli() - t3.toEpochMilli()));
	}

	@Test void testDmFiltering() {
		// NOTE: this test would be simpler if it was a unit test directly on the filesystem-independent method in Generator
		// (a smaller integration test for the CLI would be enough)

		// see gradle file for the filter paths that are passed to the generator
		com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.pointers._domaincontact._contact.PMisc<?> testThatCompilerFindsIt = null;

		// Pointers exist
		Assertions.assertTrue(doesClassExistOnClasspath(
				"com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.pointers._domaincontact._contact.PMisc"));
		Assertions.assertTrue(doesClassExistOnClasspath(
				"com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.pointers._domaincontact._contact.PPhones"));

		// views exist
		Assertions.assertTrue(doesClassExistOnClasspath(
				"com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.views._domaincontact._contact.Phones"));
		Assertions.assertTrue(doesClassExistOnClasspath(
				"com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.views._domaincontact._contact.Misc"));
		// view / enum below an included path exists
		Assertions.assertTrue(doesClassExistOnClasspath(
				"com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.views._domaincontact._contact._phones.Type"));
		// view above an included path exists
		Assertions.assertTrue(doesClassExistOnClasspath(
				"com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.views._domaincontact.Contact"));

		// deseleted classes don't exist
		Assertions.assertFalse(doesClassExistOnClasspath(
				"com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.pointers._domaincontact._contact.PAddress"));
		Assertions.assertFalse(doesClassExistOnClasspath(
				"com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.pointers._domaincontact._contact.PPersonalData"));
		Assertions.assertFalse(doesClassExistOnClasspath(
				"com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.views._domaincontact._contact.Address"));
		Assertions.assertFalse(doesClassExistOnClasspath(
				"com.mgmtp.a12.kernel.md.facade.test.generated_typings.domaincontactphonesandmisconly.views._domaincontact._contact.PersonalData"));
	}

	@Test void testCustomAnnotations() {
		Assertions.assertTrue(com.mgmtp.a12.kernel.md.facade.test.generated_typings
			.domaincontactwithcustomannotations.views.DomainContact.class
				.isAnnotationPresent(SampleAnnotationWithRuntimeRetention.class));
		Assertions.assertTrue(com.mgmtp.a12.kernel.md.facade.test.generated_typings
				.domaincontactwithcustomannotations.views.DomainContact.class
				.isAnnotationPresent(SecondSampleAnnotationWithRuntimeRetention.class));
		// sanity check: on the normal DomainContact (not in in the domaincontactwithcustomannotations package),
		// the annotation must not be present
		Assertions.assertFalse(DomainContact.class.isAnnotationPresent(SampleAnnotationWithRuntimeRetention.class));
		// hint list constants class also gets custom annotations
		Assertions.assertTrue(com.mgmtp.a12.kernel.md.facade.test.generated_typings
				.testspecialcaseswithcustomannotations.views._basedm._repeatablerg.StringFieldWithHintList.class
				.isAnnotationPresent(SampleAnnotationWithRuntimeRetention.class));
	}

	private static boolean doesClassExistOnClasspath(String fqn) {
		try {
			Class.forName(fqn);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	// NOTE: the following util code is copy-pasted form TestDocumentV2Impl, because we can't access the src/test classes from
	// the other subproject without questionable gradle config or some more refactoring of test sources

	record DeserializationResult<D>(D document, IDocumentModel model, long dmDeserializationMilliseconds,
									long docDeserializationMilliseconds) {
	}

	static DeserializationResult<DocumentV2> deserializeDocumentV2(String documentString, String modelString) {
		return deserializeDocument(documentString, modelString, IDocumentV2Serializer::deserializeV2);
	}

	@FunctionalInterface
	interface DocDeser<D> {
		D apply(DocumentSerializerImpl serializer, Reader docReader, String modelId, DocumentDeserializationConfig docSerConfig,
				Consumer<RankedNotification> notificationConsumer);
	}

	static <D> DeserializationResult<D> deserializeDocument(String documentString, String modelString, DocDeser<D> deserialize) {
		List<RankedNotification> notifications = new ArrayList<>();
		try {
			var dmDeser = new DocumentModelSerializerImpl();
			var modelReader = new StringReader(modelString);
			Instant t0 = Instant.now();
			final IDocumentModel model = dmDeser.deserialize(modelReader);
			String dmID = model.getHeader().getId();
			Instant t1 = Instant.now();
			var docSer = new DocumentSerializerImpl(id -> model);
			var docReader = new StringReader(documentString);
			var docSerConfig = DocumentDeserializationConfig.builder().build();
			Instant t2 = Instant.now();
			D document = deserialize.apply(docSer, docReader, dmID, docSerConfig, notifications::add);
			Instant t3 = Instant.now();
			if (!notifications.isEmpty()) {
				throw new RuntimeException(
						notifications.stream().map(RankedNotification::getMessage).collect(Collectors.joining(", ")));
			}
			return new DeserializationResult<>(document, model, t1.toEpochMilli() - t0.toEpochMilli(),
					t3.toEpochMilli() - t2.toEpochMilli());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static String resourceToStringUtf8(String resourcePath) {
		try (InputStreamReader reader = new InputStreamReader(
				Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(
						resourcePath)), StandardCharsets.UTF_8)) {
			return IOUtils.toString(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static String dmStringDomainContact() {
		return resourceToStringUtf8("models/document-models/A12K-1387/tutorial_application-example/DomainContact.json");
	}
}
