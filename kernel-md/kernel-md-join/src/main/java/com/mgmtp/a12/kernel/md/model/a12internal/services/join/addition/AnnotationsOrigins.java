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

package com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mgmtp.a12.model.header.Annotation;

import lombok.Getter;

@Getter public class AnnotationsOrigins implements IAdditionOrigins.IAnnotationsOrigins {

	public static AnnotationsOrigins createWithAnnotationsInAdditive(List<Annotation> annotations) {
		AnnotationsOrigins annotationsOrigins = new AnnotationsOrigins();
		annotations.forEach(annotationsOrigins::addAdditiveAnnotation);
		return annotationsOrigins;
	}

	private final List<Annotation> annotationsInRoot = new ArrayList<>();
	private final List<Annotation> annotationsInAdditive = new ArrayList<>();
	private final List<Annotation> annotationsInBoth = new ArrayList<>();

	@Override public void addRootAnnotation(Annotation annotation) {
		annotationsInRoot.add(annotation);
	}

	@Override public void addAdditiveAnnotation(Annotation annotation) {
		annotationsInAdditive.add(annotation);
	}

	@Override public void addCommonAnnotation(Annotation annotation) {
		annotationsInBoth.add(annotation);
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AnnotationsOrigins that))
			return false;
		return Objects.equals(annotationsInRoot, that.annotationsInRoot) && Objects.equals(annotationsInAdditive,
				that.annotationsInAdditive) && Objects.equals(annotationsInBoth, that.annotationsInBoth);
	}

	@Override public int hashCode() {
		return Objects.hash(annotationsInRoot, annotationsInAdditive, annotationsInBoth);
	}
}
