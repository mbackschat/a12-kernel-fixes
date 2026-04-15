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
package com.mgmtp.a12.kernel.md.rt.internal.service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.EntityInstanceImpl;

/**
 * Provides conversion between {@link IEntityInstance}s and {@link IIdentifier}s.
 */
public class EntityInstanceConverter {

	private EntityInstanceConverter() {
		throw new IllegalStateException("Utility class");
	}

	public static long[] computeIndices(final IEntityInstance entityInstance) {
		Objects.requireNonNull(entityInstance);
		return Arrays.stream(entityInstance.getRepetitions()).mapToLong(i -> i).toArray();

	}

	public static int[] convertToRepetitions(final long[] indices) {
		Objects.requireNonNull(indices);
		return Arrays.stream(indices).mapToInt(i -> (int) i).toArray();
	}

	public static IIdentifier convertToIIdentifier(final IEntityInstance entityInstance) {
		Objects.requireNonNull(entityInstance);
		return new Identifier(entityInstance.getPath(), computeIndices(entityInstance));
	}

	public static Set<IIdentifier> convertToIIdentifierSet(final Set<IEntityInstance> entityInstances) {
		Objects.requireNonNull(entityInstances);
		return entityInstances.stream()
				.map(EntityInstanceConverter::convertToIIdentifier)
				.collect(Collectors.toSet());
	}

	public static EntityInstanceImpl convertToEntityInstance(final IIdentifier identifier) {
		Objects.requireNonNull(identifier);
		int[] repetitions = convertToRepetitions(identifier.getIndices());
		return new EntityInstanceImpl(identifier.getName(), repetitions);
	}

	public static Set<IEntityInstance> convertToEntityInstanceSet(final Set<IIdentifier> identifiers) {
		Objects.requireNonNull(identifiers);
		return identifiers
				.stream()
				.map(EntityInstanceConverter::convertToEntityInstance)
				.collect(Collectors.toSet());
	}

}
