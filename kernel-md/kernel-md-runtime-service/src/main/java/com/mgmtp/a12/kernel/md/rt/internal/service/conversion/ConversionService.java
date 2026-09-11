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
package com.mgmtp.a12.kernel.md.rt.internal.service.conversion;

import java.util.Locale;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.md.rt.a12internal.service.DocumentValidationResultImpl;

public class ConversionService {
	private final Locale locale;
	private final ICustomFieldTypeFactory customFieldTypeFactory;
	private final PresentationInformation presentationInformation;

	public ConversionService(Locale locale, ICustomFieldTypeFactory customFieldTypeFactory) {
		this.locale = locale;
		this.customFieldTypeFactory = customFieldTypeFactory;
		this.presentationInformation = PresentationInformationConstants.get(this.locale);
	}

	public String convertToUI(final Object value, final IMetaField metaField) {
		DocumentValidationResultImpl validationResult = new DocumentValidationResultImpl();
		String result;
		// since kernel version 24.0.0 it is possible to have a string value even the field is not of string type
		// in that case we have a special treatment
		if (value instanceof String stringValue) {
			result = convertStringToUI(stringValue, metaField);
		} else {
			result = metaField.convertFromBasicType(value, presentationInformation, validationResult, locale, customFieldTypeFactory);
			if (!validationResult.noErrorOccurred()) {
				throw new IllegalArgumentException(validationResult.getMessages().getFirst().getErrorText());
			}
		}
		return result;
	}

	private String convertStringToUI(String value, IMetaField metaField) {
		try {
			return metaField.convert(value, presentationInformation, true, locale, customFieldTypeFactory);
		} catch (IllegalArgumentException e) {
			// in this case the value is not valid, that's why we return the original value
			return value;
		}
	}

}
