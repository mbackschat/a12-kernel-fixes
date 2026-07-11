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
package com.mgmtp.a12.kernel.md.serializer.model.internal.service.consistency;

import java.util.Locale;
import java.util.ResourceBundle;

import com.mgmtp.a12.model.consistency.ConsistencyCategory;

/**
 * Enumeration for error codes. Error messages are read from properties-file to ensure I18N.
 */
public enum BasicDocumentModelConsistencyCategory implements ConsistencyCategory {
	ID_EMPTY("problem.document.basic.idEmpty"),
	NAME_EMPTY("problem.document.basic.nameEmpty"),
	ERROR_ENTITY_EMPTY("problem.document.basic.errorEntityEmpty"),
	ERROR_CODE_EMPTY("problem.document.basic.errorCodeEmpty"),
	ERROR_CONDITION_EMPTY("problem.document.basic.errorConditionEmpty"),
	COMPUTED_FIELD_EMPTY("problem.document.basic.computedFieldEmpty"),
	COMPUTATION_OPERATION_EMPTY("problem.document.basic.computationOperationEmpty"),
	ENUM_TYPE_DUPLICATE_VALUE("problem.document.basic.enumTypeDuplicateValue"),
	ENUM_TYPE_DUPLICATE_CATEGORIES("problem.document.basic.enumTypeDuplicateCategories"),
	LOCALIZED_TEXT_ELEMENT_NULL_PROBLEM("problem.document.basic.element.localizedTextNull"),
	LOCALIZED_TEXT_TYPEDEF_NULL_PROBLEM("problem.document.basic.typedef.localizedTextNull"),
	LOCALIZED_TEXT_UNIQUENESS_CRITERION_NULL_PROBLEM("problem.document.basic.documentUniquenessCriterion.localizedTextNullOrEmpty"),
	INDEX_FIELD_NAME_EMPTY("problem.document.basic.any.indexFieldNameEmpty"),
	SORT_FIELD_NAME_EMPTY("problem.document.basic.any.sortFieldNameEmpty"),
	TYPEDEF_TYPE_REF_EMPTY("problem.document.basic.any.typedefTypeRefEmpty"),
	TYPEDEF_ID_EMPTY("problem.document.basic.typedef.idEmpty"),
	TYPEDEF_NAME_EMPTY("problem.document.basic.typedef.nameEmpty");

	private String keyValue;

	BasicDocumentModelConsistencyCategory(final String keyValue) {
		this.keyValue = keyValue;
	}

	public String getKeyValue() {
		return keyValue;
	}

	@Override
	public String getLocalizedMessage(Locale locale, String keyValue) {
		final ResourceBundle bundle = ResourceBundle.getBundle(
				"com.mgmtp.a12.kernel.md.model.internal.service.consistency.i18n.ErrorMessages", locale);
		return bundle.getString(keyValue);
	}
}
