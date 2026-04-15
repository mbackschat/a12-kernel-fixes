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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblem;
import com.mgmtp.a12.kernel.core.utils.internal.consistency.DocumentModelConsistencyProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.GroovyCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelCategory;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.DocumentModelConsistencyRule;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.RankedNotificationUtils;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

public class CodeGenerationRule extends DocumentModelConsistencyRule {
	private static final Logger LOG = LoggerFactory.getLogger(CodeGenerationRule.class);

	@Override
	public void checkModel(final IDocumentModel model, DocumentModelConsistencyProblemReporter pr) {
		final IDocumentModelService service = new ExternalDocumentModelService();

		Collection<RankedNotification> notifications = new ArrayList<>();
		service.generateValidationCode(model, new GroovyCodeGeneratorConfig(null), null, notifications::add);

		if (!notifications.isEmpty()) {
			reportProblems(pr, notifications, model);
			return;
		}
		if (!service.extractUsedVariants(model).isEmpty()) {
			LOG.warn("The given document model {} contains variants. "
					+ "A complete check was done only for the default variant (empty string). "
					+ "Therefore, the check may not be complete.", model.getHeader().getId());
		}
	}

	private void reportProblems(
			DocumentModelConsistencyProblemReporter pr,
			Collection<RankedNotification> notifications,
			IDocumentModel model
	) {
		DocumentModel modelInternal = ((DocumentModelWrapper) model).getDocumentModel();
		Collection<RankedNotification> existingNotifications = new HashSet<>();
		notifications.stream()
				.filter(existingNotifications::add)
				.map(notification -> convertCodegenProblem(modelInternal, notification))
				.forEach(pr::reportProblem);
	}

	private DocumentModelConsistencyProblem convertCodegenProblem(DocumentModel model, RankedNotification notification) {
		return new DocumentModelConsistencyProblem(
				model.getHeader().getId(),
				convertCodegenSource(model, notification.getSource()),
				DocumentModelCategory.DOCUMENT_MODEL_CODEGENERATION_PROBLEM,
				Severity.ERROR,
				RankedNotificationUtils.convertSourceToString(notification),
				notification.getMessage());
	}

	private Object convertCodegenSource(DocumentModel model, Object source) {
		if (source instanceof IEntity entity) {
			return DocumentModelUtils
					.getElementByPath(model, entity.getFullName())
					.map(ElementUtils::convert)
					.map(Object.class::cast)
					.orElse(source);
		} else {
			return source;
		}
	}
}
