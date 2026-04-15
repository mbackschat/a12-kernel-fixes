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

package com.mgmtp.a12.kernel.core.utils.internal.metaModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

public class IdentifierImpl implements IIdentifier {

	String name;
	long[] indices;

	public static IdentifierImpl ensureImpl(IIdentifier other) {
		if (other instanceof IdentifierImpl otherId) {
			return otherId;
		}
		return new IdentifierImpl(other);
	}

	public IdentifierImpl(String name, long[] indices) {
		this.name = name;
		this.indices = indices;
	}

	public IdentifierImpl(IIdentifier other) {
		this.name = other.getName();
		this.indices = other.getIndices();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long[] getIndices() {
		return indices;
	}

	@Override
	public long getUsb() {
		return IIdentifier.USB_NOT_SPECIFIED;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(indices);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IdentifierImpl other = (IdentifierImpl) obj;
		if (!Arrays.equals(indices, other.indices)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append("{id=");

		String[] parts = StringUtils.split(name, "/");
		int partOffset = 0;
		if (StringUtils.isEmpty(parts[0])) {
			sb.append("/");
			partOffset = 1;
		}
		for (int i = 0; i < Math.min(parts.length - partOffset, indices.length); i++) {
			sb.append(String.format("%s[%d]", parts[i + partOffset], indices[i]));
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * @param arg
	 * 		path with attached index values. Example is {@code /MVK[1]/Fields[34]/name[1]}
	 */
	public static IIdentifier create(String arg) {
		Pattern p = Pattern.compile("(\\/\\w+)(\\[(\\d+)\\])?(?=(/|$))");
		Matcher m = p.matcher(arg);
		List<String> path = new ArrayList<>();
		List<Long> indices = new ArrayList<>();
		while (m.find()) {
			String group = m.group(1);
			String idx = m.group(3);
			path.add(group);
			if (idx == null) {
				indices.add(1L);
			} else {
				indices.add(Long.parseLong(idx));
			}
		}
		return new IdentifierImpl(path.stream().collect(Collectors.joining()), indices.stream().mapToLong(l -> l).toArray());
	}

}
