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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * The interface <code>ICalculationService</code> defines methods to
 * <ul>
 * <li>check the consistency of a calculation rule,</li>
 * <li>create the calculation text for a rule, and</li>
 * <li>extract the calculation information to be passed to the code generator.</li>
 * </ul>
 * <p>
 * To prepare the calculation info for code generation from a set of rules,
 * <ol>
 * <li>ICalculationRule-Objects need to be created for all relevant rules.</li>
 * <li>These should be grouped by the calculated field.</li>
 * <li>Then all the rules should be checked by
 * {@link #checkCalculationRule(ICalculationRule, List, IProblemReporter)}, using the grouped rules
 * in the second parameter.</li>
 * <li>Then the calculationInfo can be extracted via
 * {@link #extractCalculationInfo(ICalculationRule, IProblemReporter)}</li>
 * </ol>
 */
@OnlyForUsage
public interface IBaseCalculationService {

	public enum RoundingMode {
		EXACT("Exact"),
		ROUND_UP("RoundUp"),
		ROUND_DOWN("RoundDown");

		private final String internalValue;

		private RoundingMode(String internalValue) {
			this.internalValue = internalValue;
		}

		@Override
		public String toString() {
			return internalValue;
		}

		public static RoundingMode getByInternalValue(String internalValue) {
			return Arrays.stream(RoundingMode.values())
					.filter(rm -> rm.internalValue.equals(internalValue))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException(internalValue + " is not a valid RoundingMode."));
		}
	}

	/**
	 * Represents a calculation rule consisting of the rule itself and the rounding mode to be used
	 * for the calculation.
	 */
	public interface ICalculationRule {
		/**
		 * @return the rule specifying the calculation.
		 */
		public IRule getRule();

		/**
		 * @return the rounding mode to be used for the calculation.
		 */
		public RoundingMode getRoundingMode();

		/**
		 * @return the field, whose value will be calculated by the rule.
		 */
		public IField getCalculatedField();
	}

	/**
	 * Returns the calculation text for the given rule and rounding mode.<br>
	 * This text includes the information of the current rule only.
	 * To get the full calculation text for a field, you have to group the calculation rules by
	 * calculated field (= error field) and concatenate the calculation texts.
	 *
	 * @param calculationRule
	 * 		the calculation rule.
	 * @param pr
	 * 		the {@link IProblemReporter}, which will accept the resulting errors.
	 * @return the calculation text of the specified rule, if the calculation is correct;
	 *        {@link Optional#empty()} otherwise.
	 */
	public Optional<String> getCalculationTextForRule(ICalculationRule calculationRule, IProblemReporter pr);

	/**
	 * Checks, whether the given rule is valid for the specified rounding mode and field. The rule
	 * is tested in the context of the other rules specifying calculations for the same field.
	 *
	 * @param calculationRule
	 * 		the calculation rule being validated. Must not be <code>null</code>.
	 * @param fieldCalculations
	 * 		a list of calculation rules that contains all calculations for the same field
	 * 		together with the corresponding rounding mode. The checked rule may or may not be
	 * 		contained in the list. May be empty but not <code>null</code>.
	 * @param pr
	 * 		the {@link IProblemReporter}, which will receive the resulting errors.
	 * 		May be <code>null</code>, if the error messages are not relevant.
	 * @return <code>true</code>, if the rule specifies a correct calculation or <code>false</code>
	 * 		otherwise.
	 */
	public boolean checkCalculationRule(ICalculationRule calculationRule, List<ICalculationRule> fieldCalculations,
			IProblemReporter pr);

	/**
	 * Precondition: The <code>condition</code> has to be expanded, i.e. the condition text has to
	 * be parsed with ExpandMode#EXPAND.
	 * Extracts an {@link ICalculationInfo} object from the given {@link ICalculationRule} and
	 * {@link ICondition}.<br>
	 * <p>
	 * This method is designed to be used in conjunction with
	 * {@link #checkCalculationRule(ICalculationRule, List, IProblemReporter)}. For the extraction
	 * of the complete list of {@link ICalculationInfo} objects for code generation from the
	 * {@link ICalculationRule} objects, use
	 * {@link #checkAndExtractAllCalculationInfo(List, IProblemReporter)}.
	 *
	 * @param cr
	 * 		the {@link ICalculationRule} object used to create the calculation information.
	 * @param pm
	 * 		the {@link IProblemReporter}, which will receive the resulting errors.
	 * @return the {@link ICalculationInfo} object created from the given parameters.
	 */
	public Optional<ICalculationInfo> extractCalculationInfo(ICalculationRule cr, IProblemReporter pm);

	/**
	 * Check the calculation rules and extract the calculation information.
	 * <p>
	 * If an error occurs during the check or the conversion, an {@link Optional#empty()} is
	 * returned and an error is reported to the problem reporter.
	 * Else a list of the resulting {@link ICalculationInfo} is returned.
	 *
	 * @param calcRules
	 * 		the rules to check and transform.
	 * @param pr
	 * 		the problem reporter, errors are reported to.
	 * @return either {@link Optional#empty()}, if an error occurred or the list of
	 *        {@link ICalculationInfo}.
	 */
	public Optional<List<ICalculationInfo>> checkAndExtractAllCalculationInfo(List<ICalculationRule> calcRules,
			IProblemReporter pr);

	public Optional<String> getCalculationTextForRule(ICalculationRule calculationRule, ICondition condition, IProblemReporter pr);
}
