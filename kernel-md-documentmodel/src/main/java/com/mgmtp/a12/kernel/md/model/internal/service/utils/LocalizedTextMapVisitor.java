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

package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.ILabeled;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

public class LocalizedTextMapVisitor extends DocumentModelVisitor {
	@FunctionalInterface
	public interface LocalizedTextMapHandler {
		/**
		 * @param text
		 * 		the {@link ILocalizedTextMap} to be visited.
		 * @param source
		 * 		the {@link IElement} or {@link IFieldTypeDefinition} containing the localized
		 * 		text.
		 * @param property
		 * 		a textual description of the function of the {@link ILocalizedTextMap} in the
		 *        {@link IElement} or {@link IFieldTypeDefinition}.
		 */
		void handleLocalizedTextMap(ILocalizedTextMap text, Object source, String property);
	}

	/**
	 * @param model
	 * 		the model to search for {@link ILocalizedTextMap}
	 * @param handler
	 * 		the handler being executed for each {@link ILocalizedTextMap}
	 */
	public static final void visitLocalizedTextMaps(IDocumentModel model, LocalizedTextMapHandler handler) {
		LocalizedTextMapVisitor visitor = new LocalizedTextMapVisitor(handler);
		new DocumentModelWalker().acceptDocumentModel(model, visitor);
	}

	private LocalizedTextMapHandler handler;

	public LocalizedTextMapVisitor(LocalizedTextMapHandler handler) {
		this.handler = handler;
	}

	@Override
	public VisitProcess visitField(IField field) {
		if (field.getHelperText() != null) {
			handleLocalizedTextMap(field.getHelperText(), field, "helper text");
		}
		IFieldType type = field.getFieldType();
		handleFieldType(type, field);
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	protected void handleFieldType(IFieldType type, Object errorSoruce) {
		if (type instanceof IStringType) {
			handleLocalizedTextMap(((IStringType) type).getErrorMessage(), errorSoruce, "string type error message");
		}
		if (type instanceof IEnumerationType) {
			IEnumerationType enumType = (IEnumerationType) type;
			handleLocalizedTextMap(enumType.getErrorMessage(), errorSoruce, "enumeration type error message");
			enumType.getValues().forEach(v -> handleLocalizedTextMap(v.getLabel(), errorSoruce,
					"enumeration type value label for value '" + v.getValue() + "'"));
		}
	}

	@Override
	public VisitProcess visitComputation(IComputation computation) {
		handleLocalizedTextMap(computation.getErrorMessage(), computation, "error message");
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitRule(IRule rule) {
		handleLocalizedTextMap(rule.getErrorMessage(), rule, "error message");
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitElement(IElement element) {
		if (element instanceof ILabeled) {
			handleLocalizedTextMap(((ILabeled) element).getLabel(), element, "label");
		}
		handleLocalizedTextMap(element.getExternalDescription(), element, "external description");
		handleLocalizedTextMap(element.getInternalDescription(), element, "internal description");
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	@Override
	public VisitProcess visitFieldTypeDefinition(IFieldTypeDefinition fieldTypeDefinition) {
		handleLocalizedTextMap(fieldTypeDefinition.getExternalDescription(), fieldTypeDefinition, "external description");
		handleLocalizedTextMap(fieldTypeDefinition.getInternalDescription(), fieldTypeDefinition, "internal description");
		handleFieldType(fieldTypeDefinition.getFieldType(), fieldTypeDefinition);
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	private void handleLocalizedTextMap(ILocalizedTextMap text, Object source, String property) {
		handler.handleLocalizedTextMap(text, source, property);
	}
}
