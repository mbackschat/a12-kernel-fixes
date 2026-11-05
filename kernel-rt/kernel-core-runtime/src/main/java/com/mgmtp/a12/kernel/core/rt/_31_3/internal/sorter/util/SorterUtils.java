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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.sorter.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DataFieldNode;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DataGroupNode;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

import lombok.NonNull;

public class SorterUtils {

	private static final String ENTITY_NOT_FOUND = "Entity '%s' was not found in the generated code.";

	private SorterUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Converts the given data to a tree structure. Each filled group contains filled fields and
	 * filled subgroups.
	 * <p>
	 * <b>Note:</b> an empty group is a filled group (i.e. with a value) without filled fields.
	 *
	 * @param data
	 * 		data to be converted. This may not be <code>null</code>.
	 * @param metaData
	 * 		meta information in order to know if the given value is for a field or an empty
	 * 		group. This may not be <code>null</code>.
	 */
	public static Map<String, List<DataGroupNode>> convertDataToTree(IData data,
			IMetaDataValidierung metaData) {
		Map<String, List<DataGroupNode>> rootGroups = new HashMap<>();
		for (IIdentifier id : data) {
			if (StringUtils.isEmpty(data.getValue(id))) {
				// empty entities are ignored
				continue;
			}
			createDataNodeAndAddItToTree(rootGroups, id.getName(), id.getIndices(), data.getValue(id), metaData);
		}
		return rootGroups;
	}

	/**
	 * Converts the given data to a node and adds the given tree.
	 *
	 * @param rootGroups
	 * 		tree to which the new data should be added.
	 * @param fullName
	 * 		full name of the entity.
	 * @param indices
	 * 		corresponding indices of the entity.
	 * @param value
	 * 		xml value for the entity.
	 * @param metaData
	 * 		meta data.
	 */
	public static void createDataNodeAndAddItToTree(Map<String, List<DataGroupNode>> rootGroups, String fullName, long[] indices,
			@NonNull String value, IMetaDataValidierung metaData) {
		// no null-check for fullName because this method is intended to be used internally only
		boolean isField = metaData.isFeldDefiniert(fullName);
		boolean isGroup = metaData.isGroupDefined(fullName);
		if (!isField && !isGroup) {
			throw new IllegalArgumentException(String.format(ENTITY_NOT_FOUND, fullName));
		}

		String[] fullNameParts = fullName.substring(1).split(EntityUtil.SEPARATOR_CHAR);

		String groupFullName = "";
		DataGroupNode group = null;
		Map<String, List<DataGroupNode>> groupsByFullName = rootGroups;

		for (int i = 0; i < (isGroup ? indices.length : indices.length - 1); i++) {
			groupFullName += EntityUtil.SEPARATOR_CHAR + fullNameParts[i];
			// get given groups with the full name
			List<DataGroupNode> groups = getGroups(groupsByFullName, groupFullName);
			// get specific group with the given index
			group = getGroup(groups, groupFullName, indices[i]);
			groupsByFullName = group.getChildGroupsByFullName();
		}
		assert group != null;

		if (isGroup) {
			if (!StringUtils.isEmpty(value)) {
				group.setValue(value);
			}
		} else {
			DataFieldNode field = new DataFieldNode(fullName, value);
			group.addFieldNode(field);
		}
	}

	/**
	 * @return a list of the groups with the given full name. If the given group full name is not
	 * 		present in the given map, it will be added and an empty list is returned.
	 */
	private static List<DataGroupNode> getGroups(Map<String, List<DataGroupNode>> groupsByFullName, String groupFullName) {
		return groupsByFullName.computeIfAbsent(groupFullName, l -> new ArrayList<>());
	}

	/**
	 * @return the group in the list with the given group index. If the object does not exist, a new
	 * 		instance of {@link DataGroupNode} is created, added to the list and returned.
	 */
	private static DataGroupNode getGroup(List<DataGroupNode> groups, String groupFullName, long groupIndex) {
		for (DataGroupNode group : groups) {
			if (groupFullName.equals(group.getFullName()) && groupIndex == group.getInitialIndex()) {
				return group;
			}
		}
		DataGroupNode group = new DataGroupNode(groupFullName, groupIndex);
		groups.addLast(group);
		return group;
	}

}
