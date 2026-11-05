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
import type { DataFormats } from "@com.mgmtp.a12.utils/utils-localization";

import type { IMetaField } from "../a12internal/index.js";
import type { ICustomFieldTypeFactory } from "../custom/ICustomFieldTypeFactory.js";

// A12K-2826: this interface should be removed when internal uses of the conversion are removed
export interface IMetaFieldInternalTmpA12K2645 extends IMetaField { // this is only a temporary workaround that helps us remove methods for A12K-2645 from the api quickly, and cleanup the implementation later


    /**
 * Converts the given value from the UI representation to the internal representation or vice
 * versa.
 *
 * @param data
 *            the value to be converted.
 * @param representationInfo
 *            defines the external representation. If this value is
 *            <code>null</code>,
 *            no conversion is performed.
 * @param fromInternal
 *            this value specifies whether the given data is to be transformed
 *            from internal to
 *            the external representation or vice versa.
 * @param customFieldTypeFactory
 *            required for the conversion of custom field types
 *
 * @return the converted value.
 * @throws Error
 *             if <code>data</code> doesn't contain a valid value for the
 *             field. In this case, the message of the Error will contain the error text of
 *             the formal error. The error text will be in the language specified by the given locale string.
 */
    convert(
        data: string,
        representationInfo: DataFormats,
        fromInternal: boolean,
        customFieldTypeFactory?: ICustomFieldTypeFactory,
    ): string | undefined;
}