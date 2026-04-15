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
package com.mgmtp.a12.kernel.md.model.internal.service.mapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationDataTable;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService.ICalculationRule;
import com.mgmtp.a12.kernel.md.model.a12internal.services.ElementContainer;

/**
 * Default implementation of {@link IEC} with added convenience functionality.
 */
public class ElementContainerImpl extends Group implements ElementContainer {
	private final Map<String, String> groupIdsByPathCache = new HashMap<>();
	private HashSetValuedHashMap<String, String> shortName2FullNameMap = null;
	private List<IRule> allRules = null;
	private List<ICalculationRule> allCalculations = null;

	public ElementContainerImpl() {
		super("<ROOT>", "/", new long[0], Collections.emptyList(), null, null, null, new HashMap<>());
	}

	@Override
	public IEntity get(final String fullName) {
		return internalGet(fullName);
	}

	private Entity internalGet(final String fullName) {
		if ("/".equals(fullName)) {
			return this;
		} else {
			final ElementIdentifier elementId;
			try {
				elementId = ElementIdentifier.createFromFullName(fullName);
			} catch (Exception e) {
				return null;
			}
			Entity result = getDescendantByPath(elementId.getPath());
			if (result == null || result.isField() || result.isGroup()) {
				return result;
			} else {
				return null;
			}
		}
	}

	@Override
	public List<IField> getAllContainedFields(final String pathToGroup) {
		final Entity entity = internalGet(pathToGroup);
		if (!(entity instanceof Group)) {
			return Collections.emptyList();
		} else {
			return entity
					.flattenToStream()
					.filter(IEntity::isField)
					.map(IField.class::cast)
					.toList();
		}
	}

	@Override
	public List<IEntity> getDirectlyContainedEntities(final String pathToGroup) {
		final Entity entity = internalGet(pathToGroup);
		if (!(entity instanceof Group group)) {
			return Collections.emptyList();
		} else {
			return group
					.getChildren()
					.stream()
					.filter(e -> e.isField() || e.isGroup())
					.collect(Collectors.toList());
		}
	}

	public List<IRule> getAllRules() {
		// We have to initialize it retarded to have the EntityContainer built up completely.
		// We assume, that the EntityContainer is not changed after using it.
		lazyInitRules();
		return allRules;
	}

	private void lazyInitRules() {
		if (allRules != null) {
			return;
		}
		allRules = this
				.flattenToStream()
				.map(entity -> {
					if (entity instanceof ICalculationRule rule) {
						return rule.getRule();
					}
					return entity;
				})
				.filter(IRule.class::isInstance)
				.map(IRule.class::cast)
				.toList();
	}

	public List<ICalculationRule> getAllCalculations() {
		// We have to initialize it retarded to have the EntityContainer built up completely.
		// We assume, that the EntityContainer is not changed after using it.
		lazyInitCalculations();
		return allCalculations;
	}

	private void lazyInitCalculations() {
		if (allCalculations != null) {
			return;
		}
		allCalculations = this
				.flattenToStream()
				.filter(ICalculationRule.class::isInstance)
				.map(ICalculationRule.class::cast)
				.toList();
	}

	@Override
	public Set<String> shortName2FullName(String shortName) {
		// We have to initialize it retarded to have the EntityContainer built up completely.
		// We assume, that the EntityContainer is not changed after using it.
		lazyInitShortName();

		return Collections.unmodifiableSet(shortName2FullNameMap.get(shortName));
	}

	@Override
	public Map<String, IEnumerationDataTable> getEnumerationTables() {
		// Currently, in A12 enumeration tables are not supported.
		return Collections.emptyMap();
	}

	private void lazyInitShortName() {
		if (shortName2FullNameMap != null) {
			return;
		}
		shortName2FullNameMap = new HashSetValuedHashMap<>();
		this.flattenToStream()
				.filter(IEntity::isField)
				.map(IEntity::getFullName)
				.forEach(fn -> shortName2FullNameMap.put(EntityUtils.getShortName(fn), fn));
	}

	public void addToGroupCache(Group group) {
		groupIdsByPathCache.put(group.getFullName(), group.getId());
	}

	public String getGroupIdByPath(String path) {
		return groupIdsByPathCache.getOrDefault(path, null);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

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
		return super.equals(obj);
	}
}
