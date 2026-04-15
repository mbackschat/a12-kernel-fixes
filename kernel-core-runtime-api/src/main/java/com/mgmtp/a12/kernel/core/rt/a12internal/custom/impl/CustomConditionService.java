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

package com.mgmtp.a12.kernel.core.rt.a12internal.custom.impl;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mgmtp.a12.kernel.core.rt.a12internal.custom.ICustomConditionFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.custom.ICustomConditionService;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;

/**
 * This service class provides the applications the opportunity to register their custom condition
 * factories so that they can be used to produce custom condition instances during validation.
 * Custom conditions are named conditions with special validation algorithms outside the core
 * runtime library.
 * This class does not need to know which custom condition names are supported by a factory. The registered factories
 * are kept in a list and {@link #getCustomCondition(String)} iterates over the list and returns the first {@link ICustomCondition}
 * for the given name or {@link Optional#empty()} if none is found for this name. If there would be a collision of registered
 * custom condition names, the factory that has been registered first would be used. A previously registered custom condition name
 * cannot be overwritten.
 *
 * @deprecated Will be removed with A12K-3589. This class—and consequently the ability to statically register your
 *        {@link ICustomConditionFactory}—will be removed. Please use {@link DocumentProcessingConfig#customConditionFactory()}
 * 		instead to provide your implementations e.g. for validation, computation or mapping.
 */
@Deprecated(since = "30.6.0", forRemoval = true)
public final class CustomConditionService implements ICustomConditionService {

	private static final CustomConditionService INSTANCE = new CustomConditionService();
	private static final ThreadLocal<CustomConditionService> THREAD_LOCAL = new InheritableThreadLocal<>();

	/**
	 * Since {@link ICustomConditionService} was set to deprecated, this method cannot be used anymore.
	 * Please refer to {@link CustomConditionService#getInstanceV2()} instead.
	 *
	 * @deprecated since version 29.4.0
	 */
	@Deprecated(since = "29.4.0")
	public static ICustomConditionService getInstance() {
		final ICustomConditionService threadLocalInstance = THREAD_LOCAL.get();
		if (threadLocalInstance != null) {
			return threadLocalInstance;
		}
		return INSTANCE;
	}

	public static CustomConditionService getInstanceV2() {
		final CustomConditionService threadLocalInstance = THREAD_LOCAL.get();
		if (threadLocalInstance != null) {
			return threadLocalInstance;
		}
		return INSTANCE;
	}

	private CustomConditionService() {
	}

	private final CopyOnWriteArrayList<ICustomConditionFactory> registeredConditionFactories = new CopyOnWriteArrayList<>();
	private ICustomConditionFactory customConditionFactory;

	/**
	 * @deprecated since version 29.2.0. The implementation is now obsolete. There is always {@code false} returned.
	 */
	@Deprecated(since = "29.2.0")
	public boolean register(ICustomConditionFactory customConditionFactory) {
		registerV2(customConditionFactory);
		return false;
	}

	/**
	 * Register a {@link ICustomConditionFactory}.
	 * The given {@link ICustomConditionFactory} is expected to handle all {@link ICustomCondition} implementations existing in your
	 * project, across all models. For this reason, it is not expected that it is necessary to call this method multiple times in
	 * your application (e.g. in different threads) nor to set different {@link ICustomConditionFactory} implementations. However,
	 * the registration of further {@link ICustomConditionFactory} is possible.
	 *
	 * @param customConditionFactory
	 * 		The custom condition factory to be registered. May not be {@code null}.
	 * 		<b>Note:</b> A previously registered custom condition name cannot be overwritten with this call.
	 */
	public void registerV2(ICustomConditionFactory customConditionFactory) {
		for (ICustomConditionFactory factory : registeredConditionFactories) {
			if (factory == customConditionFactory) {
				return;
			}
		}
		registeredConditionFactories.add(customConditionFactory);
	}

	/**
	 * Sets the {@link ICustomConditionFactory}. This {@link ICustomConditionFactory} is only used if you do not
	 * {@link #register(ICustomConditionFactory)} any {@link ICustomConditionFactory}.
	 * <b>Warnung:</b> This method is deliberately not defined in the interface, it was provided for tests and the
	 * application "Qualle".
	 *
	 * @param customConditionFactory
	 * 		The {@link ICustomConditionFactory} to create all {@link ICustomCondition}s being
	 * 		specified in the models.
	 * @return If no {@link ICustomConditionFactory} was set before this method was called {@link Optional#empty()} ,
	 * 		otherwise the previously set and now replaced {@link ICustomConditionFactory}.
	 */
	public Optional<ICustomConditionFactory> setCustomConditionFactory(ICustomConditionFactory customConditionFactory) {
		Optional<ICustomConditionFactory> previousValue = Optional.ofNullable(this.customConditionFactory);
		this.customConditionFactory = customConditionFactory;
		return previousValue;
	}

	/**
	 * @param name
	 * 		The name of the custom condition.
	 * @return An instance of the correspondingly named custom condition or empty if no
	 *        {@link ICustomConditionFactory} for this name has been registered.
	 */
	public Optional<ICustomCondition> getCustomCondition(String name) {
		if (!registeredConditionFactories.isEmpty()) {
			for (ICustomConditionFactory factory : registeredConditionFactories) {
				// TODO: Remove the following try/catch handler when ER-50 does not need to be supported any longer
				try {
					var customCondition = factory.createCustomConditionV2(name);
					if (customCondition.isPresent()) {
						return customCondition;
					}
				} catch (NoSuchMethodError e) {
					if (factory.getSupportedConditionNames().contains(name)) {
						return Optional.of(factory.createCustomCondition(name));
					}
				}
			}
			return Optional.empty();
		} else {
			return this.customConditionFactory != null ?
					this.customConditionFactory.createCustomConditionV2(name) :
					Optional.empty();
		}
	}

	/**
	 * Remove all registered custom condition factories. <b>Note:</b> This method should only be used for testing!
	 */
	public void cleanRegistry() {
		registeredConditionFactories.clear();
	}

	/**
	 * Initialize a thread local instance. One usage could be in <i>Spring Test</i> with the <i>ICustomConditionFactory</i>
	 * implemented as <i>Spring Bean</i>.
	 * <b>Note:</b> This method should only be used for testing!
	 */
	public static void initializeThreadLocalInstance() {
		final CustomConditionService threadLocalInstance = new CustomConditionService();
		THREAD_LOCAL.set(threadLocalInstance);
	}

	/**
	 * Remove the thread local instance. <b>Note:</b> This method should only be used for testing!
	 */
	public static void removeThreadLocalInstance() {
		THREAD_LOCAL.remove();
	}
}
