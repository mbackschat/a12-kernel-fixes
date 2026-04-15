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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.util;

import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.FieldUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.ParseTreeCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.BoolConstantOperationImpl;
import com.mgmtp.a12.kernel.core.parser.internal.util.IVirtual;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.BoolConstantOperationName;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IYesNoType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IYesType;

/**
 * Hilfsklasse für gemeinsam verwendete Methoden.
 */
public class CreatorUtils {

	private CreatorUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String getUnknownTokenErrorMessage(Token t) {
		return String.format("internal error: unknown terminal '%s'", t.getText());

	}

	public static String tokenTextOrEmptyString(Token t) {
		return t == null ? "" : t.getText();
	}

	/**
	 * Sets the actual value which is used in the comparison for the bool constant.
	 * This is required for <code>VergleichsBedingung</code> and <code>AnzahlWertInFeldListe</code>
	 *
	 * @param konstante
	 * @param fid
	 * @param tc
	 */
	public static void setActualValueInBoolKonstanteIfNecessary(IOperation konstante, IEntityId fid, ParseTreeCreator tc) {
		if (!(konstante instanceof BoolConstantOperationImpl boolConstOp)) {
			return;
		}

		IField e = (IField) tc.getParser().getEc().get(fid.getFullName());
		IFieldType ft = e.getType();
		if (boolConstOp.getValue() == BoolConstantOperationName.True) {
			String trueValue;
			if (FieldUtils.isYes(e)) {
				trueValue = ((IYesType) ft).getTrueValue();
			} else if (FieldUtils.isYesNo(e)) {
				trueValue = ((IYesNoType) ft).getTrueValue();
			} else if (FieldUtils.isUnspecified(e) || e instanceof IVirtual) {
				// EXT-19695: we allow comparison with unspecified type
				trueValue = "";
			} else {// should not occur due to earlier check
				throw new IllegalArgumentException(String.format("Unexpected type for comparison with boolean constant: '%s'.",
						ft.getClass().getCanonicalName()));
			}
			boolConstOp.setStringValue(trueValue);
		} else { // boolConstOp.getValue() == BoolConstantOperationName.False
			String falseValue;
			if (FieldUtils.isYesNo(e)) {
				falseValue = ((IYesNoType) ft).getFalseValue();
			} else if (FieldUtils.isUnspecified(e) || e instanceof IVirtual) {
				// EXT-19695: we allow comparison with unspecified type
				falseValue = "";
			} else {// should not occur due to earlier check
				throw new IllegalArgumentException(String.format("Unexpected type for comparison with boolean constant: '%s'.",
						ft.getClass().getCanonicalName()));
			}
			boolConstOp.setStringValue(falseValue);
		}
	}

}
