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

package com.mgmtp.a12.kernel.core.tool.internal;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.INumberType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IPredefinedType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

/**
 * Utility methods for extracting informations for {@link IFieldType}s.
 */
public class FieldTypeUtils {

	private FieldTypeUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * @param type
	 * 		the type to get the min length for.
	 * @return the min length of the type or {@value IFieldType#UNDEFINED}, if the type does not
	 * 		specify a min length.
	 */
	public static int getMinLength(IFieldType type) {
		if (type instanceof INumberType) {
			return ((INumberType) type).getMinLength().orElse(IFieldType.UNDEFINED);
		} else if (type instanceof IPredefinedType) {
			return ((IPredefinedType) type).getMinLength();
		} else if (type instanceof IStringType) {
			return ((IStringType) type).getMinLength();
		} else {
			return IFieldType.UNDEFINED;
		}
	}

	/**
	 * @param type
	 * 		the type to get the max length for.
	 * @return the max length of the type or {@value IFieldType#UNDEFINED}, if the type does not
	 * 		specify a max length.
	 */
	public static int getMaxLength(IFieldType type) {
		if (type instanceof INumberType) {
			return ((INumberType) type).getMaxLength().orElse(IFieldType.UNDEFINED);
		} else if (type instanceof IPredefinedType) {
			return ((IPredefinedType) type).getMaxLength();
		} else if (type instanceof IStringType) {
			return ((IStringType) type).getMaxLength();
		} else {
			return IFieldType.UNDEFINED;
		}
	}

	/**
	 * @param type
	 * 		field type
	 * @param maxLengthAllowed
	 * 		maximum allowed length
	 * @return <code>true</code> if the defined max and min length are less or equal to the given
	 * 		maximum, otherwise <code>false</code>.
	 */
	public static boolean isLengthCorrect(IFieldType type, int maxLengthAllowed) {
		if (!needsLengthTest(type)) {
			return true;
		} else {
			int maxLength = FieldTypeUtils.getMaxLength(type);
			int minLength = FieldTypeUtils.getMinLength(type);
			return maxLength <= maxLengthAllowed && minLength <= maxLengthAllowed;
		}
	}

	/**
	 * Tests, whether the given {@link IFieldType} needs a test against max length.
	 *
	 * @param type
	 * 		the type to test.
	 * @return <code>true</code> if the given field needs a test against max length.
	 */
	private static boolean needsLengthTest(IFieldType type) {
		return type instanceof IStringType ||
				type instanceof IPredefinedType;
	}

	/**
	 * The ExtMP server treats following states for localized (based on locale as string) texts as
	 * equal (the example is taken from the error messages in string and enum fields):
	 * <ul>
	 * <li>"errorTextMap" : { "de_DE" : "" }</li>
	 * <li>"errorTextMap" : { }</li>
	 * </ul>
	 * Additionally, the locale as string is mapped to the corresponding locale.
	 *
	 * @param map
	 * 		map with the localized texts (based on locale as string) coming from the ExtMP
	 * 		server. May not be {@code null}.
	 * @return a map containing the localized <b>non-empty</b> texts based on locale. Is never
	 *        {@code null}.
	 */
	public static Map<Locale, String> createLocaleMap(Map<String, String> map) {
		return map.entrySet().stream()
				.filter(e -> !StringUtils.isEmpty(e.getValue()))
				.collect(Collectors.toMap(e -> CommonUtils.toLocale(e.getKey()), Entry::getValue));
	}

}