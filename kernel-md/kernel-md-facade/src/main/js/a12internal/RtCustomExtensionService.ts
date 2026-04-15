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

import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { CustomFieldTypeService } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import type {
    ICustomConditionFactory
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
    CustomConditionService
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

/**
 * Service provides projects the ability to extend kernel with their own functionality.
 * 
 * @deprecated Use {@link ValidationMode.customFieldTypeFactory} and {@link ValidationMode.customConditionFactory}
 * 		instead to provide your implementations e.g. for validation or computation. This class — and consequently the ability to
 * 		statically register your {@link ICustomFieldTypeFactory} and {@link ICustomConditionFactory} — will be removed with A12K-3589.
 */
export class RtCustomExtensionService {
    /**
     * Universally registers custom field types to be available to all future validation/conversion
     * calls.
     *
     * @param customFactory The custom field type factory to be registered. May not be <code>null</code>.
     *
     * <b>Note:</b> A previously registered custom field type name cannot be overwritten with this call.
     */
    static registerCustomFieldTypes(customFactory: ICustomFieldTypeFactory): void {
        CustomFieldTypeService.getInstance().register(customFactory);
    }

    /**
     * Universally registers custom conditions to be available to all future validation calls.
     *
     * @param customFactory The custom condition factory to be registered. May not be <code>null</code>.
     *
     * <b>Note:</b> A previously registered custom condition name cannot be overwritten with this call.
     */
    static registerCustomConditions(customFactory: ICustomConditionFactory): void {
        CustomConditionService.getInstance().register(customFactory);
    }
}