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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.customfieldtype.internal.CustomFieldValidatorImpl;

import lombok.NonNull;

/**
 * Common interface for custom field type factories.
 * <p>
 * Note: Implementing classes must guarantee thread-safety of all methods.
 */
public interface ICustomFieldTypeFactory {

	/**
	 * {@link ICustomFieldTypeService} uses this method to register the factory with its types
	 * names.
	 *
	 * @return A set of names of the custom field types which can be created by this factory. Never
	 * 		returns {@code null} or contains {@code null} or the empty-string.
	 *
	 * @deprecated since version 29.2.0. The implementation is now obsolete. It will be removed with A12K-3589.
	 */
	@Deprecated(since = "29.2.0", forRemoval = true)
	default Set<String> getSupportedTypeNames() {
		return new HashSet<>();
	}

	/**
	 * {@link ICustomFieldTypeService} uses this method to create new instances of the custom field
	 * types which are provided by this factory.
	 *
	 * @param customFieldTypeName
	 * 		The name of the custom field type to be instantiated.
	 * @return A new instance of the custom field type specified by {@code customFieldTypeName}.
	 * 		Never returns {@code null}.
	 *
	 * @deprecated since version 29.2.0. The implementation is now obsolete. It will be removed with A12K-3589.
	 */
	// A12K-3589 remove this SuppressWarnings
	@SuppressWarnings("removal")
	@Deprecated(since = "29.2.0", forRemoval = true)
	default ICustomFieldType createCustomFieldType(String customFieldTypeName) {
		return null; // since the method is deprecated, it is ok to return null even it should not be.
	}

	/**
	 * {@link ICustomFieldTypeService} uses this method to create new instances of custom field types
	 * without knowing the custom type names supported by the factory.
	 *
	 * @param customFieldTypeName
	 * 		The name of the custom field type to be instantiated.
	 * @return A new instance of the custom field type specified by {@code customFieldTypeName} or
	 * 		empty if the factory does not support this custom field type.
	 * 		<b>Note:</b> There must not be thrown an exception if the factory does not support this custom field type.
	 *
	 * @deprecated since version 30.2.0. Will be removed with A12K-3589. The implementation is now obsolete.
	 * Please implement {@link #createCustomFieldValidator(String)} instead. It will be removed with A12K-3589.
	 */
	// A12K-3589 remove this SuppressWarnings
	@SuppressWarnings("removal")
	@Deprecated(since = "30.6.0", forRemoval = true)
	default Optional<ICustomFieldType> createCustomFieldTypeV2(String customFieldTypeName) {
		return Optional.ofNullable(createCustomFieldType(customFieldTypeName));
	}

	/**
	 * {@link ICustomFieldTypeService} uses this method to create new instances of custom field types
	 * without knowing the custom type names supported by the factory.
	 *
	 * @param customFieldTypeName
	 * 		The name of the custom field type to be instantiated.
	 * @return A new instance of the custom field type specified by {@code customFieldTypeName} or
	 * 		empty if the factory does not support this custom field type.
	 * 		<b>Note:</b> There must not be thrown an exception if the factory does not support this custom field type.
	 */
	// A12K-3589 remove this SuppressWarnings
	@SuppressWarnings("removal")
	default Optional<ICustomFieldValidator> createCustomFieldValidator(@NonNull String customFieldTypeName) {
		return createCustomFieldTypeV2(customFieldTypeName).map(CustomFieldValidatorImpl::new);
	}

	/**
	 * Create new instances of custom field type factories from a map of custom field types.
	 *
	 * @param map
	 * 		The {@code Map<String, ICustomFieldValidator>} of custom field types with the name of the custom field type as key.
	 * @return A new instance of the custom condition factory that creates the custom field types specified by {@code map}.
	 */
	static ICustomFieldTypeFactory fromMap(Map<String, ICustomFieldValidator> map) {
		return new ICustomFieldTypeFactory() {
			@Override
			public Optional<ICustomFieldValidator> createCustomFieldValidator(String customFieldTypeName) {
				return Optional.ofNullable(map.get(customFieldTypeName));
			}
		};
	}
}
