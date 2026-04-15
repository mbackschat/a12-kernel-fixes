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

package com.mgmtp.a12.kernel.md.model.a12internal.services;

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Group.IncludeDetails;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.CollectGroupsWithIncludeVisitor;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.SerializationUtils;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.model.header.HeaderFactory;
import com.mgmtp.a12.model.header.ModelReference;

/**
 * Offers the copy functionality (deep copy) for:
 * <ul>
 * <li>{@link DocumentModel}: see {@link DocumentModelCopyService#copy(DocumentModel)}</li>
 * <li>{@link Element}: see {@link #copy(Element)}</li>
 * <li>{@link FieldTypeDefinition}: see {@link #copy(FieldTypeDefinition)}</li>
 * </ul>
 * <b>Note:</b> These methods use internally the serialization offered at
 * {@link DocumentModelSerializer} and throw an {@link UncheckedIOException} if the given object
 * could not be (de)serialized.
 */
@OnlyForUsage
public class DocumentModelCopyService {
	public DocumentModelCopyService() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * like {@link #copy(DocumentModel, Consumer)}, but inconsistencies are not reported but ignored
	 */
	public static DocumentModel copy(DocumentModel documentModel) {
		return copy(documentModel, p -> {});
	}

	/**
	 * Copies the given {@link DocumentModel} (for more information see
	 * {@link DocumentModelCopyService}).
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} to be copied. May not be {@code null}.
	 * @param problemConsumer notifications might be reported here if there are inconsistencies in the DM. If errors occur,
	 *                       this could possibly indicate that maybe, additional inconsistencies could be present in the
	 *                       resulting copy, indirectly caused by the original inconsistency. If strict behavior is desired,
	 *                       the provided problemConsumer should abort with an exception if any reported problem has severity
	 *                       of level ERROR.
	 * @return the copied {@link DocumentModel}.
	 */
	public static DocumentModel copy(DocumentModel documentModel, Consumer<? super IProblem> problemConsumer) {
		DocumentModel copy = SerializationUtils.copy(documentModel, problemConsumer);
		transferIncludeDetailsToCopy(documentModel.getContent().getModelRoot(), copy.getContent().getModelRoot());
		transferTypeDefModelReferencesToCopy(documentModel.getContent().getTypeDefinitions(),
				copy.getContent().getTypeDefinitions());
		return copy;
	}

	/**
	 * Copies the given {@link FieldTypeDefinition} (for more information see
	 * {@link DocumentModelCopyService}).
	 *
	 * @param fieldTypeDefinition
	 * 		the {@link FieldTypeDefinition} to be copied. May not be {@code null}.
	 * @return the copied {@link FieldTypeDefinition}.
	 */
	public static FieldTypeDefinition copy(FieldTypeDefinition fieldTypeDefinition) {
		return SerializationUtils.copy(fieldTypeDefinition);
	}

	/**
	 * Copies the given {@link Element} (for more information see {@link DocumentModelCopyService})
	 *
	 * @param <T>
	 * 		an extension of {@link Element}: {@link Group}, {@link Field}, {@link Rule} or
	 *        {@link Computation}.
	 * @param t
	 * 		the {@link Element} to be copied. May not be {@code null}.
	 * @return the copied {@link Element}.
	 */
	public static <T extends Element> T copy(T t) {
		T copy = SerializationUtils.copy(t);
		copy.setParent(null);
		if (t instanceof Group group) {
			transferIncludeDetailsToCopy(group, (Group) copy);
		}
		return copy;
	}

	private static IncludeDetails copyIncludeDetails(Group originalGroupWithIncludeDetails) {
		IncludeDetails includeDetails = originalGroupWithIncludeDetails
				.getIncludeDetails()
				.orElseThrow(() -> new IllegalArgumentException(
						"Group " + originalGroupWithIncludeDetails.getId() + " does not contain include details."));
		ModelReference modelReference = copy(includeDetails.getModelReference());
		return IncludeDetails.builder()
				.modelReference(modelReference)
				.includeLevel(includeDetails.getIncludeLevel())
				.excludeRules(includeDetails.isExcludeRules())
				.excludeComputations(includeDetails.isExcludeComputations())
				.build();
	}

	private static void transferIncludeDetailsToCopy(Group group, Group copy) {
		CollectGroupsWithIncludeVisitor visitor = new CollectGroupsWithIncludeVisitor();
		new DocumentModelWalker().acceptElements(Collections.singletonList(group), visitor);
		List<Group> originalDescendantsWithIncludeDetails = visitor.getCollectedGroups();
		if (originalDescendantsWithIncludeDetails.isEmpty()) {
			return;
		}

		DocumentModelSearchService searchService4Copy = new DocumentModelSearchService(Collections.singletonList(copy));
		for (Group originalDescendantWithIncludeDetails : originalDescendantsWithIncludeDetails) {
			Group descendantInCopy = (Group) searchService4Copy
					.getById(originalDescendantWithIncludeDetails.getId())
					.orElseThrow(() -> new IllegalArgumentException(
							"Copied group with id " + originalDescendantWithIncludeDetails.getId() + " not found."));
			IncludeDetails includeDetailsCopy = copyIncludeDetails(originalDescendantWithIncludeDetails);
			descendantInCopy.setIncludeDetails(includeDetailsCopy);
		}
	}

	private static ModelReference copy(ModelReference modelReference) {
		return HeaderFactory.createModelReference(modelReference.getPurpose(),
				modelReference.getModelType(),
				modelReference.getAlias(),
				modelReference.getReference());
	}

	private static void transferTypeDefModelReferencesToCopy(List<FieldTypeDefinition> original, List<FieldTypeDefinition> copy) {
		Map<String, Set<List<ModelReference>>> originalModelRefPathsByTypeDefId = original.stream()
				.filter(ftd -> !ftd.getAllModelReferencePaths().isEmpty())
				.collect(Collectors.toMap(FieldTypeDefinition::getId, FieldTypeDefinition::getAllModelReferencePaths));
		for (FieldTypeDefinition copiedFieldTypeDef : copy) {
			if (!originalModelRefPathsByTypeDefId.containsKey(copiedFieldTypeDef.getId())) {
				continue;
			}
			Set<List<ModelReference>> originalModelRefPaths = originalModelRefPathsByTypeDefId.get(
					copiedFieldTypeDef.getId());
			Set<List<ModelReference>> copiedModelRefPaths = originalModelRefPaths.stream()
					.map(list -> list.stream().map(DocumentModelCopyService::copy).toList())
					.collect(Collectors.toSet());
			copiedFieldTypeDef.setAllModelReferencePaths(copiedModelRefPaths);
		}
	}

}
