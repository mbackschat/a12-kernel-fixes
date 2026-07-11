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

import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.BooleanType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.ConfirmType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.CustomFieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateFragmentType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateRangeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateTimeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.NumberType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TimeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.IRequirednessConfig;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.BooleanTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.ConfirmTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.CustomFieldTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.DateFragmentTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.DateRangeTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.DateTimeTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.DateTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.EnumerationTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.NumberTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.StringTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.TimeTypeWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes.TypeDefTypeWrapper;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.NonNull;

public class FieldWrapper extends ElementWrapper implements IField {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	public static Optional<IFieldType> getFieldType(@NonNull FieldType fieldType) {
		IFieldType ft = switch (fieldType) {
			case NumberType numberType -> new NumberTypeWrapper(numberType);
			case DateTimeType dateTimeType -> new DateTimeTypeWrapper(dateTimeType);
			case TimeType timeType -> new TimeTypeWrapper(timeType);
			case DateType dateType -> new DateTypeWrapper(dateType);
			case DateFragmentType dateFragmentType -> new DateFragmentTypeWrapper(dateFragmentType);
			case DateRangeType dateRangeType -> new DateRangeTypeWrapper(dateRangeType);
			case BooleanType booleanType -> new BooleanTypeWrapper(booleanType);
			case ConfirmType confirmType -> new ConfirmTypeWrapper(confirmType);
			case EnumerationType enumerationType -> new EnumerationTypeWrapper(enumerationType);
			case TypeDefType typeDefType -> new TypeDefTypeWrapper(typeDefType);
			case StringType stringType -> new StringTypeWrapper(stringType);
			case CustomFieldType customFieldType -> new CustomFieldTypeWrapper(customFieldType);
			default -> null;
		};
		return Optional.ofNullable(ft);
	}

	private Field field;

	public FieldWrapper(@NonNull Field field) {
		super(field);
		this.field = field;
	}

	@Override
	public @NonNull ILocalizedTextMap getLabel() {
		return field.getLabel();
	}

	@Override
	public @NonNull IFieldType getFieldType() {
		Optional<IFieldType> fieldType = getFieldType(field.getFieldType());
		if (fieldType.isPresent()) {
			return fieldType.get();
		} else {
			String path = ElementUtils.getPath(this);
			String fieldTypeClassName = field.getFieldType().getClass().getName();
			throw new IllegalArgumentException(
					String.format("Field type '%s' of field '%s' is not supported.", fieldTypeClassName, path));
		}
	}

	@Override public Optional<IRequirednessConfig> getRequirednessConfig() {
		return field.getRequirednessConfig().map(RequirednessConfigWrapper::new);
	}

	@Override
	public boolean isGlobal() {
		return field.isGlobal();
	}

	@Override
	public boolean isTransient() {
		return field.isTransient();
	}

	@Override
	public @NonNull ILocalizedTextMap getHelperText() {
		return field.getHelperText();
	}

	@Override
	public Optional<String> getAdditionalInfo() {
		return field.getAdditionalInfo();
	}

	public @NonNull Field getField() {
		return field;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FieldWrapper other = (FieldWrapper) obj;
		if (field == null) {
			if (other.field != null) {
				return false;
			}
		} else if (!field.equals(other.field)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return field.toString();
	}
}
