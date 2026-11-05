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

package com.mgmtp.a12.kernel.core.parser.internal;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckWiederholungBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.DateUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.common.error.CommonParserErrorHandler;
import com.mgmtp.a12.kernel.core.parser.internal.common.error.CommonParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.MVKDefaultErrorStrategy;
import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserError;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.FullChecks_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Full_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.ParseTreeCreator;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.IterationChecker;
import com.mgmtp.a12.kernel.core.parser.internal.util.IterationNotValidVisitor;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;

/**
 * Implements the interface for parsing conditions.
 */
public class ParserService {

	private final IModelSpecificSettings modelSpecificSettings;
	private IEC ec;
	private final IProjectSpecificSettings pss;
	private final CommonParserErrorHandler commonParserErrorHandler;
	private final IServiceConfiguration.ParserStrictness strictness;

	/**
	 * Convenience constructor to initialize parameters by the {@link IServiceConfiguration}.
	 *
	 * @param mvkServiceConfiguration
	 * 		the {@link IServiceConfiguration} containing the parameters used for parsing.
	 */
	public ParserService(IServiceConfiguration mvkServiceConfiguration) {
		this(mvkServiceConfiguration.getIEC(), mvkServiceConfiguration.getModelSpecificSettings(),
				mvkServiceConfiguration.getProjectSpecificSettings(), mvkServiceConfiguration.strictness());
	}

	/**
	 * Constructor.
	 *
	 * @param ec
	 * 		the entity container.
	 * @param modelSpecificSettings
	 * 		additional properties for the entity container.
	 * @param pss
	 * 		project specific settings.
	 */
	public ParserService(IEC ec, IModelSpecificSettings modelSpecificSettings, IProjectSpecificSettings pss,
			IServiceConfiguration.ParserStrictness strictness) {
		this.modelSpecificSettings = modelSpecificSettings;
		this.ec = ec;
		this.pss = pss;
		this.strictness = strictness;
		commonParserErrorHandler = new CommonParserErrorHandler(modelSpecificSettings.getLanguageUsedInConditionText());
	}

	/**
	 * Creates a condition parser object. The object servers as the wrapper object for the parse
	 * tree.
	 *
	 * @param rule
	 * 		rule to be parsed, may not be null
	 * @param conditionText
	 * 		condition text of a rule
	 * @param pm
	 * 		problem reporter
	 * @return parser or null in case of an error
	 */
	private Lenient_ConditionParser createConditionParserAndParseCondition(IRule rule, String conditionText,
			IProblemReporter pm) {
		return createConditionParserAndParseCondition(rule, conditionText, pm, p -> {
		});
	}

	/**
	 * Creates a condition parser object. The object servers as the wrapper object for the parse
	 * tree.
	 *
	 * @param rule
	 * 		rule to be parsed, may not be null
	 * @param conditionText
	 * 		condition text of a rule
	 * @param pm
	 * 		problem reporter
	 * @param parserAdditionalInit
	 * 		function to initialize the parser with
	 * @return parser or null in case of an error
	 */
	private Lenient_ConditionParser createConditionParserAndParseCondition(IRule rule, String conditionText,
			IProblemReporter pm, Consumer<Lenient_ConditionParser> parserAdditionalInit) {
		return createConditionParserAndParse(rule, conditionText, pm, Lenient_ConditionParser::bedingung_start,
				parserAdditionalInit, IServiceConfiguration.ParserStrictness.STRICT);
	}

	private Lenient_ConditionParser createConditionParserAndParse(IRule rule, String textToParse, IProblemReporter pm,
			Function<Lenient_ConditionParser, ParseTree> grammarRule,
			Consumer<Lenient_ConditionParser> parserAdditionalInit, IServiceConfiguration.ParserStrictness strictness) {
		try {
			Lenient_ConditionParser parser = initConditionParser(rule, pm, textToParse, strictness);

			// initialize additional information for the parser
			parserAdditionalInit.accept(parser);

			// checks for rule conditions and Preconditions: we need the error field in the parsing
			checkErrorFieldSetAndValid(parser);

			parser.setParseTree(grammarRule.apply(parser));
			return parser;
		} catch (Exception t) {
			reportMVKCoreErrorOrExceptionFromParsing(t, rule, pm);
		}
		return null;
	}

	private Lenient_ConditionParser initConditionParser(IRule rule, IProblemReporter pm, String textToParse,
			IServiceConfiguration.ParserStrictness strictness) {
		CharStream input = CharStreams.fromString(textToParse);

		//use the specific Lexer, with the bail-out error strategy and the multi language handler
		Full_ConditionLexer lexer = new Full_ConditionLexer(input);
		lexer.initLexer(rule, modelSpecificSettings.getLanguageUsedInConditionText(), pss);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Lenient_ConditionParser parser;
		if (strictness == IServiceConfiguration.ParserStrictness.LENIENT) {
			parser = new Lenient_ConditionParser(tokens);
			parser.initParser(rule, modelSpecificSettings, ec, pss, pm);
		} else {
			FullChecks_ConditionParser fullParser = new FullChecks_ConditionParser(tokens);
			fullParser.initParser(rule, modelSpecificSettings, ec, pss, pm);
			parser = fullParser;
		}

		// NOTE: without errorHandler, the parser will not fail on invalid conditions, but will instead try to parse as much as
		// possible and put ErrorNodeImpls in the parsetree in places that didn't parse
		parser.setErrorHandler(new MVKDefaultErrorStrategy(rule, parser));
		return parser;
	}

	public ICondition parseAndCheckCondition(IRule rule, IProblemReporter pm, ExpandMode expand) {
		return parseAndCheckCondition(rule, rule.getErrorConditionText(), pm, expand);
	}

	/**
	 * Checks if snippetText conforms to the given grammarRule (e.g. {@link Lenient_ConditionParser#operationSnippet()}) and
	 * reports
	 * any occuring parsing problems to {@link IProblemReporter} pm.</br>
	 * Parsing is attempted with {@link IRule} rule as context, which defines e.g. the RuleGroup's path and iteration context.
	 * </p>
	 * Note: in contrast to {@link #parseAndCheckCondition(IRule, String, IProblemReporter, ExpandMode)}, no global checks are done
	 * that
	 * apply to a validation condition as a whole.
	 */
	public void checkSnippet(
			IRule rule,
			String snippetText,
			Function<Lenient_ConditionParser, ParseTree> grammarRule,
			IProblemReporter pm,
			Consumer<Lenient_ConditionParser> parserInit
	) {
		// NOTE: for the check to be effective, it is critical that a suitable error handler is set in the parser
		createConditionParserAndParse(rule, snippetText, pm, grammarRule, parserInit,
				IServiceConfiguration.ParserStrictness.STRICT);
	}

	/**
	 * @param grammarRule
	 * 		e.g. {@link Lenient_ConditionParser#bedingungSnippet}
	 * @param deriveHighlevelParsetree
	 * 		e.g. {@link ParseTreeCreator#createBedingung}
	 */
	private <T extends IParseTreeEntity> TwoStageParseResult<T> createParseTreeForSnippet(
			IRule rule,
			String snippetText,
			Function<Lenient_ConditionParser, ParseTree> grammarRule,
			BiFunction<Lenient_ConditionParser, ExpandMode, T> deriveHighlevelParsetree,
			IProblemReporter pm, ExpandMode expandModeForParsetreeCreator) {
		Objects.requireNonNull(snippetText);
		Objects.requireNonNull(pm);
		Objects.requireNonNull(expandModeForParsetreeCreator);
		Objects.requireNonNull(rule);

		try {
			Lenient_ConditionParser parser = createConditionParserAndParse(rule, snippetText, pm, grammarRule, p -> {
			}, strictness);
			if (parser == null) {
				// Errors have been reported already
				return new TwoStageParseResult<>(null, null);
			}
			if (parser.isA12Model()) {
				setParallelIterationIndices(
						parser); // TODO/Frage: is this safe/applicable for operations or condition snippets at all?
			}
			return new TwoStageParseResult<>(parser, deriveHighlevelParsetree.apply(parser, expandModeForParsetreeCreator));
		} catch (Exception t) {
			reportMVKCoreErrorOrExceptionFromParsing(t, rule, pm);
			return new TwoStageParseResult<>(null, null);
		} finally {
			DateUtils.unload();
		}
	}

	private record TwoStageParseResult<T extends IParseTreeEntity>(
			Lenient_ConditionParser parserWithAntrlParseTree,
			T highLevelParseTree
	) {
	}

	public IOperation parseOperationSnippet(IRule rule, String operationSnippet, IProblemReporter pm, ExpandMode expand) {
		return createParseTreeForSnippet(rule, operationSnippet,
				Lenient_ConditionParser::operationSnippet,
				ParseTreeCreator::createOperation,
				pm, expand).highLevelParseTree();
	}

	public ICondition parseConditionSnippet(IRule rule, String conditionSnippet, IProblemReporter pm, ExpandMode expand) {
		return createParseTreeForSnippet(rule, conditionSnippet,
				Lenient_ConditionParser::bedingungSnippet,
				// not bedingung_start, because preamble is not allowed in snippets, e.g. in computation preconditions.
				ParseTreeCreator::createBedingung,
				pm, expand
		).highLevelParseTree();
	}

	public ICondition parseAndCheckCondition(IRule rule, String conditionText, IProblemReporter pm, ExpandMode expand) {
		return parseAndCheckConditionInternal(rule, conditionText, pm, expand);
	}

	private ICondition parseAndCheckConditionInternal(IRule rule, String conditionText, IProblemReporter pm, ExpandMode expand) {
		Objects.requireNonNull(conditionText, "parseCondition: Parameter 'conditionText' may not be null.");
		Objects.requireNonNull(pm, "parseCondition: Parameter 'pm' may not be null.");
		Objects.requireNonNull(expand, "parseCondition: Parameter 'expand' may not be null.");
		Objects.requireNonNull(rule, "parseCondition: Parameter 'rule' may not be null.");

		if (strictness == IServiceConfiguration.ParserStrictness.LENIENT && expand != ExpandMode.NO_EXPAND && expand != ExpandMode.NO_EXPAND_OR_RG_REPLACEMENT) {
			throw new IllegalArgumentException("Cannot use lenient parser with expansion.");
		}

		var twoStageParseResult = createParseTreeForSnippet(rule, conditionText,
				Lenient_ConditionParser::bedingung_start,
				ParseTreeCreator::createBedingung,
				pm, expand
		);

		Lenient_ConditionParser parser = twoStageParseResult.parserWithAntrlParseTree();
		ICondition result = twoStageParseResult.highLevelParseTree();

		if (result == null) {
			return null;
		}

		if (parser.isLenient()) {
			return result;
		}

		try { // global checks (not applicable for snippets and not required to derive IParseTreeEntitiy:
			if (!checkNormalRule(rule, result, parser)) {
				return null;
			}

			return result;
		} catch (Exception t) {
			reportMVKCoreErrorOrExceptionFromParsing(t, rule, pm);
			return null;
		}
		// DateUtils.unload() hase already been called after createParseTreeForSnippet and
		//			// does not appear to be relevant outside of parser
	}

	private boolean checkNormalRule(IRule rule, ICondition result, Lenient_ConditionParser parser) {
		if (!CheckWiederholungBedingungImpl.checkForParallelIteration(result)) {
			String opName =
					new ConditionMultiLanguageHandler(modelSpecificSettings.getLanguageUsedInConditionText(),
							parser.getPss()).getTerminalName(
							Base_ConditionLexer.WIEDERHOLUNG_NICHT_EINDEUTIG);
			throwError(parser, ParserErrorTypesEnum.MVK_PARALLEL_ITERATION_NOT_ALLOWED, opName);
		}
		if (AnalyseService.hasParallelIteratedFilterConditionId(result)) {
			String opName = new ConditionMultiLanguageHandler(modelSpecificSettings.getLanguageUsedInConditionText(),
					parser.getPss()).getTerminalName(
					Base_ConditionLexer.MIT);
			throwError(parser, ParserErrorTypesEnum.MVK_PARALLEL_ITERATION_FOR_PATH_WITH_ASTERISKS_NOT_ALLOWED, opName);
		}
		if (AnalyseService.hasParallelIteratedSetOperationIterationEntity(result)) {
			String opName = new ConditionMultiLanguageHandler(modelSpecificSettings.getLanguageUsedInConditionText(),
					parser.getPss()).getTerminalName(
					Base_ConditionLexer.SUMME_VON_TERMEN);
			throwError(parser, ParserErrorTypesEnum.MVK_PARALLEL_ITERATION_FOR_PATH_WITH_ASTERISKS_NOT_ALLOWED, opName);
		}
		List<ICondition> filterConditions = AnalyseService.getFilterConditions(List.of(result));
		if (filterConditions.stream().anyMatch(fc -> AnalyseService.isParallelIteration(fc, false))) {
			String opName = new ConditionMultiLanguageHandler(modelSpecificSettings.getLanguageUsedInConditionText(),
					parser.getPss()).getTerminalName(
					Base_ConditionLexer.MIT);
			throwError(parser, ParserErrorTypesEnum.MVK_PARALLEL_ITERATION_NOT_ALLOWED, opName);
		}
		if (filterConditions.stream().anyMatch(AnalyseService::usesSemanticIndex)) {
			String opName = new ConditionMultiLanguageHandler(modelSpecificSettings.getLanguageUsedInConditionText(),
					parser.getPss()).getTerminalName(
					Base_ConditionLexer.MIT);
			throwError(parser, ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_NOT_ALLOWED, opName);
		}
		List<IOperation> setOperations = AnalyseService.getSetOperations(result);
		if (setOperations.stream().anyMatch(fc -> AnalyseService.isParallelIteration(fc, true))) {
			String opName = new ConditionMultiLanguageHandler(modelSpecificSettings.getLanguageUsedInConditionText(),
					parser.getPss()).getTerminalName(
					Base_ConditionLexer.SUMME_VON_TERMEN);
			throwError(parser, ParserErrorTypesEnum.MVK_PARALLEL_ITERATION_NOT_ALLOWED, opName);
		}
		if (setOperations.stream().anyMatch(AnalyseService::usesSemanticIndex)) {
			String opName = new ConditionMultiLanguageHandler(modelSpecificSettings.getLanguageUsedInConditionText(),
					parser.getPss()).getTerminalName(
					Base_ConditionLexer.SUMME_VON_TERMEN);
			throwError(parser, ParserErrorTypesEnum.MVK_SEMANTIC_INDEX_NOT_ALLOWED, opName);
		}

		if (AnalyseService.containsMultipleRepetitionNotUnique(result)) {
			String opName = new ConditionMultiLanguageHandler(modelSpecificSettings.getLanguageUsedInConditionText(),
					parser.getPss()).getTerminalName(
					Base_ConditionLexer.WIEDERHOLUNG_NICHT_EINDEUTIG);
			throwError(parser, ParserErrorTypesEnum.MVK_INVALID_COMBINATION_OF_REPETITON_NOT_UNIQUE, opName);
		}
		if (AnalyseService.containsMultipleZeileNichtEindeutig(result)) {
			String opName = new ConditionMultiLanguageHandler(modelSpecificSettings.getLanguageUsedInConditionText(),
					parser.getPss()).getTerminalName(
					Base_ConditionLexer.ZEILE_NICHT_EINDEUTIG);
			throwError(parser, ParserErrorTypesEnum.MVK_INVALID_COMBINATION_OF_REPETITON_NOT_UNIQUE, opName);
		}
		if (AnalyseService.containsInvalidLenghOfNoValueValidation(result, ec)) {
			throwError(parser, ParserErrorTypesEnum.MVK_INVALID_LENGTH_OF_RAW_TYPE);
		}

		/*
		 * The iteration consistency is checked on the AntLR-Tree instead of the parse tree
		 * for historical reasons. In the future we should do the checks on the parse tree
		 * if possible (i.e. if the check is done after the parse tree was created)!
		 */
		if (!IterationChecker.checkIteration(parser)) {
			return false;
		}

		if (!IterationNotValidVisitor.testUSBIteration(result)) {
			throwError(parser, ParserErrorTypesEnumForHModel.MVK_UITERATION_INVALID);
		}
		if (!IterationNotValidVisitor.testParallelIteration(result)) {
			throwError(parser, ParserErrorTypesEnum.MVK_PARALLEL_ITERATION_INVALID);
		}
		checkSimpleIteration(rule, parser, result);
		return true;
	}

	private void reportMVKCoreErrorOrExceptionFromParsing(Exception e, Object origin, IProblemReporter pm) {
		if (e instanceof MVKCoreError coreError) {
			pm.reportProblem(coreError);
		} else {
			String stackTrace = Java8Utils.getStackTraceAsString(e);
			String errorMsg = commonParserErrorHandler.getParserErrorMessage(CommonParserErrorTypesEnum.MVK_INTERNAL_ERROR,
					stackTrace);
			MVKParserError pe = new MVKParserError(origin, errorMsg);
			pm.reportProblem(pe);
		}
	}

	private void checkSimpleIteration(IRule rule, Lenient_ConditionParser parser, ICondition result) {
		int levelWithIterationError = IterationNotValidVisitor.testSimpleIteration(result, ec,
				ExtmpUtils.isHModel(modelSpecificSettings));
		if (levelWithIterationError != IterationNotValidVisitor.NO_SIMPLE_ITERATION_ERROR) {
			if (parser.isHModel()) {
				if (levelWithIterationError == 0) {
					throwError(parser, ParserErrorTypesEnumForHModel.MVK_NEG_CONDITION_IN_ITERATION_LFDNR);
				} else if (levelWithIterationError == 1) {
					throwError(parser, ParserErrorTypesEnumForHModel.MVK_NEG_CONDITION_IN_ITERATION_ZEILE);
				} else {
					throwError(parser, CommonParserErrorTypesEnum.MVK_INTERNAL_ERROR,
							"Unknown invalid iteration level for H model!");
				}
			} else {
				String errorField = rule.getErrorEntity().getFullName();
				String pathWithIterationError = EntityUtils.getPathUpToLevel(levelWithIterationError, errorField);
				throwError(parser, ParserErrorTypesEnum.MVK_NEG_CONDITION_IN_ITERATION, pathWithIterationError);
			}
		}
	}

	private void setParallelIterationIndices(Lenient_ConditionParser parser) {
		boolean containsParallelIteration = setSeparatePathIndexes(parser);
		// set indices for entities on error field path
		if (containsParallelIteration) {
			setIndexesForParallelIteration(parser);
		}
	}

	private static void setIndexesForParallelIteration(Lenient_ConditionParser parser) {
		for (EntityRef ref : parser.getEntityMap().getEntityMap().values()) {
			if (ref.isInSetExpression() && !ref.isOuter()) {
				// In filter conditions, we could have parallel iteration only in the 'outer' references
				continue;
			}
			for (int i = 0; i < ref.getIndices().size(); i++) {
				if (ref.getIndices().get(i) == IParseTreeEntity.ITERATION) {
					String iterationGroupFullName = EntityUtils.getPathUpToLevel(i, ref.getEntity().getFullName());
					IEntity iterationGroup = parser.getEc().get(iterationGroupFullName);
					assert iterationGroup instanceof IGroup;
					// if there is parallel iteration there has to be exactly one index field on the error field path
					if (EntityUtils.isGroupAboveOrEqualErrorEntityPath(iterationGroup, parser.getErrorEntity())
							&& !((IGroup) iterationGroup).getIndexFieldNames().isEmpty()) {
						ref.setIndex(i, IParseTreeEntity.PARALLEL_ITERATION);
					}
				}
			}
		}
	}

	private static boolean setSeparatePathIndexes(Lenient_ConditionParser parser) {
		boolean containsParallelIteration = false;
		// set indices for entities not on error field path
		for (EntityRef ref : parser.getEntityMap().getEntityMap().values()) {
			if (ref.isInSetExpression() && !ref.isOuter()) {
				// In filter conditions, we could have parallel iteration only in the 'outer' references
				continue;
			}
			if (ref.getIndices().contains(IParseTreeEntity.ITERATION)) {
				// if there is parallel iteration outside the error field path, it is the lowest iteration level
				int iterationLevel = ref.getIndices().lastIndexOf(IParseTreeEntity.ITERATION);
				String iterationGroupFullName = EntityUtils.getPathUpToLevel(iterationLevel, ref.getEntity().getFullName());
				IEntity iterationGroup = parser.getEc().get(iterationGroupFullName);
				if (!EntityUtils.isGroupAboveOrEqualErrorEntityPath(iterationGroup, parser.getErrorEntity())) {
					containsParallelIteration = true;
					ref.setIndex(iterationLevel, IParseTreeEntity.PARALLEL_ITERATION);
				}
			}
		}
		return containsParallelIteration;
	}

	public Set<IEntity> getDirectlyReferencedEntities(IRule rule, IProblemReporter pm) {
		Objects.requireNonNull(rule, "getDirectlyReferencedEntities: Parameter 'rule' may not be null.");
		Objects.requireNonNull(pm, "getDirectlyReferencedEntities: Parameter 'pm' may not be null.");

		Lenient_ConditionParser parser = createConditionParserAndParseCondition(rule, rule.getErrorConditionText(), pm);
		if (parser == null) {
			return null;
		}

		return parser.getEntityMap().getEntityMap().values().stream()
				.map(EntityRef::getEntity)
				.collect(Collectors.toSet());
	}

	public IProjectSpecificSettings getProjectSpecificSettings() {
		return pss;
	}

	/**
	 * Throws an {@link MVKParserError}, if the error field of the rule is
	 * <ul>
	 * <li>not set</li> or
	 * <li>not a valid field</li>
	 * </ul>
	 *
	 * @throws MVKParserError
	 * 		if error field not set or not valid.
	 */
	private void checkErrorFieldSetAndValid(Lenient_ConditionParser parser) {
		if (parser.getErrorEntity() == null) {
			throwError(parser, ParserErrorTypesEnum.MVK_MISSING_ERROR_FIELD);
		}

		String fName = parser.getErrorEntity().getFullName();
		if (ec.get(fName) == null && !parser.isLenient()) {
			throwError(parser, ParserErrorTypesEnum.MVK_ERROR_FIELD_NOT_VALID, fName);
		}
	}

	private void throwError(Lenient_ConditionParser parser, ParserErrorTypesEnumForHModel errKey, Object... args) {
		String errMsg = parser.getMVKErrorHandler().getParserErrorMessage(errKey, args);
		throw new MVKParserError(null, null, parser.getRule(), errMsg);
	}

	private void throwError(Lenient_ConditionParser parser, ParserErrorTypesEnum errKey, Object... args) {
		String errMsg = parser.getMVKErrorHandler().getParserErrorMessage(errKey, args);
		throw new MVKParserError(null, null, parser.getRule(), errMsg);
	}

	private void throwError(Lenient_ConditionParser parser, CommonParserErrorTypesEnum errKey, Object... args) {
		String errMsg = commonParserErrorHandler.getParserErrorMessage(errKey, args);
		throw new MVKParserError(null, null, parser.getRule(), errMsg);
	}

	public IModelSpecificSettings getModelSpecificSettings() {
		return this.modelSpecificSettings;
	}
}
