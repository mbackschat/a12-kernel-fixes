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
import type { DataFormats, SupportedType, ValueConversionConfig } from "@com.mgmtp.a12.utils/utils-localization";

import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import type { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import type { ValidType } from "../util/ValidType.js";

import type { IValidierungParam } from "./IValidierungParam.js";

export const INITCODE_TRENNER = ";";

export class FeldFormatEnumType {
	static readonly ER_ZAHL: FeldFormatEnumType = new FeldFormatEnumType("Zahl");
	static readonly ER_STRING: FeldFormatEnumType = new FeldFormatEnumType("String");
	static readonly ER_DATUM: FeldFormatEnumType = new FeldFormatEnumType("Datum");
	static readonly ER_DATUMBEREICH: FeldFormatEnumType = new FeldFormatEnumType("DatumBereich");
	static readonly ER_FELDJA: FeldFormatEnumType = new FeldFormatEnumType("Ja");
	static readonly ER_FELDJANEIN: FeldFormatEnumType = new FeldFormatEnumType("JaNein");
	static readonly ER_VORDEFINIERT: FeldFormatEnumType = new FeldFormatEnumType("Vordefiniert");
	static readonly ER_ENUMERATION: FeldFormatEnumType = new FeldFormatEnumType("Enumeration");
	static readonly ER_ENUMERATION_NICHT_ABGESCHLOSSEN: FeldFormatEnumType = new FeldFormatEnumType(
		"NichtAbgeschlosseneEnumeration"
	);

	constructor(private _typ: string) { }

	public toString(): string {
		return this._typ;
	}
}

export interface IFormatDefinition {
	validiereFormat(
		param: IValidierungParam,
		fullCheck: boolean
	): ValidType;

	convert(
		wert: string | undefined,
		darstellungsInfo: DarstellungsInformationIntern,
		vonXml: boolean,
		customFieldTypeFactory: ICustomFieldTypeFactory | undefined,
	): string;

	getDefaultValue(
		metaData: IMetaDataValidierung,
		dataFormats: DataFormats | null
	): string | number;

	/**
	 * Liefert alle MetaInformationen, die mit dem Feldtypen assoziiert sind.
	 */
	getWert(key: string): any;

	/**
	 * Convert from internal value to a representation as a basic type.
	 * Used for {@link IMetaField#convertWithOnlyBasicTypeCheck}.
	 *
	 * The method should be called after {@link validiereFormat} to ensure a correct value.
	 * An Error may occur, if a wrong value is given to the function.
	 *
	 * @param internalValue 	the internal value to be converted.
	 * @param metaData 	the meta data for the generated code. Used for additional parameters like base year.
	 * @return the value as basic type conforming to the type of the specific implementation of
	 *         {@link IFormatDefinition}.
	 */
	convertToBasicType(
		internalValue: string | null,
		metaData: IMetaDataValidierung
	): SupportedType;

	/**
	 * Convert from an object representation to the internal string representation.
	 *
	 * @param value 	the object representation.
	 * @param metaData 	holds additional information.
	 * @return the internal string representation.
	 * @throws Error
	 *             if the given type is not of the correct type for the field.
	 */
	convertFromBasicType(
		value: SupportedType,
		metaData: IMetaDataValidierung
	): string | null;

	/**
	 * @returns the {@link ValueConversionConfig}-instance for the field. Is never <code>null</code>.
	 */
	getConversionConfig(): ValueConversionConfig;
}
