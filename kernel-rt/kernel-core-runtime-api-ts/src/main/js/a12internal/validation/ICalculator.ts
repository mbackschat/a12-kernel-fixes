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

import type { Logger } from "@com.mgmtp.a12.utils/utils-logging";

import type { IExternalCalculation } from "../custom/IExternalCalculation.js";

import type { ValidationMode } from "../ValidationMode.js";

import type { IData } from "./IData.js";
import type { IIdentifier } from "./IIdentifier.js";
import type { IResult } from "./IResult.js";

/**
 * Interface to calculations.
 */
export interface ICalculator {
	/**
	 * This method specifies the interface for <b>calculations</b>. The given data must contain the
	 * values necessary for the calculations. But they can also contain additional data. The values
	 * of calculated fields will be ignored in the input data.<br>
	 *
	 * Those fields of the input data that are operands of internal or external calculations
	 * will be validated syntactically and for uniqueness of index field values. This excludes all
	 * internally calculated fields and the non-operand externally calculated fields. If errors occur,
	 * they are saved to `formalErrors`. Regardless of the formal errors found, calculations will be
	 * performed. Any calculations relying directly or indirectly on invalid fields will be skipped.
	 *
	 * The calculations are performed in a pre-specified order (unless `externalCalculations` are given)
	 * corresponding to the dependencies of the calculated fields. If a calculation results in a value
	 * that is not correct syntactically (e.g. a value outside the valid range), a corresponding error
	 * message will be added to `formalErrors`. In this case, further calculations will only be
	 * performed, if they depend on that value neither directly nor indirectly.<br>
	 *
	 * The return value will contain all values of fields that could be calculated. If a field value
	 * was identified as not being syntactically correct, it will be returned nevertheless. The
	 * values will be returned in UI representation.<br>
	 *
	 * Note: fields, that were not calculated, will not be contained in the returned data set.
	 * Therefore, in addition to set returned values for the calculated fields, the caller
	 * eventually needs to reset values of calculated fields that are not returned.
	 *
	 * @param data 	the input data of the calculations. Must not be <code>null</code>.
	 * @param mode 	configures aspects of the calculations. May not be <code>null</code>.
	 * @param formalErrors 	container object for resulting messages on syntactically incorrect values. May
	 *          not be <code>null</code>.
	 * @param logger 	the application has to pass a logger which is used for logging. May not be
	 *          <code>null</code>
	 * @param externalCalculations A list of {@link IExternalCalculation}s that will be processed together
	 * 			with the Document Model's calculations.
	 * @param changedFieldInstances A list of field instances that should always be considered as changed
	 * 			for {@link IExternalCalculation.FieldInstance.hasChanged}. Allows the specification of
	 * 			indices as {@link IIdentifier.ALL_INDICES}.
	 * @return a {@link IData} object, containing all the values of fields that could be calculated.
	 *          It can contain also syntactically incorrect values. But in this case, formalErrors
	 *          will contain corresponding error messages, too.
	 * 
	 * Throws error
	 *  - if *data*, *relevantFields*, *validationResult* or *logger* are <code>null</code>
	 *  - if the mode of validation specifies a language not supported by the generated code
	 *  - if the combined dependency graph of the specified {@link IExternalCalculation}s and the Document Model's calculations would produce a cycle
	 *  - if {@link IExternalCalculation#execute} throws an error or returns invalid {@link IExternalCalculation.Result}s
	 *  - if {@link IExternalCalculation.inputs} or {@link IExternalCalculation.output} specify fields that do not exist in the model
	 *  - if {@link IExternalCalculation.output} is already a calculated field in the Document Model, or output for another {@link IExternalCalculation}
	 *  - if *changedFieldInstances* contains an invalid {@link IIdentifier}
	 */
	calculateAll(
		data: IData,
		mode: ValidationMode,
		formalErrors: IResult,
		logger: Logger,
		externalCalculations?: IExternalCalculation[],
		changedFieldInstances?: IIdentifier[],
	): IData;
}
