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

package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.datum;

import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.OperationImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateAddOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.DateAddOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;

public class DateAddOperationImpl extends OperationImpl implements IDateAddOperation {

	private final DateAddOperationName name;
	private final IOperation dateSpec;
	private final IOperation amountSpec;

	public DateAddOperationImpl(DateAddOperationName name, IOperation dateSpec, IOperation amountSpec) {
		super(OperationType.DATE);
		this.name = name;
		this.dateSpec = dateSpec;
		this.amountSpec = amountSpec;
	}

	public DateAddOperationImpl(IDateAddOperation op, IOperation dateSpec, IOperation amountSpec) {
		super(op);
		this.name = op.getOperatorName();
		this.dateSpec = dateSpec;
		this.amountSpec = amountSpec;
	}

	@Override
	public String toString() {
		return String.format("%s(%s, %s)", name.getRuntimeName(), dateSpec, amountSpec);
	}

	@Override
	public DateAddOperationName getOperatorName() {
		return name;
	}

	@Override
	public IOperation getDateSpec() {
		return dateSpec;
	}

	@Override
	public IOperation getAmountSpec() {
		return amountSpec;
	}

}
