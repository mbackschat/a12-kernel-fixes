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

package com.mgmtp.a12.kernel.md.document.apiV2;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

@OnlyForUsage
public interface UpdateAction {
	UpdateActionType type();

	DocumentPointer pointer();

	FieldInstanceV2 argFieldInstance();

	GroupInstanceV2 argGroupInstance();

	enum UpdateActionType {
		PUT_GROUP_REPETITION,
		PUT_FIELD,
	}

	/**
	 * @return an update action that will add the given group at path and repetitions specified by pointer. Any preexisting group
	 * 		at or below that path and repetitions will be replaced. Any missing parent groups or lower repetitions will be created.
	 */
	static UpdateAction putGroup(@NonNull DocumentPointer pointer, @NonNull GroupInstanceV2 group) {
		return new UpdateActionImpl(UpdateActionType.PUT_GROUP_REPETITION, pointer, null, group);
	}

	static UpdateAction putGroup(@NonNull String pointerPath, @NonNull GroupInstanceV2 group) {
		return putGroup(DocumentPointer.of(pointerPath), group);
	}

	static UpdateAction putField(@NonNull DocumentPointer pointer, @NonNull FieldInstanceV2 field) {
		return new UpdateActionImpl(UpdateActionType.PUT_FIELD, pointer, field, null);
	}

	static UpdateAction putField(@NonNull String pointerPath, @NonNull FieldInstanceV2 field) {
		return putField(DocumentPointer.of(pointerPath), field);
	}

	static UpdateAction putFieldValue(@NonNull DocumentPointer pointer, Object value) {
		// field instances currently have no other property than the value, so we don't need to distinguish between putField and
		// putFieldValue for the foreseeable future
		return putField(pointer, FieldInstanceV2.ofValue(value));
	}

	static UpdateAction putFieldValue(@NonNull String pointerPath, Object value) {
		return putFieldValue(DocumentPointer.of(pointerPath), value);
	}
}
