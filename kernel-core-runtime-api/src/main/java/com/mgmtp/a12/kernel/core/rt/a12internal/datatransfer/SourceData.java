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

package com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Use {@link #of(String, String, IData, IResult)} to create an instance.
 */
@OnlyForUsage
public interface SourceData {
	/** The name of the source, as defined in the MappingModel for this datatransfer **/
	String sourceName();

	/** An id that will be returned in the source identifiers {@link SourceIdentifier} **/
	String sourceId();

	IData data();

	/**
	 * this result will receive any eligibility-validation errors that may occur.
	 * If an error is reported to this result, the SourceData will be omitted from the data transfer due to the
	 * validation failure.
	 */
	IResult result();

	static SourceData of(String sourceName, String sourceId, IData data, IResult result) {
		return new SourceDataImpl(sourceName, sourceId, data, result);
	}
}
