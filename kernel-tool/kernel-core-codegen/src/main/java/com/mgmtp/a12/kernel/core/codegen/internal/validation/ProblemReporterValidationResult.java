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

package com.mgmtp.a12.kernel.core.codegen.internal.validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.metaModel.IdentifierImpl;

public class ProblemReporterValidationResult extends AbstractProblemReporterValidationResult {

	private final IData data;
	private final IEC ec;
	private final Map<String, ? extends IRule> rules;

	public ProblemReporterValidationResult(IProblemReporter pr, IData data, IEC ec, Map<String, ? extends IRule> rules) {
		super(pr);
		this.data = data;
		this.ec = ec;
		this.rules = Collections.unmodifiableMap(rules);
	}

	@Override
	protected Object retrieveErrorOrigin(IIdentifier errorField) {
		if (errorField.getName().startsWith("/MVK/ModelProperties/")) {
			return null;
		} else if (errorField.getName().startsWith("/MVK/Model/Rootgroups/")) {
			return lookupRootGroup(errorField);
		} else if (errorField.getName().startsWith("/MVK/Model/Groups/")) {
			return lookupGroup(errorField);
		} else if (errorField.getName().startsWith("/MVK/Fields/")) {
			return lookupField(errorField);
		} else if (errorField.getName().startsWith("/MVK/Rules/")) {
			return lookupRule(errorField);
		} else if (errorField.getName().startsWith("/MVK/FieldgroupModule/")) {
			return null;
		} else if (errorField.getName().startsWith("/MVK/PrintData/")) {
			return lookupField(errorField, "/MVK/PrintData/name");
		} else if (errorField.getName().startsWith("/MVK/Kzs/")) {
			return lookupField(errorField, "/MVK/Kzs/name");
		} else if (errorField.getName().startsWith("/MVK/CalcInfo")) {
			return lookupRule(errorField, "/MVK/CalcInfo/rule");
		} else if (errorField.getName().startsWith("/MVK_TOOL/")) {
			return null;
		} else {
			return null;
		}
	}

	private IRule lookupRule(IIdentifier errorField, String metaModelFullNameRule) {
		long[] indices = getIndices4Lookup(errorField, 3);
		IIdentifier nameId = new IdentifierImpl(metaModelFullNameRule, indices);
		String ruleName = data.getValue(nameId);
		return ruleName != null ? rules.get(ruleName) : null;
	}

	private IEntity lookupRootGroup(IIdentifier errorField) {
		long[] indices = getIndices4Lookup(errorField, 4);
		IIdentifier nameId = new IdentifierImpl("/MVK/Model/Rootgroups/name", indices);
		String rootGroupName = data.getValue(nameId);
		return rootGroupName != null ? ec.get("/" + rootGroupName) : null;
	}

	private IEntity lookupGroup(IIdentifier errorField) {
		long[] indices = getIndices4Lookup(errorField, 4);
		IIdentifier nameId = new IdentifierImpl("/MVK/Model/Groups/name", indices);
		String groupFullName = data.getValue(nameId);
		return groupFullName != null ? ec.get(groupFullName) : null;
	}

	private IEntity lookupField(IIdentifier errorField) {
		long[] indices = getIndices4Lookup(errorField, 3);
		IIdentifier nameId = new IdentifierImpl("/MVK/Fields/name", indices);
		IIdentifier groupId = new IdentifierImpl("/MVK/Fields/group", indices);
		String fieldName = getFullName(groupId, nameId);
		return fieldName != null ? ec.get(fieldName) : null;
	}

	private IEntity lookupField(IIdentifier errorField, String fullNameField) {
		long[] indices = getIndices4Lookup(errorField, 3);
		IIdentifier nameId = new IdentifierImpl(fullNameField, indices);
		String fullName = data.getValue(nameId);
		return fullName != null ? ec.get(fullName) : null;
	}

	private IRule lookupRule(IIdentifier errorField) {
		long[] indices = getIndices4Lookup(errorField, 3);
		IIdentifier nameId = new IdentifierImpl("/MVK/Rules/name", indices);
		IIdentifier groupId = new IdentifierImpl("/MVK/Rules/group", indices);
		String ruleName = getFullName(groupId, nameId);
		return ruleName != null ? rules.get(ruleName) : null;
	}

	private String getFullName(IIdentifier groupId, IIdentifier nameId) {
		String shortName = data.getValue(nameId);
		String group = data.getValue(groupId);
		if (shortName == null || group == null) {
			return null;
		}
		return EntityUtils.createPath(group, shortName);
	}

	private long[] getIndices4Lookup(IIdentifier errorField, int toIndex) {
		long[] indices4Lookup = Arrays.copyOfRange(errorField.getIndices(), 0, toIndex);
		// fields cannot be repeatable by themselves (only through a group)
		indices4Lookup[indices4Lookup.length - 1] = 1;
		return indices4Lookup;
	}
}
