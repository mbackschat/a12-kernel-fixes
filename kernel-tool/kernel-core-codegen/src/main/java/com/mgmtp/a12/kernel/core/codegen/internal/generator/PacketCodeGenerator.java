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

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.SourceCodeCollectorV2;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams.IMappedData;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.Product;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * The code generator for the mapped model. The mapped model will be generated
 * as it were a source model, with the difference that the XML-writer will be also generated.
 */
public class PacketCodeGenerator extends ModelBasedCodeGenerator {

	private final ModelBasedCodeGenerator mainModelCodeGenerator;
	// for the mapped model we have the validation code generator and XML-writer code generator
	private final List<ModelBasedCodeGenerator> mappedCodeGenerators;
	private final IExtmpCodeGenParams params;

	public PacketCodeGenerator(CodeGenData data, IProblemReporter pm, SourceCodeCollectorV2 collector) {
		super(data, pm, collector);

		params = (IExtmpCodeGenParams) data.getParams();

		mainModelCodeGenerator = new CodeGeneratorCollection(data, pm, collector, true);

		String destPath = data.getParams().getName();

		mappedCodeGenerators = buildCodeGenerators4MappedModel(params, destPath, data.isReReDv());
	}

	@Override
	protected String deriveRelativePath() {
		return data.getParams().getName();
	}

	private List<ModelBasedCodeGenerator> buildCodeGenerators4MappedModel(IExtmpCodeGenParams params, String relativePath,
			boolean isMainModelReReDv) {
		List<ModelBasedCodeGenerator> codeGenerators = new ArrayList<>();
		IMappedData md = params.getMappedModelParameter();
		if (md != null) {
			CodeGenData data = new CodeGenData(md.getEc(), md.getEc().getModelSpecificSettings(), md.getCodeGenParams(),
					new ArrayList<>(),
					this.data.getPss(), isMainModelReReDv);
			ValidationCodeGenerator validation = new ValidationCodeGenerator(data, this.problemReporter, this.collector);
			validation.setRelativePath(relativePath);
			codeGenerators.add(validation);
			XMLWriterCodeGenerator xmlWriter = new XMLWriterCodeGenerator(data, this.problemReporter, this.collector);
			xmlWriter.setRelativePath(relativePath);
			codeGenerators.add(xmlWriter);
			XMLReaderCodeGenerator xmlReader = new XMLReaderCodeGenerator(data, this.problemReporter, this.collector);
			xmlReader.setRelativePath(relativePath);
			codeGenerators.add(xmlReader);
		}
		return codeGenerators;
	}

	@Override
	protected boolean preCheck() {
		boolean mainModelValid = mainModelCodeGenerator.preCheck();
		boolean mappedCodeGenValid = Java8Utils.allMatchEvaluateAll(
				mappedCodeGenerators.stream(),
				ModelBasedCodeGenerator::preCheck);
		return mainModelValid && mappedCodeGenValid;
	}

	@Override
	protected boolean createTemplateParameterClasses() {
		if (!mainModelCodeGenerator.createTemplateParameterClasses()) {
			return false;
		}
		if (!mappedCodeGenerators.stream().allMatch(ModelBasedCodeGenerator::createTemplateParameterClasses)) {
			return false;
		}

		return true;
	}

	@Override
	protected void createEntries() {
		createEntries4MappedModel();
		mainModelCodeGenerator.createEntries();

		CodeGenContext.initialize(Product.VALIDATION, data.getParams().getProgrammingLanguage());
	}

	private void createEntries4MappedModel() {
		switch (data.getParams().getProgrammingLanguage()) {
			case JAVA -> mappedCodeGenerators.forEach(ModelBasedCodeGenerator::createEntries);
			case CPLUS -> {
			}
			default -> throw new IllegalArgumentException("Code for mapper may only be generated for java");
		}
	}
}
