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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAndSearchService;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.DocumentJsonAndXmlDeserializationInfoImpl;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ValueNode;

public class DocumentV2JsonReader {
	private static final String UNEXPECTED_NODE_TYPE = "The node %s has an unexpected node type %s, our json schema requires %s in this context.";

	private DocumentV2JsonReader() {
		throw new IllegalStateException("This class should not be instantiated, use the static methods instead.");
	}

	public static DocumentV2 readDocument(@NonNull String dmId, @NonNull IDocumentModelResolver resolver,
			@NonNull JsonNode json,
			@NonNull Consumer<RankedNotification> notificationReceiver, @NonNull DocumentDeserializationConfig config) {
		DmAndSearchService dmSS = DmAndSearchService.of(dmId, resolver);
		IDeserializationInfo deserializationInfo =
				new DocumentJsonAndXmlDeserializationInfoImpl(dmSS, config, notificationReceiver);

		try {
			// A12 (SME) enforces that models have the same filename and id.
			// In our metamodels those differ, because the export includes the variant.
			// now we read the id from the dm itself and use it to store in the deserialized Document.
			return docFromJson(json, deserializationInfo,
					dmSS.dm().getHeader().getId()
			);
		} catch (IllegalArgumentException e) {
			throw new DocumentSerializationException(e);
		}
	}

	private static DocumentV2 docFromJson(@NonNull JsonNode json, @NonNull IDeserializationInfo info, @NonNull String dmId) {
		if (!(json instanceof ObjectNode objectNode)) {
			throw new IllegalArgumentException(
					String.format(UNEXPECTED_NODE_TYPE, "root object", json.getNodeType(), ObjectNode.class.getSimpleName()));
		}

		GroupInstanceV2 asGroup = groupFromJson(objectNode, info).orElseThrow(()
				-> new RuntimeException("The serializationInfo rejected the root group of the document, this should not happen"));

		Object documentIdIfPresent = asGroup.directFieldValue(DocumentSerializerImpl.ID);
		if (documentIdIfPresent != null && !(documentIdIfPresent instanceof String)) {
			throw new IllegalArgumentException("The id of the document has to be a String");
		}
		asGroup = asGroup.withFieldRemoved(DocumentSerializerImpl.ID);
		return DocumentV2.ofRootRootGroup(dmId, (String) documentIdIfPresent, asGroup);
	}

	/**
	 * @return the group, or empty if the {@link IDeserializationInfo} rejected the group (typically because it does not exist in
	 * 		the DM)
	 */
	private static Optional<GroupInstanceV2> groupFromJson(@NonNull ObjectNode json, @NonNull IDeserializationInfo info) {
		if (!info.checkIsGroup()) {
			return Optional.empty();
		}

		@NonNull Map<String, RepetitionsV2> subGroups = new HashMap<>();
		@NonNull Map<String, FieldInstanceV2> fields = new HashMap<>();
		json.forEachEntry((name, childNode) -> {
			var infoForChild = info.forChild(name);
			switch (childNode) {
				case ValueNode fieldNode -> fieldFromJson(fieldNode, infoForChild).ifPresent(value -> fields.put(name, value));
				case ArrayNode repetitionsNode ->
						repetitionsFromJson(repetitionsNode, infoForChild).ifPresent(reps -> subGroups.put(name, reps));
				case ObjectNode subgroupNode -> groupFromJson(subgroupNode, infoForChild).ifPresent(
						// can only be a non-repeatable group, as it is a named child
						group -> subGroups.put(name, RepetitionsV2.of(List.of(group))));
				default ->
					// this should not happen unless the json-serialized a12 document is malformed
					throw new IllegalArgumentException(
						String.format(UNEXPECTED_NODE_TYPE, name, childNode.getNodeType(),
								String.format("%s, %s, or %s",
										ValueNode.class.getSimpleName(), ArrayNode.class.getSimpleName(),
										ObjectNode.class.getSimpleName())));
			}
		});

		return Optional.of(GroupInstanceV2.of(subGroups, fields));
	}

	/**
	 * @return the repetitions, or empty if the {@link IDeserializationInfo} rejected the repetitions (maybe because it does
	 * 		not exist in the DM)
	 */
	private static Optional<RepetitionsV2> repetitionsFromJson(@NonNull ArrayNode repetitionsNode,
			@NonNull IDeserializationInfo info) {
		List<ObjectNode> repetitions = new ArrayList<>();

		int index = 1;
		for (Iterator<JsonNode> it = repetitionsNode.iterator(); it.hasNext(); index++) {
			var elemNode = it.next();
			if (elemNode instanceof ObjectNode groupNode) {
				repetitions.add(groupNode);
			} else {
				// this should not happen unless the json-serialized a12 document is malformed
				throw new IllegalArgumentException(
						String.format(UNEXPECTED_NODE_TYPE, "element at index " + index, elemNode.getNodeType(),
								ObjectNode.class.getSimpleName()));
			}
		}

		// now repetitions all need the same IDeserializationInfo, no need to adapt it here
		return Optional.of(RepetitionsV2.of(
				IntStream.range(1, repetitions.size() + 1)
						.mapToObj(i -> groupFromJson(repetitions.get(i - 1), info.atIndex(i)))
						.filter(Optional::isPresent).map(Optional::get)
						.toList()));
	}

	/**
	 * @return the field, or empty if the {@link IDeserializationInfo} rejected the field (typically because it does not exist in
	 * 		the DM or the value cannot be parsed)
	 */
	private static Optional<FieldInstanceV2> fieldFromJson(@NonNull ValueNode json, @NonNull IDeserializationInfo info) {
		return info.parseFieldValue(json);
	}

	/**
	 * Encapsulates and "api-minimizes" the DM-specific information that is needed for deserialization from json.
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
		 * @return {@code Optional.empty} if the field should be ommited from the resulting document (e.g. value could not be
		 * 		parsed), otherwise the parsed value.
		 */
		Optional<FieldInstanceV2> parseFieldValue(ValueNode value);

		/**
		 * Checks if a group is ok at this path. Otherwise, reports the inconsistency that an unknown element or element that is
		 * known but not a group was encountered.
		 * For use with the DocumentModel it can be checked if there is a group defined at this specific path.
		 */
		boolean checkIsGroup();

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
