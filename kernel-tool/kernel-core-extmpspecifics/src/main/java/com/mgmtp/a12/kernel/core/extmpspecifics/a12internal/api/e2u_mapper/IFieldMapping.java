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

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

/**
 * Represents a mapping info that maps a field of an A12 model to one or more fields in an H model.
 * <p>
 * {@link IFieldMapping#getEntity()} has to return an {@link IField}.
 */
public interface IFieldMapping extends IMapInfo {

	/**
	 * Returns the field identifiers in the H model to that the A12 field is mapped. In case of
	 * splitting root groups each pair contains the field identifiers for the two root groups. If
	 * there is no splitting the second element of each pair is null.
	 *
	 * @return field identifiers in the H model
	 */
	public List<Pair<String, String>> getFieldIdentifiers();

	/**
	 * In case of mapping a repetition layer to a field list combined with MZIs, these MZI
	 * specifications are returned here. If for the field list no MZIs are specified, the
	 * list contains only {@link IMapInfo#NOT_APPLICABLE}-items. This means there is no mix between
	 * mzi specification and no mzi specification.
	 *
	 * @return MZI specifications if present. The list has the same size as the list returned by
	 *        {@link #getFieldIdentifiers()}.
	 */
	public List<Integer> getMziSpecs();

	/**
	 * @return the accumulated mzi specifications, if the mzi specification is [3,2,5,1], the
	 * 		returned list contains [3,5,10,11].
	 */
	public List<Integer> getAccumulatedMziSpecs();

	/**
	 * The MZI level is only set for code generation, otherwise
	 * {@link IMapInfo#NOT_APPLICABLE} is returned.
	 * <p>
	 * In case of mapping to a field list with MZI specifications, {@link IMapInfo#NOT_APPLICABLE}
	 * is returned for this method. In this case the respective level is returned for
	 * {@link #getHierarchyLevel4FieldList()}.
	 *
	 * @return the level at which the repeatability of the field (MZI) was mapped or
	 *        {@link IMapInfo#NOT_APPLICABLE} if there is no mapping to MZI.
	 */
	public int getHierarchyLevel4Mzi();

	/**
	 * Sets the level at which the repeatability of the field (MZI) was mapped.
	 */
	public void setHierarchyLevel4Mzi(int hierarchyLevel4Mzi);

	/**
	 * The field list level is only set for code generation, otherwise
	 * {@link IMapInfo#NOT_APPLICABLE} is returned.
	 *
	 * @return the level at which a field list was mapped or {@link IMapInfo#NOT_APPLICABLE} if
	 * 		there is no mapping to a field list.
	 */
	public int getHierarchyLevel4FieldList();

	/**
	 * Sets the the level at which a field list was mapped.
	 */
	public void setHierarchyLevel4FieldList(int hierarchyLevel4FieldList);

	/**
	 * @return the index field which controls the order of the field list or the rows in case of an
	 * 		mzi mapping. Since the index field is not required, {@code null} will be returned if
	 * 		the field mapping has no index field.
	 */
	public IField getIndexField4SbKzQuery();

	/**
	 * Sets the index field which controls the order of the field list.
	 */
	public void setIndexField4SbKzQuery(IField field);

}
