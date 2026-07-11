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

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = false)
@Value
@SuppressWarnings("unused")
public class AddCommentToMethodOverrideImplAndInvocation extends Recipe {
	@Option(displayName = "Method pattern",
			description = "A method pattern that is used to find matching method declarations/invocations.",
			example = "org.mockito.Matchers anyVararg()")
	String methodPattern;

	@Option(displayName = "Write a comment to use the new method instead",
			description = "The name of the method to use instead. If nothing is set, the method will be commented with obsolete information",
			example = "betterMethodThanBefore()",
			required = false)
	String newMethodName;

	@Option(displayName = "Comment for the method declaration",
			description = "The comment to add to the matching method declaration.",
			example = "method impl here",
			required = false)
	String commentForDeclaration;

	@Option(displayName = "Comment for the method invocation",
			description = "The comment to add to the matching method invocations.",
			example = "method used here",
			required = false)
	String commentForInvocation;

	public AddCommentToMethodOverrideImplAndInvocation(@JsonProperty("methodPattern") String methodPattern,
			@JsonProperty("newMethodName") String newMethodName,
			@JsonProperty("commentForDeclaration") String commentForDeclaration,
			@JsonProperty("commentForInvocation") String commentForInvocation) {
		this.methodPattern = methodPattern;
		this.newMethodName = newMethodName;
		boolean isObsolete = newMethodName == null || newMethodName.isEmpty();

		if (commentForDeclaration != null) {
			if (commentForDeclaration.isEmpty()) {
				this.commentForDeclaration = null;
			} else {
				this.commentForDeclaration = commentForDeclaration;
			}
		} else if (isObsolete) {
			this.commentForDeclaration = "this method is obsolete and should no longer be implemented";
		} else {
			this.commentForDeclaration = "this method is deprecated and should no longer be implemented, implement `" + newMethodName + "` instead";
		}
		if (commentForInvocation != null) {
			if (commentForInvocation.isEmpty()) {
				this.commentForInvocation = null;
			} else {
				this.commentForInvocation = commentForInvocation;
			}
		} else if (isObsolete) {
			this.commentForInvocation = "this method is obsolete and should no longer be used";
		} else {
			this.commentForInvocation = "this method is deprecated, use `" + newMethodName + "` instead";
		}
	}

	@Override public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
		return "Find and comment a method implementation and invocation";
	}

	@Override public @NlsRewrite.Description @NotNull String getDescription() {
		return "Find and comment a method implementation and invocation.";
	}

	@Override public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
		return RecipeUtils.combineRecipes(new AddCommentToMethodOverrideImpl(methodPattern, commentForDeclaration),
				new AddCommentToMethodInvocation(methodPattern, commentForInvocation, true));
	}

}
