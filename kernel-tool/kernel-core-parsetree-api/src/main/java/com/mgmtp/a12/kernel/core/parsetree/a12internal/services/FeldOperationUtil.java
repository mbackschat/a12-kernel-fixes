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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.services;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperationWithValue;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.FieldIdOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Hilfsmethoden für Feldoperationen.
 */
@OnlyForUsage
public class FeldOperationUtil {

	private FeldOperationUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Hilfsmethode: ist die Angegebene FeldOperation vom Typ AnzahlAngegebenerFelder oder
	 * AnzahlAngegebenerKontexte ? Wenn ja,
	 * wird diese zurückgegeben, sonst null.
	 */
	public static IEntityListOperation getAnzahlAngegebnerFelderOrKontexteOp(IOperation op) {
		if (op instanceof IEntityListOperation) {
			IEntityListOperation fop = (IEntityListOperation) op;
			EntityListOperationName name = fop.getOperatorName();
			if (name == EntityListOperationName.NumberOfFilledFields ||
					name == EntityListOperationName.NumberOfFilledGroups) {
				return fop;
			}
		}
		return null;
	}

	/**
	 * Hilfsmethode: ist die Angegebene FeldOperation vom Typ AnzahlAngegebenerFelder oder
	 * AnzahlAngegebenerKontexte oder AnzahlVerschiedenerWerte ? Wenn ja,
	 * wird diese zurückgegeben, sonst null.
	 */
	public static IEntityListOperation getAnzahlAngegebnerFelderOrKontexteOrVerschiedenerWerteOp(IOperation op) {
		if (op instanceof IEntityListOperation) {
			IEntityListOperation fop = (IEntityListOperation) op;
			EntityListOperationName name = fop.getOperatorName();
			if (name == EntityListOperationName.NumberOfFilledFields
					|| name == EntityListOperationName.NumberOfFilledGroups
					|| name == EntityListOperationName.NumberOfDifferentValues) {
				return fop;
			}
		}
		return null;
	}

	public static boolean enthaeltAnzahlAngegebenOrAnzahlVerschiedeneWerteSprachkonstrukt(IParseTreeEntity op) {
		boolean result[] = new boolean[1];
		ASTVisitor visitor = new ASTVisitor() {
			@Override public boolean visitGeneral(IParseTreeEntity e) {
				return !result[0];
			}

			@Override public boolean visit(IEntityListOperation p) {
				result[0] |= switch (p.getOperatorName()) {
					case NumberOfFilledFields, NumberOfFilledGroups, NumberOfDifferentValues -> true;
					default -> false;
				};
				return false;
			}

			@Override
			public void endVisit(INumberOfRootGroupsOperation p) {
				result[0] = true;
			}
		};
		// Note: it appears to be intentional that filter conditions are not traversed here (but we do want to traverse setoperations aka SummeVonTermen)
		op.traverse(visitor);
		return result[0];
	}

	/**
	 * Hilfsmethode: ist die Angegebene FeldOperation vom Typ AnzahlWertInFeldliste? Wenn ja,
	 * wird diese zurückgegeben, sonst null.
	 */
	public static IEntityListOperationWithValue getAnzahlWertInFeldliste(IOperation op) {
		if (op instanceof IEntityListOperationWithValue) {
			IEntityListOperationWithValue fop = (IEntityListOperationWithValue) op;
			EntityListOperationName name = fop.getOperatorName();
			if (name == EntityListOperationName.NumberOfValueInFields) {
				return fop;
			}
		}
		return null;
	}

	/**
	 * Gibt den ganzzahligen Wert von op zurück, falls diese eine Zahlkonstante ist und -1
	 * sonst.
	 */
	public static int getZahlKonstanteWert(IOperation op) {
		if (isZahlKonstante(op)) {
			IConstantOperation fop = (IConstantOperation) op;
			double value = Double.parseDouble(fop.getValue());
			long l = Math.round(value);
			return (int) l;
		}
		return -1;

	}

	public static boolean isZahlKonstante(IOperation op) {
		return op instanceof IConstantOperation && op.getType() == OperationType.NUMBER;
	}
}
