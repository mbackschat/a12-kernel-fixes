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

package com.mgmtp.a12.kernel.md.document.internal.service.implV2.docs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// DO NOT RE-ENABLE THE AUTOFORMATTER. LINE-LENGTH, INDENTATIONS, LINE BREAKS ETC MATTER FOR THE
// FORMATTING IN THE RENDERED DOCUMENTATION. Formatting must only be adjusted manually, with the resulting documentation layout in mind.
// @formatter:off // disable auto formatter, because it could break code snippets that are included in the documentation
// tag::tagUntypedDocV2APIImports[]
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.Change;
import com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.DocumentChanges;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentMergeConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDmAwareDocService;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV1V2Converter;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils.CompareConfig;
// end::tagUntypedDocV2APIImports[]

// tag::tagOtherImports[]
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.utils.conversion.InstantRange;
// end::tagOtherImports[]

// @formatter:off // disable auto-formatter again, as optimize imports might remove setting from above

// NOTE: this code provides code snippets for the new Document API V2 Documentation in
// kernel-documentation-dev/DocumentAPIV2Java.adoc.
// Please make sure not to break the tag:: ... end::... snippets that are referenced from the asciidoc file
// and make sure not to break the build step that copies this source file to documentation-dev (for later processing by ascidoctor)
//
// The code is written as a Junit test in order to make sure that it gets compiled in normal CI builds,
// in order to continuously verify that the code snippets are correct.
public class TestDocumentV2CodeSnippetsForDocs {
	private static final DocumentV2 docV2 = DocumentV2.empty("myDmId");

	// tag::tagDocumentPointerConstant[]
	static final DocumentPointer SECOND_REPETITION_OF_FIELD_A = DocumentPointer.of("MyRootGroup/SubGroup[2]/fieldA");
	// end::tagDocumentPointerConstant[]

	@Test void testCreateDocumentEmpty() {
		// tag::tagCreateDocumentEmpty[]
		DocumentV2 docV2 = DocumentV2.empty("myDmId");
		// end::tagCreateDocumentEmpty[]
		Assertions.assertNotNull(docV2);
	}

	@Test void testCreateDocumentWithSubgroups() {
		RepetitionsV2 mySubgroupRepetitions = RepetitionsV2.of(List.of(GroupInstanceV2.empty()));
		Map<String, RepetitionsV2> subGroups = Map.of("g1", mySubgroupRepetitions);
		// tag::tagCreateDocumentWithSubgroups[]
		// subGroups is a Map<String, RepetitionsV2>
		DocumentV2 docV2 = DocumentV2.of("myDmId", subGroups);
		// end::tagCreateDocumentWithSubgroups[]
		Assertions.assertEquals(Set.of(Map.entry("g1", mySubgroupRepetitions)), docV2.directSubgroups());
	}

	@Test void testCreateIDocumentV1V2Converter() {
		// tag::tagCreateIDocumentV1V2Converter[]
		IDocumentV1V2Converter converter = new DocumentServiceFactory(
			dmID -> null // dm resolver is not used here
		).createDocumentV1V2Converter();
		// end::tagCreateIDocumentV1V2Converter[]

		IDocument docV1 = makeSmallDocV1();

		// tag::tagCreateDocumentConvert[]
		DocumentV2 docV2 = converter.ofDocumentV1(docV1);
		IDocument docV1Again = converter.toDocumentV1(docV2);
		// end::tagCreateDocumentConvert[]

		Assertions.assertEquals(docV1.getEntityInstances(), docV1Again.getEntityInstances());
	}

	@Test void testCreateDocumentSetupForSerialization() {
		// tag::tagCreateDocumentSetupForSerialization[]
		IDocumentModel myDm; // we need the DM first
		try (Reader dmReader = new InputStreamReader(Objects.requireNonNull(
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"document-models/small-example-for-deserialization/MyDm.json")))
		) {
			var dmSer = new DocumentModelServiceFactory().createDocumentModelSerializer();
			myDm = dmSer.deserialize(dmReader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// now that we have the DM, we can create the document serializer:
		DocumentServiceFactory serviceFactory = new DocumentServiceFactory(
				// you'll probably want to use a "real" IDocumentModelResolver instead of the map
				Map.of("MyDm", myDm)::get);
		IDocumentV2Serializer serializer = serviceFactory.createDocumentV2Serializer();
		Reader myReader = new StringReader("""
			{"RGx": [
			  { "s1": "myString", "n1": 42 },
			  { "s1": "anotherString" }
			]}""");
		java.io.Writer writer = new StringWriter();
		// now we are ready to call the serializer to deserialize and serialize the document...
		// end::tagCreateDocumentSetupForSerialization[]

		// tag::tagCreateDocumentSerialize[]
		// serializer is an IDocumentV2Serializer (obtain one from
		// DocumentServiceFactory#createDocumentV2Serializer)
		DocumentV2 docV2 = serializer.deserializeV2(myReader, "MyDm",
				DocumentDeserializationConfig.builder().build(), rankedNotification -> {});
		// serialize:
		serializer.serializeV2(docV2, writer, DocumentSerializationConfig.builder().build());
		// writer is a java.io.Writer
		// end::tagCreateDocumentSerialize[]

		Assertions.assertEquals(
				DocumentV2.empty("MyDm")
						.withFieldValue("RGx[1]/s1", "myString").withFieldValue("RGx[1]/n1", BigDecimal.valueOf(42))
						.withFieldValue("RGx[2]/s1", "anotherString"),
				docV2
		);
		// writer.toString will look approximately like
		// {
		//  "RGx": [
		//    {
		//      "n1": 42,
		//      "s1": "myString"
		//    },
		//    {
		//      "s1": "anotherString"
		//    }
		//  ]
		//}
	}

	@Test void testDocumentPointerProgrammatic() {
		// see the attribute static final DocumentPointer SECOND_REPETITION_OF_FIELD_A = DocumentPointer.of("MyRootGroup/SubGroup[2]/fieldA");
		/*static final */ DocumentPointer SECOND_REPETITION_OF_FIELD_A = DocumentPointer.of("MyRootGroup/SubGroup[2]/fieldA");

		Assertions.assertEquals(
				DocumentPointer.of("MyRootGroup/SubGroup[2]/fieldA"),
				DocumentPointer.of("MyRootGroup[1]/SubGroup[2]/fieldA[1]"));

		// tag::tagDocumentPointerProgrammatic[]
		DocumentPointer pointer2 = DocumentPointer.of(
				List.of("MyRootGroup", "SubGroup", "fieldA"),
				List.of(1, 2, 1));
		// end::tagDocumentPointerProgrammatic[]
		Assertions.assertEquals(SECOND_REPETITION_OF_FIELD_A, pointer2);

		// tag::tagDocumentPointerOverload[]
		// the group() method is overloaded to also accept the string notation for the pointer
		GroupInstanceV2 subGroup = docV2.group("MyRootGroup[2]/subGroup");
		// end::tagDocumentPointerOverload[]
	}

	@Test void testDocumentMultiPointer() {
		DocumentPointer documentPointer = DocumentPointer.of("a/b/c");
		DocumentMultiPointer documentMultiPointer;

		// tag::tagDocumentMultiPointerCreation[]
		documentMultiPointer = DocumentMultiPointer.of(documentPointer);
		// end::tagDocumentMultiPointerCreation[]

		// tag::tagDocumentMultiPointerCreationWithPath[]
		documentMultiPointer = DocumentMultiPointer.of("a[0]/b[3]/c[0]");
		// end::tagDocumentMultiPointerCreationWithPath[]

		// tag::tagDocumentMultiPointerCreationWithAllWildcards[]
		documentMultiPointer = DocumentMultiPointer.ofPathWithAllWildcards("a/b/c");
		Assertions.assertEquals(DocumentMultiPointer.of("a[0]/b[0]/c[0]"), documentMultiPointer);
		// end::tagDocumentMultiPointerCreationWithAllWildcards[]
	}

	@Test void testDocumentMultiPointerRelation() {
		// tag::tagDocumentMultiPointerRelation[]
		DocumentMultiPointer abcPointer = DocumentMultiPointer.of("a[0]/b[3]/c[0]");
		// end::tagDocumentMultiPointerRelation[]

		// tag::tagDocumentMultiPointerAncestor[]
		// Any repetition index (>0) is included where a wildcard index is specified
		DocumentPointer dp = DocumentPointer.of("a[1]/b[3]/c[100]");
		Assertions.assertTrue(abcPointer.containsAncestorOf(dp));

		// The repetition 1 of b is *not* included
		DocumentPointer wrongRepetition = DocumentPointer.of("a[1]/b[1]/c[1]");
		Assertions.assertFalse(abcPointer.containsAncestorOf(wrongRepetition));

		// Any group/field that is a part of the subtree of documentMultiPointer
		DocumentPointer subGroup = DocumentPointer.of("a[7]/b[3]/c[4]/d");
		Assertions.assertTrue(abcPointer.containsAncestorOf(subGroup));

		// Parent groups are *not* included
		DocumentPointer parent = DocumentPointer.of("a[6]/b[3]");
		Assertions.assertFalse(abcPointer.containsAncestorOf(parent));
		// end::tagDocumentMultiPointerAncestor[]

		// tag::tagDocumentMultiPointerDescendent[]
		dp = DocumentPointer.of("a[7]/b[3]");
		Assertions.assertTrue(abcPointer.containsDescendentOf(dp));

		// subgroups are not included.
		DocumentPointer subgroup = DocumentPointer.of("a[7]/b[3]/c[4]/d");
		Assertions.assertFalse(abcPointer.containsDescendentOf(subgroup));
		// end::tagDocumentMultiPointerDescendent[]

		// tag::tagDocumentMultiPointerContains[]
		// A pointer has to have the same path length, but can have different indexes,
		// where wildcard indexes are specified
		DocumentPointer match = DocumentPointer.of("a[7]/b[3]/c[4]");
		Assertions.assertTrue(abcPointer.contains(match));

		// A pointer with different path length is not included in "the set" of pointers.
		DocumentPointer differentLength = DocumentPointer.of("a[7]/b[3]");
		Assertions.assertFalse(abcPointer.contains(differentLength));
		// end::tagDocumentMultiPointerContains[]
	}

	@Test void testDocumentV2UtilsGetFieldInstances() {
		GroupInstanceV2 group = GroupInstanceV2.empty();
		Collection<Map.Entry<DocumentPointer,FieldInstanceV2>> fieldInstances;
		// tag::tagDocumentV2UtilsGetFieldInstances[]
		DocumentMultiPointer documentMultiPointer = DocumentMultiPointer.of("a[0]/b[3]/c[0]");
		fieldInstances = DocumentV2Utils.getFieldInstances(group, documentMultiPointer);
		// end::tagDocumentV2UtilsGetFieldInstances[]
	}

	@Test void testDocumentV2UtilsCompare() {
		GroupInstanceV2 oldGroupInstance = GroupInstanceV2.empty();
		GroupInstanceV2 newGroupInstance = GroupInstanceV2.empty();
		CompareConfig config;
		// tag::tagDocumentV2UtilsCompare[]
		config = DocumentV2Utils.CompareConfig.builder().emptyAndNullAsAbsent(true).build();
		DocumentChanges comparisonResult =
				DocumentV2Utils.compare(oldGroupInstance, newGroupInstance, config);
		// end::tagDocumentV2UtilsCompare[]

		Collection<Change<FieldInstanceV2>> changes;
		// tag::tagDocumentV2UtilsCompareFieldChanges[]
		changes = comparisonResult.expandedFieldChanges();
		// end::tagDocumentV2UtilsCompareFieldChanges[]
	}

	@Test void testIDmAwareDocServiceRemoveUnknowns() {
		IDocumentModel myDm;
		try (Reader dmReader = new InputStreamReader(Objects.requireNonNull(
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
						"document-models/small-example-for-deserialization/MyDm.json")))
		) {
			var dmSer = new DocumentModelServiceFactory().createDocumentModelSerializer();
			myDm = dmSer.deserialize(dmReader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		DocumentServiceFactory documentServiceFactory = new DocumentServiceFactory(
				_id -> myDm);

		DocumentV2 document = DocumentV2.empty("MyDm");
		IDmAwareDocService dmAwareDocService;

		// tag::tagIDmAwareDocServiceRemoveUnknowns[]
		dmAwareDocService =
			documentServiceFactory.createDmAwareDocService(document.getDocumentModelId());
		DocumentV2 documentWithoutUnknowns = dmAwareDocService.removeUnknowns(document);
		// end::tagIDmAwareDocServiceRemoveUnknowns[]
	}

	@Test void testIDmAwareDocServiceConvertToJavaTypeV2() {
		IDocumentModel myDm;
		try (Reader dmReader = new InputStreamReader(Objects.requireNonNull(
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
						"document-models/small-example-for-deserialization/MyDm.json")))
		) {
			var dmSer = new DocumentModelServiceFactory().createDocumentModelSerializer();
			myDm = dmSer.deserialize(dmReader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		DocumentServiceFactory documentServiceFactory = new DocumentServiceFactory(
				_id -> myDm);

		DocumentV2 document = DocumentV2.empty("MyDm");
		IDmAwareDocService dmAwareDocService =
				documentServiceFactory.createDmAwareDocService(document.getDocumentModelId());

		List<RankedNotification> notifcationReceiver = new ArrayList<>();
		// tag::tagIDmAwareDocServiceConvertToJavaTypeV2[]
		DocumentPointer fieldInstancePointer = DocumentPointer.of("/RGx[2]/n1");
		Object typedFieldValue = /* BigDecimal */ dmAwareDocService.convertToJavaTypeV2(
				fieldInstancePointer.fullName(), "1234", notifcationReceiver::add);
		document = document.withFieldValue(fieldInstancePointer, typedFieldValue);
		// end::tagIDmAwareDocServiceConvertToJavaTypeV2[]

		Assertions.assertEquals(List.of(), notifcationReceiver);
		Assertions.assertEquals(new BigDecimal(1234), document.fieldValue(fieldInstancePointer));
	}

	@Test void testFieldValue() {
		// tag::tagFieldValue[]
		Object myFieldValue = docV2.fieldValue("MyGroup[2]/myField");
		// equivalent to
		DocumentPointer ptr = DocumentPointer.of("MyGroup[2]/myField");
		Object myFieldValueAgain = docV2.fieldValue(ptr);
		// end::tagFieldValue[]
	}
	@Test void testGroup() {
		// tag::tagGroup[]
		GroupInstanceV2 subGroup = docV2.group("MyRootGroup/SubGroup[2]");
		// equivalent to
		DocumentPointer ptr = DocumentPointer.of("MyRootGroup/SubGroup[2]");
		GroupInstanceV2 subGroupAgain = docV2.group(ptr);
		// end::tagGroup[]
	}
	@Test void testGroupAllRepetitions() {
		// tag::tagGroupAllRepetitions[]
		for (GroupInstanceV2 subGroup : docV2.groupAllRepetitions("MyGroup[0]")) {
			// do something with subGroup
		}
		// end::tagGroupAllRepetitions[]

		// tag::tagGroupAllRepetitionsAssign[]
		RepetitionsV2 groupRepetitions = docV2.groupAllRepetitions("MyGroup[0]");
		GroupInstanceV2 subGroup1 = groupRepetitions.get(1); // 1-based index
		// end::tagGroupAllRepetitionsAssign[]
	}
	@Test void testReadFromSubgroup() {
		DocumentV2 docV2 = DocumentV2.empty("myDmId")
				.withFieldValue("MyRootGroup/SubGroup[2]/myField", "myFieldValue");

		// tag::tagReadFromSubgroup[]
		GroupInstanceV2 subGroup = docV2.group("MyRootGroup/SubGroup[2]");

		// get the field value at /MyRootGroup/SubGroup[2]/myField:
		Object myFieldValue = subGroup.fieldValue("myField");
		// get the group at /MyRootGroup/SubGroup[2]/subSubGroup:
		GroupInstanceV2 subSubGroup = subGroup.group("subSubGroup");
		// get /MyRootGroup/SubGroup[2]/repeatableSubSubGroup[0]:
		RepetitionsV2 subSubGroupRepetitions = subGroup.groupAllRepetitions("repeatableSubSubGroup[0]");
		// end::tagReadFromSubgroup[]
	}

	// Updating Documents and Groups
	@Test void testWithFieldValue() {
		DocumentV2 docV2 = DocumentV2.empty("myDmId");

		// tag::tagWithFieldValue[]
		docV2 = docV2.withFieldValue("Address[2]/Street", "Taunusstr");
		// end::tagWithFieldValue[]
	}

	@Test void tagWithFieldValueNewVariable() {
		// tag::tagWithFieldValueNewVariable[]
		// create the document
		DocumentV2 document1 = DocumentV2.empty("myDmId")
				.withFieldValue("Phones[1]/PhoneNo", "1234")
				.withFieldValue("Phones[1]/Type", "Home")
				.withFieldValue("Phones[2]/Type", "Office")
				.withFieldValue("Phones[3]/PhoneNo", "9876")
				.withFieldValue("Phones[3]/Type", "Mobile")
				.withFieldValue("PersonalData/Nationality", "german")
				.withFieldValue("PersonalData/Email", "my@mail")
				.withFieldValue("Address[1]/Street", "Machtlfing")
				.withFieldValue("Address[1]/City", "Munich");

		// update/add a single field
		DocumentV2 document2 = document1.withFieldValue("Address[2]/Street", "Taunusstr");

		// the document2 itself is new
		Assertions.assertNotSame(document1, document2);
		// the address repetitions-list is new
		Assertions.assertNotSame(
				document1.groupAllRepetitions("Address[0]"),
				document2.groupAllRepetitions("Address[0]"));
		// the Address[1] is reused from document1
		Assertions.assertSame(document1.group("Address[1]"), document2.group("Address[1]"));
		// end::tagWithFieldValueNewVariable[]

	}

	@Test void testWithGroup() {
		DocumentV2 docV2 = DocumentV2.empty("myDmId");
		GroupInstanceV2 newGroup = GroupInstanceV2.empty();

		// tag::tagWithGroup[]
		docV2 = docV2.withGroup("MyGroup", newGroup); // where newGroup is some GroupInstanceV2
		// end::tagWithGroup[]

		// tag::tagWithGroup2[]
		docV2 = docV2.withGroup("MyGroup[2]", newGroup);
		// end::tagWithGroup2[]
	}
	@Test void testWithGroupRepetitionInserted() {
		DocumentV2 docV2 = DocumentV2.empty("myDmId");
		GroupInstanceV2 newGroup = GroupInstanceV2.empty();
		RepetitionsV2 myRepetitions = RepetitionsV2.empty();

		// tag::tagWithGroupRepetitionInserted[]
		docV2 = docV2.withGroupRepetitionInserted("MyGroup[1]", newGroup);
		// end::tagWithGroupRepetitionInserted[]

		// tag::tagWithGroupRepetitionAppended[]
		docV2 = docV2.withGroupRepetitionAppended("MyGroup[0]", newGroup);
		// end::tagWithGroupRepetitionAppended[]

		// tag::tagWithGroupAllRepetitions[]
		docV2 = docV2.withGroupAllRepetitions("foo[0]", myRepetitions); // myRepetitions is a RepetitionsV2
		// end::tagWithGroupAllRepetitions[]
	}
	@Test void testExampleUseGroupUpdatingMethod() {
		DocumentPointer mySubgroupPointer = DocumentPointer.of("Rg[2]/MySubGroup");
		DocumentV2 docV2 = DocumentV2.empty("myDmId").withGroup(mySubgroupPointer, GroupInstanceV2.empty());

		// tag::tagExampleUseGroupUpdatingMethod[]
		// mySubgroupPointer is a DocumentPointer
		GroupInstanceV2 updatedSubGroup = processAndUpdateSubgroup(docV2.group(mySubgroupPointer));
		DocumentV2 updatedDocument = docV2.withGroup(mySubgroupPointer, updatedSubGroup);
		// end::tagExampleUseGroupUpdatingMethod[]
	}

	@Test void testComplexWithFieldValue() {
		DocumentV2 docV2 = DocumentV2.empty("myDmId").
				withFieldValue("g1[1]/g1_sub/field1", "field1Value").
				withFieldValue("g1[1]/g1_sub/field2", "field2Value").
				withGroup("g1[2]/g1_sub", GroupInstanceV2.empty()).
				withFieldValue("g1[3]/g1_sub/field1", "field1Value").
				withGroup("g2", GroupInstanceV2.empty()).
				withFieldValue("g3/g3_sub/field2", "field2Value");

		// tag::tagComplexWithFieldValue[]
		DocumentV2 changedDocument = docV2.withFieldValue("g3[2]/field1", "field1Value");
		Assertions.assertEquals("field1Value", changedDocument.fieldValue("g3[2]/field1"));
		// end::tagComplexWithFieldValue[]

		// tag::tagComplexWithFieldValue_Original[]
		Assertions.assertEquals(null, docV2.field("g3[2]/field1"));
		// tag::tagComplexWithFieldValue_Original[]
	}

	@Test void testAddMissingAncestors() {
		// tag::tagAddMissingAncestors[]
		DocumentV2 docV2 = DocumentV2.empty("myDmId")
				.withFieldValue("myRootGroup/mySubGroup[3]/fieldA", "some value");
		// docV2 will contain the ancestor groups myRootGroup, three repetitions of
		// subgroup mySubGroup (the first two are empty groups), and a field
		// instance with value "foo bar" in the 3rd repetition of mySubGroup.
		// ==>
		// {"myRootGroup": {"mySubGroup": [{}, {}, {"fieldA": "foo bar"}]}}
		// end::tagAddMissingAncestors[]
	}

	@Test void testWith() {
		DocumentV2 docV2 = DocumentV2.empty("myDmId");

		// tag::tagWithFieldRemoved[]
		docV2 = docV2.withFieldRemoved("myGroup/myField");
		// end::tagWithFieldRemoved[]

		// tag::tagWithGroupRemoved[]
		docV2 = docV2.withGroupRemoved("myGroup");
		// end::tagWithGroupRemoved[]

		// tag::tagWithGroupRemoved2[]
		docV2 = docV2.withGroupRemoved("myRepeatableGroup[2]");
		// end::tagWithGroupRemoved2[]

		// tag::tagWithGroupEmptiedButNotRemoved[]
		docV2 = docV2.withGroupEmptiedButNotRemoved("myRepeatableGroup[1]");
		// end::tagWithGroupEmptiedButNotRemoved[]

		// tag::tagWithGroupAllRepetitionsRemoved[]
		docV2 = docV2.withGroupAllRepetitionsRemoved("myGroupB[1]/mySubGroup[0]");
		// end::tagWithGroupAllRepetitionsRemoved[]

		docV2 = docV2 // this is here just in order to test that the operation works/checks the pointer indices when the removed group is indeed present
				.withFieldValue("myGroupB[1]/mySubGroup[3]/f1", "foo")
				.withGroupAllRepetitionsRemoved("myGroupB[1]/mySubGroup[0]");

	}
	@Test void testTraverseField() {
		// tag::tagTraverseField[]
		docV2.traverse(new IDocumentV2Visitor() {
			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field) {
				IDocumentV2Visitor.super.visitField(pointerRelativeToBase, field);
				// record some information about field and / or pointerRelativeToBase
			}
		});
		// end::tagTraverseField[]
	}

	@Test void testTraverseSpecific() {
		// tag::tagTraverseSpecific[]
		// we want to collect all group instances matching a given path
		Map<DocumentPointer, GroupInstanceV2> groupInstances = new HashMap<>();

		// we want to visit every group that matches the pointer
		DocumentMultiPointer groupsWeAreLookingFor = DocumentMultiPointer.of("a/b[0]/c[5]");

		// create a custom visitor implementation that traverses groups
		IDocumentV2Visitor groupVisitor = new IDocumentV2Visitor() {
			@Override public DescendType visitGroup(
					DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
				// the parent method should always be called
				IDocumentV2Visitor.super.visitGroup(pointerRelativeToBase, group);

				// are we on the right track with the current group, successfully matches:
				// - '' (root)
				// - 'a[1]'
				// - 'a[1]/b[X]' (any repetition 'X')
				// - 'a[1]/b[X]/c[5]'
				// no other subgroups
				if (groupsWeAreLookingFor.containsDescendentOf(pointerRelativeToBase)) {
					// if the group is reached
					if (groupsWeAreLookingFor.contains(pointerRelativeToBase)) {
						groupInstances.put(pointerRelativeToBase, group);

						// group found, subgroups are no longer relevant
						return DescendType.SKIP_CHILDREN;
					} else {
						// only visit the specific subgroup that is given by the pointer
						String nextName =
							groupsWeAreLookingFor.getPartAtLevel(pointerRelativeToBase.size()).name();
						return DescendType.visitSingleSubgroup(nextName);
					}
				} else {
					// we don't have to check further if this is
					// either a different path or
					// a different repetition as the one we are searching for
					return DescendType.SKIP_CHILDREN;
				}
			}

			/* the following methods are also available
			void endVisitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group);
			RepetitionDescendType visitRepetitions(DocumentPointer pointerRelativeToBase, RepetitionsV2 rep);
			void endVisitRepetitions(DocumentPointer pointerRelativeToBase, RepetitionsV2 rep);
			void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field);
			*/
		};

		// document we want to check
		DocumentV2 docV2 = DocumentV2.empty("myDmId")
				.withGroup("a/b[1]/c[5]", GroupInstanceV2.empty())
				.withGroup("a/b[2]/c[5]", GroupInstanceV2.empty())
				// this instance should not be returned as it does not match the pointer
				.withGroup("a[2]/b[1]/c[5]", GroupInstanceV2.empty())
				.withFieldValue("a/b[4]/c[5]/cField", "Value");
		docV2.traverse(groupVisitor);

		Assertions.assertEquals(3, groupInstances.size());

		Assertions.assertNotNull(groupInstances.get(DocumentPointer.of("a/b/c[5]")));
		Assertions.assertNotNull(groupInstances.get(DocumentPointer.of("a/b[2]/c[5]")));
		Assertions.assertEquals(GroupInstanceV2.empty().withDirectFieldValue("cField", "Value"),
				groupInstances.get(DocumentPointer.of("a/b[4]/c[5]")));

		Assertions.assertNull(groupInstances.get(DocumentPointer.of("a[1]/b[3]/c[5]")));
		Assertions.assertNull(groupInstances.get(DocumentPointer.of("a[2]/b[1]/c[5]")));
		// end::tagTraverseSpecific[]
	}

	@Test void testTraverseAllSubgroups() {
		DocumentPointer pointerRelativeToBase = DocumentPointer.of("");
		DocumentMultiPointer pointerToVisit = DocumentMultiPointer.of("");
		// tag::tagTraverseAllSubgroups[]
		if (
			// Check if we need to traverse further to reach the searched pointer
			pointerToVisit.containsDescendentOf(pointerRelativeToBase) ||
			// Check if we are in any subgroup
			pointerToVisit.containsAncestorOf(pointerRelativeToBase)
		) {
			// ...
		}
		// end::tagTraverseAllSubgroups[]
	}

	@Test void testConvertDocumentToGroup() {
		DocumentV2 doc = DocumentV2.empty("myDmId")
				.withFieldValue("subgroup/someField", "someValue");
		// tag::tagConvertDocumentToGroup[]
		GroupInstanceV2 asGroup = doc.toRootRootGroup();

		Assertions.assertEquals(
			doc    .fieldValue("subgroup/someField"),
			asGroup.fieldValue("subgroup/someField"));

		// in contrast to the doc, the group allows direct fields
		GroupInstanceV2 groupWithDirectField = asGroup.withDirectFieldValue("directField", "someValue");
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> doc.withDirectFieldValue("directField", "someValue"));
		// end::tagConvertDocumentToGroup[]

		// tag::tagConvertGroupToDocument[]
		DocumentV2 asDocAgain = DocumentV2.ofRootRootGroup("myDmId", asGroup);
		Assertions.assertEquals(doc, asDocAgain);
		// end::tagConvertGroupToDocument[]
	}
	@Test void testMergeSimple() {
		DocumentV2 otherDocV2 = DocumentV2.empty("someDmId");

		// tag::tagMergeSimple[]
		DocumentV2 mergedDocument = docV2.withMerged(otherDocV2);
		// end::tagMergeSimple[]
	}

	@Test void testMergePickReps() {
		DocumentV2 otherDocV2 = DocumentV2.empty("someDmId");

		// tag::tagMergePickReps[]
		DocumentV2 mergedDocument = docV2.withMerged(
			otherDocV2,
			// pass some DocumentPointer -> boolean function (as a minimal IDocumentMergeConfig):
			pointer -> shouldRepetitionsBeConcatenated(pointer)
		);
		// end::tagMergePickReps[]
	}

	@Test void testMergeFullConfig() {
		DocumentV2 otherDocV2 = DocumentV2.empty("someDmId");

		// tag::tagMergeFullConfig[]
		DocumentV2 mergedDocument = docV2.withMerged(
			otherDocV2,
			new IDocumentMergeConfig() {
				@Override public boolean shouldUseCustomMergeForRepetitions(DocumentPointer pointer) {
					return shouldRepetitionsBeConcatenated(pointer);
				}

				@Override public Collection<UpdateAction> customMergeForRepetitions(DocumentPointer ptr,
						RepetitionsV2 repCurrent, RepetitionsV2 repOther) {
					// see the default implementation of customMergeForRepetitions
					// as an example for myCustomMergeFunction
					return myCustomMergeFunction(ptr, repCurrent, repOther);
				}
			}
		);
		// end::tagMergeFullConfig[]
	}

	@Test void testWithBatchUpdates() {
		DocumentV2 docV2 = DocumentV2.empty("myDmId");
		GroupInstanceV2 subGroup = GroupInstanceV2.empty();
		GroupInstanceV2 otherSubGroup = GroupInstanceV2.empty();
		DocumentPointer mySubGroupPointer = DocumentPointer.of("myGroup/subGroup[5]");

		// tag::tagWithBatchUpdates[]
		docV2 = docV2.withBatchUpdates(List.of(
			UpdateAction.putFieldValue("myGroup/fieldA", "foo"),
			UpdateAction.putField("myGroup/fieldB", FieldInstanceV2.ofValue("foo")),
			UpdateAction.putGroup("myGroup/subGroup[1]", subGroup),
			UpdateAction.putGroup(mySubGroupPointer, otherSubGroup)
			// ...
			// ... e.g. thousands of inserts
		));
		// end::tagWithBatchUpdates[]
	}

	@Test void testCreate() {
		Map<String, FieldInstanceV2> fields = Map.of();
		Map<String, RepetitionsV2> subGroups = Map.of();
		List<GroupInstanceV2> repetitionsList = List.of();
		Instant i1 = Instant.parse("2023-01-01T12:45:00Z");
		Instant i2 = Instant.parse("2024-01-01T12:45:00Z");
		InstantRange irange = InstantRange.of(i1, i2);
		Date[] dateRange = new Date[] { Date.from(irange.start()), Date.from(irange.end()) };
		IFieldInstance fieldV1 = getMinimalDocFactory().createFieldInstance("/a/b", new int[] { 2, 1 });
		fieldV1.setValue(dateRange);

		// tag::tagCreateGroup[]
		GroupInstanceV2 g = GroupInstanceV2.empty();
		// or
		GroupInstanceV2 g2 = GroupInstanceV2.of(subGroups, fields); // subGroups is a Map<String, RepetitionsV2>,
		// fields is a Map<String, FieldInstanceV2>
		// end::tagCreateGroup[]

		// tag::tagCreateRepetitions[]
		RepetitionsV2 r = RepetitionsV2.empty();
		RepetitionsV2 r2 = RepetitionsV2.of(repetitionsList); // repetitionsList is a List<GroupInstanceV2>
		// end::tagCreateRepetitions[]

		// tag::tagOfValue[]
		FieldInstanceV2 f = FieldInstanceV2.ofValue(BigDecimal.valueOf(42));
		// allowed values: String, BigDecimal, Boolean, Instant, InstantRange, null
		// end::tagOfValue[]

		// tag::tagOfV1Value[]
		FieldInstanceV2 f2 = FieldInstanceV2.ofV1Value(fieldV1.getValue().orElse(null));
		// V1 values are wrapped in an Optional<Object> and need to be unwrapped first.
		// Instead of Instant and InstantRange, V1 fields use Date and Date[];
		// ofV1Value takes care of the conversion.
		// end::tagOfV1Value[]

		Assertions.assertEquals(irange, f2.value());

		// tag::tagCreateInstantRange[]
		InstantRange instantRange = InstantRange.of(i1, i2);
		// end::tagCreateInstantRange[]
	}

	private static Collection<UpdateAction> myCustomMergeFunction(DocumentPointer ptr, RepetitionsV2 repCurrent,
			RepetitionsV2 repOther) {
		return List.of(); // dummy impl
	}

	private static boolean shouldRepetitionsBeConcatenated(DocumentPointer pointer) {
		return false; // dummy impl
	}

	// tag::tagGroupProcessingMethodExample[]
	private GroupInstanceV2 processAndUpdateSubgroup(GroupInstanceV2 subGroup) {
		// withFieldValue, withGroup, etc. work on the subgroup too, like on a document
		subGroup = subGroup.withFieldValue("myField", "new value");
		// ... (update subGroup in some complex way)
		return subGroup;
	}
	// end::tagGroupProcessingMethodExample[]

	private static IDocument makeSmallDocV1() {
		IDocumentFactory f = getMinimalDocFactory();
		IDocument docV1 = f.createDocument("myDmId");
		docV1.addEntityInstance(f.createFieldInstance("/a/b/c", new int[] { 2, 3, 1 }));
		docV1.addEntityInstance(f.createGroupInstance("/a/b", new int[] { 2, 3 }));
		docV1.addEntityInstance(f.createGroupInstance("/a/b", new int[] { 2, 2 }));
		docV1.addEntityInstance(f.createGroupInstance("/a/b", new int[] { 2, 1 }));
		docV1.addEntityInstance(f.createGroupInstance("/a/b", new int[] { 1, 3 }));
		docV1.addEntityInstance(f.createGroupInstance("/a/b", new int[] { 1, 2 }));
		docV1.addEntityInstance(f.createGroupInstance("/a/b", new int[] { 1, 1 }));
		docV1.addEntityInstance(f.createGroupInstance("/a", new int[] { 2 }));
		docV1.addEntityInstance(f.createGroupInstance("/a", new int[] { 1 }));
		return docV1;
	}

	private static IDocumentFactory getMinimalDocFactory() {
		return new DocumentServiceFactory(dmId -> {
			throw new RuntimeException("not implemented");
		}).createDocumentFactory();
	}
}
