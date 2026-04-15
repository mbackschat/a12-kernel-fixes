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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ComparisonOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.common.VordruckIntern;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.services.FeldOperationUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FeldIdUtilities;

/**
 * Visitor, der ausgehend von einer Menge von Pflichtfeldinformationen und einer Menge von
 * Pflicht-Vordrucken, ermittelt, ob eine Bedingung immer zu wahr evaluiert. Dies ist nach der
 * Traversierung der Bedingung mit {@link EvaluatorVisitor#isTrue()} abfragbar.<br>
 * Wird als aktuelle LfdNr 0 übergeben und spezifiziert die Bedingung LfdNr-Muster, so wird geprüft,
 * ob die Bedingung für alle LfdNummern wahr ist, sonst nur für die aktuelle LfdNr.
 */
public class EvaluatorVisitor extends ASTVisitor {

	private final Set<PflichtInformation> pfInfos;
	private final Set<VordruckIntern> vordrucke;
	private long aktLfdNr;
	private boolean istImmerTrue = false;

	public EvaluatorVisitor(Set<PflichtInformation> pfInfos, Set<VordruckIntern> pflVordrucke, long aktLfdNr) {
		this.pfInfos = pfInfos;
		this.vordrucke = pflVordrucke;
		this.aktLfdNr = aktLfdNr;
	}

	@Override
	public boolean visit(IComparisonCondition b) {
		IOperation op1 = b.getOperation1();
		IOperation op2 = b.getOperation2();

		IEntityListOperation fop = FeldOperationUtil.getAnzahlAngegebnerFelderOrKontexteOp(op1);
		int z = FeldOperationUtil.getZahlKonstanteWert(op2);
		Boolean mussMitGleichVerglichenWerden = null;

		if (fop != null && z != -1) {
			// AnzahlFelder >= Konstante
			if (b.getCmpOp().equals(ComparisonOperation.GE)) {
				mussMitGleichVerglichenWerden = true;
			}
			// AnzahlFelder > Konstante
			if (b.getCmpOp().equals(ComparisonOperation.GT)) {
				mussMitGleichVerglichenWerden = false;
			}
			if (mussMitGleichVerglichenWerden == null) {
				// andere Vergleichsbedingung
				return false;
			}
		}

		if (fop == null) {
			fop = FeldOperationUtil.getAnzahlAngegebnerFelderOrKontexteOp(op2);
			z = FeldOperationUtil.getZahlKonstanteWert(op1);
		}

		if (fop == null || z == -1) {
			return false;
		}
		if (b.getCmpOp().equals(ComparisonOperation.LE)) {
			// Konstante <= AnzahlFelder/Kontexte
			mussMitGleichVerglichenWerden = true;
		}
		if (b.getCmpOp().equals(ComparisonOperation.LT)) {
			// Konstante < AnzahlFelder/Kontexte
			mussMitGleichVerglichenWerden = false;
		}
		if (mussMitGleichVerglichenWerden == null) {
			return false;
		}

		IEntityIdList felderListe = fop.getEntityList();
		Collection<IEntityId> felder = felderListe.getEntityIdList();
		int anzahl = getAnzahlPflichtfelder(felder);
		if (mussMitGleichVerglichenWerden && anzahl >= z) {
			istImmerTrue = true;
		}

		if (!mussMitGleichVerglichenWerden && anzahl > z) {
			istImmerTrue = true;
		}

		return false;
	}

	private int getAnzahlPflichtfelder(Collection<IEntityId> felder) {
		int result = 0;
		for (IEntityId id : felder) {
			if (mindEinsMussAngegebenSein(id)) {
				result++;
			}
		}
		return result;
	}

	@Override
	public boolean visit(IEntityListCondition b) {
		List<IEntityId> ids = b.getEntityList().getEntityIdList();
		switch (b.getConditionName()) {
			case AtLeastOneFieldFilled:
				for (IEntityId iid : ids) {
					// Even if a field has to be specified,
					// with a filter condition this does not necessarily make the condition true.
					if (iid.getFilterCondition() != null) {
						continue;
					}
					if (mindEinsMussAngegebenSein(iid)) {
						istImmerTrue = true;
						break;
					}
				}
				return false;
			case AllFieldsFilled:
				for (IEntityId id : ids) {
					//Innerhalb der Liste gibt es mehrzeilige Felder, die kein Zeilen
					//Muster spezifizieren.  Daher kann die Lösung nicht true werden
					if (!FeldIdUtilities.isEinzeilig(id) && !FeldIdUtilities.isZIteration(id)) {
						return false;
					}
				}

				// alle referenzierten Felder sind entweder einzeilig, oder sie verwenden
				// ein Zeilenmuster: püfe ob alle referenzierten Felder Pflichtfelder sind.
				for (IEntityId id : ids) {
					if (!alleMuessenAngegebenSein(id)) {
						return false;
					}
				}
				istImmerTrue = true;
				return false;
			default:
				return false;
		}
	}

	private boolean alleMuessenAngegebenSein(IEntityId id) {
		for (PflichtInformation pInfo : pfInfos) {
			for (PflichtIdentifier pf : pInfo.getpMenge()) {
				if (!id.getFullName().equals(pf.getFullName())) {
					continue;
				}
				if (FeldIdUtilities.isLIteration(id)) {
					return pf.getLfdNummer() == aktLfdNr
							|| pf.getLfdNummer() == IParseTreeEntity.WILDCARD
							|| (pf.getLfdNummer() == IParseTreeEntity.ITERATION && aktLfdNrInBereich(pInfo, pf.getMaxLfdNr()));
				}

				if (FeldIdUtilities.isLStern(id)) {
					return pf.getLfdNummer() == IParseTreeEntity.WILDCARD
							|| (id.getRootGroupWildcardStart() <= pf.getLfdNummer() && pf.getLfdNummer() <= id.getRootGroupWildcardEnd());
				}
				long l = FeldIdUtilities.getLfdNr(id);
				if (FeldIdUtilities.isLKonkret(id)
						&& (pf.getLfdNummer() == l
						|| pf.getLfdNummer() == IParseTreeEntity.WILDCARD
						|| (pf.getLfdNummer() == IParseTreeEntity.ITERATION && aktLfdNr == l))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean mindEinsMussAngegebenSein(IEntityId id) {
		for (PflichtInformation pInfo : pfInfos) {
			for (PflichtIdentifier pf : pInfo.getpMenge()) {
				if (!id.getFullName().equals(pf.getFullName())) {
					continue;
				}
				if (isMehrzeligUndZeileKonkret(id)) {
					return false;
				}
				if (FeldIdUtilities.isLIteration(id)
						&& (pf.getLfdNummer() == aktLfdNr
						|| pf.getLfdNummer() == IParseTreeEntity.WILDCARD
						|| (pf.getLfdNummer() == IParseTreeEntity.ITERATION && aktLfdNrInBereich(pInfo, pf.getMaxLfdNr())))) {
					return true;
				}
				long l = FeldIdUtilities.getLfdNr(id);
				if (FeldIdUtilities.isLKonkret(id)
						&& (pf.getLfdNummer() == l
						|| pf.getLfdNummer() == IParseTreeEntity.WILDCARD
						|| (pf.getLfdNummer() == IParseTreeEntity.ITERATION && aktLfdNr == l))) {
					return true;
				}
				if (FeldIdUtilities.isLStern(id)
						&& (pf.getLfdNummer() == IParseTreeEntity.WILDCARD
						|| (pf.getLfdNummer() >= id.getRootGroupWildcardStart() && pf.getLfdNummer() <= id.getRootGroupWildcardEnd()))) {
					return true;
				}
			}
		}
		return false;

	}

	/**
	 * Liefert <code>true</code>, wenn die zu untersuchende laufende Nummer {@link #aktLfdNr} im
	 * LfdNr-Bereich liegt, für den die übergebene {@link PflichtInformation} gültig ist.<br>
	 * Im Sonderfall, dass die aktuelle LfdNr 0 ist, wird <code>true</code> zurückgegeben, wenn die
	 * übergebene {@link PflichtInformation} für alle LfdNummern zw. 1 und maxLfdNr gültig ist.
	 */
	private boolean aktLfdNrInBereich(PflichtInformation pInfo, long maxLfdNr) {
		return (aktLfdNr == 0 && pInfo.getLfdNrStart() == 1 && pInfo.getLfdNrEnde() == maxLfdNr)
				|| (pInfo.getLfdNrStart() <= aktLfdNr && aktLfdNr <= pInfo.getLfdNrEnde());

	}

	/**
	 * f muss ein Feld sein, kein Kontext.
	 *
	 * @param id
	 * 		- ein Feld
	 * @return <code>true</code>, wenn das Feld mehrzeilig ist oder eine konkrete Zeile spezifiziert
	 */
	private boolean isMehrzeligUndZeileKonkret(IEntityId id) {
		return FeldIdUtilities.isMehrzeilig(id) && FeldIdUtilities.isZKonkret(id);
	}

	@Override
	public boolean visit(IRootGroupCondition b) {
		if (b.getConditionName() == RootGroupConditionName.MindestensEinVordruckAngegeben
				&& vordruckMengeUmfasstVordruck(vordrucke, b, aktLfdNr)) {
			istImmerTrue = true;
		}
		return false;
	}

	@Override
	public boolean visit(ICompositeCondition b) {
		if (b.getLogOp() == LogOp.Or) {
			return true; // bei oder traversiere weiter
		}

		/* Im Falle einer UND Verbindung mache eine Schnittmenge */
		for (ICondition teilbedingung : b.getConditionList()) {
			EvaluatorVisitor v = new EvaluatorVisitor(pfInfos, vordrucke, aktLfdNr);
			teilbedingung.traverse(v);
			if (!v.isTrue()) {
				return false;
			}
		}

		this.istImmerTrue = true;
		return false; // Es wird nicht mehr weiter traversiert.  Traversierung erfolgte über die Aufrufe der jeweiligen Visitors
	}

	public boolean isTrue() {
		return istImmerTrue;
	}

	/**
	 * Prüft, ob der in der Bedingung referenzierte Vordruck in der vordruckMenge vorhanden ist.
	 * Dabei wird zunächst geprüft, ob ein Vordruck mit diesem Namen vorhanden ist. Falls dies der
	 * Fall ist wird geprüft
	 * <li>Ist der Vordruck in der Menge als für alle Vordrucke markiert ?</li>
	 * <li>Referenziert die Bedingung keinen spezifischen Vordruck ?</li>
	 * <li>Stimmt der aktuelle
	 * Index mit dem Index in der Vordruckmenge überein.</li>
	 *
	 * @param vordruckMenge
	 * 		Menge der Vordrucke
	 * @param b
	 * 		Bedingung
	 * @param aktIndex
	 * 		aktuelle Index beim Suchen für ein Regelmuster
	 * @return gibt wahr zurück, falls der in der Bedingung referenzierte Vordruck in der Menge der
	 * 		Vordrucke enthalten ist.
	 */
	private boolean vordruckMengeUmfasstVordruck(
			Set<VordruckIntern> vordruckMenge,
			IRootGroupCondition b,
			long aktIndex) {

		String name = b.getRootGroupName();
		long suchIndex = 0;

		// Falls ein spezifischer Vordruck referenziert wurde:
		// Im Falle eines Regelmusters verwende den aktuellen Index der
		// als Parameter übergeben wurde.
		suchIndex = b.getRootGroupIndex() == IParseTreeEntity.ITERATION ? aktIndex : b.getRootGroupIndex();

		for (VordruckIntern v : vordruckMenge) {
			if (v.getName().equals(name)) {
				if (v.getLfdNummer() == 0) {
					return true;
				}
				if (v.getLfdNummer() == suchIndex) {
					return true;
				}
			}
		}
		return false;
	}

}
