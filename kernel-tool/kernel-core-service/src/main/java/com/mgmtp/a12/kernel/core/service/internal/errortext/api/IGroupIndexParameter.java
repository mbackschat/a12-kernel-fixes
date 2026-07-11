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
 * Represents the error text parameters $#&lt;group&gt;$ and $#&lt;group&gt; Fuer &lt;semantic
 * index&gt;$ ($#&lt;group&gt; For &lt;semantic index&gt;$) for the group index.
 */
public interface IGroupIndexParameter extends IRepetitionIndexParameter {

	/**
	 * @return the name of the group as referenced in the error text parameter or null if the rule
	 * 		group is referenced by the keyword 'RuleGroup' ('RegelKontext').
	 */
	public String getGroup();

	/**
	 *
	 * @return the fullname of the referenced group
	 */
	public String getGroupFullName();

	/**
	 * @return the semantic index, or null if no semantic index is specified
	 */
	public ISemanticIndex getSemanticIndex();

	/**
	 * Set the semantic index. Used for changing error texts
	 * after moving or renaming groups or fields. Only relevant, if the semantic index is specified
	 * by a field.
	 *
	 * @param semanticIndex
	 */
	public void setSemanticIndex(ISemanticIndex semanticIndex);
}
