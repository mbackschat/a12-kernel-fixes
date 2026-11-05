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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.utils.conversion.a12internal.util.DatePart;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.internal.DataFormatsBuilder;

import lombok.NonNull;

/**
 * Utility Methoden für die Oberflächendarstellung für ein Datumsfeld
 */
public class DarstellungsInfoDatum {

	private static final String[] GUELTIGE_REIHENFOLGEN = { "JMT", "JTM", "MTJ", "MJT", "TMJ", "TJM" };

	private final String datumTrenner;
	private final String datumBereichTrenner;
	private final boolean nullenOptional;
	private final DatumReihenfolge fragmentReihenfolge;
	private final DataFormats.DATE_FRAGMENT_ORDERING_TYPE dateFragmentOrdering;

	private final PresentationInformationValues defaultValues;

	private final String dateTimeFormat;
	private final String timeFormat;

	private class DatumReihenfolge {

		private DatePart[] fragmente = new DatePart[3];

		DatumReihenfolge(String reihenfolge) {
			checkReihenfolge(reihenfolge);
			if (reihenfolge != null) {
				for (int i = 0; i <= 2; i++) {
					this.fragmente[i] = DatePart.parseFragmentOrder(reihenfolge.charAt(i));
				}
			}
		}

		boolean tagVorMonat() {
			return fragmente[0] == DatePart.DAY || fragmente[2] == DatePart.MONTH;
		}

		String getFormatFragment(String srcFormat, int i) {
			if (i >= fragmente.length || fragmente[i] == null) {
				return "";
			}
			DatePart fragment = fragmente[i];
			if (!fragment.isInFormat(srcFormat)) {
				return "";
			}
			//tempcobertura::0.75::tempcobertura
			switch (fragment) {
				case MONTH:
					return DateUtil.getMonthFormat(nullenOptional);
				case YEAR:
					return DateUtil.YEAR_FORMAT;
				case DAY:
					return DateUtil.getDayFormat(nullenOptional);
				default:
					//tempcobertura::0.00::tempcobertura
					return "";
			}
		}

		private void checkReihenfolge(String datumReihenfolge) {
			if (datumReihenfolge != null && !Arrays.asList(GUELTIGE_REIHENFOLGEN).contains(datumReihenfolge)) {
				werfeDarstellungsInfoFehler(
						String.format("The key '%s' has the invalid value '%s'.", PresentationInformation.DATE_FRAGMENT_ORDERING,
								datumReihenfolge));
			}
		}
	}

	public DarstellungsInfoDatum() {
		this.datumTrenner = null;
		this.datumBereichTrenner = null;
		this.fragmentReihenfolge = null;
		this.dateFragmentOrdering = null;
		this.nullenOptional = false;
		this.defaultValues = null;
		this.dateTimeFormat = null;
		this.timeFormat = null;
	}

	/**
	 * Erstellt aus der Darstellungsinformation wie sie vom aufrufenden
	 * Programm übergeben wurde eine interne Darstellung.
	 * <p>
	 * Es findet eine Überprüfung statt, ob die übergebene Darstellungsinformation korrekt ist. Bei
	 * fehlerhaften Angaben wird eine {@link IllegalArgumentException} geworfen.
	 * </p>
	 */
	public DarstellungsInfoDatum(PresentationInformation info, PresentationInformationValues defaultValues) {
		this.defaultValues = defaultValues;
		this.datumTrenner = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_SEPARATOR, defaultValues);
		this.datumBereichTrenner = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_RANGE_SEPARATOR,
				defaultValues);
		String nullOpt = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_ZERO_OPTIONAL, defaultValues);
		this.nullenOptional = PresentationInformation.TRUE.equals(nullOpt);

		String reihenfolge = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_FRAGMENT_ORDERING,
				defaultValues);
		this.fragmentReihenfolge = reihenfolge != null ? new DatumReihenfolge(reihenfolge) : null;
		this.dateFragmentOrdering = toDateFragmentOrdering(reihenfolge);

		this.dateTimeFormat = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_TIME_FORMAT,
				defaultValues);
		this.timeFormat = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.TIME_FORMAT, defaultValues);

		checkSchluesselWerte(info);
	}

	private void checkSchluesselWerte(PresentationInformation info) {
		checkNullenOptional(info);
		checkDatumTrenner();
		checkDatumBereichTrenner();
		checkWertekompatibel();
		checkDateTimeFormat();
	}

	/**
	 * Prüfe, dass der Wert für NullenOptional nicht gesetzt oder zulässig ist.
	 */
	private void checkNullenOptional(PresentationInformation info) {
		String value = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_ZERO_OPTIONAL, defaultValues);
		if (value != null && !PresentationInformation.TRUE.equals(value) && !PresentationInformation.FALSE.equals(value)) {
			werfeDarstellungsInfoFehler(
					String.format("The key '%s' has the invalid value '%s'.", PresentationInformation.DATE_ZERO_OPTIONAL, value));
		}
	}

	private void werfeDarstellungsInfoFehler(String fehlerMeldung) {
		throw new IllegalArgumentException("Error in the presentation information: " + fehlerMeldung);
	}

	/**
	 * Prüfe, dass der Wert für den Datumstrenner nicht gesetzt ist oder zulässig ist.
	 * Leerzeichen oder mehr als ein Zeichen sind als Datumstrenner nicht zulässig.
	 */
	private void checkDatumTrenner() {
		if (datumTrenner == null) {
			return;
		}
		if (" ".equals(datumTrenner)) {
			werfeDarstellungsInfoFehler("The date separator may not be a blank.");
		}
		if (datumTrenner.length() > 1) {
			werfeDarstellungsInfoFehler(
					String.format("The date separator must be a single character. Instead '%s' was given.", datumTrenner));
		}
	}

	private void checkDatumBereichTrenner() {
		if (datumBereichTrenner != null && datumBereichTrenner.length() != 1) {
			werfeDarstellungsInfoFehler(
					String.format("The date range separator must be a single character. Instead '%s' was given.",
							datumBereichTrenner));
		}
	}

	/**
	 * Prüfe, dass die Werte für Datumdarstellung zueinander passen.
	 * <ul>
	 * <li>datumsTrenner, datumsBereichsTrenner und datumsReihenfolge müssen gemeinsam oder gar
	 * nicht angegeben werden</li>
	 * <li>Wenn Nullen im Datum optional sind, dann muss ein Datumstrenner spezifiziert sein.</li>
	 * <li>Datumstrenner und Datumsbereichstrenner müssen unterschiedlich sein.</li>
	 * </ul>
	 */
	private void checkWertekompatibel() {
		if (!alleOderKeinsNull(datumTrenner, datumBereichTrenner, fragmentReihenfolge)) {
			String errorMsg = "If a value for one of the keys 'DatumTrenner', 'DatumBereichTrenner' or 'DatumReihenfolge' was specified, a value for the others needs to be specified as well.";
			werfeDarstellungsInfoFehler(errorMsg);
		}
		if (nullenOptional && StringUtils.isEmpty(datumTrenner)) {
			werfeDarstellungsInfoFehler(
					"The key 'DatumNullenOptional' is active, but no value was specified for the key 'DatumTrenner'.");
		}
		if (datumTrenner != null && datumTrenner.equals(datumBereichTrenner)) {
			werfeDarstellungsInfoFehler("The keys 'DatumTrenner' and 'DatumBereichTrenner' must be different.");
		}
	}

	/**
	 * Ensure, that the date time format contains a year specification.
	 */
	private void checkDateTimeFormat() {
		if (!StringUtils.isEmpty(dateTimeFormat) && !dateTimeFormat.contains("y")) {
			werfeDarstellungsInfoFehler("The key 'DateTimeFormat' must contain a year specification (y).");
		}
	}

	private boolean alleOderKeinsNull(Object... werte) {
		boolean einsIstNull = false;
		boolean einsIstNichtNull = false;
		for (Object wert : werte) {
			if (wert == null) {
				einsIstNull = true;
			} else {
				einsIstNichtNull = true;
			}
		}
		return !(einsIstNull && einsIstNichtNull);
	}

	/**
	 * Liefert <code>true</code>, wenn für das übergebene Format bzgl. der Darstellungsinformation
	 * ein Datumstrenner hinten erlaubt ist, aber optional. Dies ist der Fall, wenn
	 * <ul>
	 * <li>in der Darstellungsinformation der Punkt als Datumstrenner spezifziert wurde</li>
	 * <li>in der Darstellungsinformation eine Datumsreihenfolge spezifiziert wurde, in der Tag vor
	 * Monat kommt</li>
	 * <li>das Format spezifiziert Tag und Monat, kein Jahr</li>
	 * </ul>
	 */
	boolean datumTrennerHintenOptional(String datumsFormat) {
		if (datumTrenner == null) {
			return false;
		}
		return ".".equals(datumTrenner) && DateUtil.formatContainsOnlyDayMonth(datumsFormat) && fragmentReihenfolge.tagVorMonat();
	}

	/**
	 * Konvertiert den XML Format-String in den Formatstring gemäß der
	 * {@link PresentationInformation}. Für die Konvertierung gilt:
	 * <ul>
	 * <li>im Sonderfall, daß ein Trenner hinten erlaubt ist (vgl.
	 * {@link #datumTrennerHintenOptional}), wird hier das Format <b>ohne</b> diesen optionalen
	 * Trenner erzeugt.</li>
	 * <li>sind Nullen optional, so wird für Tag und Monat "d" bzw. "M", also einstellig erzeugt
	 * </li>
	 * </ul>
	 */
	@NonNull String convertDatumFormatXml2Ofl(@NonNull String xmlFormat) {
		if (xmlFormat.contains("HH")) {
			if (xmlFormat.contains("MM")) {
				// DateTime
				if (dateTimeFormat == null) {
					// for empty presentation information use internal format
					return xmlFormat;
				} else {
					return dateTimeFormat;
				}
			} else {
				// Time
				if (timeFormat == null) {
					// for empty presentation information use internal format
					return xmlFormat;
				} else {
					return timeFormat;
				}
			}
		}
		if (fragmentReihenfolge == null && !nullenOptional) {
			return xmlFormat;
		}
		String oflTrenner = datumTrenner != null ? datumTrenner : DateUtil.getDateSeparator(xmlFormat);
		if (fragmentReihenfolge != null) {//Reihenfolge von Tag, Monat und Jahr muss evtl. geändert werden
			List<String> formatteile = new ArrayList<>();
			for (int i = 0; i <= 2; i++) {
				if (!StringUtils.isEmpty(fragmentReihenfolge.getFormatFragment(xmlFormat, i))) {
					formatteile.add(fragmentReihenfolge.getFormatFragment(xmlFormat, i));
				}
			}
			return StringUtils.join(formatteile, oflTrenner);
		}

		//keine darstellungsspezifische Reihenfolge -> DatumTrenner wie im XML. Nur NullenOptional muss berücksichtigt werden
		return DateUtil.makeFormatStringDayMonthOptional(xmlFormat); // nullenOptional == true
	}

	String convertDatumBereichTrennerXML2Ofl(String xmlDatumBereichTrenner) {
		return datumBereichTrenner != null ? datumBereichTrenner : xmlDatumBereichTrenner;
	}

	/**
	 * Ermittelt die MinLaenge für ein Datumsformat für die Metaschnittstelle.
	 */
	int getMinLaengeDatum(String xmlFormat) {
		String oflFormat = convertDatumFormatXml2Ofl(xmlFormat);

		String oflFormatCorrectLength = oflFormat.replace("'", "");
		return oflFormatCorrectLength.length();
	}

	/**
	 * Ermittelt die MaxLaenge für ein Datumsformat für die Metaschnittstelle.
	 */
	int getMaxLaengeDatum(String xmlFormat) {
		String oflFormat = convertDatumFormatXml2Ofl(xmlFormat);
		//für die MaxLaenge nehmen wir den optionalen hinteren Trenner
		if (datumTrennerHintenOptional(oflFormat)) {
			oflFormat = oflFormat + ".";
		}

		oflFormat = DateUtil.makeFormatStringDayMonthTwoDigits(oflFormat);
		String oflFormatCorrectLength = oflFormat.replace("'", "");
		return oflFormatCorrectLength.length();
	}

	/**
	 * Ermittelt die MinLaenge für ein Datumsbereichsformat für die Metaschnittstelle.
	 */
	int getMinLaengeDatumBereich(String xmlDatumFormat, String xmlDatumBereichTrenner) {
		return 2 * getMinLaengeDatum(xmlDatumFormat) + dbTrennerLaenge(xmlDatumBereichTrenner);
	}

	private int dbTrennerLaenge(String xmlDatumBereichTrenner) {
		String dbTrenner = convertDatumBereichTrennerXML2Ofl(xmlDatumBereichTrenner);
		if (StringUtils.isEmpty(dbTrenner)) {
			return 0;
		}
		return dbTrenner.length();
	}

	/**
	 * Ermittelt die MaxLaenge für ein Datumsbereichsformat für die Metaschnittstelle.
	 */
	int getMaxLaengeDatumBereich(String xmlDatumFormat, String xmlDatumBereichTrenner) {
		return 2 * getMaxLaengeDatum(xmlDatumFormat) + dbTrennerLaenge(xmlDatumBereichTrenner);
	}

	/**
	 * Maps the presentation-information date fragment order (e.g. {@code "TMJ"}) to the
	 * {@link DataFormats.DATE_FRAGMENT_ORDERING_TYPE} used by the conversion module
	 * ({@code T -> DAY}, {@code M -> MONTH}, {@code J -> YEAR}). Returns {@code null} if no order is given.
	 */
	private static DataFormats.DATE_FRAGMENT_ORDERING_TYPE toDateFragmentOrdering(String reihenfolge) {
		if (reihenfolge == null) {
			return null;
		}
		String name = reihenfolge.chars()
				.mapToObj(c -> switch ((char) c) {
					case 'J' -> "YEAR";
					case 'M' -> "MONTH";
					case 'T' -> "DAY";
					default -> throw new IllegalArgumentException(
							"Invalid date fragment order character: " + (char) c);
				})
				.collect(Collectors.joining("_"));
		return DataFormats.DATE_FRAGMENT_ORDERING_TYPE.valueOf(name);
	}

	DataFormats getDateDisplayDataFormats(DataFormats defaults) {
		return new DataFormatsBuilder(defaults)
				.dateSeparator(datumTrenner)
				.dateZeroOptional(nullenOptional)
				.dateFragmentOrdering(dateFragmentOrdering)
				.dateTimeFormat(dateTimeFormat)
				.timeFormat(timeFormat)
				.build();
	}

}
