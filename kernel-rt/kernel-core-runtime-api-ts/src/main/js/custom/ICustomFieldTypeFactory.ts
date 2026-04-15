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

import type { ICustomFieldValidator } from "./ICustomFieldValidator.js";

/**
 * Common interface for custom field type factories.
 *
 * Note: Implementing classes must guarantee thread-safety of all methods.
 */
export interface ICustomFieldTypeFactory {
	/**
	 * {@link CustomFieldTypeService} uses this method to create new instances of custom field types
	 * without knowing the custom type names supported by the factory.
	 *
	 * @param customFieldTypeName The name of the custom field type to be instantiated.
	 * @return A new instance of the custom field type specified by `customFieldTypeName` or `null` if the factory does
	 * 		not support this custom field type.
	 * 
	 * **Warning:** The factory must not throw an exception if it does not support the requested custom field type
	 * (multiple factories can be registered).
	 */
	createCustomFieldType(customFieldTypeName: string): ICustomFieldValidator | null;
}

/**
 * A factory method for the simplest {@link ICustomFieldTypeFactory} where a key statically maps to a custom field validator.
 * 
 * @param obj an object whose keys are custom field type names and whose values are the custom field validators to use for that respective name
 * @returns a custom field type factory based on the provided object
 */
export function createSimpleCustomFieldTypeFactory(obj: { readonly [key: string]: ICustomFieldValidator }): ICustomFieldTypeFactory {
	return {
		createCustomFieldType: customFieldTypeName => {
			return obj[customFieldTypeName] ?? null;
		}
	}
}
