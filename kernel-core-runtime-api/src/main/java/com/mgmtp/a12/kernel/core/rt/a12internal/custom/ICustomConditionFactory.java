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

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;

/**
 * Common interface for custom condition factories.
 * <p>
 * Note: Implementing classes must guarantee thread-safety of all methods.
 */
public interface ICustomConditionFactory {

	/**
	 * {@link ICustomConditionService} uses this method to register the factory with its condition
	 * names.
	 *
	 * @return A set of names of the custom conditions which can be created by this factory. Never
	 * 		returns {@code null} or contains {@code null} or the empty-string.
	 *
	 * @deprecated since version 29.2.0. The implementation is now obsolete. It will be removed with A12K-3589.
	 */
	@Deprecated(since = "29.2.0")
	default Set<String> getSupportedConditionNames() {
		return new HashSet<>();
	}

	/**
	 * {@link ICustomConditionService} uses this method to create new instances of custom conditions
	 * without knowing the condition names supported by the factory.
	 *
	 * @param customConditionName
	 * @return A new instance of the custom condition specified by {@code customConditionName} or
	 * 		{@code null} if the factory does not support this custom condition.
	 *		<b>Note:</b> There must not be thrown an exception if the factory does not support this custom condition
	 *		(multiple factories can be registered).
	 *
	 * @deprecated since version 29.2.0. The implementation is now obsolete. It will be removed with A12K-3589.
	 */
	@Deprecated(since = "29.2.0")
	default ICustomCondition createCustomCondition(String customConditionName) {
		return null;
	}

	/**
	 * {@link ICustomConditionService} uses this method to create new instances of custom conditions
	 * without knowing the condition names supported by the factory.
	 *
	 * @param customConditionName
	 * @return A new instance of the custom condition specified by {@code customConditionName} or
	 * 		empty if the factory does not support this custom condition.
	 *		<b>Note:</b> There must not be thrown an exception if the factory does not support this custom condition
	 *		(multiple factories can be registered).
	 */
	default Optional<ICustomCondition> createCustomConditionV2(String customConditionName) {
		return Optional.ofNullable(createCustomCondition(customConditionName));
	}

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
