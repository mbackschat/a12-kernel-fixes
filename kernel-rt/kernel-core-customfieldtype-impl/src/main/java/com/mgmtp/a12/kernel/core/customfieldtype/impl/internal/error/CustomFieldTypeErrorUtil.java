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
package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error;

import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeCheckErrorImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.utils.InternalFallbackErrorLookup;

/**
 * Utility-Methoden zur Ermittlung der sprachabhängigen Fehlermeldungen zu den Message-Keys.
 */
public class CustomFieldTypeErrorUtil {

	private CustomFieldTypeErrorUtil() {
		throw new UnsupportedOperationException("No instatiation for static helper classes!");
	}

	public static final String ERROR_MESSAGES_RESOURCE_NAME = "custom_field_types/error_messages";

	/**
	 * Liefert das sprachabhängige Ergebnis der Validierung eines vordefinierten Feldtyps.
	 */
	public static Optional<ICustomFieldTypeCheckError> getResult(CustomFieldTypeErrorEnum error, Locale locale, String errorKey) {
		String errorMsg = getErrorMessage(locale, error);
		if (StringUtils.isEmpty(errorMsg)) {
			return Optional.empty();
		}
		return Optional.of(new CustomFieldTypeCheckErrorImpl(errorMsg, errorKey));
	}

	/**
	 * Liefert die sprachabhängige Fehlermeldung zu einem Errormessage-Key aus
	 * {@link CustomFieldTypeErrorEnum}. Die zu verwendende Sprache wird durch das übergebene
	 * {@link java.util.Locale}-Objekt spezifiziert.
	 */
	public static String getErrorMessage(Locale locale, CustomFieldTypeErrorEnum error) {
		return getErrorMessage(locale, error.name());
	}

	/**
	 * Liefert die sprachabhängige Fehlermeldung zu einem Errormessage-Key aus
	 * {@link CustomFieldTypeErrorEnum}. Die zu verwendende Sprache wird durch das übergebene
	 * {@link java.util.Locale}-Objekt spezifiziert.
	 */
	private static String getErrorMessage(Locale locale, String error) {
		if (CustomFieldTypeErrorEnum.KEIN_FEHLER.name().equals(error)) {
			return "";
		}
		return InternalFallbackErrorLookup.getBareErrorMessageUnformatted(error, ERROR_MESSAGES_RESOURCE_NAME, locale);
	}

}
