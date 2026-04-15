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
import java.util.List;
import java.util.function.UnaryOperator;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenContext;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.AnnotationInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.MetaCodeGenHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule.RuleSeverityType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;
import com.mgmtp.a12.model.header.Annotation;

/**
 * Parameterklasse für die Codeerzeugung für eine Metaregel.
 */
@StringTemplateGroup("meta_rules")
public class MetaRegelMethod extends STParameterClassBase {

	private final IRule rule;
	private final String ruleNumber;
	private final MetaRegelReferenzen refs;
	private final AdditionalKeyValuesDelegate additionalKeyValuesDelegate;
	private String fehlertext;

	private final ICodeGeneratorParameters.ProgrammingLanguage programmingLanguage;

	public MetaRegelMethod(MetaRegelReferenzen metaRuleRefs, List<IMetaInfoCodeGenParams> metaCodeGenParams) {
		this.refs = metaRuleRefs;
		this.rule = refs.getRuleInfo().getRule();
		this.ruleNumber = refs.getRegelNummer();
		this.programmingLanguage = CodeGenContext.getInstance().getProgrammingLanguage();
		this.additionalKeyValuesDelegate = new AdditionalKeyValuesDelegate(metaCodeGenParams,
				existingKeys -> createAdditionalValues(),
				IMetaInfoCodeGenParams.META_ELEMENT_TYPE_RULE, UnaryOperator.identity(), rule.getFullName()
		);
	}

	public String getName() {
		return EntityUtils.getShortName(rule.getFullName());
	}

	public String getPfadName() {
		return rule.getFullName();
	}

	public String getPfad() {
		return EntityUtils.getParentPath(rule.getFullName());
	}

	public String getNummer() {
		return ruleNumber;
	}

	public String getFehlercode() {
		return new StringParameter(rule.getErrorCode()).toCode();
	}

	/**
	 * Die Methode wird für die C++ Codegenerierung verwendet. Da es dort keine Mehrsprachigkeit
	 * gibt, wird hier der deutsche Fehlertext zurückgegeben.
	 */
	public String getFehlerText() {
		return fehlertext;
	}

	/**
	 * Die Methode wird für die C++ Codegenerierung verwendet. Da es dort keine Mehrsprachigkeit
	 * gibt, muss hier der (deutsche) Fehlertext explizit gesetzt werden.
	 */
	public void setFehlertext(String fehlertextCode) {
		this.fehlertext = fehlertextCode;
	}

	public String getRegelArt() {
		return mapSeverityType(rule.getSeverityType());
	}

	private String mapSeverityType(RuleSeverityType severityType) {
		switch (severityType) {
			case ERROR:
				return "Fehler";
			case WARNING:
				return "Hinweis";
			case INFO:
				return "Info";
			default:
				throw new IllegalArgumentException("Unknown severity type" + severityType);
		}
	}

	public long getNumIdentifierRef() {
		return refs.getNumIdentifierRef();
	}

	public String getIdentifierRef() {
		return refs.getIdentifierRef();
	}

	public String getIdentifierAuslassungsRef() {
		return refs.getIdentifierAuslassungsRef();
	}

	public String getEntityRef() {
		return refs.getEntityRef();
	}

	public String getEntityOmissionRef() {
		return refs.getEntityAuslassungsRef();
	}

	public String getVordruckAuslassungsRef() {
		return refs.getVordruckAuslassungsRef();
	}

	public String isServerBerechnungsRegel() {
		return Boolean.toString(refs.isServerBerechnungsRegel());
	}

	public String isDynamicEnumerationRule() {
		return Boolean.toString(refs.isDynamicEnumerationRule());
	}

	public String getFehlerfeld() {
		return rule.getErrorEntity().getFullName();
	}

	public int getRuleNumber() {
		return refs.getRuleInfo().getRuleNumber();
	}

	public String getAdditionalKeyValuesCode() {
		return additionalKeyValuesDelegate.getAdditionalValuesCode();
	}

	public String getInternalAdditionalKeysCode() {
		return additionalKeyValuesDelegate.getInternalAdditionalKeysCode();
	}

	private List<IMetaInfoCodeGenParams> createAdditionalValues() {
		List<IMetaInfoCodeGenParams> result = new ArrayList<>();
		if (isJava() || isGroovy() || isJsVk()) {
			List<Annotation> annotations = rule.getAnnotations();
			if (annotations != null && !annotations.isEmpty()) {
				String code = MetaCodeGenHelper.getAnnotationsCode(annotations);
				result.add(MetaCodeGenHelper.createMetaInfoCodeGenParam("Annotation", code,
						IMetaInfoCodeGenParams.META_ELEMENT_TYPE_RULE,
						rule.getFullName()));
			}
		}
		return result;
	}

	// C++ only
	public boolean isWithAnnotationInfo() {
		List<Annotation> annotations = rule.getAnnotations();
		return (annotations != null && !annotations.isEmpty());
	}

	// C++ only
	public String getAnnotationInfoCode() {
		List<Annotation> annotations = rule.getAnnotations();
		var annotationInfo = new AnnotationInfo(getAnnotationInfoVariableName(), annotations);
		return annotationInfo.toCode();
	}

	// C++ only
	public String getAnnotationInfoVariableName() {
		return AnnotationInfo.VARIABLENAME_PREFIX + "regel_" + ruleNumber;
	}

	private boolean isJava() {
		return programmingLanguage == ICodeGeneratorParameters.ProgrammingLanguage.JAVA;
	}

	private boolean isGroovy() {
		return programmingLanguage == ICodeGeneratorParameters.ProgrammingLanguage.GROOVY;
	}

	private boolean isCpp() {
		return programmingLanguage == ICodeGeneratorParameters.ProgrammingLanguage.CPLUS;
	}

	private boolean isJsVk() {
		return programmingLanguage == ICodeGeneratorParameters.ProgrammingLanguage.JAVA_SCRIPT_VK;
	}

}
