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
package com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules;

import java.math.BigDecimal;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblem;
import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblemReporter;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelCategory;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelConsistencyRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.utils.NumberLimits;
import com.mgmtp.a12.model.notification.Severity;

/**
 * <p>
 * Verifies that min and max value declared for a number field do not exceed the maximum length
 * possible for MVK number
 * types.
 * </p>
 * <p>
 * MVK numbers can have at most 17 places with one reserved for the decimal point and one for the
 * sign. That means 15
 * places remain for integer and decimal places.
 * </p>
 * <p>
 * With k decimal places declared for a given number field, the maximum allowed value that fits
 * these length limitations
 * will then be given by 10^(15 - k) -10^(-k). The minimum allowed is -(10^(15 - k) -10^(-k))
 * respectively.
 * </p>
 */
public class NumberFieldValueLimitRule extends DocumentModelConsistencyRule {

	@Override
	public void checkModel(final IDocumentModel model, DocumentModelConsistencyProblemReporter pr) {
		final NumberValueLimitChecker checker = new NumberValueLimitChecker(model.getHeader().getId(), pr);
		new DocumentModelWalker().acceptDocumentModel(model, checker);
	}

	private static class NumberValueLimitChecker extends DocumentModelVisitor {
		private final String modelName;
		private final DocumentModelConsistencyProblemReporter problemReporter;
		private IField checkedField;

		public NumberValueLimitChecker(final String modelName, DocumentModelConsistencyProblemReporter problemReporter) {
			this.modelName = modelName;
			this.problemReporter = problemReporter;
		}

		@Override
		public DocumentModelWalker.VisitProcess visitField(final IField field) {
			checkField(field);
			return DocumentModelWalker.VisitProcess.CONTINUE_TRAVERSAL;
		}

		public void checkField(final IField field) {
			this.checkedField = field;
			Optional<IFieldType> dataType = field.getEffectiveType();
			if (dataType.filter(INumberType.class::isInstance).isPresent()) {
				final INumberType numberType = (INumberType) dataType.get();
				final int maxDecimalPlaces = numberType.getMaxFractionalDigits();
				final double maxAllowedValue = NumberLimits.getMaxAllowedValue(maxDecimalPlaces);
				numberType.getMaxValue().ifPresent(
						maxValue -> checkMaxValue(maxValue, maxAllowedValue, maxDecimalPlaces));
				numberType.getMinValue().ifPresent(
						minValue -> checkMinValue(minValue, maxAllowedValue, maxDecimalPlaces));
			}
		}

		private void checkMinValue(final BigDecimal minValue,
				final double maxAllowedValue,
				final int maxDecimalPlaces) {
			final double minValueDouble = minValue.doubleValue();
			if (Math.abs(minValueDouble) > maxAllowedValue) {
				problemReporter.reportProblem(
						new DocumentModelConsistencyProblem(
								modelName,
								checkedField,
								DocumentModelCategory.DOCUMENT_MODEL_NUMBER_MIN_VALUE_EXCEEDED,
								Severity.ERROR,
								checkedField.getName(),
								minValue.toString(),
								NumberLimits.printValue(maxAllowedValue, maxDecimalPlaces, '.')));
			}
		}

		private void checkMaxValue(final BigDecimal maxValue,
				final double maxAllowedValue,
				final int maxDecimalPlaces) {
			final double maxValueDouble = maxValue.doubleValue();
			if (maxValueDouble > maxAllowedValue) {
				problemReporter.reportProblem(
						new DocumentModelConsistencyProblem(
								modelName,
								checkedField,
								DocumentModelCategory.DOCUMENT_MODEL_NUMBER_MAX_VALUE_EXCEEDED,
								Severity.ERROR,
								checkedField.getName(),
								maxValue.toString(),
								NumberLimits.printValue(maxAllowedValue, maxDecimalPlaces, '.')));
			}
		}
	}
}
