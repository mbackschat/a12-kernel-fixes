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

package com.mgmtp.a12.kernel.codeexamples;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.mgmtp.a12.kernel.internal.test.md.rt.api.impl.DocumentDynamicServiceConfig;
import com.mgmtp.a12.kernel.internal.test.md.rt.api.impl.ModelCodeCacheImpl;
import com.mgmtp.a12.kernel.internal.test.md.utils.DocumentModelResolverFromClasspath;
import com.mgmtp.a12.kernel.mapping.facade.MappingServiceFactory;
import com.mgmtp.a12.kernel.mapping.facade.StaticMappingServiceConfig;
import com.mgmtp.a12.kernel.mapping.rt.api.IMappingService;
import com.mgmtp.a12.kernel.mapping.rt.api.MappedField;
import com.mgmtp.a12.kernel.mapping.rt.api.MappingResult;
import com.mgmtp.a12.kernel.mapping.rt.api.SourceDocument;
import com.mgmtp.a12.kernel.mapping.rt.api.SourceField;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.facade.DocumentRtServiceFactory;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;

public class TestMdMappingServiceSnippetsForDocs {
	@Test void test() {
		var serviceFactory = new DocumentRtServiceFactory(
				DocumentModelResolverFromClasspath.of(List.of(
						dmId -> "sample1/document-models/" + dmId + ".json"
				)));

		IDocumentRtService documentRtService = serviceFactory.createDocumentRtService(
				new DocumentDynamicServiceConfig(new ModelCodeCacheImpl())
		);

		// @formatter:off // disable auto formatter, because it could break code snippets that are included in the documentation

		// tag::tagCreateStaticMappingService[]
		// Obtain a static IMappingService based on the mapping code that was previously generated
		// for the specified package name, compiled and put on the runtime classpath
		IMappingService mappingService = MappingServiceFactory.createMappingServiceFromStaticCode(
				StaticMappingServiceConfig.of("some.packageprefix.for_the.generated.mapping_code",
						documentRtService));
		// end::tagCreateStaticMappingService[]


		// tag::tagUseMappingService[]
		// Given some initial data for source documents and target document:
		DocumentV2 sourceDoc1 = DocumentV2.empty("example_source_DM_for_docs")
				.withFieldValue("/SourceRG/someFieldInSource", "valueFromSource1");
		DocumentV2 sourceDoc2 = DocumentV2.empty("example_source_DM_for_docs")
				.withFieldValue("/SourceRG/someFieldInSource", "valueFromSource2");
		DocumentV2 targetDoc = DocumentV2.empty("example_target_DM_for_docs");

		// 1) we assume we already have an IMappingService mappingService (static or dynamic)

		// 2) Wrap the source DocumentV2s as SourceDocument, with some additional info that is
		//    needed for the mapping:
		//    - sourceName (because a Mapping can specify multiple different sources, identified
		//      by their sourceName)
		//    - sourceInstanceId (an arbitrary unique id that is used to identify this source
		//      instance in metadata that is available in the MappingResult)
		List<SourceDocument> sourceInstances = List.of(
				SourceDocument.of("Source1", "idForSourceInstance1", sourceDoc1),
				SourceDocument.of("Source1", "idForSourceInstance2", sourceDoc2)
		);

		// 3) Perform the mapping and obtain the resulting updated target document
		MappingResult mappingResult = mappingService.transferData(sourceInstances, targetDoc,
				DocumentProcessingConfig.builder(Locale.GERMANY).build());
		DocumentV2 updatedTargetDoc = mappingResult.applyTo(targetDoc);
		// end::tagUseMappingService[]

		// tag::tagCheckMappingResult[]
		Assertions.assertEquals(
		    DocumentV2.empty("example_target_DM_for_docs")
		        .withFieldValue("/RGTarget[1]/someFieldInTarget", "valueFromSource1_precomputedSuffix")
		        .withFieldValue("/RGTarget[2]/someFieldInTarget", "valueFromSource2_precomputedSuffix"),
		    updatedTargetDoc
		);

		Assertions.assertEquals(
		    List.of(
		        MappedField.of(DocumentPointer.of("/RGTarget[1]/someFieldInTarget"), "valueFromSource1_precomputedSuffix",
		            Set.of(SourceField.of("idForSourceInstance1", DocumentPointer.of("SourceRG/someFieldInSource")))),
		        MappedField.of(DocumentPointer.of("/RGTarget[2]/someFieldInTarget"), "valueFromSource2_precomputedSuffix",
		            Set.of(SourceField.of("idForSourceInstance2", DocumentPointer.of("SourceRG/someFieldInSource"))))
		    ),
		    mappingResult.mappedFields()
		        .stream().sorted(Comparator.comparing(MappedField::targetField)).toList());

		Assertions.assertTrue(mappingResult.sourceEligibilityResults().get("idForSourceInstance1").sourceWasIncludedInMapping());
		Assertions.assertTrue(mappingResult.sourceEligibilityResults().get("idForSourceInstance2").sourceWasIncludedInMapping());
		
		Assertions.assertTrue(mappingResult.success());
		// end::tagCheckMappingResult[]
		// @formatter:on
	}
}
