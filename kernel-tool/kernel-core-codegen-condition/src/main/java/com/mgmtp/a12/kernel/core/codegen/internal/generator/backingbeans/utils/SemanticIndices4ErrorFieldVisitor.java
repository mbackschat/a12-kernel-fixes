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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

/**
 * Determines the semantic indices used in a rule condition for the error field.
 */
public class SemanticIndices4ErrorFieldVisitor extends ASTVisitor {

	private final String errorFieldName;

	private Set<SemanticIndexWrapper> semIndices = new HashSet<>();

	private boolean inFilterCondition = false;

	public SemanticIndices4ErrorFieldVisitor(String errorFieldName) {
		this.errorFieldName = errorFieldName;
	}

	@Override
	public boolean visit(IEntityId id) {
		if (inFilterCondition && !id.isOuter()) {
			// We ignore every non-outer entity in filter conditions
			return false;
		}
		if (!id.getSemanticIndex().isEmpty()) {
			// currently only one semantic index per entity is supported
			Object semIdx = id.getSemanticIndex().get(0);
			SemanticIndexWrapper semIndexInternal = new SemanticIndexWrapper(semIdx);

			if (id.getFullName().equals(errorFieldName)) {
				this.semIndices.add(semIndexInternal);
			}
		}
		if (id.getFilterCondition() != null) {
			this.inFilterCondition = true;
			id.getFilterCondition().traverse(this);
			this.inFilterCondition = false;
		}
		return false;
	}

	/**
	 * Returns an optional containing the semantic index as a {@link String} or an {@link IEntityId}
	 * object if a semantic index is used for the error field in the condition for a not iterating
	 * level and all occurrences of the error field with semantic index have the same semantic
	 * index. Otherwise an empty optional is returned.
	 */
	public static Optional<Object> getUniqueSemanticIndex4NotIteratingLevel(ICondition condition, IEntity errorField,
			int semanticIndexLevel, IEC ec) {
		Set<Integer> iterationLevels = new HashSet<>();
		int rowLevelOfIndexField = EntityUtils.getLevel(errorField.getFullName());
		int parallelIterationLevel4ErrorField = AnalyseService.getParallelIterationLevel(condition, errorField, ec);
		if (parallelIterationLevel4ErrorField >= 0 && semanticIndexLevel == parallelIterationLevel4ErrorField) {
			return Optional.empty();
		}
		for (int i = 0; i < rowLevelOfIndexField; i++) {
			if (AnalyseService.isIteration(condition, i, true)) {
				iterationLevels.add(i);
			}
		}
		if (iterationLevels.contains(semanticIndexLevel)) {
			return Optional.empty();
		}

		SemanticIndices4ErrorFieldVisitor visitor = new SemanticIndices4ErrorFieldVisitor(errorField.getFullName());
		condition.traverse(visitor);
		if (visitor.semIndices.size() != 1) {
			return Optional.empty();
		}
		return Optional.of(visitor.semIndices.iterator().next().semIdx);
	}

	private static class SemanticIndexWrapper {

		private final Object semIdx;

		public SemanticIndexWrapper(Object semIdx) {
			this.semIdx = semIdx;
		}

		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			SemanticIndexWrapper other = (SemanticIndexWrapper) obj;
			if (this.semIdx == other.semIdx) {
				return true;
			}
			if (this.semIdx == null || other.semIdx == null) {
				return false;
			}
			if (this.semIdx.getClass() != other.semIdx.getClass()) {
				return false;
			}
			if (this.semIdx instanceof IEntityId) {
				IEntityId id = (IEntityId) this.semIdx;
				IEntityId otherId = (IEntityId) other.semIdx;
				return id.getFullName().equals(otherId.getFullName()) &&
						Arrays.equals(id.getIndices(), otherId.getIndices());
			}
			return this.semIdx.equals(other.semIdx);
		}

		public int hashCode() {
			if (semIdx == null) {
				return 0;
			}
			if (semIdx instanceof IEntityId) {
				IEntityId id = (IEntityId) semIdx;
				final int prime = 31;
				int result = 1;
				result = prime * result + (id.getFullName() == null ? 0 : id.getFullName().hashCode());
				result = prime * result + Arrays.hashCode(id.getIndices());
				return result;
			}
			return semIdx.hashCode();
		}

	}

}
