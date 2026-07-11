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

package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.LocaleUtils;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.mmtypings.ct_model_header_1.views._ct_model_header_1.Header;
import com.mgmtp.a12.kernel.mmtypings.ct_model_header_1.views._ct_model_header_1._header.Annotations;
import com.mgmtp.a12.kernel.mmtypings.ct_model_header_1.views._ct_model_header_1._header.Labels;
import com.mgmtp.a12.kernel.mmtypings.ct_model_header_1.views._ct_model_header_1._header.Locales;
import com.mgmtp.a12.kernel.mmtypings.ct_model_header_1.views._ct_model_header_1._header.ModelReferences;
import com.mgmtp.a12.model.header.HeaderFactory;

import lombok.NonNull;

/**
 * see also {@link com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil}
 */
public class ModelHeaderUtils {
	private ModelHeaderUtils() {
		throw new UnsupportedOperationException("should not be instantiated");
	}

	public static @NonNull com.mgmtp.a12.model.header.Header fromGroupInstance(@NonNull GroupInstanceV2 groupInstance) {
		var header = Header._viewOf(groupInstance);
		HeaderFactory.Builder builder = new HeaderFactory.Builder()
				.withModelType(header.modelType())
				.withId(header.id())
				.withModelVersion(header.modelVersion());

		if (header.description() != null) {
			builder = builder.withDescription(header.description());
		}

		return builder
				.withLocales(header.locales().stream()
						.map(locale -> LocaleUtils.toLocale(locale.code()))
						.toList())
				.withLabels(header.labels().stream()
						.map(label -> HeaderFactory.createLabel(LocaleUtils.toLocale(label.locale()), label.text()))
						.toList())
				.withAnnotations(header.annotations().stream()
						.map(annotation -> HeaderFactory.createAnnotation(annotation.name(), annotation.value()))
						.toList())
				.withModelReferences(header.modelReferences().stream()
						.map(modelReference -> HeaderFactory.createModelReference(
								modelReference.purpose(),
								modelReference.modelType(),
								modelReference.alias(),
								modelReference.reference()))
						.toList())
				.build();
	}

	public static @NonNull GroupInstanceV2 toGroupInstance(@NonNull com.mgmtp.a12.model.header.Header header) {
		return Header._empty()
				._with(Header._pointer().id(), header.getId())
				._with(Header._pointer().modelType(), header.getModelType())
				._with(Header._pointer().modelVersion(), header.getModelVersion())
				._with(Header._pointer().description(), header.getDescription())
				._with(Header._pointer().locales(), header.getLocales().stream()
						.map(locale -> Locales._empty()
								._with(Locales._pointer().code(), locale.getLanguage()))
						.toList())
				._with(Header._pointer().labels(), header.getLabels().stream()
						.map(label -> Labels._empty()
								._with(Labels._pointer().locale(), label.getLocale().getLanguage())
								._with(Labels._pointer().text(), label.getText()))
						.toList())
				._with(Header._pointer().annotations(), header.getAnnotations().stream()
						.map(annotation -> Annotations._empty()
								._with(Annotations._pointer().name(), annotation.getName())
								._with(Annotations._pointer().value(), annotation.getValue()))
						.toList())
				._with(Header._pointer().modelReferences(), ListUtils.emptyIfNull(header.getModelReferences()).stream()
						.map(modelReference -> ModelReferences._empty()
								._with(ModelReferences._pointer().alias(), modelReference.getAlias())
								._with(ModelReferences._pointer().modelType(), modelReference.getModelType())
								._with(ModelReferences._pointer().purpose(), modelReference.getPurpose())
								._with(ModelReferences._pointer().reference(), modelReference.getReference()))
						.toList())
				._unwrap();
	}
}
