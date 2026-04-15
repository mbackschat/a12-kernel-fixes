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

package com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.xml;

/**
 * Model-Object from Rest-Server representing a XMLSchemaNode.
 * <p>
 * Note: Either the rootGroup, group or field (or none of them) can be filled!<br>
 */
public class XMLSchemaNode {

	private final String rootGroup;
	private final String group;
	private final String field;
	private final AssignmentType assignmentType;
	private final String pseudoXpath;
	private final boolean mziRepeat;
	private final boolean virtual;

	public XMLSchemaNode(String rootGroup,
			String group,
			String field,
			AssignmentType assignmentType,
			String pseudoXpath,
			boolean mziRepeat,
			boolean virtual) {

		this.rootGroup = rootGroup;
		this.group = group;
		this.field = field;
		this.assignmentType = assignmentType;
		this.pseudoXpath = pseudoXpath;
		this.mziRepeat = mziRepeat;
		this.virtual = virtual;
	}

	public String getRootGroup() {
		return rootGroup;
	}

	public String getGroup() {
		return group;
	}

	public String getField() {
		return field;
	}

	public AssignmentType getAssignmentType() {
		return assignmentType;
	}

	public String getPseudoXpath() {
		return pseudoXpath;
	}

	public boolean isMziRepeat() {
		return mziRepeat;
	}

	public boolean isVirtual() {
		return virtual;
	}

}
