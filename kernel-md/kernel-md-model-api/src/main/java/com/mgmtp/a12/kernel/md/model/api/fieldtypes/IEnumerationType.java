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
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * For {@link IField}s containing enumerable values.
 */
@OnlyForUsage
public interface IEnumerationType extends IFieldType {

	/**
	 * An enumeration value consists of the actual value and its localized representations.
	 */
	interface IEnumValue extends Serializable {

		/**
		 * @return the value. Never {@code null}.
		 */
		@NonNull String getValue();

		/**
		 * @return the label of this enum value or an empty {@link ILocalizedTextMap} if no label
		 * 		exists.
		 */
		@NonNull ILocalizedTextMap getLabel();
	}

	/**
	 * An enumeration category consists of its name, description and values.
	 */
	interface IEnumCategory extends Serializable {

		/**
		 * @return the name of the category.
		 */
		@NonNull String getName();

		/**
		 * @return the description of the category.
		 */
		Optional<String> getDescription();

		/**
		 * @return the values of the category. The order of the list corresponds to the values given
		 * 		by {@link IEnumCategory#getValues()}.
		 */
		@NonNull List<String> getValues();

	}

	/**
	 * @return the values of this enumeration. Not {@code null}.
	 */
	@NonNull List<IEnumValue> getValues();

	/**
	 * @return the error message of this enumeration. Is never {@code null}. Empty
	 *        {@link ILocalizedTextMap} implies the usage of a default error message.
	 */
	@NonNull ILocalizedTextMap getErrorMessage();

	/**
	 * The list of the category values uses the ordering of the list returned by
	 * {@link #getValues()} and must provide a non-empty and non-null value for list entry.
	 * Never returns {@code null}.
	 * <p>
	 * Example: if {@link #getValues()} returns ["a","b","c"] the method has to return only
	 * lists with three values: e.g. ("greek_category", ["alpha", "beta", "gamma"]), ("tree_view",
	 * ["1.1", "1.2", "1.3"])
	 *
	 * @return the defined categories for the enumeration. For more information see
	 *        {@link IEnumCategory}. Never {@code null}. If no categories are defined, the
	 * 		returned list is empty.
	 */
	@NonNull List<IEnumCategory> getCategories();

	/**
	 * This property specifies, whether on client side, the values of the enumeration should be
	 * sorted alphabetically.
	 * This property does not affect the order of {@link #getValues()}.
	 *
	 * @return whether or not the values should be sorted alphabetically at client.
	 */
	boolean isAlphabeticalSorting();

	/**
	 * Returns the s value, if specified, otherwise an empty Optional.
	 * The s value ('s' stands for special) specifies the value that is set for the field during
	 * validation if the field is not filled and only one repetition of the corresponding group is
	 * specified in the data record.
	 * The field is automatically filled with this value, if specified, at runtime (for
	 * prerequisites and further information see the description on the field type Enumeration in
	 * the <i>A12 Validation Language Documentation</i> section <i>Type definitions: abstracting
	 * field definitions</i>.
	 *
	 * @return the s value
	 */
	Optional<String> getsValue();

}
