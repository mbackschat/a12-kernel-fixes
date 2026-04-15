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

import java.util.LinkedList;
import java.util.List;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationStepConfigType;

/**
 * Base class for migration step with complex migration use case, that consists many document
 * transformations.
 * <p>
 * The sub class should configure a transformer list via
 * {@link #addTransformer(ModelRepresentationTransformer)}
 * </p>
 */
public abstract class CompositeMigrationStep<T> extends BaseMigrationStep<T> {

	private final List<ModelRepresentationTransformer<T>> transformers = new LinkedList<>();

	/**
	 * Instantiates a new Base migration step.
	 *
	 * @param stepConfigType
	 * 		the step config type of this migration step
	 * @param customMigConfig
	 */
	protected CompositeMigrationStep(final MigrationCache cache, final MigrationStepConfigType stepConfigType,
			ICustomMigrationConfig customMigConfig) {
		super(cache, stepConfigType, customMigConfig);
	}

	protected void addTransformer(ModelRepresentationTransformer<T> documentTransformer) {
		this.transformers.add(documentTransformer);
	}

	@Override
	public void execute(final T model, final MigrationLogger migrationLogger) {
		for (ModelRepresentationTransformer<T> transformer : transformers) {
			transformer.execute(model, migrationLogger);
		}
	}
}
