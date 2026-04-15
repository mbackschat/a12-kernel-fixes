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

import static com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT;
import static com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper.deriveSplitFunctionCallsCode;
import static com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper.deriveSplitFunctionCode;
import static com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper.deriveSplitFunctionCodeCalls;

import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenEREntities;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util.MetaSprachUnterschiedeHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Parameter class for the templates of the Meta_Regel_ classes.
 */
@StringTemplateGroup("meta_rules")
public class CodeGenMetaRules extends CodeGenEREntities<CodeGenData> {

	private final boolean fullMetaCode;

	private final List<MetaRegelReferenzen> regelReferenzen;
	private final List<MetaRegelFehlertexte> regelFehlertexte;
	private final List<MetaRegelMethod> metaRegelMethods;

	private final Integer numMetaRulesPerFile;

	private final int numMetaRuleFiles;
	private int currentMetaRuleFile = 1;

	public CodeGenMetaRules(CodeGenMetaRulesBuilder builder) {
		super(builder);
		this.fullMetaCode = builder.isFullMetaCode();
		this.regelReferenzen = builder.getRuleReferences();
		this.regelFehlertexte = builder.getRegelFehlertexte();
		this.metaRegelMethods = builder.getMetaRegelMethods();
		this.numMetaRuleFiles = builder.getNumMetaRuleFiles();
		this.numMetaRulesPerFile = builder.getNumMetaRulesPerFile();
	}

	/**
	 * Aufgrund einer Java-Einschränkung kann in Java, in Gegensatz zu C++, die Initialisierung
	 * nicht über lange Initialisierungslisten erfolgen. In Java existiert eine Begrenzung
	 * (65000Bytes) der statitischen Initialisierer, sowie der einzelnen Methoden (inklusive des
	 * Konstruktors). Aus diesem Grund werden die Regeln in Java in mehreren privaten Methoden
	 * initialisiert. *
	 *
	 * @return String, der die Methoden für die Regelerstellung repräsentiert
	 */
	public String getRegelMethodSplitFunctionCode() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}

		List<MetaRegelMethod> metaRuleMethods = getCurrentMetaRuleMethods();
		String methodSigTemplate = ProgrammingLanguage.VBA.equals(data.getParams().getProgrammingLanguage()) ?
				MetaSprachUnterschiedeHelper.VBA_INIT_REGEL_METHOD_NAME :
				MetaSprachUnterschiedeHelper.JAVA_INIT_REGEL_METHOD_NAME;
		return deriveSplitFunctionCode(metaRuleMethods,
				methodSigTemplate,
				ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				data.getParams().getProgrammingLanguage());
	}

	public String getRegelMethodMvkJsCode() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}

		if (!metaRegelMethods.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format(MetaSprachUnterschiedeHelper.JSVK_METHOD_DEF_INIT_REGEL_TEMPLATE, this.getName()));
			for (MetaRegelMethod mrm : metaRegelMethods) {
				sb.append(mrm.toCode());
				sb.append("\n");
			}
			sb.append("};");
			return sb.toString();
		} else {
			return "";
		}
	}

	public String getMetaRegelnSplitFunctionCodeAufrufe() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}
		List<MetaRegelReferenzen> ruleReferences = getCurrentRuleReferences();
		return deriveSplitFunctionCodeCalls(ruleReferences.size(),
				MetaSprachUnterschiedeHelper.formatMethodCall(MetaSprachUnterschiedeHelper.METHOD_NAME_INIT_REGEL_TEMPLATE,
						data.getParams().getProgrammingLanguage()),
				ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				data.getParams().getProgrammingLanguage());
	}

	public String getMetaRegelnMvkJsCodeAufruf() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}

		if (regelReferenzen.isEmpty()) {
			return "";
		} else {
			return MetaSprachUnterschiedeHelper.JSVK_METHOD_NAME_INIT_REGEL_TEMPLATE;
		}
	}

	public String getRegelMethodRefsSplitFunctionCode() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}
		List<MetaRegelReferenzen> ruleReferences = getCurrentRuleReferences();
		return SplitFunctionsHelper.deriveSplitFunctionCodeBasedOnSize(ruleReferences,
				MetaSprachUnterschiedeHelper.JAVA_INIT_REGEL_REFS_METHOD_NAME,
				ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				data.getParams().getProgrammingLanguage(),
				MetaRegelReferenzen::getNumberOfFragments);
	}

	public String getRegelMethodRefsMvkJsCode() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}

		if (!regelReferenzen.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format(MetaSprachUnterschiedeHelper.JSVK_METHOD_DEF_INIT_REGEL_REFS_TEMPLATE, this.getName()));
			for (MetaRegelReferenzen mrr : regelReferenzen) {
				sb.append(mrr.toCode());
				sb.append("\n");
			}
			sb.append("};");
			return sb.toString();
		} else {
			return "";
		}
	}

	public String getMetaRegelRefsSplitFunctionCodeAufrufe() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}
		List<MetaRegelReferenzen> ruleReferences = getCurrentRuleReferences();
		int numSplits4RuleReferences = SplitFunctionsHelper.computeNumSplitsBasedOnSize(ruleReferences,
				ELEMENTS_PER_SPLIT_METHOD_DEFAULT, MetaRegelReferenzen::getNumberOfFragments);
		return deriveSplitFunctionCallsCode(numSplits4RuleReferences,
				MetaSprachUnterschiedeHelper.METHOD_NAME_INIT_REGEL_REFS_TEMPLATE,
				data.getParams().getProgrammingLanguage(), null);
	}

	public String getMetaRegelRefsMvkJsCodeAufruf() {
		if (regelReferenzen.isEmpty()) {
			return "";
		} else {
			return MetaSprachUnterschiedeHelper.JSVK_METHOD_NAME_INIT_REGEL_REFS_TEMPLATE;
		}
	}

	public String getMetaRegelFehlertexteSplitFunctionCode() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}

		List<MetaRegelFehlertexte> ruleErrorTexts = getCurrentRuleErrorTexts();
		String methodSigTemplate = ProgrammingLanguage.VBA.equals(data.getParams().getProgrammingLanguage()) ?
				MetaSprachUnterschiedeHelper.VBA_INIT_REGEL_FEHLERTEXTE_METHOD_NAME :
				MetaSprachUnterschiedeHelper.JAVA_INIT_REGEL_FEHLERTEXTE_METHOD_NAME;
		return deriveSplitFunctionCode(ruleErrorTexts,
				methodSigTemplate,
				ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				data.getParams().getProgrammingLanguage());
	}

	public String getMetaRegelFehlertexteMvkJsCode() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}

		if (!regelFehlertexte.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format(MetaSprachUnterschiedeHelper.JSVK_METHOD_DEF_INIT_REGEL_FEHLERTEXTE_TEMPLATE, this.getName()));
			for (MetaRegelFehlertexte mrf : regelFehlertexte) {
				sb.append(mrf.toCode());
				sb.append("\n");
			}
			sb.append("};");
			return sb.toString();
		} else {
			return "";
		}
	}

	public String getMetaRegelFehlertexteSplitFunctionCodeAufrufe() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}
		List<MetaRegelFehlertexte> ruleErrorTexts = getCurrentRuleErrorTexts();
		return deriveSplitFunctionCodeCalls(ruleErrorTexts.size(),
				MetaSprachUnterschiedeHelper.formatMethodCall(
						MetaSprachUnterschiedeHelper.METHOD_NAME_INIT_REGEL_FEHLERTEXTE_TEMPLATE,
						data.getParams().getProgrammingLanguage()),
				ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				data.getParams().getProgrammingLanguage());
	}

	public String getMetaRegelFehlertexteMvkJsCodeAufruf() {
		//MetaRegelinformation nur generieren,  wenn CP Full.
		if (!fullMetaCode) {
			return "";
		}

		if (regelFehlertexte.isEmpty()) {
			return "";
		} else {
			return MetaSprachUnterschiedeHelper.JSVK_METHOD_NAME_INIT_REGEL_FEHLERTEXTE_TEMPLATE;
		}
	}

	// TODO: Speicherplatzverschwendung. Wir sollten die eingeschränkte Regelmenge verwenden
	// Aber dann müsste man die verwendete Regelnummer relativ zur startRegel machen.
	public String getAnzRegeln() {
		return String.valueOf(regelReferenzen.size());
	}

	public int getNumMetaRuleFiles() {
		return numMetaRuleFiles;
	}

	public void setMetaRuleFileNumber(int fileNumber) {
		currentMetaRuleFile = fileNumber;
	}

	public String getDateiNummerCode() {
		return Integer.toString(currentMetaRuleFile);
	}

	private int getStartRuleNumber() {
		if (numMetaRulesPerFile == null) {
			return 0;
		}
		return (currentMetaRuleFile - 1) * numMetaRulesPerFile;
	}

	private int getEndRuleNumber() {
		if (numMetaRulesPerFile == null) {
			return regelReferenzen.size();
		}
		return Math.min((currentMetaRuleFile) * numMetaRulesPerFile, regelReferenzen.size());
	}

	private List<MetaRegelReferenzen> getCurrentRuleReferences() {
		return getCurrentRuleParts(regelReferenzen, rr -> rr.getRuleInfo().getRuleNumber());
	}

	private List<MetaRegelMethod> getCurrentMetaRuleMethods() {
		return getCurrentRuleParts(metaRegelMethods, MetaRegelMethod::getRuleNumber);
	}

	private List<MetaRegelFehlertexte> getCurrentRuleErrorTexts() {
		return getCurrentRuleParts(regelFehlertexte, MetaRegelFehlertexte::getRegelNummer);
	}

	private <T> List<T> getCurrentRuleParts(List<T> referenceList, ToIntFunction<T> getRuleNumber) {
		int startRule = getStartRuleNumber();
		int endRule = getEndRuleNumber();
		return referenceList.stream()
				.filter(r -> startRule <= getRuleNumber.applyAsInt(r) && getRuleNumber.applyAsInt(r) < endRule)
				.collect(Collectors.toList());
	}

	public boolean isFullMetaCode() {
		return fullMetaCode;
	}

	//VBA
	public int getNumberOfMetaRulesMinusOne() {
		return Math.max(0, regelReferenzen.size() - 1);
	}
}
