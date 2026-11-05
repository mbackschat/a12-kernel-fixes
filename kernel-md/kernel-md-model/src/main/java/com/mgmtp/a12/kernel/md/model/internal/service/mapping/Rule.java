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
package com.mgmtp.a12.kernel.md.model.internal.service.mapping;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.model.header.Annotation;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Provides a default implementation for {@link IRule} with a mapping of the properties from {@link
 * com.mgmtp.a12.kernel.md.model.internal.service.mapping.Rule}.
 */
public class Rule extends Entity implements IRule {

	@Getter private final String errorConditionText;
	@Getter private final Map<Locale, String> errorMessages;
	@Getter private final String errorCode;
	@Getter private final RuleSeverityType severityType;
	@Getter private final String errorEntityRelPath;
	@Getter @Setter private IEntity errorEntity;
	@Getter private final boolean hasCustomRefEntities;
	@Getter private final @NonNull List<String> customRefErrorEntitiesIfFilled;
	@Getter private final @NonNull List<String> customRefErrorEntitiesIfNotFilled;

	public Rule(
			String name,
			String path,
			long[] maxIndices,
			String conditionText,
			Map<Locale, String> errorMessages,
			String errorCode,
			RuleSeverityType severityType,
			String errorEntityRelPath,
			List<Annotation> annotations,
			String ruleId,
			boolean hasCustomRefEntities,
			@NonNull List<String> customRefErrorEntitiesIfFilled,
			@NonNull List<String> customRefErrorEntitiesIfNotFilled
	) {
		super(name, path, maxIndices, annotations, ruleId);

		this.errorConditionText = conditionText;
		this.errorMessages = errorMessages;
		this.errorCode = errorCode;
		this.severityType = severityType;
		this.errorEntityRelPath = errorEntityRelPath;
		this.hasCustomRefEntities = hasCustomRefEntities;
		this.customRefErrorEntitiesIfFilled = customRefErrorEntitiesIfFilled;
		this.customRefErrorEntitiesIfNotFilled = customRefErrorEntitiesIfNotFilled;
	}

	@Override
	public String getErrorMessage(final Locale loc) {
		if (errorMessages.containsKey(loc)) {
			return errorMessages.get(loc);
		} else {
			// fallback to language (without country)
			return errorMessages.get(Locale.of(loc.getLanguage()));
		}
	}

	@Override
	public Stream<Entity> flattenToStream() {
		return Stream.of(this);
	}
}
