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

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Describes occurred migration error.
 */
public class MigrationError {

	private final MigrationErrorType errorType;
	private String elementId;
	private String message;

	/**
	 * Instantiates a new Migration error.
	 *
	 * @param errorType
	 * 		the error type
	 */
	public MigrationError(final MigrationErrorType errorType) {
		Validate.notNull(errorType, "errorType must not be NULL");
		this.errorType = errorType;
	}

	// BUILDER Methods -----------------------------------------------

	/**
	 * With element id.
	 *
	 * @param elementId
	 * 		the element id
	 * @return the migration error
	 */
	public MigrationError withElementId(final String elementId) {
		this.elementId = elementId;
		return this;
	}

	/**
	 * With message.
	 *
	 * @param message
	 * 		the message
	 * @return the migration error
	 */
	public MigrationError withMessage(final String message) {
		this.message = message;
		return this;
	}

	// Getter Methods -----------------------------------------------

	/**
	 * Gets error type.
	 *
	 * @return the error type
	 */
	public MigrationErrorType getErrorType() {
		return errorType;
	}

	/**
	 * Gets element id.
	 *
	 * @return the element id
	 */
	public String getElementId() {
		return elementId;
	}

	/**
	 * Gets message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("errorType", errorType)
				.append("elementId", elementId)
				.append("message", message)
				.toString();
	}
}
