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
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateExtractionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.DateExtractionOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;

/**
 *
 */
public class DateExtractionOperationImpl extends OperationImpl implements IDateExtractionOperation {

	private final DateExtractionOperationName operatorName;
	private final IOperation datumAngabe;

	public DateExtractionOperationImpl(DateExtractionOperationName name, IOperation datumAngabe) {
		super(determineOpType(name));
		this.operatorName = name;
		this.datumAngabe = datumAngabe;
	}

	public DateExtractionOperationImpl(IDateExtractionOperation op, IOperation datumAngabe) {
		super(op);
		this.operatorName = op.getOperatorName();
		this.datumAngabe = datumAngabe;
	}

	@Override
	public String toString() {
		return String.format("%s(%s)", operatorName.getRuntimeName(), datumAngabe);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateExtractionOperation#
	 * getOperatorName()
	 */
	@Override
	public DateExtractionOperationName getOperatorName() {
		return operatorName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateExtractionOperation#
	 * getDateSpec()
	 */
	@Override
	public IOperation getDateSpec() {
		return datumAngabe;
	}

	private static OperationType determineOpType(DateExtractionOperationName name) {
		switch (name) {
			case DateFromDateTime:
			case TimeFromDateTime:
				return OperationType.DATE;
			default:
				return OperationType.NUMBER;
		}
	}

}
