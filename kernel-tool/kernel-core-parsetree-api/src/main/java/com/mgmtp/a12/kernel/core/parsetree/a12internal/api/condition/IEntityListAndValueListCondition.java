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
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListAndValueListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Schnittstelle zu allen Bedingungen zum Prüfen einer EntityListe
 * gegen eine Liste von Werten.
 * Werte können entweder eine Entity-Liste sein oder eine Liste von String-Konstanten
 * D.h. entweder
 * {@link IEntityListAndValueListCondition#getValueListAsStringList} gibt eine
 * leere Liste zurück oder {@link IEntityListAndValueListCondition#getValueListAsEntityList}.
 * Es können nicht beide Listen leer sein und es können nicht beide Listen nicht leer sein.
 * Eine Prüfung, ob eine Liste leer ist, kann daher immer als Entscheidung verwendet werden, um
 * zu erkennen auf welche Art die Parameter spezifiziert wurden.
 */
@OnlyForUsage
public interface IEntityListAndValueListCondition extends ICondition {
	/**
	 * @return Name der Bedingung, immer ungleich <code>null</code>
	 */
	public EntityListAndValueListConditionName getConditionName();

	/**
	 * Die Liste der Parameter, die geprüft werden sollen können Felder und
	 * Kontexte enthalten. In der Grammatik ist beides erlaubt.
	 * Die Liste ist nie leer, dies wird bereits von der Grammatik zugesichert.
	 * Die Liste enthält keine Duplikate, dies wird bereits von der Grammatik zugesichert.
	 *
	 * @return Liste der Entitäten, die geprüft werden sollen - immer ungleich <code>null</code>
	 */
	public IEntityIdList getEntityList();

	/**
	 * Wenn die Werteliste über String-Konstanten bestimmt werden,
	 * wird eine leere Liste zurückgegeben.
	 *
	 * @return gibt die Liste von EntityIds zurück, die die Werte bestimmen, gibt nie
	 * 		<code>null</code> zurück sondern eine leere Liste, falls die Werte über
	 * 		String-Konstanten bestimmt werden
	 */
	public IEntityIdList getValueListAsEntityList();

	/**
	 * @return gibt die Liste der String-Konstanten zurück, die die Werte bestimmen, gibt nie
	 * 		<code>null</code> zurück sondern eine leere Liste, falls die Werte über
	 * 		eine Liste von EntityIds bestimmt werden
	 */
	public List<String> getValueListAsStringList();

	/**
	 * @return the list of string-constants or {@link IEntityId}s, which may be mixed. Never returns
	 * 		<code>null</code> but an empty list if not applicable.
	 */
	public List<Object> getValueListAsMixedList();

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		if (visitor.visit(this)) {
			getEntityList().traverse(visitor);
			// Only traverse value list if it contains (also) entities
			if (visitor.visitNext(1, this) && getValueListAsStringList().isEmpty()) {
				getValueListAsMixedList().stream()
						.filter(x -> x instanceof IEntityId)
						.forEach(x -> ((IEntityId) x).traverse(visitor));
				if (!getValueListAsEntityList().getEntityIdList().isEmpty()) {
					getValueListAsEntityList().traverse(visitor);
				}
			}
			visitor.endVisit(this);
		}
	}

}
