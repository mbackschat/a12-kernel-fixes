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

/**
 * A12K-3632 uXML-mapper specific class should be isolated.
 * Mapping information for a root group.
 */
public class RootgroupMapInfo extends MapInfo {

	private final String rootGroupName1;
	private final String rootGroupName2;
	private final int eXMLHierarchyLevel4Splitting;
	private final int eXMLHierarchyLevel4RootGroupRepetition;
	private final int eXMLHierarchyLevel4Usb;
	private final String indexFieldFullName4Usb;

	/**
	 * @param fullName
	 * 		full name of the group in eXML (this may be a group or a root group).
	 * @param rootGroupName1
	 * 		full name of the first root group in uXML.
	 * @param rootGroupName2
	 * 		full name of the second root group in uXML.
	 * @param eXMLHierarchyLevel4Splitting
	 * 		hierarchy level in eXML to which a root group splitting was mapped to, otherwise
	 *        {@link MapInfo#NOT_APPLICABLE}.
	 * @param eXMLHierarchyLevel4RootGroupRepetition
	 * 		hierarchy level in eXML to which a root group repetition was mapped to, otherwise
	 *        {@link MapInfo#NOT_APPLICABLE}.
	 * @param eXMLHierarchyLevel4Usb
	 * 		hierarchy level in eXML to which an USB was mapped to, otherwise
	 *        {@link MapInfo#NOT_APPLICABLE}.
	 * @param indexFieldFullName4Usb
	 * 		full name of the index field that defines the USB index or {@code null} if there is no
	 * 		mapping to USB
	 */
	public RootgroupMapInfo(String fullName, String rootGroupName1, String rootGroupName2, int eXMLHierarchyLevel4Splitting,
			int eXMLHierarchyLevel4RootGroupRepetition,
			int eXMLHierarchyLevel4Usb, String indexFieldFullName4Usb) {
		super(fullName);
		this.rootGroupName1 = rootGroupName1;
		this.rootGroupName2 = rootGroupName2;
		this.eXMLHierarchyLevel4Splitting = eXMLHierarchyLevel4Splitting;
		this.eXMLHierarchyLevel4RootGroupRepetition = eXMLHierarchyLevel4RootGroupRepetition;
		this.eXMLHierarchyLevel4Usb = eXMLHierarchyLevel4Usb;
		this.indexFieldFullName4Usb = indexFieldFullName4Usb;
	}

	public String getRootGroupName1() {
		return rootGroupName1;
	}

	public String getRootGroupName2() {
		return rootGroupName2;
	}

	public int getEXMLHierarchyLevel4Splitting() {
		return eXMLHierarchyLevel4Splitting;
	}

	public boolean isRootGroupSplitting() {
		return eXMLHierarchyLevel4Splitting != MapInfo.NOT_APPLICABLE;
	}

	public int getEXMLHierarchyLevel4RootGroupRepetition() {
		return eXMLHierarchyLevel4RootGroupRepetition;
	}

	public boolean isRootGroupRepetitionMapping() {
		return eXMLHierarchyLevel4RootGroupRepetition != MapInfo.NOT_APPLICABLE;
	}

	public int getEXMLHierarchyLevel4Usb() {
		return eXMLHierarchyLevel4Usb;
	}

	public String getIndexFieldFullName4Usb() {
		return indexFieldFullName4Usb;
	}

	public boolean isUsbMapping() {
		return eXMLHierarchyLevel4Usb != MapInfo.NOT_APPLICABLE;
	}

	public boolean isTechnicalUsbMapping() {
		return isUsbMapping() && (getIndexFieldFullName4Usb() == null);
	}

}
