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

import {
	ASTPath,
	ClassDeclaration,
	ClassImplements,
	Collection,
	JSCodeshift,
	TSExpressionWithTypeArguments
} from "jscodeshift";

import { EntityTransformAddCommentToDeclaration } from "../entity-transform-spec";

export function transformEntityAddCommentToImplementOptionalMethod(
	j: JSCodeshift,
	root: Collection,
	spec: EntityTransformAddCommentToDeclaration
) {
	root.find(j.ClassDeclaration).forEach(transform);

	function transform(path: ASTPath<ClassDeclaration>) {
		const classDeclaration = path.value;
		if (spec.addComments && ClassDeclaration.check(classDeclaration)) {
			const clazz = classDeclaration;
			if (clazz.implements === undefined || clazz.implements.length === 0) {
				return;
			} else {
				if (
					!clazz.implements.some(impl => {
						if (j.ClassImplements.check(impl)) {
							const element: ClassImplements = impl;
							return j.Identifier.check(element.id) && element.id.name === spec.className;
						} else {
							const element: TSExpressionWithTypeArguments = impl;
							return (
								j.Identifier.check(element.expression) && element.expression.name === spec.className
							);
						}
					})
				) {
					return;
				}

				if (
					clazz.body.body.every(element => {
						if (j.ClassMethod.check(element)) {
							if (!j.Identifier.check(element.key) || element.key.name !== spec.methodName) {
								return true;
							}
						}

						return false;
					})
				) {
					if (clazz.body.body.length === 0) {
						clazz.body.comments = clazz.body.comments ?? [];
						const newComments = spec.addComments.map(comment => j.commentLine(comment));
						clazz.body.comments = clazz.body.comments.concat(newComments);
					} else {
						clazz.body.body[0].comments = clazz.body.body[0].comments ?? [];
						const newComments = spec.addComments.map(comment => j.commentLine(comment));
						clazz.body.body[0].comments = clazz.body.body[0].comments.concat(newComments);
					}
				}
			}
		}
	}
}
