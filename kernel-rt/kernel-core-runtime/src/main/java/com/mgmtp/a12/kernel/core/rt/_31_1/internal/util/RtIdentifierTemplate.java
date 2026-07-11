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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * This class is used as a container for {@link RtInternalIdentifier} in generated code. Indexes and
 * USB may not be specific here, but can be specified as an interval, iteration or for all.
 * <p>
 * An instance of this class is a template to generate a {@link RtInternalIdentifier}.
 */
public final class RtIdentifierTemplate {

	private static final String USB_NICHT_ANGEGEBEN_STRING = Long.toString(IIdentifier.USB_NOT_SPECIFIED);
	private static final String SEMANTIC_INDEX_STRING = Long.toString(IIdentifier.SEMANTIC_INDEX);
	private static final String PARALLEL_ITERATION_STRING = Long.toString(IIdentifier.PARALLEL_ITERATION);
	private static final String INDEX_TRENNER = "-";

	public static RtIdentifierTemplateBuilder builder() {
		return new RtIdentifierTemplateBuilder();
	}

	/**
	 * This method should be used for the translation of error message field references only.
	 *
	 * @param entityStr
	 * 		The string representation of the entity.
	 * @return The corresponding {@link RtIdentifierTemplate}.
	 */
	public static RtIdentifierTemplate fromStringRepresentation(String entityStr) {
		RtIdentifierTemplateBuilder builder = builder();

		String catEntity = entityStr;
		if (catEntity.contains(Constants.SEMANTIC_INDEX_SEPARATOR)) {
			int idx = catEntity.indexOf(Constants.SEMANTIC_INDEX_SEPARATOR);
			catEntity = entityStr.substring(0, idx);
			String semIdxStr = entityStr.substring(idx + 1);
			builder.semIdx(SemanticIndexTemplate.createFromStringRepresentation(semIdxStr));
		}

		String entity = catEntity;
		if (entity.contains(Constants.CATEGORY_VALUE_ACCESS)) {
			int idx = catEntity.indexOf(Constants.CATEGORY_VALUE_ACCESS);
			entity = catEntity.substring(0, idx);
			builder.category(catEntity.substring(idx + Constants.CATEGORY_VALUE_ACCESS.length()));
		}

		String[] teile = entity.trim().split(Constants.FIELD_LAYER_SEPARATOR);
		builder.unqNm(teile[0]);
		long[] usbSpec = parseLayerSpec(teile[1]);
		builder.usb(usbSpec[0], usbSpec[1]);
		for (int i = 2; i < teile.length; i++) {
			long[] layerSpec = parseLayerSpec(teile[i]);
			builder.idx(layerSpec[0], layerSpec[1]);
		}
		return builder.build();
	}

	private static long[] parseLayerSpec(String ebenenSpecAlsString) {
		if ("e".equals(ebenenSpecAlsString)) {
			return new long[] { IIdentifier.ITERATION, IIdentifier.ITERATION };
		} else if (USB_NICHT_ANGEGEBEN_STRING.equals(ebenenSpecAlsString)) {
			return new long[] { IIdentifier.USB_NOT_SPECIFIED, IIdentifier.USB_NOT_SPECIFIED };
		} else if (SEMANTIC_INDEX_STRING.equals(ebenenSpecAlsString)) {
			return new long[] { IIdentifier.SEMANTIC_INDEX, IIdentifier.SEMANTIC_INDEX };
		} else if (PARALLEL_ITERATION_STRING.equals(ebenenSpecAlsString)) {
			return new long[] { IIdentifier.PARALLEL_ITERATION, IIdentifier.PARALLEL_ITERATION };
		} else {
			String[] idx = ebenenSpecAlsString.split(INDEX_TRENNER);
			return new long[] { Long.parseLong(idx[0]), Long.parseLong(idx[idx.length - 1]) };
		}
	}

	private final String uniqueName;
	private final RtIdentifierTemplateLayerSpec usbSpec;
	private final List<RtIdentifierTemplateLayerSpec> layerSpec;
	private final SemanticIndexTemplate semanticIndex;
	private final boolean isField;
	private final String category;
	private boolean aggregation;
	private final FilterPredicate filter;
	private final boolean outer;

	RtIdentifierTemplate(String uniqueName, RtIdentifierTemplateLayerSpec usbSpec, List<RtIdentifierTemplateLayerSpec> layerSpec,
			SemanticIndexTemplate semanticIndex, boolean isField, String category, FilterPredicate filter, boolean outer) {
		this.uniqueName = uniqueName;
		this.usbSpec = usbSpec;
		this.layerSpec = Collections.unmodifiableList(layerSpec);
		this.semanticIndex = semanticIndex;
		this.isField = isField;
		this.category = category;
		this.aggregation = layerSpecIsAggregation() || usbSpec.getIdxStart() != usbSpec.getIdxEnd();
		this.filter = filter;
		this.outer = outer;
	}

	private boolean layerSpecIsAggregation() {
		for (RtIdentifierTemplateLayerSpec ls : layerSpec) {
			if (ls.getIdxStart() != ls.getIdxEnd()) {
				return true;
			}
		}
		return false;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public RtIdentifierTemplateLayerSpec getUsbSpec() {
		return usbSpec;
	}

	public List<RtIdentifierTemplateLayerSpec> getLayerSpec() {
		return layerSpec;
	}

	public List<RtIdentifierTemplateLayerSpec> getLayerSpecWithUsb() {
		List<RtIdentifierTemplateLayerSpec> ll = new ArrayList<>(layerSpec);
		ll.add(0, usbSpec);
		return ll;
	}

	public RtIdentifierTemplateLayerSpec getSingleLayerSpec(int idx) {
		return layerSpec.get(idx);
	}

	public SemanticIndexTemplate getSemanticIndex() {
		return semanticIndex;
	}

	public boolean isField() {
		return isField;
	}

	public String getCategory() {
		return category;
	}

	public boolean isAggregation() {
		return aggregation;
	}

	public FilterPredicate getFilter() {
		return filter;
	}

	public boolean isOuter() {
		return outer;
	}

	public static RtIdentifierTemplate createFromIdentifierWithoutSemIdx(RtInternalIdentifier entity) {
		RtIdentifierTemplateBuilder builder = RtIdentifierTemplate.builder()
				.category(entity.getCategory())
				.isField(!entity.getName().startsWith("/"))
				.unqNm(entity.getName())
				.usb(entity.getUsb());
		for (long idx : entity.getIndices()) {
			builder.idx(idx);
		}
		return builder.build();
	}

	@Override
	public String toString() {
		return "RtIdentifierTemplate(name: " + uniqueName + ", usb: " + usbSpec + ", layers: " + layerSpec + ", semIdx: " + semanticIndex + ", cat: " + category + ")";
	}

	public RtIdentifierTemplate withLayerSpecWithUsb(List<RtIdentifierTemplateLayerSpec> newLayerSpecWithUsb) {
		return new RtIdentifierTemplate(
				uniqueName, newLayerSpecWithUsb.get(0),
				newLayerSpecWithUsb.subList(1, newLayerSpecWithUsb.size()),
				semanticIndex, isField, category, filter, outer);
	}
}
