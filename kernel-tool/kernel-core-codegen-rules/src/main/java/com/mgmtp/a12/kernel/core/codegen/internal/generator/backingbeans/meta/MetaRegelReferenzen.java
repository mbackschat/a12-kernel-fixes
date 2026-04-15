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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenRuleConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util.MetaSprachUnterschiedeHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeFragmentContainer;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeOutput;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.NullTerminal;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Parameter class for the template used to initialize information for references used by rules.
 * Used for the Meta_Regel_ class of the generated code.
 */
@StringTemplateGroup("meta_rules")
public class MetaRegelReferenzen extends STParameterClassBase implements ICodeFragmentContainer {
	private final RuleInfo ruleInfo;

	private Map<String, Integer> feldIndices;
	private Set<IEntityId> referenzierteFeldIds;

	private CodeGenData data;
	private Set<IEntityId> referenzierteAuslassungsFeldIds;
	private Set<String> referenzierteAuslassungsVordrucke;
	private final Set<EntityId> referenzierteAuslassungsEntityIds;
	private final Set<EntityId> referenzierteEntityIds;
	/*
	 * The order matters because entities may reference other entities, and the order ensures that all entities occur before they
	 * are referenced.
	 * References between entities are possible for semantic index with a field ref, and in filter conditions (but filter conditions
	 * do not currently occur for refEntities because they get removed before code generation)
	 */
	private final List<EntityId> orderedEntitiesForVariableDeclartion;

	private final boolean isServerCalcRule;
	private final boolean isDynamicEnumerationRule;

	public MetaRegelReferenzen(MetaRegelReferenzenBuilder builder) {
		this.data = builder.getData();
		this.ruleInfo = builder.getRuleInfo();
		this.isServerCalcRule = builder.isServerCalcRule();
		this.isDynamicEnumerationRule = builder.isDynamicEnumerationRule();

		this.referenzierteAuslassungsFeldIds = builder.getReferenzierteAuslassungsFeldIds();
		this.referenzierteAuslassungsVordrucke = builder.getReferenzierteAuslassungsVordrucke();
		this.referenzierteEntityIds = builder.getReferenzierteEntityIds();
		this.referenzierteAuslassungsEntityIds = builder.getReferenzierteAuslassungsEntityIds();
		this.orderedEntitiesForVariableDeclartion = builder.getOrderedEntitiesForVariableDeclartion();
		assert this.orderedEntitiesForVariableDeclartion.stream().allMatch(eid -> eid.getFilterCondition() == null) :
				"filter conditions are not allowed on refEntities, the RtIdentifierTemplate variables that might be referenced " +
						"in the filter code are not currently defined in the generated MetaRegel classes " +
						"(Note: entityIds *from* filter conditions *are allowed*";
		assert this.orderedEntitiesForVariableDeclartion.stream().allMatch(eid -> !eid.isOuter()) :
				"referenced entities from filter condition should have been converted to the 'outside perspective' in " +
						"ReferencedIIdsUtils.transformFilterConditionId";
		// note: if something like IEntityId.isInner existed, we'd check that it is not set for any of the above ids
		this.referenzierteFeldIds = builder.getReferenzierteFeldIds();
		this.feldIndices = builder.getFieldIndices();
	}

	@Override
	public int getNumberOfFragments() {
		// even if this object contains other types of sub elements,
		// we take the referenced identifiers since the number of these is the greatest
		return referenzierteFeldIds.size();
	}

	/**
	 * Bestimme den intern verwendeten Feld-Index
	 *
	 * @param fullName
	 * @return Index des Feldes. Im Falle einer Gruppe wird immer -1 zurückgeliefert.
	 * 		Falls der Index eines Feldes nicht gefunden wird, wird eine Runtime-Exception geworfen.
	 */
	private Integer getIndexFromFullName(String fullName) {
		Integer feldIndex = feldIndices.get(fullName);
		if (feldIndex == null) {
			if (data.getEc().get(fullName).isGroup()) {
				// We need no field index for groups
				return -1;
			}
			throw new IllegalArgumentException("Index für Feld " + fullName + " nicht gefunden.");
		}

		return feldIndex;
	}

	public String getRegelNummer() {
		return Long.toString(ruleInfo.getRuleNumber());
	}

	public String getRegelFullName() {
		return ruleInfo.getRule().getFullName();
	}

	public long getNumIdentifierRef() {
		return feldIdSetToCode(referenzierteFeldIds).size();
	}

	public String getIdentifierCode() {
		return feldIdSetToCode(referenzierteFeldIds).stream().collect(Collectors.joining(",\n"));
	}

	public boolean getHasAuslassungsReferenzen() {
		return !this.referenzierteAuslassungsFeldIds.isEmpty();
	}

	public String getIdentifierAuslassungsCode() {
		return feldIdSetToCode(referenzierteAuslassungsFeldIds).stream().collect(Collectors.joining(",\n"));
	}

	public boolean getHasOmissionEntityReferences() {
		return !this.referenzierteAuslassungsEntityIds.isEmpty();
	}

	/**
	 * this code constructs RtIdentifierTemplates for all entitiyIds in referenzierteEntityIds
	 * and referenzierteAuslassungsEntityIds and assigns them to variables.
	 * {@link #getEntityTemplateRefsCode} and {@link #getAuslassungsEntityTemplateRefsCode} only
	 * generate lists of references to those RtIdentifierTemplate variables.
	 */
	// only for IResult.addMessageV2 (currently java & groovy only)
	public List<String> getRefEntityVarDeclarationCode() {
		return orderedEntitiesForVariableDeclartion.stream()
				// Do *not* sort alphabetically, that could result in a corrupt ordering if multi-digit entityId variable numbers
				// occur ("entityVar10" would sort before "entityVar9"). This can result in "reference-before-declared" in the
				// generated code, if e.g. entityVar10 has a semantic index reference to entityVar9.
				.map(co -> co.toCode("varAssignment"))
				.collect(Collectors.toList());
	}

	// only for IResult.addMessageV2 (currently java & groovy only)
	public String getAuslassungsEntityTemplateRefsCode() {
		return referenzierteAuslassungsEntityIds.stream()
				.map(EntityId::toCode)
				.sorted()
				.collect(Collectors.joining(",\n"));
	}

	// only for IResult.addMessageV2 (currently java & groovy only)
	public String getEntityTemplateRefsCode() {
		return referenzierteEntityIds.stream()
				.map(EntityId::toCode)
				.sorted()
				.collect(Collectors.joining(",\n"));
	}

	public boolean getHasAuslassungsVordrucke() {
		return !this.referenzierteAuslassungsVordrucke.isEmpty();
	}

	public String getVordruckAuslassungsCode() {
		List<String> codeFragmente = new ArrayList<>();
		for (String v : referenzierteAuslassungsVordrucke) {
			codeFragmente.add(new StringParameter(v).toCode());
		}
		return StringUtils.join(codeFragmente, ", ");
	}

	public boolean getHasFelder() {
		return !referenzierteFeldIds.isEmpty();
	}

	public boolean getHasEntities() {
		return !referenzierteEntityIds.isEmpty();
	}

	public boolean getHasSemanticIndices() {
		return referenzierteFeldIds.stream().anyMatch(fid -> fid.getSemanticIndex().stream().anyMatch(e -> e instanceof IEntityId));
	}

	/**
	 * Die folgenden Methoden erzeugen die Parameter für den Aufbau einer Regel.
	 * Falls die entsprechenden Regelreferenzen leer sind (z.B. keine referenzierten Felder) wird
	 * statt des Parameternamens ein null erzeugt.
	 * <p>
	 * Die entsprechenden Methode werden von gleichnamigen MethodRegelMethod Methoden aufgerufen und
	 * im entsprechenden Template verwendet.
	 */
	public String getIdentifierRef() {
		return getRef(referenzierteFeldIds, "refIdentifier");
	}

	public String getIdentifierAuslassungsRef() {
		return getRef(referenzierteAuslassungsFeldIds, "refAuslassungsIdentifier");
	}

	public String getEntityRef() {
		return getRef(referenzierteEntityIds, "refEntity");
	}

	public String getEntityAuslassungsRef() {
		return getRef(referenzierteAuslassungsEntityIds, "refAuslassungsEntity");
	}

	public String getVordruckAuslassungsRef() {
		return getRef(referenzierteAuslassungsVordrucke, "refAuslassungsVordrucke");
	}

	private String getRef(Collection<? extends Object> ref, String varName) {
		if (ref.isEmpty()) {
			return new NullTerminal().toCode("nullptr");
		}
		return MetaSprachUnterschiedeHelper.getReferenzen(ruleInfo.getRuleNumber(), varName,
				data.getParams().getProgrammingLanguage());
	}

	// Groovy only
	public boolean getUseSplittedClosuresForIdentifierRef() {
		return referenzierteFeldIds.size() >= CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY;
	}

	// Groovy only
	public boolean getUseSplittedClosuresForAuslassungsIdentifierRef() {
		return referenzierteAuslassungsFeldIds.size() >= CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY;
	}

	// Groovy only
	public String getRefIdentifierSplitFunctionCalls() {
		String methodName = String.format(MetaSprachUnterschiedeHelper.CLOSURE_NAME_IDENTIFIERREF_TEMPLATE + "()",
				getRegelNummer());
		List<ICodeOutput> codeOutputs = feldIdSetToCodeOutput(referenzierteFeldIds);
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(codeOutputs.size(), methodName,
				CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY, ProgrammingLanguage.GROOVY, " + ", "");
	}

	// Groovy only
	public String getRefIdentifierSplitFunctionCode() {
		String methodNameTemplate = String.format(MetaSprachUnterschiedeHelper.GROOVY_IDENTIFIERREF_CLOSURE_NAME, getRegelNummer());
		List<ICodeOutput> codeOutputs = feldIdSetToCodeOutput(referenzierteFeldIds);
		return SplitFunctionsHelper.deriveSplitFunctionCode(codeOutputs, methodNameTemplate,
				CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY, ProgrammingLanguage.GROOVY, "\t[\n",
				"\t]\n");
	}

	// Groovy only
	public String getRefAuslassungsIdentifierSplitFunctionCalls() {
		String methodName = String.format(MetaSprachUnterschiedeHelper.CLOSURE_NAME_AUSLASSUNGSIDENTIFIERREF_TEMPLATE + "()",
				getRegelNummer());
		List<ICodeOutput> codeOutputs = feldIdSetToCodeOutput(referenzierteAuslassungsFeldIds);
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(codeOutputs.size(), methodName,
				CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY, ProgrammingLanguage.GROOVY, " + ", "");
	}

	// Groovy only
	public String getRefAuslassungsIdentifierSplitFunctionCode() {
		String methodNameTemplate = String.format(MetaSprachUnterschiedeHelper.GROOVY_AUSLASSUNGSIDENTIFIERREF_CLOSURE_NAME,
				getRegelNummer());
		List<ICodeOutput> codeOutputs = feldIdSetToCodeOutput(referenzierteAuslassungsFeldIds);
		return SplitFunctionsHelper.deriveSplitFunctionCode(codeOutputs, methodNameTemplate,
				CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY, ProgrammingLanguage.GROOVY, "\t[\n",
				"\t]\n");
	}

	// Groovy only
	public boolean getUseOneClosureForIdentifierRef() {
		return referenzierteFeldIds.size() >= CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_ONE_CLOSURE_GROOVY;
	}

	// Groovy only
	public boolean getUseOneClosureForAuslassungsIdentifierRef() {
		return referenzierteAuslassungsFeldIds.size() >= CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_ONE_CLOSURE_GROOVY;
	}

	// Groovy only
	public boolean getUseSplittedClosuresForEntityRef() {
		return referenzierteEntityIds.size() >= CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY;
	}

	// Groovy only
	public boolean getUseSplittedClosuresForAuslassungsEntityRef() {
		return referenzierteAuslassungsEntityIds.size() >= CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY;
	}

	// Groovy only
	public String getRefEntitySplitFunctionCalls() {
		String methodName = String.format(MetaSprachUnterschiedeHelper.CLOSURE_NAME_ENTITYREF_TEMPLATE + "()",
				getRegelNummer());
		List<ICodeOutput> codeOutputs = feldIdSetToCodeOutput(referenzierteEntityIds);
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(codeOutputs.size(), methodName,
				CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY, ProgrammingLanguage.GROOVY, " + ", "");
	}

	// Groovy only
	public String getRefAuslassungsEntitySplitFunctionCalls() {
		String methodName = String.format(MetaSprachUnterschiedeHelper.CLOSURE_NAME_AUSLASSUNGSENTITYREF_TEMPLATE + "()",
				getRegelNummer());
		List<ICodeOutput> codeOutputs = feldIdSetToCodeOutput(referenzierteAuslassungsEntityIds);
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(codeOutputs.size(), methodName,
				CodeGenRuleConstants.NUM_INIT_PER_IDENTIFIERREF_SPLITTED_CLOSURES_GROOVY, ProgrammingLanguage.GROOVY, " + ", "");
	}

	/**
	 * Erzeugt aus einer Menge an FeldIds den Code für die Initialisierung
	 */
	private List<String> feldIdSetToCode(Set<? extends IEntityId> inFeldIds) {
		Function<IEntityId, Boolean> entityFilter = e -> true;
		Function<MetaIdentifier, String> metaIdToCode = MetaIdentifier::toCode;
		return entityIdSetToCode(inFeldIds, entityFilter, metaIdToCode);
	}

	// Groovy only
	private ICodeOutput toCodeOutput(String s) {
		return () -> "\t\t" + s + ",";
	}

	// Groovy only
	private List<ICodeOutput> feldIdSetToCodeOutput(Set<? extends IEntityId> inFeldIds) {
		Function<IEntityId, Boolean> entityFilter = e -> true;
		Function<MetaIdentifier, String> metaIdToCode = MetaIdentifier::toCode;
		List<String> codeFragments = entityIdSetToCode(inFeldIds, entityFilter, metaIdToCode);
		return codeFragments.stream().map(this::toCodeOutput).collect(Collectors.toList());
	}

	private List<String> entityIdSetToCode(Set<? extends IEntityId> entityIdSet,
			Function<IEntityId, Boolean> entityFilter, Function<MetaIdentifier, String> metaIdToCode) {
		// ACHTUNG: Das folgende Set ist notwendig, denn ansonsten
		//          wird derselbe String u.U. mehrfach in die Metadaten
		//          generiert, denn FeldId definiert keine equals-Methode.
		Set<String> codeFragmente = new HashSet<>();
		List<String> result = new ArrayList<>();
		Function<IEntityId, Integer> fieldIndexMapper = e -> getIndexFromFullName(e.getFullName());
		for (IEntityId eId : entityIdSet) {
			if (entityFilter.apply(eId)) {
				MetaIdentifier metaId = new MetaIdentifier(ruleInfo.getRuleNumber(), fieldIndexMapper, eId,
						data.getParams().getProgrammingLanguage());
				String codeFragment = metaIdToCode.apply(metaId);
				boolean isNoCopy = codeFragmente.add(codeFragment);
				if (isNoCopy) {
					result.add(codeFragment);
				}
			}
		}
		return result.stream().sorted().collect(Collectors.toList());
	}

	public boolean isServerBerechnungsRegel() {
		return this.isServerCalcRule;
	}

	public boolean isDynamicEnumerationRule() {
		return this.isDynamicEnumerationRule;
	}

	RuleInfo getRuleInfo() {
		return ruleInfo;
	}
}
