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
package com.mgmtp.a12.kernel.md.document.api.services;

import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.utils.CustomImplementationIsOptional;

/**
 * Provides creation functionality for {@link IDocument}s and the corresponding content, e.g.
 * {@link IFieldInstance}s and {@link IGroupInstance}s.
 * <p>
 * <b><i>Important note:</i></b> The caller may implement this interface, if he plans to use own
 * implementations of {@link IDocument}, {@link IGroupInstance} and {@link IFieldInstance}.
 *
 * @deprecated No longer needed, use {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2},
 *        {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2}, and
 *        {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2} instead. The interfaces each provide static factory
 * 		methods
 *        {@code empty()} and {@code of(...)} to create the respective elements.
 */
@CustomImplementationIsOptional
@Deprecated(since = "30.0.0")
public interface IDocumentFactory {

	/**
	 * Creates an {@link IDocument} instance based on the given {@link IDocumentModel} id.<br>
	 * Does not create or set any content besides the document model's id (see
	 * {@link IDocument#getDocumentModelId()}).
	 *
	 * @param documentModelId
	 * 		the id of the corresponding {@link IDocumentModel}. May not be {@code null} or
	 * 		blank.
	 * @return the new {@link IDocument} instance.
	 *
	 * @deprecated use {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2#empty(String)} instead.
	 */
	@Deprecated(since = "30.0.0")
	IDocument createDocument(String documentModelId);

	/**
	 * Creates a new {@link IGroupInstance} for the given path and repetitions.
	 *
	 * @param path
	 * 		the path of the {@link IGroup} the new {@link IGroupInstance} is referring to.
	 * 		May not be {@code null} or blank. Further explanations can be found in
	 *        {@link IGroupInstance#getPath()}.
	 * @param repetitions
	 * 		the specific repetitions of the new {@link IGroupInstance}. May not be
	 *        {@code null} or empty. Further explanations can be found in
	 *        {@link IGroupInstance#getRepetitions()}.
	 * @return the new {@link IGroupInstance}.
	 *
	 * @deprecated use document api V2 methods instead such as
	 *        {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2#empty()},
	 *        {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2#of(...)} or
	 *        {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2#withGroup}. Note that group instances in document
	 * 		V2 are different from group instances in document V1: V2 groups represent a subtree of the document including subgroups and
	 * 		fields (but no path or repetitions), whereas V1 groups represent path and repetitions, but no contents.
	 */
	@Deprecated(since = "30.0.0")
	IGroupInstance createGroupInstance(String path, int[] repetitions);

	/**
	 * Creates a new {@link IFieldInstance} for the given path and repetitions.<br>
	 * No value is assigned to {@link IFieldInstance}, here.<br>
	 * To assign a value {@link IFieldInstance#setValue(Object)} can be used.<br>
	 * To obtain the correct java type of a value in string representation
	 * {@link IDocumentService#convertToJavaType(IFieldInstance, String, Consumer)} can be
	 * used.
	 *
	 * @param path
	 * 		the path of the {@link IField} the new {@link IFieldInstance} is referring to. May
	 * 		not be {@code null} or blank. Further explanations can be found in
	 *        {@link IFieldInstance#getPath()}.
	 * @param repetitions
	 * 		the specific repetitions of the new {@link IFieldInstance}. May not be
	 *        {@code null} or empty. Further explanations can be found in
	 *        {@link IFieldInstance#getRepetitions()}.
	 * @return the new {@link IFieldInstance}.
	 *
	 * @deprecated use {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2#withField} or
	 *        {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2#ofValue} instead.
	 */
	@Deprecated(since = "30.0.0")
	IFieldInstance createFieldInstance(String path, int[] repetitions);

	/**
	 * Like {@link #createFieldInstance(String, int[])}, but allows to pass a value for the field instance.
	 *
	 * @deprecated use {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2#withFieldValue} or
	 *        {@code com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2#ofValue} instead.
	 */
	@Deprecated(since = "30.0.0")
	default IFieldInstance createFieldInstance(String path, int[] repetitions, Object value) {
		IFieldInstance fi = createFieldInstance(path, repetitions);
		fi.setValue(value);
		return fi;
	}
}
