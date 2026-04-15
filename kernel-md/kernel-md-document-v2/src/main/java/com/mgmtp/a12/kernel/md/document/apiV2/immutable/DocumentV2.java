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

package com.mgmtp.a12.kernel.md.document.apiV2.immutable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentMergeConfig;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.GroupDocCommonImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.ImmutableDocumentV2Impl;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * An alternative interface for accessing and manipulating {@code IDocument}s that is more suitable for efficient lookup and
 * update operations and strives to have more developer-friendly util methods.
 * <p>
 * See the <a href="https://geta12.com/#/docs/2025.06/ext1/kernel/kernel-documentation-dev%23document_api_v2_java">"Document API V2 (Java)"</a>
 * developer guide in the GetA12 kernel developer documentation for details
 *
 */
@OnlyForUsage // Don't allow arbitrary user-supplied implementations (at least for now)
public interface DocumentV2 extends GroupInstanceV2 {
	/**
	 * see {@code IDocument#getId()}
	 * <p>
	 * The id can be used to identify the specific document.
	 * It is optional as it is not typically used by any kernel functionality.
	 */
	Optional<String> getId();

	/**
	 * similar to {@code IDocument#setId(String)}
	 */
	DocumentV2 withId(String id);

	/**
	 * see {@code IDocument#getDocumentModelId()} and the note about the dmId in {@link #empty(String)}
	 */
	String getDocumentModelId();

	DocumentV2 withDocumentModelId(String documentModelId);

	/**
	 * Converts this DocumentV2 to a GroupInstanceV2. i.e. returns a "root of root-groups" GroupInstanceV2 that has the root groups
	 * of the document as its subgroups.
	 * See also {@link #ofRootRootGroup(String, String, GroupInstanceV2)}.
	 */
	GroupInstanceV2 toRootRootGroup();

	// Groups and fields accessors are inherited from GroupInstanceV2.

	/**
	 * Instances of DocumentV2 are compared for equality based on their current fields and subgroups (including their names)
	 * and the documentModelId. The documentId, if present, does currently not affect equals and hashCode
	 */
	@Override boolean equals(Object other);

	/**
	 * see {@link #equals}
	 */
	@Override int hashCode();

	// override some GroupInstanceV2 methods to be more specific:
	@Override DocumentV2 withDirectSubgroup(@NonNull String name, RepetitionsV2 groupRepetitions);

	/**
	 * unsupported operation for documents
	 */
	@Override DocumentV2 withDirectField(@NonNull String name, FieldInstanceV2 field);

	@Override DocumentV2 withBatchUpdates(@NonNull Collection<UpdateAction> updates);

	// UTILS =====

	/**
	 * @param documentModelId
	 * 		This should be the id of a document model to which this document conforms (typically set e.g. during
	 * 		deserialization), but note that the document has no reference to a DM and is independent of it.
	 * 		The dmId is stored as an attribute of the document mainly for compatibility, because {@code IDocument}
	 * 		(V1) also has this dmId attribute.
	 */
	static DocumentV2 empty(String documentModelId) {
		return ImmutableDocumentV2Impl.emptyInternal(documentModelId);
	}

	/**
	 * @param documentModelId
	 * 		see the description of the documentModelId in {@link #empty(String)}
	 */
	static DocumentV2 empty(String documentModelId, String documentId) {
		return ImmutableDocumentV2Impl.emptyInternal(documentModelId, documentId);
	}

	/**
	 * @param documentModelId
	 * 		see the description of the documentModelId in {@link #empty(String)}
	 */
	static DocumentV2 of(String documentModelId, @NonNull Map<String, RepetitionsV2> subGroups) {
		return of(documentModelId, null, subGroups);
	}

	/**
	 * @param documentModelId
	 * 		see the description of the documentModelId in {@link #empty(String)}
	 */
	static DocumentV2 of(String documentModelId, String documentId,
			@NonNull Map<String, RepetitionsV2> subGroups) {
		return ofRootRootGroup(documentModelId, documentId, GroupInstanceV2.of(subGroups, Collections.emptyMap()));
	}

	/**
	 * like {@link #ofRootRootGroup(String, String, GroupInstanceV2)}, but does not set a documentId.
	 *
	 * @param documentModelId
	 * 		see the description of the documentModelId in {@link #empty(String)}
	 */
	static DocumentV2 ofRootRootGroup(String documentModelId, @NonNull GroupInstanceV2 rootOfRootGroups) {
		return ofRootRootGroup(documentModelId, null, rootOfRootGroups);
	}

	/**
	 * converts the given GroupInstanceV2 {@code rootOfRootGroups} to a DocumentV2 with the given {@code
	 * documentModelId} and {@code documentId}.
	 * The document's root groups will be the subgroup of {@code rootOfRootGroups}.
	 *
	 * @param rootOfRootGroups
	 * 		must not contain direct fields, as those are not allowed for documents
	 * @param documentModelId
	 * 		see the description of the documentModelId in {@link #empty(String)}
	 */
	static DocumentV2 ofRootRootGroup(String documentModelId, String documentId,
			@NonNull GroupInstanceV2 rootOfRootGroups) {
		return ImmutableDocumentV2Impl.ofRootOfRootGroups(
				documentModelId, documentId,
				rootOfRootGroups
		);
	}

	@Override
	default DocumentV2 withDirectSubgroup(@NonNull String name, int index, GroupInstanceV2 group) {
		return GroupDocCommonImpl.withDirectSubgroup(
				this, name, index, group
		);
	}

	@Override
	default DocumentV2 withGroup(@NonNull DocumentPointer pointer, @NonNull GroupInstanceV2 group) {
		return GroupDocCommonImpl.withGroup(this, pointer, group);
	}

	@Override
	default DocumentV2 withGroup(@NonNull String pointer, @NonNull GroupInstanceV2 group) {
		return withGroup(DocumentPointer.of(pointer), group);
	}

	@Override
	default DocumentV2 withGroupAllRepetitions(@NonNull DocumentPointer pointer,
			@NonNull RepetitionsV2 groupRepetitions) {
		return GroupDocCommonImpl.withGroupAllRepetitions(this, pointer, groupRepetitions);
	}

	@Override
	default DocumentV2 withGroupAllRepetitions(@NonNull String pointer,
			@NonNull RepetitionsV2 groupRepetitions) {
		return withGroupAllRepetitions(DocumentPointer.of(pointer), groupRepetitions);
	}

	@Override
	default DocumentV2 withDirectSubgroupRepetitionInserted(@NonNull String name, int index, @NonNull GroupInstanceV2 group) {
		return GroupDocCommonImpl.withDirectSubgroupRepetitionInserted(this, name, index, group);
	}

	@Override
	default DocumentV2 withGroupRepetitionInserted(@NonNull DocumentPointer pointer, @NonNull GroupInstanceV2 group) {
		return GroupDocCommonImpl.withGroupRepetitionInserted(this, pointer, group);
	}

	@Override
	default DocumentV2 withGroupRepetitionInserted(@NonNull String pointer, @NonNull GroupInstanceV2 group) {
		return withGroupRepetitionInserted(DocumentPointer.of(pointer), group);
	}

	@Override
	default DocumentV2 withGroupRepetitionAppended(@NonNull DocumentPointer pointer, @NonNull GroupInstanceV2 group) {
		return GroupDocCommonImpl.withGroupRepetitionAppended(this, pointer, group);
	}

	@Override
	default DocumentV2 withGroupRepetitionAppended(@NonNull String pointer, @NonNull GroupInstanceV2 group) {
		return GroupDocCommonImpl.withGroupRepetitionAppended(this, DocumentPointer.of(pointer), group);
	}

	@Override
	default DocumentV2 withGroupRemoved(@NonNull DocumentPointer pointer) {
		return GroupDocCommonImpl.withGroupRemoved(this, pointer);
	}

	@Override
	default DocumentV2 withGroupRemoved(@NonNull String pointer) {
		return withGroupRemoved(DocumentPointer.of(pointer));
	}

	@Override
	default DocumentV2 withDirectSubgroupRemoved(@NonNull String name, int index) {
		return GroupDocCommonImpl.withDirectSubgroupRemoved(this, name, index);
	}

	@Override
	default DocumentV2 withGroupAllRepetitionsRemoved(@NonNull DocumentPointer pointer) {
		return GroupDocCommonImpl.withGroupAllRepetitionsRemoved(this, pointer);
	}

	@Override
	default DocumentV2 withGroupAllRepetitionsRemoved(@NonNull String pointer) {
		return withGroupAllRepetitionsRemoved(DocumentPointer.of(pointer));
	}

	@Override
	default DocumentV2 withDirectSubgroupAllRepetitionsRemoved(@NonNull String name) {
		return GroupDocCommonImpl.withDirectSubgroupAllRepetitionsRemoved(this, name);
	}

	@Override
	default DocumentV2 withGroupEmptiedButNotRemoved(@NonNull DocumentPointer pointer) {
		return GroupDocCommonImpl.withGroupEmptiedButNotRemoved(this, pointer);
	}

	@Override
	default DocumentV2 withGroupEmptiedButNotRemoved(@NonNull String pointer) {
		return withGroupEmptiedButNotRemoved(DocumentPointer.of(pointer));
	}

	@Override
	default DocumentV2 withDirectSubgroupEmptiedButNotRemoved(@NonNull String name, int index) {
		return GroupDocCommonImpl.withDirectSubgroupEmptiedButNotRemoved(this, name, index);
	}

	@Override
	default DocumentV2 withField(@NonNull DocumentPointer pointer, @NonNull FieldInstanceV2 field) {
		return GroupDocCommonImpl.withField(this, pointer, field);
	}

	@Override
	default DocumentV2 withField(@NonNull String pointer, @NonNull FieldInstanceV2 field) {
		return withField(DocumentPointer.of(pointer), field);
	}

	@Override
	default DocumentV2 withDirectFieldRemoved(@NonNull String name) {
		return GroupDocCommonImpl.withDirectFieldRemoved(this, name);
	}

	@Override
	default DocumentV2 withFieldRemoved(@NonNull DocumentPointer pointer) {
		return GroupDocCommonImpl.withFieldRemoved(this, pointer);
	}

	@Override
	default DocumentV2 withFieldRemoved(@NonNull String pointer) {
		return withFieldRemoved(DocumentPointer.of(pointer));
	}

	@Override
	default DocumentV2 withDirectFieldValue(@NonNull String name, Object value) {
		return GroupDocCommonImpl.withDirectFieldValue(this, name, value);
	}

	@Override
	default DocumentV2 withFieldValue(@NonNull DocumentPointer pointer, Object value) {
		return GroupDocCommonImpl.withFieldValue(this, pointer, value);
	}

	@Override
	default DocumentV2 withFieldValue(@NonNull String pointer, Object value) {
		return withFieldValue(DocumentPointer.of(pointer), value);
	}

	/**
	 * see {@link #withMerged(GroupInstanceV2, IDocumentMergeConfig)}
	 */
	@Override
	default DocumentV2 withMerged(@NonNull GroupInstanceV2 other) {
		return withMerged(other, ptr -> false);
	}

	/**
	 * like {@link GroupInstanceV2#withMerged(GroupInstanceV2, IDocumentMergeConfig)}, but will return an updated version of this
	 * document
	 * rather than a group instance.
	 */
	@Override
	default DocumentV2 withMerged(@NonNull GroupInstanceV2 other, @NonNull IDocumentMergeConfig mergeConfig) {
		return GroupDocCommonImpl.merge(this, other, mergeConfig);
	}
}
