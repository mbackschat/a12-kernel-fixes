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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.ProjectLanguageSettingsHelper;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectLanguageSettings;

@StringTemplateGroup("meta")
public class CodeGenProjectLanguageSettings extends STParameterClassBase {

	private final IProjectLanguageSettings projectLanguageSettings;

	public CodeGenProjectLanguageSettings(IProjectLanguageSettings pls) {
		this.projectLanguageSettings = pls;
	}

	public String getDezimalTrenner() {
		return String.valueOf(projectLanguageSettings.getDecimalSeparator());
	}

	public String getThousandsSeparator() {
		return projectLanguageSettings.getThousandsSeparator();
	}

	public String getDatumTrenner() {
		return ProjectLanguageSettingsHelper.extractDateSeparator(projectLanguageSettings.getDateFormat());
	}

	public String getDatumReihenfolge() {
		return ProjectLanguageSettingsHelper.extractDateOrder(projectLanguageSettings.getDateFormat());
	}

	public boolean getDatumNullOptional() {
		return projectLanguageSettings.isDateNullOptional();
	}

	public String getDatumbereichTrenner() {
		return String.valueOf(projectLanguageSettings.getDateRangeSeparator());
	}

	public String getDateTimeFormat() {
		return projectLanguageSettings.getDateTimeFormat();
	}

	public String getTimeFormat() {
		return projectLanguageSettings.getTimeFormat();
	}
}
