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
package com.mgmtp.a12.kernel.core.customfieldtype.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeCheckErrorImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorUtil;

import lombok.NonNull;

public abstract class DatumVerifier implements ICustomFieldValidator {

	protected abstract Pattern getFormatPattern();

	protected abstract String getSimpleDateFormatPattern();

	protected abstract String getErrorKey();

	protected abstract String getFormatFuerFehlermeldung();

	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String datum,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		CustomFieldTypeErrorEnum error = checkDatum(datum);
		if (error == CustomFieldTypeErrorEnum.KEIN_FEHLER) {
			return Optional.empty();
		}
		String errorMsg = CustomFieldTypeErrorUtil.getErrorMessage(valParam.getErrorMsgLocale(), error);
		errorMsg = errorMsg.replace("<dateFormat>", getFormatFuerFehlermeldung());
		return Optional.of(new CustomFieldTypeCheckErrorImpl(errorMsg, getErrorKey()));
	}

	private CustomFieldTypeErrorEnum checkDatum(String wert) {
		if (!getFormatPattern().matcher(wert).matches()) {
			/*
			 * The check against the pattern is necessary because DateFormat tolerates it when
			 * leading zeros are omitted.
			 */
			return CustomFieldTypeErrorEnum.DATUM_UNGUELTIG;
		}
		try {
			/*
			 * ATTENTION: DateFormat is not a class variable because the API states:
			 * Date formats are not synchronized. It is recommended to create
			 * separate format instances for each thread.
			 */
			DateFormat formatter = new SimpleDateFormat(getSimpleDateFormatPattern());
			formatter.setLenient(false);
			formatter.parse(wert);
			return CustomFieldTypeErrorEnum.KEIN_FEHLER;
		} catch (ParseException e) {
			return CustomFieldTypeErrorEnum.DATUM_UNGUELTIG;
		}
	}
}
