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

import { HashMap } from "@com.mgmtp.a12.utils/utils-collections";

import type {
	DataFormatPlaceholder,
	DataFormats,
	DateConversionConfig,
	Localizable,
	LocalizableArgs,
	SupportedType,
	ValueConversionConfig,
	DatePrecision
} from "@com.mgmtp.a12.utils/utils-localization";
import {
	defaultValueConversion
} from "@com.mgmtp.a12.utils/utils-localization";

import {
	DateAndFormat,
	DateConversion,
	DatePart,
	DateUtil,
	PartiallyKnownDatesUtil,
	StringUtil,
	TimeZonedDate,
	ValidationDateParser
} from "@com.mgmtp.a12.utils/utils-localization/a12internal";

import { RuntimeFormalErrorEnum } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { DatumTeilbekanntArt } from "../util/DatumTeilbekanntArt.js";
import { IFormaleFehlerConstants } from "../util/IFormaleFehlerConstants.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";
import { TeilbekanntesDatumUtil } from "../util/TeilbekanntesDatumUtil.js";
import { ValidType } from "../util/ValidType.js";

import { ConversionUtils } from "./ConversionUtils.js";
import { FehlerHandler } from "./error/FehlerHandler.js";
import type { FormatDefinitionDatumBereich } from "./FormatDefinitionDatumBereich.js";
import type { IFormatDefinition } from "./IFormatDefinition.js";
import { FeldFormatEnumType } from "./IFormatDefinition.js";
import type { IValidierungParam } from "./IValidierungParam.js";

export enum FehlerBereich {
	BEREICH_START_DATUM,
	BEREICH_END_DATUM
}

/**
 * Klasse definiert ein DatumsFormat zur Validierung von Eingaben zur Laufzeit
 * SimpleDateFormat ist nicht thread-safe, daher synchronisierten Zugriff.
 */
export class FormatDefinitionDatum implements IFormatDefinition {
	protected _format: string;
	protected _metaFormat: string;
	private _zusatzPruefung: boolean;
	private _teilBekanntArt: DatumTeilbekanntArt;
	protected _laenge: number;
	protected _feldTyp: string;

	private _datePrecision: DatePrecision;
	private _conversionConfigDate: DateConversionConfig;
	protected _internalDataFormats: DataFormats;

	private _dateFromCache: HashMap<string, TimeZonedDate> = new HashMap();
	private _dateFromForAdditionalCheckCache: HashMap<string, TimeZonedDate> = new HashMap();

	protected dateInvalid(d: Date, timeZone: string): boolean {
		let dateFrom = this._dateFromCache.get(timeZone);
		if (!dateFrom) {
			dateFrom = TimeZonedDate.createFromString("16.10.1583", "dd.MM.yyyy", timeZone);
			this._dateFromCache.set(timeZone, dateFrom);
		}
		return this._dateInvalid(d, timeZone, dateFrom); // begin of the gregorian calendar
	}

	protected dateInvalidForAdditionalCheck(d: Date, timeZone: string): boolean {
		let dateFrom = this._dateFromForAdditionalCheckCache.get(timeZone);
		if (!dateFrom) {
			dateFrom = TimeZonedDate.createFromString("01.01.1900", "dd.MM.yyyy", timeZone);
			this._dateFromForAdditionalCheckCache.set(timeZone, dateFrom);
		}
		return this._dateInvalid(d, timeZone, dateFrom);
	}

	private _dateInvalid(d: Date, timeZone: string, dateFrom: TimeZonedDate): boolean {
		const m: TimeZonedDate = TimeZonedDate.createFromDate(d, timeZone);
		return m.isBefore(dateFrom);
	}

	/**
	 * Konstruktor wird beim Einlesen der Formatdaten beim Initialisieren der
	 * Checker-Klasse aufgerufen.
	 */
	constructor(format: string, zusatzPruefung: boolean, teilbekanntheit: DatumTeilbekanntArt) {
		this._format = format;
		this._zusatzPruefung = zusatzPruefung;
		this._teilBekanntArt = teilbekanntheit;

		this._metaFormat = format.replace(/d/gi, "T").replace(/y/gi, "J");
		const formatWithCorrectLength: string = format.replace(/'/gi, "");
		this._laenge = formatWithCorrectLength.length;
		this._feldTyp = FeldFormatEnumType.ER_DATUM.toString();
		this._datePrecision = this._mapToDatePrecision(this._teilBekanntArt);
		this._conversionConfigDate = DateConversion.createDateConversionConfig(this._format, undefined, undefined, this._datePrecision);
		this._internalDataFormats = FormatDefinitionDatum.getInternalDataFormats(this._format);
	}

	public convert(
		wert: string,
		darstellungsInfo: DarstellungsInformationIntern,
		vonXml: boolean,
		_customFieldTypeFactory: ICustomFieldTypeFactory | undefined,
	): string {
		const internalDataType = ConversionUtils.createDateConversionConfig(
			this._conversionConfigDate,
			darstellungsInfo.baseYear,
			darstellungsInfo.timeZone,
			this._conversionConfigDate.format,
			this._internalDataFormats.dateFragmentOrdering,
			this._internalDataFormats.dateZeroOptional,
			this._internalDataFormats.dateTimeFormat,
			this._internalDataFormats.timeFormat,
			this._internalDataFormats.formatCharacters
		);
		const displayDataType = darstellungsInfo.createDateConversionConfig(this._conversionConfigDate, this._internalDataFormats);
		let conversionResult;
		if (vonXml) {
			conversionResult = DateConversion.convert(wert, internalDataType, displayDataType);
		} else {
			conversionResult = DateConversion.convert(wert, displayDataType, internalDataType);
		}
		return conversionResult;
	}

	public validiereFormat(
		param: IValidierungParam,
		fullCheck: boolean
	): ValidType {
		if (!fullCheck && param.getWert().length === 0) {
			// this case is for the base type conversion -> an empty value will be converted to null later on
			return ValidType.Valid;
		}

		const info: DarstellungsInformationIntern = param.getDarstellungsInfo();
		const pruefFormat: string = info.convertDatumFormatXml2Ofl(this._format);

		const dateWithFormat: DateAndFormat = new DateAndFormat(
			param.getWert(),
			pruefFormat,
			param.getMetaData().getTimeZone()
		);
		DateUtil.completeDateAndFormat(
			dateWithFormat,
			this._format,
			info.datumTrennerHintenOptional(this._format),
			param.getMetaData().getBaseYearOrDefault()
		);

		const teilbekanntErgebnis: ValidType = this._checkTeilbekanntSpezifischePruefungen(
			dateWithFormat,
			param
		);
		if (ValidType.Valid !== teilbekanntErgebnis) {
			return teilbekanntErgebnis;
		}

		if (this.isTeilbekannt) {
			/*
			 * Ersetze den Tag/Monat 00 durch 01, das Jahr 0000 durch 2000
			 */
			PartiallyKnownDatesUtil.replaceUnknownDateParts(dateWithFormat);
		}

		if (!ValidationDateParser.isSyntacticallyCorrect(dateWithFormat)) {
			return this._werfeAllgemeinenFormatFehler(param);
		}

		let date: Date;
		try {
			// feine Überprüfung durch Parsen
			date = ValidationDateParser.parseDateWithoutSyntaxCheck(dateWithFormat);
		} catch (_e) {
			return this._createDetailedDatumFormatFehler(dateWithFormat, param, fullCheck);
		}

		if (!fullCheck) {
			return ValidType.Valid;
		}

		const timeZone = param.getMetaData().getTimeZone();
		// Falls Zusatzprüfung, prüfe das Datum.
		if (this._zusatzPruefung) {
			if (this.dateInvalidForAdditionalCheck(date, timeZone)) {
				return this._createFehler(
					param,
					RuntimeFormalErrorEnum.DATUM_ADDITIONAL_CHECK,
					IFormaleFehlerConstants.FEHLER_DATUM
				);
			}
			return ValidType.Valid;
		}

		// check if date generally valid
		if (this.dateInvalid(date, timeZone)) {
			return this._createFehler(
				param,
				RuntimeFormalErrorEnum.DATUM,
				IFormaleFehlerConstants.FEHLER_DATUM
			);
		}

		return ValidType.Valid;
	}

	// For format MM/DD/YYYY'T'HH:mm:ss, getValueOfDatePart("04/31/2016T12:10:05", DatePart.YEAR) returns 2016T12:10:05.
	private static getNumPart(s: string): string {
		for (let i: number = 0; i < s.length; i++) {
			if (s.charAt(i) < '0' || s.charAt(i) > '9') {
				return s.substring(0, i);
			}
		}
		return s;
	}

	protected _createDetailedDatumFormatFehler(
		dateWithFormat: DateAndFormat,
		param: IValidierungParam,
		fullCheck: boolean,
		fehlerBereich?: FehlerBereich,
		trenner?: string
	): ValidType {
		const day: number = Number(FormatDefinitionDatum.getNumPart(PartiallyKnownDatesUtil.getValueOfDatePart(dateWithFormat, DatePart.DAY)));
		const month: number = Number(FormatDefinitionDatum.getNumPart(PartiallyKnownDatesUtil.getValueOfDatePart(dateWithFormat, DatePart.MONTH)));
		const year: number = Number(FormatDefinitionDatum.getNumPart(PartiallyKnownDatesUtil.getValueOfDatePart(dateWithFormat, DatePart.YEAR)));
		if (Number.isNaN(day) || Number.isNaN(month) || Number.isNaN(year) || day == 0 || month == 0 || year == 0) {
			if (fehlerBereich !== undefined && trenner !== undefined) {
				return (this as any as FormatDefinitionDatumBereich)._createFehlerForDatumBereich(param, RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT,
					IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT // instead of IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_DATUM
				);
			} else {
				return this._werfeAllgemeinenFormatFehler(param);
			}
		}
		if (fullCheck && this._zusatzPruefung && year < 1900) {
			if (fehlerBereich !== undefined && trenner !== undefined) {
				return (this as any as FormatDefinitionDatumBereich)._createFehlerForDatumBereich(param, RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM_ADDITIONAL_CHECK,
					IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT // instead of IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_DATUM
				);
			} else {
				return this._createFehler(
					param,
					RuntimeFormalErrorEnum.DATUM_ADDITIONAL_CHECK,
					IFormaleFehlerConstants.FEHLER_DATUM_FORMAT // instead of IFormaleFehlerConstants.FEHLER_DATUM like below
				);
			}
		}
		if (month > 12) {
			const replacements: LocalizableArgs = {
				dateStringMonth: LocalizableUtil.createPlainPlaceholder(month.toString())
			};
			if (fehlerBereich !== undefined && trenner !== undefined) {
				return (this as any as FormatDefinitionDatumBereich)._createFehlerForDatumBereich(param,
					fehlerBereich === FehlerBereich.BEREICH_START_DATUM ? RuntimeFormalErrorEnum.DATUM_BEREICH_START_MONAT_FEHLER : RuntimeFormalErrorEnum.DATUM_BEREICH_END_MONAT_FEHLER,
					IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT,
					replacements
				);
			}
			else {
				return this._createFehler(
					param,
					RuntimeFormalErrorEnum.DATUM_MONAT_FEHLER,
					IFormaleFehlerConstants.FEHLER_DATUM_FORMAT,
					replacements
				);
			}
		}
		if (day == 29 && month == 2) {
			const replacements: LocalizableArgs = {
				dateString: LocalizableUtil.createPlainPlaceholder(dateWithFormat.date),
				dateStringYear: LocalizableUtil.createPlainPlaceholder(year.toString())
			};
			if (fehlerBereich !== undefined && trenner !== undefined) {
				return (this as any as FormatDefinitionDatumBereich)._createFehlerForDatumBereich(param,
					fehlerBereich === FehlerBereich.BEREICH_START_DATUM ? RuntimeFormalErrorEnum.DATUM_BEREICH_START_FEBRUAR_29_FEHLER : RuntimeFormalErrorEnum.DATUM_BEREICH_END_FEBRUAR_29_FEHLER,
					IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT,
					replacements
				);
			} else {
				return this._createFehler(
					param,
					RuntimeFormalErrorEnum.DATUM_FEBRUAR_29_FEHLER,
					IFormaleFehlerConstants.FEHLER_DATUM_FORMAT,
					replacements
				);
			}
		}
		const replacements: LocalizableArgs = {
			dateString: LocalizableUtil.createPlainPlaceholder(dateWithFormat.date),
			dateStringDay: LocalizableUtil.createPlainPlaceholder(day.toString())
		};
		if (fehlerBereich !== undefined && trenner !== undefined) {
			return (this as any as FormatDefinitionDatumBereich)._createFehlerForDatumBereich(param,
				fehlerBereich === FehlerBereich.BEREICH_START_DATUM ? RuntimeFormalErrorEnum.DATUM_BEREICH_START_TAG_FEHLER : RuntimeFormalErrorEnum.DATUM_BEREICH_END_TAG_FEHLER,
				IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT,
				replacements
			);
		} else {
			return this._createFehler(
				param,
				RuntimeFormalErrorEnum.DATUM_TAG_FEHLER,
				IFormaleFehlerConstants.FEHLER_DATUM_FORMAT,
				replacements
			);
		}
	}

	private _checkTeilbekanntSpezifischePruefungen(
		dateWithFormat: DateAndFormat,
		param: IValidierungParam
	): ValidType {
		if (this.isTeilbekannt) {
			if (!ValidationDateParser.isSyntacticallyCorrect(dateWithFormat)) {
				return this._createFehler(
					param,
					RuntimeFormalErrorEnum.DATUM_FORMAT,
					IFormaleFehlerConstants.FEHLER_DATUM_FORMAT
				);
			}
			const runtimeFormalError: RuntimeFormalErrorEnum =
				TeilbekanntesDatumUtil.pruefeNichtBekannteDatumsteile(dateWithFormat, this._teilBekanntArt);
			if (runtimeFormalError === RuntimeFormalErrorEnum.KEIN_FEHLER) {
				return ValidType.Valid;
			}
			const errorTexts = FehlerHandler.getAllErrorTexts(runtimeFormalError);
			const key = LocalizableUtil.createFormalErrorKey(runtimeFormalError);
			const localizableErrorText: Localizable = {
				key,
				defaults: errorTexts
			}
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_DATUM, [localizableErrorText], param);
		}
		return ValidType.Valid;
	}

	private _werfeAllgemeinenFormatFehler(
		param: IValidierungParam
	): ValidType {
		return this._createFehler(
			param,
			RuntimeFormalErrorEnum.DATUM_FORMAT,
			IFormaleFehlerConstants.FEHLER_DATUM_FORMAT
		);
	}

	private _createFehler(
		param: IValidierungParam,
		formalError: RuntimeFormalErrorEnum,
		fehlerId: string,
		replacements?: LocalizableArgs
	): ValidType {
		const dateFormatPlaceholder: DataFormatPlaceholder = {
			type: "dataFormat",
			value: this._format,
			properties: {
				type: "date"
			}
		}
		const args: LocalizableArgs = {
			dateFormat: dateFormatPlaceholder, ...replacements
		};
		const localizableErrorText: Localizable = LocalizableUtil.createLocalizableFormalError(
			formalError,
			args
		);
		return FehlerHandler.addFehler(fehlerId, [localizableErrorText], param);
	}

	public getDefaultValue(
		_metaData: IMetaDataValidierung,
		_dataFormats: DataFormats
	): string {
		return "";
	}

	public get isZusatzPruefung(): boolean {
		return this._zusatzPruefung;
	}

	public get isTeilbekannt(): boolean {
		return this._teilBekanntArt !== DatumTeilbekanntArt.BEKANNT;
	}

	public get format(): string {
		return this._format;
	}

	public get teilbekanntArt(): DatumTeilbekanntArt {
		return this._teilBekanntArt;
	}

	public get datePrecision(): DatePrecision {
		return this._datePrecision;
	}

	public getWert(key: string): any {
		if (key === IMetaKeys.FIELD_MIN_LENGTH) {
			return this._laenge;
		}
		if (key === IMetaKeys.FIELD_MAX_LENGTH) {
			return this._laenge;
		}
		if (key === IMetaKeys.FIELD_FIELD_TYPE) {
			return this._feldTyp;
		}
		if (key === IMetaKeys.FIELD_FORMAT) {
			return this._metaFormat;
		}
		return undefined;
	}

	// return type is also string because of DateRange and partially known dates
	public convertToBasicType(
		internalValue: string,
		metaData: IMetaDataValidierung
	): Date | Date[] | string | null {
		const internalDataType = ConversionUtils.createDateConversionConfig(
			this._conversionConfigDate,
			metaData.getBaseYearOrDefault(),
			metaData.getTimeZone(),
			this._conversionConfigDate.format,
			this._internalDataFormats.dateFragmentOrdering,
			this._internalDataFormats.dateZeroOptional,
			this._internalDataFormats.dateTimeFormat,
			this._internalDataFormats.timeFormat,
			this._internalDataFormats.formatCharacters
		);
		const conversionResult = DateConversion.convertStringToDate(internalValue, internalDataType);
		return (conversionResult instanceof Date || typeof conversionResult === "string") ? conversionResult : null;
	}

	public convertFromBasicType(
		value: SupportedType,
		metaData: IMetaDataValidierung
	): string | null {
		const internalDataType = DateConversion.createDateConversionConfig(this._format, metaData.getBaseYearOrDefault(), metaData.getTimeZone(), this._datePrecision);
		return defaultValueConversion(this._internalDataFormats).formatValue(value, internalDataType) ?? null;
	}

	getConversionConfig(): ValueConversionConfig {
		return this._conversionConfigDate;
	}

	private _mapToDatePrecision(teilBekanntArt: DatumTeilbekanntArt): DatePrecision {
		switch (teilBekanntArt) {
			case DatumTeilbekanntArt.BEKANNT:
				return "FULL";
			case DatumTeilbekanntArt.TAG_OPTIONAL:
				return "DAY_OPTIONAL";
			case DatumTeilbekanntArt.MONAT_OPTIONAL:
				return "MONTH_OPTIONAL";
			case DatumTeilbekanntArt.JAHR_OPTIONAL:
				return "YEAR_OPTIONAL";
		}
		throw new Error(StringUtil.stringFormat("Date precision '{0}' could not be mapped.", teilBekanntArt.toString()));
	}

	public static getInternalDataFormats(format: string): DataFormats {
		const _defaultDataFormats = DarstellungsInformationIntern.getInternalDataFormats(".");
		switch (format) {
			case "dd.MM.yyyy":
			case "dd.MM.":
			case "dd.MM":
				return {
					..._defaultDataFormats,
					dateFragmentOrdering: "DAY_MONTH_YEAR",
					dateSeparator: "."
				};
			case "ddMM":
				return {
					..._defaultDataFormats,
					dateFragmentOrdering: "DAY_MONTH_YEAR",
					dateSeparator: ""
				};
			case "yyyyMMdd":
			case "MMdd":
			case "yyyyMM":
				return {
					..._defaultDataFormats,
					dateFragmentOrdering: "YEAR_MONTH_DAY",
					dateSeparator: ""
				};
			default:
				return _defaultDataFormats;
		}
	}

}
