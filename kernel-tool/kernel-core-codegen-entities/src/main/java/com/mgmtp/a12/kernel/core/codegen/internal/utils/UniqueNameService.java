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
package com.mgmtp.a12.kernel.core.codegen.internal.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;

/**
 * This class provides methods to convert full names into unique names. These unique names are used
 * as rule method names or similar.
 */
public class UniqueNameService {

	private UniqueNameService() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Generates a Map from full name to unique name from a set of full names. The unique name will
	 * only differ from the short name of the given full name, if the short name is used more than
	 * once.
	 *
	 * @param fullNames
	 * 		the full names of the fields.
	 * @return Map containing the unique names
	 */
	public static Map<String, String> makeUniqueFieldNames(Set<String> fullNames) {
		return makeUniqueNames(fullNames, "");
	}

	/**
	 * Generates a Map from full name to unique name from a set of full names. The unique name
	 * will consist of the short name of the given full name, with an added prefix "mkv_" (to avoid
	 * collisions with reserved words in in the generated source code) and an optional suffix if the
	 * short name is used more than once.
	 *
	 * @param fullNames
	 * 		the full names of the rules.
	 * @return Map containing the unique names
	 */
	public static Map<String, String> makeUniqueRuleNames(Set<String> fullNames) {
		return makeUniqueNames(fullNames, "mvk_");
	}

	private static Map<String, String> makeUniqueNames(Set<String> fullNames, String prefix) {
		assert (fullNames != null);

		Set<String> usedUniqueNames = new HashSet<>();

		Map<String, String> fullName2ShortName = fullNames.stream()
				.collect(Java8Utils.toTreeMap(Function.identity(), fullName -> prefix + EntityUtils.getShortName(fullName)));

		final Set<String> allShortNames = new HashSet<>();
		Set<String> duplicatedShortNames = fullName2ShortName.values().stream()
				.filter(sn -> !allShortNames.add(sn))
				.collect(Collectors.toSet());

		// No duplicates -> finished
		if (duplicatedShortNames.isEmpty()) {
			return fullName2ShortName;
		}

		return Java8Utils.mapMapValues(fullName2ShortName,
				sn -> makeUniqueName(sn, duplicatedShortNames, usedUniqueNames, allShortNames));
	}

	/**
	 * Create a unique name.
	 * <p>
	 * If the short name is not in duplicatedShortNames, the short name itself will be returned and
	 * no further operation is performed.
	 * <p>
	 * Otherwise, the name will be &lt;shortName&gt;_&lt;n&gt;, where n is the smallest non-negative
	 * integer, so that the name is neither contained in usedUniqueNames nor in allShortNames. The
	 * resulting name will be added to usedUniqueNames.
	 *
	 * @param shortName
	 * 		the short name to be made unique.
	 * @param duplicatedShortNames
	 * 		the short names, that are duplicates.
	 * @param usedUniqueNames
	 * 		the already used unique names. The resulting name will be added, if it is not
	 * 		already unique before.
	 * @param allShortNames
	 * 		all short names used.
	 * @return the unique name as described above.
	 */
	private static String makeUniqueName(String shortName, Set<String> duplicatedShortNames, Set<String> usedUniqueNames,
			Set<String> allShortNames) {
		if (!duplicatedShortNames.contains(shortName)) {
			return shortName;
		}
		String uniqueName = IntStream.iterate(0, i -> i + 1)
				.mapToObj(i -> shortName + "_" + i)
				.filter(un -> !usedUniqueNames.contains(un) && !allShortNames.contains(un))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Too many unique fields with name " + shortName));
		usedUniqueNames.add(uniqueName);

		return uniqueName;
	}
}
