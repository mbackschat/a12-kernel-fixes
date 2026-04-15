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
package com.mgmtp.a12.kernel.md.model.api;

import java.io.Serializable;
import java.util.List;

import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.model.header.Annotation;

import lombok.NonNull;

/**
 * Interface to get ant set annotations.
 * <p>
 * An annotation is a name-value pair which defines a specific attribute outside the defined scope
 * of the current document model. It can be used to set project specific information.
 */
@OnlyForUsage
public interface IAnnotated extends Serializable {

	/**
	 * @return the defined annotations. Is never {@code null}. If no annotations are defined, the
	 * 		list is empty.
	 */
	@NonNull List<Annotation> getAnnotations();

	/**
	 * @param annotations
	 * 		the new annotations. May not be {@code null}. Use an empty list to unset.
	 */
	void setAnnotations(@NonNull List<Annotation> annotations);

}
