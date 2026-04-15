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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung;

import java.util.Optional;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;

/**
 * Implementierung der Klasse für die FeldlistenBedingungen
 */
public class EntityListConditionImpl extends ConditionImpl implements IEntityListCondition {

	private final IEntityIdList paramListe;
	private EntityListConditionName bedingungName;
	private final IEntityId additionalGroupId;

	public EntityListConditionImpl(EntityListConditionName bedingungName, IEntityIdList felderListe, IEntityId additionalGroupId) {
		paramListe = felderListe;
		this.bedingungName = bedingungName;
		this.additionalGroupId = additionalGroupId;
	}

	public EntityListConditionImpl(IEntityListCondition c, IEntityIdList felderListe, IEntityId additionalGroupId) {
		super(c);
		paramListe = felderListe;
		this.bedingungName = c.getConditionName();
		this.additionalGroupId = additionalGroupId;
	}

	public String toString() {
		return String.format("%s(%s)", bedingungName.getRuntimeName(), paramListe);
	}

	@Override
	public EntityListConditionName getConditionName() {
		return bedingungName;
	}

	@Override
	public IEntityIdList getEntityList() {
		return paramListe;
	}

	@Override
	public Optional<IEntityId> getAdditionalGroupReference() {
		return Optional.ofNullable(additionalGroupId);
	}

	@Override
	public void setConditionName(EntityListConditionName name) {
		this.bedingungName = name;

	}

}
