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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization;

import static com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl.error;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DmAndSearchService;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.convertion.FieldValueConverter;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.json.DocumentV2JsonReader;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.DocumentV2XmlReader;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.simplexmlnode.XmlElement;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;
import tools.jackson.databind.node.ValueNode;

public class DocumentJsonAndXmlDeserializationInfoImpl
		implements DocumentV2JsonReader.IDeserializationInfo, DocumentV2XmlReader.IDeserializationInfo {
	public static final String UNKNOWN_ENTITY = "For the entity instance '%s', the corresponding entity was not found in the corresponding document model.";
	public static final String NOT_A_FIELD_OR_GROUP = "For the entity instance '%s', the corresponding entity is neither a field nor a group in the corresponding document model.";
	public static final String GROUP_IS_FIELD = "For the entity instance '%s', the corresponding entity is defined as a field, but was provided as a group.";

	private final @NonNull DmAndSearchService dmAndSearchService;
	private final @NonNull DocumentDeserializationConfig config;
	private final @NonNull String fullName;
	private final @NonNull List<Integer> indexesOnlyRelevantForErrorReporting;
	private final @NonNull Consumer<RankedNotification> notificationReceiver;

	public DocumentJsonAndXmlDeserializationInfoImpl(DmAndSearchService dmAndSearchService,
			@NonNull DocumentDeserializationConfig config, @NonNull Consumer<RankedNotification> notificationReceiver
	) {
		this(dmAndSearchService, "", List.of(), config, notificationReceiver);
	}

	private DocumentJsonAndXmlDeserializationInfoImpl(
			@NonNull DmAndSearchService dmAndSearchService,
			@NonNull String fullName,
			@NonNull List<Integer> indexesOnlyRelevantForErrorReporting,
			@NonNull DocumentDeserializationConfig config,
			@NonNull Consumer<RankedNotification> notificationReceiver
	) {
		this.dmAndSearchService = dmAndSearchService;
		this.fullName = fullName;
		this.indexesOnlyRelevantForErrorReporting = indexesOnlyRelevantForErrorReporting;
		this.config = config;
		this.notificationReceiver = notificationReceiver;
	}

	@Override public Optional<FieldInstanceV2> parseFieldValue(ValueNode value) {
		// special case of document id. It is like a string field but not present in the dm
		if (("/" + DocumentSerializerImpl.ID).equals(fullName)) {
			if (!value.isString()) {
				// int/float was previously allowed
				if (value.isNumber()) {
					assert false : "The document id is a number, but should be a string";
				} else {
					throw new IllegalArgumentException(
							"The value type of the document id is not supported. The id should be a string.");
				}
			}
			return Optional.of(FieldInstanceV2.ofValue(value.asString()));
		}

		String strValue = value.isNull() ? null : value.asString(); // note: we don't _need_ to stringify numbers and booleans,
		// but the old impl was like this, and it makes subsequent processing more uniform
		// from json, we could in principle skip the lookup in the dm for values of type number or boolean, but since we need
		// the dm to disambiguate string and date/time values, and for parsing date/time values, it seems less suprising and more
		// testable to unifromly lookup the field in the dm, and in particular require that _all_ fields are known in the dm
		// (not just string/date/time fields).
		return parseStringValueCommon(strValue);
	}

	@Override public Optional<FieldInstanceV2> parseFieldValue(XmlElement fieldElement) {
		if (!fieldElement.children().isEmpty()) {
			notificationReceiver.accept(
					error(String.format(GROUP_IS_FIELD, DocumentPointer.of(fullName,
							indexesOnlyRelevantForErrorReporting))));
			return Optional.empty();
		}

		String strValue = fieldElement.textContent();
		if (strValue.isEmpty() &&
				"true".equals(fieldElement.attributes().get("xsi:nil"))
		) {
			strValue = null;
		}

		// Note: our the xml-based serialization format, there is not the special case (like in json) of a "field" /id, because
		// the document id is encoded as an XML element attribute, rather than like a field.
		return parseStringValueCommon(strValue);
	}

	/**
	 * @param strValueNullable
	 * 		the string value of the field, as stored in the xml serialization format or as obtained from
	 *        {@link ValueNode#asString()} from our json serialization format (both string representations "happen" to be the same at this
	 * 		point, after normalization of the null representation).
	 */
	private Optional<FieldInstanceV2> parseStringValueCommon(String strValueNullable) {
		Optional<IElement> fieldOptional = dmAndSearchService.searchService().getByPath(fullName);
		if (fieldOptional.isPresent()) {
			if (ElementUtils.isField(fieldOptional.get())) {
				FieldValueConverter.ConversionResult conversionResult = FieldValueConverter.convertToJavaTypeV2(
						fullName, indexesOnlyRelevantForErrorReporting, strValueNullable, dmAndSearchService, notificationReceiver);
				return handleValueConversionResultFallback(conversionResult, strValueNullable);
			} else {
				notificationReceiver.accept(
						error(String.format(NOT_A_FIELD_OR_GROUP, DocumentPointer.of(fullName,
								indexesOnlyRelevantForErrorReporting))));
			}
		} else {
			// can not handle unknowns, but ignore them. We handle this case outside (and partially redundantly to)
			// convertToJavaTypeV2 because the reporting in convertToJavaTypeV2 does not support the isIgnoreUnknownEntities
			// suppression.
			if (!config.isIgnoreUnknownEntities()) {
				notificationReceiver.accept(
						error(String.format(UNKNOWN_ENTITY, DocumentPointer.of(fullName,
								indexesOnlyRelevantForErrorReporting))));
			}
		}

		return Optional.empty();
	}

	/**
	 * If parsing of a value failed ({@link FieldValueConverter.ConversionResult#success()} is false), fall back to the original
	 * unparsed string value.
	 * </p>
	 * This is done for compatibility with the previous parsing behavior, we considered removing this fallback to simplify the
	 * serialization semantics and enforce more consistent data, but decided that it seems to worth it to "clean up" the old json
	 * serialization format, and we should focus both our effort and migration effort of users on a fresh, dm-independent
	 * serialization format for a12 documents.
	 * </p>
	 * Assumes that in case of failed conversion, an error has already been reported
	 */
	private Optional<FieldInstanceV2> handleValueConversionResultFallback(FieldValueConverter.ConversionResult conversionResult,
			String strValueForQuestionableCompatibilityFallback) {
		if (conversionResult.success()) {
			return Optional.of(FieldInstanceV2.ofValue(conversionResult.convertedValueNullable()));
		} else {
			// Note: we had previously planned to remove the following fallback behavior, but decided to leave it unchanged and
			//       focus on a fresh, simpler, dm-independent serialization format instead, see comment in the javadoc of this
			//       method. _some_ considerations related to the fallback are kept below, in case we should reevaluate the
			//       "leave-as-is" decision later:
			// Some preliminary thoughts:
			// - passing the broken value through has some merit with the reasoning "we must deserialize it first so the user can fix it and an error gets reported anyway"
			// - but if users ignore the error, broken values could maybe be completely hidden unless validation is called? (_serialiation takes strings
			//   untouched, and doesn't check the basictype at all)
			// - maybe the best solution would be to check the basictype (or at least "correct basictype OR string that is parseable to correct basictype")
			//   during _serialization_ (we already run checkStructuralConsistency there). This would catch broken values early.
			// Note: parsing errors have already been reported at this point, FieldValueConverter should take care of this)
			return Optional.of(FieldInstanceV2.ofValue(strValueForQuestionableCompatibilityFallback));
		}
	}

	@Override public boolean checkIsGroup() {
		return switch (checkElementType()) {
			case GROUP -> true;
			case FIELD -> {
				// we don't condition the reporting on config.isIgnoreUnknownEntities() because the entity is not unknown, but of wrong type (how was the previuos behavior here?)
				this.notificationReceiver.accept(
						error(String.format(GROUP_IS_FIELD, DocumentPointer.of(fullName,
								indexesOnlyRelevantForErrorReporting))));

				yield false;
			}
			case INVALID -> false; // already reported by checkIsGroup
		};
	}

	@Override public ElementType checkElementType() {
		if (fullName.isEmpty()) {
			return ElementType.GROUP; // the document root is always a group
		}
		Optional<IElement> groupOptional = dmAndSearchService.searchService().getByPath(fullName);
		if (groupOptional.isEmpty()) {
			if (!config.isIgnoreUnknownEntities()) {
				this.notificationReceiver.accept(
						error(String.format(UNKNOWN_ENTITY, DocumentPointer.of(fullName,
								indexesOnlyRelevantForErrorReporting))));
			}
			return ElementType.INVALID;
		}
		return switch (groupOptional.get()) {
			case IGroup _g -> ElementType.GROUP;
			case IField _f -> ElementType.FIELD;
			default -> {
				// we don't condition the reporting on config.isIgnoreUnknownEntities() because the entity is not unknown, but of
				// wrong type
				this.notificationReceiver.accept(
						error(String.format(NOT_A_FIELD_OR_GROUP, DocumentPointer.of(fullName,
								indexesOnlyRelevantForErrorReporting))));
				yield ElementType.INVALID;
			}
		};
	}

	@Override public DocumentJsonAndXmlDeserializationInfoImpl forChild(String childName) {
		return new DocumentJsonAndXmlDeserializationInfoImpl(
				this.dmAndSearchService,
				this.fullName + "/" + childName,
				Java8Utils.appendList(this.indexesOnlyRelevantForErrorReporting, 1),
				this.config,
				this.notificationReceiver);
	}

	@Override public DocumentJsonAndXmlDeserializationInfoImpl atIndex(int repetitionIndexOnlyRelevantForErrorReporting) {
		if (this.indexesOnlyRelevantForErrorReporting.isEmpty()) {
			throw new IllegalArgumentException("Cannot set the repetition index for an empty path");
		}

		return new DocumentJsonAndXmlDeserializationInfoImpl(
				this.dmAndSearchService,
				this.fullName,
				Java8Utils.changeLast(this.indexesOnlyRelevantForErrorReporting, repetitionIndexOnlyRelevantForErrorReporting),
				this.config,
				this.notificationReceiver);
	}

	@Override public void notify(Function<String, RankedNotification> rankedNotification) {
		this.notificationReceiver.accept(
				rankedNotification.apply(DocumentPointer.of(fullName, indexesOnlyRelevantForErrorReporting).toString()));
	}

	@Override public String toString() {
		return "IDeserializationInfo for " + fullName;
	}
}
