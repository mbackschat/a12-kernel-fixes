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
package com.mgmtp.a12.kernel.core.parser.internal.check.operation.hdv;

import java.util.List;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityListenUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityListenUtilsHModel;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntityListeContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeldListenOpHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;

/**
 * Stellt die Prüfungen für <code>FeldListenOpHModel</code> bereit.
 * Entspricht <code> MaximalAngegebenerZeilenindex  </code>.
 * Folgendes wird geprüft:
 * <ul>
 * <li>In der Parameterliste sollen keine Parameter doppelt enthalten sein.
 * {@link ParserErrorTypesEnum#MVK_DUPLICATE_PARAM1}
 * {@link ParserErrorTypesEnum#MVK_DUPLICATE_PARAM2}.
 * <li>In der Parameterliste darf kein Kontext enthalten sein.
 * {@link ParserErrorTypesEnum#MVK_NO_GROUPS_ALLOWED}
 * <li>Alle Felder der Parameterliste müssen mehrzeilig sein
 * {@link ParserErrorTypesEnumForHModel#MVK_NO_Z_MULT}.
 * <li>Alle Felder der Parameterliste müssen den gleichen MZI haben
 * {@link ParserErrorTypesEnumForHModel#MVK_DIFFERENT_Z_MULT}.
 * <li>Zeilen müssen mit Stern referenziert werden
 * {@link ParserErrorTypesEnumForHModel#MVK_NO_Z_WILDCARD_WITHOUT_RESTRICTION}.
 * <li>Laufende Nummer und USB dürfen nicht mit Stern referenziert werden
 * {@link ParserErrorTypesEnumForHModel#MVK_INVALID_LU_WILDCARD}.
 * <li>Alle Parameter müssen auf dem gleichen Vordruck liegen.
 * {@link ParserErrorTypesEnum#MVK_DIFFERENT_ROOT_GROUPS}
 * </ul>
 */
public class CheckFeldListenOpHModelImpl extends CheckImplAbstractClass {
	public CheckFeldListenOpHModelImpl(FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(FeldListenOpHModelContext ctx) {
		addNumberOpType(ctx, 0, false);
		EntityListeContext elCtx = ctx.entityListe();
		CheckEntityListenUtils checker = new CheckEntityListenUtils(fullParser);
		CheckEntityListenUtilsHModel oChecker = new CheckEntityListenUtilsHModel(this);
		String opName = ctx.feldListenOpHModelName().getText();

		List<EntityRef> paramListe = checker.getRefList(elCtx, opName);

		checker.checkOnlyFields(paramListe, elCtx, opName);
		oChecker.checkAllFieldsSameMZI(paramListe, elCtx, opName);
		oChecker.checkFeldMehrzeilig(paramListe, elCtx, opName);
		oChecker.checkAllEntitiesZWildcard(paramListe, elCtx, opName);
		checker.checkAllEntitiesInSameRootGroup(paramListe, elCtx, opName);
		checker.checkAllFieldsSameL(paramListe, elCtx, opName);
		oChecker.checkNoLUWildCard(paramListe, elCtx, opName);
		checker.checkNoCategories(elCtx, opName);
	}
}
