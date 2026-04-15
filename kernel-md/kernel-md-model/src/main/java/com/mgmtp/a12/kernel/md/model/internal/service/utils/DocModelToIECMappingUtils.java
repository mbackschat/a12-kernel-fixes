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

package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateType.PartlyKnownDateType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.JsVkCodeGenConfig;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType.DatePrecision;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig.JsCodeGenConfig;

public class DocModelToIECMappingUtils {

	private DocModelToIECMappingUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static PartlyKnownDateType convertDatePrecision(DatePrecision datePrecision) {
		switch (datePrecision) {
			case FULL:
				return PartlyKnownDateType.BEKANNT;
			case DAY_OPTIONAL:
				return PartlyKnownDateType.TAG_OPTIONAL;
			case MONTH_OPTIONAL:
				return PartlyKnownDateType.MONAT_OPTIONAL;
			case YEAR_OPTIONAL:
				return PartlyKnownDateType.JAHR_OPTIONAL;
			default:
				throw new IllegalArgumentException("Unknown date precision " + datePrecision.name());
		}

	}

	public static JsVkCodeGenConfig convertJsCodeGenConfig(JsCodeGenConfig jsCodeGenConfig) {
		switch (jsCodeGenConfig) {
			case STANDARD:
				return JsVkCodeGenConfig.STANDARD;
			case WRAPPED_IN_FUNCTION:
				return JsVkCodeGenConfig.WRAPPED_IN_FUNCTION;
			case WRAPPED_IN_FUNCTION_EXTENDED:
				return JsVkCodeGenConfig.WRAPPED_IN_FUNCTION_EXTENDED;
			default:
				throw new IllegalArgumentException("Unknown config for the javascript code generation " + jsCodeGenConfig.name());
		}
	}

}
