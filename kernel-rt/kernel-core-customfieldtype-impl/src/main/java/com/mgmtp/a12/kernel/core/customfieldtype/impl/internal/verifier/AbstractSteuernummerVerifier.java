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
package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.verifier;

import java.util.Locale;
import java.util.Optional;
import java.util.function.IntPredicate;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeConversionResult;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeConversionResultImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.CustomFieldTypeValidationParamImpl;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorUtil;

import lombok.NonNull;

/**
 * Gemeinsame Oberklasse für die Prüfung von Steuernummern und Bundessteuernummern.
 * <p>
 * Die Unterklassen konfigurieren das Verhalten über Konstruktorparameter und können zwei
 * Template-Methoden überschreiben:
 * <ul>
 * <li>{@link #getBufaValidator()} — liefert den Prüfer für die Bundesfinanzamtsnummer</li>
 * <li>{@link #mapValidationError(CustomFieldTypeErrorEnum)} — bildet das Ergebnis von
 * {@link SteuernummerValidationHelper#checkSteuernummer} auf den gewünschten Fehler-Enum ab
 * (Standard: alle Fehler werden auf den konfigurierten {@code errorEnum} abgebildet)</li>
 * </ul>
 */
abstract class AbstractSteuernummerVerifier implements ICustomFieldValidator {

	private final SteuernummerValidationHelper helper;
	private final String errorKey;

	protected AbstractSteuernummerVerifier(
			SteuernummerValidationHelper helper,
			String errorKey
	) {
		this.helper = helper;
		this.errorKey = errorKey;
	}

	/**
	 * Liefert den Prüfer für die Bundesfinanzamtsnummer.
	 */
	protected abstract @NonNull IntPredicate getBufaValidator();

	/**
	 * @return {@link CustomFieldTypeErrorEnum}
	 * 		Maps the {@link CustomFieldTypeErrorEnum} that {@link SteuernummerValidationHelper#checkSteuernummer} returned to the
	 * 		subclass specific {@link CustomFieldTypeErrorEnum}.
	 */
	protected abstract @NonNull CustomFieldTypeErrorEnum mapValidationError(CustomFieldTypeErrorEnum error);

	/**
	 * @return the sub-class specific {@link CustomFieldTypeErrorEnum}
	 */
	protected abstract @NonNull CustomFieldTypeErrorEnum createValidationError();

	@Override
	public final @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String value,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		Locale errorMsgLocale = valParam.getErrorMsgLocale();
		if (isDisplayValue) {//der Wert muss vor der Validierung in XML-Format konvertiert werden
			ICustomFieldTypeConversionResult convertResult = convertDisplay2Internal(value);

			if (convertResult.getErrorMessage().isPresent()) {//Fehler bei der Konvertierung
				return CustomFieldTypeErrorUtil.getResult(createValidationError(), errorMsgLocale, errorKey);
			}
			//nehmen wir die XML-Darstellung
			value = convertResult.getConvertedValue();
		}

		CustomFieldTypeErrorEnum error = helper.checkSteuernummer(value, getBufaValidator());
		return CustomFieldTypeErrorUtil.getResult(mapValidationError(error), errorMsgLocale, errorKey);
	}

	/**
	 * Konvertiert die Steuernummer im XML-Format (gleichbedeutend mit dem
	 * ELSTER-Steuernummerformat) in die Oberflächendarstellung.
	 *
	 * @return Steuernummer in Oberflächendarstellung
	 */
	@Override
	public final @NonNull ICustomFieldTypeConversionResult convertInternal2Display(@NonNull String xmlStNr) {
		ICustomFieldTypeValidationParam valParam = new CustomFieldTypeValidationParamImpl(Locale.GERMANY,
				CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH, CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH);
		Optional<ICustomFieldTypeCheckError> checkResult = validate(xmlStNr, valParam, false);
		if (checkResult.isPresent()) {
			return new CustomFieldTypeConversionResultImpl(xmlStNr,
					"Konvertiere nur gültige Steuernummern. " + checkResult.get().getErrorMessage());
		}
		return helper.convertValidInternal2Display(xmlStNr);
	}

	/**
	 * Konvertiert die Steuernummer in Oberflächendarstellung in das XML-Format (gleichbedeutend mit
	 * dem ELSTER-Steuernummerformat).
	 *
	 * @param oflStNrMitBuLaNr
	 * 		- der String enthält neben der Steuernummer in Oberflächendarstellung
	 * 		auch die
	 * 		Bundeslandnummer getrennt durch das Zeichen '@'.
	 */
	@Override
	public final @NonNull ICustomFieldTypeConversionResult convertDisplay2Internal(@NonNull String oflStNrMitBuLaNr) {
		return helper.convertDisplay2Internal(oflStNrMitBuLaNr);
	}
}
