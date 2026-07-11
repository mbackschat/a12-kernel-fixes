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

import java.io.Reader;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.verifier.EWAzVerifierImpl;

import lombok.NonNull;

/**
 * The {@link ICustomFieldValidator} implementation for the Einheitswert-Aktenzeichen (EW-Az, property tax reference number).
 * <br>
 * <br>
 * The EW-Az (short for 'Einheitswert-Aktenzeichen') has the following different representations:
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
 * Example of an EW-Az in this representation: 208/035-3-03889.3.
 * <br>
 * <b>Note – Difference between FA number / BUFA number:</b>
 * <br>
 * The FA number is 3 digits. Unlike the 4-digit BUFA number, the first digit is missing. The first two digits of the
 * BUFA number are identical to the state number of the federal state. The federal states Bavaria and North Rhine-Westphalia
 * have multiple state numbers assigned, all other federal states only one. The state numbers of Bavaria start with the digit 9,
 * the state numbers of North Rhine-Westphalia with the digit 5.
 * <br>
 * The EW-Az cannot be fully validated in this representation because the 4-digit BUFA number is required.
 * To determine the BUFA number, the display representation described below is introduced.
 * </li>
 * <li> The <b>display representation</b> is a technically necessary representation agreed upon between the projects ExtMP and projAlpha.
 * In this representation, the federal state code according to the ElsterBasis XML schema is appended to the taxpayer-known
 * representation, separated by the character '@'.
 * <br>
 * Example of an EW-Az in display representation: 208/035-3-03889.3@NW
 * <br>
 * This representation allows ExtMP to fully validate the EW-Az without having to access the value of another field
 * (the federal state).
 * <br>
 * <b>Note – The federal state parameter is sufficient:</b>
 * <br>
 * Knowledge of the federal state is sufficient to determine the BUFA number, even though the first digit of the tax office
 * number may be suppressed in the taxpayer-known representation, because the EW-Az in the taxpayer-known representation
 * always contains all three digits of the tax office number for the federal states Bavaria and North Rhine-Westphalia.
 * For all other federal states there is only one state number each; therefore, for these federal states the last two digits
 * of the FA number are sufficient to determine the unique BUFA number together with the federal state.
 * </li>
 * <li>Analogous to the Steuernummer, the EW-Az <b>in internal format</b> starts with the 4-digit BUFA number instead of the
 * FA number (tax office number). These digits are followed by the so-called format key. The format key has the value 4 for all
 * states. The remaining digits of the EW-Az are appended to the BUFA number without separators. For example, the EW-Az
 * “208/035-3-03889.3” in North Rhine-Westphalia has the value “520840353038893” in internal format. The first 3 digits of the
 * value – i.e. 208 – specify the tax office number, the unique 4-digit BUFA number for this is 5208.
 * The value must be transmitted to the downstream systems in ELSTER in this format, which is why it is often called ELSTER format.
 * </li>
 * </ul>
 * <b>Validation of the EW-Az:</b>
 * <ul>
 * <li>
 * The validation of the <b>EW-Az in ELSTER format</b> (the technical or internal format) consists
 * of the following parts:
 * <ul>
 * <li>Is the EW-Az formally valid?</li>
 * <li>Does the BUFA number contained in the EW-Az exist?</li>
 * <li>Does the check digit calculation method yield the correct check digit?</li>
 * </ul>
 *
 * <p>
 * In the formal check, the EW-Az must match the following regular expression:
 * <code>[0-9]{4}[045][0-9]{8,14}</code>
 * </p>
 *
 * <p>
 * The first four characters of the Einheitswert-Aktenzeichen form the BUFA number. It is
 * verified whether this BUFA number is permitted according to the authoritative list in the technical
 * requirements specification (V35, chapter 1.2.2).
 * </p>
 * <p>
 * At the 5th position is the format key. For the classification term “Einheitswertaktenzeichen”
 * of the property tax, it has the value 0, 4, or 5 in different states.
 *
 * <p>
 * Special format rules for Schleswig-Holstein and North Rhine-Westphalia are also checked.
 * </p>
 *
 * <p>
 * Finally, the Einheitswert-Aktenzeichen is also verified using a check digit calculation method.
 * </p>
 * </li>
 * <li>
 * To validate the <b>EW-Az in display format</b>, it is converted to ELSTER format and only then
 * is the actual validation performed. If the suffix '@' followed by the federal state code is missing, it is an
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
public class EWAzVerifier {

	private EWAzVerifier() {
	}

	/**
	 * @param fadat
	 * 		the federal tax office data. Can be created via {@link IFadat#deserialize(Reader, boolean)}.
	 * @return the instance of {@link ICustomFieldValidator}.
	 */
	public static @NonNull ICustomFieldValidator create(@NonNull IFadat fadat) {
		return new EWAzVerifierImpl(fadat);
	}
}
