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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping;

import java.util.List;

/**
 * Represents a single entity in a document model, uniquely identified by its full name.
 * <p>
 * Root groups are not {@link #isRoot()}, but instead share the same parent named {@code ""}. This root node occupies level
 * {@code 0}, meaning root groups start at level {@code 1}.
 */
public interface Entity extends Parentable<Entity, Entity>, Named {
	@Override
	default Entity self() {
		return this;
	}

	/**
	 * @return the repeatability of the receiver (always {@literal 1} for fields).
	 */
	int repeatability();

	/**
	 * @return {@code true} if the receiver is a field, otherwise {@code false}.
	 */
	boolean isField();

	/**
	 * @return {@code true} if the receiver is a group, otherwise {@code false}.
	 */
	default boolean isGroup() {
		return !isField();
	}

	/**
	 * @return {@code true} if the receiver is repeatable by itself (i.e. has {@link #repeatability()} higher than {@literal 1}),
	 * 		otherwise {@code false}.
	 */
	default boolean isRepetitionLayer() {
		return repeatability() > 1;
	}

	/**
	 * @return {@code true} if there is an ancestor of the receiver (i.e. not the receiver itself) for which
	 *        {@link #isRepetitionLayer()} is {@code true}, otherwise {@code false}.
	 */
	default boolean hasRepetitionLayer() {
		return ancestors().stream().anyMatch(Entity::isRepetitionLayer);
	}

	/**
	 * @return all ancestors ({@link #ancestors()}) that are repetition layers ({@link #isRepetitionLayer()}).
	 */
	default List<Entity> repetitionLayers() {
		return ancestors().stream().filter(Entity::isRepetitionLayer).toList();
	}

	/**
	 * Test whether a reference from the receiver to the given entity would be repeatable.
	 *
	 * @param referencedEntity
	 * 		the target entity for the reference
	 * @return {@code true} if the {@code referencedEntity} or one of its ancestors up to but not including the
	 *        {@link #commonAncestor} of the receiver and {@code referencedEntity} is repeatable by itself (i.e.
	 *        {@link #isRepetitionLayer()} is {@code true}), otherwise {@code false}.
	 */
	default boolean isReferenceRepeatable(Entity referencedEntity) {
		return referencedEntity.withAncestorsUntil(commonAncestor(referencedEntity).level()).stream()
				.anyMatch(Entity::isRepetitionLayer);
	}

	/**
	 * @return the innermost repeatable group of the receiver, which is the closest ancestor (i.e. not the receiver itself) for which
	 *        {@link #isRepetitionLayer()} is {@code true}, or the root if no such ancestor exists.
	 */
	default Entity irg() {
		if (isRoot()) {
			return this;
		}
		Entity ancestor = parent();
		while (!ancestor.isRoot() && !ancestor.isRepetitionLayer()) {
			ancestor = ancestor.parent();
		}
		return ancestor;
	}

	/**
	 * @return the ancestor at level {@code 1} (e.g. {@literal /A} for entity {@literal /A/B/F}).
	 *
	 * @throws IllegalArgumentException
	 * 		if the receiver {@link #isRoot()}
	 */
	default Entity rootGroup() {
		if (isRoot()) {
			throw new IllegalArgumentException("Cannot get the root group for the virtual root entity");
		}
		return ancestorAtLevel(1);
	}
}
