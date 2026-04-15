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
package com.mgmtp.a12.kernel.core.tool.a12internal.api.ado;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.model.header.Annotation;

/**
 * This interface is intended to be implemented by clients.
 * Models a rule.
 */
public interface IRule {

	/**
	 * Severity Type of the rule.
	 */
	enum RuleSeverityType {
		ERROR,
		WARNING,
		INFO
	}

	/**
	 * Uses the path notation as described in {@link IEntity#getFullName()}. The full name specifies
	 * the group which the rule is defined in. This group is also referred to as 'rule group'.
	 * The rule group specifies the path prefix for all fields in the rule condition, which are
	 * referenced by a relative path.
	 * My not be empty.
	 * Must be a correct path name.
	 * For future use: see description of {@link IEntity#getFullName()}
	 *
	 * @return the full name of the rule
	 */
	String getFullName();

	/**
	 * The condition text may be invalid with respect to parsing.
	 * The returned string may be empty, but not null.
	 *
	 * @return the condition text of the rule or an empty string, if no condition text has been
	 * 		specified yet.
	 */
	String getErrorConditionText();

	/**
	 * Returns the error text of the rule for the specified language.
	 * The error text may contain error text params, included in $.
	 * The error text params depend on the language settings of the project
	 * refer to {@link IModelSpecificSettings#getLanguageUsedInConditionText()}.
	 * The replacement of error text parameters is done in the classes FehlertextChecker and
	 * FehlertextBuilder.
	 *
	 * @return the error text of the rule
	 */
	String getErrorMessage(Locale loc);

	/**
	 * Returns the error texts of the rule for all languages.
	 * The error texts may contain error text params, included in $.
	 * The error text params depend on the language settings of the project
	 * refer to {@link IModelSpecificSettings#getLanguageUsedInConditionText()}
	 *
	 * @return the error texts of the rule
	 */
	Map<Locale, String> getErrorMessages();

	/**
	 * Returns the error entity of the rule.
	 * May not return null.
	 * The error entity must be directly or indirectly referenced in the error condition.
	 * The repetition layers of the error entity specify the iteration of the rule.
	 *
	 * @return the error entity of the rule
	 */
	IEntity getErrorEntity();

	/**
	 * Returns the error code of the rule.
	 * The returned string may be empty, but not null.
	 * There is no restrictions about the error code, especially error codes don't need to be
	 * unique.
	 *
	 * @return the error code of the rule
	 */
	String getErrorCode();

	/**
	 * Returns the message type of the rule.
	 * The analysis, which fields are mandatory, is based on rules with the
	 * message type <code>error</code> only.
	 *
	 * @return the message type of the rule, may not be null
	 */
	RuleSeverityType getSeverityType();

	/**
	 * @return map from the name of an annotation to its value. The value of the annotation is an
	 * 		empty string, if no value was specified.
	 * 		May not be {@code null}.
	 */
	default List<Annotation> getAnnotations() {
		return List.of();
	}

	/**
	 * @return the <em>relative paths</em> (e.g. "G11/f1", "../G2/f2", "../../RG2/G2", "f4") of the modeler-defined
	 * 		"angabe-fehlerentities",
	 * 		or the empty list if the default automatic calculation of the possiblyCausingEntities should be used for this rule.
	 */
	default List<String> getCustomRefErrorEntitiesIfFilled() {
		// the customRefErroEntities are currently not supported in A12
		// in order to prevent an (a12internal) breaking change that forces
		// other A12 components to add meaningless emtpy impls, we
		// provide this default impl here:
		return List.of();
	}

	;

	/**
	 * @return the <em>relative paths</em> (e.g. "G11/f1", "../G2/f2", "../../RG2/G2", "f4") of the modeler-defined
	 * 		"nichtangabe-fehlerfelder",
	 * 		* or the empty list if the default automatic calculation of the possiblyCausingEntities should be used for this rule.
	 */
	default List<String> getCustomRefErrorEntitiesIfNotFilled() {
		// the customRefErroEntities are currently not supported in A12
		// in order to prevent an (a12internal) breaking change that forces
		// other A12 components to add meaningless emtpy impls, we
		// provide this default impl here:
		return List.of();
	}
}
