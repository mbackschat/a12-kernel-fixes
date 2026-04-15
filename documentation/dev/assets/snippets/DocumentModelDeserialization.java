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

package com.mgmtp.a12.kernel.md.model.api.docs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.serializer.MDSerializerFactory;

// @formatter:off // disable auto formatter, because it could break code snippets that are included in the documentation
class DocumentModelDeserialization {

	@Test
	void documentModelDeserializerTest() throws IOException {
		Reader reader = new InputStreamReader(
				Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("document-models/small-example-for-deserialization/MyDm.json")),
				StandardCharsets.UTF_8);

		// tag::documentModelDeserialization[]
		// Your own implementation of Reader
		// E.g. reader=new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(documentModelPath), StandardCharsets.UTF_8)

		DocumentModelServiceFactory factory = new DocumentModelServiceFactory();
		// as an alternative you can use the following line
		{
			MDSerializerFactory mdFactory = new MDSerializerFactory();
		}
		IDocumentModelSerializer serializer = factory.createDocumentModelSerializer();
		// reader is a java.io.Reader
		IDocumentModel documentModel = serializer.deserialize(reader);
		// end::documentModelDeserialization[]

		Assertions.assertEquals("MyDm", documentModel.getHeader().getId());
	}
}
