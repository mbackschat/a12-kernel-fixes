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
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.LocaleUtils;

import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.NonNull;

public class StringTypeWrapper extends FieldTypeWrapper<StringType> implements IStringType {

	public static class HintListWrapper implements IStringType.IHintList {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		private StringType.HintList hintList;

		HintListWrapper(StringType.HintList hintList) {
			this.hintList = hintList;
		}

		@Override public Locale getLocale() {
			return LocaleUtils.toLocale(hintList.getLocale());
		}

		@Override
		public List<String> getValues() {
			return hintList.getValues();
		}
	}

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	public StringTypeWrapper(@NonNull StringType stringType) {
		super(stringType);
	}

	@Override
	public @NonNull List<IHintList> getHintList() {
		return fieldType.getHintList().stream()
				.map(HintListWrapper::new)
				.collect(Collectors.toList());
	}

	@Override
	public Optional<Integer> getMinLength() {
		return fieldType.getMinLength();
	}

	@Override
	public Optional<Integer> getMaxLength() {
		return fieldType.getMaxLength();
	}

	@Override
	public boolean isLineBreaksPermitted() {
		return fieldType.isLineBreaksPermitted();
	}

	@Override
	public boolean isNoValueValidation() {
		return fieldType.isNoValueValidation();
	}

	@Override
	public Optional<String> getPattern() {
		return fieldType.getPattern();
	}

	@Override
	public @NonNull ILocalizedTextMap getErrorMessage() {
		return fieldType.getErrorMessage();
	}

	@Override
	public boolean isAlphabeticalSorting() {
		return fieldType.isAlphabeticalSorting();
	}

}
