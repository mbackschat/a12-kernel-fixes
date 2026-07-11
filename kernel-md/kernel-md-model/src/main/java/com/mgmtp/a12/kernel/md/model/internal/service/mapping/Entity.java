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

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.model.header.Annotation;

/**
 * Default implementation of
 * {@link IEntity} which maps the properties of
 * {@link com.mgmtp.a12.kernel.md.model.a12internal.Element}.
 */
public abstract class Entity implements IEntity {

	private final String name;
	private final String path;
	private final long[] maxIndices;
	private final List<Annotation> annotations;
	private final String id;

	protected Entity(final String name, final String path, final long[] maxIndices,
			final List<Annotation> annotations, final String id) {
		Validate.notBlank(name);
		Validate.notBlank(path);
		Objects.requireNonNull(maxIndices);

		this.name = name;
		this.path = path;
		this.maxIndices = maxIndices;
		this.annotations = annotations;
		this.id = id;
	}

	@Override
	public String getFullName() {
		return path;
	}

	@Override
	public long[] getMax() {
		return maxIndices;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public abstract Stream<Entity> flattenToStream();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
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
		Entity other = (Entity) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}

}
