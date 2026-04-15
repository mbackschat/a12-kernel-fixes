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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.FormatDefinitionDatumBereich.Jahresinterpretation;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;

/**
 * Utility Methoden für die Oberflächendarstellung für ein Datumsfeld
 */
public class DarstellungsInfoDatum {

	private static final String[] GUELTIGE_REIHENFOLGEN = { "JMT", "JTM", "MTJ", "MJT", "TMJ", "TJM" };

	private final String datumTrenner;
	private final String datumBereichTrenner;
	private final boolean nullenOptional;
	private final DatumReihenfolge fragmentReihenfolge;
	private final int baseYear;

	private final PresentationInformationValues defaultValues;

	private final String dateTimeFormat;
	private final String timeFormat;
	private final TimeZone timeZone;

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

	public DarstellungsInfoDatum(int baseYear, TimeZone timeZone) {
		this.datumTrenner = null;
		this.datumBereichTrenner = null;
		this.fragmentReihenfolge = null;
		this.baseYear = baseYear;
		this.nullenOptional = false;
		this.defaultValues = null;
		this.dateTimeFormat = null;
		this.timeFormat = null;
		this.timeZone = timeZone;
	}

	/**
	 * Erstellt aus der Darstellungsinformation wie sie vom aufrufenden
	 * Programm übergeben wurde eine interne Darstellung.
	 * <p>
	 * Es findet eine Überprüfung statt, ob die übergebene Darstellungsinformation korrekt ist. Bei
	 * fehlerhaften Angaben wird eine {@link IllegalArgumentException} geworfen.
	 * </p>
	 */
	public DarstellungsInfoDatum(int baseYear, PresentationInformation info, PresentationInformationValues defaultValues,
			TimeZone timeZone) {
		this.baseYear = baseYear;
		this.defaultValues = defaultValues;
		this.datumTrenner = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_SEPARATOR, defaultValues);
		this.datumBereichTrenner = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_RANGE_SEPARATOR,
				defaultValues);
		String nullOpt = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_ZERO_OPTIONAL, defaultValues);
		this.nullenOptional = PresentationInformation.TRUE.equals(nullOpt);

		String reihenfolge = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_FRAGMENT_ORDERING,
				defaultValues);
		this.fragmentReihenfolge = reihenfolge != null ? new DatumReihenfolge(reihenfolge) : null;

		this.dateTimeFormat = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.DATE_TIME_FORMAT,
				defaultValues);
		this.timeFormat = DarstellungsInformationIntern.getInfoKey(info, PresentationInformation.TIME_FORMAT, defaultValues);

		this.timeZone = timeZone;

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
	 * Konvertiere den Datumswert in den XML Wert
	 *
	 * <pre>
	 * Der Wert ist ein gültiges Datum. Dies wurde bereits überprüft.
	 * </pre>
	 */
	String convertDatumOfl2Xml(String oflWert, String xmlFormat, boolean istTeilbekannt, int ergaenzungsJahr) {

		//Zur Konvertierung wird das Oberflächenformat benötigt
		String oflFormat = convertDatumFormatXml2Ofl(xmlFormat);

		DateAndFormat oflDatumMitFormat = new DateAndFormat(oflWert, oflFormat, timeZone);

		//In einem teilbekannten Datum müssen die nicht bekannten Teile konkret gemacht werden,
		//um ein konkretes Datum in ein konkretes Datum zu überführen.  Welche DatumsAngabeteile geändert
		//werden wird gespeichert,  damit nach der Konvertierung diese DatumsAngabeteile wieder nicht
		//konkret gemacht werden können.
		List<DatePart> geaenderteDatumsAngabeTeile = new ArrayList<>();
		if (istTeilbekannt) {
			geaenderteDatumsAngabeTeile = PartiallyKnownDatesUtil.replaceUnknownDateParts(oflDatumMitFormat);
		}

		String xmlWert = convertDatumWertInternal(oflDatumMitFormat, xmlFormat, ergaenzungsJahr);
		DateAndFormat xmlDatumMitFormat = new DateAndFormat(xmlWert, xmlFormat, timeZone);

		//Setze nicht bekannte DatumsAngabeTeile wieder auf nicht bekannt.
		PartiallyKnownDatesUtil.setDatePartsToUnknown(xmlDatumMitFormat, geaenderteDatumsAngabeTeile);

		return xmlDatumMitFormat.getDate();
	}

	String convertDatumBereichOfl2Xml(String oflWert, String xmlFormatDatum, String xmlDatumBereichTrenner,
			Jahresinterpretation jahresinterpretation) {
		//Precondition:  wert ist gültig in Bezug auf Oberflächenformat.
		String dbTrenner = convertDatumBereichTrennerXML2Ofl(xmlDatumBereichTrenner);

		String[] datumBereichOfl = DateUtil.splitStringBySeparator(oflWert, dbTrenner);
		String oflFormat = convertDatumFormatXml2Ofl(xmlFormatDatum);

		int[] ergaenzungsJahre = DateUtil.getSupplementaryYears(oflFormat, datumBereichOfl, jahresinterpretation, baseYear,
				datumTrennerHintenOptional(oflFormat), timeZone);

		String xmlWertStart = convertDatumOfl2Xml(datumBereichOfl[0], xmlFormatDatum, false, ergaenzungsJahre[0]);
		String xmlWertEnde = convertDatumOfl2Xml(datumBereichOfl[1], xmlFormatDatum, false, ergaenzungsJahre[1]);

		return xmlWertStart + xmlDatumBereichTrenner + xmlWertEnde;
	}

	/**
	 * Konvertiert ein Datum in XML-Darstellung in ein Datum
	 * in Oberflächendarstellung. Behandelt alle Datumsarten, d.h. auch
	 * die teilbekannten Datumsangaben.
	 */
	String convertDatumXml2Ofl(String xmlWert, String xmlFormat, boolean istTeilbekannt, int ergaenzungsJahr) {
		DateAndFormat xmlDatumMitFormat = new DateAndFormat(xmlWert, xmlFormat, timeZone);

		Collection<DatePart> geaenderteDatumsteile = new ArrayList<>();
		if (istTeilbekannt) {
			geaenderteDatumsteile = PartiallyKnownDatesUtil.replaceUnknownDateParts(xmlDatumMitFormat);
		}

		String oflFormat = convertDatumFormatXml2Ofl(xmlFormat);
		String oflWert = convertDatumWertInternal(xmlDatumMitFormat, oflFormat, ergaenzungsJahr);
		DateAndFormat oflDatumMitFormat = new DateAndFormat(oflWert, oflFormat, timeZone);

		PartiallyKnownDatesUtil.setDatePartsToUnknown(oflDatumMitFormat, geaenderteDatumsteile);
		return oflDatumMitFormat.getDate();
	}

	/**
	 * Konvertiert einen Datumsbereich in XML-Darstellung in einen Datumsbereich in
	 * Oberflächendarstellung
	 */
	String convertDatumBereichXml2Ofl(String xmlWert, String xmlFormatDatum,
			String xmlDatumBereichTrenner, Jahresinterpretation jahresinterpretation) {
		String[] datumBereich = DateUtil.splitStringBySeparator(xmlWert, xmlDatumBereichTrenner);

		int[] ergaenzungsJahre = DateUtil.getSupplementaryYears(xmlFormatDatum, datumBereich, jahresinterpretation, baseYear,
				datumTrennerHintenOptional(xmlFormatDatum), timeZone);

		String dateVonConverted = convertDatumXml2Ofl(datumBereich[0], xmlFormatDatum, false, ergaenzungsJahre[0]);
		String dateBisConverted = convertDatumXml2Ofl(datumBereich[1], xmlFormatDatum, false, ergaenzungsJahre[1]);

		String oflDatumBereichTrenner = convertDatumBereichTrennerXML2Ofl(xmlDatumBereichTrenner);
		return dateVonConverted + oflDatumBereichTrenner + dateBisConverted;
	}

	/**
	 * Konvertiert ein DateAndFormat in das übergebene Zielformat.
	 * Der übergebene Parameter datumMitFormat wird als Seiteneffekt geändert.
	 */
	private String convertDatumWertInternal(DateAndFormat datumMitFormat, String targetFormat, int ergaenzungsJahr) {
		//für Meldung in Fehlersituation brauchen wir die Ursprungswerte
		String srcFormat = datumMitFormat.getFormat();

		//Normiere das übergebene Datum für reine Datumsangaben.
		if (!targetFormat.contains("HH") && !srcFormat.contains("HH")) {
			datumMitFormat.unifyLastSeparator();
		}

		// Falls kein Jahr mit angegeben ist hänge Jahr an - für den 29.2.
		if (DateUtil.formatContainsOnlyDayMonth(srcFormat)) {
			datumMitFormat.completeWithYear(ergaenzungsJahr);
		}

		// die eigentliche Arbeit: parse das Datum und formatiere diese ins neue Format
		Date targetDatum = ValidationDateParser.parseDateWithoutSyntaxCheck(datumMitFormat);
		return ValidationDateParser.makeStringDayMonthTwoDigits(targetDatum, targetFormat, datumMitFormat.getTimeZone());
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
	String convertDatumFormatXml2Ofl(String xmlFormat) {
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

	public String convertDatumXml2Ofl(String xmlWert, String xmlFormat, boolean istTeilbekannt) {
		return convertDatumXml2Ofl(xmlWert, xmlFormat, istTeilbekannt, baseYear);
	}

	public String convertDatumOfl2Xml(String xmlWert, String xmlFormat, boolean istTeilbekannt) {
		return convertDatumOfl2Xml(xmlWert, xmlFormat, istTeilbekannt, baseYear);
	}

}
