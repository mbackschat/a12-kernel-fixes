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
package com.mgmtp.a12.kernel.md.model.internal.services.join.util;

import java.util.HashMap;
import java.util.Map;

import com.mgmtp.a12.kernel.core.utils.internal.HashingHelper;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;

public class IdUtil {

	private final Map<String, String> idCache = new HashMap<>();

	public String getIdForAdditiveModel(DocumentModel additiveModel, Element element) {
		return getIdForAdditiveModel(additiveModel, element.getId());
	}

	public String getIdForAdditiveModel(DocumentModel additiveModel, String id) {
		String dmHash = idCache.computeIfAbsent(additiveModel.getHeader().getId(), HashingHelper::calculateHash);
		return String.format("%s_%s", dmHash, id);
	}

	public static String getIdForDecorationModel(Element decoratedElement, Element element) {
		return String.format("%s_%s", decoratedElement.getId(), element.getId());
	}
}
