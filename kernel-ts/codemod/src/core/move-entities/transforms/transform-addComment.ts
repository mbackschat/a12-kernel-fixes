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
	Collection,
	Expression,
	ExpressionStatement,
	JSCodeshift,
	MemberExpression
} from "jscodeshift";

import { EntityTransformAddComment } from "../entity-transform-spec";

export function transformEntityAddComment(
	j: JSCodeshift,
	root: Collection,
	spec: EntityTransformAddComment
) {
	if (spec.methodName || spec.propertyName) {
		root.find(j.Expression).forEach(transform);
	}

	root.find(j.Expression).forEach(transform);

	function findExpressionStatementParent(
		path: ASTPath<Expression>
	): ExpressionStatement | MemberExpression | undefined {
		if (j.ExpressionStatement.check(path.value)) {
			return path.value as ExpressionStatement;
		}
		if (j.CallExpression.check(path.value) && j.MemberExpression.check(path.value.callee)) {
			return path.value.callee as MemberExpression;
		}
		if (path.parent == undefined) {
			return undefined;
		}
		return findExpressionStatementParent(path.parent);
	}

	function transform(path: ASTPath<Expression>) {
		const expression = path.value;
		if (spec.methodName && j.CallExpression.check(expression)) {
			const callee = expression.callee;
			if (
				j.MemberExpression.check(callee) &&
				j.Identifier.check(callee.object) &&
				(!spec.className || callee.object.name == spec.className) &&
				j.Identifier.check(callee.property) &&
				callee.property.name == spec.methodName
			) {
				const statement: ExpressionStatement | MemberExpression | undefined =
					findExpressionStatementParent(path);
				if (statement == undefined) {
					// where can an expression stand outside of statements?
					return;
				}
				statement.comments = statement.comments ?? [];
				const newComments = spec.addComments
					.filter(
						comment =>
							!statement.comments!.some(cmt => j.CommentLine.check(cmt) && cmt.value === comment)
					)
					.map(comment => j.commentLine(comment));
				statement.comments = statement.comments.concat(newComments);
			}
		} else if (spec.propertyName && j.MemberExpression.check(expression)) {
			const obj = expression.object;
			if (
				j.Identifier.check(expression.property) &&
				expression.property.name == spec.propertyName &&
				j.Identifier.check(obj) &&
				(!spec.className || obj.name == spec.className)
			) {
				const statement: ExpressionStatement | MemberExpression | undefined =
					findExpressionStatementParent(path);
				if (statement == undefined) {
					// where can an expression stand outside of statements?
					return;
				}
				statement.comments = statement.comments ?? [];
				const newComments = spec.addComments
					.filter(
						comment =>
							!statement.comments!.some(cmt => j.CommentLine.check(cmt) && cmt.value === comment)
					)
					.map(comment => j.commentLine(comment));
				statement.comments = statement.comments.concat(newComments);
			}
		}
	}
}
