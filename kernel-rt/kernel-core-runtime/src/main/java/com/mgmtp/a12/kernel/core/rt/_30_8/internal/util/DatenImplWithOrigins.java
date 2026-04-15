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
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculationResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

public class DatenImplWithOrigins extends DatenImpl implements ICalculationResult {
	private final Map<RtExternalIdentifier, Set<IIdentifier>> originsMap = new HashMap<>();
	private Set<RtExternalIdentifier> fieldsToBeCleared = null;

	public DatenImplWithOrigins() {
		super();
	}

	public DatenImplWithOrigins(Map<IIdentifier, String> data) {
		super(data);
	}

	public void addOrigins(IIdentifier id, Set<IIdentifier> origins) {
		var key = keyFor(id);
		assert !originsMap.containsKey(key) : "overriding of existing entries is not expected here";
		originsMap.put(key, origins);
	}

	public void setFieldsToBeCleared(Set<RtExternalIdentifier> fieldIds) {
		assert fieldsToBeCleared == null;
		this.fieldsToBeCleared = fieldIds;
		fieldsToBeCleared.forEach(id -> this.addValue(id, ""));
	}

	/**
	 * @return the set of origins if origins have been registered for this id, or null
	 */
	public Set<IIdentifier> getOrigins(IIdentifier id) {
		return originsMap.get(keyFor(id));
	}

	/**
	 * @return the set of origins if origins have been registered for this id, or a set that contains the id. I.e. if a field
	 * 		does not originate from other fields, it is treated like it is its own origin.
	 */
	public Set<IIdentifier> getOriginsOrSelf(IIdentifier id) {
		var externalOrigins = getOrigins(id);
		return externalOrigins != null ? externalOrigins : Set.of(id);
	}

	private RtExternalIdentifier keyFor(IIdentifier id) {
		assert !(id instanceof RtExternalIdentifierWithSemanticIndex) :
				"didn't expect RtExternalIdentifierWithSemanticIndex here but a plain IIdentifier with only concrete indices";
		return RtExternalIdentifier.ensureImpl(id);
	}

	public IData apply(IData originalData) {
		Set<RtExternalIdentifier> effectiveFieldsToBeCleared = fieldsToBeCleared != null
				? fieldsToBeCleared
				: Set.of();
		IData result = new DatenImpl();
		for (var id : originalData) {
			if (!effectiveFieldsToBeCleared.contains(RtExternalIdentifier.ensureImpl(id))) {
				result.addValue(id, originalData.getValue(id));
			}
		}
		for (var id : this) {
			result.addValue(id, getValue(id));
		}
		return result;
	}
}
