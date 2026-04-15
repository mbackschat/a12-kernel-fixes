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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Represents a date construction operation: 'Datum(...)'. The date fragments can be specified by
 * fields or string constants, which can be mixed as well.
 */
@OnlyForUsage
public interface IDateConstructionOperation extends IOperation {

	/**
	 * Returns the operation that specifies the day of the date.
	 *
	 * @return day specification, never returns null
	 */
	public IOperation getDay();

	/**
	 * Returns the operation that specifies the month of the date.
	 *
	 * @return month specification, never returns null
	 */
	public IOperation getMonth();

	/**
	 * Returns the operation that specifies the 4-digit year of the date or the 2-digit century, if
	 * the year is specified by {@link #getYear2()}.
	 *
	 * @return year or century specification, respectively, or null if the year is not specified
	 */
	public IOperation getYear1();

	/**
	 * Returns the operation that specifies the 2-digit year of the date if {@link #getYear1()}
	 * specifies a 2-digit century.
	 *
	 * @return year specification or null if {@link #getYear1()} does not specify a 2-digit century
	 */
	public IOperation getYear2();

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		if (visitor.visit(this)) {
			getDay().traverse(visitor);
			if (visitor.visitNext(1, this)) {
				getMonth().traverse(visitor);
				if (visitor.visitNext(2, this) && getYear1() != null) {
					getYear1().traverse(visitor);
					if (visitor.visitNext(3, this) && getYear2() != null) {
						getYear2().traverse(visitor);
					}
				}
			}
			visitor.endVisit(this);
		}
	}

}
