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

import java.util.List;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OpListOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * The interface for operations on lists of operations.
 * Is used for parameters that are the result of an operation.
 * <p>
 * <p>
 * For the operations
 * <ul>
 * <li>{@link OpListOperationName#RoundAccounting}</li>
 * <li>{@link OpListOperationName#RoundUp}</li>
 * <li>{@link OpListOperationName#RoundDown}</li>
 * </ul>
 * the derived interface {@link IOpListOperationWithParameter} is used.
 */
@OnlyForUsage
public interface IOpListOperation extends IOperation {

	/**
	 * The parameter list consisting of the result of the operations.
	 * For {@link OpListOperationName#Max} and {@link OpListOperationName#Min},
	 * parsing ensures that the list has at least one element.
	 * For all other operations, parsing ensures that the list has exactly one element.
	 *
	 * @return Parameter list, never returns <code>null</code> or an empty list.
	 */
	public List<? extends IOperation> getOperations();

	/**
	 * @return the name of the operation.
	 */
	public OpListOperationName getOperatorName();

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		if (visitor.visit(this)) {
			for (int i = 0; i < getOperations().size(); i++) {
				getOperations().get(i).traverse(visitor);
				if (!visitor.visitNext(i, this)) {
					break;
				}

			}
			visitor.endVisit(this);
		}
	}
}
