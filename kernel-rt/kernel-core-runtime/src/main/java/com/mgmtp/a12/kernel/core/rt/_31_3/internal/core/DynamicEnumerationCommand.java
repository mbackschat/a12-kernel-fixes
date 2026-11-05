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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ErgebnisImpl;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

import lombok.NonNull;

public class DynamicEnumerationCommand extends CheckCommand {

	private final IDynEnumsIntern enumCalc;

	public DynamicEnumerationCommand(
			ValidationMode modeOrNull,
			@NonNull IMetaDataValidierung metaData,
			@NonNull IData data
	) {
		super(null, modeOrNull, metaData, data);
		this.enumCalc = metaData.getDynamicEnumerationCalculator();
	}

	public @NonNull List<Pair<String, String>> calcEnumValues(@NonNull IIdentifier field, @NonNull IData operands) {
		ErgebnisImpl validationResult = new ErgebnisImpl();

		this.validationCache.setDaten(operands);

		IMetaField metaField = metaData.getMetaField(field.getName());
		@SuppressWarnings("unchecked")
		Set<IIdentifier> ids = (Set<IIdentifier>) metaField.getValue(IMetaKeys.FIELD_SOURCE_FIELDS_FOR_DYNAMIC_ENUM, field);
		Set<RtInternalIdentifier> relevantFields = new HashSet<>();
		for (IIdentifier id : ids) {
			RtInternalIdentifier internId = EntityUtil.makeInternIdentifier(id, metaData);
			relevantFields.add(internId);
		}
		formalePruefung(relevantFields, validationResult);
		if (validationResult.getCountOf(null) > 0) {
			return Collections.emptyList();
		}

		DynamicEnumerationController controller = new DynamicEnumerationController(validationResult, this.validationCache,
				modus, metaData);
		RtInternalIdentifier fieldIntern = EntityUtil.makeInternIdentifier(field, metaData);
		List<Pair<String, String>> result = enumCalc.calcEnumerationValues(fieldIntern, controller);
		if (validationResult.getCountOf(null) > 0) {
			return Collections.emptyList();
		}
		return result;
	}
}
