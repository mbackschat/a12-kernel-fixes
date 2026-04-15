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
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.utils.internal.metaModel.customConditions.CustomConditionUtil;
import com.mgmtp.a12.kernel.core.utils.internal.services.NameValidationService;

/**
 * Uses the {@link NameValidationService} to check the short name of {@link IField#getFullName()}.
 */
public class CheckFieldName implements ICustomCondition {

	private final NameValidationService nameValidator;
	private final boolean isHmodel;

	public CheckFieldName(NameValidationService nameValidator, boolean isHmodel) {
		this.nameValidator = nameValidator;
		this.isHmodel = isHmodel;
	}

	@Override
	public boolean check(IData data, Set<IIdentifier> relevantFields, Set<IIdentifier> formallyIncorrectFields,
			IIdentifier errorField) {
		if (CustomConditionUtil.isNotRelevant(errorField, relevantFields, formallyIncorrectFields)) {
			return false;
		}
		String fieldName = data.getValue(errorField);

		if (nameValidator instanceof NameValidationService nameValidationService) {
			// do only those checks that are not done in separate rules in the meta model
			return !nameValidationService.checkFieldNameNoMetaModelChecks(fieldName);
		}

		return !nameValidator.isValidFieldname(fieldName, isHmodel);
	}

}
