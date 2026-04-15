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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.datum;

import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.OperationImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateRangeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;

/**
 *
 */
public class DateRangeConstructionOperationImpl extends OperationImpl implements IDateRangeConstructionOperation {

	private final IEntityId date1Id;
	private final IEntityId date2Id;

	public DateRangeConstructionOperationImpl(IEntityId date1Id, IEntityId date2Id, String dateFormat) {
		super(OperationType.DATE_RANGE, dateFormat);

		this.date1Id = date1Id;
		this.date2Id = date2Id;
	}

	@Override
	public String toString() {
		return String.format("DateRange(%s, %s; %s)", date1Id, date2Id, getFormat());
	}

	@Override public IEntityId getDate1Id() {
		return date1Id;
	}

	@Override public IEntityId getDate2Id() {
		return date2Id;
	}
}
