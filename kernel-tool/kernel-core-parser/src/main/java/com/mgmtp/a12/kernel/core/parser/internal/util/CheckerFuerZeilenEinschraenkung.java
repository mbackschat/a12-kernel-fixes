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

package com.mgmtp.a12.kernel.core.parser.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserError;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorHandler;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.services.FeldOperationUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * Überprüft für eine Regelbedingung, ob diese eine Einschränkung für die Zeile
 * modelliert. Die Methode
 * {@link #checkAndSaveAnwendungsSpezifischeZeile(ModelChanges, Map, IEC, IProblemReporter, Locale)}
 * bietet den Extraktionsservice für den Aufrufer an.
 */
public class CheckerFuerZeilenEinschraenkung extends com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor {

	private IRule regel;
	private List<IRule> rulesModellingRestriction = new ArrayList<>();
	private IProblemReporter problemManager;
	private Locale locale;
	private Map<String, Long> fieldnameToMaxRows = new HashMap<>();

	/**
	 * Methode überprüft, ob Regeln eine anwendungsspezifische Zeilennummer modellieren. Falls ja,
	 * werden die entsprechenden Regeln und die zu ändernde Information bei den betroffenen Feldern
	 * in modelChanges gespeichert.
	 */
	public static void checkAndSaveAnwendungsSpezifischeZeile(ModelChanges modelChanges, Map<IRule, ICondition> regeln, IEC ec,
			IProblemReporter problemManager, Locale locale) {
		CheckerFuerZeilenEinschraenkung tester = new CheckerFuerZeilenEinschraenkung(problemManager, locale);

		// Teste alle Regeln, beim Traversieren wird ermittelt,  ob die Regel eine Zeileneinschränkung formuliert.
		for (Entry<IRule, ICondition> entry : regeln.entrySet()) {
			IRule r = entry.getKey();
			ICondition condition = entry.getValue();
			tester.setRegel(r);
			condition.traverse(tester);
		}

		// markiere zu löschende Regeln
		for (IRule rule : tester.rulesModellingRestriction) {
			modelChanges.addRuleModellingMaxValueRestrictions(rule);
		}

		// setze neuen MZI
		for (String fieldName : tester.fieldnameToMaxRows.keySet()) {
			IEntity field = ec.get(fieldName);
			Long newMaxRows = tester.fieldnameToMaxRows.get(fieldName);
			if (newMaxRows < field.getMax()[field.getMax().length - 1]) {
				modelChanges.setNewMaxValue(field, field.getMax().length - 1, newMaxRows);
			}
		}
	}

	private void setRegel(IRule regel) {
		this.regel = regel;
	}

	private CheckerFuerZeilenEinschraenkung(IProblemReporter problemManager, Locale locale) {
		this.problemManager = problemManager;
		this.locale = locale;
	}

	/**
	 * eine Zusammengesetzte Bedingung kann nie eine Einschränkung für die max Zeile
	 * spezifizieren.
	 */
	@Override
	public boolean visit(ICompositeCondition b) {
		return false;
	}

	@Override
	public boolean visit(IComparisonCondition b) {
		IOperation op1 = b.getOperation1();
		IOperation op2 = b.getOperation2();
		ComparisonOperation vglOp = b.getCmpOp();

		//Prüfe ob Bedingung der Form MaximalAngegebenerZeilenindex(Feld) > 10 ist
		if (isMZIFeldOp(op1, op2, vglOp, true)) {
			return false;
		}
		//Prüfe ob Bedingung der Form 10 < MaximalAngegebenerZeilenindex(Feld) ist
		if (isMZIFeldOp(op2, op1, vglOp, false)) {
			return false;
		}
		return false;
	}

	/**
	 * Überprüft ob die erste FeldOperation eine Zeileneinschränkung formuliert und die zweite
	 * FeldOperation eine Konstante zahl Ist
	 *
	 * @param op1
	 * 		Erster operand
	 * @param op2
	 * 		Zweiter Operand
	 * @param vglOp
	 * 		die Methode wird mit jeweils gedrehten Parametern aufgerufen - daher muss
	 * 		übergeben werden, ob nach größer (gleich)
	 * 		bzw. kleiner(gleich) geprüft werden soll
	 * @param checkGreater
	 * 		falls true, soll die Vergleichsoperation > bzw. >= sein
	 * 		(MaximalAngegebenerZeilenindex(Feld) > 10
	 * @return true, falls eine solche Operation gefunden wurde.
	 */
	private boolean isMZIFeldOp(IOperation op1, IOperation op2, ComparisonOperation vglOp, boolean checkGreater) {

		// Prüfe, ob die richtigen Vergleichsoperationen verwendet werden.
		if (!checkVglOp(vglOp, checkGreater)) {
			return false;
		}

		if (!checkOps(op1, op2)) {
			return false;
		}

		long neueMaxZeilen = getNeueMaxZeilen(op2, vglOp);

		if (neueMaxZeilen == 1) {
			// Eine Einschränkung auf 1 Zeile ist nicht zulässig (siehe EXT-8606).
			problemManager.reportProblem(
					new MVKParserError(regel, new ParserErrorHandler(locale).getParserErrorMessage(
							ParserErrorTypesEnumForHModel.MVK_MAX_INDEX_RESTRICTION_TO_ONE, "Zeilenindex")));
			return false;
		} else if (neueMaxZeilen < 1) {
			return false;
		}

		//Alle Prüfungen sind durch: Setze die neue max. Zeilen des Feldes und markiere die Regel als eine zu löschende Regel.
		rulesModellingRestriction.add(regel);

		// durch den Parser ist sichergestellt, dass keine Kontexte referenziert werden
		String fieldname = ((IEntityListOperation) op1).getEntityList().getEntityIdList().get(0).getFullName();
		Long bereitsGesetzteMaxZeilen = fieldnameToMaxRows.get(fieldname);
		if (bereitsGesetzteMaxZeilen != null && bereitsGesetzteMaxZeilen < neueMaxZeilen) {
			return false;
		}
		fieldnameToMaxRows.put(fieldname, neueMaxZeilen);

		return true;
	}

	/**
	 * Überprüfe, ob der Vergleich für die Zeileneinschränkung korrekt ist.
	 *
	 * @return true, wenn es sich um den richtigen Vergleich handelt.
	 */
	private boolean checkVglOp(ComparisonOperation vglOp, boolean checkGreater) {

		if (checkGreater) {
			if (vglOp != ComparisonOperation.GT && vglOp != ComparisonOperation.GE) {
				return false;
			}
		} else {
			if (vglOp != ComparisonOperation.LT && vglOp != ComparisonOperation.LE) {
				return false;
			}
		}
		return true;
	}

	private boolean checkOps(IOperation op1, IOperation op2) {
		if (!(op1 instanceof IEntityListOperation)) {
			return false;
		}
		IEntityListOperation flop = (IEntityListOperation) op1;
		if (flop.getEntityList().getEntityIdList().size() != 1) {
			return false;
		}
		if (flop.getOperatorName() != EntityListOperationName.MaximalAngegebenerZeilenindex) {
			return false;
		}

		if (!(FeldOperationUtil.isZahlKonstante(op2))) {
			return false;
		}
		return true;
	}

	/**
	 * Ermittle die neuen MaxZeilen
	 *
	 * @param op
	 * 		Zahlkonstante-Feldoperation
	 * @param vglOp
	 * 		wird benötigt, weil bei Vergleich auf >= bzw. <= muss jeweils die maximale zeile
	 * 		eins weniger sein.
	 * @return neue Max-Zeilen
	 */
	private long getNeueMaxZeilen(IOperation op, ComparisonOperation vglOp) {
		IConstantOperation zop = (IConstantOperation) op;
		long neueMaxZeilen = Long.parseLong(zop.getValue());

		if (vglOp == ComparisonOperation.GE || vglOp == ComparisonOperation.LE) {
			neueMaxZeilen--;
		}
		return neueMaxZeilen;
	}
}
