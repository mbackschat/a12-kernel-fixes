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

package com.mgmtp.a12.kernel.core.calc.internal.util.compOp;

import java.util.List;

import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.OpListOperationWithParameterImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOpListOperationWithParameter;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OpListOperationName;

/**
 * Extends {@link OpListOperationWithParameterImpl} with the corresponding comparison operator.
 */
public class CalcOperationRoundDownWithCompOp
		extends OpListOperationWithParameterImpl
		implements IOpListOperationWithParameter, IWithComparisonOperation {

	private final ComparisonOperation compOp;
	private final IOperation operation;

	public CalcOperationRoundDownWithCompOp(
			IOperation operation,
			int anzStellen,
			ComparisonOperation compOp
	) {
		super(OpListOperationName.RoundDown, List.of(operation), anzStellen);
		this.compOp = compOp;
		this.operation = operation;
	}

	@Override public ComparisonOperation getComparisonOperation() {
		return compOp;
	}

	@Override public IOperation getOriginalOperation() {
		return operation;
	}
}
