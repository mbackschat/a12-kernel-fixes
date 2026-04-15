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

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.custom.ICustomConditionFactory;

public class DataTransferConfigBuilder {
	private Locale validationLanguage;
	@Deprecated(since = "30.6.0", forRemoval = true)
	private Map<String, Object> additionalInformation = Map.of();
	private ICustomFieldTypeFactory customFieldTypeFactory = null;
	private ICustomConditionFactory customConditionFactory = null;
	private Date currentDateForTest = null;

	public DataTransferConfigBuilder(Locale validationLanguage) {
		this.validationLanguage = validationLanguage;
	}

	@Deprecated(since = "30.6.0", forRemoval = true)
	public DataTransferConfigBuilder additionalInformation(Map<String, Object> additionalInformation) {
		this.additionalInformation = additionalInformation;
		return this;
	}

	/**
	 * @param customFieldTypeFactory see {@link IDataTransferService.DataTransferConfig#customFieldTypeFactory()}
	 * @return this builder
	 */
	public DataTransferConfigBuilder customFieldTypeFactory(ICustomFieldTypeFactory customFieldTypeFactory) {
		this.customFieldTypeFactory = customFieldTypeFactory;
		return this;
	}

	/**
	 * @param customConditionFactory see {@link IDataTransferService.DataTransferConfig#customConditionFactory()}
	 * @return this builder
	 */
	public DataTransferConfigBuilder customConditionFactory(ICustomConditionFactory customConditionFactory) {
		this.customConditionFactory = customConditionFactory;
		return this;
	}

	/**
	 * @param currentDateForTest see {@link IDataTransferService.DataTransferConfig#currentDateForTest()}
	 * @return this builder
	 */
	public DataTransferConfigBuilder currentDateForTest(final Date currentDateForTest) {
		this.currentDateForTest = currentDateForTest;
		return this;
	}

	// A12K-3589 remove this SuppressWarnings
	@SuppressWarnings("removal")
	public IDataTransferService.DataTransferConfig build() {
		return new DataTransferConfigImpl(validationLanguage, additionalInformation, customFieldTypeFactory, customConditionFactory, currentDateForTest);
	}

}
