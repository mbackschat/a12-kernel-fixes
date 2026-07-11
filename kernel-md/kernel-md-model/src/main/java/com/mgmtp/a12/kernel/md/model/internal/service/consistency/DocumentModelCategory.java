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
package com.mgmtp.a12.kernel.md.model.internal.service.consistency;

import java.util.Locale;
import java.util.ResourceBundle;

import com.mgmtp.a12.model.consistency.ConsistencyCategory;

/**
 * Enumeration for error codes. Error messages are read from properties-file to ensure I18N.
 */
public enum DocumentModelCategory implements ConsistencyCategory {
	DOCUMENT_MODEL_WRONG_VERSION("problem.document.wrongVersion"),
	DOCUMENT_MODEL_WRONG_VERSION_PATTERN("problem.document.wrongVersionPattern"),
	DOCUMENT_MODEL_ID_NOT_UNIQUE("problem.document.nonUniqueId"),

	DOCUMENT_MODEL_ATTACHMENT_MISSING_FIELD("problem.document.attachment.missing.field"),
	DOCUMENT_MODEL_ATTACHMENT_REQUIRED_RULES("problem.document.attachment.rules.required"),
	DOCUMENT_MODEL_ATTACHMENT_CONTENT_NO_VALUE_VALIDATION("problem.document.attachment.content.validation"),

	DOCUMENT_MODEL_MULTI_SELECT_REPEATABLE_GROUP("problem.document.multiSelect.repeatable.group"),
	DOCUMENT_MODEL_MULTI_SELECT_REQUIRED_FIELD("problem.document.multiSelect.required.field"),
	DOCUMENT_MODEL_MULTI_SELECT_FIELD_INDEX_FIELD("problem.document.multiSelect.field.index.field"),
	DOCUMENT_MODEL_MULTI_SELECT_OTHER_ELEMENTS("problem.document.multiSelect.other.elements"),
	DOCUMENT_MODEL_MULTI_SELECT_FIELD_REQUIRED_FLAG("problem.document.multiSelect.field.required.flag"),
	DOCUMENT_MODEL_MULTI_SELECT_FIELD_FIELDTYPE("problem.document.multiSelect.field.fieldtype"),

	DOCUMENT_MODEL_CODEGENERATION_PROBLEM("problem.document.codeGen"),
	DOCUMENT_MODEL_CONSISTENCY_CHECK_FROM_SERIALIZER_PROBLEM("problem.document.consistencyCheckFromSerializer"),

	DOCUMENT_MODEL_UNIQUENESS_CRITERION_FIELDS_EMPTY("problem.document.documentUniquenessCriterion.fieldsEmpty"),
	DOCUMENT_MODEL_UNIQUENESS_CRITERION_FIELD_NOT_FOUND("problem.document.documentUniquenessCriterion.fieldNotFound"),
	DOCUMENT_MODEL_UNIQUENESS_CRITERION_FIELD_NOT_REQUIRED("problem.document.documentUniquenessCriterion.fieldNotRequired"),
	DOCUMENT_MODEL_UNIQUENESS_CRITERION_FIELD_REPEATABLE("problem.document.documentUniquenessCriterion.fieldRepeatable"),
	DOCUMENT_MODEL_UNIQUENESS_CRITERION_FIELD_TRANSIENT("problem.document.documentUniquenessCriterion.fieldTransient"),
	DOCUMENT_MODEL_UNIQUENESS_CRITERION_MISSING_ERROR_TEXT("problem.document.documentUniquenessCriterion.missingErrorText"),
	DOCUMENT_MODEL_UNIQUENESS_CRITERION_INVALID_ERROR_TEXT("problem.document.documentUniquenessCriterion.invalidErrorText"),
	DOCUMENT_MODEL_UNIQUENESS_CRITERION_INVALID_ERROR_TEXT_PARAMETER(
			"problem.document.documentUniquenessCriterion.invalidErrorTextParameter"),
	DOCUMENT_MODEL_UNIQUENESS_CRITERION_DUPLICATE_NAME("problem.document.documentUniquenessCriterion.duplicateName"),
	DOCUMENT_MODEL_UNIQUENESS_CRITERION_EMPTY_NAME("problem.document.documentUniquenessCriterion.emptyName");

	private final String keyValue;

	DocumentModelCategory(final String keyValue) {
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
