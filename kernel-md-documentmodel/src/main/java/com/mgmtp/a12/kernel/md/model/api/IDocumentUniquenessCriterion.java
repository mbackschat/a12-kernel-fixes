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

package com.mgmtp.a12.kernel.md.model.api;

import java.io.Serializable;
import java.util.SequencedSet;
import java.util.Set;

import lombok.NonNull;

/**
 * A document uniqueness criterion defines a set of fields whose combination of values should be unique over all documents of this
 * model. This uniqueness is not ensured by any Kernel APIs - in A12, Data Services will check such uniqueness criteria.
 */
public interface IDocumentUniquenessCriterion extends Serializable {
	/**
	 * @return the name of this uniqueness criterion. In a valid Document Model this {@link String} will not be empty.
	 */
	@NonNull String getName();

	/**
	 * @return the fields that make up this uniqueness criterion as a {@link Set} of field full names. In a valid Document Model
	 * 		this {@link Set} will not be empty.
	 */
	@NonNull SequencedSet<String> getFieldFullNames();

	/**
	 * @return the error messages per {@link java.util.Locale} that should be reported if the uniqueness criterion is violated /
	 * 		would be violated by a document. If no error message was defined, the {@link ILocalizedTextMap} is empty and the default
	 * 		error messages will be reported.
	 */
	@NonNull ILocalizedTextMap getErrorMessage();
}
