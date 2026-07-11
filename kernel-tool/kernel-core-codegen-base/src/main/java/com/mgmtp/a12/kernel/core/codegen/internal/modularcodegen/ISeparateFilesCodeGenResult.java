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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface ISeparateFilesCodeGenResult<E extends IEntryPoints> extends ICodeGenResult<E> {
	Map<String, byte[]> sources(); // behavior in mergeWith: union of entries, reject key collisions

	static <E extends IEntryPoints> ISeparateFilesCodeGenResult<E> of(E entryPoints, Map<String, byte[]> sources) {
		return new SeparateFilesCodeGenResultImpl<>(entryPoints, sources);
	}

	static <E extends IEntryPoints> ISeparateFilesCodeGenResult<E> ofSingleFileUtf8(
			List<String> entryPointFqn,
			Function<List<String>, E> mkEntryPoints,
			String fileExtension,
			String fileContent
	) {
		return of(mkEntryPoints.apply(entryPointFqn),
				Map.of(String.join("/", entryPointFqn) + fileExtension, fileContent.getBytes(StandardCharsets.UTF_8)));
	}

	@Override default byte[] toSingleFileBytesIfApplicableOrThrow() {
		if (sources().size() > 1) {
			throw new IllegalStateException(
					"toSingleFileBytesIfApplicableOrThrow is not supported for non-IMergedSingleScriptCodeGenResult codegenresults with more than one entry, got entries " + sources().keySet());
		}
		return sources().values().iterator().next();
	}
}
