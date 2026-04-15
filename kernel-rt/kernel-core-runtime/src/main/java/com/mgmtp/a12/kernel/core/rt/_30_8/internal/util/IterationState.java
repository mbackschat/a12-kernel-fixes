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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

import java.util.Arrays;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * Represents the concrete indices of an iteration. In case of parallel iteration, one of the
 * indices can be defined by the value of an index field ({@link #indexValue}).
 * When inside a filter conditions, also stores the {@link IterationState} of the (outer) rule iteration.
 */
public class IterationState {

	private final long[] indicesWithUsb;
	private final String indexValue;
	private final IterationState outerState;

	/**
	 * @param indicesWithUsb
	 * 		Indices of the iteration, including USB at position 0. May contain
	 *        {@link IIdentifier#PARALLEL_ITERATION}. In this case,
	 * 		the concrete repetition is defined by {@link #indexValue}.
	 * @param indexValue
	 * 		Value of the index field that defines the concrete repetition for the indexed
	 * 		layer.
	 */
	public IterationState(long[] indicesWithUsb, String indexValue) {
		this.indicesWithUsb = indicesWithUsb.clone();
		this.indexValue = indexValue;
		this.outerState = null;
	}

	/**
	 * @param indicesWithUsb
	 * 		Indices of the iteration, including USB at position 0. May contain
	 *        {@link IIdentifier#PARALLEL_ITERATION}. In this case,
	 * 		the concrete repetition is defined by {@link #indexValue}.
	 * @param indexValue
	 * 		Value of the index field that defines the concrete repetition for the indexed
	 * 		layer.
	 * @param outerState
	 * 		the state of the (outer) rule iteration when inside a filter condition.
	 */
	public IterationState(long[] indicesWithUsb, String indexValue, IterationState outerState) {
		this.indicesWithUsb = indicesWithUsb.clone();
		this.indexValue = indexValue;
		this.outerState = outerState;
	}

	public long getIndex(int level) {
		return indicesWithUsb[level];
	}

	public String getValue() {
		return indexValue;
	}

	public long[] getIndexes() {
		return indicesWithUsb.clone();
	}

	public long[] getIndexesWithoutUsb() {
		return Arrays.copyOfRange(indicesWithUsb, 1, indicesWithUsb.length);
	}

	public long getZeile() {
		return indicesWithUsb[indicesWithUsb.length - 1];
	}

	@Override
	public String toString() {
		return "IterationState [indices=" + Arrays.toString(indicesWithUsb) + ", indexValue=" + indexValue + "]";
	}

	/**
	 * @return the state of the (outer) rule iteration when inside a filter condition. Outside of filter conditions
	 * 		<code>null</code>.
	 */
	public IterationState getOuterState() {
		return outerState;
	}
}
