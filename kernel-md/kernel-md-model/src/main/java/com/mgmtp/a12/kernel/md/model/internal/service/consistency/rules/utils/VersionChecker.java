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
package com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelContent;

public class VersionChecker {
	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+\\.\\d+)");
	private static final VersionFragments MODEL_VERSION_FRAGMENTS = new VersionFragments(DocumentModelContent.VERSION);

	private VersionChecker() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean matchesVersionPattern(String version) {
		return VERSION_PATTERN.matcher(version).matches();
	}

	public static boolean isModelSchemaVersionCompatible(String version) {
		VersionFragments vf = new VersionFragments(version);
		return MODEL_VERSION_FRAGMENTS.isValid && vf.isValid
				&& MODEL_VERSION_FRAGMENTS.major == vf.major
				&& MODEL_VERSION_FRAGMENTS.minor >= vf.minor;
	}

	public static boolean isMajorVersionEqual(String version1, String version2) {
		VersionFragments vf1 = new VersionFragments(version1);
		VersionFragments vf2 = new VersionFragments(version2);
		return vf1.major == vf2.major;
	}

	public static String getMaxVersion(String version1, String version2) {
		VersionFragments vf1 = new VersionFragments(version1);
		VersionFragments vf2 = new VersionFragments(version2);

		if (vf1.major == vf2.major) {
			return vf1.minor > vf2.minor ? version1 : version2;
		} else {
			return vf1.major > vf2.major ? version1 : version2;
		}
	}

	private static class VersionFragments {
		private final int major;
		private final double minor;
		private final boolean isValid;

		private VersionFragments(String version) {
			Matcher m = VERSION_PATTERN.matcher(version);
			if (m.matches()) {
				isValid = true;
				major = Integer.parseInt(m.group(1));
				minor = Double.parseDouble(m.group(2));
			} else {
				isValid = false;
				major = Integer.MIN_VALUE;
				minor = Double.NaN;
			}
		}
	}
}
