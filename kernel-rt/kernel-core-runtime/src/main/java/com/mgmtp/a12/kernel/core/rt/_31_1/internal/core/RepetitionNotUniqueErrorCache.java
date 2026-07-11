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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.LongArray;

/**
 * The cache is used for the runtime implementation of the language construct
 * <i>RepetitionNotUnique</i> (= <i>WiederholungNichtEindeutig</i> in German)
 */
public class RepetitionNotUniqueErrorCache {

	private Map<LongArray, Set<LongArray>> erroneousRepetitionsWithSameValueValueError = new HashMap<>();
	private Map<LongArray, Set<LongArray>> erroneousRepetitionsWithSameValueOmissionError = new HashMap<>();
	private boolean initialised = false;

	public void clear() {
		this.erroneousRepetitionsWithSameValueValueError = new HashMap<>();
		this.erroneousRepetitionsWithSameValueOmissionError = new HashMap<>();
		this.initialised = false;
	}

	public Set<LongArray> getErroneousRepetitionsWithValueError(LongArray rep) {
		return this.erroneousRepetitionsWithSameValueValueError.getOrDefault(rep, new HashSet<>());
	}

	public boolean hasValueError(LongArray rep) {
		return erroneousRepetitionsWithSameValueValueError.containsKey(rep);
	}

	public boolean hasOmissionError(LongArray rep) {
		return erroneousRepetitionsWithSameValueOmissionError.containsKey(rep);
	}

	public Set<LongArray> getErroneousRepetitionsWithOmissionsError(LongArray rep) {
		return this.erroneousRepetitionsWithSameValueOmissionError.getOrDefault(rep, new HashSet<>());
	}

	public void addErroneousRepetitionWithValueError(LongArray rep1, LongArray rep2) {
		Set<LongArray> repetitions = erroneousRepetitionsWithSameValueValueError.computeIfAbsent(rep1, v -> new HashSet<>());
		repetitions.add(rep1);
		repetitions.add(rep2);
		repetitions = erroneousRepetitionsWithSameValueValueError.computeIfAbsent(rep2, v -> new HashSet<>());
		repetitions.add(rep1);
		repetitions.add(rep2);
	}

	public void addErroneousRepetitionWithOmissionsError(LongArray rep1, LongArray rep2) {
		Set<LongArray> repetitions = erroneousRepetitionsWithSameValueOmissionError.computeIfAbsent(rep1, v -> new HashSet<>());
		repetitions.add(rep1);
		repetitions.add(rep2);
		repetitions = erroneousRepetitionsWithSameValueOmissionError.computeIfAbsent(rep2, v -> new HashSet<>());
		repetitions.add(rep1);
		repetitions.add(rep2);
	}

	public boolean isInitialised() {
		return this.initialised;
	}

	public void setInitialised() {
		this.initialised = true;
	}
}
