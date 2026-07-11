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
	DataFormatPlaceholder,
	DataFormats,
	DateRangeConversionConfig,
	LocalizableArgs,
	SupportedType,
	ValueConversionConfig
	,
	InterpretationOfYear
} from "@com.mgmtp.a12.utils/utils-localization";
import {
	defaultValueConversion
} from "@com.mgmtp.a12.utils/utils-localization";
import type {
	DateAndFormat
} from "@com.mgmtp.a12.utils/utils-localization/a12internal";
import {
	DateRangeConversion,
	DateRangeConversionConfigBuilder,
	DateUtil,
	ValidationDateParser
} from "@com.mgmtp.a12.utils/utils-localization/a12internal";

import { RuntimeFormalErrorEnum } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import type { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { DatumTeilbekanntArt } from "../util/DatumTeilbekanntArt.js";
import { IFormaleFehlerConstants } from "../util/IFormaleFehlerConstants.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";
import { ValidType } from "../util/ValidType.js";

import { ConversionUtils } from "./ConversionUtils.js";
import { FehlerBereich, FormatDefinitionDatum } from "./FormatDefinitionDatum.js";
import type { IFormatDefinition } from "./IFormatDefinition.js";
import { FeldFormatEnumType } from "./IFormatDefinition.js";
import type { IValidierungParam } from "./IValidierungParam.js";
import { FehlerHandler } from "./error/FehlerHandler.js";

export enum Jahresinterpretation {
	Standard,
	Folgejahr,
	Vorjahr
}

export class FormatDefinitionDatumBereich
	extends FormatDefinitionDatum
	implements IFormatDefinition {
	private _trenner: string;
	private readonly _jahresinterpretation: Jahresinterpretation;
	private readonly _interpretationOfYear: InterpretationOfYear | undefined;
	private readonly _singleDate = "notAllowed";
	private _conversionConfigDateRange: DateRangeConversionConfig;

	constructor(
		javaFormat: string,
		zusatzPruefung: boolean,
		trenner: string,
		jahresinterpretation: Jahresinterpretation
	) {
		super(javaFormat, zusatzPruefung, DatumTeilbekanntArt.BEKANNT);
		this._trenner = trenner;
		this._laenge = javaFormat.length * 2 + trenner.length;
		this._metaFormat = this._metaFormat + trenner + this._metaFormat;
		this._feldTyp = FeldFormatEnumType.ER_DATUMBEREICH.toString();
		this._jahresinterpretation = jahresinterpretation;
		this._interpretationOfYear = this._mapToInterpretationOfYear(jahresinterpretation);
		this._conversionConfigDateRange = new DateRangeConversionConfigBuilder()
			.withFormat(this._format)
			.withInterpretationOfYear(this._interpretationOfYear)
			.withSingleDate(this._singleDate)
			.build();
		this._internalDataFormats = this._getInternalDataFormats();
	}

	public convert(
		wert: string,
		darstellungsInfo: DarstellungsInformationIntern,
		vonXml: boolean,
		_customFieldTypeFactory: ICustomFieldTypeFactory | undefined,
	): string {
		const internalDataType = ConversionUtils.createDateRangeConversionConfig(
			this._conversionConfigDateRange,
			darstellungsInfo.baseYear,
			darstellungsInfo.timeZone,
			this._conversionConfigDateRange.format,
			this._internalDataFormats.dateFragmentOrdering,
			this._internalDataFormats.dateZeroOptional,
			this._internalDataFormats.formatCharacters,
			this._internalDataFormats.dateRangeSeparator
		);
		const displayDataType = darstellungsInfo.createDateRangeConversionConfig(this._conversionConfigDateRange, this._internalDataFormats);
		let conversionResult;
		if (vonXml) {
			conversionResult = DateRangeConversion.convert(wert, internalDataType, displayDataType);
		} else {
			conversionResult = DateRangeConversion.convert(wert, displayDataType, internalDataType);
		}
		return conversionResult;
	}

	public validiereFormat(
		param: IValidierungParam,

		_fullCheck: boolean
	): ValidType {
		if (!_fullCheck && !param.getWert()) {
			return ValidType.Valid;
		}

		// Anhand der Oberflächendarstellung und dem aktuellen Format
		// wird das Format ermittelt. Es muss unterschieden werden zwischen
		// dem Format, das für Fehlermeldungen verwendet wird und dem Format
		// das im Formatter zum prüfen verwendet wird. Die beiden Formate unterscheiden
		// sich in dem Fall, dass in dem Ursprungsformat keine Jahresangabe verwendet
		// wird.  Dann wird zum Prüfen eine Jahresangabe hinzugefügt, und diese Angabe
		// auch zum zu prüfenden Wert hinzugefügt.

		const info: DarstellungsInformationIntern = param.getDarstellungsInfo();
		const pruefFormat: string = info.convertDatumFormatXml2Ofl(this._format);

		const dbTrenner: string = info.convertDatumBereichTrennerXML2Ofl(this._trenner);

		const wert: string = param.getWert();
		if (!wert.includes(dbTrenner)) {
			return this._createTrennerFehltFehler(param);
		}

		// Trenne den Datumsbereich auf,  verwende den Oberflächentrenner.
		const datumBereich: string[] = DateUtil.splitStringBySeparator(wert, dbTrenner);
		if (datumBereich.length !== 2 || datumBereich[0].length === 0 || datumBereich[1].length === 0) {
			return this._createFormatFehler(param);
		}

		const dates: DateAndFormat[] = DateUtil.getDateRange(
			wert,
			dbTrenner,
			pruefFormat,
			info.datumTrennerHintenOptional(this._format),
			param.getMetaData().getBaseYearOrDefault(),
			this._interpretationOfYear,
			param.getMetaData().getTimeZone()
		);

		if (!ValidationDateParser.isSyntacticallyCorrect(dates[0]) ||
			!ValidationDateParser.isSyntacticallyCorrect(dates[1])) {
			return this._createFormatFehler(param);
		}

		let dbStartDate: Date;
		let dbEndeDate: Date;
		try {
			// hier erfolgt die feinere Überprüfung durch Parsen
			dbStartDate = ValidationDateParser.parseDateWithoutSyntaxCheck(dates[0]);
		} catch (_error) {
			return this._createDetailedDatumFormatFehler(
				dates[0],
				param,
				_fullCheck,
				FehlerBereich.BEREICH_START_DATUM,
				this._trenner
			);
		}
		try {
			// hier erfolgt die feinere Überprüfung durch Parsen
			dbEndeDate = ValidationDateParser.parseDateWithoutSyntaxCheck(dates[1]);
		} catch (_error) {
			return this._createDetailedDatumFormatFehler(
				dates[1],
				param,
				_fullCheck,
				FehlerBereich.BEREICH_END_DATUM,
				this._trenner
			);
		}

		if (!_fullCheck) {
			return ValidType.Valid;
		}

		// gültiges Intervall ?
		if (DateUtil.dateBefore(dbEndeDate, dbStartDate)) {
			return this._createFehlerForDatumBereich(
				param,
				RuntimeFormalErrorEnum.DATUM_BEREICH_NICHT_GUELTIG,
				IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_NICHT_GUELTIG
			);
		}
		const timeZone = param.getMetaData().getTimeZone();
		if (this.isZusatzPruefung) {
			return this._checkZusatzPruefung(dbStartDate, timeZone, param);
		}
		// check if date generally valid
		if (this.dateInvalid(dbStartDate, timeZone)) {
			return this._createFehlerForDatumBereich(
				param,
				RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM,
				IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_DATUM);
		}
		return ValidType.Valid;
	}

	private _checkZusatzPruefung(
		dbStart: Date,
		timeZone: string,
		param: IValidierungParam
	): ValidType {
		if (this.dateInvalidForAdditionalCheck(dbStart, timeZone)) {
			// liegt das erste Datum vor 1900
			return this._createFehlerForDatumBereich(
				param,
				RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM_ADDITIONAL_CHECK,
				IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_DATUM
			);
		}
		return ValidType.Valid;
	}

	private _createFormatFehler(
		param: IValidierungParam
	): ValidType {
		return this._createFehlerForDatumBereich(
			param,
			RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT,
			IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT
		);
	}

	private _createTrennerFehltFehler(
		param: IValidierungParam
	): ValidType {
		return this._createFehlerForDatumBereich(
			param,
			RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT,
			IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_TRENNER_FEHLT
		);
	}

	public _createFehlerForDatumBereich(
		param: IValidierungParam,
		formalError: RuntimeFormalErrorEnum,
		fehlerId: string,
		replacements?: LocalizableArgs
	): ValidType {
		const value = { // DateRangeValue, but the type is currently non-public in utils-localization
			dateFormat: this._format,
			dateRangeSeparator: this._trenner
		}
		const timeIntervalFormatPlaceholder: DataFormatPlaceholder = {
			type: "dataFormat",
			value,
			properties: {
				type: "dateRange"
			}
		};
		const args: LocalizableArgs = {
			timeIntervalFormat: timeIntervalFormatPlaceholder, ...replacements
		}
		const localizableErrorText = LocalizableUtil.createLocalizableFormalError(
			formalError,
			args)
		return FehlerHandler.addFehler(fehlerId, [localizableErrorText], param);
	}

	public get trenner(): string {
		return this._trenner;
	}

	public get jahresinterpretation(): Jahresinterpretation {
		return this._jahresinterpretation;
	}

	public get interpretationOfYear(): InterpretationOfYear | undefined {
		return this._interpretationOfYear;
	}

	public getWert(key: string): any {
		return super.getWert(key);
	}

	public convertToBasicType(
		internalValue: string,
		metaData: IMetaDataValidierung
	): Date[] | null {
		const internalDataType = ConversionUtils.createDateRangeConversionConfig(
			this._conversionConfigDateRange,
			metaData.getBaseYearOrDefault(),
			metaData.getTimeZone(),
			this._conversionConfigDateRange.format,
			this._internalDataFormats.dateFragmentOrdering,
			this._internalDataFormats.dateZeroOptional,
			this._internalDataFormats.formatCharacters,
			this._internalDataFormats.dateRangeSeparator
		);
		const conversionResult = DateRangeConversion.convertStringToDateRange(internalValue, internalDataType, false);
		return this._isArrayOfTwoDates(conversionResult.value) ? conversionResult.value : null;
	}

	private _isArrayOfTwoDates(value: unknown): value is Date[] {
		return (
			Array.isArray(value) &&
			value.length === 2 &&
			value.every(item => item && item instanceof Date)
		);
	}

	public convertFromBasicType(
		value: SupportedType,
		metaData: IMetaDataValidierung
	): string | null {
		const internalDataType = this._createDateRangeConversionConfig(metaData.getBaseYearOrDefault(), metaData.getTimeZone());
		return defaultValueConversion(this._internalDataFormats).formatValue(value, internalDataType) ?? null;
	}

	private _createDateRangeConversionConfig(baseYear: number, timeZone: string): DateRangeConversionConfig {
		return new DateRangeConversionConfigBuilder()
			.withBaseYear(baseYear)
			.withTimeZone(timeZone)
			.withFormat(this._format)
			.withInterpretationOfYear(this._interpretationOfYear)
			.withSingleDate(this._singleDate)
			.build();
	}

	private _getInternalDataFormats(): DataFormats {
		return {
			...FormatDefinitionDatum.getInternalDataFormats(this._format),
			dateRangeSeparator: this._trenner
		}
	}

	private _mapToInterpretationOfYear(jahresinterpretation: Jahresinterpretation): InterpretationOfYear | undefined {
		switch (jahresinterpretation) {
			case Jahresinterpretation.Standard:
				return undefined;
			case Jahresinterpretation.Folgejahr:
				return "FROM";
			case Jahresinterpretation.Vorjahr:
				return "TO";
		}
	}

	getConversionConfig(): ValueConversionConfig {
		return this._conversionConfigDateRange;
	}
}
