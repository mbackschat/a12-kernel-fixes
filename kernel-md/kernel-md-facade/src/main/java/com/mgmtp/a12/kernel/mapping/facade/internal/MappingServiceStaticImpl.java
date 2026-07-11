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

package com.mgmtp.a12.kernel.mapping.facade.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.IDataTransferService;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.mapping.facade.StaticMappingServiceConfig;
import com.mgmtp.a12.kernel.mapping.rt.a12internal.IMappingServiceA12Internal;
import com.mgmtp.a12.kernel.mapping.rt.api.MappingResult;
import com.mgmtp.a12.kernel.mapping.rt.api.SourceDocument;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.rt.a12internal.service.datatransfer.DataTransferServiceAdapter;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.DocumentValidationException;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.rt.internal.service.DataTransferConfigUtil;
import com.mgmtp.a12.kernel.md.rt.internal.service.DocumentAbstractRtService;

import lombok.NonNull;

public class MappingServiceStaticImpl implements IMappingServiceA12Internal {
	@NonNull private final IDataTransferService transferService;
	@NonNull private final IDocumentRtService documentRtService;

	public MappingServiceStaticImpl(@NonNull StaticMappingServiceConfig config) {
		this(deriveDataTransferService(config.packagePrefix()), config.documentRtService());
	}

	public MappingServiceStaticImpl(@NonNull IDataTransferService transferService, @NonNull IDocumentRtService documentRtService) {
		this.transferService = transferService;
		this.documentRtService = documentRtService;
	}

	private static IDataTransferService deriveDataTransferService(String packagePrefix) {
		String className = String.format("%s.DataTransferFactory",
				packagePrefix != null ? packagePrefix : ICodeGeneratorParameters.DEFAULT_PACKAGE_PATH);
		Class<?> accessorClass;
		try {
			accessorClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new DocumentValidationException(
					"Generated mapping code accessor class '" + className + "' not found in classpath.", e);
		}
		try {
			return (IDataTransferService) accessorClass.getMethod("getInstance").invoke(null);
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			throw new DocumentValidationException("Error creating model instance.", e);
		}
	}

	// currently only via ci-tested code snippets, not via normal unit tests!
	@Override
	public @NonNull MappingResult transferData(
			@NonNull List<SourceDocument> sourceDocuments,
			@NonNull DocumentV2 targetDocument,
			@NonNull DocumentProcessingConfig config) {
		IMetaModel targetMetaModel = ((DocumentAbstractRtService) documentRtService).getMetaModel(
				targetDocument); // cast is safe as IDocumentRtService is OnlyForUsage and all implementations implement 'DocumentAbstractRtService'
		return DataTransferServiceAdapter.wrap(transferService, targetMetaModel)
				.transferData(sourceDocuments, targetDocument, DataTransferConfigUtil.fromDocumentProcessingConfig(config));
	}

	@Override public @NonNull IMappingServiceA12Internal.ISortedSourceDocumentResult sortSourceDocument(
			@NonNull SourceDocument sourceDocument,
			@NonNull DocumentProcessingConfig config) {
		var metaModel = transferService._internal_getMetaObjectForSource(sourceDocument.sourceName());
		return DataTransferServiceAdapter.sortData(sourceDocument, metaModel, config);
	}

}
