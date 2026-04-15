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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;

/**
 * Parameter class to produce code for a special entity list contained in NumberOfFilledGroups.
 */
@StringTemplateGroup("entityid")
public final class GroupedEntityIdList extends EntityIdList implements IEntityIdList {

	@StringTemplateGroup("entityid")
	public static class GroupedEntityIdListElem extends STParameterClassBase {
		private final EntityId key;
		private final List<EntityId> list = new ArrayList<>();

		GroupedEntityIdListElem(EntityId key) {
			this.key = key;
		}

		EntityId getKey() {
			return key;
		}

		List<EntityId> getList() {
			return list;
		}

		void add(EntityId entity) {
			list.add(entity);
		}

		// used by string template
		public String getVariableName() {
			return key.getVariableName();
		}

		// used by string template
		public List<String> getValuesCode() {
			List<String> result = new ArrayList<>();
			for (int i = 0; i < list.size(); i++) {
				EntityId id = list.get(i);
				result.add(id.getVariableName());
			}
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + list.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			GroupedEntityIdListElem other = (GroupedEntityIdListElem) obj;
			if (key == null) {
				if (other.key != null) {
					return false;
				}
			} else if (!key.equals(other.key)) {
				return false;
			}
			if (!list.equals(other.list)) {
				return false;
			}
			return true;
		}

	}

	private final List<GroupedEntityIdListElem> groupedList;
	private String varName = null;

	public GroupedEntityIdList(EntityIdList superList) {
		super(superList);
		this.groupedList = Collections.unmodifiableList(computeGroupedList());
	}

	private List<GroupedEntityIdListElem> computeGroupedList() {
		List<GroupedEntityIdListElem> retVal = new ArrayList<>();
		boolean withGroupExpansion = isGroupExpansion(0);
		GroupedEntityIdListElem currGroupEntityList = null;
		for (int i = 0; i < entities.size(); i++) {
			EntityId currEntityId = entities.get(i);
			if ((withGroupExpansion && isGroupExpansion(i)) || (!withGroupExpansion && !currEntityId.isField())) {
				currGroupEntityList = new GroupedEntityIdListElem(currEntityId);
				retVal.add(currGroupEntityList);
			} else if (currGroupEntityList != null) {
				currGroupEntityList.add(currEntityId);
			}
		}
		return retVal;
	}

	private boolean isGroupExpansion(int i) {
		return entities.size() > i + 1 && entities.get(i).essentiallyEquals(entities.get(i + 1));
	}

	@Override
	void setVarName(String varName) {
		this.varName = varName;
	}

	public List<GroupedEntityIdListElem> getGroupedList() {
		return groupedList;
	}

	public List<String> getAllGroupedEntityIdsCode() {
		List<String> result = new ArrayList<>();
		for (GroupedEntityIdListElem elem : groupedList) {
			result.add(elem.toCode());
		}
		return result;
	}

	@Override
	public String getVariableName() {
		if (varName == null) {
			throw new RuntimeException("VariableName has not been set for this entity.");
		}
		return varName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((groupedList == null) ? 0 : groupedList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GroupedEntityIdList other = (GroupedEntityIdList) obj;
		if (groupedList == null) {
			if (other.groupedList != null) {
				return false;
			}
		} else if (!groupedList.equals(other.groupedList)) {
			return false;
		}
		return true;
	}
}
