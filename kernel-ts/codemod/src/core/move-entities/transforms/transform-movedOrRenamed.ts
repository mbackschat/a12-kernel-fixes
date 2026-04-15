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

import { Collection, JSCodeshift } from "jscodeshift";

import { BaseEntity, NamedEntity } from "../entities";
import { EntityTransformMovedOrRenamed } from "../entity-transform-spec";
import { findAvailableIdentifier } from "../utils";

export function transformMovedOrRenamed(
	j: JSCodeshift,
	root: Collection,
	spec: EntityTransformMovedOrRenamed
) {
	transformUsages(j, root, spec);
}

function transformUsages(j: JSCodeshift, root: Collection, spec: EntityTransformMovedOrRenamed) {
	const { oldName, oldPath, newPath, newName } = spec;

	const oldEntity = new NamedEntity(j, root, oldPath, oldName);
	if (!oldEntity.importDeclarations) {
		return;
	}

	const newEntity = new BaseEntity(j, root, newPath, newName ?? oldName);
	const newLocalEntryPoint = findAvailableIdentifier(j, root, newEntity.publicEntryPoint);

	const memberExpressionNodes = oldEntity
		.usages()
		.replaceWith(newEntity.createMemberExpression(newLocalEntryPoint));

	const qualifiedNameNodes = oldEntity
		.tSQualifiedNameUsages()
		.replaceWith(newEntity.createMemberExpression(newLocalEntryPoint));

	if (memberExpressionNodes.size() > 0 || qualifiedNameNodes.size() > 0) {
		oldEntity.importDeclarations?.insertAfter(
			newEntity.createImportDeclaration(newLocalEntryPoint)
		);
	}
}
