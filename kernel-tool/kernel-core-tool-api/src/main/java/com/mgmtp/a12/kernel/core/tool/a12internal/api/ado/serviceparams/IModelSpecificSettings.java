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

package com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.model.header.Annotation;

/**
 * Specifies additional properties of an entity container.
 * The interface is used for model specific add ons.
 */
public interface IModelSpecificSettings {

	/**
	 * When operating with date formats without the year part, this property will be taken to
	 * complete the missing year part (important for the consideration of leap years).
	 *
	 * @return the base year.
	 */
	Optional<Integer> getBaseYear();

	/**
	 * The time zone used to interpret date strings in.
	 * This will be stored in the generated code and used during validation when converting the
	 * given date-/time-strings to {@link Date} objects and during conversion in the meta API of the
	 * generated code.
	 * <p>
	 * In ExtMP, this should always be <code>"Europe/Berlin"</code> in A12-context usually
	 * <code>"UTC"</code> is used. And only these two are supported so far.
	 *
	 * @return the time zone.
	 */
	String getTimeZone();

	/**
	 * @return list from the name of an annotation to its value. The value of the annotation is an
	 * 		empty string, if no value was specified.
	 * 		May not be {@code null}.
	 */
	default List<Annotation> getAnnotations() {
		return List.of();
	}

	/**
	 * Returns the language, if
	 * <ul>
	 * <li>the condition text uses this language</li>
	 * <li>errors, which are reported to the problem manager, use this language.</li>
	 * </ul>
	 * <p>
	 * Note: Do not mix up with the language, used in the generated code. This language is specified
	 * by {@link ICodeGeneratorParameters#getLanguages}.
	 * <p>
	 * Note: Do not mix up with the programming language, which is used by the generated code. The
	 * programming language is specified by
	 * {@link ICodeGeneratorParameters#getProgrammingLanguage()}.
	 *
	 * @return the {@link Locale}, if the rule condition text uses this language or error messages
	 * 		reported to the problem manager use this language.
	 */
	Locale getLanguageUsedInConditionText();

	/**
	 * If true, then referencing fields in rule conditions is also allowed via the short name.
	 *
	 * @return true if referencing fields in rule conditions is also allowed via the short name, otherwise false.
	 */
	boolean isFieldRefByShortNameAllowed();
}
