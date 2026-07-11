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

package com.mgmtp.a12.kernel.core.parser.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IEntityChangeSet;
import com.mgmtp.a12.kernel.core.tool.internal.IMissingEntityEC;

public class MissingEntityEC implements IEC, IMissingEntityEC {

	public static class VirtualFieldType implements IFieldType, IVirtual {
		@Override public boolean matchBaseType(IFieldType expectedBaseType) {
			return true;
		}

		@Override public boolean isValueApplicable(String value) {
			return true;
		}
	}

	public static class VirtualGroup implements IGroup, IVirtual {
		private final IEntity parent;
		private final String fullName;

		public VirtualGroup(IEntity parent, String fullName) {
			this.parent = parent;
			this.fullName = fullName;
		}

		@Override public Set<String> getIndexFieldNames() {
			return Set.of();
		}

		@Override public String getFullName() {
			return fullName;
		}

		@Override public long[] getMax() {
			long[] parentMax = parent == null ? new long[0] : parent.getMax();
			long[] result = Arrays.copyOf(parentMax, parentMax.length + 1);
			result[result.length - 1] = 1;
			return result;
		}
	}

	public static class VirtualEntity extends VirtualGroup implements IField {
		private boolean knownToBeGroup = false;

		public VirtualEntity(final IEntity parent, String fullName) {
			super(parent, fullName);
		}

		@Override public IFieldType getType() {
			return new VirtualFieldType();
		}

		@Override public Optional<IRequirednessConfig> getRequirednessConfig() {
			return Optional.empty();
		}

		@Override public boolean isField() {
			return !knownToBeGroup;
		}

		@Override
		public boolean isGroup() {
			return true;
		}
	}

	private final IEC originalEC;

	private final ConcurrentHashMap<String, IEntity> virtualEntities = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, List<IEntity>> virtualEntitiesByParent = new ConcurrentHashMap<>();

	public static MissingEntityEC wrapEC(IEC originalEC) {
		if (originalEC instanceof MissingEntityEC missingEntityEC) {
			return missingEntityEC;
		} else {
			return new MissingEntityEC(originalEC);
		}
	}

	private MissingEntityEC(IEC originalEC) {
		this.originalEC = originalEC;
	}

	@Override public IEntity get(String fullName) {
		if (StringUtils.isEmpty(fullName)) {
			return null;
		}
		IEntity originalEntity = originalEC.get(fullName);
		if (originalEntity != null) {
			return originalEntity;
		}
		if (!EntityUtils.isAbsolutePath(fullName)) {
			assert false : "fullName must be a valid absolute path but was " + fullName;
			// (according to the javadoc in the interface that declares the method)
			return null;
		}
		if (virtualEntities.containsKey(fullName)) {
			return virtualEntities.get(fullName);
		}
		return createVirtualEntity(fullName);
	}

	private IEntity createVirtualEntity(String fullName) {
		String parentFullName = EntityUtils.getParentPath(fullName);
		IEntity parentEntity = this.get(parentFullName);
		if (parentEntity instanceof VirtualEntity parentGroup) {
			parentGroup.knownToBeGroup = true;
		}
		IEntity result = new VirtualEntity(parentEntity, fullName);
		virtualEntities.put(fullName, result);
		virtualEntitiesByParent.computeIfAbsent(parentFullName, n -> new ArrayList<>()).add(result);
		return result;
	}

	@Override
	public List<IField> getAllContainedFields(String pathName) {
		IEntity start = get(pathName);
		if (start == null) {
			return Collections.emptyList();
		}
		List<IField> fieldsCollector = new ArrayList<>();
		appendAllContainedFields(fieldsCollector, start);
		return fieldsCollector;
	}

	private void appendAllContainedFields(List<IField> fieldsCollector, IEntity parent) {
		List<IEntity> containedEntities = getDirectlyContainedEntities(parent.getFullName());
		List<IEntity> groups = new LinkedList<>();
		for (IEntity entity : containedEntities) {
			if (entity.isField()) {
				fieldsCollector.add((IField) entity);
			}
			if (entity.isGroup()) {
				groups.add(entity);
			}
		}
		for (IEntity entity : groups) {
			appendAllContainedFields(fieldsCollector, entity);
		}
	}

	@Override public List<IEntity> getDirectlyContainedEntities(String pathName) {
		return Java8Utils.concatToList(
				originalEC.getDirectlyContainedEntities(pathName),
				virtualEntitiesByParent.getOrDefault(pathName, Collections.emptyList())
		);
	}

	public IEC getOriginalEC() {
		return originalEC;
	}

	@Override public void applyChanges(Set<IEntityChangeSet> changes) {
		// We do nothing here
	}
}
