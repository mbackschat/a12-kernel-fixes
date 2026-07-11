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
package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition;

import java.util.Locale;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateRangeType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateType.PartlyKnownDateType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.utils.conversion.InterpretationOfYear;

import lombok.Getter;
import lombok.Setter;

/**
 * Definiert einen DatumsBereich
 */
public class CodeGenDateRangeTypeImpl extends CodeGenFeldTypBasis {

	private String dateRangeformat;
	private String dateFormat;
	private String separator;
	private String interpretationOfYear;
	private String jahresinterpretation;

	//wird auf TRUE gesetzt, wenn  auf Datum > 1900 geprüft werden soll
	@Setter @Getter private boolean zusatzPruefung;

	private Locale aktuelleSprache;
	private ProgrammingLanguage lang;

	public CodeGenDateRangeTypeImpl(IFieldType dbObject, CodeGenEntityData data) {

		IDateRangeType dateRangeType = (IDateRangeType) dbObject;
		this.dateRangeformat = dateRangeType.getFormatOfDateRange();
		this.dateFormat = CodeGenDateTypeImpl.transformFormat(dateRangeType.getFormatOfDate());
		this.separator = dateRangeType.getSeparator();

		this.lang = data.getParams().getProgrammingLanguage();

		switch (dateRangeType.getInterpretationOfYear()) {
			case STANDARD:
				interpretationOfYear = InterpretationOfYear.STANDARD.name();
				jahresinterpretation = "Standard";
				break;
			case PREVIOUS:
				interpretationOfYear = InterpretationOfYear.TO.name();
				jahresinterpretation = "Vorjahr";
				break;
			case SUBSEQUENT:
				interpretationOfYear = InterpretationOfYear.FROM.name();
				jahresinterpretation = "Folgejahr";
				break;
		}

		this.zusatzPruefung = dateRangeType.isAdditionalCheck();
	}

	public String getFormat() {
		return CodeGenDateTypeImpl.transformFormat(dateRangeformat);
	}

	private String appendDatesJS(String f) {
		StringBuilder buf = new StringBuilder(f);
		buf.append(JavaScriptLanguageHelper.getJsDatumbereichTrenner(aktuelleSprache));
		buf.append(f);
		return buf.toString();
	}

	public String getFormatJS() {
		String f = CodeGenDateTypeImpl.getSprachspezifischesFormat(dateFormat,
				JavaScriptLanguageHelper.getJsDatumFormat(aktuelleSprache, dateFormat));
		// Locale.GERMANY used for JAVA_SCRIPT_REDUCED only
		if (Locale.GERMANY.equals(aktuelleSprache)) {
			f = f.replace("dd", "TT").replace("yyyy", "JJJJ");
		}
		f = f.toUpperCase();
		return appendDatesJS(f);
	}

	public String getFormatPatternJS() {
		String f = CodeGenDateTypeImpl.getFormatPatternJSCode(dateFormat, PartlyKnownDateType.BEKANNT, aktuelleSprache, lang);
		return appendDatesJS(f);
	}

	public String getTrenner() {
		return separator;
	}

	public String getFormat1() {
		return dateFormat;
	}

	public String getFormat2() {
		return dateFormat;
	}

	/**
	 * berechnet einen Hashcode zur Verwendung in Hash Containern Implementierung von hashCode und
	 * equals (Algorithmus analog Joshua Bloch Effective Java)
	 *
	 * @return Hashcode des Typs
	 */
	public int hashCode() {
		return hashCodeHelperAdd(37 * CodeGenDateTypeImpl.transformFormat(dateRangeformat).hashCode(), zusatzPruefung ? 1 : 0);
	}

	/**
	 * Evaluiert, ob zwei Feldschlüssel gleich sind.
	 *
	 * @return wahr, falls beide Felschlüssel gleich sind, false sonst.
	 */
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (other.getClass() != getClass()) {
			return false;
		}

		CodeGenDateRangeTypeImpl o = (CodeGenDateRangeTypeImpl) other;

		return (CodeGenDateTypeImpl.transformFormat(dateRangeformat)
				.equals(CodeGenDateTypeImpl.transformFormat(o.dateRangeformat)) && zusatzPruefung == o.zusatzPruefung);
	}

	public void setSprache(Locale aktuelleSprache) {
		this.aktuelleSprache = aktuelleSprache;
	}

	/**
	 * Code für die Jahresinterpretation. Benötigt für die Formate
	 * "TT.MM-TT.MM (bis/von ist Referenzjahr)".
	 */
	public String getJahresinterpretationCode() {
		return jahresinterpretation;
	}

	public String getInterpretationOfYearCode() {
		return interpretationOfYear;
	}

	public String getClassnameSuffix() {
		return "DatumBereich";
	}
}
