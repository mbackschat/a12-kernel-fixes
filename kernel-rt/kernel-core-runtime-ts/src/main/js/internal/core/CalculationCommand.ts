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

import type { IData, IIdentifier, IResult, ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IExternalCalculation } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { arraysEqual } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { DatenFilterImpl } from "../util/DatenFilterImpl.js";

import { EntityUtil } from "../util/EntityUtil.js";

import { CalculationSorter } from "../util/CalculationSorter.js";


import { CalculationCache } from "./CalculationCache.js";
import { CalculationController } from "./CalculationController.js";
import { CheckCommand } from "./CheckCommand.js";
import { ExternalCalculationRegistry } from "./ExternalCalculationRegistry.js";
import type { ICalculatorIntern } from "./ICalculatorIntern.js";
import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";
import type { IValidatorIntern } from "./IValidatorIntern.js";
import { MainValidatorController } from "./MainValidatorController.js";

export class CalculationCommand extends CheckCommand {
	private readonly _calculationCache: CalculationCache;
	private readonly _indexFieldFullNames: Set<string>;
	private readonly _internallyCalculatedFieldFullNames: Set<string>;
	private readonly _externalCalculationRegistry: ExternalCalculationRegistry;
	private readonly _expandedOperandFieldFullNames: Set<string>;
	private readonly _entityFullNamesNotRemovedFromData: Set<string>;

	constructor(
		validator: IValidatorIntern,
		modus: ValidationMode,
		metaData: IMetaDataValidierung,
		logger: Logger,
		private _calculator: ICalculatorIntern,
		externalCalculations?: IExternalCalculation[],
		changedFieldInstances?: IIdentifier[],
		private _shouldTestCalculationSorting = false,
	) {
		super(validator, modus, metaData, logger);
		this._calculationCache = new CalculationCache(this._metaData, this._darstellungsInfo, this._modus);
		this._validationCache = this._calculationCache;
		this._indexFieldFullNames = new Set(this._metaData.getIndexFieldFullNames());
		this._internallyCalculatedFieldFullNames = new Set(Array.from(
			this._metaData.getBerechneteFelder(),
			calculatedField => <string>calculatedField.getValue(IMetaKeys.FIELD_FULLNAME)
		));
		this._externalCalculationRegistry = new ExternalCalculationRegistry(
			externalCalculations,
			this._internallyCalculatedFieldFullNames,
			this._calculator.getExpandedOperandFieldsOfCalculations(),
			this._metaData,
		);
		this._externalCalculationRegistry.initializeChangedFieldInstances(changedFieldInstances, this._metaData);
		this._expandedOperandFieldFullNames = this._getExpandedOperandFieldFullNames();
		this._entityFullNamesNotRemovedFromData = this._getEntityFullNamesNotRemovedFromData();
	}

	public processCalc(data: IData, formaleFehler: IResult): IData {
		const startMesspunkt = this._logger.startMesspunkt();
		this._calculationCache.setDaten(this._getDataWithoutEntitiesNotNeededForCalculations(data));
		const result = this._processInternalC(formaleFehler, data);
		this._logger.logMessung("The calculation took ", startMesspunkt);
		return result;
	}

	private _getDataWithoutEntitiesNotNeededForCalculations(data: IData): IData {
		return new DatenFilterImpl(data, {
			test: (id: IIdentifier) => {
				// test that the given entity path exists, throws error otherwise
				const uniqueName: string = this._metaData.getUniqueName(id.getName());
				const fullName: string = this._metaData.getPfadName(uniqueName);

				return this._isNotRemovedFromData(fullName);
			}
		});
	}

	/**
	 * @returns all entities (full names) which must not be removed from the user-provided {@link IData} for calculations
	 * 
	 * Includes:
	 *  - all operands of internal and external calculations (+ their ancestor groups), except internally or externally calculated fields
	 * 	- ancestor groups of internally and externally calculated fields
	 */
	private _getEntityFullNamesNotRemovedFromData(): Set<string> {
		const entityFullNamesRelevantForCalculation: Set<string> = new Set();
		for (const fieldFullName of this._getFieldFullNamesRelevantForCalculation()) {
			if (!this._shouldFieldValueBeIgnoredInFormalValidation(fieldFullName)) {
				entityFullNamesRelevantForCalculation.add(fieldFullName);
			}

			// all ancestors of internally and externally calculated fields are needed
			let groupFullName = EntityUtil.getParentPath(fieldFullName);
			while (groupFullName.length > 0 && !entityFullNamesRelevantForCalculation.has(groupFullName)) {
				entityFullNamesRelevantForCalculation.add(groupFullName);
				groupFullName = EntityUtil.getParentPath(groupFullName);
			}
		}

		return entityFullNamesRelevantForCalculation;
	}

	private _getFieldFullNamesRelevantForCalculation(): Set<string> {
		const result: Set<string> = new Set();
		this._expandedOperandFieldFullNames.forEach(fullFieldName => result.add(fullFieldName));
		this._internallyCalculatedFieldFullNames.forEach(fullFieldName => result.add(fullFieldName));
		this._externalCalculationRegistry.getExternallyCalculatedFields().forEach(fullFieldName => result.add(fullFieldName));
		return result;
	}

	/**
	 * @returns `true`, if the given field (full name) relevant for calculations should not have its value formally validated
	 */
	private _shouldFieldValueBeIgnoredInFormalValidation(fullName: string): boolean {
		return this._isInternallyCalculated(fullName) // internal calculation always produces a new value anyways
			|| this._isExternallyCalculated(fullName); // external calculation defers formal validation until after values were calculated
	}

	private _processInternalC(erg: IResult, originalData: IData): IData {
		this.formalePruefung(null, erg);

		this._preliminaryValidationOfIndexFields(erg);

		this._calculationCache.convertInvalidFieldsForCalculation();

		return this._executeCalculations(originalData, erg);
	}

	/**
	 * Executes the preliminary validation only for index fields which are operands and not externally calculated.
	 *
	 * For externally calculated index fields, the preliminary validation will be done after the external
	 * calculation has been executed. The dependency-sorted execution order of calculations ensures these
	 * index fields are not accessed directly or indirectly before then.
	 */
	private _preliminaryValidationOfIndexFields(erg: IResult): void {
		const validationController = new MainValidatorController(
			erg,
			this._calculationCache,
			this._logger,
			this._modus,
			this._metaData,
			this._formalChecker,
		);

		for (const indexFieldFullName of this._indexFieldFullNames) {
			if (this._isNotRemovedFromData(indexFieldFullName) && !this._isExternallyCalculated(indexFieldFullName)) {
				const uniqueIndexFieldName = this._metaData.getUniqueName(indexFieldFullName);
				this._validator.validatePreliminaryRulesForField(validationController, uniqueIndexFieldName);
			}
		}
	}

	private _executeCalculations(originalData: IData, result: IResult): IData {
		const controller = new CalculationController(
			result,
			this._calculationCache,
			this._logger,
			this._modus,
			this._metaData,
			this._darstellungsInfo,
			this._validator,
			this._formalChecker,
		);
		this._externalCalculationRegistry.initializeInitialValues(originalData);

		for (const fullFieldName of this._getCalculatedFieldFullNamesInOrder()) {
			if (this._isInternallyCalculated(fullFieldName)) {
				this._calculator.executeCalculation(fullFieldName, controller);
			} else if (this._isExternallyCalculated(fullFieldName)) {
				this._externalCalculationRegistry.executeCalculation(fullFieldName, controller);
			}
		}

		return this._calculationCache.getBerechneteFelder();
	}

	private _getCalculatedFieldFullNamesInOrder(): string[] {
		if (!this._shouldTestCalculationSorting && !this._externalCalculationRegistry.hasCalculations()) {
			// avoid sorting the calculations if there are no external calculations
			return this._calculator.getCalculatedFieldFullNamesInOrder();
		}

		const sortedCalculatedFieldFullNames = CalculationSorter.sortCalculations(
			this._calculator.getDependencyGraph(),
			this._externalCalculationRegistry.getDependencyGraph());

		if (this._shouldTestCalculationSorting && !this._externalCalculationRegistry.hasCalculations()) {
			if (!arraysEqual(sortedCalculatedFieldFullNames, this._calculator.getCalculatedFieldFullNamesInOrder())) {
				throw new Error(`Expected order of calculated fields to be ${this._calculator.getCalculatedFieldFullNamesInOrder().join(",")} but was ${sortedCalculatedFieldFullNames.join(",")}`);
			}
		}

		return sortedCalculatedFieldFullNames;
	}

	private _getExpandedOperandFieldFullNames(): Set<string> {
		const operandFieldFullNames: Set<string> = new Set();
		this._calculator.getExpandedOperandFieldsOfCalculations().forEach(fullFieldName => operandFieldFullNames.add(fullFieldName));
		this._externalCalculationRegistry.getOperandFieldsOfExternalCalculations().forEach(fullFieldName => operandFieldFullNames.add(fullFieldName));
		return operandFieldFullNames;
	}

	private _isNotRemovedFromData(fullName: string): boolean {
		return this._entityFullNamesNotRemovedFromData.has(fullName);
	}

	private _isInternallyCalculated(fullName: string): boolean {
		return this._internallyCalculatedFieldFullNames.has(fullName);
	}

	private _isExternallyCalculated(fullName: string): boolean {
		return this._externalCalculationRegistry.hasCalculation(fullName);
	}
}
