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
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.NonNull;

/**
 * This service class provides the applications the opportunity to register
 * their custom field type factories so that they can be used to produce custom field type instances
 * during validation. Custom field types are named types with special validation algorithms outside
 * the core runtime library.
 * <p>
 * This class does not need to know which custom field types are supported by a factory. The registered factories
 * are kept in a list and {@link #getCustomFieldType(String)} iterates over the list and returns the first {@link ICustomFieldType}
 * for the given name or {@link Optional#empty()} if none is found for this name. If there would be a collision of registered
 * custom type names, the factory that has been registered first would be used. A previously registered custom field type name
 * cannot be overwritten.
 *
 * @deprecated Will be removed with A12K-3589. This class—and consequently the ability to statically register your {@link ICustomFieldTypeFactory}—will be removed.
 * 		Please use {@code DocumentProcessingConfig#customFieldTypes()} instead  to provide your implementations e.g. for validation,
 * 		computation or mapping.
 */
@Deprecated(since = "30.6.0", forRemoval = true)
@SuppressWarnings("removal")
public final class CustomFieldTypeService implements ICustomFieldTypeService {

	private static final CustomFieldTypeService INSTANCE = new CustomFieldTypeService();
	private static final ThreadLocal<CustomFieldTypeService> THREAD_LOCAL = new InheritableThreadLocal<>();

	/**
	 * Since {@link ICustomFieldTypeService} was set to deprecated, this method cannot be used anymore.
	 * Please refer to {@link CustomFieldTypeService#getInstanceV2()} instead.
	 *
	 * @deprecated since version 29.4.0
	 */
	@Deprecated(since = "29.4.0")
	public static ICustomFieldTypeService getInstance() {
		final ICustomFieldTypeService threadLocalInstance = THREAD_LOCAL.get();
		if (threadLocalInstance != null) {
			return threadLocalInstance;
		}
		return INSTANCE;
	}

	public static CustomFieldTypeService getInstanceV2() {
		final CustomFieldTypeService threadLocalInstance = THREAD_LOCAL.get();
		if (threadLocalInstance != null) {
			return threadLocalInstance;
		}
		return INSTANCE;
	}

	private CustomFieldTypeService() {
	}

	private final CopyOnWriteArrayList<ICustomFieldTypeFactory> registeredFieldTypeFactories = new CopyOnWriteArrayList<>();

	/**
	 * @deprecated since version 29.2.0. The implementation is now obsolete. There is always {@code false} returned.
	 */
	@Deprecated(since = "29.2.0")
	public boolean register(ICustomFieldTypeFactory customFieldTypeFactory) {
		registerV2(customFieldTypeFactory);
		return false;
	}

	/**
	 * @param customFieldTypeFactory
	 * 		The custom field type factory to be registered. May not be {@code null}.
	 * 		<b>Note:</b> A previously registered custom field type name cannot be overwritten with this call.
	 */
	public void registerV2(@NonNull ICustomFieldTypeFactory customFieldTypeFactory) {
		for (ICustomFieldTypeFactory factory : registeredFieldTypeFactories) {
			if (factory == customFieldTypeFactory) {
				return;
			}
		}
		registeredFieldTypeFactories.add(customFieldTypeFactory);
	}

	/**
	 * @param name
	 * 		The name of the custom field type.
	 * @return An instance of the correspondingly named custom field type or empty if no
	 *        {@link ICustomFieldTypeFactory} for this name has been registered.
	 */
	public Optional<ICustomFieldType> getCustomFieldType(String name) {
		for (ICustomFieldTypeFactory factory : registeredFieldTypeFactories) {
			var customFieldValidator = factory.createCustomFieldValidator(name);
			if (customFieldValidator.isPresent()) {
				return Optional.of(customFieldValidator.get());
			}
		}
		return Optional.empty();
	}

	/**
	 * Remove all registered custom field type factories. <b>Note:</b> This method should only be used for testing!
	 */
	public void cleanRegistry() {
		registeredFieldTypeFactories.clear();
	}

	/**
	 * Initialize a thread local instance. One usage could be in <i>Spring Test</i> with the <i>ICustomFieldTypeFactory</i>
	 * implemented as <i>Spring Bean</i>.
	 * <b>Note:</b> This method should only be used for testing!
	 */
	public static void initializeThreadLocalInstance() {
		final CustomFieldTypeService threadLocalInstance = new CustomFieldTypeService();
		THREAD_LOCAL.set(threadLocalInstance);
	}

	/**
	 * Remove the thread local instance. <b>Note:</b> This method should only be used for testing!
	 */
	public static void removeThreadLocalInstance() {
		THREAD_LOCAL.remove();
	}
}
