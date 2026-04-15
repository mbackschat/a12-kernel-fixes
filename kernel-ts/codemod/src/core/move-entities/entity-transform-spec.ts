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

import {
	transformEntityAddComment,
	transformEntityAddCommentToDeclaration,
	transformEntityAddCommentToImplementOptionalMethod,
	transformEntityRenamedIdentifier,
	transformEntityRenamedProperty,
	transformMovedEntity,
	transformMovedOrRenamed,
	transformPreferTopLevelImport
} from "./transforms";
import { transformRenameImport } from "./transforms/transform-renameImport";

export interface EntityTransformBase {
	readonly type: string;
	readonly ticket: string;
}

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
export interface EntityTransformSpec extends EntityTransformBase { }

export interface EntityTransformMoved extends EntityTransformBase {
	readonly oldPath: string;
	readonly oldName: string;
	readonly newPath: string;
	readonly newName?: string;
}

export interface EntityTransformRenamedIdentifier extends EntityTransformBase {
	readonly oldName: string;
	readonly newName: string;
}

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
export interface EntityTransformMovedOrRenamed extends EntityTransformMoved { }

export interface EntityTransformAddComment extends EntityTransformBase {
	readonly className?: string;
	readonly methodName?: string;
	readonly propertyName?: string;
	readonly addComments: string[];
}
export interface EntityTransformRenameProperty extends EntityTransformBase {
	readonly className?: string;
	readonly methodName?: string;
	readonly propertyName?: string;
	readonly newName: string;
}

export interface EntityTransformAddCommentToDeclaration extends EntityTransformBase {
	readonly className: string;
	readonly methodName: string;
	readonly addComments: string[];
}

export interface EntityTransformPreferTopLevelImport extends EntityTransformBase {
	readonly packageName: string;
}

export interface EntityTransformRenameImport extends EntityTransformBase {
	readonly oldImportName: string;
	readonly newImportName: string;
}

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
export interface EntityTransformAddCommentToImplementOptionalMethod
	extends EntityTransformAddCommentToDeclaration { }

export namespace EntityTransformSpec {
	export function transform(j: JSCodeshift, root: Collection, spec: EntityTransformSpec) {
		switch (Kind.getKind(spec)) {
			case EntityTransformSpec.Kind.Moved: {
				transformMovedEntity(j, root, <EntityTransformMoved>spec);
				break;
			}
			case EntityTransformSpec.Kind.MovedOrRenamed: {
				transformMovedOrRenamed(j, root, <EntityTransformMovedOrRenamed>spec);
				break;
			}
			case EntityTransformSpec.Kind.RenamedIdentifier: {
				transformEntityRenamedIdentifier(j, root, <EntityTransformRenamedIdentifier>spec);
				break;
			}
			case EntityTransformSpec.Kind.AddComment: {
				transformEntityAddComment(j, root, <EntityTransformAddComment>spec);
				break;
			}
			case EntityTransformSpec.Kind.RenamedProperty: {
				transformEntityRenamedProperty(j, root, <EntityTransformRenameProperty>spec);
				break;
			}
			case EntityTransformSpec.Kind.AddCommentToDeclaration: {
				transformEntityAddCommentToDeclaration(
					j,
					root,
					<EntityTransformAddCommentToDeclaration>spec
				);
				break;
			}
			case EntityTransformSpec.Kind.AddCommentToImplementOptionalMethod: {
				transformEntityAddCommentToImplementOptionalMethod(
					j,
					root,
					<EntityTransformAddCommentToImplementOptionalMethod>spec
				);
				break;
			}
			case EntityTransformSpec.Kind.PreferTopLevelImport: {
				transformPreferTopLevelImport(j, root, <EntityTransformPreferTopLevelImport>spec);
				break;
			}
			case EntityTransformSpec.Kind.RenameImport: {
				transformRenameImport(j, root, <EntityTransformRenameImport>spec);
				break;
			}
		}
	}

	export enum Kind {
		Moved = "moved",
		MovedOrRenamed = "movedOrRenamed",
		RenamedIdentifier = "renamedIdentifier",
		RenamedProperty = "renamedProperty",
		AddComment = "addComment",
		AddCommentToDeclaration = "addCommentToDeclaration",
		AddCommentToImplementOptionalMethod = "addCommentToImplementOptionalMethod",
		PreferTopLevelImport = "preferTopLevelImport",
		RenameImport = "renameImport"
	}

	export namespace Kind {
		export function getKind(migration: EntityTransformBase): Kind {
			return migration.type as Kind;
		}
	}
}
