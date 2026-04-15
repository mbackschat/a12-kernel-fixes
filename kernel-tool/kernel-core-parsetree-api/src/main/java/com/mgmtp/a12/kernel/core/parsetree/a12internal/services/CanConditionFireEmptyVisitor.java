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

import java.util.function.Predicate;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IDateValidityCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListAndValueListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.LogOp;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.ValidityConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FindAllAst;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FindAnyAst;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * A visitor to detect conditions that can fire even if there is no field set in a repetition of a
 * root group.
 * A condition can fire empty, if
 * <ul>
 * <li>it is a negative {@link IEntityListCondition}</li>
 * <li>a negative {@link IEntityListAndValueListCondition}</li>
 * <li>a {@link ValidityConditionName#Invalid} check for dates</li>
 * <li>a {@link RootGroupConditionName#KeinVordruckAngegeben} check</li>
 * <li>a condition without a reference to a field on the regarded root group</li>
 * <li>an {@link ICompositeCondition} Or case: one of the partial conditions can fire empty</li>
 * <li>an {@link ICompositeCondition} And case: all partial conditions can fire empty.</li>
 * </ul>
 */
@OnlyForUsage
public class CanConditionFireEmptyVisitor extends ASTVisitor {

	private boolean canFire = false;

	private final String rootGroup;

	private final Predicate<IParseTreeEntity> containsFieldOnRootGroup;

	private CanConditionFireEmptyVisitor(final String rootGroup) {
		this.rootGroup = rootGroup;
		containsFieldOnRootGroup = e -> e instanceof IEntityId && ((IEntityId) e).getFullName().startsWith("/" + rootGroup + "/");
	}

	@Override
	public boolean visit(IEntityListCondition fb) {
		if (ParseBaumBedingungsServices.isNegativBedingung(fb) ||
				!FindAnyAst.findeInBedingung(fb, containsFieldOnRootGroup, false).isPresent()) {
			canFire = true;
		}
		// In the case of AlleFelderAngegeben where all fields have filter conditions,
		// the condition can fire empty (when the filter condition is always false)
		if (fb.getConditionName() == EntityListConditionName.AllFieldsFilled &&
				FindAllAst.findAll(fb, e -> e instanceof IEntityId, false).stream()
						.allMatch(e -> ((IEntityId) e).getFilterCondition() != null)) {
			canFire = true;
		}
		return false;
	}

	@Override
	public boolean visit(IEntityListAndValueListCondition cond) {
		if (cond.getConditionName() == EntityListAndValueListConditionName.NoFieldValueIncludedInValueList ||
				!FindAnyAst.findeInBedingung(cond, containsFieldOnRootGroup, false).isPresent()) {
			canFire = true;
		}
		return false;
	}

	@Override
	public boolean visit(IDateValidityCondition cond) {
		if (cond.getConditionName() == ValidityConditionName.Invalid ||
				!FindAnyAst.findeInBedingung(cond, containsFieldOnRootGroup, false).isPresent()) {
			canFire = true;
		}
		return false;
	}

	@Override
	public boolean visit(ICompositeCondition fb) {
		if (fb.getLogOp().equals(LogOp.Or)) {
			return true; // es wird weiter traversiert.
		}

		for (ICondition b : fb.getConditionList()) {
			CanConditionFireEmptyVisitor v = new CanConditionFireEmptyVisitor(rootGroup);
			b.traverse(v);
			if (!v.canFire()) {
				return false;
			}
		}
		canFire = true;
		return false;
	}

	@Override
	public boolean visitGeneral(IParseTreeEntity p) {
		if (!(p instanceof ICondition)) {
			throw new IllegalArgumentException("There should never be anything other than a Condition on top level");
		}

		// If a condition does not reference a field on the relevant root group it is negative.
		// If it references such a field only in a filter condition it is still negative.
		if (!FindAnyAst.findeInParseBaumEntity(p, containsFieldOnRootGroup, false).isPresent()) {
			canFire = true;
		}
		return false;
	}

	@Override
	public boolean visit(IRootGroupCondition fb) {
		if (fb.getConditionName() == RootGroupConditionName.KeinVordruckAngegeben ||
				!fb.getRootGroupName().equals(rootGroup)) {
			canFire = true;
		}
		return false;
	}

	private boolean canFire() {
		return canFire;
	}

	public static boolean canConditionFireEmpty(ICondition b, String rootGroupPrefix) {
		CanConditionFireEmptyVisitor v = new CanConditionFireEmptyVisitor(rootGroupPrefix);
		b.traverse(v);
		return v.canFire();
	}

}
