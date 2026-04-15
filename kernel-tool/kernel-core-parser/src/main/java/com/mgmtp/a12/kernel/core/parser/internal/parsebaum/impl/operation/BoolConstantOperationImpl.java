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

package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IBoolConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.BoolConstantOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;

/**
 * Die Konstante, die für WAHR bzw. FALSCH verwendet werden, werden mit den konkreten Werten
 * besetzt, die abgeglichen werden müssen. Diese hängen von dem aktuellen WAHR bzw. FALSCH Wert der
 * FeldOperation ab, die beim konkreten Vergleich verwendet wird.
 * Für die Auswertung werden direkt diese WAHR bzw. FALSCH Werte verwendet, daher
 * müssen diese Werte gespeichert werden.
 */
public final class BoolConstantOperationImpl extends OperationImpl implements IBoolConstantOperation {

	private final BoolConstantOperationName value; // WAHR bzw. FALSCH
	private String stringValue = ""; // konkreter Wert,  der in der Operation abgeglichen werden muss.

	public BoolConstantOperationImpl(BoolConstantOperationName name) {
		super(OperationType.STRING);
		this.value = name;
	}

	public BoolConstantOperationImpl(IBoolConstantOperation op) {
		super(op);
		value = op.getValue();
		stringValue = op.getStringValue();
	}

	@Override
	public String toString() {
		return String.format("%s[\"%s\"]", value.getRuntimeName(), stringValue);
	}

	@Override
	public BoolConstantOperationName getValue() {
		return value;
	}

	public void setStringValue(String value) {
		stringValue = value;
	}

	@Override
	public String getStringValue() {
		return stringValue;
	}

}
