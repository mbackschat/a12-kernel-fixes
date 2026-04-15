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

import type {IData} from "./IData.js";
import type {IIdentifier} from "./IIdentifier.js";

/**
 * <p>
 * Interface for the implementation of conditions that cannot be expressed in the rule language.
 * </p>
 * <p>
 * The implementation of this interface must be passed by the caller for all custom
 * conditions that are used in the model type.
 * </p>
 *
 * If a rule condition contains an custom condition, the method
 * {@link check check(IData, Set, Set, IIdentifier)} of the custom condition is called by the
 * runtime system when the rule is evaluated.
 * This custom condition must be passed at the {@link IValidator} interface with the name that
 * was used in the rule condition.
 * <br>
 * <u>Example:</u>
 * <br>
 * If a rule condition uses an custom condition like
 * <br>
 * <code>CustomCondition WID_Invalid</code>,
 * <br>
 * the <code>check</code> method of the custom condition is called, which was passed at the
 * validation interface with the name <code>WID_Invalid</code>.
 * An Error is thrown, if no such custom condition was passed although it
 * was used in a rule condition.
 * </p>
 * <p>
 * <b>Attention: </b>The kernel validation and thus especially custom conditions (as part of it) are designed exclusively
 * for the validation of values supplied in {@link IData}. Checks that are independent of the {@link IData} should be implemented
 * at the appropriate extension point (e.g. in Form Engine). The interface is intended for fast, synchronous validations.
 * More time-consuming validations or those that require access to third-party systems should not be placed here.
 */
export interface ICustomCondition {
	/**
	 * The validation logic as a callback function.
	 * The method must not change the data or the set of relevant entities.
	 * The method must be thread-safe.
	 *
	 * @param data  the user data which has to be validated.
	 *            Must not be <code>null</code>.
	 * @param relevantEntities  all relevant entities for this check
	 *            <ul>
	 *                <li>full validation: <code>null</code> (all entities are relevant)</li>
	 *                <li>partial validation: the relevant entities
	 *                  that were passed to the validation call
	 *                  (see {@link IValidator.validatePartlyWith3ValueLogic}).
	 *              <b>Note:</b> Relevant groups passed to <code>validatePartlyWith3ValueLogic</code> are
	 *                  not expanded beforehand.</li>
	 *            </ul>
	 * @param formallyIncorrectFields  the entities that are formally incorrect.
	 *            In kernel runtime formal incorrect entities
	 *            are not relevant for further checks to avoid additional
	 *            errors. Therefore formal incorrect entities
	 *            should be ignored by the custom condition check.
	 *            Must not be <code>null</code>.
	 * @param errorField  the error identifier representing the error field
	 *            of the rule whose rule condition specifies this custom
	 *            condition.
	 *            <br><br>
	 *            Remark {@link IIdentifier.getIndices}:
	 *            <ul>
	 *            <li>used in an iteration: the repetitions specify concrete values.
	 *              The rule and therefore the check method is evaluated for each repetition
	 *              and contains the concrete index of the current repetition.
	 *            </li>
	 *            <li>used in a parallel iteration: an index value may, in special
	 *              cases be {@link IIdentifier.UNKNOWN}. In these cases, a concrete index
	 *              for this layer of the error field cannot be assigned.</li>
	 *            </ul>
	 *            Must not be <code>null</code>.
     *
     * @throws CustomConditionException
     *     		if an Exception should be thrown through to the {@link IValidator} interface methods. It is basically an integration
     *     		error to throw any other exception here. But if any other Exception is thrown, it is caught and converted to a
     *     		validation error (for the first repetition of the error field)., This is to enable the user to at least get some error
     *     		messages and go on with editing and maybe fix the problem on his own.
	 *
	 * @return <code>true</code>, if the condition was met, <code>false</code>, if<br>
	 *            a) one or more necessary fields are <em>not</em> included in relevantEntities<br>
	 *            b) one or more necessary fields are included in formallyIncorrectFields<br>
	 *            c) condition is not met<br>
	 *            <em><b>Note:</b> Conditions describe the error case, i.e. if <code>true</code> is returned here, the rule in
	 *              which this custom condition was specified reports an error.</em>
	 */
	check(
		data: IData,
		relevantEntities: Set<IIdentifier> | undefined,
		formallyIncorrectFields: Set<IIdentifier>,
		errorField: IIdentifier
	): boolean;
}
