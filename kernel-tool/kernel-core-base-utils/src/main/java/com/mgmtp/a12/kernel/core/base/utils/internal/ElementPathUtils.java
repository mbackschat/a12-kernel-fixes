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

package com.mgmtp.a12.kernel.core.base.utils.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class ElementPathUtils {
	public static final String SEPARATOR = "/";
	public static final char SEPARATOR_CHAR = '/';

	private ElementPathUtils() {
		throw new UnsupportedOperationException("This may not be instantiated.");
	}

	public static String createPath(String basePath, String... furtherPathParts) {
		if (Arrays.stream(furtherPathParts).anyMatch(StringUtils::isEmpty)) {
			throw new IllegalArgumentException("Further path parts must not be empty.");
		}
		String furtherPathPart = String.join(SEPARATOR, furtherPathParts);
		return createPath(basePath, furtherPathPart);
	}

	// this overload is a potential performance
	// optimization of the varargs version.
	// In the java performance test, 31 thresholds were breached after the original version with
	// a single toAdd String was generalized to the varargs version (and moved here from another file)
	// We have confirmed that the test breaches are in fact due to this change: they disappeared
	// after introducing this optimization, and re-appeared with version
	// 29.0.0-buildOnDemand.A12K-0000-like00092-without-perfffix that has the optimization reverted
	// We did not measure or estimate if the performance degradation is large or relevant in practice.
	public static String createPath(String basePath, String toAdd) {
		if (SEPARATOR.equals(basePath)) {
			return String.format("%s%s", basePath, toAdd);
		} else {
			return String.format("%s%s%s", basePath, SEPARATOR, toAdd);
		}
	}

	public static String getName(final String path) {
		return path.substring(path.lastIndexOf(SEPARATOR) + 1);
	}

	public static String getParentPath(final String path) {
		return path.substring(0, path.lastIndexOf(SEPARATOR));
	}

	public static String getParentPath(String path, int level) {
		for (int i = 0; i < level; i++) {
			path = getParentPath(path);
		}
		return path;
	}

	public static String getParentPathWithSlash(String fullName) {
		return fullName.substring(0, fullName.lastIndexOf(SEPARATOR) + 1);
	}

	public static List<String> getPathsBetween(String originPath, String targetPath) {
		Objects.requireNonNull(originPath);
		Objects.requireNonNull(targetPath);
		if (!isDescendantOrEqual(targetPath, originPath)) {
			return new ArrayList<>();
		}
		List<String> result = new ArrayList<>();
		int index = targetPath.indexOf(SEPARATOR, originPath.length() + 1);
		while (index > -1) {
			String path = targetPath.substring(0, index);
			result.add(path);
			index = targetPath.indexOf(SEPARATOR, path.length() + 1);
		}
		return result;
	}

	/**
	 * @return the path of the entity which is direct descendant of {@code parentPath} and is still
	 * 		part of {@code descendantPath} (or is {@code descendantPath} itself). It returns the
	 * 		empty string if {@code parentPath} is equal to {@code descendantPath} or if
	 *        {@code parentPath} is not an ancestor of {@code descendantPath}.
	 */
	public static String getChildPath(final String parentPath, final String descendantPath) {
		Validate.notBlank(parentPath);
		Validate.notBlank(descendantPath);
		if (parentPath.equals(descendantPath) || !isDescendantOrEqual(descendantPath, parentPath)) {
			return "";
		}
		int endIndex = descendantPath.indexOf(SEPARATOR, parentPath.length() + 1);
		if (endIndex > -1) {
			return descendantPath.substring(0, endIndex);
		} else {
			return descendantPath;
		}
	}

	/**
	 * @return a list with all ancestor paths of the given path. This means the given path is not
	 * 		part of the list. Is never {@code null}. The returned list keeps the order of the
	 * 		original path, e.g. if {@code "/G2/G_a/G1/G_b"} is given, the return list will be
	 *        {@code ["/G2", "/G2/G_a", "/G2/G_a/G1"]}
	 */
	public static List<String> getAncestorPaths(final String path) {
		List<String> ancestorPaths = new ArrayList<>();
		int index = path.indexOf(SEPARATOR, 1);
		while (index != -1) {
			String pathTmp = path.substring(0, index);
			ancestorPaths.add(pathTmp);
			index = path.indexOf(SEPARATOR, index + 1);
		}
		return ancestorPaths;
	}

	public static int getNumberOfPathParts(String path) {
		if (SEPARATOR.equals(path)) {
			return 0;
		}
		return StringUtils.countMatches(path, SEPARATOR);
	}

	/**
	 * @param descendantPath
	 * 		the path that should be tested to be below the other one. Must not be blank.
	 * @param path
	 * 		the path against which {@code descendantPath} should be tested. Must not be blank.
	 * @return {@code true} if the given {@code descendantPath} is a descendant of or equal to the given {@code path}, otherwise
	 *        {@code false}.
	 */
	public static boolean isDescendantOrEqual(String descendantPath, String path) {
		Validate.notBlank(descendantPath);
		Validate.notBlank(path);
		if (SEPARATOR.equals(path)) {
			return true;
		} else if (descendantPath.length() <= path.length()) {
			return descendantPath.equals(path);
		} else {
			return descendantPath.startsWith(path + SEPARATOR);
		}
	}

	/**
	 * @param descendantPath
	 * 		the path that should be tested to be below the other one. Must not be blank.
	 * @param path
	 * 		the path against which {@code descendantPath} should be tested. Must not be blank.
	 * @return {@code true} if the given {@code descendantPath} is a descendant of but not equal to the given {@code path},
	 * 		otherwise {@code false}.
	 */
	public static boolean isDescendant(String descendantPath, String path) {
		Validate.notBlank(descendantPath);
		Validate.notBlank(path);
		if (SEPARATOR.equals(path)) {
			return !SEPARATOR.equals(descendantPath);
		} else if (descendantPath.length() <= path.length()) {
			return false;
		} else {
			return descendantPath.startsWith(path + SEPARATOR);
		}
	}

	/**
	 * @param ancestorPath
	 * 		the path that should be tested to be above the other one. Must not be blank.
	 * @param path
	 * 		the path against which {@code ancestorPath} should be tested. Must not be blank.
	 * @return {@code true} if the given {@code ancestorPath} is an ancestor of or equal to the given {@code path}, otherwise
	 *        {@code false}.
	 */
	public static boolean isAncestorOrEqual(String ancestorPath, String path) {
		return isDescendantOrEqual(path, ancestorPath);
	}

	/**
	 * @param ancestorPath
	 * 		the path that should be tested to be below the other one. Must not be blank.
	 * @param path
	 * 		the path against which {@code ancestorPath} should be tested. Must not be blank.
	 * @return {@code true} if the given {@code ancestorPath} is an ancestor of but not equal to the given {@code path},
	 * 		otherwise {@code false}.
	 */
	public static boolean isAncestor(String ancestorPath, String path) {
		return isDescendant(path, ancestorPath);
	}

	/**
	 * @return {@code true} if the given {@code path} is a direct descendant from the given
	 *        {@code parentPath}, otherwise {@code false}.
	 */
	public static boolean isChild(String path, String parentPath) {
		Validate.notBlank(path);
		Validate.notBlank(parentPath);
		if (path.equals(parentPath) || !isDescendantOrEqual(path, parentPath)) {
			return false;
		}
		return path.indexOf(SEPARATOR, parentPath.length() + 1) == -1;
	}

	public static String absPath(String basePath, String relPath) {
		// NOTE: this seems similar to EntityUtils.relPath2AbsPath, but
		// EntityUtils.relPath2AbsPath deals with paths in the condition grammar,
		// and where we deal with paths that have nothing to do with the grammar.
		// So we should keep the code separate.
		List<String> path = new ArrayList<>(splitAbsPath(basePath));
		List<String> relpathParts = splitRelPath(relPath);

		for (String part : relpathParts) {
			if ("..".equals(part)) {
				path.remove(path.size() - 1);
			} else {
				path.add(part);
			}
		}
		return toAbsPathString(path);
	}

	public static String toAbsPathString(List<String> path) {
		return path.stream().flatMap(p -> Stream.of(SEPARATOR, p)).collect(Collectors.joining());
	}

	/**
	 * @return the minimal relative path from the given {@code basePath} to {@code absPath}.
	 */
	public static String relPath(String absPath, String basePath) {
		List<String> basePathParts = splitAbsPath(basePath);
		List<String> absPathParts = splitAbsPath(absPath);
		return relPath(absPathParts, basePathParts);
	}

	public static String relPath(List<String> absPathParts, List<String> basePathParts) {
		int commonAncestorLevel = commonAncestorLevel(absPathParts, basePathParts);

		List<String> relPathParts = new ArrayList<>(Collections.nCopies(basePathParts.size() - commonAncestorLevel - 1, ".."));
		relPathParts.addAll(absPathParts.subList(commonAncestorLevel + 1, absPathParts.size()));

		return String.join(SEPARATOR, relPathParts);
	}

	/**
	 * @param absPathA
	 * 		must not end with '/', and must start with '/' unless empty.
	 * 		e.g. "/A/B/C", "/A" and "" are valid, but "/A/B/" and "/" are not.
	 * @param absPathB
	 * 		must not end with '/', and must start with '/' unless empty
	 * @return the 'index' of the level of common ancestors, can be -1, which means there are no common ancestors
	 */
	public static int commonAncestorLevel(String absPathA, String absPathB) {
		List<String> partsA = splitAbsPath(absPathA);
		List<String> partsB = splitAbsPath(absPathB);
		return commonAncestorLevel(partsA, partsB);
	}

	/**
	 * @param partsA
	 * 		list of path parts, e.g. ["A", "B", "C"] is the path "/A/B/C".
	 * @param partsB
	 * 		list of path parts
	 * @return the 'index' of the level of common ancestors, can be -1, which means there are no common ancestors
	 */
	public static int commonAncestorLevel(List<String> partsA, List<String> partsB) {
		int shorterLength = Math.min(partsA.size(), partsB.size());
		for (int i = 0; i < shorterLength; i++) {
			if (!partsA.get(i).equals(partsB.get(i))) {
				return i - 1;
			}
		}
		return shorterLength - 1;
	}

	public static List<String> splitAbsPath(String absPath) {
		if (absPath == null || (!absPath.startsWith(SEPARATOR) && !StringUtils.isEmpty(absPath))) {
			throw new IllegalArgumentException("illegal abspath: '" + absPath +
					"' (abspath must start with '/' unless they are empty");
		}
		if (StringUtils.isEmpty(absPath)) {
			return List.of();
		}
		return splitRelPath(absPath.substring(1));
	}

	private static List<String> splitRelPath(String relPath) {
		if (relPath == null || (relPath.startsWith(SEPARATOR))) {
			throw new IllegalArgumentException("illegal relPath: '" + relPath + "'");
		}
		if (StringUtils.isEmpty(relPath)) {
			return List.of();
		}
		return Arrays.asList(relPath.split(SEPARATOR));
	}
}
