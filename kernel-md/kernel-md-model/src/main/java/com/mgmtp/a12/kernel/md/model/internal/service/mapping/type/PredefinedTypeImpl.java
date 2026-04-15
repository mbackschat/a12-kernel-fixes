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

package com.mgmtp.a12.kernel.md.model.internal.service.mapping.type;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IPredefinedType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

import lombok.Builder;

/**
 *
 */
@Builder
public class PredefinedTypeImpl implements IPredefinedType {

	private String name;
	@Builder.Default
	private int minLength = 1;
	@Builder.Default
	private int maxLength = IFieldType.UNDEFINED;
	@Builder.Default
	private String pattern = null;
	@Builder.Default
	private Map<ProgrammingLanguage, ValidationStrategy> validationConfig = new EnumMap<>(ProgrammingLanguage.class);
	@Builder.Default
	private Map<Locale, String> errorMessages = new HashMap<>();

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getMinLength() {
		return minLength;
	}

	@Override
	public int getMaxLength() {
		return maxLength;
	}

	@Override
	public Optional<String> getPattern() {
		return Optional.ofNullable(pattern);
	}

	@Override
	public Optional<ValidationStrategy> getValidationConfig(ProgrammingLanguage plang) {
		return Optional.ofNullable(validationConfig.get(plang));
	}

	@Override
	public String getErrorMessage(Locale loc) {
		return FieldTypeMappingUtils.getErrorMessage(loc, errorMessages);
	}
}
