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

import {
    createSimpleCustomFieldTypeFactory,
    customFieldTypeCheckError,
    customFieldTypeConversionResult,
    DocumentRtServiceFactory,
    GeneratedCodeAccessorFactory,
    type ICustomFieldTypeCheckError,
    type ICustomFieldTypeConversionResult,
    type ICustomFieldTypeFactory,
    type ICustomFieldTypeValidationParam,
    type ICustomFieldValidator
} from "@com.mgmtp.a12.kernel/kernel-md-facade";

const __filename = fileURLToPath(import.meta.url);
const __dirname = Path.dirname(__filename);

// tag::implementCustomFieldType[]
class CheckCreditCardNumber implements ICustomFieldValidator {
    validate(value: string, valParam: ICustomFieldTypeValidationParam, isDisplayValue: boolean): ICustomFieldTypeCheckError | undefined {
        if (!isValidCreditCardNumber(value)) {
            return customFieldTypeCheckError("invalidCreditCardNumber", [{
                key: "invalidCreditCardNumber",
                defaults: {
                    en: "A credit card number must consist of 8 to 19 digits.",
                    de: "Eine Kreditkartennummer muss aus 8 bis 19 Ziffern bestehen.",
                }
            }]);
        }
        return undefined;
    }

    convertDisplay2Internal(displayValue: string): ICustomFieldTypeConversionResult {
        return customFieldTypeConversionResult(displayValue); // a no-op conversion is usually sufficient
    }

    convertInternal2Display(internalValue: string): ICustomFieldTypeConversionResult {
        return customFieldTypeConversionResult(internalValue); // a no-op conversion is usually sufficient
    }
}
// end::implementCustomFieldType[]

function isValidCreditCardNumber(value: string): boolean {
    return /^\d{8,19}$/.test(value.replaceAll(/\s+/g, ""));
}

// tag::MyCustomFieldTypeFactory[]
// option 1: implement the interface ICustomFieldTypeFactory
class MyCustomFieldTypeFactory implements ICustomFieldTypeFactory {
    createCustomFieldType(customFieldTypeName: string): ICustomFieldValidator | null {
        if (customFieldTypeName == "MyCreditCardNumber") {
            return new CheckCreditCardNumber();
        }
        return null;
    }
}

// option 2: use the simplified factory method
createSimpleCustomFieldTypeFactory({
    "MyCreditCardNumber": new CheckCreditCardNumber()
});
// end::MyCustomFieldTypeFactory[]

suite("CustomFieldValidator", () => {
    test("full validation", () => {
        const filePath = Path.join(__dirname, "..", "resources", "dm", "CustomFieldValidatorModel.js");
        const modelString = Fs.readFileSync(filePath, "utf8");
        const genCodeAccessor = new GeneratedCodeAccessorFactory().createScriptAccessor(modelString);
        const document = {
            PaymentInfo: [
                { CreditCardNumber: "1234567812345678" }, // valid
                { CreditCardNumber: "1234 5678 1234" }, // valid
                { CreditCardNumber: "123456" }, // invalid
            ]
        };
        // tag::registerCustomFieldTypes[]
        // configure the custom field type factory when creating the DocumentRtService
        const documentRtService = DocumentRtServiceFactory.createDocumentRtService(genCodeAccessor, {
            customFieldTypeFactory: new MyCustomFieldTypeFactory(),
        });
        // use this DocumentRtService for validation/computation
        const validationResult = documentRtService.validateFull(document);
        // end::registerCustomFieldTypes[]

        Assert.strictEqual(validationResult.messages.length, 1);
        Assert.strictEqual(validationResult.messages[0].errorCode, "invalidCreditCardNumber");
        Assert.deepStrictEqual(validationResult.messages[0].entityInstance, [
            { elementName: "PaymentInfo", index: 3 },
            { elementName: "CreditCardNumber", index: 1 },
        ]);
    });
});