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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;

/**
 * This class models the boundaries of a repetition layer.
 * This class is outside {@link RtIdentifierTemplate} because otherwise we have difficulties to
 * translate the class to typescript.
 */
public class RtIdentifierTemplateLayerSpec {

	private final long idxStart;
	private final long idxEnd;

	public RtIdentifierTemplateLayerSpec(long idxStart, long idxEnd) {
		assert idxStart <= idxEnd;
		// (0,0) happens at least in the following case: createEbenenIterator via getReferencedFields via addValidatorMessage
		assert idxStart >= 1 || (idxStart == idxEnd && idxStart <= 0) : idxStart + " " + idxEnd;
		this.idxStart = idxStart;
		this.idxEnd = idxEnd;
	}

	public long getIdxStart() {
		return idxStart;
	}

	public long getIdxEnd() {
		return idxEnd;
	}

	public String toString() {
		return "[" + idxStart + " - " + idxEnd + "]";
	}

	public static Pair<long[], long[]> layerSpecsToStartEndIndices(List<RtIdentifierTemplateLayerSpec> ll) {
		long[] startIdx = new long[ll.size()];
		long[] endIdx = new long[ll.size()];
		for (int i = 0; i < ll.size(); i++) {
			startIdx[i] = ll.get(i).getIdxStart();
			endIdx[i] = ll.get(i).getIdxEnd();
		}
		return new Pair<>(startIdx, endIdx);
	}

	public static List<RtIdentifierTemplateLayerSpec> startEndIndicesToLayerSpecs(long[] startIndices, long[] endIndices) {
		if (startIndices.length != endIndices.length) {
			throw new IllegalArgumentException();
		}
		List<RtIdentifierTemplateLayerSpec> ls = new ArrayList<>(startIndices.length);
		for (int i = 0; i < startIndices.length; i++) {
			ls.add(new RtIdentifierTemplateLayerSpec(startIndices[i], endIndices[i]));
		}
		return ls;
	}
}
