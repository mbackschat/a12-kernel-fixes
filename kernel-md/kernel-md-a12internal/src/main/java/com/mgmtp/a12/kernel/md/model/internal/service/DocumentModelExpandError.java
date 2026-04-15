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

package com.mgmtp.a12.kernel.md.model.internal.service;

import lombok.NonNull;

public enum DocumentModelExpandError {

	SUPPORTED_LOCALES_DONT_FIT(
			"Cannot expand include because included model [=%s] does not support all locales of the base model."),
	SELF_RECURSION("Cannot expand include because of self recursion. Model includes/imports itself."),
	MULTIPLE_ROOT_GROUPS(
			"Cannot expand include because included model [=%s] does not contain exactly one root group. Models which contain only type definitions have to be included differently."),
	NO_MODEL_REFERENCE("The corresponding model reference of the include group '%s' in [=%s] cannot be found."),
	UNRESOLVABLE_REFERENCE_MSG(
			"Cannot successfully expand model because the %s could not be resolved (unambiguously). [%s=%s]"),
	MISSING_REFERENCE_MSG(
			"Cannot successfully expand model because the %s has an empty reference."),
	REFERENCED_MODEL_NOT_RESOLVED("Cannot open included document model '%s'."),
	REPEATABLE_ROOT_GROUP("Cannot expand include because the root group of the included model [=%s] is repeatable."),
	ROOT_GROUP_WITHOUT_CHILDREN(
			"Cannot expand include because the root group of the included model [=%s] does not contain any elements or define an include, and the include uses 'includeLevel=SINGLE_RG'."),
	EMPTY_INCLUDED_DM("Included DMs must not be empty, but the included model [=%s] contains no root groups.");

	private String messageFormat;

	DocumentModelExpandError(@NonNull String messageFormat) {
		this.messageFormat = messageFormat;
	}

	public String getMessage(Object... args) {
		return String.format(messageFormat, args);
	}

}
