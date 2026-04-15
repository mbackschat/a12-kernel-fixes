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

package com.mgmtp.a12.kernel.core.service.internal.errortext.creation;

import java.util.Map;
import java.util.Optional;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IErrortextParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.calc.ICalculation.CalcOp;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.BaseYearParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.GroupIndexParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.IndexFieldParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.RootgroupIndexParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.RootgroupNameParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.RootgroupRowParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.RowIndexParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.SemanticIndex;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.SimpleFieldParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.USBParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.calc.Calculation;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.FullChecks_ErrorTextParameterParser;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.CalcContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.CategoryContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.EntityHModelContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.FieldContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.GroupIndexContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.IndexFieldContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.LfdNrContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.LineContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.ParameterContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.ReferenzjahrContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.SemanticIndexContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.UsbContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.ValueContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.ValueOrCategoryContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.VordrucknameContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.VordruckzeileContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParserBaseListener;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

import lombok.NonNull;

public class ErrortextParameterCreator extends GrammarOnly_ErrorTextParameterParserBaseListener {

	private final Map<ParserRuleContext, FullChecks_ErrorTextParameterParser.EntityRef> ctx2ParsedEntities;
	private IErrortextParameter errortextParameter;
	private final String ruleGroupFullName;
	private final boolean isHModel;

	private ErrortextParameterCreator(String ruleGroupFullName,
			@NonNull Map<ParserRuleContext, FullChecks_ErrorTextParameterParser.EntityRef> ctx2ParsedEntities
			, boolean isHModel) {
		this.ruleGroupFullName = ruleGroupFullName;
		this.ctx2ParsedEntities = ctx2ParsedEntities;
		this.isHModel = isHModel;
	}

	public static IErrortextParameter createErrortextParameter(
			@NonNull ParameterContext parameterContext,
			@NonNull Map<ParserRuleContext, FullChecks_ErrorTextParameterParser.EntityRef> ctx2ParsedEntities,
			String ruleGroupFullName,
			boolean isHModel) {
		// parsing is correct -> construct the object.
		ParseTreeWalker walker = new ParseTreeWalker();
		ErrortextParameterCreator tc = new ErrortextParameterCreator(ruleGroupFullName, ctx2ParsedEntities, isHModel);
		walker.walk(tc, parameterContext);

		return tc.errortextParameter;
	}

	@Override
	public void exitReferenzjahr(ReferenzjahrContext ctx) {
		errortextParameter = new BaseYearParameter(getCalculation(ctx.calc()));
	}

	private Calculation getCalculation(CalcContext calc) {
		if (calc == null) {
			return null;
		}
		CalcOp calcOp = CalcOp.getCalcOp(calc.operator.getText());
		int number = Integer.parseInt(calc.DigitSequence().getText());
		return new Calculation(calcOp, number);
	}

	@Override
	public void exitLfdNr(LfdNrContext ctx) {
		errortextParameter = new RootgroupIndexParameter();
	}

	@Override
	public void exitLine(LineContext ctx) {
		errortextParameter = new RowIndexParameter(getCalculation(ctx.calc()));
	}

	@Override
	public void exitUsb(UsbContext ctx) {
		errortextParameter = new USBParameter();
	}

	@Override
	public void exitVordruckname(VordrucknameContext ctx) {
		errortextParameter = new RootgroupNameParameter(ctx.id.getText(), getPathSpecification(ctx.id));
	}

	@Override
	public void exitVordruckzeile(VordruckzeileContext ctx) {
		errortextParameter = new RootgroupRowParameter(ctx.id.getText(), getPathSpecification(ctx.id));
	}

	private String getPathSpecification(EntitySpecContext id) {
		var entitySpecText = id.getText();
		var fullName = getFullName(id);

		//we have to put fullnames for relative paths: there are two cases

		//1) the seemingly shortname is just a one element relative paths (to an entity in the rule group)
		boolean doReplaceSingletonPath =
				(!isHModel && ElementPathUtils.createPath(this.ruleGroupFullName, entitySpecText).equals(fullName));

		if (doReplaceSingletonPath
				//2) it is real relative path
				|| entitySpecText.contains(EntityUtils.SEPARATOR)
		) {
			return fullName;
		}

		return entitySpecText;
	}

	private String getFullName(EntitySpecContext id) {
		var entityRef = this.ctx2ParsedEntities.get(id);
		return Optional.ofNullable(entityRef)
				.flatMap(eRef -> Optional.ofNullable(eRef.getEntity()))
				.map(IEntity::getFullName).orElse(null);
	}

	private String getSemanticIndexFullName(SemanticIndexContext semanticIndexCtx) {
		if (semanticIndexCtx == null || semanticIndexCtx.id == null)
			return null;
		return getFullName(semanticIndexCtx.id);
	}

	@Override
	public void exitGroupIndex(GroupIndexContext ctx) {
		String groupName = ctx.id.regelKontextEntity() != null ? null : ctx.id.getText();
		var groupFullName = ctx.id.regelKontextEntity() != null ? null : getFullName(ctx.id);
		String semanticIndexSpec = getSemanticIndex(ctx.semanticIndex());
		String semanticIndexFullName = getSemanticIndexFullName(ctx.semanticIndex());
		var semanticIndex = Optional.ofNullable(semanticIndexSpec).map(it ->
				new SemanticIndex(it, semanticIndexFullName)
		).orElse(null);
		errortextParameter = new GroupIndexParameter(groupName, groupFullName, semanticIndex);
	}

	private String getSemanticIndex(SemanticIndexContext semanticIndexCtx) {
		return semanticIndexCtx != null ? CommonUtils.unescapeQuotesAndRemoveCrFromCrLf(semanticIndexCtx.getText()) : null;
	}

	@Override
	public void exitField(FieldContext ctx) {
		String nameUsedInErrorText = getFieldNameRef(ctx);
		String fieldFullName = getFullName(ctx.id);
		boolean isValueReference = isValueReference(ctx.valueOrCategory());
		String categoryName = getCategoryName(ctx.valueOrCategory());
		String semanticIndexSpec = getSemanticIndex(ctx.semanticIndex());
		String semanticIndexFullName = getSemanticIndexFullName(ctx.semanticIndex());
		var semanticIndex = Optional.ofNullable(semanticIndexSpec).map(it ->
				new SemanticIndex(it, semanticIndexFullName)
		).orElse(null);
		String rootgroupIndex = getRootgroupIndex(ctx.entitySpec().entityHModel());
		String rowIndex = getRowIndex(ctx.entitySpec().entityHModel());
		errortextParameter = new SimpleFieldParameter(
				nameUsedInErrorText, ruleGroupFullName, fieldFullName,
				isValueReference, categoryName, semanticIndex, rootgroupIndex, rowIndex);
	}

	private String getFieldNameRef(FieldContext fieldContext) {
		EntitySpecContext entitySpecCtx = fieldContext.id;
		if (entitySpecCtx.entity() != null) {
			return entitySpecCtx.entity().getText();
		} else if (entitySpecCtx.entityHModel() != null) {
			return entitySpecCtx.entityHModel().pathHModel().getText();
		}
		throw new IllegalArgumentException("the entitySpecCtx in a fieldCtx cannot be a RegelKontext because of " +
				"FullChecks_ErrorTextParameterParser#checkFieldRef --> one of the above cases should already have been true");
	}

	private boolean isValueReference(ValueOrCategoryContext valueOrCategoryCtx) {
		return valueOrCategoryCtx instanceof ValueContext;
	}

	private String getRootgroupIndex(EntityHModelContext entityAlteWelt) {
		if (entityAlteWelt == null || entityAlteWelt.spezifierHModel().lfdNrSpezifier() == null) {
			return null;
		}
		return entityAlteWelt.spezifierHModel().lfdNrSpezifier().DigitSequence().getText();
	}

	private String getRowIndex(EntityHModelContext entityAlteWelt) {
		if (entityAlteWelt == null || entityAlteWelt.spezifierHModel().zeileSpezifier() == null) {
			return null;
		}
		return entityAlteWelt.spezifierHModel().zeileSpezifier().DigitSequence().getText();
	}

	private String getCategoryName(ValueOrCategoryContext valueOrCategoryCtx) {
		if (valueOrCategoryCtx instanceof CategoryContext categoryContext) {
			return categoryContext.categoryRef().categoryName.getText();
		}
		return null;
	}

	@Override
	public void exitIndexField(IndexFieldContext ctx) {
		boolean isValueReference = isValueReference(ctx.valueOrCategory());
		String categoryName = getCategoryName(ctx.valueOrCategory());
		errortextParameter = new IndexFieldParameter(ctx.id.getText(), getFullName(ctx.id),
				isValueReference, categoryName);
	}
}
