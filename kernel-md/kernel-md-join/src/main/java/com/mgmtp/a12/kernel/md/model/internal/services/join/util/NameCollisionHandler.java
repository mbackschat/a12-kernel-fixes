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
package com.mgmtp.a12.kernel.md.model.internal.services.join.util;

import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCode;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCodeUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.JoinProblem;
import com.mgmtp.a12.model.header.Header;

/**
 * Helper class to handle name collisions during DM-joins.
 */
public class NameCollisionHandler {

	private static final Map<Class<?>, ErrorCode> errorMessageFragmentStartCodeByClass = Map.of(
			Group.class, ErrorCode.ErrorMessageFragmentStartGroup,
			Rule.class, ErrorCode.ErrorMessageFragmentStartRule,
			Field.class, ErrorCode.ErrorMessageFragmentStartField,
			Computation.class, ErrorCode.ErrorMessageFragmentStartComputation,
			Header.class, ErrorCode.ErrorMessageFragmentStartHeader
	);
	private static final Map<Class<?>, ErrorCode> errorMessageFragmentMiddleCodeByClass = Map.of(
			Group.class, ErrorCode.ErrorMessageFragmentMiddleGroup,
			Rule.class, ErrorCode.ErrorMessageFragmentMiddleRule,
			Field.class, ErrorCode.ErrorMessageFragmentMiddleField,
			Computation.class, ErrorCode.ErrorMessageFragmentMiddleComputation
	);

	private final String dmToJoinDetails;
	private final Locale locale;
	private final JoiningProblemReporter problemReporter;

	public NameCollisionHandler(String dmToJoinDetails, Locale locale, JoiningProblemReporter problemReporter) {
		this.dmToJoinDetails = dmToJoinDetails;
		this.locale = locale;
		this.problemReporter = problemReporter;
	}

	/**
	 * Builds meaningful error message and reports it as {@link JoinProblem} to the {@link JoiningProblemReporter}
	 *
	 * @param newElement
	 * 		the new element to be added by join
	 * @param existingElement
	 * 		the existing element of the root model
	 * @param elementFullName
	 * 		the full name of the colliding elements
	 */
	public void handleNameCollision(Element newElement, Element existingElement, String elementFullName) {
		handleNameCollision(ErrorCode.NameCollision, newElement.getClass(), existingElement.getClass(), newElement,
				elementFullName);
		handleNameCollision(ErrorCode.NameCollision, newElement.getClass(), existingElement.getClass(), existingElement,
				elementFullName);
	}

	/**
	 * Builds meaningful error message and reports it as {@link JoinProblem} to the {@link JoiningProblemReporter}
	 *
	 * @param newElementClass
	 * 		the class of the new element to be added by join
	 * @param existingElementClass
	 * 		the class of the existing element of the root model
	 * @param element
	 * 		source of the problem
	 * @param elementFullName
	 * 		the full name of the colliding elements
	 */
	public void handleNameCollision(ErrorCode errorCode, Class<?> newElementClass, Class<?> existingElementClass,
			Element element, String elementFullName) {
		ErrorCode errMsgFragment1Code = errorMessageFragmentStartCodeByClass.get(newElementClass);
		ErrorCode errMsgFragment2Code = errorMessageFragmentMiddleCodeByClass.get(existingElementClass);

		String errMsgFragment1 = ErrorCodeUtil.getLocalizedMessageFragment(errMsgFragment1Code, locale);
		String errMsgFragment2 = ErrorCodeUtil.getLocalizedMessageFragment(errMsgFragment2Code, locale);
		String joinProblemMessage = ErrorCodeUtil.getMessageWithCode(errorCode, locale, errMsgFragment1,
				elementFullName,
				dmToJoinDetails, errMsgFragment2);
		problemReporter.reportProblem(JoinProblem.createError(element, joinProblemMessage));
	}

	/**
	 * Builds meaningful error message if root model and model to join contain the same element with inconsistent annotation values
	 * and reports it as {@link JoinProblem} to the {@link JoiningProblemReporter}
	 *
	 * @param elementClass
	 * 		the class of the element with inconsistent annotations
	 * @param element
	 * 		the element with inconsistent annotations
	 */
	public void handleInconsistentAnnotations(Class<?> elementClass, Element element) {
		ErrorCode errMsgFragmentStartCode = errorMessageFragmentStartCodeByClass.get(elementClass);
		String errMsgFragmentStart = ErrorCodeUtil.getLocalizedMessageFragment(errMsgFragmentStartCode, locale);
		String elementFullName = element != null ? String.format(" '%s'", ElementUtils.getPath(element)) : "";
		String joinProblemMessage = ErrorCodeUtil.getMessageWithCode(ErrorCode.InconsistentAnnotations, locale, errMsgFragmentStart,
				elementFullName, dmToJoinDetails);
		problemReporter.reportProblem(JoinProblem.createError(element, joinProblemMessage));
	}
}
