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
package com.mgmtp.a12.kernel.md.model.internal.service.mapping;

import com.mgmtp.a12.kernel.md.model.a12internal.VariantFilter;
import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.api.IVariantFilter;

/**
 * Provides filtering of {@link com.mgmtp.a12.kernel.md.model.internal.service.mapping.Rule}s based
 * on {@link VariantFilter}s.
 */
public class VariantFiltering {

	private VariantFiltering() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean fieldIsAcceptedByFilter(final IField field, final String variant) {
		return field.getVariantFilter()
				.map(filter -> filterAcceptsVariant(filter, variant))
				.orElse(true);
	}

	public static boolean ruleIsAcceptedByFilter(final IRule rule, final String variant) {
		return rule.getVariantFilter()
				.map(filter -> filterAcceptsVariant(filter, variant))
				.orElse(true);
	}

	public static boolean computationIsAcceptedByFilter(final IComputation computation, final String variant) {
		return computation.getVariantFilter()
				.map(filter -> filterAcceptsVariant(filter, variant))
				.orElse(true);
	}

	private static boolean filterAcceptsVariant(final IVariantFilter variantFilter, final String variant) {
		final boolean variantMatchesFilter = variant != null
				&& variantFilter.getVariants()
				.stream()
				.anyMatch(a -> a.equals(variant));

		switch (variantFilter.getFilterType()) {
			case ALL_EXCEPT:
				return !variantMatchesFilter;
			case ONLY:
				return variantMatchesFilter;
			default:
				throw new UnsupportedOperationException(
						"IVariant#FilterType '" + variantFilter.getFilterType().name() + " ' is unknown.");
		}
	}
}
