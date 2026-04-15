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

package com.mgmtp.a12.kernel.core.calc.internal.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService.RoundingMode;

/**
 * Modelliert die spezielle Struktur der Berechnungen. Dazu zählt das berechnete Feld, die einzelnen
 * Vorbedingungen und die Berechnungsfunktionen. Zusätzlich wird auch die Rundungsart gesetzt.
 */
public class CalculationInfo implements ICalculationInfo {
	private final IEntityId berechnetesFeld;
	private final ICondition commonPrecondition;
	private final List<Pair<ICondition, IOperation>> teilberechnungen;
	private final RoundingMode rundungsArt;
	private final String fullName;

	public CalculationInfo(IEntityId berechnetesFeld, ICondition commonPrecondition,
			List<Pair<ICondition, IOperation>> teilberechnungen, RoundingMode rundungsArt, String fullName) {
		this.berechnetesFeld = berechnetesFeld;
		this.teilberechnungen = Collections.unmodifiableList(teilberechnungen);
		this.rundungsArt = rundungsArt;
		this.fullName = fullName;
		this.commonPrecondition = commonPrecondition;
	}

	@Override
	public IEntityId getCalculatedField() {
		return berechnetesFeld;
	}

	@Override
	public Optional<ICondition> getCommonPrecondition() {
		return Optional.ofNullable(commonPrecondition);
	}

	@Override
	public List<Pair<ICondition, IOperation>> getPartialCalculations() {
		return teilberechnungen;
	}

	@Override
	public RoundingMode getRoundingMode() {
		return rundungsArt;
	}

	@Override
	public String getName() {
		return fullName;
	}
}
