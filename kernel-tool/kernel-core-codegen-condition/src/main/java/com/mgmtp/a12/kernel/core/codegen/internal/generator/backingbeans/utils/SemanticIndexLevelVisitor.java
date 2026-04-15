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

import org.apache.commons.lang3.ArrayUtils;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;

/**
 * Extracts the level of the index field for the error field, when a semantic index is used for the
 * error field.
 */
public class SemanticIndexLevelVisitor extends ASTVisitor {

	private String errorFieldName;
	private int semanticIndexLevel = -1;
	private boolean inFilterCondition;

	public SemanticIndexLevelVisitor(String errorFieldName) {
		this.errorFieldName = errorFieldName;
	}

	@Override
	public boolean visitGeneral(IParseTreeEntity e) {
		// stop criterion
		return semanticIndexLevel < 0;
	}

	@Override
	public boolean visit(IEntityId id) {
		if (semanticIndexLevel >= 0) {
			return false;
		}
		if (id.getFilterCondition() != null) {
			inFilterCondition = true;
			id.getFilterCondition().traverse(this);
			inFilterCondition = false;
		}
		if (inFilterCondition && !id.isOuter()) {
			return false;
		}
		if (id.getSemanticIndex().isEmpty()) {
			return false;
		}

		int semIdxLvl = -1;
		if (id.getFullName().equals(errorFieldName)) {
			semIdxLvl = getSemIdxLvl(id);
		}
		if (semIdxLvl >= 0) {
			this.semanticIndexLevel = semIdxLvl;
			return false;
		}

		if (id.getSemanticIndex().get(0) instanceof String) {
			return false;
		}
		IEntityId semIdx = (IEntityId) id.getSemanticIndex().get(0);
		if (semIdx.getFullName().equals(errorFieldName)) {
			semIdxLvl = getSemIdxLvl(semIdx);
		}
		if (semIdxLvl >= 0) {
			this.semanticIndexLevel = semIdxLvl;
		}
		return false;

	}

	private int getSemIdxLvl(IEntityId id) {
		return ArrayUtils.indexOf(id.getIndices(), IParseTreeEntity.SEMANTIC_INDEX);
	}

	private int getSemanticIndexLevel() {
		return semanticIndexLevel;
	}

	public static int getSemanticIndexLevel(ICondition condition, String errorFieldName) {
		SemanticIndexLevelVisitor visitor = new SemanticIndexLevelVisitor(errorFieldName);
		condition.traverse(visitor);
		return visitor.getSemanticIndexLevel();
	}

}
