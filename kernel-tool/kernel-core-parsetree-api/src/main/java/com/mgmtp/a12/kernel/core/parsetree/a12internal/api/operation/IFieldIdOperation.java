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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.FieldIdOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Represents operations on a single field.
 * Parsing checks that the parameter is a field.
 * <p>
 * For the operations
 * <ul>
 * <li>RangeAsString, RangeAsNumber</li>
 * <li>ValueAsDate und</li>
 * <li>RoundAccountingValue, RoundUpValue, RoundDownValue</li>
 * </ul>
 * the derived interface {@link IFieldIdOperationWithParameters} is used.
 */
@OnlyForUsage
public interface IFieldIdOperation extends IOperation {

	/**
	 * @return the name of the operation, never <code>null</code>
	 */
	public FieldIdOperationName getOperatorName();

	/**
	 * Via the parser it is checked that
	 * <ul>
	 * <li>there is no {@link IParseTreeEntity#WILDCARD} specified on any repetition.
	 * </li>
	 * <li>the entity is a field.</li>
	 * </ul>
	 *
	 * @return the field on which the operation should be performed, never <code>null</code>
	 */
	public IEntityId getField();

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		if (visitor.visit(this)) {
			getField().traverse(visitor);
		}
		visitor.endVisit(this);
	}
}
