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

package com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams;

import java.util.List;
import java.util.Locale;

/**
 * Intended to be implemented by the client.
 * Specifies the parameters required in both code generation and on-the-fly testing.
 */
public interface IModelCodeGeneratorParameters {

	/**
	 * The method specifies, if the generated code restricts the allowed grapheme set. If the
	 * returned list is empty, no additional checks are included in the generated
	 * code. If the method returns a non-empty list, the strings in the list can contain
	 *
	 * <ul>
	 *     <li>Graphemes of length 1 (single characters defined by one Unicode code point)</li>
	 *     <li>Ranges of Graphemes of length 1 (Unicode code point ranges)</li>
	 *     <li>Graphemes of length > 1 (Graphemes made up of code point sequences)</li>
	 * </ul>
	 *
	 * @return a list of Strings containing the valid grapheme set, if restrictions of the
	 * 		character set are specified. Otherwise, an empty list.
	 */
	List<String> restrictedValidGraphemeSet();

	/**
	 * Specifies the decimal separator the generated code uses to check the input data.
	 * <p>
	 * Please note: this specification is independent from the {@link Locale}s specified by
	 * {@link ICodeGeneratorParameters#getLanguages()}. At the moment only point (.) or comma (,)
	 * may be used as decimal separators.
	 *
	 * @return the character of the decimal separator.
	 */
	char getDecimalSeparator();

	/**
	 * Returns the meta information, which should be generated. The ordering of the list specifies
	 * the ordering in the generated code.
	 *
	 * @return the list of meta information objects.
	 */
	List<IMetaInfoCodeGenParams> getMetaInfoCodeGenParams();
}
