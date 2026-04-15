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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.utils.ExtmpToolUtils;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Klasse wird verwendet, um Code für Stringtypen zu generieren.
 */
public class CodeGenStringTypeImpl extends CodeGenFeldTypBasis {

	private final String muster;

	private final int minLaenge;
	protected final int maxLaenge;
	private final String minLaengePrintable;
	private final String maxLaengePrintable;
	private final boolean lineBreakAllowed;
	private final boolean noValueValidation;
	private final Map<Locale, String> spracheToFehlertext;
	protected final Map<Locale, List<String>> values;

	private String fieldFullName;

	private Locale aktuelleSprache;
	private ProgrammingLanguage lang;

	public CodeGenStringTypeImpl(IFieldType type, CodeGenEntityData data, String fieldFullName) {

		IStringType stringType = (IStringType) type;

		this.fieldFullName = fieldFullName;
		this.lang = data.getParams().getProgrammingLanguage();

		this.minLaenge = stringType.getMinLength();
		boolean isHostbased = ExtmpUtils.isHostbasiert(data.getModelSpecificSettings());
		boolean hasAssignedUXMLModel = ExtmpUtils.hasAssignedUXMLModel(data.getModelSpecificSettings());

		this.maxLaenge = data.getMaxLengthDefinedByRules().containsKey(fieldFullName)
				?
				data.getMaxLengthDefinedByRules().get(fieldFullName)
				:
				ExtmpToolUtils.computeMaxLength(stringType.getMaxLength(), isHostbased, hasAssignedUXMLModel, fieldFullName,
						stringType.isNoValueValidation());

		this.muster = analysePattern(stringType);

		String tmpMuster = stringType.getPattern() == null ? "" : stringType.getPattern();
		this.minLaengePrintable = computeMinLaengePrintable(stringType.getMinLength(), tmpMuster);
		this.maxLaengePrintable = data.getMaxLengthDefinedByRules().containsKey(fieldFullName)
				?
				computeMaxLaengePrintable(maxLaenge, tmpMuster, isHostbased, hasAssignedUXMLModel, stringType.isNoValueValidation())
				:
				computeMaxLaengePrintable(stringType.getMaxLength(), tmpMuster, isHostbased, hasAssignedUXMLModel,
						stringType.isNoValueValidation());

		this.spracheToFehlertext = computeSpracheToFehlertext(data, stringType::getErrorMessage);

		this.lineBreakAllowed = stringType.hasLineBreaksPermitted();
		this.noValueValidation = stringType.isNoValueValidation();

		this.values = computeValues(data, stringType::getHintList);
	}

	private String computeMinLaengePrintable(int minLength, String muster) {
		int calcMinLength = ExtmpToolUtils.evalMinLengthBasedOn(minLength, muster);

		return Integer.toString(calcMinLength > 0 ? calcMinLength : ExtmpToolUtils.STRING_MINIMUM_ALLOWED_LENGTH);
	}

	private String computeMaxLaengePrintable(int maxLength, String muster, boolean isHostbased, boolean hasAssignedUXMLModel,
			boolean isNoValueValidation) {
		int calcMaxLength = ExtmpToolUtils.evalMaxLengthBasedOn(maxLength, muster, isHostbased, hasAssignedUXMLModel, fieldFullName,
				isNoValueValidation);

		if (calcMaxLength > 0) {
			return Integer.toString(calcMaxLength);
		}

		return Integer.toString((isHostbased || hasAssignedUXMLModel) ?
				ExtmpToolUtils.DEFAULT_MAX_VALUE_LENGTH_4_HOST_BASED_MODELS :
				ExtmpToolUtils.DEFAULT_MAX_VALUE_LENGTH);
	}

	private String analysePattern(IStringType stringType) {
		String tmpMuster = stringType.getPattern();
		if (tmpMuster == null) {
			return "";
		}

		if (tmpMuster.length() > 0) {
			if (tmpMuster.charAt(0) == ' ') {
				throw new IllegalArgumentException("Muster  '" + tmpMuster + "' beginnt mit einem Leerzeichen");
			}
			if (tmpMuster.charAt(tmpMuster.length() - 1) == ' ') {
				throw new IllegalArgumentException("Muster  '" + tmpMuster + "' endet mit einem Leerzeichen");
			}
		}

		return switch (lang) {
			// Bei der Zielsprache Java-Skript muss der Slash escaped werden, da die Expression von Slashes umgeben ist.
			case JAVA_SCRIPT_REDUCED -> tmpMuster.replace("/", "\\/");
			// Bei VBA muss das Anführungszeichen durch ein zusätzliches Anführunszeichen escaped werden.
			case VBA -> tmpMuster.replace("\"", "\"\"");
			case GROOVY -> tmpMuster.replace("\\", "\\\\").replace("$", "\\$");
			// Bei c++ und Java wird der String als String übergeben, daher BackSlash nochmal escapen
			case JAVA, CPLUS, JAVA_SCRIPT_VK -> tmpMuster.replace("\\", "\\\\");
		};
	}

	public int getMinLaenge() {
		return minLaenge;
	}

	public String getMaxLaengePrintable() {
		return maxLaengePrintable;
	}

	public String getMinLaengePrintable() {
		return minLaengePrintable;
	}

	public boolean hasFehlertext() {
		return !spracheToFehlertext.isEmpty();
	}

	public Map<Locale, String> getFehlertexte() {
		return spracheToFehlertext;
	}

	/**
	 * Die Methode wird für die C++ Codegenerierung verwendet. Da es dort (noch) keine
	 * Mehrsprachigkeit gibt, wird hier der deutsche Fehlertext zurückgegeben.
	 */
	public String getFehlerTextCode() {
		// Locale.GERMANY used for C++ only
		String fehlertext = spracheToFehlertext.get(Locale.GERMANY);

		if (fehlertext == null) {
			return new StringParameter("").toCode();
		}

		return new StringParameter(fehlertext).toCode();
	}

	public String getFehlerTextJsReducedCode() {
		return getFehlerTextForJsReducedCode(spracheToFehlertext, aktuelleSprache, this::replaceFieldName);
	}

	public String getMuster() {
		return muster.replace("\"", "\\\"");
	}

	/**
	 * In VBScript gibt es keine "CONTAINS_ONLY_MUSTER" Funktion!
	 *
	 * @return spezielles Muster mit Anfang und Ende
	 */
	public String getMusterVBA() {
		if (muster.length() == 0) {
			return "";
		}
		return "^(?:" + muster + ")$";
	}

	public int getMaxLaenge() {
		return maxLaenge;
	}

	public boolean isLineBreakAllowed() {
		return lineBreakAllowed;
	}

	public boolean isNoValueValidation() {
		return noValueValidation;
	}

	public Map<Locale, List<String>> getValues() {
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((spracheToFehlertext == null) ? 0 : spracheToFehlertext.hashCode());
		result = prime * result + (lineBreakAllowed ? 1231 : 1237);
		result = prime * result + (noValueValidation ? 1231 : 1237);
		result = prime * result + maxLaenge;
		result = prime * result + ((maxLaengePrintable == null) ? 0 : maxLaengePrintable.hashCode());
		result = prime * result + minLaenge;
		result = prime * result + ((minLaengePrintable == null) ? 0 : minLaengePrintable.hashCode());
		result = prime * result + ((muster == null) ? 0 : muster.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
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
		CodeGenStringTypeImpl o = (CodeGenStringTypeImpl) other;
		return spracheToFehlertext.equals(o.spracheToFehlertext) &&
				lineBreakAllowed == o.lineBreakAllowed &&
				noValueValidation == o.noValueValidation &&
				minLaenge == o.minLaenge &&
				minLaengePrintable.equals(o.minLaengePrintable) &&
				maxLaenge == o.maxLaenge &&
				maxLaengePrintable.equals(o.maxLaengePrintable) &&
				muster.equals(o.muster) &&
				values.equals(o.values);
	}

	public void setSprache(Locale locale) {
		this.aktuelleSprache = locale;
	}

	public String getClassnameSuffix() {
		return "String";
	}

	private String replaceFieldName(String fehlertext) {
		String fieldName = EntityUtils.getShortName(fieldFullName);
		String replacement = "$" + fieldName + "$";
		return fehlertext.replace("$Feld$", replacement);
	}

	private Map<Locale, List<String>> computeValues(CodeGenEntityData data, Function<Locale, List<String>> getHintList) {
		Map<Locale, List<String>> localeToHintListTmp = new HashMap<>();
		for (Locale l : data.getParams().getLanguages()) {
			List<String> hintList = getHintList.apply(l);
			if (hintList != null && !hintList.isEmpty()) {
				localeToHintListTmp.put(l, hintList);
			}
		}
		return Collections.unmodifiableMap(localeToHintListTmp);
	}
}