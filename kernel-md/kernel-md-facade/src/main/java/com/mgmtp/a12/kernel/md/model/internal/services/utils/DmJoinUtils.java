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

package com.mgmtp.a12.kernel.md.model.internal.services.utils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.internal.services.join.JoiningType;
import com.mgmtp.a12.kernel.md.model.internal.services.join.item.SelectionJoiningItem;
import com.mgmtp.a12.kernel.mmtypings.ct_selection_v1.views._ct_selection_v1._selection.Escaped_Default;
import com.mgmtp.a12.kernel.mmtypings.mm_selectionmodel_2.views.MM_SelectionModel_2;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

public class DmJoinUtils {

	public static @NonNull DocumentModel withoutRulesAndComputations(@NonNull DocumentModel dm) {
		return withoutRulesAndComputations(dm, p -> {
			if (p.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) { // this can probably not happen
				throw new RuntimeException(p.getMessage());
			}
		}).orElseThrow();
	}

	/**
	 * @param dm
	 * 		the DM to remove the rules and computations from. Will not be modified.
	 * @param pr
	 * 		reports encountered problems using Locale.US.
	 * @return the provided {@code dm} without rules and computations, or {@link Optional#empty()} if errors were encountered.
	 */
	public static Optional<DocumentModel> withoutRulesAndComputations(
			@NonNull DocumentModel dm,
			@NonNull IProblemReporter pr
	) {
		return DocumentModelJoiningService.join(
				dm,
				List.of(deselectRulesAndCompsJoiningItem()),
				Locale.US,
				pr // not sure it's even possible for this operation to produce any errors
		);
	}

	private static SelectionJoiningItem deselectRulesAndCompsJoiningItem() {
		var sp = MM_SelectionModel_2._pointer();
		return new SelectionJoiningItem(
				MM_SelectionModel_2._empty()
						._with(sp.content().data().escaped_Default(), Escaped_Default.Selected)
						._with(sp.content().validation().escaped_Default(), Escaped_Default.Unselected)
						._with(sp.content().computation().escaped_Default(), Escaped_Default.Unselected)
						._unwrap(),
				JoiningType.SELECTION
		);
	}
}
