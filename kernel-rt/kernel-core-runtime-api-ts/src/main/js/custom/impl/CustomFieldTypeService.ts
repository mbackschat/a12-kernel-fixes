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

import type { ICustomFieldTypeFactory } from "../ICustomFieldTypeFactory.js";
import type { ICustomFieldValidator } from "../ICustomFieldValidator.js";

/**
 * This service class provides the applications the opportunity to register
 * their custom field type factories so that they can be used to produce custom field type instances
 * during validation. Custom field types are named types with special validation algorithms outside
 * the core runtime library.
 * <p>
 * This class does not need to know which custom field types are supported by a factory. The registered factories
 * are kept in a list and <i>getCustomFieldType()</i> iterates over the list and returns the first <i>ICustomFieldValidator</i> for the given name
 * or <i>undefined</i> if none is found for this name. If there would be a collision of registered custom type names,
 * the factory that has been registered first would be used. A previously registered custom field type name cannot be overwritten. 
 * 
 * @deprecated Use {@link ValidationMode.customFieldTypeFactory} instead to provide your implementations
 * 		e.g. for validation or computation. This class — and consequently the ability to statically register your
 * 		{@link ICustomFieldTypeFactory} — will be removed with A12K-3589.
 */
export class CustomFieldTypeService {

	private static readonly _INSTANCE: CustomFieldTypeService = new CustomFieldTypeService();

	public static getInstance(): CustomFieldTypeService {
		return CustomFieldTypeService._INSTANCE;
	}

	private constructor() { }

	private _registeredFieldTypeFactories: ICustomFieldTypeFactory[] = new Array<ICustomFieldTypeFactory>(); // TODO: concurrent?

	/**
	 * @param customFieldTypeFactory
	 *            The custom field type factory to be registered. May not be <code>null</code>.
	 *
	 * <b>Note:</b> A previously registered custom field type name cannot be overwritten with this call.
	 */
	public register(customFieldTypeFactory: ICustomFieldTypeFactory): void {
		if (this._registeredFieldTypeFactories.indexOf(customFieldTypeFactory) == -1) {
			this._registeredFieldTypeFactories.push(customFieldTypeFactory);
		}
	}

	/**
	 * @param name
	 *            The name of the custom field type.
	 * @return An instance of the correspondingly named custom field type or empty if no
	 *         {@link ICustomFieldTypeFactory} for this name has been registered.
	 */
	public getCustomFieldType(name: string): ICustomFieldValidator | undefined {
		for (let i = 0; i < this._registeredFieldTypeFactories.length; i++) {
			const _factory = this._registeredFieldTypeFactories[i];
			const _customFieldType = _factory.createCustomFieldType(name);
			if (_customFieldType != null) {
				return _customFieldType;
			}
		}
		return undefined;
	}

	/**
	 * Remove all registered custom field type factories. <b>Note:</b> This method should only be used for testing!
	 */
	public cleanRegistry(): void {
		this._registeredFieldTypeFactories = new Array<ICustomFieldTypeFactory>();
	}
}
