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

package com.mgmtp.a12.kernel.md.combination.a12internal;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.JsonMapper;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views.MM_CombinationModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_selectionmodel_2.views.MM_SelectionModel_2;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.model.utils.Experimental;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Implementation of {@link IUnexpandedModelResolver} using the provided reader function.
 */
@Experimental
@OnlyForUsage
public class UnexpandedModelResolverImpl implements IUnexpandedModelResolver {
	/**
	 * note: we are responsible for closing any readers obtained from this function after use, e.g. with try-with-resources
	 */
	private final Function<@NonNull String, Reader> readerForId;

	/**
	 * @param readerForId
	 * 		a function that provides a reader (with the json of the serialized dm, cm or selection) for a given model
	 * 		id. The function must provide fresh reader instances on each invocation, as the readers will be closed
	 * 		after use (and a single call to {@link #resolve(String)} may apply readerForId more than once per id).
	 */
	public UnexpandedModelResolverImpl(@NonNull Function<@NonNull String, Reader> readerForId) {
		this.readerForId = readerForId;
	}

	@Override public @NonNull DMLike resolve(@NonNull String modelId) throws CombinationException {
		try (Reader reader = this.readerForId.apply(modelId)) {
			String modelType = getModelType(modelId);
			switch (modelType) {
				case DocumentModel.MODEL_TYPE_DOCUMENT: {
					IDocumentModel deserialized;
					try {
						deserialized = new DocumentModelService().convertToExternal(
								new DocumentModelSerializer().deserialize(reader));
					} catch (IOException e) {
						throw new CombinationException("Error loading document model: " + e.getMessage());
					}

					return new DMWrapper(deserialized);
				}
				case CombinationModelService.MODEL_TYPE: {
					List<RankedNotification> notifications = new ArrayList<>();
					MM_CombinationModel_1 deserialized = CombinationModelService.deserialize(reader, notifications::add);
					if (!notifications.isEmpty() && notifications.stream().anyMatch(n -> n.getSeverity().equals(Severity.ERROR))) {
						throw new CombinationException("Error loading combination model:\n" + String.join("\n",
								notifications.stream().map(RankedNotification::getMessage).toList()));
					}
					return new CMWrapper(deserialized);
				}
				default:
					throw new CombinationException("Unknown model type in " + modelId);
			}
		} catch (IOException e) {
			throw new RuntimeException(e); // can maybe only occur in case _closing_ of the reader fails?
		}
	}

	@Override public MM_SelectionModel_2 resolveSelectionModel(@NonNull String selId) {
		try (Reader reader = this.readerForId.apply(selId)) {
			String modelType = getModelType(selId);

			if (SelectionModelService.MODEL_TYPE.equals(modelType)) {
				List<RankedNotification> notifications = new ArrayList<>();
				MM_SelectionModel_2 deserialized = SelectionModelService.deserialize(reader, notifications::add);
				if (!notifications.isEmpty() && notifications.stream().anyMatch(n -> n.getSeverity().isAtLeastAsSevereAs(Severity.ERROR))) {
					throw new CombinationException("Error loading selection model:\n" + String.join("\n",
							notifications.stream().map(RankedNotification::getMessage).toList()));
				}
				return deserialized;
			} else {
					throw new CombinationException("Unexpected model type " + modelType + " in " + selId + " during SelectionModel resolving");
			}
		} catch (IOException e) {
			throw new RuntimeException(e); // can maybe only occur in case _closing_ of the reader fails?
		}
	}

	private String getModelType(String modelId) {
		ObjectNode jsonNode;
		try (Reader reader = this.readerForId.apply(modelId)) {
			// load as json, to check the modelType
			jsonNode = JsonMapper.getMapper().readValue(reader, ObjectNode.class);
		} catch (IOException e) {
			throw new CombinationException("Error loading model with model id " + modelId, e);
		}

		Optional<JsonNode> modelTypeField = getNested(jsonNode, MM_CombinationModel_1._pointer().header().modelType()._unwrap());
		if (modelTypeField.isEmpty()) {
			throw new CombinationException("Model type not present in model with id " + modelId);
		}

		return modelTypeField.get().asText();
	}

	private static Optional<JsonNode> getNested(@NonNull JsonNode jsonNode, @NonNull DocumentPointer pointer) {
		String name = pointer.getPathParts().getFirst().name();
		if (!jsonNode.has(name)) {
			return Optional.empty();
		}

		DocumentPointer rest = pointer.rest();
		jsonNode = jsonNode.get(name);

		if (rest.size() == 0) {
			return Optional.of(jsonNode);
		}
		return getNested(jsonNode, rest);
	}
}
