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
package com.mgmtp.a12.kernel.md.model.internal.betterbuilders;

import java.util.function.UnaryOperator;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelObjectReferenceBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.IEntity;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;

public class RuleBuilder {

	private final DocumentModel documentModel;
	private final Rule.RuleBuilder ruleBuilder;
	private Group parent;
	private Integer targetPosition;
	private String fullName;

	private RuleBuilder(DocumentModel documentModel) {
		this.documentModel = documentModel;
		this.ruleBuilder = Rule.builder()
				.errorMessage(BuilderUtil.getTextMap(documentModel, "errorText"))
				.errorCode("errorCode")
				.severity(IRule.Severity.ERROR);
	}

	public static RuleBuilder with(DocumentModel documentModel) {
		return new RuleBuilder(documentModel);
	}

	public RuleBuilder id(String id) {
		ruleBuilder.id(id);
		return this;
	}

	public RuleBuilder name(String fullName) {
		String pathWithRepetitions = BuilderUtil.pathPartOf(fullName);
		parent = GroupBuilder.with(documentModel).ensureGroup(pathWithRepetitions);
		ruleBuilder.id(fullName)
				.name(BuilderUtil.lastPartOf(fullName));
		this.fullName = fullName;
		return this;
	}

	public RuleBuilder field(Field field) {
		String fieldFullName = ElementUtils.getPath(field);
		String fieldRelPath = ElementPathUtils.relPath(fieldFullName, fullName);
		ruleBuilder
				.errorEntity(new DocumentModelObjectReferenceBuilder<IEntity>().relativePath(fieldRelPath).dmo(field).build())
				.errorCondition(String.format("FieldFilled(%s)", field.getName()));
		return this;
	}

	public RuleBuilder errorFieldToRelPathReference(String fieldFullName) {
		String fieldRelPath = ElementPathUtils.relPath(fieldFullName, fullName);
		String fieldRelPathForCondition = ElementPathUtils.relPath(fieldFullName, ElementPathUtils.getParentPath(fullName));
		ruleBuilder
				.errorEntity(new DocumentModelObjectReferenceBuilder<IEntity>().relativePath(fieldRelPath).build())
				.errorCondition(String.format("FieldFilled(%s)", fieldRelPathForCondition));
		return this;
	}

	public RuleBuilder modify(UnaryOperator<Rule.RuleBuilder> modifier) {
		modifier.apply(ruleBuilder);
		return this;
	}

	public RuleBuilder moveToTop() {
		return moveToPosition(0);
	}

	public RuleBuilder moveToPosition(Integer position) {
		targetPosition = position;
		return this;
	}

	public Rule build() {
		Rule rule = ruleBuilder.build();
		if (targetPosition == null) {
			parent.addElement(rule);
		} else {
			parent.addElement(targetPosition, rule);
		}
		return rule;
	}
}
