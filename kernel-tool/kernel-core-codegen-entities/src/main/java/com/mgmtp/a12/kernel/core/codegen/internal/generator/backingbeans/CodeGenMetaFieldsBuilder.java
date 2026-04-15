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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.BaseCodeGenConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaFeldFehlertexte;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaGemeinsameFormate;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.PackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.CalculationCodeGenUtilities;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.service.internal.extservices.MandatoryInformationService;
import com.mgmtp.a12.kernel.core.service.internal.mandatoryinformation.PflichtAngaben;
import com.mgmtp.a12.kernel.core.service.internal.mandatoryinformation.PflichtIdentifier;
import com.mgmtp.a12.kernel.core.service.internal.mandatoryinformation.PflichtInformation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * Builder for the template parameter class for the Meta_Felder_&lt;name&gt; classes.
 */
public class CodeGenMetaFieldsBuilder extends CodeGenEREntitiesBuilder<CodeGenEntityData> {

	private List<CodeGenFieldDefinition> fieldDefinitions;
	private PflichtAngaben pflichtAngaben;
	private Map<String, CodeGenFieldDefinition> fieldMap;
	private Map<String, Integer> fieldIndices = new HashMap<>();
	private final List<MetaFeldFehlertexte> errorTexts4Req = new ArrayList<>();

	private Set<String> carryoverFields;
	private List<Locale> languages;

	public CodeGenMetaFieldsBuilder(CodeGenEntityData data, PflichtAngaben pflichtAngaben, IProblemReporter problemReporter,
			PackageInformation packageInfo) {
		super(data, packageInfo, problemReporter);

		this.languages = data.getParams().getLanguages();
		this.pflichtAngaben = pflichtAngaben;
		List<IField> fields = EntityUtils.getAllFields(data.getEc());
		fieldMap = new LinkedHashMap<>();
		fieldDefinitions = new ArrayList<>();

		determineCarryoverAndOperandFields(data);

		Set<String> globalePflichtfelder = MandatoryInformationService.getPflichtFuerAlleLfdNrn(
				pflichtAngaben.getGlobalePflichtfelder());
		Set<String> mandatoryForRepeatableGroup = pflichtAngaben.getLocalMandatoryFieldsForRepeatableGroup();
		for (int i = 0; i < fields.size(); i++) {
			IField field = fields.get(i);
			CodeGenFieldDefinition newField = new CodeGenFieldDefinitionBuilder(
					i,
					data,
					field,
					globalePflichtfelder.contains(field.getFullName()),
					mandatoryForRepeatableGroup.contains(field.getFullName()),
					carryoverFields.contains(field.getFullName()),
					this.problemReporter,
					packageInfo).build();
			fieldMap.put(field.getFullName(), newField);

			fieldDefinitions.add(newField);
			fieldIndices.put(newField.getFullName(), i);
			if (newField.hasErrorTexts4Req()) {
				errorTexts4Req.add(
						new MetaFeldFehlertexte(String.valueOf(newField.getInternalNumber()), newField.getErrorTexts4Req(),
								BaseCodeGenConstants.PARAM_CUSTOM_ERROR_MSG_4_REQ));
			}
		}

		generateMandatoryInformationInFields();

	}

	@Override
	public CodeGenMetaFields build() {
		return new CodeGenMetaFields(this);
	}

	public List<CodeGenFieldDefinition> getFieldDefinitions() {
		return fieldDefinitions;
	}

	private void determineCarryoverAndOperandFields(CodeGenEntityData data) {
		Map<String, List<ICalculationInfo>> calculationInfos = data.getParams().getCalculationInformation().stream()
				.collect(Collectors.groupingBy(ci -> ci.getCalculatedField().getFullName()));

		carryoverFields = new HashSet<>();

		for (Entry<String, List<ICalculationInfo>> entry : calculationInfos.entrySet()) {
			String fullName = entry.getKey();
			List<ICalculationInfo> calcInfos = entry.getValue();
			if (CalculationCodeGenUtilities.isUebertrag(calcInfos)) {
				carryoverFields.add(fullName);
			}
		}
	}

	private void generateMandatoryInformationInFields() {
		addMandatoryInformationToFieldDefinition(pflichtAngaben.getGlobalePflichtfelder(), false);
		addMandatoryInformationToFieldDefinition(pflichtAngaben.getVordruckPflichtFelder(), true);
	}

	private void addMandatoryInformationToFieldDefinition(Set<PflichtInformation> vpfInfos, boolean rootgroupMandatoryInformation) {
		for (PflichtInformation pfInfo : vpfInfos) {
			for (PflichtIdentifier pf : pfInfo.getpMenge()) {
				CodeGenFieldDefinition fd = fieldMap.get(pf.getFullName());

				if (fd == null) {
					continue;
				}

				// Füge für jede laufende Nummer die Pflichteigenschaft in die entspr. Menge
				for (int lfdNr : MandatoryInformationService.ermittlePflichtLfdNummern(pfInfo, pf)) {
					if (rootgroupMandatoryInformation) {
						fd.addVordruckPflichtfeldLfdNr(lfdNr);
					} else {
						fd.addPflichtfeldLfdNr(lfdNr);
					}
				}
			}
		}
	}

	public Map<String, Integer> getFieldIndices() {
		return fieldIndices;
	}

	public int getNumFieldFilesCpp() {
		return SplitFunctionsHelper.computeNumSplits(fieldDefinitions.size(), CodeGenEntityConstants.NUM_METAFIELDS_PER_FILE_CPP);
	}

	public MetaGemeinsameFormate getGemeinsameFormate() {
		return new MetaGemeinsameFormate(fieldDefinitions, data);
	}

	public List<Locale> getLanguages() {
		return languages;
	}

	public List<MetaFeldFehlertexte> getErrorTexts4Req() {
		return errorTexts4Req;
	}
}
