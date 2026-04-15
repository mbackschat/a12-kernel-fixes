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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation;

import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.ParseTreeCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.datum.DateTimeConstructionOperationImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateTimeConstructionOperation;

/**
 * Creates an {@link IDateTimeConstructionOperation} for 2 - 6 parameters.
 * <p>
 * For the grammar rule
 * <p>
 * <code>
 * dateTimeKonstrukt: 'DateTime' '(' date=datumAngabe2 ',' time=datumAngabe ')';
 * </code>
 * <p>
 * the correct parse tree object will be created.
 * <p>
 * Parameters 2 to 6 are optional because you can create a DateTime by specifying:
 * <ul>
 * <li>a date and a time parameter</li>
 * <li>a date and hour, minute, second, where hour, minute and second are optional</li>
 * <li>year, month, day, and a time parameter</li>
 * <li>year, month, day, hour, minute, second, where hour, minute and second are optional.</li>
 * </ul>
 */
public class DateTimeKonstruktCreator {

	private DateTimeKonstruktCreator() {
		throw new IllegalStateException("Utility class");
	}

	public static void create(ParseTreeCreator tc) {
		IOperation timeOp = tc.popFop();
		IOperation dateOp = tc.popFop();

		tc.push(new DateTimeConstructionOperationImpl(dateOp, timeOp));
	}
}
