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
package com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.utils;

import com.mgmtp.a12.kernel.core.base.utils.internal.RegExpLengthTester;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Contains different utility methods specific to the ExtMP world also used by tool server.
 */
@OnlyForUsage
public class ExtmpToolUtils {

	/**
	 * Maximal allowed value length with which a field can be filled.
	 */
	public static final int DEFAULT_MAX_VALUE_LENGTH = 99999;
	/**
	 * Maximal allowed value length with which a field can be filled in host-based models.
	 */
	public static final int DEFAULT_MAX_VALUE_LENGTH_4_HOST_BASED_MODELS = 999;
	public static final int MAX_DIGITS_ALLOWED = 15;
	public static final int MIN_FRACTIONAL_DIGITS = 1;
	public static final int MAX_FRACTIONAL_DIGITS = 4;
	public static final int STRING_MINIMUM_ALLOWED_LENGTH = 1;

	protected ExtmpToolUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Computes the max length which a field value (for the given field) can have as follows:
	 * <ol>
	 * <li>If max length is already defined, this max length will be returned.</li>
	 * <li>Otherwise,
	 * <ul>
	 * <li>If the field is of type string and the property <code>noValueValidation</code> is set to
	 * <code>true</code>, {@link ExtmpToolUtils#DEFAULT_MAX_VALUE_LENGTH} will be returned.</li>
	 * <li>If the model is host based or is an eXML model, to which an uXML model was assigned,
	 * {@link ExtmpToolUtils#DEFAULT_MAX_VALUE_LENGTH_4_HOST_BASED_MODELS}
	 * will be returned. <b>Note:</b> For fields in Vorsatz, a specific length will be
	 * returned.</li>
	 * <li>Otherwise {@link ExtmpToolUtils#DEFAULT_MAX_VALUE_LENGTH}.</li>
	 * </ul>
	 * </li>
	 * </ol>
	 *
	 * @param origMaxLength
	 * 		already defined max length, if present.
	 * @param isHostBased
	 * 		<code>true</code> if the model is host based, otherwise
	 * 		<code>false</code>.
	 * @param hasAssignedUXMLModel
	 * 		<code>true</code> if the model is an eXML model to which an uXML
	 * 		model was
	 * 		assigned, otherwise <code>false</code>.
	 * @param fieldFullName
	 * 		field full name. May not be neither <code>null</code> nor empty.
	 * @param isNoValueValidation
	 * 		<code>true</code> if the field is of type string and the property
	 * 		<code>noValueValidation</code> was set to <code>true</code>,
	 * 		otherwise
	 * 		<code>false</code>.
	 * @return see method description.
	 */
	public static int computeMaxLength(int origMaxLength, boolean isHostBased, boolean hasAssignedUXMLModel, String fieldFullName,
			boolean isNoValueValidation) {
		if (origMaxLength > 0) {
			return origMaxLength;
		}
		if (isNoValueValidation) {
			return ExtmpToolUtils.DEFAULT_MAX_VALUE_LENGTH;
		}
		if (isHostBased || hasAssignedUXMLModel) {
			return ExtmpToolUtils.DEFAULT_MAX_VALUE_LENGTH_4_HOST_BASED_MODELS;
		}
		return DEFAULT_MAX_VALUE_LENGTH;
	}

	/**
	 * Like {@link ExtmpToolUtils#computeMaxLength(int, boolean, boolean, String, boolean)} but in this
	 * case the property 'noValueValidation' is set to <code>false</code>.
	 */
	public static int computeMaxLength(int origMaxLength, boolean isHostBased, boolean hasAssignedUXMLModel, String fieldFullName) {
		return ExtmpToolUtils.computeMaxLength(origMaxLength, isHostBased, hasAssignedUXMLModel, fieldFullName, false);
	}

	public static String getRegExpForFractionalDigits(int minFractionalDigits, int maxFractionalDigits, String decimalSeparator) {
		if (minFractionalDigits != 0 || maxFractionalDigits > 0) {
			//Nachkomma-Teil: Enthält den Punkt, denn wenn wir 0 Nachkommastellen haben, darf auch kein Punkt da sein
			// Daher hier wird auch auf mindestens eine Nachkommastelle geprüft
			String regExNachkmmaMinLength = minFractionalDigits > 0 ?
					String.valueOf(minFractionalDigits) :
					String.valueOf(MIN_FRACTIONAL_DIGITS);

			String regExNachkmmaMaxLength = maxFractionalDigits > 0 ?
					String.valueOf(maxFractionalDigits) :
					String.valueOf(MAX_FRACTIONAL_DIGITS);
			if (minFractionalDigits > 0) {
				regExNachkmmaMaxLength = maxFractionalDigits > 0 ? String.valueOf(maxFractionalDigits) : regExNachkmmaMinLength;
			}

			String dTrenner = String.valueOf(decimalSeparator);
			if (decimalSeparator.equals(".")) {
				dTrenner = "\\.";
			}

			//Wenn 0 Nachkommastellen erlaubt sind => Nachkommateil optional
			String optionalChar = (minFractionalDigits <= 0) ? "?" : "";
			return String.format("(%s\\d{%s,%s})%s", dTrenner, regExNachkmmaMinLength, regExNachkmmaMaxLength, optionalChar);
		}

		return "";
	}

	public static String getRegExpForAmountOfBeforeDecimalPlaceDigits(int beforeDecimalPlaceDigits) {
		int regExVorkmmaMaxLength = (beforeDecimalPlaceDigits > 0) ? beforeDecimalPlaceDigits : MAX_DIGITS_ALLOWED;
		return String.format("\\d{1,%s}", regExVorkmmaMaxLength);
	}

	/**
	 * Gets the minimum length depending on the field "template".
	 * <p>
	 * If the current minLength is {@literal <= 0}, the minimum length
	 * is calculated out of the given regExp within the field "template".
	 *
	 * @return a minimum length that is at least {@value #STRING_MINIMUM_ALLOWED_LENGTH}
	 */
	public static int evalMinLengthBasedOn(int minLength, String template) {
		if (minLength > 0) {
			return minLength;
		}

		if (template.length() > 0) {
			return new RegExpLengthTester(template).calculateMinLength();
		}

		return STRING_MINIMUM_ALLOWED_LENGTH;
	}

	/**
	 * Gets the maximum length depending on the field "template".
	 * <p>
	 * If the current maxLength is {@literal <= 0}, the maximum length
	 * is calculated out of the given regExp within the field "template".
	 *
	 * @return the maximum length
	 * 		(note: results {@literal <= 0} mean, that there is no maximum length)
	 */
	public static int evalMaxLengthBasedOn(int maxLength, String template, boolean isFeldkennungsbasiert,
			boolean hasAssignedUxmlDa, String fieldFullName, boolean noValueValidation) {
		if (maxLength > 0) {
			return maxLength;
		}

		if (template.length() > 0) {
			return new RegExpLengthTester(template).calculateMaxLength();
		}

		return computeMaxLength(maxLength, isFeldkennungsbasiert, hasAssignedUxmlDa, fieldFullName, noValueValidation);
	}
}
