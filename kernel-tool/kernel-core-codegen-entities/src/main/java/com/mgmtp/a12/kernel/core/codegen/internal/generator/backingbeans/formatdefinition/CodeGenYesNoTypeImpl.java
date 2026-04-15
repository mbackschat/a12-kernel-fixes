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

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IYesNoType;

/**
 * Definiert ein FeldJaNein Feld
 */
public class CodeGenYesNoTypeImpl extends CodeGenFeldTypBasis {

	private final String jaWert;
	private final String neinWert;

	public CodeGenYesNoTypeImpl(IFieldType type) {
		IYesNoType yesNoType = (IYesNoType) type;
		jaWert = yesNoType.getTrueValue();
		neinWert = yesNoType.getFalseValue();
	}

	public String getJaWert() {
		return jaWert;
	}

	public String getNeinWert() {
		return neinWert;
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

		CodeGenYesNoTypeImpl o = (CodeGenYesNoTypeImpl) other;

		return (o.jaWert.equals(jaWert) && o.neinWert.equals(neinWert));
	}

	public int hashCode() {
		String s = jaWert + neinWert;
		return s.hashCode();
	}

	public String getClassnameSuffix() {
		return "JaNein";
	}

}
