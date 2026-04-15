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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * Default implementation of {@link IData}.
 * This class saves a value through an instance of {@link RtInternalIdentifier} (intern
 * implementation of
 * {@link IIdentifier}). The access to the value(s) works in the same way.
 * Method {@link IData#identifierToString(IIdentifier)} implements a default string representation
 * of {@link IIdentifier}.
 */
public class DatenImpl implements IData {
	Map<IIdentifier, String> data;

	public DatenImpl() {
		this.data = new HashMap<>();
	}

	public DatenImpl(Map<IIdentifier, String> data) {
		this.data = convertToRtIdentifierMap(data);
	}

	public String getValue(IIdentifier id) {
		return data.get(RtExternalIdentifier.ensureImpl(id));
	}

	public String getValueForDisplay(IIdentifier id) {
		return getValue(id);
	}

	public Iterator<IIdentifier> iterator() {
		return data.keySet().iterator();
	}

	public String identifierToString(IIdentifier id) {
		return idToString(id);
	}

	public static String idToString(IIdentifier id) {
		assert id.getName() != null;
		long[] indices = id.getIndices();
		assert indices != null && indices.length > 0;
		String[] nameParts = StringUtils.split(id.getName(), IIdentifier.SEPARATOR);

		StringBuilder sb = new StringBuilder();
		// 1. USB
		sb.append("/[");
		if (IIdentifier.USB_NOT_SPECIFIED != id.getUsb()) {
			sb.append(id.getUsb());
		}
		sb.append("]/");

		// 2. root group -> length of indices is always at least 1
		sb.append(nameParts[0]);
		sb.append("[");
		sb.append(indices[0]);
		sb.append("]");

		if (nameParts.length == 1) {
			// just root group
			return sb.toString();
		} else {
			sb.append("/");
		}

		// 3. groups
		boolean hasIdxForAllNameParts = (indices.length == nameParts.length);
		assert hasIdxForAllNameParts || indices.length == 2;
		for (int i = 1; i < nameParts.length - 1; i++) {
			sb.append(nameParts[i]);
			sb.append("[");
			if (hasIdxForAllNameParts) {
				sb.append(indices[i]);
			}
			sb.append("]/");
		}

		// 4. field or last group
		sb.append(nameParts[nameParts.length - 1]);
		sb.append("[");
		sb.append(indices[indices.length - 1]);
		sb.append("]");

		return sb.toString();
	}

	public void addValue(IIdentifier id, String value) {
		data.put(RtExternalIdentifier.ensureImpl(id), value);
	}

	private static Map<IIdentifier, String> convertToRtIdentifierMap(Map<IIdentifier, String> externMap) {
		Map<IIdentifier, String> result = new HashMap<>();

		externMap.forEach((k, v) -> result.put(RtExternalIdentifier.ensureImpl(k), v));
		return result;
	}

	/**
	 * Liefert die Stringrepräsentation zum übergebenen IIdentifier.
	 */
	protected static String idIdToString(IIdentifier id) {
		assert (id instanceof RtExternalIdentifier);
		long[] indices = id.getIndices();

		assert (indices.length == 2);

		String s = String.format("%s/%d.%d", id.getName(), indices[0], indices[1]);

		if (id.getUsb() != IIdentifier.USB_NOT_SPECIFIED) {
			s = String.format("%s@%d", s, id.getUsb());
		}

		return s;
	}
}
