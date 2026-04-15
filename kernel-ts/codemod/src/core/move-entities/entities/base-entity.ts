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

import assert from "assert";

import {
	Collection,
	Identifier,
	ImportDeclaration,
	ImportSpecifier,
	JSCodeshift,
	MemberExpression
} from "jscodeshift";

import { UniversalImportSpecifier } from "../utils";

export interface ImportMetaData {
	declarations: Collection<ImportDeclaration>;
	localEntryPoint: string;
}

export class BaseEntity {
	protected readonly segments: string[];
	protected importMetadata: ImportMetaData | undefined;

	constructor(
		protected j: JSCodeshift,
		protected root: Collection,
		private entitySource: string,
		entityPath: string
	) {
		this.segments = entityPath.split(".");
	}

	public get publicEntryPoint() {
		return this.segments[0];
	}

	public get importDeclarations() {
		return this.importMetadata?.declarations;
	}

	public createMemberExpression(localEntryPoint: string): MemberExpression | Identifier {
		let result: Identifier | MemberExpression = this.j.identifier(localEntryPoint);
		for (let segmentIndex = 1; segmentIndex < this.segments.length; segmentIndex++) {
			result = this.j.memberExpression.from({
				object: result,
				property: this.j.identifier(this.segments[segmentIndex])
			});
		}

		return result;
	}

	public createImportDeclaration(localEntryPoint = this.publicEntryPoint): ImportDeclaration {
		return this.j.importDeclaration.from({
			specifiers: [
				this.j.importSpecifier.from({
					imported: this.j.identifier(this.publicEntryPoint),
					local: this.j.identifier(localEntryPoint)
				})
			],
			source: this.j.literal.from({ value: this.entitySource })
		});
	}

	protected analyseImport(specifierMatcher: (specifier: UniversalImportSpecifier) => boolean) {
		const declarations = this.root.find(this.j.ImportDeclaration, ({ source, specifiers }) => {
			return source.value === this.entitySource && !!specifiers?.some(specifierMatcher);
		});
		if (declarations.size() !== 1) {
			return;
		}

		const specifier = declarations.nodes()[0].specifiers?.find(specifierMatcher);
		if (!specifier) {
			return;
		}

		const localEntryPoint = <string>(
			(specifier.local ?? (specifier as ImportSpecifier)?.imported)?.name
		);
		assert(localEntryPoint, "Expected local entry point to be present");

		this.importMetadata = { declarations, localEntryPoint };
	}
}
