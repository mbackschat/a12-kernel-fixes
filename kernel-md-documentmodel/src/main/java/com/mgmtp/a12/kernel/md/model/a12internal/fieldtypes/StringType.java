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
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.model.internal.impl.HintLists;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * For {@link Field}s containing {@link String} values.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StringType extends FieldType {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return the hint list for each locale or an empty list if no hint lists are specified.
	 */
	private @NonNull HintLists hintList = new HintLists();

	/**
	 * @param minLength
	 * 		the new minimum length required for the string. May be {@code null} to
	 * 		unset.
	 */
	private Integer minLength;
	/**
	 * @param maxLength
	 * 		the new maximum length required for the string. May be {@code null} to
	 * 		unset.
	 */
	private Integer maxLength;
	/**
	 * @return {@code true} if line breaks are allowed in the string. Otherwise {@code false}.
	 * @param lineBreaksPermitted
	 * 		use {@code true} if line breaks should be allowed for the string. Otherwise
	 *        {@code false}.
	 */
	private boolean lineBreaksPermitted = false;
	/**
	 * @return {@code true} if the given value for the field should <b>not</b> be checked while
	 * 		validating the data at runtime (usually this is used for attached files). Otherwise
	 *        {@code false}.
	 * @param noValueValidation
	 * 		use {@code true} if the given value for the field should <b>not</b> be checked
	 * 		while validating the data at runtime (usually this is used for attached files).
	 * 		Otherwise {@code false}.
	 */
	private boolean noValueValidation = false;
	/**
	 * @param pattern
	 * 		the new pattern which should be fulfilled by the string. May be {@code null}
	 * 		to
	 * 		unset.
	 */
	private String pattern;
	/**
	 * @return the defined error message in different {@link Locale}s which will be reported if the
	 * 		defined pattern (see {@link #getPattern()}) is violated. Is never
	 *        {@code null}. If no error messages are defined, the {@link ILocalizedTextMap} is
	 * 		empty.
	 * @param errorMessage
	 * 		the new error message in different {@link Locale}s which should be reported if the
	 * 		defined pattern (see {@link #getPattern()}) is violated. May not be {@code null}.
	 * 		Use an empty {@link ILocalizedTextMap} to unset.
	 */
	private @NonNull ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build();
	/**
	 * For future use only.
	 *
	 * @return see {@link IStringType#isAlphabeticalSorting()}.
	 * @param alphabeticalSorting
	 * 		use {@code true} if the values (coming e.g. from an external or dynamic
	 * 		enumeration) should be sorted alphabetically at client. Otherwise {@code false}.
	 */
	private boolean alphabeticalSorting = false;

	@Builder(toBuilder = true, builderClassName = "StringTypeBuilder")
	private StringType(Integer minLength, Integer maxLength, boolean lineBreaksPermitted, boolean noValueValidation,
			String pattern, @NonNull ILocalizedTextMap errorMessage, boolean alphabeticalSorting,
			@NonNull List<HintList> hintList) {
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.lineBreaksPermitted = lineBreaksPermitted;
		this.noValueValidation = noValueValidation;
		this.pattern = pattern;
		this.errorMessage = errorMessage;
		this.alphabeticalSorting = alphabeticalSorting;
		this.hintList = new HintLists(hintList);
	}

	/**
	 * @return the new minimum length required for the string.
	 */
	public Optional<Integer> getMinLength() {
		return Optional.ofNullable(minLength);
	}

	/**
	 * @return the new maximum length required for the string.
	 */
	public Optional<Integer> getMaxLength() {
		return Optional.ofNullable(maxLength);
	}

	/**
	 * @return the defined pattern which should be fulfilled by the string.
	 */
	public Optional<String> getPattern() {
		return Optional.ofNullable(pattern);
	}

	public static class StringTypeBuilder {
		private boolean lineBreaksPermitted = false; // we set explicit the default values
		private boolean noValueValidation = false; // we set explicit the default values
		private ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build(); // in order to make GroupBuilder.errorMessage() optional
		private boolean alphabeticalSorting = false;
		private List<HintList> hintList = new ArrayList<>();
	}

	/**
	 * Definition of a hint list for the {@link StringType}.
	 */
	@Data
	@Builder(toBuilder = true, builderClassName = "HintListBuilder")
	@NoArgsConstructor
	@AllArgsConstructor
	public static class HintList implements Serializable {
		private @NonNull Locale locale;
		@Builder.Default
		private @NonNull List<String> values = new ArrayList<>();

		public static class HintListBuilder {
		}
	}
}
