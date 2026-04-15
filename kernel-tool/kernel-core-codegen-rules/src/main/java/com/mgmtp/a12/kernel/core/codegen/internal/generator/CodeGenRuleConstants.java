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

package com.mgmtp.a12.kernel.core.codegen.internal.generator;

import java.util.Collections;
import java.util.Map;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

public interface CodeGenRuleConstants {

	String JAVA_JS_RT_STRING_USB_NICHT_ANGEGEBEN = "IIdentifier.USB_NOT_SPECIFIED";
	String JAVA_JS_RT_STRING_ALL_INDICES = "IIdentifier.ALL_INDICES";
	String JAVA_JS_RT_STRING_ITERATION = "IIdentifier.ITERATION";
	String JAVA_JS_RT_STRING_PARALLEL_ITERATION = "IIdentifier.PARALLEL_ITERATION";
	String JAVA_JS_RT_STRING_REP_NOT_UNQ_LEVEL_TO_REPLACE = "IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED";
	int NUM_RULES_PER_CLASS_GROOVY = 10;
	int NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY = 100;
	int NUM_INIT_PER_IDENTIFIERREF_ONE_CLOSURE_GROOVY = 2;
	int NUM_CONDITIONLINES_PER_METHOD_GROOVY = 50;
	int NUM_MAX_CONDITIONS_PER_CONDITIONBOOLEAN_GROOVY = 250;
	int NUM_RULES_PER_FILE_CPP = 250;
	Map<ICodeGeneratorParameters.ProgrammingLanguage, Integer> NUM_METARULES_PER_FILE = Collections.unmodifiableMap(
			Map.of(ICodeGeneratorParameters.ProgrammingLanguage.GROOVY, 500));
}
