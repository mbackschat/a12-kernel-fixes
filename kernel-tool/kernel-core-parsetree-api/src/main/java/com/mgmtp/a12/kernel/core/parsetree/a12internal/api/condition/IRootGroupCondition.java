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

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Spezifiziert eine Bedingung für einen ganzen Vordruck.
 * Ein Vordruck entspricht einem root group / root context.
 */
@OnlyForUsage
public interface IRootGroupCondition extends ICondition {

	/**
	 * Als Name der Bedingung werden nicht alle syntaktisch verwendeten
	 * Namen zurückgegeben, da z.B. VordruckAngegeben und VordruckNichtAngegeben
	 * semantisch gleich-bedeutend sind zu MindestensEinVordruckAngegeben/KeinVordruckAngegeben
	 * mit einer ein-elementigen Liste.
	 *
	 * @return Name der Bedingung, gibt niemals <code>null</code> zurück
	 */
	public RootGroupConditionName getConditionName();

	/**
	 * als Vordruckname wird der Name ohne den slash zurückgegeben.
	 * d.h. dies entspricht <b>nicht</b> dem fullName des Wurzel-Kontextes.
	 *
	 * @return Name des Vordrucks, gibt niemals <code>null</code> oder Leerstring zurück
	 */
	public String getRootGroupName();

	/**
	 * Der Index kann entweder {@link IParseTreeEntity#ITERATION},
	 * {@link IParseTreeEntity#PARALLEL_ITERATION} oder
	 * {@link IParseTreeEntity#WILDCARD}
	 * sein. Nur in der alten ExtMP Welt ist auch eine konkrete Angabe der lfd. Nr. Vordruck
	 * möglich.
	 *
	 * @return Index-Spezifikation der laufenden Nummer
	 */
	public long getRootGroupIndex();

	/**
	 * nur relevant für die alte ExtMP Welt, in der neuen Welt wird
	 * immer {@link IParseTreeEntity#UNSPECIFIED} zurückgegeben.
	 * In der alten ExtMP Welt ist nur {@link IParseTreeEntity#ITERATION},
	 * {@link IParseTreeEntity#WILDCARD} oder {@link IParseTreeEntity#UNSPECIFIED} möglich,
	 * da die Spezifikation mit einem konkreten Index für den USB nicht zugelassen ist.
	 * <p>
	 * Wenn {@link IEntityId#getMaxUsb()} den Wert {@link IParseTreeEntity#UNSPECIFIED}
	 * liefert, liefert <code>getUsb()</code> auch immer {@link IParseTreeEntity#UNSPECIFIED}
	 *
	 * @return Index-Spezifikation des USB bzw. {@link IParseTreeEntity#UNSPECIFIED}
	 */
	public long getUsb();

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		if (visitor.visit(this) && getEntityId() != null) {
			getEntityId().traverse(visitor);
		}
		visitor.endVisit(this);
	}

	public IEntityId getEntityId();

}
