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

package com.mgmtp.a12.kernel.md.model.migration.internal.utils;

import static com.mgmtp.a12.kernel.md.model.migration.internal.utils.ReferencedDmsChecker.PORTABLE_FILE_SEPARATOR;
import static com.mgmtp.a12.kernel.md.model.migration.internal.utils.ReferencedDmsChecker.WINDOWS_FILE_SEPARATOR;

public class IdReferenceConverter {
	public IdReferenceConverter() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * We can create the new reference in following way since we check that the file name (which is normally referenced) is the
	 * same as the model id.
	 */
	public static String createIdReference(String reference) {
		int slashIdx = reference.lastIndexOf(PORTABLE_FILE_SEPARATOR); // standard case
		int backSlashIdx = reference.lastIndexOf(WINDOWS_FILE_SEPARATOR); // in case it is a windows reference
		int extensionIdx = reference.lastIndexOf(ModelJsonUtil.JSON_EXTENSION);
		int start = Math.max(slashIdx, backSlashIdx) + 1;
		// there are some cases in DS for which there is already no extension in the reference.
		int end = extensionIdx < 0 ? reference.length() : extensionIdx;
		return reference.substring(start, end);
	}
}
