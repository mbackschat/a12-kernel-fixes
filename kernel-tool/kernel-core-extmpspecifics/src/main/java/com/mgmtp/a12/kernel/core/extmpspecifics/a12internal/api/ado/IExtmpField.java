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

package com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.ado;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

public interface IExtmpField extends IField, IExtmpEntity {

	/**
	 * @return the list of the applications for which the restriction applies (see also
	 *        {@link RestrictionType}). If no application restriction is defined, an empty list is
	 * 		returned.
	 */
	public List<String> getRestrictionApps();

	/**
	 * @return the type of restriction. If no restriction is defined {@link RestrictionType#None}
	 * 		is returned.
	 */
	public RestrictionType getRestrictionType();

	/**
	 * @return the sachbereich/kennzahl pair mapped by the field indices (i.e. in case of h models: root
	 * 		group repetition and row index).
	 */
	public default Map<List<Long>, Pair<String, String>> getSbKzMap() {
		return new HashMap<>();
	}

	/**
	 * @return <code>true</code> if all sb/kz (if provided) of the first repetition of the root group
	 * 		(in which the field exists) shall be used for all repetitions of the root group.
	 * 		Otherwise <code>false</code>.
	 */
	public default boolean isKennzahlFlag() {
		return false;
	}

}
