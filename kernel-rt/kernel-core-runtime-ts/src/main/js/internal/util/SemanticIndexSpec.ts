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
import { stringconv } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { RtInternalIdentifier } from "./RtInternalIdentifier.js";

/**
 * Represents a semantic index, which can be either a string or an [IIdentifier](../../
 * kernel-core-runtime-api-ts/modules/_validation_iidentifier_.iidentifier.html) specification.
 */
export class SemanticIndexSpec {
	semIdx: RtInternalIdentifier | string | undefined;

	constructor(idOrConstantOrSpec: RtInternalIdentifier | string | SemanticIndexSpec | undefined) {
		if (typeof idOrConstantOrSpec === "string" || idOrConstantOrSpec instanceof String) {
			this.semIdx = <string>idOrConstantOrSpec;
		} else if (idOrConstantOrSpec instanceof RtInternalIdentifier) {
			this.semIdx = idOrConstantOrSpec;
		} else if (idOrConstantOrSpec === undefined) {
			this.semIdx = undefined;
		} else if (idOrConstantOrSpec.isEntity()) {
			this.semIdx = idOrConstantOrSpec.getAsEntity().clone();
		} else {
			this.semIdx = idOrConstantOrSpec.semIdx;
		}
	}

	public clone(): SemanticIndexSpec {
		const result: SemanticIndexSpec = new SemanticIndexSpec(this);
		if (this.semIdx instanceof RtInternalIdentifier) {
			result.semIdx = this.semIdx.clone();
		}
		return result;
	}

	/**
	 * Checks whether the semantic index is a string.
	 */
	public isConstant(): boolean {
		return typeof this.semIdx === "string";
	}

	/**
	 * Checks, whether the semantic index is a entity.
	 */
	public isEntity(): boolean {
		return this.semIdx instanceof RtInternalIdentifier;
	}

	/**
	 * Cast the index to a String.
	 */
	public getAsConstant(): string {
		return <string>this.semIdx;
	}

	/**
	 * Cast the index to [IIdentifier](../../kernel-core-runtime-api-ts/modules/
	 * _validation_iidentifier_.iidentifier.html).
	 */
	public getAsEntity(): RtInternalIdentifier {
		return <RtInternalIdentifier>this.semIdx;
	}

	public hashCode(): number {
		return this.semIdx == null ? 0 : this._getHashCode(this.semIdx);
	}
	private _getHashCode(semIdx: RtInternalIdentifier | string): number {
		if (typeof semIdx === "string") {
			return stringconv(semIdx);
		} else {
			return semIdx.hashCode();
		}
	}

	public equals(obj: any): boolean {
		if (this === obj) {
			return true;
		}
		if (obj === undefined) {
			return false;
		}
		const other: SemanticIndexSpec = obj;
		if (this.semIdx === undefined) {
			if (other.semIdx !== undefined) {
				return false;
			}
		} else if (this.semIdx !== other.semIdx) {
			return false;
		}
		return true;
	}

	public toString(): string {
		return this.semIdx ? this.semIdx.toString() : "";
	}
}
