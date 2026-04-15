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

import static java.util.Locale.GERMANY;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaStringHintLists;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringFormatUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

public class CodeGenExtensibleTypeImpl extends CodeGenStringTypeImpl {

	private final ProgrammingLanguage lang;

	public CodeGenExtensibleTypeImpl(IFieldType type, CodeGenEntityData data, String fieldFullName) {
		super(type, data, fieldFullName);
		this.lang = data.getParams().getProgrammingLanguage();
	}

	@Override
	public String getContentVariables() {
		return toCode("content");
	}

	public String getMaxLaengeCode() {
		return Integer.toString(maxLaenge);
	}

	public boolean isWithValues() {
		return values != null && !values.isEmpty();
	}

	/**
	 * Used for C++ and VBA.
	 * For other target languages the multilingual values are generated, see {@link MetaStringHintLists}.
	 */
	public String getWerteCode() {
		return StringFormatUtil.makeCodeString(values.get(GERMANY));
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		result = prime * result + getMaxLaengeCode().hashCode();
		return result;
	}

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

		CodeGenExtensibleTypeImpl o = (CodeGenExtensibleTypeImpl) other;

		if (values.equals(o.values) &&
				getMaxLaengeCode().equals(o.getMaxLaengeCode())) {
			return true;
		}

		return false;
	}

	public String getClassnameSuffix() {
		if (lang == ProgrammingLanguage.CPLUS) {
			return "nichtabgeschlosseneenumeration";
		} else {
			return "String";
		}
	}
}
