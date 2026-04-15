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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.SetParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.util.MetaInfoCodeGenParams;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;

/**
 * Holds common code for creating additional key-values.
 */
public class AdditionalKeyValuesDelegate {
	private final List<IMetaInfoCodeGenParams> relevantParams;
	private final String name;

	public AdditionalKeyValuesDelegate(List<IMetaInfoCodeGenParams> additionalKeyValues,
			UnaryOperator<List<IMetaInfoCodeGenParams>> extraKeyValues, String metaType,
			UnaryOperator<List<IMetaInfoCodeGenParams>> sorter, String name) {
		this.relevantParams = sorter.apply(getRelevantParams(extraKeyValues, metaType, additionalKeyValues));
		this.name = name;
	}

	private static List<IMetaInfoCodeGenParams> getRelevantParams(UnaryOperator<List<IMetaInfoCodeGenParams>> extraKeyValues,
			String metaType, List<IMetaInfoCodeGenParams> additionalKeyValues) {
		List<IMetaInfoCodeGenParams> relevantParams = additionalKeyValues.stream()
				.filter(p -> metaType.equals(p.getMetaElementType()))
				.collect(Collectors.toList());
		relevantParams.addAll(extraKeyValues.apply(relevantParams));
		return relevantParams;
	}

	public String getAdditionalValuesCode() {
		AdditionalKeyValues additionalKeyValues = new AdditionalKeyValues(name, relevantParams);
		return additionalKeyValues.toCode();
	}

	public String getInternalAdditionalKeysCode() {
		Set<String> internalKeys =
				relevantParams.stream().filter(this::isInternalKey).map(IMetaInfoCodeGenParams::getKey).collect(Collectors.toSet());
		Set<String> externalKeys =
				relevantParams.stream().filter(p -> !isInternalKey(p)).map(IMetaInfoCodeGenParams::getKey)
						.collect(Collectors.toSet());
		Set<String> duplicates = new HashSet<>(internalKeys);
		duplicates.retainAll(externalKeys);
		if (!duplicates.isEmpty()) {
			throw new IllegalArgumentException("Following keys are defined as internal and external: " + duplicates);
		}

		return new SetParameter<>(internalKeys.stream()
				.map(StringParameter::new)
				.collect(Collectors.toCollection(LinkedHashSet::new)))
				.toCode();
	}

	private boolean isInternalKey(IMetaInfoCodeGenParams param) {
		return param instanceof MetaInfoCodeGenParams p && p.isInternal();
	}
}
