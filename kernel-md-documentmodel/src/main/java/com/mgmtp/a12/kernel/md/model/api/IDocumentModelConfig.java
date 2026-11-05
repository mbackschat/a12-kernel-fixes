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

package com.mgmtp.a12.kernel.md.model.api;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import com.mgmtp.a12.kernel.md.utils.NotInD;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Configuration of the {@link IDocumentModel}.
 */
@OnlyForUsage
public interface IDocumentModelConfig extends Serializable {

	/**
	 * @return a list that contains all supported characters. During the validation, the string values (e.g. for string, enum or
	 * 		custom fields) are firstly checked character-wise, if a value contains at least one character that does not appear in this
	 * 		list, the value is reported as invalid. Never returns {@code null}. If this property was not defined, the list
	 * 		is empty. If an empty list is returned, then all characters from <i>Unicode-Plane-0</i> are valid.
	 */
	@NonNull List<String> getSupportedCharacters();

	/**
	 * The decimal separator used in A12 is the <b>point</b> ({@literal U+002E}). Additionally, a
	 * custom decimal separator can be independently defined here which won't have any effect in the
	 * A12 internal processes. Such information can be used in the custom processes.
	 *
	 * <p>
	 *
	 * <b>Example:</b> In some document models coming from ExtMP, this property is set to ','
	 * (comma) which is the common decimal separator in Germany. Data coming from ExtMP (at the
	 * moment, only in xml format, called eXML), which corresponds to the document models mentioned
	 * above, contains numbers like '10,50' but this will be converted to '10.50' in the
	 * corresponding A12 document. The information in this property is used for the documentation
	 * purpose. I.e. when the value equals comma then we know the ExtMP model has been created with
	 * comma as the decimal separator.
	 *
	 * @return the defined decimal separator. Not {@code null}.
	 */
	@NotInD
	@NonNull String getDecimalSeparator();

	/**
	 * The time zone used to interpret the date/time strings in the documents.
	 *
	 * @return the default time zone for date, date time, time and date range values.
	 */
	@NonNull String getTimeZone();

	/**
	 * The language used in the rule condition text (comments are not affected) and also for the reserved terms when defining
	 * parameters in the rule error message, e.g. ".value".<br>
	 * <b>Note: </b>Currently, only {@link Locale#US} and {@link Locale#GERMANY} are supported.
	 *
	 * @return the language used in the rule condition text (comments are not affected) and also for the reserved terms when
	 * 		defining parameters in the rule error message, e.g. ".value".
	 */
	@NonNull Locale getConditionLanguage();

	/**
	 * If true, then referencing fields in rule conditions is also allowed via the short name.
	 *
	 * @return true if referencing fields in rule conditions is also allowed via the short name,
	 * 		otherwise false.
	 */
	boolean isFieldRefByShortNameAllowed();
}
