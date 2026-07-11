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

import java.util.function.IntPredicate;

import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorEnum;
import com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.error.CustomFieldTypeErrorKeys;

import lombok.NonNull;

/**
 * Prüft die Steuernummer.
 * Die Prüfung der Steuernummer im Elster-Format besteht aus den folgenden Teilen:
 * <ul>
 * <li>Ist die Steuernummer formal gültig?</li>
 * <li>Existiert die in der Steuernummer enthaltene Bundesfinanzamtsnummer?</li>
 * <li>Ist die in der Steuernummer enthaltene Bezirksnummer als Zahl ungleich 0?</li>
 * <li>Ist kein bundeslandspezifisches Negativmuster erfüllt?</li>
 * <li>Ist die Bezirksnummer ungleich 0000, 0998, 0999 für NRW bzw. ungleich 000, 998, 999 sonst
 * (wurde als bundeslandspezifisches Negativmuster implementiert)?</li>
 * <li>Liefert das Prüfziffernberechnungsverfahren die passende Prüfziffer?</li>
 * </ul>
 *
 * <p>
 * Bei der formalen Prüfung muss die Steuernummer dem folgenden regulären Ausdruck entsprechen:
 * <code>[0-9]{4}0[0-9]{8}</code>
 * </p>
 *
 * <p>
 * Die Bezirksnummer beginnt mit der sechsten Stelle der Steuernummer. Die Anzahl der Stellen der
 * Bezirksnummer ist landesspezifisch und kann über die Methode
 * {@link com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberFormatting#getNumPositionsDistrict} ermittelt. Eine Implementierung der
 * Schnittstelle
 * {@link com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberFormatting} bekommt man wiederum über die Methode
 * {@link com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData#getFormatting} zur Bundeslandnummer. Die ersten beiden
 * Stellen der
 * Steuernummer bilden die sogenannte Bundeslandnummer.
 * </p>
 *
 * <p>
 * Anschließend wird geprüft, dass kein landesspezifisches Negativmuster erfüllt ist. Die Methode
 * {@link com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData#getNegativePattern} liefert zur Bundeslandnummer einen
 * Vektor von Paaren bestehend aus Negativmuster und Fehlermeldung.
 * </p>
 * <p>
 * Die Schnittstellen
 * {@link com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberDefiningData} und {@link com.mgmtp.a12.kernel.core.customfieldtype.impl.internal.defdata.api.IGermanTaxNumberFormatting} werden vom
 * aufrufenden System
 * implementiert.
 *
 * <p>
 * Abschließend wird die Steuernummer noch anhand eines Prüfziffernberechnundsverfahrens geprüft.
 * </p>
 */
public final class SteuernummerVerifierImpl extends AbstractSteuernummerVerifier {

	private final @NonNull IFadat fadat;

	public SteuernummerVerifierImpl(@NonNull IFadat fadat) {
		super(new SteuernummerValidationHelper(SteuernummerValidationHelper.VALID_STNR),
				CustomFieldTypeErrorKeys.UNGUELTIGE_STNR);
		this.fadat = fadat;
	}

	@Override
	protected IntPredicate getBufaValidator() {
		return bufaNr -> FadatLookupUtil.existsFederalTaxOfficeNumber(fadat, bufaNr);
	}

	@Override
	protected @NonNull CustomFieldTypeErrorEnum mapValidationError(CustomFieldTypeErrorEnum error) {
		return error; // preserve specific error codes from checkSteuernummer
	}

	@Override
	protected @NonNull CustomFieldTypeErrorEnum createValidationError() {
		return CustomFieldTypeErrorEnum.STNR_FORMALER_FEHLER;
	}
}
