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
import type { ICustomFieldTypeCheckError, ICustomFieldTypeConversionResult, ICustomFieldTypeValidationParam, ICustomFieldValidator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { customFieldTypeConversionResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import { CustomFieldTypeErrorEnum } from "./internal/error/CustomFieldTypeErrorEnum.js";
import { CustomFieldTypeErrorKeys } from "./internal/error/CustomFieldTypeErrorKeys.js";
import { CustomFieldTypeErrorUtil } from "./internal/error/CustomFieldTypeErrorUtil.js";

/**
 * This class check the BIC (Bank Identifier Code).
 * The validation of the BIC consists of two parts:
 * <ul>
 * <li>Is the BIC formally valid?</li>
 * <li>Is the country code valid?</li>
 * </ul>
 * <p>
 * If the BIC does not match the defined pattern in the regular expression it is a formal error.
 * </p>
 */
export class BICVerifier implements ICustomFieldValidator {
	private static readonly _INSTANCE: BICVerifier = new BICVerifier();
	/**
	 * Für den BIC sind zwei unterschiedliche Formate erlaubt:
	 * <p>
	 * BIC-Format 1:<br>
	 * Bankcode: vier Buchstaben,<br>
	 * Ländercode: zwei Buchstaben,<br>
	 * Ortscodierung: zwei Buchstaben/Ziffern.
	 * </p>
	 * <p>
	 * BIC-Format 2:<br>
	 * Wie Format 1, aber zusätzlich:<br>
	 * Filial- oder Abteilungskennzeichen: drei Buchstaben/Ziffern.
	 * </p>
	 * <p>
	 * Die beiden Formate können in einem regulären Ausdruck mit
	 * optionalem Abteilungskennzeichen zusammengefasst werden:
	 * <code>[a-zA-Z]{4}([a-zA-Z]{2})[0-9a-zA-Z]{2}([0-9a-zA-Z]{3})?</code>
	 * </p>
	 */
	private static readonly _VALID_BIC: RegExp = new RegExp(
		/^[a-zA-Z]{4}([a-zA-Z]{2})[0-9a-zA-Z]{2}([0-9a-zA-Z]{3})?$/
	);

	/**
	 * Liste der gültigen BIC-Länderkennungen.
	 * Die BIC-Liste enthält derzeit vier Einträge weniger, als die IBAN-Liste.
	 * Da die Listen laut Spezifikation nicht identisch sein müssen und zudem
	 * sehr klein sind werden sie getrennt gepflegt.
	 */
	private static readonly _BIC_LAENDER: Set<string> = new Set<string>();

	/**
	 * Don't use this. This is just for a static initialization.
	 */
	static _constructor = (() => {
		BICVerifier._BIC_LAENDER.add("AT"); // Österreich
		BICVerifier._BIC_LAENDER.add("BE"); // Belgien
		BICVerifier._BIC_LAENDER.add("BG"); // Bulgarien
		BICVerifier._BIC_LAENDER.add("CH"); // Schweiz
		BICVerifier._BIC_LAENDER.add("CY"); // Zypern
		BICVerifier._BIC_LAENDER.add("CZ"); // Tschechien
		BICVerifier._BIC_LAENDER.add("DE"); // Deutschland
		BICVerifier._BIC_LAENDER.add("DK"); // Dänemark
		BICVerifier._BIC_LAENDER.add("EE"); // Estland
		BICVerifier._BIC_LAENDER.add("ES"); // Spanien
		BICVerifier._BIC_LAENDER.add("FI"); // Finnland
		BICVerifier._BIC_LAENDER.add("FR"); // Frankreich
		BICVerifier._BIC_LAENDER.add("GB"); // Great Britain
		BICVerifier._BIC_LAENDER.add("GF"); // Französisch Guyana
		BICVerifier._BIC_LAENDER.add("GI"); // Gibraltar
		BICVerifier._BIC_LAENDER.add("GP"); // Guadeloupe
		BICVerifier._BIC_LAENDER.add("GR"); // Griechenland
		BICVerifier._BIC_LAENDER.add("HU"); // Ungarn
		BICVerifier._BIC_LAENDER.add("IE"); // Irland
		BICVerifier._BIC_LAENDER.add("IS"); // Island
		BICVerifier._BIC_LAENDER.add("IT"); // Italien
		BICVerifier._BIC_LAENDER.add("LI"); // Liechtenstein
		BICVerifier._BIC_LAENDER.add("LT"); // Litauen
		BICVerifier._BIC_LAENDER.add("LU"); // Luxemburg
		BICVerifier._BIC_LAENDER.add("LV"); // Lettland
		BICVerifier._BIC_LAENDER.add("MQ"); // Martinique
		BICVerifier._BIC_LAENDER.add("MT"); // Malta
		BICVerifier._BIC_LAENDER.add("NL"); // Niederlande
		BICVerifier._BIC_LAENDER.add("NO"); // Norwegen
		BICVerifier._BIC_LAENDER.add("PL"); // Polen
		BICVerifier._BIC_LAENDER.add("PT"); // Portugal
		BICVerifier._BIC_LAENDER.add("RE"); // Réunion
		BICVerifier._BIC_LAENDER.add("RO"); // Rumänien
		BICVerifier._BIC_LAENDER.add("SE"); // Schweden
		BICVerifier._BIC_LAENDER.add("SI"); // Slowenien
		BICVerifier._BIC_LAENDER.add("SK"); // Slowakei
		BICVerifier._BIC_LAENDER.add("MC"); // Monaco
		BICVerifier._BIC_LAENDER.add("YT"); // Mayotte
		BICVerifier._BIC_LAENDER.add("BL"); // Saint Barthélemy
		BICVerifier._BIC_LAENDER.add("MF"); // Saint Martin
		BICVerifier._BIC_LAENDER.add("PM"); // Saint Pierre and Miquelo
		BICVerifier._BIC_LAENDER.add("HR"); // Kroatien
		BICVerifier._BIC_LAENDER.add("SM"); // San Marino
		BICVerifier._BIC_LAENDER.add("AD"); // Andorra
		BICVerifier._BIC_LAENDER.add("VA"); // Vatikanstadt
		BICVerifier._BIC_LAENDER.add("AL"); // Albanien
		BICVerifier._BIC_LAENDER.add("MD"); // Moldawien
		BICVerifier._BIC_LAENDER.add("ME"); // Montenegro
		BICVerifier._BIC_LAENDER.add("MK"); // Nordmazedonien
	})();

	private constructor() {
		// at the moment, we don't need to do something
	}

	public static getInstance(): BICVerifier {
		return BICVerifier._INSTANCE;
	}

	validate(
		bic: string,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		valParam: ICustomFieldTypeValidationParam,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		isDisplayValue: boolean
	): ICustomFieldTypeCheckError | undefined {
		const _error = this._checkBic(bic);
		return CustomFieldTypeErrorUtil.getResult(
			_error,
			CustomFieldTypeErrorKeys.UNGUELTIGE_BIC
		);
	}

	/**
	 * Prüft die angegebene BIC und liefert den Key der passenden Fehlermeldung bzw.
	 * <code>undefined</code>, wenn die BIC gültig ist.
	 */
	private _checkBic(bic: string): CustomFieldTypeErrorEnum {
		const match: boolean = BICVerifier._VALID_BIC.test(bic);
		if (!match) {
			return CustomFieldTypeErrorEnum.BIC_FORMALER_FEHLER;
		}
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const arr: RegExpExecArray = BICVerifier._VALID_BIC.exec(bic)!;
		const laenderkennung: string = arr[1];
		if (!this._istLaenderkennungGueltig(laenderkennung)) {
			return CustomFieldTypeErrorEnum.BIC_LAENDERCODE_FEHLER;
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Prüft, ob die Länderkennung für eine BIC gültig ist.
	 */
	private _istLaenderkennungGueltig(laenderkennung: string): boolean {
		return BICVerifier._BIC_LAENDER.has(laenderkennung);
	}

	convertDisplay2Internal(displayValue: string): ICustomFieldTypeConversionResult {
		return customFieldTypeConversionResult(displayValue);
	}

	convertInternal2Display(internalValue: string): ICustomFieldTypeConversionResult {
		return customFieldTypeConversionResult(internalValue);
	}
}
