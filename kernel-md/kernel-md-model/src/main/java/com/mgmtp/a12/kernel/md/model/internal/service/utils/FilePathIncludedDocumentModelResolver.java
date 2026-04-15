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

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.ObjectMapperProvider;

/**
 * Resolves {@link DocumentModel}s by their file path.
 * This implementation builds the file path of the included {@link DocumentModel} with the parent
 * path of the host {@link DocumentModel} and the relative reference of the included
 * {@link DocumentModel} and deserializes the {@link DocumentModel}.
 */
public class FilePathIncludedDocumentModelResolver implements DocumentModelReferenceResolver {

	private static final String JSON_EXTENSION = ".json";
	private static final String HEADER = "header";
	private static final String MODEL_ID = "id";
	private static final String MODEL_TYPE = "modelType";
	private static final String MODEL_VERSION = "modelVersion";
	private static final String DOCUMENT_MODEL_TYPE = DocumentModel.MODEL_TYPE_DOCUMENT;
	private static final String CONTENT = "content";
	private static final DocumentModelSerializer DEFAULT_SERIALIZER = new DocumentModelSerializer();

	private final DocumentModelSerializer serializer;
	/**
	 * We store a set of paths in order to detect if an id occurs more than once in workspace (directory tree below basePath).
	 * If such non-unique ids exist (not permitted by SME, but not enforced by kernel), then that id cannot be resolved
	 * unambiguously.
	 * (In order to not be overly restrictive, we should not care about non-unique ids if the id never needs to be resolved).
	 */
	private final Map<String, Set<Path>> docModelPathsById = new HashMap<>();
	private Path basePath;

	/**
	 * @param basePath
	 * 		parent path of the including {@link DocumentModel}. May not be {@code null} or
	 * 		empty.
	 */
	public FilePathIncludedDocumentModelResolver(Path basePath) {
		this(basePath, DEFAULT_SERIALIZER);
	}

	public FilePathIncludedDocumentModelResolver(Path basePath, DocumentModelSerializer serializer) {
		this.serializer = serializer;
		this.basePath = basePath;
		try (Stream<Path> walk = Files.walk(basePath)) {
			walk.filter(p -> p.toString().endsWith(JSON_EXTENSION))
					.forEach(p -> {
						JsonNode model = parseModel(p);
						if (!isDocModel(model)) {
							return;
						}
						String modelId = model.get(HEADER).get(MODEL_ID).asText();
						docModelPathsById.computeIfAbsent(modelId, id -> new HashSet<>()).add(p);
						// when assertions are enabled, fail-fast in order to eagerly detect likely-unintended situations(instead of
						// failing only if the ambiguous references is actually resolved):
						assert docModelPathsById.get(modelId).size() == 1
								: "Id" + modelId + " is not unique in " + basePath + ": " + docModelPathsById.get(modelId)
								+ "this is probably an error (but might not be critical if the id never needs to be resolved).";
					});
		} catch (IOException e) {
			throw new UncheckedIOException("Could not iterate through '" + basePath + "'.", e);
		}
	}

	private JsonNode parseModel(Path jsonPath) {
		try (final InputStream inputStream = Files.newInputStream(jsonPath)) {
			return ObjectMapperProvider.createPreconfiguredObjectMapper().readTree(inputStream);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read the json file '" + jsonPath + "'.", e);
		}
	}

	private boolean isDocModel(JsonNode model) {
		JsonNode header = model.get(HEADER);
		JsonNode content = model.get(CONTENT);
		if (header == null || !header.isObject()
				|| content == null || !content.isObject()) {
			return false;
		}
		JsonNode modelId = header.get(MODEL_ID);
		JsonNode modelType = header.get(MODEL_TYPE);
		JsonNode modelVersion = header.get(MODEL_VERSION);
		if (modelId == null || !modelId.isTextual()
				|| modelType == null || !modelType.isTextual()
				|| modelVersion == null || !modelVersion.isTextual()) {
			return false;
		}
		return modelType.asText().equals(DOCUMENT_MODEL_TYPE);
	}

	@Override
	public DocumentModel getDocumentModel(String reference) {
		Set<Path> docModelPaths = docModelPathsById.get(reference);
		if (docModelPaths == null) {
			throw new IllegalArgumentException("Reference '" + reference + "' cannot be resolved in workspace with basePath "
					+ this.basePath);
		}
		if (docModelPaths.size() != 1) {
			throw new RuntimeException("The requested model id " + reference + " is not unique in the workspace at "
					+ this.basePath + ": " + docModelPaths);
		}
		Path docModelPath = docModelPaths.stream().findFirst().get();
		DocumentModel documentModel;

		try (Reader reader = new FileReader(docModelPath.toFile(), StandardCharsets.UTF_8)) {
			documentModel = serializer.deserialize(reader);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not load model from file '" + docModelPath + "'.", e);
		}
		return documentModel;
	}

	public Set<String> getCachedDocModelIds() {
		return docModelPathsById.keySet();
	}

}
