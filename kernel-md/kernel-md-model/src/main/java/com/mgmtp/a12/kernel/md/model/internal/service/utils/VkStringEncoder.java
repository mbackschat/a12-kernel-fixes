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
package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import org.apache.commons.lang3.Validate;

/**
 * Provides encoding of string values according to the rules of
 * com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters#getName()
 * <p>
 * The underscore is used as the escape character.
 *
 * <p>
 * This class is necessary since there is a difference between ExtMP and A12 regarding the allowed
 * characters for the model name:
 * <ul>
 * <li>ExtMP: <i>[A-za-z_][A-za-z0-9_]*</i> (see NoMetaModelChecks#MODEL_NAME_PATTERN)</li>
 * <li>A12: <i>[_A-za-z][-_.a-zA-Z0-9]*</i> (see ModelNameUtil#VALID_CHARACTERS_PATTERN)</li>
 * </ul>
 * In other words, only because of hyphens and periods.
 */
public class VkStringEncoder {

	private static final String VALID_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789";

	private VkStringEncoder() {
		throw new IllegalStateException("Utility class");
	}

	public static String encode(final String input) {
		Validate.notBlank(input);
		final StringBuilder sb = new StringBuilder();
		boolean isFirstCharacter = true;
		for (final Character c : input.toCharArray()) {
			if (VALID_CHARACTERS.indexOf(c) >= 0 && !(isFirstCharacter && Character.isDigit(c))) {
				sb.append(c);
			} else {
				// add escape character
				sb.append('_');
				// convert to numerical (int) representation
				final int convertedChar = c;
				sb.append(String.format("%04x", convertedChar));
			}
			isFirstCharacter = false;
		}
		return sb.toString();
	}
}
