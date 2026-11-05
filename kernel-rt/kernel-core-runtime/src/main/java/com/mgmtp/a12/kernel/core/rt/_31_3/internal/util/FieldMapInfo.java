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

import java.util.ArrayList;
import java.util.List;

/**
 * A12K-3632 uXML-mapper specific class should be isolated.
 * Mapping information for a field.
 */
public class FieldMapInfo extends MapInfo {

	private final List<String> uXMLNames4RootGroup1;
	private final List<String> uXMLNames4RootGroup2;
	private final List<Integer> mziSpecs4UXMLNames;
	private final List<Integer> accumulatedMziSpecs4UXMLNames;
	private final int eXMLHierarchyLevel4Mzi;
	private final int eXMLHierarchyLevel4FieldList;
	private final RootgroupMapInfo rootGroupMapInfo;

	/**
	 * @param fullName
	 * 		field full name in eXML.
	 * @param uXMLNames4RootGroup1
	 * 		list of names in uXML model to which the eXML field was mapped to for the first
	 * 		root group. This may not be <code>null</code>.
	 * @param uXMLNames4RootGroup2
	 * 		list of names in uXML model to which the eXML field was mapped to for the second
	 * 		root group, only if for the corresponding root group a splitting was mapped. This
	 * 		my be <code>null</code> if there is no root group splitting.
	 * @param mziSpecs4UXMLNames
	 * 		mzi specifications for each field in uXML to which the eXML was mapped to. This is
	 * 		only possible if for the field a field list mapping was defined. This may not be
	 * 		<code>null</code>.
	 * @param eXMLHierarchyLevel4Mzi
	 * 		hierarchy level in eXML for which a mzi mapping was defined, otherwise
	 *        {@link MapInfo#NOT_APPLICABLE}.
	 * @param eXMLHierarchyLevel4FieldList
	 * 		hierarchy level in eXML for which a field list mapping was defined, otherwise
	 *        {@link MapInfo#NOT_APPLICABLE}.
	 * @param rootgGroupMapInfo
	 * 		mapping information for the corresponding group.
	 */
	public FieldMapInfo(String fullName, List<String> uXMLNames4RootGroup1, List<String> uXMLNames4RootGroup2,
			List<Integer> mziSpecs4UXMLNames,
			int eXMLHierarchyLevel4Mzi, int eXMLHierarchyLevel4FieldList, RootgroupMapInfo rootgGroupMapInfo) {
		super(fullName);
		this.uXMLNames4RootGroup1 = uXMLNames4RootGroup1;
		this.uXMLNames4RootGroup2 = uXMLNames4RootGroup2;
		this.mziSpecs4UXMLNames = mziSpecs4UXMLNames;
		this.accumulatedMziSpecs4UXMLNames = createAccumulatedMziSpecs(mziSpecs4UXMLNames);
		this.eXMLHierarchyLevel4Mzi = eXMLHierarchyLevel4Mzi;
		this.eXMLHierarchyLevel4FieldList = eXMLHierarchyLevel4FieldList;
		this.rootGroupMapInfo = rootgGroupMapInfo;
	}

	private List<Integer> createAccumulatedMziSpecs(List<Integer> mziSpecs4UXMLNames) {
		List<Integer> accumulatedMziSpecs = new ArrayList<>();
		int accumulatedMziSpec = 0;
		for (Integer mziSpec : mziSpecs4UXMLNames) {
			if (mziSpec == MapInfo.NOT_APPLICABLE) {
				accumulatedMziSpecs.add(mziSpec);
			} else {
				accumulatedMziSpec += mziSpec;
				accumulatedMziSpecs.add(accumulatedMziSpec);
			}
		}
		return accumulatedMziSpecs;
	}

	public List<String> getUXMLNames4RootGroup1() {
		return uXMLNames4RootGroup1;
	}

	public List<String> getUXMLNames4RootGroup2() {
		return uXMLNames4RootGroup2;
	}

	public boolean isRootGroupSplitting() {
		return uXMLNames4RootGroup2 != null;
	}

	public List<Integer> getMziSpecs4UXMLNames() {
		return mziSpecs4UXMLNames;
	}

	public boolean isFieldListWithMziMapping() {
		return mziSpecs4UXMLNames.getFirst() != MapInfo.NOT_APPLICABLE;
	}

	/**
	 * @return a list with accumulated mzi specifications. Example: if
	 *        {@link FieldMapInfo#getMziSpecs4UXMLNames()} returns [3,3,5,5,4], this method returns
	 * 		[3,6,11,16,20].
	 */
	public List<Integer> getAccumulatedMziSpecs4UXMLNames() {
		return accumulatedMziSpecs4UXMLNames;
	}

	public int getEXMLHierarchyLevel4Mzi() {
		return eXMLHierarchyLevel4Mzi;
	}

	public boolean isMziMapping() {
		return eXMLHierarchyLevel4Mzi != MapInfo.NOT_APPLICABLE;
	}

	public int getEXMLHierarchyLevel4FieldList() {
		return eXMLHierarchyLevel4FieldList;
	}

	public boolean isFieldListMapping() {
		return eXMLHierarchyLevel4FieldList != MapInfo.NOT_APPLICABLE;
	}

	public RootgroupMapInfo getRootGroupMapInfo() {
		return rootGroupMapInfo;
	}

}
