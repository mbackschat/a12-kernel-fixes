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

package com.mgmtp.a12.kernel.md.model.a12internal;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

import com.mgmtp.a12.kernel.md.model.api.IDocumentUniquenessCriterion;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * see {@link IDocumentUniquenessCriterion}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUniquenessCriterion implements Serializable {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return see {@link IDocumentUniquenessCriterion#getName()}.
	 * @param name the name of this uniqueness criterion.
	 */
	@Builder.Default
	private @NonNull String name = ""; // "" is not a valid value but this way we can safely mark it a NonNull
	/**
	 * @return see {@link IDocumentUniquenessCriterion#getFieldFullNames()}.
	 * @param fieldFullNames the new field full names that should make up this uniqueness criterion.
	 */
	@Builder.Default
	private @NonNull SequencedSet<String> fieldFullNames = new LinkedHashSet<>();
	/**
	 * @return see {@link IDocumentUniquenessCriterion#getErrorMessage()}
	 * @param errorMessage the new error messages. Use an empty {@link ILocalizedTextMap} to unset.
	 */
	@Builder.Default
	private @NonNull ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build();

}
