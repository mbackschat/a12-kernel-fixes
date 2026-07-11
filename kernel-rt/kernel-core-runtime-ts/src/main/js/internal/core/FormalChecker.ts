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
import type { Localizable, LocalizableArgs } from "@com.mgmtp.a12.utils/utils-localization";


import type { IIdentifier, IResult, ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { ErrorType } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import {
    RuntimeFormalErrorEnum
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import { ValidierungParam } from "../formatdef/ValidierungParam.js";
import { Constants } from "../util/Constants.js";
import { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { EntityUtil } from "../util/EntityUtil.js";
import { IFormaleFehlerConstants } from "../util/IFormaleFehlerConstants.js";
import { LegalCharTester } from "../util/LegalCharTester.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";
import { ValidType } from "../util/ValidType.js";


import { FormatDefinitionString } from "../formatdef/FormatDefinitionString.js";
import type { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";

import { RuntimeUtils } from "../util/RuntimeUtils.js";

import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";

/**
 * Provides methods for formal checks of the input data.
 */
export class FormalChecker {
    private _legalCharTester: LegalCharTester;
    private _presentationInfoIntern: DarstellungsInformationIntern;

    private static get MAX_WERT_LAENGE(): number {
        return 99999;
    } // Die technische Laengenbegrenzung einzelner Werte

    constructor(
        private _metaData: IMetaDataValidierung,
        private _presentationInfo: DarstellungsInformationIntern,
        private _modus: ValidationMode,
        private _meldeFehler: boolean,
    ) {
        this._legalCharTester = new LegalCharTester(_metaData.getLegalCodepoints(), _metaData.getLegalGraphemeTrie(),
            _metaData.isLegalCharCheckDeactivated());
        this._presentationInfoIntern = new DarstellungsInformationIntern(
            this._metaData.getDezimalTrenner(),
            null,
            this._metaData.getBaseYearOrDefault(),
            this._metaData.getTimeZone(),
        );
    }

    /**
     * @return `false` if the number of levels does not match the number of indices (and reports an error in `result`), `true` otherwise.
     */
    private _checkNumIndicesCorrect(
        idExtern: IIdentifier,
        numLevels: number,
        result: IResult
    ): boolean {
        const indices: number[] = idExtern.getIndices();
        if (indices.length !== numLevels) {
            const args: LocalizableArgs = {
                fieldPathName: LocalizableUtil.createPlainPlaceholder(idExtern.getName()),
                numberOfIndicesSpecified: LocalizableUtil.createPlainPlaceholder(String(indices.length)),
                numberOfIndicesExpected: LocalizableUtil.createPlainPlaceholder(
                    String(numLevels)
                )
            };
            const errorLocalizable = LocalizableUtil.createLocalizableFormalError(
                RuntimeFormalErrorEnum.FALSCHE_ANZAHL_INDICES_FM_TEXT,
                args
            );
            this.addFormalError(
                result,
                idExtern,
                IFormaleFehlerConstants.FEHLER_FALSCHE_ANZAHL_INDICES,
                [errorLocalizable]
            );
            return false;
        }
        return true;
    }

    /**
     * @return `false` if the indices were invalid (and reports an error in `result`), `true` otherwise
     */
    public checkIndices(
        idExtern: IIdentifier,
        maxIndices: number[],
        result: IResult
    ): boolean {
        if (!this._checkNumIndicesCorrect(idExtern, maxIndices.length, result)) {
            return false;
        }
        if (!this._checkIndicesLargerThanZero(idExtern, result)) {
            return false;
        }
        if (!this._checkIndicesLessOrEqualThanMax(idExtern, maxIndices, result)) {
            return false;
        }
        return true;
    }

    /**
     * @return `false` if any of the indices are smaller than `1` (and reports an error in `result`), `true` otherwise.
     */
    private _checkIndicesLargerThanZero(idExtern: IIdentifier, result: IResult) {
        const indices: number[] = idExtern.getIndices();
        if (indices[0] < 1) {
            // erster Index ist die laufende Nummer Vordruck
            this.addFormalError(result, idExtern, IFormaleFehlerConstants.FEHLER_LAUFENDE_NR_ZU_KLEIN,
                [LocalizableUtil.createLocalizableFormalError(
                    RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_FM_TEXT
                )]);
            return false;
        }
        for (let i = 1; i < indices.length; i++) {
            if (indices[i] < 1) {
                // letzter Index ist die Zeile
                if (i === indices.length - 1) {
                    this.addFormalError(result, idExtern, IFormaleFehlerConstants.FEHLER_ZEILENNUMMER_ZU_KLEIN,
                        [LocalizableUtil.createLocalizableFormalError(
                            RuntimeFormalErrorEnum.FEHLER_ZEILE_FM_TEXT
                        )]);
                } else {
                    // wiederholbarer Kontext
                    const parameterReplacements: LocalizableArgs = {
                        groupName: LocalizableUtil.createPlainPlaceholder(
                            EntityUtil.getPathOfHierarchyLevel(idExtern.getName(), i)
                        ),
                        groupIndexSpecified: LocalizableUtil.createPlainPlaceholder("" + indices[i])
                    };
                    const fehlerText = LocalizableUtil.createLocalizableFormalError(
                        RuntimeFormalErrorEnum.KONTEXTNUMMER_ZU_KLEIN_FM_TEXT,
                        parameterReplacements
                    );
                    this.addFormalError(result, idExtern, IFormaleFehlerConstants.FEHLER_KONTEXTNUMMER_ZU_KLEIN, [fehlerText]);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method to report a formal error.
     * Used in the formal validation and the preliminary rule validation.
     *
     * @param results    the validation result storing errors.
     * @param idExtern    the field identifier, the error occurred for.
     * @param key        the error id of the formal error.
     * @param errorMsg    the error message.
     * @param errorType    the type of the error. This is only relevant for the preliminary rule checks,
     *                    which can be omission errors as well.
     */
    public addFormalError(
        results: IResult,
        idExtern: IIdentifier,
        key: string,
        errorMsgs: Localizable[],
        errorType: ErrorType = ErrorType.VALUE_ERROR,
    ): void {
        if (!this._meldeFehler) {
            return;
        }

        const fieldNameArg = LocalizableUtil.createFieldReplacementArg(idExtern, this._metaData);
        const msgs = errorMsgs.map(errorMsg => LocalizableUtil.addLocalizableArgs(errorMsg, fieldNameArg));
        const referencedFields: Set<IIdentifier> = new Set<IIdentifier>();
        referencedFields.add(idExtern);
        const refOmissionErrorResponsible: Set<IIdentifier> = new Set<IIdentifier>();
        if (errorType === ErrorType.OMISSION_ERROR) {
            refOmissionErrorResponsible.add(idExtern);
        }
        results.addError(
            IFormaleFehlerConstants.FORMALE_PRUEFUNG,
            key,
            idExtern,
            msgs,
            errorType,
            referencedFields,
            refOmissionErrorResponsible,
            new Set<IIdentifier>()
        );
    }

    /**
     * @returns an object containing:
     * - `validity`: {@link ValidType.Invalid} if the value was invalid (and reports an error in `result`), otherwise {@link ValidType.Valid}
     * - `normalizedValue`: `undefined` if the provided value was not valid or not validated, otherwise the provided `value` with normalized linebreaks.
     * - `internalXmlValue`: `undefined` if the provided value was not valid or not validated, otherwise the provided `value` in internal XML format
     */
    public checkAndConvertFieldValue(
        idIntern: RtInternalIdentifier,
        idExtern: IIdentifier,
        value: string,
        result: IResult,
    ): { validity: ValidType, normalizedValue?: string, internalXmlValue?: string } {
        const formatDefinition: IFormatDefinition = this._metaData.getIFormatDefinition(idIntern.getName());
        if (
            formatDefinition instanceof FormatDefinitionString &&
            formatDefinition.isNoValueValidation
        ) {
            // additional checks are not required
            return {
                validity: ValidType.Valid,
            };
        }

        if (!this._checkFieldValueTooLong(idExtern, value, result)) {
            return {
                validity: ValidType.Invalid,
            };
        }

        const normalizedValue = RuntimeUtils.normalizeLinebreaks(value);

        const vt = this._checkFieldValueFormat(result, normalizedValue, idExtern, formatDefinition, this._presentationInfo);
        if (vt === ValidType.Invalid) {
            return {
                validity: vt,
            };
        } else {
            return {
                validity: vt,
                normalizedValue: normalizedValue,
                internalXmlValue: formatDefinition.convert(normalizedValue, this._presentationInfo, false, this._modus.customFieldTypeFactory),
            };
        }
    }

    /**
     * Check the provided `value` against the provided `formatDefinition` (same as {@link checkFieldValueFormatBasic}).
     * 
     * Additionally also checks:
     * - `value` follows the legal charset
     * - `value` does not start with a blank (" ")
     * - `value` does not end with a blank (" ")
     * 
     * @return{} {@link ValidType.Invalid} if the field value is not formally correct (and reports an error in `result`).
     */
    private _checkFieldValueFormat(
        result: IResult,
        value: string,
        idExtern: IIdentifier,
        formatDefinition: IFormatDefinition,
        presentationInfo: DarstellungsInformationIntern,
    ): ValidType {
        if (!this._legalCharTester.pruefeZeichensatz(value)) {
            this.addFormalError(result, idExtern,
                IFormaleFehlerConstants.ZEICHEN_NICHT_IM_ZEICHENSATZ,
                [LocalizableUtil.createLocalizableFormalError(
                    RuntimeFormalErrorEnum.ZEICHEN_NICHT_IM_ZEICHENSATZ_FM_TEXT
                )]
            );
            return ValidType.Invalid;
        }

        if (value.startsWith(Constants.BLANK)) {
            this.addFormalError(result, idExtern,
                IFormaleFehlerConstants.FEHLER_FUEHRENDES_BLANK,
                [LocalizableUtil.createLocalizableFormalError(
                    RuntimeFormalErrorEnum.FEHLER_FUEHRENDES_BLANK_FM_TEXT
                )]
            );
            return ValidType.Invalid;
        }

        if (value.endsWith(Constants.BLANK)) {
            this.addFormalError(result, idExtern,
                IFormaleFehlerConstants.FEHLER_NACHFOLGENDES_BLANK,
                [LocalizableUtil.createLocalizableFormalError(
                    RuntimeFormalErrorEnum.FEHLER_NACHFOLGENDES_BLANK_FM_TEXT
                )]
            );
            return ValidType.Invalid;
        }

        return this.checkFieldValueFormatBasic(result, value, idExtern, formatDefinition, presentationInfo);
    }

    /**
     * Check the provided `value` against the provided `formatDefinition`.
     * 
     * @param presentationInfo the presentation info to use for the check. If `undefined`, defaults to the presentation info for the internal representation.
     * 
     * @return{} {@link ValidType.Invalid} if the field value is not formally correct (and reports an error in `result`).
     */
    public checkFieldValueFormatBasic(
        result: IResult,
        value: string,
        idExtern: IIdentifier,
        formatDefinition: IFormatDefinition,
        presentationInfo?: DarstellungsInformationIntern,
    ): ValidType {
        const param: ValidierungParam = new ValidierungParam(
            value,
            idExtern,
            result,
            !this._meldeFehler,
            presentationInfo ?? this._presentationInfoIntern,
            true, // always non-lenient with thousand separator placement in conversion
            this._metaData,
            this._modus.customFieldTypeFactory,
        );

        return formatDefinition.validiereFormat(param, true);
    }

    /**
     * @return `false` if the provided value is longer than {@link MAX_WERT_LAENGE} (and reports an error in `result`), `true` otherwise
     */
    private _checkFieldValueTooLong(idExtern: IIdentifier, value: string, result: IResult): boolean {
        if (value.length > FormalChecker.MAX_WERT_LAENGE) {
            this.addFormalError(result, idExtern,
                IFormaleFehlerConstants.FEHLER_WERT_ZU_LANG,
                [LocalizableUtil.createLocalizableFormalError(
                    RuntimeFormalErrorEnum.FEHLER_WERT_ZU_LANG_FM_TEXT
                )]
            );
            return false;
        }
        return true;
    }

    /**
     * @return `false` if any of the indices are largern than the allowed max index (and reports an error in `result`), `true` otherwise.
     */
    private _checkIndicesLessOrEqualThanMax(
        idExtern: IIdentifier,
        maxIndices: number[],
        result: IResult
    ): boolean {
        const indices: number[] = idExtern.getIndices();
        const maxLfdNr: number = maxIndices[0];

        // erster Index ist die laufende Nummer, Fehlermeldung muss ggf. entsprechend lauten
        if (indices[0] > maxLfdNr) {
            this.addFormalError(
                result,
                idExtern,
                IFormaleFehlerConstants.FEHLER_LAUFENDE_NR_ZU_GROSS,
                [LocalizableUtil.createLocalizableFormalError(
                    RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_ZU_GROSS_FM_TEXT
                )]
            );
            return false;
        }

        // Zeilenindex und Kontextindices
        for (let i = 1; i < indices.length; i++) {
            const maxIndex: number = maxIndices[i];
            if (indices[i] > maxIndex) {
                // der letze Index im Indexarray ist die Zeile, Fehlermeldung muss entsprechend lauten
                if (i === indices.length - 1) {
                    this.addFormalError(
                        result,
                        idExtern,
                        IFormaleFehlerConstants.FEHLER_ZEILENNUMMER_ZU_GROSS,
                        [LocalizableUtil.createLocalizableFormalError(
                            RuntimeFormalErrorEnum.FEHLER_ZEILENNUMMER_ZU_GROSS_FM_TEXT
                        )]
                    );
                    return false;
                }
                const kontextPfad: string = EntityUtil.getPathOfHierarchyLevel(idExtern.getName(), i);
                const parameterReplacements: LocalizableArgs = {
                    groupName: LocalizableUtil.createPlainPlaceholder(kontextPfad),
                    groupIndexSpecified: LocalizableUtil.createPlainPlaceholder("" + indices[i]),
                    maxGroupIndex: LocalizableUtil.createPlainPlaceholder("" + maxIndex)
                };
                const localizable = LocalizableUtil.createLocalizableFormalError(
                    RuntimeFormalErrorEnum.FEHLER_KONTEXTNUMMER_ZU_GROSS_FM_TEXT,
                    parameterReplacements
                );
                this.addFormalError(
                    result,
                    idExtern,
                    IFormaleFehlerConstants.FEHLER_KONTEXTNUMMER_ZU_GROSS,
                    [localizable]
                );
                return false;
            }
        }
        return true;
    }
}
