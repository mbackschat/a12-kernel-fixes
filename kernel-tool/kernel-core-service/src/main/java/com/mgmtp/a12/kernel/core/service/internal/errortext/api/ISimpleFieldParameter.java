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

package com.mgmtp.a12.kernel.core.service.internal.errortext.api;

/**
 * Represents the error text parameters $&lt;field&gt;$ and $&lt;field&gt;...$ where ... may be
 * '.value' ('.Wert') or a category reference ('->&lt;categoryname&gt;') and/or a semantic index.
 */
public interface ISimpleFieldParameter extends IFieldParameter {

	/**
	 * @return the semantic index, or null if no semantic index is specified
	 */
	ISemanticIndex getSemanticIndex();

	void setSemanticIndex(ISemanticIndex semanticIndex);
	//TODO[A12K-3890]: check whether this setter is still needed, after the FehlertextUtil is cleaned from orphaned methods.

	/**
	 * @return concrete root group index (lfd. Nr.) as specified in the error text parameter, or
	 * 		null if no root group index is specified. Only relevant in H models.
	 */
	public String getRootgroupIndex();

	/**
	 * @return concrete row index as specified in the error text parameter, or null if no row index
	 * 		is specified. Only relevant in H models.
	 */
	public String getRowIndex();

}
