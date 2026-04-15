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

import static com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.EntityResolver.SearchResult.AMBIGOUS;
import static com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.EntityResolver.SearchResult.NOTFOUND;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.antlr.v4.runtime.ParserRuleContext;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorType;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.EntityResolver;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.FullChecks_ErrorTextParameterParser;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.IdNameContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.RelativeUpContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.ParserUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;

public class CheckIdNameImpl {
	/**
	 * as defined in BaseErrorTextParameterLexer.g4
	 */
	private final FullChecks_ErrorTextParameterParser.ErrorComposer errorComposer;
	private final String rulePath;
	private final IEC ec;

	private final EntityResolver entityResolver;
	private final String errorTextParameter;
	/**
	 * map of a parsed context to its resolved entity
	 */
	private Map<ParserRuleContext, FullChecks_ErrorTextParameterParser.EntityRef> referencedEntities = new HashMap<>();

	public CheckIdNameImpl(IRule rule
			, FullChecks_ErrorTextParameterParser.ErrorComposer errorComposer
			, IEC ec
			, EntityResolver entityResolver
			, String errorTextParameter
			, Map<ParserRuleContext, FullChecks_ErrorTextParameterParser.EntityRef> referencedEntities
	) {
		this.rulePath = EntityUtils.getRuleGroupName(rule);
		this.errorComposer = errorComposer;
		this.ec = ec;
		this.referencedEntities = referencedEntities;
		this.entityResolver = entityResolver;
		this.errorTextParameter = errorTextParameter;
	}

	public void check(GrammarOnly_ErrorTextParameterParser.IdNameContext ctx) {

		EntitySpecContext entitySpecContext = ParserUtils.getEntitySpecContext(ctx);

		if (isIndexFieldParam(ctx)) {
			return; //checked elsewhere
		}

		if (ctx.getParent() instanceof RelativeUpContext) {
			return; // the idName part of relativeUp (explicit turning point) is checked and processed in CheckRelativeUpImpl
		}

		var fullNameResult = checkAndCreateFullName(ctx.getText(), entitySpecContext);
		resolveAndStoreEntity(fullNameResult, ctx, entitySpecContext);
	}

	private void resolveAndStoreEntity(FullNameResult fullNameResult,
			IdNameContext ctx,
			EntitySpecContext entitySpecContext) {
		IEntity entity = ec.get(fullNameResult.fullOrShortName());
		if (entity != null) {
			referencedEntities.put(entitySpecContext,
					new FullChecks_ErrorTextParameterParser.EntityRef(fullNameResult.nameUsedInErrorText(), entity));
		} else {
			if (isGroupReference(ctx)) {
				throw errorComposer.composeError(FehlertextErrorType.INVALID_GROUP, errorTextParameter,
						fullNameResult.nameUsedInErrorText());
			} else {
				throw errorComposer.composeError(EntityUtils.isRootGroup(fullNameResult.fullOrShortName()) ?
						FehlertextErrorType.INVALID_ROOT_GROUP :
						FehlertextErrorType.INVALID_ENTITY, fullNameResult.nameUsedInErrorText());
			}
		}
	}

	private record FullNameResult(String nameUsedInErrorText, String fullOrShortName, boolean isRelPath,
								  boolean shortnamePathNotation) {

	}

	/**
	 * @return FullNameResult containing the fullname, if it can be determined
	 */
	private FullNameResult checkAndCreateFullName(
			String idNameText,
			GrammarOnly_ErrorTextParameterParser.EntitySpecContext entitySpecContext) {

		String nameUsedInErrorText;
		String fullOrShortName;//try to find fullname, unless in some HDV this must still be a shortname

		boolean isAbsolutePath = ParserUtils.isAbsolutePath(entitySpecContext);
		boolean isShortnamePathNotation = entityResolver.entitySpecIsShortName(entitySpecContext);
		boolean isRelPath = !isShortnamePathNotation && !isAbsolutePath;

		FullChecks_ErrorTextParameterParser.EntityRef entityRef;

		if (referencedEntities.containsKey(entitySpecContext)) { // parent path part already parsed
			entityRef = referencedEntities.get(entitySpecContext);
			String parentPath = entityRef.getEntity().getFullName();
			fullOrShortName = EntityUtils.createPath(parentPath, idNameText);
			nameUsedInErrorText = EntityUtils.createPath(entityRef.getNameUsedInErrorText(), idNameText);
		} else {
			if (isShortnamePathNotation) {
				nameUsedInErrorText = idNameText;
				var shortNameResult = entityResolver.findShortName(idNameText);
				if (shortNameResult.searchResult().equals(AMBIGOUS)) {
					throw errorComposer.composeError(FehlertextErrorType.ERROR_TEXT_FIELDNAME_NOT_UNIQUE, idNameText,
							shortNameResult.foundNames());
				}
				if (shortNameResult.searchResult().equals(NOTFOUND)) {
					var errorType = ParserUtils.getErrorTypeFromParents(entitySpecContext);
					if (errorType.equals(FehlertextErrorType.INVALID_GROUP)) {
						throw errorComposer.composeError(errorType, errorTextParameter, idNameText);
					} else {
						throw errorComposer.composeError(FehlertextErrorType.INVALID_ENTITY, idNameText);
					}
				} else { //shortNameResult is unique
					fullOrShortName = shortNameResult.fullName();
				}
			} else {
				nameUsedInErrorText = getNameUsedInErrorText(idNameText, isAbsolutePath);

				//first fullName candidate of the entity built from the rule group path and the name used in the error text
				String entityFullNameCandidate = constructEntityFullName(nameUsedInErrorText, isAbsolutePath);

				IEntity entity = ec.get(entityFullNameCandidate);
				if (entity == null) {
					boolean isRootGroup = EntityUtils.isRootGroup(entityFullNameCandidate);
					FehlertextErrorType errorType = isRootGroup ? FehlertextErrorType.INVALID_ROOT_GROUP :
							FehlertextErrorType.INVALID_ENTITY;
					throw errorComposer.composeError(errorType, nameUsedInErrorText);
				}

				fullOrShortName = entity.getFullName();
			}
		}
		return new FullNameResult(nameUsedInErrorText, fullOrShortName, isRelPath, isShortnamePathNotation);

	}

	private String getNameUsedInErrorText(String namePart,
			boolean isAbsolutePath) {
		return (isAbsolutePath ? "/" : "") + namePart;
	}

	private boolean isIndexFieldParam(ParserRuleContext ctx) {
		return isOrExistsParent(ctx, GrammarOnly_ErrorTextParameterParser.IndexFieldContext.class::isInstance);
	}

	private boolean isGroupReference(ParserRuleContext ctx) {
		return isOrExistsParent(ctx, GrammarOnly_ErrorTextParameterParser.GroupIndexContext.class::isInstance);
	}

	private boolean isOrExistsParent(ParserRuleContext ctx, Predicate<ParserRuleContext> predicate) {
		if (predicate.test(ctx)) {
			return true;
		}
		if (ctx.getParent() != null) {
			return isOrExistsParent(ctx.getParent(), predicate);
		}
		return false;
	}

	private String constructEntityFullName(String name, boolean isAbsolutePath) {
		try {
			return isAbsolutePath ? name : EntityUtils.relPath2AbsPath(rulePath, name);
		} catch (EntityUtils.RelativeBeyondRootException e) {
			throw errorComposer.composeError(FehlertextErrorType.ERROR_TEXT_PARAMETER_TOO_MANY_PARENT_GROUPS, name);
		}
	}

}
