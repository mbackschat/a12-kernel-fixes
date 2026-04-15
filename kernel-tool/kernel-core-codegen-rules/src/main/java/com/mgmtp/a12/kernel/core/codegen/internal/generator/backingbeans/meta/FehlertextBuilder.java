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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.constants.EntityIdConstants;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdImpl;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.parser.internal.util.ReferencedIdsUtil;
import com.mgmtp.a12.kernel.core.parser.internal.util.ReferencedIdsUtil.ReferencedIdsMode;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FeldIdUtilities;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorLookup;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextErrorType;
import com.mgmtp.a12.kernel.core.service.internal.errortext.FehlertextUtil;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IErrortextParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IGroupIndexParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IIndexFieldParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IRepetitionIndexParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IRootgroupIndexParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IRowIndexParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.ISemanticIndex;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.ISimpleFieldParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.api.IUSBParameter;
import com.mgmtp.a12.kernel.core.service.internal.errortext.creation.impl.IndexFieldParameter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Creates a {@link Fehlertext} class and performs replacements of the error text parameters,
 * that can be performed during code generation.
 * <p>
 * The performed replacements are:
 * <ol>
 * <li>replacement of identifiers containing <code>#LfdNr</code>, <code>#Zeile</code>,
 * <code>#Usb</code>, if the field specifies iteration
 * for the corresponding level.</li>
 * <li>replacement of field names by the corresponding unique names.</li>
 * </ol>
 * <p>
 * ExtMP specific replacements of the form
 * <ul>
 * <li><code>$&lt;field name&gt;.Vordruckzeile$</code>,</li>
 * <li><code>$&lt;field name&gt;.Vordruckname$</code> or</li>
 * <li><code>$BaseYear[+-]$</code>
 * </ul>
 * will <b>not</b> be performed here and must have been done beforehand.
 */
public class FehlertextBuilder {

	private final Locale locale;
	private final String origErrorMsg;
	private final String replacedErrorMsg;
	private final Map<String, String> parameterMapping;
	private final RuleInfo ruleInfo;
	private final CodeGenData data;
	private final Map<String, String> full2UniqueFieldName;
	private final Set<IEntityId> refFeldIds;
	private final Locale lang;
	private final boolean isProgLangWithUsb;

	public FehlertextBuilder(Locale locale, String origErrorMsg,
			RuleInfo ruleInfo,
			CodeGenData data,
			Map<String, String> full2UniqueFieldName) {
		this.locale = locale;
		this.ruleInfo = ruleInfo;
		this.data = data;
		this.full2UniqueFieldName = full2UniqueFieldName;

		this.lang = data.getModelSpecificSettings().getLanguageUsedInConditionText();
		this.isProgLangWithUsb = data.getParams().getProgrammingLanguage().isWithUsb();

		this.refFeldIds = ReferencedIdsUtil.getDirektReferenzierteIFeldIds(ruleInfo.getCondition(),
				ReferencedIdsMode.USE_FILTERCONDITION_REPLACE_PSEUDO_ITERATION, data.getEc(), true);
		this.origErrorMsg = origErrorMsg;
		this.parameterMapping = getParameterMapping(origErrorMsg);
		this.replacedErrorMsg = replaceParameters(origErrorMsg, parameterMapping);
	}

	private String replaceParameters(String rawErrorText, Map<String, String> parameterMapping) {
		String[] teile = StringUtils.splitPreserveAllTokens(rawErrorText, "$");
		//nur jedes zweite Teil muss ersetzt werden.
		for (int i = 1; i < teile.length; i = i + 2) {
			String param = teile[i];
			teile[i] = parameterMapping.getOrDefault(param, param);
		}
		return StringUtils.join(teile, "$");
	}

	private Map<String, String> getParameterMapping(String rawErrorText) {
		Map<String, String> result = new HashMap<>();
		//Führe weitere Ersetzungen durch
		String[] teile = StringUtils.splitPreserveAllTokens(rawErrorText, "$");
		//nur jedes zweite Teil muss ersetzt werden.
		for (int i = 1; i < teile.length; i = i + 2) {
			String param = teile[i];
			IErrortextParameter parsedParam = null;
			if (!StringUtils.isEmpty(param)) {
				parsedParam = ruleInfo.getErrorTextParameters().get(param);
				if (parsedParam == null) {
					throw new IllegalArgumentException(
							String.format("Rule %s: Error text parameter '%s' not valid.", ruleInfo.getRule().getFullName(),
									param));
				}
			}
			if (parsedParam instanceof IRepetitionIndexParameter iRepetitionIndexParameter
					&& !(parsedParam instanceof IUSBParameter)) {
				result.put(param, ersetzeIterationsParameter(iRepetitionIndexParameter));
			} else if (parsedParam instanceof IIndexFieldParameter iIndexFieldParameter) {
				result.put(param, ersetzeIndexFeldParameter(iIndexFieldParameter));
			} else if (parsedParam instanceof ISimpleFieldParameter iSimpleFieldParameter) {
				result.put(param, normiereFeldName(iSimpleFieldParameter));
			} else {
				//keine Ersetzung
				result.put(param, param);
			}
		}
		return result;
	}

	/**
	 * Converts a token into the normalized output for the code generation. Furthermore the field
	 * name is replaced by the unique name of the field and '.value' is replaced by '.Wert' if
	 * English is set as language. This is done to simplify the processing in the runtime.
	 *
	 * @param param
	 * 		parsed field error text parameter
	 * @return normalized field name
	 */
	private String normiereFeldName(ISimpleFieldParameter param) {
		IEntity e = data.getEc().get(param.getFieldFullName());

		if (e == null || param.getFieldFullName() == null) {
			throw new IllegalArgumentException(
					"Field with fullname '" + param.getFieldFullName() + "' and referred to as '" + param.getEntityRefInErrorText() +
							"' not found");
		}
		String result = getFeldNameNormiert(param, e.getFullName());
		if (param.getCategoryName() != null) {
			// we add again the category name, if present
			result = result + EntityIdConstants.CATEGORY_VALUE_ACCESS + param.getCategoryName();
		}
		if (param.getSemanticIndex() != null) {
			// Semantic index
			String semIdxSpec = createSemIdxSpec(param.getSemanticIndex());
			result = result + EntityIdConstants.SEMANTIC_INDEX_SEPARATOR + semIdxSpec;
		}
		if (param.isValueReference()) {
			// hänge Postfix wieder an, wenn nötig.
			// Um die Verarbeitung in der Runtime einfach zu halten übersetzen wir .value mit .Wert
			result = result + FehlertextUtil.WERT;
		}

		return result;
	}

	private String ersetzeIndexFeldParameter(IIndexFieldParameter param) {
		if (param.getFieldFullName() == null) {
			throw new IllegalArgumentException(
					"Feld " + param.getEntityRefInErrorText() + "' nicht gefunden");
		}

		IIndexFieldParameter paramWithUniqueName = new IndexFieldParameter(full2UniqueFieldName.get(param.getFieldFullName()),
				null, param.isValueReference(), param.getCategoryName());

		String result = paramWithUniqueName.format(Locale.GERMANY);
		if (ProgrammingLanguage.VBA.equals(data.getParams().getProgrammingLanguage())) {
			return "<VBA_Indexfeldparameter: '" + result + "'>";
		} else {
			return result;
		}
	}

	private String getRulePath() {
		return EntityUtils.getParentPath(ruleInfo.getRule().getFullName());
	}

	/**
	 * Normiert ausgehend von den einzelnen FeldNamenteilen den Feldnamen.
	 *
	 * @return gibt normierten Feldnamen zurück.
	 */
	private String getFeldNameNormiert(ISimpleFieldParameter param, String fullName) {
		return getFeldNameNormiert(fullName, param.getRootgroupIndex(), param.getRowIndex(), param.getSemanticIndex());
	}

	private String getFeldNameNormiert(String fullName, String lfdNrSpec, String zeileSpec,
			ISemanticIndex semanticIndex) {

		String uniqueName = full2UniqueFieldName.get(fullName);
		String ebenenString = getEbenenSpecString(fullName, lfdNrSpec, zeileSpec, semanticIndex);

		return String.format("%s%s", uniqueName, ebenenString);
	}

	/**
	 * Ersetze für alle referenzierten Felder die Angaben für die Wiederholbarkeit entweder mit
	 * '@e', wenn die Ebene mit Iteration referenziert ist, '@1' für den * oder '@n' für eine
	 * konkreten Zahl n, falls eine spezifische Wiederholung angegeben wurde. Einschränkung: Falls
	 * in der Regelbedingung ein Feld mit verschiedenen Wiederholungen für die gleiche
	 * Wiederholungsebenen angegeben wurde z.B. [feldA.1] > [feldA.2] so wird entweder 1 oder 2
	 * zurückgegeben. Diese Einschränkung wird jedoch akzeptiert, da keine gute Lösung dafür möglich
	 * ist und der Fall auch eher exotisch ist.
	 * Falls es sowohl eine passende Referenz mit * als auch ohne * gibt, wird die Referenz ohne * verwendet.
	 *
	 * @param zeileSpec
	 * 		- Zeile als String, wie im Fehlertext angegeben bzw. null, wenn im Fehlertext
	 * 		keine Zeile explizit spezifiziert wurde
	 * @param lfdNrSpec
	 * 		- LfdNr als String, wie im Fehlertext angegeben bzw. null, wenn im Fehlertext
	 * 		keine LfdNr explizit spezifiziert wurde
	 * @param semanticIndex
	 * 		- Die Spezifikation des semantischen index, falls vorhanden, sonst
	 * 		<code>null</code>.
	 * @return Angabe der Ebenenspezifikation, die im Fehlertext verwendet werden soll.
	 */
	private String getEbenenSpecString(String fullName, String lfdNrSpec, String zeileSpec,
			ISemanticIndex semanticIndex) {
		// Consider ids without wildcards (or with fewer wildcards, incidentally,) first:
		// Note: we still need the fallback behavior where wildcard references
		// will be picked if no other matching reference exists (which will be
		// resolved to the first repetition of the field in a
		// subsequent (older) fallback). This is due to H-DVs, where the parser
		// does not currently guarantee that non-wildcard references exist. See
		// EXT-10438 (not implemented as of 2022) and
		// RuleErrortextParameterParser.checkDotValue.
		List<IEntityId> refFeldIdsNonWildcardFirst = refFeldIds.stream().sorted(Comparator.comparing(
						id -> (int) Arrays.stream(id.getIndices()).filter(i -> i == IParseTreeEntity.WILDCARD).count()))
				.toList();

		for (IEntityId id : refFeldIdsNonWildcardFirst) {
			if (fullName.equals(id.getFullName()) && semIdxSpecMatches(id, semanticIndex)) {
				return getLevelCode(id, lfdNrSpec, zeileSpec);
			}
		}
		//keine Referenz gefunden, darf nicht sein.
		throw new IllegalArgumentException(new FehlertextErrorLookup(lang)
				.getErrorMessage(FehlertextErrorType.INVALID_FIELD, fullName));
	}

	private boolean semIdxSpecMatches(IEntityId id, ISemanticIndex semanticIndex) {

		var semanticIndexSpec = Optional.ofNullable(semanticIndex).map(ISemanticIndex::getSemanticIndexSpec)
				.orElse(null);
		if (semanticIndexSpec == null) {
			return id.getSemanticIndex().isEmpty();
		}
		if (id.getSemanticIndex().isEmpty()) {
			return false;
		}
		if (id.getSemanticIndex().size() > 1) {
			throw new IllegalArgumentException("Currently, only one semantic index is allowed!");
		}
		Object semIdxOfEntityId = id.getSemanticIndex().get(0);

		if (semanticIndexSpec.startsWith("\"")) {
			if (semIdxOfEntityId instanceof String) {
				String semIdxFromSpec = semanticIndexSpec.substring(1, semanticIndexSpec.length() - 1);
				return semIdxOfEntityId.equals(semIdxFromSpec);
			}
			return false;
		} else {
			if (semIdxOfEntityId instanceof IEntityId semIdxEntity) {
				var semanticIndexSpecFullName =
						Optional.of(semanticIndex).map(ISemanticIndex::getSemanticIndexRefFullName)
								.orElse(null);

				if (semanticIndexSpecFullName == null) {
					throw new IllegalArgumentException("No entity found for " + semanticIndexSpec);
				}
				return (semIdxEntity.getFullName().equals(semanticIndexSpecFullName));
			}
			return false;
		}
	}

	private String getLevelCode(IEntityId id, String lfdNrSpec, String zeileSpec) {
		StringBuilder result = new StringBuilder();
		if (isProgLangWithUsb) {
			result.append(getSingleLevelCode(id.getUsb(), null));
		}
		result.append(getSingleLevelCode(FeldIdUtilities.getLfdNr(id), lfdNrSpec));
		long[] ebenenSpecs = id.getIndices();
		for (int i = 1; i < ebenenSpecs.length - 1; i++) {
			result.append(getSingleLevelCode(ebenenSpecs[i], null));
		}
		if (ebenenSpecs.length > 1) {
			result.append(getSingleLevelCode(FeldIdUtilities.getZeile(id), zeileSpec));
		}
		return result.toString();
	}

	private String getSingleLevelCode(long i, String specInFehlertext) {
		if (specInFehlertext != null) {
			return "@" + specInFehlertext;
		}
		if (i == IParseTreeEntity.ITERATION) {
			return FeldIdUtilities.ITERATIONS_EBENE_CODE;
		}
		return (i == IParseTreeEntity.WILDCARD) ? "@1" : "@" + Long.toString(i);
	}

	/**
	 * Ersetzung für folgende Parameter :
	 * <ul>
	 * <li>'#LfdNr' oder '#RootGroup'</li>
	 * <li>'#Zeile' bzw '#Zeile +/- Konstante'</li>
	 * <li>'#kontextPfad' (nur bei A12-Modellen)</li>
	 * </ul>
	 *
	 * @param param
	 * 		- der zu ersetzende Parameter
	 * @return - der Parameter, wie er im generierten Code erwartet wird
	 */
	private String ersetzeIterationsParameter(IRepetitionIndexParameter param) {
		if (param instanceof IRootgroupIndexParameter) {
			//LfdNr ist immer die Ebene 0
			return "#0";
		}

		if (param instanceof IRowIndexParameter rowIndexParam) {
			//Bei H-DVs gibt es keine wiederholbaren Kontexte
			int anzahlWHKontexte = getAnzWiederholbarerKontexteOhneVordruck(ruleInfo.getRule().getErrorEntity().getFullName());
			//Anzahl Wiederholungsebenen ist Anzahl der wiederholbaren Kontexte + 2 (Vordruck und Zeile)
			//Index ist eins weniger
			String calc = rowIndexParam.getCalculation() != null ?
					rowIndexParam.getCalculation().format() :
					"";
			return "#" + Integer.toString(anzahlWHKontexte + 1) + calc;
		}

		if (!(param instanceof IGroupIndexParameter groupIndexParam)) {
			throw new IllegalArgumentException("Method may only be called for rootgroup, group and row index parameters.");
		}

		String groupFullName = groupIndexParam.getGroupFullName();
		ISemanticIndex semanticIndex = groupIndexParam.getSemanticIndex();

		if (groupFullName == null) { // parameter #RuleGroup (#RegelKontext)
			groupFullName = getRulePath();
		}

		if (semanticIndex == null) {
			if (AnalyseService.isParallelIteration(ruleInfo.getCondition(), true)) {
				return "#" + createGroupSpecWithIndexFieldRef(groupFullName, null);
			}
			//wir sind in der neuen Welt: jede Ebene ist wiederholbar - sonst hätte der Fehlertextchecker einen Fehler gemeldet.
			int anzahlSlash = StringUtils.countMatches(groupFullName, EntityUtils.SEPARATOR);
			return "#" + Integer.toString(anzahlSlash - 1);
		} else {
			return "#" + createSemIdxGroupSpec(groupFullName, semanticIndex);
		}
	}

	private String createSemIdxGroupSpec(String fullPath, ISemanticIndex semanticIndex) {
		String semanticIndexSpec = createSemIdxSpec(semanticIndex);
		return createGroupSpecWithIndexFieldRef(fullPath,
				semanticIndex) + EntityIdConstants.SEMANTIC_INDEX_SEPARATOR + semanticIndexSpec;
	}

	/**
	 * Create the group reference for semantic index or parallel iteration. The latter if semantic index is <code>null</code>.
	 */
	private String createGroupSpecWithIndexFieldRef(String groupFullName, ISemanticIndex semanticIndex) {
		IEntityId entityId = findMatchingReference(groupFullName, semanticIndex)
				.orElseThrow(() ->
						new IllegalArgumentException(getExceptionMessage(groupFullName, semanticIndex)));
		IEntityId groupId = createGroupId(entityId, groupFullName)
				.orElseThrow(() -> new IllegalArgumentException("Unable to resolve " + groupFullName));
		String ebenenSpec = getLevelCode(groupId, null, null);

		return groupFullName + ebenenSpec;
	}

	private String getExceptionMessage(String path, ISemanticIndex semanticIndex) {
		String semanticIndexSpec = semanticIndex != null ? semanticIndex.getSemanticIndexSpec() : null;
		if (semanticIndexSpec != null) {
			return String.format("Cannot find semantic index reference for %s in group %s", semanticIndexSpec, path);
		} else {
			return String.format("Cannot find reference with parallel iteration in group %s", path);
		}
	}

	private String createSemIdxSpec(ISemanticIndex param) {
		var semanticIndex = param.getSemanticIndexSpec();
		if (semanticIndex == null)
			return null;

		if (semanticIndex.startsWith("\"")) {
			return semanticIndex;
		} else {
			var fullPath = param.getSemanticIndexRefFullName();
			if (fullPath == null) {
				throw new IllegalArgumentException(String.format("Field '/%s/%s' nicht gefunden.", getRulePath(), semanticIndex));
			}
			return getFeldNameNormiert(fullPath, null, null, null);
		}
	}

	/**
	 * Find a matching reference for the given path and semanticIndex. If semantic index is <code>null</code>, parallel iteration
	 * is checked for the entity.
	 */
	private Optional<IEntityId> findMatchingReference(String fullPath,
			ISemanticIndex semanticIndex) {

		String pathWithSlash = fullPath + EntityUtils.SEPARATOR_CHAR;
		return refFeldIds.stream()
				.filter(e -> matchesGroup(e, fullPath, pathWithSlash, semanticIndex))
				.findAny();
	}

	/**
	 * Does the entity e match the path and semanticIndex? If semantic index is <code>null</code>, parallel iteration is checked
	 * for the entity.
	 */
	private boolean matchesGroup(IEntityId e, String path, String pathWithSlash,
			ISemanticIndex semanticIndex) {
		var semanticIndexSpeck = Optional.ofNullable(semanticIndex).map(ISemanticIndex::getSemanticIndexSpec);
		if (semanticIndexSpeck.isEmpty() && !AnalyseService.isParallelIterationId(e)) {
			return false;
		}
		return (e.getFullName().equals(path) || e.getFullName().startsWith(pathWithSlash)) &&
				semIdxSpecMatches(e, semanticIndex);
	}

	private Optional<IEntityId> createGroupId(IEntityId subEntityId, String fullName) {
		IEntity group = data.getEc().get(fullName);
		if (group == null) {
			return Optional.empty();
		}

		List<Long> indices = Arrays.stream(subEntityId.getIndices())
				.boxed()
				.limit((long) EntityUtils.getLevel(group.getFullName()) + 1)
				.collect(Collectors.toList());

		return Optional.of(new EntityIdImpl(group, indices, IParseTreeEntity.UNSPECIFIED, subEntityId.getSemanticIndex(), false,
				data.getEc(), subEntityId.isOuter(), PathNotationConfig.IRRELEVANT));
	}

	private int getAnzWiederholbarerKontexteOhneVordruck(String fullName) {
		if (ExtmpUtils.isHModel(data.getModelSpecificSettings())) {
			return 0;
		} else {
			return StringUtils.countMatches(fullName, EntityUtils.SEPARATOR) - 2;
		}
	}

	Locale getLocale() {
		return locale;
	}

	String getOrigErrorMsg() {
		return origErrorMsg;
	}

	public String getReplacedErrorMsg() {
		return replacedErrorMsg;
	}

	Map<String, String> getParameterMapping() {
		return parameterMapping;
	}

	int getRuleNumber() {
		return ruleInfo.getRuleNumber();
	}

	public Fehlertext build() {
		return new Fehlertext(this);
	}
}
