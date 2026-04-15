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

package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.ElementWrapper;

/**
 * Corresponding class to {@link ProblemUtils}, but for document models.
 */
public class DMProblemUtils {

	private DMProblemUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * @return a string in which the origin of the given {@link IProblem} can be immediately
	 * 		identified in the document model (format: &lt;element type name&gt; with path '&lt;path&gt;', for example:
	 * 		Field with path '/root/group/fieldname'). If the origin is {@code null}, an empty
	 * 		string is returned.
	 */
	public static String convertOriginToErrorMessagePrefix(IProblem problem) {
		Object origin = problem.getSource();
		if (origin == null) {
			return "";
		} else if (origin instanceof Element element) {
			return String.format("%s with path '%s': ",
					origin.getClass().getSimpleName(), ElementUtils.getPath(new ElementWrapper(element)));
		} else {
			// this should not happen. If additional cases occur, we don't want a silent fallback, but want to notice the need for
			// an explicit specific implementation case, because the error messages are user-facing
			throw new IllegalArgumentException("The java type '" + origin.getClass().getName() + "' is not supported.");
		}
	}

	/**
	 * @return a string with the given error message in the form describe in
	 *        {@link #convertOriginToErrorMessagePrefix(IProblem)} separated through
	 *        {@link System#lineSeparator()}
	 */
	public static String join(List<IProblem> problems) {
		return problems.stream()
				.map(p -> DMProblemUtils.convertOriginToErrorMessagePrefix(p) + p.getMessage())
				.collect(Collectors.joining(System.lineSeparator()));
	}

}
