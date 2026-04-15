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
import * as Fs from "node:fs";
import * as Path from "node:path";
import * as Assert from "node:assert";
import { fileURLToPath } from "node:url";

import { DocumentRtServiceFactory, GeneratedCodeAccessorFactory, } from "@com.mgmtp.a12.kernel/kernel-md-facade";

const __filename = fileURLToPath(import.meta.url);
const __dirname = Path.dirname(__filename);

suite("ValidatingDocument", () => {
    test("validating", () => {
        // tag::DocumentRtServiceFactoryExample[]
        // Load the generated code of a document model
        const _genCodePath = Path.join(__dirname, "..", "resources", "dm", "TestModelMDAPI.js");
        const js = Fs.readFileSync(_genCodePath, "utf8"); // The entire content of the generated code (.js file)
        const genCodeAccessor = new GeneratedCodeAccessorFactory().createScriptAccessor(js);

        // Create an instance of the service to validate and compute
        const documentRtService = DocumentRtServiceFactory.createDocumentRtService(genCodeAccessor);

        // The document to validate
        const document = {
            base: {
                Computations: {
                    Purchase: [
                        { Quantity: 2, Price: 1.23 },
                        { Quantity: 4, Price: 9.99 },
                        { Quantity: 7 }
                    ]
                }
            }
        };

        // Optional: additional field instances can be provided to the validation
        const additionalFieldInstances = new Map([
            [
                [
                    { elementName: "base", index: 1 },
                    { elementName: "Computations", index: 1 },
                    { elementName: "Purchase", index: 3 },
                    { elementName: "Price", index: 1 }
                ],
                "4.56"
            ]
        ]);

        // Call validation, either full or partial validation with given relevantEntityInstances
        const validationResult = documentRtService.validateFull(document, additionalFieldInstances);

        // Interpret validation result

        // Call computation
        const computationResult = documentRtService.compute(document);

        // Update document with computed fields
        const updatedDocument = computationResult.appliedTo(document);

        // Use the new updated document
        // end::DocumentRtServiceFactoryExample[]

        Assert.notStrictEqual(validationResult, null)
        Assert.notStrictEqual(updatedDocument, null)
    });
});