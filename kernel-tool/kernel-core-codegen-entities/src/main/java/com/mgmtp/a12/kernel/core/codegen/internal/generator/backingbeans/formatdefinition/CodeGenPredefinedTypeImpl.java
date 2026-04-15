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
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IPredefinedType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IPredefinedType.ValidationStrategy;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * CodeGenFeldTypVordefiniert ...
 *
 * @version $Id: CodeGenFeldTypVordefiniert.java 17755 2012-08-10 10:39:49Z lgargouri $
 */
public class CodeGenPredefinedTypeImpl extends CodeGenFeldTypBasis {

	private String name = "";
	private int maxLaenge;
	private int minLaenge;
	private String regexp;
	private final Map<Locale, String> spracheToFehlertext;
	private ValidationStrategy validationStrategy;
	private IPredefinedType predefinedType;
	private Locale aktuelleSprache;

	public CodeGenPredefinedTypeImpl(IFieldType type, CodeGenEntityData data) {
		this.predefinedType = (IPredefinedType) type;
		Optional<String> regexPattern = predefinedType.getPattern();

		this.minLaenge = predefinedType.getMinLength();
		this.maxLaenge = predefinedType.getMaxLength();
		this.name = predefinedType.getName();
		this.regexp = regexPattern.isPresent() ? regexPattern.get() : "";

		ProgrammingLanguage lang = data.getParams().getProgrammingLanguage();

		if (lang == ProgrammingLanguage.VBA) {
			this.spracheToFehlertext = computeSpracheToFehlertext(data, this::getErrorMessageForVBA);
		} else {
			this.spracheToFehlertext = computeSpracheToFehlertext(data, predefinedType::getErrorMessage);
		}

		this.validationStrategy = predefinedType.getValidationConfig(lang).orElse(ValidationStrategy.NONE);

		if (lang == ProgrammingLanguage.VBA && validationStrategy != ValidationStrategy.LENGTH_AND_PATTERN_CHECK) {
			// If neither the length nor the pattern are checked, the following default values are used
			// This effectively disables formal validation
			this.minLaenge = 0; // minimum length 0 is always fulfilled
			this.maxLaenge = 999; // default maximum length as in #getMaxLaenge
			this.regexp = ".*"; // any character sequence is allowed
		}
	}

	public String getName() {
		return new StringParameter(name).toCode();
	}

	public int getMaxLaenge() {
		return (maxLaenge == -1) ? 999 : maxLaenge;
	}

	public int getMinLaenge() {
		return minLaenge;
	}

	public boolean hasFehlertext() {
		return !spracheToFehlertext.isEmpty();
	}

	public Map<Locale, String> getFehlertexte() {
		return spracheToFehlertext;
	}

	public String getRegexpForVBA() {
		String prefix = "^(?:";
		String suffix = ")$";
		String regexForVBA = String.format("%s%s%s", prefix, regexp, suffix);
		return new StringParameter(regexForVBA).toCode();
	}

	public String getErrorIdForVBA() {
		String errorId = "invalid" + name;
		return new StringParameter(errorId).toCode();
	}

	public String getFehlerTextJsReducedCode() {
		return getFehlerTextForJsReducedCode(spracheToFehlertext, aktuelleSprache, UnaryOperator.identity());
	}

	public String getRegexpJsReduced() {
		String regex = String.format("^%s$", this.regexp);
		return new StringParameter(regex).toCode();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + maxLaenge;
		result = prime * result + minLaenge;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * evaluiert, ob zwei Feldschlüssel gleich sind.
	 *
	 * @return wahr, falls beide Feldschlüssel gleich sind, false sonst.
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

		CodeGenPredefinedTypeImpl o = (CodeGenPredefinedTypeImpl) other;

		return (name.equals(o.name) && minLaenge == o.minLaenge && maxLaenge == o.maxLaenge);
	}

	public String getClassnameSuffix() {
		return "Vordefiniert";
	}

	private String getErrorMessageForVBA(Locale locale) {
		String message = predefinedType.getErrorMessage(locale);
		String commonPrefix = "'$<fieldName>$': ";

		if (locale.toString().startsWith("de")) {
			message = String.format("Feld %s%s", commonPrefix, message);
		} else if (locale.toString().startsWith("en")) {
			message = String.format("Field %s%s", commonPrefix, message);
		}
		return message;
	}

	public void setSprache(Locale locale) {
		this.aktuelleSprache = locale;
	}

}
