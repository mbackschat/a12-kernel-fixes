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

import java.util.Comparator;
import java.util.List;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.sorter.SortOrder;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;

/**
 * In this class is defined how two groups are to be compared in order to be sorted.
 */
public class DataGroupNodeComparator implements Comparator<DataGroupNode> {

	private final String groupFullName;
	private final List<Pair<String, SortOrder>> sortInfo;
	private final IMetaDataValidierung metaData;

	/**
	 * @param sortInfo
	 * 		- sort information of the group
	 * @param metaData
	 * 		- meta data of the model
	 */
	public DataGroupNodeComparator(String groupFullName, List<Pair<String, SortOrder>> sortInfo, IMetaDataValidierung metaData) {
		this.groupFullName = groupFullName;
		this.sortInfo = sortInfo;
		this.metaData = metaData;
	}

	@Override
	public int compare(DataGroupNode g1, DataGroupNode g2) {
		int result = 0;
		for (Pair<String, SortOrder> sortInfoItem : sortInfo) {
			// compare the groups by the actual sort field
			result = compareGroups(sortInfoItem, g1, g2);
			if (result == 0) {
				// if the groups have the same value for the actual sort field, the groups are compare by the next sort field
			} else {
				// if not, the comparison is finished
				break;
			}
		}
		if (result == 0) {
			// if the groups have the same values for all sort fields, the groups are compare by their index
			return Long.compare(g1.getInitialIndex(), g2.getInitialIndex());
		} else {
			return result;
		}
	}

	/**
	 * Compares two groups by the given sort information.
	 */
	private int compareGroups(Pair<String, SortOrder> groupSortInfo, DataGroupNode g1, DataGroupNode g2) {
		String sortFieldName = String.format("%s/%s", groupFullName, groupSortInfo.getEntry1());
		// get the sort field value for each group
		DataFieldNode f1 = g1.getFieldNode(sortFieldName);
		DataFieldNode f2 = g2.getFieldNode(sortFieldName);
		if (f1 == null) {
			if (f2 == null) {
				// if the sort field is not filled for both groups, the groups are equal
				return 0;
			} else {
				// first group is greater and goes to the end of the list
				return 1;
			}
		} else {
			if (f2 == null) {
				// first group is smaller and goes to the beginning of the list
				return -1;
			} else {
				// comparison of the values for the sort field
				return compareValues(sortFieldName, groupSortInfo.getEntry2(), f1, f2);
			}
		}
	}

	/**
	 * Compares two fields by their value.
	 */
	private int compareValues(String sortFieldName, SortOrder sortOrder, DataFieldNode f1, DataFieldNode f2) {
		// the comparison of the values depends on the field type
		IFormatDefinition sortFieldFormatDef = metaData.getIFormatDefinition(metaData.getUniqueName(sortFieldName));
		switch (sortOrder) {
			case ASCENDING:
				return sortFieldFormatDef.compare(f1.getValue(), f2.getValue(), metaData);
			case DESCENDING:
			default:
				return sortFieldFormatDef.compare(f2.getValue(), f1.getValue(), metaData);
		}
	}

}
