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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.stringtemplate.v4.STGroup;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenContext;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.Product;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * The base class for all StringTemplateBackingBeans, that are connected to a string template by a
 * {@link StringTemplateGroup} annotation.
 * <p>
 * The methods {@link #toCode()} or {@link #toCode(String)} can be used to render the connected
 * string templates with the values represented by this class.
 * <p>
 * WARNING: This class uses the {@link CodeGenContext}-instance that lies as {@link ThreadLocal} in
 * the thread context of the current thread. That class must be initialized beforehand!
 */
// TODO: Completely eliminate occurrences of the term "backing bean" and change it to "parameter class".
public abstract class STParameterClassBase implements ICodeOutput {

	private static final String TEMPLATE_BASE_DIR = "templates";
	private static final String PRODUCT_REPLACEMENT_STRING = "$product$";
	private static final String PROGRAMMING_LANGUAGE_REPLACEMENT_STRING = "$programmingLanguage$";

	private static final Map<Product, String> productDirs = Map.of(
			Product.VALIDATION, "validation",
			Product.XML_READER, "xml_reader",
			Product.XML_WRITER, "xml_writer");

	private static final Map<ProgrammingLanguage, String> programmingLanguageDirs = Map.of(ProgrammingLanguage.JAVA, "java",
			ProgrammingLanguage.CPLUS, "cplus",
			ProgrammingLanguage.VBA, "vba",
			ProgrammingLanguage.JAVA_SCRIPT_REDUCED, "java_script",
			ProgrammingLanguage.JAVA_SCRIPT_VK, "java_script_vk",
			ProgrammingLanguage.GROOVY, "groovy");

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeOutput#toCode()
	 */
	@Override
	public final String toCode() {
		return toCode(StringUtils.EMPTY);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeOutput#toCode(
	 * java.lang.String)
	 */
	@Override
	public String toCode(String param) {
		String templatePath = getTemplatePath(param);

		return StringTemplateUtil.getInstance().getTemplateCode(this, templatePath, getGroupAdapter());
	}

	private String getProductDirName() {
		Product product = CodeGenContext.getInstance().getProduct();
		if (!productDirs.containsKey(product)) {
			return product.name().toLowerCase();
		} else {
			return productDirs.get(product);
		}
	}

	private String getProgrammingLangDirName() {
		ProgrammingLanguage programmingLanguage = CodeGenContext.getInstance().getProgrammingLanguage();
		if (!programmingLanguageDirs.containsKey(programmingLanguage)) {
			return programmingLanguage.name().toLowerCase();
		} else {
			return programmingLanguageDirs.get(programmingLanguage);
		}
	}

	private String getFullPath(String relativePath, String templateName) {
		String basePath;
		if (!relativePath.startsWith(StringTemplateUtil.TEMPLATE_FILE_SEPARATOR)) {
			basePath = TEMPLATE_BASE_DIR + StringTemplateUtil.TEMPLATE_FILE_SEPARATOR + getProductDirName() + StringTemplateUtil.TEMPLATE_FILE_SEPARATOR + getProgrammingLangDirName() + StringTemplateUtil.TEMPLATE_FILE_SEPARATOR;
		} else {
			basePath = TEMPLATE_BASE_DIR;
		}
		String path = basePath + relativePath;
		// If the relativePath is empty, then a trailing / will already be there
		return path
				+ (path.endsWith(StringTemplateUtil.TEMPLATE_FILE_SEPARATOR) ? "" : StringTemplateUtil.TEMPLATE_FILE_SEPARATOR)
				+ templateName;
	}

	private String replaceParams(String fileName) {
		if (fileName.contains(PROGRAMMING_LANGUAGE_REPLACEMENT_STRING)) {
			fileName = fileName.replace(PROGRAMMING_LANGUAGE_REPLACEMENT_STRING, this.getProgrammingLangDirName());
		}
		if (fileName.contains(PRODUCT_REPLACEMENT_STRING)) {
			fileName = fileName.replace(PRODUCT_REPLACEMENT_STRING, this.getProductDirName());
		}
		return fileName;
	}

	private String getTemplatePath(String param) {
		StringTemplateGroup dir = this.getClass().getAnnotation(StringTemplateGroup.class);

		String templateDir = dir == null ? "" : replaceParams(dir.value());
		if (StringUtils.isEmpty(templateDir)) {
			throw new IllegalArgumentException("Forgot a @StringTemplateGroup annotation for class " + this.getClass() + "?");
		}

		String templateName;
		if (StringUtils.isEmpty(param)) {
			templateName = this.getClass().getSimpleName();
		} else {
			templateName = this.getClass().getSimpleName() + "_" + param;
		}
		return getFullPath(templateDir, templateName);
	}

	protected Consumer<STGroup> getGroupAdapter() {
		return it -> {
		};
	}
}
