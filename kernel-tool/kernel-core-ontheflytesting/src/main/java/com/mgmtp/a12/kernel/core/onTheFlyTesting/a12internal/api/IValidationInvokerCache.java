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

package com.mgmtp.a12.kernel.core.onTheFlyTesting.a12internal.api;

import java.util.function.Supplier;

/**
 * The cache for {@link IValidationInvoker} instances created by
 * {@link IOnTheFlyValidationGenerator}.
 * <p>
 * <i>Note:</i> The implementing class has to be thread safe and must be provided by the caller.
 */
public interface IValidationInvokerCache {
	/**
	 * Get the {@link IValidationInvoker} for the given identifier from the cache.
	 * <p>
	 * If the identifier is not present, the value should be retrieved from the
	 * validationInvokerCreator, stored in the cache, and returned.
	 *
	 * @param identifier
	 * 		the identifier, for which the code should be returned.
	 * @param validationInvokerCreator
	 * 		the creation call for the corresponding validation invoker.
	 * @return the {@link IValidationInvoker} in the cache.
	 */
	IValidationInvoker computeIfAbsent(String identifier, Supplier<IValidationInvoker> validationInvokerCreator);
}
