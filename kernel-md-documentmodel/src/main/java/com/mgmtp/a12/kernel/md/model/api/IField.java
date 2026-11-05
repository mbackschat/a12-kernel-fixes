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
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ITypeDefType;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Provides access to {@link IField} specific properties.
 * A {@link IField} is a leaf {@link IElement} of a {@link IDocumentModel} that describes an element
 * containing a value.
 * The format of this value is defined by the {@link IFieldType}.
 */
@OnlyForUsage
public interface IField extends IElement, IEntity, Serializable {

	/**
	 * @return the {@link IFieldType} of this {@link IField}. May not be {@code null}.
	 */
	@NonNull IFieldType getFieldType();

	/**
	 * See also {@link IRequirednessConfig#getMode()}.
	 *
	 * @return an instance of {@link IRequirednessConfig} if this field is required (no matter the configuration).
	 */
	Optional<IRequirednessConfig> getRequirednessConfig();

	/**
	 * Specifies whether the field is global or not.
	 * <p>
	 * Global fields should always be passed to a partial validation in addition to the usual
	 * relevant entities. The caller of the validation is responsible for passing the global fields.
	 * <p>
	 * <b>Note:</b> This information is not used in the A12 form engine, see the respective
	 * description in MM_Field in the meta model.
	 *
	 * @return {@code true} if the property <i>global</i> was set to {@code true}, otherwise
	 *        {@code false}.
	 */
	boolean isGlobal();

	/**
	 * Specifies whether the field is transient or not.
	 * <p>
	 * Transient fields are not meant to be persisted (i.e. json format).
	 * If transient fields are referenced by rules, they should be passed to the validation, if they
	 * are calculated, they should be passed to the calculation.
	 *
	 * @return {@code true} if the property <i>transient</i> was set to {@code true}, otherwise
	 *        {@code false}.
	 */
	boolean isTransient();

	/**
	 * @return the text which should appear as help to the user input. Is
	 * 		never {@code null}. If no helper texts were defined, the {@link ILocalizedTextMap} is
	 * 		empty.
	 */
	@NonNull ILocalizedTextMap getHelperText();

	/**
	 * @return an additional information - the concrete syntactics and semantics of the information
	 * 		is specified project specific. In ExtMP it is used e.g. for specifying the maximum
	 * 		number of pages in a scan for an attachment.
	 */
	Optional<String> getAdditionalInfo();

	/**
	 * @return the effective {@link IFieldType} resolving the actual type if this {@link IField}
	 * 		is specified with a {@link ITypeDefType}.
	 * 		<p>
	 * 		<b>Note:</b> This method returns {@link Optional#empty()} in case the {@link IField}
	 * 		is specified with {@link ITypeDefType} <b>and</b> the corresponding
	 *        {@link IFieldTypeDefinition} could not be resolved.
	 */
	default Optional<IFieldType> getEffectiveType() {
		if (getFieldType() instanceof ITypeDefType tdt) {
			return tdt.getTypeDefinition().map(IFieldTypeDefinition::getFieldType);
		} else {
			return Optional.of(getFieldType());
		}
	}
}
