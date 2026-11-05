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

import com.mgmtp.a12.kernel.core.utils.internal.string.sourcemap.StringWithFragmentsOriginMap;
import com.mgmtp.a12.kernel.core.utils.internal.string.sourcemap.StringWithFragmentsOriginMapEntry;
import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.model.header.Annotation;

import lombok.Getter;

public class RuleFromComputationImpl extends Rule implements IRuleFromComputation {
	private final StringWithFragmentsOriginMap conditionTextWithFragmentOriginMap;

	@Getter private final IComputation originalComputation;

	public RuleFromComputationImpl(
			final String name,
			final String path,
			final long[] maxIndices,
			final StringWithFragmentsOriginMap conditionTextWithFragmentOriginMap,
			final Map<Locale, String> errorMessages,
			final String errorCode,
			final RuleSeverityType severity,
			final String errorEntityRelPath,
			List<Annotation> annotations,
			IComputation originalComputation) {
		super(name,
				path,
				maxIndices,
				conditionTextWithFragmentOriginMap.getString(),
				errorMessages,
				errorCode,
				severity,
				errorEntityRelPath,
				annotations,
				originalComputation.getId(),
				false,
				List.of(),
				List.of());
		this.conditionTextWithFragmentOriginMap = conditionTextWithFragmentOriginMap;
		this.originalComputation = originalComputation;
	}

	@Override public StringWithFragmentsOriginMapEntry mapPositionInConditionTextToOriginInComputationAlternative(int positionIdx) {
		return this.conditionTextWithFragmentOriginMap.mapPositionToOrigin(positionIdx);
	}

}
