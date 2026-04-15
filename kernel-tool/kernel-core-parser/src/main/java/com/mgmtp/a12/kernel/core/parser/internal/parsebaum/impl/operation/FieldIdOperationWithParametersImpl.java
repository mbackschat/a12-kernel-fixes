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

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperationWithParameters;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.FieldIdOperationName;

/**
 *
 */
public class FieldIdOperationWithParametersImpl extends FieldIdOperationImpl implements IFieldIdOperationWithParameters {

	private final boolean isErsterTag;
	private final int bereichStart;
	private final int bereichEnde;
	private final int anzStellen;

	public FieldIdOperationWithParametersImpl(FieldIdOperationName operatorName, IEntityId feldId, int bereichStart,
			int bereichEnde) {
		this(operatorName, feldId, bereichStart, bereichEnde, false, 0, null);
	}

	public FieldIdOperationWithParametersImpl(
			FieldIdOperationName operatorName,
			IEntityId feldId,
			boolean isErsterTag,
			String format) {
		this(operatorName, feldId, 0, 0, isErsterTag, 0, format);
	}

	public FieldIdOperationWithParametersImpl(FieldIdOperationName operatorName, IEntityId feldId, int anzStellen) {
		this(operatorName, feldId, 0, 0, false, anzStellen, null);
	}

	public FieldIdOperationWithParametersImpl(FieldIdOperationName operatorName, IEntityId feldId, int bereichStart,
			int bereichEnde,
			boolean isErsterTag, int anzStellen, String format) {
		super(operatorName, feldId);
		this.bereichStart = bereichStart;
		this.bereichEnde = bereichEnde;
		this.setFormat(format);
		this.isErsterTag = isErsterTag;
		this.anzStellen = anzStellen;
	}

	public FieldIdOperationWithParametersImpl(IFieldIdOperationWithParameters op, IEntityId id) {
		super(op, id);
		this.bereichStart = op.getRangeStart();
		this.bereichEnde = op.getRangeEnd();
		this.isErsterTag = op.isFirstDay();
		this.anzStellen = op.getNumberOfDecimalPlaces();
	}

	@Override
	public String toString() {
		String suffix;
		switch (getOperatorName()) {
			// WertAlsDatum wird nur im ExtMP Kontext verwendet,  daher ist die Deutsche Log-Ausgabe o.k.
			case ValueAsDate:
				suffix = (isErsterTag ? ",ErsterTag" : ",LetzterTag");
				break;
			case RangeAsString:
			case RangeAsNumber:
				suffix = String.format(",%s,%s", bereichStart, bereichEnde);
				break;
			case RoundAccountingValue:
			case RoundUpValue:
			case RoundDownValue:
				suffix = String.format(",%d", anzStellen);
				break;
			default:
				suffix = "";
		}
		return String.format("%s(%s%s)", getOperatorName().getRuntimeName(), getField(), suffix);
	}

	@Override
	public boolean isFirstDay() {
		return isErsterTag;
	}

	@Override
	public int getRangeStart() {
		return bereichStart;
	}

	@Override
	public int getRangeEnd() {
		return bereichEnde;
	}

	@Override
	public int getNumberOfDecimalPlaces() {
		return anzStellen;
	}

}
