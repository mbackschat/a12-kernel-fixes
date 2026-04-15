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

package com.mgmtp.a12.kernel.core.parser.internal.check.entity.semanticIndex;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.CheckEntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

public class CheckSemanticIndexImpl extends CheckSemanticLenientIndexImpl {

	private final FullChecks_ConditionParser fullParser;

	public CheckSemanticIndexImpl(FullChecks_ConditionParser parser) {
		super(parser);
		this.fullParser = parser;
	}

	@Override
	protected void doChecks(GrammarOnly_ConditionParser.SemanticIndexContext ctx, EntityRef valueSpecRef,
			GrammarOnly_ConditionParser.EntitySpecContext valueSpecContext, EntityRef semIdxEntity) {
		if (ctx.e != null) {
			checkSemIdxEntity(semIdxEntity, ctx.e);
		}

		CheckEntityUtils entityUtils = new CheckEntityUtils(fullParser);
		if (entityUtils.isSemanticIndex(valueSpecContext)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_WITH_SEMANTIC_INDEX,
					valueSpecRef.getNameBuiltFromConditionText());
			throw createMVKParserError(valueSpecContext, errMessage);
		}

		IGroup indexGroup = getIndexGroup(valueSpecContext, valueSpecRef);
		checkIndexField(indexGroup, valueSpecContext, valueSpecRef, ctx);

		if (semIdxEntity != null &&
				EntityUtils.isAncestorOrEqual(indexGroup, semIdxEntity.getEntity().getFullName())) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_CONTAINED_IN_INDEX,
					semIdxEntity.getNameBuiltFromConditionText());
			throw createMVKParserError(valueSpecContext, errMessage);
		}

		if (ctx.e != null && semIdxEntity != null) {
			String opName = getBareErrorMessage(ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_REF_OP);
			entityUtils.checkIsValueValidation(semIdxEntity, opName, valueSpecContext);
			if (ctx.e.categoryName != null) {
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_WITH_CATEGORY,
						semIdxEntity.getNameBuiltFromConditionText());
				throw createMVKParserError(valueSpecContext, errMessage);
			}
		}

		checkAndSetIndex(valueSpecContext, valueSpecRef, indexGroup);
	}

	private void checkAndSetIndex(GrammarOnly_ConditionParser.EntitySpecContext valueSpecContext, EntityRef valueSpecRef,
			IGroup indexGroup) {
		if (indexGroup == null) {
			assert false : "in case of null index group, we expect to have aborted already";
			return;
		}
		int idxLevel = EntityUtils.getLevel(indexGroup.getFullName());
		List<Long> indices = valueSpecRef.getIndices();
		if (indices.get(idxLevel) == IParseTreeEntity.WILDCARD) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_AND_WILDCARD,
					valueSpecRef.getNameBuiltFromConditionText());
			throw createMVKParserError(valueSpecContext, errMessage);
		}
		indices.set(idxLevel, IParseTreeEntity.SEMANTIC_INDEX);
	}

	private IGroup getIndexGroup(GrammarOnly_ConditionParser.EntitySpecContext valueSpecContext, EntityRef valueSpecRef) {
		IEntity valueSpecEntity = valueSpecRef.getEntity();
		CheckEntityUtils entityUtils = new CheckEntityUtils(fullParser);
		// at the moment multiple index fields are checked elsewhere for semantic index, but for future extension we accept them here
		return entityUtils.getIndexGroup(fullParser.getEc(), valueSpecContext, valueSpecEntity,
				valueSpecRef.getNameBuiltFromConditionText());
	}

	private void checkIndexField(IGroup indexGroup, GrammarOnly_ConditionParser.EntitySpecContext valueSpecContext,
			EntityRef valueSpecRef,
			GrammarOnly_ConditionParser.SemanticIndexContext ctx) {
		if (indexGroup == null) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_NO_INDEX_FIELD,
					valueSpecRef.getNameBuiltFromConditionText());
			throw createMVKParserError(valueSpecContext, errMessage);
		}

		String indexFieldName = indexGroup.getIndexFieldNames().iterator().next();
		CheckEntityUtils entityUtils = new CheckEntityUtils(fullParser);
		IField indexField = entityUtils.getIndexField(fullParser.getEc(), indexFieldName, indexGroup,
				valueSpecRef.getNameBuiltFromConditionText(),
				valueSpecContext);
		if (ctx.e != null) {
			EntityRef ref = getEntityRef(ctx.e);
			IField semIdxEntity = (IField) ref.getEntity();
			if (!indexField.getType().matchBaseType(semIdxEntity.getType())) {
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INDEX_TYPE_INCONSISTENT,
						ref.getNameBuiltFromConditionText(), indexField.getFullName());
				throw createMVKParserError(valueSpecContext, errMessage);
			}
		} else {
			String constValue = CommonUtils.removeAndUnescapeQuotesAndRemoveCrFromCrLf(ctx.konst.getText());
			// We check, that the constant is not empty to avoid [A for ""]. An empty string makes no sense for the semantic index.
			if (!indexField.getType().isValueApplicable(constValue) || StringUtils.isEmpty(constValue)) {
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INDEX_VALUE_INVALID, constValue,
						indexField.getFullName());
				throw createMVKParserError(valueSpecContext, errMessage);
			}
		}
	}

	private void checkSemIdxEntity(EntityRef semIdxEntity, GrammarOnly_ConditionParser.EntitySpecContext esc) {
		IEntity entity = semIdxEntity.getEntity();
		if (!entity.isField()) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_NO_FIELD,
					semIdxEntity.getNameBuiltFromConditionText());
			throw createMVKParserError(esc, errMessage);
		}
		if (semIdxEntity.getIndices().contains(IParseTreeEntity.WILDCARD)) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_WITH_WILDCARD,
					semIdxEntity.getNameBuiltFromConditionText());
			throw createMVKParserError(esc, errMessage);
		}
	}

	@Override public FullChecks_ConditionParser getParser() {
		return fullParser;
	}
}
