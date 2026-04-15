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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * Class contains information needed to determine the correct index for the error field if it is
 * referenced with semantic index in the rule condition.
 * S. EXT-18287
 */
@StringTemplateGroup("entityid")
class SemanticIndexValue extends STParameterClassBase {

	private final EntityId entityId;
	private final boolean isConstant;

	public SemanticIndexValue(EntityId eId, boolean isConstant) {
		this.entityId = eId;
		this.isConstant = isConstant;
	}

	/**
	 * For a constant semantic index the code for the constant value is returned, for a semantic
	 * index given by a field the code for the variable used for that field in the generated code.
	 */
	public String getSemanticIndexCode() {
		if (!entityId.getHasSemanticIndex() || entityId.getSemanticIndex().size() != 1) {
			throw new IllegalArgumentException(
					"Internal error. Method should only be called for entities with exactly one semantic index.");
		}
		return entityId.getSemanticIndexCodeRef();
	}

	public boolean isSemIdxConstant() {
		return isConstant;
	}

}
