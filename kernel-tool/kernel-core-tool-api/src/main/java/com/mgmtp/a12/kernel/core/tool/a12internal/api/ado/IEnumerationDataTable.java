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

package com.mgmtp.a12.kernel.core.tool.a12internal.api.ado;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.tool.internal.EnumerationDataTable;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * This class is used to access CSV-Data. The getters should not be used outside kernel.
 */
@OnlyForUsage
public interface IEnumerationDataTable {

	static @NonNull IEnumerationDataTable of(@NonNull String name, byte[] csvBytes) {
		return new EnumerationDataTable(name, csvBytes);
	}

	/**
	 * @return a map from column name to the list of values for this column for the table.
	 */
	Map<String, List<String>> getColumns();

	/**
	 * @return the string representation of the table as a UTF-8 encoded byte array.
	 */
	byte[] getBytes();

	/**
	 * @param columnName
	 * 		the enumeration column name.
	 * @return <code>true</code> if there is a display column for the given enumeration column,
	 * 		<code>false</code> otherwise.
	 */
	boolean hasDisplayColumn(String columnName);

	/**
	 * @param enumColumnName
	 * 		the column name of the enumeration the category should be returned for.
	 * @param category
	 * 		the category name of the enumeration.
	 * @return the distinct values of the column corresponding to the specified category in the
	 * 		table.
	 */
	Set<String> getDistinctCategoryValues(String enumColumnName, String category);

	/**
	 * @param columnName
	 * 		the column name to return the distinct values for.
	 * @return the distinct values of the given column in the table.
	 */
	Set<String> getDistinctValues(String columnName);
}
