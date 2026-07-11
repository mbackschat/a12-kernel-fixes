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

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

/**
 * Represents the results of code generation in a way that allows to combine results from nested codegen calls in a modular, collision-safe way. {@link #entryPoints()} provides info for referencing the (collision-safe, fully-qualified) entry points of the result in other gencode, {@link #mergeWith(List)} specifies how to merge the top-level codegen result with the nested results into an overall result.
 * </p>
 * Sub-interfaces {@link ISeparateFilesCodeGenResult} and {@link IMergedSingleScriptCodeGenResult} add specifics for two common scenarios that require different merge mechanisms.
 * </p>
 * The mergeability (and immutability) is the main distinction vs {@link SourceCodeCollectorV2} (which is still used in the validation code generator and mainly exists for historic reasons)
 */
public interface ICodeGenResult<E extends IEntryPoints> {
	E entryPoints(); // behavior in mergeWith: not merged, top-level "wins"

	/**
	 * The returned instance should probably be of the same class as the original instance, but we can't represent this statically
	 * with generics, without introducing a too strict limitation for the type {@link E2}
	 */
	<E2 extends IEntryPoints> ICodeGenResult<E2> withEntryPoints(E2 newEntryPoints);

	/**
	 * Returns a new codeGenResult instance of same type as this instance, that has the entrypoints from {@code this} instance,
	 * and the collision-free merged contents of {@code this} and the {@code otherInstances}.
	 *
	 * @throws IllegalArgumentException if any of the otherInstances is of an incompatible subtype
	 */
	ICodeGenResult<E> mergeWith(List<ICodeGenResult<?>> otherInstances);

	/**
	 * Only if the codeGenResult is either a {@link IMergedSingleScriptCodeGenResult} or if it contains exactly 1 (or zero) entries.
	 *
	 * @return a byte[] that corresponds to a "plain" code generation (see e.g. {@link ICodeGeneratorParameters#isPlain()}).
	 * 		Note that the order of entries may or may not be the same as it would be when using the previous
	 * 		implementation via PlainSourceCodeCollector.
	 * 		We currently assume that the order is not important.
	 * @throws IllegalStateException if this codeGenResult is not an IMergedSingleScriptCodeGenResult and does not contain exactly
	 * 1 (or zero) entries.
	 */
	byte[] toSingleFileBytesIfApplicableOrThrow();
}
