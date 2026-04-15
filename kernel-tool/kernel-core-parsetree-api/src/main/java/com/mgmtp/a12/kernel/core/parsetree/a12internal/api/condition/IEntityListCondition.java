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

import java.util.Optional;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Interface für alle Bedingungen, die als Parameter nur eine
 * Liste von Parametern (Felder oder Kontexte) haben.
 */
@OnlyForUsage
public interface IEntityListCondition extends ICondition {

	/**
	 * Als Name der Bedingung werden nicht alle syntaktisch verwendeten
	 * Namen zurückgegeben, da z.B. FeldAngegeben und FeldNichtAngegeben
	 * semantisch gleich-bedeutend sind zu AlleFelderAngegeben/KeinFeldAngegeben
	 * mit einer ein-elementigen Liste.
	 *
	 * @return Name der Bedingung, immer ungleich <code>null</code>
	 */
	public EntityListConditionName getConditionName();

	/**
	 * Setzt den Namen der Bedingung.
	 * Diese Methode wird verwendet, um den Namen einer Bedingung zu verändern.
	 * Dies ist nur notwendig, wenn der Parse-Baum transformiert werden soll.
	 */
	public void setConditionName(EntityListConditionName name);

	/**
	 * Durch die Grammatik ist sichergestellt, dass die Liste immer mindestens
	 * ein Element enthält. Beim Parsen ist zudem sichergestellt, dass die Liste
	 * keine Duplikate enthält und auch keine Kontexte bzw. Felder, die in einem Kontext
	 * der Parameterliste enthalten sind.
	 * Die Reihenfolge der Parameter in der Liste entspricht der Reihenfolge der Aufschreibung
	 * in der Regelbedingung.
	 *
	 * @return Parameterliste der Bedingung, immer ungleich <code>null</code>
	 */
	public IEntityIdList getEntityList();

	/**
	 * An optional reference to the reference point of a RepetitionNotUnique condition.
	 * This is only set if it is a RepetitionNotUnique condition and the reference point was explicitly specified.
	 */
	default Optional<IEntityId> getAdditionalGroupReference() {
		return Optional.empty();
	}

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		if (visitor.visit(this)) {
			getEntityList().traverse(visitor);
			visitor.endVisit(this);
		}
	}
}
