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

package com.mgmtp.a12.kernel.tool.docanalysis.docs;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.tool.docanalysis.BaseTestDocumentModelConsistency;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;

import lombok.NonNull;

class TODO {
	public static final Path PATH = Paths.get("src/test/resources");
}

// tag::consistencyCheck[]
public class DocumentModelConsistencyCheck extends BaseTestDocumentModelConsistency {
	private static final HeaderParser HEADER_PARSER = new DefaultHeaderParser();
	private static final DocumentModelServiceFactory DOC_MODEL_SERVICE_FACTORY = new DocumentModelServiceFactory();
	private static final IDocumentModelSerializer DOC_MODEL_SERIALIZER = DOC_MODEL_SERVICE_FACTORY.createDocumentModelSerializer();
	private static final IDocumentModelService DOC_MODEL_SERVICE = DOC_MODEL_SERVICE_FACTORY.createDocumentModelService();
	private static final Path DOC_MODELS_PATH = TODO.PATH; // set your path here

	@Override
	public Stream<Arguments> testData() throws IOException {
		try (Stream<Path> files = Files.walk(DOC_MODELS_PATH)) {
			Stream<Arguments> argumentsStream = files.filter(path -> path.toFile().isFile())
					.filter(path -> path.getFileName().toString().endsWith(".json"))
					.filter(DocumentModelConsistencyCheck::isDocumentModel)
					.map(path -> Arguments.of(path.toString(), deserializeAndExpand(path)));
			// creating a new stream, so the file walker can be closed
			return argumentsStream.toList().stream();
		}
	}

	private static boolean isDocumentModel(Path path) {
		Optional<String> optDocModelJson = readToString(path);
		if (optDocModelJson.isEmpty()) {
			return false;
		}
		Optional<Header> optHeader = parseHeader(optDocModelJson.get());
		if (optHeader.isEmpty()) {
			return false;
		}
		return "document".equals(optHeader.get().getModelType());
	}

	private static Optional<String> readToString(Path path) {
		try (Scanner scanner = new Scanner(path, StandardCharsets.UTF_8)) {
			return Optional.of(scanner.useDelimiter("\\A").next());
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private static Optional<Header> parseHeader(String docModelJson) {
		try {
			return Optional.of(HEADER_PARSER.parseJson(docModelJson));
		} catch (HeaderParseException e) {
			return Optional.empty();
		}
	}

	private static IDocumentModel deserializeAndExpand(Path docModelPath) {
		IDocumentModel docModel = deserialize(docModelPath);
		IDocumentModelReferenceResolver resolver = new DocumentModelReferenceResolverImpl(docModelPath.getParent());
		DOC_MODEL_SERVICE.expand(docModel, resolver);
		return docModel;
	}

	private static IDocumentModel deserialize(Path docModelPath) {
		try (Reader reader = new FileReader(docModelPath.toFile(), StandardCharsets.UTF_8)) {
			return DOC_MODEL_SERIALIZER.deserialize(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(
					String.format("Could not load model from file '%s'.", docModelPath.toAbsolutePath()), e);
		}
	}

	static class DocumentModelReferenceResolverImpl implements IDocumentModelReferenceResolver {
		private final Path basePath;

		public DocumentModelReferenceResolverImpl(Path basePath) {
			this.basePath = basePath;
		}

		@Override
		public IDocumentModel getDocumentModel(@NonNull String modelReference) {
			Path filePath = basePath.resolve(modelReference);
			return deserialize(filePath);
		}
	}
}
// end::consistencyCheck[]