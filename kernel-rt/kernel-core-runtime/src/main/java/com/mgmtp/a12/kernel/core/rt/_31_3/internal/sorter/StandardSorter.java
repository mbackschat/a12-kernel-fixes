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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.sorter;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.CheckCommand;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DataGroupNode;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult.ErrorType;

/**
 * In this class is defined how to sort the given data.
 */
public class StandardSorter extends AbstractSorter implements ISorter {

	public StandardSorter(IMetaDataValidierung metaData, ICustomFieldTypeFactory customFieldTypeFactory, Locale locale) {
		super(metaData, customFieldTypeFactory, locale);
	}

	@Override
	protected Map<String, List<Pair<String, SortOrder>>> getSortInfo() {
		return metaData.getSortInfo();
	}

	@Override
	protected boolean shouldRunSort(Map<String, List<Pair<String, SortOrder>>> sortInfo, IData data, IResult formalErrors) {
		// is there any sort information?
		if (sortInfo == null || sortInfo.isEmpty()) {
			return false;
		}
		// formal check
		ValidationMode mode = new ValidationMode();
		mode.setCustomFieldTypeFactory(customFieldTypeFactory);
		mode.setLanguage(locale);
		CheckCommand cc = new CheckCommand(null, mode, metaData, data);
		cc.formalePruefungMitDaten(data, null, formalErrors);
		return (formalErrors.getCountOf(ErrorType.OMISSION_ERROR) + formalErrors.getCountOf(
				ErrorType.VALUE_ERROR)) == 0;
	}

	@Override
	protected void updateGroupIndicesAfterSorting(Entry<String, List<DataGroupNode>> groupEntry) {
		// group indices will be set ascending
		updateGroupIndicesAscending(groupEntry.getValue());
	}

}
