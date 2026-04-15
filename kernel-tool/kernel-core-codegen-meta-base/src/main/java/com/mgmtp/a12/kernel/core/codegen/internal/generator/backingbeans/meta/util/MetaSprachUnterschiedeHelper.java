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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * TODO[Template]: In Template auslagern
 */
public class MetaSprachUnterschiedeHelper {

	public static final String METHOD_NAME_INIT_FELDER_TEMPLATE = "initFelder_%s()";
	public static final String METHOD_NAME_INIT_FELDTYPEN_TEMPLATE = "initFeldtypen_%s()";
	public static final String METHOD_NAME_INIT_REGEL_TEMPLATE = "initRegeln_%s()";
	public static final String METHOD_NAME_INIT_REGEL_REFS_TEMPLATE = "initRegelRefs_%s()";
	public static final String METHOD_NAME_INIT_REGEL_FEHLERTEXTE_TEMPLATE = "initRegelFehlertexte_%s()";
	public static final String METHOD_NAME_INIT_FELD_FEHLERTEXTE_TEMPLATE = "initFeldFehlertexte_%s()";
	public static final String METHOD_NAME_INIT_FIELD_ERROR_TEXTS_4_REQ_TEMPLATE = "initErrorTexts4Req_%s()";
	public static final String METHOD_NAME_INIT_ENUM_WERTE_TEMPLATE = "initEnumerationOberflaechenWerte_%s()";
	public static final String METHOD_NAME_INIT_ENUM_CATEGORY_VALUES_TEMPLATE = "initEnumerationCategoryValues_%s()";
	public static final String METHOD_NAME_INIT_FELD_DRUCKTEXTE_TEMPLATE = "initFeldDrucktexte_%s()";
	public static final String METHOD_NAME_INIT_FIELD_MAP_INFOS_TEMPLATE = "initFieldMapInfos_%s(fieldMapInfosTemp)";
	public static final String METHOD_NAME_INIT_STRING_HINT_LISTS_TEMPLATE = "initStringHintLists_%s()";
	public static final String CLOSURE_NAME_INIT_FELDER_TEMPLATE = "initFelder_%s";
	public static final String CLOSURE_NAME_IDENTIFIERREF_TEMPLATE = "refIdentifier_%s_%%s";
	public static final String CLOSURE_NAME_AUSLASSUNGSIDENTIFIERREF_TEMPLATE = "refAuslassungsIdentifier_%s_%%s";
	public static final String CLOSURE_NAME_ENTITYREF_TEMPLATE = "refEntities_%s_%%s";
	public static final String CLOSURE_NAME_AUSLASSUNGSENTITYREF_TEMPLATE = "refAuslassungsEntity_%s_%%s";
	public static final String CLOSURE_NAME_CALCULATIONCODE_TEMPLATE = "calculationCode_%s";
	public static final String CLOSURE_NAME_CONDITIONLINECODE_TEMPLATE = "conditionLineCode_%s";

	public static final String JSVK_METHOD_NAME_INIT_FELDER_TEMPLATE = "this._initFelder();";
	public static final String JSVK_METHOD_NAME_INIT_FIELD_ERROR_TEXTS_4_REQ_TEMPLATE = "this._initErrorTexts4Req();";
	public static final String JSVK_METHOD_NAME_INIT_FELDTYPEN_TEMPLATE = "this._initFeldtypen();";
	public static final String JSVK_METHOD_NAME_INIT_REGEL_TEMPLATE = "this._initRegeln();";
	public static final String JSVK_METHOD_NAME_INIT_REGEL_REFS_TEMPLATE = "this._initRegelRefs();";
	public static final String JSVK_METHOD_NAME_INIT_REGEL_FEHLERTEXTE_TEMPLATE = "this._initRegelFehlertexte();";
	public static final String JSVK_METHOD_NAME_INIT_FELD_FEHLERTEXTE_TEMPLATE = "this._initFeldFehlertexte();";
	public static final String JSVK_METHOD_NAME_INIT_ENUM_WERTE_TEMPLATE = "this._initEnumerationOberflaechenWerte();";
	public static final String JSVK_METHOD_NAME_INIT_ENUM_CATEGORY_VALUES_TEMPLATE = "this._initEnumerationCategoryValues();";
	public static final String JSVK_METHOD_NAME_INIT_STRING_HINT_LISTS_TEMPLATE = "this._initStringHintLists();";

	public static final String JSVK_METHOD_DEF_INIT_FELDER_TEMPLATE = "Meta_Felder_%s.prototype._initFelder = function () {\n";
	public static final String JSVK_METHOD_DEF_INIT_FIELD_ERROR_TEXTS_4_REQ_TEMPLATE = "Meta_Felder_%s.prototype._initErrorTexts4Req = function () {\n";
	public static final String JSVK_METHOD_DEF_INIT_FELDTYPEN_TEMPLATE = "Meta_Felder_%s.prototype._initFeldtypen = function () {\n";
	public static final String JSVK_METHOD_DEF_INIT_REGEL_TEMPLATE = "Meta_Regel_%s.prototype._initRegeln = function () {\n";
	public static final String JSVK_METHOD_DEF_INIT_REGEL_REFS_TEMPLATE = "Meta_Regel_%s.prototype._initRegelRefs = function () {\n";
	public static final String JSVK_METHOD_DEF_INIT_REGEL_FEHLERTEXTE_TEMPLATE = "Meta_Regel_%s.prototype._initRegelFehlertexte = function () {\n";
	public static final String JSVK_METHOD_DEF_INIT_FELD_FEHLERTEXTE_TEMPLATE = "Meta_Felder_%s.prototype._initFeldFehlertexte = function () {\n";
	public static final String JSVK_METHOD_DEF_INIT_ENUM_WERTE_TEMPLATE = "Meta_Felder_%s.prototype._initEnumerationOberflaechenWerte = function () {\n";
	public static final String JSVK_METHOD_DEF_INIT_ENUM_CATEGORY_VALUES_TEMPLATE = "Meta_Felder_%s.prototype._initEnumerationCategoryValues = function () {\n";
	public static final String JSVK_METHOD_DEF_INIT_STRING_HINT_LISTS_TEMPLATE = "Meta_Felder_%s.prototype._initStringHintLists = function () {\n";

	private static final String PRIVATE_VOID = "private void ";
	public static final String JAVA_INIT_REGEL_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_REGEL_TEMPLATE;
	public static final String JAVA_INIT_FELD_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_FELDER_TEMPLATE;
	public static final String JAVA_INIT_FIELD_ERROR_TEXTS_4_REQ_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_FIELD_ERROR_TEXTS_4_REQ_TEMPLATE;
	public static final String JAVA_INIT_FELDTYP_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_FELDTYPEN_TEMPLATE;
	public static final String JAVA_INIT_REGEL_REFS_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_REGEL_REFS_TEMPLATE;
	public static final String JAVA_INIT_REGEL_FEHLERTEXTE_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_REGEL_FEHLERTEXTE_TEMPLATE;
	public static final String JAVA_INIT_FELD_FEHLERTEXTE_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_FELD_FEHLERTEXTE_TEMPLATE;
	public static final String JAVA_INIT_ENUM_WERTE_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_ENUM_WERTE_TEMPLATE;
	public static final String JAVA_INIT_ENUM_CATEGORY_VALUES_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_ENUM_CATEGORY_VALUES_TEMPLATE;
	public static final String JAVA_INIT_FELD_DRUCKTEXTE_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_FELD_DRUCKTEXTE_TEMPLATE;
	public static final String JAVA_INIT_FIELD_MAP_INFOS_METHOD_NAME = "private static void initFieldMapInfos_%s(List<FieldMapInfo> fieldMapInfosTemp)";
	public static final String JAVA_INIT_STRING_HINT_LISTS_METHOD_NAME = PRIVATE_VOID + METHOD_NAME_INIT_STRING_HINT_LISTS_TEMPLATE;

	public static final String INIT_CONTAINER_METHOD_NAME_TEMPLATE = "initContainer_%s";
	public static final String INIT_CONTAINER_METHOD_SIGNATURE_TEMPLATE = "private static void " + INIT_CONTAINER_METHOD_NAME_TEMPLATE + "(Container_t[] result)";
	public static final String INIT_CONTAINER_METHOD_CALL_TEMPLATE = INIT_CONTAINER_METHOD_NAME_TEMPLATE + "(result)";

	public static final String GROOVY_INIT_FELDER_CLOSURE_NAME = "Closure " + CLOSURE_NAME_INIT_FELDER_TEMPLATE + " = ";
	public static final String GROOVY_IDENTIFIERREF_CLOSURE_NAME = "Closure<Identifier_t[]> " + CLOSURE_NAME_IDENTIFIERREF_TEMPLATE + " = ";
	public static final String GROOVY_AUSLASSUNGSIDENTIFIERREF_CLOSURE_NAME = "Closure<Identifier_t[]> " + CLOSURE_NAME_AUSLASSUNGSIDENTIFIERREF_TEMPLATE + " = ";
	public static final String GROOVY_ENTITYREF_CLOSURE_NAME = "Closure<Identifier_t[]> " + CLOSURE_NAME_ENTITYREF_TEMPLATE + " = ";
	public static final String GROOVY_AUSLASSUNGSENTITYREF_CLOSURE_NAME = "Closure<Identifier_t[]> " + CLOSURE_NAME_AUSLASSUNGSENTITYREF_TEMPLATE + " = ";
	public static final String GROOVY_CALCULATIONCODE_CLOSURE_NAME = "Closure<Boolean> " + CLOSURE_NAME_CALCULATIONCODE_TEMPLATE + " = ";
	public static final String GROOVY_CONDITIONLINECODE_CLOSURE_NAME = "Closure " + CLOSURE_NAME_CONDITIONLINECODE_TEMPLATE + " = ";

	private static final String PRIVATE_SUB = "Private Sub ";
	public static final String VBA_INIT_REGEL_METHOD_NAME = PRIVATE_SUB + METHOD_NAME_INIT_REGEL_TEMPLATE + "\n\tDim regel As ER_Regel_t\n";
	public static final String VBA_INIT_FELD_METHOD_NAME = PRIVATE_SUB + METHOD_NAME_INIT_FELDER_TEMPLATE + "\n\tDim feld As ER_Feld_t\n";
	public static final String VBA_INIT_FELDTYP_METHOD_NAME = PRIVATE_SUB + METHOD_NAME_INIT_FELDTYPEN_TEMPLATE + "\n\t";
	public static final String VBA_INIT_REGEL_FEHLERTEXTE_METHOD_NAME = PRIVATE_SUB + METHOD_NAME_INIT_REGEL_FEHLERTEXTE_TEMPLATE + "\n\tDim fehlertexte As Scripting.Dictionary\n";
	public static final String VBA_INIT_FELD_FEHLERTEXTE_METHOD_NAME = PRIVATE_SUB + METHOD_NAME_INIT_FELD_FEHLERTEXTE_TEMPLATE + "\n\tDim fehlertexte As Scripting.Dictionary\n";
	public static final String VBA_INIT_FIELD_ERROR_TEXTS_4_REQ_METHOD_NAME = PRIVATE_SUB + METHOD_NAME_INIT_FIELD_ERROR_TEXTS_4_REQ_TEMPLATE + "\n\tDim fehlertexte As Scripting.Dictionary\n";
	public static final String VBA_INIT_FELD_DRUCKTEXTE_METHOD_NAME = PRIVATE_SUB + METHOD_NAME_INIT_FELD_DRUCKTEXTE_TEMPLATE + "\n\t    Dim druckTexte As Scripting.Dictionary\n";

	private static final String VBA_CALL_PREFIX = "Call ";

	private MetaSprachUnterschiedeHelper() {
		throw new IllegalStateException("Utility class");
	}

	//ref_identifier
	//ref_kat
	public static String getReferenzen(final long regelNummer, final String varName, final ProgrammingLanguage lang) {
		if (ProgrammingLanguage.JAVA_SCRIPT_VK == lang) {
			return "this._" + varName + "[" + regelNummer + "]";
		} else if (ProgrammingLanguage.JAVA != lang && ProgrammingLanguage.GROOVY != lang) {
			return varName + "_" + regelNummer;
		} else {
			return varName + "[" + regelNummer + "]";
		}
	}

	public static String formatMethodCall(final String aufruf, final ProgrammingLanguage lang) {
		if (ProgrammingLanguage.VBA == lang) {
			return VBA_CALL_PREFIX + aufruf;
		} else {
			return aufruf;
		}
	}

}
