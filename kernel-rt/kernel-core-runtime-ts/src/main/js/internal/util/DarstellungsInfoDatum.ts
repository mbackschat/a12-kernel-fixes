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
import type { DataFormats } from "@com.mgmtp.a12.utils/utils-localization";

import {
	DatePart,
	DateUtil,
	StringUtil
} from "@com.mgmtp.a12.utils/utils-localization/a12internal";

export interface DarstellungsInfoDatumProxy {
	_nullenOptional: boolean;
	checkReihenfolge(reihenfolge: string): void;
}

interface IDatumReihenfolge {
	tagVorMonat(): boolean;
	getFormatFragment(srcFormat: string, i: number): string;
}

export class DarstellungsInfoDatum implements DarstellungsInfoDatumProxy {
	private static get GUELTIGE_REIHENFOLGEN(): string[] {
		return ["YEAR_MONTH_DAY",
			"YEAR_DAY_MONTH",
			"DAY_YEAR_MONTH",
			"DAY_MONTH_YEAR",
			"MONTH_YEAR_DAY",
			"MONTH_DAY_YEAR"];
	}

	private readonly _datumTrenner: string | undefined;
	private readonly _datumBereichTrenner: string | undefined;
	private readonly _fragmentReihenfolge: IDatumReihenfolge | undefined;
	readonly _nullenOptional: boolean;
	readonly _dateTimeFormat: string | undefined;
	readonly _timeFormat: string | undefined;

	private DatumReihenfolge = class implements IDatumReihenfolge {
		private fragmente: DatePart[] = new Array(3);

		constructor(private parent: DarstellungsInfoDatumProxy, reihenfolge: string) {
			parent.checkReihenfolge(reihenfolge);
			if (reihenfolge != undefined) {
				const parts = reihenfolge.split("_");
				for (let i = 0; i <= 2; i++) {
					this.fragmente[i] = DatePart.parseFragmentOrder(parts[i]);
				}
			}
		}

		tagVorMonat(): boolean {
			return (
				this.fragmente[0] === DatePart.DAY || this.fragmente[2] === DatePart.MONTH
			);
		}

		getFormatFragment(srcFormat: string, i: number): string {
			if (i >= this.fragmente.length || this.fragmente[i] == undefined) {
				return "";
			}
			const fragment: DatePart = this.fragmente[i];
			if (!fragment.isInFormat(srcFormat)) {
				return "";
			}
			switch (fragment) {
				case DatePart.MONTH:
					return DateUtil.getMonthFormat(this.parent._nullenOptional);
				case DatePart.YEAR:
					return DateUtil.YEAR_FORMAT;
				case DatePart.DAY:
					return DateUtil.getDayFormat(this.parent._nullenOptional);
				default:
					return "";
			}
		}
	};

	constructor(
		info?: DataFormats
	) {
		if (!info) {
			this._datumTrenner = undefined;
			this._datumBereichTrenner = undefined;
			this._fragmentReihenfolge = undefined;
			this._nullenOptional = false;
			this._dateTimeFormat = undefined;
			this._timeFormat = undefined;
		} else {
			this._datumTrenner = info.dateSeparator;
			this._datumBereichTrenner = info.dateRangeSeparator;

			this._nullenOptional = info.dateZeroOptional ?? false;

			const reihenfolge: string | undefined = info.dateFragmentOrdering;
			this._fragmentReihenfolge = reihenfolge
				? new this.DatumReihenfolge(this, reihenfolge)
				: undefined;

			this._dateTimeFormat = info.dateTimeFormat;

			this._timeFormat = info.timeFormat;

			this._checkSchluesselWerte();
		}
	}

	private _checkSchluesselWerte(): void {
		this._checkDatumTrenner();
		this._checkDatumBereichTrenner();
		this._checkWertekompatibel();
		this._checkDateTimeFormat();
	}

	private _werfeDarstellungsInfoFehler(fehlerMeldung: string): void {
		throw new Error("Error in the presentation information: " + fehlerMeldung);
	}

	/**
	 * Prüfe, dass der Wert für den Datumstrenner nicht gesetzt ist oder zulässig ist.
	 * Leerzeichen oder mehr als ein Zeichen sind als Datumstrenner nicht zulässig.
	 */
	private _checkDatumTrenner(): void {
		if (this._datumTrenner == undefined) {
			return;
		}
		if (" " === this._datumTrenner) {
			this._werfeDarstellungsInfoFehler("The date separator may not be a blank.");
		}
		if (this._datumTrenner.length > 1) {
			this._werfeDarstellungsInfoFehler(
				StringUtil.stringFormat(
					"The date separator must be a single character. Instead '{0}' was given.",
					this._datumTrenner
				)
			);
		}
	}

	private _checkDatumBereichTrenner(): void {
		if (this._datumBereichTrenner != undefined && this._datumBereichTrenner.length !== 1) {
			this._werfeDarstellungsInfoFehler(
				StringUtil.stringFormat(
					"The date range separator must be a single character. Instead '{0}' was given.",
					this._datumBereichTrenner
				)
			);
		}
	}

	/**
	 * Prüfe, dass die Werte für Datumdarstellung zueinander passen.
	 * <ul>
	 * <li>datumsTrenner, datumsBereichsTrenner und datumsReihenfolge müssen gemeinsam oder gar
	 * nicht angegeben werden</li>
	 * <li>Wenn Nullen im Datum optional sind, dann muss ein Datumstrenner spezifiziert sein.</li>
	 * <li>Datumstrenner und Datumsbereichstrenner müssen unterschiedlich sein.</li>
	 * </ul>
	 */
	private _checkWertekompatibel(): void {
		if (
			!this._alleOderKeinsNull(
				this._datumTrenner,
				this._datumBereichTrenner,
				this._fragmentReihenfolge
			)
		) {
			/* eslint:disable */
			const errorMsg =
				"If a value for one of the keys 'DatumTrenner', 'DatumBereichTrenner' or " +
				"'DatumReihenfolge' was specified, a value for the others needs to be specified as well.";
			/* eslint:enable */
			this._werfeDarstellungsInfoFehler(errorMsg);
		}
		if (this._nullenOptional && !this._datumTrenner) {
			this._werfeDarstellungsInfoFehler(
				"The key 'DatumNullenOptional' is active, but no value was specified for the key 'DatumTrenner'."
			);
		}
		if (this._datumTrenner && this._datumTrenner === this._datumBereichTrenner) {
			this._werfeDarstellungsInfoFehler(
				"The keys 'DatumTrenner' and 'DatumBereichTrenner' must be different."
			);
		}
	}

	/**
	 * Ensure, that the date time format contains a year specification.
	 */
	private _checkDateTimeFormat(): void {
		if (this._dateTimeFormat && this._dateTimeFormat.length !== 0 && this._dateTimeFormat.indexOf("y") < 0) {
			this._werfeDarstellungsInfoFehler(
				"The key 'DateTimeFormat' must contain a year specification (y)."
			);
		}
	}

	private _alleOderKeinsNull(...werte: any[]): boolean {
		let einsIstNull = false;
		let einsIstNichtNull = false;
		for (const wert of werte) {
			if (wert == undefined) {
				einsIstNull = true;
			} else {
				einsIstNichtNull = true;
			}
		}
		return !(einsIstNull && einsIstNichtNull);
	}

	checkReihenfolge(datumReihenfolge: string): void {
		if (
			datumReihenfolge != undefined &&
			DarstellungsInfoDatum.GUELTIGE_REIHENFOLGEN.indexOf(datumReihenfolge) === -1
		) {
			this._werfeDarstellungsInfoFehler(
				"The key 'DatumReihenfolge' has the invalid value '" + datumReihenfolge + "'."
			);
		}
	}

	/**
	 * Konvertiert den XML Format-String in den Formatstring gemäß der
	 * {@link DataFormats}. Für die Konvertierung gilt:
	 * <ul>
	 * <li>im Sonderfall, daß ein Trenner hinten erlaubt ist (vgl.
	 * {@link datumTrennerHintenOptional}), wird hier das Format <b>ohne</b> diesen optionalen
	 * Trenner erzeugt.</li>
	 * <li>sind Nullen optional, so wird für Tag und Monat "d" bzw. "M", also einstellig erzeugt
	 * </li>
	 * </ul>
	 */
	convertDatumFormatXml2Ofl(xmlFormat: string): string {
		if (xmlFormat.includes("HH")) {
			if (xmlFormat.includes("MM")) {
				// DateTime
				if (!this._dateTimeFormat) {
					// for empty presentation information use internal format
					return xmlFormat;
				} else {
					return this._dateTimeFormat;
				}
			} else {
				// Time
				if (!this._timeFormat) {
					// for empty presentation information use internal format
					return xmlFormat;
				} else {
					return this._timeFormat;
				}
			}
		}
		if (this._fragmentReihenfolge == undefined && !this._nullenOptional) {
			return xmlFormat;
		}
		const oflTrenner: string =
			this._datumTrenner != undefined ? this._datumTrenner : DateUtil.getDateSeparator(xmlFormat);
		if (this._fragmentReihenfolge != undefined) {
			// Reihenfolge von Tag, Monat und Jahr muss evtl. geändert werden
			const formatteile: string[] = [];
			for (let i = 0; i <= 2; i++) {
				if (this._fragmentReihenfolge.getFormatFragment(xmlFormat, i)) {
					formatteile.push(this._fragmentReihenfolge.getFormatFragment(xmlFormat, i));
				}
			}
			return formatteile.join(oflTrenner);
		}
		// keine darstellungsspezifische Reihenfolge
		// -> DatumTrenner wie im XML. Nur NullenOptional muss berücksichtigt werden
		return this._nullenOptional ? DateUtil.makeFormatStringDayMonthOptional(xmlFormat) : xmlFormat;
	}

	convertDatumBereichTrennerXML2Ofl(xmlDatumBereichTrenner: string): string {
		return this._datumBereichTrenner ? this._datumBereichTrenner : xmlDatumBereichTrenner;
	}

	/**
	 * Ermittelt die MinLaenge für ein Datumsformat für die Metaschnittstelle.
	 */
	getMinLaengeDatum(xmlFormat: string): number {
		return this.convertDatumFormatXml2Ofl(xmlFormat).replace(/'/g, "").length;
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
	datumTrennerHintenOptional(datumsFormat: string): boolean {
		if (!this._datumTrenner) {
			return false;
		}
		return (
			"." === this._datumTrenner &&
			DateUtil.formatContainsOnlyDayMonth(datumsFormat) &&
			// we expect that the fragment order is set, otherwise it should fail (but no valid return value)
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			this._fragmentReihenfolge!.tagVorMonat()
		);
	}

	/**
	 * Ermittelt die MaxLaenge für ein Datumsformat für die Metaschnittstelle.
	 */
	getMaxLaengeDatum(xmlFormat: string): number {
		let oflFormat: string = this.convertDatumFormatXml2Ofl(xmlFormat);
		// für die MaxLaenge nehmen wir den optionalen hinteren Trenner
		if (this.datumTrennerHintenOptional(oflFormat)) {
			oflFormat = oflFormat + ".";
		}

		oflFormat = DateUtil.makeFormatStringDayMonthTwoDigits(oflFormat);
		const oflFormatCorrectLength: string = oflFormat.replace(/'/gi, "");
		return oflFormatCorrectLength.length;
	}

	private _dbTrennerLaenge(xmlDatumBereichTrenner: string): number {
		const dbTrenner: string = this.convertDatumBereichTrennerXML2Ofl(xmlDatumBereichTrenner);
		if (!dbTrenner) {
			return 0;
		}
		return dbTrenner.length;
	}

	/**
	 * Ermittelt die MinLaenge für ein Datumsbereichsformat für die Metaschnittstelle.
	 */
	getMinLaengeDatumBereich(xmlDatumFormat: string, xmlDatumBereichTrenner: string): number {
		return (
			2 * this.getMinLaengeDatum(xmlDatumFormat) + this._dbTrennerLaenge(xmlDatumBereichTrenner)
		);
	}

	/**
	 * Ermittelt die MaxLaenge für ein Datumsbereichsformat für die Metaschnittstelle.
	 */
	getMaxLaengeDatumBereich(xmlDatumFormat: string, xmlDatumBereichTrenner: string): number {
		return (
			2 * this.getMaxLaengeDatum(xmlDatumFormat) + this._dbTrennerLaenge(xmlDatumBereichTrenner)
		);
	}
}
