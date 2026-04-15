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
import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorKeys;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorUtil;

import lombok.NonNull;

/**
 * Prüft die Bundessteuernummer.
 * <p>
 * Der Prüfungesalgorithmus für Bundessteuernummer ist weitgehend identisch mit dem
 * Prüfungesalgorithmus für Steuernummern in Bayern. Damit hier die Implementierung der
 * Steuernummerprüfung verwendet werden kann, wird der Bundessteuernummer in Oberflächenformat der
 * String "@91" angehängt. Diese Endung zeigt der Steuernummerprüfung, dass es sich um eine
 * Steuernummer in Bayern handelt und der entsprechende Prüfalgorithmus verwendet werden soll.
 * <p>
 * Die Prüfung der BZSt-Steuernummer im Elster-Format besteht aus den folgenden Teilen:<br>
 * <ul>
 * <li>Ist die BZSt-Steuernummer formal gültig?</li>
 * <li>Liefert das Prüfziffernberechnungsverfahren die passende Prüfziffer?</li>
 * </ul>
 * </p>
 * <p>
 * Bei der formalen Prüfung muss die BZSt-Steuernummer dem folgenden regulären Ausdruck entsprechen:
 * <code>91160[0-9]{8}</code>
 * </p>
 * <p>
 * Abschließend wird die BZSt-Steuernummer noch anhand eines Prüfziffernberechnundsverfahrens
 * geprüft.
 * </p>
 */
public class BundessteuernummerVerifierImpl extends SteuernummerVerifierImpl {

	// Formale Prüfung: 13-stellige Zahl, die mit den Ziffern 91160 beginnt.
	private static final Pattern VALID_BUSTNR = Pattern.compile("(91)(16)0[0-9]{8}");

	public BundessteuernummerVerifierImpl(IFadat fadat) {
		super(fadat);
	}

	/**
	 * Überprüft die übergebene Bundessteuernummer.<br>
	 * Als zusätzlicher Parameter werden die definierenden Daten der Steuernummerprüfung benötigt.
	 * Wird keiner übergeben, wird eine {@link IllegalArgumentException} geworfen.
	 */
	@Override
	protected Optional<ICustomFieldTypeCheckError> validate(
			@NonNull String value,
			@NonNull ICustomFieldTypeValidationParam valParam,
			boolean isDisplayValue,
			@NonNull ParamsProvider pp
	) {
		Optional<ICustomFieldTypeCheckError> result = super.validate(value, valParam, isDisplayValue, pp);
		if (result.isPresent()) {
			return CustomFieldTypeErrorUtil.getResult(CustomFieldTypeErrorEnum.BUNDESSTNR_UNGUELTIG, valParam.getErrorMsgLocale(),
					CustomFieldTypeErrorKeys.UNGUELTIGE_BUSTNR);
		}
		return result;
	}

	@Override
	protected Matcher getMatcher(final String xmlSteuernummer) {
		return VALID_BUSTNR.matcher(xmlSteuernummer);
	}

	@Override
	boolean istBundesfinanzamtsnummerGueltig(final int bundesfinanzamtsnummer, ParamsProvider pp) {
		return true;
	}

}
