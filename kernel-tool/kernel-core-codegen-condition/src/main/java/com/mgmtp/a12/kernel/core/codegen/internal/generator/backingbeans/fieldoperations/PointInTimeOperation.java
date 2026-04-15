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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations;

import com.mgmtp.a12.kernel.core.codegen.internal.utils.RuntimeMethodNameFactory;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IPointInTimeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.PointInTimeOperationName;

/**
 *
 */
public class PointInTimeOperation extends CodeGenOperation implements IPointInTimeOperation {

	private final PointInTimeOperationName operatorName;

	public PointInTimeOperation(IPointInTimeOperation op) {
		super(op);
		this.operatorName = op.getOperatorName();
	}

	@Override
	public PointInTimeOperationName getOperatorName() {
		return operatorName;
	}

	/**
	 * Is called from the templates. If it is not a date, it has to be
	 * {@link PointInTimeOperationName#BaseYear} used as a number.
	 */
	public boolean isDatum() {
		assert getType() == OperationType.DATE || (getType() == OperationType.NUMBER && operatorName == PointInTimeOperationName.BaseYear);

		return getType() == OperationType.DATE;
	}

	public String getOperatorNameCode() {
		return RuntimeMethodNameFactory.getRuntimeMethodNameFromParseTreeObject(this);
	}
}
