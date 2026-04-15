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

package com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.verifier;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeConversionResult;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl.FADATData;

import lombok.NonNull;

abstract class AbstractFadatRelatedCustomFieldType implements ICustomFieldValidator {

	protected final IFadat givenFADAT;

	protected AbstractFadatRelatedCustomFieldType(IFadat fadat) {
		this.givenFADAT = fadat;
	}

	static class ParamsProvider {
		private final IFadat fadatData;

		ParamsProvider(IFadat givenFADAT, Map<String, Object> configData) {
			if (givenFADAT != null) {
				this.fadatData = givenFADAT;
			} else {
				configData = configData != null ? configData : Collections.emptyMap();
				Object dd = configData.get("DEF_DATEN_STEUERNUMMER_KEY");
				if (!(dd instanceof IGermanTaxNumberDefiningData)) {
					this.fadatData = FADATData.createFADATData(configData);
				} else {
					// old usage with IGermanTaxNumberDefiningData passed by project, already containing FADAT
					this.fadatData = null;
				}
			}
		}

		IFadat fadatData() {
			return fadatData;
		}
	}

	// =========================== VALIDATE ==========================
	protected abstract Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String value,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue,
			@NonNull ParamsProvider pp
	);

	@Override
	public final @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String value,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		return validate(value, valParam, isDisplayValue, Collections.emptyMap());
	}

	// A12K-3589 remove this SuppressWarnings
	@SuppressWarnings("removal")
	@Override
	public final @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String value,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue,
			@NonNull Map<String, Object> configData
	) {
		ParamsProvider pp = new ParamsProvider(givenFADAT, configData);
		return validate(value, valParam, isDisplayValue, pp);
	}

	// =========================== convertDisplay2Internal ==========================
	protected abstract ICustomFieldTypeConversionResult convertDisplay2Internal(String displayValue,
			ParamsProvider pp);

	// A12K-3589 remove this SuppressWarnings
	@SuppressWarnings("removal")
	@NonNull @Override
	public final ICustomFieldTypeConversionResult convertDisplay2Internal(@NonNull String displayValue,
			@NonNull Map<String, Object> configData) {
		ParamsProvider pp = new ParamsProvider(givenFADAT, configData);
		return convertDisplay2Internal(displayValue, pp);
	}

	// =========================== convertXml2Display ==========================
	protected abstract ICustomFieldTypeConversionResult convertInternal2Display(String internalValue,
			ParamsProvider pp);

	// A12K-3589 remove this SuppressWarnings
	@SuppressWarnings("removal")
	@NonNull @Override
	public final ICustomFieldTypeConversionResult convertInternal2Display(@NonNull String internalValue,
			@NonNull Map<String, Object> configData) {
		ParamsProvider pp = new ParamsProvider(givenFADAT, configData);
		return convertInternal2Display(internalValue, pp);
	}

	// =========================== Utility ==========================

	protected final boolean existsFederalTaxOfficeNumber(Integer federalTaxOfficeNumber, ParamsProvider pp) {
		if (!pp.fadatData().getFederalTaxOfficeNumbers().isEmpty()) {
			return pp.fadatData().getFederalTaxOfficeNumbers().contains(federalTaxOfficeNumber);
		} else {
			throw new IllegalArgumentException("Die notwendigen FADAT-Informationen liegen nicht vor (Integrationsfehler).");
		}
	}

	protected final boolean existsEWAzTestFederalTaxOfficeNumber(Integer federalTaxOfficeNumber, ParamsProvider pp) {
		return pp.fadatData().getEWAzTestFederalTaxOfficeNumber().contains(federalTaxOfficeNumber);
	}
}
