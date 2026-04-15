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

import java.util.List;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;

/**
 * Bietet den Service an, eine Bedingung zu normalisieren. Dies beinhaltet unter Anderem:
 * <ul>
 * <li>FeldAngegeben(A) oder FeldAngegeben(B) wird dann zu MindestensEinFeldAngegeben(A,B)
 * <li>MindestensEinFeldAngegeben(A,B) Oder MindestensEinFeldAngegeben(C,D) wird zu
 * MindestensEinFeldAngegeben(A,B,C,D)
 * <li>FeldNichtAngegeben(A) oder FeldNichtAngegeben(B)wird zu
 * NichtAlleFelderOderKeinFeldAngegeben(A,B)
 * <li>NichtAlleFelderOderKeinFeldAngegeben(A,B) oder NichtAlleFelderOderKeinFeldAngegeben(C,D)wird
 * zu NichtAlleFelderOderKeinFeldAngegeben(A,B,C,D)
 * <li>FeldNichtAngegeben(A) Und FeldNichtAngegeben(B) wird zu KeinFeldAngegeben(A,B)
 * <li>KeinFeldAngegeben(A,B) Und KeinFeldAngegeben(C,D) wird zu KeinFeldAngegeben(A,B,C,D)
 * <li>FelderNichtGemeinsamAngegeben(A,B) wird zu MindestensEinFeldAngegeben(A,B)und
 * NichtAlleFelderOderKeinFeldAngegeben(A,B)
 * <li>NichtGenauEinFeldAngegeben(A,B) wird zu KeinFeldAngegeben(A,B) Oder
 * MehrAlsEinFeldAngegeben(A,B)
 * <li>Wenn die obigen Normalisierungsschritte abgeschlossen sind, werden alle Vorkommen von
 * FeldNichtAngegeben(A) durch NichtAlleFelderOderKeinFeldAngegeben(A) ersetzt.
 * </ul>
 * Die normalisierten Bedingungen werden bei der Ermittlung lokaler Pflichtfeld- und
 * Pflichtmengeneigenschaften verwendet.
 */
public class NormalizeService {

	private NormalizeService() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Normalisiert die Bedingung, wie im JavaDoc der Klasse beschrieben. Es wird vorausgesetzt, daß
	 * die Element-Sprachkonstrukte bereits durch reine Feld-Sprachkonstrukte ersetzt wurden.
	 */
	public static ICondition normalize(ICondition b, IEC mvkEc) {

		// normalisiere die Bedingungsnamen
		b = normalizeBedingungsNamen(b);

		NormalizeTeilBedingungenVisitor visitorFuerTeilBedingungen = new NormalizeTeilBedingungenVisitor();
		b.traverse(visitorFuerTeilBedingungen);

		NormalizeBedingungVisitor visitorFuerBedingungen = new NormalizeBedingungVisitor(mvkEc);
		b.traverse(visitorFuerBedingungen);

		b = transformEinelementigeZusGesBedingung(b);

		NormalizeFeldNichtAngegebenVisitor visitorFuerFeldNichtAngegeben = new NormalizeFeldNichtAngegebenVisitor();
		b.traverse(visitorFuerFeldNichtAngegeben);

		return b;
	}

	private static ICondition normalizeBedingungsNamen(ICondition b) {
		NormalizeBedingungsNamenVisitor visitorFuerNamen = new NormalizeBedingungsNamenVisitor();
		if (visitorFuerNamen.isFelderNichtGemAngBedingung(b)) {
			// b ist von der Form FelderNichtGemeinsamAngegeben(a1,...,an): es wird sofort die normalisierte Bedingung
			// (MindestensEinFeldAngegeben(a1,...,an) Und NichtAlleFelderOderKeinFeldAngegeben(a1,...,an))
			// zurückgegeben
			b = visitorFuerNamen.normalizeFelderNichtGemAng((IEntityListCondition) b);
		}
		if (visitorFuerNamen.isNichtGenauEinFeldAngegeben(b)) {
			// b ist von der Form NichtGenauEinFeldAngegeben(a1,...,an)
			// in diesem Fall wird die normalisierte Bedingung
			// (KeinFeldAngegeben(a1,...,an) Oder MehrAlsEinFeldAngegeben(a1,...,an))
			// sofort zurückgegeben
			b = visitorFuerNamen.normalizeNichtGenauEinFeldAng((IEntityListCondition) b);

		}
		b.traverse(visitorFuerNamen);

		return b;
	}

	/**
	 * Gibt für eine zusammengesetzte Bedingung, die nur eine Teilbedingung hat, diese Teilbedingung
	 * zurück. Sonst wird die Bedingung unverändert zurückgegeben. <br>
	 * Situation kann während der früheren Normalisierungsschritte entstanden sein. Muß nicht
	 * rekursiv für alle Teilbedingungen gemacht werden, da für diese die Transformation bereits im
	 * {@link NormalizeBedingungVisitor} erfolgt ist.
	 */
	private static ICondition transformEinelementigeZusGesBedingung(ICondition b) {
		if (b instanceof ICompositeCondition) {
			List<ICondition> teilBedingungen = ((ICompositeCondition) b).getConditionList();
			if (teilBedingungen.size() == 1) {
				return teilBedingungen.get(0);
			}
		}
		return b;
	}

}
