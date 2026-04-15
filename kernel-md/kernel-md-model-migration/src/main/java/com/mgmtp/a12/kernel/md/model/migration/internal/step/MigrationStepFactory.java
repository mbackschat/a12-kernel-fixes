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
package com.mgmtp.a12.kernel.md.model.migration.internal.step;

import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationStepConfigType;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;

/**
 * Creates new MigrationStep instance from configured class name of bean.
 */
public final class MigrationStepFactory {

	/**
	 * Creates a migration step instance configured in MeliesMigratorConfig.xml.
	 *
	 * @param stepConfig
	 * 		the step config
	 * @return the migration step
	 */
	public <T> MigrationStep<T> create(MigrationCache cache, MigrationStepConfigType stepConfig,
			ICustomMigrationConfig customConfig) {
		Validate.notNull(stepConfig, "stepConfig must not be NULL!");

		final String clazzName = stepConfig.getBean().getInternalClazz();
		try {
			final Class<?> stepClazz = Class.forName(clazzName);
			final Constructor<?> stepConstructor = stepClazz.getConstructor(MigrationCache.class, MigrationStepConfigType.class,
					ICustomMigrationConfig.class);
			final Object step = stepConstructor.newInstance(cache, stepConfig, customConfig);

			if (step instanceof MigrationStep) {
				@SuppressWarnings("unchecked")
				MigrationStep<T> result = (MigrationStep<T>) step;
				return result;
			}
			throw new MigrationException(new MigrationError(MigrationErrorType.STEP_UNSUPPORTED_TYPE).withMessage(
					format("Current migration step '%s' is not a sub class of '%s'", clazzName,
							MigrationStep.class.getName())));

		} catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException
				 | NoSuchMethodException e) {

			throw new MigrationException(new MigrationError(MigrationErrorType.STEP_NOT_CREATABLE).withMessage(
					format("Creation of migration step with config: '%s' failed! Exception: '%s'", stepConfig, e.getMessage())), e);
		}
	}

}
