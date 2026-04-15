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

package com.mgmtp.a12.kernel.core.codegen.internal.utils;

import java.util.Locale;

import org.apache.commons.text.WordUtils;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IDateValidityCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IPredefinedTypeValidityCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ICompositeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperationWithValue;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IGroupOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfUsbOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOpListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IPointInTimeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateAddOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateDiffOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateExtractionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateRangeExtractionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.FieldIdOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OpListOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.services.ParseBaumBedingungsServices;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IDateType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.INumberType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IPredefinedType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;

/**
 *
 */
public class RuntimeMethodNameFactory {

	private static final String STRING_METHOD_SUFFIX = "String";
	private static final String ZAHL_METHOD_SUFFIX = "Zahl";
	private static final String DATUM_METHOD_SUFFIX = "Datum";
	private static final String DATE_RANGE_METHOD_SUFFIX = "DateRange";
	private static final String ANZAHL_USB = "anzahlUSB";
	private static final String ANZ_VORDRUCKE = "anzVordrucke";

	private RuntimeMethodNameFactory() {
		throw new IllegalStateException("Utility class");
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IValueListCondition obj) {
		String suffix = (ParseBaumBedingungsServices.isStringListe(obj)) ? "STRING" : "";
		return String.format("%s%s", WordUtils.uncapitalize(obj.getConditionName().getRuntimeName()), suffix);
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IEntityListAndValueListCondition obj, String suffix) {
		return String.format("%s%s", WordUtils.uncapitalize(obj.getConditionName().getRuntimeName()), suffix);
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IFieldIdOperation obj) {
		String suffix = "";
		if (obj.getOperatorName() == FieldIdOperationName.FieldValue) {
			if (obj.getType() == OperationType.NUMBER) {
				suffix = "AlsZahl";
			}
			if (obj.getType() == OperationType.DATE) {
				suffix = "AlsDatum";
			}
			if (obj.getType() == OperationType.DATE_RANGE) {
				suffix = "AlsDateRange";
			}
		}

		return String.format("%s%s", WordUtils.uncapitalize(obj.getOperatorName().getRuntimeName()), suffix);
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IEntityListOperationWithValue fo) {
		String suffix = "";
		if (fo.getOperatorName() == EntityListOperationName.NumberOfValueInFields) {
			OperationType art = fo.getConstantOperation().getType();
			if (art == OperationType.NUMBER) {
				suffix = ZAHL_METHOD_SUFFIX;
			} else if (art == OperationType.STRING) {
				suffix = STRING_METHOD_SUFFIX;
			}
		}

		return String.format("%s%s", WordUtils.uncapitalize(fo.getOperatorName().getRuntimeName()), suffix);
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IEntityListCondition b) {
		return WordUtils.uncapitalize(b.getConditionName().getRuntimeName());
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IRootGroupCondition b) {
		return WordUtils.uncapitalize(b.getConditionName().toString());
	}

	public static String getRuntimeMethodNameFromParseTreeObject(INumberOfRootGroupsOperation b) {
		return ANZ_VORDRUCKE;
	}

	public static String getRuntimeMethodNameFromParseTreeObject(INumberOfUsbOperation b) {
		return ANZAHL_USB;
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IEntityListOperation op, IEC ec) {
		String suffix = "";
		String opName = WordUtils.uncapitalize(op.getOperatorName().getRuntimeName());

		if (op.getOperatorName() == EntityListOperationName.FirstFilledValue) {
			OperationType art = op.getType();
			if (art == OperationType.NUMBER) {
				suffix = ZAHL_METHOD_SUFFIX;
			} else if (art == OperationType.DATE) {
				suffix = DATUM_METHOD_SUFFIX;
			} else if (art == OperationType.DATE_RANGE) {
				suffix = DATE_RANGE_METHOD_SUFFIX;
			}
		}
		if (op.getOperatorName() == EntityListOperationName.MaxValue || op.getOperatorName() == EntityListOperationName.MinValue) {
			OperationType art = op.getType();
			if (art == OperationType.DATE) {
				suffix = DATUM_METHOD_SUFFIX;
			}
		}
		if (op.getOperatorName() == EntityListOperationName.NumberOfDifferentValues) {
			suffix = getSuffix4AnzahlVerschiedenerWerte(op, ec);
		}
		return opName + suffix;
	}

	private static String getSuffix4AnzahlVerschiedenerWerte(IEntityListOperation op, IEC ec) {
		// for AnzahlVerschiedenerWerte, suffix encodes the *parameter* type, not the result type
		// the parser has already checked that all fields have compatible types so it's enough to check the first
		assert !op.getEntityList().getEntityIdList().isEmpty();
		assert ec.get(op.getEntityList().getEntityIdList().get(0).getFullName()) instanceof IField;
		IFieldType firstType = ((IField) ec.get(op.getEntityList().getEntityIdList().get(0).getFullName())).getType();
		if (firstType instanceof IStringType
				|| firstType instanceof IEnumerationType
				|| firstType instanceof IPredefinedType) {
			return STRING_METHOD_SUFFIX;
		} else if (firstType instanceof IDateType) {
			return DATUM_METHOD_SUFFIX;
		} else if (firstType instanceof INumberType) {
			return ZAHL_METHOD_SUFFIX;
		} else {
			throw new IllegalArgumentException("unexpected parameter type for " + op.getOperatorName() + ": " + firstType);
		}
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IOpListOperation op) {
		String suffix = "";
		String opName = WordUtils.uncapitalize(op.getOperatorName().getRuntimeName());

		if (op.getOperatorName() == OpListOperationName.Max || op.getOperatorName() == OpListOperationName.Min) {
			OperationType art = op.getType();
			if (art == OperationType.DATE) {
				suffix = DATUM_METHOD_SUFFIX;
			}
		}
		return opName + suffix;
	}

	public static String getRuntimeMethodNameFromParseTreeObject(ICompositeOperation op) {
		return op.getCalcOp().toString().toLowerCase(Locale.ROOT);
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IDateValidityCondition b) {
		return "date" + b.getConditionName().getRuntimeName();
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IPredefinedTypeValidityCondition b) {
		return "predefinedType" + b.getConditionName().getRuntimeName();
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IGroupOperation op) {
		return WordUtils.uncapitalize(op.getOperatorName().getRuntimeName());
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IPointInTimeOperation op) {
		return op.getOperatorName().getRuntimeName();
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IDateRangeExtractionOperation op) {
		return op.getOperatorName().getRuntimeName();
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IDateAddOperation op) {
		return WordUtils.uncapitalize(op.getOperatorName().getRuntimeName());
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IDateDiffOperation op) {
		return op.getOperatorName().getRuntimeName();
	}

	public static String getRuntimeMethodNameFromParseTreeObject(IDateExtractionOperation op) {
		return op.getOperatorName().getRuntimeName();
	}

	public static String getRuntimeMethodNameFromParseTreeObject(ICompositeCondition op) {
		return op.getLogOp().getRuntimeName().toUpperCase();
	}
}
