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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.utils.UniqueNameService;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;

public class CodeGenEntityData implements IBaseCodeGenData {
	protected final IEC ec;
	protected final IModelSpecificSettings modelSpecificSettings;
	protected final ICodeGeneratorParameters params;
	protected final IProjectSpecificSettings pss;
	private final Map<String, Integer> maxLengthDefinedByRules = new HashMap<>();
	private Map<String, String> uniqueFieldNamesMap;

	public CodeGenEntityData(IEC ec, IModelSpecificSettings modelSpecificSettings, ICodeGeneratorParameters params,
			IProjectSpecificSettings pss) {
		this.ec = ec;
		this.modelSpecificSettings = modelSpecificSettings;
		this.params = params;
		this.pss = pss;
	}

	public IEC getEc() {
		return ec;
	}

	public IModelSpecificSettings getModelSpecificSettings() {
		return modelSpecificSettings;
	}

	@Override public ICodeGeneratorParameters getParams() {
		return params;
	}

	public IProjectSpecificSettings getPss() {
		return pss;
	}

	/**
	 * @return a map from full name of entities to their unique name.
	 */
	public Map<String, String> getUniqueFieldNames() {
		if (this.uniqueFieldNamesMap == null) {
			Set<String> fullFieldNames = EntityUtils.getAllFields(ec).stream().map(IField::getFullName).collect(Collectors.toSet());
			this.uniqueFieldNamesMap = UniqueNameService.makeUniqueFieldNames(fullFieldNames);
		}
		return this.uniqueFieldNamesMap;
	}

	/**
	 * Can be used instead of {@link com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils#getModelVersion}.
	 * Only use this function if a year and not a version is needed.
	 *
	 * @return {@link IModelSpecificSettings#getBaseYear()} or null.
	 */
	public String getBaseYear() {
		return modelSpecificSettings.getBaseYear().map(String::valueOf).orElse(null);
	}

	/**
	 * Add the maxLength attribute for a string field. This value replaces the value for the field
	 * in the Meta API. If the attribute is set for a non-existent field or a field that doesn't
	 * have a string type, the value is ignored.
	 *
	 * @param fieldFullName
	 * 		the full name of the field, the maxLength should be set.
	 * @param newMaxLength
	 * 		the new value for the maxLength attribute.
	 */
	public void addMaxLengthDefinedByRules(String fieldFullName, Integer newMaxLength) {
		this.maxLengthDefinedByRules.put(fieldFullName, newMaxLength);
	}

	/**
	 * @return a map from field name to the newly defined maxLength-attribute.
	 */
	public Map<String, Integer> getMaxLengthDefinedByRules() {
		return this.maxLengthDefinedByRules;
	}
}
