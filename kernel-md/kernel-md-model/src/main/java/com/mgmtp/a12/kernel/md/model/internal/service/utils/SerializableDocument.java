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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;

import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @implNote subclasses must not add any attributes, and must only provide a DM-specific implementation of
 *        {@link #getDocumentSerializer()} and {@link #getDocumentModelId()}
 */
@ToString
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SerializableDocument implements Serializable {
	// non-final for Serializable
	@NonNull protected transient DocumentV2 document;

	@Serial @NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @implNote darf nicht auf Instanzattribute zugreifen, wird auf uninitialisiertem Objekt aufgerufen
	 */
	protected abstract IDocumentV2Serializer getDocumentSerializer();

	/**
	 * @implNote darf nicht auf Instanzattribute zugreifen, wird auf uninitialisiertem Objekt aufgerufen
	 */
	protected abstract String getDocumentModelId();

	@Serial
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();

		StringWriter stringWriter = new StringWriter();
		getDocumentSerializer().serializeV2(document, stringWriter, DocumentSerializationConfig.builder().build());
		out.writeUTF(stringWriter.toString());
	}

	@Serial
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		String serializedDocument = in.readUTF();
		StringReader stringReader = new StringReader(serializedDocument);
		try {
			document = getDocumentSerializer().deserializeV2(
					stringReader,
					getDocumentModelId(),
					DocumentDeserializationConfig.builder().build(),
					notification -> {
						assert false : "did not expect notifications during deserialization but got " + notification.getMessage();
					});
			if (document == null) {
				throw new InvalidObjectException("deserialization of document returned null");
			}
		} catch (Exception e) {
			throw new IOException("Failed to deserialize meta model document", e);
		}
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass())
			return false;
		SerializableDocument that = (SerializableDocument) object;
		return Objects.equals(document, that.document);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(document);
	}
}
