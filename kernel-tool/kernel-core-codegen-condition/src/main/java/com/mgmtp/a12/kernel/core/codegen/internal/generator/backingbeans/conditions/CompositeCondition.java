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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.utils.RuntimeMethodNameFactory;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;

/**
 *
 */
public class CompositeCondition extends CodeGenCondition implements ICompositeCondition {
	private final LogOp logOp;
	private final List<CodeGenCondition> bedingungen;
	private final boolean isGeklammert;

	public CompositeCondition(LogOp logOp, boolean geklammert, List<CodeGenCondition> bedingungen) {
		this.logOp = logOp;
		this.isGeklammert = geklammert;
		this.bedingungen = new ArrayList<>(bedingungen);
	}

	public boolean hasBrackets() {
		return isGeklammert;
	}

	public List<CodeGenCondition> getBedingungen() {
		return bedingungen;
	}

	@Override
	public LogOp getLogOp() {
		return logOp;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<ICondition> getConditionList() {
		return (List) bedingungen;
	}

	/**
	 * Create the boolean code fragment for a composite condition.
	 */
	@Override
	public String getBooleanCode() {
		return getCondCode(CodeGenCondition::getBooleanCode);
	}

	public String getCondCode(Function<CodeGenCondition, String> innerCodeCreation) {
		String logOpCode = new LogOpCode(logOp).toCode();
		String code = bedingungen.stream().map(innerCodeCreation).collect(Collectors.joining(logOpCode));
		return addBracketsIfNecessary(code);
	}

	private String addBracketsIfNecessary(String code) {
		return (hasBrackets()) ? String.format("(%s)", code) : code;
	}

	public String getLogOpCode() {
		return RuntimeMethodNameFactory.getRuntimeMethodNameFromParseTreeObject(this);
	}

}
