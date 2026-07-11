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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenConditionConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.CodeGenOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;

import lombok.Getter;

/**
 * Parameter Class for template Comparison condition
 */
@Getter
public class ComparisonCondition extends CodeGenCondition implements IComparisonCondition {
	private final ComparisonOperation cmpOp;
	private final CodeGenOperation operation1;
	private final CodeGenOperation operation2;

	public ComparisonCondition(IComparisonCondition b, CodeGenOperation operation1, CodeGenOperation operation2) {
		this.cmpOp = b.getCmpOp();
		this.operation1 = operation1;
		this.operation2 = operation2;
	}

	public String getOperation1Code() {
		return operation1.toCode();
	}

	public String getOperation2Code() {
		return operation2.toCode();
	}

	public String getCompKind() {
		if (operation1.getType() == OperationType.NUMBER) {
			return "";
		}
		if (cmpOp == ComparisonOperation.NM || cmpOp == ComparisonOperation.WM) {
			return CodeGenConditionConstants.PATTERN_STRING;
		} else {
			return operation1.getType().getRuntimeName();
		}
	}
}
