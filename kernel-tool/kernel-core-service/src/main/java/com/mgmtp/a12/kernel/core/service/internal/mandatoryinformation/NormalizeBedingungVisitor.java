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
import java.util.function.Predicate;

import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.FeldIdUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

public class NormalizeBedingungVisitor extends ASTVisitor {

	static final EntityListConditionName MIND_EIN_FELD = EntityListConditionName.AtLeastOneFieldFilled;
	static final EntityListConditionName NICHT_ALLE_FELDER = EntityListConditionName.NotAllFieldsFilled;
	static final EntityListConditionName KEIN_FELD = EntityListConditionName.NoFieldFilled;
	private List<ICondition> bedingungenToRemove = new ArrayList<>();

	private IEC mvkEc;

	public NormalizeBedingungVisitor(IEC mvkEc) {
		this.mvkEc = mvkEc;
	}

	/**
	 * hier erfolgt die Zusammenfassung geeigneter Teilbedingungen
	 */
	@Override
	public boolean visit(ICompositeCondition b) {
		if (b.getLogOp() == LogOp.Or) {

			verdichteTeilBedingungen(b, MIND_EIN_FELD);
			verdichteTeilBedingungen(b, NICHT_ALLE_FELDER);
		}
		if (LogOp.And == b.getLogOp()) {
			verdichteTeilBedingungen(b, KEIN_FELD);
		}
		return true; //traversiere weiter
	}

	/**
	 * Faßt alle Teilbedingungen von <b>b</b> vom Typ {@link IFeldListenBedingung} mit dem Namen
	 * <b>bedingungName</b> zu einer Bedingung zusammen.
	 */
	private void verdichteTeilBedingungen(
			ICompositeCondition b,
			EntityListConditionName bedingungName) {

		/*
		 * Hole alle Teilbedingungen die entweder eine Feldlistenbedingung mit Namen bedingungName
		 * sind, oder, falls bedingungName = NichtAlleFelderAngegeben oder bedingungName =
		 * KeinFeldAngegeben, dann hole auch alle FeldNichtAngegeben-Bedingungen
		 */
		final List<IEntityListCondition> bedingungenZumAendern = getRelevanteTeilBedingungen(b, bedingungName);

		if (bedingungenZumAendern.size() < 2) {
			//nichts zu tun aber traversiere weiter
			return;
		}

		//Nimm die erste Bedingung raus, diese wird erweitert, die anderen nachher
		//aus der Zusammengesetzten Bedingung gelöscht.
		final IEntityListCondition neueBedingung = bedingungenZumAendern.remove(0);
		//Bedingungname muss neu gesetzt werden, da neueBedingung auch eine FeldNichtAngegeben-Bedingung sein könnte,
		//wir wollen aber NichtAlleFelder- bzw. KeinFeldAngegeben um die neuen Felder ergänzen
		neueBedingung.setConditionName(bedingungName);
		final IEntityIdList felder = neueBedingung.getEntityList();

		for (IEntityListCondition fb : bedingungenZumAendern) {
			List<IEntityId> fbFelder = fb.getEntityList().getEntityIdList();
			for (IEntityId feld : fbFelder) {
				felder.getEntityIdList().add(feld);
			}
		}

		b.getConditionList().removeAll(bedingungenZumAendern);
	}

	private List<IEntityListCondition> getRelevanteTeilBedingungen(
			ICompositeCondition b,
			EntityListConditionName bedingungName) {
		List<IEntityListCondition> result = new ArrayList<>();
		for (ICondition teilbedingung : b.getConditionList()) {
			if (isRichtigeFeldlistenBedingung(teilbedingung, bedingungName)) {
				result.add((IEntityListCondition) teilbedingung);
			}
		}
		return result;
	}

	private boolean isRichtigeFeldlistenBedingung(
			ICondition b,
			EntityListConditionName bedingungName) {
		if (b instanceof IEntityListCondition) {
			IEntityListCondition bed = (IEntityListCondition) b;
			if (bed.getConditionName() == bedingungName) {
				return true;
			}
			//NichtAlleFelderAngegeben(a,b) Oder FeldNichtAngegeben(c) --> NichtAlleFelderAngegeben(a,b,c),
			//hole also auch FeldNichtAngegeben-Bedingungen
			if (NICHT_ALLE_FELDER == bedingungName && isFeldNichtAngegebenBedingung(bed)) {
				return true;
			}
			//KeinFeldAngegeben(a,b) Und FeldNichtAngegeben(c) wird zu --> KeinFeldAngegeben(a,b,c),
			//hole also auch FeldNichtAngegeben-Bedingungen
			if (KEIN_FELD == bedingungName && isFeldNichtAngegebenBedingung(bed)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isFeldNichtAngegebenBedingung(IEntityListCondition bed) {
		// internally 'FeldNichtAngegeben(f)' is read as 'KeinFeldAngegeben(f)'
		return bed.getConditionName() == KEIN_FELD && FeldIdUtil.referenceOneField(bed);
	}

	/**
	 * Teste
	 * <li>ob bei der Zusammenfassung der verschiedenen Teilbedingungen die i-te Teilbedingung
	 * zwar vom Bedingungstyp her noch eine Zusammengesetzte Bedingung ist, diese Bedingung aber der
	 * Sonderfall ist, dass sie nur noch aus einer Teilbedingung besteht. Dann wird die
	 * Zusammengesetzte Bedingung mit der entsprechenden direkten Bedingung ersetzt.</li>
	 * <li>ob die
	 * i-te Teilbedingung eine Vordruckbedingung ist, die entfernt werden kann, weil keines der
	 * Felder auf dem Vordruck für die Anwendung gültig ist</li>
	 */
	@Override
	public boolean visitNext(int i, ICompositeCondition bedingung) {

		ICondition teilBedingung = bedingung.getConditionList().get(i);
		if (teilBedingung instanceof ICompositeCondition) {
			ICompositeCondition zBed = (ICompositeCondition) teilBedingung;

			if (zBed.getConditionList().size() == 1) {
				//Sonderfall:  zusammengesetzteBedingung besteht nur aus einer Teilbedingung
				teilBedingung = zBed.getConditionList().get(0);
				bedingung.getConditionList().set(i, teilBedingung);
			}
		}
		if (teilBedingung instanceof IRootGroupCondition) {
			IRootGroupCondition fb = (IRootGroupCondition) teilBedingung;
			String vordruckName = fb.getRootGroupName();
			if (!vordruckGueltigFuerAnwendung(vordruckName)) {
				//VordruckNichtVorhanden(V_NichtRelFuerAppl) Und B wird zu B
				if (bedingung.getLogOp() == LogOp.And && fb.getConditionName() == RootGroupConditionName.KeinVordruckAngegeben) {
					bedingungenToRemove.add(teilBedingung);
				}
				//VordruckVorhanden(V_NichtRelFuerAppl) Oder B wird zu B
				if (bedingung.getLogOp() == LogOp.Or && fb.getConditionName() == RootGroupConditionName.MindestensEinVordruckAngegeben) {
					bedingungenToRemove.add(teilBedingung);
				}
			}
		}

		return true; // es soll immer weiter traversiert werden
	}

	//wenn wg. Anwendungseinschränkungen eine Teilbedingung "leer" wurde, wird diese entfernt.
	@Override
	public void endVisit(ICompositeCondition bedingung) {
		for (ICondition teilBed : bedingungenToRemove) {
			bedingung.getConditionList().remove(teilBed);
		}
		bedingungenToRemove.clear();
	}

	/**
	 * Gibt true zurück, wenn auf Vordruck 'vordruckName' ein für die aktuelle Anwendung gültiges
	 * Feld existiert.
	 * Wir müssen hier genau prüfen, denn in dem Vordruck könnten ja Kontexte vorhanden sein,
	 * und diese sind leer.
	 * Aus Performance-Gründen sollte nicht generell ein getAllContainedEntities aufgerufen
	 * werden. Diese Methode prüft, ob mindestens ein Feld im Container enthalten ist.
	 */
	private boolean vordruckGueltigFuerAnwendung(String vordruckName) {
		return containsAField(mvkEc.getDirectlyContainedEntities("/" + vordruckName));
	}

	/**
	 * Prüft, ob die Liste von Entities mindestens ein Feld umfasst. Dies ist der
	 * Fall, wenn ein Element ein Feld ist, ansonsten müssen die Kinder geprüft werden.
	 *
	 * @param list
	 * @return gibt true zurück, wenn die Liste von Entities ein Feld umfasst.
	 */
	private boolean containsAField(List<IEntity> list) {
		Predicate<IEntity> isAFieldOrContainsAField = e -> e instanceof IField || containsAField(
				mvkEc.getDirectlyContainedEntities(e.getFullName()));
		return list.stream().anyMatch(isAFieldOrContainsAField);
	}
}
