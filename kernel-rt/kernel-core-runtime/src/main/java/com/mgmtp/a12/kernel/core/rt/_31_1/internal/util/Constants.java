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

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * Klasse kapselt alle verwendeten Konstanten
 */
public class Constants {

	public static final String FIELD_LAYER_SEPARATOR = "@";
	public static final String SEMANTIC_INDEX_SEPARATOR = "#";

	public static final long SEMANTIC_INDEX = IIdentifier.SEMANTIC_INDEX;

	// allgemeine Strings für die Formatprüfung
	public static final String BLANK = " ";

	//	Fehlermeldungen für Datum Format:
	//	--------------------------------
	//allgemeine Strings: dieses Datum gilt zur Festlegung eines gültigen Datums,
	// ein Datum muss später als dieses Datum sein, außer es wurde beim Datumsformat
	// definiert, dass keine Zusatzprüfung erfolgen soll.
	public static final String DATUM_FORMAT = "dd.MM.yyyy";
	public static final String DATE_FROM_FOR_ADDITIONAL_CHECK = "01.01.1900";
	public static final String DATE_FROM = "16.10.1583"; // begin of the gregorian calendar

	public static final String FORMAL_ERRORS_MESSAGES_RESOURCE_NAME = "vk_rt_formal_errors/error_messages";
	public static final String DATATRANSFER_ERROR_MESSAGES_MESSAGES_RESOURCE_NAME = "datatransfer_rt_errors/error_messages";
	public static final String LOCALIZATION_HELPER_RESOURCE_NAME = "vk_rt_localization_helper/helper";

	/**
	 * Used in rule error texts to access to the value of the field.
	 * <p>
	 * Format: $FIELD_NAME.Wert$
	 * </p>
	 * Note: If the rule error text was written in English with $FIELD_NAME.value$, the code
	 * generator replaces the substring ".value" with ".Wert". In this way we have to deal only with
	 * ".Wert" in the runtime.
	 */
	public static final String ERROR_TEXT_PARAM_4_FIELD_VALUE = ".Wert";
	/**
	 * Used in rule error texts to access to the corresponding category value of the field.
	 * <p>
	 * Format: {@literal $FIELD_NAME->CATEGORY_VALUE$}
	 * </p>
	 * Note: This constant must be equal to EntityIdConstants.CATEGORY_VALUE_ACCESS
	 */
	public static final String CATEGORY_VALUE_ACCESS = "->";
	/**
	 * Used in rule error texts to access index field properties
	 * <p>
	 * Format: {@literal $index(FIELD_NAME).Wert$ resp. $index(FIELD_NAME)->CATEGORY_VALUE$}
	 * </p>
	 * Note: If the rule error text was written in English with $index(FIELD_NAME).value$, the code
	 * generator replaces the substring ".value" with ".Wert". In this way we have to deal only with
	 * ".Wert" in the runtime.
	 */
	public static final String ERROR_TEXT_PARAM_4_INDEX_FIELD_REF = "index(";

	/**
	 * Used in formal error texts to access to the value of the field.
	 * <p>
	 * Format: "$<fieldName>.Value$"
	 * <p>
	 */
	public static final String FORMAL_ERROR_TEXT_PARAM_4_FIELD_VALUE = "$<fieldName>.Value$";

	/**
	 * <b>Note:</b>
	 * The value of 'RUNTIME_VERSION' is generally not equal to the 'version' of the module,
	 * because, for example in case of a minor patch that does not change the internal interface
	 * between generated code and runtime, 'version' needs to be changed, but 'RUNTIME_VERSION'
	 * can remain the same. This has the advantage for the projects that they do not need to
	 * re-generate code.
	 * <p>
	 * <b>Note:<b>
	 * For Java and Groovy, the compatibility check between runtime and generated code does no longer rely on the version constant
	 * here and in GeneratorConstants, but our runtime-vs-runtime-api compatibility check relies on the version
	 * constants.
	 * <p>
	 * <b>WARNING:</b> Don't change this line. Updated by versionupdate script
	 * <p>
	 * <b>Don't remove 'new String(..)' to prevent constant inlining (at compile-time).</b>
	 */
	public static final String RUNTIME_VERSION = new String("31.1");

	// must be updated manually, when the runtime (or generated code) start using new api features
	// (i.e. typically this may occur when a new api version is released)
	// see MGM Wiki Shortlink x/ZGQqDQ for details.
	// Don't remove 'new String(..)' to prevent constant inlining (at compile-time).
	public static final String OLDEST_COMPATIBLE_RUNTIME_API_VERSION = new String(
			"32.2.8");
	// must be updated manually, when the runtime (or generated code) start using new api features
	// (i.e. typically this may occur when a new api version is released)
	// see MGM Wiki Shortlink x/ZGQqDQ for details.
	// Don't remove 'new String(..)' to prevent constant inlining (at compile-time).
	public static final String OLDEST_COMPATIBLE_CUSTOMFIELDTYPE_API_VERSION = new String(
			"28.1.4");

	private Constants() {
		throw new IllegalStateException("Utility class");
	}
}
