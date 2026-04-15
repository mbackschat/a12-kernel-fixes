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

import type { ValidationMode } from "../ValidationMode.js";

import type { IData } from "./IData.js";
import type { IIdentifier } from "./IIdentifier.js";
import type { IResult } from "./IResult.js";

/**
 * API for validation method calls.<br>
 * The methods are thread-safe.
 */
export interface IValidator {
	/**
	 * This method executes a partly validation. Only the entities defined in
	 * <code>relevantEntities</code> will be checked. Only rules, whose error field is a relevant
	 * field, are checked.
	 * Rule conditions are validated in a way, which will not return errors, if omission
	 * errors of non-relevant fields are present.
	 * <p>
	 * <b>Note:</b> Some rules may eventually not fire if not all fields referenced by the rule are
	 * marked as relevant for the validation.
	 *
	 * @param data 	the data to be validated. May not be <code>null</code>.
	 * @param relevantEntities 	Specification of the entities (fields and/or groups) relevant for the validation.
	 *            If <code>null</code> is passed, an <code>IllegalArgumentException</code> will be thrown.<br>
	 *            Entities in the set of relevant entities may specify one or more indices with
	 *            {@link IIdentifier.ALL_INDICES}. This is recommended as a short-cut specification,
	 *            e.g. if all one thousand instances of an entity are relevant.
	 *            For Groups in the set of relevant entities, their descendants (fields and/or
	 *            groups) are assumed as relevant, too. Indices of the descendant levels are
	 *            implicitly assumed as {@link IIdentifier.ALL_INDICES}.
	 *            If entity instances with a higher index value than the maximum repetition of the
	 *            corresponding repetition layer are passed, these entity instances will be ignored.
	 *            <p>Note:<br>
	 *            Setting all index levels to {@link IIdentifier.ALL_INDICES} including non
	 *            repeatable layers instead of listing each single index will give a performance
	 *            gain. Listing all indices of a repetition layer separately is discouraged not only
	 *            because of the performance, but also because some conditions are not evaluated in
	 *            this case (NumberOfFilledFields, NumberOfFilledGroups and in some constellations
	 *            semantic index and parallel iteration).</p>
	 * @param validationResult 	container object for resulting messages on erroneous plausibility checks. May not
	 *            be <code>null</code>.
	 * @param mode 	specifies the mode of validation. May not be <code>null</code>.
	 * @param logger 	the caller has to pass a logger which is used for logging. May not be <code>null</code>.
	 * @throws Error,
	 *             <ul>
	 *             <li>if a validated rule uses a custom condition which is not registered via
	 *             {@link CustomConditionService}.</li>
	 *             <li>if <i>data, relevantEntities, validationResult, mode</i> or <i>logger</i> are
	 *             <code>null</code>.</li>
	 *             <li>if the mode of validation specifies a language not supported by the generated
	 *             code.</li>
	 *             </ul>
	 * @throws CustomConditionException
	 * 		<ul>
	 * 			<li>If an {@link ICustomCondition} throws it during validation.</li>
	 * 		</ul>
	 */
	validatePartlyWith3ValueLogic(
		data: IData,
		relevantEntities: Set<IIdentifier>,
		validationResult: IResult,
		mode: ValidationMode,
		logger: Logger
	): void;

	/**
	 * This method executes a full validation.
	 *
	 * @param data 	the data to be validated. May not be <code>null</code>.
	 * @param validationResult 	container object for resulting messages on erroneous plausibility checks. May not
	 *          be <code>null</code>.
	 * @param mode 	specifies the mode of validation. May not be <code>null</code>.
	 * @param logger 	the caller has to pass a logger, which is used for logging. May not be <code>null</code>.
	 * @throws Error,
	 *           <ul>
	 *           <li>if a validated rule uses a custom condition which is not registered via
	 *           {@link CustomConditionService}.</li>
	 *           <li>if <i>data, relevantEntities, validationResult, mode</i> or <i>logger</i> are
	 *           <code>null</code>.</li>
	 *           <li>if the mode of validation specifies a language not supported by the generated
	 *           code.</li>
	 *           </ul>
	 * @throws CustomConditionException
	 * 		<ul>
	 * 			<li>If an {@link ICustomCondition} throws it during validation.</li>
	 * 		</ul>
	 */
	validateAll(data: IData, validationResult: IResult, mode: ValidationMode, logger: Logger): void;
}
