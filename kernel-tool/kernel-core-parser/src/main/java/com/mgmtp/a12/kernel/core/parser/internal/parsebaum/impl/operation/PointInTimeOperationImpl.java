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

import com.mgmtp.a12.kernel.core.parser.internal.check.util.DateUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IPointInTimeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.PointInTimeOperationName;

/**
 *
 */
public class PointInTimeOperationImpl extends OperationImpl implements IPointInTimeOperation {

	private final PointInTimeOperationName operatorName;

	public PointInTimeOperationImpl(PointInTimeOperationName name) {
		this(name, getFeldOperationArt(name));
	}

	public PointInTimeOperationImpl(PointInTimeOperationName name, OperationType opArt) {
		super(opArt, getFormat(name));

		this.operatorName = name;
	}

	public PointInTimeOperationImpl(IPointInTimeOperation op) {
		super(op);
		this.operatorName = op.getOperatorName();
	}

	/**
	 * The default is to use 'Today' as date and 'BaseYear' as number. If 'BaseYear'
	 * is used as a date, it is later transformed using
	 * {@link #PointInTimeOperationImpl(PointInTimeOperationName, OperationType)}
	 */
	private static OperationType getFeldOperationArt(PointInTimeOperationName name) {
		switch (name) {
			case Today:
			case Now:
				return OperationType.DATE;
			case BaseYear:
				return OperationType.NUMBER;
			default:
				throw new IllegalArgumentException("Point in time operation " + name + " not supported.");
		}
	}

	private static String getFormat(PointInTimeOperationName name) {
		switch (name) {
			case Today:
			case BaseYear:
				return DateUtils.DATUM_FORMAT_WITH_YEAR;
			case Now:
				return DateUtils.DATETIME_FORMAT;
			default:
				return null;
		}
	}

	@Override
	public String toString() {
		return String.format("Zeitpunkt<%s>", operatorName.getRuntimeName());
	}

	@Override
	public PointInTimeOperationName getOperatorName() {
		return operatorName;
	}

}
