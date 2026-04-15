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

package com.mgmtp.a12.utils.conversion.internal.config;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.utils.conversion.DatePrecision;
import com.mgmtp.a12.utils.conversion.config.PartiallyKnownDateConversionConfig;

/**
 * Data type builder for partially known dates.
 */
public class PartiallyKnownDateConversionConfigBuilder extends DateBasicConversionConfigBuilder {

	private DatePrecision datePrecision = DatePrecision.FULL;

	public PartiallyKnownDateConversionConfigBuilder() {
		this(null);
	}

	public PartiallyKnownDateConversionConfigBuilder(PartiallyKnownDateConversionConfig other) {
		super(other);
		if (other != null) {
			this.format = StringUtils.isEmpty(other.format()) ? "yyyy-MM-dd" : other.format();
			this.datePrecision = other.datePrecision().orElse(DatePrecision.FULL);
		}
	}

	public PartiallyKnownDateConversionConfigBuilder withDatePrecision(DatePrecision datePrecision) {
		this.datePrecision = datePrecision;
		return this;
	}

	public PartiallyKnownDateConversionConfig build() {
		return new DefaultPartiallyKnownDateConversionConfig(modelId, modelPath, baseYear, timeZone, format, datePrecision);
	}

}
