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

package com.mgmtp.a12.kernel.rewrite;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TextComment;
import org.openrewrite.marker.Markers;

public class RecipeUtils {
	private RecipeUtils() {
	}

	public static <E extends J> E addComment(E j, String commentText) {
		List<Comment> comments = new ArrayList<>(j.getComments());
		if (comments.stream().noneMatch(comment -> {
			if (comment instanceof TextComment textComment) {
				return textComment.getText().equals(commentText);
			} else {
				return false;
			}
		})) {
			comments.add(new TextComment(true, commentText,
					j.getPrefix().getWhitespace(), Markers.EMPTY));
			return j.withComments(comments);
		}
		return j;
	}

	static @NotNull TreeVisitor<?, ExecutionContext> combineRecipes(Recipe initialRecipe, Recipe... others) {
		return new JavaIsoVisitor<>() {
			@Override public @NotNull J.CompilationUnit visitCompilationUnit(@NotNull J.CompilationUnit cu,
					@NotNull ExecutionContext ctx) {
				this.doAfterVisit(initialRecipe.getVisitor());
				for (Recipe other : others) {
					this.doAfterVisit(other.getVisitor());
				}
				return super.visitCompilationUnit(cu, ctx);
			}
		};
	}
}
