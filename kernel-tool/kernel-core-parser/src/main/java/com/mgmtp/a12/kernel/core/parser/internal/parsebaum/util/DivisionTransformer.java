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

package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.CompositeOperationImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ICompositeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.CalcOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;

/**
 * Wandelt alle zusammengesetzten FeldOperationen der Form
 * a * (b/c) in (a * b) / c .
 * Falls mehrere Divisionen in einer ZusgesetztenFeldOperationen enthalten sind
 * also a * (b/c) * (d/e) wird umgeformt in (a*b*d) / (c*e)
 */
public class DivisionTransformer extends ASTVisitor {

	/**
	 * Die zusammengesetzte FeldOperation ist besucht worden.
	 * Es wird geprüft
	 * <ol>
	 * <li>berechnet die zusammengesetzte FeldOperation eine Multiplikation</li>
	 * <li>besteht ein Multiplikator aus einer Division</li>
	 * </ol>
	 * Falls ja, wird die Zusammengesetzte FeldOperation umgebaut.
	 */
	@Override
	public void endVisit(ICompositeOperation op) {
		if (op.getCalcOp() != CalcOp.MULTIPLY) {
			return;
		}

		List<IOperation> divFopListe = getListeDerDivisionsOperatoren(op.getOperations());

		if (divFopListe.isEmpty()) {
			return; //keine Divisionen gefunden, höre auf
		}

		List<IOperation> neueZaehlerOps = getListeNeueZaehler(divFopListe);
		List<IOperation> neueTeilerOps = getListeNeueTeiler(divFopListe);

		//Lösch die alten divisionen
		loeschDivOperationen(op, divFopListe);

		//nehme die ZaehlerOps zu den alten ops (ohne die divisionen)
		addZaehlerOperationen(op, neueZaehlerOps);

		IOperation neuerZaehler = createFop(op.getOperations());
		IOperation neuerTeiler = createFop(neueTeilerOps);

		setOpToDivision(op, neuerZaehler, neuerTeiler);
	}

	@Override
	public void endVisit(IEntityId p) {
		super.endVisit(p);
		if (p.getFilterCondition() != null) {
			p.getFilterCondition().traverse(this);
		}
	}

	private void addZaehlerOperationen(ICompositeOperation op, List<IOperation> neueZaehlerOps) {
		for (IOperation z : neueZaehlerOps) {
			op.getOperations().add(z);
		}
	}

	private void loeschDivOperationen(ICompositeOperation op, List<IOperation> divFopListe) {
		List<IOperation> ops = new ArrayList<>(op.getOperations());
		for (IOperation div : divFopListe) {
			ops.remove(div);
		}
		op.setOperations(ops);
	}

	private List<IOperation> getListeDerDivisionsOperatoren(List<IOperation> fops) {
		return fops.stream().filter(this::isDivision).collect(Collectors.toList());
	}

	private List<IOperation> getListeNeueZaehler(List<IOperation> divFops) {
		return getListeIntern(divFops, 0);
	}

	private List<IOperation> getListeNeueTeiler(List<IOperation> divFops) {
		return getListeIntern(divFops, 1);
	}

	private List<IOperation> getListeIntern(List<IOperation> divFops, int index) {
		List<IOperation> result = new ArrayList<>();
		for (IOperation divOp : divFops) {
			ICompositeOperation zop = (ICompositeOperation) divOp;
			assert (zop.getOperations().size() == 2); //Mehrfach division ist nicht erlaubt
			result.add(zop.getOperations().get(index));
		}
		return result;
	}

	/*
	 * Erzeugt eine Feldoperation aus der Liste der Teiloperationen.
	 */
	private IOperation createFop(List<IOperation> neueTeilerOps) {
		if (neueTeilerOps.size() == 1) {
			return neueTeilerOps.get(0);
		}
		return new CompositeOperationImpl(neueTeilerOps, CalcOp.MULTIPLY, OperationType.NUMBER, true);
	}

	/**
	 * Prüft ob die übergebene FeldOperation eine Division beschreibt.
	 *
	 * @param fop
	 * 		zu prüfende FeldOperation
	 * @return true, falls eine Division beschrieben wird.
	 */
	private boolean isDivision(IOperation fop) {
		if (fop instanceof ICompositeOperation) {
			ICompositeOperation zop = (ICompositeOperation) fop;
			return (zop.getCalcOp() == CalcOp.DIVIDE);
		}
		return false;
	}

	/**
	 * Setzt die übergebene Zusammengesetzte FeldOperation in eine Division um, in der Form
	 * neuer Divident / divisor
	 *
	 * @param op
	 * 		Operation, die geändert wird
	 * @param neuerZaehler
	 * 		neuer Divident
	 * @param neuerTeiler
	 * 		Divisor (alter Divisor)
	 */
	private void setOpToDivision(ICompositeOperation op, IOperation neuerZaehler, IOperation neuerTeiler) {
		assert (op.getCalcOp() == CalcOp.MULTIPLY);
		op.setCalcOp(CalcOp.DIVIDE);
		List<IOperation> fops = Arrays.asList(neuerZaehler, neuerTeiler);
		op.setOperations(fops);
	}

	/**
	 * Hilfsmethode zum Aufruf der Transformation
	 *
	 * @param b
	 * 		zu transformierende Bedingung
	 */
	public static <T extends IParseTreeEntity> T transform(T b) {
		assert (b != null);
		DivisionTransformer dt = new DivisionTransformer();
		b.traverse(dt);
		return b;
	}
}
