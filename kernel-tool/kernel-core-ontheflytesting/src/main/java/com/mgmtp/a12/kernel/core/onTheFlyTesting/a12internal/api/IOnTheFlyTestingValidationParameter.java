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

package com.mgmtp.a12.kernel.core.onTheFlyTesting.a12internal.api;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.customfieldtype.CustomFieldTypeService;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.onTheFlyTesting.internal.OnTheFlyTestingService;
import com.mgmtp.a12.kernel.core.rt.a12internal.custom.ICustomConditionFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.custom.impl.CustomConditionService;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;

public interface IOnTheFlyTestingValidationParameter {
	/**
	 * Specifies the language that should be used for validation. Do not mix up with the
	 * programming language. And do not mix up with the language specified by the project specific
	 * settings. This should correspond with the language used in
	 * {@link IOnTheFlyTestingCodegenParameters#getLanguage()}.
	 *
	 * @return the language used for validation.
	 */
	Locale getLanguage();

	/**
	 * @return the data to be validated
	 */
	IData getData();

	/**
	 * @return the {@link IResult} receiving validation errors.
	 */
	IResult getResult();

	/**
	 * These should only be used in conjunction with {@link OnTheFlyTestingService}.
	 *
	 * @return all fields for which default values are to be used during validation
	 *
	 * @deprecated will be removed with A12K-3593
	 */
	@Deprecated(since = "30.2.0")
	Set<IIdentifier> getDefaultValues();

	/**
	 * @return a map from the name of a custom condition to an implementation thereof.
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link #customConditionFactory()}
	 * instead.
	 */
	@Deprecated(since = "30.6.0")
	Map<String, ICustomCondition> getCustomConditionsByName();

	/**
	 * Additional information that will be passed to the project specific implementation of the predefined field types.
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link #customFieldTypeFactory()} or {@link #customConditionFactory()}
	 * instead. In your implementation of {@link ICustomFieldTypeFactory} or {@link ICustomConditionFactory} you can already create the objects in the
	 * suitable configuration. Therefore, it is not necessary to pass this data all the way through any longer.
	 */
	@Deprecated(since = "30.6.0")
	Map<String, Object> getAdditionalInformation4PredefinedFieldTypes();

	/**
	 * If set, the rule language construct <code>Today</code> will evaluate to this value instead of
	 * the actual current date.<br>
	 * @deprecated implement {@link #currentDateForTest()} instead. Will be removed with A12K-3589.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	Date getCurrentDateForTest();

	default Date currentDateForTest() {
		return getCurrentDateForTest();
	}
	/**
	 * @return The {@link ICustomConditionFactory}. If you still use the deprecated static registration of your implementations via {@link CustomConditionService} return {@code null}.
	 */
	ICustomConditionFactory customConditionFactory();

	/**
	 * @return The {@link ICustomFieldTypeFactory}. If you still use the deprecated static registration of your implementations via {@link CustomFieldTypeService} return {@code null}.
	 */
	ICustomFieldTypeFactory customFieldTypeFactory();
}