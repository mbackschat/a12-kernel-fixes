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

import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.CodeGenOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.RuntimeMethodNameFactory;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ValueListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.services.ParseBaumBedingungsServices;

/**
 * parameter class for a value list condition
 */
public class ValueListCondition extends CodeGenCondition implements IValueListCondition {

	private final List<CodeGenOperation> feldOperationen;

	private final EntityId feldId;
	private final ValueListConditionName bedingungName;

	public ValueListCondition(IValueListCondition b, EntityId entity, List<CodeGenOperation> ops) {
		this.feldId = entity;
		this.bedingungName = b.getConditionName();
		this.feldOperationen = ops;
	}

	@Override
	public ValueListConditionName getConditionName() {
		return bedingungName;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<IOperation> getValueList() {
		return (List) this.feldOperationen;
	}

	@Override
	public IEntityId getField() {
		return feldId;
	}

	/**
	 * Je nachdem ob der Wert über eine StringListe oder eine Zahlliste geprüft werden soll, kommt
	 * an den Namen der Methode ein Suffix dran.
	 */
	public String getBedingungNameCode() {
		return RuntimeMethodNameFactory.getRuntimeMethodNameFromParseTreeObject(this);
	}

	public String getFeldIdCode() {
		return feldId.toCode();
	}

	public List<String> getOpCodeFragments() {
		return feldOperationen.stream().map(CodeGenOperation::toCode).collect(Collectors.toList());
	}

	public boolean isStringList() {
		return ParseBaumBedingungsServices.isStringListe(this);
	}
}
