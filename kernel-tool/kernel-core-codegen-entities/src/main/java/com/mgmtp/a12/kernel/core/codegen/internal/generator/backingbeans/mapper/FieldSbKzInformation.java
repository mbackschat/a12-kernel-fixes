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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenFieldDefinition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.IntegerParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.ListParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.LongParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.CodeGenMap;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.CodeGenPair;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.NullTerminal;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.ado.IExtmpField;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IFieldMapping;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IMapInfo;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IRootGroupMapping;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams.IMappedData;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.MapInfoUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

public class FieldSbKzInformation {

	private final CodeGenEntityData data;
	private final IExtmpField field;
	private final boolean kennzahlFlag;
	private CodeGenMap<ListParameter<LongParameter>, CodeGenPair<StringParameter, StringParameter>> sbKzMap;
	private ListParameter<IntegerParameter> sbKzRepetition1List;
	private String indexFieldUniqueName; // this index field controls either the root group splitting or the order in a field list
	private final IFieldMapping fieldMapInfo;
	private final IRootGroupMapping rootGroupMapInfo;
	private final IEC mappedModelEc;

	public FieldSbKzInformation(CodeGenFieldDefinition fieldDefinition) {
		this.data = fieldDefinition.getData();
		this.field = (IExtmpField) fieldDefinition.getField();
		this.kennzahlFlag = field.isKennzahlFlag();
		this.sbKzMap = new CodeGenMap<>("List<Long>, Pair<String, String>");
		this.sbKzRepetition1List = new ListParameter<>();
		IMappedData md = ExtmpUtils.isPacketCodeGenerationWithAssignedUXMLModel(data.getParams()) ?
				((IExtmpCodeGenParams) data.getParams()).getMappedModelParameter() :
				null;
		this.fieldMapInfo = md != null ? md.getFieldMappingInfo(field.getFullName()) : null;
		this.rootGroupMapInfo = fieldMapInfo != null ? md.getRootGroupMappingInfo(field.getFullName()) : null;
		this.mappedModelEc = md != null ? md.getEc() : null;
		fillSbKzInformation();
	}

	private void fillSbKzInformation() {
		if (ExtmpUtils.isPacketCodeGenerationWithAssignedUXMLModel(data.getParams())) {
			if (fieldMapInfo != null) {
				// the field mappings were checked before, if rootGroupMapInfo is null, it means there is an implementation error
				assert rootGroupMapInfo != null;
				// all sb/kz information is in the uXML model after the synchronization
				AtomicInteger i = new AtomicInteger(0);
				fieldMapInfo.getFieldIdentifiers().stream()
						.forEach(pairUXMLShortNames -> addSbKzEntries(pairUXMLShortNames, i.getAndIncrement()));
				this.indexFieldUniqueName = determineIndexFieldUniqueName();
			} // if there is no field mapping, this means no payload field -> we ignore the field
		} else if (!ExtmpUtils.hasAssignedUXMLModel(data.getModelSpecificSettings()) && !field.getSbKzMap().isEmpty()) {
			int maxIterLvl = EntityUtils.getMaxIterationLvl(field);
			long[] indicesTemplate = initIndices();
			field.getSbKzMap().entrySet().stream()
					.forEach(entry -> {
						long[] indices = indicesTemplate.clone();
						// the first index is always for the root group
						indices[0] = entry.getKey().get(0);
						if (maxIterLvl > 0) {
							// we need to fill in the second index on the correct layer
							// there can only be at most one iteration level besides the root group (assured through ExtmpNoMetaModelChecks#checkKennzahl)
							indices[maxIterLvl] = entry.getKey().get(1);
						}
						createSbKzEntryAndAddItToMap(indices, entry.getValue());
					});
		}
		addItemsToSbKzRepetition1List();
	}

	private void addSbKzEntries(Pair<String, String> pairUXMLShortNames, int fieldListIndex) {
		IExtmpField uXMLField1 = createUXMLField(rootGroupMapInfo.getRootGroupName1(), pairUXMLShortNames.getLeft());
		addAllSbKzEntriesToMap(uXMLField1, fieldListIndex, 1);

		if (!StringUtils.isEmpty(pairUXMLShortNames.getRight())) {
			IExtmpField uXMLField2 = createUXMLField(rootGroupMapInfo.getRootGroupName2(), pairUXMLShortNames.getRight());
			addAllSbKzEntriesToMap(uXMLField2, fieldListIndex, 2);
		}
	}

	private IExtmpField createUXMLField(String rootGroupName, String uxmlShortName) {//at that point, mappedModelEc cannot be null
		String uxmlFullname = rootGroupName + ElementPathUtils.SEPARATOR_CHAR + uxmlShortName;
		return (IExtmpField) mappedModelEc.get(uxmlFullname);
	}

	private void addAllSbKzEntriesToMap(IExtmpField uXMLField, int fieldListIndex, int splittingIndex) {
		for (Entry<List<Long>, Pair<String, String>> entry : uXMLField.getSbKzMap().entrySet()) {
			assert entry.getKey().size() == 2; // in h models there are only 2 repetition layers
			addSbKzEntryToMap(fieldListIndex, splittingIndex, entry);
		}
	}

	private void addSbKzEntryToMap(int fieldListIndex, int splittingIndex,
			Entry<List<Long>, Pair<String, String>> sbKzEntryForUXMLField) {
		long[] indices = initIndices();
		// no extra handling for usb mapping because the indices were initialized with 1
		// and for usb mapping this is what we want at the runtime

		if (MapInfoUtils.isMappingToRootGroupRepetition(rootGroupMapInfo)) {
			Long rootGroupRepetition4SbKz = sbKzEntryForUXMLField.getKey().get(0);
			if (kennzahlFlag && rootGroupRepetition4SbKz > 1) {
				// if the kennzahl flag is set to 'true', we don't generate sb/kz information for root group repetition > 1
				return;
			} else {
				indices[rootGroupMapInfo.getHierarchyLevel4RootgroupRepetition()] = rootGroupRepetition4SbKz;
			}
		}
		if (MapInfoUtils.isMappingToFieldList(fieldMapInfo)) {
			Integer mziSpec = fieldMapInfo.getMziSpecs().get(fieldListIndex);
			if (mziSpec != IMapInfo.NOT_APPLICABLE) {
				Integer accumulatedMziSpec = fieldMapInfo.getAccumulatedMziSpecs().get(fieldListIndex) - mziSpec;
				indices[fieldMapInfo.getHierarchyLevel4FieldList()] = accumulatedMziSpec + sbKzEntryForUXMLField.getKey().get(1);
			} else {
				indices[fieldMapInfo.getHierarchyLevel4FieldList()] = fieldListIndex + 1L;
			}
		}
		if (MapInfoUtils.isMappingToMzi(fieldMapInfo)) {
			indices[fieldMapInfo.getHierarchyLevel4Mzi()] = sbKzEntryForUXMLField.getKey().get(1);
		}
		if (MapInfoUtils.isSplit(rootGroupMapInfo)) {
			indices[rootGroupMapInfo.getHierarchyLevel4Splitting()] = splittingIndex;
		}
		createSbKzEntryAndAddItToMap(indices, sbKzEntryForUXMLField.getValue());
	}

	/**
	 * Creates the indices array of the field and initializes it with 1s.
	 */
	private long[] initIndices() {
		long[] indices = new long[field.getMax().length];
		Arrays.fill(indices, 1L);
		return indices;
	}

	private void createSbKzEntryAndAddItToMap(long[] indices, Pair<String, String> sbKzValue) {
		ListParameter<LongParameter> entryKey;
		if (ICodeGeneratorParameters.ProgrammingLanguage.CPLUS.equals(data.getParams().getProgrammingLanguage())) {
			entryKey = createListParameterCpp(indices);
		} else {
			entryKey = createListParameter(indices);
		}
		CodeGenPair<StringParameter, StringParameter> entryValue = new CodeGenPair<>(new StringParameter(sbKzValue.getLeft()),
				new StringParameter(sbKzValue.getRight()));

		sbKzMap.putEntry(entryKey, entryValue);
	}

	private ListParameter<LongParameter> createListParameterCpp(long[] array) {
		int maxListSize = 5; // there are at most 5 repeatable levels
		LongParameter defaultValue = new LongParameter(-1);
		ListParameter<LongParameter> result = new ListParameter<>();
		for (int i = 0; i < field.getMax().length; i++) {
			if (field.getMax()[i] > 1) {
				result.add(new LongParameter(array[i]));
			}
		}
		for (int i = result.size(); i < maxListSize; i++) {
			result.add(defaultValue);
		}
		return result;
	}

	private ListParameter<LongParameter> createListParameter(long[] array) {
		ListParameter<LongParameter> result = new ListParameter<>();
		for (Long item : array) {
			result.add(new LongParameter(item));
		}
		return result;
	}

	private void addItemsToSbKzRepetition1List() {
		if (rootGroupMapInfo == null) {
			if (kennzahlFlag) {
				sbKzRepetition1List.add(new IntegerParameter(0));
			}
		} else {
			if (MapInfoUtils.isMappingToUsb(rootGroupMapInfo) || MapInfoUtils.isMappingToTechnicalUsb(rootGroupMapInfo)) {
				sbKzRepetition1List.add(new IntegerParameter(rootGroupMapInfo.getHierarchyLevel4Usb()));
			}
			if (kennzahlFlag && MapInfoUtils.isMappingToRootGroupRepetition(rootGroupMapInfo)) {
				sbKzRepetition1List.add(new IntegerParameter(rootGroupMapInfo.getHierarchyLevel4RootgroupRepetition()));
			}
		}
	}

	private String determineIndexFieldUniqueName() {
		IField indexField = rootGroupMapInfo.getIndexField4SbKzQuery();
		if (indexField == null) {
			// there is no index field for the root group mapping, we check if there is an index field for the field mapping
			indexField = fieldMapInfo.getIndexField4SbKzQuery();
		}
		return indexField == null ? null : data.getUniqueFieldNames().get(indexField.getFullName());
	}

	public ListParameter<IntegerParameter> getSbKzRepetition1List() {
		return sbKzRepetition1List.isEmpty() ? null : sbKzRepetition1List;
	}

	public CodeGenMap<ListParameter<LongParameter>, CodeGenPair<StringParameter, StringParameter>> getSbKzMap() {
		return sbKzMap.isNotEmpty() ? sbKzMap : null;
	}

	public String getIndexField4SbKzQueryCode() {
		if (indexFieldUniqueName == null) {
			return new NullTerminal().toCode("nullptr");
		} else {
			return new StringParameter(indexFieldUniqueName).toCode();
		}
	}

	public int getHierarchyLevel4Usb() {
		return rootGroupMapInfo != null ? rootGroupMapInfo.getHierarchyLevel4Usb() : IMapInfo.NOT_APPLICABLE;
	}

}
