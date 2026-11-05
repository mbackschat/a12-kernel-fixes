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
package com.mgmtp.a12.kernel.md.model.internal.wrapper;

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.model.header.Annotation;

import lombok.NonNull;

public class FieldTypeDefinitionWrapper implements IFieldTypeDefinition {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	private FieldTypeDefinition fieldTypeDefinition;

	public FieldTypeDefinitionWrapper(@NonNull FieldTypeDefinition fieldTypeDefinition) {
		this.fieldTypeDefinition = fieldTypeDefinition;
	}

	public @NonNull FieldTypeDefinition unwrap() {
		return fieldTypeDefinition;
	}

	@Override
	public @NonNull String getId() {
		return fieldTypeDefinition.getId();
	}

	@Override
	public @NonNull String getName() {
		return fieldTypeDefinition.getName();
	}

	@Override
	public @NonNull ILocalizedTextMap getInternalDescription() {
		return fieldTypeDefinition.getInternalDescription();
	}

	@Override
	public @NonNull ILocalizedTextMap getExternalDescription() {
		return fieldTypeDefinition.getExternalDescription();
	}

	@Override
	public Optional<String> getChangeNote() {
		return fieldTypeDefinition.getChangeNote();
	}

	@Override
	public @NonNull List<Annotation> getAnnotations() {
		return fieldTypeDefinition.getAnnotations();
	}

	@Override
	public void setAnnotations(@NonNull List<Annotation> annotations) {
		fieldTypeDefinition.setAnnotations(annotations);
	}

	@Override
	public @NonNull IFieldType getFieldType() {
		Optional<IFieldType> fieldType = FieldWrapper.getFieldType(fieldTypeDefinition.getFieldType());
		if (fieldType.isPresent()) {
			return fieldType.get();
		} else {
			throw new IllegalArgumentException(
					"Unsupported field type: " + fieldTypeDefinition.getFieldType().getClass().getName());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldTypeDefinition == null) ? 0 : fieldTypeDefinition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FieldTypeDefinitionWrapper other = (FieldTypeDefinitionWrapper) obj;
		if (fieldTypeDefinition == null) {
			if (other.fieldTypeDefinition != null) {
				return false;
			}
		} else if (!fieldTypeDefinition.equals(other.fieldTypeDefinition)) {
			return false;
		}
		return true;
	}

}
