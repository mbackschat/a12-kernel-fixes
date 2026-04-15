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

import static com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util.MetaInfoCodeGenParams.getFilteredSortedAdditionalValuesCpp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.AnnotationInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenFieldDefinition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenFeldTypBasis;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.mapper.FieldSbKzInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BooleanParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.CodeGenMap;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICPPMetaKeys;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.LanguageHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.MetaCodeGenHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.NullTerminal;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.internal.utils.IExtmpMetaKeysBase;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.CodeProfileType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;
import com.mgmtp.a12.model.header.Annotation;

/**
 * Generates code to instantiate a Feld_t class for a given field.
 */
@StringTemplateGroup("meta_entity")
public final class MetaFeld extends STParameterClassBase {

	public static final List<String> ADDITIONAL_VALUES_CPP_ORDERING = List.of(
			// feld_t values
			IExtmpMetaKeysBase.FIELD_FORMATKENNZEICHEN,
			ICPPMetaKeys.FIELD_SONDERDRUCK,
			ICPPMetaKeys.FIELD_EINGEFUEGTKENNZEICHEN,
			IExtmpMetaKeysBase.FIELD_VORDRUCKZEILENNUMMER,
			IMetaKeys.FIELD_TRANSIENT,
			IMetaKeys.FIELD_EXPLICITLY_MANDATORY,
			IExtmpMetaKeysBase.FIELD_INDEX_FIELD_REQUIRED_4_SB_KZ_QUERY,
			IMetaKeys.FIELD_INDEX_FIELD,
			ICPPMetaKeys.FIELD_HIERARCHY_LEVEL_4_USB_MAPPING
	);

	private final int interneNr;
	private final CodeGenFieldDefinition feld;
	private final MetaGemeinsamesFormat gemeinsamesFormat;
	private final CodeProfileType codeProfileType;
	private final ProgrammingLanguage programmingLanguage;
	private final AdditionalKeyValuesDelegate additionalKeyValuesDelegate;

	private FieldSbKzInformation sbKzInformation;

	public MetaFeld(int interneNr, CodeGenFieldDefinition feld, MetaGemeinsamesFormat mgf,
			List<IMetaInfoCodeGenParams> metaCodeGenParams, CodeProfileType codeProfileType) {
		this.interneNr = interneNr;
		this.feld = feld;
		this.gemeinsamesFormat = mgf;
		this.codeProfileType = codeProfileType;
		if (ExtmpUtils.isExtmp(feld.getData().getModelSpecificSettings())) {
			this.sbKzInformation = new FieldSbKzInformation(feld);
		}
		this.programmingLanguage = feld.getData().getParams().getProgrammingLanguage();
		// Must be initialized after necessary parameters for createExtendedAdditionalValues

		UnaryOperator<List<IMetaInfoCodeGenParams>> sorter =
				ProgrammingLanguage.CPLUS.equals(programmingLanguage)
						? params -> getFilteredSortedAdditionalValuesCpp(params, ADDITIONAL_VALUES_CPP_ORDERING)
						: UnaryOperator.identity();
		this.additionalKeyValuesDelegate = new AdditionalKeyValuesDelegate(metaCodeGenParams, this::createExtendedAdditionalValues,
				IMetaInfoCodeGenParams.META_ELEMENT_TYPE_FIELD, sorter, feld.getFullName());
	}

	private List<IMetaInfoCodeGenParams> createExtendedAdditionalValues(List<IMetaInfoCodeGenParams> existingKeys) {
		List<IMetaInfoCodeGenParams> result = new ArrayList<>();
		if (!ExtmpUtils.isMapper(codeProfileType)) {
			result.add(createFieldMetaInfo(IMetaKeys.FIELD_GLOBAL, Boolean.toString(feld.getField().isGlobal())));
		}
		result.add(createFieldMetaInfo(IMetaKeys.FIELD_EXPLICITLY_MANDATORY, Boolean.toString(feld.getField().isMandatory())));
		result.add(createFieldMetaInfo(IMetaKeys.FIELD_TRANSIENT, Boolean.toString(feld.getField().isTransient())));
		result.add(createFieldMetaInfo(IMetaKeys.FIELD_INDEX_FIELD, Boolean.toString(feld.isIndexField())));
		if (existingKeys.stream().noneMatch(it -> IMetaKeys.FIELD_LABEL.equals(it.getKey()))) {
			// Create an empty Map<Locale, String> in generated code
			result.add(
					createFieldMetaInfo(IMetaKeys.FIELD_LABEL,
							new CodeGenMap<LanguageHelper, StringParameter>("Locale, String").toCode()));
		}

		if (ExtmpUtils.isExtmp(feld.getData().getModelSpecificSettings()) && (isJava() || isCpp())) {
			String indexField4SbKzQueryCode = sbKzInformation.getIndexField4SbKzQueryCode();
			result.add(createFieldMetaInfo(IExtmpMetaKeysBase.FIELD_INDEX_FIELD_REQUIRED_4_SB_KZ_QUERY,
					(isCpp() && indexField4SbKzQueryCode == null) ? "" : indexField4SbKzQueryCode));

			if (isCpp()) {
				int hierarchyLevel4UsbMapping = sbKzInformation.getHierarchyLevel4Usb();
				result.add(createFieldMetaInfo(ICPPMetaKeys.FIELD_HIERARCHY_LEVEL_4_USB_MAPPING,
						Integer.toString(hierarchyLevel4UsbMapping)));

			}
		}

		if (isJava() || isGroovy() || isJsVk()) {
			List<Annotation> annotations = feld.getField().getAnnotations();
			if (annotations != null && !annotations.isEmpty()) {
				String code = MetaCodeGenHelper.getAnnotationsCode(annotations);
				result.add(createFieldMetaInfo(IMetaKeys.FIELD_ANNOTATION, code));
			}
		}

		return result;
	}

	private boolean isJava() {
		return programmingLanguage == ProgrammingLanguage.JAVA;
	}

	private boolean isCpp() {
		return programmingLanguage == ProgrammingLanguage.CPLUS;
	}

	private boolean isJsVk() {
		return programmingLanguage == ProgrammingLanguage.JAVA_SCRIPT_VK;
	}

	private boolean isGroovy() {
		return programmingLanguage == ProgrammingLanguage.GROOVY;
	}

	private IMetaInfoCodeGenParams createFieldMetaInfo(String key, String value) {
		return createFieldMetaInfo(key, value, false);
	}

	private IMetaInfoCodeGenParams createFieldMetaInfo(String key, String value, boolean internal) {
		return MetaCodeGenHelper.createMetaInfoCodeGenParam(key, value, IMetaInfoCodeGenParams.META_ELEMENT_TYPE_FIELD,
				feld.getFullName(), internal);
	}

	public int getSbKzRepetition1ListSize() {
		if (ExtmpUtils.isExtmp(feld.getData().getModelSpecificSettings()) && sbKzInformation.getSbKzRepetition1List() != null) {
			return sbKzInformation.getSbKzRepetition1List().size();
		} else {
			return 0;
		}
	}

	public boolean isSbKzRepetition1ListEmpty() {
		return getSbKzRepetition1ListSize() <= 0;
	}

	public String getSbKzRepetition1ListCode() {
		if (!isSbKzRepetition1ListEmpty()) {
			return sbKzInformation.getSbKzRepetition1List().toCode();
		} else {
			return new NullTerminal().toCode("nullptr");
		}
	}

	public int getSbKzMapSize() {
		if (ExtmpUtils.isExtmp(feld.getData().getModelSpecificSettings()) && sbKzInformation.getSbKzMap() != null) {
			return sbKzInformation.getSbKzMap().size();
		} else {
			return 0;
		}
	}

	public boolean isSbKzMapEmpty() {
		return getSbKzMapSize() <= 0;
	}

	/*
	 * Note that the 'Sachbereich' is not provided at the meta API in Java anymore, but in C++. To keep code generation simple, we
	 * still generate this information for Java too.
	 */
	public String getSbKzMapCode() {
		if (!isSbKzMapEmpty()) {
			return sbKzInformation.getSbKzMap().toCode();
		} else {
			return new NullTerminal().toCode("nullptr");
		}
	}

	public String getGemeinsamesFormatNrCode() {
		return gemeinsamesFormat.getNumber();
	}

	public String getName() {
		return EntityUtils.getShortName(feld.getFullName());
	}

	public String getPfadName() {
		return feld.getField().getFullName();
	}

	public String getMaxZeile() {
		return String.valueOf(feld.getMaxZeilen());
	}

	public String getPflichtFeldCode() {
		BooleanParameter p = new BooleanParameter(feld.isPflichtfeld());
		return p.toCode();
	}

	public String getMandatoryForRepeatableGroupCode() {
		BooleanParameter p = new BooleanParameter(feld.isMandatoryForRepeatableGroup());
		return p.toCode();
	}

	public String getFormat() {
		CodeGenFeldTypBasis ft = feld.getFeldtyp();
		return MetaUtilService.getERFormat(ft);
	}

	public String getFormatVariableName() {
		return gemeinsamesFormat.getTypName();
	}

	public String getJSName() {
		return feld.getJSFunktionsName();
	}

	public Set<Integer> getVordruckPflichtFeldLfdNr() {
		return feld.getVordruckPflichtFelder();
	}

	public Set<Integer> getPflichtFeldLfdNr() {
		return feld.getPflichtFeldLfdNr();
	}

	public String getUniqueName() {
		return feld.getUniqueName();
	}

	public String getAdditionalKeyValuesCode() {
		return additionalKeyValuesDelegate.getAdditionalValuesCode();
	}

	public String getInternalAdditionalKeysCode() {
		return additionalKeyValuesDelegate.getInternalAdditionalKeysCode();
	}

	public int getInterneNr() {
		return interneNr;
	}

	public boolean isBerechnungsUebertragServer() {
		return feld.isBerechnungsUebertragServer();
	}

	// C++ only
	public boolean isWithAnnotationInfo() {
		List<Annotation> annotations = feld.getField().getAnnotations();
		return (annotations != null && !annotations.isEmpty());
	}

	// C++ only
	public String getAnnotationInfoCode() {
		List<Annotation> annotations = feld.getField().getAnnotations();
		var annotationInfo = new AnnotationInfo(getAnnotationInfoVariableName(), annotations);
		return annotationInfo.toCode();
	}

	// C++ only
	public String getAnnotationInfoVariableName() {
		return AnnotationInfo.VARIABLENAME_PREFIX + "feld_" + String.valueOf(interneNr);
	}

	// C++ only
	public String getErrorText4ReqCode() {
		StringParameter p = new StringParameter(feld.getErrorText4Req());
		return p.toCode();
	}

	@Override
	public String toCode(String param) {
		if (ExtmpUtils.isMapper(codeProfileType)) {
			if (!StringUtils.isEmpty(param)) {
				throw new IllegalArgumentException("param currently not supported for MetaFeld#toCode in mapper code generation");
			}
			return super.toCode("mapperOnly");
		} else {
			return super.toCode(param);
		}
	}

}
