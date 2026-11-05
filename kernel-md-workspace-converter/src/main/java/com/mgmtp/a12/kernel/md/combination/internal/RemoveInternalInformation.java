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
package com.mgmtp.a12.kernel.md.combination.internal;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.mgmtp.a12.dataservices.wcf.WorkspaceFactory;
import com.mgmtp.a12.dataservices.wcf.domain.ModelTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.kernel.core.base.utils.internal.Consumer2ProblemReporter;
import com.mgmtp.a12.kernel.core.parser.internal.ConditionCommentStripper;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel.MappingModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation.ComputationAlternative;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelContent;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentUniquenessCriterion;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.ILabeled;
import com.mgmtp.a12.kernel.md.model.a12internal.LocalizedTextMapBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.codegen.A12SpecificSettings;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

/**
 * Post-expansion step: removes authoring-only / internal information from the document models in the workspace, so that it
 * is not shipped to (and potentially leaked by) the runtime.
 * <p>
 * The following information is removed from every model that is a document-model serialization (plain expanded
 * {@value DocumentModel#MODEL_TYPE_DOCUMENT} models as well as the precomputation fragments retained by
 * {@link HandlePrecompAndOtherFragments}):
 * <ul>
 *   <li>the {@code internalDescription} and {@code changeNote} of every element and type definition</li>
 *   <li>the {@code additionalInfo} ("Zusatzbeschriftung") of every field</li>
 *   <li>comments (the {@code ;;} {@code Comment} tokens) in rule conditions and computation expressions</li>
 *   <li>type definitions that are not (transitively) referenced by any field</li>
 *   <li>translations for locales that are not declared in the model header (every localized text map is stripped down to
 *       the declared locales, so authoring-only languages are not shipped); the declared locale list itself is left
 *       untouched</li>
 * </ul>
 */
public class RemoveInternalInformation {

	private RemoveInternalInformation() {
	}

	public static @NonNull Workspace remove(@NonNull Workspace workspace,
			@NonNull Consumer<? super RankedNotification> notificationConsumer) {
		Map<String, ModelTuple> models = workspace.getModels();

		// iterate over a snapshot of the keys, since we mutate the map
		for (String modelId : new HashSet<>(models.keySet())) {
			ModelTuple tuple = models.get(modelId);
			if (!isCleanableDocumentModel(tuple)) {
				continue;
			}

			DocumentModelSerializer serializer = new DocumentModelSerializer();
			DocumentModel dm = serializer.deserialize(new StringReader(tuple.getContent()));

			cleanDocumentModel(dm, notificationConsumer);

			String serialized = serialize(serializer, dm, notificationConsumer);
			if (serialized != null) {
				models.put(modelId, WorkspaceFactory.getInstance().createModelTuple(dm.getHeader(), serialized));
			}
			// else: serialization failed; an error was reported and the original model is left untouched
		}

		return workspace;
	}

	private static boolean isCleanableDocumentModel(ModelTuple tuple) {
		String modelType = tuple.getHeader().getModelType();
		return DocumentModel.MODEL_TYPE_DOCUMENT.equals(modelType)
				|| MappingModelService.RUNTIME_PRECOMP_FRAGMENT_MODEL_TYPE.equals(modelType);
	}

	private static void cleanDocumentModel(DocumentModel dm, Consumer<? super RankedNotification> notificationConsumer) {
		DocumentModelContent content = dm.getContent();
		Locale conditionLanguage = content.getModelConfig().getConditionLanguage();
		IProjectSpecificSettings pss = new A12SpecificSettings(dm.getHeader().getLocales());

		// 1. clear internal descriptions + additionalInfo, strip rule/computation comments and collect referenced type defs
		Set<String> referencedTypeDefIds = new HashSet<>();
		new DocumentModelWalker().acceptDocumentModel(dm, new CleaningVisitor(conditionLanguage, pss, referencedTypeDefIds));

		// 2. remove type definitions no longer referenced (done before locale stripping, so the removed type definitions'
		// texts don't need to be visited)
		removeUnusedTypeDefinitions(content, referencedTypeDefIds);

		// 3. strip undeclared (authoring-only) locales from every localized text map
		stripUndeclaredLocales(dm);
	}

	/**
	 * Clears the internal description and change note of every element / type definition, clears the additionalInfo of every
	 * field, strips comments from rule conditions and computation expressions, and collects the ids of all type definitions
	 * referenced by a field.
	 */
	private static final class CleaningVisitor extends DocumentModelVisitor {

		private final Locale conditionLanguage;
		private final IProjectSpecificSettings pss;
		private final Set<String> referencedTypeDefIds;

		private CleaningVisitor(Locale conditionLanguage, IProjectSpecificSettings pss, Set<String> referencedTypeDefIds) {
			this.conditionLanguage = conditionLanguage;
			this.pss = pss;
			this.referencedTypeDefIds = referencedTypeDefIds;
		}

		@Override
		public VisitProcess visitElement(Element element) {
			// covers all element kinds (field, group, rule, computation)
			element.setInternalDescription(new LocalizedTextMapBuilder().build());
			element.setChangeNote(null);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitField(Field field) {
			field.setAdditionalInfo(null);
			if (field.getFieldType() instanceof TypeDefType typeDefType) {
				referencedTypeDefIds.add(typeDefType.getId());
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitRule(Rule rule) {
			rule.setErrorCondition(strip(rule.getErrorCondition()));
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitComputation(Computation computation) {
			computation.getCommonPrecondition().ifPresent(cp -> computation.setCommonPrecondition(strip(cp)));
			for (ComputationAlternative alternative : computation.getComputationAlternatives()) {
				String operation = alternative.getOperation();
				if (!operation.isEmpty()) {
					alternative.setOperation(strip(operation));
				}
				alternative.getPrecondition().ifPresent(pc -> alternative.setPrecondition(strip(pc)));
				alternative.getToleranceRangeOp().ifPresent(tr -> alternative.setToleranceRangeOp(strip(tr)));
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitFieldTypeDefinition(FieldTypeDefinition fieldTypeDefinition) {
			fieldTypeDefinition.setInternalDescription(new LocalizedTextMapBuilder().build());
			fieldTypeDefinition.setChangeNote(null);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private String strip(String conditionText) {
			return ConditionCommentStripper.stripComments(conditionText, conditionLanguage, pss);
		}
	}

	private static void removeUnusedTypeDefinitions(DocumentModelContent content, Set<String> referencedFromFields) {
		List<FieldTypeDefinition> typeDefinitions = content.getTypeDefinitions();

		// a referenced type definition may itself reference further type definitions, so compute the transitive closure
		Set<String> reachable = new HashSet<>(referencedFromFields);
		boolean changed = true;
		while (changed) {
			changed = false;
			for (FieldTypeDefinition typeDefinition : typeDefinitions) {
				if (reachable.contains(typeDefinition.getId())
						&& typeDefinition.getFieldType() instanceof TypeDefType typeDefType
						&& reachable.add(typeDefType.getId())) {
					changed = true;
				}
			}
		}

		content.setTypeDefinitions(typeDefinitions.stream()
				.filter(td -> reachable.contains(td.getId()))
				.toList());
	}

	/**
	 * Removes, from every localized text map in the model, the entries for locales that are not declared in the model
	 * header. This drops authoring-only translations (e.g. an internal language) that must not be shipped to the runtime,
	 * while leaving the declared locale list itself untouched.
	 */
	private static void stripUndeclaredLocales(DocumentModel dm) {
		List<Locale> declaredLocales = dm.getHeader().getLocales();
		if (declaredLocales == null || declaredLocales.isEmpty()) {
			// no locales declared: don't strip, as that would wipe every localized text
			return;
		}
		new DocumentModelWalker().acceptDocumentModel(dm, new LocaleStrippingVisitor(new HashSet<>(declaredLocales)));
	}

	/**
	 * Strips every localized text map (label, external/internal description, helper text, requiredness-config error
	 * message, rule/computation/uniqueness-criterion error message and string/enumeration type error messages and
	 * enumeration value labels) down to the declared locales.
	 */
	private static final class LocaleStrippingVisitor extends DocumentModelVisitor {

		private final Set<Locale> declaredLocales;

		private LocaleStrippingVisitor(Set<Locale> declaredLocales) {
			this.declaredLocales = declaredLocales;
		}

		@Override
		public VisitProcess visitElement(Element element) {
			// covers all element kinds (field, group, rule, computation)
			strip(element.getExternalDescription());
			strip(element.getInternalDescription());
			if (element instanceof ILabeled labeled) {
				strip(labeled.getLabel());
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitField(Field field) {
			strip(field.getHelperText());
			field.getRequirednessConfig().ifPresent(config -> strip(config.errorMessage));
			stripFieldType(field.getFieldType());
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitRule(Rule rule) {
			strip(rule.getErrorMessage());
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitComputation(Computation computation) {
			strip(computation.getErrorMessage());
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitDocumentUniquenessCriterion(DocumentUniquenessCriterion uniquenessCriterion) {
			strip(uniquenessCriterion.getErrorMessage());
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitFieldTypeDefinition(FieldTypeDefinition fieldTypeDefinition) {
			strip(fieldTypeDefinition.getExternalDescription());
			strip(fieldTypeDefinition.getInternalDescription());
			stripFieldType(fieldTypeDefinition.getFieldType());
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private void stripFieldType(FieldType fieldType) {
			if (fieldType instanceof StringType stringType) {
				strip(stringType.getErrorMessage());
			}
			if (fieldType instanceof EnumerationType enumType) {
				enumType.getValues().forEach(value -> strip(value.getLabel()));
				strip(enumType.getErrorMessage());
			}
		}

		private void strip(ILocalizedTextMap text) {
			if (text != null) {
				text.keySet().retainAll(declaredLocales);
			}
		}
	}

	/**
	 * @return the serialized document model, or {@code null} if serialization failed with an {@link IOException}.
	 */
	private static String serialize(DocumentModelSerializer serializer, DocumentModel dm,
			Consumer<? super RankedNotification> notificationConsumer) {
		StringWriter writer = new StringWriter();
		try {
			serializer.serialize(dm, writer, new Consumer2ProblemReporter(notificationConsumer));
		} catch (IOException e) {
			notificationConsumer.accept(RankedNotificationImpl.of(e.getMessage(), Severity.ERROR, dm));
			return null;
		}
		return writer.toString();
	}
}
