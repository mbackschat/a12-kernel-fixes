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
package com.mgmtp.a12.kernel.md.model.migration.internal.config;

import static com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType.CONFIG_FILE_INVALID_JSON;
import static com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType.CONFIG_FILE_NOT_READABLE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType;
import com.mgmtp.a12.kernel.md.model.migration.internal.config.internal.InternalConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;

/**
 * Migration configuration loader.
 */
public final class MigratorConfigLoader {

	protected static final Logger LOG = LoggerFactory.getLogger(MigratorConfigLoader.class);

	/**
	 * Load migration config.
	 *
	 * @param internalFileName
	 * 		Name of the internal configuration file. Must not be
	 * 		<code>NULL</code>!
	 * @param customConfig
	 * 		The custom configuration or null {@code null} if no customization is
	 * 		needed.
	 * @return the migration config
	 */
	public MigrationConfig load(String internalFileName, ICustomMigrationConfig customConfig) {
		ICustomMigrationConfig internalCustomConfig = customConfig != null ? customConfig : new ICustomMigrationConfig() {
		};
		InternalConfig internalConfig = loadInternalConfig(internalFileName);
		return MigrationConfig.builder().internalConfig(internalConfig).customConfig(internalCustomConfig).build();
	}

	private InternalConfig loadInternalConfig(String internalFileName) {
		InternalConfig result = null;
		try (final InputStream config = getClass().getResourceAsStream(internalFileName)) {
			if (config == null) {
				throw new MigrationException(
						new MigrationError(MigrationErrorType.COFIG_FILE_NOT_FOUND)
								.withMessage("Input stream of configuration file must not be NULL!"));
			}
			final String configJSON = readConfigJSON(config);
			try {
				Validate.validState(configJSON != null);
				ObjectMapper mapper = new ObjectMapper();
				result = mapper.readValue(configJSON, InternalConfig.class);
			} catch (final Exception e) {
				throw new MigrationException(
						new MigrationError(CONFIG_FILE_INVALID_JSON).withMessage("Cannot parse JSON contents: " + e.getMessage()));
			}
		} catch (IOException e) { // can originate from closing the "config" stream
			if (result == null) {
				throw new RuntimeException(e);
			} else {
				LOG.error("The file '" + internalFileName + "' could be read, but an error occurred while closing it.", e);
			}
		}
		return result;
	}

	private static String readConfigJSON(InputStream config) {
		try {
			return IOUtils.toString(config, StandardCharsets.UTF_8);
		} catch (final IOException e) {
			throw new MigrationException(new MigrationError(CONFIG_FILE_NOT_READABLE).withMessage(e.getMessage()), e);
		}
	}
}
