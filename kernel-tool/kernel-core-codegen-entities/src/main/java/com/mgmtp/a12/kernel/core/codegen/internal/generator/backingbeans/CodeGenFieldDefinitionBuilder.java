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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporterProxy;

/**
 * Builder used to generate a {@link CodeGenFieldDefinition}.
 */
public class CodeGenFieldDefinitionBuilder extends CodeGenEREntitiesBuilder<CodeGenEntityData> {

	private final int internalNumber;
	private final IField field;
	private final boolean globalesPflichtfeld;
	private final boolean mandatoryForRepeatableGroup;
	private final Boolean isCarryoverField;
	private final Map<Locale, String> errorTexts4Req = new HashMap<>();

	public CodeGenFieldDefinitionBuilder(int internalNumber, CodeGenEntityData data, IField field, boolean globalesPflichtfeld,
			boolean mandatoryForRepeatableGroup, Boolean isCarryoverField, ProblemReporterProxy pr,
			PackageInformation packageInfo) {
		super(data, packageInfo, pr);
		this.internalNumber = internalNumber;
		this.field = field;
		this.globalesPflichtfeld = globalesPflichtfeld;
		this.mandatoryForRepeatableGroup = mandatoryForRepeatableGroup;
		this.isCarryoverField = isCarryoverField;
		var requirednessConfig = field.getRequirednessConfig();
		if (requirednessConfig.isPresent()) {
			for (Locale l : data.getParams().getLanguages()) {
				requirednessConfig.get().getErrorMessage(l).ifPresent(m -> errorTexts4Req.put(l, m));
			}
		}
	}

	@Override
	public CodeGenFieldDefinition build() {
		return new CodeGenFieldDefinition(internalNumber, this);
	}

	public IField getField() {
		return field;
	}

	public Boolean isCarryoverField() {
		return isCarryoverField;
	}

	public boolean isMandatoryField() {
		return globalesPflichtfeld;
	}

	public boolean isMandatoryForRepeatableGroup() {
		return mandatoryForRepeatableGroup;
	}

	public Map<Locale, String> getErrorTexts4Req() {
		return errorTexts4Req;
	}
}
