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
	ConfirmConversionConfig,
	DataFormats,
	LocalizableArgs,
	SupportedType,
	ValueConversionConfig
} from "@com.mgmtp.a12.utils/utils-localization";
import {
	defaultValueConversion
} from "@com.mgmtp.a12.utils/utils-localization";

import {
	ConfirmConversion
} from "@com.mgmtp.a12.utils/utils-localization/a12internal";

import { RuntimeFormalErrorEnum } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";


import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import type { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { IFormaleFehlerConstants } from "../util/IFormaleFehlerConstants.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";
import { RuntimeUtils } from "../util/RuntimeUtils.js";
import { ValidType } from "../util/ValidType.js";

import { ConversionUtils } from "./ConversionUtils.js";
import { FehlerHandler } from "./error/FehlerHandler.js";
import type { IFormatDefinition } from "./IFormatDefinition.js";
import { FeldFormatEnumType } from "./IFormatDefinition.js";
import type { IValidierungParam } from "./IValidierungParam.js";

/**
 * Klasse modelliert zur Laufzeit ein Ja-Feld.
 */
export class FormatDefinitionJa implements IFormatDefinition {
	private _jaWert: string;

	private _conversionConfig: ConfirmConversionConfig;

	constructor(jaWert: string) {
		this._jaWert = jaWert;
		this._conversionConfig = { type: "ConfirmType" };
	}

	public convert(
		wert: string,
		darstellungsInfo: DarstellungsInformationIntern,
		vonXml: boolean,
		_customFieldTypeFactory: ICustomFieldTypeFactory | undefined,
	): string {
		const internalDataType = ConversionUtils.createConfirmConversionConfig(
			this._conversionConfig,
			this._jaWert
		);
		const displayDataType = ConversionUtils.createConfirmConversionConfig(
			this._conversionConfig,
			darstellungsInfo.wahrWert ?? this._jaWert
		);
		let conversionResult;
		if (vonXml) {
			conversionResult = ConfirmConversion.convert(wert, internalDataType, displayDataType);
		} else {
			conversionResult = ConfirmConversion.convert(wert, displayDataType, internalDataType);
		}
		return conversionResult;
	}

	public toString(): string {
		return RuntimeUtils.getSimpleNameFromClass(this) + ": " + this._jaWert;
	}

	/**
	 * Allgemeine Methode zum Validieren des Eingabewertes wert.
	 *
	 */
	public validiereFormat(
		param: IValidierungParam,
		fullCheck: boolean
	): ValidType {
		if (!fullCheck && param.getWert().length === 0) {
			// this case is for the base type conversion -> an empty value will be converted to null later on
			return ValidType.Valid;
		}

		// Standardmäßig wird gegen den im Editor gespeicherten Wert geprüft.
		let wahrWert: string = this._jaWert;

		// Falls Wert in der Darstellungsinformation überschrieben ist,
		// nehme diese Information
		const info: DarstellungsInformationIntern = param.getDarstellungsInfo();
		if (info.wahrWert != undefined) {
			wahrWert = info.wahrWert;
		}

		const wert: string = param.getWert();
		if (wahrWert !== wert) {
			const args: LocalizableArgs = {
				trueValue: LocalizableUtil.createPlainPlaceholder(wahrWert)
			};
			const localizableErrorText = LocalizableUtil.createLocalizableFormalError(
				RuntimeFormalErrorEnum.FELD_JA,
				args
			)
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_FELD_JA, [localizableErrorText], param);
		}
		return ValidType.Valid;
	}

	public getDefaultValue(

		_metaData: IMetaDataValidierung,

		_dataFormats: DataFormats
	): string {
		return "";
	}

	public getWert(key: string): any {
		if (key === IMetaKeys.FIELD_MIN_LENGTH) {
			return this._jaWert.length;
		}
		if (key === IMetaKeys.FIELD_MAX_LENGTH) {
			return this._jaWert.length;
		}
		if (key === IMetaKeys.FIELD_FIELD_TYPE) {
			return FeldFormatEnumType.ER_FELDJA.toString();
		}
		if (key === IMetaKeys.FIELD_VALUE_TRUE) {
			return this._jaWert;
		}

		return undefined;
	}

	public convertToBasicType(
		internalValue: string,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		metaData: IMetaDataValidierung
	): boolean | null {
		const conversionResult = defaultValueConversion({ trueValue: this._jaWert }).parseValue(internalValue, this._conversionConfig);
		return typeof conversionResult.value === "boolean" ? conversionResult.value : null;
	}

	public convertFromBasicType(
		value: SupportedType,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		metaData: IMetaDataValidierung
	): string | null {
		return defaultValueConversion({ trueValue: this._jaWert }).formatValue(value, this._conversionConfig) ?? null;
	}

	getConversionConfig(): ValueConversionConfig {
		return this._conversionConfig;
	}
}
