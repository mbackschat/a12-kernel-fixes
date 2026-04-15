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

import type { SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type { IIdentifier } from "../validation/IIdentifier.js";
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { ICalculator } from "../validation/ICalculator.js";

// The API specified here mirrors the API in @kernel-md-facade/compute.ts
// Please check if changes made here need to be applied there as well.

/**
 * Specifies a calculation that is not part of the Document Model but should be executed during calculation
 * (see {@link ICalculator#calculateAll}).
 */
export interface IExternalCalculation {
	/**
	 * Specifies the fields the {@link IExternalCalculation} depends on as inputs.
	 *
	 * For example for dependent fields in Form Models, the inputs are
	 * - a master field that triggers changes
	 * - an optional "value source" field, i.e. a field whose value will be applied to the output field
	 */
	readonly inputs: string[];

	/**
	 * Specifies the output field of the {@link IExternalCalculation}.
	 * 
	 * The field must not be the output of another calculation or {@link IExternalCalculation}.
	 *
	 * For example for dependent fields in Form Models, this would be the dependent field itself.
	 */
	readonly output: string;

	/**
	 * A callback that computes changes to be applied to instances of the output field.
	 * 
	 * The callback will be invoked exactly once per invocation of {@link ICalculator#calculateAll}
	 * (assuming no fatal errors occurred), i.e. it must properly handle repeatable fields.
	 *
	 * At the time the callback is invoked, it is guaranteed that all input fields have already been calculated (if necessary).
	 */
	execute: IExternalCalculation.Callback;
}

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace IExternalCalculation {
	/**
	 * The parameter *operands* contains an {@link Operand} for every input field, in the order they were specified in {@link IExternalCalculation.inputs}.
	 * 
	 * The return value may specify an arbitrary number of {@link Result}s, meaning that changes to the output field are optional.
	 * For all {@link Result}s, {@link Result.field} must be an instance of {@link IExternalCalculation.output}.
	 * If multiple {@link Result}s target the same field instance, the field instance's value will be determined by the first {@link Result} targeting it.
	*/
	export type Callback = (operands: Operand[]) => Result[];

	/**
	 * Provides access to all instances of {@link Operand.field} that:
	 *  - exist in the Document at the time {@link IExternalCalculation#execute} is invoked, or
	 *  - were filled in the Document passed to {@link ICalculator#calculateAll}.
	 * 
	 * <b>Note:</b> A repeatable field instance *exists* if its innermost repeatable group is filled, even if the field instance itself is not *filled*.
	 */
	export interface Operand {
		readonly field: string;
		readonly instances: FieldInstance[];
	}

	/**
	 * Provides access to a single field instance (i.e. a field with an indexed path into the Document and a value).
	*/
	export interface FieldInstance {
		readonly field: IIdentifier;
		readonly value: SupportedType;

		/**
		 * Indicates whether the field instance's value has changed compared to the Document provided to {@link ICalculator#calculateAll}.
		 * If that initial value is invalid, it is considered to be `null` for the purposes of this comparison.
		 */
		readonly hasChanged: boolean;

		/**
		 * Indicates whether the field instance's value is valid.
		 */
		readonly isValid: boolean;
	}

	export type Result = FieldResult; // can be extended for groups if necessary at some point in the future

	/**
	 * Specifies a change to be made to one or more existing field instances in the Document.
	 */
	export interface FieldResult {
		readonly type: "Field";

		/**
		 * The existing field instance(s) which *value* should be applied to.
		 * 
		 * Allows the specification of indices as {@link IIdentifier.ALL_INDICES}. The set of field instances to change will then consist
		 * of all *existing* field instances of the target field that match the other (explicitly set) indices.
		 * 
		 * If *field* does not match any existing instance (even if no wildcards are used), no changes will be made, and no error will be thrown.
		 * 
		 * <b>Note:</b> A repeatable field instance *exists* if its innermost repeatable group is filled, even if the field instance itself is not *filled*.
		 */
		readonly field: IIdentifier;

		/**
		 * The value to be applied to the field instance(s) described by *field*.
		 * 
		 * For the provided values it is assumed that they:
		 * - have normalized linebreaks (`\n` instead of `\r\n`)
		 * - do not start with a blank (" ")
		 * - do not end with a blank (" ")
		 * - only contain legal characters (as defined in the Document Model)
		 */
		readonly value: SupportedType;
	}
}
