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
import java.util.regex.Matcher;
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
 * Mit dieser Klasse wird das Straf-Aktenzeichen geprüft.
 * Die Prüfung des Straf-Aktenzeichens besteht aus folgenden Teilen:
 * <ul>
 * <li>Formale Prüfung</li>
 * <li>Gültigkeitsprüfung</li>
 * </ul>
 * <p>
 * Bei der formalen Prüfung wird geprüft, ob das Straf-Aktenzeichen dem folgenden regulären
 * Ausdruck entspricht:
 * <code>[0-9]{4}D[0-9]{4}[0-9]{7}[0-9]{1}</code>
 * </p>
 * <p>
 * Die ersten vier Zeichen des Straf-Aktenzeichens bilden die Bundesfinanzamtsnummer.
 * </p>
 * <p>
 * Bei der Gültigkeitsprüfung wird die Korrektheit der Prüfziffer (letzte Ziffer) geprüft.
 * </p>
 */
public class StrafAzVerifierImpl implements ICustomFieldValidator {

	private static final Pattern VALID_STRAFAZ = Pattern.compile("([0-9]{4})D[0-9]{4}[0-9]{7}[0-9]{1}");

	private final @NonNull IFadat fadat;

	public StrafAzVerifierImpl(@NonNull IFadat fadat) {
		this.fadat = fadat;
	}

	@Override
	public Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String strafaz,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue
	) {
		CustomFieldTypeErrorEnum error = checkStrafAz(strafaz);
		return CustomFieldTypeErrorUtil.getResult(error, valParam.getErrorMsgLocale(),
				CustomFieldTypeErrorKeys.UNGUELTIGES_STRAFAZ);
	}

	/**
	 * Prueft das angegebene Straf-Aktenzeichens und liefert den Key der passenden Fehlermeldung
	 * bzw. <code>null</code>, wenn das Straf-Aktenzeichen gueltig ist.
	 */
	private CustomFieldTypeErrorEnum checkStrafAz(String strafaz) {
		Matcher matcher = VALID_STRAFAZ.matcher(strafaz);
		if (!matcher.matches()) {
			return CustomFieldTypeErrorEnum.STRAFAZ_FORMAT_FEHLER;
		}

		int bundeslandNr = Integer.parseInt(matcher.group(1));
		if (!FadatLookupUtil.existsFederalTaxOfficeNumber(fadat, bundeslandNr)) {
			return CustomFieldTypeErrorEnum.STRAFAZ_FINANZAMT_FEHLER;
		}

		if (!istPruefzifferRichtig(strafaz)) {
			return CustomFieldTypeErrorEnum.STRAFAZ_PRUEFZIFFER_FEHLER;
		}
		return CustomFieldTypeErrorEnum.KEIN_FEHLER;
	}

	private boolean istPruefzifferRichtig(String strafaz) {
		int buFaNr = Integer.parseInt(strafaz.substring(0, 4));
		int jahr = Integer.parseInt(strafaz.substring(5, 9));
		long laufendeNummer = Long.parseLong(strafaz.substring(9, 16));
		int pruefZiffer = Integer.parseInt(strafaz.substring(16, 17));

		return generierePruefziffer(buFaNr, jahr, laufendeNummer) == pruefZiffer;
	}

	/**
	 * Die Methode generiert die Prüfziffer für das BuStra/Steufa Ordnungskriterium.
	 *
	 * @param buFaNr
	 * 		die 4. stellige Nummer des Finanzamts
	 * @param jahr
	 * 		das 4. stellige Jahr
	 * @param laufendeNummer
	 * 		die max 7. stellige laufende Nummer
	 * @return die Prüfziffer
	 */
	private static int generierePruefziffer(int buFaNr, int jahr, long laufendeNummer) {
		long l = buFaNr * 10000L + jahr;
		l = l * 10000000L + laufendeNummer;
		l = l * 298765432L;

		int p = (int) (l % 11);
		p = 11 - p;
		p = p % 10;
		return p;
	}
}
