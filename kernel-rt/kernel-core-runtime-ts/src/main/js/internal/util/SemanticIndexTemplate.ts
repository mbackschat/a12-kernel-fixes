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

import { RuntimeController } from "../core/RuntimeController.js";

import type { IterationState } from "./IterationState.js";
import { RtIdentifierTemplate } from "./RtIdentifierTemplate.js";
import type { RtInternalIdentifier } from "./RtInternalIdentifier.js";
import { SemanticIndexSpec } from "./SemanticIndexSpec.js";

/**
 * An instance of this class specifies the definition of a semantic index before the concretization
 * with indices.
 *
 * This class is a template to generate a {@link SemanticIndexSpec} with the method
 * {@link createSemanticIndex}.
 */
export class SemanticIndexTemplate {
	readonly _constant: SemanticIndexSpec | undefined;
	readonly _field: RtIdentifierTemplate | undefined;

	constructor(
		constant: SemanticIndexSpec | undefined,
		container: RtIdentifierTemplate | undefined
	) {
		this._constant = constant ? constant : undefined;
		this._field = container ? container : undefined;
	}

	static createNullIndex(): SemanticIndexTemplate {
		return new SemanticIndexTemplate(undefined, undefined);
	}

	static createFromConstant(constant: string | undefined): SemanticIndexTemplate {
		return new SemanticIndexTemplate(new SemanticIndexSpec(constant), undefined);
	}

	static createFromContainer(container: RtIdentifierTemplate): SemanticIndexTemplate {
		return new SemanticIndexTemplate(undefined, container);
	}

	static createFromStringRepresentation(semIndexSpec: string): SemanticIndexTemplate {
		if (!semIndexSpec) {
			return this.createFromConstant(undefined);
		} else if (semIndexSpec.startsWith('"') && semIndexSpec.endsWith('"')) {
			const semIdxSpecWithoutQuotes = semIndexSpec.substring(1, semIndexSpec.length - 1);
			return this.createFromConstant(semIdxSpecWithoutQuotes);
		} else {
			const field: RtIdentifierTemplate =
				RtIdentifierTemplate.fromStringRepresentation(semIndexSpec);
			return this.createFromContainer(field);
		}
	}

	static createFromSpec(semIdxSpec: SemanticIndexSpec | undefined): SemanticIndexTemplate {
		if (!semIdxSpec) {
			return this.createNullIndex();
		} else if (semIdxSpec.isConstant()) {
			return new SemanticIndexTemplate(semIdxSpec, undefined);
		} else {
			return this.createFromContainer(
				RtIdentifierTemplate.createFromIdentifierWithoutSemIdx(semIdxSpec.getAsEntity())
			);
		}
	}

	getField(): RtIdentifierTemplate | undefined {
		return this._field;
	}

	createSemanticIndex(iterationState?: IterationState): SemanticIndexSpec | undefined {
		if (this._constant) {
			return this._constant;
		} else if (this._field && iterationState) {
			const id: RtInternalIdentifier = RuntimeController.makeIdentifier(
				iterationState,
				this._field
			);
			return new SemanticIndexSpec(id);
		} else {
			return undefined;
		}
	}
}
