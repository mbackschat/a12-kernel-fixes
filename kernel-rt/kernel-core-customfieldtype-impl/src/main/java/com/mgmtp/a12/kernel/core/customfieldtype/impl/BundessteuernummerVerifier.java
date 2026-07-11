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
package com.mgmtp.a12.kernel.core.customfieldtype.impl;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.verifier.BundessteuernummerVerifierImpl;

import lombok.NonNull;

/**
 * The {@link ICustomFieldValidator} implementation for the German federal tax number (Bundessteuernummer).
 * <br>
 * <br>
 * The Bundessteuernummer has the following different representations:
 * <ul>
 * <li>the representation known to the taxpayer</li>
 * <li>the display representation</li>
 * <li>the XML representation, also called ELSTER format or internal format</li>
 * </ul>
 * These three types of representation are described below.
 * <ul>
 * <li>The <b>representation known to the taxpayer</b> contains federal-state-specific separators and starts with the
 * FA number (tax office number) and not with the unique 4-digit BUFA number (Bundesfinanzamtsnummer), because the 4-digit
 * BUFA number is not known to taxpayers.
 * <br>
 * <b>Note – Difference between FA number / BUFA number:</b>
 * <br>
 * The FA number is 3 digits. Unlike the 4-digit BUFA number, the first digit is missing. The first two digits of the
 * BUFA number are identical to the state number of the federal state. The federal states Bavaria and North Rhine-Westphalia
 * have multiple state numbers assigned, all other federal states only one. The state numbers of Bavaria start with the digit 9,
 * the state numbers of North Rhine-Westphalia with the digit 5.
 * <br>
 * The Bundessteuernummer cannot be fully validated in this representation because the 4-digit BUFA number is required.
 * To determine the BUFA number, the display representation described below is introduced.
 * </li>
 * <li> The <b>display representation</b> is a technically necessary representation agreed upon between the projects ExtMP and projAlpha.
 * In this representation, the first two digits of the BUFA number (the number of the regional tax authority) are appended to the
 * taxpayer-known representation, separated by the character '@'.
 * <br>
 * This representation allows ExtMP to fully validate the tax number without having to access the value of another
 * field.
 * </li>
 * <li>The Bundessteuernummer <b>in internal format</b> starts with the 4-digit BUFA number instead of the FA number
 * (tax office number) and contains no separators. The value must be transmitted to the downstream systems in ELSTER in this
 * format, which is why it is often called ELSTER format.
 * </li>
 * </ul>
 * <b>Validation of the Bundessteuernummer:</b>
 * <ul>
 * <li>
 * The validation of the <b>Bundessteuernummer</b> in ELSTER format (also called technical or internal format) is largely identical
 * to the validation algorithm for tax numbers in Bavaria (see also {@link SteuernummerVerifier}).
 * <p>
 * The validation of the BZSt tax number in ELSTER format consists of the following parts:<br>
 * <ul>
 * <li>Is the BZSt tax number formally valid?</li>
 * <li>Does the check digit calculation method yield the correct check digit?</li>
 * </ul>
 * </p>
 * <p>
 * In the formal check, the BZSt tax number must match the following regular expression:
 * <code>91160[0-9]{8}</code>
 * </p>
 * <p>
 * Finally, the BZSt tax number is also verified using a check digit calculation method.
 * </p>
 * </li>
 * <li>
 * To validate the <b>Bundessteuernummer in display format</b>, it is converted to ELSTER format and only then
 * is the actual validation performed. If the suffix '@' followed by the regional tax authority number is missing, it is an
 * integration error and an {@link IllegalArgumentException} is thrown.
 * </li>
 * <li>The <b>representation known to the taxpayer</b> cannot be validated by this class. In this
 * format, the federal state code appended after the '@' character is missing. If this format is passed for validation,
 * it is an integration error in the project and an {@link IllegalArgumentException} is thrown.</li>
 * </ul>
 * <br>
 *
 * @see com.mgmtp.a12.kernel.core.customfieldtype.impl
 */
public class BundessteuernummerVerifier {

	private BundessteuernummerVerifier() {
	}

	/**
	 * @return the instance of {@link ICustomFieldValidator}.
	 */
	public static @NonNull ICustomFieldValidator create() {
		return new BundessteuernummerVerifierImpl();
	}
}
