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
    Localizable,
    LocalizableArgs,
    SupportedType,
    ValueConversionConfig
} from "@com.mgmtp.a12.utils/utils-localization";
import {
    defaultValueConversion
} from "@com.mgmtp.a12.utils/utils-localization";


import { RuntimeFormalErrorEnum } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import type { Pair } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import type { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { IFormaleFehlerConstants } from "../util/IFormaleFehlerConstants.js";
import type { LocalizableDefaults } from "../util/LocalizableUtil.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";
import { RuntimeUtils } from "../util/RuntimeUtils.js";
import { ValidType } from "../util/ValidType.js";

import { FehlerHandler } from "./error/FehlerHandler.js";
import type { IFormatDefinition } from "./IFormatDefinition.js";
import { FeldFormatEnumType } from "./IFormatDefinition.js";
import type { IValidierungParam } from "./IValidierungParam.js";

/**
 * Klasse modelliert zur Laufzeit ein String-Feld.
 */
export class FormatDefinitionString implements IFormatDefinition {

    private _pattern: RegExp | undefined;
    private _feldTyp: string;
    private _muster: string | undefined;
    private _musterDisp: string | undefined;
    private _fehlertexte: Map<string, string> = new Map<string, string>();

    private _minLaenge: number;
    private _maxLaenge: number;
    private _minLaengePrintable = -1;
    private _maxLaengePrintable = -1;

    private _lineBreakAllowed: boolean;
    private _lineBreakAllowedDisp: boolean | undefined;
    private _noValueValidation: boolean;
    private _enumWerte: string[] = new Array<string>();
    private _values: Map<string, string[]> = new Map<string, string[]>();

    private _enumOberflaechenWerte: Map<string, Pair<string, string>[]> = new Map<
        string,
        Pair<string, string>[]
    >();

    private _enumXML2OflMap: Map<string, Map<string, string>> = new Map<
        string,
        Map<string, string>
    >();
    private _enumOfl2XMLMap: Map<string, Map<string, string>> = new Map<
        string,
        Map<string, string>
    >();
    private _enumCategoryValues: Map<string, Map<string, string>> = new Map<
        string,
        Map<string, string>
    >();
    private _sValue = "";

    private _conversionConfig: ValueConversionConfig;

    constructor( // constructor 1
        minLaenge: number,
        maxLaenge: number,
        minLaengePrintable: number,
        maxLaengePrintable: number,
        muster: string,
        fehlertexte: Map<string, string>,
        lba: boolean,
        nvv: boolean,
        hintLists: Map<string, string[]>
    );
    constructor( // constructor 2
        minLaenge: number,
        maxLaenge: number,
        muster: string,
        fehlertexte: Map<string, string>,
        lba: boolean,
        nvv: boolean
    );
    constructor( // constructor 3
        fehlertexte: Map<string, string>,
        xmlWerte: string[],
        oberflaechenWerte: Map<string, Pair<string, string>[]>,
        categoryValues: Map<string, Map<string, string>>,
        sValue: string
    );
    constructor(maxLaenge: number, hintLists: Map<string, string[]>); // constructor 4
    constructor(
        minLaengeOrMaxLaengeOrFehlertexte: number | Map<string, string>,
        maxLaengeOrXmlWerteOrWerteOrHintLists: number | string[] | Map<string, string[]>,
        minLaengePrintableOrMusterOrOberflaechenWerte?: number | string | Map<string, Pair<string, string>[]>,
        maxLaengePrintableOrFehlertexteOrCategoryValues?:
            | number
            | Map<string, string>
            | Map<string, Map<string, string>>,
        musterOrLbaOrSValue?: string | boolean,
        fehlertexteOrNvv?: Map<string, string> | boolean,
        lba?: boolean,
        nvv?: boolean,
        hintLists?: Map<string, string[]>
    ) {
        if (lba != undefined) {
            // constructor 1
            this._minLaenge = <number>minLaengeOrMaxLaengeOrFehlertexte;
            this._maxLaenge = <number>maxLaengeOrXmlWerteOrWerteOrHintLists;
            this._minLaengePrintable = <number>minLaengePrintableOrMusterOrOberflaechenWerte;
            this._maxLaengePrintable = <number>maxLaengePrintableOrFehlertexteOrCategoryValues;
            this._muster = <string>musterOrLbaOrSValue;
            this._musterDisp = <string>musterOrLbaOrSValue;
            this._fehlertexte =
                fehlertexteOrNvv instanceof Map && fehlertexteOrNvv != undefined
                    ? fehlertexteOrNvv
                    : new Map<string, string>();
            this._lineBreakAllowed = lba;
            this._lineBreakAllowedDisp = lba;
            this._noValueValidation = nvv || false;
            this._feldTyp = FeldFormatEnumType.ER_STRING.toString();
            if (this._muster.length > 0) {
                this._pattern = new RegExp("^(?:" + this._muster + ")$");
            } else {
                this._musterDisp = undefined;
            }
            if (this._minLaenge === -1) {
                this._minLaenge = 1;
            }
            this._values = hintLists instanceof Map && hintLists != undefined ? hintLists
                : new Map<string, string[]>();
        } else if (
            minLaengePrintableOrMusterOrOberflaechenWerte != undefined &&
            typeof minLaengePrintableOrMusterOrOberflaechenWerte === "string"
        ) {
            // constructor 2
            this._minLaenge = <number>minLaengeOrMaxLaengeOrFehlertexte;
            this._maxLaenge = <number>maxLaengeOrXmlWerteOrWerteOrHintLists;
            this._muster = minLaengePrintableOrMusterOrOberflaechenWerte;
            this._musterDisp = this._muster;
            this._fehlertexte = <Map<string, string>>maxLaengePrintableOrFehlertexteOrCategoryValues;
            this._lineBreakAllowed = <boolean>musterOrLbaOrSValue;
            this._lineBreakAllowedDisp = <boolean>musterOrLbaOrSValue;
            this._noValueValidation = <boolean>fehlertexteOrNvv || false;
            this._feldTyp = FeldFormatEnumType.ER_STRING.toString();

            if (this._muster) {
                this._pattern = new RegExp("^(?:" + this._muster + ")$");
            } else {
                this._musterDisp = undefined;
            }
        } else if (
            minLaengeOrMaxLaengeOrFehlertexte instanceof Map ||
            maxLaengePrintableOrFehlertexteOrCategoryValues instanceof Map ||
            (typeof minLaengePrintableOrMusterOrOberflaechenWerte !== "number" &&
                typeof minLaengeOrMaxLaengeOrFehlertexte !== "number")
        ) {
            // constructor 3
            this._minLaenge = 1;
            this._maxLaenge = -1;
            this._muster = "";
            this._musterDisp = undefined;
            this._lineBreakAllowed = false;
            this._lineBreakAllowedDisp = undefined;
            this._noValueValidation = false;
            this._fehlertexte =
                <Map<string, string>>minLaengeOrMaxLaengeOrFehlertexte != undefined
                    ? <Map<string, string>>minLaengeOrMaxLaengeOrFehlertexte
                    : new Map<string, string>();
            this._feldTyp = FeldFormatEnumType.ER_ENUMERATION.toString();

            for (const wert of <string[]>maxLaengeOrXmlWerteOrWerteOrHintLists) {
                this._enumWerte.push(wert);
            }

            const oflWerte: Map<string, Pair<string, string>[]> = new Map<
                string,
                Pair<string, string>[]
            >();
            if (minLaengePrintableOrMusterOrOberflaechenWerte != undefined) {
                (<Map<string, Pair<string, string>[]>>(
                    minLaengePrintableOrMusterOrOberflaechenWerte
                )).forEach((value: Pair<string, string>[], key: string) => {
                    oflWerte.set(key, value);
                });
            }
            this._enumOberflaechenWerte = oflWerte;
            for (const [key, value] of this._enumOberflaechenWerte) {
                const _xml2OflMap: Map<string, string> = new Map<string, string>();
                const _ofl2XMLMap: Map<string, string> = new Map<string, string>();
                for (const oflXml of value) {
                    _xml2OflMap.set(oflXml.entry2, oflXml.entry1);
                    _ofl2XMLMap.set(oflXml.entry1, oflXml.entry2);
                }
                this._enumXML2OflMap.set(key, _xml2OflMap);
                this._enumOfl2XMLMap.set(key, _ofl2XMLMap);
            }
            const catValues: Map<string, Map<string, string>> = new Map<string, Map<string, string>>();
            if (maxLaengePrintableOrFehlertexteOrCategoryValues != undefined) {
                (<Map<string, Map<string, string>>>maxLaengePrintableOrFehlertexteOrCategoryValues).forEach(
                    (value: Map<string, string>, key: string) => {
                        catValues.set(key, value);
                    }
                );
            }
            this._enumCategoryValues = catValues;
            this._sValue = <string>musterOrLbaOrSValue;
        } else {
            // constructor 4
            this._minLaenge = 1;
            this._maxLaenge = minLaengeOrMaxLaengeOrFehlertexte;
            this._muster = "";
            this._musterDisp = undefined;
            this._lineBreakAllowed = false;
            this._lineBreakAllowedDisp = undefined;
            this._noValueValidation = false;
            this._feldTyp = FeldFormatEnumType.ER_ENUMERATION_NICHT_ABGESCHLOSSEN.toString();
            const hintListValues: Map<string, string[]> = new Map<string, string[]>();
            if (maxLaengeOrXmlWerteOrWerteOrHintLists instanceof Map) {
                maxLaengeOrXmlWerteOrWerteOrHintLists.forEach((value: string[], key: string) => {
                    hintListValues.set(key, value)
                })
            }
            this._values = hintListValues;
        }
        if (this._feldTyp == "String" || this._feldTyp == "NichtAbgeschlosseneEnumeration") {
            this._conversionConfig = {
                type: "StringType"
            }
        } else {
            this._conversionConfig = {
                type: "EnumerationType"
            }
        }
    }

    public toString(): string {
        let str = "";
        str += RuntimeUtils.getSimpleNameFromClass(this);
        if (this._minLaenge > 0) {
            str += "\nMinLaenge ";
            str += this._minLaenge;
        }
        if (this._maxLaenge > 0) {
            str += "\nMaxLaenge ";
            str += this._maxLaenge;
        }
        if (this._muster) {
            str += "\nMuster ";
            str += this._muster;
        }
        if (this._lineBreakAllowed) {
            str += "\nZeilenumbrüche erlaubt. ";
        }
        if (this._noValueValidation) {
            str += "\nKeine Werteprüfung. ";
        }
        if (this._fehlertexte.size > 0) {
            str += "\nFehlertext ";
            const mehrsprachigerFehlertext: string[] = new Array<string>();
            for (const [sprache, fehlertext] of this._fehlertexte) {
                mehrsprachigerFehlertext.push(sprache + " " + fehlertext);
            }
            str += mehrsprachigerFehlertext.join(", ");
        }
        return str.toString();
    }

    /**
     * allgemeine Methode zum Validieren des Eingabewertes.
     *
     */
    public validiereFormat(
        param: IValidierungParam,
        fullCheck: boolean
    ): ValidType {
        let wert: string = param.getWert();

        if (this.isNoValueValidation) {
            return ValidType.Valid;
        }
        if (!fullCheck) {
            return ValidType.Valid;
        }

        if (!this._lineBreakAllowed && (wert.indexOf("\n") > -1 || wert.indexOf("\r") > -1)) {
            const fehlerId = RuntimeFormalErrorEnum.STRING_ENTHAELT_NEWLINE;
            const errorText = LocalizableUtil.createLocalizableFormalError(fehlerId);
            return FehlerHandler.addFehler(
                IFormaleFehlerConstants.FEHLER_STRING_MIT_NEWLINE,
                [errorText],
                param
            );
        }

        if (wert && wert !== "") {
            // Normalisierung durchführen,
            wert = RuntimeUtils.normalizeLinebreaks(wert);
        }

        if (this._pattern != undefined) {
            if (!this._pattern.test(wert)) {
                const key = LocalizableUtil.createFieldErrorKey(param.getIdentifier().getName(), param.getMetaData().getModelId());
                const args = FormatDefinitionString._getLocalizableArgs(param);
                const defaults = this.getFehlertexte();
                const fehler: Localizable = {
                    key,
                    args,
                    defaults
                }
                return FehlerHandler.addFehler(
                    IFormaleFehlerConstants.FEHLER_STRING_FALSCHES_MUSTER,
                    [fehler],
                    param
                );
            }
        } else if (
            this._enumWerte.length > 0 &&
            this._feldTyp !== FeldFormatEnumType.ER_ENUMERATION_NICHT_ABGESCHLOSSEN.toString()
            // falls nicht abgeschlossene enum darf nicht geprüft werden, ob Wert in enum enthalten ist
        ) {
            let match = false;
            if (this._enumWerte.indexOf(wert) !== -1) {
                match = true;
            }
            if (!match) {
                const key1 = LocalizableUtil.createFieldErrorKey(param.getIdentifier().getName(), param.getMetaData().getModelId());
                const args = FormatDefinitionString._getLocalizableArgs(param);
                const defaults1 = this.getFehlertexte();
                const error: Localizable = { key: key1, args, defaults: defaults1 };
                const key2 = LocalizableUtil.createFormalErrorKey(RuntimeFormalErrorEnum.ENUMERATION_DEFAULT_ERROR);
                const defaults2 = FehlerHandler.getAllErrorTexts(RuntimeFormalErrorEnum.ENUMERATION_DEFAULT_ERROR);
                const fallbackError: Localizable = { key: key2, args, defaults: defaults2 };
                return FehlerHandler.addFehler(
                    IFormaleFehlerConstants.FEHLER_STRING_FALSCHES_MUSTER,
                    [error, fallbackError],
                    param,
                );
            }
        }

        const wertLength: number = wert.length;
        if (this._minLaenge > 0 && wertLength < this._minLaenge) {
            const errorArgs: LocalizableArgs = {
                minLength: LocalizableUtil.createPlainPlaceholder(String(this._minLaenge))
            }
            const fehlerId = RuntimeFormalErrorEnum.STRING_ZU_KURZ;
            const errorText = LocalizableUtil.createLocalizableFormalError(
                fehlerId,
                errorArgs
            );
            return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_STRING_ZU_KURZ, [errorText], param);
        }

        if (this._maxLaenge > 0 && wertLength > this._maxLaenge) {
            const errorArgs: LocalizableArgs = {
                maxLength: LocalizableUtil.createPlainPlaceholder(String(this._maxLaenge))
            }
            const fehlerId = RuntimeFormalErrorEnum.STRING_ZU_LANG;
            const errorText = LocalizableUtil.createLocalizableFormalError(
                fehlerId,
                errorArgs
            );
            return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_STRING_ZU_LANG, [errorText], param);
        }

        return ValidType.Valid;
    }

    private static _getLocalizableArgs(
        param: IValidierungParam
    ): LocalizableArgs {
        return LocalizableUtil.createLocalizableArgs4FieldErrorMsg(
            param.getMetaData(),
            param.getIdentifier(),
            param.getWert()
        );
    }

    public convert(
        wert: string,
        // it is ok to ignore the following three parameters, since they are define in the interface and we cannot remove them
        _darstellungsInfo: DarstellungsInformationIntern,
        _vonXml: boolean,
        _customFieldTypeFactory: ICustomFieldTypeFactory | undefined,
    ): string {
        return wert;
    }

    public getEnumOberflaechenWerte(locale: string): Pair<string, string>[] | undefined {
        return this._enumOberflaechenWerte.get(locale);
    }

    public getHintListValues(sprache: string): string[] | undefined {
        return this._values.get(sprache);
    }

    public getEnumCategoryValues(xmlValue: string): Map<string, string> | undefined {
        return this._enumCategoryValues.get(xmlValue);
    }

    public getAllEnumUIValues(xmlValue: string): LocalizableDefaults {
        const result: { [locale: string]: string | undefined } = {};
        this._enumXML2OflMap.forEach((values, lang) => {
            const uiValue = values.get(xmlValue);
            if (uiValue) { result[lang] = uiValue; }
        });
        return result;
    }

    public getDefaultValue(

        _metaData: IMetaDataValidierung,

        _dataFormats: DataFormats
    ): string {
        return "";
    }

    public get minLaengePrintable(): number {
        return this._minLaengePrintable;
    }

    public get maxLaengePrintable(): any {
        return this._maxLaengePrintable;
    }

    public getWert(key: string): any {
        if (key === IMetaKeys.FIELD_MIN_LENGTH) {
            return this._minLaenge;
        }
        if (key === IMetaKeys.FIELD_MAX_LENGTH) {
            return this._maxLaenge;
        }
        if (key === IMetaKeys.FIELD_FIELD_TYPE) {
            return this._feldTyp;
        }
        if (key === IMetaKeys.FIELD_PATTERN) {
            return this._musterDisp;
        }
        if (key === IMetaKeys.FIELD_WORD_WRAP) {
            return this._lineBreakAllowedDisp;
        }
        if (key === IMetaKeys.FIELD_ENUMERATION_VALUES) {
            if (this._values != undefined && this._values.size > 0 && this._values.get("de_DE") !== undefined) {
                // wird kein Parameter übergeben, so werden die deutschen Werte zurückgegeben
                const hintListValues: string[] = new Array<string>();
                // Locale.GERMANY ExtMP fallback

                // we know that the key exists, otherwise we would not be in this branch
                /* eslint-disable-next-line @typescript-eslint/no-non-null-assertion */
                for (const wert of this._values.get("de_DE")!) {
                    hintListValues.push(wert);
                }
                return hintListValues;
            }
            return this._enumWerte;
        }
        if (
            key === IMetaKeys.FIELD_ENUMERATION_UI_VALUES &&
            this._enumOberflaechenWerte.size > 0
        ) {
            // wird kein Parameter übergeben, so werden die deutschen Werte zurückgegeben (ohne Zuordnung zum XML-Wert)
            const oberflaechenWerte: string[] = new Array<string>();
            // Locale.GERMANY ExtMP fallback
            // TODO: Fix this the next time the file is edited.
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            for (const oflXmlWert of this._enumOberflaechenWerte.get("de_DE")!) {
                oberflaechenWerte.push(oflXmlWert.entry1);
            }
            return oberflaechenWerte;
        }
        if (key === IMetaKeys.FIELD_ENUMERATION_CATEGORY_VALUES) {
            return this._enumCategoryValues;
        }
        if (key === IMetaKeys.FIELD_NO_VALUE_VALIDATION) {
            return this._noValueValidation;
        }
        if (key === IMetaKeys.FIELD_S_VALUE) {
            return this._sValue;
        }
        return undefined;
    }

    public get fehlertexte(): Map<string, string> {
        return this._fehlertexte;
    }

    public get isNoValueValidation(): boolean {
        return this._noValueValidation;
    }

    public compare(s1: string, s2: string): number {
        if (
            this._enumWerte.length > 0 &&
            this._feldTyp !== FeldFormatEnumType.ER_ENUMERATION_NICHT_ABGESCHLOSSEN.toString()
        ) {
            // enum fields are sorted according to the order of the xml values
            return this._enumWerte.indexOf(s1) - this._enumWerte.indexOf(s2);
        } else {
            // string and extensible enums are alphabetically sorted
            return s1.toLocaleLowerCase().localeCompare(s2.toLocaleLowerCase());
        }
    }

    public convertToBasicType(
        internalValue: string,
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        metaData: IMetaDataValidierung
    ): string | null {
        const dataType = this._conversionConfig;
        const conversionResult = defaultValueConversion().parseValue(internalValue, dataType);
        return typeof conversionResult.value === "string" ? conversionResult.value : null;
    }

    public convertFromBasicType(
        value: SupportedType,
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        metaData: IMetaDataValidierung
    ): string | null {
        const dataType = this._conversionConfig;
        return defaultValueConversion().formatValue(value, dataType) ?? null;
    }

    getConversionConfig(): ValueConversionConfig {
        return this._conversionConfig;
    }

    public getFehlertexte(): LocalizableDefaults {
        const result: { [locale: string]: string | undefined } = {};
        this.fehlertexte.forEach((ft, lang) => {
            result[lang] = !ft ? FehlerHandler.getSingleErrorText(RuntimeFormalErrorEnum.ENUMERATION_DEFAULT_ERROR, lang) : ft
        });
        return result;
    }

    public getFieldErrorText(locale: string): string | undefined {
        const fehlertext: string | undefined = this.fehlertexte.get(locale);
        if (fehlertext !== undefined) {
            return fehlertext;
        }
        return FehlerHandler.getSingleErrorText(
            RuntimeFormalErrorEnum.ENUMERATION_DEFAULT_ERROR,
            locale
        );
    }
}
