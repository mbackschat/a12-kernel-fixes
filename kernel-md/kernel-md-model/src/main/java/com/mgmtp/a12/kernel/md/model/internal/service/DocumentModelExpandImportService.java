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

package com.mgmtp.a12.kernel.md.model.internal.service;

import static com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandSanityChecks.checkImportedDocModelForCompatibility;
import static com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandSanityChecks.checkIncludeCycle;
import static com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandSanityChecks.checkResolvedDocumentModel;
import static com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandSanityChecks.checkTypeDefPurpose;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.LocalizedTextMapBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.expansion.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.internal.service.expansion.TypeDefCache;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.ModelReference;

/**
 * This class handles the recursive import of type definition document models.
 */
public class DocumentModelExpandImportService {

	private final DocumentModelReferenceResolver modelResolver;
	private final List<Locale> supportedLocales;
	private final IProblemReporter problemReporter;

	public DocumentModelExpandImportService(DocumentModelReferenceResolver resolver, List<Locale> supportedLocales,
			IProblemReporter problemReporter) {
		this.modelResolver = resolver;
		this.supportedLocales = supportedLocales;
		this.problemReporter = problemReporter;
	}

	public List<FieldTypeDefinition> extractImportedTypeDefinitions(ModelReference modelRef) {
		return extractImportedTypeDefinitions(modelRef, null);
	}

	List<FieldTypeDefinition> extractImportedTypeDefinitions(ModelReference modelRef, ExpansionInfo expansionInfo) {
		// The case should never occur, but just to be sure (process should continue if an error is found)
		checkTypeDefPurpose(modelRef, problemReporter);

		String modelRefString = modelRef.getReference();
		if (!checkIncludeCycle(modelRefString, expansionInfo, problemReporter)) {
			// break cycle since we have already reported the problem
			return List.of();
		}

		ExpansionInfo newExpansionInfo = new ExpansionInfo(modelRef, expansionInfo);
		DocumentModel importedDocumentModel = modelResolver.getDocumentModel(modelRefString);
		if (!checkResolvedDocumentModel(importedDocumentModel, modelRefString, problemReporter)) {
			// No document model, no expansion
			return List.of();
		}
		checkImportedDocModelForCompatibility(importedDocumentModel, modelRefString, supportedLocales, problemReporter);

		ImportTypeDefsVisitor visitor = new ImportTypeDefsVisitor(newExpansionInfo);
		new DocumentModelWalker().acceptDocumentModel(importedDocumentModel, visitor);

		return visitor.getImportedTypeDefsById();
	}

	private class ImportTypeDefsVisitor extends DocumentModelVisitor {

		private final ExpansionInfo expansionInfo;
		private final TypeDefCache typeDefCache = new TypeDefCache();

		public ImportTypeDefsVisitor(final ExpansionInfo expansionInfo) {
			this.expansionInfo = expansionInfo;
		}

		public List<FieldTypeDefinition> getImportedTypeDefsById() {
			return typeDefCache.getOrderedTypeDefs();
		}

		@Override
		public VisitProcess visitHeader(final Header header) {
			// recursively collect imported type definitions
			CollectionUtils.emptyIfNull(header.getModelReferences()).forEach(mr -> {
				List<FieldTypeDefinition> importedTypeDefs = extractImportedTypeDefinitions(mr, this.expansionInfo);
				typeDefCache.addTypeDefs(importedTypeDefs);
			});
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitFieldTypeDefinition(final FieldTypeDefinition fieldTypeDefinition) {
			FieldTypeDefinition td = cloneAndStripUnsupportedLocalizedTexts(fieldTypeDefinition);
			td.addModelReferencePath(expansionInfo.getModelReferencePath());
			// add local typeDefs to the cache
			typeDefCache.addUniqueTypeDef(td); // Note: there is a separate typeDefCache for each level of the import/include
			// expansion -> each TD should be visited only once within the scope of the cache -> we call addUniqueTypeDef instead
			// of addTypeDef
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private FieldTypeDefinition cloneAndStripUnsupportedLocalizedTexts(FieldTypeDefinition ftOrig) {
			return ftOrig.toBuilder()
					.id(ftOrig.getId())
					.fieldType(updateFieldType(ftOrig.getFieldType()))
					.internalDescription(extractSupportedLocalizedTextMap(ftOrig.getInternalDescription()))
					.externalDescription(extractSupportedLocalizedTextMap(ftOrig.getExternalDescription()))
					.build();
		}

		private FieldType updateFieldType(FieldType fieldType) {
			return switch (fieldType) {
				case StringType stOrig -> stOrig.toBuilder()
						.errorMessage(extractSupportedLocalizedTextMap(stOrig.getErrorMessage()))
						.build();
				case EnumerationType etOrig -> etOrig.toBuilder()
						.errorMessage(extractSupportedLocalizedTextMap(etOrig.getErrorMessage()))
						.values(etOrig.getValues().stream()
								.map(ev -> ev.toBuilder()
										.label(extractSupportedLocalizedTextMap(ev.getLabel()))
										.build())
								.toList())
						.build();
				default -> fieldType;
			};
		}

		private ILocalizedTextMap extractSupportedLocalizedTextMap(ILocalizedTextMap original) {
			if (original.isEmpty()) {
				return original;
			}
			LocalizedTextMapBuilder builder = new LocalizedTextMapBuilder();
			for (Entry<Locale, String> entry : original.entrySet()) {
				if (!supportedLocales.contains(entry.getKey())) {
					continue;
				}
				builder.add(entry.getKey(), entry.getValue());
			}
			return builder.build();
		}

	}

}
