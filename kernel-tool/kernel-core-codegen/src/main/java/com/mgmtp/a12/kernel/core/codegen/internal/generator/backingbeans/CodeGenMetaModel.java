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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.CharsetUtil;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenRuleConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.GeneratorConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.builder.CodeGenMetaModelBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.AdditionalKeyValuesDelegate;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.DefaultPresentationInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaContainerInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util.MetaSprachUnterschiedeHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.xml.MetaXMLReader;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.xml.MetaXMLReaderBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.xml.MetaXMLWriter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.xml.MetaXMLWriterBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.EncodingUtil;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.LanguageHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.CodeGenUtilities;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams.XML_RW_Type;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.Pair;
import com.mgmtp.a12.model.header.Annotation;

/**
 * Parameter class for the template generating a <code>Meta_&lt;name&gt;</code> class.
 */
@StringTemplateGroup("meta")
public class CodeGenMetaModel extends CodeGenModel {

	public static final IProblemReporter VOID_PROBLEM_REPORTER = p -> {
	};

	private final AdditionalKeyValuesDelegate additionalKeyValuesDelegate;
	private final Optional<Pair<List<int[]>, Set<String>>> charsetParseResult;

	private final Optional<int[][]> allowedCharRanges;
	private final Optional<List<int[]>> allowedCombinedCodepoints;
	private final ProgrammingLanguage progLang;
	private final int numFields;
	private final int numRules;

	private String productVersion;
	private char decimalSeparator;
	private Set<String> applBedNames;
	private Set<String> customFieldTypeNames;
	private List<MetaContainerInfo> metaContainerInfoList;
	private List<GroupSortInfo> groupSortInfoList;

	private MetaXMLWriter metaXmlWriter;
	private MetaXMLWriter metaUXmlWriter;
	private MetaXMLReader metaXmlReader;

	private final DefaultPresentationInformation defaultPresentationInformation;

	public CodeGenMetaModel(CodeGenMetaModelBuilder builder) {
		super(builder);

		final List<String> restrictedGraphemeSet = data.getParams().restrictedValidGraphemeSet();
		charsetParseResult = new CharsetUtil().parse(restrictedGraphemeSet, VOID_PROBLEM_REPORTER);

		this.allowedCharRanges = EncodingUtil.allowedRanges(charsetParseResult
				.map(Pair::getEntry1).orElse(Collections.emptyList()));
		this.allowedCombinedCodepoints = EncodingUtil.allowedCombinedGraphemes(charsetParseResult
				.map(Pair::getEntry2).orElse(Collections.emptySet()));

		this.progLang = data.getParams().getProgrammingLanguage();
		this.numFields = builder.getNumFields();
		this.numRules = builder.getNumRules();

		this.additionalKeyValuesDelegate = new AdditionalKeyValuesDelegate(builder.getAdditionalValues(),
				existingKeys -> createCommonAdditionalInfos(), IMetaInfoCodeGenParams.META_ELEMENT_TYPE_MODEL,
				UnaryOperator.identity(), "");

		this.productVersion = builder.getProductVersion();
		this.decimalSeparator = builder.getData().getParams().getDecimalSeparator();
		this.applBedNames = builder.getApplBedNames();
		this.customFieldTypeNames = builder.getCustomFieldTypeNames();
		this.metaContainerInfoList = builder.getMetaContainerInfoList();
		for (int i = 0; i < metaContainerInfoList.size(); i++) {
			MetaContainerInfo mi = metaContainerInfoList.get(i);
			mi.setContainerIndex(i);
		}
		this.groupSortInfoList = builder.getGroupSortInfoList();

		this.defaultPresentationInformation = builder.getDefaultPresentationInformation();

		XML_RW_Type type = ExtmpUtils.getXmlType(data.getParams());

		this.metaXmlWriter = new MetaXMLWriterBuilder(builder.getData(), type, false, builder.getPackageInformation()).build();
		this.metaUXmlWriter = new MetaXMLWriterBuilder(builder.getData(), type, true, builder.getPackageInformation()).build();
		this.metaXmlReader = new MetaXMLReaderBuilder(builder.getData(), type, builder.getPackageInformation()).build();
	}

	public boolean isEingeschraenkteZeichen() {
		return allowedCharRanges.isPresent() || allowedCombinedCodepoints.isPresent();
	}

	public boolean isMapperOnly() {
		return ExtmpUtils.isMapper(data.getParams());
	}

	/**
	 * Called in C++ template.
	 */
	public String getErlaubterZeichensatzCode() {
		return allowedCharRanges.map(ranges -> cppRanges(ranges[0], ranges[1]))
				.orElse("");
	}

	private void appendRange(StringBuilder stringBuilder, int start, int end) {
		stringBuilder.append(String.format("\n\t\t0x%04X, 0x%04X,", start, end));
	}

	private String cppRanges(int[] starts, int[] ends) {
		if (starts.length == 0)
			return "";

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < starts.length; ++i) {
			appendRange(sb, starts[i], ends[i]);
		}
		return sb.append("\n\t").toString();
	}

	/**
	 * Only gets called if #isEingeschraenkteZeichen is true - returns empty String to initialize empty arrays
	 */
	public String getErlaubterZeichensatzCodeStart() {
		if (ProgrammingLanguage.VBA == progLang) {
			return getRangesWithoutCrLf()
					.map(vbaRanges -> EncodingUtil.getAllowedCharsCode(vbaRanges[0], progLang))
					.orElse("");
		}
		return allowedCharRanges.map(ranges -> EncodingUtil.getAllowedCharsCode(ranges[0], progLang))
				.orElse("");
	}

	/**
	 * Only gets called if #isEingeschraenkteZeichen is true - returns empty String to initialize empty arrays
	 */
	public String getErlaubterZeichensatzCodeEnd() {
		if (ProgrammingLanguage.VBA == progLang) {
			return getRangesWithoutCrLf()
					.map(vbaRanges -> EncodingUtil.getAllowedCharsCode(vbaRanges[1], progLang))
					.orElse("");
		}
		return allowedCharRanges.map(ranges -> EncodingUtil.getAllowedCharsCode(ranges[1], progLang))
				.orElse("");
	}

	public boolean isCombinedCodepointsEmpty() {
		return allowedCombinedCodepoints.map(List::isEmpty).orElse(true);
	}

	public String getInitLegalGraphemeTrie() {
		return allowedCombinedCodepoints.map(graphemes -> EncodingUtil.graphemeTrieInitialization(graphemes, progLang))
				.orElse("");
	}

	/**
	 * If VBA-Code is generated, we want to filter out \r and \n from the ranges.
	 */
	private Optional<int[][]> getRangesWithoutCrLf() {
		return EncodingUtil.allowedRanges(charsetParseResult
				.map(Pair::getEntry1)
				.map(EncodingUtil::removeCrLfFromRangesForVba)
				.orElse(Collections.emptyList()));
	}

	/**
	 * Gib den Code für die MetaContainerInfo zurück. Dazu werden zunächst die Strukturen für den
	 * Container zusammengebaut und aus diesen dann Code erzeugt.
	 *
	 * @return Code für container_t Strukturen
	 */
	public String getMetaContainerListe() {
		return getMetaContainerListe2(StringUtils.EMPTY);
	}

	private String getMetaContainerListe2(String param) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < metaContainerInfoList.size(); i++) {
			MetaContainerInfo mi = metaContainerInfoList.get(i);
			mi.setContainerIndex(i);
			String code = mi.toCode(param);
			builder.append(code);
		}
		return builder.toString();
	}

	// C++ only
	public String getMetaContainerListeContentCode() {
		return getMetaContainerListe2("content");
	}

	public String getMetaContainerSplitFunctionCalls() {
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(
				metaContainerInfoList.size(),
				MetaSprachUnterschiedeHelper.INIT_CONTAINER_METHOD_CALL_TEMPLATE,
				CodeGenConstants.NUM_CONTAINERS_PER_METHOD_JAVA_GROOVY,
				progLang);
	}

	public String getMetaContainerSplitFunctionCode() {
		// If this method should be used for VBA or JSVK, we have to transform the method name
		return SplitFunctionsHelper.deriveSplitFunctionCode(
				metaContainerInfoList,
				MetaSprachUnterschiedeHelper.INIT_CONTAINER_METHOD_SIGNATURE_TEMPLATE,
				CodeGenConstants.NUM_CONTAINERS_PER_METHOD_JAVA_GROOVY,
				progLang);
	}

	public int getNumContainers() {
		return metaContainerInfoList.size();
	}

	public String getMetaCode() {
		return toCode("model");
	}

	public String getProduktVersion() {
		return productVersion;
	}

	public String getDezimalTrenner() {
		return String.valueOf(decimalSeparator);
	}

	public String getUnterstuetzteSprachenCode() {
		return data.getParams().getLanguages().stream()
				.map(LanguageHelper::new)
				.map(LanguageHelper::toCode)
				.collect(Collectors.joining(", "));
	}

	public Set<String> getApplBedNamenCode() {
		Set<String> nameCodes = new TreeSet<>();
		for (String name : applBedNames) {
			nameCodes.add(new StringParameter(name).toCode());
		}
		return nameCodes;
	}

	public Set<String> getCustomFieldTypeNamesCode() {
		return customFieldTypeNames.stream()
				.map(s -> new StringParameter(s).toCode())
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public String getSortInfo() {
		return toCode("sortInfo");
	}

	public boolean isWithSortInfo() {
		return !groupSortInfoList.isEmpty();
	}

	public List<String> getSortInfoCodeFragments() {
		return groupSortInfoList.stream()
				.map(GroupSortInfo::toCode)
				.collect(Collectors.toList());
	}

	public List<String> getSortInfoGroupNames() {
		return groupSortInfoList.stream()
				.map(GroupSortInfo::getFullname)
				.collect(Collectors.toList());
	}

	public String getAdditionalValuesCode() {
		return additionalKeyValuesDelegate.getAdditionalValuesCode();
	}

	public String getInternalAdditionalKeysCode() {
		return additionalKeyValuesDelegate.getInternalAdditionalKeysCode();
	}

	// C++ only
	public boolean isWithAnnotationInfo() {
		List<Annotation> annotations = data.getModelSpecificSettings().getAnnotations();
		return (annotations != null && !annotations.isEmpty());
	}

	// C++ only
	public String getAnnotationInfoCode() {
		List<Annotation> annotations = data.getModelSpecificSettings().getAnnotations();
		var annotationInfo = new AnnotationInfo(getAnnotationInfoVariableName(), annotations);
		return annotationInfo.toCode();
	}

	// C++ only
	public String getAnnotationInfoVariableName() {
		return AnnotationInfo.VARIABLENAME_PREFIX + "da";
	}

	public String getXmlWriterCode() {
		return metaXmlWriter.toCode();
	}

	public String getUXmlWriterCode() {
		return metaUXmlWriter.toCode();
	}

	public String getXmlReaderCode() {
		return metaXmlReader.toCode();
	}

	public boolean isWithMappingInfos() {
		return ExtmpUtils.isPacketCodeGenerationWithAssignedUXMLModel(data.getParams());
	}

	private List<Integer> getMetaElemNumbers(int numElems, int elemsPerUnit) {
		int num = SplitFunctionsHelper.computeNumSplits(numElems, elemsPerUnit);
		return IntStream.rangeClosed(1, num).boxed().collect(Collectors.toList());
	}

	/**
	 * Die Felder-Metainformationen sind auf mehrere Dateien aufgeteilt und daher benötigen
	 * die Templates eine Liste der Dateinummern für Includes und Variablennamen
	 *
	 * @return Liste aller Dateinummer der Felder-Metainformationen
	 */
	public List<Integer> getMetaFelderNumbers() {
		return getMetaElemNumbers(numFields, CodeGenEntityConstants.NUM_METAFIELDS_PER_FILE_CPP);
	}

	/**
	 * Die Regel-Metainformationen sind auf mehrere Dateien aufgeteilt und daher benötigen
	 * die Templates eine Liste der Dateinummern für Includes und Variablennamen
	 *
	 * @return Liste aller Dateinummer der Regel-Metainformationen
	 */
	public List<Integer> getMetaRegelnNumbers() {
		return getMetaElemNumbers(numRules,
				CodeGenRuleConstants.NUM_METARULES_PER_FILE.get(data.getParams().getProgrammingLanguage()));
	}

	public boolean isEXMLReader() {
		return metaXmlReader.getXMLType() == XML_RW_Type.EXML || metaXmlReader.getXMLType() == XML_RW_Type.EXML_MAPPED;
	}

	public boolean isSXMLReader() {
		return metaXmlReader.getXMLType() == XML_RW_Type.SXML;
	}

	public boolean isSXMLWriter() {
		return metaXmlWriter.getXMLType() == XML_RW_Type.SXML;
	}

	public boolean isEXMLWriter() {
		return metaXmlWriter.getXMLType() == XML_RW_Type.EXML;
	}

	public boolean isWithEnumTables() {
		return data.getEc().getEnumerationTables() != null
				&& !data.getEc().getEnumerationTables().isEmpty();
	}

	public boolean getFullMetaCode() {
		return CodeGenUtilities.isFullMetaCode(data);
	}

	public String getDefaultPresentationInfo() {
		return defaultPresentationInformation.toCode();
	}

	/**
	 * Gibt den UBound der Containerliste für die Metadaten in VBA zurück.
	 */
	public String getUBoundMetaContainer() {
		return String.valueOf(Math.max(0, metaContainerInfoList.size() - 1));
	}

	public String getGencodeJavaGroovyRuntimeVersion() {
		return GeneratorConstants.GENCODE_JAVA_GROOVY_RUNTIME_VERSION;
	}

	public String getGencodeTypescriptRuntimeVersion() {
		return GeneratorConstants.GENCODE_TYPESCRIPT_RUNTIME_VERSION;
	}
}
