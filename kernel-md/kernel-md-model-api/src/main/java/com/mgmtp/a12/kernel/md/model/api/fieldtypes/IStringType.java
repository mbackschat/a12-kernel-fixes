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
package com.mgmtp.a12.kernel.md.model.api.fieldtypes;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * For {@link IField}s containing {@link String} values.
 */
@OnlyForUsage
public interface IStringType extends IFieldType {

	/**
	 * A hint list consists of the given locale and its according list of hints (values).
	 */
	interface IHintList extends Serializable {

		Locale getLocale();

		/**
		 * @return the list of the hints defined for the {@link Locale} returned in {@link #getLocale()}. The list is empty if no
		 * 		hints were defined for the locale.
		 */
		List<String> getValues();

	}

	/**
	 * @return the hint list for each locale or an empty list if no hint lists are specified.
	 */
	List<IHintList> getHintList();

	/**
	 * @return the minimal length of the string value or an empty Optional instance if no minimal
	 * 		length is specified.
	 */
	Optional<Integer> getMinLength();

	/**
	 * @return the maximum length of the string value or an empty Optional instance if no maximum
	 * 		length is specified.
	 */
	Optional<Integer> getMaxLength();

	/**
	 * @return whether or not line breaks are allowed for the string value.
	 */
	boolean isLineBreaksPermitted();

	/**
	 * @return whether or not the value of this string should be validated.
	 */
	boolean isNoValueValidation();

	/**
	 * @return the pattern that must be matched by the string value or an empty Optional instance if
	 * 		no pattern is specified.
	 */
	Optional<String> getPattern();

	/**
	 * @return the error message that will be display if the pattern is violated or an empty
	 *        {@link ILocalizedTextMap} if no error message is specified.
	 */
	ILocalizedTextMap getErrorMessage();

	/**
	 * For future use.
	 * This property specifies, whether on client side, the values of a dynamic or external
	 * enumeration based on this string type, should be sorted alphabetically.
	 *
	 * @return whether or not the values (coming e.g. from an external or dynamic enumeration)
	 * 		should be sorted alphabetically at client.
	 */
	boolean isAlphabeticalSorting();

}
