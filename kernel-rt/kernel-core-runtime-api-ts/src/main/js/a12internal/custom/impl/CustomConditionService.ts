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

import type { ICustomCondition } from "../../validation/ICustomCondition.js";

import type { ICustomConditionFactory } from "../ICustomConditionFactory.js";

/**
 * This service class provides the applications the opportunity to register their custom condition
 * factories so that they can be used to produce custom condition instances during validation.
 * Custom conditions are named conditions with special validation algorithms outside the core
 * runtime library.
 * <p>
 * This class does not need to know which custom condition names are supported by a factory. The registered factories
 * are kept in a list and <i>getCustomCondition()</i> iterates over the list and returns the first <i>ICustomCondition</i> for the given name
 * or <i>undefined</i> if none is found for this name. If there would be a collision of registered custom condition names,
 * the factory that has been registered first would be used. A previously registered custom condition name cannot be overwritten.
 * 
 * @deprecated Use {@link ValidationMode.customConditionFactory} instead to provide your implementations
 * 		e.g. for validation or computation. This class — and consequently the ability to statically register your
 * 		{@link ICustomConditionFactory} — will be removed with A12K-3589.
 */
export class CustomConditionService {

	private static _INSTANCE: CustomConditionService = new CustomConditionService();

	public static getInstance(): CustomConditionService {
		return this._INSTANCE;
	}

	private constructor() { }

	private _registeredConditionFactories: ICustomConditionFactory[] = new Array<ICustomConditionFactory>();

	/**
	 * @param customConditionFactory
	 *            The custom condition factory to be registered. May not be <code>null</code>.
	 * 
	 * <b>Note:</b> A previously registered custom condition name cannot be overwritten with this call.
	 */
	public register(customConditionFactory: ICustomConditionFactory): void {
		if (this._registeredConditionFactories.indexOf(customConditionFactory) == -1) {
			this._registeredConditionFactories.push(customConditionFactory);
		}
	}

	/**
	 * @param name
	 *            The name of the custom condition.
	 * @return An instance of the correspondingly named custom condition or empty if no
	 *         {@link ICustomConditionFactory} for this name has been registered.
	 */
	public getCustomCondition(name: string): ICustomCondition | undefined {
		for (let i = 0; i < this._registeredConditionFactories.length; i++) {
			const _factory = this._registeredConditionFactories[i];
			const _customCondition = _factory.createCustomCondition(name);
			if (_customCondition != null) {
				return _customCondition;
			}
		}
		return undefined;
	}

	/**
	 * Remove all registered custom condition factories. <b>Note:</b> This method should only be used for testing!
	 */
	public cleanRegistry(): void {
		this._registeredConditionFactories = new Array<ICustomConditionFactory>();
	}
}
