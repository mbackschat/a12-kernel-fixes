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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.dynamicEnum;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions.CodeGenCondition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations.CodeGenOperation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * Models a calculation operation for a dynamic enumeration with an optional precondition.
 * <p>
 * Such an operation has three parts:
 * <ul>
 * <li>the field, the operation is performed for,</li>
 * <li>the optional negative precondition, that suppresses value calculation,</li>
 * <li>and the calculation operation to be performed.</li>.
 * <p>
 * An instance of this class encapsulates the parameters needed by templates.
 */
@StringTemplateGroup("dynamicEnum")
public class CodeGenDynamicEnumerationValue extends STParameterClassBase {
	// Die ursprünglichen Objekte.
	private CodeGenCondition codeGenPreCondition;

	private CodeGenOperation codeGenValueOperation;

	public CodeGenDynamicEnumerationValue(CodeGenCondition preCondition, CodeGenOperation calcFunction) {
		this.codeGenPreCondition = preCondition;
		this.codeGenValueOperation = calcFunction;
	}

	/**
	 * @return den Inhalt des if-Statements für die Vorbedingung.
	 */
	public String getPreConditionBooleanCode() {
		if (codeGenPreCondition == null) {
			// non existent conditions do not suppress value calculation
			return "false";
		}
		return codeGenPreCondition.getBooleanCode();
	}

	public boolean isWithPreCondition() {
		return codeGenPreCondition != null;
	}

	public String getDynEnumCalcCode() {
		return codeGenValueOperation.toCode();
	}
}
