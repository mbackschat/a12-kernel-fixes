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

package com.mgmtp.a12.kernel.md.model.internal.service.expansion;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;

/**
 * This class manages the type definitions which are gathered while expanding a document model.
 */
public class TypeDefCache {

	private final Map<String, FieldTypeDefinition> typeDefsById = new HashMap<>();

	/**
	 * Use this method to add a type definition whose id should be uniques in the scope of {@code this} instance.
	 *
	 * @param typeDef
	 * 		type definition to add. Must not be {@code null}.
	 * @throws IllegalArgumentException
	 * 		if the type definition id already exits.
	 */
	public void addUniqueTypeDef(FieldTypeDefinition typeDef) {
		if (typeDefsById.containsKey(typeDef.getId())) {
			throw new IllegalArgumentException("The type definition id '" + typeDef.getId() + "' already exists.");
		}
		typeDefsById.put(typeDef.getId(), typeDef);
	}

	/**
	 * If {@code typeDef} is already cached, all the model reference paths will be added to the already cached type definition,
	 * otherwise it will be cached.
	 *
	 * @param typeDef
	 * 		type definition to add. Must not be {@code null}.
	 */
	public void addTypeDef(FieldTypeDefinition typeDef) {
		if (typeDefsById.containsKey(typeDef.getId())) {
			FieldTypeDefinition cachedTypeDef = typeDefsById.get(typeDef.getId());
			typeDef.getAllModelReferencePaths().forEach(cachedTypeDef::addModelReferencePath);
		} else {
			typeDefsById.put(typeDef.getId(), typeDef);
		}
	}

	/**
	 * Caches all given type definition based on the logic from {@link #addTypeDef(FieldTypeDefinition)}.
	 *
	 * @param typeDefs
	 * 		type definitions to add. Must not be {@code null}.
	 */
	public void addTypeDefs(Collection<FieldTypeDefinition> typeDefs) {
		typeDefs.forEach(this::addTypeDef);
	}

	public List<FieldTypeDefinition> getOrderedTypeDefs() {
		return typeDefsById.values().stream()
				.sorted(Comparator.comparing(FieldTypeDefinition::getId))
				.toList();
	}
}
