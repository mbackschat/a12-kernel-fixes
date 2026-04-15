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
package com.mgmtp.a12.kernel.core.parser.internal.check.entity;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplLenientBaseClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.RegelKontextEntityContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

import jakarta.validation.constraints.NotNull;

/**
 * Stellt die Prüfungen für <code>RegelKontextEntity</code> bereit.
 * Wird für H- und A12-Modelle verwendet.
 * Folgendes wird geprüft:
 * <ul>
 * <li>Für den Regelkontext darf kein Stern referenziert werden.
 * {@link ParserErrorTypesEnum#MVK_INVALID_WILDCARD_FOR_RULEGROUP}
 * <p>
 * (Es wird bereits über die Grammatik sichergestellt, dass #Zeile nur für H-Modelle
 * referenziert werden kann.)
 * <li>Für H-Modelle: Falls der Regelkontext der Vordruck ist, muss ein Vordruck-Sprachkonstrukt
 * verwendet werden. {@link ParserErrorTypesEnumForHModel#MVK_ROOT_GROUP_REFERENCED_AS_GROUP}
 * <li>Für A12-Modelle: wenn iteriert wird, muss das Fehlerfeld im Regelkontext sein.
 * {@link ParserErrorTypesEnum#MVK_ERROR_FIELD_NOT_IN_RULEGROUP}</li>
 * <li>Der Regelkontext darf nicht leer sein (kann nur in H-Modellen vorkommen)
 * {@link ParserErrorTypesEnum#MVK_GROUP_IS_EMPTY}
 */
public class CheckRegelKontextLenientEntityImpl extends CheckImplLenientBaseClass {

	public CheckRegelKontextLenientEntityImpl(@NotNull Lenient_ConditionParser parser) {
		super(parser);
	}

	protected void checksBeforeEntityCreation(RegelKontextEntityContext rctx, String nameUsedInConditionText,
			EntitySpecContext ectx, String ruleGroup, IEntity e) {
	}

	public void check(@NotNull RegelKontextEntityContext rctx) {
		//To handle multiple languages, take the text used in the condition text
		if (rctx == null || rctx.start == null) {
			throw new IllegalArgumentException("check called with null reference");
		}
		String nameUsedInConditionText = rctx.start.getText();

		EntitySpecContext ectx = (EntitySpecContext) rctx.getParent();

		String ruleGroup = EntityUtils.getRuleGroupName(getRule());

		//save the reference to the entity in the EntityRefMap.
		IEntity e = getEntity(ruleGroup);

		checksBeforeEntityCreation(rctx, nameUsedInConditionText, ectx, ruleGroup, e);

		// RegelKontext is in effect a relative path (a special way to write "" or "./") -> RELATIVE
		EntityRef ref = parser.createEntryForEntity(ectx, e, nameUsedInConditionText, PathNotationConfig.RELATIVE);

		postProcessEntityRef(rctx, ref, e, nameUsedInConditionText);
	}

	protected void postProcessEntityRef(@NotNull RegelKontextEntityContext rctx, EntityRef ref, IEntity e,
			String nameUsedInConditionText) {
		if (!getParser().isHModel()) {
			//make sure to add the current index!
			long[] max = EntityUtils.getMaxInA12Model(e);
			long ind = max[max.length - 1] > 1 ? IParseTreeEntity.ITERATION : 1L;
			ref.addIndex(ind);
		}
	}

}
