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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer;

import java.util.Objects;

import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

public class SourceIdentifierImpl implements SourceIdentifier {
	private final IIdentifier id;
	private final String sourceId;

	public SourceIdentifierImpl(IIdentifier id, String sourceId) {
		this.id = id instanceof SourceIdentifierImpl srcId ? srcId.id : id;
		this.sourceId = sourceId;
	}

	@Override public String getName() {
		return id.getName();
	}

	@Override public long[] getIndices() {
		return id.getIndices();
	}

	@Override public long getUsb() {
		return id.getUsb();
	}

	@Override public String sourceId() {
		return sourceId;
	}

	@Override public String toString() {
		return id + " von " + sourceId();
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SourceIdentifierImpl that = (SourceIdentifierImpl) o;
		return Objects.equals(id, that.id) && Objects.equals(sourceId, that.sourceId);
	}

	@Override public int hashCode() {
		return Objects.hash(id, sourceId);
	}
}
