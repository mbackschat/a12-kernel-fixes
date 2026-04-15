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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

record SeparateFilesCodeGenResultImpl<E extends IEntryPoints>(
		E entryPoints,
		Map<String, byte[]> sources
) implements ISeparateFilesCodeGenResult<E> {
	@Override public <E2 extends IEntryPoints> SeparateFilesCodeGenResultImpl<E2> withEntryPoints(E2 newEntryPoints) {
		return new SeparateFilesCodeGenResultImpl<>(newEntryPoints, this.sources);
	}

	@Override public ISeparateFilesCodeGenResult<E> mergeWith(List<ICodeGenResult<?>> otherInstances) {
		List<ISeparateFilesCodeGenResult<?>> allAsSeparateFileResults =
				Stream.concat(Stream.of(this),
						otherInstances.stream().map(cgr -> {
							if (cgr instanceof ISeparateFilesCodeGenResult<?> filesCgr) {
								return filesCgr;
							} else {
								throw new IllegalArgumentException(
										"merge of ISeparateFilesCodeGenResult codegenresult with non-ISeparateFilesCodeGenResult " +
												"results is not supported, got : " + cgr);
							}
						})).toList();

		Map<String, byte[]> combinedSources = new HashMap<>();
		for (var part : allAsSeparateFileResults) {
			for (var entry : part.sources().entrySet()) {
				if (combinedSources.containsKey(entry.getKey())) {
					if (!Arrays.equals(combinedSources.get(entry.getKey()), entry.getValue())) {
						throw new RuntimeException("Clashing source entries for: " + entry.getKey());
					}

					// "merge" files with same content and name
					continue;
				}
				combinedSources.put(entry.getKey(), entry.getValue());
			}
		}
		return withSources(combinedSources);
	}

	private SeparateFilesCodeGenResultImpl<E> withSources(Map<String, byte[]> newSources) {
		return new SeparateFilesCodeGenResultImpl<>(this.entryPoints, newSources);
	}
}
