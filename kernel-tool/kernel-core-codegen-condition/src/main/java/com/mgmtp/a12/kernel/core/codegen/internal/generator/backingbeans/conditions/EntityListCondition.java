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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions;

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdList;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.RuntimeMethodNameFactory;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;

/**
 *
 */
public class EntityListCondition extends CodeGenCondition implements IEntityListCondition {

	private final EntityIdList paramListe;
	private EntityListConditionName bedingungName;
	/*
	 * wird benötigt für die Sprachkonstrukte WiederholungNichtEindeutig und ZeileNichtEindeutig
	 * für diese Ebenen werden Duplikate gesucht. Bei WiederholungNichtEindeutig entspricht es den
	 * iterierenden Ebenen der Entities unterhalb der Ebene des Regelkontextes + 1, bei
	 * ZeileNichtEindeutig ist die Liste leer.
	 */
	private List<Integer> searchedDuplicateLayers;
	private final IEntityId additionalGroupReference;

	public EntityListCondition(EntityListConditionName conditionName, EntityIdList paramListe, EntityId additionalGroupReference) {
		this.paramListe = paramListe;
		this.bedingungName = conditionName;
		this.additionalGroupReference = additionalGroupReference;
	}

	@Override
	public EntityListConditionName getConditionName() {
		return bedingungName;
	}

	@Override
	public IEntityIdList getEntityList() {
		return paramListe;
	}

	public String getBedingungNameCode() {
		return RuntimeMethodNameFactory.getRuntimeMethodNameFromParseTreeObject(this);
	}

	/**
	 * Die Methode wird vom Template aus aufgerufen und liefert <code>true</code>, wenn es sich um
	 * das Sprachkonstrukt ZeileNichtEindeutig oder WiederholungNichtEindeutig handelt.<br>
	 * Die Unterscheidung ist nötig, da die Signatur der generierten Methoden anders ist als bei den
	 * restlichen Feldlistenbedingungen.
	 */
	public boolean isWiederholungNichtEindeutig() {
		return bedingungName == EntityListConditionName.ZeileNichtEindeutig ||
				bedingungName == EntityListConditionName.RepetitionNotUnique;
	}

	public List<Integer> getSearchedDuplicateLayers() {
		return searchedDuplicateLayers;
	}

	public void setSearchedDuplicateLayers(List<Integer> layers) {
		searchedDuplicateLayers = layers;
	}

	public String getFeldListeCode() {
		return paramListe.toCode();
	}

	@Override public Optional<IEntityId> getAdditionalGroupReference() {
		return Optional.ofNullable(this.additionalGroupReference);
	}

	@Override
	public void setConditionName(EntityListConditionName name) {
		this.bedingungName = name;
	}
}
