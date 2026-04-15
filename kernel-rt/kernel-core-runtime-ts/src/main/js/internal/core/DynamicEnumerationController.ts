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

import type { IMetaField } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { Pair } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { ValidatorException } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { ValidationLogger } from "../logging/ValidationLogger.js";
import type { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";
import type { VkBigDecimal } from "../util/VkBigDecimal.js";
import { VkString } from "../util/VkString.js";

import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";
import { RuntimeController } from "./RuntimeController.js";
import type { ValidationCache } from "./ValidationCache.js";

export class DynamicEnumerationController extends RuntimeController {
	public constructor(
		ergebnisse: IResult,
		valCache: ValidationCache,
		logger: ValidationLogger,
		modus: ValidationMode,
		metaData: IMetaDataValidierung
	) {
		super(ergebnisse, valCache, logger, modus, metaData);
	}

	public handleEnumerationValue(
		value: string | VkString | VkBigDecimal,
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		id: RtInternalIdentifier
	): string[] {
		if (typeof value === "string") {
			return this._handleEnumerationValue(!value, value);
		} else if (value instanceof VkString) {
			return this._handleEnumerationValue(!value.hasValue, value.wert);
		} else {
			// We want to return an empty list for the overall result, not for this partial value
			// If we have fractional digits, something went wrong.
			return this._handleEnumerationValue(
				value.wertIstUngueltig || value.scale() > 0,
				value.toFixed(0)
			);
		}
	}

	private _handleEnumerationValue(condition: boolean, value: string): string[] {
		if (condition) {
			throw new ValidatorException("Error in calculation of dynamic enumeration value!");
		}
		return [value];
	}

	public addUIValues(xmlValues: string[], id: RtInternalIdentifier): Pair<string, string>[] {
		const field: IMetaField = this.metaData.getMetaField(id.getName());
		const result: Pair<string, string>[] = [];
		if (IMetaKeys.FIELD_TYPE_ENUMERATION !== field.getValue(IMetaKeys.FIELD_FIELD_TYPE)) {
			// The simple case
			for (const xmlValue of xmlValues) {
				result.push(new Pair<string, string>(xmlValue, xmlValue));
			}
			return result;
		}
		const xmlEnumValues: string[] = field.getValue(IMetaKeys.FIELD_ENUMERATION_VALUES);
		for (const xmlEnumValue of xmlEnumValues) {
			if (xmlValues.indexOf(xmlEnumValue) !== -1) {
				result.push(new Pair<string, string>(xmlEnumValue, xmlEnumValue));
			}
		}
		return result;
	}
}
