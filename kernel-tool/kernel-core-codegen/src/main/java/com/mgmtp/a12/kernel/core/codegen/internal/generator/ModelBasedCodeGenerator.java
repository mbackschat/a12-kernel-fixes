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

package com.mgmtp.a12.kernel.core.codegen.internal.generator;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.SourceCodeCollectorV2;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * The base class for code generation. It divides the abstract task of code generation into steps.
 */
public abstract class ModelBasedCodeGenerator extends BaseCodeGenerator<CodeGenData, PackageInformation> implements ICodeGenerator {
	public ModelBasedCodeGenerator(CodeGenData data, IProblemReporter pr, SourceCodeCollectorV2 collector) {
		super(data, pr, collector);
	}

	@Override void nonStandardPluginMethod() {
		createRuntimeVersionProperties();
	}

	private void createRuntimeVersionProperties() {
		if (ExtmpUtils.isExtmp(data.getModelSpecificSettings()) &&
				ProgrammingLanguage.JAVA.equals(data.getParams().getProgrammingLanguage())) {
			createEntry("runtime-version.properties",
					String.format(
							"# Aus dem major und minor Teil wird die zum Generat passende Version der kernel-core-runtime ermittelt.\nruntime.version=%s",
							GeneratorConstants.GENCODE_JAVA_GROOVY_RUNTIME_VERSION));
		}
	}

	@Override protected PackageInformation createPackageInformation() {
		return new PackageInformation(relativePath, data);
	}

	protected boolean areLegacyPublicExtmpAccessorsNeeded(boolean isPublicAccessorMayBeNeeded) {
		return ExtmpUtils.useExtmpTemplates(data.getModelSpecificSettings()) // only for ExtMP
				&& data.getParams().getNondefaultPackagePrefix()
				.isEmpty() // only if no custom prefix has bene specified (otherwise there is no need for the legacy entry point)
				&& isPublicAccessorMayBeNeeded;
	}
}
