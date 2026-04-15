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
import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.md.model.api.IVariantFilter.FilterType;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Provides configuration for variant-specific filtering on {@link DocumentModel} {@link Element}s,
 * e.g. {@link Rule}s, {@link Field}s.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@OnlyForA12InternalUsage
public class VariantFilter implements Serializable {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return the corresponding {@link FilterType}. Is never {@code null}.
	 * @param filterType
	 * 		the new {@link FilterType}. May not be {@code null}.
	 */
	private @NonNull FilterType filterType;
	/**
	 * @return a list with the variants to which the {@link FilterType} from
	 *        {@link #getFilterType()} should be applied. Is never {@code null}. If no variants are
	 * 		defined yet, the list is empty.
	 * @param variants
	 * 		the new variants to which the {@link FilterType} from
	 *        {@link #getFilterType()}
	 * 		should be applied. May not be {@code null} or empty.
	 */
	@Builder.Default
	private @NonNull List<String> variants = new ArrayList<>();

}
