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
package com.mgmtp.a12.kernel.md.model.migration.internal;

import static com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType.UNKNOWN_ERROR;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.md.model.migration.internal.accessor.SchemaVersionAccessor;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.MigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;
import com.mgmtp.a12.kernel.md.model.migration.internal.step.MigrationStepExecutor;
import com.mgmtp.a12.kernel.md.model.migration.internal.validation.ModelValidator;
import com.mgmtp.a12.kernel.md.model.migration.internal.validation.SchemaValidator;

/**
 * This class orchestrates the migration process of document model.
 */
public class Migrator<T> {

	protected static final Logger LOG = LoggerFactory.getLogger(Migrator.class);

	private final ModelValidator<T> modelValidator;
	private final SchemaVersionAccessor<T> schemaVersionAccessor;
	private final SchemaValidator<T> schemaValidator;
	private final MigrationCache cache;
	private final MigrationConfig config;

	/**
	 * Instantiates a new Migrator.
	 *
	 * @param schemaValidator
	 * 		the schema validator
	 * @param modelValidator
	 * 		the model validator
	 * @param schemaVersionAccessor
	 * 		the schemaVersionAccessor
	 * @param config
	 */
	private Migrator(final ModelValidator<T> modelValidator,
			final SchemaVersionAccessor<T> schemaVersionAccessor,
			final SchemaValidator<T> schemaValidator,
			final MigrationCache cache,
			final MigrationConfig config) {

		Validate.notNull(modelValidator, "modelValidator must not be NULL");
		Validate.notNull(schemaVersionAccessor, "schemaVersionAccessor must not be null!");
		Validate.notNull(schemaValidator, "schemaValidator must not be NULL");
		Validate.notNull(cache, "cache must not be NULL");
		Validate.notNull(config, "config must not be NULL");

		this.modelValidator = modelValidator;
		this.schemaVersionAccessor = schemaVersionAccessor;
		this.schemaValidator = schemaValidator;
		this.cache = cache;
		this.config = config;
	}

	/**
	 * Conduct the actual migration. Migrates the model document in-place.
	 *
	 * @param model
	 * 		the model
	 * @param targetModelVersion
	 * @return Result of migration
	 */
	public MigrationResult migrate(final T model, final String targetModelVersion) {
		final MigrationStepExecutor<T> stepExecutor = new MigrationStepExecutor<>(cache, loadConfiguration(),
				schemaVersionAccessor);

		try {
			// 2.) validate source model
			modelValidator.validate(model);

			final MigrationResult migrationResult = new MigrationResult(MigrationResultType.SUCCESSFUL);

			// 3.) execute migration steps
			stepExecutor.execute(model, migrationResult, targetModelVersion);

			// 4.) verify result
			schemaValidator.validate(model, migrationResult, stepExecutor.getLastTargetModelVersion());

			return migrationResult;

		} catch (MigrationException migExcp) {
			return MigrationResult.createWithError(migExcp.getError());
		} catch (Exception e) {
			LOG.warn("", e);
			return MigrationResult.createWithError(new MigrationError(UNKNOWN_ERROR).withMessage(e.getMessage()));
		}
	}

	MigrationConfig loadConfiguration() {
		return config;
	}

	/**
	 * Migrator Builder that uses no default values.
	 */
	public static class Builder<T> {

		private MigrationCache cache;
		private MigrationConfig config;
		private ModelValidator<T> modelValidator;
		private SchemaVersionAccessor<T> schemaVersionAccessor;
		private SchemaValidator<T> schemaValidator;

		public Builder<T> withMigrationCache(final MigrationCache cache) {
			this.cache = cache;
			return this;
		}

		public Builder<T> withMigrationConfig(final MigrationConfig config) {
			this.config = config;
			return this;
		}

		/**
		 * Setter of model validator.
		 *
		 * @param modelValidator
		 * 		the model validator
		 */
		public Builder<T> withModelValidator(final ModelValidator<T> modelValidator) {
			Validate.notNull(modelValidator, "modelValidator must not be null");
			this.modelValidator = modelValidator;
			return this;
		}

		/**
		 * Setter of schema version accessor.
		 *
		 * @param schemaVersionAccessor
		 * 		the model validator
		 */
		public Builder<T> withSchemaVersionAccessor(final SchemaVersionAccessor<T> schemaVersionAccessor) {
			Validate.notNull(schemaVersionAccessor, "schemaVersionAccessor must not be null");
			this.schemaVersionAccessor = schemaVersionAccessor;
			return this;
		}

		/**
		 * Setter of schema validator.
		 *
		 * @param schemaValidator
		 * 		the schema validator
		 */
		public Builder<T> withSchemaValidator(final SchemaValidator<T> schemaValidator) {
			Validate.notNull(schemaValidator, "schemaValidator must not be null");
			this.schemaValidator = schemaValidator;
			return this;
		}

		/**
		 * Builds a new migrator instance.
		 *
		 * @return the new migrator instance
		 */
		public Migrator<T> build() {
			return new Migrator<>(modelValidator, schemaVersionAccessor, schemaValidator, cache, config);
		}
	}
}
