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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Interface for a comparison condition.
 * <p>
 * To determine the type of comparison see {@link IOperation#getType()}} for one of the
 * operations (the types on both sides of the comparison have to be the same).
 */
@OnlyForUsage
public interface IComparisonCondition extends ICondition {

	/**
	 * Operation auf der linken Seite des Vergleichs.
	 * Darf niemals <code>null</code> zurückgegeben, da bereits
	 * durch die Grammatik zugesichert ist, dass eine Vergleichsbedingung
	 * eine linke und eine rechte Seite hat.
	 *
	 * @return gibt die linke Seite des Vergleichs zurück.
	 */
	public IOperation getOperation1();

	/**
	 * Operation auf der rechten Seite des Vergleichs.
	 * Darf niemals <code>null</code> zurückgegeben, da bereits
	 * durch die Grammatik zugesichert ist, dass eine Vergleichsbedingung
	 * eine linke und eine rechte Seite hat.
	 *
	 * @return gibt die rechte Seite des Vergleichs zurück.
	 */
	public IOperation getOperation2();

	/**
	 * Darf niemals <code>null</code> zurückgegeben, da bereits
	 * durch die Grammatik zugesichert ist, dass eine Vergleichsbedingung
	 * immer eine VergleichsOperation hat.
	 *
	 * @return gibt die Vergleichs-Operation zurück
	 */
	public ComparisonOperation getCmpOp();

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		if (visitor.visit(this)) {
			getOperation1().traverse(visitor);
			if (visitor.visitNext(1, this)) {
				getOperation2().traverse(visitor);
			}
			visitor.endVisit(this);
		}
	}
}
