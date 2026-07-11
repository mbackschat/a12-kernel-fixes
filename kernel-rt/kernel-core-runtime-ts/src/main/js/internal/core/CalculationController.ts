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
import { Big } from "big.js";

import type { Localizable } from "@com.mgmtp.a12.utils/utils-localization";

import { ValidationDateParser } from "@com.mgmtp.a12.utils/utils-localization/a12internal";

import { RuntimeFormalErrorEnum } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import type { IResult, ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { ErrorType, HashSet, IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { FormatDefinitionDatum } from "../formatdef/FormatDefinitionDatum.js";
import type { FormatDefinitionDatumBereich } from "../formatdef/FormatDefinitionDatumBereich.js";
import type { FormatDefinitionZahl } from "../formatdef/FormatDefinitionZahl.js";
import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import { FehlerHandler } from "../formatdef/error/FehlerHandler.js";
import type { ValidationLogger } from "../logging/ValidationLogger.js";
import type { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { IFormaleFehlerConstants } from "../util/IFormaleFehlerConstants.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";
import type { RtExternalIdentifier } from "../util/RtExternalIdentifier.js";
import { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";
import { ValidType } from "../util/ValidType.js";
import { VkBigDecimal } from "../util/VkBigDecimal.js";
import { VkDate } from "../util/VkDate.js";
import { VkDateRange } from "../util/VkDateRange.js";
import { VkString } from "../util/VkString.js";

import { BigDecimalUtils } from "./BigDecimalUtils.js";
import type { CalculationCache } from "./CalculationCache.js";
import { CollectingResultImpl } from "./CollectingResultImpl.js";
import type { FormalChecker } from "./FormalChecker.js";
import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";
import type { IValidatorIntern } from "./IValidatorIntern.js";
import { MainValidatorController } from "./MainValidatorController.js";
import { RuntimeController } from "./RuntimeController.js";

export class CalculationController extends RuntimeController {
	private static get _MAX_FRACTIONAL_DIGITS_TOTAL(): number {
		return 15;
	}

	public constructor(
		ergebnisse: IResult,
		private _calcCache: CalculationCache,
		logger: ValidationLogger,
		modus: ValidationMode,
		metaData: IMetaDataValidierung,
		private _darstellungsInfo: DarstellungsInformationIntern,
		private _validator: IValidatorIntern,
		private _formalChecker: FormalChecker,
	) {
		super(ergebnisse, _calcCache, logger, modus, metaData);
	}

	/**
	 * Set the calculated value in the calculation cache.
	 *
	 * @param idIntern	the field to be set.
	 * @param valueIntern	calculated value in internal format.
	 * @param isValueCorrect  specifies the correctness of the value. If the value is correct, an error during
	 * conversion should never happen and will therefore be re-thrown, else the internal value will be used as UI value.
	 */
	public setBerechnetenStringWert(
		idIntern: RtInternalIdentifier,
		valueIntern: string,
		isValueCorrect: boolean
	): void {
		const formatDefinition: IFormatDefinition = this.metaData.getIFormatDefinition(
			idIntern.getName()
		);
		let uiValue: string;
		try {
			uiValue = formatDefinition.convert(valueIntern, this._darstellungsInfo, true, this.modus.customFieldTypeFactory);
		} catch (e) {
			if (isValueCorrect) {
				throw e;
			} else {
				uiValue = valueIntern;
			}
		}
		this._calcCache.addCalculatedFieldValue(idIntern, uiValue, valueIntern);
	}

	public isInvalidField(idIntern: RtInternalIdentifier): boolean {
		return this._calcCache.isInvalidField(idIntern);
	}

	public getInternalFieldValue(idIntern: RtInternalIdentifier): string | null {
		return this._calcCache.isFieldWithPseudoValue(idIntern)
			? null
			: this._calcCache.getXMLValueUnchecked(idIntern) ?? null;
	}

	public getFilledGroupInstances(groupPath: string): Iterable<RtInternalIdentifier> {
		return this._calcCache.getFilledGroupInstances(groupPath);
	}

	public deleteField(idIntern: RtInternalIdentifier): void {
		this._calcCache.addCalculatedFieldValue(idIntern, "", "");
	}

	/**
	 * Updates the given index field after new values were calculated for it.
	 * @param indexFieldFullName full name of the index field which was calculated
	 * @param calculatedInstances the list of instances which were calculated (i.e. may have changed)
	 * @param formallyInvalidInstances the list of instances which are arleady formally invalid
	 */
	public updateCalculatedIndexField(
		indexFieldFullName: string,
		calculatedInstances: HashSet<RtInternalIdentifier>,
		formallyInvalidInstances: RtInternalIdentifier[],
	): void {
		// Before evaluating the preliminary rules, apply any applicable replacements of emtpy computed values with defaults.
		this._calcCache.addDefaultValuesForCalculatedIndexField(indexFieldFullName);

		// The calculation cache remembers erroneous field instances in order to throw errors if an access to them in a
		// calculation rule should lead to a cascading error. This differs from an analogous mechanism used for validation,
		// which treats erroneous field instances as not relevant. Since we are about to enter a validation, we must add the
		// already formally incorrect index fields into this list, as throwing an error would not be considreed a cascading
		// error in this context.
		formallyInvalidInstances.forEach(id => this._calcCache.markFieldAsInvalidForValidation(id));

		// Evaluate the preliminary rules for this index field. Checks uniqueness and requiredness.
		const invalidInstances = formallyInvalidInstances.slice();
		const indexFieldUniqueName = this.metaData.getUniqueName(indexFieldFullName);
		const result = new CollectingResultImpl();
		this._validatePreliminaryRulesForField(indexFieldUniqueName, result);
		for (const errorMessage of result.errorMessages) {
			const errorIdIntern = new RtInternalIdentifier(indexFieldUniqueName, errorMessage.entityId.getIndices());
			if (calculatedInstances.has(errorIdIntern)) {
				// do not report errors for the non-calculated index fields for now --> will be handled in a second step
				this.pruefErgebnis.addError(
					errorMessage.ruleName,
					errorMessage.id,
					errorMessage.entityId,
					errorMessage.message,
					ErrorType.VALUE_ERROR, // always consider invalid calculated value as value error
					errorMessage.referencedFields,
					new Set(),
					errorMessage.unknowns,
				);
			}
			invalidInstances.push(errorIdIntern);
		}

		// In order to correctly separate preliminary errors caused by calculations from those already present in the original document,
		// we execute the preliminary validation again with only the calculated and formally invalid instances set to not relevant.
		// Otherwise, we would e.g. mark an index field made non-unique by a calculation as a formally incorrect operand.
		this._calcCache.resetErroneousEntitiesForValidation();
		calculatedInstances.forEach(id => this._calcCache.markFieldAsInvalidForValidation(id));
		formallyInvalidInstances.forEach(id => this._calcCache.markFieldAsInvalidForValidation(id));
		this._validatePreliminaryRulesForField(indexFieldUniqueName, this.pruefErgebnis);

		// Mark the invalid index fields so that any access to them will cause a cascading error.
		this._calcCache.resetErroneousEntitiesForValidation();
		this._calcCache.removeAllInvalidIndexFields(indexFieldUniqueName);
		invalidInstances.forEach(id => this._calcCache.markFieldAsInvalidForValidation(id));
		this._calcCache.convertInvalidFieldsForCalculation();
	}

	private _validatePreliminaryRulesForField(uniqueFieldName: string, result: IResult): void {
		const validationController = new MainValidatorController(
			result,
			this._calcCache,
			this.logger,
			this.modus,
			this.metaData,
			this._formalChecker
		);
		this._validator.validatePreliminaryRulesForField(validationController, uniqueFieldName);
	}

	/**
	 * Checks the given value of the given field and creates an error message in case of an error.
	 *
	 * @param idIntern	the field to be tested.
	 * @param internValue	the value of the field in internal format.
	 * @return <code>true</code>, in case of a correct value, <code>false</code> otherwise.
	 */
	public checkField(idIntern: RtInternalIdentifier, internValue: string): boolean {
		return this._formalChecker.checkFieldValueFormatBasic(
			this.pruefErgebnis,
			internValue,
			this._calcCache.convert2Extern(idIntern),
			this.metaData.getIFormatDefinition(idIntern.getName())
		) !== ValidType.Invalid;
	}

	public markiereAlsFehlerhaftBerechnet(id: RtInternalIdentifier): void {
		this._calcCache.markFieldAsInvalid(id);
	}

	/**
	 * see {@link CalculationCache.markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated}
	 */
	public markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated(uniqueFieldName: string, parallelIterationGroups: string[]): void {
		this._calcCache.markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated(uniqueFieldName, parallelIterationGroups);
	}

	/**
	 * Erzeugt einen formalen Fehler für das Berechnete Feld.
	 */
	public addBerechnungsFehler(idIntern: RtInternalIdentifier): void {
		const idExtern = this.validationCache.convert2Extern(idIntern);
		this.pruefErgebnis.addError(
			IFormaleFehlerConstants.FORMALE_PRUEFUNG,
			IFormaleFehlerConstants.BERECHNUNG_WERT_FEHLER,
			idExtern,
			[this._createLocalizableForErroneousCalculation(idIntern)],
			ErrorType.VALUE_ERROR,
			// as any is needed since Set is implemented by utils and has to be fixed there ... new methods need to be implemented as well
			new HashSet<RtExternalIdentifier>([idExtern]) as any,
			new HashSet<RtExternalIdentifier>() as any,
			new HashSet<RtExternalIdentifier>() as any
		);
	}

	private _createLocalizableForErroneousCalculation(idIntern: RtInternalIdentifier): Localizable {
		return {
			key: LocalizableUtil.createFormalErrorKey(RuntimeFormalErrorEnum.BERECHNUNG_WERT_FEHLER),
			args: {
				fieldName: LocalizableUtil.createLocalizableFieldNamePlaceholder(idIntern.getName(), this.metaData),
			},
			defaults: FehlerHandler.getAllErrorTexts(RuntimeFormalErrorEnum.BERECHNUNG_WERT_FEHLER),
		};
	}

	/**
	 * Setzt den Berechneten Wert im Cache. Im Fehlerfall wird das Feld als fehlerhaft berechnet
	 * markiert und die entsprechende Fehlermeldung erzeugt.
	 *
	 * @param value	berechneter Wert
	 * @param id	interne Id
	 */
	public handleBerechnetenWert(
		value: string | VkString | VkDate | VkDateRange | VkBigDecimal,
		id: RtInternalIdentifier
	): void {
		if (typeof value === "string") {
			this._handleCalculatedStringValue(new VkString(value), id);
		} else if (value instanceof VkString) {
			this._handleCalculatedStringValue(value, id);
		} else if (value instanceof VkDate) {
			this._handleCalculatedDateValue(value, id);
		} else if (value instanceof VkBigDecimal) {
			this._handleCalculatedNumberValue(value, id);
		} else if (value instanceof VkDateRange) {
			this._handleCalculatedDateRangeValue(value, id);
		}
	}

	private _handleCalculatedStringValue(value: VkString, id: RtInternalIdentifier): void {
		if (value.isNichtPruefungsrelevant || !value.hasValue) {
			// For fields that are not relevant or for empty values, do like no calculation was done.
			return;
		}
		const isValueCorrect = this.checkField(id, value.wert);
		if (!isValueCorrect) {
			this.markiereAlsFehlerhaftBerechnet(id);
		}
		this.setBerechnetenStringWert(id, value.wert, isValueCorrect);
	}

	private _handleCalculatedDateValue(value: VkDate, id: RtInternalIdentifier): void {
		if (value.isNichtPruefungsrelevant || value.date === undefined) {
			// For fields that are not relevant or for empty values, do like no calculation was done.
			return;
		}
		const formatDefinition = <FormatDefinitionDatum>(
			this.metaData.getIFormatDefinition(id.getName())
		);
		const formatString = formatDefinition.format;
		const internalValue = ValidationDateParser.makeStringDayMonthTwoDigits(
			value.date,
			formatString,
			this.timeZone
		);

		const isValueCorrect = this.checkField(id, internalValue);
		if (!isValueCorrect) {
			this.markiereAlsFehlerhaftBerechnet(id);
		}
		this.setBerechnetenStringWert(id, internalValue, isValueCorrect);
	}

	private _handleCalculatedDateRangeValue(value: VkDateRange, id: RtInternalIdentifier): void {
		if (value.isNichtPruefungsrelevant || value.start === undefined || value.end === undefined) {
			// For fields that are not relevant or for empty values, do like no calculation was done.
			return;
		}
		const formatDefinition = <FormatDefinitionDatumBereich>(
			this.metaData.getIFormatDefinition(id.getName())
		);
		const formatString = formatDefinition.format;
		const internalStartValue = ValidationDateParser.makeStringDayMonthTwoDigits(value.start, formatString, this.timeZone);
		const internalEndValue = ValidationDateParser.makeStringDayMonthTwoDigits(value.end, formatString, this.timeZone);
		const internalValue = internalStartValue + formatDefinition.trenner + internalEndValue;

		const isValueCorrect = this.checkField(id, internalValue);
		if (!isValueCorrect) {
			this.markiereAlsFehlerhaftBerechnet(id);
		}
		this.setBerechnetenStringWert(id, internalValue, isValueCorrect);
	}

	private _handleCalculatedNumberValue(value: VkBigDecimal, id: RtInternalIdentifier): void {
		// internally we calculate with max INTERNAL_DECIMAL_PLACES decimal places and
		// round to JAVA_INTERNAL_PRECISION significant figures (in order to simulate
		// behaviour of the java implementation)
		// Here we make a rounding to VkBigDecimal#DEFAULT_SCALE fractional digits
		const valueWithDefaultScale = value.setScale(VkBigDecimal.DEFAULT_SCALE, Big.roundHalfUp);
		const formatDefinition = <FormatDefinitionZahl>(
			this.metaData.getIFormatDefinition(id.getName())
		);
		const minNachkommaMeta = Number(formatDefinition.getWert(IMetaKeys.FIELD_DECIMAL_PLACES));
		const maxNachkommaMeta = Number(formatDefinition.getWert(IMetaKeys.FIELD_MAX_DECIMAL_PLACES));
		if (valueWithDefaultScale.wertIstUngueltig) {
			this.markiereAlsFehlerhaftBerechnet(id);
			this.addBerechnungsFehler(id);
		} else {
			const minScale = valueWithDefaultScale.scale();
			// Passt der Wert in die Nachkommastellen?
			if (minScale <= maxNachkommaMeta) {
				const destFractionalDigits = Math.max(minScale, minNachkommaMeta);
				const strWert = valueWithDefaultScale.toFixed(destFractionalDigits);
				const wertXML = strWert.replace(".", this._darstellungsInfo.dezimalTrennerIntern);
				const isValueCorrect = this.checkField(id, wertXML);
				if (!isValueCorrect) {
					this.markiereAlsFehlerhaftBerechnet(id);
				}
				this.setBerechnetenStringWert(id, wertXML, isValueCorrect);
			} else {
				// nein => nimm max. die max. möglichen Stellen + 1
				const strWert = BigDecimalUtils.toStringWertLaengenBeschraenkt(
					valueWithDefaultScale,
					CalculationController._MAX_FRACTIONAL_DIGITS_TOTAL + 1
				);
				const wertXML = strWert.replace(".", this._darstellungsInfo.dezimalTrennerIntern);
				if (this.checkField(id, wertXML)) {
					// Der Wert sollte eigentlich immer fehlerhaft sein, also sollten wir hier nie landen.
					// Könnte auftreten, wenn die projektspezifische maximale Länge kleiner als 15 ist.
					// PruefeFeld dient eigentlich der Erzeugung einer spezifischeren Fehlermeldung.
					this.addBerechnungsFehler(id);
				}
				this.setBerechnetenStringWert(id, wertXML, false);
				this.markiereAlsFehlerhaftBerechnet(id);
			}
		}
	}

	public handleExternallyCalculatedValue(value: string | null, idIntern: RtInternalIdentifier): void {
		if (value === null || value === "") {
			this.deleteField(idIntern);
		} else {
			this._handleCalculatedStringValue(new VkString(value), idIntern);
		}
	}

	/**
	 * @param displayValue a display value as provided in the original data. Must not be empty.
	 * @returns `true` if the formal validation of `displayValue` was successful, `false` otherwise.
	 */
	public handleDeferredFormalValidation(idIntern: RtInternalIdentifier, idExtern: RtExternalIdentifier, displayValue: string): boolean {
		const fieldValueCheck = this._formalChecker.checkAndConvertFieldValue(idIntern, idExtern, displayValue, this.pruefErgebnis);
		if (fieldValueCheck.validity === ValidType.Invalid) {
			this._calcCache.addInvalidFieldValue(idIntern, idExtern);
			this._calcCache.convertInvalidFieldsForCalculation();
		} else {
			if (fieldValueCheck.normalizedValue !== undefined && fieldValueCheck.internalXmlValue !== undefined) {
				this._calcCache.addValidFieldValue(idIntern, fieldValueCheck.normalizedValue, fieldValueCheck.internalXmlValue);
			} else { // considered valid, but the value should not be interned
				this._calcCache.addPseudoFieldValueIfAbsent(idIntern);
			}
		}
		return fieldValueCheck.validity !== ValidType.Invalid;
	}

	/**
	 * Is called from the generated code to check if the repetition is already set. Is used for
	 * parallel iteration because no additional repetitions should be created by calculation.
	 *
	 * @param id	determines the indices
	 * @param level	the level up to which it should be checked if the repetition is set (here this is
	 * the parallel iteration level on the calc field path)
	 * @return <code>true</code> if the repetition is set, <code>false</code> otherwise
	 */
	public isWiederholungGesetzt(id: RtInternalIdentifier, level: number): boolean {
		const resolvedId = this.validationCache.resolveSemanticIndex(id);
		return this.validationCache
			.getGesetzteWiederholungen()
			.istWiederholungGesetzt(resolvedId.getIndices(), level);
	}

	public get formalChecker(): FormalChecker {
		return this._formalChecker;
	}
}
