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
import java.util.Optional;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.model.header.Annotation;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Default implementation of
 * {@link IField} which maps the properties of
 * {@link com.mgmtp.a12.kernel.md.model.internal.service.mapping.Field}
 */
@Getter
public class Field extends Entity implements IField {

	private final IFieldType type;
	private final boolean isGlobal;
	private final boolean isTransient;
	@Getter(AccessLevel.NONE) private final IRequirednessConfig requirednessConfig;
	private final boolean isInAttachment;

	public Field(
			final String fieldName,
			final IFieldType fieldType,
			final String path,
			final long[] maxIndices,
			final List<Annotation> annotations,
			final String fieldId,
			final boolean isGlobal,
			final boolean isTransient,
			final IRequirednessConfig requirednessConfig,
			final boolean isInAttachment) {

		super(fieldName, path, maxIndices, annotations, fieldId);
		this.type = fieldType;
		this.isGlobal = isGlobal;
		this.isTransient = isTransient;
		this.requirednessConfig = requirednessConfig;
		this.isInAttachment = isInAttachment;
	}

	@Override public Optional<IRequirednessConfig> getRequirednessConfig() {
		return Optional.ofNullable(requirednessConfig);
	}

	@Override
	public Stream<Entity> flattenToStream() {
		return Stream.of(this);
	}
}
