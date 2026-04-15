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

package com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.Change;
import com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.DocumentChanges;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;

import lombok.NonNull;

public record DocumentChangesImpl(@NonNull Collection<Change<GroupInstanceV2>> groupChanges,
								  @NonNull Collection<Change<FieldInstanceV2>> fieldChanges) implements DocumentChanges {

	public static DocumentChangesImpl empty() {
		return new DocumentChangesImpl(new ArrayList<>(), new ArrayList<>());
	}

	public void groupChange(@NonNull Change<GroupInstanceV2> groupChange) {
		this.groupChanges.add(groupChange);
	}

	public void fieldChange(@NonNull Change<FieldInstanceV2> fieldChange) {
		this.fieldChanges.add(fieldChange);
	}

	@Override public Collection<Change<FieldInstanceV2>> expandedFieldChanges() {
		Set<Change<FieldInstanceV2>> changes = new HashSet<>(fieldChanges());

		for (Change<GroupInstanceV2> changedGroup : groupChanges()) {
			if (changedGroup.isAdd()) {
				changedGroup.newValue().traverse(new AddedGroupExpanderVisitor(changedGroup.pointer(), changes::add));
			} else if (changedGroup.isDelete()) {
				changedGroup.oldValue().traverse(new DeletedGroupExpanderVisitor(changedGroup.pointer(), changes::add));
			} else {
				throw new RuntimeException("internal: Unexpected change type: " + changedGroup);
			}
		}

		return changes;
	}

	private record AddedGroupExpanderVisitor(DocumentPointer pointerFromBaseToAddedGroup, Consumer<Change<FieldInstanceV2>> changes)
			implements IDocumentV2Visitor {
		@Override public void visitField(DocumentPointer pointerRelativeToAddedGroup, FieldInstanceV2 field) {
			IDocumentV2Visitor.super.visitField(pointerRelativeToAddedGroup, field);
			DocumentPointer pointerFromBaseToAddedField = pointerFromBaseToAddedGroup.withConcatenated(pointerRelativeToAddedGroup);
			changes.accept(Change.fieldAdded(pointerFromBaseToAddedField, field));
		}
	}

	private record DeletedGroupExpanderVisitor(DocumentPointer pointerFromBaseToDeletedGroup, Consumer<Change<FieldInstanceV2>> changes)
			implements IDocumentV2Visitor {
		@Override public void visitField(DocumentPointer pointerRelativeToDeletedGroup, FieldInstanceV2 field) {
			IDocumentV2Visitor.super.visitField(pointerRelativeToDeletedGroup, field);
			DocumentPointer pointerFromBaseToDeletedField = pointerFromBaseToDeletedGroup.withConcatenated(
					pointerRelativeToDeletedGroup);
			changes.accept(Change.fieldDeleted(pointerFromBaseToDeletedField, field));
		}
	}
}
