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

record UpdateActionImpl(
		UpdateAction.UpdateActionType type,
		DocumentPointer pointer,
		FieldInstanceV2 argFieldInstance,
		GroupInstanceV2 argGroupInstance
) implements UpdateAction {
	UpdateActionImpl {
		switch (type) {
			case PUT_FIELD -> {
				if(argFieldInstance == null) {
					// PUT_FIELD cannot be used to delete fields, to support deletions, we should create an explicit deletion
					// action type, to e.g. handle absent parents correctly
					throw new IllegalArgumentException("field update must not be null for PUT_FIELD action, but was for pointer " + pointer);
				}
			}
			case PUT_GROUP_REPETITION -> {
				if(argGroupInstance == null) {
					// PUT_GROUP_REPETITION cannot be used to delete groups.
					// to support that, to support deletions, we should create an explicit deletion
					// action type, to e.g. handle absent parents correctly, and also to make sure that
					// reordering during the initial sorting in batchupdate doesn't result in unexpected behavior
					throw new IllegalArgumentException("group update must not be null for PUT_GROUP action, but was for pointer " + pointer);
				}
			}
		}
	}
}