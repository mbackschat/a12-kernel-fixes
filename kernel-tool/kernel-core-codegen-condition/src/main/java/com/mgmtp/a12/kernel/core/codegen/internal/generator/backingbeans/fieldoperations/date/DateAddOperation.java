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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.date;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.CodeGenOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.RuntimeMethodNameFactory;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateAddOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.DateAddOperationName;

/**
 *
 */
public class DateAddOperation extends CodeGenOperation implements IDateAddOperation {

	private final DateAddOperationName operatorName;
	private final CodeGenOperation datumAngabe;
	private final CodeGenOperation amount;

	public DateAddOperation(IDateAddOperation op, CodeGenOperation datumAngabe, CodeGenOperation amount) {
		super(op);
		this.operatorName = op.getOperatorName();
		this.datumAngabe = datumAngabe;
		this.amount = amount;
	}

	@Override
	public DateAddOperationName getOperatorName() {
		return operatorName;
	}

	@Override
	public CodeGenOperation getDateSpec() {
		return datumAngabe;
	}

	@Override
	public CodeGenOperation getAmountSpec() {
		return amount;
	}

	public String getDatumAngabeCode() {
		return datumAngabe.toCode();
	}

	public String getAmountCode() {
		return amount.toCode();
	}

	public String getOperatorNameCode() {
		return RuntimeMethodNameFactory.getRuntimeMethodNameFromParseTreeObject(this);
	}
}
