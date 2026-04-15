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

import org.antlr.v4.runtime.ParserRuleContext;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorType;

public class ParserUtils {
	private ParserUtils() {
	}

	public static FehlertextErrorType getErrorTypeFromParents(ParserRuleContext ctx) {
		var parent = ctx.parent;
		while (parent != null) {
			switch (parent) {
				case GrammarOnly_ErrorTextParameterParser.IndexFieldContext ifc:
					return FehlertextErrorType.INVALID_INDEX_FIELD;
				case GrammarOnly_ErrorTextParameterParser.FieldContext fc:
					return FehlertextErrorType.INVALID_FIELD;
				case GrammarOnly_ErrorTextParameterParser.GroupIndexContext gic:
					return FehlertextErrorType.INVALID_GROUP;
				default:
					parent = parent.parent;
					break;
			}
		}
		return FehlertextErrorType.INVALID_ENTITY;
	}

	public static GrammarOnly_ErrorTextParameterParser.EntitySpecContext getEntitySpecContext(ParserRuleContext ctx) {
		if (ctx instanceof GrammarOnly_ErrorTextParameterParser.EntitySpecContext entitySpecContext) {
			return entitySpecContext;
		}
		if (ctx.getParent() != null) {
			return getEntitySpecContext(ctx.getParent());
		}
		throw new IllegalArgumentException("EntitySpecContext not found");
	}

	public static String getSemanticIndexRepresentation(Object semIndex) {
		if (semIndex instanceof String) {
			return "\"" + semIndex + "\"";
		} else if (semIndex instanceof IEntityId entityId) {
			return entityId.getFullName();
		} else {
			throw new IllegalArgumentException("Semantic indexes may only be strings or entities!");
		}
	}

	static String getWithTrailingSlash(String path) {
		return path.endsWith(ElementPathUtils.SEPARATOR) ? path : (path + ElementPathUtils.SEPARATOR);
	}

	static String trimTrailingSlash(String pathOne) {
		return pathOne.replaceAll("/$", "");
	}

	public static boolean isAbsolutePath(GrammarOnly_ErrorTextParameterParser.EntitySpecContext entitySpecContext) {
		return entitySpecContext.entity() != null && entitySpecContext.entity().slash != null
				|| entitySpecContext.entityHModel() != null && entitySpecContext.entityHModel().pathHModel().slash != null;
	}

}
