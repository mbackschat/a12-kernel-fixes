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
import type { CustomStringConversionConfig, DataFormats, SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type { ICustomFieldValidator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import { CustomFieldTypeValidationParamImpl } from "../custom/CustomFieldTypeValidationParamImpl.js";
import type { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { ValidType } from "../util/ValidType.js";

import type { IFormaleFehlerConstants } from "../util/IFormaleFehlerConstants.js";

import type { IFormatDefinition } from "./IFormatDefinition.js";
import type { IValidierungParam } from "./IValidierungParam.js";

import { FehlerHandler } from "./error/FehlerHandler.js";

export class FormatDefinitionVordefiniert implements IFormatDefinition, IFormaleFehlerConstants {
	private _name: string;
	private _minLaenge: number;
	private _maxLaenge: number;
	private _conversionComfig: CustomStringConversionConfig;

	constructor(minLaenge: number, maxLaenge: number, name: string) {
		this._minLaenge = minLaenge;
		this._maxLaenge = maxLaenge;
		this._name = name;
		this._conversionComfig = {
			type: "CustomFieldType",
			name: this._name,
			representation: "internal"
		};
	}

	public convert(
		wert: string,
		darstellungsInfo: DarstellungsInformationIntern,
		vonXml: boolean,
		customFieldTypeFactory: ICustomFieldTypeFactory | undefined,
	): string {
		if (wert.length === 0) {
			return wert;
		}
		// convert goes to the implementation
		if (darstellungsInfo.doConvertCustomFieldType(this._name)) {
			const customFieldValidator = this._getCustomFieldValidator(customFieldTypeFactory);
			if (vonXml) {
				return customFieldValidator.convertInternal2Display(wert).getConvertedValue();
			} else {
				return customFieldValidator.convertDisplay2Internal(wert).getConvertedValue();
			}
		}
		return wert;
	}

	public getValue(key: string): string | undefined {
		if (key === "minLaenge") {
			if (this._minLaenge > 0) {
				return String(this._minLaenge);
			}
		}
		if (key === "maxLaenge") {
			if (this._maxLaenge > 0) {
				return String(this._maxLaenge);
			}
		}
		return undefined;
	}

	public toString(): string {
		let buf = "";
		buf += this._name;
		if (this._maxLaenge > 0) {
			buf += "\nMaxLaenge ";
			buf += this._maxLaenge;
		}
		if (this._minLaenge > 0) {
			buf += "\nMinLaenge ";
			buf += this._minLaenge;
		}

		return buf.toString();
	}

	public validiereFormat(
		param: IValidierungParam,
		fullCheck: boolean
	): ValidType {
		if (!fullCheck) {
			return ValidType.Valid;
		}
		const _valParam = new CustomFieldTypeValidationParamImpl(
			this._minLaenge,
			this._maxLaenge
		);

		const _isDisplayValue = param.getDarstellungsInfo().doConvertCustomFieldType(this._name);

		const _wert = param.getWert();

		const customFieldValidator = this._getCustomFieldValidator(param.getCustomFieldTypeFactory());
		const _res = customFieldValidator.validate(_wert, _valParam, _isDisplayValue);
		if (_res) {
			return FehlerHandler.addFehler(_res.getErrorKey(), _res.getErrorMessage(), param);
		}
		return ValidType.Valid;
	}

	/**
	 * Checks the value without producing an error.
	 */
	public validateWithoutErrorReporting(value: string, mode: ValidationMode, isDisplayValue: boolean): ValidType {
		const customFieldValidator = this._getCustomFieldValidator(mode.customFieldTypeFactory);
		// the locale doesn't matter as no errors are reported
		const _valParam = new CustomFieldTypeValidationParamImpl(
			this._minLaenge,
			this._maxLaenge
		);
		const _res = customFieldValidator.validate(value, _valParam, isDisplayValue);
		if (!_res) {
			return ValidType.Valid;
		}
		return ValidType.Invalid;
	}

	private _getCustomFieldValidator(factory: ICustomFieldTypeFactory | undefined): ICustomFieldValidator {
		const customFieldValidator = factory?.createCustomFieldType(this._name);
		if (!customFieldValidator) {
			throw new Error(`Custom field validator '${this._name}' missing.`);
		}
		return customFieldValidator;
	}

	public getDefaultValue(

		_metaData: IMetaDataValidierung,

		_dataFormats: DataFormats
	): string {
		return "";
	}

	public getWert(key: string): any {
		if (key === IMetaKeys.FIELD_MIN_LENGTH) {
			return this._minLaenge;
		}
		if (key === IMetaKeys.FIELD_MAX_LENGTH) {
			return this._maxLaenge;
		}
		if (key === IMetaKeys.FIELD_FIELD_TYPE) {
			return this._name;
		}

		return undefined;
	}

	public convertToBasicType(
		internalValue: string,
		_metaData: IMetaDataValidierung
	): string {
		return internalValue;
	}

	public convertFromBasicType(
		value: SupportedType,
		_metaData: IMetaDataValidierung
	): string | null {
		// in both cases ('null' and 'undefined') we return 'null'
		if (value == null) {
			return null;
		}
		if (typeof value !== "string") {
			throw new Error("The type of the value (" + typeof value + ") is not a string");
		}
		return value;
	}

	getConversionConfig(): CustomStringConversionConfig {
		return this._conversionComfig;
	}
}
