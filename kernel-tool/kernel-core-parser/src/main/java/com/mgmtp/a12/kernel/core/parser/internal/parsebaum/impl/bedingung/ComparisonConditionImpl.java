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

package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;

/**
 *
 */
public class ComparisonConditionImpl extends ConditionImpl implements IComparisonCondition {

	private final ComparisonOperation vglOp;
	private final IOperation op1;
	private final IOperation op2;

	public ComparisonConditionImpl(ComparisonOperation vglOp, IOperation op1, IOperation op2) {
		this.vglOp = vglOp;
		this.op1 = op1;
		this.op2 = op2;
	}

	public ComparisonConditionImpl(IComparisonCondition op, IOperation op1, IOperation op2) {
		super(op);
		this.vglOp = op.getCmpOp();
		this.op1 = op1;
		this.op2 = op2;
	}

	public String toString() {
		return String.format("%s %s %s", op1, vglOp.name(), op2);
	}

	@Override
	public IOperation getOperation1() {
		return op1;
	}

	@Override
	public IOperation getOperation2() {
		return op2;
	}

	@Override
	public ComparisonOperation getCmpOp() {
		return vglOp;
	}

}
