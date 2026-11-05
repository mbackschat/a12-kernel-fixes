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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * This class models a filled group: either as an empty group or as a group for which at least a
 * field in one of its subgroups was filled.
 */
public class DataGroupNode {

	@Getter private final String fullName;
	@Getter private final long initialIndex;
	@Getter @Setter private long sortedIndex;
	@Getter @Setter private String value = null;

	@Getter private final Map<String, DataFieldNode> fieldsByFullName = new HashMap<>();
	@Getter private final Map<String, List<DataGroupNode>> childGroupsByFullName = new HashMap<>();

	/**
	 * @param fullName
	 * 		- full name of the filled group.
	 * @param initialIndex
	 * 		- index of the filled group.
	 */
	public DataGroupNode(String fullName, long initialIndex) {
		this.fullName = fullName;
		this.initialIndex = initialIndex;
		this.sortedIndex = initialIndex;
	}

	public DataFieldNode getFieldNode(String fullName) {
		return fieldsByFullName.get(fullName);
	}

	public void addFieldNode(DataFieldNode field) {
		this.fieldsByFullName.put(field.getFullName(), field);
	}

	@Override
	public String toString() {
		String valueStr = value == null ? "" : String.format("=%s", value);
		return String.format("%s[%d]%s", fullName, initialIndex, valueStr);
	}

}
