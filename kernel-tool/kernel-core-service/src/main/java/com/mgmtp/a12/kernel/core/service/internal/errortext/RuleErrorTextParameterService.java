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

package com.mgmtp.a12.kernel.core.service.internal.errortext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.common.error.CommonParserErrorHandler;
import com.mgmtp.a12.kernel.core.parser.internal.common.error.CommonParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserError;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IErrortextParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.ErrortextParameterCreator;
import com.mgmtp.a12.kernel.core.service.internal.errortext.error.RuleErrorTextParameterDefaultErrorStategy;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.FullChecks_ErrorTextParameterParser;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.FullErrorTextParameterLexer;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen.GrammarOnly_ErrorTextParameterParser.ParameterContext;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;

/**
 * Service for parsing and checking error text parameters.
 */
public class RuleErrorTextParameterService {

	private Logger log = LoggerFactory.getLogger(RuleErrorTextParameterService.class);

	private IModelSpecificSettings modelSpecificSettings;
	private IEC ec;

	public RuleErrorTextParameterService(IServiceConfiguration serviceConfig) {
		this(serviceConfig.getIEC(), serviceConfig.getModelSpecificSettings());
	}

	public RuleErrorTextParameterService(IEC ec, IModelSpecificSettings modelSpecificSettings) {
		this.modelSpecificSettings = modelSpecificSettings;
		this.ec = ec;
	}

	/**
	 * @param rule
	 * 		rule of that the error text parameter is checked, may not be null
	 * @param condition
	 * 		parsed condition of the rule - if null is passed, checks that require the
	 * 		condition are skipped. Note: the condition has to be expanded
	 *        {@link ExpandMode#EXPAND}.
	 * @param errorTextParameter
	 * 		error text parameter (text between $$ inside an error text of the rule), may not
	 * 		be empty or null - a $$ without text between the dollars is no error text
	 * 		parameter, but is interpreted as a single dollar in the error text (the dollar is
	 * 		escaped by doubling it).
	 * @param pm
	 * 		errors are reported to the problem reporter, may not be null
	 * @return the parsed error text parameter or null if error occurred
	 */
	public IErrortextParameter parseParameter(IRule rule, ICondition condition, String errorTextParameter, IProblemReporter pm) {
		var parserResult = createParameterParserAndParseParameter(rule, condition, errorTextParameter, pm);
		if (parserResult == null || parserResult.parameterContext == null || parserResult.ctx2ParsedEntities == null)
			return null;


		var isHModel = ExtmpUtils.isHModel(modelSpecificSettings);
		return ErrortextParameterCreator.createErrortextParameter(
				parserResult.parameterContext,
				parserResult.ctx2ParsedEntities,
				ElementPathUtils.getParentPath(rule.getFullName()),
				 isHModel
					);
	}

	/**
	 * @param rule
	 * 		the rule, the error texts of which are checked.
	 * @param condition
	 * 		the condition for the rule, the error text will be checked against.
	 * @param pr
	 * 		the {@link IProblemReporter}, potential errors will be reported to.
	 * @return the entities referenced by the error text. It is only guaranteed to return all referenced fields, if the error
	 * 		text does not contain parse errors and the condition parameter is not <code>null</code>. If the condition parameter is
	 * 		<code>null</code>, you will get most of the referenced entities, but referenced index fields will currently not be
	 * 		included.
	 * 		<p>
	 * 		Hint: We also return Group entities for
	 *        {@link com.mgmtp.a12.kernel.core.service.internal.errortext.api.IRepetitionIndexParameter}s. The caller is responsible for
	 * 		expanding these entities in a reasonable way.
	 */
	public Set<IEntity> getReferencedEntities(IRule rule, ICondition condition, IProblemReporter pr) {
		Set<IEntity> result = new HashSet<>();
		for (String errorText : rule.getErrorMessages().values()) {
			result.addAll(getReferencedEntities(rule, condition, errorText, pr));
		}
		return result;
	}

	private Set<IEntity> getReferencedEntities(IRule rule, ICondition condition, String errorMsg, IProblemReporter pr) {
		Set<IEntity> result = new HashSet<>();
		String[] parts = StringUtils.splitPreserveAllTokens(errorMsg, '$');
		// only each second part should be examined
		for (int i = 1; i < parts.length; i = i + 2) {
			String param = parts[i];
			if (!StringUtils.isEmpty(param)) {
				createParameterParserAndParseParameter(rule, condition, parts[i], pr, p -> {
					result.addAll(p.getReferencedEntities());
				});
			}
		}
		return result;
	}

	private record ParseResult(ParameterContext parameterContext,
							   Map<EntitySpecContext, FullChecks_ErrorTextParameterParser.EntityRef> ctx2ParsedEntities) {
	}

	private ParseResult createParameterParserAndParseParameter(IRule rule, ICondition condition, String errorTextParameter,
			IProblemReporter pm) {
		return createParameterParserAndParseParameter(rule, condition, errorTextParameter, pm, p -> {
		});
	}

	private ParseResult createParameterParserAndParseParameter(IRule rule, ICondition condition, String errorTextParameter,
			IProblemReporter pm, Consumer<FullChecks_ErrorTextParameterParser> parserConsumer) {
		try {
			FullChecks_ErrorTextParameterParser parser = initParameterParser(rule, condition, errorTextParameter);

			ParameterContext parameterContext = parser.parameter();
			parserConsumer.accept(parser);
			return new ParseResult(parameterContext, parser.getReferencedEntitiesMap());
		} catch (MVKCoreError e) {
			pm.reportProblem(e);
		} catch (Exception t) {
			String errorMsg = new CommonParserErrorHandler(
					modelSpecificSettings.getLanguageUsedInConditionText()).getParserErrorMessage(
					CommonParserErrorTypesEnum.MVK_INTERNAL_ERROR, t.getMessage());
			MVKParserError e = new MVKParserError(rule, errorMsg);
			pm.reportProblem(e);
			log.warn(errorMsg, t);
		}
		return null;
	}

	private FullChecks_ErrorTextParameterParser initParameterParser(IRule rule, ICondition condition, String errorTextParameter) {
		CharStream input = CharStreams.fromString(errorTextParameter);

		//use the specific Lexer, with the bail-out error strategy and the multi language handler
		FullErrorTextParameterLexer lexer = new FullErrorTextParameterLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		FullChecks_ErrorTextParameterParser parser = new FullChecks_ErrorTextParameterParser(tokens);
		parser.initParser(rule, condition, errorTextParameter, ec, modelSpecificSettings);

		FehlertextErrorLookup errorLookup = parser.getErrorLookup();
		String errMsgPrefix = errorLookup.getBareErrorMessage(FehlertextErrorType.INVALID_PARAMETER, errorTextParameter) + " ";
		parser.setErrorHandler(
				new RuleErrorTextParameterDefaultErrorStategy(rule, modelSpecificSettings, errorLookup, errMsgPrefix));
		lexer.initLexer(rule, modelSpecificSettings, parser.getMultiLanguageHandler(), errMsgPrefix);
		return parser;
	}
}
