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

package com.mgmtp.a12.kernel.md.model.api.services;

import java.util.Optional;

import lombok.NonNull;

/**
 * Configuration of the generator of validation code.
 * <p>
 * <b>Note:</b> The caller has to provide an implementation of this interface.
 */
// similar but different: ICodeGeneratorParameters
// similar but different: ICodeControlParameter
// (this class: IValidationCodeGeneratorConfig)
public interface IValidationCodeGeneratorConfig {

	/**
	 * Supported programming languages in which validation code can be generated.
	 */
	// NOTE: there's a similar enum in similar: com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage
	enum ProgrammingLanguage {
		JAVA,
		JAVASCRIPT,
		GROOVY
	}

	/**
	 * Configuration for the javascript code generation.
	 */
	enum JsCodeGenConfig {
		STANDARD,
		/**
		 * The generated javascript code will be wrapped in a function.
		 */
		WRAPPED_IN_FUNCTION,
		/**
		 * In addition to {@link #WRAPPED_IN_FUNCTION}, a file with the same name and extension
		 * {@code .d.ts} will be generated with the needed typescript definition code.
		 */
		WRAPPED_IN_FUNCTION_EXTENDED
	}

	/**
	 * @return the {@link ProgrammingLanguage} in which the validation code is to be generated. May
	 * 		not be {@code null}.
	 */
	@NonNull ProgrammingLanguage getProgrammingLanguage();

	/**
	 * For generated java code, the packageName will be used as the package name of the
	 * generated factory class that serves as the entry point to the generated code.
	 * When empty (or null in cases where the parameter is represented as a nullable String rather than {@code Optional<String>}), the default value {@code com.mgmtp.a12.kernel.generated.}
	 * (from {@code ICodeGeneratorParameters.DEFAULT_PACKAGE_PATH}) will be used,
	 * but files other than the factory might be placed below different subdirectories,
	 * for historic reasons.
	 * When non-empty, <em>all</em> generated source files (for java codegen) will be placed below
	 * the corresponding subdirectory in the generated source zip; except for the file
	 * runtime-version.properties, which is always put in the root of the zip.
	 * <p>
	 * Code generation for other target languages may or should ideally use this parameter
	 * in an analogous way as far as this is meaningful and appropriate for the target language
	 * (to avoid name and file-path clashes between generated code).
	 * But this is not necessarily currently the case.
	 */
	default Optional<String> getPackageName() {
		return Optional.empty();
	}

	/**
	 * This configuration will affect the code generation only if {@link #getProgrammingLanguage()}
	 * returns {@link ProgrammingLanguage#JAVASCRIPT}.
	 *
	 * @return the way how the javascript code should be generated. May not be {@code null}.
	 */
	default @NonNull JsCodeGenConfig getJsCodeGenConfig() {
		return JsCodeGenConfig.STANDARD;
	}

	/**
	 * This configuration will affect the code generation only if {@link #getProgrammingLanguage()}
	 * returns {@link ProgrammingLanguage#JAVASCRIPT} or {@link ProgrammingLanguage#GROOVY}.
	 *
	 * @return {@code true} if plain source code should be returned by {@link IDocumentModelService#generateValidationCode}. If
	 *        {@code false} (default), zipped source code will be returned.
	 */
	default boolean isPlain() {
		return false;
	}

}
