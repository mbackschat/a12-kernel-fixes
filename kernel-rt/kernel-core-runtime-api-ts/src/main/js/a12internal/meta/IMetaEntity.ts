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

/**
 * Common interface to meta data of all model entities.
 */
export interface IMetaEntity {
	/**
	 * Returns the value corresponding to the given key. Please refer to the documenation of the
	 * specific meta entity for a list and a specification of the supported keys.
	 * <p>
	 * If the entity has no value for the key then <code>null</code> is returned.
	 * </p>
	 * <p>
	 * If additional parameters are passed, even though they are not required, they will be ignored.
	 * </p>
	 * <p>
	 * If a string is returned, it is UTF-8 encoded.
	 * </p>
	 *
	 * @param key		Key whose value should be returned.
	 * @param params	additional parameters that might be necessary for specification.
	 * @return value for this key
	 */
	getValue(key: string, ...params: any[]): any;

	/**
	 * Returns all valid keys supported by the generated artifact.
	 * Note: a project may specify additional keys. Thus, the generated artifact may
	 * support more keys than documented in the meta interfaces, all keys are specified.
	 * Refer to the project documentation to get specifications of these additional keys.
	 *
	 * @return array of all valid meta keys for this artifact.
	 */
	getValidKeys(): string[];

}
