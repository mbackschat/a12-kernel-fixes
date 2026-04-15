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

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenContext;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.RuntimeMethodNameFactory;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.formatter.FormatterUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ICompositeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.CalcOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

/**
 *
 */
public class CompositeOperation extends CodeGenOperation implements ICompositeOperation {

	private List<CodeGenOperation> feldOps = new ArrayList<>();
	private CalcOp calcOp;
	private final boolean isGeklammert;

	public CompositeOperation(ICompositeOperation op, List<CodeGenOperation> ops) {
		super(op);
		calcOp = op.getCalcOp();
		isGeklammert = op.hasBrackets();
		this.feldOps = ops;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<IOperation> getOperations() {
		return (List) feldOps;
	}

	public List<CodeGenOperation> getFeldOps() {
		return feldOps;
	}

	@Override
	public CalcOp getCalcOp() {
		return calcOp;
	}

	@Override
	public boolean hasBrackets() {
		return isGeklammert;
	}

	public String getFeldOperationCodeJava() {
		String calcOpCode = RuntimeMethodNameFactory.getRuntimeMethodNameFromParseTreeObject(this);
		StringBuilder buf = new StringBuilder();
		CodeGenOperation op0 = feldOps.get(0);

		if (op0 instanceof IConstantOperation && op0.getType() == OperationType.STRING) {
			// In case of a string constant at the first position, we create a VkString, not an ordinary String
			buf.append(op0.toCode("extended"));
		} else {
			buf.append(op0.toCode());
		}
		for (int i = 1; i < feldOps.size(); i++) {
			buf.append(String.format(".%s(%s)", calcOpCode, feldOps.get(i).toCode()));
		}
		return buf.toString();
	}

	/**
	 * create cplus or vba code
	 *
	 * @return code-fragment
	 */
	public String getFeldOpCode() {
		if (calcOp == CalcOp.POWER &&
				ICodeGeneratorParameters.ProgrammingLanguage.CPLUS == CodeGenContext.getInstance().getProgrammingLanguage()) {
			return getFeldOperationCodeJava();
		}
		String calcOpCode = FormatterUtil.getCalcOperator(calcOp);
		StringBuilder buf = new StringBuilder();

		CodeGenOperation op0 = feldOps.get(0);
		// We have to ensure that the first operation is a VkString, not an ordinary String
		if (op0 instanceof IConstantOperation && op0.getType() == OperationType.STRING) {
			buf.append(op0.toCode("extended"));
		} else {
			buf.append(op0.toCode());
		}
		for (int i = 1; i < feldOps.size(); i++) {
			buf.append(String.format(" %s %s ", calcOpCode, feldOps.get(i).toCode()));
		}
		return buf.toString();
	}

	public String getOpenBrackets() {
		return hasBrackets() ? "(" : "";
	}

	public String getClosingBrackets() {
		return hasBrackets() ? ")" : "";
	}

	@Override
	public void setCalcOp(CalcOp op) {
		this.calcOp = op;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setOperations(List<IOperation> fops) {
		this.feldOps = (List) fops;
	}

}
