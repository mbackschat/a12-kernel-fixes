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

package com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable;

import static com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.AbstractImmutableGroupInstance.repIsNonEmpty;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.util.MapInvariantsAndNormalizations;

public final class SubGroupsInvariants implements MapInvariantsAndNormalizations<String, RepetitionsV2, SubGroupsInvariants> {
	public static final SubGroupsInvariants SINGLETON = new SubGroupsInvariants();

	private SubGroupsInvariants() {
	}

	@Override public void checkKey(String key) {
		validateNameOfFieldOrSubgroup(key);
	}

	@Override public boolean treatLikeAbsent(RepetitionsV2 value) {
		return !repIsNonEmpty(value);
	}

	@Override public RepetitionsV2 substituteAbsent() {
		return RepetitionsV2.empty();
	}

	static void validateNameOfFieldOrSubgroup(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("names of subgroups or fields must not be the empty string or null, " +
					"but got '" + name + "'");
		}
		if(name.contains("/")) {
			throw new IllegalArgumentException(
					"slashes are not allowed in field or subgroup names because they make paths ambiguous, but got " + name);
		}
	}
}
