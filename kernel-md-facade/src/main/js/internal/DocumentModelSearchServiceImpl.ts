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
import type { ModelPath } from "@com.mgmtp.a12.base/base-model-api";

import type { DocumentModel, DocumentModelSearchService } from "../api.js";

export class DocumentModelSearchServiceImpl implements DocumentModelSearchService {
	private readonly _modelPathsById: Map<string, ModelPath> = new Map<string, ModelPath>();
	constructor(private documentModel: DocumentModel) { }
	getPathById(id: string): ModelPath | undefined {
		this._initCachePathById();
		return this._modelPathsById.get(id);
	}
	private _initCachePathById(): void {
		if (this._modelPathsById.size === 0) {
			this._fillCachePathById(this.documentModel.content.modelRoot, []);
		}
	}
	private _fillCachePathById(element: DocumentModel.Group, modelPath: ModelPath): void {
		for (const child of element.elements) {
			const _currentModelPath = [...modelPath, { elementName: child.name }];
			this._modelPathsById.set(child.id, _currentModelPath);
			if (child.type === "Group") {
				this._fillCachePathById(child, _currentModelPath);
			}
		}
	}
	getByPath(path: ModelPath): DocumentModel.Element | undefined {
		// this kind of search is more targeted, that's why no cache
		if (path.length === 0) {
			return undefined;
		}
		let element: DocumentModel.Element | undefined = this.documentModel.content.modelRoot;
		for (const { elementName } of path) {
			if (element === undefined || element.type !== "Group") {
				return undefined;
			}
			element = element.elements.find(el => el.name === elementName);
		}
		return element;
	}
}
