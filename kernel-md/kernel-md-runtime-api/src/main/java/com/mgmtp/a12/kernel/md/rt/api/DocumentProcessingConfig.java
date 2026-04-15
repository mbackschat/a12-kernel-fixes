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
package com.mgmtp.a12.kernel.md.rt.api;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.customfieldtype.CustomFieldTypeService;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.rt.a12internal.custom.impl.CustomConditionService;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

// A12K-3589 remove this SuppressWarnings
@SuppressWarnings("removal")
@OnlyForUsage
public interface DocumentProcessingConfig {

	/**
	 * The locale for the validation error messages during source-data validation.
	 */
	@NonNull Locale languageOfErrorMessages();

	/**
	 * @return The {@link ICustomConditionFactory}. If you still use the deprecated static registration of your implementations via {@link CustomConditionService} return {@code null}.
	 */
	ICustomConditionFactory customConditionFactory();

	/**
	 * @return The {@link ICustomFieldTypeFactory}. If you still use the deprecated static registration of your implementations via {@link CustomFieldTypeService} return {@code null}.
	 */
	ICustomFieldTypeFactory customFieldTypeFactory();

	/**
	 * This data can be accessed in implementations of {@link ICustomFieldValidator}.
	 * @return The {@link Map} containing your custom configurations.
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link #customConditionFactory()} or {@link #customFieldTypeFactory()} instead. In your implementation
	 * 		of {@link ICustomFieldTypeFactory} or {@link ICustomConditionFactory} you can already create the objects in the suitable
	 * 		configuration. Therefore, it is not necessary to pass this data all the way through any longer.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	@NonNull Map<String, Object> additionalInformation();

	/**
	 * The rule language construct <code>Today</code> will evaluate to the passed date instead of the actual current date. If
	 * {@code null} the actual current date is used.
	 */
	Date currentDateForTest();

	/**
	 * @param validationLanguage
	 * 		The locale for error messages (e.g. during source-data validation).
	 * @return The {@link DocumentProcessingConfigBuilder}. Never {@code null}.
	 */
	static DocumentProcessingConfigBuilder builder(@NonNull Locale validationLanguage) {
		return new DocumentProcessingConfigBuilder(validationLanguage);
	}
}
