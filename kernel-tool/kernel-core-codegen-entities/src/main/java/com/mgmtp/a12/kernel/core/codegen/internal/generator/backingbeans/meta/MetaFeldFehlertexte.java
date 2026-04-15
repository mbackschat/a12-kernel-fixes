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
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.LanguageHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;

/**
 * Parameter class for the templates used to create error messages for the <code>Meta_Felder_</code>
 * classes.
 */
@StringTemplateGroup("meta_entity")
public class MetaFeldFehlertexte extends STParameterClassBase {

	private Map<Locale, String> fehlertexte;
	private String nummer;
	private String param;

	public MetaFeldFehlertexte(String nummer, Map<Locale, String> fehlertexte) {
		this(nummer, fehlertexte, "");
	}

	public MetaFeldFehlertexte(String nummer, Map<Locale, String> fehlertexte, String param) {
		this.fehlertexte = fehlertexte;
		this.nummer = nummer;
		this.param = param;
	}

	public String getFehlertexteCode() {
		if (fehlertexte == null) {
			return "";
		}
		StringBuilder fehlertexteCode = new StringBuilder();
		for (Entry<Locale, String> entry : fehlertexte.entrySet()) {
			Locale sprache = entry.getKey();
			String text = entry.getValue();
			if (StringUtils.isEmpty(text)) {
				// We do not want to have empty error messages generated
				continue;
			}
			Fehlertext fehlertext = new Fehlertext(sprache, text);
			fehlertexteCode.append(fehlertext.toCode(param));
		}
		return fehlertexteCode.toString();
	}

	public String getNummer() {
		return nummer;
	}

	@StringTemplateGroup("meta_entity")
	public class Fehlertext extends STParameterClassBase {

		private Locale sprache;
		private String text;

		public Fehlertext(Locale sprache, String fehlertext) {
			this.sprache = sprache;
			this.text = fehlertext;
		}

		public String getNummer() {
			return nummer;
		}

		public String getLocaleCode() {
			return new LanguageHelper(sprache).toCode();
		}

		public String getFehlerTextCode() {
			return new StringParameter(text).toCode();
		}

		public String getOriginalFehlerTextCode() {
			return new StringParameter(text).toCode();
		}
	}
}
