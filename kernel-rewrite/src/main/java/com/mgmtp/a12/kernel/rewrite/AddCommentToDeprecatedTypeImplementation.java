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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.NameTree;
import org.openrewrite.java.tree.TypeUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = false)
@Value
public class AddCommentToDeprecatedTypeImplementation extends Recipe {
	@Option(displayName = "Type name",
			description = "The interface/class fqn that is deprecated.",
			example = "com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeService")
	String typeName;

	@Option(displayName = "Comment for the type usage",
			description = "The comment to add to the type usage.",
			example = "interface is implemented here",
			required = false)
	String comment;

	@Option(displayName = "The alternative type to use",
			description = "The name of the type to use instead.",
			example = "com.mgmtp.a12.kernel.core.customfieldtype.CustomFieldTypeService",
			required = false)
	String alternativeTypeName;

	@Option(displayName = "Also check the return type of method calls",
			description = "Check the return type of a method call, if it is the searched type",
			required = false)
	Boolean findReturnType;

	public AddCommentToDeprecatedTypeImplementation(@JsonProperty("typeName") String typeName,
			@JsonProperty("comment") String comment, @JsonProperty("alternativeTypeName") String alternativeTypeName,
			@JsonProperty("findReturnType") Boolean findReturnType) {
		this.typeName = typeName;

		if (alternativeTypeName == null && comment == null) {
			throw new IllegalArgumentException("Either provide a comment or alternative type name to use for the comment");
		}

		this.alternativeTypeName = alternativeTypeName;
		this.findReturnType = Boolean.TRUE.equals(findReturnType);

		if (comment != null) {
			this.comment = comment;
		} else {
			this.comment = "deprecated type " + typeName + " use " + alternativeTypeName + " instead";
		}
	}

	@Override
	public @NotNull String getDisplayName() {
		return "Find interface usages and overrides";
	}

	public @NotNull String getDescription() {
		return "Find interface usages and overrides and add a comment.";
	}

	public static Set<J> find(J j, String typeName, boolean findReturnType) {
		return new HashSet<>(find(false, j, typeName, findReturnType));
	}

	// Third-party code from rewrite-java, https://github.com/openrewrite/rewrite/blob/main/rewrite-java/src/main/java/org/openrewrite/java/search/FindTypes.java,
	// Copyright © rewrite authors, licensed under Apache License Version 2.0."
	// "Modified by mgm technology partners GmbH on 2025-01-15 – allow broader search and add comments"

	/*
	 * ============ BEGIN THIRD-PARTY SOURCE: rewrite (https://github.com/openrewrite/rewrite/blob/main/rewrite-java/src/main/java/org/openrewrite/java/search/FindTypes.java,)
	 * https://github.com/openrewrite/rewrite/blob/v8.33.5/rewrite-java/src/main/java/org/openrewrite/java/search/FindTypes.java#L124-L170
	 * Copyright © rewrite authors, licensed under Apache License Version 2.0
	 *
	 * Copyright 2020 the original author or authors.
	 * <p>
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 * <p>
	 * https://www.apache.org/licenses/LICENSE-2.0
	 * <p>
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 *
	 */
	private static Set<NameTree> find(boolean checkAssignability, J j, String fullyQualifiedClassName, boolean findReturnType) {
		Pattern fullyQualifiedType = Pattern.compile(StringUtils.aspectjNameToPattern(fullyQualifiedClassName));

		JavaIsoVisitor<Set<NameTree>> findVisitor = new JavaIsoVisitor<Set<NameTree>>() {
			@Override
			public J.Identifier visitIdentifier(J.Identifier ident, Set<NameTree> ns) {
				if (ident.getType() != null) {
					JavaType.FullyQualified type = TypeUtils.asFullyQualified(ident.getType());
					if (typeMatches(checkAssignability, fullyQualifiedType, type) && ident.getSimpleName()
							.equals(type.getClassName())) {
						ns.add(ident);
					}
				}
				return super.visitIdentifier(ident, ns);
			}

			@Override
			public <N extends NameTree> N visitTypeName(N name, Set<NameTree> ns) {
				N n = super.visitTypeName(name, ns);
				if (findReturnType) {
					JavaType.FullyQualified type = TypeUtils.asFullyQualified(n.getType());
					if (typeMatches(checkAssignability, fullyQualifiedType, type) &&
							getCursor().firstEnclosing(J.Import.class) == null) {
						ns.add(name);
					}
				}
				return n;
			}
		};

		Set<NameTree> ts = new HashSet<>();
		findVisitor.visit(j, ts);
		return ts;
	}

	private static boolean typeMatches(boolean checkAssignability, Pattern pattern,
			JavaType.@Nullable FullyQualified test) {
		return test != null && (checkAssignability ?
				test.isAssignableFrom(pattern) :
				pattern.matcher(test.getFullyQualifiedName()).matches()
		);
	}

	// ===== END THIRD-PARTY SOURCE =====

	@Override public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {

		return new JavaIsoVisitor<>() {
			Set<UUID> foundElements = new HashSet<>();

			@Override public @NotNull J.CompilationUnit visitCompilationUnit(@NotNull J.CompilationUnit cu,
					@NotNull ExecutionContext executionContext) {
				foundElements = find(cu, typeName, findReturnType).stream().map(Tree::getId)
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
