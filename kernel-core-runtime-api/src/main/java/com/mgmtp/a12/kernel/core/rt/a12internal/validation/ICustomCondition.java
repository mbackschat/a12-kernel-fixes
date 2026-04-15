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
package com.mgmtp.a12.kernel.core.rt.a12internal.validation;

import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;

/**
 * <p>
 * Interface for the implementation of conditions that cannot be expressed in the rule language.
 * </p>
 * Formerly known as <code>IApplicationCondition</code>.
 * <p>
 * The implementation of this interface must be passed by the caller for all custom
 * conditions that are used in the model type.
 * </p>
 * <p>
 * If a rule condition contains a custom condition, the method
 * {@link #check(IData, Set, Set, IIdentifier)} of the custom condition is called by the
 * runtime system when the rule is evaluated.
 * This custom condition must be passed at the {@link IValidator} interface with the name that
 * was used in the rule condition.
 * <br>
 * <u>Example</u>
 * <br>
 * If a rule condition uses a custom condition like
 * <br>
 * <code>CustomCondition WID_Invalid</code>,
 * <br>
 * the <code>check</code> method of the custom condition is called, which was passed at the
 * validation interface with the name <code>WID_Invalid</code>.
 * A {@link ValidatorException} is thrown, if no such custom condition was passed, even though it was used in a rule condition.
 * </p>
 */
// @formatter:off (the formatter keeps adding indents in the comments below on every save)
public interface ICustomCondition {

	/**
	 * This method implements a custom condition. It must return <i>false</i>, if the condition
	 * is not met or - in case of a partial validation - if it cannot be evaluated with regard to
	 * the passed relevant entities.
	 * <p>
	 * The method will only be called if all relevant fields are formally correct.
	 * </p>
	 * <p>
	 * <b>Note:</b> A custom condition must always return <i>false</i>, if the
	 * fields to be validated are not relevant fields for the validation.
	 * </p>
	 * The method may not change the data or the set of relevant entities. The method must be
	 * thread-safe.
	 *
	 * @param data
	 * 		the user data which has to be validated
	 * @param relevantEntities <ul>
	 * 		<li>for partial validation with the method
	 * 		{@link IValidator#validatePartlyWith3ValueLogic(IData, Set, Map, IResult, ValidationMode)} the set of relevant entities
	 * 		(fields and/or groups) is transferred analogously. See also: The use of wildcards (*) for index specifications.
	 * 		<b>Note:</b> Relevant groups passed to <code>checkPartlyWith3ValueLogic</code> are not expanded beforehand.</li>
	 * 		<li>for full validation: <code>null</code></li>
	 * 	</ul>
	 * @param formallyIncorrectFields
	 * 		the set of fields with formal errors. In the runtime system, erroneous fields are
	 * 		marked as not relevant.
	 * @param errorField
	 * 		the error field of the rule. If rule iteration is used, the indices have to
	 * 		specify a concrete value.
	 * 		If used in conjunction with a parallel iteration, an index value may, in special
	 * 		cases, also be {@value IIdentifier#UNKNOWN}. In these cases, a concrete index
	 * 		for this layer of the error field cannot be assigned.
	 * @return <code>true</code>, if the condition was met or <code>false</code>, if it was not met
	 * 		or could not be evaluated. <code>False</code> must also be returned for partly
	 * 		validations of non-relevant or erroneous fields (formal error).
	 *
	 * @throws CustomConditionException
	 * 		if an Exception should be thrown through to the {@link IValidator} interface methods. It is basically an integration
	 * 		error to throw any other exception here. But if any other Exception is thrown, it is caught and converted to a
	 * 		validation error (for the first repetition of the error field)., This is to enable the user to at least get some error
	 * 		messages and go on with editing and maybe fix the problem on his own.
	 */
	boolean check(
			final IData data,
			final Set<IIdentifier> relevantEntities,
			final Set<IIdentifier> formallyIncorrectFields,
			final IIdentifier errorField);
}
