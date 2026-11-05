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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;

public class ExpansionPurposes {
	private ExpansionPurposes() {
	}

	public static final String DM_PURPOSE_INCLUDE = "include";
	public static final String DM_PURPOSE_TYPEDEF = "typeDefinitions";
	public static final Set<String> DM_MANAGED_PURPOSES =
			// we need a set that won't crash on .contains(null), so Set.of() alone won't do
			Collections.unmodifiableSet(new HashSet<>(Set.of(DM_PURPOSE_INCLUDE, DM_PURPOSE_TYPEDEF)));
	public static final String CM_PURPOSE_BASE = "combination-base";
	public static final String CM_PURPOSE_ADDITION = "combination-addition";
	public static final String CM_PURPOSE_SELECTION = "combination-selection";
	public static final String CM_PURPOSE_DECORATION = "combination-decoration";
	public static final Set<String> CM_MANAGED_PURPOSES =
			// we need a set that won't crash on .contains(null), so Set.of() alone won't do
			Collections.unmodifiableSet(new HashSet<>(Set.of(CM_PURPOSE_BASE, CM_PURPOSE_ADDITION, CM_PURPOSE_SELECTION,
					CM_PURPOSE_DECORATION)));
	public static final Set<String> MANAGED_PURPOSES =
			// we need a set that won't crash on .contains(null), so Set.of() alone won't do
			Collections.unmodifiableSet(Java8Utils.setUnion(DM_MANAGED_PURPOSES, CM_MANAGED_PURPOSES));
}
