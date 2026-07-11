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

package com.mgmtp.a12.kernel.md.model.a12internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mgmtp.a12.kernel.core.base.utils.internal.BaseUtilConstants;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelConfig;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Configuration of the {@link DocumentModel}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentModelConfig implements Serializable {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return a list with all supported characters for the {@link DocumentModel}. Is never
	 *        {@code null}. If this property was not defined, the list is empty.
	 * @param supportedCharacters
	 * 		a list with the new supported characters for the
	 *        {@link DocumentModel}. May not be
	 *        {@code null}. Use an empty list to unset.
	 */
	@Builder.Default
	private @NonNull List<String> supportedCharacters = new ArrayList<>();

	/**
	 * The decimal separator defined here is currently ignored in A12. It is used as placeholder for
	 * model compatibility with the ExtMP. The desired effects of this configuration has to be clarified.
	 *
	 * @return the defined decimal separator for the {@link DocumentModel}. Is never {@code null} or
	 * 		empty.
	 * @param decimalSeparator
	 * 		the new decimal separator. May not be {@code null} or empty. Default
	 * 		value is '.'.
	 */
	@Builder.Default
	private @NonNull String decimalSeparator = ".";

	/**
	 * The time zone used for the interpretation of date, date time, time, and date range
	 * objects.
	 *
	 * @return the time zone used to interpret date-/time-strings.
	 * @param timeZone
	 * 		the time zone to interpret date-/time-strings.
	 */
	@Builder.Default
	private @NonNull String timeZone = BaseUtilConstants.TIME_ZONE_UTC;

	/**
	 * See {@link IDocumentModelConfig#getConditionLanguage()}.
	 *
	 * @param conditionLanguage
	 * 		the language used in the rule condition text (comments are not affected) and also for the reserved terms when
	 * 		defining parameters in the rule error message, e.g. ".value". May not be {@code null}. Default
	 * 		value is {@code Locale.US}.
	 */
	@Builder.Default
	private @NonNull Locale conditionLanguage = Locale.US;

	/**
	 * See {@link IDocumentModelConfig#isFieldRefByShortNameAllowed()}.
	 */
	@Builder.Default
	private boolean fieldRefByShortNameAllowed = false;

}
