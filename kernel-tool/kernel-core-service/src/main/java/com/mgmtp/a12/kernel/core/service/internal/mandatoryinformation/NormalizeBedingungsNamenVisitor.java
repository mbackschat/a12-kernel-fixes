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

import java.util.Arrays;
import java.util.List;

import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.CompositeConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.EntityListConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.FeldIdUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;

/**
 * Vereinheitlich die in der Feldlistenbedingung verwendeten Bedingungsnamen.
 * <p>
 * Precondition: the condition is expanded, i.e. parsed with {@link ExpandMode#EXPAND}}.
 */
public class NormalizeBedingungsNamenVisitor extends ASTVisitor {

	static final EntityListConditionName MIND_EIN_FELD = EntityListConditionName.AtLeastOneFieldFilled;
	static final EntityListConditionName NICHT_ALLE_FELDER = EntityListConditionName.NotAllFieldsFilled;
	static final EntityListConditionName FELDER_NICHT_GEM = EntityListConditionName.FieldsNotCollectivelyFilled;
	static final EntityListConditionName NICHT_GENAU_EIN_FELD = EntityListConditionName.NotExactlyOneFieldFilled;
	static final EntityListConditionName KEIN_FELD = EntityListConditionName.NoFieldFilled;
	static final EntityListConditionName MEHR_ALS_EIN_FELD = EntityListConditionName.MoreThanOneFieldFilled;

	/**
	 * Normalisiert die Bedingungsnamen: Verwende MindestensEinFeldangegeben für
	 * <li>FeldAngegeben (die Feldliste darf nur einelementig sein, das wird bei der Eingabe
	 * überprüft)</li>
	 * <li>AlleFelderAngegeben, wenn die Feldliste einelementig ist</li>
	 * <p>
	 * Verwende NichtAlleFelderAngegeben für
	 * <li>NichtGenauEinFeldangegeben, wenn die Feldliste einelementig ist</li>
	 *
	 * @param b
	 * 		ParseBedingung die traversiert wird.
	 * @return gebe immer false zurück, da nicht weiter traversiert werden muss
	 */
	@Override
	public boolean visit(IEntityListCondition b) {
		switch (b.getConditionName()) {

			case AllFieldsFilled:
				if (FeldIdUtil.referenceOneField(b)) {
					b.setConditionName(MIND_EIN_FELD);
				}
				break;

			case NotExactlyOneFieldFilled:
				if (FeldIdUtil.referenceOneField(
						b)) { // kann eigentlich nicht vorkommen, da mehr als ein Feld referenziert werden muss
					b.setConditionName(NICHT_ALLE_FELDER);
				}
				break;

			default: // o.k. wenn nicht alle betrachtet.
		}
		return false; // es wird nicht weiter traversiert.
	}

	@Override
	public boolean visit(ICompositeCondition bedingung) {
		List<ICondition> teilbedingungen = bedingung.getConditionList();
		for (int i = 0; i < teilbedingungen.size(); i++) {
			ICondition bed = teilbedingungen.get(i);
			if (isFelderNichtGemAngBedingung(bed)) {
				bed = normalizeFelderNichtGemAng((IEntityListCondition) bed);
				teilbedingungen.set(i, bed);
			}
			if (isNichtGenauEinFeldAngegeben(bed) &&
					FeldIdUtil.referencesMoreThanOneParam((IEntityListCondition) bed)) {
				bed = normalizeNichtGenauEinFeldAng((IEntityListCondition) bed);
				teilbedingungen.set(i, bed);
			}
		}
		// es muß immer weiter traversiert werden
		return true;

	}

	/**
	 * Gibt true zurück, wenn die Bedingung der Form FelderNichtGemeinsamAngegeben(a1,...,an) ist,
	 * false sonst.
	 */
	public boolean isFelderNichtGemAngBedingung(ICondition bedingung) {
		if (bedingung instanceof IEntityListCondition feldListBed) {
			return FELDER_NICHT_GEM.equals(feldListBed.getConditionName());
		}
		return false;
	}

	/**
	 * Gibt true zurück, wenn die Bedingung der Form NichtGenauEinFeldAngegeben(a1,...,an) ist mit
	 * {@literal n >= 2}, false sonst.
	 */
	public boolean isNichtGenauEinFeldAngegeben(ICondition bedingung) {
		if (bedingung instanceof IEntityListCondition feldListBed) {
			return NICHT_GENAU_EIN_FELD.equals(feldListBed.getConditionName());
		}
		return false;
	}

	/**
	 * Konvertiert eine FeldListenBedingung der Form FelderNichtGemeinsamAngegeben(a1,...,an) in
	 * MindestensEinFeldAngegeben(a1,...,an) Und NichtAlleFelderOderKeinFeldAngegeben(a1,...,an)
	 */
	public ICompositeCondition normalizeFelderNichtGemAng(IEntityListCondition b) {
		b.setConditionName(MIND_EIN_FELD);
		IEntityListCondition b1 = new EntityListConditionImpl(NICHT_ALLE_FELDER, b.getEntityList(), null);
		return new CompositeConditionImpl(LogOp.And, Arrays.asList(b, b1));

	}

	/**
	 * Konvertiert eine FeldListenBedingung der Form NichtGenauEinFeldAngegeben(a1,...,an) in
	 * KeinFeldAngegeben(a1,...,an) Oder MehrAlsEinFeldAngegeben(a1,...,an)
	 */
	public ICompositeCondition normalizeNichtGenauEinFeldAng(IEntityListCondition b) {
		b.setConditionName(KEIN_FELD);
		IEntityListCondition b1 = new EntityListConditionImpl(MEHR_ALS_EIN_FELD, b.getEntityList(), null);
		return new CompositeConditionImpl(LogOp.Or, Arrays.asList(b, b1), true);
	}

}
