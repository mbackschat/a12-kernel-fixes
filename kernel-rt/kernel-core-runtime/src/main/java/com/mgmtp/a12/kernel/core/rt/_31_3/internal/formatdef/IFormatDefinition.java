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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef;

import java.util.Locale;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;

import lombok.NonNull;

/**
 * Schnittstelle für alle Formatdefinitionen zur Laufzeit
 */
public interface IFormatDefinition {

	public static final String INITCODE_TRENNER = ";";

	public enum FeldFormatEnumType {
		ER_ZAHL("Zahl"),
		ER_STRING("String"),
		ER_DATUM("Datum"),
		ER_DATUMBEREICH("DatumBereich"),
		ER_FELDJA("Ja"),
		ER_FELDJANEIN("JaNein"),
		ER_VORDEFINIERT("Vordefiniert"),
		ER_ENUMERATION("Enumeration"),
		ER_ENUMERATIONN_NICHT_ABGESCHLOSSEN("NichtAbgeschlosseneEnumeration");

		private String typ;

		FeldFormatEnumType(String inTyp) {
			this.typ = inTyp;
		}

		@Override
		public String toString() {
			return typ;
		}
	}

	/**
	 * überprüft das Format
	 *
	 * @return Art der Gültigkeit
	 */
	public ValidType validiereFormat(IValidierungParam param, IMetaDataValidierung metaData, boolean fullCheck);

	public String convert(String wert,
			DarstellungsInformationIntern darstellungsInfo,
			boolean vonXml, final RawParamHandler4Convert additionalParameters);

	public String getDefaultValue(IMetaDataValidierung metaData, PresentationInformation presentationInformation, Locale sprache);

	/**
	 * Liefert alle MetaInformationen, die mit dem Feldtypen assoziiert sind.
	 */
	public Object getWert(String key);

	/**
	 * Compares the given string values depending on the field type.
	 *
	 * @param s1
	 * 		- first value
	 * @param s2
	 * 		- second value
	 * @param metaData
	 * 		- the meta data used to extract additional information
	 */
	public int compare(@NonNull String s1, @NonNull String s2, IMetaDataValidierung metaData);

	/**
	 * Convert from internal value to a representation as a basic type. Used for
	 * {@link IMetaField#convertWithOnlyBasicTypeCheck(String, PresentationInformation,
	 * com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult,
	 * Object...)}.
	 * <p>
	 * The method should be called after
	 * {@link #validiereFormat(IValidierungParam, IMetaDataValidierung, boolean)} to ensure a
	 * correct
	 * value. A {@link RuntimeException} may occur, if a wrong value is given to the function.
	 *
	 * @param internalValue
	 * 		the internal value to be converted.
	 * @param metaData
	 * 		the meta data for the generated code. Used for additional parameters like base year.
	 * @return the value as basic type conforming to the type of the specific implementation of
	 *        {@link IFormatDefinition}.
	 */
	public Object convertToBasicType(String internalValue, IMetaDataValidierung metaData);

	/**
	 * Convert from an object representation to the internal string representation.
	 *
	 * @param value
	 * 		the object representation.
	 * @param metaData
	 * 		holds additional information.
	 * @return the internal string representation.
	 *
	 * @throws IllegalArgumentException
	 * 		if the given type is not of the correct type for the field.
	 */
	String convertFromBasicType(@NonNull Object value, @NonNull IMetaDataValidierung metaData);
}
