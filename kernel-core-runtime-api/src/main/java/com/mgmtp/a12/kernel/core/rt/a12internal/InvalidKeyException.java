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
package com.mgmtp.a12.kernel.core.rt.a12internal;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * This exception will be thrown, if an undefined key is used to access a meta entity.
 *
 * <p>
 * Basically there are three cases to be distinguished for illegal access to meta information:
 * <ul>
 * <li>if an invalid key is used, an <i>InvalidKeyException</i> will be thrown
 * <li>if a valid key is used to access an attribute not containing any value, a <code>null</code>
 * value will be returned
 * <li>if a valid key is used in an inappropriate context (e.g. for a number field, the line break
 * property key is used), an IllegalArgumentException will be thrown.
 * </ul>
 * </p>
 */

@SuppressWarnings("serial")
@OnlyForUsage
public class InvalidKeyException extends RuntimeException {
	private final String key;
	private final Object entity;

	public InvalidKeyException(
			String key,
			Object entity) {
		super(String.format("Invalid key '%s' for entity '%s'", key, entity.getClass().getSimpleName()));
		this.key = key;
		this.entity = entity;
	}

	public String getKey() {
		return key;
	}

	public Object getEntity() {
		return entity;
	}
}
