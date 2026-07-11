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

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = false)
@Value
public class RemoveMethodOverrideImpl extends Recipe {
	@Option(displayName = "Method pattern",
			description = "A method pattern that is used to find matching method declarations to remove.",
			example = "com.yourorg.IFoo bar()")
	String methodPattern;

	public RemoveMethodOverrideImpl(@JsonProperty("methodPattern") String methodPattern) {
		this.methodPattern = methodPattern;
	}

	@Override public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
		return "Remove a method implementation that overrides or implements the method of an interface/superclass";
	}

	@Override public @NlsRewrite.Description @NotNull String getDescription() {
		return "Remove a method implementation that overrides or implements the method of an interface/superclass.";
	}

	@Override public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
		return new JavaIsoVisitor<>() {
			private final MethodMatcher methodMatcher = new MethodMatcher(methodPattern, true);

			@Override
			public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
				J.NewClass newClass = getCursor().firstEnclosing(J.NewClass.class);
				J.ClassDeclaration classDecl = getCursor().firstEnclosing(J.ClassDeclaration.class);
				boolean methodMatches = newClass != null && methodMatcher.matches(method, newClass) ||
						classDecl != null && methodMatcher.matches(method, classDecl);
				if (methodMatches) {
					return null; // this appears to be the way to do this in open rewrite even though the return type is marked as nonnull
				}
				return super.visitMethodDeclaration(method, ctx);
			}
		};
	}
}
