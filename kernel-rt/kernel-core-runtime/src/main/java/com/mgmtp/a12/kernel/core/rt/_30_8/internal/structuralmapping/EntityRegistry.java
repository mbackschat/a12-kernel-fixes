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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EntityRegistry {
	private final Map<String, Entity> fullNameToEntity;

	private EntityRegistry(Map<String, Entity> fullNameToEntity) {
		this.fullNameToEntity = fullNameToEntity;
	}

	public Entity get(String fullName) {
		return fullNameToEntity.get(fullName);
	}

	public boolean has(String fullName) {
		return fullNameToEntity.containsKey(fullName);
	}

	public Collection<Entity> getAll() {
		return fullNameToEntity.values();
	}

	public Map<String, Entity> toMap() {
		return Map.copyOf(fullNameToEntity);
	}

	public static EntityRegistry.Builder builder() {
		return new EntityRegistry.Builder();
	}

	public static class Builder {
		private final Map<String, EntityDetails> fullNameToEntityDetails = new HashMap<>();

		private record EntityDetails(boolean isField, int repeatability) {
		}

		public Builder entity(String fullName, boolean isField, int repeatability) {
			fullNameToEntityDetails.put(fullName, new EntityDetails(isField, repeatability));
			return this;
		}

		public Builder field(String fullName) {
			return entity(fullName, true, 1);
		}

		public Builder group(String fullName, int repeatability) {
			return entity(fullName, false, repeatability);
		}

		public Builder group(String fullName, long[] repeatabilities) {
			String[] parts = fullName.split("/");
			if (parts.length - 1 != repeatabilities.length) {
				throw new IllegalArgumentException("Wrong number of repetitions");
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < parts.length; i++) {
				sb.append("/");
				sb.append(parts[i]);
				group(sb.toString(), (int) repeatabilities[i - 1]);
			}
			return this;
		}

		/**
		 * Build the {@link EntityRegistry}. Ensures that {@link Entity#parent()} is created before an entity itself. If no group
		 * was specified for an entity's ancestor, assumes it to be a non-repeatable group.
		 */
		public EntityRegistry build() {
			Map<String, Entity> fullNameToEntity = new HashMap<>();
			for (String fullName : fullNameToEntityDetails.keySet()) {
				getOrCreateEntity(fullName, fullNameToEntity);
			}
			return new EntityRegistry(fullNameToEntity);
		}

		private Entity getOrCreateEntity(String fullName, Map<String, Entity> fullNameToEntity) {
			Entity entity = fullNameToEntity.get(fullName);
			if (entity == null) {
				int lastSlashIndex = fullName.lastIndexOf("/");
				if (lastSlashIndex < 0) {
					throw new IllegalArgumentException("Invalid full name: " + fullName);
				}
				String parentFullName = fullName.substring(0, lastSlashIndex);
				Entity parent = fullNameToEntity.get(parentFullName);
				if (parent == null) {
					parent = parentFullName.isEmpty()
							? EntityImpl.ROOT
							: getOrCreateEntity(parentFullName, fullNameToEntity);
				}
				String shortName = fullName.substring(lastSlashIndex + 1);
				int level = parent.level() + 1;
				EntityDetails entityDetails = fullNameToEntityDetails.get(fullName);
				if (entityDetails == null) {
					entityDetails = new EntityDetails(false, 1);
				}
				entity = new EntityImpl(parent, level, fullName, shortName, entityDetails.isField(),
						entityDetails.repeatability());
				fullNameToEntity.putIfAbsent(fullName, entity);
			}
			return entity;
		}
	}
}
