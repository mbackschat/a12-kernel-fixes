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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IApplicationCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IPredefinedTypeValidityCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FindAllAst;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 *
 */
@OnlyForUsage
public class ParseBaumBedingungsServices {

	private ParseBaumBedingungsServices() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Gibt die Menge der Namen aller Applikationsbedingungen der Bedingung zurück oder eine leere
	 * Menge, falls keine Applikationsbedingungen in der Bedingung vorhanden sind
	 *
	 * @param b
	 * 		zu prüfende Bedingung
	 * @return Menge der Namen
	 */

	public static Set<String> getApplikationsBedingungsnamen(ICondition b) {
		final Set<String> result = new HashSet<>();
		if (b == null) {
			return result;
		}

		b.traverse(new ASTVisitor() {
			@Override
			public void endVisit(IApplicationCondition b) {
				result.add(b.getName());
			}
		});
		return result;
	}

	public static Set<String> getCustomFieldTypeReferences(ICondition condition) {
		Set<String> result = new HashSet<>();
		if (condition == null) {
			return result;
		}
		return FindAllAst.findAll(condition, IPredefinedTypeValidityCondition.class::isInstance, true).stream()
				.map(p -> ((IPredefinedTypeValidityCondition) p).getPredefinedTypeConstant())
				.collect(Collectors.toSet());
	}

	public static boolean isNegativBedingung(IEntityListCondition b) {
		EntityListConditionName name = b.getConditionName();
		switch (name) {
			case NoFieldFilled:
			case NotAllFieldsFilled:
			case NotExactlyOneFieldFilled:
			case NotAllGroupsFilled:
			case NoGroupFilled:
				return true;
			case AllFieldsFilled:
			case MoreThanOneFieldFilled:
			case FieldsNotCollectivelyFilled:
			case ZeilenNichtAufsteigend:
			case ZeilenLueckenlos:
			case AtLeastOneFieldFilled:
			case ZeileNichtEindeutig:
			case RepetitionNotUnique:
			case FieldValuesNotUnique:
			case DateRangesOverlap:
			case AtLeastOneGroupFilled:
			case GroupsNotCollectivelyFilled:
			case AllGroupsFilled:
				return false;
			default:
				throw new IllegalArgumentException("Condition name " + name + " not supported.");
		}
	}

	public static boolean isStringListe(IValueListCondition b) {
		return (b.getValueList().get(0).getType() == OperationType.STRING);
	}
}
