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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.FindMethods;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.SearchResult;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = false)
@Value
public class AddCommentToMethodInvocation extends Recipe {
	/**
	 * A method pattern that is used to find matching method invocations.
	 * See {@link MethodMatcher} for details on the expression's syntax.
	 */
	@Option(displayName = "Method pattern",
			description = "A method pattern that is used to find matching method invocations.",
			example = "java.util.List add(..)")
	String methodPattern;

	/**
	 * The comment that is added to the found method invocation.
	 */
	@Option(displayName = "Comment for the method invocation",
			description = "The comment to add to the matching method invocations.",
			example = "method used here")
	String comment;

	@Option(displayName = "Match on overrides",
			description = "When enabled, find methods that are overrides of the method pattern.",
			required = false)
	@Nullable
	Boolean matchOverrides;

	public AddCommentToMethodInvocation(@JsonProperty("methodPattern") String methodPattern,
			@JsonProperty("comment") String comment, @JsonProperty("matchOverrides") @Nullable Boolean matchOverrides) {
		this.methodPattern = methodPattern;
		this.comment = comment;
		this.matchOverrides = matchOverrides;
	}

	@Override
	public @NotNull String getDisplayName() {
		return "Find method invocation and add a comment";
	}

	public @NotNull String getDescription() {
		return "Find method invocations by pattern and add a comment.";
	}

	@Override public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
		if (comment == null || comment.isEmpty()) {
			return new JavaIsoVisitor<>() {
			};
		}

		return new JavaIsoVisitor<>() {
			Set<UUID> foundElements = new HashSet<>();

			@Override public @NotNull J.CompilationUnit visitCompilationUnit(@NotNull J.CompilationUnit cu,
					@NotNull ExecutionContext executionContext) {
				foundElements = FindMethods.find(cu, methodPattern, Boolean.TRUE.equals(matchOverrides)).stream()
						.filter(j -> !j.getMarkers().findAll(SearchResult.class).isEmpty())
						.map(Tree::getId)
						.collect(Collectors.toSet());
				return super.visitCompilationUnit(cu, executionContext);
			}

			@Override public @Nullable J preVisit(@NonNull J tree, @NotNull ExecutionContext executionContext) {
				J j = super.preVisit(tree, executionContext);
				if (j == null) {
					return null;
				}
				if (foundElements.contains(j.getId())) {
					J j1 = RecipeUtils.addComment(j, comment);
					if (j1 != null)
						return j1;
				}
				return j;
			}
		};
	}

}
