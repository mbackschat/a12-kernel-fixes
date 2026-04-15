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
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.datum.TimeConstructionOperationImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.ITimeConstructionOperation;

/**
 * Creates a {@link ITimeConstructionOperation} with 0 to 3 parameters.
 * <p>
 * For the grammar rule
 * <p>
 * <code>
 * timeKonstrukt: 'Zeit' '(' (stunden=datumParam (',' minuten=datumParam (',' sekunden=datumParam ) ? ) ?) ?')';
 * </code>
 * <p>
 * the correct parseTree object is created.
 * <p>
 * All parameters are optional.
 */
public class TimeKonstruktCreator {

	private TimeKonstruktCreator() {
		throw new IllegalStateException("Utility class");
	}

	public static void create(ParseTreeCreator tc) {

		//Wir haben immer zwei Parameter,  d.h. hier muss nichts zusätzlich überprüft werden.
		IOperation dp1 = tc.popFop();
		if (dp1 instanceof ITimeConstructionOperation) { // No parameter given?
			tc.push(new TimeConstructionOperationImpl(null, null, null));
			return;
		}
		IOperation dp2 = tc.popFop();
		if (dp2 instanceof ITimeConstructionOperation) { // one parameter given.
			tc.push(new TimeConstructionOperationImpl(dp1, null, null));
			return;
		}
		IOperation dp3 = tc.popFop();
		if (dp3 instanceof ITimeConstructionOperation) { // two parameters given
			tc.push(new TimeConstructionOperationImpl(dp2, dp1, null));
			return;
		}

		tc.popFop();
		tc.push(new TimeConstructionOperationImpl(dp3, dp2, dp1)); // all three parameters given.
	}

	public static void addAnEmptyTimeKonstrukt(ParseTreeCreator tc) {
		tc.push(new TimeConstructionOperationImpl(null, null, null));
	}

}
