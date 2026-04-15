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
package com.mgmtp.a12.kernel.md.model.a12internal.services.join.item;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.JoiningType;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.Getter;
import lombok.NonNull;

/**
 * Element container, containing all information to join a {@link DocumentV2} via {@link DocumentModelJoiningService} with respect
 * to the given usage type.
 */
@Getter
@OnlyForUsage
public class SelectionJoiningItem extends JoiningItem {

	private final DocumentV2 selection;

	/**
	 * @param selection
	 * 		a selection specification in document format.
	 * 		<em>NOTE</em> If the selection document is constructed in code, consider using
	 *        {@link com.mgmtp.a12.kernel.mmtypings.mm_selectionmodel_2.views.MM_SelectionModel_2}
	 * 		for compile-time type checks against the corresponding meta model. See the getA12 documentation on
	 * 		<a
	 * 		href="https://docs.geta12.com/docs/#content:asciidoc,product:kernel,artifact:kernel-documentation-dev,scene:qpcVHT,anchor:generated_typed_accessors">Typed
	 * 		Accessor Classes for Compile-Time Type Checks & IDE-Autocomplete</a>
	 * 		for details.
	 */
	public SelectionJoiningItem(@NonNull DocumentV2 selection, @NonNull JoiningType joiningType) { // A12K-2824 NOTE
		super(joiningType);
		if (!joiningType.isSelectionType()) {
			throw new RuntimeException("Invalid usage type " + joiningType);
		}
		this.selection = selection;
	}
}
