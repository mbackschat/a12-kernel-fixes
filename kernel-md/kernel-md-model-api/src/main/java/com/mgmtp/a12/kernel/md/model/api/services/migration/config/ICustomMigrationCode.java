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

package com.mgmtp.a12.kernel.md.model.api.services.migration.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.utils.UseOnlyAfterCloseConsultation;

import lombok.NonNull;

/**
 * If classes for executing custom code are specified in the custom configuration of the migration,
 * they must inherit from this class.
 * <br/>
 * Example: This class can be used for project-specific mass changes. In the project, there could
 * be fields for a special number that is checked via a regular expression (e.g. an insurance policy
 * number). If the regular expression would be extended, the customization could be done in all
 * models by a suitable implementation of this class.
 */
@UseOnlyAfterCloseConsultation
public interface ICustomMigrationCode {

	/**
	 * Transforms the given model.
	 *
	 * @param model
	 * 		the model. Is never {@code null}.
	 */
	void execute(@NonNull JsonNode model);
}
