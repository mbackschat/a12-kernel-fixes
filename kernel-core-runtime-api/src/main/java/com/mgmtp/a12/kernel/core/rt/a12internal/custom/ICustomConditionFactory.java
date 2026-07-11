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
package com.mgmtp.a12.kernel.core.rt.a12internal.custom;

import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;

/**
 * Common interface for custom condition factories.
 * <p>
 * Note: Implementing classes must guarantee thread-safety of all methods.
 */
public interface ICustomConditionFactory {

	/**
	 * Create new instances of custom conditions specified by name.
	 *
	 * @param customConditionName
	 * @return A new instance of the custom condition specified by {@code customConditionName} or
	 * 		empty if the factory does not support this custom condition.
	 *		<b>Note:</b> There must not be thrown an exception if the factory does not support this custom condition.
	 */
	Optional<ICustomCondition> createCustomConditionV2(String customConditionName);

/**
 * Create new instances of custom condition factories from a map of custom conditions.
 *
 * @param map
 * 		The {@code Map<String, ICustomCondition>} of custom conditions with the name of the custom condition as key.
 * @return A new instance of the custom condition factory that creates the custom conditions specified by {@code map}.
 */
 static ICustomConditionFactory fromMap(Map<String, ICustomCondition> map) {
		return new ICustomConditionFactory() {
			@Override
			public Optional<ICustomCondition> createCustomConditionV2(String customConditionName) {
				return Optional.ofNullable(map.get(customConditionName));
			}
		};
	}
}
