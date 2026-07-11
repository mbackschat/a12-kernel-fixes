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

import type { Logger } from "@com.mgmtp.a12.utils/utils-logging";

import type { DataFormats, SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { IData, IResult, Pair, ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { HashSet, IIdentifier, IMetaKeys, LocalizableError, stringconv } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import type {
    IMetaFieldInternalTmpA12K2645
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/internal";

import { DynamicEnumerationCommand } from "../core/DynamicEnumerationCommand.js";
import type { Feld_t } from "../formatdef/Feld_t.js";
import { FormatDefinitionDatumBereich } from "../formatdef/FormatDefinitionDatumBereich.js";
import { FormatDefinitionString } from "../formatdef/FormatDefinitionString.js";
import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import { ValidierungParam } from "../formatdef/ValidierungParam.js";
import { Constants } from "../util/Constants.js";
import { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { ErgebnisImpl } from "../util/ErgebnisImpl.js";
import { IMetaKeysInternal } from "../util/IMetaKeysInternal.js";
import { LocaleUtil } from "../util/LocaleUtil.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";
import { LoggerDummy } from "../util/LoggerDummy.js";
import { RtExternalIdentifier } from "../util/RtExternalIdentifier.js";
import { ValidType } from "../util/ValidType.js";

import { FormatDefinitionDatum } from "../formatdef/FormatDefinitionDatum.js";
import { FormatDefinitionVordefiniert } from "../formatdef/FormatDefinitionVordefiniert.js";
import { ZahlArt } from "../formatdef/FormatDefinitionZahl.js";

import type { MetaContainer } from "./MetaContainer.js";
import { MetaEntity } from "./MetaEntity.js";
import type { MetaModelImpl } from "./MetaModelImpl.js";

export class MetaFeld extends MetaEntity implements IMetaFieldInternalTmpA12K2645, IMetaKeysInternal {
    // Use dummy like in c++, if no logger is provided.
    private static _LOG: Logger = new LoggerDummy();
    private static _schluessel: Set<string> = new Set<string>();
    private static _internalKeys: Set<string> = new Set<string>();

    // Should be package private but can't
    public _operandenMengeServer: HashSet<RtExternalIdentifier> = new HashSet<RtExternalIdentifier>();
    public _zielMengeFuerBerechnungServer: HashSet<RtExternalIdentifier> =
        new HashSet<RtExternalIdentifier>();
    public operandSetDynamicEnum: HashSet<RtExternalIdentifier> = new HashSet<RtExternalIdentifier>();
    public _errorTexts4Req: Map<string, string>;

    private _formatDef: IFormatDefinition;
    private _metaModel: MetaModelImpl;

    /**
     * Don't use this. This is just for a static initialization.
     */
    static _constructor = (() => {
        MetaFeld._schluessel.add(IMetaKeys.FIELD_SHORT_NAME);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_FULLNAME);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_MANDATORY_FIELD);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_MIN_LENGTH);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_MAX_LENGTH);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_FIELD_TYPE);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_PATTERN);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_ERROR_MESSAGE);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_WORD_WRAP);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_SIGN);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_DECIMAL_PLACES);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_MAX_DECIMAL_PLACES);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_INTEGER_DIGIT);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_ZERO_ALLOWED);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_VALUE_FALSE);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_LEADING_ZEROS);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_FORMAT);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_VALUE_TRUE);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_DECIMAL_SEPARATOR);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_ENUMERATION_VALUES);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_ROOT_GROUP_MANDATORY_FIELD);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_ENUMERATION_UI_VALUES);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_OPERANDS_SET);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_TARGET_SET_FOR_CALC);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_S_VALUE);

        MetaFeld._schluessel.add(IMetaKeys.FIELD_ENUMERATION_CATEGORY_VALUES);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_NO_VALUE_VALIDATION);

        MetaFeld._schluessel.add(IMetaKeys.FIELD_DYNAMIC_ENUM_VALUES);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_SOURCE_FIELDS_FOR_DYNAMIC_ENUM);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_DYNAMIC_ENUMERATION);

        MetaFeld._schluessel.add(IMetaKeys.FIELD_MANDATORY_GLOBAL_OR_IN_SURROUNDING_REPEATABLE_GROUP);

        MetaFeld._schluessel.add(IMetaKeys.FIELD_META_FIELD_DATE_OF_DATE_RANGE);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_ANNOTATION);
        MetaFeld._schluessel.add(IMetaKeys.FIELD_CUSTOM_ERROR_MESSAGE_FOR_REQUIRED);

        MetaFeld._internalKeys.add(IMetaKeysInternal.FELD_INTERNENR);
        MetaFeld._internalKeys.add(IMetaKeysInternal.FELD_ZUSATZPRUEFUNG);
        MetaFeld._internalKeys.add(IMetaKeysInternal.FELD_UNIQUENAME);
        MetaFeld._internalKeys.add(IMetaKeysInternal.FELD_MAXINDICES);
    })();

    constructor(
        metaModel: MetaModelImpl,
        f: Feld_t,
        dezimalTrenner: string,
        formatDefinition?: IFormatDefinition
    ) {
        super(MetaFeld._schluessel, MetaFeld._internalKeys);
        if (formatDefinition !== undefined) {
            this._formatDef = formatDefinition;
        } else {
            this._formatDef = f.formatDefintion;
        }
        this._errorTexts4Req = f.errorTexts4Req as any;
        this._metaModel = metaModel;
        const path: string = f.pfadName;
        this.addWert(IMetaKeysInternal.FELD_INTERNENR, f.interneNr);
        this.addWert(IMetaKeys.FIELD_SHORT_NAME, path.substring(path.lastIndexOf("/") + 1));
        this.addWert(IMetaKeys.FIELD_FULLNAME, path);
        this.addWert(IMetaKeys.FIELD_DECIMAL_SEPARATOR, String(dezimalTrenner));
        this.addWert(IMetaKeys.FIELD_MANDATORY_FIELD, f.pflichtFeldLfdNr);
        this.addWert(
            IMetaKeys.FIELD_MANDATORY_GLOBAL_OR_IN_SURROUNDING_REPEATABLE_GROUP,
            f.isMandatoryForRepeatableGroup || f.isPflichtFeld
        );
        this.addWert(IMetaKeys.FIELD_ROOT_GROUP_MANDATORY_FIELD, f.vordruckPflichtFeldLfdNr);
        this.addWert(IMetaKeysInternal.FELD_UNIQUENAME, f.uniqueName);

        // Will be reset in MetaModelImpl#_createRegel if necessary
        this.addWert(IMetaKeys.FIELD_DYNAMIC_ENUMERATION, false);
        if (this._formatDef instanceof FormatDefinitionDatumBereich) {
            const fdd = new FormatDefinitionDatum(
                this._formatDef.format,
                this._formatDef.isZusatzPruefung,
                this._formatDef.teilbekanntArt
            );
            this.addWert(
                IMetaKeys.FIELD_META_FIELD_DATE_OF_DATE_RANGE,
                new MetaFeld(metaModel, f, dezimalTrenner, fdd)
            );
        } else {
            this.addWert(IMetaKeys.FIELD_META_FIELD_DATE_OF_DATE_RANGE, null);
        }

        // "Annotation" is added to super.metadaten. This makes sure that super.pruefeSchluessel works fine. If any annotation is
        // provided in additional values, the 'null' value is overwritten.
        this.addWert(IMetaKeys.FIELD_ANNOTATION, undefined);

        const internalKeys: Set<string> = f.internalAdditionalKeys
        for (const key of f.additionalKeys) {
            if (internalKeys.has(key)) {
                this.addAdditionalInternalKey(key);
            } else {
                this.addAdditionalKey(key);
            }
            this.addWert(key, f.getAdditionalValue(key));
        }
    }

    setMaxIndices(metaModel: MetaModelImpl): void {
        const maxIndices: number[] = this._getMaxIndices(metaModel);
        this.addWert(IMetaKeysInternal.FELD_MAXINDICES, maxIndices);
    }

    private _getMaxIndices(meta: MetaModelImpl): number[] {
        const result: number[] = new Array<number>();

        const path = String(this.getValue(IMetaKeys.FIELD_FULLNAME));
        let endIndex: number = path.indexOf("/", 1);
        while (endIndex !== -1) {
            const aktPfad: string = path.substring(0, endIndex);
            const metaCont: MetaContainer = meta.getValue(IMetaKeys.MODEL_META_GROUP, aktPfad);
            const maxIndex: number = metaCont.getValue(IMetaKeys.GROUP_MAX_INDEX);
            if (maxIndex != undefined) {
                result.push(maxIndex);
            }
            endIndex = path.indexOf("/", endIndex + 1);
        }
        // der max. Zeilenindex fehlt noch
        result.push(1);
        return result;
    }

    public getTopContainer(): MetaContainer | undefined {
        let result: MetaContainer | undefined;
        let topContainerName = "";

        const path: string = this.getValue(IMetaKeys.FIELD_FULLNAME);
        const endIndex: number = path.indexOf("/", 1);
        if (endIndex !== -1) {
            topContainerName = path.substring(0, endIndex);
        } else {
            topContainerName = path;
        }
        if (topContainerName.length !== 0) {
            result = this._metaModel.getValue(
                IMetaKeys.MODEL_META_GROUP,
                topContainerName
            );
        }
        return result;
    }

    public equals(other: any): boolean {
        if (this === other) {
            return true;
        }
        if (!(other instanceof MetaFeld)) {
            return false;
        }

        // der erste Parameter reicht für die Identifikation
        return this.getValue(IMetaKeys.FIELD_FULLNAME) === other.getValue(IMetaKeys.FIELD_FULLNAME);
    }

    public hashCode(): number {
        return stringconv(<string>this.getValue(IMetaKeys.FIELD_FULLNAME));
    }

    // is been used instead of javas equals method
    public toString(): string {
        return this.getValue(IMetaKeys.FIELD_FULLNAME);
    }

    public convert(
        data: string,
        darstellungsInfo: DataFormats,
        vonXml: boolean,
        customFieldTypeFactory?: ICustomFieldTypeFactory,
    ): string | undefined {
        if (data == undefined) {
            return undefined;
        }
        const erg = new ErgebnisImpl();
        const conversionResult = this._convertInternal(
            data,
            darstellungsInfo,
            vonXml,
            true, // fullCheck
            true, // nonLenient
            erg,
            customFieldTypeFactory,
        );
        if (erg.fehlerText != undefined) {
            throw new LocalizableError(erg.fehlerText);
        }
        return conversionResult ?? "";
    }

    public convertWithOnlyBasicTypeCheck(
        value: string,
        dataFormats: DataFormats | null,
        result: IResult,
        customFieldTypeFactory?: ICustomFieldTypeFactory,
    ): SupportedType {
        // in both cases ('null' and 'undefined') we return 'null'
        if (value == null) {
            return null;
        }
        const internalValue: string | null = this._convertInternal(
            value,
            dataFormats,
            false, // fromXml
            false, // fullCheck
            false, // nonLenient
            result,
            customFieldTypeFactory,
        );

        return this._formatDef.convertToBasicType(internalValue, this._metaModel.getMetaDataValidierung());
    }

    public convertFromBasicType(
        value: SupportedType,
        dataFormats: DataFormats | null,
        result: IResult,
        customFieldTypeFactory?: ICustomFieldTypeFactory,
    ): string | null {
        if (value == null) {
            // an empty value will be converted to null, except for string-, enumeration- and predefined types
            // that's why the conversion of an empty value is type specific
            return null;
        }
        const internalValue = this._formatDef.convertFromBasicType(value, this._metaModel.getMetaDataValidierung());
        if (internalValue == null) {
            return null;
        }

        return this._convertInternal(
            internalValue,
            dataFormats,
            true, // fromXml
            false, // fullCheck
            false, // nonLenient
            result,
            customFieldTypeFactory,
        );
    }

    private _convertInternal(
        data: string,
        darstellungsInfo: DataFormats | null,
        vonXml: boolean,
        fullCheck: boolean,
        nonLenient: boolean,
        result: IResult,
        customFieldTypeFactory: ICustomFieldTypeFactory | undefined,
    ): string | null {
        if (this._formatDef instanceof FormatDefinitionString && this._formatDef.isNoValueValidation) {
            // No checks nor transformations for noValueValidation
            return data;
        }

        // validate before conversion
        const fieldFullName: string = this.getValue(IMetaKeys.FIELD_FULLNAME);
        const param = new ValidierungParam(
            data,
            new RtExternalIdentifier(fieldFullName, [1, 1]),
            result,
            false, // nurPruefen
            this._getDarstellungsInformationIntern(vonXml ? null : darstellungsInfo),
            nonLenient,
            this._metaModel.getMetaDataValidierung(),
            customFieldTypeFactory,
        );
        if (this._formatDef.validiereFormat(param, fullCheck) === ValidType.Invalid) {
            return null;
        }

        // actual conversion
        const di = this._getDarstellungsInformationIntern(darstellungsInfo);
        return this._formatDef.convert(data, di, vonXml, customFieldTypeFactory);
    }

    private _getLaengeMitDarstellungsInfo(
        info: DataFormats,
        key: string,
    ): number {
        const di = this._getDarstellungsInformationIntern(info);
        const typ: string = this.getValue(IMetaKeys.FIELD_FIELD_TYPE);

        if (typ === "Ja") {
            let wahrWert: string | undefined = di.wahrWert;
            if (!wahrWert) {
                // Nehme XMl Darstellung, wenn in DarstellungsInfo nicht gesetzt
                wahrWert = <string>this.getValue(IMetaKeys.FIELD_VALUE_TRUE);
            }
            return wahrWert.length;
        }
        if (typ === "JaNein") {
            let wahrWert: string | undefined = di.wahrWert;
            let falschWert: string | undefined = di.falschWert;
            if (!wahrWert) {
                // Nehme XMl Darstellung, wenn in DarstellungsInfo nicht gesetzt
                wahrWert = <string>this.getValue(IMetaKeys.FIELD_VALUE_TRUE);
            }
            if (!falschWert) {
                // Nehme XMl Darstellung, wenn in DarstellungsInfo nicht gesetzt
                falschWert = <string>this.getValue(IMetaKeys.FIELD_VALUE_FALSE);
            }
            if (key === IMetaKeys.FIELD_MAX_LENGTH) {
                return Math.max(wahrWert.length, falschWert.length);
            } else {
                return Math.min(wahrWert.length, falschWert.length);
            }
        }

        if (typ === "DatumBereich") {
            const db: FormatDefinitionDatumBereich = <FormatDefinitionDatumBereich>this._formatDef;
            const xmlFormat: string = db.format.replace(/TT/g, "dd").replace(/J/g, "y");
            const xmlDatumBereichTrenner: string = db.trenner;
            if (IMetaKeys.FIELD_MAX_LENGTH === key) {
                return di.getMaxLaengeDatumBereich(xmlFormat, xmlDatumBereichTrenner);
            } else {
                return di.getMinLaengeDatumBereich(xmlFormat, xmlDatumBereichTrenner);
            }
        }
        if (typ === "Datum") {
            const xmlFormat: string = (<string>this.getValue(IMetaKeys.FIELD_FORMAT))
                .replace(/TT/g, "dd")
                .replace(/J/g, "y");

            if (IMetaKeys.FIELD_MAX_LENGTH === key) {
                return di.getMaxLaengeDatum(xmlFormat);
            } else {
                return di.getMinLaengeDatum(xmlFormat);
            }
        }

        if (this.isNumber(typ)) {
            const trait: string = this.isAmount(typ) ? ZahlArt.GELDBETRAG.toString() : ZahlArt.ZAHL.toString();
            const thousandsSeparatorForTrait = di.getThousandsSeparatorForTrait(trait);
            if (IMetaKeys.FIELD_MAX_LENGTH === key && thousandsSeparatorForTrait) {
                const xmlMaxLength: number = (<number>this.getValue(key));
                const maxIntegerDigits: number | undefined = (<number | undefined>this.getValue(IMetaKeys.FIELD_INTEGER_DIGIT));
                if (xmlMaxLength > 0 && maxIntegerDigits != undefined && maxIntegerDigits > 0) {
                    // following assertion is fine, since we checked already that thousandsSeparatorForTrait is not empty
                    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                    const thousandsSepLength: number = thousandsSeparatorForTrait!.length;
                    return xmlMaxLength + Math.floor((maxIntegerDigits - 1) / 3) * thousandsSepLength;
                }
                return xmlMaxLength;
            }
        }

        if (this._formatDef instanceof FormatDefinitionVordefiniert) {
            if (IMetaKeys.FIELD_MAX_LENGTH === key && di.doConvertCustomFieldType(typ)) {
                /**
                 * Return the maximum length of the display value of the custom field type. At the meta data interface for a field, the maximum length
                 * of the internal value is returned by default. The length of the display value is only returned, if the user passes
                 * <i>DataFormats</i> and sets parameter <i>convertCustomFieldTypesAll</i> or adds the custom field type to parameter
                 * <i>convertCustomFieldTypesList</i>.
                 * projAlpha retrieves the maximum length at the meta data interface to determine the maximum length of an input field. This function
                 * is currently only implemented for the custom field types <i>IDNr</i> and <i>IBAN</i> where the display value is larger than the internal value.
                 * For all other custom field types the maximum length of the internal value is returned.
                 */
                if (typ === "IBAN" || typ === "IBANinternational") {
                    return 34 + 8;
                } else if (typ === "IDNr") {
                    return 11 + 3;
                }
            }
        }

        return this.getValue(key);
    }

    private isNumber(fieldType: string): boolean {
        return fieldType === "Zahl" || this.isAmount(fieldType);
    }

    private isAmount(fieldType: string): boolean {
        return fieldType === "GeldBetragMitCent" || fieldType === "GeldBetragOhneCent";
    }

    public getVordruckPflichtfeldIntern(): number[] {
        return super.getValue(IMetaKeys.FIELD_ROOT_GROUP_MANDATORY_FIELD);
    }

    public getPflichtfeldIntern(): number[] {
        return super.getValue(IMetaKeys.FIELD_MANDATORY_FIELD);
    }

    private _checkParameterFuerBerechnungsSchluessel(key: string, parameter: any[]): void {
        if (parameter != undefined && parameter.length > 1) {
            throw new Error(
                "For the key '" +
                key +
                "' at most one additional parameter with value '" +
                Constants.SERVER +
                "' may be given."
            );
        }
    }

    public getValue(key: string, ...parameter: any[]): any {
        if (IMetaKeys.FIELD_ANNOTATION === key) {
            return this._getAnnotation(key, parameter);
        }
        if (IMetaKeys.FIELD_OPERANDS_SET === key) {
            this._checkParameterFuerBerechnungsSchluessel(key, parameter);
            return this._operandenMengeServer;
        }
        if (IMetaKeys.FIELD_TARGET_SET_FOR_CALC === key) {
            this._checkParameterFuerBerechnungsSchluessel(key, parameter);
            return this._zielMengeFuerBerechnungServer;
        }
        if (IMetaKeys.FIELD_SOURCE_FIELDS_FOR_DYNAMIC_ENUM === key) {
            if (parameter == undefined || parameter.length < 1) {
                throw new Error("The number of additional parameters must be at least 1.");
            }
            const id: RtExternalIdentifier = parameter[0];
            if (this.getValue(IMetaKeys.FIELD_FULLNAME) !== id.getName()) {
                throw new Error(
                    "The name of the MetaFeld (" +
                    this.getValue(IMetaKeys.FIELD_FULLNAME) +
                    " is not equal to the name of the given identifier (" +
                    id.getName() +
                    ")."
                );
            }
            const result: HashSet<RtExternalIdentifier> = new HashSet<RtExternalIdentifier>();
            for (const op of this.operandSetDynamicEnum) {
                result.add(this._concretizeIndices(op, id));
            }
            return result;
        }
        if (IMetaKeys.FIELD_DYNAMIC_ENUM_VALUES === key) {
            if (parameter == undefined || parameter[2].length < 3) {
                throw new Error("The number of additional parameters must be at least 3.");
            }
            const field: RtExternalIdentifier = parameter[2][0];
            if (this.getValue(IMetaKeys.FIELD_FULLNAME) !== field.getName()) {
                throw new Error(
                    "The name of the MetaFeld (" +
                    this.getValue(IMetaKeys.FIELD_FULLNAME) +
                    " is not equal to the name of the given identifier (" +
                    field.getName() +
                    ")."
                );
            }
            const data: IData = parameter[2][1];
            const mode: ValidationMode = parameter[2][2];

            return this._calculateEnumValues(field, data, mode);
        }

        if (
            key === IMetaKeys.FIELD_ROOT_GROUP_MANDATORY_FIELD ||
            key === IMetaKeys.FIELD_MANDATORY_FIELD
        ) {
            const liste: number[] = super.getValue(key);
            return liste.indexOf(0) !== -1;
        }
        if (IMetaKeys.FIELD_CUSTOM_ERROR_MESSAGE_FOR_REQUIRED == key) {
            if (!this.getValue(IMetaKeys.FIELD_EXPLICITLY_MANDATORY)) {
                return null;
            }
            if (this._errorTexts4Req.size > 0) {
                return LocalizableUtil.defaultsFromMap(this._errorTexts4Req);
            } else {
                return null;
            }
        }
        if (this.informationIsInFeldTyp(key)) {
            if (
                IMetaKeys.FIELD_ENUMERATION_VALUES === key ||
                IMetaKeys.FIELD_ENUMERATION_UI_VALUES === key
            ) {
                const typ: string = this.getValue(IMetaKeys.FIELD_FIELD_TYPE);
                if (
                    IMetaKeys.FIELD_ENUMERATION_UI_VALUES === key &&
                    "Enumeration" === typ &&
                    parameter != undefined &&
                    parameter.length !== 0
                ) {
                    const sprache: string = this._pruefeUndExtrahiereSprachParameter(key, parameter);
                    // Enumeration mit Sprachparameter: es wird eine Liste von Pair (Oberflächen-/XML-Wert) zurückgegeben
                    const wertePaare: Pair<string, string>[] | undefined = (<FormatDefinitionString>(
                        this._formatDef
                    )).getEnumOberflaechenWerte(sprache);
                    if (wertePaare == undefined) {
                        return new Array<Pair<string, string>>();
                    }
                    // kopiere Werte:
                    return wertePaare.slice(0);
                } else if (IMetaKeys.FIELD_ENUMERATION_VALUES === key &&
                    ("NichtAbgeschlosseneEnumeration" === typ || "String" === typ)) {
                    let sprache: string = "de_DE";
                    if (this.checkParameterAngegeben(key, 1, parameter)) {
                        sprache = this._pruefeUndExtrahiereSprachParameter(key, parameter);
                    }
                    const hintListValues: string[] | undefined = (<FormatDefinitionString>(
                        this._formatDef
                    )).getHintListValues(sprache);
                    if (hintListValues != undefined && hintListValues.length !== 0) {
                        return hintListValues.slice(0);
                    }
                }
                const werte: string[] = this._formatDef.getWert(key);
                if (werte == undefined) {
                    return new Array<string>();
                }
                // kopiere Werte:
                return werte.slice(0);
            }
            if (IMetaKeysInternal.FELD_UI_VALUES_FOR_VALUE === key) {
                const typ: string = this.getValue(IMetaKeys.FIELD_FIELD_TYPE);
                if (typ !== IMetaKeys.FIELD_TYPE_ENUMERATION || parameter === undefined || parameter.length === 0) {
                    return undefined;
                }
                return (<FormatDefinitionString>this._formatDef).getAllEnumUIValues(parameter[0]);
            }
            if (IMetaKeys.FIELD_ENUMERATION_CATEGORY_VALUES === key) {
                const typ: string = this.getValue(IMetaKeys.FIELD_FIELD_TYPE);
                if ("Enumeration" === typ) {
                    const categoryValueByXmlValue: Map<string, Map<string, string>> = this._formatDef.getWert(
                        key
                    );
                    if (categoryValueByXmlValue != undefined) {
                        const result: Map<string, Map<string, string>> = new Map<string, Map<string, string>>();
                        // copy value to return
                        for (const [pKey, pValue] of categoryValueByXmlValue) {
                            result.set(pKey, pValue);
                        }
                        return result;
                    }
                }
                return new Map<string, Map<string, string>>();
            }

            // Sonderbehandlung Länge
            if (
                IMetaKeys.FIELD_MAX_LENGTH === key ||
                IMetaKeys.FIELD_MIN_LENGTH === key
            ) {
                const typ: string = this.getValue(IMetaKeys.FIELD_FIELD_TYPE);
                if (typ.startsWith("Datum") || typ.startsWith("Ja") || this.isNumber(typ) || this._formatDef instanceof FormatDefinitionVordefiniert) {
                    if (parameter != undefined && parameter.length > 0) {
                        const o: any = parameter[0];
                        // As in JavaScript there are no types, we don't handle a cast exception here, especially because
                        // we checked the type of o above with instanceof
                        const info: DataFormats = o;
                        return this._getLaengeMitDarstellungsInfo(info, key);
                    }
                }
                // Sonderbehandlung Enumeration
                if ("Enumeration" === typ) {
                    const werte: string[] = this.getValue(IMetaKeys.FIELD_ENUMERATION_VALUES);
                    let laenge: number = werte[0].length;
                    for (let i = 1; i < werte.length; i++) {
                        const actLaenge: number = werte[i].length;
                        if (key === IMetaKeys.FIELD_MIN_LENGTH && laenge > actLaenge) {
                            laenge = actLaenge;
                        }
                        if (key === IMetaKeys.FIELD_MAX_LENGTH && laenge < actLaenge) {
                            laenge = actLaenge;
                        }
                    }
                    return laenge;
                } else if ("String" === typ) {
                    // Sonderbehandlung sonstiger Strings
                    const sf: FormatDefinitionString = <FormatDefinitionString>this._formatDef;
                    if (sf != undefined) {
                        if (key === IMetaKeys.FIELD_MIN_LENGTH) {
                            return sf.minLaengePrintable;
                        } else {
                            return sf.maxLaengePrintable;
                        }
                    }
                }
            }

            if (IMetaKeys.FIELD_ERROR_MESSAGE === key) {
                const typ: string = this.getValue(IMetaKeys.FIELD_FIELD_TYPE);
                if ("Enumeration" === typ) {
                    const sprache: string = this._pruefeUndExtrahiereSprachParameter(
                        IMetaKeys.FIELD_ERROR_MESSAGE,
                        parameter
                    );
                    return (<FormatDefinitionString>this._formatDef).getFieldErrorText(sprache);
                } else if ("String" === typ) {
                    const fehlertexte: Map<string, string> = (<FormatDefinitionString>this._formatDef)
                        .fehlertexte;
                    const sprache: string = this._pruefeUndExtrahiereSprachParameter(
                        IMetaKeys.FIELD_ERROR_MESSAGE,
                        parameter
                    );
                    return fehlertexte.get(sprache);
                }
            }
            return this._formatDef.getWert(key);
        }
        return super.getValue(key);
    }

    private _concretizeIndices(
        operand: RtExternalIdentifier,
        field: RtExternalIdentifier
    ): RtExternalIdentifier {
        const indices: number[] = operand.getIndices().slice(0);

        for (let i = 0; i < indices.length; i++) {
            if (indices[i] === IIdentifier.ITERATION) {
                if (field.getIndices().length <= i) {
                    throw new Error(
                        "The dynamic enum filed has less levels than iterations in the operand?!? This should never be possible."
                    );
                }
                if (field.getIndices()[i] <= 0) {
                    throw new Error("The given field identifier must be concrete!");
                }
                indices[i] = field.getIndices()[i];
            }
        }
        return new RtExternalIdentifier(operand.getName(), indices);
    }

    private _calculateEnumValues(
        field: IIdentifier,
        data: IData,
        mode: ValidationMode
    ): Pair<string, string>[] {
        const dec: DynamicEnumerationCommand = new DynamicEnumerationCommand(
            mode,
            this._metaModel.getMetaDataValidierung(),
            MetaFeld._LOG
        );
        return dec.calcEnumValues(field, data);
    }

    /**
     * There is no default language for js vk runtime.
     * If parameter arrays contains no langugage, there will be an error message.
     */
    private _pruefeUndExtrahiereSprachParameter(key: string, parameter: any[]): string {
        if (!this.checkParameterAngegeben(key, 1, parameter)) {
            throw new Error(
                "The given parameter(s) '" +
                parameter.toString() +
                "' are not correct." +
                "There has to be at least one language parameter."
            );
        }
        const sprache = new LocaleUtil(
            <string[]>this._metaModel.getValue(IMetaKeys.MODEL_LANGUAGES)
        ).deriveLocaleFromFirstParam(parameter);
        if (sprache == undefined) {
            throw new Error("The additional parameter for the key '" + key + "' must be of type Locale.");
        }
        return sprache;
    }

    private _getDezimalTrenner(): string {
        return (<string>this.getValue("DezimalTrenner")).charAt(0);
    }

    informationIsInFeldTyp(key: string): boolean {
        return (
            key === IMetaKeys.FIELD_MIN_LENGTH ||
            key === IMetaKeys.FIELD_MAX_LENGTH ||
            key === IMetaKeys.FIELD_FIELD_TYPE ||
            key === IMetaKeys.FIELD_PATTERN ||
            key === IMetaKeys.FIELD_ERROR_MESSAGE ||
            key === IMetaKeys.FIELD_WORD_WRAP ||
            key === IMetaKeys.FIELD_DECIMAL_PLACES ||
            key === IMetaKeys.FIELD_MAX_DECIMAL_PLACES ||
            key === IMetaKeys.FIELD_SIGN ||
            key === IMetaKeys.FIELD_INTEGER_DIGIT ||
            key === IMetaKeys.FIELD_ZERO_ALLOWED ||
            key === IMetaKeys.FIELD_LEADING_ZEROS ||
            key === IMetaKeys.FIELD_FORMAT ||
            key === IMetaKeysInternal.FELD_ZUSATZPRUEFUNG ||
            key === IMetaKeys.FIELD_VALUE_TRUE ||
            key === IMetaKeys.FIELD_VALUE_FALSE ||
            key === IMetaKeys.FIELD_ENUMERATION_VALUES ||
            key === IMetaKeys.FIELD_ENUMERATION_UI_VALUES ||
            key === IMetaKeys.FIELD_ENUMERATION_CATEGORY_VALUES ||
            key === IMetaKeys.FIELD_NO_VALUE_VALIDATION ||
            key === IMetaKeys.FIELD_S_VALUE ||
            key === IMetaKeysInternal.FELD_UI_VALUES_FOR_VALUE
        );
    }

    private _getDarstellungsInformationIntern(darstellungsInfo: DataFormats | null) {
        return new DarstellungsInformationIntern(
            this._getDezimalTrenner(),
            darstellungsInfo,
            this._metaModel.getMetaDataValidierung().getBaseYearOrDefault(),
            this._metaModel.getMetaDataValidierung().getTimeZone(),
        );
    }
}
