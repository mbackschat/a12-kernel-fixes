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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateAndFormat;
import com.mgmtp.a12.utils.conversion.a12internal.util.DatePart;
import com.mgmtp.a12.utils.conversion.a12internal.util.PartiallyKnownDatesUtil;

/**
 * Die Klasse stellt Hilfskunktionalität zur Behandlung von teilbekannten Datumsfeldern zur
 * Verfügung. Sämtliche Methoden setzen ohne Überprüfung voraus, daß
 * <ul>
 * <li>das Format eines teilbekannten Datums Tag, Monat und Jahr spezifizieren muß.</li>
 * <li>der übergebene Wert "rein syntaktisch" korrekt ist, d.h. entspricht dem regulären Ausdruck,
 * der durch das Format gegeben ist.</li>
 * </ul>
 */
@CopyOfA12InternalSource
public class TeilbekanntesDatumUtil {

	@CopyOfA12InternalSource
	private TeilbekanntesDatumUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Prüft für ein teilbekanntes Datum, ob die nicht bekannten Datumsteile korrekt und
	 * konsistent
	 * sind.<br>
	 * Es ist z.B. nicht erlaubt, dass für ein Datum mit Monat optional der Monat mit "00"
	 * angegeben
	 * ist, aber der Tag nicht.<br>
	 * Die restlichen Prüfungen erfolgen woanders, insb. wird hier z.B. "00.13.2000", mit dem
	 * Format
	 * "dd.MM.yyyy" und Tag optional, nicht als fehlerhaft erkannt.<br>
	 * Im Fehlerfall wird die Fehlermeldung zurückgegeben, sonst ein Leerstring.
	 * <p>
	 * <b>Precondition:</b> das Datum ist "rein syntaktisch" korrekt, d.h. entspricht dem regulären
	 * Ausdruck, der durch das Format gegeben ist. Darf NUR für Datumsangaben ohne Zeitangabe
	 * verwendet werden.
	 * </p>
	 */
	public static RuntimeFormalErrorEnum pruefeNichtBekannteDatumsteile(
			DateAndFormat datumMitFormat, DatumTeilbekanntArt tbArt) {

		String tag = PartiallyKnownDatesUtil.getValueOfDatePart(datumMitFormat, DatePart.DAY);
		String monat = PartiallyKnownDatesUtil.getValueOfDatePart(datumMitFormat, DatePart.MONTH);
		String jahr = PartiallyKnownDatesUtil.getValueOfDatePart(datumMitFormat, DatePart.YEAR);

		//Bei Teilbekanntem Datum darf nie nur 0 anstelle von 00 eingegeben werden.
		//Um schöne Fehlermeldungen zu bekommen,  muss dies getrennt geprüft werden.
		//Diese Fehlermeldungen sind nur möglich bei DatumNullen Optional.
		boolean jahrIstNull = ("0000".equals(jahr));
		boolean monatIst00 = ("00".equals(monat));
		boolean monatIst0 = ("0".equals(monat));
		boolean tagIst00 = ("00".equals(tag));
		boolean tagIst0 = ("0".equals(tag));

		//Cobertura:/Alle Elemente des Enums werden behandelt und auch getestet.
		switch (tbArt) {
			case TAG_OPTIONAL:
				//Behandle zunächst die Fälle das für Tag oder Monat nur eine 0 eingegeben wurden
				if (tagIst0) {//Tag darf nicht 0 sein
					return RuntimeFormalErrorEnum.DATUM_TAG_0;
				}
				if (monatIst0) {// Monat darf nicht 0 sein
					return RuntimeFormalErrorEnum.DATUM_MONAT_FEHLT0;
				}

				//Es wurde nirgends nur 0 eingegeben, prüfe die anderen Fälle
				if (monatIst00) {// Monat darf nicht 00 sein
					return RuntimeFormalErrorEnum.DATUM_MONAT_FEHLT;
				}

				if (jahrIstNull) {// Jahr darf nicht 0000 sein
					return RuntimeFormalErrorEnum.DATUM_JAHR_FEHLT;
				}
				return RuntimeFormalErrorEnum.KEIN_FEHLER;
			case MONAT_OPTIONAL:
				//Behandle zunächst die Fälle das für Tag oder Monat nur eine 0 eingegeben wurden
				if (tagIst0) {//Tag darf nicht 0 sein
					return monatIst00 ? RuntimeFormalErrorEnum.DATUM_TAG_0_STATT_00 : RuntimeFormalErrorEnum.DATUM_TAG_0;
				}
				if (monatIst0) {//Monat darf nicht 0 sein
					return (tagIst00) ?
							RuntimeFormalErrorEnum.DATUM_MONAT_0 :
							RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN0;
				}

				//Tag und Monat ist ungleich 0: prüfe die anderen Fehlerfälle
				if (monatIst00 && !tagIst00) {// Falls Monat 00, dann muss auch Tag 00 sein
					return RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN;
				}

				if (jahrIstNull) {// Jahr darf nich 0 sein
					return RuntimeFormalErrorEnum.DATUM_JAHR_FEHLT;
				}
				return RuntimeFormalErrorEnum.KEIN_FEHLER;
			case JAHR_OPTIONAL:
				// Falls Jahr 0000, dann müssen auch Monat und Tag 00 sein
				if (jahrIstNull && ((!monatIst00 && !monatIst0) || (!tagIst00 && !tagIst0))) {
					return RuntimeFormalErrorEnum.DATUM_JAHROPTIONAL_MONAT_ODER_TAG_ANGEGEBEN;
				}

				if (tagIst0) {
					return (monatIst00 || monatIst0) ?
							RuntimeFormalErrorEnum.DATUM_TAG_0_STATT_00 :
							RuntimeFormalErrorEnum.DATUM_TAG_0;
				}
				if (monatIst0) {
					if (tagIst00) {
						return (jahrIstNull) ? RuntimeFormalErrorEnum.DATUM_MONAT_0_STATT_00 : RuntimeFormalErrorEnum.DATUM_MONAT_0;
					} else {
						return RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN0;
					}
				}

				if (monatIst00 && !tagIst00) {// Falls Monat 00, dann muss auch Tag 00 sein
					return RuntimeFormalErrorEnum.DATUM_MONATOPTIONAL_TAG_ANGEGEBEN;
				}
				return RuntimeFormalErrorEnum.KEIN_FEHLER;
			case BEKANNT:
				return RuntimeFormalErrorEnum.KEIN_FEHLER;
			default:
				throw new IllegalArgumentException("Unknown type of partial knowledge about the date format: '" + tbArt + "'.");
		}
	}
}
