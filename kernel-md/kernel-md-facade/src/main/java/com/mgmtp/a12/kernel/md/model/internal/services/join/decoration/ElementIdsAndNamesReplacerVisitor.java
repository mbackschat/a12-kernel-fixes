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
package com.mgmtp.a12.kernel.md.model.internal.services.join.decoration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelRoot;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCode;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCodeUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.JoinProblem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.IdUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;

import lombok.Getter;

public class ElementIdsAndNamesReplacerVisitor extends DocumentModelVisitor {

	private final Locale locale;
	private final String anchorName;
	private final JoiningProblemReporter problemReporter;
	private final String decorationModelName;
	private final Element elementToBeDecorated;
	/**
	 * Mapping for the former and new full names of elements in a decoration model.
	 * <b>Note:</b> The order in which the elements were renamed and added to the map is
	 * important for later adapting the rules and computations.
	 */
	private final LinkedHashMap<String, EntityFullName> formerToNewFullNamesMapper;
	private final Set<String> fullNamesCache;
	@Getter
	private final Map<String, Pair<String, String>> newIdToFormerIdAndFullNameMapper;

	protected ElementIdsAndNamesReplacerVisitor(String decorationModelName, Element elementToBeDecorated,
			LinkedHashMap<String, EntityFullName> formerToNewFullNamesMapper, String anchorName, Locale locale,
			JoiningProblemReporter problemReporter) {
		this.decorationModelName = decorationModelName;
		this.elementToBeDecorated = elementToBeDecorated;
		this.formerToNewFullNamesMapper = formerToNewFullNamesMapper;
		this.fullNamesCache = new HashSet<>();
		this.newIdToFormerIdAndFullNameMapper = new HashMap<>();
		this.anchorName = anchorName;
		this.locale = locale;
		this.problemReporter = problemReporter;
	}

	@Override
	public VisitProcess visitElement(final Element element) {
		//set id of all decorated elements
		String formerId = element.getId();
		// update the id
		// if it is the anchor element we keep the original id
		element.setId(IdUtil.getIdForDecorationModel(elementToBeDecorated, element, anchorName));

		String formerShortName = element.getName();
		String formerFullName = determineFormerFullName(element);
		// if the name of the element contains the name of the anchor field, the new full name is changed below
		String newFullName = ElementUtils.getPath(element);

		newIdToFormerIdAndFullNameMapper.put(element.getId(), Pair.of(formerId, formerFullName));

		/*
		 * Error messages are also issued for intermittently occurring name collisions
		 * and the decoration fails. The reason for this is that the later adjustment of
		 * the rules and calculations can be kept simple this way.
		 * Intermittent name collisions occur when an element in the decoration model
		 * has the same path after the name change as an unchanged element in the same
		 * model.
		 */
		checkNameAndAddToCache(newFullName);

		if (formerShortName.contains(anchorName)) {
			element.setName(formerShortName.replaceFirst(anchorName, elementToBeDecorated.getName()));
			if (!formerShortName.equals(element.getName())) {
				newFullName = ElementUtils.getPath(element);
				checkNameAndAddToCache(newFullName);
			}
		}
		if (!formerFullName.equals(newFullName)) {
			formerToNewFullNamesMapper.put(formerFullName,
					EntityFullName.builder().fullName(newFullName).element(element).build());
		}
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	private String determineFormerFullName(Element element) {
		if (element.getParent() instanceof DocumentModelRoot) {
			return ElementUtils.getPath(element);
		}
		String parentPath = newIdToFormerIdAndFullNameMapper.get(element.getParent().getId()).getRight();
		return ElementPathUtils.createPath(parentPath, element.getName());
	}

	private void checkNameAndAddToCache(String fullName) {
		if (fullNamesCache.contains(fullName)) {
			problemReporter.reportProblem(JoinProblem.createError(elementToBeDecorated,
					ErrorCodeUtil.getMessageWithCode(ErrorCode.DuplicatePath, locale,
							ElementUtils.getPath(elementToBeDecorated), decorationModelName, fullName)));
		}
		fullNamesCache.add(fullName);
	}
}
