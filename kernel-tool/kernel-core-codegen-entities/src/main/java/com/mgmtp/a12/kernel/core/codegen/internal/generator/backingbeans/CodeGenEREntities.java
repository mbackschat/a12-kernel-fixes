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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;

import lombok.NonNull;

public class CodeGenEREntities<D extends CodeGenEntityData> extends CodeGenEntities<D, PackageInformation> {
	protected final String cppRuntimeNamespace;
	protected final String mappedPackageName;
	protected final String mappedName;

	public CodeGenEREntities(CodeGenEREntitiesBuilder<D> builder) {
		super(builder);
		this.cppRuntimeNamespace = builder.getCppRuntimeNamespace();
		this.mappedPackageName = builder.getMappedPackegeName();
		this.mappedName = builder.getMappedName();
	}

	public @NonNull String getLegacyPublicExtmpPackageName() {
		return packageInformation.getLegacyPublicExtmpPackageName();
	}

	public @NonNull String getInternalXmlReaderPackageName() {
		return packageInformation.getInternalXmlReaderPackageName();
	}

	public @NonNull String getInternalXmlWriterPackageName() {
		return packageInformation.getInternalXmlWriterPackageName();
	}

	public @NonNull String getInternalSXmlPackageName() {
		return packageInformation.getInternalSXmlPackageName();
	}

	public @NonNull String getInternalUXmlMapperPackageName() {
		return packageInformation.getInternalUXmlMapperPackageName();
	}

	public @NonNull String getPublicUXmlMapperPackageName() {
		return packageInformation.getPublicUXmlMapperPackageName();
	}

	public String getMappedPackageName() {
		return mappedPackageName;
	}

	public String getMappedName() {
		return mappedName;
	}

	// ---------------------- C++ specific -------------------------
	public String getCppRuntimeNamespace() {
		return cppRuntimeNamespace;
	}

	public boolean isMapperOnly() {
		return ExtmpUtils.isMapper(data.getParams());
	}

	public boolean isExtmp() {
		return ExtmpUtils.useExtmpTemplates(data.getModelSpecificSettings());
	}
}
