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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelResolverUtils;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAndSearchService;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.convertion.FieldValueConverter;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.json.DocumentV2JsonWriter;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.DocumentV2XmlWriter;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.simplexmlnode.XmlElement;
import com.mgmtp.a12.utils.conversion.InstantRange;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV2;

import lombok.NonNull;

public class DocumentJsonAndXmlSerializationInfoImpl
		implements DocumentV2JsonWriter.ISerializationInfo, DocumentV2XmlWriter.ISerializationInfo {
	private final @NonNull DmAndSearchService dmAndSearchService;
	private final @NonNull String fullName;
	private final @NonNull List<Integer> indexes;

	public DocumentJsonAndXmlSerializationInfoImpl(@NonNull IDocumentModelResolver documentModelResolver,
			@NonNull String documentModelId) {
		this(DmAndSearchService.of(
						documentModelResolver.getDocumentModelById(documentModelId),
						DocumentModelResolverUtils.getOrCreateSearchService(documentModelResolver, documentModelId)),
				"", List.of());
	}

	private DocumentJsonAndXmlSerializationInfoImpl(@NonNull DmAndSearchService dmAndSearchService, @NonNull String fullName,
			@NonNull List<Integer> indexes) {
		this.dmAndSearchService = dmAndSearchService;
		this.fullName = fullName;
		this.indexes = indexes;
	}

	@Override public XmlElement toXmlFieldNode(String ownName, Object fieldValue) {
		assert BasicTypeV2.hasValidType(fieldValue) : fieldValue;
		// in xml we do not have the special case of /id (like in json), because the document id is encoded not like a field
		// but as an attribute of the XML root element.
		IElement element = dmAndSearchService.searchService().getByPath(fullName)
				.orElseThrow(() -> new DocumentSerializationException("unknown field " + fullName));
		IField field;
		if (element instanceof IField fieldElement) {
			field = fieldElement;
		} else {
			throw new DocumentSerializationException(String.format("%s is not a field", fullName));
		}

		TimeZone timeZone = TimeZone.getTimeZone(dmAndSearchService.dm().getContent().getDocumentModelConfig().getTimeZone());

		try {
			String serializedFieldValue = FieldValueConverter.serializeXmlValueFromJavaTypeV2(field, timeZone, fieldValue);
			if (serializedFieldValue == null) {
				return XmlElement.of(ownName, Map.of("xsi:nil", "true"), "");
			} else {
				return XmlElement.of(ownName, serializedFieldValue);
			}
		} catch (IllegalArgumentException e) {
			throw new DocumentSerializationException(
					String.format("Could not serialize %s:%n%s", DocumentPointer.of(this.fullName, this.indexes), e.getMessage()),
					e);
		}
	}

	@Override public ValueNode toJsonFieldNode(Object fieldValue) {
		assert BasicTypeV2.hasValidType(fieldValue) : fieldValue;
		JsonNodeFactory factory = JsonNodeFactory.instance;

		if (fullName.equals("/" + DocumentSerializerImpl.ID)) {
			// Normally fieldValue is always a String, but
			// it seems that sometimes numbers are used too
			return factory.textNode(fieldValue.toString());
		}

		IElement element = dmAndSearchService.searchService().getByPath(fullName)
				.orElseThrow(() -> new DocumentSerializationException("unknown field " + fullName));
		IField field;
		if (element instanceof IField fieldElement) {
			field = fieldElement;
		} else {
			throw new DocumentSerializationException(String.format("%s is not a field", fullName));
		}

		TimeZone timeZone = TimeZone.getTimeZone(dmAndSearchService.dm().getContent().getDocumentModelConfig().getTimeZone());

		try {
			// we are not checking if the fieldValue has the correct type given by fieldElement, the user would need to validate
			return switch (fieldValue) {
				case null -> factory.nullNode();
				case String value -> factory.textNode(value);
				case Boolean value -> factory.booleanNode(value);
				case BigDecimal value -> factory.numberNode(value);
				case Instant value -> factory.textNode(FieldValueConverter.serializeXmlValueFromJavaTypeV2(field, timeZone, value));
				case InstantRange value ->
						factory.textNode(FieldValueConverter.serializeXmlValueFromJavaTypeV2(field, timeZone, value));
				default -> {
					String errorMsg = String.format("Value of '%s' from type '%s' cannot be serialized.",
							DocumentPointer.of(this.fullName, this.indexes), fieldValue.getClass().getName());
					throw new DocumentSerializationException(errorMsg);
				}
			};
		} catch (IllegalArgumentException e) {
			throw new DocumentSerializationException(
					String.format("Could not serialize %s:%n%s", DocumentPointer.of(this.fullName, this.indexes), e.getMessage()),
					e);
		}
	}

	@Override public boolean isGroupRepeatable() {
		// we expect to only have defined groups
		IGroup groupElement = (IGroup) dmAndSearchService.searchService().getByPath(fullName)
				.orElseThrow(() -> new DocumentSerializationException(
						"unknown group " + DocumentPointer.of(this.fullName, this.indexes)));
		return groupElement.getRepeatability() > 1;
	}

	@Override public DocumentJsonAndXmlSerializationInfoImpl forChild(String childName) {
		// Note: it is important that the constructor which is called here does _not_ build a new document model search
		// service, as that is a rather expensive operation, which we don't want to redundantly re-do for each group and field
		// instance in the document.
		return new DocumentJsonAndXmlSerializationInfoImpl(this.dmAndSearchService, this.fullName + "/" + childName,
				Java8Utils.appendList(this.indexes, 1));
	}

	@Override public DocumentJsonAndXmlSerializationInfoImpl atIndex(int repetitionIndex) {
		if (this.indexes.isEmpty()) {
			throw new IllegalArgumentException("Cannot set the repetition index for an empty path");
		}

		return new DocumentJsonAndXmlSerializationInfoImpl(
				this.dmAndSearchService,
				this.fullName,
				Java8Utils.changeLast(this.indexes, repetitionIndex));
	}

	@Override public String toString() {
		return "ISerializationInfo for " + fullName;
	}
}
