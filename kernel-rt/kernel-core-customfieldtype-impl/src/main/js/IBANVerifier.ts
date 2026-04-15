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
import type { ICustomFieldTypeCheckError, ICustomFieldTypeConversionResult, ICustomFieldTypeValidationParam } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { customFieldTypeConversionResult, type ICustomFieldValidator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import { CustomFieldTypeValidationParamImpl } from "./internal/CustomFieldTypeValidationParamImpl.js";
import { CustomFieldTypeErrorEnum } from "./internal/error/CustomFieldTypeErrorEnum.js";
import { CustomFieldTypeErrorKeys } from "./internal/error/CustomFieldTypeErrorKeys.js";
import { CustomFieldTypeErrorUtil } from "./internal/error/CustomFieldTypeErrorUtil.js";

/**
 * This class is used to check the IBAN (International Bank Account Number).
 * The IBAN check consists of four parts:
 * <ul>
 * <li>Is the IBAN formally valid?</li>
 * <li>Is the country code valid?</li>
 * <li>Is the country format valid?</li>
 * <li>Is the check digit valid?</li>
 * </ul>
 *
 * <p>
 * The IBAN must conform to the following format: Two capital letters for the country code, two
 * check digits, Basic BAN with 1-30 digits/letters.
 * This check is done with the following regular expression:
 * <code>([a-zA-Z]{2})[0-9]{2}[0-9a-zA-Z]{1,30}</code>
 * </p>
 *
 * <p>
 * If the IBAN does not match the pattern defined by the regular expression, this is a formal error.
 * </p>
 *
 * <p>
 * The country code is extracted from the IBAN using the regular expression and then it is checked
 * whether it is contained in the IBAN country code list.
 * </p>
 *
 * <p>
 * A regular expression is specified for each country in the IBAN country code list,
 * which is used to check the country format.
 * </p>
 *
 * <p>
 * When checking the check digit, the first four characters are first moved to the end of the IBAN.
 * A conversion then takes place: using a conversion table, each digit of the IBAN is converted into a number.
 * The number obtained is divided by the check digit 97 and it is checked whether the remainder of this division
 * corresponds to the expected remainder of 1.
 * </p>
 */
export class IBANVerifier implements ICustomFieldValidator {
	private static readonly _INSTANCE: IBANVerifier = new IBANVerifier();
	private static readonly _SCHIEBE_POSITION: number = 4;
	private static readonly _PRUEFZIFFERN_DIVISOR: number = 97;
	private static readonly _ERWARTETER_REST: number = 1;

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static readonly _CONVERSION_ERROR_IDENTIFIER: string = "CONVERSION_ERROR";

	// Formale Prüfung: Zwei Grossbuchstaben für Laenderkennung, zwei
	// Prüfziffern, Basic BAN mit 1-30 Ziffern/Buchstaben.
	private static readonly _VALID_IBAN: RegExp = /^(?:([a-zA-Z]{2})[0-9]{2}[0-9a-zA-Z]{1,30})$/;

	/**
	 * Liste der gültigen IBAN-Länderkennungen. Die BIC-Liste enthält derzeit
	 * vier Einträge weniger, als die IBAN-Liste. Da die Listen laut
	 * Spezifikation nicht identisch sein müssen und zudem sehr klein sind
	 * werden sie getrennt gepflegt. Die auskommentierten Länder gehören für die
	 * IBAN-Prüfung zu Frankreich: GF, GP, MQ, RE.
	 */
	private static readonly _LAENDER_X_PATTERN: Map<string, RegExp> = new Map<string, RegExp>();

	/**
	 * Don't use this. This is just for a static initialization.
	 */
	static _constructor = (() => {
		IBANVerifier._LAENDER_X_PATTERN.set("AT", new RegExp("^(?:AT[0-9]{2}[0-9]{5}[0-9]{11})$")); // Österreich
		IBANVerifier._LAENDER_X_PATTERN.set(
			"BE",
			new RegExp("^(?:BE[0-9]{2}[0-9]{3}[0-9]{7}[0-9]{2})$")
		); // Belgien
		IBANVerifier._LAENDER_X_PATTERN.set(
			"BG",
			new RegExp("^(?:BG[0-9]{2}[A-Z]{4}[0-9]{4}[0-9]{2}[a-zA-Z0-9]{8})$")
		); // Bulgarien
		IBANVerifier._LAENDER_X_PATTERN.set(
			"CH",
			new RegExp("^(?:CH[0-9]{2}[0-9]{5}[a-zA-Z0-9]{12})$")
		); // Schweiz
		IBANVerifier._LAENDER_X_PATTERN.set(
			"CY",
			new RegExp("^(?:CY[0-9]{2}[0-9]{3}[0-9]{5}[a-zA-Z0-9]{16})$")
		); // Zypern
		IBANVerifier._LAENDER_X_PATTERN.set(
			"CZ",
			new RegExp("^(?:CZ[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{10})$")
		); // Tschechien
		IBANVerifier._LAENDER_X_PATTERN.set("DE", new RegExp("^(?:DE[0-9]{2}[0-9]{8}[0-9]{10})$")); // Deutschland
		IBANVerifier._LAENDER_X_PATTERN.set(
			"DK",
			new RegExp("^(?:DK[0-9]{2}[0-9]{4}[0-9]{9}[0-9]{1})$")
		); // Dänemark
		IBANVerifier._LAENDER_X_PATTERN.set(
			"EE",
			new RegExp("^(?:EE[0-9]{2}[0-9]{2}[0-9]{2}[0-9]{11}[0-9]{1})$")
		); // Estland
		IBANVerifier._LAENDER_X_PATTERN.set(
			"ES",
			new RegExp("^(?:ES[0-9]{2}[0-9]{4}[0-9]{4}[0-9]{1}[0-9]{1}[0-9]{10})$")
		); // Spanien
		IBANVerifier._LAENDER_X_PATTERN.set(
			"FI",
			new RegExp("^(?:FI[0-9]{2}[0-9]{6}[0-9]{7}[0-9]{1})$")
		); // Finnland
		IBANVerifier._LAENDER_X_PATTERN.set(
			"FR",
			new RegExp("^(?:FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2})$")
		); // Frankreich
		IBANVerifier._LAENDER_X_PATTERN.set(
			"GB",
			new RegExp("^(?:GB[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8})$")
		); // Great Britain
		// IBANVerifier._LAENDER_X_PATTERN.set(
		// "GF",
		// new RegExp("^(?:FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2})$")
		// ); // Französisch Guyana
		IBANVerifier._LAENDER_X_PATTERN.set(
			"GI",
			new RegExp("^(?:GI[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{15})$")
		); // Gibraltar
		// IBANVerifier._LAENDER_X_PATTERN.set(
		// "GP",
		// new RegExp("^(?:FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2})$")
		// ); // Guadeloupe
		IBANVerifier._LAENDER_X_PATTERN.set(
			"GR",
			new RegExp("^(?:GR[0-9]{2}[0-9]{3}[0-9]{4}[a-zA-Z0-9]{16})$")
		); // Griechenland
		IBANVerifier._LAENDER_X_PATTERN.set(
			"HU",
			new RegExp("^(?:HU[0-9]{2}[0-9]{3}[0-9]{4}[0-9]{1}[0-9]{15}[0-9]{1})$")
		); // Ungarn
		IBANVerifier._LAENDER_X_PATTERN.set(
			"IE",
			new RegExp("^(?:IE[0-9]{2}[A-Z]{4}[0-9]{6}[0-9]{8})$")
		); // Irland
		IBANVerifier._LAENDER_X_PATTERN.set(
			"IS",
			new RegExp("^(?:IS[0-9]{2}[0-9]{4}[0-9]{2}[0-9]{6}[0-9]{10})$")
		); // Island
		IBANVerifier._LAENDER_X_PATTERN.set(
			"IT",
			new RegExp("^(?:IT[0-9]{2}[A-Z]{1}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{12})$")
		); // Italien
		IBANVerifier._LAENDER_X_PATTERN.set(
			"LI",
			new RegExp("^(?:LI[0-9]{2}[0-9]{5}[a-zA-Z0-9]{12})$")
		); // Liechtenstein
		IBANVerifier._LAENDER_X_PATTERN.set("LT", new RegExp("^(?:LT[0-9]{2}[0-9]{5}[0-9]{11})$")); // Litauen
		IBANVerifier._LAENDER_X_PATTERN.set(
			"LU",
			new RegExp("^(?:LU[0-9]{2}[0-9]{3}[a-zA-Z0-9]{13})$")
		); // Luxemburg
		IBANVerifier._LAENDER_X_PATTERN.set(
			"LV",
			new RegExp("^(?:LV[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{13})$")
		); // Lettland
		// IBANVerifier._LAENDER_X_PATTERN.set(
		// "MQ",
		// new RegExp("^(?:FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2})$")
		// ); // Martinique
		IBANVerifier._LAENDER_X_PATTERN.set(
			"MT",
			new RegExp("^(?:MT[0-9]{2}[A-Z]{4}[0-9]{5}[a-zA-Z0-9]{18})$")
		); // Malta
		IBANVerifier._LAENDER_X_PATTERN.set("NL", new RegExp("^(?:NL[0-9]{2}[A-Z]{4}[0-9]{10})$")); // Niederlande
		IBANVerifier._LAENDER_X_PATTERN.set(
			"NO",
			new RegExp("^(?:NO[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{1})$")
		); // Norwegen
		IBANVerifier._LAENDER_X_PATTERN.set("PL", new RegExp("^(?:PL[0-9]{2}[0-9]{8}[0-9]{16})$")); // Polen
		IBANVerifier._LAENDER_X_PATTERN.set(
			"PT",
			new RegExp("^(?:PT[0-9]{2}[0-9]{4}[0-9]{4}[0-9]{11}[0-9]{2})$")
		); // Portugal
		// IBANVerifier._LAENDER_X_PATTERN.set(
		// "RE",
		// new RegExp("^(?:FR[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2})$")
		// ); // Réunion
		IBANVerifier._LAENDER_X_PATTERN.set(
			"RO",
			new RegExp("^(?:RO[0-9]{2}[A-Z]{4}[a-zA-Z0-9]{16})$")
		); // Rumänien
		IBANVerifier._LAENDER_X_PATTERN.set(
			"SE",
			new RegExp("^(?:SE[0-9]{2}[0-9]{3}[0-9]{16}[0-9]{1})$")
		); // Schweden
		IBANVerifier._LAENDER_X_PATTERN.set(
			"SI",
			new RegExp("^(?:SI[0-9]{2}[0-9]{5}[0-9]{8}[0-9]{2})$")
		); // Slowenien
		IBANVerifier._LAENDER_X_PATTERN.set(
			"SK",
			new RegExp("^(?:SK[0-9]{2}[0-9]{4}[0-9]{6}[0-9]{10})$")
		); // Slowakei
		IBANVerifier._LAENDER_X_PATTERN.set(
			"MC",
			new RegExp("^(?:MC[0-9]{2}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{11}[0-9]{2})$")
		); // Monaco
		IBANVerifier._LAENDER_X_PATTERN.set("HR", new RegExp("^(?:HR[0-9]{2}[0-9]{7}[0-9]{10})$")); // Kroatien
		IBANVerifier._LAENDER_X_PATTERN.set(
			"SM",
			new RegExp("^(?:SM[0-9]{2}[A-Z]{1}[0-9]{5}[0-9]{5}[a-zA-Z0-9]{12})$")
		); // San Marino
		IBANVerifier._LAENDER_X_PATTERN.set(
			"AD",
			new RegExp("^(?:AD[0-9]{2}[0-9]{4}[0-9]{4}[a-zA-Z0-9]{12})$")
		); // Andorra
		IBANVerifier._LAENDER_X_PATTERN.set("VA", new RegExp("^(?:VA[0-9]{2}[0-9]{3}[0-9]{15})$")); // Vatikanstadt
		IBANVerifier._LAENDER_X_PATTERN.set("AL", new RegExp("^(?:AL[0-9]{2}[0-9]{8}[a-zA-Z0-9]{16})$")); // Albanien
		IBANVerifier._LAENDER_X_PATTERN.set("MD", new RegExp("^(?:MD[0-9]{2}[a-zA-Z0-9]{2}[a-zA-Z0-9]{18})$")); // Moldawien
		IBANVerifier._LAENDER_X_PATTERN.set("ME", new RegExp("^(?:ME[0-9]{2}[0-9]{3}[0-9]{13}[0-9]{2})$")); // Montenegro
		IBANVerifier._LAENDER_X_PATTERN.set("MK", new RegExp("^(?:MK[0-9]{2}[0-9]{3}[a-zA-Z0-9]{10}[0-9]{2})$")); // Nordmazedonien
	})();

	private constructor() {
		// at the moment, we don't need to do something
	}

	public static getInstance(): IBANVerifier {
		return IBANVerifier._INSTANCE;
	}

	validate(
		iban: string,
		valParam: ICustomFieldTypeValidationParam,
		isDisplayValue: boolean
	): ICustomFieldTypeCheckError | undefined {
		if (isDisplayValue) {//der Wert muss vor der Validierung in XML-Format konvertiert werden
			const convertResult: ICustomFieldTypeConversionResult = this.convertDisplay2Internal(iban);

			if (convertResult.getErrorMessage()) {//Fehler bei der Konvertierung
				return CustomFieldTypeErrorUtil.getResult(CustomFieldTypeErrorEnum.IBAN_LEERZEICHEN_UNGUELTIG, CustomFieldTypeErrorKeys.UNGUELTIGE_IBAN);
			}
			//nehmen wir die XML-Darstellung
			iban = convertResult.getConvertedValue();
		}
		const _error = this._checkIban(iban);
		return CustomFieldTypeErrorUtil.getResult(
			_error,
			CustomFieldTypeErrorKeys.UNGUELTIGE_IBAN
		);
	}

	/**
	 * Prüft die angegebene IBAN und liefert den Key der passenden Fehlermeldung
	 * bzw. <code>undefined</code>, wenn die IBAN gültig ist.
	 */
	private _checkIban(iban: string): CustomFieldTypeErrorEnum {
		const match: boolean = IBANVerifier._VALID_IBAN.test(iban);
		if (!match) {
			return CustomFieldTypeErrorEnum.IBAN_FORMALER_FEHLER;
		}
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const arr: RegExpExecArray = IBANVerifier._VALID_IBAN.exec(iban)!;
		const laenderkennung: string = arr[1];
		if (!this._istLaenderkennungGueltig(laenderkennung)) {
			return CustomFieldTypeErrorEnum.IBAN_LAENDERCODE_FEHLER;
		}

		if (!this._istLandesformatGueltig(laenderkennung, iban)) {
			return CustomFieldTypeErrorEnum.IBAN_LANDESFORMAT_FEHLER;
		}

		if (!this._istDeutschesFormatGueltig(iban)) {
			return CustomFieldTypeErrorEnum.IBAN_LANDESFORMAT_FEHLER;
		}

		if (!this._istPruefzifferGueltig(iban)) {
			return CustomFieldTypeErrorEnum.IBAN_PRUEFZIFFER_FEHLER;
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Ist die Länderkennung für IBAN gültig?
	 */
	private _istLaenderkennungGueltig(laenderkennung: string): boolean {
		return IBANVerifier._LAENDER_X_PATTERN.has(laenderkennung);
	}

	/**
	 * Hat die IBAN ein für das Land gültiges Format?
	 */
	private _istLandesformatGueltig(laenderkennung: string, iban: string): boolean {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const landesformatPattern: RegExp = IBANVerifier._LAENDER_X_PATTERN.get(laenderkennung)!;
		return landesformatPattern.test(iban);
	}

	/**
	 * Hat die IBAN ein für Deutschland gültiges Format? Eine deutsche IBAN, die in den Stellen 13 bis
	 * 22 nur aus der Ziffer „0“ besteht, soll als unzulässig abgewiesen werden.
	 */
	private _istDeutschesFormatGueltig(iban: string): boolean {
		const endziffern: string = iban.substring(12, 22);
		return endziffern !== "0000000000";
	}

	/**
	 * @brief Ist die Prüfziffer der IBAN gültig?<br>
	 * Pruefziffernberechnung gemäß "IBAN-Prüfziffernberechnung", 06/03, Swiss Interbank Clearing AG
	 */
	private _istPruefzifferGueltig(iban: string): boolean {
		// 1. Schritt: Schiebe die ersten vier Zeichen nach hinten.
		// 2. Schritt: Konvertiere A-Z in Ziffern.
		let convertedIBAN = "";
		const ibanLength: number = iban.length;
		try {
			for (let i: number = IBANVerifier._SCHIEBE_POSITION; i < ibanLength; i++) {
				convertedIBAN += this._pruefziffernKonversionstabelle(iban.charAt(i));
			}
			for (let i = 0; i < Math.min(IBANVerifier._SCHIEBE_POSITION, ibanLength); i++) {
				convertedIBAN += this._pruefziffernKonversionstabelle(iban.charAt(i));
			}
		} catch (_e) {
			return false;
		}
		// 3. Schritt: Division.
		const rest: number = this._pruefziffernDivision(convertedIBAN);
		if (IBANVerifier._ERWARTETER_REST !== rest) {
			return false;
		}
		return true;
	}

	/**
	 * Konvertiere a-zA-Z in zweistellige Ziffernfolge.
	 *
	 * Durch die vorausgehende formale Prüfung ist sichergestellt, dass hier nur
	 * a-zA-Z0-9 als Parameter buchstabe auftreten.
	 */
	private _pruefziffernKonversionstabelle(buchstabe: string): string {
		switch (buchstabe) {
			case "0":
				return "0";
			case "1":
				return "1";
			case "2":
				return "2";
			case "3":
				return "3";
			case "4":
				return "4";
			case "5":
				return "5";
			case "6":
				return "6";
			case "7":
				return "7";
			case "8":
				return "8";
			case "9":
				return "9";

			case "A":
				return "10";
			case "B":
				return "11";
			case "C":
				return "12";
			case "D":
				return "13";
			case "E":
				return "14";
			case "F":
				return "15";
			case "G":
				return "16";
			case "H":
				return "17";
			case "I":
				return "18";
			case "J":
				return "19";
			case "K":
				return "20";
			case "L":
				return "21";
			case "M":
				return "22";
			case "N":
				return "23";
			case "O":
				return "24";
			case "P":
				return "25";
			case "Q":
				return "26";
			case "R":
				return "27";
			case "S":
				return "28";
			case "T":
				return "29";
			case "U":
				return "30";
			case "V":
				return "31";
			case "W":
				return "32";
			case "X":
				return "33";
			case "Y":
				return "34";
			case "Z":
				return "35";

			case "a":
				return "10";
			case "b":
				return "11";
			case "c":
				return "12";
			case "d":
				return "13";
			case "e":
				return "14";
			case "f":
				return "15";
			case "g":
				return "16";
			case "h":
				return "17";
			case "i":
				return "18";
			case "j":
				return "19";
			case "k":
				return "20";
			case "l":
				return "21";
			case "m":
				return "22";
			case "n":
				return "23";
			case "o":
				return "24";
			case "p":
				return "25";
			case "q":
				return "26";
			case "r":
				return "27";
			case "s":
				return "28";
			case "t":
				return "29";
			case "u":
				return "30";
			case "v":
				return "31";
			case "w":
				return "32";
			case "x":
				return "33";
			case "y":
				return "34";
			case "z":
				return "35";
			default:
				throw new Error("The IBAN contains an invalid letter.");
		}
	}

	/**
	 * Berechne den Rest einer Division.
	 *
	 * Der Ziffernstring ist möglicherweise zu lang, um in ein int o.ä. zu
	 * passen. Deshalb wird die Division häppchenweise mit jeweils 10
	 * Zeichen durchgeführt.
	 *
	 * @return Rest der Division.
	 */
	private _pruefziffernDivision(divident: any): number {
		const partLength = 10;
		while (divident.length > partLength) {
			const part: any = divident.substring(0, partLength);
			divident = (part % IBANVerifier._PRUEFZIFFERN_DIVISOR) + divident.substring(partLength);
		}
		return divident % IBANVerifier._PRUEFZIFFERN_DIVISOR;
	}

	convertDisplay2Internal(displayValue: string): ICustomFieldTypeConversionResult {
		const xmlValue = IBANVerifier._convertDisplay2Internal(displayValue);

		if (xmlValue != null) {
			return customFieldTypeConversionResult(xmlValue, undefined);
		} else {
			return customFieldTypeConversionResult(displayValue, IBANVerifier._CONVERSION_ERROR_IDENTIFIER);
		}
	}

	/**
	 * Convert display value to internal value. Check that the blanks occur after every 4th character and remove the blanks.
	 *
	 * @return internal value or <code>null</code>, if the display value is invalid.
	 *
	 */
	private static _convertDisplay2Internal(displayValue: string): string | null {
		if (displayValue.indexOf(' ') != -1) {
			let sb = "";
			for (let i = 0; i < displayValue.length; i++) {
				// Blanks must be at 4, 9, 14, ...
				if ((i + 1) % 5 == 0) {
					if (displayValue.charAt(i) != ' ') {
						return null;
					}
				} else {
					if (displayValue.charAt(i) == ' ') {
						return null;
					}
					sb += displayValue.charAt(i);
				}
			}
			return sb.toString();
		} else {
			return displayValue;
		}
	}

	convertInternal2Display(internalValue: string): ICustomFieldTypeConversionResult {
		const valParam: ICustomFieldTypeValidationParam = new CustomFieldTypeValidationParamImpl(CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH, CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH);
		const checkResult = this.validate(internalValue, valParam, false);
		if (checkResult) {
			return customFieldTypeConversionResult(internalValue,
				"Konvertiere nur gültige IBAN. " + checkResult.getErrorMessage());
		}
		return customFieldTypeConversionResult(IBANVerifier._convertInternal2Display(internalValue), undefined);
	}

	/**
	 * Convert internal value to display value. Insert a blank after every 4 characters.
	 *
	 * @return display value
	 *
	 */
	private static _convertInternal2Display(xmlValue: string): string {
		let sb = "";
		for (let i = 0; i < xmlValue.length; i++) {
			// Insert blanks at 4, 8, 12, ...
			if (i > 0 && i % 4 == 0) {
				sb += ' ';
			}
			sb += xmlValue.charAt(i);
		}
		return sb.toString();
	}
}
