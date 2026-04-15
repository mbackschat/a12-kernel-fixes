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

package com.mgmtp.a12.kernel.md.model.migration.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.NodePath;

/**
 * Since Kernel version 28.0.0, it is strongly recommended that the whole workspace should be migrated at once, since migration
 * steps need information across document models.
 * This class gathers the information which should be used shared across the document models which are being migrated.
 */
public class MigrationCache {

	// Used in UseUuidInTypeDefsStep
	/**
	 * Since type definitions should have a corresponding uuid, we cache the type definition uuids based on the original unique id
	 * (model id + local type def id).
	 */
	private final Map<String, String> typeDefUuidsByUniqueId = new HashMap<>();
	/**
	 * like the previous ona, only the other way around.
	 */
	private final Map<String, String> typeDefUniqueIdsByUuid = new HashMap<>();
	/**
	 * We cache the already processed document models based on the model id. The value is a list, since we consider the case that
	 * more than one document model have the same model id.
	 */
	private final Map<String, List<JsonNode>> docModelsById = new HashMap<>();

	/**
	 * For each document model we store the fields and their relative paths to the model root
	 */
	private final Map<String, Map<String, NodePath>> fieldPathByIdMapByDocModelId = new HashMap<>();

	/**
	 * Intended to be used in tests to get deterministic results.
	 */
	private Map<String, String> customTypeDefUuidsByUniqueId = new HashMap<>();

	public MigrationCache() {
	}

	public MigrationCache(Map<String, String> customTypeDefUuidsByUniqueId) {
		this.customTypeDefUuidsByUniqueId = customTypeDefUuidsByUniqueId;
	}

	public void addTypeDefUuidByUniqueId(String uniqueId, String uuid) {
		this.typeDefUuidsByUniqueId.put(uniqueId, uuid);
		this.typeDefUniqueIdsByUuid.put(uuid, uniqueId);
	}

	public String getTypeDefUuid(String uniqueId) {
		return typeDefUuidsByUniqueId.get(uniqueId);
	}

	public boolean containsTypeDefUniqueId(String uniqueId) {
		return typeDefUuidsByUniqueId.containsKey(uniqueId);
	}

	public String getTypeDefUniqueId(String uuid) {
		return typeDefUniqueIdsByUuid.get(uuid);
	}

	public boolean containsTypeDefUuid(String uuid) {
		return typeDefUniqueIdsByUuid.containsKey(uuid);
	}

	public void addDocModelById(String docModelId, JsonNode docModel) {
		this.docModelsById.computeIfAbsent(docModelId, key -> new ArrayList<>()).add(docModel);
	}

	public List<JsonNode> getDocModels(String docModelId) {
		return docModelsById.getOrDefault(docModelId, List.of());
	}

	public Map<String, NodePath> getFieldPathByIdMapByDocModelId(String docModelId) {
		return fieldPathByIdMapByDocModelId.getOrDefault(docModelId, new HashMap<>());
	}

	public void addFieldPathByIdMapByDocModelId(String docModelId, Map<String, NodePath> fieldPathById) {
		fieldPathByIdMapByDocModelId.computeIfAbsent(docModelId, key -> new HashMap<>()).putAll(fieldPathById);
	}

	public boolean containsCustomTypeDefUuid(String uniqueId) {
		return customTypeDefUuidsByUniqueId.containsKey(uniqueId);
	}

	public String getCustomTypeDefUuid(String uniqueId) {
		return customTypeDefUuidsByUniqueId.get(uniqueId);
	}

}
