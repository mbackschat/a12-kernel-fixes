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

import { Collection, ImportDeclaration, JSCodeshift } from "jscodeshift";

export type UniversalImportSpecifier = NonNullable<ImportDeclaration["specifiers"]>[number];

export function findAvailableIdentifier(
	j: JSCodeshift,
	root: Collection,
	baseIdentifier: string
): string {
	if (isUnusedIdentifier(j, root, baseIdentifier)) {
		return baseIdentifier;
	}

	for (let suffix = 2; ; suffix++) {
		const candidateName = baseIdentifier + suffix;

		if (isUnusedIdentifier(j, root, candidateName)) {
			return candidateName;
		}
	}
}

function isUnusedIdentifier(j: JSCodeshift, root: Collection, name: string): boolean {
	return (
		root
			.find(j.Identifier, { name })
			.filter(path => {
				const parent = path.parent.node;
				return (
					parent.type !== "ImportDefaultSpecifier" &&
					parent.type !== "ImportSpecifier" &&
					parent.type !== "MemberExpression"
				);
			})
			.size() === 0
	);
}

export interface MemberExpressionMatcher {
	object: { name: string } | MemberExpressionMatcher;
	property: { name: string };
}

export namespace MemberExpressionMatcher {
	export function create(j: JSCodeshift, ...segments: string[]): MemberExpressionMatcher {
		if (segments.length < 2) {
			throw new Error("Segments should be at least 2. Got:" + segments.toString());
		}

		let result: MemberExpressionMatcher = {
			object: { name: segments[0] },
			property: { name: segments[1] }
		};

		for (let segmentIndex = 2; segmentIndex < segments.length; segmentIndex++) {
			result = { object: result, property: { name: segments[segmentIndex] } };
		}

		return result;
	}
}

export interface TSQualifiedNameMatcher {
	left: { name: string } | TSQualifiedNameMatcher;
	right: { name: string };
}

export namespace TSQualifiedNameMatcher {
	export function create(j: JSCodeshift, ...segments: string[]): TSQualifiedNameMatcher {
		if (segments.length < 2) {
			throw new Error("Segments should be at least 2. Got:" + segments.toString());
		}

		let result: TSQualifiedNameMatcher = {
			left: { name: segments[0] },
			right: { name: segments[1] }
		};

		for (let segmentIndex = 2; segmentIndex < segments.length; segmentIndex++) {
			result = { left: result, right: { name: segments[segmentIndex] } };
		}

		return result;
	}
}
