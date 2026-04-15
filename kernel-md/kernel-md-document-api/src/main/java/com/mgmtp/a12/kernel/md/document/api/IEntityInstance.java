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
package com.mgmtp.a12.kernel.md.document.api;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.utils.CustomImplementationIsOptional;

/**
 * An {@link IEntityInstance} represents a specific instance of the corresponding {@link IElement}
 * of the {@link IDocumentModel}. It is the base type of all entities being used in a
 * {@link IDocument}.
 *
 * @deprecated use the {@code DocumentV2} API, e.g. {@code DocumentPointer} or {@code DocumentMultiPointer}, instead.
 */
@CustomImplementationIsOptional
@Deprecated(since = "30.0.0")
public interface IEntityInstance {

	/**
	 * This constant specifies the path separator used in {@link IEntityInstance#getPath()}.
	 */
	public static final String PATH_SEPARATOR = "/";
	/**
	 * This constant is used to refer to all repetitions of a repeatable group.
	 */
	public static final int ALL_SEMANTIC = 0;

	/**
	 * UNKNOWN is used, when for a semantic index or parallel iteration the iteration cannot be
	 * determined, e.g. no repetition with this semantic index exists.
	 */
	public static final long UNKNOWN = -5;

	/**
	 * The Path represents a path that is hierarchical and composed of a sequence of
	 * {@link IElement#getName()} elements separated by {@value #PATH_SEPARATOR}. The first
	 * character is
	 * always the separator {@value #PATH_SEPARATOR}.<br>
	 * The path goes from the {@link IDocumentModel} root to the {@link IElement} that corresponds
	 * to
	 * this {@link IEntityInstance}.
	 * <p>
	 * <b>Example:</b><br>
	 * This {@link IEntityInstance} refers to the following {@link IField}:<br>
	 * {@link IDocumentModel} root
	 * contains the {@link IGroup} named {@code PersonalData} which
	 * contains the sub-{@link IGroup} named {@code Address} which contains the {@link IField} named
	 * {@code StreetName}.<br>
	 * In this case the following path will be returned: {@code /PersonalData/Address/StreetName}.
	 *
	 * @return the path of this {@link IEntityInstance}. May not be {@code null} or blank.
	 */
	String getPath();

	/**
	 * An {@link IEntityInstance} represents a specific instance of the corresponding
	 * {@link IElement}. Every path element of {@link IEntityInstance} refers to a specific
	 * {@link IElement} of the {@link IDocumentModel} (see also {@link IEntityInstance#getPath()}).
	 * The returned repetitions-array specifies the repetition of every path element.
	 * <p>
	 * <b>Note:</b>
	 * <ul>
	 * <li>The length of the repetitions-array is equal to the number of path elements.</li>
	 * <li>In case of a {@link IFieldInstance}, the last repetition in the array is always 1 since a
	 * {@link IField} is not repeatable by itself.</li>
	 * </ul>
	 * <p>
	 * <b>Example:</b><br>
	 * The {@link IDocumentModel} root contains the {@link IGroup} named {@code PersonalData} which
	 * is
	 * not repeatable and contains the {@link IGroup} named {@code ChildData} which is repeatable 15
	 * times. If we have 3 {@link IGroupInstance}s of {@code ChildData}, we would have following:
	 * <ol>
	 * <li>{@code PersonalData} repetition 1, {@code ChildData} repetition 1</li>
	 * <li>{@code PersonalData} repetition 1, {@code ChildData} repetition 2</li>
	 * <li>{@code PersonalData} repetition 1, {@code ChildData} repetition 3</li>
	 * </ol>
	 *
	 * @return an array of the specific repetitions of this {@link IEntityInstance}.
	 * 		May not be {@code null} or empty.
	 */
	int[] getRepetitions();

}
