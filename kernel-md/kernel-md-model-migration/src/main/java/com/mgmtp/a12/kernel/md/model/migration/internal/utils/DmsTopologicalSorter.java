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

package com.mgmtp.a12.kernel.md.model.migration.internal.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.utils.internal.TopologicalSortHelper;
import com.mgmtp.a12.kernel.md.model.migration.internal.DmToMigrate;

import tools.jackson.databind.JsonNode;

/**
 * Sort document models topologically according to the imported/included document models.
 */
public class DmsTopologicalSorter {

	private final Map<String, Set<String>> graph;

	/**
	 * Applies the given consumer to every document model in {@code dmsToMigrate}, visiting them in topological order so
	 * that a document model is always processed after all the document models it imports/includes. Document models that
	 * (invalidly) share the same id are grouped together and visited consecutively; the sort order between such groups is
	 * determined by treating all document models with the same id as a single node in the dependency graph.
	 *
	 * @param dmsToMigrate the document models to visit
	 * @param consumer invoked once per document model with the document model itself and a lookup map from document
	 *                 model id to all document models carrying that id
	 * @throws IllegalArgumentException if the imported/included references between the document models form a cycle
	 */
	public static void forEachDmSorted(
			List<DmToMigrate> dmsToMigrate,
			BiConsumer<DmToMigrate, Map<String, List<DmToMigrate>>> consumer
	) {
		Map<String, List<DmToMigrate>> dmsById = dmsToMigrate.stream()
				.collect(Collectors.groupingBy(dm -> ModelJsonUtil.extractModelId(dm.jsonNode())));
		List<String> dmIdsInOrder = DmsTopologicalSorter.sortedIds(dmsToMigrate.stream().map(DmToMigrate::jsonNode).toList());
		dmIdsInOrder.forEach(dmId ->
				dmsById.get(dmId).forEach(dm -> consumer.accept(dm, dmsById)));
	}

	private static List<String> sortedIds(Collection<JsonNode> dmsToSort) {
		DmsTopologicalSorter sorter = DmsTopologicalSorter.ofDMsWithAmbiguousIds(dmsToSort);
		TopologicalSortHelper.TopologicalSortResult<String> sortResult = sorter.sort();
		if (!sortResult.isSuccessful()) {
			throw new IllegalArgumentException("Following document models " +
					"create a cycle: " + String.join(" -> ", sortResult.getCycle()));
		}
		return sortResult.getSort();
	}

	/**
	 * Computes a topological sort for a collection of DMs where some DMs might (invalidly) share the same id. In order
	 * to still determine correct topological sorts (for test data that violates the uniqueness of DM ids), all outgoing DM
	 * references for in the DMs that share an id are combined. i.e. all DMs with the same id are treated as if they are one
	 * DM, with respect to the dependency graph. This can result in false-positive cycle detections, but not in false-negative
	 * results (where a cycle exists, but none is reported).
	 */
	private static DmsTopologicalSorter ofDMsWithAmbiguousIds(Collection<JsonNode> dmsToSort) {
		Set<String> relevantDocModelIds = dmsToSort.stream().map(ModelJsonUtil::extractModelId).collect(Collectors.toSet());
		Map<String, Set<String>> graph = new HashMap<>();
		for (JsonNode docModel : dmsToSort) {
			String docModelId = ModelJsonUtil.extractModelId(docModel);
			Set<String> referencedDmIds = getReferencedDocModelIds(docModel, relevantDocModelIds);
			// We simply merge the referenced ids of all models with the same id, this should be a defensive approach that cannot
			// result in false-negatives regarding cycle detection.
			// False positives in this case appear to be somewhat unlikely for the expected typical cases of dms with non-unique
			// ids.
			graph.computeIfAbsent(docModelId, id -> new HashSet<>()).addAll(referencedDmIds);
		}
		return new DmsTopologicalSorter(graph);
	}

	private DmsTopologicalSorter(Map<String, Set<String>> graph) {
		this.graph = graph;
	}

	public TopologicalSortHelper.TopologicalSortResult<String> sort() {
		return TopologicalSortHelper.topologischeSortierung(this.graph);
	}

	private static Set<String> getReferencedDocModelIds(JsonNode docModel, Set<String> relevantDmIDs) {
		Set<JsonNode> rdms = ModelJsonUtil.extractReferencesToDms(docModel);
		return rdms.stream()
				.filter(ModelJsonUtil::isSupportedModelReference)
				.map(ModelJsonUtil::extractReferenceFromModelReference)
				.filter(Objects::nonNull)
				// we don't know which kind of reference we have, that's why we determine the new one
				.map(IdReferenceConverter::createIdReference)
				// if the reference is not in dmsToSort, we don't add it here, otherwise we get a NPE while sorting
				.filter(relevantDmIDs::contains)
				.collect(Collectors.toSet());
	}

}
