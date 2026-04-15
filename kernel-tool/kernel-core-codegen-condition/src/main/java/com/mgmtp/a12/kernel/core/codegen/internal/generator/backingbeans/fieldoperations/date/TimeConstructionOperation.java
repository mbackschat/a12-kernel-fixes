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
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.ITimeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;

/**
 *
 */
public class TimeConstructionOperation extends CodeGenOperation implements ITimeConstructionOperation {

	private final CodeGenOperation hour;
	private final CodeGenOperation minute;
	private final CodeGenOperation second;

	public TimeConstructionOperation(ITimeConstructionOperation da,
			CodeGenOperation hour,
			CodeGenOperation minute,
			CodeGenOperation second) {
		super(da);
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	@Override
	public IOperation getHour() {
		return hour;
	}

	@Override
	public IOperation getMinute() {
		return minute;
	}

	@Override
	public IOperation getSecond() {
		return second;
	}

	public String getHourCode() {
		if (hour == null) {
			return new ConstantOperation("00", OperationType.NUMBER).toCode();
		} else {
			return hour.toCode();
		}
	}

	public String getMinuteCode() {
		if (minute == null) {
			return new ConstantOperation("00", OperationType.NUMBER).toCode();
		} else {
			return minute.toCode();
		}
	}

	public String getSecondCode() {
		if (second == null) {
			return new ConstantOperation("00", OperationType.NUMBER).toCode();
		} else {
			return second.toCode();
		}
	}
}
