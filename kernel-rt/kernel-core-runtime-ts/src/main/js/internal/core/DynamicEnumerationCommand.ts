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
import type { Logger } from "@com.mgmtp.a12.utils/utils-logging";

import type { IData, IIdentifier, IMetaField, Pair, ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { HashSet, IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { ErrorType } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { FeldUtil } from "../util/FeldUtil.js";
import type { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";

import { CheckCommand } from "./CheckCommand.js";
import { DynamicEnumerationController } from "./DynamicEnumerationController.js";
import type { IDynEnumsIntern } from "./IDynEnumsIntern.js";
import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";
import { ResultBaseImpl } from "./ResultBaseImpl.js";

export class DynamicEnumerationCommand extends CheckCommand {
	private _enumCalc: IDynEnumsIntern | undefined;

	constructor(modus: ValidationMode, metaData: IMetaDataValidierung, logger: Logger) {
		super(undefined, modus, metaData, logger);
		this._enumCalc = metaData.getDynamicEnumerationCalculator();
	}
	public calcEnumValues(field: IIdentifier, operands: IData): Pair<string, string>[] {
		const validationResult = new ResultBaseImpl();

		this._validationCache.setDaten(operands);

		const metaField: IMetaField = this._metaData.getMetaField(field.getName());
		const ids: Set<IIdentifier> = metaField.getValue(
			IMetaKeys.FIELD_SOURCE_FIELDS_FOR_DYNAMIC_ENUM,
			field
		);
		const relevantFields: HashSet<RtInternalIdentifier> = new HashSet();
		for (const id of ids) {
			const internId: RtInternalIdentifier = FeldUtil.makeInternIdentifier(id, this._metaData);
			relevantFields.add(internId);
		}
		this.formalePruefung(relevantFields, validationResult);
		if (validationResult.getCountOf(ErrorType.VALUE_ERROR) > 0) {
			return [];
		}

		const controller: DynamicEnumerationController = new DynamicEnumerationController(
			validationResult,
			this._validationCache,
			this._logger,
			this._modus,
			this._metaData
		);
		const fieldIntern: RtInternalIdentifier = FeldUtil.makeInternIdentifier(field, this._metaData);
		const result: Pair<string, string>[] = this._enumCalc
			? this._enumCalc.calcEnumerationValues(fieldIntern, controller)
			: [];
		if (validationResult.getCountOf(ErrorType.VALUE_ERROR) > 0) {
			return [];
		}
		return result;
	}
}
