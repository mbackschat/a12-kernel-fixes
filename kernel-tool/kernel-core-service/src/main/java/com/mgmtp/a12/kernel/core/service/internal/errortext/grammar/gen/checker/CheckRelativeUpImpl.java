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

package com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.checker;

import static com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorType.ERROR_TEXT_PARAMETER_TOO_MANY_PARENT_GROUPS;
import static com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorType.INVALID_ENTITY;

import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.EntityIdFormatterUtil;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.FullChecks_ErrorTextParameterParser;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.RelativeUpContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.ParserUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;

/**
 * Note: this class and the class  {@link com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckRelativeUpImpl}
 * are similar, but the respective grammars are different. So if behaviour is changed here, make sure it conforms with the behaviour
 * of the other CheckRelativeUpImpl
 */
public class CheckRelativeUpImpl {

	private final String rulePath;
	private final FullChecks_ErrorTextParameterParser.ErrorComposer errorComposer;
	private final IEC ec;
	Map<ParserRuleContext, FullChecks_ErrorTextParameterParser.EntityRef> referencedEntities;

	public CheckRelativeUpImpl(IRule rule
			, FullChecks_ErrorTextParameterParser.ErrorComposer errorComposer
			, IEC ec
			, Map<ParserRuleContext, FullChecks_ErrorTextParameterParser.EntityRef> referencedEntities
	) {
		this.rulePath = EntityUtils.getParentPath(rule.getFullName());
		this.errorComposer = errorComposer;
		this.ec = ec;
		this.referencedEntities = referencedEntities;

	}

	public void check(RelativeUpContext ctx) {

		int relUpwardReferenceLevels = 1; // always 1 level in relativeUpContext, relUp only captures the extra levels
		if (ctx.relUp() != null) {
			relUpwardReferenceLevels += ctx.relUp().oneUp().size();
		}
		String relPath = EntityIdFormatterUtil.makeUpwardPath(relUpwardReferenceLevels);

		String groupPath;
		try {
			groupPath = EntityUtils.relPath2AbsPath(rulePath, relPath);
		} catch (EntityUtils.RelativeBeyondRootException e) {
			// this can happen even if checkRelUp has not failed, because relative upward paths with a single upward steps
			// have no relUp
			throw errorComposer.composeError(ERROR_TEXT_PARAMETER_TOO_MANY_PARENT_GROUPS, ctx.getText());
		}
		IEntity entity = ec.get(groupPath);
		if (entity == null) {//case should not happen
			throw new IllegalArgumentException("Group not found from relative path specification: " + ctx.getText());
		}

		String nameUsedInErrorText = relPath;
		if (ctx.idName() != null) {
			// NOTE: if we ever consider completely removing idName, the explicit turning point (optional since A12K-1703 in 2022), we should
			// also consider that the on-the-fly document-model migration service will then have to load an old parser version to reliably find
			// and remove the idNames. But the parser classes and package names are not namespaced by version, therefore loading an old parser
			// is not easily possible in projects that also use the current parser.
			String idName = ctx.idName().start.getText();
			nameUsedInErrorText += idName;
			String groupName = EntityUtils.getShortName(entity.getFullName());
			if (!groupName.equals(idName)) {
				throw errorComposer.composeError(INVALID_ENTITY, nameUsedInErrorText);
			}
		}

		var entitySpecContext = ParserUtils.getEntitySpecContext(ctx.getParent());

		FullChecks_ErrorTextParameterParser.EntityRef entityRef = new FullChecks_ErrorTextParameterParser.EntityRef(
				nameUsedInErrorText, entity);
		referencedEntities.put(entitySpecContext, entityRef);
	}

}
