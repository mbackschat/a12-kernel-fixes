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

package com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;

public interface IDynamicEnumerationInfo {
	/**
	 * @return the field, the values of which are calculated.
	 */
	IEntityId getDynamicEnumerationField();

	/**
	 * @return the safeguard that has to be fulfilled to calculate values.
	 */
	ICondition getSafeGuard();

	/**
	 * Return the alternative value operations.
	 * <p>
	 * Each of the alternative values consists of a negative precondition and a calculation
	 * operation.
	 * The precondition may be <code>null</code>, in which case, the value is always calculated.
	 * If the precondition is fulfilled, the corresponding value calculation will be omitted.
	 *
	 * @return the alternative value calculations.
	 */
	List<Pair<ICondition, IOperation>> getValueOperations();

	/**
	 * @return the name of the source for the calculation info. Used for output.
	 */
	String getName();

	default List<IParseTreeEntity> getAllParts() {
		List<IParseTreeEntity> valueOperationParts = getValueOperations().stream()
				.flatMap(p -> Stream.<IParseTreeEntity>of(p.getLeft(), p.getRight()))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		List<IParseTreeEntity> result = new ArrayList<>(valueOperationParts);
		if (getSafeGuard() != null) {
			result.add(getSafeGuard());
		}
		return result;
	}
}
