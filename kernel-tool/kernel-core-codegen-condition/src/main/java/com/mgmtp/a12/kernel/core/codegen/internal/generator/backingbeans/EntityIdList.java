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
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;

/**
 * Parameter class to produce code for an entity list.
 */
@StringTemplateGroup("entityid")
public class EntityIdList extends STParameterClassBase implements IEntityIdList {

	protected final List<EntityId> entities;
	private String varName = null;

	public EntityIdList(List<EntityId> liste) {
		this.entities = Collections.unmodifiableList(liste);
	}

	public EntityIdList() {
		this.entities = Collections.emptyList();
		this.varName = "unknown";
	}

	EntityIdList(EntityIdList list) {
		this.entities = list.entities;
	}

	void setVarName(String varName) {
		this.varName = varName;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<IEntityId> getEntityIdList() {
		return (List) entities;
	}

	public List<String> getAllEntityIds() {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < entities.size(); i++) {
			EntityId id = entities.get(i);
			result.add(id.getVariableName());
		}
		return result;
	}

	public boolean getHasEntityIds() {
		return !entities.isEmpty();
	}

	public String getVariableName() {
		if (varName == null) {
			throw new RuntimeException("VariableName has not been set for this entity.");
		}
		return varName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
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
		EntityIdList other = (EntityIdList) obj;
		if (entities == null) {
			if (other.entities != null) {
				return false;
			}
		} else if (!entities.equals(other.entities)) {
			return false;
		}
		return true;
	}
}
