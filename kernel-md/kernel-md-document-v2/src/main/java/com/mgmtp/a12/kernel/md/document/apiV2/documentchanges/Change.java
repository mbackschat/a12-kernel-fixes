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

package com.mgmtp.a12.kernel.md.document.apiV2.documentchanges;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.documentchanges.internal.ChangeImpl;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Any difference between two {@link DocumentV2}s.
 * <p>
 * This can not be created directly, use
 * {@link com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils#compare(GroupInstanceV2, GroupInstanceV2,
 * DocumentV2Utils.CompareConfig)}
 * to compute changes between two documents or groups.
 *
 * @param <T>
 * 		the type of the changed entity
 */
@OnlyForUsage
public interface Change<T> {
	@NonNull DocumentPointer pointer();

	T oldValue();

	T newValue();

	default boolean isAdd() {
		return oldValue() == null && newValue() != null;
	}

	default boolean isDelete() {
		return oldValue() != null && newValue() == null;
	}

	default boolean isUpdate() {
		return oldValue() != null && newValue() != null;
	}

	static Change<FieldInstanceV2> fieldAdded(@NonNull DocumentPointer pointer, @NonNull FieldInstanceV2 newField) {
		return new ChangeImpl<>(pointer, null, newField);
	}

	static Change<FieldInstanceV2> fieldDeleted(@NonNull DocumentPointer pointer, @NonNull FieldInstanceV2 oldField) {
		return new ChangeImpl<>(pointer, oldField, null);
	}

	static Change<FieldInstanceV2> fieldUpdated(@NonNull DocumentPointer pointer, @NonNull FieldInstanceV2 oldField,
			@NonNull FieldInstanceV2 newField) {
		return new ChangeImpl<>(pointer, oldField, newField);
	}

	static Change<GroupInstanceV2> groupAdded(@NonNull DocumentPointer pointer, @NonNull GroupInstanceV2 newGroup) {
		return new ChangeImpl<>(pointer, null, newGroup);
	}

	static Change<GroupInstanceV2> groupDeleted(@NonNull DocumentPointer pointer, @NonNull GroupInstanceV2 oldGroup) {
		return new ChangeImpl<>(pointer, oldGroup, null);
	}
}
