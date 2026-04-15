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
	Localizable,
	LocalizableArgs,
	NumberConversionConfig,
	SupportedType
} from "@com.mgmtp.a12.utils/utils-localization";
import {
	defaultValueConversion
} from "@com.mgmtp.a12.utils/utils-localization";

import {
	NumberConversion,
	NumberConversionConfigBuilder,
	NumberUtil,
	StringUtil
} from "@com.mgmtp.a12.utils/utils-localization/lib/main/conversion/a12internal/index.js";

import { RuntimeFormalErrorEnum } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import type { IIdentifier, IResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { IFormaleFehlerConstants } from "../util/IFormaleFehlerConstants.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";
import { ValidType } from "../util/ValidType.js";
import { VkBigDecimal } from "../util/VkBigDecimal.js";

import { ConversionUtils } from "./ConversionUtils.js";
import { FehlerHandler } from "./error/FehlerHandler.js";
import type { IFormatDefinition } from "./IFormatDefinition.js";
import type { IValidierungParam } from "./IValidierungParam.js";

export enum ZahlArt {
	GELDBETRAG = "amount",
	ZAHL = "none"
}

export class FormatDefinitionZahl implements IFormatDefinition {
	private static get _ZAHL_MIT_UNGUELTIGEN_ZEICHEN(): number {
		return 1;
	}
	private static get _ZAHL_ZU_LANG(): number {
		return 2;
	}
	private static get _ZAHL_ENTHAELT_VORZEICHEN(): number {
		return 3;
	}
	private static get _ZAHL_ENTHAELT_PLUS(): number {
		return 4;
	}
	private static get _ZAHL_IST_NULL(): number {
		return 5;
	}
	private static get _ZAHL_HAT_FUEHRENDE_NULLEN(): number {
		return 6;
	}
	private static get _ZAHL_HAT_DEZIMALTRENNER(): number {
		return 7;
	}
	private static get _ZAHL_OHNE_DEZIMALTRENNER(): number {
		return 8;
	}
	private static get _ZAHL_ZUVIELE_VORKOMMASTELLEN(): number {
		return 9;
	}
	private static get _ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN(): number {
		return 10;
	}
	private static get _ZAHL_NULL_ENTHAELT_VORZEICHEN(): number {
		return 11;
	}
	private static get _ZAHL_ZU_KURZ(): number {
		return 12;
	}
	private static get _ZAHL_KlEINER_MINWERT(): number {
		return 13;
	}
	private static get _ZAHL_GROESSER_MAXWERT(): number {
		return 14;
	}

	private static get _VORKOMMA_PATTERN(): string {
		return "[-]?[0-9]+";
	}
	private static get _VORKOMMA_PATTERN_KLAMMER(): string {
		return "[-]?[0-9]+(";
	}
	private static get _GKLAMMER_ZU(): string {
		return "}";
	}
	private static get _GKLAMMER_ZU_OPT(): string {
		return "})?";
	}
	private static get _ANGABE_ANZAHL(): string {
		return "[0-9]{";
	}
	private static get _VORKOMMA_MAX(): string {
		return "[-]?[0-9]{1,";
	}

	private _hatVorzeichen: boolean;
	private _nullErlaubt: boolean;
	private _fuehrendeNullenErlaubt: boolean;

	private _vorkommaStellenDisp: number | undefined;
	private _vorkommaStellen = -1;

	private _nachkommaStellenBisDisp: number | undefined;
	private _nachkommaStellenBis = -1;

	private _nachkommaStellenDisp: number | undefined;
	private _nachkommaStellen = -1;

	private _art: ZahlArt;
	private _minLaenge: number;
	private _minLaengeDisp: number;
	private _maxLaenge: number;
	private _maxLaengeDisp: number;

	private _minWert: VkBigDecimal | undefined;
	private _maxWert: VkBigDecimal | undefined;
	// minWertDisp = der im ExtMP Editor angezeigte Wert für minWert
	private _minWertDisp: string;
	// maxWertDisp = der im ExtMP Editor angezeigte Wert für maxWert
	private _maxWertDisp: string;

	private _nullen = "";
	feldTyp: string | undefined;

	// Patterns werden im Konstruktor gebildet, damit nachher die Überprüfung
	// schnell geht.
	private _anzahlNachkommastellenPattern: RegExp[] = new Array<RegExp>(2);
	private _anzahlVarOptNachkommastellenPattern: RegExp[] = new Array<RegExp>(2);
	private _anzahlVarNachkommastellenPattern: RegExp[] = new Array<RegExp>(2);
	private _anzahlVorkommastellenPattern: RegExp[] = new Array<RegExp>(2);

	private _thousandsSeparatorPatternCache: Map<string, RegExp[]> = new Map<string, RegExp[]>();

	private _conversionConfig: NumberConversionConfig;

	private static readonly _zahlMitPlusPattern: RegExp[] = [
		new RegExp("^(?:[+][0-9]+(\\.[0-9]+)?)$"),
		new RegExp("^(?:[+][0-9]+(,[0-9]+)?)$")
	];

	// Omitting backtracking hell (\d+(X?\d{3})*) -> \d+(X\d{3}(X?\d{3})*)?
	// We don't know what the thousand separator is, so we quote it with \Q\E
	// We want only one thousand separator parameter and one for the decimal separator,
	// so {0} is the thousand separator and {1} is the decimal separator
	// The pattern is similar to the DECIMAL_PLACES_OPTIONAL_PRECHECK_PATTERN
	// Attention: The {3} will not be replaced as long as there are less than four parameters in the format method.
	private static readonly _THOUSANDS_SEPARATOR_PATTERN_TEMPLATE =
		"^(?:[-]?[0-9]+({0}[0-9]{3}(({0})?[0-9]{3})*)?({1}[0-9]*)?)$";

	constructor(
		vorzeichen: boolean,
		nullErlaubt: boolean,
		fuehrendeNullenErlaubt: boolean,
		vorkomma: number,
		nachkomma: number,
		nachkommaBis: number,
		zahl: ZahlArt,
		minLaenge: number,
		maxLaenge: number,
		minWert: string,
		maxWert: string
	);
	constructor(
		vorzeichen: boolean,
		nullErlaubt: boolean,
		fuehrendeNullenErlaubt: boolean,
		vorkomma: number,
		nachkomma: number,
		nachkommaBis: number,
		minLaenge: number,
		minLaengeDisp: number,
		maxLaenge: number,
		maxLaengeDisp: number,
		istGeldbetrag: boolean,
		minWert: string,
		maxWert: string
	);
	constructor(
		vorzeichen: boolean,
		nullErlaubt: boolean,
		fuehrendeNullenErlaubt: boolean,
		vorkomma: number,
		nachkomma: number,
		nachkommaBis: number,
		zahlOrMinLaenge: ZahlArt | number,
		minLaengeOrMinLaengeDisp: number,
		maxLaenge: number,
		minWertOrMaxLaengeDisp: string | number,
		maxWertOrIstGeldbetrag: string | boolean,
		minWert?: string,
		maxWert?: string
	) {
		if (minWert == undefined) {
			this._hatVorzeichen = vorzeichen;
			this._nullErlaubt = nullErlaubt;
			this._fuehrendeNullenErlaubt = fuehrendeNullenErlaubt;
			this._vorkommaStellen = vorkomma;
			this._vorkommaStellenDisp = -1;
			this._nachkommaStellen = nachkomma;
			this._nachkommaStellenDisp = -1;
			this._nachkommaStellenBis = nachkommaBis;
			this._nachkommaStellenBisDisp = -1;
			this._art = <ZahlArt>zahlOrMinLaenge;
			this._minLaenge = minLaengeOrMinLaengeDisp;
			this._minLaengeDisp = -1;
			this._maxLaenge = maxLaenge;
			this._maxLaengeDisp = -1;
			this._minWertDisp = <string>minWertOrMaxLaengeDisp;
			if (!this._minWertDisp || 0 === this._minWertDisp.length) {
				this._minWert = new VkBigDecimal(this._minWertDisp);
			}
			this._maxWertDisp = <string>maxWertOrIstGeldbetrag;
			if (this._maxWertDisp || 0 === this._maxWertDisp.length) {
				this._maxWert = new VkBigDecimal(this._maxWertDisp);
			}
		} else {
			this._hatVorzeichen = vorzeichen;
			this._nullErlaubt = nullErlaubt;
			this._fuehrendeNullenErlaubt = fuehrendeNullenErlaubt;
			this._vorkommaStellen = vorkomma;
			this._vorkommaStellenDisp = vorkomma;
			this._nachkommaStellen = nachkomma;
			this._nachkommaStellenDisp = nachkomma;
			this._nachkommaStellenBis = nachkommaBis;
			this._nachkommaStellenBisDisp = nachkommaBis;
			this._art = <boolean>maxWertOrIstGeldbetrag ? ZahlArt.GELDBETRAG : ZahlArt.ZAHL;
			this._minLaenge = <number>zahlOrMinLaenge;
			this._minLaengeDisp = minLaengeOrMinLaengeDisp;
			this._maxLaenge = maxLaenge;
			this._maxLaengeDisp = <number>minWertOrMaxLaengeDisp;
			this._minWertDisp = minWert;
			if (this._minWertDisp.length !== 0) {
				this._minWert = new VkBigDecimal(this._minWertDisp);
			}
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			this._maxWertDisp = maxWert!;
			if (this._maxWertDisp.length !== 0) {
				this._maxWert = new VkBigDecimal(this._maxWertDisp);
			}
		}
		this._init();
		// not in init because the compiler does not recognize that it is initialized
		this._conversionConfig = new NumberConversionConfigBuilder()
			.withMinFractionalDigits(this._nachkommaStellen)
			.withLeadingZerosAllowed(this._fuehrendeNullenErlaubt)
			.build();
	}

	private _init(): void {
		if (this._art === ZahlArt.GELDBETRAG) {
			if (this._nachkommaStellen === 0) {
				this.feldTyp = "GeldBetragOhneCent";
			} else {
				this.feldTyp = "GeldBetragMitCent";
			}
		} else {
			this.feldTyp = "Zahl";
		}

		// in diesem Fall muss nachkommaStellenBis > 0 sein und entspricht der oberen
		// Grenze der Nachkommastellen, vgl. EXT-5094
		if (this._nachkommaStellen === -1) {
			this._nachkommaStellenDisp = 0;
		}

		// in diesem Fall muss nachkommaStellen >= 0 sein und entspricht der exakten
		// Anzahl der Nachkommastellen, vgl. EXT-5094
		if (this._nachkommaStellenBis === -1) {
			this._nachkommaStellenBisDisp = this._nachkommaStellenDisp;
		}
		if (this._vorkommaStellen === -1) {
			this._vorkommaStellenDisp = undefined;
		}

		// Vorbelegung der regexps, damit dies nur einmal beim Einlesen gemacht
		// werden muss.
		if (
			this._nachkommaStellen > 0 &&
			(this._nachkommaStellenBis === this._nachkommaStellen || this._nachkommaStellenBis === -1)
		) {
			for (let i = 0; i < 2; i++) {
				let strRegex = "^(?:";
				strRegex += FormatDefinitionZahl._VORKOMMA_PATTERN;
				strRegex += DarstellungsInformationIntern.getDezimalTrennerForPattern(i);
				strRegex += FormatDefinitionZahl._ANGABE_ANZAHL;
				strRegex += this._nachkommaStellen;
				strRegex += FormatDefinitionZahl._GKLAMMER_ZU;
				strRegex += ")$";
				this._anzahlNachkommastellenPattern[i] = new RegExp(strRegex);
			}
			let str = "";
			for (let i = 0; i < this._nachkommaStellen; i++) {
				str += "0";
			}
			this._nullen = str;
		}

		if (
			this._nachkommaStellenBis > 0 &&
			(this._nachkommaStellen === 0 || this._nachkommaStellen === -1)
		) {
			this._nachkommaStellen = 0;
			for (let i = 0; i < 2; i++) {
				let str = "^(?:";
				str += FormatDefinitionZahl._VORKOMMA_PATTERN_KLAMMER;
				str += DarstellungsInformationIntern.getDezimalTrennerForPattern(i);
				str += FormatDefinitionZahl._ANGABE_ANZAHL;
				str += this._nachkommaStellen;
				str += ",";
				str += this._nachkommaStellenBis;
				str += FormatDefinitionZahl._GKLAMMER_ZU_OPT;
				str += ")$";
				this._anzahlVarOptNachkommastellenPattern[i] = new RegExp(str);
			}
		}
		if (this._nachkommaStellenBis > 0 && this._nachkommaStellen > 0) {
			for (let i = 0; i < 2; i++) {
				let str = "^(?:";
				str += FormatDefinitionZahl._VORKOMMA_PATTERN;
				str += DarstellungsInformationIntern.getDezimalTrennerForPattern(i);
				str += FormatDefinitionZahl._ANGABE_ANZAHL;
				str += this._nachkommaStellen;
				str += ",";
				str += this._nachkommaStellenBis;
				str += FormatDefinitionZahl._GKLAMMER_ZU;
				str += ")$";
				this._anzahlVarNachkommastellenPattern[i] = new RegExp(str);
			}
		}
		if (this._vorkommaStellen > 0) {
			let str = "^(?:";
			str += FormatDefinitionZahl._VORKOMMA_MAX;
			str += this._vorkommaStellen;
			str += FormatDefinitionZahl._GKLAMMER_ZU;
			str += ")$";
			this._anzahlVorkommastellenPattern[0] = new RegExp(str);
			this._anzahlVorkommastellenPattern[1] = this._anzahlVorkommastellenPattern[0];
		}
	}

	/**
	 * Konvertierungsmethode: es wird von der Oberflächendarstellung in die XML
	 * Darstellung konvertiert.
	 * Die Art des Dezimaltrenners ist zum Codegenerierungszeitpunkt bekannt und
	 * ist daher fest in der Konvertierungsmethode implementiert.
	 */
	public convert(
		wert: string,
		darstellungsInfo: DarstellungsInformationIntern,
		vonXml: boolean,
		_customFieldTypeFactory: ICustomFieldTypeFactory | undefined,
	): string {
		const internalDataType = ConversionUtils.createNumberConversionConfig(
			this._conversionConfig,
			darstellungsInfo.dezimalTrennerIntern,
			false
		);
		const displayDataType = ConversionUtils.createNumberConversionConfig(
			this._conversionConfig,
			darstellungsInfo.dezimalTrenner,
			darstellungsInfo.decimalPlacesOptional,
			darstellungsInfo.getThousandsSeparatorForTrait(this._art.toString())
		);
		let conversionResult;
		if (vonXml) {
			conversionResult = NumberConversion.convert(wert, internalDataType, displayDataType);
		} else {
			conversionResult = NumberConversion.convert(wert, displayDataType, internalDataType);
		}
		return conversionResult;
	}

	validiereFormat(
		param: IValidierungParam,
		fullCheck: boolean
	): ValidType {
		if (!fullCheck && param.getWert().length === 0) {
			// this case is for the base type conversion -> an empty value will be converted to null later on
			return ValidType.Valid;
		}

		const darstellungsInfo: DarstellungsInformationIntern = param.getDarstellungsInfo();
		const id: IIdentifier = param.getIdentifier();
		const erg: IResult = param.getErgebnis();
		let wert: string = param.getWert();
		const nurPruefen: boolean = param.nurPruefen();

		// Mit Hilfe des Parameters darstellungsInfo wird die Darstellungsinformation
		// zu dem Dezimaltrenner geholt.
		const dezimalTrenner = darstellungsInfo.dezimalTrenner;

		// In den vereinbarten Patterns ist jeweils eine Option für Kommadarstellung
		// und eine Option für Punktdarstellung generiert.
		// Im Index wird nun gespeichert, ob die Komma oder Punkt Notation verwendet wird.
		// Dazu wird aus der Darstellungsinformation der richtige Index geholt.
		const index: number = darstellungsInfo.dezimalIndexFuerValidierung;

		const thousandsSeparator = darstellungsInfo.getThousandsSeparatorForTrait(this._art.toString());
		if (thousandsSeparator) {
			const thousandsSeparatorPattern: RegExp = this._getThousandsSeparatorPattern(
				// TODO: Fix this the next time the file is edited.
				// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
				thousandsSeparator!,
				index
			);
			const nonLenient: any = param.nonLenientConversion();
			// This method is not only used for the conversion, but also for other functions like validation,
			// calculation and others.
			// At this point the non-lenient parameter is always set for the conversions,
			// but it is not set for the other functions, for which we want to keep the original behavior:
			// non-lenient validation.
			// This means, if the non-lenient parameter is not set, the validation should be non-lenient.
			if ((nonLenient == null || <boolean>nonLenient) && !thousandsSeparatorPattern.test(wert)) {
				const fehlerText = this._getFehlerText(
					FormatDefinitionZahl._ZAHL_MIT_UNGUELTIGEN_ZEICHEN,
					param
				);
				return FehlerHandler.addFehler(
					IFormaleFehlerConstants.FEHLER_ZAHL_MIT_UNGUELTIGEN_ZEICHEN,
					[fehlerText],
					param
				);
			}
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			wert = wert.replaceAll(thousandsSeparator!, "");
		}

		if (darstellungsInfo.decimalPlacesOptional) {
			wert = NumberUtil.addDecimalPlacesIfNecessary(wert, dezimalTrenner, index, this._nachkommaStellen);
		}

		// Enthaelt Wert ungültiges Zeichen ?
		// zwar ist es eine Zahl, aber mit einem Plus-Vorzeichen
		if (FormatDefinitionZahl._zahlMitPlusPattern[index].test(wert)) {
			const fehlerText = this._getFehlerText(
				FormatDefinitionZahl._ZAHL_ENTHAELT_PLUS,
				param
			);
			return FehlerHandler.addFehler(
				erg,
				IFormaleFehlerConstants.FEHLER_ZAHL_ENTHAELT_PLUS,
				id,
				[fehlerText],
				nurPruefen
			);
		}
		if (!NumberUtil.NUMBER_PATTERN[index].test(wert)) {
			const fehlerText = this._getFehlerText(
				FormatDefinitionZahl._ZAHL_MIT_UNGUELTIGEN_ZEICHEN,
				param
			);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.FEHLER_ZAHL_MIT_UNGUELTIGEN_ZEICHEN,
				[fehlerText],
				param
			);
		}

		// Wir wollen nur die Stelligkeit überprüfen, die 15 ist
		// Dezimaltrenner und Vorzeichen müssen rausgefiltert werden
		const stelligkeit: string = wert.replace(dezimalTrenner, "").replace(/-/g, "");
		if (stelligkeit.length > 15) {
			const fehlerText = LocalizableUtil.createLocalizableFormalError(RuntimeFormalErrorEnum.STELLIGKEIT_ZU_LANG);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.STELLIGKEIT_ZAHL_ZU_LANG,
				[fehlerText],
				param
			);
		}

		if (!fullCheck) {
			return ValidType.Valid;
		}

		const d: VkBigDecimal = new VkBigDecimal(wert.replace(dezimalTrenner, "."));

		// Null enthält ein Vorzeichen
		if (
			VkBigDecimal.ZAHL_0.compareTo(d) === 0 &&
			this._nullErlaubt &&
			this._hatVorzeichen &&
			wert.indexOf("-") !== -1
		) {
			const fehlerText = this._getFehlerText(
				FormatDefinitionZahl._ZAHL_NULL_ENTHAELT_VORZEICHEN,
				param
			);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.FEHLER_NULL_ENTHAELT_VORZEICHEN,
				[fehlerText],
				param
			);
		}

		// Zahl enthält Vorzeichen, obwohl ohne Vorzeichen definiert?
		if (!this._hatVorzeichen && wert.indexOf("-") !== -1) {
			if (
				VkBigDecimal.ZAHL_0.compareTo(d) === 0 &&
				!this._nullErlaubt &&
				this._art === ZahlArt.GELDBETRAG
			) {
				const fehlerText = this._getFehlerText(FormatDefinitionZahl._ZAHL_IST_NULL, param);
				return FehlerHandler.addFehler(
					IFormaleFehlerConstants.FEHLER_ZAHL_IST_NULL,
					[fehlerText],
					param
				);
			} else {
				const fehlerText = this._getFehlerText(FormatDefinitionZahl._ZAHL_ENTHAELT_VORZEICHEN, param);
				return FehlerHandler.addFehler(
					IFormaleFehlerConstants.FEHLER_ZAHL_ENTHAELT_VORZEICHEN,
					[fehlerText],
					param
				);
			}
		}

		if (
			this._nachkommaStellen === 0 &&
			this._nachkommaStellenBis <= 0 &&
			wert.indexOf(dezimalTrenner) !== -1
		) {
			// darf kein "," im String sein
			const fehlerText = this._getFehlerText(
				FormatDefinitionZahl._ZAHL_HAT_DEZIMALTRENNER,
				param
			);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.FEHLER_ZAHL_HAT_DEZIMALTRENNER,
				[fehlerText],
				param
			);
		}
		if (this._nachkommaStellen > 0 && wert.indexOf(dezimalTrenner) === -1) {
			// es muss ein "," im String sein
			const fehlerText = this._getFehlerText(
				FormatDefinitionZahl._ZAHL_OHNE_DEZIMALTRENNER,
				param
			);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.FEHLER_ZAHL_OHNE_DEZIMALTRENNER,
				[fehlerText],
				param
			);
		}

		if (this._nachkommaStellen > 0 && this._nachkommaStellenBis === -1) {
			if (!this._anzahlNachkommastellenPattern[index].test(wert)) {
				const fehlerText = this._getFehlerText(
					FormatDefinitionZahl._ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN,
					param
				);
				return FehlerHandler.addFehler(
					IFormaleFehlerConstants.FEHLER_ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN,
					[fehlerText],
					param
				);
			}
		}

		if (this._nachkommaStellenBis > 0 && this._nachkommaStellen === 0) {
			if (!this._anzahlVarOptNachkommastellenPattern[index].test(wert)) {
				const fehlerText = this._getFehlerText(
					FormatDefinitionZahl._ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN,
					param
				);
				return FehlerHandler.addFehler(
					IFormaleFehlerConstants.FEHLER_ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN,
					[fehlerText],
					param
				);
			}
		}
		if (this._nachkommaStellenBis > 0 && this._nachkommaStellen > 0) {
			if (!this._anzahlVarNachkommastellenPattern[index].test(wert)) {
				const fehlerText = this._getFehlerText(
					FormatDefinitionZahl._ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN,
					param
				);
				return FehlerHandler.addFehler(
					IFormaleFehlerConstants.FEHLER_ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN,
					[fehlerText],
					param
				);
			}
		}

		// Vorkommastellen Check
		if (this._vorkommaStellen > -1) {
			// bei einer komma-Zahl nimm nur den vorderen Wert zum Überprüfen
			const indexDezimalTrenner: number = wert.indexOf(dezimalTrenner);
			const w: string = indexDezimalTrenner > -1 ? wert.substring(0, indexDezimalTrenner) : wert;
			if (!this._anzahlVorkommastellenPattern[index].test(w)) {
				const fehlerText = this._getFehlerText(
					FormatDefinitionZahl._ZAHL_ZUVIELE_VORKOMMASTELLEN,
					param
				);
				return FehlerHandler.addFehler(
					IFormaleFehlerConstants.FEHLER_ZAHL_ZUVIELE_VORKOMMASTELLEN,
					[fehlerText],
					param
				);
			}
		}
		if (VkBigDecimal.ZAHL_0.compareTo(d) === 0 && !this._nullErlaubt) {
			const fehlerText = this._getFehlerText(FormatDefinitionZahl._ZAHL_IST_NULL, param);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.FEHLER_ZAHL_IST_NULL,
				[fehlerText],
				param
			);
		}

		// Fängt mit Null an, und ist länger als 1 Zeichen bzw. keine Null
		// vor der Vorkommastelle. -> Fehler
		if (!this._fuehrendeNullenErlaubt && NumberUtil.containsLeadingZeros(wert, dezimalTrenner)) {
			const fehlerText = this._getFehlerText(
				FormatDefinitionZahl._ZAHL_HAT_FUEHRENDE_NULLEN,
				param
			);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.FEHLER_ZAHL_HAT_FUEHRENDE_NULLEN,
				[fehlerText],
				param
			);
		}

		if (this._minLaenge !== -1 && wert.length < this._minLaenge) {
			const fehlerText = this._getFehlerText(FormatDefinitionZahl._ZAHL_ZU_KURZ, param);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.FEHLER_ZAHL_ZU_KURZ,
				[fehlerText],
				param
			);
		}

		if (this._maxLaenge !== -1 && wert.length > this._maxLaenge) {
			const fehlerText = this._getFehlerText(FormatDefinitionZahl._ZAHL_ZU_LANG, param);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.FEHLER_ZAHL_ZU_LANG,
				[fehlerText],
				param
			);
		}
		if (this._minWert != undefined && this._minWert.compareTo(d) > 0) {
			const fehlerText = this._getFehlerText(
				FormatDefinitionZahl._ZAHL_KlEINER_MINWERT,
				param
			);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.FEHLER_ZAHL_ZU_KLEIN,
				[fehlerText],
				param
			);
		}
		if (this._maxWert != undefined && this._maxWert.compareTo(d) < 0) {
			const fehlerText = this._getFehlerText(
				FormatDefinitionZahl._ZAHL_GROESSER_MAXWERT,
				param
			);
			return FehlerHandler.addFehler(
				IFormaleFehlerConstants.FEHLER_ZAHL_ZU_GROSS,
				[fehlerText],
				param
			);
		}

		return ValidType.Valid;
	}

	private _getThousandsSeparatorPattern(thousandsSeparator: string, index: number): RegExp {
		let patterns = this._thousandsSeparatorPatternCache.get(thousandsSeparator);
		if (patterns == undefined) {
			this._thousandsSeparatorPatternCache.set(thousandsSeparator, (patterns = new Array(2)));
		}
		if (patterns[index] == undefined) {
			return (patterns[index] = this._createThousandsSeparatorPattern(thousandsSeparator, index));
		} else {
			return patterns[index];
		}
	}

	private _createThousandsSeparatorPattern(thousandsSeparator: string, index: number): RegExp {
		const thousandsSeparatorPatternString = FormatDefinitionZahl.escapeRegExp(thousandsSeparator);
		const decimalSeparatorPatternString =
			DarstellungsInformationIntern.getDezimalTrennerForPattern(index);
		// Attention: The stringFormat method must get less than four parameters, else {3} will be replaced, too
		return new RegExp(
			StringUtil.stringFormat(
				FormatDefinitionZahl._THOUSANDS_SEPARATOR_PATTERN_TEMPLATE,
				thousandsSeparatorPatternString,
				decimalSeparatorPatternString
			)
		);
	}

	private static escapeRegExp(str: string) { // may be replaced with RegExp.escape once available
		// eslint-disable-next-line no-useless-escape
		return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
	}

	/**
	 * Der Default wird verwendet, wenn in einem Fehlertext auf ein Wert zugegriffen wird,
	 * in dem Feld aber gar kein Wert übergeben wurde.
	 */
	public getDefaultValue(

		_metaData: IMetaDataValidierung,

		_dataFormats: DataFormats | null
	): number {
		return 0;
	}

	/**
	 * allgemeine Methode zum Validieren des Eingabewertes wert.
	 */
	private _getFehlerIdFuerZahlen(error: number): RuntimeFormalErrorEnum {
		if (error === FormatDefinitionZahl._ZAHL_ENTHAELT_PLUS) {
			// ohne Vorzeichen
			if (!this.hatVorzeichen) {
				return RuntimeFormalErrorEnum.ZAHL_ENTHAELT_VORZEICHEN;
			} else {
				// mit Vorzeichen
				return RuntimeFormalErrorEnum.ZAHL_ENTHAELT_PLUS;
			}
		}
		if (
			error === FormatDefinitionZahl._ZAHL_MIT_UNGUELTIGEN_ZEICHEN ||
			error === FormatDefinitionZahl._ZAHL_OHNE_DEZIMALTRENNER ||
			error === FormatDefinitionZahl._ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN
		) {
			// ohne Nachkommastellen
			if (
				this._nachkommaStellen === 0 &&
				(this._nachkommaStellenBis === this._nachkommaStellen || this._nachkommaStellenBis === -1)
			) {
				return RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_ONK;
			}

			// variable Nachkommastellen
			if (this._nachkommaStellenBis > 0 && this._nachkommaStellenBis !== this._nachkommaStellen) {
				return RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_VNK;
			}

			// fixe Nachkommastellen
			if (
				this._nachkommaStellen > 0 &&
				(this._nachkommaStellenBis === this._nachkommaStellen || this._nachkommaStellenBis === -1)
			) {
				return this._hatVorzeichen
					? RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKPVZ
					: RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKOVZ;
			}
			// über Nachkommastellen wurde keine Aussage getroffen.
			return RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN;
		}

		if (error === FormatDefinitionZahl._ZAHL_ENTHAELT_VORZEICHEN) {
			return RuntimeFormalErrorEnum.ZAHL_ENTHAELT_VORZEICHEN;
		}
		if (error === FormatDefinitionZahl._ZAHL_HAT_DEZIMALTRENNER) {
			return RuntimeFormalErrorEnum.ZAHL_HAT_DEZIMALTRENNER;
		}

		if (error === FormatDefinitionZahl._ZAHL_ZUVIELE_VORKOMMASTELLEN) {
			if (this._nachkommaStellen === 0 && this._nachkommaStellenBis < 1) {
				return this._hatVorzeichen
					? RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZONK
					: RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZONK;
			} else {
				return this._hatVorzeichen
					? RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZ
					: RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZ;
			}
		}

		if (error === FormatDefinitionZahl._ZAHL_IST_NULL) {
			return this._hatVorzeichen
				? RuntimeFormalErrorEnum.ZAHL_IST_NULL
				: RuntimeFormalErrorEnum.ZAHL_IST_NULL_OVZ;
		}

		if (error === FormatDefinitionZahl._ZAHL_NULL_ENTHAELT_VORZEICHEN) {
			return RuntimeFormalErrorEnum.ZAHL_NULL_ENTHAELT_VORZEICHEN;
		}

		if (error === FormatDefinitionZahl._ZAHL_HAT_FUEHRENDE_NULLEN) {
			return RuntimeFormalErrorEnum.ZAHL_HAT_FUEHRENDE_NULLEN;
		}

		if (error === FormatDefinitionZahl._ZAHL_ZU_LANG) {
			return RuntimeFormalErrorEnum.ZAHL_ZU_LANG;
		}

		if (error === FormatDefinitionZahl._ZAHL_ZU_KURZ) {
			return RuntimeFormalErrorEnum.ZAHL_ZU_KURZ;
		}

		if (error === FormatDefinitionZahl._ZAHL_KlEINER_MINWERT) {
			return RuntimeFormalErrorEnum.ZAHL_ZU_KLEIN;
		}
		if (error === FormatDefinitionZahl._ZAHL_GROESSER_MAXWERT) {
			return RuntimeFormalErrorEnum.ZAHL_ZU_GROSS;
		}

		return RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN;
	}

	private _getFehlerIdGBMC(error: number): RuntimeFormalErrorEnum {
		if (error === FormatDefinitionZahl._ZAHL_ENTHAELT_PLUS) {
			// ohne Vorzeichen
			if (!this.hatVorzeichen) {
				return RuntimeFormalErrorEnum.GBMC_ENTHAELT_VORZEICHEN;
			} else {
				// mit Vorzeichen
				return RuntimeFormalErrorEnum.GBMC_ENTHAELT_PLUS;
			}
		}
		if (
			error === FormatDefinitionZahl._ZAHL_MIT_UNGUELTIGEN_ZEICHEN ||
			error === FormatDefinitionZahl._ZAHL_OHNE_DEZIMALTRENNER ||
			error === FormatDefinitionZahl._ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN
		) {
			return this._hatVorzeichen
				? RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_MVZ
				: RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_OVZ;
		}
		if (error === FormatDefinitionZahl._ZAHL_ENTHAELT_VORZEICHEN) {
			return RuntimeFormalErrorEnum.GBMC_ENTHAELT_VORZEICHEN;
		}

		if (error === FormatDefinitionZahl._ZAHL_NULL_ENTHAELT_VORZEICHEN) {
			return RuntimeFormalErrorEnum.GBMC_NULL_ENTHAELT_VORZEICHEN;
		}

		if (error === FormatDefinitionZahl._ZAHL_ZUVIELE_VORKOMMASTELLEN) {
			return this._hatVorzeichen
				? RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_MVZ
				: RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_OVZ;
		}

		if (error === FormatDefinitionZahl._ZAHL_IST_NULL) {
			return this._hatVorzeichen
				? RuntimeFormalErrorEnum.ZAHL_IST_NULL
				: RuntimeFormalErrorEnum.ZAHL_IST_NULL_OVZ;
		}

		if (error === FormatDefinitionZahl._ZAHL_HAT_FUEHRENDE_NULLEN) {
			return RuntimeFormalErrorEnum.GBMC_HAT_FUEHRENDE_NULLEN;
		}
		if (error === FormatDefinitionZahl._ZAHL_ZU_LANG) {
			return RuntimeFormalErrorEnum.GBMC_ZU_LANG;
		}

		if (error === FormatDefinitionZahl._ZAHL_ZU_KURZ) {
			return RuntimeFormalErrorEnum.GBMC_ZU_KURZ;
		}

		if (error === FormatDefinitionZahl._ZAHL_KlEINER_MINWERT) {
			return RuntimeFormalErrorEnum.GBMC_ZU_KLEIN;
		}
		if (error === FormatDefinitionZahl._ZAHL_GROESSER_MAXWERT) {
			return RuntimeFormalErrorEnum.GBMC_ZU_GROSS;
		}

		return RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_OVZ;
	}

	private _getFehlerIdGBOC(error: number): RuntimeFormalErrorEnum {
		if (error === FormatDefinitionZahl._ZAHL_ENTHAELT_PLUS) {
			// ohne Vorzeichen
			if (!this.hatVorzeichen) {
				return RuntimeFormalErrorEnum.GBOC_ENTHAELT_VORZEICHEN;
			} else {
				// mit Vorzeichen
				return RuntimeFormalErrorEnum.GBOC_ENTHAELT_PLUS;
			}
		}
		if (
			error === FormatDefinitionZahl._ZAHL_MIT_UNGUELTIGEN_ZEICHEN ||
			error === FormatDefinitionZahl._ZAHL_HAT_DEZIMALTRENNER
		) {
			return this._hatVorzeichen
				? RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_MVZ
				: RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_OVZ;
		}
		if (error === FormatDefinitionZahl._ZAHL_ENTHAELT_VORZEICHEN) {
			return RuntimeFormalErrorEnum.GBOC_ENTHAELT_VORZEICHEN;
		}

		if (error === FormatDefinitionZahl._ZAHL_ZUVIELE_VORKOMMASTELLEN) {
			return this._hatVorzeichen
				? RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_MVZ
				: RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_OVZ;
		}

		if (error === FormatDefinitionZahl._ZAHL_IST_NULL) {
			return this._hatVorzeichen
				? RuntimeFormalErrorEnum.ZAHL_IST_NULL
				: RuntimeFormalErrorEnum.ZAHL_IST_NULL_OVZ;
		}

		if (error === FormatDefinitionZahl._ZAHL_NULL_ENTHAELT_VORZEICHEN) {
			return RuntimeFormalErrorEnum.GBOC_NULL_ENTHAELT_VORZEICHEN;
		}

		if (error === FormatDefinitionZahl._ZAHL_HAT_FUEHRENDE_NULLEN) {
			return RuntimeFormalErrorEnum.GBOC_HAT_FUEHRENDE_NULLEN;
		}

		if (error === FormatDefinitionZahl._ZAHL_ZU_LANG) {
			return RuntimeFormalErrorEnum.GBOC_ZU_LANG;
		}

		if (error === FormatDefinitionZahl._ZAHL_ZU_KURZ) {
			return RuntimeFormalErrorEnum.GBOC_ZU_KURZ;
		}

		if (error === FormatDefinitionZahl._ZAHL_KlEINER_MINWERT) {
			return RuntimeFormalErrorEnum.GBOC_ZU_KLEIN;
		}
		if (error === FormatDefinitionZahl._ZAHL_GROESSER_MAXWERT) {
			return RuntimeFormalErrorEnum.GBOC_ZU_GROSS;
		}
		return this._hatVorzeichen
			? RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_MVZ
			: RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_OVZ;
	}

	private _getFehlerText(error: number, param: IValidierungParam): Localizable {
		let fehlerId: RuntimeFormalErrorEnum;
		switch (this._art) {
			case ZahlArt.ZAHL:
				fehlerId = this._getFehlerIdFuerZahlen(error);
				break;
			case ZahlArt.GELDBETRAG:
				fehlerId =
					this._nachkommaStellen === 2
						? this._getFehlerIdGBMC(error)
						: this._getFehlerIdGBOC(error);
				break;
			default:
				throw new Error("Not Implemented - " + this._art);
		}

		const errorTextDefaults = FehlerHandler.getAllErrorTexts(fehlerId);

		const decimalSeparatorPlaceholder: DataFormatPlaceholder = {
			type: "dataFormat",
			value: param.getDarstellungsInfo().dezimalTrenner,
			properties: {
				type: "decimalSeparator"
			}
		}

		const fieldShortName = param.getMetaData().getUniqueName(param.getIdentifier().getName());
		const minValuePlaceholder = LocalizableUtil.createFieldValuePlaceholder(fieldShortName, param.getMetaData(), this._minWert?.number())
		const maxValuePlaceholder = LocalizableUtil.createFieldValuePlaceholder(fieldShortName, param.getMetaData(), this._maxWert?.number())

		const args: LocalizableArgs = {
			decimalSeparator: decimalSeparatorPlaceholder,
			minValue: minValuePlaceholder,
			maxValue: maxValuePlaceholder,
			zeros: LocalizableUtil.createPlainPlaceholder(this._nullen),
			numberOfDecimalPlaces: LocalizableUtil.createPlainPlaceholder(this._nachkommaStellen.toString()),
			maxNumberOfDecimalPlaces: LocalizableUtil.createPlainPlaceholder(this._nachkommaStellenBis.toString()),
			numberOfDigitsBeforeDecimalPoint: LocalizableUtil.createPlainPlaceholder(this._vorkommaStellen.toString()),
			minLength: LocalizableUtil.createPlainPlaceholder(this._minLaenge.toString()),
			maxLength: LocalizableUtil.createPlainPlaceholder(this._maxLaenge.toString())
		}

		return {
			key: LocalizableUtil.createFormalErrorKey(fehlerId),
			args,
			defaults: errorTextDefaults
		};
	}

	public get hatVorzeichen(): boolean {
		return this._hatVorzeichen;
	}

	/**
	 * Liefert alle MetaInformationen, die mit dem Feldtypen assoziiert sind.
	 */
	getWert(key: string): any {
		if (key === IMetaKeys.FIELD_MIN_LENGTH) {
			return this._minLaengeDisp;
		}
		if (key === IMetaKeys.FIELD_MAX_LENGTH) {
			return this._maxLaengeDisp;
		}
		if (key === IMetaKeys.FIELD_FIELD_TYPE) {
			return this.feldTyp;
		}
		if (key === IMetaKeys.FIELD_DECIMAL_PLACES) {
			return this._nachkommaStellenDisp;
		}
		if (key === IMetaKeys.FIELD_INTEGER_DIGIT) {
			return this._vorkommaStellenDisp;
		}
		if (key === IMetaKeys.FIELD_MAX_DECIMAL_PLACES) {
			return this._nachkommaStellenBisDisp;
		}
		if (key === IMetaKeys.FIELD_ZERO_ALLOWED) {
			return this._nullErlaubt;
		}
		if (key === IMetaKeys.FIELD_LEADING_ZEROS) {
			return this._fuehrendeNullenErlaubt;
		}
		if (key === IMetaKeys.FIELD_SIGN) {
			return this._hatVorzeichen;
		}

		return undefined;
	}

	public convertToBasicType(
		value: string,
		metaData: IMetaDataValidierung
	): number | string | null {
		const internalDataType = ConversionUtils.createNumberConversionConfig(
			this._conversionConfig,
			metaData.getDezimalTrenner(),
			false
		);
		const conversionResult = NumberConversion.convertStringToNumber(value, internalDataType);
		return (typeof conversionResult === "number" || typeof conversionResult === "string") ? conversionResult : null;
	}

	public convertFromBasicType(
		value: SupportedType,
		metaData: IMetaDataValidierung
	): string | null {
		const dataFormats = {
			decimalSeparator: metaData.getDezimalTrenner(),
			decimalPlacesOptional: false
		};
		const internalDataType = this._conversionConfig;
		return defaultValueConversion(dataFormats).formatValue(value, internalDataType) ?? null;
	}

	getConversionConfig(): NumberConversionConfig {
		return this._conversionConfig;
	}
}
