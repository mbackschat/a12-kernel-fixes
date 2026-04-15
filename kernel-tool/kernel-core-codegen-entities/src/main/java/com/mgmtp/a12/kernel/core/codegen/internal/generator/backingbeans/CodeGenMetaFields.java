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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.BaseCodeGenConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaFeldFehlertexte;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaGemeinsameFormate;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util.MetaSprachUnterschiedeHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.LanguageHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.SplitFunctionsHelper;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

/**
 * Template parameter class for the generated Meta_Felder_&lt;name&gt; classes.
 */
@StringTemplateGroup("meta_entity")
public class CodeGenMetaFields extends CodeGenEREntities<CodeGenEntityData> {

	private final ICodeGeneratorParameters.ProgrammingLanguage lang;
	private List<CodeGenFieldDefinition> codeGenFields = new ArrayList<>();
	private final MetaGemeinsameFormate gemeinsameFormate;
	private final List<MetaFeldFehlertexte> errorTexts4Req;
	private final int numMetaFieldFilesCpp;
	private int currentMetaFieldFileCpp;
	private final List<Locale> languages;

	public CodeGenMetaFields(final CodeGenMetaFieldsBuilder builder) {
		super(builder);
		this.lang = data.getParams().getProgrammingLanguage();
		codeGenFields = builder.getFieldDefinitions();
		errorTexts4Req = builder.getErrorTexts4Req();
		numMetaFieldFilesCpp = builder.getNumFieldFilesCpp();
		gemeinsameFormate = builder.getGemeinsameFormate();
		languages = builder.getLanguages();
	}

	public String getMetaFelderCode() {
		return gemeinsameFormate.toCode();
	}

	public String getMetaFelderContentCode() {
		return gemeinsameFormate.toCode("content");
	}

	public String getMetaFeldtypenSplitFunctionCodeAufrufe() {
		return gemeinsameFormate.getFeldtypenSplitFunctionCodeAufrufe();
	}

	public String getMetaFeldtypenJsVkCodeAufruf() {
		return gemeinsameFormate.getFeldtypenJsVkCodeAufruf();
	}

	public String getMetaFeldFehlertexteSplitFunctionCodeAufrufe() {
		return gemeinsameFormate.getFeldFehlertexteSplitFunctionCodeAufrufe();
	}

	public String getMetaFeldFehlertexteJsVkCodeAufruf() {
		return gemeinsameFormate.getFeldFehlertexteJsVkCodeAufruf();
	}

	public String getMetaStringHintListsSplitFunctionCodeAufrufe() {
		return gemeinsameFormate.getStringHintListsSplitFunctionCodeAufrufe();
	}

	public String getMetaStringHintListsJsVkCodeAufruf() {
		return gemeinsameFormate.getStringHintListsJsVkCodeAufruf();
	}

	public String getMetaEnumerationSplitFunctionCodeAufrufe() {
		return gemeinsameFormate.getEnumerationSplitFunctionCodeAufrufe();
	}

	public String getMetaEnumerationCategoryValuesSplitFunctionCodeAufrufe() {
		return gemeinsameFormate.getEnumerationCategoryValuesSplitFunctionCodeAufrufe();
	}

	public String getMetaEnumerationCategoryValuesJsVkCodeAufruf() {
		return gemeinsameFormate.getEnumerationCategoryValuesJsVkCodeAufruf();
	}

	public String getMetaEnumerationJsVkCodeAufruf() {
		return gemeinsameFormate.getEnumerationJsVkCodeAufruf();
	}

	public String getMetaFelderSplitFunctionCodeAufrufe() {
		return gemeinsameFormate.getFelderSplitFunctionCodeAufrufe();
	}

	public String getMetaFelderJsVkCodeAufruf() {
		return gemeinsameFormate.getFelderJsVkCodeAufruf();
	}

	public String getMetaFeldFormateCode() {
		return gemeinsameFormate.getFormateDefinitions();
	}

	public String getMetaFeldFormateHCode() {
		return gemeinsameFormate.getFormateDefinitions_H();
	}

	public String getErrorText4ReqSplitFunctionCode() {
		final String methodSigTemplate = lang == ICodeGeneratorParameters.ProgrammingLanguage.VBA ?
				MetaSprachUnterschiedeHelper.VBA_INIT_FIELD_ERROR_TEXTS_4_REQ_METHOD_NAME :
				MetaSprachUnterschiedeHelper.JAVA_INIT_FIELD_ERROR_TEXTS_4_REQ_METHOD_NAME;
		return SplitFunctionsHelper.deriveSplitFunctionCode(errorTexts4Req,
				methodSigTemplate,
				SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT,
				lang, "", "", BaseCodeGenConstants.PARAM_CUSTOM_ERROR_MSG_4_REQ);
	}

	public String getErrorTexts4ReqSplitFunctionCodeAufrufe() {
		final String aufruf = MetaSprachUnterschiedeHelper.formatMethodCall(
				MetaSprachUnterschiedeHelper.METHOD_NAME_INIT_FIELD_ERROR_TEXTS_4_REQ_TEMPLATE, lang);
		return SplitFunctionsHelper.deriveSplitFunctionCodeCalls(errorTexts4Req.size(),
				aufruf, SplitFunctionsHelper.ELEMENTS_PER_SPLIT_METHOD_DEFAULT, lang);
	}

	public String getErrorTexts4ReqJsVkCode() {
		if (errorTexts4Req.size() > 0) {
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format(MetaSprachUnterschiedeHelper.JSVK_METHOD_DEF_INIT_FIELD_ERROR_TEXTS_4_REQ_TEMPLATE,
					data.getParams().getName()));
			for (final MetaFeldFehlertexte mff : errorTexts4Req) {
				sb.append(mff.toCode(BaseCodeGenConstants.PARAM_CUSTOM_ERROR_MSG_4_REQ));
				sb.append("\n");
			}
			sb.append("};");
			return sb.toString();
		} else {
			return "";
		}
	}

	public String getErrorTexts4ReqJsVkCodeAufruf() {
		if (errorTexts4Req.isEmpty()) {
			return "";
		} else {
			return MetaSprachUnterschiedeHelper.JSVK_METHOD_NAME_INIT_FIELD_ERROR_TEXTS_4_REQ_TEMPLATE;
		}
	}

	public int getNumMetaFieldFilesCpp() {
		return numMetaFieldFilesCpp;
	}

	public void setMetaFieldFileNumberCpp(final int fileNumber) {
		currentMetaFieldFileCpp = fileNumber;
		gemeinsameFormate.setAktuelleDateiNummer(fileNumber);
	}

	//VBA
	public int getNumberOfMetaFieldsMinusOne() {
		return Math.max(0, codeGenFields.size() - 1);
	}

	public String getDateiNummerCode() {
		return Integer.toString(currentMetaFieldFileCpp);
	}

	public String getFeldDefinitionenJSCode() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < codeGenFields.size(); i++) {
			sb.append(codeGenFields.get(i).getJSCode(data.getParams().getLanguages()));
		}
		return sb.toString();
	}

	public String getLanguagesCode() {
		return languages.stream()
				.map(LanguageHelper::new)
				.map(LanguageHelper::toCode)
				.collect(Collectors.joining(", "));
	}
}
