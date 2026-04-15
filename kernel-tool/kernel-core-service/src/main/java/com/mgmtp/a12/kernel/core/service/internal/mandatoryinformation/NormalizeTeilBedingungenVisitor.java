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

package com.mgmtp.a12.kernel.core.service.internal.mandatoryinformation;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;

/**
 * Klasse dient dazu, unnötige Verschachtelungen von zusammengesetzten Bedingungen im Parsebaum zu
 * eliminieren. Falls in einer Oder-Bedingung weitere geoderte Unterbedingungen vorhanden sind,
 * werden diese in dieselbe Ebene gezogen. Falls in einer Und-Bedingung weitere mit Und
 * zusammengesetzte Unterbedingungen sind, werden die auch in die selbe Ebene gezogen. D.h. aus A
 * oder (B oder C) wird A oder B oder C
 * <p>
 * bzw.
 * <p>
 * aus (A und B) und C wird A und B und C
 */
public class NormalizeTeilBedingungenVisitor extends ASTVisitor {

	@Override
	public void endVisit(ICompositeCondition bedingung) {
		List<ICondition> neueBedingungen = new ArrayList<>();
		for (ICondition teilBedingung : bedingung.getConditionList()) {
			neueBedingungen.addAll(getAlleLogOpTeilbedingungen(teilBedingung, bedingung.getLogOp()));
		}

		bedingung.getConditionList().clear();
		bedingung.getConditionList().addAll(neueBedingungen);
	}

	/**
	 * Ist b eine mit dem logischen Operator logOp zusammengesetzte Bedingung, so wird die Liste
	 * aller (direkten) Teilbedingungen zurückgegeben. Sonst die Bedingung b selbst.
	 */
	private List<ICondition> getAlleLogOpTeilbedingungen(ICondition b, LogOp logOp) {
		List<ICondition> result = new ArrayList<>();
		if (b instanceof ICompositeCondition) {
			ICompositeCondition zb = (ICompositeCondition) b;
			if (zb.getLogOp().equals(logOp)) {
				result = zb.getConditionList();
			}
		}
		if (result.isEmpty()) {
			result.add(b);
		}
		return result;
	}
}
