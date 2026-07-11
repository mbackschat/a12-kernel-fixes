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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.meta;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.LocaleUtil;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;

/**
 * A parameter class that was retrofitted to ease the handling of the raw
 * Object array parameters of IMetaField's id IFormatDefinition's convert* methods.
 * This class allows us to preserve the (a12internal) api signatures (for now), while
 * making the subsequent internal handling nicer (order-independent and typed access to
 * the parameters, and explicit enumeration of the possible ones).
 */
public class RawParamHandler4Convert {
	private final Object[] rawParams;

	private RawParamHandler4Convert(Object[] rawParams) {
		this.rawParams = rawParams != null ? rawParams : new Object[] {};
	}

	public static RawParamHandler4Convert of(Object[] rawParams) {
		return new RawParamHandler4Convert(rawParams);
	}

	public IResult getResultParam() {
		return (IResult) findInParams(IResult.class::isInstance);
	}

	public Locale getSprache(List<Locale> allowedLanguages) {
		Locale locale = (Locale) findInParams(Locale.class::isInstance);

		if (locale == null) {
			// Locale.GERMANY ExtMP fallback
			// Note: The fallback is not checked against allowedLanguages and hardcoded to GERMAN.
			// Changing this might be sensible but would change the documented api behaviour and
			// break current tests, so we keep it for now. It may be cleaner to use the first of
			// the allowedLanguages as the fallback instead.
			return Locale.GERMANY; // default ist deutsch (falls kein Sprachparameter angegeben)
		}

		if (!new LocaleUtil(allowedLanguages).isSupportedLocale(locale)) {
			throw new IllegalArgumentException(
					// Do not display ICustomFieldTypeFactory in error messages because it would break many of our tests.
					String.format("The locale '%s' extracted from the parameters '%s' is not supported.", locale,
							Arrays.toString(
									Arrays.stream(rawParams)
											.sequential()
											.filter(Predicate.not(ICustomFieldTypeFactory.class::isInstance))
											.toArray()
							)));
		}
		return locale;
	}

	public ICustomFieldTypeFactory getCustomFieldTypeFactory() {
		return (ICustomFieldTypeFactory) findInParams(ICustomFieldTypeFactory.class::isInstance);
	}

	private Object findInParams(Predicate<Object> test) {
		for (Object o : rawParams) {
			if (test.test(o)) {
				return o;
			}
		}
		return null;
	}
}
