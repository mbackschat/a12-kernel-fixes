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
 * Represents the error text parameters that reference a field:
 * <ul>
 * <li>{@link ISimpleFieldParameter}</li>
 * <li>{@link IIndexFieldParameter}</li>
 * </ul>
 */
public interface IFieldParameter extends IErrortextParameter {

	/**
	 * @return the name of the field as referenced in the error text parameter
	 */
	public String getEntityRefInErrorText();

	/**
	 * Set the name of the field. Used for changing error texts after moving or renaming groups or
	 * fields.
	 * WARNING: using this setter leaves the fieldParameter in inconsistent state, because the corresponding
	 * fullName is NOT updated accordingly
	 *
	 * @param field
	 * 		name of the field as referenced in the error text parameter
	 */
	public void setEntityRefInErrorText(String field);

	/**
	 * can be null, if parser is invoked without condition
	 *
	 * @return the fullname of the field, if determined by parser, or else null
	 */
	String getFieldFullName();

	/**
	 * @return true, if the value of the field is referenced with '.value' ('.Wert'), false
	 * 		otherwise. Can only return true, if {@link #getCategoryName()} returns null.
	 */
	public boolean isValueReference();

	/**
	 * @return name of the category, if the category of an enumeration field is referenced in the
	 * 		error text parameter, null otherwise. Can only be given, if
	 *        {@link #isValueReference()} returns false.
	 */
	public String getCategoryName();

}
