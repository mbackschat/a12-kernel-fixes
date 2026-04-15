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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdListImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListAndValueListConditionName;

/**
 *
 */
public class EntityListAndValueListConditionImpl extends ConditionImpl implements IEntityListAndValueListCondition {

	private final EntityListAndValueListConditionName bedingungName;

	private final IEntityIdList feldListe;
	private final IEntityIdList valueListAsFieldList;
	private final List<String> valueListAsStringList;
	private final List<Object> valueListAsMixedList;

	public EntityListAndValueListConditionImpl(EntityListAndValueListConditionName name, IEntityIdList feldListe,
			IEntityIdList wertListeAlsFeldListe, List<String> wertListeAlsStringListe, List<Object> valueListAsMixedList) {
		this.bedingungName = name;
		this.feldListe = feldListe;
		this.valueListAsFieldList = wertListeAlsFeldListe == null ?
				new EntityIdListImpl(new ArrayList<IEntityId>()) :
				wertListeAlsFeldListe;
		this.valueListAsStringList = wertListeAlsStringListe == null ? new ArrayList<>() : wertListeAlsStringListe;
		this.valueListAsMixedList = valueListAsMixedList == null ? Collections.emptyList() : valueListAsMixedList;
	}

	public EntityListAndValueListConditionImpl(IEntityListAndValueListCondition b, IEntityIdList feldListe,
			IEntityIdList wertListeAlsFeldListe, List<String> wertListeAlsStringListe, List<Object> mixedList) {
		super(b);
		this.bedingungName = b.getConditionName();
		this.feldListe = feldListe;
		this.valueListAsFieldList = wertListeAlsFeldListe == null ?
				new EntityIdListImpl(new ArrayList<IEntityId>()) :
				wertListeAlsFeldListe;
		this.valueListAsStringList = wertListeAlsStringListe == null ? new ArrayList<>() : wertListeAlsStringListe;
		this.valueListAsMixedList = mixedList == null ? Collections.emptyList() : mixedList;
	}

	public String toString() {
		if (!valueListAsFieldList.getEntityIdList().isEmpty()) {
			return String.format("%s(%s IN %s)", bedingungName.getRuntimeName(), feldListe, valueListAsFieldList);
		} else if (!valueListAsStringList.isEmpty()) {
			return String.format("%s(%s IN %s)", bedingungName.getRuntimeName(), feldListe, valueListAsStringList);
		} else {
			return String.format("%s(%s ZU %s)", bedingungName.getRuntimeName(), feldListe, valueListAsMixedList);
		}

	}

	@Override
	public EntityListAndValueListConditionName getConditionName() {
		return bedingungName;
	}

	@Override
	public IEntityIdList getEntityList() {
		return feldListe;
	}

	@Override
	public IEntityIdList getValueListAsEntityList() {
		return valueListAsFieldList;
	}

	@Override
	public List<String> getValueListAsStringList() {
		return valueListAsStringList;
	}

	@Override
	public List<Object> getValueListAsMixedList() {
		return valueListAsMixedList;
	}
}
