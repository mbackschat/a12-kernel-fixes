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

package com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;

/**
 * Represents a mapping info that specifies how to map the repeatability of a group of an eXML model
 * to a root group repeatability.
 * Possible kinds of field repeatability are: (technical) USB, LfdNr (root group index) and root group splitting.
 * <p>
 * {@link IRootgroupRepeatabilityMapping#getEntity()} has to return an {@link IGroup}.
 */
public interface IRootgroupRepeatabilityMapping extends IMapInfo {

	public final String USB = "[USB]";
	public final String LFD_NR = "[LfdNr]";
	public final String SPLIT = "[Split]";

	public enum MappingType {
		USB,
		TECH_USB,
		LFD_NR,
		SPLIT
	}

	public MappingType getType();
}
