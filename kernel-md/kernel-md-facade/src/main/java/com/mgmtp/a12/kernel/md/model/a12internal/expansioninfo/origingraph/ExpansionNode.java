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

package com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Represents the origin of a single element (or annotation, model details, or model reference)
 * within an expanded document model. It combines the element's {@link LocalCoordinates} in the
 * current model with the set of {@link ExpansionEdge}s that describe how
 * the element was brought in from one or more source models.
 *
 * <p>A {@link ExpansionNode} is a node in an origin graph. Each entry in
 * {@link #previousOrigins()} pairs the
 * {@link ExpansionEdgeLabel}
 * type (e.g. {@link ExpansionEdgeLabel.Dm.Base} for
 * elements sourced from a DocumentModel,
 * {@link ExpansionEdgeLabel.Cm.CmItself} for
 * elements sourced from a {@code CombinationModel}) with the element's {@link ExpansionNode} as it
 * exists in the source model, whose ID is available via the nested node's {@link #modelId()}.</p>
 *
 * <p>The chain terminates at a {@link ExpansionNode} with an empty {@link #previousOrigins()}
 * set, which is the <em>ultimate</em> origin: the element as it is directly defined in a
 * concrete model file.</p>
 */
@Value(staticConstructor = "of") @Accessors(fluent = true)
public class ExpansionNode {
	/**
	 * The local coordinates that identify this element within the current model; never {@code null}.
	 */
	@NonNull LocalCoordinates localCoordinates;

	/**
	 * The ID of the model in which this element exists at this level of the origin chain; never {@code null}.
	 */
	@NonNull String modelId;

	/**
	 * The direct expansion connections through which this element was brought into the
	 * model at this level in the origin chain; never {@code null}, empty for ultimate origins.
	 */
	@NonNull Set<ExpansionEdge> previousOrigins;

	/**
	 * Returns the ultimate origins of this element by recursively following all
	 * {@link #previousOrigins()} chains until a {@link ExpansionNode} with an empty
	 * previous-origins set is reached. Each ultimate origin corresponds to a concrete element
	 * as it exists in a specific model file.
	 *
	 * <p>The set has size 1 for elements coming from a single include or combination step, and
	 * is larger for elements originating from multiple sources. It is never empty: if the element is already
	 * local, the set contains only {@code this}.</p>
	 *
	 * @return the set of ultimate origins; never {@code null} or empty
	 */
	public @NonNull Set<ExpansionNode> ultimateOrigins() {
		if (previousOrigins.isEmpty()) {
			return Set.of(this);
		}
		return previousOrigins.stream()
				.flatMap(nested -> nested.previousNode().ultimateOrigins().stream())
				.collect(Collectors.toSet());
	}

	/**
	 * Creates a {@link ExpansionNode} from a varargs array of previous origins.
	 *
	 * @param localCoordinates
	 * 		the coordinates of the element in the current model
	 * @param modelId
	 * 		the ID of the model in which the element exists at this level
	 * @param previousOrigins
	 * 		the expansion connections leading to this element (nulls ignored)
	 * @return a new {@link ExpansionNode}
	 */
	public static @NonNull ExpansionNode of(@NonNull LocalCoordinates localCoordinates,
			@NonNull String modelId,
			ExpansionEdge... previousOrigins) {
		return ExpansionNode.of(localCoordinates, modelId,
				Arrays.stream(previousOrigins).filter(Objects::nonNull).collect(Collectors.toSet()));
	}

	/**
	 * Returns a new {@link ExpansionNode} with the same coordinates and model ID as this instance
	 * but with the supplied set of previous origins.
	 *
	 * @param previousOrigins
	 * 		the replacement set of previous origins
	 * @return a new {@link ExpansionNode}
	 */
	public @NonNull ExpansionNode withPreviousOrigins(
			@NonNull Set<ExpansionEdge> previousOrigins) {
		return ExpansionNode.of(this.localCoordinates, this.modelId, previousOrigins);
	}

	/**
	 * Merges this origin with an additional origin for the same coordinates, combining their
	 * previous-origins sets. Used when an element is contributed by more than one expansion
	 * source (e.g. addition).
	 *
	 * @param additionalOrigin
	 * 		the other origin to merge with; must share the same coordinates
	 * @return a new {@link ExpansionNode} whose previous-origins is the union of both inputs
	 */
	public @NonNull ExpansionNode merge(ExpansionNode additionalOrigin) {
		if (!additionalOrigin.modelId().equals(this.modelId)) {
			throw new IllegalArgumentException("modelIds must be equal when merging NestedOrigins");
		}
		HashSet<ExpansionEdge> mergedNestedOrigins = new HashSet<>(this.previousOrigins);
		mergedNestedOrigins.addAll(additionalOrigin.previousOrigins());
		return ExpansionNode.of(this.localCoordinates, this.modelId, mergedNestedOrigins);
	}

	/**
	 * Merges a set of {@link ExpansionNode}s into a map keyed by {@link LocalCoordinates},
	 * combining any two nodes that share the same coordinates via {@link #merge(ExpansionNode)}.
	 *
	 * @param origins
	 * 		the set of nodes to merge
	 * @return a map from coordinates to the merged node; never {@code null}
	 */
	public static @NonNull Map<LocalCoordinates, ExpansionNode> merge(Set<ExpansionNode> origins) {
		return origins.stream().collect(Collectors.toMap(
				ExpansionNode::localCoordinates, Function.identity(), ExpansionNode::merge
		));
	}
}
