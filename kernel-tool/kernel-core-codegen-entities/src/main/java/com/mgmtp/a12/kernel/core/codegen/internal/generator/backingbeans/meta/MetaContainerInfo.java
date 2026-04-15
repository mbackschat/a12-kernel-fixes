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
import java.util.Set;
import java.util.function.UnaryOperator;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.AnnotationInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.LongParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.MetaCodeGenHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.NullTerminal;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;
import com.mgmtp.a12.model.header.Annotation;

/**
 * Implementiert die Daten für MetaContainerInfo für die Codegenerierung der MetaSchnittstelle
 */
@StringTemplateGroup("meta_entity")
public class MetaContainerInfo extends STParameterClassBase {

	private Long maxIndex = 1L;//Wiederholbarkeit - null, falls nicht wiederholbarer Kontext
	private final String name;
	private Integer containerIndex = null;
	private final ProgrammingLanguage lang;
	private final AdditionalKeyValuesDelegate additionalKeyValuesDelegate;
	private final Set<String> indexFields;
	private final List<Annotation> annotations;

	/**
	 * Generiere einen Container, der nicht oberster Container ist.
	 */
	public MetaContainerInfo(IGroup group, List<IMetaInfoCodeGenParams> metaParams, ProgrammingLanguage lang, boolean isHModel) {
		this.name = group.getFullName();
		this.maxIndex = isHModel && !EntityUtils.isRootGroup(this.name) ? null : EntityUtils.getMax(group);
		this.lang = lang;
		this.indexFields = group.getIndexFieldNames();
		this.annotations = group.getAnnotations();

		this.additionalKeyValuesDelegate = new AdditionalKeyValuesDelegate(
				metaParams,
				existingMetaInfos -> createExtraMetaInfoCodeGenParams(),
				IMetaInfoCodeGenParams.META_ELEMENT_TYPE_GROUP,
				UnaryOperator.identity(),
				this.name);
	}

	public String getName() {
		return name;
	}

	/**
	 * Getter für die Codegenerierung
	 *
	 * @return Darstellung in den Container_t Strukturen, abhängig von der Zielsprache
	 */
	public String getMaxIndex() {
		if (maxIndex == null) {
			if (lang == ProgrammingLanguage.CPLUS || lang == ProgrammingLanguage.VBA) {
				return "0";
			}
			return new NullTerminal().toCode("nullptr");
		}
		return new LongParameter(maxIndex).toCode();
	}

	public List<String> getIndexFieldsCode() {
		return new ArrayList<>(indexFields);
	}

	// C++ only
	public boolean isWithAnnotationInfo() {
		return (annotations != null && !annotations.isEmpty());
	}

	// C++ only
	public String getAnnotationInfoCode() {
		var annotationInfo = new AnnotationInfo(getAnnotationInfoVariableName(), annotations);
		return annotationInfo.toCode();
	}

	// C++ only
	public String getAnnotationInfoVariableName() {
		return AnnotationInfo.VARIABLENAME_PREFIX + "container_" + containerIndex.toString();
	}

	public String getAdditionalValuesCode() {
		return additionalKeyValuesDelegate.getAdditionalValuesCode();
	}

	public String getInternalAdditionalKeysCode() {
		return additionalKeyValuesDelegate.getInternalAdditionalKeysCode();
	}

	private List<IMetaInfoCodeGenParams> createExtraMetaInfoCodeGenParams() {
		List<IMetaInfoCodeGenParams> result = new ArrayList<>();
		if (isJava() || isGroovy() || isJsVk()) {
			if (annotations != null && !annotations.isEmpty()) {
				result.add(createGroupMetaInfo("Annotation", MetaCodeGenHelper.getAnnotationsCode(annotations)));
			}
		}
		return result;
	}

	public Integer getContainerIndex() {
		return containerIndex;
	}

	public void setContainerIndex(Integer conatinerIndex) {
		this.containerIndex = conatinerIndex;
	}

	private boolean isJava() {
		return lang == ICodeGeneratorParameters.ProgrammingLanguage.JAVA;
	}

	private boolean isGroovy() {
		return lang == ICodeGeneratorParameters.ProgrammingLanguage.GROOVY;
	}

	private boolean isCpp() {
		return lang == ICodeGeneratorParameters.ProgrammingLanguage.CPLUS;
	}

	private boolean isJsVk() {
		return lang == ICodeGeneratorParameters.ProgrammingLanguage.JAVA_SCRIPT_VK;
	}

	private IMetaInfoCodeGenParams createGroupMetaInfo(String key, String value) {
		return createGroupMetaInfo(key, value, false);
	}

	private IMetaInfoCodeGenParams createGroupMetaInfo(String key, String value, boolean internal) {
		return MetaCodeGenHelper.createMetaInfoCodeGenParam(key, value, IMetaInfoCodeGenParams.META_ELEMENT_TYPE_GROUP, name,
				internal);
	}
}
