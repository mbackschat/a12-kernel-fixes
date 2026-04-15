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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml;

import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.mapMapValues;
import static com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl.error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAndSearchService;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.DocumentJsonAndXmlDeserializationInfoImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.simplexmlnode.XmlElement;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

public class DocumentV2XmlReader {

	private DocumentV2XmlReader() {
		throw new IllegalStateException("This class should not be instantiated, use the static methods instead.");
	}

	public static DocumentV2 readDocument(@NonNull String dmId, @NonNull IDocumentModelResolver resolver,
			@NonNull XmlElement xml,
			@NonNull Consumer<RankedNotification> notificationReceiver, @NonNull DocumentDeserializationConfig config) {
		DmAndSearchService dmSS = DmAndSearchService.of(dmId, resolver);
		IDeserializationInfo deserializationInfo =
				new DocumentJsonAndXmlDeserializationInfoImpl(dmSS, config, notificationReceiver);
		// A12 (SME) enforces that models have the same filename and id.
		// In our metamodels those differ, because the export includes the variant.
		// now we read the id from the dm itself and use it to store in the deserialized Document.
		return docFromXml(
				xml, deserializationInfo,
				dmSS.dm().getHeader().getId()
		);
	}

	public static DocumentV2 docFromXml(@NonNull XmlElement xml, @NonNull IDeserializationInfo info, @NonNull String dmId) {
		GroupInstanceV2 asGroup = groupFromXml(xml, info).orElseThrow(()
				-> new RuntimeException("the serializationInfo rejected the root group of the document, this should not happen"));

		String documentIdIfPresent = xml.attributes().get(DocumentSerializerImpl.ID);

		// note: xml.name() typically holds the dm ID, but we _don't_ check it here because we _want_ to allow deserialization
		// with a different (compatible) dm
		return DocumentV2.ofRootRootGroup(dmId, documentIdIfPresent, asGroup);
	}

	private static Optional<GroupInstanceV2> groupFromXml(@NonNull XmlElement xml, @NonNull IDeserializationInfo info) {
		// our expectation is that the text content for groups is empty (not even blank),
		// but we can be a bit more lenient to support more (wrongly) serialized documents.
		if (!StringUtils.isBlank(xml.textContent())) {
			info.notify(path -> error(
					"The data at " + path + " does not encode a valid group, as the text content is non-empty: " + xml));
			return Optional.empty();
		}

		@NonNull Map<String, List<GroupInstanceV2>> subGroups = new HashMap<>();
		@NonNull Map<String, FieldInstanceV2> fields = new HashMap<>();
		xml.children().forEach(child -> {
			String name = child.name();
			var infoForChild = info.forChild(name);
			// the xml representation doesn't reliably disambiguate groups and fields, so we must ask the DM / IDeserializationInfo
			switch (infoForChild.checkElementType()) {
				case GROUP:
					groupFromXml(child,
							infoForChild.atIndex(Optional.ofNullable(subGroups.get(name)).map(List::size).orElse(0) + 1))
							.ifPresent(childGroupSingleRep ->
									subGroups.computeIfAbsent(name, key -> new ArrayList<>()).add(childGroupSingleRep));
					break;
				case FIELD:
					fieldFromXml(child, infoForChild).ifPresent(value -> fields.put(name, value));
					break;
				case INVALID: // nothing to convert, inconsistency has already been reported by checkElementType()
					break;
			}
		});

		return Optional.of(GroupInstanceV2.of(
				mapMapValues(subGroups, RepetitionsV2::of).entrySet().stream()
						.map(e -> Pair.of(e.getKey(), e.getValue()))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
				fields));
	}

	private static Optional<FieldInstanceV2> fieldFromXml(@NonNull XmlElement element, @NonNull IDeserializationInfo info) {
		return info.parseFieldValue(element);
	}

	/**
	 * Encapsulates and "api-minimizes" the DM-specific information that is needed for deserialization from xml.
	 * <p/>
	 * Instances of this class are specific to a particular path in the DM/document (because dm-specific info like isGroup or the
	 * field type obviously depends on the path in the dm), {@link #forChild(String)} derives an instance with an appended path
	 * part, for a child element.</br>
	 * <p/>
	 * Also bundled in this interface is the notification receiver for error reporting, mainly because some of the occurring
	 * error messages are specific to the information from the DM.
	 */
	public interface IDeserializationInfo {
		/**
		 * Converts the value to a FieldInstanceV2 with value.
		 * The implementation chooses how and if a value can be converted.
		 *
		 * @return {@code Optional.empty} if the field should be omitted from the resulting document (e.g. value could not be
		 * 		parsed), otherwise the parsed value.
		 */
		Optional<FieldInstanceV2> parseFieldValue(XmlElement fieldElement);

		/**
		 * Tells whether the element at the path of this IDeserializationInfo is a group, field, or neither (e.g. a rule). In the
		 * latter case,
		 * a consistency error is reported (either about the entity being unknown, or being known but neither a field nor group)
		 */
		ElementType checkElementType();

		enum ElementType {
			FIELD,
			GROUP,
			/**
			 * unknown element, or known but neither a field nor a group (e.g. a rule)
			 */
			INVALID
		}

		/**
		 * Create a new info with the child elements name.
		 */
		IDeserializationInfo forChild(String childName);

		/**
		 * Changes the info to have a specific repetitionIndexOnlyRelevantForErrorReporting.
		 */
		IDeserializationInfo atIndex(int repetitionIndexOnlyRelevantForErrorReporting);

		/**
		 * Formats the given notification template by passing the fullName/pointer of this IDeserializationInfo to the given
		 * function, and reports the resulting RankedNotification to the notification receiver of this IDeserializationInfo.
		 */
		void notify(Function<String, RankedNotification> rankedNotification);
	}
}
