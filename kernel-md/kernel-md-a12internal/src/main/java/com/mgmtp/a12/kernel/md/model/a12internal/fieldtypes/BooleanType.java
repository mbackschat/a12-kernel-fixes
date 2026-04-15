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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IBooleanType;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;
import com.mgmtp.a12.model.header.Annotation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * see {@link IBooleanType}
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@OnlyForA12InternalUsage
public class BooleanType extends FieldType {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @param notInDCustomTrueValue
	 * 		true values other than 'true'. See also
	 *        {@link IBooleanType#getNotInDCustomTrueValue()}
	 */
	private String notInDCustomTrueValue;

	/**
	 * @param notInDCustomFalseValue
	 * 		false values other than 'false'. See also
	 *        {@link IBooleanType#getNotInDCustomFalseValue()}
	 */
	private String notInDCustomFalseValue;

	@Builder(builderClassName = "BooleanTypeBuilder")
	private BooleanType(@NonNull List<Annotation> annotations, String notInDCustomTrueValue, String notInDCustomFalseValue) {
		super(annotations);
		this.notInDCustomTrueValue = notInDCustomTrueValue;
		this.notInDCustomFalseValue = notInDCustomFalseValue;
	}

	public static class BooleanTypeBuilder {
		private List<Annotation>  annotations = new ArrayList<>(); // in order to make BooleanTypeBuilder.annotations() optional
	}

	/**
	 * @return the true value other than 'true'. See also
	 *        {@link IBooleanType#getNotInDCustomTrueValue()}
	 */
	public Optional<String> getNotInDCustomTrueValue() {
		return Optional.ofNullable(notInDCustomTrueValue);
	}

	/**
	 * @return the false value other than 'false'. See also
	 *        {@link IBooleanType#getNotInDCustomFalseValue()}
	 */
	public Optional<String> getNotInDCustomFalseValue() {
		return Optional.ofNullable(notInDCustomFalseValue);
	}

}
