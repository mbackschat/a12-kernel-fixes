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

package com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record MergedSingleScriptCodeGenResultImpl<E extends IEntryPoints>(
		E entryPoints,
		String fileHeader,
		Set<String> importsLines,
		String globals,
		List<String> contentParts,
		List<String> filePathForScript
) implements IMergedSingleScriptCodeGenResult<E> {

	@Override public <E2 extends IEntryPoints> MergedSingleScriptCodeGenResultImpl<E2> withEntryPoints(E2 newEntryPoints) {
		return new MergedSingleScriptCodeGenResultImpl<>(newEntryPoints, fileHeader, importsLines, globals, contentParts,
				filePathForScript);
	}

	@Override public IMergedSingleScriptCodeGenResult<E> mergeWith(List<ICodeGenResult<?>> otherInstances) {
		List<IMergedSingleScriptCodeGenResult<?>> allAsSingleFileResults = Stream.concat(
				Stream.of(this),
				otherInstances.stream().map(cgr -> {
					if (cgr instanceof IMergedSingleScriptCodeGenResult<?> otherGroovyResult) {
						return otherGroovyResult;
					} else {
						throw new IllegalArgumentException(
								"merge of IMergedSingleScriptCodeGenResult codegenresult with non-IMergedSingleScriptCodeGenResult" +
										" codegen result is not supported, got " + cgr);
					}
				})
		).toList();

		Set<String> mergedImports = allAsSingleFileResults.stream()
				.flatMap(cgr -> cgr.importsLines().stream())
				.collect(Collectors.toSet());
		List<String> concatenatedContentParts = allAsSingleFileResults.stream()
				.flatMap(cgr -> cgr.contentParts().stream())
				.toList();
		return new MergedSingleScriptCodeGenResultImpl<>(
				this.entryPoints(),
				this.fileHeader,
				mergedImports,
				this.globals,
				concatenatedContentParts,
				this.filePathForScript
		);
	}
}
