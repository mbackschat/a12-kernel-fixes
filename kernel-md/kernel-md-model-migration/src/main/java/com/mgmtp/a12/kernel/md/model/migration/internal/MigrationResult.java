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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;

import lombok.Getter;
import lombok.Setter;

/**
 * Result of Migration.
 */
public class MigrationResult implements MigrationLogger {

	private final List<MigrationError> errors = new ArrayList<>();
	private final List<MigrationInfo> infos = new ArrayList<>();
	@Getter @Setter private MigrationResultType type;
	@Getter @Setter private String sourceModelVersion;
	@Getter @Setter private String targetModelVersion;

	/**
	 * Instantiates a new Migration result.
	 *
	 * @param type
	 * 		the type of result
	 */
	public MigrationResult(final MigrationResultType type) {
		this.type = type;
	}

	public void catchAndReportErrorsDuring(Runnable runnnable) {
		try {
			runnnable.run();
		} catch (MigrationException e) {
			addError(e.getError());
		} catch (Exception e) {
			addError(new MigrationError(MigrationErrorType.UNKNOWN_ERROR, e.getMessage()));
		}
	}

	/**
	 * Gets all migration errors.
	 *
	 * @return the errors
	 */
	public List<MigrationError> getErrors() {
		return Collections.unmodifiableList(errors);
	}

	/**
	 * Gets all migration infos.
	 *
	 * @return the errors
	 */
	public List<MigrationInfo> getInfos() {
		return Collections.unmodifiableList(infos);
	}

	/**
	 * Add occurred migration error.
	 *
	 * @param migrationError
	 * 		the migration error
	 */
	@Override
	public void addError(final MigrationError migrationError) {
		type = MigrationResultType.FAILED;
		errors.add(migrationError);
	}

	/**
	 * Add a migration info.
	 *
	 * @param migrationInfo
	 * 		the migration error
	 */
	@Override
	public void addInfo(final MigrationInfo migrationInfo) {
		infos.add(migrationInfo);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("type", type)
				.append("sourceModelVersion", sourceModelVersion)
				.append("targetModelVersion", targetModelVersion)
				.append("errors", errors)
				.append("infos", infos)
				.toString();
	}
}
