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
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeCheckError;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeValidationParam;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorKeys;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorUtil;

import lombok.NonNull;

/**
 * Prüft die Bundesfinanzamtsnummer.
 * Die Prüfung der Bundesfinanzamtsnummer besteht aus folgenden Teilen:
 * <ul>
 * <li>Ist die Bundesfinanzamtsnummer formal gültig?</li>
 * <li>Gibt es ein Bundesfinanzamt mit dieser Nummer?</li>
 * </ul>
 * Bei der formalen Prüfung wird geprüft, ob es sich um eine vierstellige Zahl handelt. Falls die
 * Bundesfinanzamtsnummer nicht dem regulären Ausdruck
 * <code>[0-9]{4}</code>
 * entspricht handelt es sich um einen formalen Fehler.
 */
public class BundesfinanzamtsnummerVerifierImpl implements ICustomFieldValidator {

	private static final Pattern VALID_BUFANR = Pattern.compile("[0-9]{4}");

	private final @NonNull IFadat fadat;

	public BundesfinanzamtsnummerVerifierImpl(@NonNull IFadat fadat) {
		this.fadat = fadat;
	}

	/**
	 * Prüft die angegebene Bundesfinanzamtsnummer. Als zusätzlicher Parameter werden die
	 * definierenden Daten der Steuernummerprüfung benötigt. Wird keiner übergeben, wird eine
	 * {@link IllegalArgumentException} geworfen.
	 */
	@Override
	public @NonNull Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String bundesfinanzamtsnummer,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		CustomFieldTypeErrorEnum error = checkBufaNr(bundesfinanzamtsnummer);
		return CustomFieldTypeErrorUtil.getResult(error, valParam.getErrorMsgLocale(),
				CustomFieldTypeErrorKeys.UNGUELTIGE_BUFANR);
	}

	/**
	 * Prüft die angegebene Bundesfinanzamtsnummer und liefert den Key der passenden Fehlermeldung
	 * bzw. <code>null</code>, wenn die Bundesfinanzamtsnummer gültig ist.
	 */
	private CustomFieldTypeErrorEnum checkBufaNr(String bundesfinanzamtsnummer) {
		if (!istFormalGueltig(bundesfinanzamtsnummer)) {
			return CustomFieldTypeErrorEnum.BUFANR_FORMALER_FEHLER;
		}
		// Integer.parseInt is ok, because formally correct
		if (!FadatLookupUtil.existsFederalTaxOfficeNumber(fadat, Integer.parseInt(bundesfinanzamtsnummer))) {
			return CustomFieldTypeErrorEnum.BUFANR_UNGUELTIGE_FINANZAMTSNR;
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	/**
	 * Ist die Bundesfinanzamtsnummer formal gültig?.
	 */
	private boolean istFormalGueltig(final String bufaNr) {
		return VALID_BUFANR.matcher(bufaNr).matches();
	}
}
