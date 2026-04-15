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

package com.mgmtp.a12.kernel.core.codegen.internal.generator;

import static com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils.GROOVY_CONTENT_KEY;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BasePackageInformation;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.error.CreateEntryException;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.SourceCodeCollectorV2;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporterProxy;

/**
 * The base class for code generation. It divides the abstract task of code generation into steps.
 */
public abstract class BaseCodeGenerator<D extends IBaseCodeGenData, P extends BasePackageInformation> implements ICodeGenerator {
	private static final String UTF_8 = "UTF-8";

	protected final D data;
	protected final ProblemReporterProxy problemReporter;
	protected final SourceCodeCollectorV2 collector;
	protected String relativePath;
	protected P packageInfo;

	public BaseCodeGenerator(D data, IProblemReporter pr, SourceCodeCollectorV2 collector) {
		this.data = data;
		this.problemReporter = new ProblemReporterProxy(pr);
		this.collector = collector;
		this.relativePath = deriveRelativePath();
		this.packageInfo = createPackageInformation();
	}

	protected String deriveRelativePath() {
		return "";
	}

	@Override
	public boolean generateCode() {
		if (!preCheck()) {
			return false;
		}
		if (!createTemplateParameterClasses()) {
			return false;
		}
		try {
			createEntries();
			nonStandardPluginMethod();
		} catch (CreateEntryException e) {
			return false;
		}
		CodeGenContext.unload();

		return true;
	}

	void nonStandardPluginMethod() {
	}

	/**
	 * Performs checks, for which no further transformations in
	 * {@link #createTemplateParameterClasses()} are
	 * necessary.
	 *
	 * @return <code>true</code>, if the checks succeed, <code>false</code> otherwise.
	 */
	protected abstract boolean preCheck();

	/**
	 * Buildup the parameter classes used for the StringTemplates.
	 *
	 * @return <code>true</code>, if the beans could be filled successfully, <code>false</code>
	 * 		otherwise.
	 */
	protected abstract boolean createTemplateParameterClasses();

	/**
	 * Create the individual source code entries by calling the corresponding string template.
	 */
	protected abstract void createEntries();

	protected void createEntry(String entryName, String text) {
		if (ProgrammingLanguage.CPLUS.equals(data.getParams().getProgrammingLanguage())) {
			text = Normalizer.normalize(text,
					Normalizer.Form.NFC); // for c++ we need normalized UTF-8 // NOTE (sb, 2024-10): is this still necessary?
		}
		byte[] encoded;
		try {
			encoded = text.getBytes(getEncoding());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		collector.createEntry(entryName, encoded);
	}

	private String getEncoding() {
		return switch (data.getParams().getProgrammingLanguage()) {
			case JAVA, CPLUS, JAVA_SCRIPT_REDUCED, JAVA_SCRIPT_VK, GROOVY -> UTF_8;
			case VBA -> "CP1252";
		};
	}

	protected P getPackageInfo() {
		return packageInfo;
	}

	protected String getPrefix(String packageName) {
		if (ProgrammingLanguage.CPLUS.equals(data.getParams().getProgrammingLanguage())) {
			return data.getParams().getName() + "/";
		} else {
			return packageName.replace('.', '/') + "/";
		}
	}

	protected void createJavaEntry(String name, String text) {
		createJavaEntry(name, text, getPackageInfo().getVKPackageName());
	}

	protected void createJavaEntry(String name, String text, String packageName) {
		createEntry(getFullNameWithoutExtension(name, packageName) + BaseCodeGenConstants.JAVA_SUFFIX, text);
	}

	protected String getFullNameWithoutExtension(String name) {
		return getFullNameWithoutExtension(name, getPackageInfo().getVKPackageName());
	}

	protected String getFullNameWithoutExtension(String name, String packageName) {
		return getPrefix(packageName) + name + data.getParams().getName();
	}

	protected void createGroovyHelperEntry(String name, String text) {
		createEntry(name, text);
	}

	protected void createGroovyContentEntry(String name, String text) {
		createEntry(GROOVY_CONTENT_KEY + getFullNameWithoutExtension(name,
				data.getParams().getName()) + BaseCodeGenConstants.GROOVY_SUFFIX, text);
	}

	protected void createCppAndHIterativeEntry(String name, String cppText, String hText, int entryIndex) {
		if (cppText != null) {
			createEntry(getFullNameWithoutExtension(name) + "_" + entryIndex + BaseCodeGenConstants.CPP_SUFFIX, cppText);
		}
		if (hText != null) {
			createEntry(getFullNameWithoutExtension(name) + "_" + entryIndex + BaseCodeGenConstants.H_SUFFIX, hText);
		}
	}

	protected void createCppAndHEntry(String name, String cppText, String hText) {
		if (cppText != null) {
			createEntry(getFullNameWithoutExtension(name) + BaseCodeGenConstants.CPP_SUFFIX, cppText);
		}
		if (hText != null) {
			createEntry(getFullNameWithoutExtension(name) + BaseCodeGenConstants.H_SUFFIX, hText);
		}
	}

	protected void createJavaScriptMvkEntry(String name, String text, String extension) {
		createEntry(getFullNameWithoutExtension(name, data.getParams().getName()) + extension, text);
	}

	protected abstract P createPackageInformation();

	/**
	 * The relative path to use for the entries.
	 *
	 * @param relativePath
	 * 		the relative path.
	 */
	public void setRelativePath(String relativePath) {
		while (relativePath.endsWith("/")) {
			relativePath = relativePath.substring(0, relativePath.length() - 1);
		}
		this.relativePath = relativePath;
		this.packageInfo = createPackageInformation();
	}
}
