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

package com.mgmtp.a12.kernel.md.rt.a12internal.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaGroup;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.rt.internal.service.DocumentPointerConverter;
import com.mgmtp.a12.kernel.md.rt.internal.service.Identifier;
import com.mgmtp.a12.kernel.md.rt.internal.service.LowKernelApiUtils;
import com.mgmtp.a12.kernel.md.rt.internal.service.ResultForConversion;
import com.mgmtp.a12.kernel.md.rt.internal.service.ValidationData;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion;

import lombok.NonNull;

/**
 * This class contains utility methods for converting {@link IData} to {@link DocumentV2} and vice versa.
 */
@OnlyForUsage
public class DataDocumentConversionUtils {

	private DataDocumentConversionUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Utility method to convert {@link IData} to {@link DocumentV2} for a given document model id.
	 *
	 * @param data
	 *        {@link IData} to be converted
	 * @param documentModelId
	 * 		id of the according document model
	 * @param metaModel
	 * 		needed to check if the elements are fields or groups
	 * @param presentationInformation
	 * 		defines the external representation. If this value is null, no value conversion is performed. This means, the values given
	 * 		in <code>data</code> will be interpreted as internal values.
	 * @param result
	 * 		needed to gather the problems which occur while converting the value
	 */
	public static DocumentV2 createDocumentV2(@NonNull IData data, @NonNull String documentModelId, @NonNull IMetaModel metaModel,
			@NonNull IResult result, PresentationInformation presentationInformation) {
		if (data instanceof ValidationData validationData) {
			assert presentationInformation == null : "it seems unexpected (though possibly correct) to call this with an IData " +
					"that holds ui values";
			DocumentV2 unwrappedDocV2 = validationData.getInternalDocumentV2();
			assert documentModelId.equals(unwrappedDocV2.getDocumentModelId()) : "dm ids should match: " + documentModelId + " vs "
					+ unwrappedDocV2.getDocumentModelId();
			return unwrappedDocV2; // unwrap here, to prevent nested redundant wrappers if createData(DocumentV2) is used too
		}
		return convert(data, documentModelId, metaModel, result, presentationInformation);
	}

	private static DocumentV2 convert(@NonNull IData d, @NonNull String dmId, @NonNull IMetaModel metaModel,
			@NonNull IResult result, PresentationInformation presentationInformation) {
		List<UpdateAction> updates = StreamSupport
				.stream(d.spliterator(), false)
				.map(identifier -> {
					DocumentPointer pointer = DocumentPointerConverter.convertToDocumentPointer(identifier);

					IMetaField metaField = (IMetaField) metaModel.getValue(IMetaKeys.MODEL_META_FIELD, identifier.getName());
					if (metaField != null) {
						String sValue = d.getValue(identifier);
						ResultForConversion conversionResult = new ResultForConversion(result, identifier);
						Object value = BasicTypeV1V2Conversion.basicTypeV1ToV2(
								metaField.convertWithOnlyBasicTypeCheck(sValue, presentationInformation, conversionResult));
						return Optional.of(UpdateAction.putField(pointer, FieldInstanceV2.ofValue(value)));
					}

					IMetaGroup metaGroup = (IMetaGroup) metaModel.getValue(IMetaKeys.MODEL_META_GROUP, identifier.getName());
					if (metaGroup != null) {
						return Optional.of(UpdateAction.putGroup(pointer, GroupInstanceV2.empty()));
					}

					return Optional.<UpdateAction>empty();
				})
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();

		return DocumentV2.empty(dmId).withBatchUpdates(updates);
	}

	/**
	 * Utility method to convert an {@link DocumentV2} to {@link IData}.
	 *
	 * @param document
	 *        {@link DocumentV2} to be converted
	 * @param metaModel
	 * 		needed to get values for given {@link IIdentifier}s (see e.g. {@link IData#getValue(IIdentifier)}).
	 * @param locale
	 * 		needed to get the according display values
	 * @param result
	 * 		needed to be able to pass results from conversion while getting the value of a specific field instance
	 * @return the corresponding {@link IData}-instance.
	 *
	 * @throws IllegalArgumentException
	 * 		if {@code locale} is not supported in {@code metaModel}
	 * @deprecated use
	 *        {@link DataDocumentConversionUtils#createData(DocumentV2, IMetaModel, Locale, IResult, ICustomFieldTypeFactory)} instead.
	 */
	@Deprecated(since = "31.2.0")
	public static IData createData(DocumentV2 document, IMetaModel metaModel, Locale locale, IResult result) {
		return createData(document, metaModel, locale, result, null);
	}

	/**
	 * Utility method to convert an {@link DocumentV2} to {@link IData}.
	 *
	 * @param document
	 *        {@link DocumentV2} to be converted
	 * @param metaModel
	 * 		needed to get values for given {@link IIdentifier}s (see e.g. {@link IData#getValue(IIdentifier)}).
	 * @param locale
	 * 		needed to get the according display values
	 * @param result
	 * 		needed to be able to pass results from conversion while getting the value of a specific field instance
	 * @param customFieldTypeFactory
	 * 		the {@link ICustomFieldTypeFactory} to be used. Only required if IData is validated later.
	 * @return the corresponding {@link IData}-instance.
	 *
	 * @throws IllegalArgumentException
	 * 		if {@code locale} is not supported in {@code metaModel}
	 */
	public static @NonNull IData createData(
			@NonNull DocumentV2 document,
			@NonNull IMetaModel metaModel,
			@NonNull Locale locale,
			@NonNull IResult result,
			ICustomFieldTypeFactory customFieldTypeFactory
	) {
		// throw early for wrong locale, otherwise the lazy conversion might throw an exception much later
		LowKernelApiUtils.checkLocaleIsSupported(metaModel, locale);
		return LowKernelApiUtils.createData(document, metaModel, locale, result,
				id -> new Identifier(id.getName(), id.getIndices()).toString(), customFieldTypeFactory);
	}
}

