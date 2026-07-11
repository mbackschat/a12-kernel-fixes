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
package com.mgmtp.a12.kernel.md.model.internal.services.join.decoration;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DMWithExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.selection.SelectionModelV2Wrapper;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.SerializationUtils;

/**
 * Executor class to join field decoration models.
 */
public class DecorationFieldModelJoiner extends DecorationModelJoiner<Field> {
	// This annotation marks a field in a decoration model to use the field type of the reference model.
	// The field type of the field in the decoration model is then ignored.
	// It allows a reuse of the decoration model without needing to know the referenced field's type.
	// In SME the fieldtype parameters are called 'data type configuration'.
	public static final String TAKE_REFERENCE_CONFIGURATION = "_take_reference_configuration";

	public DecorationFieldModelJoiner(DMWithExpansionInfo referenceModelMutatedInPlaceToResultModel,
			DMWithExpansionInfo decorationModel, int stepNumber, SelectionModelV2Wrapper selectionModelWrapper,
			Locale locale, JoiningProblemReporter problemReporter) {
		super(referenceModelMutatedInPlaceToResultModel, decorationModel, stepNumber, selectionModelWrapper, locale, problemReporter, Field.class);
	}

	@Override public boolean isValidDecorationModel() {
		return isValidFieldDecorationModel(decorationModel, locale, problemReporter);
	}

	public static boolean isValidFieldDecorationModel(DocumentModel decorationModel, Locale locale,
			JoiningProblemReporter problemReporter) {
		String anchorName = createAnchorName(Field.class);
		Consumer<Group> checkAnchor = (Group rootGroup) -> DecorationModelJoiner.checkAnchor(
				Field.class, decorationModel, rootGroup, anchorName, locale, problemReporter);
		return internal_isValidDecorationModel(checkAnchor, decorationModel, anchorName, locale, problemReporter);
	}

	private static class FieldTypesAdjusterVisitor extends DocumentModelVisitor {

		private final FieldType fieldType;

		public FieldTypesAdjusterVisitor(FieldType fieldType) {
			this.fieldType = fieldType;
		}

		@Override
		public VisitProcess visitField(final Field field) {
			if (
					field.getAnnotations().removeIf(annotation -> annotation.getName().equals(TAKE_REFERENCE_CONFIGURATION))
			) {
				FieldType fieldTypeCopy = SerializationUtils.copy(fieldType);
				if (fieldType instanceof TypeDefType typedef) {
					typedef.getTypeDefinition().ifPresent(((TypeDefType) fieldTypeCopy)::setTypeDefinition);
				}
				field.setFieldType(fieldTypeCopy);
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}
	}

	@Override
	public DocumentModel prepareDecoModelForElement(Field elementToBeDecorated) {
		DocumentModel decoModelCopy = super.prepareDecoModelForElement(elementToBeDecorated);

		adjustFieldTypesInDecorationModel(decoModelCopy, elementToBeDecorated.getFieldType());
		return decoModelCopy;
	}

	@Override protected boolean noDecorationNecessary(Group group) {
		Optional<String> optUsageType = group.getUsageType();
		boolean isAttachment = optUsageType.isPresent()
				&& (DocumentModelUtils.ATTACHMENT_NAME.equals(optUsageType.get())
				|| "cockpit_attachment".equals(optUsageType.get()));
		return isAttachment;
	}

	private void adjustFieldTypesInDecorationModel(DocumentModel decorationModel, FieldType fieldType) {
		DocumentModelWalker decorationModelWalker = new DocumentModelWalker();
		FieldTypesAdjusterVisitor fieldTypeAdjuster = new FieldTypesAdjusterVisitor(
				fieldType);
		decorationModelWalker.acceptDocumentModel(decorationModel, fieldTypeAdjuster);
	}
}
