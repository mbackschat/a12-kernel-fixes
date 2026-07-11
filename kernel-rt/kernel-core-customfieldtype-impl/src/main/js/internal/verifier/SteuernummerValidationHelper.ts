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
import type { ICustomFieldTypeConversionResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { customFieldTypeConversionResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import { DefdataStnrAccessHelper } from "../defdatastnr/DefdataStnrAccessHelper.js";
import type { IGermanTaxNumberCheckDigitDefinition } from "../defdatastnr/IGermanTaxNumberCheckDigitDefinition.js";
import type { IGermanTaxNumberFormatting } from "../defdatastnr/IGermanTaxNumberFormatting.js";
import { CustomFieldTypeErrorEnum } from "../error/CustomFieldTypeErrorEnum.js";
import { PruefzifferVerfahren } from "../utils/PruefzifferVerfahren.js";

import { GermanTaxNumberDefiningData } from "../defdatastnr/GermanTaxNumberDefiningData.js";

/**
 * Shared validation and conversion logic for Steuernummer and Bundessteuernummer verification.
 * This class is parameterized by a regex pattern and does not depend on IFadat directly.
 * Instead, BUFA validation is provided as a predicate by the caller.
 */
export class SteuernummerValidationHelper {
	// Formale Prüfung: 13-stellige Zahl, wobei die 5. Stelle den Wert 0 hat.
	static readonly VALID_STNR = /^([0-9]{2})([0-9]{2})0[0-9]{8}$/;

	private static readonly _STELLEN_BUFANR = 4;
	private static readonly _TRENNZEICHEN_STNR = "@";

	/*
	 * The value of this constant is irrelevant. It's only important that the value is not empty.
	 */
	private static readonly _CONVERSION_ERROR_IDENTIFIER = "CONVERSION_ERROR";

	private readonly validPattern: RegExp;

	/*
	 * Key kann hier nicht die Bundeslandnummer sein (obwohl das Pattern bundeslandspezifisch ist),
	 * denn die Daten, die das Pattern bestimmen, werden in der Schnittstelle
	 * {@link IDefdatenSteuernummer} übergeben. Diese Daten sind im Portal reloadable, können sich
	 * somit zur Laufzeit ändern.
	 */
	private readonly mapPatternForOflToXml = new Map<string, RegExp>();

	constructor(validPattern: RegExp) {
		this.validPattern = validPattern;
	}

	// =========================== Validation ==========================

	getMatcher(xmlSteuernummer: string): RegExpExecArray | null {
		return this.validPattern.exec(xmlSteuernummer);
	}

	/**
	 * Prüft die angegebene Steuernummer und liefert den Key der passenden Fehlermeldung
	 * bzw. {@link CustomFieldTypeErrorEnum.KEIN_FEHLER}, wenn die Steuernummer gültig ist.
	 *
	 * @param steuernummer the tax number to check
	 * @param bufaValidator predicate to validate the federal tax office number
	 */
	checkSteuernummer(
		steuernummer: string,
		bufaValidator: (bufaNr: number) => boolean
	): CustomFieldTypeErrorEnum {
		const _matcher = this.getMatcher(steuernummer);
		if (_matcher == null) {
			return CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER;
		}
		const _bundeslandnummerStr = _matcher[1];
		const _bundesfinanzamtsnummer = Number(_bundeslandnummerStr.concat(_matcher[2]));
		if (!bufaValidator(_bundesfinanzamtsnummer)) {
			return CustomFieldTypeErrorEnum.STNR_FINANZAMT_FEHLER;
		}
		// Steuernummer in die verschiedenen Teile zerstückeln.
		const _bundeslandnummer = Number(_bundeslandnummerStr);
		const _idxBezirkEnd = DefdataStnrAccessHelper.getIdxBezirkEnd(GermanTaxNumberDefiningData.instance, _bundeslandnummer);
		const _bezirksnummer = Number(
			steuernummer.substring(DefdataStnrAccessHelper.BEZIRKSNUMMER_BEGIN_IDX, _idxBezirkEnd)
		);
		if (_bezirksnummer === 0) {
			return CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER_BEZIRK;
		}
		// Nach der formalen Prüfung muss die Steuernummer 13 Stellen besitzen.
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
	 * liefert den Key der passenden Fehlermeldung bzw. {@link CustomFieldTypeErrorEnum.KEIN_FEHLER},
	 * wenn die Steuernummer gültig ist.
	 * Die Bundesfinanzamtsnummer im Einheitswert-Aktenzeichens wurde bereits geprüft.
	 */
	checkEWAzAlsSteuernummer(steuernummer: string): CustomFieldTypeErrorEnum {
		return this.checkSteuernummer(steuernummer, () => true);
	}

	/**
	 * Checks whether the tax number matches negative patterns.
	 *
	 * @return {@code false} if no negative pattern matches, otherwise {@code true}
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
			return this.pruefeMitElferVerfahrenNRW(steuernummer, pruefziffer, _factors);
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
		return this.valuesAreEqual(_upper - _sum, pruefziffer);
	}

	private pruefeMitElferVerfahrenNRW(
		steuernummer: string,
		pruefziffer: number,
		factors: number[]
	): boolean {
		let _sum = 0;
		for (let i = 0; i < factors.length; i++) {
			_sum += factors[i] * Number(steuernummer.charAt(i));
		}
		const _lower = Math.floor(_sum / 11) * 11;
		return this.valuesAreEqual(_sum - _lower, pruefziffer);
	}

	private valuesAreEqual(a: number, b: number): boolean {
		return a === b;
	}

	// =========================== Conversion ==========================

	/**
	 * Konvertiert die Steuernummer im XML-Format (gleichbedeutend mit dem
	 * ELSTER-Steuernummerformat) in die Oberflächendarstellung.
	 * Assumes the input has already been validated.
	 *
	 * @return Steuernummer in Oberflächendarstellung
	 */
	convertValidInternal2Display(xmlStNr: string): ICustomFieldTypeConversionResult {
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const _matcher = this.getMatcher(xmlStNr)!;
		const _bundeslandNr = Number(_matcher[1]);
		const _formatting = GermanTaxNumberDefiningData.instance.getFormatting(_bundeslandNr);

		const _idxStartBezirk = SteuernummerValidationHelper._STELLEN_BUFANR + 1;
		const _idxEndBezirk = _idxStartBezirk + _formatting.getNumPositionsDistrict();
		const _idxStartUNr = _idxEndBezirk;
		const _idxEndUNr = _idxStartUNr + _formatting.getNumPositionsDifferentiationNumber();
		const _idxStartPruefziffer = _idxEndUNr;
		return customFieldTypeConversionResult(
			_formatting.getPrefix() +
			xmlStNr.substring(
				SteuernummerValidationHelper._STELLEN_BUFANR - _formatting.getNumPositionsTaxOffice(),
				SteuernummerValidationHelper._STELLEN_BUFANR
			) + // Finanzamtsnummer
			_formatting.getDelimiter1() +
			xmlStNr.substring(_idxStartBezirk, _idxEndBezirk) + // Bezirksnummer
			_formatting.getDelimiter2() +
			xmlStNr.substring(_idxStartUNr, _idxEndUNr) + // Unterscheidungsnummer
			_formatting.getDelimiter3() +
			xmlStNr.substring(_idxStartPruefziffer) + // Prüfziffer
			SteuernummerValidationHelper._TRENNZEICHEN_STNR +
			_bundeslandNr,
			undefined
		);
	}

	/**
	 * Konvertiert die Steuernummer in Oberflächendarstellung in das XML-Format (gleichbedeutend mit
	 * dem ELSTER-Steuernummerformat).
	 *
	 * @param oflStNrMitBuLaNr
	 *            - der String enthält neben der Steuernummer in Oberflächendarstellung
	 *            auch die Bundeslandnummer getrennt durch das Zeichen '@'.
	 */
	convertDisplay2Internal(oflStNrMitBuLaNr: string): ICustomFieldTypeConversionResult {
		// Bundeslandnummer bestimmen
		if (oflStNrMitBuLaNr.indexOf(SteuernummerValidationHelper._TRENNZEICHEN_STNR) === -1) {
			throw new Error(
				'Der übergebene String "' +
				oflStNrMitBuLaNr +
				'" muss neben der Steuernummer in Oberflächendarstellung, ' +
				"getrennt durch das Zeichen '@', auch die Bundeslandnummer enthalten."
			);
		}
		const _idxTrz = oflStNrMitBuLaNr.indexOf(SteuernummerValidationHelper._TRENNZEICHEN_STNR);
		if (_idxTrz + 1 >= oflStNrMitBuLaNr.length) {
			return customFieldTypeConversionResult(
				oflStNrMitBuLaNr,
				SteuernummerValidationHelper._CONVERSION_ERROR_IDENTIFIER
			);
		}
		const _bundeslandStr = oflStNrMitBuLaNr.substring(_idxTrz + 1);
		const _bundeslandNr = Number(_bundeslandStr);
		if (isNaN(_bundeslandNr)) {
			return customFieldTypeConversionResult(
				oflStNrMitBuLaNr,
				SteuernummerValidationHelper._CONVERSION_ERROR_IDENTIFIER
			);
		}
		// Infos zur bundeslandspezifischen Formatierung bestimmen
		let _formatting: IGermanTaxNumberFormatting;
		try {
			_formatting = GermanTaxNumberDefiningData.instance.getFormatting(_bundeslandNr);
		} catch (_e) {
			return customFieldTypeConversionResult(
				oflStNrMitBuLaNr,
				SteuernummerValidationHelper._CONVERSION_ERROR_IDENTIFIER
			);
		}
		// Formale Prüfung der Oberflächendarstellung
		const _validOflFormat = this.getPatternToConvertOflToXml(_formatting);
		const _matcher = _validOflFormat.exec(oflStNrMitBuLaNr);
		if (!_matcher) {
			return customFieldTypeConversionResult(
				oflStNrMitBuLaNr,
				SteuernummerValidationHelper._CONVERSION_ERROR_IDENTIFIER
			);
		}

		const _finanzamt = _matcher[1];
		const _bezirk = _matcher[2];
		const _unterscheidungsnr = _matcher[3];
		const _pruefziffer = _matcher[4];

		const _bufaNr = this.getBundesfinanzamtsnummer(
			_bundeslandNr,
			_finanzamt,
			_formatting.getNumPositionsTaxOffice()
		);
		if (_bufaNr.length < 2 || _bufaNr.substring(0, 2) !== _bundeslandStr) {
			return customFieldTypeConversionResult(
				oflStNrMitBuLaNr,
				SteuernummerValidationHelper._CONVERSION_ERROR_IDENTIFIER
			);
		}
		return customFieldTypeConversionResult(
			_bufaNr + "0" + _bezirk + _unterscheidungsnr + _pruefziffer,
			undefined
		);
	}

	private getPatternToConvertOflToXml(formatting: IGermanTaxNumberFormatting): RegExp {
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
		if (!this.mapPatternForOflToXml.has(_muster)) {
			const _validOflFormat = new RegExp(_muster);
			this.mapPatternForOflToXml.set(_muster, _validOflFormat);
		}
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return this.mapPatternForOflToXml.get(_muster)!;
	}

	private getBundesfinanzamtsnummer(
		bundeslandnr: number,
		finanzamtsnr: string,
		stellenFinanzamt: number
	): string {
		const _strBulaNr = String(bundeslandnr);
		return (
			_strBulaNr.substring(0, SteuernummerValidationHelper._STELLEN_BUFANR - stellenFinanzamt) +
			finanzamtsnr
		);
	}
}
