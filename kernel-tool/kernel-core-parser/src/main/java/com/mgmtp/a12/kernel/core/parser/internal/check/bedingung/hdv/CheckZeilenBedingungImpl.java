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
package com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.hdv;

import java.util.List;

import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityListenUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityListenUtilsHModel;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntityListeContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.ZeilenBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;

import jakarta.validation.constraints.NotNull;

/**
 * Stellt die Prüfungen für <code>checkZeilenBedingung</code> bereit.
 * Nur H-Modelle!
 * <ul>
 * <li>Für <b>alle ZeilenBedingungen</b> wird geprüft:
 * <p>
 * In der Parameterliste sollen keine Parameter doppelt enthalten sein. Dies ist möglich entweder
 * durch eine Doppelnennung {@link ParserErrorTypesEnum#MVK_DUPLICATE_PARAM1}, oder durch indirekte
 * Doppelnennung {@link ParserErrorTypesEnum#MVK_DUPLICATE_PARAM2}, da ein Feld / ein Kontext auch
 * indirekt durch einen umfassenden Kontext spezifiziert werden kann. Die Überprüfung wurde
 * eingeführt, um Fehleingaben z.B. durch Copy/Paste-Fehler zu vermeiden.
 * <p>
 * Durch die Grammatik wird bereits geprüft, dass die Parameterliste nicht leer ist.</li>
 * <p>
 * Für alle Sprachkonstrukte wird geprüft:
 * <ul>
 * <li>
 * Alle Felder der Parameterliste sind mehrzeilig
 * {@link ParserErrorTypesEnumForHModel#MVK_NO_Z_MULT}
 * <li>Alle Felder der Parameterliste liegen auf dem gleichen Vordruck
 * {@link ParserErrorTypesEnum#MVK_DIFFERENT_ROOT_GROUPS}
 * <li>Für laufende Nummer muss immer die gleiche Angabe (konkret oder Iteration) verwendet werden.
 * {@link ParserErrorTypesEnum#MVK_INVALID_PARAMS_FOR_OP}
 * </ul>
 * <li>für <b>ZeilenLueckenlos</b> wird geprüft:
 * <p>
 * Alle Felder der Parameterliste referenzieren die Zeilen mit Stern (ohne eine Einschränkung)
 * {@link ParserErrorTypesEnumForHModel#MVK_NO_Z_WILDCARD_WITHOUT_RESTRICTION} und alle anderen
 * Wiederholungsebenen ohne Stern {@link ParserErrorTypesEnumForHModel#MVK_INVALID_LU_WILDCARD}.
 * Felder
 * können über einen Kontext oder über die Felder referenziert werden. Alle Felder müssen mehrzeilig
 * sein mit identischem MZI {@link ParserErrorTypesEnumForHModel#MVK_DIFFERENT_Z_MULT}. Anmerkung:
 * das
 * Sprachkonstrukt wird nur in H-Modellen und nur 4 mal verwendet.
 * <li>für <b>ZeilenNichtAufsteigend</b> wird geprüft:
 * <p>
 * Zeilen müssen als Iteration spezifiziert werden.
 * {@link ParserErrorTypesEnumForHModel#MVK_MISSING_Z_ITERATION} Kein Feld wird auf irgendeiner
 * Ebene
 * mit
 * Stern referenziert. {@link ParserErrorTypesEnum#MVK_NO_WILDCARDS_ALLOWED}
 * {@link ParserErrorTypesEnum#MVK_INVALID_PARAMS_FOR_OP}
 *
 * <li>für <b>ZeilenNichtEindeutig</b> wird geprüft:
 * <p>
 * Es wird geprüft, dass in der Parameterliste nur Felder verwendet werden.
 * {@link ParserErrorTypesEnum#MVK_NO_GROUPS_ALLOWED} Die Felder dürfen keinen Stern spezifizieren.
 * {@link ParserErrorTypesEnum#MVK_NO_WILDCARDS_ALLOWED} Alle Felder müssen mehrzeilig spezifiziert
 * sein
 * mit Iteration für die Zeile.
 * </ul>
 */
public class CheckZeilenBedingungImpl extends CheckImplAbstractClass {
	public CheckZeilenBedingungImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull ZeilenBedingungContext ctx) {

		CheckEntityListenUtils checker = new CheckEntityListenUtils(fullParser);
		CheckEntityListenUtilsHModel oChecker = new CheckEntityListenUtilsHModel(this);
		String opName = ctx.zeilenBedingungName().getText();

		EntityListeContext elCtx = ctx.entityListe();
		List<EntityRef> refs = checker.getRefList(elCtx, opName);

		checker.checkNoCategories(elCtx, opName);

		checker.checkAllEntitiesInSameRootGroup(refs, elCtx, opName);
		oChecker.checkFeldMehrzeilig(refs, elCtx, opName);
		checker.checkAllFieldsSameL(refs, elCtx, opName);

		checker.checkNotRootGroupReferenced(refs, elCtx, opName, fullParser.isHModel());

		Token t = ctx.zeilenBedingungName().start;
		switch (t.getType()) {
			case GrammarOnly_ConditionParser.ZEILEN_LUECKENLOS:
				oChecker.checkAllFieldsSameMZI(refs, elCtx, opName);
				oChecker.checkAllEntitiesZWildcard(refs, elCtx, opName);
				oChecker.checkNoLUWildCard(refs, elCtx, opName);

				return;
			case GrammarOnly_ConditionParser.ZEILEN_NICHT_AUFSTEIGEND:
				oChecker.checkAlleEbeneOhneSternUndZeileNichtKonkret(refs, elCtx, opName);
				return;

			case GrammarOnly_ConditionParser.ZEILE_NICHT_EINDEUTIG:
				checker.checkOnlyFields(refs, elCtx, opName);
				checker.checkNoWildCards(refs, elCtx, opName);
				oChecker.checkAllFieldsZIteration(refs, elCtx, opName);
				checker.checkAllAreValueValidation(refs, elCtx, opName);
				return;
			default:
				throwUnknownTokenError(t);
		}
	}
}
