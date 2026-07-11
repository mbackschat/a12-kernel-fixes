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
package com.mgmtp.a12.kernel.md.model.internal.wrapper.fieldtypes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType.EnumCategory;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType.EnumValue;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.NonNull;

public class EnumerationTypeWrapper extends FieldTypeWrapper<EnumerationType> implements IEnumerationType {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	public static class EnumValueWrapper implements IEnumValue {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		private EnumValue enumValue;

		EnumValueWrapper(EnumValue enumValue) {
			this.enumValue = enumValue;
		}

		@Override
		public @NonNull String getValue() {
			return enumValue.getValue();
		}

		@Override
		public @NonNull ILocalizedTextMap getLabel() {
			return enumValue.getLabel();
		}

	}

	public static class EnumCategoryWrapper implements IEnumCategory {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		private EnumCategory enumCategory;

		EnumCategoryWrapper(EnumCategory enumCategory) {
			this.enumCategory = enumCategory;
		}

		@Override
		public @NonNull String getName() {
			return enumCategory.getName();
		}

		@Override
		public Optional<String> getDescription() {
			return enumCategory.getDescription();
		}

		@Override
		public @NonNull List<String> getValues() {
			return enumCategory.getValues();
		}
	}

	public EnumerationTypeWrapper(@NonNull EnumerationType enumerationType) {
		super(enumerationType);
	}

	@Override
	public @NonNull List<IEnumValue> getValues() {
		return fieldType.getValues().stream()
				.map(EnumValueWrapper::new)
				.collect(Collectors.toList());
	}

	@Override
	public @NonNull ILocalizedTextMap getErrorMessage() {
		return fieldType.getErrorMessage();
	}

	@Override
	public @NonNull List<IEnumCategory> getCategories() {
		return fieldType.getCategories().stream()
				.map(EnumCategoryWrapper::new)
				.collect(Collectors.toList());
	}

	@Override
	public boolean isAlphabeticalSorting() {
		return fieldType.isAlphabeticalSorting();
	}

	@Override
	public Optional<String> getsValue() {
		return fieldType.getsValue();
	}

}
