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
import type {
	ICustomFieldTypeCheckError,
	ICustomFieldTypeConversionResult,
	ICustomFieldTypeValidationParam,
	ICustomFieldValidator
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { customFieldTypeConversionResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import { CustomFieldTypeValidationParamImpl } from "../CustomFieldTypeValidationParamImpl.js";
import type { CustomFieldTypeErrorEnum } from "../error/CustomFieldTypeErrorEnum.js";
import { CustomFieldTypeErrorUtil } from "../error/CustomFieldTypeErrorUtil.js";

import type { SteuernummerValidationHelper } from "./SteuernummerValidationHelper.js";

/**
 * Gemeinsame Oberklasse für die Prüfung von Steuernummern und Bundessteuernummern.
 *
 * Die Unterklassen konfigurieren das Verhalten über Konstruktorparameter und können
 * Template-Methoden überschreiben:
 * <ul>
 * <li>{@link getBufaValidator} — liefert den Prüfer für die Bundesfinanzamtsnummer</li>
 * <li>{@link mapValidationError} — bildet das Ergebnis von
 * {@link SteuernummerValidationHelper.checkSteuernummer} auf den gewünschten Fehler-Enum ab</li>
 * <li>{@link createValidationError} — liefert den Fehler bei Konvertierungsproblemen</li>
 * </ul>
 */
export abstract class AbstractSteuernummerVerifier implements ICustomFieldValidator {
	private readonly helper: SteuernummerValidationHelper;
	private readonly errorKey: string;

	protected constructor(helper: SteuernummerValidationHelper, errorKey: string) {
		this.helper = helper;
		this.errorKey = errorKey;
	}

	protected abstract getBufaValidator(): (bufaNr: number) => boolean;

	protected abstract mapValidationError(error: CustomFieldTypeErrorEnum): CustomFieldTypeErrorEnum;

	protected abstract createValidationError(): CustomFieldTypeErrorEnum;

	validate(
		value: string,
		valParam: ICustomFieldTypeValidationParam,
		isDisplayValue: boolean
	): ICustomFieldTypeCheckError | undefined {
		if (isDisplayValue) {
			// der Wert muss vor der Validierung in XML-Format konvertiert werden
			const _convertResult = this.convertDisplay2Internal(value);

			if (_convertResult.getErrorMessage()) {
				// Fehler bei der Konvertierung
				return CustomFieldTypeErrorUtil.getResult(
					this.createValidationError(),
					this.errorKey
				);
			}
			// nehmen wir die XML-Darstellung
			value = _convertResult.getConvertedValue();
		}

		const _error = this.helper.checkSteuernummer(value, this.getBufaValidator());
		return CustomFieldTypeErrorUtil.getResult(
			this.mapValidationError(_error),
			this.errorKey
		);
	}

	/**
	 * Konvertiert die Steuernummer im XML-Format (gleichbedeutend mit dem
	 * ELSTER-Steuernummerformat) in die Oberflächendarstellung.
	 *
	 * @return Steuernummer in Oberflächendarstellung
	 */
	convertInternal2Display(xmlStNr: string): ICustomFieldTypeConversionResult {
		const valParam = new CustomFieldTypeValidationParamImpl(
			CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH,
			CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH
		);
		const _checkResult = this.validate(xmlStNr, valParam, false);
		if (_checkResult) {
			return customFieldTypeConversionResult(
				xmlStNr,
				"Konvertiere nur gültige Steuernummern. " + _checkResult.getErrorMessage()
			);
		}
		return this.helper.convertValidInternal2Display(xmlStNr);
	}

	/**
	 * Konvertiert die Steuernummer in Oberflächendarstellung in das XML-Format (gleichbedeutend mit
	 * dem ELSTER-Steuernummerformat).
	 *
	 * @param oflStNrMitBuLaNr
	 *            - der String enthält neben der Steuernummer in Oberflächendarstellung
	 *            auch die Bundeslandnummer getrennt durch das Zeichen '@'.
	 */
	convertDisplay2Internal(oflStNrMitBuLaNr: string): ICustomFieldTypeConversionResult {
		return this.helper.convertDisplay2Internal(oflStNrMitBuLaNr);
	}
}
