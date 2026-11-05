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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.core;

import static com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RuntimeUtils.nonRepeatableDescendentIndices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Stores the fill status of groups by remembering all filled child entities per group instance.
 */
@RequiredArgsConstructor
public class GroupFillCache {
	private final @NonNull Map<
			String, // full group name
			Map< // "instance map"
					RtInternalIdentifier, // group identifier
					CacheNode // cache node storing identifiers of direct child entities which are filled
					>
			> pathToInstanceMap = new HashMap<>();
	private final @NonNull List<Long> highestRepetitions = new ArrayList<>();

	private final @NonNull IMetaDataValidierung metaData;

	/**
	 * Set the given entity and all its ancestor groups to filled.
	 *
	 * @param entityId
	 * 		a valid entity in the document model
	 */
	public void setEntityFilled(RtInternalIdentifier entityId) {
		if (metaData.isGroupDefined(entityId.getName())) {
			setGroupFilled(entityId);
		} else {
			setFieldFilled(entityId);
		}
	}

	/**
	 * Sets the given field and all its ancestor groups to filled.
	 *
	 * @param fieldId
	 * 		a valid field in the document model
	 */
	public void setFieldFilled(RtInternalIdentifier fieldId) {
		propagateEntityFilled(fieldId, true);
	}

	/**
	 * Sets the given group and all its ancestor groups to filled.
	 * This is counted as an explicit set, meaning the group will not be removed through propagation
	 * by {@link GroupFillCache#setFieldNotFilled}.
	 *
	 * @param groupId
	 * 		a valid group in the document model
	 */
	public void setGroupFilled(RtInternalIdentifier groupId) {
		setGroupFilledExplicitly(groupId);
		propagateEntityFilled(groupId, false);
	}

	/**
	 * Sets all ancestor groups of *entity* to filled.
	 */
	private void propagateEntityFilled(RtInternalIdentifier entityId, boolean entityIsField) {
		assert entityId.getUsb() == IIdentifier.USB_NOT_SPECIFIED;
		RtInternalIdentifier childEntity = entityId;
		RtInternalIdentifier parentGroup = RtInternalIdentifier.createParentId(entityId, metaData.getPfadName(entityId.getName()));

		while (parentGroup != null) {
			Map<RtInternalIdentifier, CacheNode> instanceMap = assuredInstanceMap(parentGroup.getName());
			CacheNode cacheNode = instanceMap.get(parentGroup);
			if (cacheNode != null) {
				cacheNode.addChild(childEntity);
				childEntity = parentGroup;
				break;
			} else {
				instanceMap.put(parentGroup, new GroupNode(childEntity));
			}

			childEntity = parentGroup;
			parentGroup = RtInternalIdentifier.createParentId(childEntity, childEntity.getName());
		}

		// update highest repetitions of all newly added levels
		RtInternalIdentifier furthestUpdatedAncestor = childEntity;
		long[] indices = entityId.getIndices();
		int start = furthestUpdatedAncestor.getIndices().length - 1;
		updateHighestRepetition(indices, start, entityIsField ? indices.length - 1 : indices.length);
	}

	/**
	 * Compares and updates the stored max indices from {@code startIndex} (inclusive) to {@code stopIndex} (exclusive) with
	 * corresponding values in {@code indices}.
	 */
	private void updateHighestRepetition(long[] indices, int startIndex, int stopIndex) {
		// Ensure highestRepetitions has enough size
		for (int i = highestRepetitions.size(); i < stopIndex; i++) {
			highestRepetitions.add(1L);
		}
		for (int level = startIndex; level < stopIndex; level++) {
			long highestRepetition = highestRepetitions.get(level);
			long repetition = indices[level];
			if (repetition > highestRepetition) {
				highestRepetitions.set(level, repetition);
			}
		}
	}

	private void setGroupFilledExplicitly(RtInternalIdentifier groupId) {
		assuredInstanceMap(groupId.getName()).put(groupId, new ExplicitGroupNode());
	}

	private Map<RtInternalIdentifier, CacheNode> assuredInstanceMap(String groupFullName) {
		return pathToInstanceMap.computeIfAbsent(groupFullName, k -> new HashMap<>());
	}

	/**
	 * Sets *field* to not filled and updates all ancestor groups accordingly.
	 * If one or more ancestor groups were only filled because of *field*,
	 * those groups will be set to not filled (unless they are set explicitly).
	 *
	 * @param fieldId
	 * 		a valid field in the document model
	 */
	public void setFieldNotFilled(RtInternalIdentifier fieldId) {
		assert fieldId.getUsb() == IIdentifier.USB_NOT_SPECIFIED;
		propagateEntityNotFilled(fieldId);
	}

	private void propagateEntityNotFilled(RtInternalIdentifier entityId) {
		RtInternalIdentifier childEntity = entityId;
		RtInternalIdentifier parentGroup = RtInternalIdentifier.createParentId(entityId, metaData.getPfadName(entityId.getName()));
		while (parentGroup != null) {
			Map<RtInternalIdentifier, CacheNode> instanceMap = pathToInstanceMap.get(parentGroup.getName());
			if (instanceMap == null) {
				return;
			}
			CacheNode cacheNode = instanceMap.get(parentGroup);
			if (cacheNode == null) {
				return;
			}
			cacheNode.removeChild(childEntity);

			if (cacheNode.isFilled()) {
				return;
			}
			instanceMap.remove(parentGroup);

			childEntity = parentGroup;
			parentGroup = RtInternalIdentifier.createParentId(childEntity, childEntity.getName());
		}
	}

	/**
	 * @param groupFullName
	 * 		the full name of a valid group in the Document Model
	 * @return a {@link Set} containing all group instances which are currently filled. Does not contain duplicates.
	 */
	public Set<RtInternalIdentifier> getFilledGroupInstances(String groupFullName) {
		Map<RtInternalIdentifier, CacheNode> map = pathToInstanceMap.get(groupFullName);
		return map != null ? map.keySet() : Set.of();
	}

	/**
	 * @param fieldFullName
	 * 		the full name of a valid field in the Document Model
	 * @return all field instances of the given field which exist
	 * 		(i.e. not repeatable or innermost repeatable group is filled).
	 */
	public List<RtInternalIdentifier> getExistingFieldInstances(String fieldFullName) {
		List<RtInternalIdentifier> result = new ArrayList<>();
		String fieldUniqueName = metaData.getUniqueName(fieldFullName);
		int fieldLevel = EntityUtil.countHierarchyLevels(fieldFullName);
		String irgFullName = metaData.getInnermostRepeatableGroup(fieldFullName);

		if (irgFullName == null) { // exactly one instance where all indices are 1
			result.add(new RtInternalIdentifier(fieldUniqueName, nonRepeatableDescendentIndices(new long[0], fieldLevel)));
		} else { // one instance per filled IRG (indices below IRG are all 1)
			for (RtInternalIdentifier irgInstance : getFilledGroupInstances(irgFullName)) {
				long[] irgIndices = irgInstance.getIndices();
				long[] indices = nonRepeatableDescendentIndices(irgIndices, fieldLevel - irgIndices.length);
				result.add(new RtInternalIdentifier(fieldUniqueName, indices));
			}
		}
		return result;
	}

	/**
	 * @param groupFullName
	 * 		the full name of a valid group in the Document Model
	 * @return the exact number of group instances which are currently filled.
	 */
	public int numberOfFilledGroupInstances(String groupFullName) {
		Map<RtInternalIdentifier, CacheNode> map = pathToInstanceMap.get(groupFullName);
		return map != null ? map.size() : 0;
	}

	/**
	 * @param entityId
	 * 		an entity in the Document Model
	 * @return true if entityId is a filled group, false otherwise
	 */
	public boolean isGroupFilled(RtInternalIdentifier entityId) {
		assert entityId.getUsb() == IIdentifier.USB_NOT_SPECIFIED;
		Map<RtInternalIdentifier, CacheNode> map = pathToInstanceMap.get(entityId.getName());
		return map != null && map.containsKey(entityId);
	}

	/**
	 * @param level
	 * 		zero-indexed level
	 * @return the highest repetition ever recorded at the given level.
	 * 		Never shrinks (e.g. calling {@link #setFieldNotFilled} will not update it). At least 1.
	 */
	public long getHighestRepetitionAtLevel(int level) {
		return level < highestRepetitions.size()
				? highestRepetitions.get(level)
				: 1;
	}

	private abstract static class CacheNode {
		public void addChild(RtInternalIdentifier _entityId) {
			// do nothing
		}

		public void removeChild(RtInternalIdentifier _entityId) {
			// do nothing
		}

		public boolean isFilled() {
			return true;
		}
	}

	private static class GroupNode extends CacheNode {
		private final Set<RtInternalIdentifier> children = new HashSet<>();

		public GroupNode(RtInternalIdentifier childToAdd) {
			addChild(childToAdd);
		}

		@Override
		public void addChild(RtInternalIdentifier entityId) {
			children.add(entityId);
		}

		@Override
		public void removeChild(RtInternalIdentifier entityId) {
			children.remove(entityId);
		}

		@Override
		public boolean isFilled() {
			return !children.isEmpty();
		}
	}

	private static class ExplicitGroupNode extends CacheNode {
	}
}
