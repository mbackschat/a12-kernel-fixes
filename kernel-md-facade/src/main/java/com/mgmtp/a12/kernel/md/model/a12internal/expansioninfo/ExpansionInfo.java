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

package com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdge;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdgeLabel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionNode;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.LocalCoordinates;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Holds the expansion information for an expanded document model, tracking where each of its
 * elements, annotations, model details, and model references originated from.
 *
 * <p>When a document model is assembled from multiple source models (via includes, decoration,
 * additive composition, selection, or combination), an {@link ExpansionInfo} instance maps every
 * local coordinate in the resulting model to a {@link ExpansionNode} that describes the
 * chain of {@link ExpansionEdgeLabel}s
 * leading back to the element in its original model file.</p>
 *
 * <p>Each {@link ExpansionNode} in the map carries a set of
 * {@link ExpansionEdge}s
 * that point to the element's origin in the respective source model together with the expansion
 * step used. Following these links recursively leads to the <em>ultimate</em> origin: a
 * {@link ExpansionNode} with an empty {@link ExpansionNode#previousOrigins()} set, which represents
 * the element as it is directly defined in a concrete model file.</p>
 */
@OnlyForUsage
public interface ExpansionInfo {
	/**
	 * Returns the ID of the local model this expansion info belongs to.
	 *
	 * @return the local model ID
	 */
	@NonNull String localModelId();

	/**
	 * Returns the set of all {@link LocalCoordinates} tracked by this expansion info.
	 * Use {@link #getOrigin(LocalCoordinates)} to obtain the {@link ExpansionNode} for a given
	 * coordinate.
	 *
	 * @return an unmodifiable set of tracked coordinates
	 */
	@NonNull Set<LocalCoordinates> allTracedObjects();

	/**
	 * Returns the {@link ExpansionNode} for the given coordinates, or {@code null} if the
	 * coordinate is not tracked (i.e. not present in the expanded result).
	 *
	 * @param localCoordinates
	 * 		the coordinates to look up
	 * @return the nested origin, or {@code null}
	 */
	ExpansionNode getOrigin(@NonNull LocalCoordinates localCoordinates);

	/**
	 * Convenience method that looks up the origin of a model element by its element ID.
	 *
	 * @param elementId
	 * 		the element ID to look up
	 * @return the nested origin of the element, or {@code null} if not present
	 */
	default ExpansionNode elementOriginById(@NonNull String elementId) {
		return getOrigin(LocalCoordinates.Element.of(elementId));
	}

	/**
	 * Looks up the origin of a specific annotation identified by its name and the ID of the
	 * element it annotates.
	 *
	 * @param annotationName
	 * 		the name of the annotation
	 * @param annotatedElementId
	 * 		the ID of the annotated element, or the sentinel value
	 *        {@link LocalCoordinates.Annotation#MODEL_ANNOTATION}
	 * 		for header (model-level) annotations
	 * @return the nested origin of the annotation, or {@code null} if not present
	 */
	default ExpansionNode annotationOrigin(@NonNull String annotationName, @NonNull String annotatedElementId) {
		return getOrigin(LocalCoordinates.Annotation.of(annotationName, annotatedElementId));
	}

	// we could add a (default impl) method documentUniquenessCriterionOrigin here, but I think this unnecessarily obscures the more universal getOrigin(LocalCoordinates) method.

	/**
	 * Returns the origins of all annotations that are attached to the given element ID, keyed by
	 * annotation name.
	 *
	 * @param annotatedElementId
	 * 		the ID of the annotated element; use
	 *        {@link LocalCoordinates.Annotation#MODEL_ANNOTATION}
	 * 		to select model-level annotations
	 * @return a map from annotation name to its nested origin; never {@code null}, may be empty
	 */
	default @NonNull Map<String, ExpansionNode> allAnnotationsForElementIdOrigins(@NonNull String annotatedElementId) {
		return allTracedObjects().stream()
				.filter(coords -> coords instanceof LocalCoordinates.Annotation annotationCoordinates
						&& Objects.equals(annotatedElementId, annotationCoordinates.annotatedElementId()))
				.collect(Collectors.toMap(
						coords -> ((LocalCoordinates.Annotation) coords).annotationName(),
						this::getOrigin
				));
	}

	/**
	 * Creates an {@link ExpansionInfo} for a model with no expanded origins.
	 *
	 * @param localModelId
	 * 		the ID of the local model
	 * @return an {@link ExpansionInfo} with an empty origins map
	 */
	public static @NonNull ExpansionInfo empty(@NonNull String localModelId) {
		return new ExpansionInfoImpl(localModelId, Map.of());
	}

	/**
	 * Creates an {@link ExpansionInfo} from an explicit map of coordinates to origins.
	 *
	 * @param localModelId
	 * 		the ID of the local model
	 * @param origins
	 * 		a map from {@link LocalCoordinates} to the corresponding {@link ExpansionNode}
	 * @return a new {@link ExpansionInfo} instance
	 */
	public static @NonNull ExpansionInfo of(@NonNull String localModelId, @NonNull Map<LocalCoordinates, ExpansionNode> origins) {
		return new ExpansionInfoImpl(localModelId, origins);
	}

	/**
	 * Creates an {@link ExpansionInfo} from a varargs array of {@link ExpansionNode}s.
	 * Each origin is keyed by its {@link ExpansionNode#localCoordinates()}.
	 * Merges two origins with the same coordinates.
	 *
	 * @param localModelId
	 * 		the ID of the local model
	 * @param origins
	 * 		the nested origins to register
	 * @return a new {@link ExpansionInfo} instance
	 */
	public static @NonNull ExpansionInfo of(@NonNull String localModelId, @NonNull ExpansionNode... origins) {
		return new ExpansionInfoImpl(localModelId, ExpansionNode.merge(Set.of(origins)));
	}

	/**
	 * Returns a new {@link ExpansionInfo} that contains the origins of this instance merged with
	 * the supplied additional origins maps. When the same coordinate exists in both this instance
	 * and an additional map, the two {@link ExpansionNode}s are merged via
	 * {@link ExpansionNode#merge(ExpansionNode)}.
	 *
	 * @param origins
	 * 		one or more maps of additional origins to merge in
	 * @return a new {@link ExpansionInfo} with the combined origins
	 */
	@NonNull ExpansionInfo mergeWithNestedOrigins(@NonNull Map<? extends LocalCoordinates, ExpansionNode>... origins);
}
