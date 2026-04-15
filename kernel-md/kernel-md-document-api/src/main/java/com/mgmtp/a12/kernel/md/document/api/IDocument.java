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

import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.utils.CustomImplementationIsOptional;
import com.mgmtp.a12.model.header.Header;

/**
 * A {@link IDocument} is an instance of a {@link IDocumentModel} and represents a cohesive set of
 * data. To achieve a better performance for search in the document via {@link IDocumentSearchService}, the clients can implement
 * {@link IDocumentIndexed} in case an own implementation of this interface exists.
 *
 * @deprecated use {@code DocumentV2} instead.
 */
@CustomImplementationIsOptional
@Deprecated(since = "30.0.0")
public interface IDocument {

	/**
	 * @return the id of this {@link IDocument}.
	 */
	Optional<String> getId();

	/**
	 * @param id
	 * 		the new id for this {@link IDocument}. Use {@code null} to unset.
	 */
	void setId(String id);

	/**
	 * @return the id of the document model this document belongs to (see
	 *        {@link Header#getId()}).
	 */
	String getDocumentModelId();

	/**
	 * @return all {@link IEntityInstance}s which were added to this {@link IDocument} (see also
	 *        {@link #addEntityInstance(IEntityInstance)}) and have not been removed
	 * 		({@link #removeEntityInstance(IEntityInstance)}).
	 * 		If no {@link IEntityInstance} was added yet, the set is empty.
	 * 		<p>
	 * 		Note that uniqueness/set semantics are based only on
	 *        {@link IEntityInstance#getPath()} and
	 *        {@link IEntityInstance#getRepetitions()}, for example {@link IFieldInstance}s which
	 * 		only differ in the value ({@link IFieldInstance#getValue()}) cannot simultaneously
	 * 		exist in the document.
	 * 		Modifications of the returned Set object will not modify the document.
	 */
	Set<IEntityInstance> getEntityInstances();

	/**
	 * Adds the given {@link IEntityInstance} to this {@link IDocument} if no entry with the same
	 * {@link IEntityInstance#getPath()} and {@link IEntityInstance#getRepetitions()} existed in the
	 * document. If an entry with equal path and repetitions existed before, the given entity
	 * instance
	 * is not added.
	 * <br>
	 * It is <b>not</b> necessary to
	 * build the complete structure of nested {@link IGroupInstance}s, it is sufficient to add the
	 * {@link IFieldInstance}. But to indicate that a {@link IGroupInstance} exists without any
	 * {@link IFieldInstance}, it must be added explicitly.
	 * <p>
	 * <b>Notes regarding the implementation of this interface provided by Kernel:</b>
	 * <ul>
	 *     <li>if a {@link IEntityInstance} (field or group) is added, the associated ancestor
	 *     {@link IGroupInstance}s are <b>not</b> added internally.</li>
	 *     <li>if the to-add {@link IEntityInstance} is repeatable and produces a gap in the repetitions
	 *     at a certain repeatable level, the gap is <b>not</b> closed internally.</li>
	 * </ul>
	 *
	 * @param entityInstance
	 * 		the {@link IEntityInstance} to be added. May not be {@code null}. <b>Note:</b> The
	 * 		use of {@value IEntityInstance#ALL_SEMANTIC}
	 * 		as repetition is not allowed in this
	 * 		case.
	 * @return {@code true} if no entry with the same {@link IEntityInstance#getPath()} and
	 *        {@link IEntityInstance#getRepetitions()} was present before (and thus the given
	 * 		entityInstance was inserted), otherwise {@code false} (if the document was not
	 * 		changed).
	 */
	boolean addEntityInstance(IEntityInstance entityInstance);

	/**
	 * Removes from this {@link IDocument} the {@link IEntityInstance} at the path and repetitions
	 * specified by the given {@link IEntityInstance} if such an entry exists.
	 * <br>
	 * Removing a {@link IGroupInstance} does not result in removing the corresponding descendants
	 * (for this function use {@link IDocumentService#removeEntityTree(IGroupInstance)}).
	 *
	 * @param entityInstance
	 * 		an {@link IEntityInstance} with the path and repetitions
	 * 		({@link IEntityInstance#getPath()},
	 *        {@link IEntityInstance#getRepetitions()}) to be removed. May not be {@code null}.
	 * 		<b>Note:</b>
	 * 		The use of {@value IEntityInstance#ALL_SEMANTIC} as repetition is not allowed in
	 * 		this case.
	 * @return {@code true} if an entry was present at the given path and repetitions (and thus
	 * 		removed), otherwise (if nothing was there that could be removed)
	 *        {@code false}. Removing a group that was not explicitly added
	 * 		via {@link #addEntityInstance(IEntityInstance)} will return false (even if the group
	 * 		is a parent of a field that is present in the document).
	 */
	boolean removeEntityInstance(IEntityInstance entityInstance);

}
