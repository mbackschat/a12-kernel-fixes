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

package com.mgmtp.a12.kernel.md.document.internal.service.impl;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.base.utils.internal.LinkedUnmodifiableSet;
import com.mgmtp.a12.kernel.md.document.api.IDocumentIndexed;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSerializer;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.document.internal.service.utils.DocumentUtils;

/**
 * This implementation is kernel internal, but has a public visibility because of
 * {@link IDocumentSerializer#deserialize(Reader, String, DocumentDeserializationConfig, Consumer)}.
 * Changes in this implementation are not breaking.
 * If the user needs an own implementation, this is possible by implementing
 * {@link IDocumentFactory} and setting that implementation when calling
 * {@link IDocumentSerializer#deserialize(Reader, String, DocumentDeserializationConfig, Consumer)}
 * (see {@link DocumentDeserializationConfig#getDocumentFactory()}).
 */
public class DocumentImpl implements IDocumentIndexed {

	private static final String NO_ALL_SEMANTIC = "Only positive repetitions greater than 0 are allowed for this case.";

	private String id;
	private final String documentModelId;
	// we want insertion order to generate deterministic results
	private Map<EntityInstanceImpl, IEntityInstance> entityInstances = new LinkedHashMap<>();
	// cache of the already returned entity instances
	private Set<IEntityInstance> returnedEntityInstances = null;
	private Path2InstancesMap path2Instances = new Path2InstancesMap();

	public DocumentImpl(String documentModelId) {
		this.documentModelId = documentModelId;
	}

	@Override
	public Optional<String> getId() {
		return Optional.ofNullable(id);
	}

	@Override
	public void setId(String id) {
		Validate.notBlank(id);
		this.id = id;
	}

	@Override
	public String getDocumentModelId() {
		return documentModelId;
	}

	@Override
	public Set<IEntityInstance> getEntityInstances() {
		if (returnedEntityInstances != null) {
			return returnedEntityInstances;
		}
		// we want insertion order to generate deterministic results
		// the returned set must not be modified afterwards
		// we cannot use LinkedHashSet because some IEntityInstance implementations are mutable
		returnedEntityInstances = new LinkedUnmodifiableSet<>(entityInstances.values());
		return returnedEntityInstances;
	}

	@Override
	public Optional<IEntityInstance> getEntityInstance(String path, int[] repetitions) {
		Validate.isTrue(DocumentUtils.areAllRepetitionsConcrete(repetitions), NO_ALL_SEMANTIC);
		return Optional.ofNullable(entityInstances.get(new EntityInstanceImpl(path, repetitions)));
	}

	@Override
	public Collection<IEntityInstance> getEntityInstancesAtPath(final String path) {
		return path2Instances.getEntityInstances(path);
	}

	@Override
	public Collection<IEntityInstance> getEntityInstancesAtOrBelowPath(final String path) {
		return path2Instances.getEntityInstancesAtOrBelowPath(path);
	}

	@Override
	public boolean addEntityInstance(IEntityInstance entityInstance) {
		Validate.isTrue(DocumentUtils.areAllRepetitionsConcrete(entityInstance.getRepetitions()), NO_ALL_SEMANTIC);
		EntityInstanceImpl internal = getInternalEi(entityInstance);
		// In this implementation, we just want to determine if an entity instance has been already added to the document
		// and for that, we only care about the path and repetitions
		if (!entityInstances.containsKey(internal)) {
			entityInstances.put(internal, entityInstance);
			returnedEntityInstances = null; // reset of the cache
			path2Instances.add(entityInstance);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeEntityInstance(IEntityInstance entityInstance) {
		Validate.isTrue(DocumentUtils.areAllRepetitionsConcrete(entityInstance.getRepetitions()), NO_ALL_SEMANTIC,
				IEntityInstance.ALL_SEMANTIC);
		EntityInstanceImpl internal = getInternalEi(entityInstance);
		if (entityInstances.containsKey(internal)) {
			entityInstances.remove(internal);
			returnedEntityInstances = null; // reset of the cache
			path2Instances.remove(entityInstance);
			return true;
		}
		return false;
	}

	private EntityInstanceImpl getInternalEi(IEntityInstance entityInstance) {
		if (entityInstance instanceof FieldInstanceImpl) {
			return ((FieldInstanceImpl) entityInstance).getInternalEi();
		} else if (entityInstance instanceof GroupInstanceImpl) {
			return ((GroupInstanceImpl) entityInstance).getInternalEi();
		} else {
			throw new IllegalArgumentException(
					String.format("While using this implementation, you must use %s and %s as well (currently used: %s).",
							FieldInstanceImpl.class.getName(), GroupInstanceImpl.class.getName(),
							entityInstance.getClass().getName()));
		}
	}

	private class Path2InstancesMap {
		private Map<String, Map<EntityInstanceImpl, IEntityInstance>> path2Instances = new HashMap<>();

		private void add(IEntityInstance ei) {
			path2Instances.computeIfAbsent(ei.getPath(), p -> new HashMap<>()).put(getInternalEi(ei), ei);

		}

		private void remove(IEntityInstance ei) {
			EntityInstanceImpl internal = getInternalEi(ei);
			if (path2Instances.containsKey(ei.getPath()) &&
					path2Instances.get(ei.getPath()).containsKey(internal)) {
				path2Instances.get(ei.getPath()).remove(internal);
			}

		}

		private Collection<IEntityInstance> getEntityInstances(final String path) {
			return path2Instances.getOrDefault(path, Map.of()).values();
		}

		private Collection<IEntityInstance> getEntityInstancesAtOrBelowPath(final String path) {
			return path2Instances.entrySet().stream()
					.filter(e -> e.getKey().startsWith(path))
					.map(Entry::getValue)
					.map(Map::values)
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
		}
	}

	/**
	 * See {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getEntityInstances() == null) ? 0 : getEntityInstances().hashCode());
		return result;
	}

	/**
	 * Please be aware of the following:
	 * <ul>
	 * <li>{@code id} (see {@link #getId()}) and {@code documentModelId} (see
	 * {@link #getDocumentModelId()}) are not considered in this implementation</li>
	 * <li>this method does not make any special handling for transient fields</li>
	 * <li>a field instance with value {@code null} is not equal to a field instance with the same
	 * path and the same repetitions but with "" (empty string) as value</li>
	 * <li>having {@code null} or "" (empty string) as value doesn't mean that the field instance
	 * doesn't exist</li>
	 * </ul>
	 * With {@link IDocumentService#removeTransientFields()} and
	 * {@link IDocumentService#removeEmptyFieldsAndGroups()}, you are able to control by your self
	 * if transient fields and field instances with {@code null} or empty value should be considered
	 * or not.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DocumentImpl other = (DocumentImpl) obj;
		Set<IEntityInstance> thisEntityInstances = getEntityInstances();
		Set<IEntityInstance> otherEntityInstance = other.getEntityInstances();
		if (thisEntityInstances == null && otherEntityInstance == null) {
			return true;
		} else if ((thisEntityInstances == null) != (otherEntityInstance == null)) {
			return false;
		} else if (!new HashSet<>(thisEntityInstances).equals(new HashSet<>(otherEntityInstance))) {
			// we convert the list-based sets to HashSet for efficient comparison
			// (both sets are expected to be instanceof LinkedUnmodifiableSet<IEntityInstance>)
			return false;
		}
		return true;
	}

}
