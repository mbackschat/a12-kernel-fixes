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
package com.mgmtp.a12.kernel.md.model.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Provides access to {@link IRule} specific properties.
 * A {@link IRule} is a constraint on fields, groups or sets of them.
 */
@OnlyForUsage
public interface IRule extends IElement, Serializable {

	/**
	 * Specifies whether a {@link IRule} describes an error, a warning or an info.
	 */
	enum Severity {
		INFO,
		WARNING,
		ERROR
	}

	/**
	 * The <i>error entity</i> is a highlighted entity (field or group) that must be referenced
	 * directly or indirectly in the error condition.
	 * <ul>
	 * <li>directly, by using the entity in a parameter list
	 * <li>indirectly by using a group that includes the entity.
	 * </ul>
	 * The <i>error entity</i> controls the iteration of the rule. If the <i>error entity</i> lies
	 * in a
	 * repeatable group, the rule is checked for all repetitions of that repeating level.<br>
	 * Besides, the <i>error entity</i>
	 *
	 * @return the <i>error entity</i>. Not {@code null}.
	 */
	@NonNull IDocumentModelObjectReference<IEntity> getErrorEntity();

	/**
	 * @return the error code. Never {@code null} or empty.
	 */
	@NonNull String getErrorCode();

	/**
	 * @return the error condition as a String. Never {@code null} or empty.
	 */
	@NonNull String getErrorCondition();

	/**
	 * @return the {@link Severity}. Never {@code null}.
	 */
	@NonNull Severity getSeverity();

	/**
	 * @return the error message. Never {@code null}.
	 */
	@NonNull ILocalizedTextMap getErrorMessage();

	/**
	 * Currently, there is only one gui pattern:
	 * <ul>
	 * <li>key="dynamicEnumeration", value="true"|"false"</li>
	 * </ul>
	 * The gui pattern is always set in the returned map.
	 * <p>
	 * HINT: the pattern "dynamicEnumeration" currently does not have any consequence.
	 * There is no code generated for dynamic enumerations for document models.
	 *
	 * @return the map with all defined gui patterns. Neither {@code null} nor empty.
	 */
	@NonNull Map<String, String> getGuiPatterns();

	/**
	 * @return the filter that determines whether or not the {@link IRule} should be applied for a
	 * 		certain variant or an empty Optional instance if no variant filter is specified.
	 */
	Optional<IVariantFilter> getVariantFilter();

	/**
	 * @return a map with the defined variant specific error messages. Never {@code null}. If no
	 * 		variant specific error messages were defined, the {@link ILocalizedTextMap} is empty
	 * 		and in this case the error message from {@link #getErrorMessage()} will be used for
	 * 		all variants.
	 */
	@NonNull Map<String, ILocalizedTextMap> getVariantErrorMessages();

}
