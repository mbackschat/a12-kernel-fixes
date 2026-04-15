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
import type { IData, IIdentifier, ICustomCondition as RtCustomCondition } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { IMetaKeysInternal } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import type { ICustomCondition as MdCustomCondition } from "../../api.js";

import { EntityInstanceConverter } from "../EntityInstanceConverter.js";

import { ValidationData } from "./ValidationData.js";


export class CustomConditionWrapper implements RtCustomCondition {
    constructor(public customCondition: MdCustomCondition) { }

    check(data: IData, relevantEntities: Set<IIdentifier> | undefined, formallyIncorrectFields: Set<IIdentifier>, errorField: IIdentifier): boolean {
        if (!(data instanceof ValidationData)) {
            throw new Error(
                "A custom condition provided through the md-API must not be used by validation calls which bypass the md-API.");
        }
        const validationData: ValidationData = data;
        const document = validationData.document;
        const metaModel = validationData.metaModel;
        const valuesOfFieldInstancesToConsider = validationData.valuesOfFieldInstancesToConsider;

        const modelId: string = metaModel.getValue(IMetaKeysInternal.MODEL_ID);
        const relevantEntityInstances = relevantEntities == undefined ? undefined : EntityInstanceConverter.convertToEntityInstanceArray(relevantEntities);
        const formallyIncorrectEntityInstances = EntityInstanceConverter.convertToEntityInstanceArray(formallyIncorrectFields);
        const errorEntityInstance = EntityInstanceConverter.convertToEntityInstance(errorField);

        return this.customCondition.check(document, modelId, relevantEntityInstances, formallyIncorrectEntityInstances, errorEntityInstance, valuesOfFieldInstancesToConsider);
    }
}