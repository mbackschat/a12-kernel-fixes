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
	BooleanConversionConfig,
	DataFormats,
	LocalizableArgs,
	SupportedType,
	ValueConversionConfig
} from "@com.mgmtp.a12.utils/utils-localization";
import {
	defaultValueConversion
} from "@com.mgmtp.a12.utils/utils-localization";

import {
	BooleanConversion,
} from "@com.mgmtp.a12.utils/utils-localization/lib/main/conversion/a12internal/index.js";

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
 * Klasse modelliert zur Laufzeit ein JaNein-Feld.
 */
export class FormatDefinitionJaNein implements IFormatDefinition {
	private _jaWert: string;
	private _neinWert: string;

	private _minLaenge: number;
	private _maxLaenge: number;
	private _conversionConfig: BooleanConversionConfig;

	constructor(jaWert: string, neinWert: string) {
		this._jaWert = jaWert;
		this._neinWert = neinWert;

		if (this._jaWert.length > this._neinWert.length) {
			this._minLaenge = neinWert.length;
			this._maxLaenge = jaWert.length;
		} else {
			this._minLaenge = jaWert.length;
			this._maxLaenge = neinWert.length;
		}
		this._conversionConfig = { type: "BooleanType" };
	}

	public toString(): string {
		return RuntimeUtils.getSimpleNameFromClass(this) + ": " + this._jaWert + ", " + this._neinWert;
	}

	public convert(
		wert: string,
		darstellungsInfo: DarstellungsInformationIntern,
		vonXml: boolean,
		_customFieldTypeFactory: ICustomFieldTypeFactory | undefined,
	): string {
		const internalDataType = ConversionUtils.createBooleanConversionConfig(
			this._conversionConfig,
			this._jaWert,
			this._neinWert
		);
		const displayDataType = ConversionUtils.createBooleanConversionConfig(
			this._conversionConfig,
			darstellungsInfo.wahrWert ?? this._jaWert,
			darstellungsInfo.falschWert ?? this._neinWert
		);
		let conversionResult;
		if (vonXml) {
			conversionResult = BooleanConversion.convert(wert, internalDataType, displayDataType);
		} else {
			conversionResult = BooleanConversion.convert(wert, displayDataType, internalDataType);
		}
		return conversionResult;
	}

	/**
	 * allgemeine Methode zum Validieren des Eingabewertes wert.
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

		// Standard: prüfe gegen den im Editor gespeicherten Wert
		let wahrWert: string = this._jaWert;
		let falschWert: string = this._neinWert;
		const info: DarstellungsInformationIntern = param.getDarstellungsInfo();
		// Falls Werte in der Darstellungsinformation überschrieben wurden,
		// nehme diese Informationen.
		if (info.wahrWert != undefined) {
			wahrWert = info.wahrWert;
		}
		if (info.falschWert != undefined) {
			falschWert = info.falschWert;
		}
		const wert: string = param.getWert();
		if (wahrWert !== wert && falschWert !== wert) {
			const args: LocalizableArgs = {
				trueValue: LocalizableUtil.createPlainPlaceholder(wahrWert),
				falseValue: LocalizableUtil.createPlainPlaceholder(falschWert)
			};
			const localizableErrorText = LocalizableUtil.createLocalizableFormalError(
				RuntimeFormalErrorEnum.FELD_JA_NEIN,
				args
			)
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_FELD_JA_NEIN, [localizableErrorText], param);
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
			return this._minLaenge;
		}
		if (key === IMetaKeys.FIELD_MAX_LENGTH) {
			return this._maxLaenge;
		}
		if (key === IMetaKeys.FIELD_FIELD_TYPE) {
			return FeldFormatEnumType.ER_FELDJANEIN.toString();
		}
		if (key === IMetaKeys.FIELD_VALUE_TRUE) {
			return this._jaWert;
		}
		if (key === IMetaKeys.FIELD_VALUE_FALSE) {
			return this._neinWert;
		}
		return undefined;
	}

	public convertToBasicType(
		internalValue: string,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		metaData: IMetaDataValidierung
	): boolean | null {
		const dataFormats = {
			trueValue: this._jaWert,
			falseValue: this._neinWert
		};
		const conversionResult = defaultValueConversion(dataFormats).parseValue(internalValue, this._conversionConfig);
		return typeof conversionResult.value === "boolean" ? conversionResult.value : null;
	}

	public convertFromBasicType(
		value: SupportedType,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		metaData: IMetaDataValidierung
	): string | null {
		const dataFormats = {
			trueValue: this._jaWert,
			falseValue: this._neinWert
		};
		return defaultValueConversion(dataFormats).formatValue(value, this._conversionConfig) ?? null;
	}

	getConversionConfig(): ValueConversionConfig {
		return this._conversionConfig;
	}
}
