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

package com.mgmtp.a12.kernel.md.rt.internal.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaGroup;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV1V2ConverterImpl;
import com.mgmtp.a12.kernel.md.rt.internal.service.conversion.ConversionService;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion;

import lombok.NonNull;

/**
 * Converts a provided {@link DocumentV2} to {@link IData}.
 * <p>
 * The conversion is done lazy when getValue is called..
 */
public class ValidationData implements IData {
	public static final String REPEATABLE_GROUPINSTANCE_PLACEHOLDER = "NonEmptyString";
	/**
	 * absent fields, field with empty or null value, and instances of non-repeatable groups have the value {@code ""}, both
	 * as the internal String representation and as value for display. There used to be a seemingly arbitrary mix of {@code null}
	 * and {@code ""}, but it is probably ok, and less complex, to always use the same value.
	 */
	public static final String VALUE_FOR_ABSENT_AND_EMPTY = "";

	private final DocumentV2 documentV2;

	@Deprecated(since = "30.2.0")
	private final IDocument originalIDocumentIfExists;
	// needed to be able to pass results from conversion when getting the value of a specific field instance
	private final IResult result;
	private final IMetaModel metaModel;
	private final ConversionService conversionService;
	private final IIdentifierToString identifierToStringFunc;

	public ValidationData(@NonNull DocumentV2 documentV2, @NonNull IMetaModel metaModel, @NonNull Locale locale,
			@NonNull IResult result, @NonNull IIdentifierToString identifierToStringFunc,
			ICustomFieldTypeFactory customFieldTypeFactory) {
		this.documentV2 = documentV2;
		this.result = result;
		this.metaModel = metaModel;
		this.conversionService = new ConversionService(Objects.requireNonNull(locale), customFieldTypeFactory);
		this.identifierToStringFunc = identifierToStringFunc;
		this.originalIDocumentIfExists = null;
	}

	/**
	 * @deprecated IDocument should no longer be used, use
	 *        {@link #ValidationData(DocumentV2, IMetaModel, Locale, IResult, IIdentifierToString)} instead.
	 */
	@Deprecated(since = "30.2.0")
	public ValidationData(@NonNull IDocument documentV1, @NonNull IMetaModel metaModel, @NonNull Locale locale,
			@NonNull IResult result, @NonNull IIdentifierToString identifierToStringFunc,
			ICustomFieldTypeFactory customFieldTypeFactory) {
		this.originalIDocumentIfExists = documentV1;
		this.documentV2 = DocumentV1V2ConverterImpl.getInstance().ofDocumentV1(documentV1);
		this.result = result;
		this.metaModel = metaModel;
		this.conversionService = new ConversionService(Objects.requireNonNull(locale), customFieldTypeFactory);
		this.identifierToStringFunc = identifierToStringFunc;
	}

	@Override public String getValue(IIdentifier id) {
		DocumentPointer pointer = DocumentPointerConverter.convertToDocumentPointer(id);
		IMetaField metaField = (IMetaField) metaModel.getValue(IMetaKeys.MODEL_META_FIELD, id.getName());
		IMetaGroup metaGroup = (IMetaGroup) metaModel.getValue(IMetaKeys.MODEL_META_GROUP, id.getName());
		FieldInstanceV2 field = pointer.repetitionIndexes().getLast() == 1 // field lookup only allowed for pointers with last rep 1
				? documentV2.field(pointer) : null;                        // (outside IData, this should not normally happen)
		GroupInstanceV2 group = documentV2.group(pointer);

		if (field != null) {
			if (metaField == null) {
				throw new IllegalArgumentException(
						"Data contained a field for path " + id.getName() + ", which is not defined in the model.");
				// an additional reason for why we must reject this case is that the model might contain a group for the given path,
				// and in that case the caller could misinterpret a non-empty return value as the placeholder value for a present
				// repeatable group.
			}
			if (field.value() == null || "".equals(field.value())) {
				return VALUE_FOR_ABSENT_AND_EMPTY;
			} else if (field.value() instanceof String stringValue) {
				// we don't need to convert the value
				return stringValue;
			} else {
				final ResultForConversion conversionResult = new ResultForConversion(result, id);
				return metaField.convertFromBasicType(BasicTypeV1V2Conversion.basicTypeV2ToV1(
						field.value()), null, conversionResult);
			}
		}

		if (group != null) {
			if (metaGroup == null) {
				throw new IllegalArgumentException(
						"Data contained a group for path " + id.getName() + ", which is not defined in the model.");
				// an additional reason for why we must reject this case is that the model might contain a field for the given path,
				// and in that case the caller could misinterpret a returned REPEATABLE_GROUP value as a field value
			}
			Object maxIndexObj = metaGroup.getValue(IMetaKeys.GROUP_MAX_INDEX);
			boolean isRepeatableByItself = maxIndexObj != null && (Long) maxIndexObj > 1;
			// a repeating group is explicitly marked with a value
			return isRepeatableByItself ? REPEATABLE_GROUPINSTANCE_PLACEHOLDER : VALUE_FOR_ABSENT_AND_EMPTY;
		}
		return VALUE_FOR_ABSENT_AND_EMPTY;
	}

	@Override public String getValueForDisplay(IIdentifier id) {
		DocumentPointer pointer = DocumentPointerConverter.convertToDocumentPointer(id);

		FieldInstanceV2 field = pointer.repetitionIndexes().getLast() == 1 // field lookup only allowed for pointers with last rep 1
				? documentV2.field(pointer) : null;                        // (outside IData, this should not normally happen)
		if (field != null) {
			if (field.value() == null || "".equals(field.value())) {
				// we don't need to convert the value
				return VALUE_FOR_ABSENT_AND_EMPTY;
			} else {
				final IMetaField metaField = (IMetaField) metaModel.getValue(IMetaKeys.MODEL_META_FIELD, id.getName());
				return conversionService.convertToUI(BasicTypeV1V2Conversion.basicTypeV2ToV1(
						field.value()), metaField);
			}
		}

		// a group display value is also always null
		return VALUE_FOR_ABSENT_AND_EMPTY;
	}

	@NonNull @Override public Iterator<IIdentifier> iterator() {
		ArrayList<IIdentifier> pointers = new ArrayList<>();
		IDocumentV2Visitor visitor = new IDocumentV2Visitor() {
			@Override public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
				DescendType descendType = IDocumentV2Visitor.super.visitGroup(pointerRelativeToBase, group);

				// ignore root as it is not explicitly present in IData
				if (pointerRelativeToBase.size() == 0) {
					return descendType;
				}

				pointers.add(DocumentPointerConverter.convertToIIdentifier(pointerRelativeToBase));
				return descendType;
			}

			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field) {
				IDocumentV2Visitor.super.visitField(pointerRelativeToBase, field);

				pointers.add(DocumentPointerConverter.convertToIIdentifier(pointerRelativeToBase));
			}
		};
		documentV2.traverse(visitor);

		return pointers.iterator();
	}

	@Override public String identifierToString(IIdentifier id) {
		return this.identifierToStringFunc.identifierToString(id);
	}

	@Override public void addValue(IIdentifier id, String s) {
		throw new UnsupportedOperationException("This IData is not modifiable.");
	}

	public DocumentV2 getInternalDocumentV2() {
		return documentV2;
	}

	/**
	 * If this ValidationData was created from an IDocument, return this IDocument; otherwise null.
	 *
	 * @deprecated IDocument should no longer be used, use {@link #getInternalDocumentV2()} instead.
	 */
	@Deprecated(since = "30.2.0")
	public IDocument getOriginalIDocumentIfExists() {
		return originalIDocumentIfExists;
	}
}
