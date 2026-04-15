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
package com.mgmtp.a12.kernel.core.tool.a12internal.api.services;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * The interface <code>IDynamicEnumerationService</code> will define methods to
 * <ul>
 * <li>check the consistency of a rule for dynamic enumerations, and</li>
 * <li>extract the information for dynamic enumerations to be passed to the code generator.</li>
 * </ul>
 */
@OnlyForUsage
public interface IDynamicEnumerationService {

	/**
	 * Checks, whether the given rule is valid as a dynamic enumeration rule.
	 * Convenience method to omit double parsing, when the parsing was already done.
	 *
	 * @param condition
	 * 		the already parsed rule condition. The condition has to be parsed by calling
	 *        {@link IRuleParser#parseConditionNoExpand(IRule, String, IProblemReporter)}. If the condition is <code>null</code>,
	 * 		the rule will be parsed and possible errors will be reported.
	 * @param rule
	 * 		the rule to be used as source for error messages. The rule error condition is not used in this call.
	 * @param pr
	 * 		the {@link IProblemReporter} taking occurring {@link IProblem}s.
	 * @return <code>true</code>, if the rule specifies a correct calculation for dynamic enumerations or <code>false</code>
	 * 		otherwise.
	 */
	boolean checkDynamicEnumerationRule(ICondition condition, IRule rule, IProblemReporter pr);
}
