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

/**
 * Migration error type.
 */
public enum MigrationErrorType {
	UNKNOWN_ERROR,
	COFIG_FILE_NOT_FOUND,
	CONFIG_FILE_NOT_READABLE,
	CONFIG_FILE_INVALID_JSON,
	CONFIG_INVALID,
	FIELD_NOT_FOUND,
	UNSUPPORTED_SOURCE_MODEL_VERSION,
	MODEL_VERSION_WRONG,
	MODEL_CONTENT_NOT_READABLE,
	MODEL_CONTENT_INVALID_XML,
	MODEL_CONTENT_UNEXPECTED_SOURCE_VERSION,
	MODEL_CONTENT_NOT_SUPPORTED,
	MODEL_CONTENT_RESULT_INVALID,
	MODEL_CONTENT_RESULT_NOT_WRITABLE,
	STEP_UNSUPPORTED_TYPE,
	STEP_NOT_CREATABLE,
	STEP_MIGRATION_ERROR,
	STEP_UNEXPECTED_ELEMENT,
	SERIALIZATION_ERROR,
	TYPE_DEF_MISSING,
	TYPE_DEF_ID_COLLISION_FOR_NONUNIQE_DM_ID,
	INCLUDED_TYPE_DEF_ID_COLLISION,
	UNKNOWN_REFERENCED_MODEL,
	UNKNOWN_FIELD_ID,
	COMPUTATION_STATEMENT_SPLIT_ERROR
}
