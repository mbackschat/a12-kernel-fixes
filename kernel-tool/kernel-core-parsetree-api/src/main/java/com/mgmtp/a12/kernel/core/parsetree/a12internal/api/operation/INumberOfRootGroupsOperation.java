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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Bietet die Schnittstelle für die Operation AnzahlAngegebenerVordrucke.
 * Bei diesem Sprachkonstrukt darf die laufende Nummer Vordruck nicht spezifiziert
 * werden, daher ist auch im Interface keine Methode zum Zugriff auf die
 * Spezifikation der lfd. Nr. enthalten.
 */
@OnlyForUsage
public interface INumberOfRootGroupsOperation extends IOperation {

	/**
	 * Nur H-Modelle (ExtMP) - sonst immer {@link IParseTreeEntity#UNSPECIFIED}
	 * Gibt die Index-Spezifikation für den USB zurück - dies kann nur
	 * {@link IParseTreeEntity#UNSPECIFIED}oder
	 * {@link IParseTreeEntity#ITERATION} sein, da beim Parsern überprüft wird,
	 * dass der USB keinen {@link IParseTreeEntity#WILDCARD} spezifiziert.
	 *
	 * @return Index-Spezifikation für den USB zurück, d.h. immer
	 *        {@link IParseTreeEntity#UNSPECIFIED},
	 * 		ausser wir sind in einem H-Modell (ExtMP) auf einem USB Vordruck.
	 */
	public long getUsb();

	/**
	 * als Vordruckname wird der Name ohne den slash zurückgegeben.
	 * d.h. dies entspricht <b>nicht</b> dem fullName des Wurzel-Kontextes.
	 * Vergleiche auch {@link IRootGroupCondition#getRootGroupName()}
	 *
	 * @return Name des Vordrucks, gibt niemals <code>null</code> oder Leerstring zurück
	 */
	public String getRootGroupName();

	/**
	 * default implementation of the traversal method.
	 * Do not provide a different implementation (except e.g. for logging)
	 */
	@Override
	public default void traverse(ASTVisitor visitor) {
		visitor.endVisit(this);
	}

}
