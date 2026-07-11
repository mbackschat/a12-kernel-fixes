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
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifierWithSemanticIndex;

/**
 * A simple implementation of the interface {@link IIdentifier}.
 */
public class RtExternalIdentifier implements IIdentifier {
	private String name;
	private final long[] indices;
	private long usb;

	public RtExternalIdentifier(final String name, final long[] indices) {
		this(name, indices, IIdentifier.USB_NOT_SPECIFIED);
	}

	public RtExternalIdentifier(final String name, final long[] indices, final long usb) {
		this.name = name;
		this.indices = indices;
		this.usb = usb;
	}

	public RtExternalIdentifier(final IIdentifier id) {
		this(id.getName(), id.getIndices().clone(), id.getUsb());
	}

	public static RtExternalIdentifier of(final String name, final long[] idx) {
		return new RtExternalIdentifier(name, idx);
	}

	/**
	 * Returns an RtExternalIdentifier for the given IIdentifier.
	 * If id already is an RtExternalIdentifier it will be returned as is.
	 *
	 * @param id
	 * 		the id to be converted.
	 * @return an RtExternalIdentifier corresponding to the id.
	 */
	public static RtExternalIdentifier ensureImpl(final IIdentifier id) {
		if (id instanceof RtExternalIdentifier rtExtId) {
			return rtExtId;
		}
		return new RtExternalIdentifier(id);
	}

	/**
	 * Erzeugt ein {@link RtExternalIdentifier} Objekt mit dem Namen &lt;name&gt;, dessen USB das
	 * erste Element aus &lt;idxMitUsb&gt; ist, die Indices die restlichen Elemente.
	 */
	public static RtExternalIdentifier createFromIndexWithUsb(final String name, final long[] idxMitUsb) {
		final long[] idx = Arrays.copyOfRange(idxMitUsb, 1, idxMitUsb.length);
		return new RtExternalIdentifier(name, idx, idxMitUsb[0]);
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long[] getIndices() {
		return indices;
	}

	public void setUsb(long usb) {
		this.usb = usb;
	}

	@Override
	public long getUsb() {
		return usb;
	}

	/**
	 * Liefert die Stringrepräsentation dieser Klasse.
	 *
	 * @return die Stringrepräsentation dieses Objekts
	 */
	public String toString() {

		final String[] teile = name.split("/");
		if ((indices.length == 2 && teile.length != 3) || !name.contains("/")) {
			return DatenImpl.idIdToString(new RtExternalIdentifier(this.getName(), this.getIndices(), this.getUsb()));
		}

		final StringBuilder sb = new StringBuilder();
		for (int i = 1; i < teile.length; i++) {
			sb.append(String.format("/%s[%d]", teile[i], indices[i - 1]));
		}
		return sb.toString();

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(indices);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Long.hashCode(usb);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IIdentifier other)) {
			return false;
		}
		if (obj instanceof IIdentifierWithSemanticIndex) {
			return false;
		}

		return fieldsEqual(other);
	}

	protected boolean fieldsEqual(IIdentifier other) {
		if (name == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!name.equals(other.getName())) {
			return false;
		}
		if (other.getUsb() != getUsb()) {
			return false;
		}
		if (!Arrays.equals(other.getIndices(), getIndices())) {
			return false;
		}
		return true;
	}
}
