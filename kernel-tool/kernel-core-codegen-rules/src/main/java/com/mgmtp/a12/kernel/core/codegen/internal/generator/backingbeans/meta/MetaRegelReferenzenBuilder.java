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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdsCollector;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.CodeGenParseTreeHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RepetitionNotUniqueCreator;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.RuleMetaInformation;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parser.internal.util.RegelReferenzenErmittler;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FindAnyAst;
import com.mgmtp.a12.kernel.core.service.internal.RuleParser;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * Builder for the template parameter class {@link MetaRegelReferenzen}.
 * <p>
 * Calculates the referenced fields used by the rules.
 */
public class MetaRegelReferenzenBuilder {
	private CodeGenData data;
	private RuleInfo ruleInfo;
	private Set<IEntityId> referenzierteFeldIds;
	private final Set<IEntityId> referenzierteAuslassungsFeldIds;
	private final Set<String> referenzierteAuslassungsVordrucke;
	private final Set<EntityId> referenzierteAuslassungsEntityIds;
	private final Set<EntityId> referenzierteEntityIds;
	/*
	 * The order matters because entities may reference other entities, and the order ensures that all entities occur before they
	 * are referenced.
	 * References between entities are possible for semantic index with a field ref, and in filter conditions (but filter conditions
	 * do not currently occur for refEntities because they get removed before code generation)
	 */
	private final List<EntityId> orderedEntitiesForVariableDeclartion;
	private Map<String, Integer> fieldIndices;
	private boolean isServerCalculation;
	private final boolean isDynamicEnumeration;

	public MetaRegelReferenzenBuilder(RuleInfo ruleInfo, CodeGenData data, Map<String, Integer> fieldIndices,
			boolean isServerCalculation, boolean isDynamicEnumeration, IProblemReporter pr) {
		this.data = data;
		this.ruleInfo = ruleInfo;
		this.fieldIndices = fieldIndices;
		this.isServerCalculation = isServerCalculation;
		this.isDynamicEnumeration = isDynamicEnumeration;

		ICondition condition = ruleInfo.getCondition();
		boolean isA12Model = !ExtmpUtils.isHModel(data.getModelSpecificSettings());
		int levelToStartDuplicateSearch = getLevelToStartDuplicateSearch(ruleInfo.getFullName(), ruleInfo.getCondition(),
				!isA12Model);
		assert levelToStartDuplicateSearch == getLevelToStartDuplicateSearch(ruleInfo.getFullName(),
				ruleInfo.getPartiallyExpandedCondition(), !isA12Model);
		RegelReferenzenErmittler re = new RegelReferenzenErmittler(condition, ruleInfo.getPartiallyExpandedCondition(),
				data.getEc(), levelToStartDuplicateSearch);
		this.referenzierteAuslassungsFeldIds = re.getReferenzierteEntityIdentifierFuerAuslassungsfehler(false, false);
		this.referenzierteAuslassungsVordrucke = re.getReferenzierteVordruckNamenFuerAuslassungsfehler();
		this.referenzierteFeldIds = re.getReferenzierteFeldIds();

		Set<IEntityId> refEntities;
		Set<IEntityId> refOmissionEntities;
		if (!hasCustomRefEntities(ruleInfo)) {
			refEntities = re.getReferencedEntityIds4RefEntity(isA12Model);
			refOmissionEntities = re.getReferencedEntityIds4RefOmissionEntity(isA12Model, true);
			assert refEntities.stream().map(ei -> ei.getFullName() + Arrays.toString(ei.getIndices())).collect(Collectors.toSet())
					.containsAll(refOmissionEntities.stream().map(ei -> ei.getFullName() + Arrays.toString(ei.getIndices()))
							.collect(Collectors.toSet()))
					: "for automatically-computed refEntities, we currently expect " +
					"that all omissionEntities are also refEntities (refEntities is actually more like refEntitiesIfGiven";
			// refEntities.containsAll(refOmissionEntities) doesn't always work because equals is not implemented

			// TODO optional rename for consistency: in codegen, gencode & runtime: refEntities -> refErrorEntitiesIfGiven, refOmissionEntities -> refErrorEntititesIfNotGiven
		} else {
			assert isA12Model;
			assert levelToStartDuplicateSearch == getLevelToStartDuplicateSearch(
					ruleInfo.getFullName(), ruleInfo.getConditionNoExpandForCustomRefErrorEntities(), !isA12Model);
			Map<String, Set<IEntityId>> allReferencedUnexpandedByRelpath =
					RuleMetaInformation.getCandidatesForCustomRefErrorEntitiesFromConditionA12DV(
							ruleInfo.getConditionNoExpandForCustomRefErrorEntities(),
							data.getEc(),
							ruleInfo,
							levelToStartDuplicateSearch);
			Locale localeForErrMsg = data.getModelSpecificSettings().getLanguageUsedInConditionText();
			refEntities = selectMatchingEntityIds(
					ruleInfo.getCustomRefErrorEntitiesIfFilled(),
					allReferencedUnexpandedByRelpath,
					ruleInfo.getRule(), localeForErrMsg, true, pr);
			refOmissionEntities = selectMatchingEntityIds(
					ruleInfo.getCustomRefErrorEntitiesIfNotFilled(),
					allReferencedUnexpandedByRelpath,
					ruleInfo.getRule(), localeForErrMsg, false, pr);
		}

		CodeGenParseTreeHelper parsetreeConverter = parseTreeHelperFromCodegenData(data, false);
		EntityIdsCollector entityIdsCollector = new EntityIdsCollector("metaRule" + ruleInfo.getRuleNumber() + "Refs");
		this.referenzierteAuslassungsEntityIds =
				// This method name is a monstrosity, sorry. But I'm not sure if the code would really be better if this
				// was all implicit rather than spelled out.
				// Refactoring the way that parseTreeConverter works would be nice, but seems way out of scope for the current
				// somewhat small change.
				assignDeterministicVariableNamesAndConvertToCodegenEntityIdsAndCollectOrderedEntityIdsForVariableDef(
						refOmissionEntities,
						parsetreeConverter,
						entityIdsCollector);
		this.referenzierteEntityIds =
				assignDeterministicVariableNamesAndConvertToCodegenEntityIdsAndCollectOrderedEntityIdsForVariableDef(
						refEntities,
						parsetreeConverter,
						entityIdsCollector);
		// getEntityIds must be called *after* last use of entityIdsCollector!
		this.orderedEntitiesForVariableDeclartion = entityIdsCollector.getEntityIds();
	}

	private static Set<IEntityId> selectMatchingEntityIds(
			List<String> customRefEntityRelpaths,
			Map<String, Set<IEntityId>> allReferencedUnexpandedByRelpath,
			Object originRuleForErrors,
			Locale localeForErrorMessages,
			boolean errMsg4Filled,
			IProblemReporter pr
	) {
		List<String> invalidRelPaths = new ArrayList<>();
		Set<IEntityId> entityIds = new HashSet<>();
		for (String relPath : customRefEntityRelpaths) {
			if (!allReferencedUnexpandedByRelpath.containsKey(relPath) || allReferencedUnexpandedByRelpath.get(relPath).isEmpty()) {
				assert !allReferencedUnexpandedByRelpath.containsKey(relPath) : "no empty but present entries expected";
				invalidRelPaths.add(relPath);
			} else {
				Set<IEntityId> matching = allReferencedUnexpandedByRelpath.get(relPath);
				entityIds.addAll(matching);
			}
		}

		if (!invalidRelPaths.isEmpty()) {
			if (errMsg4Filled) {
				pr.reportProblem(
						RuleParser.ProblemInvalidCustomRefErrorEntityImpl.ofIfFilled(invalidRelPaths, localeForErrorMessages,
								originRuleForErrors));
			} else {
				pr.reportProblem(
						RuleParser.ProblemInvalidCustomRefErrorEntityImpl.ofIfNotFilled(invalidRelPaths, localeForErrorMessages,
								originRuleForErrors));
			}
		}
		return entityIds;
	}

	private static boolean hasCustomRefEntities(IRule rule) {
		return !rule.getCustomRefErrorEntitiesIfFilled().isEmpty()
				|| !rule.getCustomRefErrorEntitiesIfNotFilled().isEmpty();
	}

	public static int getLevelToStartDuplicateSearch(String ruleFullname, ICondition condition, boolean isHModel) {
		return FindAnyAst.findeInBedingung(condition, AnalyseService::isRepetitionNotUnique, true)
				.map(IEntityListCondition.class::cast)
				.map(repNotUnqCond -> RepetitionNotUniqueCreator.getEbeneAbDerDuplikatGesucht(isHModel, ruleFullname,
						repNotUnqCond))
				.orElse(-1);
	}

	private CodeGenParseTreeHelper parseTreeHelperFromCodegenData(CodeGenData data, boolean expandGroups) {
		return new CodeGenParseTreeHelper(
				data.getEc(),
				expandGroups,
				data.getUniqueFieldNames().get(ruleInfo.getErrorEntity().getFullName()),
				data.getBaseYear(),
				data.getUniqueFieldNames()
		);
	}

	/**
	 * @param entityIdsCollector
	 * 		has two purposes: 1) assign unique variable names to the entity ids, in a way so that references
	 * 		between entityIds (e.g. for semantic index) will work (via variable name) and with a numbering
	 * 		order that is deterministic, so that the same codegen input will result in the same generated code
	 * 		source file.
	 * 		2) collect all encountered entities (including entities that may not be contained in the set
	 * 		directly, but that are referenced by one of the entityIds). In the collected List, each entityId
	 * 		that is referenced by one of the other entityIds occurs before any entityId that references it,
	 * 		which is crucial to ensure that all references can be resolved at the point where they occur
	 * 		in the generated code.
	 * 		The collected EntityId can be retrieved afterward in order to be assigned to
	 * 		orderedEntitiesForVariableDeclartion;
	 */
	private static Set<EntityId> assignDeterministicVariableNamesAndConvertToCodegenEntityIdsAndCollectOrderedEntityIdsForVariableDef(
			Set<IEntityId> entityIds,
			CodeGenParseTreeHelper parsetreeConverter, // parsetreeConverter could be statelessly derived directly form the
			// codeGenData within this method, *except* for the filterEntityIdsCollector and filterConditionVisitor attributes
			// (although their state is *probably* irrelevant for refEntities where filter conditions should not currently occur.
			EntityIdsCollector entityIdsCollector
	) {
		return entityIds.stream()
				.sorted(Comparator.comparing(MetaRegelReferenzenBuilder::toSortKey)) // sort, for reproducible generated code (what
				// matters in the order in which entityIdsCollector receives the entityIds)
				//
				// Note: convertCondition on the original condition would lack the implicitly referenced index fields of repeatable
				// groups for which there are semantic indexes
				// -> we convert the entities, including the implicit index fields, individually. EntityIdsCollector takes care of
				// assigning unique variable names across all the referenced entities, and makes the variable names consistent so
				// that references between entity ids (in case of semidx - filterconditions don't occur) can be resolved correctly.)
				.map(e -> parsetreeConverter.convertEntity(e, entityIdsCollector)).collect(Collectors.toSet());
	}

	private static String toSortKey(IEntityId ei) {
		// should be similar to the template at EntityId_varAssignment (java or groovy), but can ignore uniqueName and must ignore
		// variableName
		// Note: we cannot use EntityId::toCode, because that is entangled in the variableName assignment (which must happen
		// *after* sorting, for reproducible results).
		String semIdxStr = "";
		if (ei.getSemanticIndex() != null && ei.getSemanticIndex().size() > 0) {
			assert ei.getSemanticIndex().size() == 1;
			Object semIdx = ei.getSemanticIndex().get(0);
			if (semIdx instanceof String) {
				semIdxStr = (String) semIdx;
			} else if (semIdx instanceof IEntityId) {
				semIdxStr = toSortKey((IEntityId) semIdx);
			} else {
				assert false : "To ensure 100% reproducible codegen output, semIdx references of type " + semIdx.getClass()
						+ " should be explicitly handled by toSortKey too";
			}
		}
		return Stream.of(
						ei.getFullName(),
						semIdxStr,
						ei.getCategoryName(),
						ei.isOuter(),
						ei.getUsb(),
						ei.getRootGroupWildcardStart(), ei.getRootGroupWildcardEnd(),
						ei.getRowWildcardStart(), ei.getRowWildcardEnd(), // maybe not needed for A12-DVs, but probably doesn't harm either.
						Arrays.toString(ei.getIndices()), // maybe not needed for H-DVs, but probably doesn't harm either
						Arrays.toString(ei.getMaxValues())
				).map(Objects::toString)
				.collect(Collectors.joining("_")); // delimiter in order to disambiguate adjacent number strings
	}

	CodeGenData getData() {
		return data;
	}

	public MetaRegelReferenzen build() {
		return new MetaRegelReferenzen(this);
	}

	public RuleInfo getRuleInfo() {
		return ruleInfo;
	}

	public Set<IEntityId> getReferenzierteFeldIds() {
		return referenzierteFeldIds;
	}

	public Set<IEntityId> getReferenzierteAuslassungsFeldIds() {
		return referenzierteAuslassungsFeldIds;
	}

	public Set<String> getReferenzierteAuslassungsVordrucke() {
		return referenzierteAuslassungsVordrucke;
	}

	public Map<String, Integer> getFieldIndices() {
		return fieldIndices;
	}

	public boolean isServerCalcRule() {
		return isServerCalculation;
	}

	public boolean isDynamicEnumerationRule() {
		return isDynamicEnumeration;
	}

	public Set<EntityId> getReferenzierteAuslassungsEntityIds() {
		return referenzierteAuslassungsEntityIds;
	}

	public Set<EntityId> getReferenzierteEntityIds() {
		return referenzierteEntityIds;
	}

	public List<EntityId> getOrderedEntitiesForVariableDeclartion() {
		return orderedEntitiesForVariableDeclartion;
	}
}
