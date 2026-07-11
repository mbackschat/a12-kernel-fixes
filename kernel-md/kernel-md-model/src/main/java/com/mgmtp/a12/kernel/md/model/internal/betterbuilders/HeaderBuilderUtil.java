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

package com.mgmtp.a12.kernel.md.model.internal.betterbuilders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderFactory;
import com.mgmtp.a12.model.header.ModelReference;

/**
 * see also {@link com.mgmtp.a12.kernel.md.model.internal.service.utils.ModelHeaderUtils}
 */
public class HeaderBuilderUtil {
	private HeaderBuilderUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * ModelReferences are conceptually a set, but represented as a list.
	 * This normalizes their order to avoid meaningless differences between semantically equal DMs.
	 */
	public static void normalizeModelRefsList(DocumentModel dm) {
		List<ModelReference> refs = dm.getHeader().getModelReferences();
		if(refs == null) {
			List<ModelReference> normalizedRefs = List.of();
			dm.setHeader(toBuilder(dm.getHeader()).withModelReferences(normalizedRefs).build());
			return;
		}
		List<ModelReference> sorted = new ArrayList<>(refs);
		sorted.sort(Comparator.comparing(ModelReference::getReference).thenComparing(ModelReference::getPurpose));
		try {
			// prefer in-place update to avoid rebuilding the header
			refs.clear();
			refs.addAll(sorted);
		} catch (UnsupportedOperationException e) {
			// The modelReferences list may be immutable (e.g. from .toList()), so we fall back to
			// rebuilding the header. Note: toBuilder is slightly lossy (e.g. null labels → empty list),
			// so we avoid it when possible. Clean fixes would be: make toBuilder faithfully preserve
			// null vs empty, or make the model/serialization treat absent and empty lists as equivalent.
			dm.setHeader(toBuilder(dm.getHeader()).withModelReferences(sorted).build());
		}
	}

	// This could be "upstreamed" to the HeaderFactory class in base-model-api
	public static HeaderFactory.Builder toBuilder(final Header header) {
		HeaderFactory.Builder builder = new HeaderFactory.Builder()
				.withId(header.getId())
				.withModelType(header.getModelType())
				.withModelVersion(header.getModelVersion());

		if (header.getDescription() != null) {
			builder = builder.withDescription(header.getDescription());
		}

		return builder
				.withLabels(Optional.ofNullable(header.getLabels()).orElse(List.of()))
				.withLocales(Optional.ofNullable(header.getLocales()).orElse(List.of()))
				.withAnnotations(Optional.ofNullable(header.getAnnotations()).orElse(List.of()))
				.withModelReferences(Optional.ofNullable(header.getModelReferences()).orElse(List.of()));
	}
}
