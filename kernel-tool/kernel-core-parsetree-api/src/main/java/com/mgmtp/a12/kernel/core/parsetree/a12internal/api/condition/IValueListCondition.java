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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition;

import java.util.List;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ValueListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Schnittstelle zu den Überprüfungen, die ein Feld gegen eine Liste von Werten prüfen.
 * Die Werteliste kann
 * <ul>
 * <li>eine Liste von Stringkonstanten</li>
 * <li>eine Liste von Zahlkonstanten</li>
 * <li>oder eine Liste von Feldwert-Operationen sein.</li>
 * </ul>
 * Eine Mischung ist nach der Grammatik nicht erlaubt.
 */
@OnlyForUsage
public interface IValueListCondition extends ICondition {

	/**
	 * @return Name der Bedingung
	 */
	public ValueListConditionName getConditionName();

	/**
	 * @return Feld das geprüft werden soll.
	 */
	public IEntityId getField();

	/**
	 * Als Operationen in der Liste sind entweder Feldwert Operationen, Zahlkonstanten
	 * oder String-Konstanten möglich. Andere Operationen werden über die Grammatik nicht erlaubt.
	 *
	 * @return Liste der Werte gegen die geprüft werden soll als eine Liste von
	 * 		Operationen. Die Liste ist nie leer.
	 */
	public List<IOperation> getValueList();

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		if (visitor.visit(this)) {
			getField().traverse(visitor);
			for (int i = 0; i < getValueList().size(); i++) {
				getValueList().get(i).traverse(visitor);
				if (!visitor.visitNext(i, this)) {
					break;
				}
			}
			visitor.endVisit(this);
		}
	}
}
