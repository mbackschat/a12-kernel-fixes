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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import java.util.Objects;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.IBaseCodeGenData;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;

import lombok.NonNull;

/**
 * This class represents package information for code generation primarily in Java.
 * <p>
 * There are different class types in different packages when generating Java code:
 * <ul>
 * <li>the vk classes</li>
 * <li>the ExtMP special factory class</li>
 * <li>the first two in the context of data transfer</li>
 * <li>the XML-reader classes</li>
 * <li>the XML-writer classes</li>
 * <li>the XML-schema classes</li>
 * <li>the mapper classes (map information)</li>
 * <li>the XML-converter classes</li>
 * </ul>
 */
public class PackageInformation extends BasePackageInformation {
	private final @NonNull String publicPackageName;
	private final String legacyPublicExtmpValidatorPackageName;
	private final String publicUXmlMapperPackageName;
	private final String publicUXmlMapperClassNamePrefix;
	private final String internalXmlReaderPackageName;
	private final String internalXmlWriterPackageName;
	private final String internalSXmlPackageName;
	private final String internalUXmlMapperPackageName;

	public PackageInformation(String relativePath, IBaseCodeGenData codeGenData) {
		super(relativePath, codeGenData);
		String packagePrefix = BaseCodeGenUtils.getNormalizedPackageNameOrDefault(codeGenData.getParams());
		this.publicPackageName = buildJavaPackageName(packagePrefix, "");

		if (codeGenData.getParams() instanceof IExtmpCodeGenParams extmpParams) {
			this.legacyPublicExtmpValidatorPackageName = buildJavaPackageName(
					extmpParams.getExtmpPackageInformation().legacyPublicPackageName(), "");
			this.publicUXmlMapperPackageName = buildJavaPackageName(
					extmpParams.getExtmpPackageInformation().publicUXmlMapperPackageName(), "");
			this.publicUXmlMapperClassNamePrefix =
					extmpParams.getExtmpPackageInformation().publicUXmlMapperClassNamePrefix();
			this.internalXmlReaderPackageName = buildJavaPackageName(
					packagePrefix, extmpParams.getExtmpPackageInformation().internalXmlReaderSubPackageName());
			this.internalXmlWriterPackageName = buildJavaPackageName(
					packagePrefix, extmpParams.getExtmpPackageInformation().internalXmlWriterSubPackageName());
			this.internalSXmlPackageName = buildJavaPackageName(
					packagePrefix, extmpParams.getExtmpPackageInformation().internalSXmlSubPackageName());
			this.internalUXmlMapperPackageName = buildJavaPackageName(
					packagePrefix, extmpParams.getExtmpPackageInformation().internalUXmlMapperSubPackageName());
		} else {
			this.legacyPublicExtmpValidatorPackageName = null;
			this.publicUXmlMapperPackageName = null;
			this.publicUXmlMapperClassNamePrefix = null;
			this.internalXmlReaderPackageName = null;
			this.internalXmlWriterPackageName = null;
			this.internalSXmlPackageName = null;
			this.internalUXmlMapperPackageName = null;
		}
	}

	public @NonNull String getPublicPackageName() {
		return publicPackageName;
	}

	public @NonNull String getLegacyPublicExtmpPackageName() {
		return Objects.requireNonNull(legacyPublicExtmpValidatorPackageName);
	}

	public @NonNull String getPublicUXmlMapperPackageName() {
		return Objects.requireNonNull(publicUXmlMapperPackageName);
	}

	public @NonNull String getPublicUXmlMapperClassNamePrefix() {
		return Objects.requireNonNull(publicUXmlMapperClassNamePrefix);
	}

	public @NonNull String getInternalXmlReaderPackageName() {
		return Objects.requireNonNull(internalXmlReaderPackageName);
	}

	public @NonNull String getInternalXmlWriterPackageName() {
		return Objects.requireNonNull(internalXmlWriterPackageName);
	}

	public @NonNull String getInternalSXmlPackageName() {
		return Objects.requireNonNull(internalSXmlPackageName);
	}

	public @NonNull String getInternalUXmlMapperPackageName() {
		return Objects.requireNonNull(internalUXmlMapperPackageName);
	}

	@Override protected @NonNull String buildJavaPackageName(String basePackagePrefix, String basePackageSuffix) {
		String javaPackageName;
		if (ExtmpUtils.isMapped(data.getParams())) {
			String mappedPackageName = ExtmpUtils.getAssignedPackageName(data.getParams());

			javaPackageName = basePackagePrefix + "." + relativePath.replace('/',
					'.') + basePackageSuffix + "." + mappedPackageName;
		} else {
			javaPackageName = super.buildJavaPackageName(basePackagePrefix, basePackageSuffix);
		}

		// replace '..' in case the given package name ends with '.'
		return javaPackageName.replace("..", ".");
	}
}
