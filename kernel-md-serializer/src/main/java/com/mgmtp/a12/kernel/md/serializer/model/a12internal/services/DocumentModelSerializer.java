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

package com.mgmtp.a12.kernel.md.serializer.model.a12internal.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelRoot;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ConsistencyCheckUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.FieldTypeDefinitionWrapper;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.consistency.BasicConsistencyCheck;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;
import tools.jackson.databind.JsonNode;

/**
 * This service offers (de)serialization of a {@link IDocumentModel}.
 */
@OnlyForUsage
public class DocumentModelSerializer {

	/**
	 * See
	 * {@link IDocumentModelSerializer#deserialize(Reader)}.
	 */
	public @NonNull DocumentModel deserialize(@NonNull Reader reader) {
		// the user is responsible for closing the reader
		BufferedReader buffer = new BufferedReader(reader);
		DocumentModel documentModel = DocumentModelSerializerImpl.getMapper().readValue(buffer, DocumentModel.class);

		return resolveParentPointersAndModelReferences(documentModel);
	}

	public @NonNull DocumentModel deserialize(@NonNull JsonNode jsonNode) {
		Objects.requireNonNull(jsonNode);
		DocumentModel documentModel = DocumentModelSerializerImpl.getMapper().treeToValue(jsonNode, DocumentModel.class);

		return resolveParentPointersAndModelReferences(documentModel);
	}

	private static @NonNull DocumentModel resolveParentPointersAndModelReferences(@NonNull DocumentModel documentModel) {
		final DocumentModelRoot modelRoot = documentModel.getContent().getModelRoot();
		modelRoot.getElements().forEach(elem -> elem.setParent(modelRoot));

		foldLegacyErrorCodesToSuppress(documentModel);

		DocumentModelUtils.resolveAndUpdateReferences(documentModel, new ProblemReporter());

		return documentModel;
	}

	/**
	 * Folds any legacy {@code errorCodesToSuppress} list read from an old document model into each computation's
	 * {@code additionalParserInformation} preamble, localized to the model's condition language. This runs on every
	 * deserialization so that models still carrying the removed field keep suppressing the intended warnings; it is a
	 * no-op for models already using {@code additionalParserInformation}. Transitional — to be removed with the next
	 * breaking release.
	 */
	private static void foldLegacyErrorCodesToSuppress(@NonNull DocumentModel documentModel) {
		Locale conditionLanguage = documentModel.getContent().getModelConfig().getConditionLanguage();
		DocumentModelUtils.getAllComputations(documentModel)
				.forEach(computation -> computation._internal_foldLegacyErrorCodesToSuppress(conditionLanguage));
	}

	/**
	 * Serializes the given {@link DocumentModel} into the given {@link Writer}:
	 * <ul>
	 * <li>If the {@link DocumentModel} is expanded the
	 * serialized document model will also be expanded and the information about the include
	 * definition (see {@link Group#getIncludeConfig()}) will be lost in order to avoid future
	 * inconsistencies.</li>
	 * <li>If the {@link DocumentModel} is not expanded, either direct from the deserialization
	 * process (see {@link DocumentModelSerializer#deserialize(Reader)}) or collapsed, the serialized document model will
	 * keep the form. This means the information about the include definition will remain.</li>
	 * </ul>
	 * <p>
	 * <b>Note:</b> After serializing, a basic consistency check will be performed in order to make
	 * sure that the given {@link DocumentModel} was correctly serialized. Even if problems are found,
	 * the {@code writer} is guaranteed to have received valid JSON.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} which should be serialized. May not be {@code null}.
	 * @param writer
	 * 		the {@link Writer} to which the {@link DocumentModel} should be
	 * 		serialized (even if problems are reported). May not be {@code null}.
	 * 		<b>Important note:</b> The caller is responsible for closing the {@link Writer}.
	 * @param pr
	 * 		a {@link IProblemReporter} to which basic consistency problems found with
	 * 		the {@link DocumentModel} will be reported. May not be {@code null}.
	 */
	public void serialize(
			@NonNull DocumentModel documentModel,
			@NonNull Writer writer,
			@NonNull IProblemReporter pr
	) throws IOException {
		DocumentModelSerializerImpl.serializeInternal(new DocumentModelWrapper(documentModel), writer,
				ConsistencyCheckUtils.problemReporterToNotificationReceiver(pr), false);
	}

	/**
	 * Serializes the given {@link FieldTypeDefinition}s into the given {@link Writer}
	 * as a JSON-Array of the same sequence.
	 * <p>
	 * <b>Note:</b> While serializing, a basic consistency check will be executed.
	 * Even if this check finds problems, the serialized {@link FieldTypeDefinition}s
	 * will be correctly written to the writer and produce valid JSON.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} all {@code fieldTypeDefinitions} belong to.
	 * 		May not be {@code null}.
	 * @param fieldTypeDefinitions
	 * 		the list of {@link FieldTypeDefinition}s which should be serialized.
	 * 		May not be {@code null}. May not contain elements that are {@code null}.
	 * @param writer
	 * 		the {@link Writer} to which the {@link FieldTypeDefinition}s should be
	 * 		serialized. May not be {@code null}. <b>Important note:</b> The caller
	 * 		is responsible for closing the {@link Writer}.
	 * @param pr
	 * 		a {@link IProblemReporter} to which basic consistency problems found with
	 * 		the {@link FieldTypeDefinition}s will be reported. May not be {@code null}.
	 */
	public void serializeFieldTypeDefinitions(
			@NonNull DocumentModel documentModel,
			@NonNull List<FieldTypeDefinition> fieldTypeDefinitions,
			@NonNull Writer writer,
			@NonNull IProblemReporter pr
	) {
		Validate.noNullElements(fieldTypeDefinitions);

		List<IFieldTypeDefinition> iFieldTypeDefinitions = fieldTypeDefinitions.stream()
				.map(FieldTypeDefinitionWrapper::new)
				.collect(Collectors.toList());

		DocumentModelSerializerImpl.getMapper().writeValue(writer, fieldTypeDefinitions);
		BasicConsistencyCheck.checkFieldTypeDefinitions(new DocumentModelWrapper(documentModel), iFieldTypeDefinitions,
				ConsistencyCheckUtils.problemReporterToNotificationReceiver(pr)::accept);
	}

	/**
	 * Serializes the given {@link Element}s into the given {@link Writer}
	 * as a JSON-Array of the same sequence.
	 * <p>
	 * <b>Note:</b> While serializing, a basic consistency check will be executed.
	 * Even if this check finds problems, the serialized {@link Element}s will
	 * be correctly written to the writer and produce valid JSON.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} all {@code computations} belong to.
	 * 		May not be {@code null}.
	 * @param elements
	 * 		the list of {@link Element}s which should be serialized.
	 * 		May not be {@code null}. May not contain elements that are {@code null}.
	 * @param writer
	 * 		the {@link Writer} to which the {@link Element}s should be
	 * 		serialized. May not be {@code null}. <b>Important note:</b> The caller
	 * 		is responsible for closing the {@link Writer}.
	 * @param pr
	 * 		a {@link IProblemReporter} to which basic consistency problems found with
	 * 		the {@link Element}s will be reported. May not be {@code null}.
	 */
	public void serializeElements(
			@NonNull DocumentModel documentModel,
			@NonNull List<? extends Element> elements,
			@NonNull Writer writer,
			@NonNull IProblemReporter pr
	) {
		Validate.noNullElements(elements);

		DocumentModelSerializerImpl.getMapper().writeValue(writer, elements);
		BasicConsistencyCheck.checkElements(new DocumentModelWrapper(documentModel), ElementUtils.convert(elements),
				ConsistencyCheckUtils.problemReporterToNotificationReceiver(pr)::accept);
	}
}
