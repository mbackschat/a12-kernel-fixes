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

package com.mgmtp.a12.kernel.md.model.a12internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.model.header.Annotation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Provides access to {@link Field} specific properties.
 * A {@link Field} is a leaf {@link Element} of a {@link DocumentModel} that describes an element
 * which can contain a value.
 * The format of this value is defined by the {@link Field}s {@link FieldType}.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Field extends Element implements IEntity, ILabeled {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return the label in different {@link Locale}s for this field. Is
	 * 		never {@code null}. If no labels were defined, the {@link ILocalizedTextMap} is
	 * 		empty.
	 * @param label
	 * 		the new label in different {@link Locale}s for this field. May not be
	 *        {@code null}. Use an empty {@link ILocalizedTextMap} to unset.
	 */
	private @NonNull ILocalizedTextMap label = new LocalizedTextMapBuilder().build();
	/**
	 * @param requirednessConfig the new requiredness configuration. Use {@code null} to set the field as non-required.
	 */
	private RequirednessConfig requirednessConfig;
	/**
	 * @return {@code true} if this field was marked as global. Otherwise {@code false}.
	 * @param isGlobal
	 * 		use {@code true} if this field was marked as global. Otherwise {@code false}.
	 */
	private boolean isGlobal;
	/**
	 * @return {@code true} if this field was marked as transient. Otherwise {@code false}.
	 * @param isTransient
	 * 		use {@code true} if this field was marked as transient. Otherwise
	 *        {@code false}.
	 */
	private boolean isTransient;
	/**
	 * @return the defined {@link FieldType} for this field. Is never {@code null}.
	 * @param fieldType
	 * 		the new {@link FieldType} for this field. May not be {@code null}.
	 */
	private @NonNull FieldType fieldType;
	/**
	 * @return the text which should appear as help to the user input. Is
	 * 		never {@code null}. If no helper texts were defined, the {@link ILocalizedTextMap} is
	 * 		empty.
	 * @param helperText
	 * 		the new text which should appear as help to the user input. May not be
	 *        {@code null}. Use an empty {@link ILocalizedTextMap} to unset.
	 */
	private @NonNull ILocalizedTextMap helperText = new LocalizedTextMapBuilder().build();
	/**
	 * @return an additional information - the concrete syntactics and semantics of the information
	 * 		is specified project specific. In ExtMP it is used e.g. for specifying the maximum
	 * 		number of pages in a scan for an attachment.
	 */
	private String additionalInfo;

	@Builder(builderClassName = "FieldBuilder")
	private Field(@NonNull String id, @NonNull String name, @NonNull ILocalizedTextMap internalDescription,
			@NonNull ILocalizedTextMap externalDescription, @NonNull List<Annotation> annotations,
			@NonNull ILocalizedTextMap label, boolean isGlobal, boolean isTransient,
			RequirednessConfig requirednessConfig, @NonNull FieldType fieldType,
			@NonNull ILocalizedTextMap helperText, String additionalInfo, String changeNote) {
		super(null, id, name, internalDescription, externalDescription, annotations, changeNote);
		this.label = label;
		this.requirednessConfig = requirednessConfig;
		this.isGlobal = isGlobal;
		this.isTransient = isTransient;
		this.fieldType = fieldType;
		this.helperText = helperText;
		this.additionalInfo = additionalInfo;
	}

	/**
	 * @return see {@link IField#getRequirednessConfig()}.
	 */
	public Optional<RequirednessConfig> getRequirednessConfig() {
		return Optional.ofNullable(requirednessConfig);
	}

	/**
	 * @return an additional information - the concrete syntactics and semantics of the information
	 * 		is specified project specific. In ExtMP it is used e.g. for specifying the maximum
	 * 		number of pages in a scan for an attachment.
	 */
	public Optional<String> getAdditionalInfo() {
		return Optional.ofNullable(additionalInfo);
	}

	/**
	 * See {@link IField#getEffectiveType()}.
	 */
	public Optional<FieldType> getEffectiveType() {
		if (fieldType instanceof TypeDefType typeDefType) {
			return typeDefType.getTypeDefinition().map(FieldTypeDefinition::getFieldType);
		} else {
			return Optional.of(fieldType);
		}
	}

	public static class FieldBuilder {
		private ILocalizedTextMap internalDescription = new LocalizedTextMapBuilder().build(); // in order to make FieldBuilder.internalDescription() optional
		private ILocalizedTextMap externalDescription = new LocalizedTextMapBuilder().build(); // in order to make FieldBuilder.externalDescription() optional
		private List<Annotation> annotations = new ArrayList<>(); // in order to make FieldBuilder.annotations() optional
		private ILocalizedTextMap label = new LocalizedTextMapBuilder().build(); // in order to make FieldBuilder.label() optional
		private ILocalizedTextMap helperText = new LocalizedTextMapBuilder().build(); // in order to make FieldBuilder.helperText() optional

		public String getId() {
			return this.id;
		}
	}

}