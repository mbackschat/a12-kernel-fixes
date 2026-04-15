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

package com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService.RoundingMode;

/**
 * An instance of the interface ICalculationInfo collects the information necessary to represent the
 * calculation of a field.
 * <p>
 * A calculation consists of
 * <ul>
 * <li>the calculated field, the result of the operation will be assigned to,</li>
 * <li>a list of partial calculations, which consist of a precondition and a corresponding
 * operation,</li>
 * <li>a rounding mode, a corresponding rounding operation will be applied to the result of the
 * operation before assignment,</li>
 * <li>and a name, that is needed for some output operations. If the calculation is specified by a
 * rule, usually the rule name is used.</li>
 * </ul>
 * <p>
 * During the evaluation of the calculation, first the preconditions are examined in turn. If a
 * precondition evaluates to <code>true</code>, the corresponding operation is performed on the
 * data. The result of the operation will then be assigned to the calculated field after rounding
 * according to the rounding mode.
 */
public interface ICalculationInfo {

	/**
	 * @return the calculated field.
	 */
	IEntityId getCalculatedField();

	/**
	 * Return the individual partial calculations.
	 * <p>
	 * Each of the partial calculations consists of a precondition and a corresponding calculation
	 * operation.
	 * <p>
	 * The generated code will test the individual preconditions and will choose the corresponding
	 * calculation operation for the one that succeeds.
	 *
	 * @return the partial calculations.
	 */
	List<Pair<ICondition, IOperation>> getPartialCalculations();

	Optional<ICondition> getCommonPrecondition();

	/**
	 * @return the rounding mode.
	 */
	RoundingMode getRoundingMode();

	/**
	 * @return the name of the source for the calculation info. Used for output.
	 */
	String getName();

}
