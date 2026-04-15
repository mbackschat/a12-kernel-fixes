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
package com.mgmtp.a12.kernel.md.model.internal.betterbuilders;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.internal.impl.LocalizedTextMapImpl;

public class BuilderUtil {
	public static final String PATH_SEPARATOR = "/";
	private static final Pattern extraInfo = Pattern.compile("(?<name>[^\\[]+)(\\[(?<repeat>\\d+)])?");

	private BuilderUtil() {
		throw new IllegalStateException("Utility class");
	}

	protected static <E> List<E> depthFirst(List<E> rootNodeList, Function<E, List<E>> provideChildren) {
		List<E> ret = new ArrayList<>();
		if (rootNodeList.isEmpty()) {
			return ret;
		}
		Deque<E> work = new ArrayDeque<>(rootNodeList);
		E current = work.pop();
		while (current != null) {
			ret.add(current);

			List<E> childrenWork = new ArrayList<>(
					Optional.ofNullable(provideChildren.apply(current)).orElse(Collections.emptyList()));
			if (!childrenWork.isEmpty()) {
				// revert to ensure the original order
				for (E child : childrenWork.reversed()) {
					work.push(child);
				}
			}
			current = work.peek() != null ? work.pop() : null;
		}
		return ret;
	}

	protected static String pathPartOf(String fullPath) {
		return StringUtils.substringBeforeLast(fullPath, PATH_SEPARATOR);
	}

	protected static String lastPartOf(String fullPath) {
		return StringUtils.substringAfterLast(fullPath, PATH_SEPARATOR);
	}

	protected static List<String> explodeParts(String fullPath) {
		List<String> parts = Arrays.stream(fullPath.split(PATH_SEPARATOR)).filter(s -> !s.isEmpty()).toList();
		List<String> ret = new ArrayList<>();
		for (int i = 1; i <= parts.size(); i++) {
			ret.add(parts.subList(0, i).stream()
					.collect(Collectors.joining(PATH_SEPARATOR, fullPath.startsWith(PATH_SEPARATOR) ? PATH_SEPARATOR : "", "")));
		}
		return ret;
	}

	protected static int getRepeat(String arg) {
		Matcher m = extraInfo.matcher(arg);
		if (m.matches()) {
			return Optional.ofNullable(m.group("repeat")).map(Integer::parseInt).orElse(1);

		} else {
			String msg = String.format("The argument '%s' does not match the pattern '%s'", arg, extraInfo.pattern());
			throw new RuntimeException(msg);
		}
	}

	protected static String stripRepeats(String arg) {
		return arg.replaceAll("\\[\\d+]", "");
	}

	public static Map<String, Element> initLookup(DocumentModel documentModel) {
		Function<Element, List<Element>> getChildren = element -> {
			if (element instanceof Group group) {
				return group.getElements();
			} else {
				return Collections.emptyList();
			}
		};
		return BuilderUtil.depthFirst(documentModel.getContent().getModelRoot().getElements(), getChildren)
				.stream()
				.collect(Collectors.toMap(Element::getId, Function.identity()));
	}

	public static ILocalizedTextMap getTextMap(DocumentModel documentModel, String text) {
		return new LocalizedTextMapImpl(documentModel.getHeader().getLocales().stream()
				.collect(Collectors.toMap(Function.identity(), e -> text)));
	}
}
