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

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenDateRangeTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenDateTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenEnumerationTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenExtensibleTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenFeldTypBasis;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenNumberTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenPredefinedTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenStringTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenYesNoTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenYesTypeImpl;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 *
 */
public class MetaUtilService {

	private static final String STRING = "String";
	private static final String DATE = "date";
	private static final String DECIMAL = "decimal";
	private static final String INT = "int";

	private MetaUtilService() {
		throw new IllegalStateException("Utility class");
	}

	public static String getERFormat(CodeGenFeldTypBasis ft) {
		if (ft instanceof CodeGenDateRangeTypeImpl) {
			return "ER_DATUMBEREICH";
		}
		if (ft instanceof CodeGenDateTypeImpl) {
			return "ER_DATUM";
		}
		if (ft instanceof CodeGenEnumerationTypeImpl) {
			CodeGenEnumerationTypeImpl fte = (CodeGenEnumerationTypeImpl) ft;
			if (fte.getLang() == ProgrammingLanguage.CPLUS && fte.isTableReference()) {
				return "ER_ENUMERATION_TABLE";
			} else {
				return "ER_ENUMERATION";
			}
		}
		if (ft instanceof CodeGenNumberTypeImpl) {
			return "ER_ZAHL";
		}
		if (ft instanceof CodeGenYesTypeImpl) {
			return "ER_FELDJA";
		}
		if (ft instanceof CodeGenYesNoTypeImpl) {
			return "ER_FELDJANEIN";
		}
		if (ft instanceof CodeGenExtensibleTypeImpl) {
			return "ER_ENUMERATIONN_NICHT_ABGESCHLOSSEN";
		}
		if (ft instanceof CodeGenStringTypeImpl) {
			return "ER_STRING";
		}
		if (ft instanceof CodeGenPredefinedTypeImpl) {
			return "ER_VORDEFINIERT";
		}

		throw new IllegalArgumentException("Feldtyp " + ft.getClass().getSimpleName() + " wird nicht behandelt. ");
	}

	//XFORMS
	public static String getRestrictionBase(CodeGenFeldTypBasis ft) {
		if (ft instanceof CodeGenDateRangeTypeImpl) {
			return STRING;
		}
		if (ft instanceof CodeGenDateTypeImpl) {
			return DATE;
		}
		if (ft instanceof CodeGenEnumerationTypeImpl) {
			return STRING;
		}
		if (ft instanceof CodeGenNumberTypeImpl) {
			CodeGenNumberTypeImpl ftz = (CodeGenNumberTypeImpl) ft;
			if (ftz.getNachkomma() > 0 || ftz.getNachkommaBis() > 0) {
				return DECIMAL;
			} else {
				return INT;
			}
		}
		if (ft instanceof CodeGenYesTypeImpl) {
			return STRING;
		}
		if (ft instanceof CodeGenYesNoTypeImpl) {
			return STRING;
		}
		if (ft instanceof CodeGenStringTypeImpl) {
			return STRING;
		}
		if (ft instanceof CodeGenPredefinedTypeImpl) {
			return STRING;
		}

		throw new IllegalArgumentException("Feldtyp " + ft.getClass().getSimpleName() + " wird nicht behandelt. ");

	}

}
