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

package com.mgmtp.a12.kernel.core.utils.a12internal.metaModel.customConditions;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.utils.internal.metaModel.IdentifierImpl;
import com.mgmtp.a12.kernel.core.utils.internal.metaModel.customConditions.CustomConditionUtil;

/**
 * Validates the values of hint lists of StringType.
 * <p>
 * The errorField value in "/Data/Field/FieldType/StringType/HintLists/Values/value" has to be checked:
 * - if it is not relevant, return false
 * - if the MM-field "/Data/Field/FieldType/StringType/pattern" is filled, the value must match the given pattern.
 */
public class CheckHintList implements ICustomCondition {

	@Override
	public boolean check(IData data, Set<IIdentifier> relevantFields, Set<IIdentifier> formallyIncorrectFields,
			IIdentifier errorField) {
		if (CustomConditionUtil.isNotRelevant(errorField, relevantFields, formallyIncorrectFields)) {
			return false;
		}
		// If the MM-field "/Data/Field/FieldType/StringType/pattern" is filled, the value must match the given pattern
		IIdentifier patternId = createPatternId(errorField);

		String patternValue = data.getValue(patternId);
		if (patternValue != null) {
			String fieldValue = data.getValue(errorField);
			try {
				final Pattern pattern = Pattern.compile(patternValue);
				return !pattern.matcher(fieldValue).matches();
			} catch (Exception e) {
				return false; // the pattern is checked through CheckStringPattern
			}
		}
		return false;
	}

	private static IIdentifier createPatternId(IIdentifier errorField) {
		long[] indices = Arrays.copyOf(errorField.getIndices(), errorField.getIndices().length - 2);
		// We assume that the templateName field is in the parent group as the error field ("PatternProperty/pattern")
		String valuesName = ElementPathUtils.getParentPath(errorField.getName()); // parent group
		String hintListsName = ElementPathUtils.getParentPath(valuesName);
		String stringTypeName = ElementPathUtils.getParentPath(hintListsName);
		String templateNameFieldFullName = ElementPathUtils.createPath(stringTypeName, "pattern");
		return new IdentifierImpl(templateNameFieldFullName, indices);
	}
}
