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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IDocumentIndexed;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.FieldInstanceImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.GroupInstanceImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.utils.DocumentUtils;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelResolverUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;

/**
 * @deprecated use {@code IDmAwareDocService} and {@code DocumentV2Utils} instead.
 */
@Deprecated(since = "30.0.0")
public class DocumentSearchServiceImpl implements IDocumentSearchService {

	private final IDocument document;
	private final IDocumentModelSearchService dmSearchService;

	public DocumentSearchServiceImpl(IDocument document, IDocumentModelResolver documentModelResolver) {
		this.document = document;
		this.dmSearchService = DocumentModelResolverUtils.getOrCreateSearchService(documentModelResolver,
				document.getDocumentModelId());
	}

	public DocumentSearchServiceImpl(IDocument document, IDocumentModelSearchService dmSearchService) {
		this.document = document;
		this.dmSearchService = dmSearchService;
	}

	@Override
	public Set<IEntityInstance> getById(String id) {
		Validate.notBlank(id);
		Optional<IElement> element = dmSearchService.getById(id);
		if (!element.isPresent() || !ElementUtils.isEntity(element.get())) {
			return new HashSet<>();
		}
		return get(ElementUtils.getPath(element.get()), null);
	}

	@Override
	public Set<IEntityInstance> get(final String path, int[] repetitions) {
		Validate.notBlank(path);
		if (IEntityInstance.PATH_SEPARATOR.equals(path)) {
			return new HashSet<>();
		}
		int numberOfPathParts = ElementPathUtils.getNumberOfPathParts(path);
		int[] repetitions4Search = getRepetitions4Search(repetitions, numberOfPathParts); // Make repetition aligned with the path

		if (document instanceof IDocumentIndexed) {
			boolean isField = dmSearchService.getByPath(path).map(IField.class::isInstance).orElse(false);
			if (isField) {
				return getFieldInstances(path, repetitions4Search, (IDocumentIndexed) document);
			} else {
				return getGroupInstances(path, repetitions4Search, numberOfPathParts, (IDocumentIndexed) document);
			}
		} else {
			return get(path, repetitions4Search, numberOfPathParts, document);
		}
	}

	private Set<IEntityInstance> get(String path, int[] repetitions4Search, int numberOfPathParts, IDocument doc) {
		Map<IEntityInstance, IEntityInstance> foundEntities = new HashMap<>(); // mapped by intern implementation because of look up
		for (IEntityInstance ei : doc.getEntityInstances()) {
			if (path.equals(ei.getPath()) && DocumentUtils.contains(repetitions4Search, ei.getRepetitions())) {
				IEntityInstance internEntityInstance = createInternEntityInstance(ei);
				foundEntities.put(internEntityInstance, ei);
			} else if (ElementPathUtils.isDescendantOrEqual(ei.getPath(), path) && DocumentUtils.contains(repetitions4Search,
					ei.getRepetitions())) {
				IEntityInstance internEntityInstance = new GroupInstanceImpl(path,
						Arrays.copyOf(ei.getRepetitions(), numberOfPathParts));
				foundEntities.computeIfAbsent(internEntityInstance, k -> internEntityInstance);
			}
		}
		return foundEntities.values().stream().collect(Collectors.toSet());
	}

	private Set<IEntityInstance> getFieldInstances(final String path, int[] repetitions4Search, IDocumentIndexed docIndexed) {
		if (DocumentUtils.areAllRepetitionsConcrete(repetitions4Search)) { // Here we search for a specific field instance
			return docIndexed.getEntityInstance(path, repetitions4Search)
					.map(Set::of)
					.orElse(Set.of());
		} else {
			// Find all fields with path equal to the given path
			return docIndexed.getEntityInstancesAtPath(path)
					.stream()
					.filter(ei -> DocumentUtils.contains(repetitions4Search, ei.getRepetitions()))
					.collect(Collectors.toSet());
		}
	}

	private Set<IEntityInstance> getGroupInstances(final String path, int[] repetitions4Search, int numberOfPathParts,
			IDocumentIndexed docIndexed) {
		/*
		 * Find all groups with path equal to the given path.
		 * For these no new GroupInstanceImpl will be created and the found object will be directly
		 * returned.
		 */
		Set<IEntityInstance> sameLevelEnitites = docIndexed.getEntityInstancesAtPath(path)
				.stream()
				.filter(ei -> DocumentUtils.contains(repetitions4Search, ei.getRepetitions()))
				.collect(Collectors.toSet());

		/*
		 * Find all non-explicitly existing groups (ghost groups) which are in effect existing
		 * because of the fields and groups below them.
		 * For these new GroupInstanceImpl will be created.
		 */
		Set<IEntityInstance> entitiesInPath =
				/*
				 * Calling the following is better than calling document.getEntityInstances()
				 * because in the worse case, when all entity instances in the document are field
				 * instances then this will become as slow as document.getEntityInstances().
				 */
				docIndexed.getEntityInstancesAtOrBelowPath(
								path + ElementUtils.PATH_SEPARATOR) // + "/" to return only those which are below the path
						.stream()
						.filter(ei -> DocumentUtils.contains(repetitions4Search, ei.getRepetitions()))
						.map(ei -> new GroupInstanceImpl(path, Arrays.copyOf(ei.getRepetitions(), numberOfPathParts)))
						.collect(Collectors.toSet());

		Set<IEntityInstance> result = new HashSet<>(sameLevelEnitites);
		result.addAll(entitiesInPath);
		return result;
	}

	@Override
	public Optional<IFieldInstance> getFieldInstance(String path, int[] repetitions) {
		return getSingularInstance(path, repetitions, IFieldInstance.class);
	}

	@Override
	public Optional<IGroupInstance> getGroupInstance(String path, int[] repetitions) {
		return getSingularInstance(path, repetitions, IGroupInstance.class);
	}

	private <E> Optional<E> getSingularInstance(String path, int[] repetitions, Class<E> clazz) {
		Validate.notBlank(path);
		Set<IEntityInstance> entityInstances = get(path, repetitions);
		if (entityInstances.isEmpty()) {
			return Optional.empty();
		}
		if (entityInstances.size() > 1) {
			throw new IllegalStateException(
					String.format("More than one entity instance was found with path='%s' and repetitions='%s'.", path,
							Arrays.toString(repetitions)));
		}
		IEntityInstance entityInstance = entityInstances.iterator().next();
		if (!clazz.isInstance(entityInstance)) {
			throw new IllegalStateException(
					String.format("The given path='%s' and repetitions='%s' point to the wrong type of entity instance.", path,
							Arrays.toString(repetitions)));
		}
		return Optional.of(clazz.cast(entityInstance));
	}

	@Override
	public Set<IEntityInstance> getDescendants(String groupPath, int[] repetitions) {
		Validate.notBlank(groupPath);
		int numberOfPathParts = ElementPathUtils.getNumberOfPathParts(groupPath);
		int[] repetitions4Search = getRepetitions4Search(repetitions, numberOfPathParts);
		// we add first the descendant instances which were explicitly added
		Map<IEntityInstance, IEntityInstance> foundEntities = document.getEntityInstances().stream()
				.filter(ei -> !groupPath.equals(ei.getPath())) // the given group should not be returned
				.filter(ei -> ElementPathUtils.isDescendantOrEqual(ei.getPath(), groupPath))
				.filter(ei -> DocumentUtils.contains(repetitions4Search, ei.getRepetitions()))
				.collect(Collectors.toMap(this::createInternEntityInstance, Function.identity()));
		// now we add the group instances which were implicitly added through the previous instances
		Set<IEntityInstance> entitiesExplicitlyAdded = new HashSet<>(foundEntities.values());
		for (IEntityInstance ei : entitiesExplicitlyAdded) {
			List<String> pathsBetween = ElementPathUtils.getPathsBetween(groupPath, ei.getPath());
			for (String pathBetween : pathsBetween) {
				int[] repetitions4Path = Arrays.copyOf(ei.getRepetitions(), ElementPathUtils.getNumberOfPathParts(pathBetween));
				IEntityInstance internEntityInstance = new GroupInstanceImpl(pathBetween, repetitions4Path);
				foundEntities.computeIfAbsent(internEntityInstance, k -> internEntityInstance);
			}
		}
		return foundEntities.values().stream().collect(Collectors.toSet());
	}

	@Override
	public Set<IEntityInstance> getChildren(String groupPath, int[] repetitions) {
		Validate.notBlank(groupPath);
		int numberOfPathParts = ElementPathUtils.getNumberOfPathParts(groupPath);
		int[] repetitions4Search = getRepetitions4Search(repetitions, numberOfPathParts);
		// we add first all children which were explicitly added
		Map<IEntityInstance, IEntityInstance> foundEntities = document.getEntityInstances().stream()
				.filter(ei -> ElementPathUtils.isChild(ei.getPath(), groupPath))
				.filter(ei -> DocumentUtils.contains(repetitions4Search, ei.getRepetitions()))
				.collect(Collectors.toMap(this::createInternEntityInstance, Function.identity()));
		// now we look for the children which were implicitly added
		document.getEntityInstances().stream()
				.filter(ei -> !groupPath.equals(ei.getPath())) // the given group should not be returned
				.filter(ei -> ElementPathUtils.isDescendantOrEqual(ei.getPath(), groupPath))
				.filter(ei -> !ElementPathUtils.isChild(ei.getPath(), groupPath))
				.filter(ei -> DocumentUtils.contains(repetitions4Search, ei.getRepetitions()))
				.forEach(ei -> {
					String childPath = ElementPathUtils.getChildPath(groupPath, ei.getPath());
					int[] childRepetitions = Arrays.copyOf(ei.getRepetitions(), numberOfPathParts + 1);
					IEntityInstance internEntityInstance = new GroupInstanceImpl(childPath, childRepetitions);
					foundEntities.computeIfAbsent(internEntityInstance, k -> internEntityInstance);
				});
		return foundEntities.values().stream().collect(Collectors.toSet());
	}

	private int[] getRepetitions4Search(int[] repetitions, int numberOfPathParts) {
		if (repetitions != null && repetitions.length > numberOfPathParts) {
			throw new IllegalArgumentException("You gave more repetitions than allowed.");
		}
		if (repetitions == null) {
			int[] newRepetitions = new int[numberOfPathParts];
			Arrays.fill(newRepetitions, 0);
			return newRepetitions;
		}
		if (repetitions.length < numberOfPathParts) {
			return Arrays.copyOf(repetitions, numberOfPathParts);
		} else {
			return repetitions;
		}
	}

	private IEntityInstance createInternEntityInstance(IEntityInstance ei) {
		// we don't clone the repetitions since the created object will be handled read-only later on
		if (ei instanceof IFieldInstance) {
			return new FieldInstanceImpl(ei.getPath(), ei.getRepetitions());
		} else {
			return new GroupInstanceImpl(ei.getPath(), ei.getRepetitions());
		}
	}

}
