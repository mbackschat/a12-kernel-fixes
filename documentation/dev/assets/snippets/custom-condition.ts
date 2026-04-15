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
import * as Assert from "node:assert";
import * as Fs from "node:fs";
import * as Path from "node:path";
import { fileURLToPath } from "node:url";

import { ModelPath } from "@com.mgmtp.a12.base/base-model-api";

import {
    createCustomConditionFactory,
    Document,
    DocumentRtServiceFactory,
    DocumentServiceFactory,
    EntityInstancePath,
    EntityInstanceValueMapping,
    GeneratedCodeAccessorFactory,
    ICustomCondition,
    ICustomConditionFactory
} from "@com.mgmtp.a12.kernel/kernel-md-facade";

const __filename = fileURLToPath(import.meta.url);
const __dirname = Path.dirname(__filename);

// tag::IsNotReverseCustomCondition[]
class IsNotReverseCustomCondition implements ICustomCondition {
    private readonly _documentService = new DocumentServiceFactory().getDocumentService();
    private _fieldPaths: ModelPath[];
    private _searchPaths: EntityInstancePath[];

    constructor(
        fieldPath1: ModelPath,
        fieldPath2: ModelPath,
    ) {
        this._fieldPaths = [fieldPath1, fieldPath2];
        this._searchPaths = this._fieldPaths.map(path => path.map(({ elementName }) => ({ elementName, index: 0 })));
    }

    public check(
        document: Document,
        _documentModelId: string,
        relevantEntityInstances: EntityInstancePath[] | undefined,
        formallyIncorrectEntityInstances: EntityInstancePath[],
        _errorEntityInstance: EntityInstancePath,
        _valuesOfFieldInstancesToConsider: EntityInstanceValueMapping | undefined
    ): boolean {
        // a) necessary fields are in relevantEntityInstances OR all fields are relevant
        if (relevantEntityInstances !== undefined && relevantEntityInstances.every(relevantEntity =>
            this._fieldPaths.some(field => ModelPath.equal(field, relevantEntity))
        )) {
            return false;
        }

        // b) necessary fields not in formallyIncorrectEntityInstances
        if (formallyIncorrectEntityInstances.some(formallyIncorrectEntity =>
            this._fieldPaths.some(field => ModelPath.equal(field, formallyIncorrectEntity))
        )) {
            return false;
        }

        // c) condition
        const fieldValues = this._fieldValuesFromDocument(document);
        if (fieldValues == null) {
            return false;
        }

        const [field1, field2] = fieldValues;
        return field1 !== field2.split('').reverse().join('');
    }

    private _fieldValuesFromDocument(
        document: Document
    ): string[] | null {
        const instancesByField = this._searchPaths.map(searchPath =>
            this._documentService.getEntityPathsInDocument(document, searchPath));
        if (instancesByField.some(fieldInstances => fieldInstances.length !== 1)) {
            return null;
        }

        const fieldValues = instancesByField.map(([fieldInstance]) =>
            this._documentService.getAssignedObject(document, fieldInstance));
        if (fieldValues.some(fieldValue => typeof fieldValue !== "string")) {
            return null;
        }

        return <string[]>fieldValues;
    }
}

// end::IsNotReverseCustomCondition[]

// tag::IsNotReverseCustomConditionFactory[]
// option 1: implement the interface ICustomConditionFactory
class IsNotReverseCustomConditionFactory implements ICustomConditionFactory {
    public createCustomCondition(customConditionName: string): ICustomCondition | null {
        switch (customConditionName) {
            case "NotReverse":
                return new IsNotReverseCustomCondition(
                    [{ elementName: "Group1" }, { elementName: "Field1" }],
                    [{ elementName: "Group1" }, { elementName: "Field2" }],
                );
            case "NotReverse2":
                return new IsNotReverseCustomCondition(
                    [{ elementName: "Group1" }, { elementName: "Field1" }],
                    [{ elementName: "Group1" }, { elementName: "Field3" }],
                );
            default:
                return null;
        }
    }
}

// option 2: use the simplified factory method
createCustomConditionFactory({
    "NotReverse": new IsNotReverseCustomCondition(
        [{ elementName: "Group1" }, { elementName: "Field1" }],
        [{ elementName: "Group1" }, { elementName: "Field2" }],
    ),
    "NotReverse2": new IsNotReverseCustomCondition(
        [{ elementName: "Group1" }, { elementName: "Field1" }],
        [{ elementName: "Group1" }, { elementName: "Field3" }],
    ),
})
// end::IsNotReverseCustomConditionFactory[]

suite("CustomCondition", () => {
    test("full validation", () => {
        const filePath = Path.join(__dirname, "..", "resources", "dm", "CustomIsReverseTestModel.js");
        const modelString = Fs.readFileSync(filePath, "utf8");
        const genCodeAccessor = new GeneratedCodeAccessorFactory().createScriptAccessor(modelString);
        const document = {
            Group1: {
                Field1: "abcdef",
                Field2: "fedcba",
                Field3: "fed",
            }
        };
        // tag::registerCustomConditions[]
        // configure the custom condition factory when creating the DocumentRtService
        const documentRtService = DocumentRtServiceFactory.createDocumentRtService(genCodeAccessor, {
            customConditionFactory: new IsNotReverseCustomConditionFactory(),
        });
        // use this DocumentRtService for validation/computation
        const validationResult = documentRtService.validateFull(document);
        // end::registerCustomConditions[]

        Assert.strictEqual(validationResult.messages.length, 1);
    });
});
