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
package com.mgmtp.a12.kernel.core.parser.internal.check.entity.hdv;

import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckIdNameRefCommonImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.IdNameRefHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefMap;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

import jakarta.validation.constraints.NotNull;

/**
 * Stellt die Prüfungen für <code>checkIdNameRefHModel</code> bereit.
 * Durch die Grammatik wird sichergestellt, dass die Methode nur für H-Modelle aufgerufen wird.
 * Die Ebenenspezifikation wird getrennt in {@link CheckSpezifierHModelImpl} geprüft.
 * <p>
 * <p>
 * Folgendes wird überprüft:
 * <ul>
 * <li>Ein Feld darf nicht mit Pfad adressiert werden.
 * {@link ParserErrorTypesEnumForHModel#MVK_H_MODEL_AND_PATH}
 * <li>Der Pfad muss gültig sein, d.h. im IEC gefunden werden.
 * {@link ParserErrorTypesEnum#MVK_INVALID_ENTITY}
 * <li>Falls es sich um einen Kontext handelt, darf dieser nicht leer sein
 * {@link ParserErrorTypesEnum#MVK_GROUP_IS_EMPTY}
 * </ul>
 * Die gefundene Entität wird jeweils in der {@link EntityRefMap} zu dem EntityKontext
 * gespeichert.
 */
public class CheckIdNameRefHModelImpl extends CheckIdNameRefCommonImpl {

	public CheckIdNameRefHModelImpl(@NotNull FullChecks_ConditionParser parser) {
		super(parser);
	}

	public void check(@NotNull IdNameRefHModelContext idNameCtx) {
		setCtxs(idNameCtx);

		String namePart = idNameCtx.idName().getText();
		String name = getNameBuiltFromConditionText(namePart);

		// H models only:  get entity with the name used in the condition text.
		IEntity e = getEntity(name);

		if (e == null) {//not a valid group or field
			//Check if the entity exists, if the path prefix is used.
			String exEntityName = EntityUtils.createPath(getRulePrefix(), name);
			IEntity ex = getEntity(exEntityName);
			if (ex != null) { // only groups may be adressed using a path
				if (ex.isGroup()) {
					parser.putEntityToMapHDVOnly(eSpecCtx, ex, name);
					return;
				} else {
					String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_H_MODEL_AND_PATH);
					throw createMVKParserError(errMessage);
				}
			}

			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_ENTITY, name);
			throw createMVKParserError(errMessage);
		}

		// H models: Fields may not be referenced via a path.
		if (!e.isGroup() && name.contains("/")) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_H_MODEL_AND_PATH);
			throw createMVKParserError(errMessage);
		}

		//empty groups are not allowed
		EntityRefUtils.checkNoEmptyGroup(e, name, parser, idNameCtx);

		//everything is o.k.: --> add the entity to the entityMap.
		parser.putEntityToMapHDVOnly(eSpecCtx, e, name);
	}

	@Override
	protected boolean isAbsolutPath() {
		return eSpecCtx == null || eSpecCtx.entityHModel().slash != null;
	}

}
