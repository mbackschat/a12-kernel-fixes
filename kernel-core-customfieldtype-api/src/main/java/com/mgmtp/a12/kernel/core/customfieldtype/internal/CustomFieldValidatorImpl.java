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
package com.mgmtp.a12.kernel.core.customfieldtype.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldType;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeConversionResult;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;

import lombok.NonNull;

/**
 * @deprecated since versin 30.2.0. Will be removed with A12K-3589. With the removal of {@link ICustomFieldType} this
 * 		class will be obsolete.
 */
@Deprecated(since = "30.6.0", forRemoval = true)
@SuppressWarnings("removal")
public class CustomFieldValidatorImpl implements ICustomFieldValidator {

	private final ICustomFieldType orig;

	public CustomFieldValidatorImpl(ICustomFieldType cft) {
		orig = cft;
	}

	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(@NonNull String value,
			@NonNull ICustomFieldTypeValidationParam valParam, boolean isDisplayValue, @NonNull Map<String, Object> configData) {
		return orig.validate(value, valParam, isDisplayValue, configData);
	}

	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(@NonNull String value,
			@NonNull ICustomFieldTypeValidationParam valParam, boolean isDisplayValue) {
		return validate(value, valParam, isDisplayValue, Collections.emptyMap());
	}

	@Override
	public @NonNull ICustomFieldTypeConversionResult convertDisplay2Internal(@NonNull String displayValue,
			@NonNull Map<String, Object> configData) {
		return orig.convertDisplay2Internal(displayValue, configData);
	}

	@Override
	public @NonNull ICustomFieldTypeConversionResult convertInternal2Display(@NonNull String internalValue,
			@NonNull Map<String, Object> configData) {
		return orig.convertInternal2Display(internalValue, configData);
	}
}
