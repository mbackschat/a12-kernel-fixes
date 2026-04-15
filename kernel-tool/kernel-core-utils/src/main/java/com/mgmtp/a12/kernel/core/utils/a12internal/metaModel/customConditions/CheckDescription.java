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

import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;
import com.mgmtp.a12.kernel.core.utils.internal.metaModel.customConditions.CustomConditionUtil;

/**
 * Checks the (internal) description of rules and fields.
 * The description may only contain symbols, that are representable as one character in UTF-16.
 */
public class CheckDescription implements ICustomCondition {

	@Override
	public boolean check(IData data, Set<IIdentifier> relevantFields, Set<IIdentifier> formallyIncorrectFields,
			IIdentifier errorField) {
		if (CustomConditionUtil.isNotRelevant(errorField, relevantFields, formallyIncorrectFields)) {
			return false;
		}
		String description = data.getValue(errorField);

		return !allSymbolsRepresentableAsOneCharacter(description);
	}

	private boolean allSymbolsRepresentableAsOneCharacter(String s) {
		return s.chars().mapToObj(i -> (char) i).allMatch(CommonUtils::representsOneCharacter);
	}
}
