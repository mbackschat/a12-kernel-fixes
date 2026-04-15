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

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaGroup;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;

public class AnnotationUtils {

	public static boolean groupHasAnnotation(IMetaGroup metaGroup, String annotation) {
		return metaGroup.getValue(IMetaKeys.GROUP_ANNOTATION, annotation) != null; // empty string counts as present
	}

	public static boolean fieldHasAnnotation(IMetaField metaField, String annotation) {
		return metaField.getValue(IMetaKeys.FIELD_ANNOTATION, annotation) != null; // empty string counts as present
	}

	public static boolean fieldOrAncestorHasAnnotation(IMetaField metaField, String annotation,
			IMetaDataValidierung metaDataValidierung) {
		if (fieldHasAnnotation(metaField, annotation)) {
			return true;
		}
		String fieldFullName = (String) metaField.getValue(IMetaKeys.FIELD_FULLNAME);
		String ancestorFullName = EntityUtil.getParentPath(fieldFullName);
		while (!ancestorFullName.isEmpty()) {
			IMetaGroup ancestorMetaGroup = metaDataValidierung.getMetaGroupOrNull(ancestorFullName);
			if (groupHasAnnotation(ancestorMetaGroup, annotation)) {
				return true;
			}
			ancestorFullName = EntityUtil.getParentPath(ancestorFullName);
		}
		return false;
	}

	private AnnotationUtils() {
		throw new UnsupportedOperationException();
	}
}
