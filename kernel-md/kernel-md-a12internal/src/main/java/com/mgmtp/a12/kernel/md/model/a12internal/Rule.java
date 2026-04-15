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

package com.mgmtp.a12.kernel.md.model.a12internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.IRule.Severity;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;
import com.mgmtp.a12.model.header.Annotation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Definition of a constraint for a certain {@link Field} or a set of {@link Field}s.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@OnlyForA12InternalUsage
public class Rule extends Element {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return the referenced {@link IEntity} which controls the iteration of this rule. Is never
	 *        {@code null}.
	 * @param errorEntity
	 * 		the new referenced {@link IEntity} of this rule (for more information see
	 *        {@link #getErrorEntity()}). May not be {@code null}.
	 */
	private @NonNull IDocumentModelObjectReference<IEntity> errorEntity;
	/**
	 * @return the code which identifies the rule. Is never {@code null} or empty.
	 * @param errorCode
	 * 		the new code which should identify the rule. May not be {@code null} or empty.
	 */
	private @NonNull String errorCode;
	/**
	 * @return the error condition: If the defined condition is fulfilled this rule fires and the
	 * 		corresponding message (see {@link #getErrorMessage()}) is reported. Is never
	 *        {@code null} or empty.
	 * @param errorCondition
	 * 		the new error condition for the rule (for more information see
	 *        {@link #getErrorCondition()}). May not be {@code null} or empty.
	 */
	private @NonNull String errorCondition;
	/**
	 * @return the severity of the message in case this rule fires (see {@link Severity}). Is never
	 *        {@code null}.
	 * @param severity
	 * 		the new severity of the message which should be reported if the rule fires (see
	 *        {@link Severity}). May not be {@code null}.
	 */
	private @NonNull Severity severity = Severity.ERROR;
	/**
	 * @return the defined message which will be reported in case the rule fires. Is never
	 *        {@code null}. If no error messages are defined, the {@link ILocalizedTextMap} is
	 * 		empty.
	 * @param errorMessage
	 * 		the new message which should be reported is the rule fires. May not be
	 *        {@code null}. Use an empty {@link ILocalizedTextMap} to unset.
	 */
	private @NonNull ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build();
	/**
	 * @return a map with the defined gui patterns. Is never {@code null} or empty. At the moment
	 * 		only following patterns are possible:
	 * 		<ul>
	 * 		<li>key="dynamicEnumeration", value="true"|"false"</li>
	 * 		</ul>
	 * @param guiPatterns
	 * 		a map with the new gui patterns (for more information see
	 *        {@link #getGuiPatterns()}). May not be {@code null} or empty.
	 */
	private @NonNull Map<String, String> guiPatterns = new LinkedHashMap<>();
	/**
	 * @param variantFilter
	 * 		the new variant filtering configuration for this rule. May be {@code null} to
	 * 		unset.
	 */
	private VariantFilter variantFilter;
	/**
	 * @return a map containing variant specific message (in different {@link Locale}s). Is never
	 *        {@code null}. If no variant specific messages are defined, the map is empty.
	 * @param variantErrorMessages
	 * 		a map containing the new variant specific messages. May not be {@code null}. Use
	 * 		an empty map to unset.
	 */
	private @NonNull Map<String, ILocalizedTextMap> variantErrorMessages = new LinkedHashMap<>();

	@Builder(builderClassName = "RuleBuilder")
	private Rule(@NonNull String id, @NonNull String name, @NonNull ILocalizedTextMap internalDescription,
			@NonNull ILocalizedTextMap externalDescription, @NonNull List<Annotation> annotations,
			@NonNull IDocumentModelObjectReference<IEntity> errorEntity, @NonNull String errorCode, @NonNull String errorCondition,
			@NonNull Severity severity, @NonNull ILocalizedTextMap errorMessage, @NonNull Map<String, String> guiPatterns,
			VariantFilter variantFilter, @NonNull Map<String, ILocalizedTextMap> variantErrorMessages) {
		super(null, id, name, internalDescription, externalDescription, annotations);
		this.errorEntity = errorEntity;
		this.errorCode = errorCode;
		this.errorCondition = errorCondition;
		this.severity = severity;
		this.errorMessage = errorMessage;
		this.guiPatterns = guiPatterns;
		this.variantFilter = variantFilter;
		this.variantErrorMessages = variantErrorMessages;
	}

	/**
	 * @return the variant filtering configuration for this rule.
	 */
	public Optional<VariantFilter> getVariantFilter() {
		return Optional.ofNullable(variantFilter);
	}

	public static class RuleBuilder {
		private ILocalizedTextMap internalDescription = new LocalizedTextMapBuilder().build(); // in order to make RuleBuilder.internalDescription() optional
		private ILocalizedTextMap externalDescription = new LocalizedTextMapBuilder().build(); // in order to make RuleBuilder.externalDescription() optional
		private List<Annotation> annotations = new ArrayList<>(); // in order to make RuleBuilder.annotations() optional
		private Map<String, String> guiPatterns = new LinkedHashMap<>(); // in order to make RuleBuilder.guiPatterns() optional
		private ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build(); // in order to make RuleBuilder.errorMessage() optional
		private Map<String, ILocalizedTextMap> variantErrorMessages = new LinkedHashMap<>(); // in order to make RuleBuilder.variantErrorMessages() optional
		private Severity severity = Severity.ERROR;
	}

}
