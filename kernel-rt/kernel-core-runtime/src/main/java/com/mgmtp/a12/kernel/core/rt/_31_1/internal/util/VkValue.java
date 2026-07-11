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

/**
 * Common interface of the kernel value types, such as {@link VkDate}, {@link VkString} and {@link VkBigDecimal}
 */
public abstract class VkValue {

	/**
	 * must be guaranteed non-null by the all constructors
	 */
	private final CollectionWithCheapMergeCopy<RtInternalIdentifier> origins;

	protected VkValue(CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		this.origins = origins != null ? origins : CollectionWithCheapMergeCopy.empty();
	}

	public abstract boolean isNichtPruefungsrelevant();

	public abstract boolean isNichtAngegeben();

	public abstract boolean hasValue();

	/**
	 * NOTE: the result of this method should be ignored by equals and hashCode, because it is metadata rather than an attribute
	 * of the value.
	 *
	 * @return guaranteed to not return null
	 */
	public CollectionWithCheapMergeCopy<RtInternalIdentifier> getOrigins() {
		assert origins != null : "origins should be guaranteed non-null by the (only) constructor";
		return origins != null ?
				origins :
				CollectionWithCheapMergeCopy.empty(); // be extra defensive here, as null could crash runtime behavior even if it doesn't even need the origins
	}

	;

}
