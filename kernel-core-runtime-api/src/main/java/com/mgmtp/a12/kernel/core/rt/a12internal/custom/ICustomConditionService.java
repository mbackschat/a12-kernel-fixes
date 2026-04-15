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
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;

/**
 * Due to the fact that this interface is not needed, we deprecate it.
 * There is no replacement and it will be removed in the next breaking release.
 *
 * @deprecated since version 29.4.0.
 */
@Deprecated
public interface ICustomConditionService {

	/**
	 * @deprecated since version 29.2.0. The implementation is now obsolete. There is always {@code false} returned.
	 */
	@Deprecated(since = "29.2.0")
	public boolean register(ICustomConditionFactory customConditionFactory);

	/**
	 * @deprecated see interface description.
	 */
	@Deprecated(since = "29.4.0")
	default void registerV2(ICustomConditionFactory customConditionFactory) {
		register(customConditionFactory);
	}

	/**
	 * @deprecated see interface description.
	 */
	@Deprecated(since = "29.4.0")
	public Optional<ICustomCondition> getCustomCondition(String name);

	/**
	 * @deprecated since version 29.2.0. The implementation is now obsolete.
	 */
	@Deprecated(since = "29.2.0")
	default Set<String> getRegisteredConditionNames() {
		return new HashSet<>();
	}

	/**
	 * @deprecated see interface description.
	 */
	@Deprecated(since = "29.4.0")
	default void cleanRegistry() {
	}
}
