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

import java.util.Arrays;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

public class IdentifierUtil {
	private IdentifierUtil() {
		throw new RuntimeException("static utility class");
	}

	public static IIdentifier extractRootGroup(IIdentifier id) {
		return RtExternalIdentifier.of(
				EntityUtil.getRootGroupFullName(id.getName()),
				new long[] { id.getIndices()[0] });
	}

	public static IIdentifier changeRoot(IIdentifier id, String newRootName, long newRootRepetition) {
		return insertRootInternal(removeRootInternal(IdentifierParams.of(id)), newRootName, newRootRepetition).toIdentifier();
	}

	public static IIdentifier insertRoot(IIdentifier id, String newRootName, long newRootRepetition) {
		return insertRootInternal(IdentifierParams.of(id), newRootName, newRootRepetition).toIdentifier();
	}

	public static IIdentifier removeRoot(IIdentifier id) {
		return removeRootInternal(IdentifierParams.of(id)).toIdentifier();
	}

	private static IdentifierParams removeRootInternal(IdentifierParams id) {
		return IdentifierParams.of(
				EntityUtil.getRestPath(id.name()),
				Arrays.copyOfRange(id.indices(), 1, id.indices().length)
		);
	}

	private static IdentifierParams insertRootInternal(IdentifierParams id, String newRootName, long newRootRepetition) {
		if (!newRootName.startsWith("/")) {
			newRootName = "/" + newRootName;
		}
		return IdentifierParams.of(
				newRootName + id.name(),
				concatArrays(new long[] { newRootRepetition }, id.indices())
		);
	}

	/**
	 * wraps name and indices of an identifier, but explicitly supports representing the case where name == "" and indices == {}
	 */
	private record IdentifierParams(String name, long[] indices){
		private static IdentifierParams of(String name, long[] indices) {
			return new IdentifierParams(name, indices);
		}
		private static IdentifierParams of(IIdentifier id) {
			return new IdentifierParams(id.getName(), id.getIndices());
		}
		private IIdentifier toIdentifier() {
			assert !"".equals(name);
			assert indices.length > 0;
			return RtExternalIdentifier.of(name, indices);
		}
	}

	private static long[] concatArrays(long[] a, long[] b) {
		long[] result = new long[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}
}
