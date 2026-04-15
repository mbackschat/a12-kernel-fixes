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

import java.util.Optional;

import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IRootgroupRepeatabilityMapping.MappingType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;

/**
 * Represents a mapping info that maps a root group of an A12 model to one or more root groups in an
 * H model.
 * <p>
 * {@link IRootGroupMapping#getEntity()} has to return an {@link IGroup}.
 */
public interface IRootGroupMapping extends IMapInfo {

	/**
	 * @return full name of the (first) root group in the H model
	 */
	public String getRootGroupName1();

	/**
	 * @return full name of the second root group in the H model or null if there is no splitting
	 */
	public String getRootGroupName2();

	/**
	 * @return the index field for the root group mapping (see also
	 *        {@link IRootGroupMapping#setIndexField4SbKzQuery(IField)}), otherwise
	 * 		<code>null</code>.
	 */
	public IField getIndexField4SbKzQuery();

	/**
	 * Sets the index field for the root group mapping (at the moment only one index field can
	 * exist, index field for usb is excluded), for example:
	 * <ul>
	 * <li>for a root group splitting (in this case the index field is required)</li>
	 * <li>mapping to the root group repetition, just in case an index field was found for the root
	 * group mapping</li>
	 * </ul>
	 * <b>Note:</b> the index field for the mapping to usb shall be not cached here.
	 */
	public void setIndexField4SbKzQuery(IField field);

	/**
	 * The splitting level is only set for code generation, otherwise
	 * {@link IMapInfo#NOT_APPLICABLE} is returned.
	 *
	 * @return the level at which splitting was mapped or {@link IMapInfo#NOT_APPLICABLE} if there
	 * 		is no splitting.
	 */
	public int getHierarchyLevel4Splitting();

	/**
	 * Sets the level at which splitting was mapped.
	 */
	public void setHierarchyLevel4Splitting(int hierarchyLevel4Splitting);

	/**
	 * The rootgroup repetition level is only set for code generation, otherwise
	 * {@link IMapInfo#NOT_APPLICABLE} is returned.
	 *
	 * @return the level at which the repeatability of the root group was mapped or
	 *        {@link IMapInfo#NOT_APPLICABLE} if the rootgroup is not repeatable.
	 */
	public int getHierarchyLevel4RootgroupRepetition();

	/**
	 * Sets the level at which the repeatability of the root group was mapped.
	 */
	public void setHierarchyLevel4RootgroupRepetition(int hierarchyLevel4RootgroupRepetition);

	/**
	 * The USB level is only set for code generation, otherwise
	 * {@link IMapInfo#NOT_APPLICABLE} is returned.
	 *
	 * @return the level at which the USB was mapped or {@link IMapInfo#NOT_APPLICABLE} if there
	 * 		is no mapping to USB.
	 */
	public int getHierarchyLevel4Usb();

	/**
	 * Sets the level at which USB was mapped.
	 */
	public void setHierarchyLevel4Usb(int hierarchyLevel4Usb);

	/**
	 * @return the corresponding mapping type if the entity was also mapped to a root group repeatability.
	 */
	public Optional<MappingType> getRootgroupRepeatabilityMappingType();

	/**
	 * Indicates whether one of the repeatable levels on the full name of the mapped entity of this
	 * mapping is technically mapped to the USB. The mapping can be this mapping (in this case
	 * {@link #getRootgroupRepeatabilityMappingType()} returns an Optional with {@link MappingType#TECH_USB}) or an instance of
	 * {@link IRootgroupRepeatabilityMapping} with {@link MappingType} == {@link MappingType#TECH_USB}
	 * for an ancestor of the mapped entity of this mapping.
	 * <p>
	 * This information is needed for code generation.
	 */
	public boolean hasLevelMappedToTechnicalUsb();

	/**
	 * Sets the information that for an entity on the path of the mapped entity of this mapping a
	 * repeatable level is technically mapped to the USB.
	 */
	public void setHasLevelMappedToTechnicalUsb();

}
