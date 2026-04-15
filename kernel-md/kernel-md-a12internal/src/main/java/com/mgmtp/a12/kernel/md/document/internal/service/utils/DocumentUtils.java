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

package com.mgmtp.a12.kernel.md.document.internal.service.utils;

import static com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils.PATH_SEPARATOR;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IDocumentIndexed;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.internal.service.DocumentFactoryImpl;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.TransientFieldsFinder;

public class DocumentUtils {

	private DocumentUtils() {
		throw new UnsupportedOperationException("This may not be instantiated.");
	}

	/**
	 * @return the field instance with the given {@code path} and {@code repetitions}, if present.
	 */
	public static Optional<IFieldInstance> getFieldInstance(IDocument document, String path, int[] repetitions) {
		if (document instanceof IDocumentIndexed) {
			return ((IDocumentIndexed) document).getEntityInstance(path, repetitions).stream()
					.filter(IFieldInstance.class::isInstance)
					.map(IFieldInstance.class::cast)
					.findFirst();
		} else {
			return document.getEntityInstances().stream()
					.filter(IFieldInstance.class::isInstance)
					.filter(ei -> ei.getPath().equals(path))
					.filter(ei -> Arrays.equals(ei.getRepetitions(), repetitions))
					.findFirst()
					.map(IFieldInstance.class::cast);
		}
	}

	/**
	 * Adds all transient fields which are missing in the document with following logic:
	 * <ul>
	 * <li>For repeatable groups, the missing transient fields will be added to the existing group
	 * instances.</li>
	 * <li>For non-repeatable groups, the missing transient fields will be added even if the group
	 * instance is not present.</li>
	 * </ul>
	 */
	public static void addTransientFields(IDocument document, IDocumentModel documentModel, IDocumentFactory documentFactory) {
		TransientFieldsFinder tff = new TransientFieldsFinder();
		new DocumentModelWalker().acceptDocumentModel(documentModel, tff);
		Set<String> allTransientFieldPaths = tff.getAllTransientFieldPaths();
		if (allTransientFieldPaths.isEmpty()) {
			return;
		}
		Set<IFieldInstance> alreadyCreatedTransientFieldInstances = document.getEntityInstances().stream()
				.filter(IFieldInstance.class::isInstance)
				.filter(ei -> allTransientFieldPaths.contains(ei.getPath()))
				.map(IFieldInstance.class::cast)
				.collect(Collectors.toSet());
		// handle repeatable transient fields
		Map<String, Set<String>> repeatableTransientFieldsByParentPath = tff.getRepeatableTransientFieldsByParentPath();
		Set<IGroupInstance> alreadyCreatedGroupInstancesWithTransientFields = document.getEntityInstances().stream()
				.filter(IGroupInstance.class::isInstance)
				.filter(ei -> repeatableTransientFieldsByParentPath.containsKey(ei.getPath()))
				.map(IGroupInstance.class::cast)
				.collect(Collectors.toSet());
		for (IGroupInstance groupInstanceWithTransientFields : alreadyCreatedGroupInstancesWithTransientFields) {
			String groupPath = groupInstanceWithTransientFields.getPath();
			for (String transientFieldName : repeatableTransientFieldsByParentPath.get(groupPath)) {
				String transientFieldPath = String.format("%s%s%s", groupPath, PATH_SEPARATOR, transientFieldName);
				int[] repetitions = Arrays.copyOf(groupInstanceWithTransientFields.getRepetitions(),
						groupInstanceWithTransientFields.getRepetitions().length + 1);
				repetitions[repetitions.length - 1] = 1;
				addTransientFieldInstance(document, documentFactory, alreadyCreatedTransientFieldInstances, transientFieldPath,
						repetitions);
			}
		}
		// handle non-repeatable transient fields
		for (String transientFieldPath : tff.getNonRepeatableTransientFieldPaths()) {
			int[] repetitions = DocumentUtils.createRepetitions4NonRepeatable(transientFieldPath);
			addTransientFieldInstance(document, documentFactory, alreadyCreatedTransientFieldInstances, transientFieldPath,
					repetitions);
		}
	}

	private static void addTransientFieldInstance(IDocument document, IDocumentFactory documentFactory,
			Set<IFieldInstance> alreadyCreatedTransientFieldInstances, String transientFieldPath, int[] repetitions) {
		IFieldInstance transientFieldInstance = documentFactory.createFieldInstance(transientFieldPath, repetitions);
		if (!alreadyCreatedTransientFieldInstances.contains(transientFieldInstance)) {
			document.addEntityInstance(transientFieldInstance);
		}
	}

	/**
	 * Return a new {@link IDocument} instance with copies of the entity instances.
	 *
	 * @param document
	 * 		the original {@link IDocument}. Mustn't be <code>null</code>.
	 * @param documentFactory
	 * 		the {@link IDocumentFactory} to create the new document with. Mustn't be <code>null</code>.
	 * @return a new {@link IDocument} instance of document.
	 */
	public static IDocument copy(IDocument document, IDocumentFactory documentFactory) {
		IDocument result = documentFactory.createDocument(document.getDocumentModelId());
		document.getId().ifPresent(result::setId);
		document.getEntityInstances().stream()
				.map(ei -> DocumentUtils.recreateEntityInstance(ei, documentFactory))
				.forEach(result::addEntityInstance);
		return result;
	}

	/**
	 * Creates a new {@link IDocument} instance without all transient fields from the given document. If the corresponding {@link
	 * IDocumentModel} does not contain any transient field, the given {@link IDocument} instance will be returned.
	 */
	public static boolean removeTransientFields(IDocument document, IDocumentModel documentModel) {
		TransientFieldsFinder tff = new TransientFieldsFinder();
		new DocumentModelWalker().acceptDocumentModel(documentModel, tff);
		Set<String> allTransientFieldPaths = tff.getAllTransientFieldPaths();
		if (allTransientFieldPaths.isEmpty()) {
			return false;
		}
		Set<IEntityInstance> entityInstancesToDelete = document.getEntityInstances().stream()
				.filter(ei -> (ei instanceof IFieldInstance) && allTransientFieldPaths.contains(ei.getPath()))
				.collect(Collectors.toSet());
		entityInstancesToDelete.forEach(document::removeEntityInstance);
		return !entityInstancesToDelete.isEmpty();
	}

	private static IEntityInstance recreateEntityInstance(IEntityInstance entityInstance, IDocumentFactory documentFactory) {
		if (entityInstance instanceof IFieldInstance) {
			IFieldInstance newFi = documentFactory.createFieldInstance(entityInstance.getPath(), entityInstance.getRepetitions());
			(((IFieldInstance) entityInstance).getValue()).ifPresent(newFi::setValue);
			return newFi;
		} else {
			return documentFactory.createGroupInstance(entityInstance.getPath(), entityInstance.getRepetitions());
		}
	}

	/**
	 * This method should be called after having checked that the paths of the corresponding entity instances are equal.
	 * <ul>
	 * <li>ref = [1,0,1]</li>
	 * <li>test = [1,1,1] => true</li>
	 * <li>test = [1,1] => false</li>
	 * <li>test = [1,1,1,1] => true</li>
	 * <li>test = [1,2,1,1] => true</li>
	 * </ul>
	 *
	 * @return {@code true} if {@code reference} contains {@code test}, otherwise {@code false}.
	 */
	public static boolean contains(int[] reference, int[] test) {
		if (isAllSemantic(reference)) {
			return true;
		}
		if (test == null || reference.length > test.length) {
			return false;
		}
		for (int i = 0; i < reference.length; i++) {
			if (reference[i] != IEntityInstance.ALL_SEMANTIC && reference[i] != test[i]) {
				return false;
			}
		}
		return true;
	}

	private static boolean isAllSemantic(int[] repetitions) {
		return repetitions == null
				|| repetitions.length == 0
				|| isEveryRepetitionAllSemantic(repetitions);
	}

	private static boolean isEveryRepetitionAllSemantic(int[] repetitions) {
		return Arrays.stream(repetitions).allMatch(i -> i == IEntityInstance.ALL_SEMANTIC);
	}

	/**
	 * @param path
	 * 		path for which the repetitions should be created. May not be {@code null}.
	 * @return the corresponding array filled with 1's. Is never {@code null}.
	 */
	public static int[] createRepetitions4NonRepeatable(String path) {
		int[] repetitions = new int[ElementPathUtils.getNumberOfPathParts(path)];
		Arrays.fill(repetitions, 1);
		return repetitions;
	}

	/**
	 * @return {@code true} if all repetitions are greater than {@value IEntityInstance#ALL_SEMANTIC}, otherwise {@code false}.
	 */
	public static boolean areAllRepetitionsConcrete(int[] repetitions) {
		return Arrays.stream(repetitions).allMatch(i -> i > IEntityInstance.ALL_SEMANTIC);
	}

	/**
	 * @param document
	 * 		the document to remove the empty fields from.
	 * @return <code>true</code>, if the document was changed, else <code>false</code>.
	 */
	public static boolean removeEmptyFieldsAndGroups(IDocument document) {
		IDocumentFactory documentFactory = new DocumentFactoryImpl();
		Set<IEntityInstance> ancestors = new HashSet<>();
		document.getEntityInstances().stream()
				.filter(DocumentUtils::isFieldInstanceWithValue)
				.forEach(fi -> DocumentUtils.addAllParents(fi, ancestors, documentFactory));
		Set<IEntityInstance> toRemove = document.getEntityInstances().stream()
				.filter(ei -> !isFieldInstanceWithValue(ei) && !ancestors.contains(recreateEntityInstance(ei, documentFactory)))
				.collect(Collectors.toSet());
		toRemove.forEach(document::removeEntityInstance);
		return !toRemove.isEmpty();
	}

	private static boolean isFieldInstanceWithValue(IEntityInstance ei) {
		return (ei instanceof IFieldInstance fieldInstance)
				&& fieldInstance.getValue().filter(DocumentUtils::valueExists).isPresent();
	}

	private static void addAllParents(IEntityInstance fi, Set<IEntityInstance> parents, IDocumentFactory documentFactory) {
		IEntityInstance currentInstance = fi;
		while (currentInstance.getRepetitions().length > 1) {
			String parentPath = ElementPathUtils.getParentPath(currentInstance.getPath());
			int[] parentRepetitions = Arrays.copyOf(currentInstance.getRepetitions(), currentInstance.getRepetitions().length - 1);
			currentInstance = documentFactory.createGroupInstance(parentPath, parentRepetitions);
			if (!parents.add(currentInstance)) {
				break;
			}
		}
	}

	/**
	 * @return {@code true} if the given object is neither {@code null} nor {@code ""}.
	 */
	public static boolean valueExists(Object obj) {
		return obj != null
				&& !((obj instanceof String string) && string.isEmpty());
	}

	/**
	 * If the given document already contains a field instance with the specified path and repetitions, unset its value. Otherwise,
	 * do nothing.
	 */
	public static void clearFieldInstanceValue(IDocument document, String path, int[] repetitions) {
		DocumentUtils
				.getFieldInstance(document, path, repetitions)
				.filter(DocumentUtils::valueExists)
				.ifPresent(fieldInstance -> fieldInstance.setValue(null));
	}

	/**
	 * If the given document does not already contain a field instance with the specified path and repetitions, use the provided
	 * factory to create it. Set this new or retrieved field instance value to the given value.
	 */
	public static void setFieldInstanceValue(IDocument document, String path, int[] repetitions, Object value,
			IDocumentFactory documentFactory) {
		DocumentUtils.getFieldInstance(document, path, repetitions).orElseGet(() -> {
			IFieldInstance fieldInstance = documentFactory.createFieldInstance(path, repetitions);
			document.addEntityInstance(fieldInstance);
			return fieldInstance;
		}).setValue(value);
	}

	/**
	 * @param document
	 * 		the document to remove the unknown entities from.
	 * @param docModelSearchService
	 * 		the search service of the corresponding document model.
	 * @return <code>true</code>, if the document was changed, else <code>false</code>.
	 */
	public static boolean removeUnknownEntityInstances(IDocument document, IDocumentModelSearchService docModelSearchService) {
		Set<IEntityInstance> unknownEntityInstances = document.getEntityInstances().stream()
				.filter(ei -> DocumentUtils.isUnknownEntityInstance(ei, docModelSearchService))
				.collect(Collectors.toSet());
		return unknownEntityInstances.stream()
				.map(document::removeEntityInstance)
				.reduce(false, (a, b) -> a || b);
	}

	private static boolean isUnknownEntityInstance(IEntityInstance ei, IDocumentModelSearchService docModelSearchService) {
		return docModelSearchService.getByPath(ei.getPath()).isEmpty();
	}
}
