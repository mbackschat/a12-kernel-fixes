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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.core.customfieldtype.internal.CustomFieldTypeApiConstants;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.RuntimeApiConstants;

public class VersionCompatibilityCheck {

	private static final Logger LOG = LoggerFactory.getLogger(VersionCompatibilityCheck.class);

	public static void checkRuntimeApiCompatibleWithRuntime() {
		final String warnOnlyProperty = "com.mgmtp.a12.kernel.versionCompat.runtimeApi.warnOnly";
		// workaround for better error message in case of versions prior to introduction of api version compat checks
		if (!doesClassExist("com.mgmtp.a12.kernel.core.rt.a12internal.utils.RuntimeApiConstants")) {
			logOrThrow(
					"The version of kernel-core-runtime-api is too old, >= 25.6.0 is required for api compat checks to work.",
					warnOnlyProperty);
			return; // return to avoid subsequent ClassNotFoundException when warnOnly is set
		}

		String error = isCompatible(
				"kernel-core-runtime", Constants.RUNTIME_VERSION, Constants.OLDEST_COMPATIBLE_RUNTIME_API_VERSION,
				"kernel-core-runtime-api", RuntimeApiConstants.API_VERSION,
				RuntimeApiConstants.OLDEST_COMPATIBLE_RUNTIME_AND_GENCODE_VERSION);
		if (error != null) {
			logOrThrow(error, warnOnlyProperty);
		}
	}

	public static void checkCustomFieldTypeApiCompatibleWithRuntime() {
		final String warnOnlyProperty = "com.mgmtp.a12.kernel.versionCompat.customFieldtypeApi.warnOnly";
		// workaround for better error message in case of versions prior to introduction of api version compat checks
		if (!doesClassExist("com.mgmtp.a12.kernel.core.customfieldtype.internal.CustomFieldTypeApiConstants")) {
			logOrThrow(
					"The version of kernel-core-customfieldtype-api is too old, >= 25.6.0 is required for api compat checks to work.",
					warnOnlyProperty);
			return; // return to avoid subsequent ClassNotFoundException when warnOnly is set
		}

		String error = isCompatible(
				"kernel-core-runtime", Constants.RUNTIME_VERSION, Constants.OLDEST_COMPATIBLE_CUSTOMFIELDTYPE_API_VERSION,
				"kernel-core-customfieldtype-api", CustomFieldTypeApiConstants.API_VERSION,
				CustomFieldTypeApiConstants.OLDEST_COMPATIBLE_RUNTIME_AND_GENCODE_VERSION);
		if (error != null) {
			logOrThrow(error, warnOnlyProperty);
		}
	}

	public static void checkGeneratedCodeCompatibleWithRuntime(String generatedCodeVersion) {
		if (!Constants.RUNTIME_VERSION.equals(generatedCodeVersion)) {
			throw new IllegalStateException(
					"The version of the generated code does not match the version of the used runtime! Please check your class loader hierarchy: Only one generated code package per class loader allowed! "
							+ Constants.RUNTIME_VERSION + " (runtime) and " + generatedCodeVersion + " (generated code)");
		}
	}

	private static boolean doesClassExist(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static void logOrThrow(String message, String warnOnlyPropertyName) {
		if (Boolean.getBoolean(warnOnlyPropertyName)) {
			LOG.error(message);
		} else {
			throw new RuntimeException(message);
		}
	}

	/**
	 * return null if compatible, otherwise an error message that explains the incompatibility
	 */
	static String isCompatible(
			String aName, String aVersion, String oldestBAcceptedByA,
			String bName, String bVersion, String oldestAAcceptedByB) {
		if (isSpecialNoComparePlaceholderVersion(aVersion) || isSpecialNoComparePlaceholderVersion(bVersion)) {
			return null;
		}
		String error = "";
		if (compareVersions(aVersion, oldestAAcceptedByB) < 0) {
			error += String.format("The version %s of %s is older than the oldest version that %s (version %s) accepts: %s",
					aVersion, aName, bName, bVersion, oldestAAcceptedByB);
		}
		if (compareVersions(bVersion, oldestBAcceptedByA) < 0) {
			if (!StringUtils.isEmpty(error)) {
				error += " ";
			}
			error += String.format("The version %s of %s is older than the oldest version that %s (version %s) accepts: %s",
					bVersion, bName, aName, aVersion, oldestBAcceptedByA);
		}
		return StringUtils.isEmpty(error) ? null : error;
	}

	/**
	 * Method is package private for unit testing.
	 *
	 * @param vA
	 * 		- version A
	 * @param vB
	 * 		- version B
	 * @return 0 if major, minor and patch part of both version strings are equal (build number, snapshot
	 * 		and so forth is ignored here), <0 if version A is smaller, >0 if version A is bigger. A missing snapshot version will be
	 * 		treated like a snapshot version of 0.
	 */
	static int compareVersions(String vA, String vB) {
		return SemanticVersion.create(vA).compareTo(SemanticVersion.create(vB));
	}

	/**
	 * In the repo, we want to use a constant placeholder version for the runtime namespace in order to avoid merge conflics
	 * after the version update after an new release (EXTP-4371).
	 * "Real" Versions will only be set dynamically be the version updater before builds that deloy artifacts.
	 * To avoid compat-check failures for other builds, we ignore the placeholder version "_00_0".
	 */
	static boolean isSpecialNoComparePlaceholderVersion(String version) {
		SemanticVersion v = SemanticVersion.create(version);
		return v.major == 0 && v.minor == 0 && v.patch == 0;
	}

	private VersionCompatibilityCheck() {
		throw new RuntimeException("utility class");
	}

	private static class SemanticVersion implements Comparable<SemanticVersion> {
		private static final Pattern pSemanticVersion = Pattern.compile("([\\d]+)\\.([\\d]+)(\\.([\\d]+)(-.*)?)?");

		private final int major;
		private final int minor;
		private final int patch;

		private SemanticVersion(int major, int minor, int patch) {
			this.major = major;
			this.minor = minor;
			this.patch = patch;
		}

		private static SemanticVersion create(String v) {
			Matcher m = pSemanticVersion.matcher(v);
			if (!m.matches()) {
				throw new IllegalArgumentException(
						String.format("Version '%s' does not match the expected pattern '%s'.", v, pSemanticVersion.toString()));
			}
			return new SemanticVersion(
					Integer.parseInt(m.group(1)),
					Integer.parseInt(m.group(2)),
					m.group(4) != null ? Integer.parseInt(m.group(4)) : 0 // accept not-given patch version as shorthand for 0
			);
		}

		@Override
		public int compareTo(SemanticVersion other) {
			int cmp = Integer.compare(major, other.major);
			if (cmp != 0) {
				return cmp;
			}
			cmp = Integer.compare(minor, other.minor);
			if (cmp != 0) {
				return cmp;
			}
			return Integer.compare(patch, other.patch);
		}
	}
}
