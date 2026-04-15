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

package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation;

import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ICompositeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.CalcOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;

/**
 *
 */
public class CompositeOperationImpl extends OperationImpl implements ICompositeOperation {

	private List<? extends IOperation> feldOps;
	private CalcOp calcOp;
	private boolean isGeklammert;

	public CompositeOperationImpl(List<IOperation> feldOps, CalcOp calcOp, OperationType type) {
		this(feldOps, calcOp, type, false);
	}

	public CompositeOperationImpl(List<IOperation> feldOps, CalcOp calcOp, OperationType type, boolean isGeklammert) {
		super(type);
		this.feldOps = feldOps;
		this.calcOp = calcOp;
		this.isGeklammert = isGeklammert;
	}

	public CompositeOperationImpl(List<IOperation> ops, ICompositeOperation op) {
		super(op);
		this.feldOps = ops;
		this.calcOp = op.getCalcOp();
		this.isGeklammert = op.hasBrackets();
	}

	@Override
	public String toString() {
		String oB = isGeklammert ? "(" : "";
		String cB = isGeklammert ? ")" : "";
		String op = String.format(" %s ", calcOp.name());
		String sop = feldOps.stream().map(IOperation::toString)
				.collect(Collectors.joining(op));
		return String.format("%s%s%s", oB, sop, cB);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IOperation> getOperations() {
		return (List<IOperation>) feldOps;
	}

	@Override
	public CalcOp getCalcOp() {
		return calcOp;
	}

	@Override
	public void setCalcOp(CalcOp op) {
		this.calcOp = op;
	}

	@Override
	public boolean hasBrackets() {
		return isGeklammert;
	}

	public void setGeklammert() {
		isGeklammert = true;
	}

	@Override
	public void setOperations(List<IOperation> fops) {
		this.feldOps = fops;

	}

}
