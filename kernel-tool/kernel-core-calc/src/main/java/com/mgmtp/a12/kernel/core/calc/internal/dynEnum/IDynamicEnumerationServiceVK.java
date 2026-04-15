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

package com.mgmtp.a12.kernel.core.calc.internal.dynEnum;

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IDynamicEnumerationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IDynamicEnumerationService;

/**
 * The interface <code>IDynamicEnumerationServiceVK</code> defines methods to
 * <ul>
 * <li>check the consistency of a rule for dynamic enumerations, and</li>
 * <li>extract the information for dynamic enumerations to be passed to the code generator.</li>
 * </ul>
 * <p>
 * To prepare the calculation info for code generation from a set of rules,
 * <ol>
 * <li>IRule-Objects need to be collected for rules relevant for dynamic enumerations.</li>
 * <li>Then all the rules should be checked and the information for dynamic enumerations extracted
 * by {@link #checkAndExtractAllDynEnumInfo(List, IProblemReporter)}</li>
 * </ol>
 */
public interface IDynamicEnumerationServiceVK extends IDynamicEnumerationService {

	/**
	 * Checks, whether the given rule is valid as a dynamic enumeration rule.
	 *
	 * @return <code>true</code>, if the rule specifies a correct calculation for dynamic
	 * 		enumerations or <code>false</code> otherwise.
	 */
	public boolean checkDynamicEnumerationRule(IRule calculationRule, IProblemReporter pr);

	/**
	 * Check the rules and extract the information regarding dynamic enumerations.
	 * <p>
	 * If an error occurs during the check or the conversion, an {@link Optional#empty()} is
	 * returned and an error is reported to the problem reporter.
	 * Else a list of the resulting {@link IDynamicEnumerationInfo} is returned.
	 *
	 * @param rules
	 * 		the rules to check and transform.
	 * @param pr
	 * 		the problem reporter, errors are reported to.
	 * @return either {@link Optional#empty()}, if an error occurred or the list of
	 *        {@link IDynamicEnumerationInfo}.
	 */
	public Optional<List<IDynamicEnumerationInfo>> checkAndExtractAllDynEnumInfo(List<IRule> rules, IProblemReporter pr);
}
