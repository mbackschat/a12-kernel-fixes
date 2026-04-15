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

import { IIdentifier, numbersconv } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { Constants } from "./Constants.js";
import type { FilterPredicate } from "./FilterPredicate.js";
import { RtIdentifierTemplateBuilder } from "./RtIdentifierTemplateBuilder.js";
import type { RtIdentifierTemplateLayerSpec } from "./RtIdentifierTemplateLayerSpec.js";
import type { RtInternalIdentifier } from "./RtInternalIdentifier.js";
import { SemanticIndexTemplate } from "./SemanticIndexTemplate.js";

/**
 * This class is used as a container for {@link RtInternalIdentifier} in generated code. Indexes
 * may not be specific here, but can be specified as an interval, iteration or for all.
 *
 * An instance of this class is a template to generate a {@link RtInternalIdentifier}.
 */
export class RtIdentifierTemplate {
	private static readonly SEMANTIC_INDEX_STRING: string = String(IIdentifier.SEMANTIC_INDEX);
	private static readonly PARALLEL_ITERATION_STRING: string = String(
		IIdentifier.PARALLEL_ITERATION
	);
	private static readonly INDEX_TRENNER: string = "-";

	static builder(): RtIdentifierTemplateBuilder {
		return new RtIdentifierTemplateBuilder();
	}

	/**
	 * This method should be used for the translation of error message field references only.
	 *
	 * @param entityStr 	The string representation of the entity.
	 * @return The corresponding {@link RtIdentifierTemplate}.
	 */
	static fromStringRepresentation(entityStr: string): RtIdentifierTemplate {
		const builder: RtIdentifierTemplateBuilder = this.builder();

		let catEntity: string = entityStr;
		if (catEntity.includes(Constants.SEMANTIC_INDEX_SEPARATOR)) {
			const idx: number = catEntity.indexOf(Constants.SEMANTIC_INDEX_SEPARATOR);
			catEntity = entityStr.substring(0, idx);
			const semIdxStr: string = entityStr.substring(idx + 1);
			builder.semIdx(SemanticIndexTemplate.createFromStringRepresentation(semIdxStr));
		}

		let entity: string = catEntity;
		if (entity.includes(Constants.CATEGORY_VALUE_ACCESS)) {
			const idx: number = catEntity.indexOf(Constants.CATEGORY_VALUE_ACCESS);
			entity = catEntity.substring(0, idx);
			builder.category(catEntity.substring(idx + Constants.CATEGORY_VALUE_ACCESS.length));
		}

		const teile: string[] = entity.trim().split(Constants.FIELD_LAYER_SEPARATOR);
		builder.unqNm(teile[0]);
		for (let i = 1; i < teile.length; i++) {
			const layerSpec: number[] = this._parseLayerSpec(teile[i]);
			builder.idx(layerSpec[0], layerSpec[1]);
		}
		return builder.build();
	}

	private static _parseLayerSpec(ebenenSpecAlsString: string): number[] {
		if ("e" === ebenenSpecAlsString) {
			return [IIdentifier.ITERATION, IIdentifier.ITERATION];
		} else if (RtIdentifierTemplate.SEMANTIC_INDEX_STRING === ebenenSpecAlsString) {
			return [IIdentifier.SEMANTIC_INDEX, IIdentifier.SEMANTIC_INDEX];
		} else if (RtIdentifierTemplate.PARALLEL_ITERATION_STRING === ebenenSpecAlsString) {
			return [IIdentifier.PARALLEL_ITERATION, IIdentifier.PARALLEL_ITERATION];
		} else {
			const idx: string[] = ebenenSpecAlsString.split(RtIdentifierTemplate.INDEX_TRENNER);
			return [Number.parseInt(idx[0], 10), Number.parseInt(idx[idx.length - 1], 10)];
		}
	}

	private readonly _uniqueName: string;
	private readonly _layerSpec: RtIdentifierTemplateLayerSpec[];
	private readonly _semanticIndex: SemanticIndexTemplate;
	private readonly _isField: boolean;
	private readonly _category: string | undefined;
	private readonly _aggregation: boolean;
	private readonly _filter: FilterPredicate | undefined;
	private readonly _outer: boolean;

	constructor(
		uniqueName: string,
		layerSpec: RtIdentifierTemplateLayerSpec[],
		semanticIndex: SemanticIndexTemplate,
		isField: boolean,
		category: string | undefined,
		filter: FilterPredicate | undefined,
		outer: boolean
	) {
		this._uniqueName = uniqueName;
		this._layerSpec = layerSpec;
		this._semanticIndex = semanticIndex;
		this._isField = isField;
		this._category = category;
		this._aggregation =
			!layerSpec.every(ls => ls.getIdxStart() === ls.getIdxEnd());
		this._filter = filter;
		this._outer = outer
	}

	getUniqueName(): string {
		return this._uniqueName;
	}

	getLayerSpec(): RtIdentifierTemplateLayerSpec[] {
		return this._layerSpec;
	}

	getSingleLayerSpec(idx: number): RtIdentifierTemplateLayerSpec {
		return this._layerSpec[idx];
	}

	getSemanticIndex(): SemanticIndexTemplate {
		return this._semanticIndex;
	}

	isField(): boolean {
		return this._isField;
	}

	getCategory(): string | undefined {
		return this._category;
	}

	isAggregation(): boolean {
		return this._aggregation;
	}

	getFilter(): FilterPredicate | undefined {
		return this._filter;
	}

	isOuter(): boolean {
		return this._outer;
	}

	static createFromIdentifierWithoutSemIdx(entity: RtInternalIdentifier): RtIdentifierTemplate {
		const builder: RtIdentifierTemplateBuilder = RtIdentifierTemplate.builder()
			.category(entity.getCategory())
			.isField(!entity.getName().startsWith("/"))
			.unqNm(entity.getName());

		for (const idx of entity.getIndices()) {
			builder.idx(idx);
		}
		return builder.build();
	}

	hashCode(): number {
		return numbersconv(this);
	}

	equals(obj: any): boolean {
		// We ignore semantic indices here and in hashCode to ease value collection in ValidationCache.
		if (this === obj) {
			return true;
		}
		if (obj == undefined) {
			return false;
		}

		const other: RtIdentifierTemplate = obj;
		if (this._uniqueName == undefined) {
			if (other.getUniqueName() != undefined) {
				return false;
			}
		} else if (this._uniqueName !== other.getUniqueName()) {
			return false;
		}
		if (other._layerSpec !== this.getLayerSpec()) {
			return false;
		}
		if (other._semanticIndex !== this.getSemanticIndex()) {
			return false;
		}
		if (other._isField !== this.isField()) {
			return false;
		}
		if (other._category !== this.getCategory()) {
			return false;
		}
		return true;
	}

	toString(): string {
		return (
			"RtIdentifierTemplate(name: " +
			this._uniqueName +
			", layers: " +
			this._layerSpec +
			", semIdx: " +
			this._semanticIndex +
			", cat: " +
			this._category +
			")"
		);
	}
}
