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

package com.mgmtp.a12.kernel.md.model.a12internal;

import java.util.ArrayList;

import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;

/**
 * Models the {@link DocumentModel} root as a {@link Group} with only following attributes:
 * <ul>
 * <li>it has an internal name and id</li>
 * <li>it is not repeatable</li>
 * <li>its child {@link Element}s are the actual groups at the highest hierarchy level of the
 * {@link DocumentModel}</li>
 * </ul>
 */
@OnlyForA12InternalUsage
public class DocumentModelRoot extends Group {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;
	private static final String DOCUMENT_MODEL_ROOT_NAME = "RootGroup";

	DocumentModelRoot() {
		super(DOCUMENT_MODEL_ROOT_NAME,
				DOCUMENT_MODEL_ROOT_NAME,
				new LocalizedTextMapBuilder().build(),
				new LocalizedTextMapBuilder().build(),
				new ArrayList<>(),
				new LocalizedTextMapBuilder().build(),
				1, null, null, new ArrayList<>(), null);
	}

}
