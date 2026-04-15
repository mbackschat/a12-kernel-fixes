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
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.ConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;

/**
 *
 */
public class DateConstructionOperation extends CodeGenOperation implements IDateConstructionOperation {

	private final CodeGenOperation tag;
	private final CodeGenOperation monat;
	private final CodeGenOperation jahr1;
	private final CodeGenOperation jahr2;
	private final String baseYear;

	public DateConstructionOperation(IDateConstructionOperation da,
			String baseYear,
			CodeGenOperation jahr1,
			CodeGenOperation jahr2,
			CodeGenOperation monat,
			CodeGenOperation tag) {
		super(da);
		this.baseYear = baseYear;
		this.tag = tag;
		this.monat = monat;
		this.jahr1 = jahr1;
		this.jahr2 = jahr2;

	}

	@Override
	public IOperation getDay() {
		return tag;
	}

	@Override
	public IOperation getMonth() {
		return monat;
	}

	@Override
	public IOperation getYear1() {
		return jahr1;
	}

	@Override
	public IOperation getYear2() {
		return jahr2;
	}

	public String getTagCode() {
		return tag.toCode();
	}

	public String getMonatCode() {
		return monat.toCode();
	}

	public String getJahrCode() {
		if (jahr1 == null) {
			// use the base year
			ConstantOperation jahr1Param = new ConstantOperation(baseYear, OperationType.NUMBER);
			return jahr1Param.toCode();
		}
		if (jahr2 == null) {
			return jahr1.toCode();
		} else {
			return String.format("%s, %s", jahr1.toCode(), jahr2.toCode());
		}
	}

}
