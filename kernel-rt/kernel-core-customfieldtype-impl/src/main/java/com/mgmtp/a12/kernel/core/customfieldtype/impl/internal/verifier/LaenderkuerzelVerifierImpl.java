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

import java.util.Optional;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.impl.GermanTaxNumberDefiningData;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorKeys;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorUtil;

import lombok.NonNull;

/**
 * Prüft das Länderkürzel.
 * Es wird geprüft, ob es ein Bundesland mit diesem Länderkürzel gibt. Dazu wird die Methode
 * {@link IGermanTaxNumberDefiningData#existsFederalStateCode} der Schnittstelle verwendet. Die
 * Schnittstelle
 * {@link IGermanTaxNumberDefiningData} wird vom aufrufenden System implementiert.
 */
public class LaenderkuerzelVerifierImpl implements ICustomFieldValidator {

	public LaenderkuerzelVerifierImpl() {
	}

	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String laenderkuerzel,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		CustomFieldTypeErrorEnum error = checkLaenderkuerzel(laenderkuerzel, GermanTaxNumberDefiningData.getInstance());
		return CustomFieldTypeErrorUtil.getResult(error, valParam.getErrorMsgLocale(),
				CustomFieldTypeErrorKeys.UNGUELTIGES_LAENDERKUERZEL);
	}

	/**
	 * Prüft das angegebene Länderkürzel und liefert den Key der passenden Fehlermeldung
	 * bzw. <code>null</code>, wenn das Länderkürzel gültig ist.
	 */
	private CustomFieldTypeErrorEnum checkLaenderkuerzel(String laenderkuerzel, IGermanTaxNumberDefiningData defdaten) {
		if (defdaten.existsFederalStateCode(laenderkuerzel)) {
			return CustomFieldTypeErrorEnum.KEIN_FEHLER;
		}
		return CustomFieldTypeErrorEnum.LAENDERKUERZEL_UNGUELTIG;
	}
}
