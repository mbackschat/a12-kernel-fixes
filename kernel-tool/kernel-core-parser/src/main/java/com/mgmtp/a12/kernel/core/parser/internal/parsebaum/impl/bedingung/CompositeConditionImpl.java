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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;

public class CompositeConditionImpl extends ConditionImpl implements ICompositeCondition {

	private final LogOp logOp;
	private final List<ICondition> bedingungen;
	private boolean brackets = false;

	public CompositeConditionImpl(LogOp logOp, List<ICondition> bedingungen) {
		this.logOp = logOp;
		this.bedingungen = new ArrayList<>(bedingungen);
	}

	public CompositeConditionImpl(LogOp logOp, List<ICondition> bedingungen, boolean isGeklammert) {
		this(logOp, bedingungen);
		this.brackets = isGeklammert;
	}

	public CompositeConditionImpl(ICompositeCondition b, List<ICondition> bedingungen) {
		super(b);
		this.logOp = b.getLogOp();
		this.bedingungen = new ArrayList<>(bedingungen);
		this.brackets = b.hasBrackets();
	}

	public CompositeConditionImpl(ICompositeCondition b, List<ICondition> bedingungen, boolean brackets) {
		super(b);
		this.logOp = b.getLogOp();
		this.bedingungen = new ArrayList<>(bedingungen);
		this.brackets = brackets;
	}

	@Override
	public LogOp getLogOp() {
		return logOp;
	}

	@Override
	public List<ICondition> getConditionList() {
		return bedingungen;
	}

	public void setGeklammert() {
		this.brackets = true;
	}

	public String toString() {
		String oB = brackets ? "(" : "";
		String cB = brackets ? ")" : "";
		String op = String.format(" %s ", logOp.name());
		String zs = bedingungen.stream()
				.map(ICondition::toString)
				.collect(Collectors.joining(op));
		return String.format("%s%s%s", oB, zs, cB);
	}

	@Override
	public boolean hasBrackets() {
		return brackets;
	}
}
