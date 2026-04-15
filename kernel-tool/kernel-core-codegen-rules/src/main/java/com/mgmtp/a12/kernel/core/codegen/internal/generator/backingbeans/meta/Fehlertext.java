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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta;

import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.LanguageHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * Parameter class for the string template of an error message.
 * <p>
 * The changes of the error text like the replacement of field names by unique names are performed
 * during creation in {@link FehlertextBuilder}.
 */
@StringTemplateGroup("meta_entity")
public class Fehlertext extends STParameterClassBase {

	private final Locale locale;
	private final String origErrorMsg;
	private final String replacedErrorMsg;
	private final Map<String, String> parameterMapping;
	private final long ruleNumber;

	Fehlertext(FehlertextBuilder builder) {
		this.locale = builder.getLocale();
		this.origErrorMsg = builder.getOrigErrorMsg();
		this.replacedErrorMsg = builder.getReplacedErrorMsg();
		this.parameterMapping = builder.getParameterMapping();
		this.ruleNumber = builder.getRuleNumber();
	}

	public String getOriginalFehlerTextCode() {
		return new StringParameter(origErrorMsg).toCode();
	}

	public String getFehlerTextCode() {
		return new StringParameter(replacedErrorMsg).toCode();
	}

	public Locale getLocale() {
		return locale;
	}

	public String getLocaleCode() {
		return new LanguageHelper(locale).toCode();
	}

	Map<String, String> getParameterMapping() {
		return parameterMapping;
	}

	public String getNummer() {
		return Long.toString(ruleNumber);
	}
}
