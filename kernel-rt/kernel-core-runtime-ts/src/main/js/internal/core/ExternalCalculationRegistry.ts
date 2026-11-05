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

import { SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type { IData, IExternalCalculation, IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { HashMap, HashSet } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import type { ICalculator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { CalculationDependencyGraph } from "../util/CalculationSorter.js";

import { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";

import { IteratorWrapper } from "../util/IteratorWrapper.js";

import { RtExternalIdentifier } from "../util/RtExternalIdentifier.js";

import { RuntimeUtils } from "../util/RuntimeUtils.js";

import { EntityUtil } from "../util/EntityUtil.js";

import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";

import type { CalculationController } from "./CalculationController.js";
import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";
import { ResultBaseImpl } from "./ResultBaseImpl.js";

/**
 * Stores and validates the {@link IExternalCalculation}s (and changed field instances) passed to
 * {@link ICalculator#calculateAll}. Is subsequently accessed from the generated code for everything
 * related to external calculations (dependency graph & execution).
 */
export class ExternalCalculationRegistry {
    /**
     * A map from the full field name of an externally calculated field to the corresponding executor
     **/
    private _fullFieldNameToExecutor: Map<string, ExternalCalculationExecutor> = new Map();
    /**
     * A map from the full field name of an externally calculated field to the full names of fields it depends on.
     */
    private _dependencyGraph: Map<string, Set<string>> = new Map();
    /**
     * A set of all fields (full names) which are externally calculated.
     */
    private _outputFieldFullNames: Set<string> = new Set();
    /**
     * A set of all fields (full names) which are an operand to an external calculation.
     */
    private _inputFieldFullNames: Set<string> = new Set();

    constructor(
        externalCalculations: IExternalCalculation[] | undefined,
        internallyCalculatedFields: Set<string>, // full names
        expandedOperandFieldsOfInternalCalculations: Set<string>, // full names
        metaData: IMetaDataValidierung,
    ) {
        if (externalCalculations == undefined || externalCalculations.length === 0) {
            return;
        }

        const expandedOperandFields = new Set(expandedOperandFieldsOfInternalCalculations);
        externalCalculations.forEach(({ inputs }) => inputs.forEach(input => expandedOperandFields.add(input)));

        const factory = new ProcessorFactory(internallyCalculatedFields, expandedOperandFields, metaData);

        for (const externalCalculation of externalCalculations) {
            const { inputs, output } = externalCalculation;

            try {
                if (this._fullFieldNameToExecutor.has(output)) {
                    throw new Error("The output field already is the output of another external calculation.");
                }
                const outputProcessor = factory.createOutputProcessor(output);
                const inputProcessors = inputs.map(input => factory.getInputProcessor(input));

                this._outputFieldFullNames.add(output);
                inputs.forEach(input => this._inputFieldFullNames.add(input));

                this._dependencyGraph.set(output, new Set(inputs));
                this._fullFieldNameToExecutor.set(output, new ExternalCalculationExecutor(inputProcessors, outputProcessor, externalCalculation));
            } catch (e) {
                throw e instanceof Error
                    ? ExternalCalculationError.fromError(externalCalculation, e)
                    : e;
            }
        }
    }

    /**
     * @returns a map from externally calculated field (full name) to the set of fields (full names) it depends on
     * (i.e. need to be calculated before it).
     */
    public getDependencyGraph(): CalculationDependencyGraph {
        return this._dependencyGraph;
    }

    /**
     * @returns a set of all fields (full names) that are operands of external calculations.
     */
    public getOperandFieldsOfExternalCalculations(): Set<string> {
        return this._inputFieldFullNames;
    }

    /**
     * @returns a set of all fields (full names) that are outputs of external calculations.
     */
    public getExternallyCalculatedFields(): Set<string> {
        return this._outputFieldFullNames;
    }

    /**
     * Executes the stored external calculation for the given field.
     * The inputs will be taken from and the results stored in the given {@link CalculationController}.
     * The given field must be an externally calculated field (see {@link ExternalCalculationRegistry#hasCalculation}).
     *
     * @param fullFieldName full field name of an externally calculated field
     * @param calculationController the calculation controller to use for the calculation
     */
    public executeCalculation(fullFieldName: string, calculationController: CalculationController): void {
        // generated code checks for existence first
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        this._fullFieldNameToExecutor.get(fullFieldName)!.execute(calculationController);
    }

    /**
     * @returns `true`, if there are any stored external calculations, `false` otherwise
     */
    public hasCalculations(): boolean {
        return this._fullFieldNameToExecutor.size > 0;
    }

    /**
     * @returns `true`, if there is a stored external calculation for the given field, `false` otherwise
     */
    public hasCalculation(fullFieldName: string): boolean {
        return this._fullFieldNameToExecutor.has(fullFieldName);
    }

    public initializeChangedFieldInstances(
        changedFieldInstancesExtern: IIdentifier[] | undefined,
        metaData: IMetaDataValidierung
    ): void {
        const fullFieldNameToChangedInstancesIntern: Map<string, RtInternalIdentifier[]> = changedFieldInstancesExtern != undefined
            ? ExternalCalculationRegistry._checkAndInternChangedFieldInstances(changedFieldInstancesExtern, metaData)
            : new Map();
        for (const executor of this._fullFieldNameToExecutor.values()) {
            executor.initializeChangedInstances(fullFieldNameToChangedInstancesIntern);
        }
    }

    private static _checkAndInternChangedFieldInstances(
        changedFieldInstancesExtern: IIdentifier[],
        metaData: IMetaDataValidierung
    ): Map<string, RtInternalIdentifier[]> {
        const fullFieldNameToChangedInstancesIntern: Map<string, RtInternalIdentifier[]> = new Map();
        for (const idExtern of changedFieldInstancesExtern) {
            const fullFieldName = idExtern.getName();

            let changedFieldInstancesIntern = fullFieldNameToChangedInstancesIntern.get(fullFieldName);
            if (changedFieldInstancesIntern == undefined) {
                if (!metaData.isFeldDefiniert(fullFieldName)) {
                    throw new Error(`One of the changed field instances specifies the field '${fullFieldName}', which does not exist: '${RuntimeUtils.idToString(idExtern)}'`);
                }
                changedFieldInstancesIntern = [];
                fullFieldNameToChangedInstancesIntern.set(fullFieldName, changedFieldInstancesIntern);
            }

            const uniqueFieldName = metaData.getUniqueName(fullFieldName);
            const maxIndices = metaData.getMaxFieldIndices(uniqueFieldName);
            checkExternalIdentifier(idExtern, maxIndices, true, "One of the changed field instances");

            const idIntern = new RtInternalIdentifier(uniqueFieldName, idExtern.getIndices());
            changedFieldInstancesIntern.push(idIntern);
        }
        return fullFieldNameToChangedInstancesIntern;
    }

    public initializeInitialValues(
        originalData: IData
    ): void {
        const fullFieldNameToFilledInstances = ExternalCalculationRegistry._gatherFilledInstances(originalData);
        for (const executor of this._fullFieldNameToExecutor.values()) {
            executor.initializeInitialValues(fullFieldNameToFilledInstances, originalData);
        }
    }

    private static _gatherFilledInstances(originalData: IData): Map<string, IIdentifier[]> {
        const fullFieldNameToFilledInstances: Map<string, IIdentifier[]> = new Map();
        for (const id of new IteratorWrapper(originalData.iterator())) {
            const fullFieldName = id.getName();
            const instances = fullFieldNameToFilledInstances.get(fullFieldName);
            if (instances == undefined) {
                fullFieldNameToFilledInstances.set(fullFieldName, [id]);
            } else {
                instances.push(id);
            }
        }
        return fullFieldNameToFilledInstances;
    }
}

/**
 * Represents an external calculation for the purpose of its execution.
 *
 * Responsible for executing the {@link IExternalCalculation.Callback}.
 */
class ExternalCalculationExecutor {
    constructor(
        private _inputProcessors: InputProcessor[],
        private _outputProcessor: OutputProcessor,
        private _externalCalculation: IExternalCalculation,
    ) { }

    public initializeChangedInstances(fullFieldNameToChangedFieldInstances: Map<string, RtInternalIdentifier[]>): void {
        for (const inputProcessor of this._inputProcessors) {
            inputProcessor.initializeChangedInstances(fullFieldNameToChangedFieldInstances);
        }
    }

    public initializeInitialValues(
        fullFieldNameToFilledInstances: Map<string, IIdentifier[]>,
        originalData: IData
    ): void {
        this._outputProcessor.initializeInitialValues(fullFieldNameToFilledInstances, originalData);
        for (const inputProcessor of this._inputProcessors) {
            inputProcessor.initializeInitialValues(fullFieldNameToFilledInstances, originalData);
        }
    }

    public execute(calculationController: CalculationController) {
        try {
            const operands = this._inputProcessors.map(
                inputProcessor => inputProcessor.getOperand(calculationController));

            const results = this._executeCallback(operands);

            this._outputProcessor.process(results, calculationController);
        } catch (e) {
            // augment the error with the responsible external calculation and rethrow it
            throw e instanceof Error
                ? ExternalCalculationError.fromError(this._externalCalculation, e)
                : e;
        }
    }

    private _executeCallback(operands: IExternalCalculation.Operand[]): IExternalCalculation.Result[] {
        try {
            return this._externalCalculation.execute(operands);
        } catch (e) {
            // prepend context of error (i.e. in the callback) to message and rethrow
            const augmentedError = new Error(`The callback threw an error: ${e instanceof Error ? e.message : JSON.stringify(e)}`);
            if (e instanceof Error && e.stack != undefined) {
                augmentedError.stack = e.stack;
            }
            throw augmentedError;
        }
    }
}

class AbstractProcessor {
    protected uniqueName: string;
    protected formatDefinition: IFormatDefinition;
    protected innermostRepeatableGroup: string | null;
    /**
     * For every non-repeatable level below the innermost repeatable group (including the field itself),
     * stores the index 1. Simplifies creation of concrete identifiers from filled instances of the
     * innermost repeatable group.
     *
     * E.g. for /A[10]/B[10]/C[1]/D[1]/F stores [1, 1, 1]
     */
    private _nonRepeatableIndices: number[];

    constructor(
        public fullName: string,
        protected metaData: IMetaDataValidierung,
    ) {
        this.uniqueName = metaData.getUniqueName(fullName);
        this.formatDefinition = metaData.getIFormatDefinition(this.uniqueName);
        this.innermostRepeatableGroup = metaData.getInnermostRepeatableGroup(this.fullName);

        const fieldLevels = EntityUtil.countHierarchyLevels(fullName);
        const nonRepeatableLevels = this.innermostRepeatableGroup == null
            ? fieldLevels
            : fieldLevels - EntityUtil.countHierarchyLevels(this.innermostRepeatableGroup);
        this._nonRepeatableIndices = Array.from({ length: nonRepeatableLevels }, () => 1);
    }

    protected *_getExistingInstances(calculationController: CalculationController): Iterable<RtInternalIdentifier> {
        if (this.innermostRepeatableGroup == null) {
            yield new RtInternalIdentifier(this.uniqueName, this._nonRepeatableIndices);
        } else {
            for (const filledGroup of calculationController.getFilledGroupInstances(this.innermostRepeatableGroup)) {
                yield new RtInternalIdentifier(this.uniqueName, filledGroup.getIndices().concat(this._nonRepeatableIndices));
            }
        }
    }
}

/**
 * Represents a single input field of an external calculation for the purpose of execution.
 *
 * Responsible for retrieving the {@link IExternalCalculation.Operand} of the represented field
 * to be passed into {@link IExternalCalculation.Callback}.
 */
class InputProcessor extends AbstractProcessor {
    private _initialValues: HashMap<RtInternalIdentifier, string> = new HashMap();
    private _changedInstances: RtInternalIdentifier[] | null = null;

    public initializeChangedInstances(fullFieldNameToChangedFieldInstances: Map<string, RtInternalIdentifier[]>): void {
        this._changedInstances = fullFieldNameToChangedFieldInstances.get(this.fullName) ?? null;
    }

    public initializeInitialValues(
        fullFieldNameToFilledInstances: Map<string, IIdentifier[]>,
        originalData: IData
    ): void {
        const maxIndices = this.metaData.getMaxFieldIndices(this.uniqueName);
        const filledInstances = fullFieldNameToFilledInstances.get(this.fullName) ?? [];

        this._initialValues = new HashMap();
        for (const idExtern of filledInstances) {
            try {
                checkExternalIdentifier(idExtern, maxIndices, false);
            } catch {
                continue; // continue pretending the field instance is not filled
            }

            const stringValue = originalData.getValue(idExtern) ?? "";
            if (stringValue) {
                const internalId = new RtInternalIdentifier(this.uniqueName, idExtern.getIndices());
                this._initialValues.set(internalId, stringValue);
            }
        }
    }

    /**
     * @returns the {@link IExternalCalculation.Operand} for the represented field according to the
     * given {@link CalculationController}.
     */
    public getOperand(calculationController: CalculationController): IExternalCalculation.Operand {
        return {
            field: this.fullName,
            instances: this.getFieldInstances(calculationController),
        };
    }

    private getFieldInstances(calculationController: CalculationController): IExternalCalculation.FieldInstance[] {
        return Array.from(this._getExistingAndDeletedInstances(calculationController), id => {
            const currentInternalValue = calculationController.getInternalFieldValue(id);
            const currentValueIsValid = !calculationController.isInvalidField(id);
            // internal string value should always at least be convertible back to a basic value, even if invalid
            const currentBasicValue = currentInternalValue == null
                ? null
                : this.formatDefinition.convertToBasicType(currentInternalValue, this.metaData);
            return {
                field: new RtExternalIdentifier(this.fullName, id.getIndices()),
                value: currentBasicValue,
                hasChanged: this._isConsideredChanged(id)
                    || this._hasValueChanged(id, currentBasicValue, calculationController),
                isValid: currentValueIsValid,
            };
        });
    }

    private _hasValueChanged(
        id: RtInternalIdentifier,
        currentBasicValue: SupportedType,
        calculationController: CalculationController
    ): boolean {
        const idExtern = new RtExternalIdentifier(this.fullName, id.getIndices());
        const initialDisplayValue = this._initialValues.get(id) ?? "";
        const initialValueCheck = calculationController.formalChecker.checkAndConvertFieldValue(id, idExtern, initialDisplayValue, new ResultBaseImpl());
        const initialInternalValue = initialValueCheck.internalXmlValue ?? null;

        // Note: we can't easily distinguish between unfilled and invalid initial values because the check here only
        // provides the syntactic validity of the value, which would not suffice for index fields
        const initialValueIsUnfilledOrInvalid = initialInternalValue === null || initialInternalValue === "";
        const currentValueIsUnfilled = currentBasicValue === null || currentBasicValue === "";
        if (initialValueIsUnfilledOrInvalid || currentValueIsUnfilled) {
            return initialValueIsUnfilledOrInvalid !== currentValueIsUnfilled;
        }

        const initialBasicValue = this.formatDefinition.convertToBasicType(initialInternalValue, this.metaData);
        return !SupportedType.equals(initialBasicValue, currentBasicValue);
    }

    private _isConsideredChanged(id: RtInternalIdentifier): boolean {
        return this._changedInstances?.some(changedId => changedId.umfasst(id)) ?? false;
    }

    private *_getExistingAndDeletedInstances(calculationController: CalculationController): Iterable<RtInternalIdentifier> {
        for (const originallyFilledInstance of this._initialValues.keys()) {
            yield originallyFilledInstance;
        }

        for (const existingInstance of this._getExistingInstances(calculationController)) {
            if (!this._initialValues.has(existingInstance)) {
                yield existingInstance;
            }
        }
    }
}

/**
 * Represents a single output field of an external calculation for the purpose of execution.
 *
 * Responsible for validating and applying the {@link IExternalCalculation.Result}s returned by
 * {@link IExternalCalculation.Callback}.
 */
class OutputProcessor extends AbstractProcessor {
    private _maxIndices: number[];
    private _isIndexField: boolean;

    private _initialValues: HashMap<RtInternalIdentifier, string> = new HashMap();

    constructor(
        fullName: string,
        metaData: IMetaDataValidierung,
        private _isOperand: boolean,
    ) {
        super(fullName, metaData);
        this._maxIndices = metaData.getMaxFieldIndices(this.uniqueName);
        this._isIndexField = metaData.isIndexField(this.uniqueName);
    }

    public initializeInitialValues(
        fullFieldNameToFilledInstances: Map<string, IIdentifier[]>,
        originalData: IData
    ): void {
        const filledInstances = fullFieldNameToFilledInstances.get(this.fullName) ?? [];

        this._initialValues = new HashMap();
        if (this._isOperand || this._isIndexField) {
            for (const idExtern of filledInstances) {
                try {
                    checkExternalIdentifier(idExtern, this._maxIndices, false);
                } catch {
                    continue; // continue pretending the field instance is not filled
                }

                const stringValue = originalData.getValue(idExtern) ?? "";
                if (stringValue) {
                    const internalId = new RtInternalIdentifier(this.uniqueName, idExtern.getIndices());
                    this._initialValues.set(internalId, stringValue);
                }
            }
        }
    }

    /**
     * Validates and applies the given results.
     * @param results the {@link IExternalCalculation.Result}s returned by {@link IExternalCalculation.Callback}
     * @param calculationController the {@link CalculationController} to be used for calculation
     */
    public process(results: IExternalCalculation.Result[], calculationController: CalculationController) {
        const existingInstances = Array.from(this._getExistingInstances(calculationController));
        const calculatedInstances: HashSet<RtInternalIdentifier> = new HashSet();
        for (const result of results) {
            const { field: idExtern, value: basicValue } = result;

            this._checkResultPath(idExtern);
            this._checkIndices(idExtern);

            const effectiveBasicValue = basicValue === "" ? null : basicValue;
            // can throw an Error if the type does not match --> considered integration error
            const internalValue = this.formatDefinition.convertFromBasicType(effectiveBasicValue, this.metaData);

            for (const idIntern of this._resolveWildcards(idExtern, existingInstances)) {
                if (calculatedInstances.tryAdd(idIntern)) {
                    calculationController.handleExternallyCalculatedValue(internalValue, idIntern);
                }
            }
        }

        // deferred formal validation of field instances that were not calculated
        const erroneousInstances: RtInternalIdentifier[] = [];
        if (this._isOperand || this._isIndexField) {
            for (const [idIntern, displayValue] of this._initialValues) {
                if (!calculatedInstances.has(idIntern)) {
                    const idExtern = new RtExternalIdentifier(this.fullName, idIntern.getIndices());
                    const isFormallyValid = calculationController.handleDeferredFormalValidation(idIntern, idExtern, displayValue);
                    if (!isFormallyValid) {
                        erroneousInstances.push(idIntern);
                    }
                }
            }
        }

        if (this._isIndexField) {
            calculatedInstances.forEach(id => {
                if (calculationController.isInvalidField(id)) {
                    erroneousInstances.push(id);
                }
            });
            calculationController.updateCalculatedIndexField(this.fullName, calculatedInstances, erroneousInstances);
        }
    }

    private _checkResultPath(idExtern: IIdentifier): void {
        const resultPath = idExtern.getName();
        if (this.fullName !== resultPath) {
            throw new Error(`Result for path '${resultPath}'.`);
        }
    }

    private _checkIndices(idExtern: IIdentifier): void {
        checkExternalIdentifier(idExtern, this._maxIndices, true, "The callback returned a field instance that");
    }

    /**
     * @return all instances that match the identifier `idExtern`.
     */
    private _resolveWildcards(idExtern: IIdentifier, instances: RtInternalIdentifier[]): RtInternalIdentifier[] {
        const idIntern = new RtInternalIdentifier(this.uniqueName, idExtern.getIndices());
        return instances.filter(instance => idIntern.umfasst(instance));
    }
}

/**
 * Creates and validates {@link InputProcessor}s and {@link OutputProcessor}s.
 */
class ProcessorFactory {
    private _fullFieldNameToInputProcessor: Map<string, InputProcessor> = new Map();

    /**
     * @throws if one of the changed field instances is invalid
     */
    constructor(
        private _internallyCalculatedFieldNames: Set<string>,
        private _expandedOperandFields: Set<string>,
        private _metaData: IMetaDataValidierung,
    ) { }

    /**
     * @returns the {@link InputProcessor} for the given field. Multiple calls will return the same instance.
     */
    public getInputProcessor(fullFieldName: string): InputProcessor {
        let result = this._fullFieldNameToInputProcessor.get(fullFieldName);
        if (result == undefined) {
            result = this.createInputProcessor(fullFieldName);
            this._fullFieldNameToInputProcessor.set(fullFieldName, result);
        }
        return result;
    }

    /**
     * @returns the {@link InputProcessor} for the given field.
     * @throws if the given field does not exist
     */
    public createInputProcessor(fullFieldName: string): InputProcessor {
        if (!this._metaData.isFeldDefiniert(fullFieldName)) {
            throw new Error(`The input field '${fullFieldName}' does not exist.`);
        }

        return new InputProcessor(fullFieldName, this._metaData);
    }

    /**
     * @returns the {@link OutputProcessor} for the given field.
     * @throws if the given field does not exist or is not a valid output field
     */
    public createOutputProcessor(fullFieldName: string): OutputProcessor {
        if (!this._metaData.isFeldDefiniert(fullFieldName)) {
            throw new Error("The output field does not exist.");
        }

        if (this._internallyCalculatedFieldNames.has(fullFieldName)) {
            throw new Error("The output field must not be a calculated field in the Document Model.");
        }

        return new OutputProcessor(fullFieldName, this._metaData, this._expandedOperandFields.has(fullFieldName));
    }
}

function checkExternalIdentifier(idExtern: IIdentifier, maxIndices: number[], allowsWildcards: boolean, errorContext = "An IIdentifier"): void {
    const indices = idExtern.getIndices();
    if (indices.length !== maxIndices.length) {
        throw new Error(`${errorContext} does not specify the correct number of indices: ${JSON.stringify(idExtern)} --> expected ${maxIndices.length} but was ${indices.length}`);
    }
    const minIndex = allowsWildcards ? 0 : 1;
    for (let level = 0; level < indices.length; level++) {
        if (indices[level] < minIndex) {
            throw new Error(`${errorContext} has an index < ${minIndex}: '${RuntimeUtils.idToString(idExtern)}'`);
        }
        if (indices[level] > maxIndices[level]) {
            throw new Error(`${errorContext} has at least one index larger than the field's repeatability allows: '${RuntimeUtils.idToString(idExtern)}' --> expected max of ${maxIndices[level]} at level ${level + 1} but was ${indices[level]}`);
        }
    }
}

/**
 * An Error augmented with the external calculation that caused it.
 */
export class ExternalCalculationError extends Error {
    private constructor(
        public externalCalculation: IExternalCalculation,
        public rawMessage: string,
        stack?: string,
    ) {
        super(`Error in external calculation for field '${externalCalculation.output}': ${rawMessage}`);
        if (stack != undefined) {
            this.stack = stack;
        }
    }

    public static fromError(calculation: IExternalCalculation, error: Error): ExternalCalculationError {
        return new ExternalCalculationError(calculation, error.message, error.stack);
    }
}