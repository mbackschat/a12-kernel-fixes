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

package com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelRoot;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;

public class SerializationUtils {

	private static final DocumentModelSerializer SERIALIZER = new DocumentModelSerializer();

	private SerializationUtils() {
		throw new UnsupportedOperationException("This may not be instantiated.");
	}

	public static void writeBoolean(JsonGenerator gen, String fieldName, boolean value) {
		if (value) {
			gen.writeBooleanProperty(fieldName, value);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T copy(T t) {
		ObjectMapper om = KernelJsonMapper.getMapper();
		try {
			Class<?> readClass = t.getClass();
			if (t instanceof Element && !(t instanceof DocumentModelRoot)) {
				readClass = Element.class;
			}
			if (t instanceof FieldType) {
				readClass = FieldType.class;
			}
			return (T) om.readValue(om.writeValueAsString(t), readClass);
		} catch (JacksonException e) {
			throw new RuntimeException("Error while copy the element.", e);
		}
	}

	/**
	 * see {@link #copy(DocumentModel, Set, Consumer)}
	 */
	public static DocumentModel copy(DocumentModel documentModel, Consumer<? super IProblem> problemConsumer) {
		Set<String> elementIdsInResult = new HashSet<>();
		return copy(documentModel, elementIdsInResult, problemConsumer);
	}

	/**
	 * Copies the given {@link DocumentModel} if {@code elementIdsInResult} is empty. Otherwise creates a reduced
	 * {@link DocumentModel}.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} to be copied or reduced. May not be {@code null}.
	 * @param elementIdsInResult
	 * 		the set of IDs of elements to be included in the reduced {@link DocumentModel}. May not be {@code null} but can be empty.
	 * @param problemConsumer
	 * 		notifications might be reported here if there are inconsistencies in the DM. If errors occur, this
	 * 		could possibly indicate that maybe, additional inconsistencies could be present in the resulting copy,
	 * 		indirectly caused by the original inconsistency. If strict behavior is desired, the provided
	 * 		problemConsumer should abort with an exception if any reported problem has severity of level ERROR.
	 * @return a {@link DocumentModel} which is a copy or a part of {@code documentModel}
	 */
	public static DocumentModel copy(
			DocumentModel documentModel,
			Set<String> elementIdsInResult,
			Consumer<? super IProblem> problemConsumer
	) {
		PipedWriter pipedWriter = new PipedWriter();
		try (PipedReaderExtended pipedReader = new PipedReaderExtended(pipedWriter)) {
			if (elementIdsInResult.isEmpty()) {
				SERIALIZER.serialize(documentModel, pipedWriter, problemConsumer::accept);
			} else {
				ObjectMapper mapper = KernelJsonMapper.createMapper(elementIdsInResult);
				mapper.writeValue(pipedWriter, documentModel);
			}
			pipedWriter.close();
			pipedReader.getThread().join();
			pipedReader.getException().ifPresent(e -> {
				throw new IllegalArgumentException("The given document model could not be serialized.", e);
			});
			return pipedReader.getDocumentModel();
		} catch (IOException e) {
			throw new UncheckedIOException("The given document model could not be serialized.", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalArgumentException(
					"The parallel thread used while copying the document model was unexpectedly interrupted.", e);
		}
	}

	static class PipedReaderExtended extends PipedReader implements Runnable {
		private DocumentModel copy;
		private Exception exception;
		private Thread thread;

		PipedReaderExtended(PipedWriter pipedWriter) {
			super();
			try {
				this.connect(pipedWriter);
			} catch (IOException e) {
				exception = e;
				throw new UncheckedIOException(e);
			}
			thread = new Thread(this);
			thread.start();
		}

		@Override
		public void run() {
			try {
				copy = SERIALIZER.deserialize(this);
			} catch (JacksonException e) {
				exception = e;
				throw new RuntimeException("The given document model could not be deserialized.", e);
			}
		}

		@Override
		public void close() throws IOException {
			if (this.thread != null && this.thread.isAlive()) {
				this.thread.interrupt();
			}
			super.close();
		}

		Optional<Exception> getException() {
			return Optional.ofNullable(exception);
		}

		DocumentModel getDocumentModel() {
			return copy;
		}

		Thread getThread() {
			return thread;
		}
	}

}
