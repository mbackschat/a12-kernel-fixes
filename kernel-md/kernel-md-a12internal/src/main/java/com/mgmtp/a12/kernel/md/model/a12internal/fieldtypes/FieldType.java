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

package com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.IAnnotated;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;
import com.mgmtp.a12.model.header.Annotation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Abstract base type for data type for {@link Field}s.
 */
@Data
@NoArgsConstructor
@OnlyForA12InternalUsage
public class FieldType implements IAnnotated {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return a map with the defined annotations in the order they were inserted into the map. Is
	 * 		never {@code null}. If no annotations were defined, the map is empty.
	 * @param annotations
	 * 		the new annotations in the order they were inserted into the map. May not
	 * 		be {@code null}. Use an empty map to unset.
	 */
	protected @NonNull List<Annotation> annotations = new ArrayList<>();

	protected FieldType(List<Annotation> annotations) {
		this.annotations = new ArrayList<>();
		this.annotations.addAll(annotations);
	}

	@Override
	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = new ArrayList<>();
		this.annotations.addAll(annotations);
	}
}
