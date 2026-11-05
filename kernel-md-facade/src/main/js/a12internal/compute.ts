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
import type { ModelPath } from "@com.mgmtp.a12.base/base-model-api";

import type { Document, DocumentComputationResult, DocumentRtService, EntityInstancePath, FieldInstanceValue, GeneratedCodeRtConfig, IGeneratedCodeAccessor } from "../api.js";
import { DocumentRtServiceImpl } from "../internal/dm-runtime-service/DocumentRtServiceImpl.js";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { DocumentRtServiceFactory } from "../facade.js";

// The API specified here mirrors the API in @kernel-core-runtime-api-ts/IExternalCalculation.ts
// Please check if changes made here need to be applied there as well.

/**
 * Specifies a computation that is not part of the Document Model but should be executed during computation
 * (see {@link DocumentRtServiceA12internal#computeA12internal}).
 */
export interface ExternalComputation {
    /**
     * Specifies the fields the {@link ExternalComputation} depends on as inputs.
     *
     * For example for dependent fields in Form Models, the inputs are
     * - a master field that triggers changes
     * - an optional "value source" field, i.e. a field whose value will be applied to the output field
     */
    readonly inputs: ModelPath[];

    /**
     * Specifies the output field of the {@link ExternalComputation}.
     * 
     * The field must not be the output of another computation or {@link ExternalComputation}.
     *
     * For example for dependent fields in Form Models, this would be the dependent field itself.
     */
    readonly output: ModelPath;

    /**
     * A callback that computes changes to be applied to instances of the output field.
     * 
     * The callback will be invoked exactly once per invocation of {@link DocumentRtServiceA12internal#computeA12internal}
     * (assuming no fatal errors occurred), i.e. it must properly handle repeatable fields.
     *
     * At the time the callback is invoked, it is guaranteed that all input fields have already been computed (if necessary).
     */
    execute: ExternalComputation.Callback;
}

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace ExternalComputation {
    /**
     * The parameter *operands* contains an {@link Operand} for every input field, in the order they were specified in {@link ExternalComputation.inputs}.
     * 
     * The return value may specify an arbitrary number of {@link Result}s, meaning that changes to the output field are optional.
     * For all {@link Result}s, {@link Result.field} must be an instance of {@link ExternalComputation.output}.
     * If multiple {@link Result}s target the same field instance, the field instance's value will be determined by the first {@link Result} targeting it.
    */
    export type Callback = (operands: Operand[]) => Result[];

    /**
     * Provides access to all instances of {@link Operand.field} that:
     *  - exist in the Document at the time {@link ExternalComputation#execute} is invoked, or
     *  - were filled in the Document passed to {@link DocumentRtServiceA12internal#computeA12internal}.
     * 
     * <b>Note:</b> A repeatable field instance *exists* if its innermost repeatable group is filled, even if the field instance itself is not *filled*.
     */
    export interface Operand {
        readonly field: ModelPath;
        readonly instances: FieldInstance[];
    }

    /**
     * Provides access to a single field instance (i.e. a field with an indexed path into the Document and a value).
     */
    export interface FieldInstance {
        readonly field: EntityInstancePath;
        readonly value: FieldInstanceValue;

        /**
         * Indicates whether the field instance's value has changed compared to the Document provided to {@link DocumentRtServiceA12internal#computeA12internal}.
         * If that initial value is invalid, it is considered to be `null` for the purposes of this comparison.
         */
        readonly hasChanged: boolean;

        /**
         * Indicates whether the field instance's value is valid.
         */
        readonly isValid: boolean;
    }

    export type Result = FieldResult; // can be extended for groups if necessary at some point in the future

    /**
     * Specifies a change to be made to one or more existing field instances in the Document.
     */
    export interface FieldResult {
        readonly type: "Field";

        /**
         * The existing field instance(s) which *value* should be applied to.
         * 
         * Allows the specification of indices as `0` to set them as wildcards. The set of field instances to change will then consist
         * of all *existing* field instances of the target field that match the other (explicitly set) indices.
         * 
         * If *field* does not match any existing instance (even if no wildcards are used), no changes will be made, and no error will be thrown.
         * 
         * <b>Note:</b> A repeatable field instance *exists* if its innermost repeatable group is filled, even if the field instance itself is not *filled*.
         */
        readonly field: EntityInstancePath;

        /**
         * The value to be applied to the field instance(s) described by *field*.
         * 
         * For the provided values it is assumed that they:
         * - have normalized linebreaks (`\n` instead of `\r\n`)
         * - do not start with a blank (" ")
         * - do not end with a blank (" ")
         * - only contain legal characters (as defined in the Document Model)
         */
        readonly value: FieldInstanceValue;
    }
}


export interface DocumentRtServiceA12internal extends DocumentRtService {
    /**
     * Analogous to {@link DocumentRtService.compute}, but also allows the specification of {@link ExternalComputation}s.
     * 
     * @param externalComputations A list of {@link ExternalComputation}s that will be processed together with the Document Model's computations.
     *  All operands of {@link ExternalComputation}s (i.e. fields in {@link ExternalComputation.inputs}) will be formally validated.
     * @param changedFieldInstances A list of field instances that should always be considered as changed for {@link Operand.hasChanged}.
     *  Allows the specification of indices as `0` to set them as wildcards. The {@link EntityInstancePath} then describes the set of field instances
     *  that match the other (explicitly set) indices.
     * 
     * Throws {@link Error}
     * - if the combined dependency graph of the specified {@link ExternalComputation}s and the Document Model's computations would produce a cycle
     * - if {@link ExternalComputation#execute} throws an error or returns invalid {@link ExternalComputation.Result}s
     * - if {@link ExternalComputation.inputs} or {@link ExternalComputation.output} specify fields that do not exist in the model
     * - if {@link ExternalComputation.output} is already a computed field in the Document Model or output for another {@link ExternalComputation}
     * - if *changedFieldInstances* contains an invalid {@link EntityInstancePath}
     * 
     * This list is not guaranteed to be exhaustive.
     * 
     * **Note:** If a field is externally computed, the formal validation of corresponding field instances contained in `document` is deferred until
     * after the external computation, so that the initially invalid values may be overridden. Formal validation for these field instances is skipped
     * altogether if the externally computed field is neither an operand for another computation nor an index field.
     * 
     * **Note:** If an index field is made invalid by an external computation (invalid value, empty value, or non-unique value), those computations
     * in the Document Model that directly or indirectly rely on this index field will not be executed.
     */
    computeA12internal(document: Document, externalComputations?: ExternalComputation[], changedFieldInstances?: EntityInstancePath[]): DocumentComputationResult;
}

export class DocumentRtServiceFactoryA12internal {
    /**
     * Analogous to {@link DocumentRtServiceFactory.createDocumentRtService}, but creates a {@link DocumentRtServiceA12internal} instead of {@link DocumentRtService}.
     */
    static createDocumentRtService(genCodeAccessor: IGeneratedCodeAccessor, config?: GeneratedCodeRtConfig): DocumentRtServiceA12internal {
        return new DocumentRtServiceImpl(genCodeAccessor, config);
    }
}