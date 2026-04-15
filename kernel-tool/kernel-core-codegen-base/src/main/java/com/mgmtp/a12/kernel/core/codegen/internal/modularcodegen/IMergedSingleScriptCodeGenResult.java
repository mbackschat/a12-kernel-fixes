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
import java.util.Set;

public interface IMergedSingleScriptCodeGenResult<E extends IEntryPoints> extends ICodeGenResult<E> {
	String fileHeader(); // behavior in mergeWith: not merged, top-level "wins"

	Set<String> importsLines(); // behavior in mergeWith: set union

	String globals(); // behavior in mergeWith: not merged, top-level "wins"

	List<String> contentParts(); // behavior in mergeWith: concatenate (top-level first, currently)

	List<String> filePathForScript(); // behavior in mergeWith: not merged, top-level "wins"

	static <E extends IEntryPoints> IMergedSingleScriptCodeGenResult<E> of(E entryPoints, String fileHeader,
			Set<String> importsLines, String globals,
			List<String> contentParts, List<String> filePathForScript) {
		return new MergedSingleScriptCodeGenResultImpl<>(entryPoints, fileHeader, importsLines, globals, contentParts,
				filePathForScript);
	}

	@Override default byte[] toZip() {
		return CodeGenResultUtils.sourcesToZip(
				Map.of(String.join("/", filePathForScript()), toSingleFileBytesIfApplicableOrThrow()));
	}

	/**
	 * see {@link ICodeGenResult#toSingleFileBytesIfApplicableOrThrow()}, but for instances of
	 * {@link IMergedSingleScriptCodeGenResult}
	 * this method is always applicable, i.e. the case where an IllegalStateException is thrown due to multiple entries cannot
	 * happen.
	 */
	@Override default byte[] toSingleFileBytesIfApplicableOrThrow() {
		return toSourceString().getBytes(StandardCharsets.UTF_8);
	}

	private String toSourceString() {
		var sb = new StringBuilder();
		sb.append(fileHeader()).append("\n");
		importsLines().stream().sorted().forEach(e -> sb.append(e).append("\n"));
		sb.append(globals()).append("\n");
		contentParts().forEach(e -> sb.append(e).append("\n"));
		return sb.toString();
	}
}
