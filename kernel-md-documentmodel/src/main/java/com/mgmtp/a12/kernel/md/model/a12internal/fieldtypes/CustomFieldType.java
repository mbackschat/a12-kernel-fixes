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

import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * For {@link Field}s which model a custom data type. For example, in ExtMP there are several custom types: EMAIL, IBAN, BIC, etc.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomFieldType extends FieldType {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return the name of the custom type. Is never {@code null} or empty.
	 * @param name
	 * 		the new name of the custom data type. May not be {@code null} or empty.
	 */
	private @NonNull String name;

	/**
	 * @param displayName
	 * 		the new non-technical name of the custom data type. May be {@code null} to unset.
	 */
	private String displayName;
	/**
	 * @param minLength
	 * 		the new minimum length required for the (string) value. May be {@code null} to
	 * 		unset.
	 */
	private Integer minLength;
	/**
	 * @param maxLength
	 * 		the new maximum length required for the (string) value. May be {@code null} to
	 * 		unset.
	 */
	private Integer maxLength;

	@Builder(builderClassName = "CustomFieldTypeBuilder")
	private CustomFieldType(@NonNull String name, String displayName, Integer minLength,
			Integer maxLength) {
		this.name = name;
		this.displayName = displayName;
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	/**
	 * @return the non-technical name of the custom data type.
	 */
	public Optional<String> getDisplayName() {
		return Optional.ofNullable(displayName);
	}

	/**
	 * @return the new minimum length required for the (string) value.
	 */
	public Optional<Integer> getMinLength() {
		return Optional.ofNullable(minLength);
	}

	/**
	 * @return the new maximum length required for the (string) value.
	 */
	public Optional<Integer> getMaxLength() {
		return Optional.ofNullable(maxLength);
	}

	public static class CustomFieldTypeBuilder {
	}

}
