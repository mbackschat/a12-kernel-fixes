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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ResultBaseImpl;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ResultMessageV1;

/**
 * Hilfsklasse für ein Ergebnis. Wird zum Validieren benötigt, bevor ein Wert von der
 * Oberflächendarstellung in die XML Darstellung konvertiert werden kann.
 */
public class ErgebnisImpl extends ResultBaseImpl implements IResult {
	private String latestFehlerText = null;

	@Override protected void messageV1Hook(ResultMessageV1 messageV1) {
		latestFehlerText = messageV1.messageStr();
	}

	// Note: getCount was previously implemented in a very cheap way
	// latestFehlerText == null ? 0 : 1
	// this is not generally correct but may be good enough and possibly performance-critical
	// in the cases where ErgebnisImpl is used
	// -> if we observe performance problems, consider overriding getCount(ErrorType) with the cheap shortcut impl.

	public String getFehlerText() {
		return latestFehlerText;
	}
}
