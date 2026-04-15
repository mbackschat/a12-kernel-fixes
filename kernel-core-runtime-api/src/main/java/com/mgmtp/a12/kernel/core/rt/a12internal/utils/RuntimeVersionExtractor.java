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

package com.mgmtp.a12.kernel.core.rt.a12internal.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Provides functionality to derive the runtime version.
 * <br>
 * <b>Note:</b>
 * The 'runtime version' is generally not equal to the 'version' of the corresponding module,
 * because, for example in case of a minor patch that does not change the internal interface
 * between generated code and runtime, 'version' needs to be changed, but the so called 'runtime version'
 * can remain the same. This has the advantage for projects that they do not need to
 * re-generate code.
 */
@OnlyForUsage
public class RuntimeVersionExtractor {

	private static final Pattern PATTERN = Pattern.compile("^.*kernel-core-runtime(-ext-java)?-([\\d]+)-([\\d]+)(-([\\d]+))?-.*$");

	private RuntimeVersionExtractor() {
	}

	/**
	 * @param runtimeLibraryName
	 * @return {@link Optional<String>} that holds the runtime version to the given name of the 'kernel-core-runtime' library or
	 *        {@link Optional#empty()} if the given name does not specify a 'kernel-core-runtime' library.
	 * 		<br>
	 * 		<b>Note:</b>
	 * 		The returned value is generally not equal to the 'version' of the module,
	 * 		because, for example in case of a minor patch that does not change the internal interface
	 * 		between generated code and runtime, 'version' needs to be changed, but the returned value
	 * 		can remain the same. This has the advantage for the projects that they do not need to
	 * 		re-generate code.
	 */
	public static Optional<String> extractRuntimeVersion(String runtimeLibraryName) {
		if (runtimeLibraryName == null) {
			return Optional.empty();
		}
		Matcher m = PATTERN.matcher(runtimeLibraryName);
		if (m.matches()) {
			return m.group(5) == null ?
					// since version 27.5
					Optional.of(String.format("%s.%s", m.group(2), m.group(3))) :
					// before version 27.5
					Optional.of(String.format("%s.%s.%s", m.group(2), m.group(3), m.group(5)));
		}
		return Optional.empty();
	}
}
