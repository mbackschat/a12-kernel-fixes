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

package com.mgmtp.a12.kernel.core.customfieldtype;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Due to the fact that this interface is not needed, we deprecate it.
 * There is no replacement and it will be removed in the next breaking release.
 *
 * @deprecated since version 29.4.0. It will be removed with A12K-3589.
 */
@Deprecated
@SuppressWarnings("removal")
public interface ICustomFieldTypeService {

	/**
	 * @param customFieldTypeFactory
	 * 		The custom field type factory to be registered for all custom type names in
	 *        {@link ICustomFieldTypeFactory#getSupportedTypeNames()}.
	 * 		May not be {@code null}.
	 * @return {@code true} if any previously registered custom type name has been overwritten by
	 * 		this call, otherwise {@code false}.
	 *
	 * @deprecated since version 29.2.0. The implementation is now obsolete. There is always {@code false} returned.
	 */
	@Deprecated(since = "29.2.0")
	boolean register(ICustomFieldTypeFactory customFieldTypeFactory);

	/**
	 * @param customFieldTypeFactory
	 * 		The custom field type factory to be registered. May not be {@code null}.
	 *		<b>Note:</b> A previously registered custom field type name cannot be overwritten with this call.
	 */
	@Deprecated(since = "29.4.0")
	default void registerV2(ICustomFieldTypeFactory customFieldTypeFactory) {
		register(customFieldTypeFactory);
	}

	/**
	 * @param name
	 * 		The name of the custom field type.
	 * @return An instance of the correspondingly named custom field type or empty if no
	 *        {@link ICustomFieldTypeFactory} for this name has been registered.
	 */
	@Deprecated(since = "29.4.0")
	public Optional<ICustomFieldType> getCustomFieldType(String name);

	/**
	 * @return An immutable set of names of all the registered custom field types.
	 *
	 * @deprecated since version 29.2.0. The implementation is now obsolete.
	 */
	@Deprecated(since = "29.2.0")
	default Set<String> getRegisteredTypeNames() {
		return new HashSet<>();
	}

	/**
	 * Remove all registered custom field type factories. <b>Note:</b> This method should only be used for testing!
	 */
	@Deprecated(since = "29.4.0")
	default void cleanRegistry() {
	}
}
