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

package com.mgmtp.a12.kernel.md.document.internal.service.utils;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.GroupInstanceImpl;

public class EntityInstanceUtils {

	public static Pair<String, List<Integer>> commonKeyForAllRepetitionsOfRepeatableGroup(IGroupInstance group) {
		int[] repetitions = Arrays.copyOf(group.getRepetitions(), group.getRepetitions().length);
		repetitions[repetitions.length - 1] = 0; // mask the repetition on the last level, so that all repetitions of that
		// level correspond to the same key
		return Pair.of(group.getPath(), Java8Utils.toBoxedList(repetitions));

	}

	// note: copied from parser's EntityUtils & adapted separator constant
	public static String[] getPathParts(String path) {
		while (path.startsWith(IEntityInstance.PATH_SEPARATOR)) {
			path = path.substring(1);
		}
		return StringUtils.split(path, IEntityInstance.PATH_SEPARATOR);
	}

	/**
	 * depth 0 corresponds to a root group
	 */
	public static IGroupInstance getAncestorGroup(IEntityInstance ei, int depth) {
		String[] pathParts = Arrays.copyOf(
				EntityInstanceUtils.getPathParts(ei.getPath()),
				depth + 1);
		int[] repetitions = Arrays.copyOf(
				ei.getRepetitions(),
				depth + 1);
		return new GroupInstanceImpl(
				IEntityInstance.PATH_SEPARATOR + String.join(IEntityInstance.PATH_SEPARATOR, pathParts),
				repetitions
		);
	}
}
