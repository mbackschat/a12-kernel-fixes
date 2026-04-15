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

import java.util.Collection;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Difference between two {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2}s or {@link GroupInstanceV2}s,
 * computed
 * by
 * {@link com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils#compare(GroupInstanceV2, GroupInstanceV2,
 * DocumentV2Utils.CompareConfig)}.
 */
@OnlyForUsage
public interface DocumentChanges {
	/**
	 * Get all changed fields including those from deleted or added groups.
	 *
	 * @return the list of field changes
	 */
	Collection<Change<FieldInstanceV2>> expandedFieldChanges();

	/**
	 * Get all the changes on groups.
	 * <p>
	 * A group can either be added or deleted.
	 *
	 * @return the list of group changes
	 */
	Collection<Change<GroupInstanceV2>> groupChanges();

	/**
	 * Get all the changes on fields.
	 * <p>
	 * A field can either be added, deleted, or the value gets changed.
	 * <p>
	 * This does not contain changes on fields where the group is deleted or newly added, for this use
	 * {@link #expandedFieldChanges()}.
	 *
	 * @return the list of field changes
	 */
	Collection<Change<FieldInstanceV2>> fieldChanges();

	default boolean noChanges() {
		return groupChanges().isEmpty() && fieldChanges().isEmpty();
	}
}