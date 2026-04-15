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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

import static com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RuntimeUtils.parentIndices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.GroupFillCache;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IndexFieldCache;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;

public class IndexFieldDefaultValueUtils {
	/**
	 * Determine which instances of the given index field are eligible for default value handling.
	 * <p>
	 * An index field instance can be filled with its default value only if:
	 * - the index field has an S-Value defined (currently only possible for enumerations)
	 * - the index field is not a mandatory field (through check box)
	 * - there is exactly 1 instance of the index group within an instance of the index group parent, and the index field in that
	 * group instance is not filled
	 *
	 * @return index field ids for which a default value should be set (without duplicates)
	 */
	public static List<RtInternalIdentifier> getIndexFieldIdsEligibleForDefaultValueHandling(
			String indexFieldFullName,
			GroupFillCache groupFillCache,
			IndexFieldCache indexFieldCache,
			IMetaDataValidierung metaData
	) {
		// skip if all instances of the index field are ineligible
		String indexFieldUniqueName = metaData.getUniqueName(indexFieldFullName);
		if (StringUtils.isEmpty(getSValue(indexFieldUniqueName, metaData))
				|| metaData.isExplicitlyMandatory(indexFieldUniqueName)) {
			return List.of();
		}

		String indexGroupFullName = EntityUtil.getParentPath(indexFieldFullName);

		List<RtInternalIdentifier> result = new ArrayList<>();
		getIndexGroupParentIndicesToIndexGroupIds(indexGroupFullName, groupFillCache).forEach(
				(indexGroupParentIndices, indexGroupIds) -> {
					if (indexGroupIds.size() == 1
							&& indexFieldCache.noIndexFieldsFilled(indexGroupFullName, indexGroupParentIndices)) {
						long[] indexGroupIndices = indexGroupIds.getFirst().getIndices();
						long[] indexFieldIndices = Arrays.copyOf(indexGroupIndices, indexGroupIndices.length + 1);
						indexFieldIndices[indexFieldIndices.length - 1] = 1;
						result.add(new RtInternalIdentifier(indexFieldUniqueName, indexFieldIndices));
					}
				});
		return result;
	}

	/**
	 * @return a map
	 * 		- from: the indices of filled instances of the given index group's parent
	 * 		- to: all filled index group instances within (i.e. all filled repetitions)
	 */
	private static Map<List<Long>, List<RtInternalIdentifier>> getIndexGroupParentIndicesToIndexGroupIds(
			String indexGroupFullName,
			GroupFillCache groupFillCache
	) {
		Map<List<Long>, List<RtInternalIdentifier>> indexGroupParentIndicesToIndexGroupIds = new HashMap<>();
		for (RtInternalIdentifier indexGroupId : groupFillCache.getFilledGroupInstances(indexGroupFullName)) {
			List<Long> parentIndices = parentIndices(indexGroupId.getIndices());
			indexGroupParentIndicesToIndexGroupIds.computeIfAbsent(parentIndices, k -> new ArrayList<>()).add(indexGroupId);
		}
		return indexGroupParentIndicesToIndexGroupIds;
	}

	public static String getSValue(String uniqueName, IMetaDataValidierung metaData) {
		IFormatDefinition formatDef = metaData.getIFormatDefinition(uniqueName);
		// at the moment an s value can only be defined for enumerations
		return (String) formatDef.getWert(IMetaKeys.FIELD_S_VALUE);
	}

	private IndexFieldDefaultValueUtils() {
		throw new UnsupportedOperationException("util class should not be instantiated");
	}
}
