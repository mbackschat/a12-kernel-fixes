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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.FormatUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateType.PartlyKnownDateType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Definiert einen Datum
 */
public class CodeGenDateTypeImpl extends CodeGenFeldTypBasis {

	private String format;

	private PartlyKnownDateType teilbekanntArt;

	private Locale aktuelleSprache;
	private ProgrammingLanguage lang;

	private static Pattern datumsFormatPattern = Pattern.compile("(dd|MM|yyyy)([^dMy]?)(dd|MM|yyyy)([^dMy]?)(dd|MM|yyyy)");
	private static Pattern dateTimeFormatPattern = Pattern.compile("(dd\\.MM\\.yyyy |MM/dd/yyyy'T')?HH:mm:ss");

	public CodeGenDateTypeImpl(IFieldType type, CodeGenEntityData data) {
		this.lang = data.getParams().getProgrammingLanguage();
		if (!(type instanceof IDateType)) {
			throw new IllegalArgumentException("Type needs to be IDateType, but was " + type.getClass().getName());
		}
		IDateType dateType = (IDateType) type;
		this.format = dateType.getFormat();
		this.teilbekanntArt = dateType.partlyKnownDate();
		if (teilbekanntArt != PartlyKnownDateType.BEKANNT && !teilbekanntesDatumErlaubt(format)) {
			throw new IllegalArgumentException("Teilbekanntes Datum nicht erlaubt für Format: " + format);
		}
	}

	/**
	 * Prüft ob das übergebene Datumsformat die Option "Teilbekanntes Datum" zulässt. Dafür müssen
	 * alle Datumsteile TT, MM und JJJJ enthalten sein
	 *
	 * @return true falls ein teilbekanntes Datum erlaubt ist, false sonst.
	 */
	public static boolean teilbekanntesDatumErlaubt(String format) {
		String tmpFormat = format.toUpperCase().replace("D", "T").replace("Y", "J");
		return tmpFormat.contains("TT") && tmpFormat.contains("MM") && tmpFormat.contains("JJJJ");
	}

	public String getTeilbekanntArt() {
		return teilbekanntArt.toString();
	}

	public String getFormat() {
		return transformFormat(format);
	}

	public static String transformFormat(String s) {
		return s.replace("jj", "yy").replace("JJ", "yy").replace("tt", "dd").replace("TT", "dd");
	}

	/**
	 * Extrahiert die Datumsteile aus dem sprachspezifischen Datumsformat für JavaScript, die im
	 * Datumsformat des Feldtyps enthalten sind.
	 *
	 * @param aFormat
	 * 		Datumsformat des Feldtyps
	 * @param formatTemplate
	 * 		Sprachspezifisches Oberflächenformat für Javascript (s. Spalte js_datum_format der
	 * 		Tabelle "Sprache" in der DB; Bsp. deutsch: "dd.MM.yyyy"){@literal ->}
	 * 		definiert die Reihenfolge der Datumsfragmente und den Datumstrenner
	 */
	public static String getSprachspezifischesFormat(String aFormat, String formatTemplate) {
		aFormat = transformFormat(aFormat);
		Matcher m = dateTimeFormatPattern.matcher(formatTemplate);
		if (m.matches()) {
			return formatTemplate;
		}
		m = datumsFormatPattern.matcher(formatTemplate);
		if (!m.matches()) {
			throw new IllegalArgumentException("Unbekanntes Datumsformat.");
		}
		assert m.group(2).equals(m.group(4)); // Datumtrenner muss eindeutig sein
		String datumTrenner = m.group(2);
		List<String> datumsTeile = new ArrayList<>();
		for (String datumsTeil : new String[] { m.group(1), m.group(3), m.group(5) }) {
			if (aFormat.contains(datumsTeil)) {
				datumsTeile.add(datumsTeil);
			}
		}
		return StringUtils.join(datumsTeile, datumTrenner);
	}

	/*
	 * Das Datumsformat wird zurzeit nur für Englisch und Deutsch passend zur Sprache geliefert.
	 * Bei dem Ausbau für eine weitere Sprache muss geprüft werden, ob dies ausreichend ist.
	 */
	public String getFormatJS() {
		String result = getSprachspezifischesFormat(format, JavaScriptLanguageHelper.getJsDatumFormat(aktuelleSprache, format));

		return convertPruefFormat2FehlermeldungFormat(result, aktuelleSprache);
	}

	// ATTENTION: Should conform to FormatDefinitionDatum#convertPruefFormat2FehlermeldungFormat
	private String convertPruefFormat2FehlermeldungFormat(String s, Locale sprache) {
		s = s.replace("dd", "d").replace("d", "DD")
				.replace("MM", "M").replace("M", "MM")
				.replace("y", "Y");
		// Locale.GERMANY: Conversion for display of german dates
		if (Locale.GERMANY.equals(sprache)) {
			s = s.replace("D", "T").replace("Y", "J");
		}
		return s;
	}

	public String getFormatPatternJS() {
		return getFormatPatternJSCode(format, teilbekanntArt, aktuelleSprache, lang);
	}

	public static String getFormatPatternJSCode(String f, PartlyKnownDateType teilbekanntArt, Locale sprache,
			ProgrammingLanguage lang) {
		String jsFormat = getSprachspezifischesFormat(f, JavaScriptLanguageHelper.getJsDatumFormat(sprache, f));
		String result = FormatUtil.datumFormat2Pattern(jsFormat, teilbekanntArt, lang,
				JavaScriptLanguageHelper.isJsDatumNullOptional(sprache));
		if (jsFormat.equals("dd.MM")) {
			//hänge optionalen Punkt an.
			result = result + "(\\.)?";
		}
		return result;
	}

	/**
	 * berechnet einen Hashcode zur Verwendung in Hash Containern Implementierung von hashCode und
	 * equals (Algorithmus analog Joshua Bloch Effective Java)
	 *
	 * @return Hashcode des Typs
	 */
	public int hashCode() {
		int code = hashCodeHelperAdd(37 * transformFormat(format).hashCode(), teilbekanntArt.ordinal());
		return hashCodeHelperAdd(code, 0);
	}

	/**
	 * evaluiert, ob zwei Feldtypen gleich sind.
	 *
	 * @return wahr, falls beide Feldtypen gleich sind, false sonst.
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

		CodeGenDateTypeImpl o = (CodeGenDateTypeImpl) other;

		return (transformFormat(format).equals(transformFormat(o.format))
				&& teilbekanntArt.equals(o.teilbekanntArt));
	}

	public void setSprache(Locale aktuelleSprache) {
		this.aktuelleSprache = aktuelleSprache;
	}

	public String getClassnameSuffix() {
		return "Datum";
	}
}
