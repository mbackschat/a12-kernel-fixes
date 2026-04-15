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

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils.RelativeBeyondRootException;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.RelativeUpContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.EntityIdFormatterUtil;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

/**
 * Note: this class and the class {@link com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.checker.CheckRelativeUpImpl}
 * are similar, but the respective grammars are different. So if behaviour is changed here, make sure it conforms with the behaviour
 * of the other CheckRelativeUpImpl
 */
public class CheckRelativeUpImpl extends CheckIdNameRefCommonImpl {

	public CheckRelativeUpImpl(Lenient_ConditionParser parser) {
		super(parser);
	}

	public void check(RelativeUpContext ctx) {
		setCtxs(ctx);

		int relUpwardReferenceLevels = 1; // always 1 level in relativeUpContext, relUp only captures the extra levels
		if (ctx.relUp() != null) {
			relUpwardReferenceLevels += ctx.relUp().oneUp().size();
		}
		String relPath = EntityIdFormatterUtil.makeUpwardPath(relUpwardReferenceLevels);

		String rulePath = EntityUtils.getParentPath(parser.getRule().getFullName());
		String groupPath;
		try {
			groupPath = EntityUtils.relPath2AbsPath(rulePath, relPath);
		} catch (RelativeBeyondRootException e) {
			// this can happen even if checkRelUp has not failed, because relative upward paths with a single upward steps
			// have no relUp
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_TOO_MANY_PARENT_GROUPS);
			throw createMVKParserError(errMessage);
		}
		IEntity entity = parser.getEc().get(groupPath);
		if (entity == null) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_TOO_MANY_PARENT_GROUPS);
			throw createMVKParserError(errMessage);
		}

		String nameUsedInConditionText = relPath;
		boolean hasExplicitTurningPoint = ctx.idName() != null;
		if (ctx.idName() != null) {
			// NOTE: if we ever consider completely removing idName, the explicit turning point (optional since A12K-1703 in 2022), we should
			// also consider that the on-the-fly document-model migration service will then have to load an old parser version to reliably find
			// and remove the idNames. But the parser classes and package names are not namespaced by version, therefore loading an old parser
			// is not easily possible in projects that also use the current parser.
			String idName = ctx.idName().start.getText();
			nameUsedInConditionText += idName;
			String groupName = EntityUtils.getShortName(entity.getFullName());
			if (!parser.isLenient() && !groupName.equals(idName)) {
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_ENTITY, nameUsedInConditionText);
				throw createMVKParserError(errMessage);
			}
		}

		if (!parser.isLenient() && ctx.stern != null) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_WILDCARD_REL);
			throw createMVKParserError(errMessage);
		}

		EntityRef ref = putEntityToEntityMap(entity, nameUsedInConditionText,
				new PathNotationConfig(PathNotationConfig.Type.RELATIVE, true, hasExplicitTurningPoint));
		long index = EntityUtils.isRepeatableOnLowestLevel(entity) ? IParseTreeEntity.ITERATION : 1;
		ref.addIndex(index);
	}

	@Override
	protected boolean isAbsolutPath() {
		return false;
	}

}
