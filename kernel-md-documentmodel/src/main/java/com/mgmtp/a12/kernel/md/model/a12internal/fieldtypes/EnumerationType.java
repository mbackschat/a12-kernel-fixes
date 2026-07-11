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

package com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.LocalizedTextMapBuilder;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.internal.impl.EnumValues;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * For {@link Field}s containing predefined string values (enumeration).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EnumerationType extends FieldType {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * Definition of an enumeration value for the {@link EnumerationType}.
	 */
	@Data
	@Builder(toBuilder = true, builderClassName = "EnumValueBuilder")
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EnumValue implements Serializable {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		/**
		 * @return the defined internal enumeration value. Is never {@code null} or blank.
		 * @param value
		 * 		the new internal enumeration value. May not be {@code null} or blank.
		 */
		private @NonNull String value;
		/**
		 * @return the label in different {@link Locale}s for this enumeration value. Is
		 * 		never {@code null}. If no labels were defined, the {@link ILocalizedTextMap} is
		 * 		empty.
		 * @param label
		 * 		the new label in different {@link Locale}s for this enumeration value.
		 * 		May not be {@code null}. Use an empty {@link ILocalizedTextMap} to unset.
		 */
		@Builder.Default
		private @NonNull ILocalizedTextMap label = new LocalizedTextMapBuilder().build();

		public static class EnumValueBuilder {
			@SuppressWarnings("unused")
			private ILocalizedTextMap label = new LocalizedTextMapBuilder().build(); // in order to make EnumValueBuilder.label() optional
		}

	}

	/**
	 * Definition of the content of a category for an {@link EnumerationType}.
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class EnumCategory implements Serializable {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		/**
		 * @return the category name. Is never {@code null} or empty.
		 * @param name
		 * 		the new category name. May not be {@code null} or empty.
		 */
		private @NonNull String name;
		/**
		 * @param description
		 * 		the new description of the category. May be {@code null} to unset.
		 */
		private String description;
		/**
		 * @return the defined values for the category. This list is coupled to the list in
		 *        {@link EnumerationType#getValues()}: This means the first category value in this
		 * 		list was assigned to the first enumeration value in
		 *        {@link EnumerationType#getValues()}. Is never {@code null}.
		 * @param values
		 * 		the new values for the category. May not be {@code null}.
		 */
		@Builder.Default
		private @NonNull List<String> values = new ArrayList<>();

		/**
		 * @return the description of the category.
		 */
		public Optional<String> getDescription() {
			return Optional.ofNullable(description);
		}
	}

	/**
	 * @return the defined values for the enumeration. For more information see {@link EnumValue}.
	 * 		Is never {@code null} or empty.
	 * @param values
	 * 		the new values for the enumeration. May not be {@code null} or empty.
	 */
	private @NonNull EnumValues values = new EnumValues();

	/**
	 * @return the defined categories for the enumeration. For more information see
	 *        {@link EnumCategory}. Is never {@code null}. If no categories are defined, the
	 * 		returned list is empty.
	 * @param categories
	 * 		the new categories for the enumeration. May not be {@code null}. Use an empty list
	 * 		to unset.
	 */
	private @NonNull List<EnumCategory> categories = new ArrayList<>();
	/**
	 * @return the error message in different {@link Locale}s which will be displayed at runtime in
	 * 		case the given value is not valid. Is never {@code null}. If no error messages were
	 * 		defined, the {@link ILocalizedTextMap} is empty.
	 * @param errorMessage
	 * 		the new error message in different {@link Locale}s. May not be {@code null}. Use
	 * 		an empty {@link ILocalizedTextMap} to unset.
	 */
	private @NonNull ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build();
	/**
	 * Specifies, whether the values should be sorted alphabetically on client side.
	 * This property does not affect the order of {@link #getValues()}.
	 *
	 * @return see {@link IEnumerationType#isAlphabeticalSorting()}.
	 * @param alphabeticalSorting
	 * 		use {@code true} if the values should be sorted alphabetically at client.
	 * 		Otherwise {@code false}.
	 */
	private boolean alphabeticalSorting = false;

	private String sValue;

	@Builder(toBuilder = true, builderClassName = "EnumerationTypeBuilder")
	private EnumerationType(@NonNull List<EnumValue> values, @NonNull List<EnumCategory> categories,
			@NonNull ILocalizedTextMap errorMessage, boolean alphabeticalSorting, String sValue) {
		this.values = new EnumValues(values);
		this.categories = categories;
		this.errorMessage = errorMessage;
		this.alphabeticalSorting = alphabeticalSorting;
		this.sValue = sValue;
	}

	public List<EnumValue> getValues() {
		return values;
	}

	public void setValues(@NonNull List<EnumValue> values) {
		this.values = new EnumValues(values);
	}

	/**
	 * @return see {@link IEnumerationType#getsValue()}
	 */
	public Optional<String> getsValue() {
		return Optional.ofNullable(sValue);
	}

	/**
	 * Value with which an index field of type Enumeration is automatically filled at runtime.
	 * For prerequisites and further information see the description on the field type Enumeration
	 * in the <i>A12 Validation Language Documentation</i> section <i>Type definitions: abstracting
	 * field definitions</i>.
	 *
	 * @param sValue
	 * 		the s value. May be {@code null} to unset.
	 */
	public void setsValue(String sValue) {
		this.sValue = sValue;
	}

	public static class EnumerationTypeBuilder {
		private List<EnumCategory> categories = new ArrayList<>(); // in order to make EnumerationTypeBuilder.categories() optional
		private ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build(); // in order to make EnumerationTypeBuilder.errorMessage() optional
		private boolean alphabeticalSorting = false;
	}

}
