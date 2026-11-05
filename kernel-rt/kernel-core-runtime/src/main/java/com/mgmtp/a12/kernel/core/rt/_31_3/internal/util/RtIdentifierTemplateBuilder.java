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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for class {@link RtIdentifierTemplate}.
 * It is outside of the class because otherwise we have difficulties to translate it to typescript.
 */
public class RtIdentifierTemplateBuilder {
	private String uniqueName;
	private RtIdentifierTemplateLayerSpec usbSpec;
	private List<RtIdentifierTemplateLayerSpec> layerSpec = new ArrayList<>();
	private SemanticIndexTemplate semanticIndex = SemanticIndexTemplate.createNullIndex();
	private boolean isField;
	private String category;
	private FilterPredicate filter;
	private boolean outer;

	public RtIdentifierTemplateBuilder fromContainer(RtIdentifierTemplate container) {
		this.uniqueName = container.getUniqueName();
		this.usbSpec = container.getUsbSpec();
		this.layerSpec = container.getLayerSpec();
		this.isField = container.isField();
		this.category = container.getCategory();
		this.semanticIndex = container.getSemanticIndex();
		this.outer = container.isOuter();
		return this;
	}

	public RtIdentifierTemplateBuilder unqNm(String uniqueName) {
		this.uniqueName = uniqueName;
		return this;
	}

	public RtIdentifierTemplateBuilder usb(long idx) {
		this.usbSpec = new RtIdentifierTemplateLayerSpec(idx, idx);
		return this;
	}

	public RtIdentifierTemplateBuilder usb(long idxStart, long idxEnd) {
		this.usbSpec = new RtIdentifierTemplateLayerSpec(idxStart, idxEnd);
		return this;
	}

	public RtIdentifierTemplateBuilder idx(long idx) {
		this.layerSpec.add(new RtIdentifierTemplateLayerSpec(idx, idx));
		return this;
	}

	public RtIdentifierTemplateBuilder idx(long idxStart, long idxEnd) {
		this.layerSpec.add(new RtIdentifierTemplateLayerSpec(idxStart, idxEnd));
		return this;
	}

	public RtIdentifierTemplateBuilder semIdx(String semIdxConst) {
		this.semanticIndex = SemanticIndexTemplate.createFromConstant(semIdxConst);
		return this;
	}

	public RtIdentifierTemplateBuilder semIdx(SemanticIndexTemplate semanticIndex) {
		this.semanticIndex = semanticIndex;
		return this;
	}

	public RtIdentifierTemplateBuilder semIdx(RtIdentifierTemplate semIdxField) {
		this.semanticIndex = SemanticIndexTemplate.createFromContainer(semIdxField);
		return this;
	}

	public RtIdentifierTemplateBuilder isField(boolean isField) {
		this.isField = isField;
		return this;
	}

	public RtIdentifierTemplateBuilder category(String category) {
		this.category = category;
		return this;
	}

	public RtIdentifierTemplateBuilder filter(FilterPredicate filter) {
		this.filter = filter;
		return this;
	}

	public RtIdentifierTemplateBuilder outer(boolean outer) {
		this.outer = outer;
		return this;
	}

	public RtIdentifierTemplate build() {
		return new RtIdentifierTemplate(uniqueName, usbSpec, layerSpec, semanticIndex, isField, category, filter, outer);
	}

	private static String getEntityFullName(String fullNameWithModelNamePrefix) {
		int idxOfFullNameStart = fullNameWithModelNamePrefix.indexOf(EntityUtil.SEPARATOR_CHAR, 2);
		return fullNameWithModelNamePrefix.substring(idxOfFullNameStart);
	}
}
