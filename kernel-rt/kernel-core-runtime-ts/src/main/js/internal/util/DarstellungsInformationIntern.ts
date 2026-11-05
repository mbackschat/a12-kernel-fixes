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
	DataFormats,
	DateConversionConfig,
	DateRangeConversionConfig
} from "@com.mgmtp.a12.utils/utils-localization";

import { ZahlArt } from "../formatdef/FormatDefinitionZahl.js";

import { ConversionUtils } from "../formatdef/ConversionUtils.js";

import { DarstellungsInfoDatum } from "./DarstellungsInfoDatum.js";
import { FeldDarstellungsTyp } from "./FeldDarstellungsTyp.js";

/**
 * Die Klasse kapselt die Prüfungen für Darstellungsinformation
 * Stellt die Information für die Darstellungsspezifischen Formate bereit.
 * 
 */
export class DarstellungsInformationIntern {
	// Liste der gültigen DezimalTrenner
	private static VALID_TRENNER: string[] = [".", ","];

	private _dataFormats: DataFormats | null;
	private _wahrWert: string | undefined;
	private _falschWert: string | undefined;
	private _doConvertCustomFieldTypesAlways: boolean;
	private _doConvertCustomFieldTypes: Set<string>;
	private _decimalPlacesOptional: boolean;
	private _infoDatum: DarstellungsInfoDatum;
	private _dezimalTrenner: string[] = new Array(2);
	private readonly _thousandsSeparator: string | undefined;
	private readonly _thousandsSeparatorApplyToTrait: string | undefined;

	readonly baseYear: number;
	readonly timeZone: string;

	/**
	 * Der Konstruktor überprüft die Darstellungsinformation
	 * @param dezimalTrennerIntern 	Dezimaltrenner in XML-Darstellung
	 * @param info 	übergebene Darstellungsinformation
	 * @param baseYear 	base year of the model
	 * @param timeZone the timeZone used to en-/decode dates.
	 */
	public constructor(
		dezimalTrennerIntern: string,
		info: DataFormats | null,
		baseYear: number,
		timeZone: string
	) {
		if (dezimalTrennerIntern.length > 1) {
			throw new Error("The decimal separator has to be a single character.");
		}
		this.baseYear = baseYear;
		this.timeZone = timeZone;
		this._dataFormats = info;

		this._dezimalTrenner[0] = dezimalTrennerIntern;
		this._dezimalTrenner[1] = dezimalTrennerIntern;
		if (!info) {
			this._infoDatum = new DarstellungsInfoDatum();
			this._doConvertCustomFieldTypesAlways = false;
			this._doConvertCustomFieldTypes = new Set();
			this._falschWert = undefined;
			this._wahrWert = undefined;
			this._decimalPlacesOptional = false;
			this._thousandsSeparator = "";
			this._thousandsSeparatorApplyToTrait = undefined;
			return;
		}

		// we need to extract the thousands separator first, since we need this information for future checks
		this._thousandsSeparator = info.thousandsSeparator;
		this._thousandsSeparatorApplyToTrait = info.thousandsSeparatorApplyToTrait;

		// check decimal separator
		const dezTrenner: string | undefined = info.decimalSeparator;
		if (dezTrenner != undefined) {
			this._dezimalTrenner[1] = dezTrenner.charAt(0);
		}
		this._checkSeparators(dezTrenner);

		this._wahrWert = info.trueValue;
		this._falschWert = info.falseValue;

		const convertCustomFieldTypesArr = info.convertCustomFieldTypesList;
		if (convertCustomFieldTypesArr != undefined) {
			this._doConvertCustomFieldTypes = new Set(convertCustomFieldTypesArr);
		} else {
			this._doConvertCustomFieldTypes = new Set();
		}
		this._doConvertCustomFieldTypesAlways = info.convertCustomFieldTypesAll ?? false;
		this._checkCustomFieldTypeConversion();

		this._decimalPlacesOptional = info.decimalPlacesOptional ?? false;

		// Konvertierungsinformation zu Datumsdarstellungen wird in eine eigene Klasse ausgelagert
		this._infoDatum = new DarstellungsInfoDatum(info);
	}

	private _checkCustomFieldTypeConversion(): void {
		if (this._doConvertCustomFieldTypesAlways && this._doConvertCustomFieldTypes.size !== 0) {
			throw new Error(
				"When defining custom field types to always be converted, it is invalid to " +
				"additionally specify a concrete list of custom field types to be converted."
			);
		}
	}

	public convertDatumFormatXml2Ofl(xmlFormat: string): string {
		return this._infoDatum.convertDatumFormatXml2Ofl(xmlFormat);
	}

	public convertDatumBereichTrennerXML2Ofl(xmlDatumBereichTrenner: string): string {
		return this._infoDatum.convertDatumBereichTrennerXML2Ofl(xmlDatumBereichTrenner);
	}

	// gib immer die Oberflächenvalidierung zurück. Falls es keine spezifische
	// Oberflächenvalidierung gibt, ist die Oberflächenvalidierung bereits im
	// Konstruktor auf die XML Darstellung gesetzt.  Daher muss hier nicht mehr
	// unterschieden werden.
	public get dezimalTrenner(): string {
		return this._dezimalTrenner[FeldDarstellungsTyp.OBERFLAECHEN_DARSTELLUNG];
	}

	public get dezimalTrennerIntern(): string {
		return this._dezimalTrenner[FeldDarstellungsTyp.XML_DARSTELLUNG];
	}

	private _checkSeparators(dezTrenner: string | undefined): void {
		if (
			dezTrenner != undefined &&
			DarstellungsInformationIntern.VALID_TRENNER.indexOf(dezTrenner) === -1
		) {
			throw new Error(
				"Invalid presentation information: The decimal separator '" + dezTrenner + "' is invalid."
			);
		}
		if (this._thousandsSeparator && !dezTrenner) {
			throw new Error("If a thousands separator was defined, a decimal separator must be defined.");
		}
		if (this._thousandsSeparator != undefined && this._thousandsSeparator === this.dezimalTrenner) {
			throw new Error("Decimal separator and thousands separator must not be the same!");
		}
		if (this._thousandsSeparatorApplyToTrait != undefined) {
			switch (this._thousandsSeparatorApplyToTrait) {
				case ZahlArt.GELDBETRAG.toString():
				case ZahlArt.ZAHL.toString():
					break;
				default:
					throw new Error("Invalid presentation information: The thousandsSeparatorApplyToTrait '" + this._thousandsSeparatorApplyToTrait + " is invalid.");
			}
		}
	}

	/**
	 * Gebe den Index (Punkt oder Komma) für die Validierung zurück
	 * In den Arrays ist immer die Punktnotation in Wert 0 gespeichert und
	 * die Kommanotation in Wert 1
	 *
	 * @return Index, der in dem Array die relevante Darstellung zurückgibt.
	 */
	public get dezimalIndexFuerValidierung(): number {
		return this.dezimalTrenner === "." ? 0 : 1;
	}

	public get falschWert(): string | undefined {
		return this._falschWert;
	}

	public get wahrWert(): string | undefined {
		return this._wahrWert;
	}

	/**
	 * Zur Verwendung des Dezimaltrenners in einem Pattern
	 * muss der Punkt (nicht das Komma) escaped werden.
	 */
	public static getDezimalTrennerForPattern(index: number): string {
		const result: string = DarstellungsInformationIntern.VALID_TRENNER[index];
		if (result === ".") {
			return "\\.";
		}
		return result;
	}

	public getMinLaengeDatumBereich(xmlDatumFormat: string, xmlDatumBereichTrenner: string): number {
		return this._infoDatum.getMinLaengeDatumBereich(xmlDatumFormat, xmlDatumBereichTrenner);
	}

	public getMaxLaengeDatumBereich(xmlDatumFormat: string, xmlDatumBereichTrenner: string): number {
		return this._infoDatum.getMaxLaengeDatumBereich(xmlDatumFormat, xmlDatumBereichTrenner);
	}

	public getMinLaengeDatum(xmlFormat: string): number {
		return this._infoDatum.getMinLaengeDatum(xmlFormat);
	}

	public getMaxLaengeDatum(xmlFormat: string): number {
		return this._infoDatum.getMaxLaengeDatum(xmlFormat);
	}

	/**
	 * Liefert <code>true</code>, wenn für das übergebene Format bzgl. der Darstellungsinformation
	 * ein Datumstrenner hinten erlaubt ist, aber optional. Dies ist der Fall, wenn
	 * <ul>
	 * <li>in der Darstellungsinformation der Punkt als Datumstrenner spezifziert wurde</li>
	 * <li>in der Darstellungsinformation eine Datumsreihenfolge spezifiziert wurde, in der Tag vor
	 * Monat kommt</li>
	 * <li>das Format spezifiziert Tag und Monat, kein Jahr</li>
	 * </ul>
	 */
	public datumTrennerHintenOptional(datumsFormat: string): boolean {
		return this._infoDatum.datumTrennerHintenOptional(datumsFormat);
	}

	public doConvertCustomFieldType(customFieldTypeName: string): boolean {
		return (
			this._doConvertCustomFieldTypesAlways ||
			this._doConvertCustomFieldTypes.has(customFieldTypeName)
		);
	}

	public createDateConversionConfig(conversionConfig: DateConversionConfig, defaultDataFormats: DataFormats): DateConversionConfig & DataFormats {
		const format = this._infoDatum.convertDatumFormatXml2Ofl(conversionConfig.format);
		return ConversionUtils.createDateConversionConfig(
			conversionConfig,
			this.baseYear,
			this.timeZone,
			format,
			this._dataFormats?.dateFragmentOrdering ?? defaultDataFormats.dateFragmentOrdering,
			this._infoDatum._nullenOptional,
			this._infoDatum._dateTimeFormat ?? defaultDataFormats.dateTimeFormat,
			this._infoDatum._timeFormat ?? defaultDataFormats.timeFormat,
			this._dataFormats?.formatCharacters ?? defaultDataFormats.formatCharacters
		);
	}

	public createDateRangeConversionConfig(
		conversionConfig: DateRangeConversionConfig,
		defaultDataFormats: DataFormats
	): DateRangeConversionConfig & DataFormats {
		// if there is no specific date format or range separator defined for display, we take the internal representation
		const format = this._infoDatum.convertDatumFormatXml2Ofl(conversionConfig.format);
		const rangeSeparator = this._infoDatum.convertDatumBereichTrennerXML2Ofl(defaultDataFormats.dateRangeSeparator);
		return ConversionUtils.createDateRangeConversionConfig(
			conversionConfig,
			this.baseYear,
			this.timeZone,
			format,
			this._dataFormats?.dateFragmentOrdering ?? defaultDataFormats.dateFragmentOrdering,
			this._infoDatum._nullenOptional,
			this._dataFormats?.formatCharacters ?? defaultDataFormats.formatCharacters,
			rangeSeparator
		);
	}

	public get decimalPlacesOptional(): boolean {
		return this._decimalPlacesOptional;
	}

	public getThousandsSeparatorForTrait(trait: string): string | undefined {
		if (this._thousandsSeparatorApplyToTrait == undefined ||
			this._thousandsSeparatorApplyToTrait === trait) {
			return this._thousandsSeparator;
		} else {
			return undefined;
		}
	}

	public static getInternalDataFormats(decimalSeparator: string): DataFormats {
		return {
			decimalSeparator: decimalSeparator,
			decimalPlacesOptional: false,
			trueValue: "true",
			falseValue: "false",
			dateFragmentOrdering: "YEAR_MONTH_DAY",
			dateSeparator: "-",
			dateZeroOptional: false,
			dateTimeFormat: "yyyy-MM-dd'T'HH:mm:ss",
			timeFormat: "HH:mm:ss",
			dateRangeSeparator: "/",
			formatCharacters: {
				year: "y",
				month: "M",
				day: "d"
			}
		};
	}

}
