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
import { IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { FilterPredicate } from "./FilterPredicate.js";
import { RtIdentifierTemplate } from "./RtIdentifierTemplate.js";
import { RtIdentifierTemplateLayerSpec } from "./RtIdentifierTemplateLayerSpec.js";
import { SemanticIndexTemplate } from "./SemanticIndexTemplate.js";

/**
 * Builder for class {@link RtIdentifierTemplate}.
 * It is outside of the class because otherwise we have difficulties to translate it to typescript.
 */
export class RtIdentifierTemplateBuilder {
	private _uniqueName: string | undefined;
	private _layerSpec: RtIdentifierTemplateLayerSpec[] = [];
	private _semanticIndex: SemanticIndexTemplate = SemanticIndexTemplate.createNullIndex();
	private _isField = false;
	private _category: string | undefined;
	private _filter: FilterPredicate | undefined;
	private _outer = false;

	fromContainer(container: RtIdentifierTemplate): this {
		this._uniqueName = container.getUniqueName();
		this._layerSpec = container.getLayerSpec();
		this._isField = container.isField();
		this._category = container.getCategory();
		this._outer = container.isOuter();
		return this;
	}

	unqNm(uniqueName: string): this {
		this._uniqueName = uniqueName;
		return this;
	}

	clearLayerSpec(): this {
		this._layerSpec = [];
		return this;
	}

	idx(idxStart: number, idxEnd?: number): this {
		// idxEnd will be checked in the constructor
		this._layerSpec.push(new RtIdentifierTemplateLayerSpec(idxStart, idxEnd));
		return this;
	}

	semIdx(semIdx: string | SemanticIndexTemplate | RtIdentifierTemplate): this {
		if (typeof semIdx === "string") {
			this._semanticIndex = SemanticIndexTemplate.createFromConstant(semIdx);
			return this;
		} else if (semIdx instanceof SemanticIndexTemplate) {
			this._semanticIndex = semIdx;
			return this;
		} else {
			this._semanticIndex = SemanticIndexTemplate.createFromContainer(semIdx);
			return this;
		}
	}

	isField(isField: boolean): this {
		this._isField = isField;
		return this;
	}

	category(category: string | undefined): this {
		this._category = category;
		return this;
	}

	concretizeIterationIndex(idx: number): this {
		const newLayerSpec: RtIdentifierTemplateLayerSpec[] = [];
		for (const spec of this._layerSpec) {
			if (IIdentifier.ITERATION === spec.getIdxStart()) {
				// Ebenenvariable muss ersetzt werden
				newLayerSpec.push(new RtIdentifierTemplateLayerSpec(idx));
			} else {
				newLayerSpec.push(new RtIdentifierTemplateLayerSpec(spec.getIdxStart(), spec.getIdxEnd()));
			}
		}
		this._layerSpec = newLayerSpec;
		return this;
	}

	filter(filter: FilterPredicate): this {
		this._filter = filter;
		return this;
	}

	outer(outer: boolean): this {
		this._outer = outer;
		return this;
	}

	build(): RtIdentifierTemplate {
		return new RtIdentifierTemplate(
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			this._uniqueName!,
			this._layerSpec,
			this._semanticIndex,
			this._isField,
			this._category,
			this._filter,
			this._outer
		);
	}
}
