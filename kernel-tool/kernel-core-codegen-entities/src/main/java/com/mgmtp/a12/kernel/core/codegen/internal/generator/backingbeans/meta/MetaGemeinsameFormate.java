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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenFieldDefinition;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenEnumerationTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenFeldTypBasis;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenPredefinedTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenStringTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util.MetaSprachUnterschiedeHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.CodeProfileType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;

/**
 * Determines which formats are commonly used between fields and generates code for these formats.
 */
@StringTemplateGroup("meta_entity")
public class MetaGemeinsameFormate extends STParameterClassBase {

	private static final int ENUM_VALUES_SIZE_PER_CALL = 20000;

	private final List<CodeGenFieldDefinition> felder;
	private final Map<CodeGenFeldTypBasis, MetaGemeinsamesFormat> formateMap = new LinkedHashMap<>();

	private final List<MetaFeldFehlertexte> feldFehlertexte = new ArrayList<>();
	private final List<MetaEnumDisplayValues> enumerationDisplayValues = new ArrayList<>();
	private final List<MetaEnumCategoryValues> enumerationCategoryValues = new ArrayList<>();
	private final List<MetaStringHintLists> stringHintLists = new ArrayList<>();

	private final List<IMetaInfoCodeGenParams> metaCodeGenParams;

	private int aktuelleDateiNummer;
	private final ProgrammingLanguage lang;
	private final String name;
	private final CodeProfileType codeProfileType;

	private List<MetaFeld> metaFelder;

	public MetaGemeinsameFormate(final List<CodeGenFieldDefinition> felder, final CodeGenEntityData data) {
		this.felder = felder;
		this.metaCodeGenParams = new ArrayList<>(data.getParams().getMetaInfoCodeGenParams());
		this.lang = data.getParams().getProgrammingLanguage();
		this.name = data.getParams().getName();
		this.codeProfileType = data.getParams().getCodeProfile();
		AtomicInteger numberOfFormat = new AtomicInteger();

		for (int i = 0; i < felder.size(); i++) {
			final CodeGenFieldDefinition feld = felder.get(i);
			final CodeGenFeldTypBasis fd = feld.getFeldtyp();
			formateMap.computeIfAbsent(fd, k -> new MetaGemeinsamesFormat(numberOfFormat.getAndIncrement(), k));
		}

		initStringsAndEnums();
	}

	private void initStringsAndEnums() {
		for (final Entry<CodeGenFeldTypBasis, MetaGemeinsamesFormat> entry : formateMap.entrySet()) {
			final CodeGenFeldTypBasis typ = entry.getKey();
			final MetaGemeinsamesFormat format = entry.getValue();
			if (typ instanceof CodeGenStringTypeImpl) {
				initStringType((CodeGenStringTypeImpl) typ, format);
			}
			if (typ instanceof CodeGenEnumerationTypeImpl) {
				initEnumType((CodeGenEnumerationTypeImpl) typ, format);
			}
			if (typ instanceof CodeGenPredefinedTypeImpl) {
				initPredefinedType((CodeGenPredefinedTypeImpl) typ, format);
			}
		}
	}

	private void initStringType(CodeGenStringTypeImpl stringType, MetaGemeinsamesFormat format) {
		if (!stringType.getValues().isEmpty()) {
			stringHintLists.add(new MetaStringHintLists(format.getNumber(), stringType.getValues()));
		}
		if (stringType.hasFehlertext()) {
			feldFehlertexte.add(new MetaFeldFehlertexte(format.getNumber(), stringType.getFehlertexte()));
		}
	}

	private void initEnumType(CodeGenEnumerationTypeImpl enumType, MetaGemeinsamesFormat format) {
		feldFehlertexte.add(new MetaFeldFehlertexte(format.getNumber(), enumType.getFehlertexte()));
		if (enumType.hasOberflaechenWerte()) {
			enumerationDisplayValues.add(new MetaEnumDisplayValues(format.getNumber(), enumType.getDisplayValueMap()));
		}
		if (enumType.hasCategoryValues()) {
			enumerationCategoryValues.add(new MetaEnumCategoryValues(format.getNumber(), enumType.getEnumType()));
		}
	}

	private void initPredefinedType(CodeGenPredefinedTypeImpl preDefType, MetaGemeinsamesFormat format) {
		if (preDefType.hasFehlertext()) {
			feldFehlertexte.add(new MetaFeldFehlertexte(format.getNumber(), preDefType.getFehlertexte()));
		}
	}

	public String getFormateDefinitions() {
		return getFormateDefinitions_intern("");
	}

	public String getFormateDefinitions_H() {
		return getFormateDefinitions_intern("h");
	}

	private String getFormateDefinitions_intern(final String suffix) {
		final StringBuilder buf = new StringBuilder();
		for (final MetaGemeinsamesFormat mgf : formateMap.values()) {
			buf.append(mgf.toCode(suffix));
		}
		return buf.toString();
	}

	public String getFelderCppCode() {
		return getFelderCppCodeInternal("");
	}

	public String getFelderContentCppCode() {
		return getFelderCppCodeInternal("content");
	}

	private String getFelderCppCodeInternal(final String suffix) {
		final StringBuilder builder = new StringBuilder();
		// erzeuge MetaFeldInformationen von 'N bis N + Anzahl_MetaFeldInformationen_ProDatei'
		for (int i = (aktuelleDateiNummer - 1) * CodeGenEntityConstants.NUM_METAFIELDS_PER_FILE_CPP; i < felder.size() && i < (aktuelleDateiNummer) * CodeGenEntityConstants.NUM_METAFIELDS_PER_FILE_CPP; i++) {

			final CodeGenFieldDefinition feld = felder.get(i);
			final MetaGemeinsamesFormat mgf = formateMap.get(feld.getFeldtyp());
			final MetaFeld mfa = new MetaFeld(feld.getInternalNumber(), feld, mgf, metaCodeGenParams, codeProfileType);
			builder.append(mfa.toCode(suffix));
		}
		return builder.toString();
	}

	/**
	 * Aufgrund einer Java-Einschränkung kann in Java, in Gegensatz zu C++, die Initialisierung
	 * nicht über lange Initialisierungslisten erfolgen. In Java existiert eine Begrenzung
	 * (65000Bytes) der statitischen Initialisierer, sowie der einzelnen Methoden (inklusive des
	 * Konstruktors). Aus diesem Grund werden die Felder in Java in mehreren privaten Methoden
	 * initialisiert.
	 *
	 * @return String, der die Methoden für die Regelerstellung repräsentiert
	 */
	public String getFelderSplitFunctionCode() {
		final List<MetaFeld> metaFeldListe = createMetaFeldListe();
		final String methodSigTemplate = lang == ProgrammingLanguage.GROOVY ?
				MetaSprachUnterschiedeHelper.GROOVY_INIT_FELDER_CLOSURE_NAME :
				MetaSprachUnterschiedeHelper.JAVA_INIT_FELD_METHOD_NAME;

		return SplitFunctionsHelper.deriveSplitFunctionCode(metaFeldListe,
				methodSigTemplate,
				lang == ProgrammingLanguage.GROOVY ? CodeGenEntityConstants.NUM_FIELDS_PER_INITFELDER_METHOD_GROOVY :
						CodeGenEntityConstants.NUM_FIELDS_PER_INITFELDER_METHOD,
				lang);
	}

	private List<MetaFeld> createMetaFeldListe() {
		if (metaFelder == null) {
			metaFelder = new ArrayList<>();
			for (int i = 0; i < felder.size(); i++) {
				final CodeGenFieldDefinition feld = felder.get(i);
				final MetaGemeinsamesFormat mgf = formateMap.get(feld.getFeldtyp());
				final MetaFeld mfa = new MetaFeld(feld.getInternalNumber(), feld, mgf, metaCodeGenParams, codeProfileType);
				metaFelder.add(mfa);
			}
			return metaFelder;
		}
		return metaFelder;
	}

	public String getFelderSplitFunctionVBACode() {
		final List<MetaFeld> metaFeldListe = createMetaFeldListe();
		return SplitFunctionsHelper.deriveSplitFunctionCode(metaFeldListe,
				MetaSprachUnterschiedeHelper.VBA_INIT_FELD_METHOD_NAME,
				SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				lang);
	}

	public String getFelderJsVkCode() {
		final List<MetaFeld> metaFeldListe = createMetaFeldListe();
		if (!metaFeldListe.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format(MetaSprachUnterschiedeHelper.JSVK_METHOD_DEF_INIT_FELDER_TEMPLATE, name));
			for (final MetaFeld mf : metaFeldListe) {
				sb.append(mf.toCode());
				sb.append("\n");
			}
			sb.append("};");
			return sb.toString();
		} else {
			return "";
		}
	}

	public String getFelderJsVkCodeAufruf() {
		if (felder.isEmpty()) {
			return "";
		} else {
			return MetaSprachUnterschiedeHelper.JSVK_METHOD_NAME_INIT_FELDER_TEMPLATE;
		}
	}

	public String getFelderSplitFunctionCodeAufrufe() {
		final String methodSigTemplate = lang == ProgrammingLanguage.GROOVY ?
				MetaSprachUnterschiedeHelper.CLOSURE_NAME_INIT_FELDER_TEMPLATE + "()" :
				MetaSprachUnterschiedeHelper.METHOD_NAME_INIT_FELDER_TEMPLATE;
		final String aufruf = MetaSprachUnterschiedeHelper.formatMethodCall(methodSigTemplate, lang);
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(felder.size(),
				aufruf,
				lang == ProgrammingLanguage.GROOVY ? CodeGenEntityConstants.NUM_FIELDS_PER_INITFELDER_METHOD_GROOVY :
						CodeGenEntityConstants.NUM_FIELDS_PER_INITFELDER_METHOD,
				lang);
	}

	public String getFeldtypenSplitFunctionCode() {
		return SplitFunctionsHelper.deriveSplitFunctionCode(formateMap.values(),
				MetaSprachUnterschiedeHelper.JAVA_INIT_FELDTYP_METHOD_NAME,
				SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				lang);
	}

	public String getFeldtypenSplitFunctionVBACode() {
		return SplitFunctionsHelper.deriveSplitFunctionCode(formateMap.values(),
				MetaSprachUnterschiedeHelper.VBA_INIT_FELDTYP_METHOD_NAME,
				SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				lang);
	}

	public String getFeldtypenJsVkCode() {
		if (!formateMap.values().isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format(MetaSprachUnterschiedeHelper.JSVK_METHOD_DEF_INIT_FELDTYPEN_TEMPLATE, name));
			for (final MetaGemeinsamesFormat mgf : formateMap.values()) {
				sb.append(mgf.toCode());
				sb.append("\n");
			}
			sb.append("};");
			return sb.toString();
		} else {
			return "";
		}
	}

	public String getFeldtypenJsVkCodeAufruf() {
		if (formateMap.keySet().isEmpty()) {
			return "";
		} else {
			return MetaSprachUnterschiedeHelper.JSVK_METHOD_NAME_INIT_FELDTYPEN_TEMPLATE;
		}
	}

	public String getFeldtypenSplitFunctionCodeAufrufe() {
		final String aufruf = MetaSprachUnterschiedeHelper.formatMethodCall(
				MetaSprachUnterschiedeHelper.METHOD_NAME_INIT_FELDTYPEN_TEMPLATE, lang);
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(formateMap.keySet().size(),
				aufruf, SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT, lang);
	}

	public String getFeldFehlertexteSplitFunctionCode() {
		final String methodSigTemplate = lang == ProgrammingLanguage.VBA ?
				MetaSprachUnterschiedeHelper.VBA_INIT_FELD_FEHLERTEXTE_METHOD_NAME :
				MetaSprachUnterschiedeHelper.JAVA_INIT_FELD_FEHLERTEXTE_METHOD_NAME;
		return SplitFunctionsHelper.deriveSplitFunctionCode(feldFehlertexte,
				methodSigTemplate, SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT, lang);
	}

	public String getFeldFehlertexteJsVkCode() {
		if (!feldFehlertexte.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format(MetaSprachUnterschiedeHelper.JSVK_METHOD_DEF_INIT_FELD_FEHLERTEXTE_TEMPLATE, name));
			for (final MetaFeldFehlertexte mff : feldFehlertexte) {
				sb.append(mff.toCode());
				sb.append("\n");
			}
			sb.append("};");
			return sb.toString();
		} else {
			return "";
		}
	}

	public String getFeldFehlertexteJsVkCodeAufruf() {
		if (feldFehlertexte.isEmpty()) {
			return "";
		} else {
			return MetaSprachUnterschiedeHelper.JSVK_METHOD_NAME_INIT_FELD_FEHLERTEXTE_TEMPLATE;
		}
	}

	public String getFeldFehlertexteSplitFunctionCodeAufrufe() {
		final String aufruf = MetaSprachUnterschiedeHelper.formatMethodCall(
				MetaSprachUnterschiedeHelper.METHOD_NAME_INIT_FELD_FEHLERTEXTE_TEMPLATE, lang);
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(feldFehlertexte.size(),
				aufruf, SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT, lang);
	}

	public String getStringHintListsSplitFunctionCode() {
		return SplitFunctionsHelper.deriveSplitFunctionCodeBasedOnSize(stringHintLists,
				MetaSprachUnterschiedeHelper.JAVA_INIT_STRING_HINT_LISTS_METHOD_NAME,
				ENUM_VALUES_SIZE_PER_CALL,
				lang,
				MetaStringHintLists::getSizeOfValues);
	}

	public String getStringHintListsJsVkCode() {
		if (!stringHintLists.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format(MetaSprachUnterschiedeHelper.JSVK_METHOD_DEF_INIT_STRING_HINT_LISTS_TEMPLATE, name));
			for (final MetaStringHintLists me : stringHintLists) {
				sb.append(me.toCode());
				sb.append("\n");
			}
			sb.append("};");
			return sb.toString();
		}
		return "";
	}

	public String getStringHintListsJsVkCodeAufruf() {
		if (stringHintLists.isEmpty()) {
			return "";
		}
		return MetaSprachUnterschiedeHelper.JSVK_METHOD_NAME_INIT_STRING_HINT_LISTS_TEMPLATE;
	}

	public String getStringHintListsSplitFunctionCodeAufrufe() {
		String methodTemplate = MetaSprachUnterschiedeHelper.formatMethodCall(
				MetaSprachUnterschiedeHelper.METHOD_NAME_INIT_STRING_HINT_LISTS_TEMPLATE, lang);
		int numSplits = SplitFunctionsHelper.computeNumSplitsBasedOnSize(stringHintLists, ENUM_VALUES_SIZE_PER_CALL,
				MetaStringHintLists::getSizeOfValues);
		return SplitFunctionsHelper.deriveSplitFunctionCallsCode(numSplits,
				methodTemplate,
				lang,
				null);
	}

	public String getEnumerationSplitFunctionCode() {
		return SplitFunctionsHelper.deriveSplitFunctionCodeBasedOnSize(enumerationDisplayValues,
				MetaSprachUnterschiedeHelper.JAVA_INIT_ENUM_WERTE_METHOD_NAME,
				ENUM_VALUES_SIZE_PER_CALL,
				lang,
				MetaEnumDisplayValues::getSizeOfValues);
	}

	public String getEnumerationSplitFunctionCodeAufrufe() {
		String methodTemplate = MetaSprachUnterschiedeHelper.formatMethodCall(
				MetaSprachUnterschiedeHelper.METHOD_NAME_INIT_ENUM_WERTE_TEMPLATE, lang);
		int numSplits = SplitFunctionsHelper.computeNumSplitsBasedOnSize(enumerationDisplayValues, ENUM_VALUES_SIZE_PER_CALL,
				MetaEnumDisplayValues::getSizeOfValues);
		return SplitFunctionsHelper.deriveSplitFunctionCallsCode(numSplits,
				methodTemplate,
				lang,
				null);
	}

	public String getEnumerationJsVkCode() {
		if (!enumerationDisplayValues.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format(MetaSprachUnterschiedeHelper.JSVK_METHOD_DEF_INIT_ENUM_WERTE_TEMPLATE, name));
			for (final MetaEnumDisplayValues me : enumerationDisplayValues) {
				sb.append(me.toCode());
				sb.append("\n");
			}
			sb.append("};");
			return sb.toString();
		}
		return "";
	}

	public String getEnumerationCategoryValuesSplitFunctionCode() {
		return SplitFunctionsHelper.deriveSplitFunctionCodeBasedOnSize(enumerationCategoryValues,
				MetaSprachUnterschiedeHelper.JAVA_INIT_ENUM_CATEGORY_VALUES_METHOD_NAME,
				ENUM_VALUES_SIZE_PER_CALL,
				lang,
				MetaEnumCategoryValues::getSizeOfValues);
	}

	public String getEnumerationCategoryValuesSplitFunctionCodeAufrufe() {
		String methodTemplate = MetaSprachUnterschiedeHelper.formatMethodCall(
				MetaSprachUnterschiedeHelper.METHOD_NAME_INIT_ENUM_CATEGORY_VALUES_TEMPLATE, lang);
		int numSplits = SplitFunctionsHelper.computeNumSplitsBasedOnSize(enumerationCategoryValues, ENUM_VALUES_SIZE_PER_CALL,
				MetaEnumCategoryValues::getSizeOfValues);
		return SplitFunctionsHelper.deriveSplitFunctionCallsCode(numSplits,
				methodTemplate,
				lang,
				null);
	}

	public String getEnumerationCategoryValuesJsVkCode() {
		if (!enumerationCategoryValues.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format(MetaSprachUnterschiedeHelper.JSVK_METHOD_DEF_INIT_ENUM_CATEGORY_VALUES_TEMPLATE, name));
			for (final MetaEnumCategoryValues me : enumerationCategoryValues) {
				sb.append(me.toCode());
				sb.append("\n");
			}
			sb.append("};");
			return sb.toString();
		}
		return "";
	}

	public String getEnumerationJsVkCodeAufruf() {
		if (enumerationDisplayValues.isEmpty()) {
			return "";
		}
		return MetaSprachUnterschiedeHelper.JSVK_METHOD_NAME_INIT_ENUM_WERTE_TEMPLATE;
	}

	public String getEnumerationCategoryValuesJsVkCodeAufruf() {
		if (enumerationCategoryValues.isEmpty()) {
			return "";
		}
		return MetaSprachUnterschiedeHelper.JSVK_METHOD_NAME_INIT_ENUM_CATEGORY_VALUES_TEMPLATE;
	}

	public String getVordruckPflichtfelder() {
		final List<MetaFeld> metaFeldListe = createMetaFeldListe();

		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < metaFeldListe.size(); i++) {
			final MetaFeld mfa = metaFeldListe.get(i);
			builder.append(mfa.toCode());
		}
		return builder.toString();
	}

	public void setAktuelleDateiNummer(final int metaFelderDateiNummer) {
		this.aktuelleDateiNummer = metaFelderDateiNummer;
	}

	public String getDateiNummerCode() {
		return Integer.toString(aktuelleDateiNummer);
	}

	public int getAnzahlFelder() {
		return felder.size();
	}

	public int getAnzFormate() {
		return formateMap.size();
	}

}
