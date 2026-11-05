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

package com.mgmtp.a12.kernel.md.model.internal.expansioninfo;

import static com.mgmtp.a12.kernel.md.model.a12internal.ExpansionPurposes.DM_PURPOSE_INCLUDE;
import static com.mgmtp.a12.kernel.md.model.a12internal.ExpansionPurposes.DM_PURPOSE_TYPEDEF;
import static com.mgmtp.a12.kernel.md.model.a12internal.ExpansionPurposes.MANAGED_PURPOSES;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdge;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdgeLabel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionNode;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.LocalCoordinates;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.KernelJsonMapper;
import com.mgmtp.a12.model.header.HeaderFactory;
import com.mgmtp.a12.model.header.ModelReference;

import lombok.NonNull;
import tools.jackson.databind.ObjectMapper;

/**
 * Utility methods for constructing {@link ExpansionNode}
 * maps when propagating expansion info.
 */
public class ExpansionInfoUtils {
	private ExpansionInfoUtils() {
	}

	/**
	 * Builds a {@link ExpansionNode}
	 * map for <em>all</em> entries in the given source expansion info, wrapping each existing
	 * origin in a new {@link ExpansionEdge}
	 * that records the expansion step used to bring the element into the current model.
	 *
	 * <p>The coordinates (keys) are preserved unchanged; only the origin chain is extended by
	 * one step.</p>
	 *
	 * @param expansionConnectionSupplier
	 * 		a supplier that creates the {@link ExpansionEdgeLabel}
	 * 		for each entry (called once per entry)
	 * @param nestedExpansionInfo
	 * 		the expansion info of the source model
	 * @return a map from local coordinates to updated nested origins, ready to be merged into
	 * 		the current model's expansion info
	 */
	@NonNull public static Map<LocalCoordinates, @NonNull ExpansionNode> allNestedOriginsNoChange(
			@NonNull String currentModelId, @NonNull ExpansionEdgeLabel expansionConnectionSupplier,
			@NonNull ExpansionInfo nestedExpansionInfo) {
		return nestedExpansionInfo.allTracedObjects().stream()
				.collect(Collectors.toMap(coords -> coords, coords -> {
					ExpansionNode baseOrigin = nestedExpansionInfo.getOrigin(coords);
					return ExpansionNode.of(baseOrigin.localCoordinates(), currentModelId,
							Set.of(ExpansionEdge.of(expansionConnectionSupplier, baseOrigin))
					);
				}));
	}

	/**
	 * Builds a map from model ID to the set of {@link ModelReference}s present in that model,
	 * by recursively walking the {@link ExpansionEdge} chain for all
	 * {@link LocalCoordinates.ModelItself} and {@link LocalCoordinates.ModelReference} entries in
	 * the given expansion info.
	 *
	 * <p>For {@link LocalCoordinates.ModelItself} entries the connection type at each hop
	 * determines the reference to record:</p>
	 * <ul>
	 *   <li>{@link ExpansionEdgeLabel.Dm.Import} → typeDefinitions reference pointing to the source
	 *       model, added to the current model's entry.</li>
	 *   <li>{@link ExpansionEdgeLabel.Dm.Include} → include reference pointing to the source model,
	 *       added to the current model's entry.</li>
	 * </ul>
	 *
	 * <p>For {@link LocalCoordinates.ModelReference} entries the model reference is added to the
	 * current model's entry only when the connection is {@link ExpansionEdgeLabel.Dm.Base} (the
	 * model natively owns the reference) or {@link ExpansionEdgeLabel.Cm.CmItself} (the
	 * reference is defined directly in a combination model). References arriving via include or
	 * import connections are recorded at the owning model's level, not at the current model's
	 * level.</p>
	 *
	 * <p>Every model reachable via an {@link ExpansionEdgeLabel.Dm.Import} or
	 * {@link ExpansionEdgeLabel.Dm.Include} appears as a key in the result; models that have no
	 * outgoing references at that level are represented by an empty set.</p>
	 *
	 * @param expansionInfo
	 * 		the expansion info to reconstruct from
	 * @return a map from model ID to its set of {@link ModelReference}s; never {@code null}
	 */
	@NonNull public static Map<String, Set<ModelReference>> getModelReferencesIncludingAlreadyExpandedOnes(
			@NonNull ExpansionInfo expansionInfo) {
		Map<String, Set<ModelReference>> result = new HashMap<>();
		expansionInfo.allTracedObjects().forEach(coords -> {
			if (coords instanceof LocalCoordinates.ModelItself || coords instanceof LocalCoordinates.ModelReference) {
				processOriginChain(expansionInfo.localModelId(), coords, expansionInfo.getOrigin(coords), result);
			}
		});
		return result;
	}

	/**
	 * Returns the IDs of all groups in the local model that were originally modeled as includes
	 * (i.e., that had a {@link com.mgmtp.a12.kernel.md.model.a12internal.Group.IncludeConfig}
	 * referencing another document model).
	 *
	 * <p>After expansion the include group's
	 * {@link com.mgmtp.a12.kernel.md.model.a12internal.Group.IncludeConfig} is cleared. The only
	 * trace left in the expansion info is the {@link ExpansionEdgeLabel.Dm.Include} recorded on every
	 * element brought in from the included model; its
	 * {@link ExpansionEdgeLabel.Dm.Include#includeGroupId()} identifies the group in the local model
	 * that acted as the include mount-point.</p>
	 *
	 * @param expansionInfo
	 * 		the expansion info to inspect; must not be {@code null}
	 * @return the set of include-group IDs; never {@code null}, may be empty
	 */
	@NonNull public static Set<String> getIncludeGroupIds(@NonNull ExpansionInfo expansionInfo) {
		ExpansionNode origin = expansionInfo.getOrigin(LocalCoordinates.ModelItself.of());
		if (origin == null) {
			throw new IllegalArgumentException("No ExpansionInfo found for 'LocalCoordinates.ModelItself.of()'");
		}

		return origin.previousOrigins().stream()
				.map(ExpansionEdge::label)
				.filter(ExpansionEdgeLabel.Dm.Include.class::isInstance)
				.map(ExpansionEdgeLabel.Dm.Include.class::cast)
				.map(ExpansionEdgeLabel.Dm.Include::includeGroupId)
				.collect(Collectors.toSet());
	}

	private static void processOriginChain(String currentModelId, LocalCoordinates coords,
			ExpansionNode origin, Map<String, Set<ModelReference>> result) {
		for (ExpansionEdge prev : origin.previousOrigins()) {
			String prevModelId = prev.previousNode().modelId();
			if (coords instanceof LocalCoordinates.ModelReference mrc) {
				if (prev.label() instanceof ExpansionEdgeLabel.Dm.Base || prev.label() instanceof ExpansionEdgeLabel.Cm.CmItself) {
					result.computeIfAbsent(currentModelId, k -> new HashSet<>()).add(mrc.modelReference());
				}
			} else {
				if (prev.label() instanceof ExpansionEdgeLabel.Dm.Import) {
					result.computeIfAbsent(currentModelId, k -> new HashSet<>())
							.add(HeaderFactory.createModelReference(DM_PURPOSE_TYPEDEF, DocumentModel.MODEL_TYPE_DOCUMENT,
									prevModelId, prevModelId));
					result.computeIfAbsent(prevModelId, k -> new HashSet<>());
				} else if (prev.label() instanceof ExpansionEdgeLabel.Dm.Include) {
					result.computeIfAbsent(currentModelId, k -> new HashSet<>())
							.add(HeaderFactory.createModelReference(DM_PURPOSE_INCLUDE, DocumentModel.MODEL_TYPE_DOCUMENT,
									prevModelId, prevModelId));
					result.computeIfAbsent(prevModelId, k -> new HashSet<>());
				}
			}
			processOriginChain(prevModelId, coords, prev.previousNode(), result);
		}
	}

	public static Set<LocalCoordinates> unmanagedModelReferences(Collection<ModelReference> modelReferences) {
		return CollectionUtils.emptyIfNull(modelReferences).stream()
				.filter(modelReference -> !MANAGED_PURPOSES.contains(modelReference.getPurpose()))
				.map(LocalCoordinates.ModelReference::of)
				.collect(Collectors.toSet());
	}

	public static Set<@NonNull ExpansionNode> forwardOrigins(@NonNull ExpansionInfo previousExpansionInfo,
			@NonNull Predicate<LocalCoordinates> filter, @NonNull ExpansionEdgeLabel edgeLabel, @NonNull String resultModelId) {
		return previousExpansionInfo.allTracedObjects().stream()
				.filter(filter)
				.map(coords -> ExpansionNode.of(coords, resultModelId, ExpansionEdge.of(
						edgeLabel, previousExpansionInfo.getOrigin(coords)
				)))
				.collect(Collectors.toSet());
	}

	/*
	 * add element as they are in the result DM
	 */
	public static Set<ExpansionNode> forwardOriginForSingleElementWithAnnotations(final String elementIdInResult,
			final String resultModelId, ExpansionEdgeLabel edgeLabel, UnaryOperator<String> newToOldIdMapper,
			@NonNull ExpansionInfo previousExpansionInfo) {
		HashSet<ExpansionNode> originsForResult = new HashSet<>();
		String elementIdInPrevious = newToOldIdMapper.apply(elementIdInResult);

		originsForResult.add(ExpansionNode.of(LocalCoordinates.Element.of(elementIdInResult), resultModelId,
				Set.of(ExpansionEdge.of(edgeLabel,
						previousExpansionInfo.elementOriginById(elementIdInPrevious))
				))
		);

		// add all annotations of the element
		previousExpansionInfo.allAnnotationsForElementIdOrigins(elementIdInPrevious)
				.forEach((annotationName, annotationOriginInPrevious) ->
						originsForResult.add(ExpansionNode.of(
								LocalCoordinates.Annotation.of(annotationName, elementIdInResult),
								resultModelId,
								ExpansionEdge.of(edgeLabel, annotationOriginInPrevious)
						))
				);

		return originsForResult;
	}

	public static void serializeEI(Writer writer, ExpansionInfo ei) {
		// adapt field detection, as the lombok fluent api breaks the default
		ObjectMapper mapper = KernelJsonMapper.getGeneralMapper()
				.changeDefaultVisibility(vc -> vc
						.with(JsonAutoDetect.Visibility.NONE)
						.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				).build();

		mapper.writeValue(writer, ei);
	}

	public static String serializeEI(ExpansionInfo ei) {
		Writer writer = new StringWriter();
		serializeEI(writer, ei);
		return writer.toString();
	}

	public static void mergeInto(Map<LocalCoordinates, ExpansionNode> acc, Map<LocalCoordinates, ExpansionNode> contribution) {
		contribution.forEach((k, v) -> acc.merge(k, v, ExpansionNode::merge));
	}

	public static void mergeInto(Map<LocalCoordinates, ExpansionNode> acc, ExpansionNode... contribution) {
		mergeInto(acc, Arrays.asList(contribution));
	}

	public static void mergeInto(Map<LocalCoordinates, ExpansionNode> acc, Collection<ExpansionNode> contribution) {
		mergeInto(acc, contribution.stream().collect(Collectors.toMap(ExpansionNode::localCoordinates, node -> node)));
	}
}
