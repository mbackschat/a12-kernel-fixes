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
package com.mgmtp.a12.kernel.core.tool.a12internal.api.ado;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ISortField;

/**
 * Interface to model a group.
 */
public interface IGroup extends IEntity {

	/**
	 * @return set of short field names specifying the index fields of the group. The set is empty,
	 * 		if no index is specified for the group.
	 */
	Set<String> getIndexFieldNames();

	/**
	 * @return map from sort-category-name to list of corresponding sort fields. The sort field list
	 * 		on a category is null, if no sort information is specified for the category.
	 *
	 * <p><strong>Note:</strong> Sort fields are used only in the mapping scenario
	 * and are only relevant for the source model(s). They define the order of the
	 * repetitions of a group. This sorting takes place <em>before</em> mapping.
	 * For example, if only the first three entries of a repeatable group are to be
	 * included in the mapping, the sort order is critical, because it defines which
	 * repetitions will be mapped.
	 */
	default Map<String, List<ISortField>> getSortInfo() {
		return new HashMap<>();
	}

	/**
	 * @return the custom type of the group or {@code null} if no custom type was defined.
	 */
	default Optional<String> getCustomType() {
		return Optional.empty();
	}

}
