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

import type { IData, IMetaField, IMetaModel, IResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { Document } from "../api.js";

import { EntityInstanceUtils } from "../internal/EntityInstanceUtils.js";

import { ValidationData } from "../internal/dm-runtime-service/ValidationData.js";

/**
 * Utility class with methods to convert IData to Document and vice versa.
 */

/**
 * Utility method to convert {@link ValidationData} to {@link IDocument} for a given document model id.
 *
 * @param data {@link IData} to be converted
 * @param metaModel
 * 		needed to get values for given {@link IIdentifier}s (see e.g. {@link IData#getValue(IIdentifier)}).
 * @param result 
 * 		needed to gather the problems which occur while converting the value
 * @param df
 * 		defines the external representation. If this value is undefined, no value conversion is performed. This means, the values given in <code>data</code> will be interpreted as internal values.
 * @param customFieldTypeFactory
 * 		required for the conversion of custom field types if required by `df` (either via {@link DataFormats.convertCustomFieldTypesAll} or {@link DataFormats.convertCustomFieldTypesList})
 */
export function createDocument(data: IData, metaModel: IMetaField, result: IResult, df?: DataFormats, customFieldTypeFactory?: ICustomFieldTypeFactory): Document {
	if (data instanceof ValidationData) {
		return data.document;
	}
	return EntityInstanceUtils.createDocumentFromData(data, metaModel, result, df, customFieldTypeFactory);
}

/**
 * Utility method to convert a {@link Document} to {@link IData}.
 *
 * @param document
 *        {@link IDocument} to be converted
 * @param metaModel
 * 		needed to get values for given {@link IIdentifier}s (see e.g. {@link IData#getValue(IIdentifier)}).
 * @param result
 * 		needed to be able to pass results from conversion while getting the value of a specific field instance
 * @return the corresponding {@link IData}-instance.
 */
export function createData(document: Document, metaModel: IMetaModel, result: IResult,): IData {
	return new ValidationData(document, metaModel, result);
}