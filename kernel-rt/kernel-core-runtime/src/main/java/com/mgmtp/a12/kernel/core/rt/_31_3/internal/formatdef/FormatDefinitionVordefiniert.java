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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef;

import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.custom.CustomFieldTypeValidationParamImpl;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.utils.conversion.internal.StringConversion;

import lombok.NonNull;

/**
 *
 */
public class FormatDefinitionVordefiniert implements IFormatDefinition, IFormaleFehlerConstants {

	private String name;
	private int minLaenge;
	private int maxLaenge;

	public FormatDefinitionVordefiniert(
			int minLaenge,
			int maxLaenge,
			String name) {
		this.minLaenge = minLaenge;
		this.maxLaenge = maxLaenge;
		this.name = name;
	}

	@Override
	public String convert(String wert,
			DarstellungsInformationIntern darstellungsInfo,
			boolean vonXml, RawParamHandler4Convert additionalParameters) {
		if (wert.isEmpty()) {
			return wert;
		}

		// Konvertierung der vordefinierten Typen delegieren
		if (darstellungsInfo.doConvertCustomFieldType(name)) {
			ICustomFieldValidator customFieldValidator = getCustomFieldValidator(additionalParameters.getCustomFieldTypeFactory());
			if (vonXml) {
				return customFieldValidator.convertInternal2Display(wert).getConvertedValue();
			} else {
				return customFieldValidator.convertDisplay2Internal(wert).getConvertedValue();
			}
		}
		return wert;
	}

	public String getValue(String key) {
		if (key.equals("minLaenge") && minLaenge > 0) {
			return Integer.toString(minLaenge);
		}
		if (key.equals("maxLaenge") && maxLaenge > 0) {
			return Integer.toString(maxLaenge);
		}
		return null;

	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(name);
		if (maxLaenge > 0) {
			buf.append("\nMaxLaenge ");
			buf.append(maxLaenge);
		}
		if (minLaenge > 0) {
			buf.append("\nMinLaenge ");
			buf.append(minLaenge);
		}

		return buf.toString();
	}

	@Override
	public ValidType validiereFormat(IValidierungParam param, IMetaDataValidierung metaData, boolean fullCheck) {
		if (!fullCheck) {
			return ValidType.Valid;
		}

		ICustomFieldValidator customFieldType = getCustomFieldValidator(param.getCustomFieldTypeFactory());
		Locale errMsgLocale = param.getSprache();
		ICustomFieldTypeValidationParam valParam = new CustomFieldTypeValidationParamImpl(errMsgLocale, minLaenge, maxLaenge);

		boolean isDisplayValue = param.getDarstellungsInfo().doConvertCustomFieldType(name);
		String wert = param.getWert();

		Optional<ICustomFieldTypeCheckError> res = customFieldType.validate(wert, valParam, isDisplayValue);
		if (res.isPresent()) {
			return FehlerHandler.addFehler(res.get().getErrorKey(), res.get().getErrorMessage(), param);
		}
		return ValidType.Valid;

	}

	/**
	 * Checks the value without producing an error.
	 */
	public ValidType validateWithoutErrorReporting(String value, ValidationMode mode, boolean isDisplayValue) {
		ICustomFieldValidator feldtypVordefiniertUtils = getCustomFieldValidator(mode.customFieldTypeFactory());
		//the locale doesn't matter as no errors are reported
		ICustomFieldTypeValidationParam valParam = new CustomFieldTypeValidationParamImpl(Locale.GERMANY, minLaenge, maxLaenge);
		Optional<ICustomFieldTypeCheckError> res = feldtypVordefiniertUtils.validate(value, valParam, isDisplayValue);
		return res.isEmpty() ? ValidType.Valid : ValidType.Invalid;
	}

	private ICustomFieldValidator getCustomFieldValidator(ICustomFieldTypeFactory factory) {
		Optional<ICustomFieldValidator> customFieldValidator =
				(factory != null) ? factory.createCustomFieldValidator(this.name) : Optional.empty();
		if (customFieldValidator.isEmpty()) {
			throw new ValidatorException(String.format("Custom field validator '%s' missing.", this.name));
		}
		return customFieldValidator.get();
	}

	@Override
	public String getDefaultValue(IMetaDataValidierung metaData, PresentationInformation presentationInformation, Locale sprache) {
		return "";
	}

	@Override
	public Object getWert(String key) {
		return switch (key) {
			case IMetaKeys.FIELD_MIN_LENGTH -> minLaenge;
			case IMetaKeys.FIELD_MAX_LENGTH -> maxLaenge;
			case IMetaKeys.FIELD_FIELD_TYPE -> name;
			case null, default -> null;
		};
	}

	@Override
	public int compare(@NonNull String s1, @NonNull String s2, IMetaDataValidierung metaData) {
		// predefined fields are alphabetically sorted
		return s1.compareToIgnoreCase(s2);
	}

	@Override
	public String convertToBasicType(String internalValue, IMetaDataValidierung metaData) {
		return internalValue;
	}

	@Override
	public @NonNull String convertFromBasicType(@NonNull Object value, @NonNull IMetaDataValidierung metaData) {
		return StringConversion.convertStringTypeToString(value);
	}
}
