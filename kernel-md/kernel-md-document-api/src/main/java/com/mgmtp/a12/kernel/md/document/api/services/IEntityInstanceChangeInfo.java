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
package com.mgmtp.a12.kernel.md.document.api.services;

import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Provides access to the information about changes in an entity instance.
 *
 * @deprecated use {@code DocumentV2Utils#compare} and {@code DocumentChanges} instead.
 */
@OnlyForUsage
@Deprecated(since = "30.0.0")
public interface IEntityInstanceChangeInfo {

	enum EntityInstanceChangeType {
		NEW,
		DELETED,
		UPDATED
	}

	/**
	 * See {@link IEntityInstance#getPath()}.
	 */
	String getPath();

	/**
	 * see {@link IEntityInstance#getRepetitions()}.
	 */
	int[] getRepetitions();

	/**
	 * @return the type of change for the entity instance. Is never {@code null}.
	 */
	EntityInstanceChangeType getChangeType();

	/**
	 * @return the changed value in case the entity instance is {@link EntityInstanceChangeType#NEW}
	 * 		or was {@link EntityInstanceChangeType#UPDATED}. This method returns {@code null}, if
	 * 		the entity instance was {@link EntityInstanceChangeType#DELETED}.
	 */
	Object getChangedValue();

}