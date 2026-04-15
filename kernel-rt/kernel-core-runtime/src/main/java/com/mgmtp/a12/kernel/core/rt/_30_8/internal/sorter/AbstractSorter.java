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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.sorter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriConsumer;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.sorter.util.SorterUtils;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DataFieldNode;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DataGroupNode;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DataGroupNodeComparator;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DatenImpl;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;

public abstract class AbstractSorter implements ISorter {

	protected final IMetaDataValidierung metaData;
	protected final ICustomFieldTypeFactory customFieldTypeFactory;
	protected final Locale locale;

	protected AbstractSorter(IMetaDataValidierung metaData, ICustomFieldTypeFactory customFieldTypeFactory, Locale locale) {
		this.metaData = metaData;
		this.customFieldTypeFactory = customFieldTypeFactory;
		this.locale = locale;
	}

	@Override
	public IData sort(IData data, IResult formalErrors) {
		Map<String, List<Pair<String, SortOrder>>> sortInfo = getSortInfo();
		if (!shouldRunSort(sortInfo, data, formalErrors)) {
			return data;
		}
		IData sortedData = new DatenImpl();
		sort(data, sortInfo, (initialId, sortedId, value) -> sortedData.addValue(sortedId, value));
		return sortedData;
	}

	protected void sort(IData data, Map<String, List<Pair<String, SortOrder>>> sortInfo,
			TriConsumer<IIdentifier, IIdentifier, String> consumer) {
		// convert data to tree
		Map<String, List<DataGroupNode>> rootGroups = SorterUtils.convertDataToTree(data, metaData);
		finishTreeConversion(rootGroups);
		// sort
		sortGroups(sortInfo, rootGroups, metaData);
		// provide changes to tree to consumer
		iterateChangesToTree(rootGroups, consumer);
	}

	/**
	 * @return the corresponding sort information.
	 */
	protected abstract Map<String, List<Pair<String, SortOrder>>> getSortInfo();

	/**
	 * @return <code>true</code> if the sort process should be run, otherwise <code>false</code>. At
	 * 		the moment the {@link StandardSorter} checks the given data formally, if there is an
	 * 		error, the sort process should not be run.
	 */
	protected abstract boolean shouldRunSort(Map<String, List<Pair<String, SortOrder>>> sortInfo, IData data, IResult formalErrors);

	/**
	 * Additionally processes applied to the created tree.
	 * Default implementation does nothing.
	 */
	protected void finishTreeConversion(Map<String, List<DataGroupNode>> rootGroups) {
	}

	/**
	 * After sorting, the indices of the given groups should be set once again. Each sorter can have
	 * different way of setting the indices.
	 *
	 * @param groupEntry
	 * 		entry with list of groups whose index should be updated.
	 */
	protected abstract void updateGroupIndicesAfterSorting(Entry<String, List<DataGroupNode>> groupEntry);

	protected void updateGroupIndicesAscending(List<DataGroupNode> groups) {
		for (int i = 0; i < groups.size(); i++) {
			DataGroupNode group = groups.get(i);
			group.setSortedIndex((long) i + 1);
		}
	}

	/**
	 * Sorts recursive the given list of groups according to the given sort information. If the
	 * natural order of the groups is broken after sorting, the index of each group is changed
	 * according to new order.
	 * <p>
	 * <b>Note:</b> the natural order of the groups is given by their group index.
	 *
	 * @param sortInfo
	 * 		sort information by group full name. This may not be <code>null</code>.
	 * @param groups
	 * 		groups to be sorted. This may not be <code>null</code>.
	 * @param metaData
	 * 		meta information in order to know how to sort. This may not be <code>null</code>.
	 */
	private void sortGroups(Map<String, List<Pair<String, SortOrder>>> sortInfo, Map<String, List<DataGroupNode>> groups,
			IMetaDataValidierung metaData) {
		for (Entry<String, List<DataGroupNode>> groupEntry : groups.entrySet()) {
			List<Pair<String, SortOrder>> sortInfo4Group = sortInfo.get(groupEntry.getKey());
			if (sortInfo4Group != null && !sortInfo4Group.isEmpty()) {
				DataGroupNodeComparator comparator = new DataGroupNodeComparator(groupEntry.getKey(), sortInfo4Group, metaData);
				groupEntry.getValue().sort(comparator);
				updateGroupIndicesAfterSorting(groupEntry);
			}
			// now the subgroups
			for (DataGroupNode group : groupEntry.getValue()) {
				sortGroups(sortInfo, group.getChildGroupsByFullName(), metaData);
			}
		}
	}

	private static void iterateChangesToTree(Map<String, List<DataGroupNode>> rootGroupsByFullName,
			TriConsumer<IIdentifier, IIdentifier, String> consumer) {
		for (List<DataGroupNode> rootGroupList : rootGroupsByFullName.values()) {
			for (DataGroupNode rootGroup : rootGroupList) {
				iterateChangesToGroup(new long[0], new long[0], rootGroup, consumer);
			}
		}
	}

	/**
	 * Iterates over all elements in the given group and provides the {@code consumer} with the following information for each
	 * contained element regarding changes:
	 * - initial identifier
	 * - sorted identifier
	 * - string value
	 */
	private static void iterateChangesToGroup(long[] initialParentIndices, long[] sortedParentIndices, DataGroupNode group,
			TriConsumer<IIdentifier, IIdentifier, String> consumer) {
		long[] initialGroupIndices = createChildIndices(initialParentIndices, group.getInitialIndex());
		long[] sortedGroupIndices = createChildIndices(sortedParentIndices, group.getSortedIndex());
		// is an empty group?
		if (!StringUtils.isEmpty(group.getValue())) {
			consumer.accept(
					new RtExternalIdentifier(group.getFullName(), initialGroupIndices),
					new RtExternalIdentifier(group.getFullName(), sortedGroupIndices),
					group.getValue());
		}
		// convert fields
		Map<String, DataFieldNode> fields = group.getFieldsByFullName();
		for (DataFieldNode field : fields.values()) {
			long[] initialFieldIndices = createChildIndices(initialGroupIndices, 1L);
			long[] sortedFieldIndices = createChildIndices(sortedGroupIndices, 1L);
			consumer.accept(
					new RtExternalIdentifier(field.getFullName(), initialFieldIndices),
					new RtExternalIdentifier(field.getFullName(), sortedFieldIndices),
					field.getValue());
		}
		// convert subgroups
		Map<String, List<DataGroupNode>> childGroups = group.getChildGroupsByFullName();
		for (List<DataGroupNode> childGroupList : childGroups.values()) {
			for (DataGroupNode childGroup : childGroupList) {
				iterateChangesToGroup(initialGroupIndices, sortedGroupIndices, childGroup, consumer);
			}
		}
	}

	private static long[] createChildIndices(long[] parentIndices, long childIndex) {
		long[] childIndices = Arrays.copyOf(parentIndices, parentIndices.length + 1);
		childIndices[childIndices.length - 1] = childIndex;
		return childIndices;
	}

}
