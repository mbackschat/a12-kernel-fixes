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

import com.mgmtp.a12.kernel.core.parser.internal.check.util.DateUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.OperationImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateTimeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;

/**
 *
 */
public class DateTimeConstructionOperationImpl extends OperationImpl implements IDateTimeConstructionOperation {

	private final IOperation dateOperation;
	private final IOperation timeOperation;

	public DateTimeConstructionOperationImpl(IOperation dateOperation, IOperation timeOperation) {
		super(OperationType.DATE, DateUtils.DATETIME_FORMAT);

		this.dateOperation = dateOperation;
		this.timeOperation = timeOperation;
	}

	public DateTimeConstructionOperationImpl(IDateTimeConstructionOperation op, IOperation dateOperation,
			IOperation timeOperation) {
		super(op);
		this.dateOperation = dateOperation;
		this.timeOperation = timeOperation;
	}

	@Override
	public String toString() {
		return String.format("DateTime(%s, %s)", dateOperation, timeOperation);
	}

	@Override
	public IOperation getDate() {
		return dateOperation;
	}

	@Override
	public IOperation getTime() {
		return timeOperation;
	}
}
