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

import type { SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type { IIdentifier, IIdentifierWithSemanticIndex } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { arraysEqual, stringconv } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { RtExternalIdentifier } from "./RtExternalIdentifier.js";

/**
 * An external identifier whose repetition is determined by a semantic index that could not be
 * resolved (its indices contain {@link IIdentifier.UNKNOWN}). It additionally carries the index field
 * and the index value that were used to look up the repetition.
 */
export class RtExternalIdentifierWithSemanticIndex extends RtExternalIdentifier implements IIdentifierWithSemanticIndex {

	constructor(
        name: string,
        indices: number[],
        private readonly _indexField: IIdentifier,
        private readonly _indexValue: string | undefined,
        private readonly _indexValueAsBasicType: SupportedType | undefined,
    ) {
		super(name, indices);
		this._indexValue = RtExternalIdentifierWithSemanticIndex.normalizeIndexValue(_indexValue);
	}

	public static normalizeIndexValue(indexValue: string | undefined): string | undefined {
		return indexValue === "" ? undefined : indexValue;
	}

	public getIndexField(): IIdentifier {
		return this._indexField;
	}

	public getIndexValue(): string | undefined {
		return this._indexValue;
	}

	public getIndexValueAsBasicType(): SupportedType | undefined {
		return this._indexValueAsBasicType ?? undefined;
	}

	public override hashCode(): number {
		const prime = 31;
		let result = super.hashCode();
		result = prime * result + (this._indexField == undefined ? 0 : this._indexField.hashCode());
		result = prime * result + (this._indexValue == undefined ? 0 : stringconv(this._indexValue));
		return result;
	}

	public override equals(obj: any): boolean {
		if (this === obj) {
			return true;
		}
		if (obj == undefined) {
			return false;
		}
		if (!(obj instanceof RtExternalIdentifierWithSemanticIndex)) {
			return false;
		}
		const other: RtExternalIdentifierWithSemanticIndex = obj;
		if (this.getName() !== other.getName()) {
			return false;
		}
		if (!arraysEqual(this.getIndices(), other.getIndices())) {
			return false;
		}
		if (!this._identifiersEqual(this._indexField, other.getIndexField())) {
			return false;
		}
		if (this._indexValue !== other.getIndexValue()) {
			return false;
		}
		return true;
	}

	private _identifiersEqual(a: IIdentifier | undefined, b: IIdentifier | undefined): boolean {
		if (a === b) {
			return true;
		}
		if (a == undefined || b == undefined) {
			return false;
		}
		return a.equals(b);
	}
}
