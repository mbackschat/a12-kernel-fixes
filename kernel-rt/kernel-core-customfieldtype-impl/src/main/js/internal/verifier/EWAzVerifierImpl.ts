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

import type { IFadat } from "../../IFadat.js";

import { CustomFieldTypeErrorEnum } from "../error/CustomFieldTypeErrorEnum.js";
import { CustomFieldTypeErrorKeys } from "../error/CustomFieldTypeErrorKeys.js";
import { CustomFieldTypeErrorUtil } from "../error/CustomFieldTypeErrorUtil.js";

import { PruefzifferVerfahren } from "../utils/PruefzifferVerfahren.js";

import { CustomFieldTypeValidationParamImpl } from "../CustomFieldTypeValidationParamImpl.js";
import { EWAzProperties } from "../utils/EWAzProperties.js";

import { GermanTaxNumberDefiningData } from "../defdatastnr/GermanTaxNumberDefiningData.js";

import { AbstractFadatRelatedCustomFieldType } from "./AbstractFadatRelatedCustomFieldType.js";
import { SteuernummerVerifierImpl } from "./SteuernummerVerifierImpl.js";

/**
 * Checks the Einheitswert-Aktenzeichen.
 * The validation of the Einheitswert-Aktenzeichen consists of the following parts:
 * <ul>
 * <li>Is the Einheitswert-Aktenzeichen formally valid?</li>
 * <li>Does the Bundesfinanzamtsnummer included in the Einheitswert-Aktenzeichen exist?</li>
 * <li>Does the validation calculate the proper verification code?</li>
 * </ul>
 *
 * <p>
 * At the formal validation the Einheitswert-Aktenzeichen has to match the following pattern:
 * <code>[0-9]{4}[045][0-9]{8,14}</code>
 * </p>
 *
 * <p>
 * The first four characters of the Einheitswert-Aktenzeichens form the Bundesfinanzamtsnummer.
 * It will be checked if this number is valid according to the binding list of the technical
 * requirement concept (V35, chapter 1.2.2).
 * </p>
 *
 * The 5. position contains the format key. The classification term "Einheitswertaktenzeichen" has
 * the values 0, 4, or 5.
 *
 * <p>
 * Special rules for Schleswig-Holstein and Nordrhein-Westfalen will also be checked.
 * </p>
 *
 * <p>
 * Additionally, the Einheitswert-Aktenzeichen will be validated by a verification code check.
 * </p>
 *
 */

export class EWAzVerifierImpl extends AbstractFadatRelatedCustomFieldType implements ICustomFieldValidator {
	// Formale Prüfung: 12- bis 18-stellige Zahl.
	private static readonly _VALID_EWAZ = /^([0-9]{2})([0-9]{2})([045])[0-9]{8,14}$/;
	private static readonly _SPLIT_DISPLAY_VALUE_PATTERN = "(.*)(@)([A-Z]{2})";
	private static readonly _SPLIT_DISPLAY_VALUE = new RegExp(
		"^" + EWAzVerifierImpl._SPLIT_DISPLAY_VALUE_PATTERN + "$"
	);
	private static readonly _STELLEN_BUFANR: number = 4;
	private static readonly _LAENDERKUERZEL_NUMMERN: Map<string, number> = new Map<
		string,
		number
	>();

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static readonly _CONVERSION_ERROR_IDENTIFIER: string = "CONVERSION_ERROR";

	private static readonly _LAENDERKUERZEL_NW: string = "NW";
	private static readonly _LAENDERKUERZEL_SH: string = "SH";

	static _constructor = (() => {
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("BW", 28); // Baden-Württemberg
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("BY", 9); // Bayern
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("BE", 11); // Berlin
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("BB", 30); // Brandenburg
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("HB", 24); // Bremen
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("HH", 22); // Hamburg
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("HE", 26); // Hessen
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("MV", 40); // Mecklenburg-Vorpommern
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("NI", 23); // Niedersachsen
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set(EWAzVerifierImpl._LAENDERKUERZEL_NW, 5); // Nordrhein-Westfalen
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("RP", 27); // Rheinland-Pfalz
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("SL", 10); // Saarland
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("SN", 32); // Sachsen
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("ST", 31); // Sachsen-Anhalt
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set(EWAzVerifierImpl._LAENDERKUERZEL_SH, 21); // Schleswig-Holstein
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.set("TH", 41); // Thüringen
	})();

	public constructor(fadat: IFadat | undefined) {
		super(fadat);
	}

	private static _getLaenderKuerzel(buLaNr: number): string {
		let laenderkuerzel: string | undefined;
		EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.forEach((value: number, key: string) => {
			if (value === buLaNr) {
				laenderkuerzel = key;
			}
		});
		if (laenderkuerzel) {
			return laenderkuerzel;
		} else {
			throw new Error("No value present");
		}
	}

	private static _getLaenderNummer(buLaString: string): number {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.get(buLaString)!;
	}

	/**
	 *
	 * Checks the provided Einheitswert-Aktenzeichen.
	 *
	 * The defining data of the Steuernummerprüfung won't be used to check the existence of the
	 * Bundesfinanzamtsnummer - the technical concept (V35, chapter 1.2.2) contains a binding list
	 * of valid Bundesfinanzamtsnummer. It is only used to validate the provided Bundeslandkürzel.
	 * The defining data of the Steuernummerprüfung will also be forwarded to the SteuernummerVerifier
	 * if it is a Einheitswert-Aktenzeichen with northern countries (SH, HH, HB, BE).
	 * The bool parameter denotes if the value should be converted to xml format before the validation.
	 *
	 * The state specific properties of a Einheitswert-Aktenzeichen will be converted to
	 * XML format either in this method or in the conversion from the string representation.
	 * If no parameters or wrong parameters are provided then an {@link !Error Error} is thrown.
	 */
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
					CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER,
					CustomFieldTypeErrorKeys.UNGUELTIGES_EWAZ
				);
			}
			// nehmen wir die XML-Darstellung
			value = _convertResult.getConvertedValue();
		}
		let buLaNummer = 0;
		try {
			buLaNummer = this._getBuLaNrFromString(value);
			EWAzVerifierImpl._getLaenderKuerzel(buLaNummer);
		} catch (_e) {
			return CustomFieldTypeErrorUtil.getResult(
				CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER,
				CustomFieldTypeErrorKeys.UNGUELTIGES_EWAZ
			);
		}
		const error = this._checkEinheitswertAktenzeichen(value, buLaNummer);
		return CustomFieldTypeErrorUtil.getResult(
			error,
			CustomFieldTypeErrorKeys.UNGUELTIGES_EWAZ
		);
	}

	/**
	 * Prüft die angegebene Einheitswert-Aktenzeichen und liefert den Key der passenden
	 * Fehlermeldung bzw. {@link CustomFieldTypeErrorEnum#KEIN_FEHLER}, wenn das
	 * Einheitswert-Aktenzeichen gültig ist.
	 *
	 * @param buLaNummer
	 */
	private _checkEinheitswertAktenzeichen(
		ewaz: string,
		buLaNummer: number,
	): CustomFieldTypeErrorEnum {
		// Nach der formalen Prüfung muss das Einheitswert-Aktenzeichen je nach Bundesland 12-18 Stellen besitzen.
		const stelligkeit: number = EWAzProperties.getStelligkeit(buLaNummer);
		if (!EWAzVerifierImpl._VALID_EWAZ.test(ewaz) || ewaz.length !== stelligkeit + 1) {
			return CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER;
		}
		if (
			buLaNummer ===
			EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.get(EWAzVerifierImpl._LAENDERKUERZEL_SH) &&
			!this._istSHFormatGueltig(ewaz)
		) {
			return CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER;
		}
		if (
			buLaNummer ===
			EWAzVerifierImpl._LAENDERKUERZEL_NUMMERN.get(EWAzVerifierImpl._LAENDERKUERZEL_NW) &&
			!this._istNWFormatGueltig(ewaz)
		) {
			return CustomFieldTypeErrorEnum.EWAZ_FORMALER_FEHLER;
		}
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const matcher = EWAzVerifierImpl._VALID_EWAZ.exec(ewaz)!;
		const bundeslandnummerStr: string = matcher[1];
		const bundeslandnummer2st = Number(bundeslandnummerStr);
		const bundesfinanzamtsnummer = Number(bundeslandnummerStr.concat(matcher[2]));
		if (
			!EWAzProperties.getBundesfinanzamtsnummern(bundeslandnummer2st).has(bundesfinanzamtsnummer)
			&& !this.existsEWAzTestFederalTaxOfficeNumber(bundesfinanzamtsnummer)
		) {
			return CustomFieldTypeErrorEnum.EWAZ_FINANZAMT_FEHLER;
		}
		const formatschluesselStr: string = matcher[3];
		const formatschluessel = Number(formatschluesselStr);
		if (EWAzProperties.getFormatschluessel(buLaNummer) !== formatschluessel) {
			return CustomFieldTypeErrorEnum.EWAZ_FORMATSCHLUESSEL_FEHLER;
		}
		const verfahren: PruefzifferVerfahren.Verfahren =
			EWAzProperties.getVerfahrenDef(buLaNummer);
		if (verfahren === PruefzifferVerfahren.Verfahren.STNR) {
			if (new SteuernummerVerifierImpl(this._fadat).istEWAzAlsSteuernummerGueltig(ewaz)) {
				return CustomFieldTypeErrorEnum.EWAZ_PRUEFZIFFER_FEHLER;
			}
		} else {
			const ewazOhneFormatschluessel: string =
				ewaz.substring(0, 4) + ewaz.substring(5, stelligkeit + 1);
			if (!this._istPruefzifferRichtig(ewazOhneFormatschluessel, stelligkeit, verfahren)) {
				return CustomFieldTypeErrorEnum.EWAZ_PRUEFZIFFER_FEHLER;
			}
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Prüfe, dass beim Einheitswert-Aktenzeichen in Schleswig-Holstein nur die Bezirknummern
	 * 001-898
	 * [3.-5. (Ofl) bzw. 5.-7. Stelle (XML-Format)] zulässig sind.
	 *
	 * @param ewaz
	 */
	private _istSHFormatGueltig(ewaz: string): boolean {
		const bezirksnummer = Number(ewaz.substring(5, 8));
		return bezirksnummer >= 1 && bezirksnummer <= 898;
	}

	/**
	 * Prüfe, dass beim Einheitswert-Aktenzeichen in Nordrhein-Westfalen an der 7. (Ofl), bzw. 8.
	 * Stelle (XML-Format) nur die Grundbesitzart 1 oder 3 angegeben werden kann.
	 *
	 * @param ewaz
	 */
	private _istNWFormatGueltig(ewaz: string): boolean {
		const grundbesitzart: string = ewaz.substring(8, 9);
		return grundbesitzart === "1" || grundbesitzart === "3";
	}

	private _istPruefzifferRichtig(
		ewaz: string,
		stelligkeit: number,
		verfahren: PruefzifferVerfahren.Verfahren
	): boolean {
		const pruefziffer = Number(ewaz.substring(stelligkeit - 1));
		switch (verfahren) {
			case PruefzifferVerfahren.Verfahren.A1: {
				const faktoren: number[] = [0, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2];
				return this._pruefeMitA1Verfahren(ewaz, pruefziffer, faktoren);
			}
			case PruefzifferVerfahren.Verfahren.A2: {
				const faktoren: number[] = [0, 0, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2];
				return this._pruefeMitA2Verfahren(ewaz, pruefziffer, faktoren);
			}
			case PruefzifferVerfahren.Verfahren.A3: {
				const faktoren: number[] = [0, 0, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7];
				return this._pruefeMitA1Verfahren(ewaz, pruefziffer, faktoren);
			}
			case PruefzifferVerfahren.Verfahren.NRW: {
				const faktoren: number[] = [0, 1, 2, 3, 4, 7, 8, 9, 2, 3, 4, 7, 8];
				return this._pruefeMitA1Verfahren(ewaz, pruefziffer, faktoren);
			}
			default:
				// Integrationsproblem
				throw new Error(
					'Es wird kein Prüfziffernverfahren mit der Bezeichnung "' +
					verfahren +
					'" unterstützt.'
				);
		}
	}

	private _pruefeMitA1Verfahren(ewaz: string, pruefziffer: number, faktoren: number[]): boolean {
		let sum = 0;
		for (let i = 0; i < faktoren.length; i++) {
			sum += faktoren[i] * Number(ewaz.charAt(i));
		}
		return PruefzifferVerfahren.einerstelle(sum) === pruefziffer;
	}

	private _pruefeMitA2Verfahren(ewaz: string, pruefziffer: number, faktoren: number[]): boolean {
		let sum = 0;
		for (let i = 0; i < faktoren.length; i++) {
			sum += PruefzifferVerfahren.quersumme(faktoren[i] * Number(ewaz.charAt(i)));
		}
		return PruefzifferVerfahren.pruefePruefzifferAusDifferenz(sum, 10, pruefziffer);
	}

	/**
	 * Converts the Einheitswert-Aktenzeichen in XML format to the string representation.
	 *
	 * @return Einheitswert-Aktenzeichen in string representation
	 */
	convertInternal2Display(xmlEwAz: string): ICustomFieldTypeConversionResult {
		const valParam: ICustomFieldTypeValidationParam = new CustomFieldTypeValidationParamImpl(
			CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH,
			CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH
		);
		// Gültigkeitsprüfung und Setzen der bundeslandspezifischen Eigenschaften,
		// sowie Initialisierung und Prüfung der IGermanTaxNumberDefiningData:
		const _checkResult = this.validate(xmlEwAz, valParam, false);
		if (_checkResult) {
			return customFieldTypeConversionResult(
				xmlEwAz,
				"Konvertiere nur gültige Aktenzeichen. " +
				_checkResult.getErrorMessage()
			);
		}

		const bundeslandNr: number = this._getBuLaNrFromString(xmlEwAz);
		return customFieldTypeConversionResult(
			xmlEwAz.substring(
				EWAzVerifierImpl._STELLEN_BUFANR - EWAzProperties.getFANrStellen(bundeslandNr),
				EWAzVerifierImpl._STELLEN_BUFANR
			) + // Finanzamtsnummer
			xmlEwAz.substring(
				EWAzVerifierImpl._STELLEN_BUFANR + 1,
				EWAzProperties.getStelligkeit(bundeslandNr) + 1
			) + // EWAz ohne Finanzamtsnummer und Formatschlüssel
			"@" + // Trennzeichen vor Bundeslandkürzel
			EWAzVerifierImpl._getLaenderKuerzel(bundeslandNr)
		);
	}

	/**
	 * Converts the Einheitswert-Aktenzeichen in string representation to the XML format.
	 *
	 * @param ewAzDisplayValueWithLaenderkuerzel
	 *            - the string contains the Bundeslandkürzel split by the character '@'
	 *                          in addition to the Einheitswert-Aktenzeichen in string representation.
	 */
	convertDisplay2Internal(
		ewAzDisplayValueWithLaenderkuerzel: string
	): ICustomFieldTypeConversionResult {
		const mToSplitDisplayValue = EWAzVerifierImpl._SPLIT_DISPLAY_VALUE.exec(
			ewAzDisplayValueWithLaenderkuerzel
		);
		if (
			!mToSplitDisplayValue ||
			!EWAzVerifierImpl._SPLIT_DISPLAY_VALUE.test(ewAzDisplayValueWithLaenderkuerzel)
		) {
			throw new Error(
				"Das EW-Az in Oberflächendarstellung '" +
				ewAzDisplayValueWithLaenderkuerzel +
				"' entspricht nicht dem geforderten Pattern '" +
				EWAzVerifierImpl._SPLIT_DISPLAY_VALUE_PATTERN +
				"'."
			);
		}
		const landerkuerzel: string = mToSplitDisplayValue[3];
		if (!GermanTaxNumberDefiningData.instance.existsFederalStateCode(landerkuerzel)) {
			return customFieldTypeConversionResult(
				ewAzDisplayValueWithLaenderkuerzel,
				EWAzVerifierImpl._CONVERSION_ERROR_IDENTIFIER
			);
		}
		const bundeslandNr: number = EWAzVerifierImpl._getLaenderNummer(landerkuerzel);

		const ewAzDisplayValue: string = mToSplitDisplayValue[1];
		// Formale Prüfung der Oberflächendarstellung
		const matcher: RegExp = EWAzProperties.getMuster(bundeslandNr);
		if (!matcher.test(ewAzDisplayValue)) {
			return customFieldTypeConversionResult(
				ewAzDisplayValueWithLaenderkuerzel,
				EWAzVerifierImpl._CONVERSION_ERROR_IDENTIFIER
			);
		}
		const finanzamtNrAnzStellen: number = EWAzProperties.getFANrStellen(bundeslandNr);
		const formattedString: string = ewAzDisplayValue.replace(/[ \\/\\.-]/g, "");
		const finanzamtNr: string = formattedString.substring(0, finanzamtNrAnzStellen);
		const nummer: string = formattedString.substring(finanzamtNrAnzStellen);

		const bufaNr: string = this._getBundesfinanzamtsnummer(
			bundeslandNr,
			finanzamtNr,
			finanzamtNrAnzStellen
		);
		const bundeslandNrStr: string = bundeslandNr.toString()
		if (!bufaNr.startsWith(bundeslandNrStr)) {
			return customFieldTypeConversionResult(
				ewAzDisplayValueWithLaenderkuerzel,
				EWAzVerifierImpl._CONVERSION_ERROR_IDENTIFIER
			);
		}
		const formatschluesselStr: string =
			EWAzProperties.getFormatschluessel(bundeslandNr).toString();
		return customFieldTypeConversionResult(bufaNr + formatschluesselStr + nummer);
	}

	private _getBundesfinanzamtsnummer(
		bundeslandNr: number,
		finanzamtNr: string,
		finanzamtNrAnzStellen: number
	): string {
		const strBulaNr: string = bundeslandNr.toString();
		return (
			strBulaNr.substring(0, EWAzVerifierImpl._STELLEN_BUFANR - finanzamtNrAnzStellen) +
			finanzamtNr
		);
	}

	private _getBuLaNrFromString(ewaz: string): number {
		if (ewaz.length !== 0 && (ewaz.charAt(0) === "5" || ewaz.charAt(0) === "9")) {
			return Number(ewaz.substring(0, 1));
		} else {
			return Number(ewaz.substring(0, 2));
		}
	}
}
