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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.teilvalidierung.CodeGenTeilvalidierungBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.teilvalidierung.RuleType;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.teilvalidierung.TeilValidierungFuehreAusCalls;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.teilvalidierung.TeilValidierungFuehreAusSplitFunctCode;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Parameter class for the template generating the classes regarding partial validation.
 */
@StringTemplateGroup("teilvalidierung")
public class CodeGenTeilValidierung extends CodeGenModel {

	private static final int NUM_FIELDS_PER_CALL_GROOVY = 10;
	private Map<String, String> uniqueFieldNamesMap;
	private Map<String, Integer> uniqueFieldIds = new HashMap<>();
	CodeGenRules rules;
	List<IField> fields;

	public CodeGenTeilValidierung(CodeGenTeilvalidierungBuilder builder) {
		super(builder);
		this.rules = builder.getRules();
		this.uniqueFieldNamesMap = builder.getData().getUniqueFieldNames();

		this.fields = EntityUtils.getAllFields(builder.getData().getEc());
		computeUniqueIds();
	}

	/**
	 * Computes the unique IDs used for the switch in order to split the partial validation into
	 * smaller chunks.
	 */
	private void computeUniqueIds() {
		int id = 0;
		for (IField field : fields) {
			uniqueFieldIds.put(uniqueFieldNamesMap.get(field.getFullName()), id++);
		}

	}

	public String toCodeAlle() {
		return toCode("all");
	}

	public String toCodeInfo() {
		return toCode("info");
	}

	public String toCodeHinweis() {
		return toCode("warning");
	}

	public String toCodeFehler() {
		return toCode("error");
	}

	public String getPaketName() {
		return this.getPackageName();
	}

	public String getFuehreAusSplitFunctionCodeCalls_Fehler() {
		return deriveSplitFunctionCodeCallsWithArgs(RuleType.ERROR, false);
	}

	public String getFuehreAusSplitFunctionCodeCalls_Hinweis() {
		return deriveSplitFunctionCodeCallsWithArgs(RuleType.WARNING, false);
	}

	public String getFuehreAusSplitFunctionCodeCalls_Info() {
		return deriveSplitFunctionCodeCallsWithArgs(RuleType.INFO, false);
	}

	public String getFuehreAusSplitFunctionCodeCalls_Preliminary() {
		return deriveSplitFunctionCodeCallsWithArgs(null, true);
	}

	public List<String> getRuleCheckObjectCreationsCode() {
		return rules.getRuleCheckObjectCreationsCode();
	}

	/**
	 * Hier kann nicht die Methode 'deriveSplitFunctionCodeCalls' der Oberklasse verwendet werden,
	 * denn die Methode der Oberklasse erzeugt Code der alle Teilmethoden nacheinander aufgeruft.
	 * Die Teilmethoden besitzen dort keine Argumente.
	 * <p>
	 * Beim Aufruf der Teilmethoden zur Ausführung der Teilvalidierung gibt es folgende
	 * Besonderheiten: - aus Performancegründen wird sichergestellt dass nur die Teilmethode
	 * aufgerufen wird, in der sich das jeweilige Fehlerfeld befindet. - um das zu ermöglichen
	 * werden die RegelAufrufe zunächst nach der internen Feldnummer des Fehlerfeldes aufsteigend
	 * sortiert. - die Teilmethoden besitzen mehrere Argumente
	 */
	private String deriveSplitFunctionCodeCallsWithArgs(RuleType ruleType, boolean preliminary) {
		Map<Integer, List<CodeGenRuleDefinition>> ruleCallsByFieldNr = getSortedFilteredRules(ruleType, preliminary);
		if (ruleCallsByFieldNr.isEmpty()) {
			if (preliminary) {
				return "\t// The model does not contain any preliminary rules.";
			} else {
				return String.format("\t// The model does not contain any rules of severity '%s'.", ruleType.toString());
			}
		}
		List<Integer> lFeldNrn = new ArrayList<>(ruleCallsByFieldNr.keySet());

		int numFieldsPerCall = SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT;
		if (data.getParams().getProgrammingLanguage() == ProgrammingLanguage.GROOVY) {
			numFieldsPerCall = NUM_FIELDS_PER_CALL_GROOVY;
		}
		int anzahlTeilmethoden = SplitFunctionsHelper.computeNumSplits(ruleCallsByFieldNr.keySet().size(), numFieldsPerCall);

		StringBuilder sbHauptMethode = new StringBuilder();
		for (int i = 0; i < anzahlTeilmethoden; i++) {
			int numElement = (i + 1) * numFieldsPerCall - 1;
			int maxFeldNr = (lFeldNrn.size() > numElement ? lFeldNrn.get(numElement) : lFeldNrn.get(lFeldNrn.size() - 1));
			sbHauptMethode.append(new TeilValidierungFuehreAusCalls(i == 0, maxFeldNr, i).toCode());
		}

		return sbHauptMethode.toString();
	}

	public String getFuehreAusSplitFunctionCode_Fehler() {
		return deriveSplitFunctionCodeWithArgs(RuleType.ERROR, false, "");
	}

	public String getFuehreAusSplitFunctionCode_Fehler_Header() {
		return deriveSplitFunctionCodeWithArgs(RuleType.ERROR, false, "h");
	}

	public String getFuehreAusSplitFunctionCode_Hinweis() {
		return deriveSplitFunctionCodeWithArgs(RuleType.WARNING, false, "");
	}

	public String getFuehreAusSplitFunctionCode_Hinweis_Header() {
		return deriveSplitFunctionCodeWithArgs(RuleType.WARNING, false, "h");
	}

	public String getFuehreAusSplitFunctionCode_Info() {
		return deriveSplitFunctionCodeWithArgs(RuleType.INFO, false, "");
	}

	public String getFuehreAusSplitFunctionCode_Info_Header() {
		return deriveSplitFunctionCodeWithArgs(RuleType.INFO, false, "h");
	}

	public String getFuehreAusSplitFunctionCode_Preliminary() {
		return deriveSplitFunctionCodeWithArgs(null, true, "");
	}

	public String getFuehreAusSplitFunctionCode_Preliminary_Header() {
		return deriveSplitFunctionCodeWithArgs(null, true, "h");
	}

	private SortedMap<Integer, List<CodeGenRuleDefinition>> getSortedFilteredRules(RuleType ruleType, boolean preliminary) {
		Predicate<CodeGenRuleDefinition> filter = rd -> rd.isPreliminary() == preliminary &&
				(ruleType == null || rd.isValidForRuleType(ruleType)) &&
				rd.getRuleInfo().getErrorEntity().isField();
		Map<String, List<CodeGenRuleDefinition>> lRegelAufrufe = rules.getFilteredRulesByErrorFieldName(filter);
		return sortByFeldNr(lRegelAufrufe);
	}

	/**
	 * Hier kann nicht die Methode 'deriveSplitFunctionCode' der Oberklasse verwendet werden, denn
	 * die Methode der Oberklasse splittet einzelne Aufrufe in mehrere Teilmethoden ohne besondere
	 * Reihenfolge außerdem benötigen dort die Teilmethoden keine Argumente und es werden stets alle
	 * Teilmethoden nacheinander aufgerufen.
	 * <p>
	 * Beim Splitten der RegelAufrufe für die Teilvalidierung gibt es folgende Besonderheiten: - aus
	 * Performancegründen wird sichergestellt dass nur die Teilmethode aufgerufen wird, in der sich
	 * das jeweilige Fehlerfeld befindet. - um das zu ermöglichen werden die RegelAufrufe zunächst
	 * nach der internen Feldnummer des Fehlerfeldes aufsteigend sortiert. - die Teilmethoden
	 * besitzen mehrere Argumente
	 */
	private String deriveSplitFunctionCodeWithArgs(RuleType ruleType, boolean preliminary, String suffix) {
		SortedMap<Integer, List<CodeGenRuleDefinition>> ruleCallsByFieldNr = getSortedFilteredRules(ruleType, preliminary);
		if (ruleCallsByFieldNr.isEmpty()) {
			return "";
		}

		int numFieldsPerCall = SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT;
		if (data.getParams().getProgrammingLanguage() == ProgrammingLanguage.GROOVY) {
			numFieldsPerCall = NUM_FIELDS_PER_CALL_GROOVY;
		}
		int anzahlTeilmethoden = SplitFunctionsHelper.computeNumSplits(ruleCallsByFieldNr
				.keySet().size(), numFieldsPerCall);
		// Die Regelaufrufe nach der internen Feldnummer des Aktionsfeldes
		// sortieren

		StringBuilder sbAll = new StringBuilder();
		Iterator<Integer> itFeldNr = ruleCallsByFieldNr.keySet().iterator();
		for (int i = 0; i < anzahlTeilmethoden; i++) {
			SortedMap<Integer, List<CodeGenRuleDefinition>> m = new TreeMap<>();
			int counter = 0;
			Integer firstFeldNr = null;
			Integer lastFeldNr = null;
			while (itFeldNr.hasNext() && counter++ < numFieldsPerCall) {
				lastFeldNr = itFeldNr.next();
				if (firstFeldNr == null) {
					firstFeldNr = lastFeldNr;
				}
				List<CodeGenRuleDefinition> ra = ruleCallsByFieldNr.get(lastFeldNr);
				m.put(lastFeldNr, ra);
			}

			sbAll.append(
					new TeilValidierungFuehreAusSplitFunctCode(firstFeldNr, lastFeldNr, i, m, ruleType, getName()).toCode(
							suffix));
		}

		if (itFeldNr.hasNext()) {
			throw new RuntimeException("Es wurden nicht alle Regelaufrufe auf die Teilmethoden verteilt.");
		}

		return sbAll.toString();
	}

	/**
	 * Liefert eine Map aus interner Feldnummer zu RegelAufruf-Objekt, wobei die Map nach der
	 * internen Feldnummer aufsteigend sortiert ist.
	 */
	private SortedMap<Integer, List<CodeGenRuleDefinition>> sortByFeldNr(Map<String, List<CodeGenRuleDefinition>> lRegelAufrufe) {
		return lRegelAufrufe.entrySet().stream()
				.collect(Java8Utils.toTreeMap(e -> getFieldId4Name(e.getKey()), Map.Entry::getValue));
	}

	private Integer getFieldId4Name(String fullName) {
		return Optional.<Integer>ofNullable(uniqueFieldIds.get(fullName))
				.orElseThrow(() -> new RuntimeException(
						String.format("Die interne Nummer des Aktionsfelds mit dem Unique Name '%s' konnte nicht ermittelt werden.",
								fullName)));
	}

	public String toCodeInfoHeader() {
		return toCode("info_h");
	}

	public String toCodeHinweisHeader() {
		return toCode("warning_h");
	}

	public String toCodeFehlerHeader() {
		return toCode("error_h");
	}
}
