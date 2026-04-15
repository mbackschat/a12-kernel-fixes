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
 * Represents a time construction operation: 'Zeit(...)'. The date fragments can be specified by
 * fields or string constants, which can be mixed as well.
 */
@OnlyForUsage
public interface ITimeConstructionOperation extends IOperation {

	/**
	 * Returns the operation that specifies the hour of the time operation.
	 *
	 * @return hour specification, never returns null.
	 */
	public IOperation getHour();

	/**
	 * Returns the operation that specifies the minute of the time operation.
	 *
	 * @return minute specification, never returns null.
	 */
	public IOperation getMinute();

	/**
	 * Returns the operation that specifies the second of the time operation.
	 *
	 * @return second pecification, never returns null.
	 */
	public IOperation getSecond();

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		if (visitor.visit(this)) {
			if (getHour() != null) {
				getHour().traverse(visitor);
				if (visitor.visitNext(1, this) && getMinute() != null) {
					getMinute().traverse(visitor);
					if (visitor.visitNext(2, this) && getSecond() != null) {
						getSecond().traverse(visitor);
					}
				}
			}
			visitor.endVisit(this);
		}
	}

}
