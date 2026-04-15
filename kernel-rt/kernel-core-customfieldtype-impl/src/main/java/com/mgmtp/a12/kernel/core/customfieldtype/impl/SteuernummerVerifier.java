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

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberFormatting;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.verifier.SteuernummerVerifierImpl;

import lombok.NonNull;

/**
 * The {@link ICustomFieldValidator} implementation for the German tax number (Steuernummer).
 * <br>
 * <br>
 * The Steuernummer has the following different representations:
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
 * Example of a Steuernummer in this representation: 102/384/0020/002/001/5.
 * <br>
 * <b>Note – Difference between FA number / BUFA number:</b>
 * <br>
 * The FA number is 3 digits. Unlike the 4-digit BUFA number, the first digit is missing. The first two digits of the
 * BUFA number are identical to the state number of the federal state. The federal states Bavaria and North Rhine-Westphalia
 * have multiple state numbers assigned, all other federal states only one. The state numbers of Bavaria start with the digit 9,
 * the state numbers of North Rhine-Westphalia with the digit 5.
 * <br>
 * The Steuernummer cannot be fully validated in this representation because the 4-digit BUFA number is required.
 * To determine the BUFA number, the display representation described below is introduced.
 * </li>
 * <li> The <b>display representation</b> is a technically necessary representation agreed upon between the projects ExtMP and projAlpha.
 * In this representation, the first two digits of the BUFA number (the number of the regional tax authority) are appended to the
 * taxpayer-known representation, separated by the character '@'.
 * <br>
 * Example of a Steuernummer in display representation: 204/704/40466@32
 * <br>
 * This representation allows ExtMP to fully validate the tax number without having to access the value of another
 * field.
 * </li>
 * <li>The Steuernummer <b>in internal format</b> starts with the 4-digit BUFA number instead of the FA number
 * (tax office number) and contains no separators. The value must be transmitted to the downstream systems in ELSTER in this
 * format, which is why it is often called ELSTER format.
 * </li>
 * </ul>
 * <b>Validation of the Steuernummer:</b>
 * <ul>
 * <li>
 * The validation of the <b>Steuernummer in ELSTER format</b> (the technical or internal format) consists of the following parts:
 * <ul>
 * <li>Is the Steuernummer formally valid?</li>
 * <li>Does the Bundesfinanzamtsnummer contained in the Steuernummer exist?</li>
 * <li>Is the district number contained in the Steuernummer non-zero as a number?</li>
 * <li>Is no federal-state-specific negative pattern matched?</li>
 * <li>Is the district number not equal to 0000, 0998, 0999 for NRW or not equal to 000, 998, 999 otherwise
 * (implemented as a federal-state-specific negative pattern)?</li>
 * <li>Does the check digit calculation method yield the correct check digit?</li>
 * </ul>
 *
 * <p>
 * In the formal check, the Steuernummer must match the following regular expression:
 * <code>[0-9]{4}0[0-9]{8}</code>
 * </p>
 *
 * <p>
 * The first four characters of the Steuernummer form the Bundesfinanzamtsnummer.
 * </p>
 *
 * <p>
 * The district number starts at the sixth position of the Steuernummer. The number of positions of the
 * district number is state-specific and can be determined via the method
 * {@link IGermanTaxNumberFormatting#getNumPositionsDistrict}. An implementation of the interface
 * {@link IGermanTaxNumberFormatting} can in turn be obtained via the method
 * {@link IGermanTaxNumberDefiningData#getFormatting} for the federal state number. The first two positions of the
 * Steuernummer form the so-called federal state number.
 * </p>
 *
 * <p>
 * Then it is verified that no state-specific negative pattern is matched. The method
 * {@link IGermanTaxNumberDefiningData#getNegativePattern} returns a vector of pairs consisting of
 * negative pattern and error message for the federal state number.
 * </p>
 * <p>
 * The interfaces
 * {@link IGermanTaxNumberDefiningData} and {@link IGermanTaxNumberFormatting} are implemented by the
 * calling system.
 *
 * <p>
 * Finally, the Steuernummer is also verified using a check digit calculation method.
 * </p>
 * </li>
 * <li>
 * To validate the <b>Steuernummer in display format</b>, it is converted to ELSTER format and only then
 * is the actual validation performed. If the suffix '@' followed by the regional tax authority number is missing, it is an
 * integration error and an {@link IllegalArgumentException} is thrown.
 * <br>
 * This behavior applies not only to the method {@link ICustomFieldValidator#validate(String, ICustomFieldTypeValidationParam, boolean)}
 * but also to {@link ICustomFieldValidator#convertDisplay2Internal(String)}.
 * </li>
 * <li>The <b>representation known to the taxpayer</b> cannot be validated by this class. In this
 * format, the regional tax authority number appended after the '@' character is missing. If this format is passed for validation,
 * it is an integration error in the project and an {@link IllegalArgumentException} is thrown.</li>
 * </ul>
 * <br>
 *
 * @see com.mgmtp.a12.kernel.core.customfieldtype.impl
 */
public class SteuernummerVerifier {

	private SteuernummerVerifier() {
	}

	/**
	 * @return the instance of {@link ICustomFieldValidator}.
	 *
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link #create(IFadat)} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	public static ICustomFieldValidator getInstance() {
		return new SteuernummerVerifierImpl(null);
	}
	
	/**
	 * @param fadat
	 * 		the federal tax office data. Can be created via {@link IFadat#deserialize(Reader, boolean)}.
	 * @return the instance of {@link ICustomFieldValidator}.
	 */
	public static @NonNull ICustomFieldValidator create(@NonNull IFadat fadat) {
		return new SteuernummerVerifierImpl(fadat);
	}
}
