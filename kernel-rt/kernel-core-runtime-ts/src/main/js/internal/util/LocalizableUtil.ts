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
	DateConversionConfig,
	DateRangeConversionConfig,
	FormatProperties,
	FormattablePlaceholder,
	Localizable,
	LocalizableArgs,
	LocalizablePlaceholder,
	Placeholder,
	PlainPlaceholder,
	ValueConversionConfig
} from "@com.mgmtp.a12.utils/utils-localization";
import {
	addDotEscaping
} from "@com.mgmtp.a12.utils/utils-localization";

import {
	DateConversionConfigBuilder,
	DateFragmentConversionConfigBuilder,
	DateRangeConversionConfigBuilder,
	DateTimeConversionConfigBuilder,
	TimeConversionConfigBuilder
} from "@com.mgmtp.a12.utils/utils-localization/a12internal";

import { RuntimeFormalErrorEnum } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import type { IIdentifier, IMetaField } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import { FehlerHandler } from "../formatdef/error/FehlerHandler.js";

import { FormatDefinitionDatum } from "../formatdef/FormatDefinitionDatum.js";

import { FormatDefinitionDatumBereich } from "../formatdef/FormatDefinitionDatumBereich.js";

import { DatenImpl } from "./DatenImpl.js";
import { EntityUtil } from "./EntityUtil.js";
import { IMetaKeysInternal } from "./IMetaKeysInternal.js";

export type LocalizableDefaults = { readonly [locale: string]: string | undefined };

export class LocalizableUtil {

	private static readonly _FIELD_REFERENCE_DE: string = "Feld";
	private static readonly _FIELD_REFERENCE_EN: string = "field";
	private static readonly _FIELD_VALUE_REFERENCE_DE: string = "Feld.Wert";
	private static readonly _FIELD_VALUE_REFERENCE_EN: string = "field.value";

	public static createPlainPlaceholder(value: string): PlainPlaceholder {
		return { type: "plain", value };
	}

	private static _isEnumeration(f: IMetaField): boolean {
		return (f.getValue(IMetaKeys.FIELD_FIELD_TYPE) === IMetaKeys.FIELD_TYPE_ENUMERATION);
	}

	public static createFieldValuePlaceholder(fieldSpec: string, metaData: IMetaDataValidierung, value?: unknown): Placeholder {
		if (value === undefined) {
			return LocalizableUtil.createPlainPlaceholder("");
		}
		const f: IMetaField = metaData.getMetaField(fieldSpec);
		if (this._isEnumeration(f)) {
			return this._createLocalizableEnumerationPlaceholder(f, metaData, value as string);
		} else {
			return this._createFormatPlaceholder(f, metaData, value);
		}
	}

	private static _createFormatPlaceholder(f: IMetaField, metaData: IMetaDataValidierung, value: unknown): FormattablePlaceholder {
		const modelId = metaData.getModelId();
		const elementPath = this._extractElementPath(f);
		const properties: FormatProperties = {
			formattingConfig: {
				...LocalizableUtil._createConvertionConfig(f, metaData),
				modelId: modelId,
				modelPath: elementPath
			}
		};
		return {
			value,
			type: "formattable",
			properties
		};
	}

	private static _createConvertionConfig(f: IMetaField, metaData: IMetaDataValidierung): ValueConversionConfig {
		const formatDef = metaData.getIFormatDefinition(f.getValue(IMetaKeysInternal.FELD_UNIQUENAME));
		const conversionConfig = formatDef.getConversionConfig();
		if (!(formatDef instanceof FormatDefinitionDatum) && !(formatDef instanceof FormatDefinitionDatumBereich)) {
			return conversionConfig;
		}
		// now we have to enrich the conversion configs with baseYear and timeZone
		const timeZone = metaData.getTimeZone();
		const baseYear = metaData.getBaseYearOrDefault();
		if (formatDef instanceof FormatDefinitionDatumBereich) {
			return new DateRangeConversionConfigBuilder(conversionConfig as DateRangeConversionConfig)
				.withBaseYear(baseYear)
				.withTimeZone(timeZone)
				.build();
		}
		// we now create the specific conversion config based on the format
		const dateConversionConfig = <DateConversionConfig>conversionConfig;
		const format = dateConversionConfig.format;
		if (format.includes("HH")) {
			if (format.includes("MM")) {
				return new DateTimeConversionConfigBuilder()
					.withBaseYear(baseYear)
					.withTimeZone(timeZone)
					.build();
			} else {
				return new TimeConversionConfigBuilder()
					.withBaseYear(baseYear)
					.withTimeZone(timeZone)
					.build();
			}
		}
		if (!format.includes("yyyy") || !format.includes("MM") || !format.includes("dd")) {
			return new DateFragmentConversionConfigBuilder()
				.withFormat(format)
				.withBaseYear(baseYear)
				.withTimeZone(timeZone)
				.build();
		} else {
			return new DateConversionConfigBuilder(dateConversionConfig)
				.withBaseYear(baseYear)
				.withTimeZone(timeZone)
				.build();
		}
	}

	private static _extractElementPath(f: IMetaField): { elementName: string; }[] {
		const fullName: string = f.getValue(IMetaKeys.FIELD_FULLNAME);
		return fullName.split(EntityUtil.SEPARATOR_CHAR).slice(1).map(this._createObject);
	}

	private static _createObject(elementName: string): { elementName: string; } {
		return { elementName };
	}

	private static _extractDotSeparatedPath(fullName: string): string {
		return fullName.split(EntityUtil.SEPARATOR_CHAR).join(".")
	}

	public static createFieldNameParameterReplacement(fieldSpec: string, metaData: IMetaDataValidierung): LocalizableArgs {
		return {
			"fieldName": this.createLocalizableFieldNamePlaceholder(fieldSpec, metaData)
		}
	}

	static createFieldReplacementArg(idExtern: IIdentifier, metaData: IMetaDataValidierung): LocalizableArgs {
		let placeholder: Placeholder;
		if (metaData.isFeldDefiniert(idExtern.getName())) {
			const shortName = metaData.getUniqueName(idExtern.getName())
			placeholder = LocalizableUtil.createLocalizableFieldNamePlaceholder(shortName, metaData);
		} else {
			placeholder = LocalizableUtil.createPlainPlaceholder(DatenImpl.idIdToString(idExtern))
		}
		return {
			fieldName: placeholder
		}
	}

	public static createLocalizableFieldNamePlaceholder(fieldSpec: string, metaData: IMetaDataValidierung): Placeholder {
		const metaField = metaData.getMetaField(fieldSpec)
		const fullName: string = metaField.getValue(IMetaKeys.FIELD_FULLNAME);
		const defaults = metaField.getValidKeys().indexOf(IMetaKeys.FIELD_LABEL) >= 0 ? this.defaultsFromMap(metaField.getValue(IMetaKeys.FIELD_LABEL) ?? new Map()) : undefined
		const key = this.createModelKey(fullName, metaData, "label");
		return {
			type: "localizable",
			value: undefined,
			properties: [{
				key,
				defaults
			}]
		};
	}

	private static _createLocalizableEnumerationPlaceholder(f: IMetaField, metaData: IMetaDataValidierung, value: string): LocalizablePlaceholder {
		const localizable = this._createLocalizableForEnumerationField(f, metaData, value);
		return {
			type: "localizable",
			properties: [localizable],
			value
		}
	}

	private static _createLocalizableForEnumerationField(f: IMetaField, metaData: IMetaDataValidierung, value: string): Localizable {
		const origDefaultValues: { [locale: string]: string | undefined; } =
			f.getValue(IMetaKeysInternal.FELD_UI_VALUES_FOR_VALUE, value);
		const defaultValues = Object.fromEntries(
			Object.entries(origDefaultValues).map(([locale, v]) => [locale, v ? LocalizableUtil.encodeDollar(v) : v]));

		const encodedValue = addDotEscaping(value);
		const key = `${this.createModelKey(f.getValue(IMetaKeys.FIELD_FULLNAME), metaData, "enumValues")}.${encodedValue}`;
		return {
			key,
			defaults: defaultValues
		};
	}

	// kernel-internal-test-utils-ts/.../LocalizationHelper contains a copy of this method
	public static encodeDollar(value: string): string {
		if (!value || value.indexOf("$") < 0) {
			return value;
		}
		return value.replace(/\$/g, "$$$$");
	}

	public static createModelKey(fullName: string, metaData: IMetaDataValidierung, type: string): string {
		const modelId = addDotEscaping(metaData.getModelId());
		return `documentModel.${type}.${modelId}${this._extractDotSeparatedPath(fullName)}`;
	}

	public static createRuleErrorKey(ruleName: string, metaData: IMetaDataValidierung): string {
		return this.createModelKey(ruleName, metaData, "ruleErrorMessage");
	}

	public static createFormalErrorKey(key: RuntimeFormalErrorEnum): string {
		return LocalizableUtil.createFormalErrorKeyFromString(RuntimeFormalErrorEnum[key]);
	}

	public static createFormalErrorKeyFromString(key: string): string {
		return `kernel.formalErrors.${key}`;
	}

	public static createFieldErrorKey(field: IIdentifier, metaData: IMetaDataValidierung) {
		return LocalizableUtil._createFieldSpecialKey(field, metaData, "fieldErrorMessage");
	}

	public static createFieldRequiredErrorKey(field: IIdentifier, metaData: IMetaDataValidierung) {
		return LocalizableUtil._createFieldSpecialKey(field, metaData, "fieldRequiredErrorMessage");
	}

	private static _createFieldSpecialKey(field: IIdentifier, metaData: IMetaDataValidierung, type: string) {
		const fullName = metaData.getPfadName(metaData.getUniqueName(field.getName()));
		return LocalizableUtil.createModelKey(fullName, metaData, type);
	}

	public static defaultsFromMap(texts: Map<string, string>): LocalizableDefaults {
		const result: { [locale: string]: string | undefined } = {};
		for (const [sprache, text] of texts) {
			result[sprache] = text;
		}
		return result;
	}

	public static createLocalizableArgs(parameterMapping: Map<string, string>, mapper: (param: string) => Placeholder): LocalizableArgs {
		const result: { [locale: string]: Placeholder } = {};
		for (const [lang, param] of parameterMapping) {
			result[lang] = mapper(param);
		}
		return result;
	}

	public static createLocalizableFormalError(errorEnum: RuntimeFormalErrorEnum, args?: LocalizableArgs, errorTexts?: LocalizableDefaults): Localizable {
		const defaults = errorTexts ?? FehlerHandler.getAllErrorTexts(errorEnum);
		const key = LocalizableUtil.createFormalErrorKey(errorEnum);
		return { key, args, defaults };
	}

	public static addLocalizableArgs(localizable: Localizable, newArgs: LocalizableArgs): Localizable {
		return {
			key: localizable.key,
			args: { ...localizable.args, ...newArgs },
			defaults: localizable.defaults
		}
	}

	public static createLocalizableArgs4FieldErrorMsg(
		metaData: IMetaDataValidierung,
		id: IIdentifier,
		value: string,
		additionalArgs?: { [placeholder: string]: Placeholder }
	): LocalizableArgs {
		const result: { [placeholder: string]: Placeholder } = {};
		const fieldNamePlaceholder = LocalizableUtil.createLocalizableFieldNamePlaceholder(
			metaData.getUniqueName(id.getName()),
			metaData
		)
		result[LocalizableUtil._FIELD_REFERENCE_DE] = fieldNamePlaceholder;
		result[LocalizableUtil._FIELD_REFERENCE_EN] = fieldNamePlaceholder;
		// For formal errors erroneous values cannot be localized => only plain placeholders
		const fieldValuePlaceholder = LocalizableUtil.createPlainPlaceholder(value);
		result[LocalizableUtil._FIELD_VALUE_REFERENCE_DE] = fieldValuePlaceholder;
		result[LocalizableUtil._FIELD_VALUE_REFERENCE_EN] = fieldValuePlaceholder;
		if (additionalArgs) {
			for (const key in additionalArgs) {
				result[key] = additionalArgs[key];
			}
		}
		return result;
	}
}