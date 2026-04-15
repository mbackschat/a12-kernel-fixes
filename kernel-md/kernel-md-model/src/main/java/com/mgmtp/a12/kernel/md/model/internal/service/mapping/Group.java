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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ISortField;
import com.mgmtp.a12.model.header.Annotation;

import lombok.Getter;

/**
 * Default implementation of
 * {@link IGroup} which maps the properties of
 * {@link com.mgmtp.a12.kernel.md.model.internal.service.mapping.Group}.
 */
public class Group extends Entity implements IGroup {

	@Getter private final List<Entity> children;
	@Getter private final Set<String> indexFieldNames;
	private final String usageType;
	@Getter private final Map<String, List<ISortField>> sortInfo;

	public Group(
			final String name,
			final String path,
			final long[] maxIndices,
			final List<Annotation> annotations,
			final String groupId,
			final String indexFieldName,
			final String usageType,
			final Map<String, List<ISortField>> sortInfo) {
		super(name, path, maxIndices, annotations, groupId);
		this.children = new ArrayList<>();
		this.indexFieldNames = new HashSet<>();
		if (indexFieldName != null) {
			this.indexFieldNames.add(indexFieldName);
		}
		this.usageType = usageType;
		this.sortInfo = sortInfo;
	}

	public void addChildEntity(final Entity entity) {
		if (getChildByName(entity.getName()) == null) {
			children.add(entity);
		} else {
			throw new IllegalArgumentException(
					"Cannot have two child elements with the same name ["
							+ entity.getName()
							+ "] in group '"
							+ getFullName()
							+ "'.");
		}
	}

	Entity getChildByName(final String name) {
		return children.stream().filter(e -> e.getName().equals(name)).findAny().orElse(null);
	}

	protected Entity getDescendantByPath(final PathElement pathElement) {
		final Entity child = getChildByName(pathElement.getName());
		if (child == null) {
			return null;
		}
		if (pathElement.getNext() == null) {
			return child;
		}
		if (child instanceof Group childGroup) {
			return childGroup.getDescendantByPath(pathElement.getNext());
		} else {
			return null;
		}
	}

	@Override
	public Stream<Entity> flattenToStream() {
		return Stream.concat(
				Stream.of(this),
				getChildren().stream().flatMap(Entity::flattenToStream));
	}

	@Override
	public Optional<String> getCustomType() {
		return Optional.ofNullable(usageType);
	}

}
