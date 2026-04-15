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

import { CustomFieldTypeValidationParamImpl } from "../CustomFieldTypeValidationParamImpl.js";
import { DefdataStnrAccessHelper } from "../defdatastnr/DefdataStnrAccessHelper.js";
import type { IGermanTaxNumberCheckDigitDefinition } from "../defdatastnr/IGermanTaxNumberCheckDigitDefinition.js";
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import type { IGermanTaxNumberDefiningData } from "../defdatastnr/IGermanTaxNumberDefiningData.js";
import type { IGermanTaxNumberFormatting } from "../defdatastnr/IGermanTaxNumberFormatting.js";
import { CustomFieldTypeErrorEnum } from "../error/CustomFieldTypeErrorEnum.js";
import { CustomFieldTypeErrorKeys } from "../error/CustomFieldTypeErrorKeys.js";
import { CustomFieldTypeErrorUtil } from "../error/CustomFieldTypeErrorUtil.js";
import { PruefzifferVerfahren } from "../utils/PruefzifferVerfahren.js";

import { GermanTaxNumberDefiningData } from "../defdatastnr/GermanTaxNumberDefiningData.js";

import { AbstractFadatRelatedCustomFieldType } from "./AbstractFadatRelatedCustomFieldType.js";

/**
 * Checks the tax number.
 * The check of the tax number in Elster format consists of the following parts:
 * <ul>
 * <li>Is the tax number formally valid?</li>
 * <li>Does the federal tax office number contained in the tax number exist?</li>
 * <li>Is the district number contained in the tax number unequal to 0?</li>
 * <li>Is no state-specific negative pattern met?</li>
 * <li>Is the district number unequal to 0000, 0998, 0999 for NRW or not equal to 000, 998, 999 otherwise
 * (was implemented as a state-specific negative pattern)?</li>
 * <li>Does the check digit calculation procedure provide the appropriate check digit?</li>
 * </ul>
 *
 * <p>
 * In the formal check, the tax number must match the following regular expression:
 * <code>[0-9]{4}0[0-9]{8}</code>
 * </p>
 *
 * <p>
 * The first four characters of the tax number form the federal tax office number.
 * </p>
 *
 * <p>
 * The district number begins with the sixth digit of the tax number.
 * The number of digits in the district number is country-specific and can be determined using the
 * {@link IGermanTaxNumberFormatting.getNumPositionsDistrict} method.
 * An implementation of the interface {@link IGermanTaxNumberFormatting} can be obtained via the method
 * {@link IGermanTaxNumberDefiningData.getFormatting} for the federal state number.
 * The first two digits of the tax number form the so-called federal state number.
 * </p>
 *
 * <p>
 * It is then checked that no country-specific negative pattern is met.
 * The {@link IGermanTaxNumberDefiningData.getNegativePattern} method returns a vector of pairs consisting of a negative pattern
 * and an error message for the federal state number.
 * </p>
 *
 * <p>
 * Finally, the tax number is checked using a check digit calculation method.
 * </p>
 */
export class SteuernummerVerifierImpl extends AbstractFadatRelatedCustomFieldType implements ICustomFieldValidator {
	// formal check: number with 13 digits, where the fifth one should be 0
	private static readonly _VALID_STNR = /^([0-9]{2})([0-9]{2})0[0-9]{8}$/;
	private static readonly _STELLEN_BUFANR = 4;
	private static readonly _TRENNZEICHEN_STNR = "@";
	/*
	 * Key kann hier nicht die Bundeslandnummer sein (obwohl das Pattern bundeslandspezifisch ist),
	 * denn die Daten, die das Pattern bestimmen, werden in der Schnittstelle
	 * {@link IDefdatenSteuernummer} übergeben. Diese Daten sind im Portal reloadable, können sich
	 * somit zur Laufzeit ändern.
	 */
	private static readonly _mapPatternForOflToXml = new Map<string, RegExp>();

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static readonly _CONVERSION_ERROR_IDENTIFIER = "CONVERSION_ERROR";

	public constructor(fadat: IFadat | undefined) {
		super(fadat);
	}

	/**
	 * Checks the transferred tax number.<br>
	 * The defining data of the tax number check and a boolean are required as additional parameters.
	 * The value of the Boolean parameter determines whether the value must be converted to XML format before validation.
	 * If the parameters are not passed or wrong parameters are passed, a {@link !Error Error} is thrown.
	 */
	validate(
		value: string,
		valParam: ICustomFieldTypeValidationParam,
		isDisplayValue: boolean
	): ICustomFieldTypeCheckError | undefined {
		if (isDisplayValue) {
			// convert the value to the internal format
			const _convertResult = this.convertDisplay2Internal(value);

			if (_convertResult.getErrorMessage()) {
				// error while converting
				return CustomFieldTypeErrorUtil.getResult(
					CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER,
					CustomFieldTypeErrorKeys.UNGUELTIGE_STNR
				);
			}
			// now we set the intern format
			value = _convertResult.getConvertedValue();
		}
		const _error = this._checkSteuernummer(value, true);
		return CustomFieldTypeErrorUtil.getResult(
			_error,
			CustomFieldTypeErrorKeys.UNGUELTIGE_STNR
		);
	}

	/**
	 * Prüft die angegebene Steuernummer und liefert den Key der passenden Fehlermeldung
	 * bzw. <code>null</code>, wenn die Steuernummer gültig ist.
	 */
	private _checkSteuernummer(
		steuernummer: string,
		checkBundesfinanzamtsnummer: boolean
	): CustomFieldTypeErrorEnum {
		if (!SteuernummerVerifierImpl._VALID_STNR.test(steuernummer)) {
			return CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER;
		}
		const _matcher = this.getMatcher(steuernummer);
		if (_matcher == undefined) {
			return CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER;
		}
		const _bundeslandnummerStr = _matcher[1];
		const _bundesfinanzamtsnummer = Number(_bundeslandnummerStr.concat(_matcher[2]));
		if (
			checkBundesfinanzamtsnummer &&
			!this.istBundesfinanzamtsnummerGueltig(_bundesfinanzamtsnummer)
		) {
			return CustomFieldTypeErrorEnum.STNR_FINANZAMT_FEHLER;
		}
		// split Steuernummer
		const _bundeslandnummer = Number(_bundeslandnummerStr);
		const _idxBezirkEnd = DefdataStnrAccessHelper.getIdxBezirkEnd(GermanTaxNumberDefiningData.instance, _bundeslandnummer);
		const _bezirksnummer = Number(
			steuernummer.substring(DefdataStnrAccessHelper.BEZIRKSNUMMER_BEGIN_IDX, _idxBezirkEnd)
		);
		if (_bezirksnummer === 0) {
			return CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER_BEZIRK;
		}
		// after the formal check, we know that the Steuernummer has 13 digits
		const _pruefziffer = Number(steuernummer.substring(12));
		if (this.anyNegativePatternValid(steuernummer, _bundeslandnummer)) {
			return CustomFieldTypeErrorEnum.STNR_FORMALER_NEGATIVMUSTER;
		}
		const _verfahrenDef = DefdataStnrAccessHelper.getPruefziffernverfahrenDefinition(
			GermanTaxNumberDefiningData.instance,
			_bundeslandnummer,
			_bundesfinanzamtsnummer,
			_bezirksnummer
		);
		if (!this.istPruefzifferRichtig(steuernummer, _pruefziffer, _verfahrenDef)) {
			return CustomFieldTypeErrorEnum.STNR_PRUEFZIFFER_FEHLER;
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Prüft die angegebene EWAz (eine Steuernummer für die Nordländer SH, HB, HH und BE) und
	 * liefert den Key der passenden Fehlermeldung bzw. <code>null</code>, wenn die Steuernummer
	 * gültig ist.
	 * Die Bundesfinanzamtsnummer im Einheitswert-Aktenzeichens wurde bereits geprüft. Im
	 * technischen Anforderungskonzept (V35, Kapitel 1.2.2) steht eine verbindliche Liste der
	 * erlaubten BUFA-Nummern.
	 */
	private _checkEWAzAlsSteuernummer(steuernummer: string): CustomFieldTypeErrorEnum {
		return this._checkSteuernummer(steuernummer, false);
	}

	istEWAzAlsSteuernummerGueltig(steuernummer: string): boolean {
		return this._checkEWAzAlsSteuernummer(steuernummer) !== CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	getMatcher(steuernummer: string): RegExpExecArray {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return SteuernummerVerifierImpl._VALID_STNR.exec(steuernummer)!;
	}

	istBundesfinanzamtsnummerGueltig(bundesfinanzamtsnummer: number): boolean {
		return this.existsFederalTaxOfficeNumber(bundesfinanzamtsnummer);
	}

	/**
	 * Checks whether the tax number matches negative patterns.
	 *
	 * @return <code>false</code> if no negative pattern matches, otherwise <code>true</code>
	 */
	anyNegativePatternValid(
		steuernummer: string,
		bundeslandnummer: number,
	): boolean {
		const _negativMusterSet = GermanTaxNumberDefiningData.instance.getNegativePattern(bundeslandnummer);
		if (_negativMusterSet != null) {
			for (const _negativMuster of _negativMusterSet) {
				const _p = new RegExp(_negativMuster);
				if (_p.test(steuernummer)) {
					return true;
				}
			}
		}
		return false;
	}

	istPruefzifferRichtig(
		steuernummer: string,
		pruefziffer: number,
		verfahrenDef: IGermanTaxNumberCheckDigitDefinition
	): boolean {
		const _factors = DefdataStnrAccessHelper.getFaktoren(verfahrenDef);
		const _description = verfahrenDef.getDescription();
		if ("11er-Verfahren" === _description) {
			return PruefzifferVerfahren.pruefeMitElferVerfahren(
				steuernummer,
				pruefziffer,
				_factors
			);
		} else if ("2er-Verfahren" === _description) {
			const _summands = DefdataStnrAccessHelper.getSummanden(verfahrenDef);
			return PruefzifferVerfahren.pruefeMitZweierVerfahren(
				steuernummer,
				pruefziffer,
				_factors,
				_summands
			);
		} else if ("modifiziertes 11er-Verfahren" === _description) {
			return this.pruefeMitModElferVerfahren(steuernummer, pruefziffer, _factors);
		} else if ("11er-Verfahren-NRW" === _description) {
			return this._pruefeMitElferVerfahrenNRW(steuernummer, pruefziffer, _factors);
		} else {
			// integration error
			throw new Error(
				"Fehler in den defininierenden Daten. " +
				'Es wird kein Prüfziffernverfahren mit der Bezeichnung "' +
				_description +
				'" unterstützt.'
			);
		}
	}

	private _pruefeMitElferVerfahrenNRW(
		steuernummer: string,
		pruefziffer: number,
		factors: number[]
	): boolean {
		let _sum = 0;
		for (let i = 0; i < factors.length; i++) {
			_sum += factors[i] * Number(steuernummer.charAt(i));
		}
		const _lower = Math.floor(_sum / 11) * 11;
		return _sum - _lower === pruefziffer;
	}

	pruefeMitModElferVerfahren(
		steuernummer: string,
		pruefziffer: number,
		factors: number[]
	): boolean {
		let _sum = 0;
		for (let i = 0; i < factors.length; i++) {
			const _product = factors[i] * Number(steuernummer.charAt(i));
			const _s = _product.toString();
			let _productEinstellig = 0;
			if (_s.length === 1) {
				_productEinstellig = _product;
			} else if (_s.length === 2) {
				_productEinstellig = Number(_s.substring(_s.length - 1)) + 1;
			} else {
				// integration error
				throw new Error(
					"Fehler in den defininierenden Daten. " +
					"Im modifizierten 11er Verfahren für die Steuernummer " +
					steuernummer +
					" hat das Produkt aus der " +
					i +
					".ten Stelle der Steuernummer " +
					"und dem Faktor " +
					factors[i] +
					" nicht wie erwartet ein oder zwei Stellen."
				);
			}
			if (_productEinstellig.toString().length !== 1) {
				// integration error
				throw new Error(
					"Fehler in den defininierenden Daten. " +
					"Im modifizierten 11er Verfahren für die Steuernummer " +
					steuernummer +
					' ist das einstellige Produkt "' +
					_productEinstellig +
					'" aus der ' +
					i +
					".ten Stelle der Steuernummer und dem Faktor " +
					factors[i] +
					" nicht - wie erwartet - einstellig."
				);
			}
			_sum += _productEinstellig;
		}
		const _upper = Math.ceil(_sum / 10) * 10;
		return _upper - _sum === pruefziffer;
	}

	/**
	 * Converts the tax number in XML format (equivalent to the ELSTER tax number format) to the display representation.
	 *
	 * @return Tax number in display representation
	 */
	convertInternal2Display(internalValue: string): ICustomFieldTypeConversionResult {
		const valParam = new CustomFieldTypeValidationParamImpl(
			CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH,
			CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH
		);
		const _checkResult = this.validate(internalValue, valParam, false);
		if (_checkResult) {
			return customFieldTypeConversionResult(
				internalValue,
				"Konvertiere nur gültige Steuernummern. " + _checkResult.getErrorMessage()
			);
		}

		const _matcher = this.getMatcher(internalValue);
		const _bundeslandNr = Number(_matcher[1]);
		const _formatting = GermanTaxNumberDefiningData.instance.getFormatting(_bundeslandNr);

		const _idxStartBezirk = SteuernummerVerifierImpl._STELLEN_BUFANR + 1;
		const _idxEndBezirk = _idxStartBezirk + _formatting.getNumPositionsDistrict();
		const _idxStartUNr = _idxEndBezirk;
		const _idxEndUNr = _idxStartUNr + _formatting.getNumPositionsDifferentiationNumber();
		const _idxStartPruefziffer = _idxEndUNr;
		return customFieldTypeConversionResult(
			_formatting.getPrefix() +
			internalValue.substring(
				SteuernummerVerifierImpl._STELLEN_BUFANR - _formatting.getNumPositionsTaxOffice(),
				SteuernummerVerifierImpl._STELLEN_BUFANR
			) + // Finanzamtsnummer
			_formatting.getDelimiter1() +
			internalValue.substring(_idxStartBezirk, _idxEndBezirk) + // Bezirksnummer
			_formatting.getDelimiter2() +
			internalValue.substring(_idxStartUNr, _idxEndUNr) + // Unterscheidungsnummer
			_formatting.getDelimiter3() +
			internalValue.substring(_idxStartPruefziffer) + // Prüfziffer
			SteuernummerVerifierImpl._TRENNZEICHEN_STNR +
			_bundeslandNr,
			undefined
		);
	}

	convertDisplay2Internal(displayValue: string): ICustomFieldTypeConversionResult {
		// Bundeslandnummer bestimmen
		if (displayValue.indexOf(SteuernummerVerifierImpl._TRENNZEICHEN_STNR) === -1) {
			throw new Error(
				'Der übergebene String "' +
				displayValue +
				'" muss neben der Steuernummer in Oberflächendarstellung, ' +
				"getrennt durch das Zeichen '@', auch die Bundeslandnummer enthalten."
			);
		}
		const _idxTrz = displayValue.indexOf(SteuernummerVerifierImpl._TRENNZEICHEN_STNR);
		if (_idxTrz + 1 >= displayValue.length) {
			return customFieldTypeConversionResult(
				displayValue,
				SteuernummerVerifierImpl._CONVERSION_ERROR_IDENTIFIER
			);
		}
		const _bundeslandStr = displayValue.substring(_idxTrz + 1);
		const _bundeslandNr = Number(_bundeslandStr);
		if (isNaN(_bundeslandNr)) {
			return customFieldTypeConversionResult(
				displayValue,
				SteuernummerVerifierImpl._CONVERSION_ERROR_IDENTIFIER
			);
		}
		// Infos zur bundeslandspezifischen Formatierung bestimmen
		let _formatting: IGermanTaxNumberFormatting;
		try {
			_formatting = GermanTaxNumberDefiningData.instance.getFormatting(_bundeslandNr);
		} catch (_e) {
			return customFieldTypeConversionResult(
				displayValue,
				SteuernummerVerifierImpl._CONVERSION_ERROR_IDENTIFIER
			);
		}
		// Formale Prüfung der Oberflächendarstellung
		const _validOflFormat = this._getPatternToConvertOflToXml(_formatting);
		const _matcher = _validOflFormat.exec(displayValue);
		if (!_matcher) {
			return customFieldTypeConversionResult(
				displayValue,
				SteuernummerVerifierImpl._CONVERSION_ERROR_IDENTIFIER
			);
		}

		const _finanzamt = _matcher[1];
		const _bezirk = _matcher[2];
		const _unterscheidungsnr = _matcher[3];
		const _pruefziffer = _matcher[4];

		const _bufaNr = this._getBundesfinanzamtsnummer(
			_bundeslandNr,
			_finanzamt,
			_formatting.getNumPositionsTaxOffice()
		);
		if (_bufaNr.length < 2 || _bufaNr.substring(0, 2) !== _bundeslandStr) {
			return customFieldTypeConversionResult(
				displayValue,
				SteuernummerVerifierImpl._CONVERSION_ERROR_IDENTIFIER
			);
		}
		return customFieldTypeConversionResult(
			_bufaNr + "0" + _bezirk + _unterscheidungsnr + _pruefziffer,
			undefined
		);
	}

	private _getPatternToConvertOflToXml(formatting: IGermanTaxNumberFormatting): RegExp {
		// Der Prefix sollte immer eine konkrete Ziffenrfolge sein und kann damit 1:1 eingefügt werden.
		const _muster =
			formatting.getPrefix() +
			"([0-9]{" +
			formatting.getNumPositionsTaxOffice() +
			"})" +
			formatting.getDelimiter1() +
			"([0-9]{" +
			formatting.getNumPositionsDistrict() +
			"})" +
			formatting.getDelimiter2() +
			"([0-9]{" +
			formatting.getNumPositionsDifferentiationNumber() +
			"})" +
			formatting.getDelimiter3() +
			"([0-9]{1})@([0-9]{2})";
		this.createPatternToConvertOflToXml(_muster);
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return SteuernummerVerifierImpl._mapPatternForOflToXml.get(_muster)!;
	}

	createPatternToConvertOflToXml(muster: string): void {
		if (!SteuernummerVerifierImpl._mapPatternForOflToXml.has(muster)) {
			const _validOflFormat = new RegExp(muster);
			SteuernummerVerifierImpl._mapPatternForOflToXml.set(muster, _validOflFormat);
		}
	}

	private _getBundesfinanzamtsnummer(
		bundeslandnr: number,
		finanzamtsnr: string,
		stellenFinanzamt: number
	): string {
		const _strBulaNr = String(bundeslandnr);
		return (
			_strBulaNr.substring(0, SteuernummerVerifierImpl._STELLEN_BUFANR - stellenFinanzamt) +
			finanzamtsnr
		);
	}
}
