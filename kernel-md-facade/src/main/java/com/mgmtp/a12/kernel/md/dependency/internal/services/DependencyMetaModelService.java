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

package com.mgmtp.a12.kernel.md.dependency.internal.services;

import java.util.Locale;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.internal.service.Identifier;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAwareDocServiceImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.MMDocumentModelLoader;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views.MM_DependencyModel_1;
import com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen.mm_dependencymodel__0031.Factory_mm_dependencymodel__0031;

import lombok.NonNull;

public class DependencyMetaModelService {
	private static final String META_MODEL_CODEGEN_DM_NAME = "extmpinternal/codegen-mm_dependencymodel-1.json";

	public static IDocumentValidationResult validate(@NonNull MM_DependencyModel_1 document, @NonNull Locale locale) {
		IDocumentModelResolver modelResolver = MMDocumentModelLoader.createResolver(META_MODEL_CODEGEN_DM_NAME);
		DocumentV2 codegenDm = DmAwareDocServiceImpl.ofResolver(META_MODEL_CODEGEN_DM_NAME, modelResolver)
				.removeUnknowns(document._unwrap());

		return LowKernelApiUtils.callValidator(
				LowKernelApiUtils.ValidationMethod.FULL_VALIDATION,
				Factory_mm_dependencymodel__0031.getInstance().getValidator(),
				Factory_mm_dependencymodel__0031.getInstance().getMetaObject(),
				codegenDm,
				null,
				DocumentProcessingConfig.builder(locale).build(),
				id -> new Identifier(id.getName(), id.getIndices()).toString());
	}

	private DependencyMetaModelService() {
		throw new UnsupportedOperationException("Should not be instantiated");
	}
}
