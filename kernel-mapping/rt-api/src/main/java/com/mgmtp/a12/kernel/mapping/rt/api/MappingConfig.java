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

package com.mgmtp.a12.kernel.mapping.rt.api;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Please use {@link DocumentProcessingConfig} instead.
 */
@OnlyForUsage
@Deprecated(since = "30.6.0", forRemoval = true)
public interface MappingConfig extends DocumentProcessingConfig {

	static DocumentProcessingConfig of(@NonNull Locale validationLanguage) {
		return of(validationLanguage, Map.of(), null);
	}

	/**
	 * @param validationLanguage
	 * 		The locale for error messages (e.g. during source-data validation).
	 * @param additionalInformation
	 * 		The additional information that will be passed to the project specific implementations
	 * 		of {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator}s (Can be empty if no custom field type is
	 * 		used that needs such configData).
	 * @param currentDateForTest
	 * 		The rule language construct <code>Today</code> will evaluate to the passed date instead of
	 * 		the actual current date. If {@code null} the actual current date is used.
	 * @return the {@link MappingConfig}
	 */
	@SuppressWarnings("removal")
	static DocumentProcessingConfig of(@NonNull Locale validationLanguage, @NonNull Map<String, Object> additionalInformation,
			Date currentDateForTest) {
		return DocumentProcessingConfig.builder(validationLanguage)
				.additionalInformation(additionalInformation)
				.currentDateForTest(currentDateForTest)
				.build();
	}
}