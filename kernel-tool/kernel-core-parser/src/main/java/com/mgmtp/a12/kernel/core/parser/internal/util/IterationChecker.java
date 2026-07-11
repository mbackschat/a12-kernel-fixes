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
package com.mgmtp.a12.kernel.core.parser.internal.util;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.ado.IExtmpRule;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.ado.IExtmpRule.IterationInterval;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.MVKParserError;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorHandler;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParserBaseListener;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.EntityIdFormatterUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

import jakarta.validation.constraints.NotNull;

/**
 * class checks if the specified iteration is consistent to the rule condition
 */
public class IterationChecker {

	//for error-Messages in H models
	private static final String[] ERR_EBENEN_SPEC_STRING = { "lfd. Nr.", "Zeile" };
	private static final String[] ERR_EBENEN_SPEC_STRING2 = { "LfdNr Vordrucke", "Zeilen" };
	private static final String ERR_EBENEN_SPEC_USB_STRING = "USB";

	private IterationChecker() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Checks if the iteration in the condition is consistent to the iteration defined for all
	 * direct or indirect references of the
	 * error entity.
	 * The error entity must be referenced in the rule (directly or indirectly). One exception in
	 * H models: AnzahlAngegebenerUSB
	 * If at one level iteration is specified, it must be specified for the error entity too.
	 * We have to check all direct and all indirect references of the error entity.
	 * We have to check all levels and the USB level.
	 * <p>
	 * In case of error: error is reported to the problem manager and return false
	 *
	 * @return false if an error is found.
	 */
	public static boolean checkIteration(Lenient_ConditionParser parser) {
		//Get all references of the condition
		Set<EntityRefInRule> allRefs = parser.getEntityMap().getEntityMap().entrySet()
				.stream()
				.filter(e -> !e.getValue().isInSetExpression() || e.getValue().isOuter())
				.map(e -> new EntityRefInRule(e.getKey(), e.getValue()))
				.collect(Collectors.toCollection(LinkedHashSet::new));

		//get the references of the error entity
		IEntity errorEntity = parser.getErrorEntity();

		if (!checkErrorEntityAndIterationCorrect(parser, allRefs, errorEntity)) {
			return false;
		}

		//Für alle Felder, die eine Zeilen-Iteration verwenden,  muss die maximale Zeile zum
		//Fehlerfeld passen. Sonst gäbe es bei z.B. FeldNichtAngegeben Interpretations-Schwierigkeiten.
		//Analog für lfd. Nr.
		if (parser.isHModel()) {
			Set<EntityRefInRule> wrongRefs = getFaultyIter(parser, allRefs, errorEntity, 1);
			if (wrongRefs.isEmpty()) {
				wrongRefs = getFaultyIter(parser, allRefs, errorEntity, 0);
			}
			if (!wrongRefs.isEmpty()) {
				return false;
			}
		}

		return checkIterationRestriction(parser, allRefs, errorEntity);
	}

	private static boolean checkIterationRestriction(Lenient_ConditionParser parser, Set<EntityRefInRule> allRefs,
			IEntity errorEntity) {
		IRule rule = parser.getRule();
		Optional<IterationInterval> rootGroupInterval = rule instanceof IExtmpRule extmpRule ?
				extmpRule.getRootgroupIterationRestriction() :
				Optional.empty();
		Optional<IterationInterval> rowInterval = rule instanceof IExtmpRule extmpRule ?
				extmpRule.getRowIterationRestriction() :
				Optional.empty();
		if (parser.isA12Model()) {
			return checkIterationRestriction4A12Model(parser, rootGroupInterval, rowInterval);
		} else { // H model
			return checkIterationRestriction4HModel(parser, allRefs, errorEntity, rootGroupInterval, rowInterval);
		}
	}

	private static boolean checkIterationRestriction4A12Model(Lenient_ConditionParser parser,
			Optional<IterationInterval> rootGroupInterval,
			Optional<IterationInterval> rowInterval) {
		// in A12 models a restriction of the iteration is not allowed
		if (rootGroupInterval.isPresent()) {
			ParserErrorHandler.reportError(parser, ParserErrorTypesEnum.MVK_ITERATION_RESTRICTION_IN_A12_MODEL,
					ERR_EBENEN_SPEC_STRING2[0]);
			return false;
		} else if (rowInterval.isPresent()) {
			ParserErrorHandler.reportError(parser, ParserErrorTypesEnum.MVK_ITERATION_RESTRICTION_IN_A12_MODEL,
					ERR_EBENEN_SPEC_STRING2[1]);
			return false;
		}
		return true;
	}

	private static boolean checkIterationRestriction4HModel(Lenient_ConditionParser parser, Set<EntityRefInRule> allRefs,
			IEntity errorEntity, Optional<IterationInterval> rootGroupInterval, Optional<IterationInterval> rowInterval) {
		if (rootGroupInterval.isPresent()) {
			if (!EntityRefUtils.isLIteration(allRefs)) {
				Object[] errMsgParams = { ERR_EBENEN_SPEC_STRING[0], rootGroupInterval.get().getStart(),
						rootGroupInterval.get().getEnd(), ERR_EBENEN_SPEC_STRING[0] };
				ParserErrorHandler.reportError(parser, ParserErrorTypesEnumForHModel.MVK_ITERATION_RESTRICTION_AND_NO_ITERATION,
						errMsgParams);
				return false;
			}
			if (invalidInterval(rootGroupInterval.get(), errorEntity.getMax()[0])) {
				ParserErrorHandler.reportError(parser, ParserErrorTypesEnumForHModel.MVK_ITERATION_RESTRICTION_INVALID_INTERVAL,
						ERR_EBENEN_SPEC_STRING2[0]);
				return false;
			}
		}
		if (rowInterval.isPresent()) {
			if (!EntityRefUtils.isZIteration(allRefs)) {
				Object[] errMsgParams = { ERR_EBENEN_SPEC_STRING[1], rowInterval.get().getStart(), rowInterval.get().getEnd(),
						ERR_EBENEN_SPEC_STRING[1] };
				ParserErrorHandler.reportError(parser, ParserErrorTypesEnumForHModel.MVK_ITERATION_RESTRICTION_AND_NO_ITERATION,
						errMsgParams);
				return false;
			}
			if (invalidInterval(rowInterval.get(), errorEntity.getMax()[errorEntity.getMax().length - 1])) {
				ParserErrorHandler.reportError(parser, ParserErrorTypesEnumForHModel.MVK_ITERATION_RESTRICTION_INVALID_INTERVAL,
						ERR_EBENEN_SPEC_STRING2[1]);
				return false;
			}
		}
		return true;
	}

	private static boolean invalidInterval(IterationInterval intervall, long max) {
		return (intervall.getStart() < 1 || intervall.getStart() > intervall.getEnd()
				|| intervall.getStart() > max);
	}

	/**
	 * Gibt die Menge der fehlerhaften Referenzierungen zurück.
	 * Eine Referenzierung ist fehlerhaft, wenn sie auf einer Wiederholungsebene (Zeile, lfd. Nr)
	 * Iteration spezifiziert, das zugehörige Feld aber eine andere max. Wiederholbarkeit für diese
	 * Wiederholungsebene hat als das Fehlerfeld.
	 *
	 * @param parser
	 * @param allRefs
	 * 		alle Feld Referenzierungen
	 * @param errorEntity
	 * 		Fehlerentität - wird für die Überprüfung von max Zeile / max lfd. Nr. benötigt.
	 * @param level
	 * 		Iterationsebene die überprüft wird (0 ist lfd. Nr.; 1 ist Zeile)
	 * @return Menge der fehlerhaften referenzierungen
	 */
	private static Set<EntityRefInRule> getFaultyIter(Lenient_ConditionParser parser, Set<EntityRefInRule> allRefs,
			IEntity errorEntity,
			int level) {
		boolean isZeile = level == 1;
		ParserErrorTypesEnumForHModel err = isZeile ?
				ParserErrorTypesEnumForHModel.MVK_DIFFERENT_Z_IN_ITER :
				ParserErrorTypesEnumForHModel.MVK_DIFFERENT_L_IN_ITER;
		long max = errorEntity.getMax()[level];
		Predicate<EntityRefInRule> isFaulty = (isZeile) ?
				rr -> isMaxRowDifferent(parser, rr, max) :
				rr -> rr.getRef().getMax()[level] != max;

		return EntityRefUtils.getAllEntityWithIter(allRefs, level).stream().filter(isFaulty)
				.peek(rr -> reportIterationError(parser, errorEntity, rr, err)).collect(Collectors.toSet());
	}

	/**
	 * Helper method for {@link #getFaultyIter(Lenient_ConditionParser, Set, IEntity, int)}. Checks if
	 * an entity ref has a different max. row value than <code>max</code>.
	 * <br>
	 * <b>Precondition:</b>
	 * The entity in <code>rr</code> is referenced with row iteration, i.e. it cannot be a root
	 * group and if it is a group the max. row value is well defined (by the max. row of
	 * <em>all</em> of its contained fields, because they have to be equal).
	 */
	private static boolean isMaxRowDifferent(Lenient_ConditionParser parser, EntityRefInRule rr, long max) {
		IEntity entity = rr.getRef().getEntity();
		if (entity.isField()) {
			return rr.getRef().getMax()[1] != max;
		}
		// if ".#Zeile" is specified, the max rows of the contained fields have to be equal, so we take the first
		List<IField> fields = EntityRefUtils.getAllContainedFieldsAndCheckNotEmpty(rr.getRef(), parser, rr.getCtx());
		long maxRowOfGroup = fields.get(0).getMax()[1];
		return maxRowOfGroup != max;
	}

	private static void reportIterationError(Lenient_ConditionParser parser, IEntity errField, EntityRefInRule e,
			ParserErrorTypesEnumForHModel errType) {

		ParserErrorHandler.reportError(parser, errType, e.getRef().getNameBuiltFromConditionText(),
				EntityUtils.getShortName(errField.getFullName()));
	}

	private static boolean checkErrorEntityAndIterationCorrect(Lenient_ConditionParser parser, Set<EntityRefInRule> allRefs,
			IEntity errorEntity) {
		Set<EntityRefInRule> dRefs = EntityRefUtils.getDRefs(allRefs, errorEntity);
		Set<EntityRefInRule> gRefs = EntityRefUtils.getGRefs(allRefs, errorEntity);
		Set<EntityRefInRule> vRefs = EntityRefUtils.getVRefs(allRefs, errorEntity);

		/*
		 * No reference of error entity only allowed, if AnzahlAngegebenerUSB
		 */
		if (dRefs.isEmpty() && gRefs.isEmpty() && vRefs.isEmpty() && !treeContainsAnzahlAngegebenerUSB(parser)) {
			ParserErrorHandler.reportError(parser, ParserErrorTypesEnum.MVK_ERROR_FIELD_NOT_REFERENCED, errorEntity.getFullName());
			return false;
		}

		/*
		 * If G-Refs and D-Refs are empty:
		 * Z-Iteration and G-Iteration is invalid
		 * for other Iteration check only vRefs.
		 */
		if (dRefs.isEmpty() && gRefs.isEmpty()) {
			return checkIterationForConditionWithOnlyVRefs(parser, allRefs, errorEntity, vRefs);
		}

		Set<EntityRefInRule> allErrRefs = new HashSet<>(gRefs);
		allErrRefs.addAll(dRefs);
		allErrRefs.addAll(vRefs);

		return checkIteration4NotOnlyVRefs(parser, allRefs, allErrRefs);
	}

	private static boolean checkIterationForConditionWithOnlyVRefs(Lenient_ConditionParser parser, Set<EntityRefInRule> allRefs,
			IEntity errorEntity, Set<EntityRefInRule> vRefs) {
		// we have only V-Refs: handle the special treatment of V-Refs.

		//Z-Iteration is not allowed.
		if (parser.isHModel() && EntityRefUtils.isZIteration(allRefs)) {
			String errF = EntityUtils.getShortName(errorEntity.getFullName());
			ParserErrorHandler.reportError(parser, ParserErrorTypesEnumForHModel.MVK_ERROR_FIELD_AND_ZITERATION_INVALID, errF);
			return false;
		}

		//G-Iteration is not allowed
		if (!parser.isHModel()) {
			//omit the level == 0,  as this is the L-Iteration. L-Iteration is checked below.
			for (int level = 1; level < getNumberOfLevelsToCheck(parser); level++) {
				Optional<EntityRefInRule> refWithIterationOpt = EntityRefUtils.getEntityWithIter(allRefs, level);
				if (refWithIterationOpt.isPresent()) {
					String errParam = getErrorParameter(parser, level, refWithIterationOpt.get());
					ParserErrorHandler.reportError(parser, ParserErrorTypesEnum.MVK_ERROR_FIELDGROUP_ITERATION4_INVALID, errParam,
							errorEntity.getFullName());
					return false;
				}
			}
		}

		// parallel iteration is not allowed if the index field is not on the root group level of the error entity path
		if (parser.isA12Model() && (getParallelIterationLevel(parser) > 0)) {
			Optional<EntityRefInRule> refWithParallelIterationOpt = EntityRefUtils.getEntityWithParallelIter(allRefs);
			if (refWithParallelIterationOpt.isPresent()) {
				String errParam = getErrorParameter(parser, getParallelIterationLevel(refWithParallelIterationOpt.get()),
						refWithParallelIterationOpt.get());
				ParserErrorHandler.reportError(parser, ParserErrorTypesEnum.MVK_ERROR_FIELDGROUP_ITERATION5_INVALID, errParam,
						errorEntity.getFullName());
				return false;
			}
		}

		// L iteration is allowed, if at least one V-Ref specifies iteration for L (root group)
		// U iteration is allowed, if at least one V-Ref specifies iteration for U (USB)
		return checkVRefs(parser, allRefs, vRefs) && checkVRefsForU(parser, allRefs, vRefs);
	}

	/**
	 * if at least one V-Ref specifies iteration for U, no additional checks are necessary
	 * otherwise check that all refences in allRefs do not specify an iteration
	 *
	 * @return true if everything is consistent, false otherwise
	 */
	private static boolean checkVRefsForU(Lenient_ConditionParser parser, Set<EntityRefInRule> allRefs,
			Set<EntityRefInRule> vRefs) {
		if (!parser.isHModel()) {
			return true;
		}

		// if one of the v-refs specifies iteration for U -> no additional checks are necessary
		if (EntityRefUtils.isIterationU(vRefs)) {
			return true;
		}

		Optional<EntityRefInRule> refWithIterationOpt = EntityRefUtils.getEntityWithUIter(allRefs);
		if (refWithIterationOpt.isPresent()) {
			EntityRefInRule refWithIteration = refWithIterationOpt.get();
			reportErrorFieldAndIterationInvalidError(parser, refWithIteration, ERR_EBENEN_SPEC_USB_STRING);
			return false;
		}

		return true;
	}

	/**
	 * if at least one V-Ref specifies iteration for L, no additional checks are necessary
	 * otherwise check that all references do not specify an L-Iteration
	 * Z- and U-Iterations are not handled in this method.
	 *
	 * @return true if everything is consistent, false otherwise
	 */
	private static boolean checkVRefs(Lenient_ConditionParser parser, Set<EntityRefInRule> allRefs, Set<EntityRefInRule> vRefs) {

		// if one of the v-refs specifies iteration for l -> no additional checks are necessary (for normal iteration)
		if (!EntityRefUtils.isLIteration(vRefs)) {
			//L-Iteration is not allowed as error entity does not specify an L-Iteration
			Optional<EntityRefInRule> refWithIterationOpt = EntityRefUtils.getEntityWithIter(allRefs, 0);
			if (refWithIterationOpt.isPresent()) {
				EntityRefInRule refWithIteration = refWithIterationOpt.get();
				reportErrorFieldAndIterationInvalidError4Level(parser, refWithIteration, 0);
				return false;
			}
		}

		// if one of the v-refs specifies parallel iteration for l -> no additional checks are necessary
		if (!EntityRefUtils.isParallelIteration(vRefs) && (getParallelIterationLevel(parser) == 0)) {
			//L-Parallel-Iteration is not allowed as error entity does not specify an L-Parallel-Iteration
			Optional<EntityRefInRule> refWithParallelIterationOpt = EntityRefUtils.getEntityWithParallelIter(allRefs);
			if (refWithParallelIterationOpt.isPresent()) {
				EntityRefInRule refWithParallelIteration = refWithParallelIterationOpt.get();
				reportErrorFieldAndIterationInvalidError4Level(parser, refWithParallelIteration, 0);
				return false;
			}
		}

		return true;
	}

	private static void reportErrorFieldAndIterationInvalidError4Level(Lenient_ConditionParser parser,
			EntityRefInRule refWithIteration,
			int level) {
		String errParam = getErrorParameter(parser, level, refWithIteration);
		reportErrorFieldAndIterationInvalidError(parser, refWithIteration, errParam);
	}

	private static void reportErrorFieldAndIterationInvalidError(Lenient_ConditionParser parser, EntityRefInRule refWithIteration,
			String errParam) {
		ParserErrorTypesEnum errType = ParserErrorTypesEnum.MVK_ERROR_FIELD_AND_ITERATION_INVALID;
		String errorEntityFullName = parser.getErrorEntity().getFullName();
		String ruleFullName = parser.getRule().getFullName();
		boolean isField = parser.getErrorEntity().isField();
		ConditionMultiLanguageHandler mlh = new ConditionMultiLanguageHandler(parser.getLanguageUsedInConditionText(),
				parser.getPss());
		String errorEntityName4Msg = parser.isHModel()
				? EntityUtils.getNameBuiltForConditionTextHModel(ruleFullName, errorEntityFullName, isField, false)
				: EntityIdFormatterUtil.formatEntityForCondationParserMessageA12DV(
				ruleFullName, errorEntityFullName,
				refWithIteration.getRef().getPathNotationConfig(),
				mlh);
		String iterationEntityName4Msg = refWithIteration.getRef().getNameBuiltFromConditionText();
		String errMsg = parser.getMVKErrorHandler()
				.getParserErrorMessage(errType, errParam, iterationEntityName4Msg, errorEntityName4Msg);

		MVKParserError err = new MVKParserError(refWithIteration.getCtx().start, refWithIteration.getCtx().stop, parser.getRule(),
				errMsg);
		parser.getPm().reportProblem(err);
	}

	/**
	 * In H models, we check z-iteration separately, in A12 models we need not check z.
	 */
	private static int getNumberOfLevelsToCheck(Lenient_ConditionParser parser) {
		return EntityUtils.getMax(parser.getErrorEntity(), parser.isHModel()).length - 1;
	}

	/**
	 * checks that if one of the references in allRefs specifies iteration, then at least one
	 * reference in errRefs must specify iteration for this level as well.
	 *
	 * @param parser
	 * 		required for creating the error messages
	 * @param allRefs
	 * 		all references in the condition
	 * @param allErrRefs
	 * 		all D- or G-references of the error entity
	 * @return true if the iteration is consistent, false otherwise. Problems are reported in case
	 * 		of an error.
	 */
	private static boolean checkIteration4NotOnlyVRefs(
			Lenient_ConditionParser parser,
			Set<EntityRefInRule> allRefs,
			Set<EntityRefInRule> allErrRefs) {

		//check for all repetition levels:  if iteration is specified,  at least one reference of the error entity has to
		//specify iteration as well.
		for (int level = 0; level < getNumberOfLevelsToCheck(parser); level++) {
			if (!EntityRefUtils.isIteration(allErrRefs,
					level)) {// no reference of the error field specifies iteration for the level
				Optional<EntityRefInRule> entityWithIter = EntityRefUtils.getEntityWithIter(allRefs, level);
				if (entityWithIter.isPresent()) {// some other field specifies iteration for the level
					EntityRefInRule iterationRef = entityWithIter.get();
					reportErrorFieldAndIterationInvalidError4Level(parser, iterationRef, level);
					return false;
				}
			}
		}

		// check for parallel iteration: if parallel iteration is specified, all references of the error entity have to
		// specify iteration for the corresponding level as well.
		if (!EntityRefUtils.isParallelIteration(allErrRefs)) {
			Optional<EntityRefInRule> parallelIterationRefOpt = EntityRefUtils.getEntityWithParallelIter(allRefs);
			if (parallelIterationRefOpt.isPresent()) {
				EntityRefInRule parallelIterationRef = parallelIterationRefOpt.get();
				int parallelIterationLevel = getParallelIterationLevel(parallelIterationRef);
				reportErrorFieldAndIterationInvalidError4Level(parser, parallelIterationRef, parallelIterationLevel);
				return false;
			}
		}

		//handle Z-Iteration and U-Iteration separately.
		return checkZAndUIteration4NotOnlyVRefs(parser, allRefs, allErrRefs);
	}

	private static boolean checkZAndUIteration4NotOnlyVRefs(Lenient_ConditionParser parser, Set<EntityRefInRule> allRefs,
			Set<EntityRefInRule> allErrRefs) {
		if (parser.isHModel()) {
			if (!EntityRefUtils.isZIteration(allErrRefs)) {
				Optional<EntityRefInRule> refWithIterOpt = EntityRefUtils.getEntityWithIter(allRefs, 1);
				if (refWithIterOpt.isPresent()) {
					EntityRefInRule refWithIter = refWithIterOpt.get();
					reportErrorFieldAndIterationInvalidError4Level(parser, refWithIter, 1);
					return false;
				}
			}
			if (!EntityRefUtils.isIterationU(allErrRefs)) {
				Optional<EntityRefInRule> refWithIterOpt = EntityRefUtils.getEntityWithUIter(allRefs);
				if (refWithIterOpt.isPresent()) {
					EntityRefInRule refWithIter = refWithIterOpt.get();
					reportErrorFieldAndIterationInvalidError(parser, refWithIter, ERR_EBENEN_SPEC_USB_STRING);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Determine the level w.r.t. the error entity path that can be used for parallel iteration.
	 * This level has to contain an index field.
	 *
	 * @param parser
	 * 		used to determine the error entity
	 * @return level of the lowest group on the error entity path that contains an index field or -1
	 * 		if there is no such level
	 */
	private static int getParallelIterationLevel(Lenient_ConditionParser parser) {
		return EntityUtils.getIndexFieldLevel(parser.getRule().getFullName(), parser.getEc());
	}

	/**
	 * @return level for that <code>ref</code> specifies parallel iteration, or -1 if there is no
	 * 		such level
	 */
	private static int getParallelIterationLevel(EntityRefInRule ref) {
		return ref.getRef().getIndices().indexOf(IParseTreeEntity.PARALLEL_ITERATION);
	}

	/**
	 * Gibt die Bezeichnung der fehlerhaften Ebene zurück.
	 * Dies ist in ExtMP H-Modellen lfd. Nr Vordruck oder Zeile.
	 * In A12-Modellen ist dies die Bezeichnung des Kontextes
	 */
	private static String getErrorParameter(
			Lenient_ConditionParser parser, int level, EntityRefInRule ei) {
		if (parser.isHModel()) {
			return ERR_EBENEN_SPEC_STRING[level];
		} else {
			String fullName = ei.getRef().getEntity().getFullName();
			String[] paths = CommonUtils.getAllPathes(fullName);
			int index = Math.min(paths.length - 1, level);

			String path = paths[index];

			String rulePath = EntityUtils.getParentPath(parser.getRule().getFullName());
			if (EntityUtils.isDescendantOrEqual(path, rulePath) && path.length() >= rulePath.length() + 2) {
				//cut off the rulePath
				return path.substring(rulePath.length() + 1);
			}
			return path;
		}
	}

	private static boolean treeContainsAnzahlAngegebenerUSB(Lenient_ConditionParser parser) {

		ParseTree tree = parser.getParseTree();

		class MyListener extends GrammarOnly_ConditionParserBaseListener {
			public final Boolean[] result = new Boolean[1];

			public MyListener() {
				result[0] = false;
			}

			@Override
			public void enterExtmpKonstanteUSB(@NotNull GrammarOnly_ConditionParser.ExtmpKonstanteUSBContext ctx) {
				result[0] = true;
			}
		}
		ParseTreeWalker walker = new ParseTreeWalker();
		MyListener m = new MyListener();
		walker.walk(m, tree);
		return m.result[0];
	}

}
