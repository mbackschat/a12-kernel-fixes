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

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedRepetitionsPointer;

import my.generated.typings.domaincontact.pointers._domaincontact._contact.PPhones;
import my.generated.typings.domaincontact.views.DomainContact;
import my.generated.typings.domaincontact.views._domaincontact._contact.Address;
import my.generated.typings.domaincontact.views._domaincontact._contact.Phones;
import my.generated.typings.domaincontact.views._domaincontact._contact._phones.Type;

// NOTE: currently there is no code-snippet in the imports, so we can leave the autoformatter enabled until here.
// this is nice because it prevents bizarre formatting that otherwise happens when intellij automatically adds
// imports
// @formatter:off // disable autoformatter, because it could break code snippets that are included in the documentation

// NOTE: this code provides code snippets for the new Document API V2 Documentation in
// kernel-documentation-dev/DocumentAPIV2Java.adoc.
// Please make sure not to break the tag:: ... end::... snippets that are referenced from the asciidoc file
// and make sure not to break the build step that copies this source file to documentation-dev (for later processing by ascidoctor)
//
// The code is written as a Junit test in order to make sure that it gets compiled in normal CI builds,
// in order to continuously verify that the code snippets are correct.
public class TestTypedAccessorSnippetsForDocs {
	private final DocumentV2 docV2 = DocumentV2.empty("DomainContact")
			.withFieldValue("Contact/Address[5]/City", "Munich")
			.withFieldValue("Contact/Address[4]/City", "Paris");

	@Test void codeSnippets() {
		{
			// tag::tagReadFromExistingDoc[]
			// The DomainContact class is the generated view class for the
			// document model named DomainContact.
			// With the gradle config that is used here, it can be imported from
			// my.generated.typings.domaincontact.views.DomainContact
			DomainContact typedDocument = DomainContact._viewOf(docV2);
			for (Address address : typedDocument._at(DomainContact._pointer().contact().address())) {
				String city = address.city();
				String country = address.country();
				// ...
			}
			// end::tagReadFromExistingDoc[]
		}
		{
			// tag::tagTypedDocumentCreation[]
			var phonePointer = Phones._pointer();
			Phones phone1 = Phones._empty()
					._with(phonePointer.phoneNumber(), "089 123456")
					._with(phonePointer.type(), Type.WORK);
			Phones phone2 = Phones._empty()
					._with(phonePointer.phoneNumber(), "0176 654321")
					._with(phonePointer.type(), Type.MOBILE);

			var contactPointer = DomainContact._pointer().contact();
			var personalDataPtr = contactPointer.personalData();
			DomainContact domainContact = DomainContact._empty()
				._with(personalDataPtr.firstName(), "Max")
				._with(personalDataPtr.lastName(), "Mustermann")
				._with(contactPointer.phones(1), phone2)
				._withInsertedRepetition(contactPointer.phones(1), phone1);

			// optional:
			DocumentV2 untypedDocument = domainContact._unwrap();
			// end::tagTypedDocumentCreation[]

			Assertions.assertEquals(
					"0176 654321",
					untypedDocument.fieldValue("Contact/Phones[2]/PhoneNumber")
			);

			// try out _with with all-repetitions pointer
			// tag::tagTypedDocumentWithAllRepetitions[]
			var allPhonesPointer = contactPointer.phones();
			DomainContact domainContact2 = DomainContact._empty()
					._with(personalDataPtr.firstName(), "Max")
					._with(personalDataPtr.lastName(), "Mustermann")
					._with(allPhonesPointer, List.of(phone1, phone2));
			// end::tagTypedDocumentWithAllRepetitions[]

			// tag::tagTypedDocumentAppendRepetition[]
			DomainContact domainContact3 = DomainContact._empty()
					._with(personalDataPtr.firstName(), "Max")
					._with(personalDataPtr.lastName(), "Mustermann")
					._withAppendedRepetition(allPhonesPointer, phone1)
					._withAppendedRepetition(allPhonesPointer, phone2);
			Assertions.assertEquals(domainContact2, domainContact3);
			// end::tagTypedDocumentAppendRepetition[]

			Assertions.assertEquals(domainContact._unwrap(), domainContact2._unwrap());
			TypedPointer<DomainContact, List<Phones>> allPhonesPointerSimpleType = allPhonesPointer;
			// tag::tagTypedAllRepetitionsPointerStrongType[]
			TypedRepetitionsPointer<DomainContact, Phones, PPhones<DomainContact>> allPhonesPointerSpecificType = allPhonesPointer;
			var firstPhonePointer = allPhonesPointerSpecificType.get(1);
			// end::tagTypedAllRepetitionsPointerStrongType[]

			// tag::tagTypedDocumentPointerUnwrap[]
			DocumentPointer untypedFirstPhonesPointer = firstPhonePointer._unwrap();
			// end::tagTypedDocumentPointerUnwrap[]
			Assertions.assertEquals(DocumentPointer.of("Contact/Phones[1]"), untypedFirstPhonesPointer);
		}
	}

}
