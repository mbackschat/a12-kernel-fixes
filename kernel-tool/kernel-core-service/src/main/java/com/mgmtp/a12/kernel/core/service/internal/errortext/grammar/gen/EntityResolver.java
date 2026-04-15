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

package com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;

public class EntityResolver {
	IRule rule;
	private IEC ec;

	public EntityResolver(IRule rule, IEC ec) {
		this.rule = rule;
		this.ec = ec;
	}

	private boolean entityMustBeShortName(GrammarOnly_ErrorTextParameterParser.EntityContext entityContext) {
		var canBeShort = entityContext.slash == null && entityContext.relativeUp() == null
				&& entityContext.idNameRef().size() == 1;
		if (!canBeShort) {
			return false;
		}
		return !relativeDownPathsResolvesInRule(entityContext.getText());
	}

	public boolean entitySpecIsShortName(GrammarOnly_ErrorTextParameterParser.EntitySpecContext entitySpecContext) {
		if (entitySpecContext.entity() != null) {
			return entityMustBeShortName(entitySpecContext.entity());
		}
		if (entitySpecContext.entityHModel() != null) {
			return entityHModelIsShortname(entitySpecContext.entityHModel());
		}

		throw new IllegalArgumentException("Tried to call entitySpecIsShortName for EntitySpecContext, that has not entity, provided: "+entitySpecContext.getText());
	}

	private boolean entityHModelIsShortname(GrammarOnly_ErrorTextParameterParser.EntityHModelContext entityHModelContext) {
		if (entityHModelContext.pathHModel().slash != null) {
			return false;
		}
		if (entityHModelContext.pathHModel().idNameRefHModel().size() == 1) {
			return !relativeDownPathsResolvesInRule(entityHModelContext.getText());
		}
		return false;
	}

	public enum SearchResult {
		NOTFOUND,
		UNIQUE,
		AMBIGOUS;
	}

	public record ShortNameResult(SearchResult searchResult, String fullName, String foundNames) {
	}

	public ShortNameResult findShortName(String shortName) {
		var fullNames = ec.shortName2FullName(shortName);
		if (fullNames.size() > 1) {
			var fullNamesJoined = String.join(", ", fullNames);
			return new ShortNameResult(SearchResult.AMBIGOUS, "", fullNamesJoined);
		}
		var firstFullNameO = fullNames.stream().findFirst();
		return firstFullNameO.map(s -> new ShortNameResult(SearchResult.UNIQUE, s, s))
				.orElseGet(() -> new ShortNameResult(SearchResult.NOTFOUND, "", ""));
	}

	private boolean relativeDownPathsResolvesInRule(String relativeDownPath) {
		var tentativeAbsPath = EntityUtils.getRuleGroupName(rule) + ElementPathUtils.SEPARATOR + relativeDownPath;
		return (ec.get(tentativeAbsPath) != null);
	}

}
