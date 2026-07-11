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
package com.mgmtp.a12.kernel.core.codegen.internal;

import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.mapList;

import java.util.Collection;

import com.mgmtp.a12.kernel.core.calc.internal.util.CalculationUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGeneratorCollection;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.ICodeGenerator;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.PacketCodeGenerator;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.EntryPointCollection;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ICodeGenResult;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.SourceCodeCollectorV2;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.CodeGeneratorParametersWrapper;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;

public class CodeGeneratorService {

	/**
	 * Entity Container providing all information about the fields and the groups.
	 */
	private final IEC ec;
	private final IModelSpecificSettings modelSpecificSettings;
	/**
	 * project specific settings.
	 */
	private final IProjectSpecificSettings pss;

	public CodeGeneratorService(IServiceConfiguration serviceConfiguration) {
		this.ec = serviceConfiguration.getIEC();
		this.pss = serviceConfiguration.getProjectSpecificSettings();
		this.modelSpecificSettings = serviceConfiguration.getModelSpecificSettings();
	}

	/**
	 * Returns the source code as zip or null in case of an error. All problems are reported to the problem reporter:
	 * - rules are not valid
	 * - error messages of the rules are not valid
	 * - a rule or a field exists which does not provide an error text for a [Locale] specified by
	 * [ICodeGeneratorParameters#getLanguages()]
	 * - programming language specified by [ICodeGeneratorParameters#getProgrammingLanguage()] is not supported
	 * - required settings for the specified programming language are not provided or are invalid
	 * - decimal separator specified by [ICodeGeneratorParameters#getDecimalSeparator()] is not valid
	 * - the name [ICodeGeneratorParameters#getName()] is not valid
	 * <p>
	 * The resulting source code will be encoded depending on [ICodeGeneratorParameters#getProgrammingLanguage()]:
	 * - UTF-8 for [ProgrammingLanguage#JAVA], [ProgrammingLanguage#JAVA_SCRIPT_VK], [ProgrammingLanguage#JAVA_SCRIPT_REDUCED],
	 * [ProgrammingLanguage#GROOVY], [ProgrammingLanguage#CPLUS]
	 * - CP1252 for [ProgrammingLanguage#VBA]
	 *
	 * @param rules
	 * 		specifies the rules of the model, may not be null
	 * @param params
	 * 		specifies the code generation params, may not be null
	 * @param pm
	 * 		problem reporter, may not be null
	 * @return zipped source code as a byte array. For [ProgrammingLanguage#JAVA], the zip is laid out as a standalone
	 * 		Gradle Java project (see [CodeGenResultUtils#applyGradleJavaLayout]). For other target languages the zip uses
	 * 		a flat, package-relative layout.
	 */
	public byte[] getSourceCodeAsZip(Collection<? extends IRule> rules, ICodeGeneratorParameters params, IProblemReporter pm) {
		ICodeGenResult<?> genResult = getSourceCodeAsCodeGenResult(rules, params, pm);
		if (genResult != null) {
			return CodeGenResultUtils.toBytes(genResult, params.isPlain(), params.getProgrammingLanguage());
		}
		return null;
	}

	public ICodeGenResult<EntryPointCollection> getSourceCodeAsCodeGenResult(
			Collection<? extends IRule> rules,
			ICodeGeneratorParameters params,
			IProblemReporter pm
	) {
		if (ExtmpUtils.isHModel(modelSpecificSettings) &&
				!params.getProgrammingLanguage().isWithUsb()) {
			throw new IllegalArgumentException(
					"Cannot generate code for an H-Model for programming language " + params.getProgrammingLanguage().toString());
		}
		params = CodeGeneratorParametersWrapper.wrap(
				params,
				mapList(params.getCalculationInformation(),
						ci -> CalculationUtils.addImplicitGroupFilledToCommonPreconditionIfApplicable(ci, ec)));
		SourceCodeCollectorV2 collector = new SourceCodeCollectorV2();
		CodeGenData modelComponents = new CodeGenData(ec, modelSpecificSettings, params, rules, pss);
		ICodeGenerator codeGen = createCodeGenerator(modelComponents, pm, collector);
		if (codeGen.generateCode()) {
			return collector.toCodeGenResult(params.getProgrammingLanguage());
		} else {
			return null;
		}
	}

	// note for review: this code was formerly in CodeGenProvider
	private static ICodeGenerator createCodeGenerator(CodeGenData data, IProblemReporter pm, SourceCodeCollectorV2 collector) {
		if (data.getParams() instanceof IExtmpCodeGenParams extmpParams) {
			IExtmpCodeGenParams.IMappedData mappedData = extmpParams.getMappedModelParameter();
			if (mappedData != null) {
				return new PacketCodeGenerator(data, pm, collector);
			}
		}
		return new CodeGeneratorCollection(data, pm, collector, true);
	}
}
