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

package com.mgmtp.a12.kernel.md.model.migration.internal.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.KernelJsonMapperProvider;
import com.mgmtp.a12.model.header.ModelReference;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * Json utilities for A12 models.
 */
public class ModelJsonUtil {

	public static final String ANNOTATIONS = "annotations";
	public static final String ANNOTATION_NAME = "name";
	public static final String ANNOTATION_VALUE = "value";
	public static final String CONDITION_LANGUAGE = "conditionLanguage";
	public static final String CONDITION_LANGUAGE_CODE = "code";
	public static final String CONTENT = "content";
	public static final String COMPUTED_FIELD_ID = "computedFieldId";
	public static final String COMPUTED_FIELD_REL_PATH = "computedFieldRelPath";
	public static final String DOCUMENT_MODEL_TYPE = DocumentModel.MODEL_TYPE_DOCUMENT;
	public static final String ELEMENT_TYPE = "type";
	public static final String ERROR_ENTITY_ID = "errorEntityId";
	public static final String ERROR_ENTITY_REL_PATH = "errorEntityRelPath";
	public static final String ENUMERATION_VALUES = "values";
	public static final String FIELD_ID = "id";
	public static final String FIELD_FIELD_TYPE = "fieldType";
	public static final String FIELD_FIELD_TYPE_TYPE = "type";
	public static final String FIELD_FIELD_TYPE_TYPEDEF_TYPE = "TypeDefType";
	public static final String GROUP_ELEMENTS = "elements";
	public static final String GROUP_ID = "id";
	public static final String GROUP_SORT_FIELDS = "sortFields";
	public static final String HEADER = "header";
	public static final String INDEX_FIELD_NAME = "indexFieldName";
	public static final String INDEX_FIELD_ID = "indexFieldId";
	public static final String JSON_EXTENSION = ".json";
	public static final String MODEL_INCLUDE_CONFIG = "includeConfig";
	public static final String MODEL_ALIAS = "modelAlias";
	public static final String EXCLUDE_RULES = "excludeRules";
	public static final String EXCLUDE_COMPUTATION = "excludeComputations";
	public static final String INCLUDE_LEVEL = "includeLevel";
	public static final String MODEL_REFERENCE = "reference";
	public static final String MODEL_CONFIG = "modelConfig";
	public static final String MODEL_ID = "id";
	public static final String MODEL_PURPOSE = "purpose";
	public static final String MODEL_REFERENCES = "modelReferences";
	public static final String MODEL_ROOT = "modelRoot";
	public static final String MODEL_TYPE = "modelType";
	public static final String MODEL_VERSION = "modelVersion";
	public static final String MR_ALIAS = "alias";
	public static final String MR_REFERENCE = "reference";
	public static final String NAME = "name";
	public static final String PURPOSE_IMPORT = "typeDefinitions";
	public static final String PURPOSE_INCLUDE = "include";
	public static final String PURPOSE_TRANSITIVE_INCLUDE = "transitiveInclude"; // NOTE: this purpos is no longer set by expansion since A12K-4103
	public static final String REQUIRED = "required";
	public static final String REQUIREDNESS_CONFIG = "requirednessConfig";
	public static final String REQUIREDNESS_MODE = "mode";
	public static final String ROOT_GROUPS = "rootGroups";
	public static final String SORT_FIELD_ID = "fieldId";
	public static final String SORT_FIELD_NAME = "name";
	public static final String SUPPORTED_CHARACTERS = "supportedCharacters";
	public static final String TYPE_COMPUTATION = "Computation";
	public static final String TYPE_DEFS = "typeDefinitions";
	public static final String TYPE_DEF_ID = "id";
	public static final String TYPE_DEF_REF_ID = "typeDefinitionId";
	public static final String TYPE_FIELD = "Field";
	public static final String TYPE_GROUP = "Group";
	public static final String TYPE_RULE = "Rule";

	private ModelJsonUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static String serializeJson(JsonNode docModel) {
		StringWriter writer = new StringWriter();
		KernelJsonMapperProvider.createPreconfiguredObjectMapper().build().writeValue(writer, docModel);
		return writer.toString();
	}

	public record JsonNodeWithPath(JsonNode node, NodePath path) {
	}

	public static class JsonNodeWithPathIterator implements Iterator<JsonNodeWithPath> {
		private final Deque<JsonNodeWithPath> jsonNodeStack = new ArrayDeque<>();

		public JsonNodeWithPathIterator(JsonNode documentModel) {
			ModelJsonUtil.createRootGroupsIterable(documentModel)
					.forEach(node -> jsonNodeStack.add(new JsonNodeWithPath(node, new NodePath(node.get(NAME).asString()))));
		}

		public Iterable<JsonNodeWithPath> asIterable() {
			return () -> this;
		}

		@Override public JsonNodeWithPath next() {
			JsonNodeWithPath node = this.jsonNodeStack.removeFirst();
			JsonNode elementNode = node.node();

			// if the element is a group, add all children to the stack
			if (isGroup(elementNode) && elementNode.get(TYPE_GROUP).has(GROUP_ELEMENTS)) {
				JsonNode elements = elementNode.get(TYPE_GROUP).get(GROUP_ELEMENTS);
				IntStream.range(0, elements.size())
						.mapToObj(elements::get)
						.forEach(e -> this.jsonNodeStack.add(
								new JsonNodeWithPath(e, node.path.addSub(e.get(NAME).asString()))));
			}

			return node;
		}

		@Override public boolean hasNext() {
			return !jsonNodeStack.isEmpty();
		}
	}

	/**
	 * @param dmModel
	 * 		the json root node of a document model
	 * @param absolutePath
	 * 		an absolute path pointing to the Element to remove
	 * @return null, if no element was removed, or the removed element
	 */
	public static JsonNode tryRemoveElementFromObject(JsonNode dmModel, String absolutePath) {

		var child = ElementPathUtils.getName(absolutePath);
		if (!ElementPathUtils.isAbsolutePath(absolutePath)) {
			throw new IllegalArgumentException("Path needs to be absolute, given:" + absolutePath);
		}
		var parentPath = ElementPathUtils.getParentPath(absolutePath);

		var parentNode = StringUtils.isEmpty(parentPath) ?
				dmModel : dmModel.at(parentPath);

		if (!(parentNode instanceof ObjectNode parentObjectNode))
			return null;
		return parentObjectNode.remove(child);
	}

	/**
	 * Parse the input as json file.
	 *
	 * @param input
	 * 		path to the file
	 * @return json representing the given file
	 */
	public static JsonNode parseModel(final Path input) throws IOException {
		try (final InputStream inputStream = Files.newInputStream(input)) {
			return parseModel(inputStream);
		}
	}

	/**
	 * @return {@code null} if the given path does not point to a valid A12 model, otherwise the parsed A12 model.
	 */
	public static Optional<JsonNode> parseValidA12Model(final Path input) {
		JsonNode model = null;
		try {
			model = parseModel(input);
		} catch (IOException | JacksonException e) {
			return Optional.empty();
		}
		if (!isValidA12Model(model)) {
			return Optional.empty();
		}
		return Optional.of(model);
	}

	public static JsonNode parseModel(final InputStream inputStream) {
		return KernelJsonMapperProvider.createPreconfiguredObjectMapper().build().readTree(inputStream);
	}

	public static JsonNode parseModel(final String docModel) {
		return KernelJsonMapperProvider.createPreconfiguredObjectMapper().build().readTree(docModel);
	}

	public static Set<JsonNode> extractReferencesToDms(JsonNode documentModel) {
		JsonNode modelReferences = documentModel.get(HEADER).get(MODEL_REFERENCES);
		if (modelReferences == null || !modelReferences.isArray()) {
			return Set.of();
		}
		Set<JsonNode> referencedDms = new HashSet<>();
		for (JsonNode referencedModel : modelReferences) {
			JsonNode modelType = referencedModel.get(MODEL_TYPE);
			if (modelType != null && modelType.asString().equals(DOCUMENT_MODEL_TYPE)) {
				referencedDms.add(referencedModel);
			}
		}
		return referencedDms;
	}

	public static Set<JsonNode> extractReferencesToIncludedDms(JsonNode documentModel, boolean useTransitiveIncludes) {
		final Supplier<Stream<String>> purposeStream;
		if (useTransitiveIncludes) {
			purposeStream = () -> Stream.of(PURPOSE_INCLUDE, PURPOSE_TRANSITIVE_INCLUDE);
		} else {
			purposeStream = () -> Stream.of(PURPOSE_INCLUDE);
		}

		return extractReferencesToDms(documentModel).stream()
				.filter(jsonNode -> purposeStream.get()
						.anyMatch(purpose -> purpose.equals(
								Optional.ofNullable(jsonNode.get(ModelJsonUtil.MODEL_PURPOSE)).map(JsonNode::asString)
										.orElse(null)
						)))
				.collect(Collectors.toSet());
	}

	/**
	 * @return {@code true} iff the purpose of the model reference is supported by Kernel (include or import). Otherwise {@code
	 * 		false}.
	 */
	public static boolean isSupportedModelReference(JsonNode modelReference) {
		JsonNode purposeNode = modelReference.get(MODEL_PURPOSE);
		if (purposeNode == null || !purposeNode.isString()) {
			return false;
		}
		String purpose = purposeNode.asString();
		return PURPOSE_INCLUDE.equals(purpose) || PURPOSE_IMPORT.equals(purpose);
	}

	/**
	 * @return the reference defined in the model reference (see {@link ModelReference#getReference()}, or {@code null} if not
	 * 		defined.
	 */
	public static String extractReferenceFromModelReference(JsonNode modelReference) {
		JsonNode referenceNode = modelReference.get(MR_REFERENCE);
		if (referenceNode == null || !referenceNode.isString()) {
			return null;
		}
		return referenceNode.asString();
	}

	/**
	 * @return the alias defined in the model reference (see {@link ModelReference#getAlias()}, or {@code null} if not
	 * 		defined.
	 */
	public static String extractAliasFromModelReference(JsonNode modelReference) {
		JsonNode referenceNode = modelReference.get(MR_ALIAS);
		if (referenceNode == null || !referenceNode.isString()) {
			return null;
		}
		return referenceNode.asString();
	}

	public static Map<String, String> createIncludedDmReferenceByAlias(JsonNode model, boolean useTransitiveIncludes) {
		return ModelJsonUtil.extractReferencesToIncludedDms(model, useTransitiveIncludes).stream()
				.filter(idm -> ModelJsonUtil.extractReferenceFromModelReference(idm) != null)
				.collect(Collectors.toMap(ModelJsonUtil::extractAliasFromModelReference,
						// cannot be null as we filter those out
						ModelJsonUtil::extractReferenceFromModelReference));
	}

	public static Set<JsonNode> extractTypeDefs(JsonNode documentModel) {
		JsonNode typeDefsNode = documentModel.get(CONTENT).get(TYPE_DEFS);
		if (!(typeDefsNode instanceof ArrayNode typeDefsArray)) {
			return Set.of();
		}
		return IntStream.range(0, typeDefsArray.size())
				.mapToObj(typeDefsArray::get)
				.collect(Collectors.toSet());
	}

	public static String getTypeDefId(JsonNode typeDef) {
		return typeDef.get(TYPE_DEF_ID).asString();
	}

	public static void setTypeDefId(JsonNode typeDef, String value) {
		((ObjectNode) typeDef).set(TYPE_DEF_ID, new StringNode(value));
	}

	/**
	 * @return {@code true} if the document model references at least other document model, otherwise {@code false}.
	 */
	public static boolean referencesOtherDm(JsonNode documentModel) {
		return !extractReferencesToDms(documentModel).isEmpty();
	}

	public static boolean containsHeaderAndContent(JsonNode model) {
		JsonNode header = model.get(HEADER);
		JsonNode content = model.get(CONTENT);
		return header != null && !header.isNull() && header.isObject()
				&& content != null && !content.isNull() && content.isObject();
	}

	public static boolean isValidA12Model(JsonNode model) {
		if (!containsHeaderAndContent(model)) {
			return false;
		}
		JsonNode header = model.get(HEADER);
		// we check that the required fields are there and are not "null"
		return header.get(MODEL_ID) != null && header.get(MODEL_ID).isString()
				&& header.get(MODEL_TYPE) != null && header.get(MODEL_TYPE).isString()
				&& header.get(MODEL_VERSION) != null && header.get(MODEL_VERSION).isString();
	}

	public static boolean isValidDocumentModel(JsonNode model) {
		if (!ModelJsonUtil.containsHeaderAndContent(model)) {
			return false;
		}
		JsonNode header = model.get(HEADER);
		JsonNode modelType = header.get(MODEL_TYPE);
		return modelType != null && modelType.isString() && DOCUMENT_MODEL_TYPE.equals(modelType.asString());
	}

	/**
	 * Precondition: This method can be called after having called {@link #isValidA12Model(JsonNode)} or
	 * {@link #isValidDocumentModel(JsonNode)}.
	 */
	public static String extractModelId(JsonNode model) {
		return model.get(HEADER).get(MODEL_ID).asString();
	}

	/**
	 * Precondition: This method can be called after having called {@link #isValidA12Model(JsonNode)} or
	 * {@link #isValidDocumentModel(JsonNode)}.
	 */
	public static String extractModelVersion(JsonNode model) {
		return model.get(HEADER).get(MODEL_VERSION).asString();
	}

	/**
	 * Precondition: This method can be called after having called {@link #isValidDocumentModel(JsonNode)}.
	 */
	public static JsonNode extractDmConfig(JsonNode dm) {
		return dm.get(CONTENT).get(MODEL_CONFIG);
	}

	/**
	 * The return of Optional.empty means that no supported locale (de_DE or en_US) was given.
	 */
	public static Optional<Locale> extractConditionLanguage(JsonNode dm) {
		JsonNode config = extractDmConfig(dm);
		if (config instanceof ObjectNode && config.get(CONDITION_LANGUAGE) instanceof ObjectNode conditionLanguage) {
			return switch (conditionLanguage.path(CONDITION_LANGUAGE_CODE).asString()) {
				case "de_DE" -> Optional.of(Locale.GERMANY);
				case "en_US" -> Optional.of(Locale.US);
				default -> Optional.empty();
			};
		}
		// Fallback for unset property
		return Optional.of(Locale.US);
	}

	public static Optional<Integer> extractBaseYearIfPresent(ObjectNode model) {
		var by = model.path(CONTENT).path("modelInfo").path("baseYear");
		if (by.isNumber()) {
			return Optional.of(by.asInt());
		} else {
			return Optional.empty();
		}
	}

	public static List<JsonNode> extractTypeDefTypes(JsonNode documentModel) {
		Iterable<JsonNode> iterable = createRootGroupsIterable(documentModel);
		return StreamSupport.stream(iterable.spliterator(), false)
				.flatMap(rootGroup -> extractTypeDefTypesInGroup(rootGroup).stream())
				.toList();
	}

	private static List<JsonNode> extractTypeDefTypesInGroup(JsonNode jsonNode) {
		// Note: typeDefs can only be referenced in fields
		if (isField(jsonNode) && isTypeDefType(jsonNode)) {
			return List.of(jsonNode.get(TYPE_FIELD).get(FIELD_FIELD_TYPE).get(FIELD_FIELD_TYPE_TYPEDEF_TYPE));
		}

		if (isGroup(jsonNode) && jsonNode.get(TYPE_GROUP).has(GROUP_ELEMENTS)) {
			JsonNode elements = jsonNode.get(TYPE_GROUP).get(GROUP_ELEMENTS);
			return IntStream.range(0, elements.size())
					.mapToObj(elements::get)
					.flatMap(e -> extractTypeDefTypesInGroup(e).stream())
					.toList();
		}

		return List.of();
	}

	public static boolean isGroup(JsonNode jsonNode) {
		return jsonNode.get(ELEMENT_TYPE).asString().equals(TYPE_GROUP);
	}

	private static boolean isTypeDefType(JsonNode field) {
		return field.get(TYPE_FIELD).get(FIELD_FIELD_TYPE).get(FIELD_FIELD_TYPE_TYPE).asString()
				.equals(FIELD_FIELD_TYPE_TYPEDEF_TYPE);
	}

	private static boolean isField(JsonNode jsonNode) {
		return jsonNode.get(ELEMENT_TYPE).asString().equals(TYPE_FIELD);
	}

	/**
	 * Root groups are included and this method works for typeDef models as well.
	 */
	public static List<JsonNode> extractAllGroups(JsonNode documentModel) {
		return extractAllElements(documentModel, TYPE_GROUP);
	}

	/**
	 * This method works for typeDef models as well.
	 */
	public static List<JsonNode> extractAllFields(JsonNode documentModel) {
		return extractAllElements(documentModel, TYPE_FIELD);
	}

	public static List<JsonNodeWithPath> extractAllFieldsWithPath(JsonNode documentModel) {
		return extractAllElementsWithPath(documentModel, TYPE_FIELD);
	}

	/**
	 * This method works for typeDef models as well.
	 *
	 * @param elementType
	 * 		type of the element, i.e. "Group", "Field", "Rule" or "Computation".
	 * @return all elements with the given type
	 */
	public static List<JsonNode> extractAllElements(JsonNode documentModel, String elementType) {
		List<JsonNode> result = new ArrayList<>();
		Iterable<JsonNode> iterable = createRootGroupsIterable(documentModel);
		StreamSupport.stream(iterable.spliterator(), false)
				.forEach(rg -> extractElementsAndAddThemToResult(rg, elementType, result));
		return result;
	}

	/**
	 * This method works for typeDef models as well.
	 *
	 * @param elementType
	 * 		type of the element, i.e. "Group", "Field", "Rule" or "Computation".
	 * @return the list of matching {@code JsonNode} and their absolute Path
	 */
	public static List<JsonNodeWithPath> extractAllElementsWithPath(JsonNode documentModel, String elementType) {
		Iterable<JsonNodeWithPath> iterable = new JsonNodeWithPathIterator(documentModel).asIterable();

		return StreamSupport.stream(iterable.spliterator(), false).filter(node -> {
			JsonNode elementTypeNode = node.node().get(ELEMENT_TYPE);
			if (elementTypeNode == null || elementTypeNode instanceof NullNode || !elementTypeNode.isString()) {
				return false;
			}
			return elementTypeNode.asString().equals(elementType);
		}).toList();
	}

	private static void extractElementsAndAddThemToResult(JsonNode elementNode, String searchedElementType, List<JsonNode> result) {
		JsonNode elementTypeNode = elementNode.get(ELEMENT_TYPE);
		if (elementTypeNode == null || elementTypeNode instanceof NullNode || !elementTypeNode.isString()) {
			// we cannot process this node
			return;
		}
		if (elementTypeNode.asString().equals(searchedElementType)) {
			result.add(elementNode);
		}
		if (isGroup(elementNode) && elementNode.get(TYPE_GROUP).has(GROUP_ELEMENTS)) {
			JsonNode elements = elementNode.get(TYPE_GROUP).get(GROUP_ELEMENTS);
			IntStream.range(0, elements.size())
					.mapToObj(elements::get)
					.forEach(e -> extractElementsAndAddThemToResult(e, searchedElementType, result));
		}
	}

	@FunctionalInterface
	public interface IsUnexpanded {
		boolean apply(JsonNode group);
	}

	/**
	 * Note that the json node 'modelAlias' has been removed in DM version 29.0.0. For newer versions, use
	 * {@link #extractUnexpandedIncludingGroupsByIncludeConfig } instead
	 */
	public static List<JsonNode> extractUnexpandedIncludingGroupsByModelAlias(JsonNode documentModel) {
		IsUnexpanded groupIsUnexpandedIncludingGroup =
				group -> group.get(TYPE_GROUP).has(MODEL_ALIAS);

		return extractUnexpandedIncludingGroupsByIncludeConfig(documentModel, groupIsUnexpandedIncludingGroup);
	}

	public static List<JsonNode> extractUnexpandedIncludingGroupsByIncludeConfig(JsonNode documentModel) {
		IsUnexpanded groupIsUnexpandedIncludingGroup =
				group -> group.get(TYPE_GROUP).has(MODEL_INCLUDE_CONFIG);

		return extractUnexpandedIncludingGroupsByIncludeConfig(documentModel, groupIsUnexpandedIncludingGroup);
	}

	private static List<JsonNode> extractUnexpandedIncludingGroupsByIncludeConfig(JsonNode documentModel,
			IsUnexpanded groupPredicate) {
		Iterable<JsonNode> iterable = createRootGroupsIterable(documentModel);
		return StreamSupport.stream(iterable.spliterator(), false)
				.flatMap(rootGroup -> extractUnexpandedIncludingGroupsInGroup(rootGroup,
						groupPredicate).stream())
				.toList();
	}

	private static Iterable<JsonNode> createRootGroupsIterable(JsonNode documentModel) {
		Iterator<JsonNode> iterator = Optional.ofNullable(documentModel.get(CONTENT).get(MODEL_ROOT))
				.map(modelRoot -> modelRoot.get(ROOT_GROUPS))
				.map(JsonNode::iterator)
				.orElse(Collections.emptyIterator());
		return () -> iterator;
	}

	private static List<JsonNode> extractUnexpandedIncludingGroupsInGroup(JsonNode group, IsUnexpanded isUnexpanded) {

		if (isGroup(group)) {
			if (isUnexpanded.apply(group)) {
				return List.of(group);
			} else if (group.get(TYPE_GROUP).has(GROUP_ELEMENTS)) {
				JsonNode elements = group.get(TYPE_GROUP).get(GROUP_ELEMENTS);
				return IntStream.range(0, elements.size())
						.mapToObj(elements::get)
						.flatMap(e -> extractUnexpandedIncludingGroupsInGroup(e, isUnexpanded).stream())
						.toList();
			}
		}
		return List.of();
	}

	/**
	 * Note that the json node 'modelAlias' has been removed in DM version 29.0.0. For newer versions, use
	 * {@link #getModelReference } instead
	 */
	public static String getModelAlias(JsonNode group) {
		return group.get(TYPE_GROUP).get(MODEL_ALIAS).asString();
	}

	public static String getModelReference(JsonNode unexpandedIncludingGroup) {
		return unexpandedIncludingGroup.get(TYPE_GROUP).get(MODEL_INCLUDE_CONFIG).get(MR_REFERENCE).asString();
	}

	public static String getTypeDefRefId(JsonNode typeDefType) {
		return typeDefType.get(TYPE_DEF_REF_ID).asString();
	}

	public static void setTypeDefRefId(JsonNode model, String value) {
		((ObjectNode) model).set(TYPE_DEF_REF_ID, new StringNode(value));
	}

	public static String extractGroupId(JsonNode group) {
		return group.get(GROUP_ID).asString();
	}

	/**
	 * @return all 'elements' nodes in the document model
	 */
	public static List<JsonNode> extractAllElementsNodes(JsonNode jsonNode) {
		List<JsonNode> currentNodes = jsonNode.findValues(GROUP_ELEMENTS);
		List<JsonNode> allElementsNodes = new ArrayList<>(currentNodes);
		for (JsonNode currentNode : currentNodes) {
			allElementsNodes.addAll(extractAllElementsNodes(currentNode));
		}
		return allElementsNodes;
	}

	public static void replaceProperty(ObjectNode node, String propertyToReplace, String propertyName, JsonNode value) {
		Map<String, JsonNode> newChildren = new LinkedHashMap<>();
		var iter = node.properties().iterator();
		while (iter.hasNext()) {
			var entry = iter.next();
			if (entry.getKey().equals(propertyToReplace)) {
				newChildren.put(propertyName, value);
			} else {
				newChildren.put(entry.getKey(), entry.getValue());
			}
		}
		node.removeAll();
		node.setAll(newChildren);
	}

	/**
	 * sets the model version (useful for migration tests)
	 *
	 */
	public static void setVersion(JsonNode modelNode, String version) {
		modelNode.get(ModelJsonUtil.HEADER).asObject().put(ModelJsonUtil.MODEL_VERSION, version);
	}
}
