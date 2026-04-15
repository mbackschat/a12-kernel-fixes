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

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateRangeType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.INumberType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IPredefinedType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IYesNoType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IYesType;

public class CodeGenFeldTypBuilder {

	public CodeGenFeldTypBasis createObject(
			IFieldType type,
			CodeGenEntityData data,
			String fieldFullName) {
		if (type instanceof IDateRangeType) {
			return new CodeGenDateRangeTypeImpl(type, data);
		}
		if (type instanceof IDateType) {
			return new CodeGenDateTypeImpl(type, data);
		}
		if (type instanceof IEnumerationType) {
			return new CodeGenEnumerationTypeImpl(type, data);
		}
		if (type instanceof IStringType && ((IStringType) type).isExtensibleEnum()) {
			return new CodeGenExtensibleTypeImpl(type, data, fieldFullName);
		}
		if (type instanceof INumberType) {
			return new CodeGenNumberTypeImpl(type);
		}
		if (type instanceof IPredefinedType) {
			return new CodeGenPredefinedTypeImpl(type, data);
		}
		if (type instanceof IStringType) {
			return new CodeGenStringTypeImpl(type, data, fieldFullName);
		}
		if (type instanceof IYesNoType) {
			return new CodeGenYesNoTypeImpl(type);
		}
		if (type instanceof IYesType) {
			return new CodeGenYesTypeImpl(type);
		}

		throw new IllegalArgumentException(
				String.format("<%s>: The FieldType '%s' is NOT instanceof an expected sub-interface.", fieldFullName,
						type.getClass().getName()));
	}
}
