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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DatumTeilbekanntArt;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.TeilbekanntesDatumUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.utils.conversion.DatePrecision;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateAndFormat;
import com.mgmtp.a12.utils.conversion.a12internal.util.DatePart;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil;
import com.mgmtp.a12.utils.conversion.a12internal.util.PartiallyKnownDatesUtil;
import com.mgmtp.a12.utils.conversion.a12internal.util.ValidationDateParser;
import com.mgmtp.a12.utils.conversion.config.DateBasicConversionConfig;
import com.mgmtp.a12.utils.conversion.internal.DateConversion;
import com.mgmtp.a12.utils.conversion.internal.config.PartiallyKnownDateConversionConfigBuilder;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.internal.DataFormatsBuilder;

import lombok.Getter;
import lombok.NonNull;

/**
 * Klasse definiert ein DatumsFormat zur Validierung von Eingaben zur Laufzeit
 */
public class FormatDefinitionDatum implements IFormatDefinition {

	protected enum FehlerBereich {
		BEREICH_START_DATUM,
		BEREICH_END_DATUM
	}

	private static final String DATE_FORMAT_PARAM_NAME = "<dateFormat>";
	private static final String DATE_STRING_PARAM_NAME = "<dateString>";
	private static final String DATE_STRING_DAY_PARAM_NAME = "<dateStringDay>";
	private static final String DATE_STRING_MONTH_PARAM_NAME = "<dateStringMonth>";
	private static final String DATE_STRING_YEAR_PARAM_NAME = "<dateStringYear>";
	protected static final String TIME_INTERVAL_FORMAT_PARAM_NAME = "<timeIntervalFormat>";
	@Getter protected @NonNull String format;
	protected String metaFormat;
	private DatumTeilbekanntArt teilBekanntArt;
	protected int laenge;
	protected String feldTyp;
	private final DatePrecision datePrecision;
	private final DataFormats internalDataFormats;

	private ConcurrentHashMap<TimeZone, Date> dateFromCache = new ConcurrentHashMap<>();

	protected boolean dateInvalid(Date d, TimeZone timeZone) {
		Date dateFrom = this.dateFromCache.computeIfAbsent(timeZone, k -> getDateFrom(k, Constants.DATE_FROM));
		return dateInvalid(d, dateFrom);
	}

	private boolean dateInvalid(Date d, Date dateFrom) {
		return d.before(dateFrom);

	}

	private Date getDateFrom(TimeZone timeZone, String dateFrom) {
		return ValidationDateParser.parseDate(dateFrom, Constants.DATUM_FORMAT, timeZone, false);
	}

	/**
	 * Konstruktor wird beim Einlesen der Formatdaten beim Initialisieren der
	 * Checker- Klasse aufgerufen.
	 */
	public FormatDefinitionDatum(
			@NonNull String format,
			DatumTeilbekanntArt teilbekanntheit) {
		this.format = format;
		this.teilBekanntArt = teilbekanntheit;

		this.metaFormat = format.replace("d", "T").replace("y", "J");
		String formatWithCorrectLength = format.replace("'", "");
		this.laenge = formatWithCorrectLength.length();
		this.feldTyp = IFormatDefinition.FeldFormatEnumType.ER_DATUM.toString();
		this.datePrecision = mapToDatePrecision(teilbekanntheit);
		this.internalDataFormats = getInternalDataFormats(format);
	}

	/**
	 * Maps the {@link DatumTeilbekanntArt} to the {@link DatePrecision} understood by the conversion module.
	 */
	private static DatePrecision mapToDatePrecision(DatumTeilbekanntArt teilBekanntArt) {
		return switch (teilBekanntArt) {
			case BEKANNT -> DatePrecision.FULL;
			case TAG_OPTIONAL -> DatePrecision.DAY_OPTIONAL;
			case MONAT_OPTIONAL -> DatePrecision.MONTH_OPTIONAL;
			case JAHR_OPTIONAL -> DatePrecision.YEAR_OPTIONAL;
		};
	}

	/**
	 * The {@link DataFormats} describing the field's own (internal / XML) date layout, so that
	 * {@link com.mgmtp.a12.utils.localization.internal.DataFormatsUtil#calculateDateFormat} reproduces
	 * exactly this format. Mirrors the TypeScript {@code FormatDefinitionDatum.getInternalDataFormats}.
	 */
	public static DataFormats getInternalDataFormats(String format) {
		return switch (format) {
			case "dd.MM.yyyy", "dd.MM.", "dd.MM" -> getDataFormat(DataFormats.DATE_FRAGMENT_ORDERING_TYPE.DAY_MONTH_YEAR, ".");
			case "ddMM" -> getDataFormat(DataFormats.DATE_FRAGMENT_ORDERING_TYPE.DAY_MONTH_YEAR, "");
			case "yyyyMMdd", "MMdd", "yyyyMM" -> getDataFormat(DataFormats.DATE_FRAGMENT_ORDERING_TYPE.YEAR_MONTH_DAY, "");
			default -> new DataFormatsBuilder().build();
		};
	}

	private static DataFormats getDataFormat(DataFormats.DATE_FRAGMENT_ORDERING_TYPE ordering,
			String separator) {
		return new DataFormatsBuilder()
				.dateSeparator(separator)
				.dateFragmentOrdering(ordering)
				.build();
	}

	/**
	 * Builds the conversion config describing a date (format + partial-date precision) for the given base
	 * year and time zone.
	 */
	private DateBasicConversionConfig createDateConversionConfig(String format, int baseYear, TimeZone timeZone) {
		PartiallyKnownDateConversionConfigBuilder builder = new PartiallyKnownDateConversionConfigBuilder();
		builder.withFormat(format);
		builder.withBaseYear(baseYear);
		builder.withTimeZone(timeZone);
		builder.withDatePrecision(datePrecision);
		return builder.build();
	}

	@Override
	public String convert(String wert, DarstellungsInformationIntern di, boolean vonXml,
			RawParamHandler4Convert additionalParameters) {
		if (wert.isEmpty()) {
			return wert;
		}
		DateBasicConversionConfig internalConfig = createDateConversionConfig(format, di.getBaseYear(), di.getTimeZone());
		DateBasicConversionConfig displayConfig =
				createDateConversionConfig(di.convertDatumFormatXml2Ofl(format), di.getBaseYear(), di.getTimeZone());
		DataFormats displayDataFormats = di.getDateDisplayDataFormats(internalDataFormats);
		if (vonXml) {
			return DateConversion.convert(wert, internalConfig, internalDataFormats, displayConfig, displayDataFormats);
		} else {
			return DateConversion.convert(wert, displayConfig, displayDataFormats, internalConfig, internalDataFormats);
		}
	}

	/**
	 * allgemeine Methode zum Validieren des Eingabewertes wert.
	 */
	@Override
	public ValidType validiereFormat(IValidierungParam param, IMetaDataValidierung metaData, boolean fullCheck) {
		if (!fullCheck && param.getWert().isEmpty()) {
			// this case is for the base type conversion -> an empty value will be converted to null later on
			return ValidType.Valid;
		}

		DarstellungsInformationIntern info = param.getDarstellungsInfo();
		final String pruefFormat = info.convertDatumFormatXml2Ofl(format);
		/*
		 * Die Umwandlung ('d' -> 'T', 'y' -> 'J') muß bereits hier erfolgen, da das Prüfformat
		 * evtl. manipuliert wird:
		 * ergänzt um einen optionalen Trenner und das Jahresformat, damit der 29.2. korrekt
		 * behandelt werden kann
		 */
		String formatFuerFehlermeldungen = convertPruefFormat2FehlermeldungFormat(pruefFormat, param.getSprache());

		DateAndFormat datumMitFormat = new DateAndFormat(param.getWert(), pruefFormat, metaData.getTimeZone());
		DateUtil.completeDateAndFormat(datumMitFormat, format, info.datumTrennerHintenOptional(format),
				metaData.getBaseYearOrDefault());

		ValidType teilbekanntErgebnis = checkTeilbekanntSpezifischePruefungen(datumMitFormat, param, formatFuerFehlermeldungen);
		if (ValidType.Valid != teilbekanntErgebnis) {
			return teilbekanntErgebnis;
		}
		if (isTeilbekannt()) {
			// replace day/month 00 with 01, year 0000 with 2000
			PartiallyKnownDatesUtil.replaceUnknownDateParts(datumMitFormat);
		}

		if (!ValidationDateParser.isSyntacticallyCorrect(datumMitFormat)) {
			return werfeAllgemeinenFormatFehler(formatFuerFehlermeldungen, param);
		}
		Date date;
		try {
			// feine Überprüfung durch Parsen
			date = ValidationDateParser.parseDateWithoutSyntaxCheck(datumMitFormat);
		} catch (DateTimeParseException e) {
			return createDetailedDatumFormatFehler(datumMitFormat, formatFuerFehlermeldungen, param, fullCheck, null, null);
		}

		if (!fullCheck) {
			return ValidType.Valid;
		}

		// check if date generally valid
		if (dateInvalid(date, metaData.getTimeZone())) {
			return createFehler(param, DATE_FORMAT_PARAM_NAME, formatFuerFehlermeldungen,
					RuntimeFormalErrorEnum.DATUM,
					IFormaleFehlerConstants.FEHLER_DATUM);
		}

		return ValidType.Valid;
	}

	// For format MM/DD/YYYY'T'HH:mm:ss, getValueOfDatePart("04/31/2016T12:10:05", DatePart.YEAR) returns 2016T12:10:05.
	private static String getNumPart(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) < '0' || s.charAt(i) > '9') {
				return s.substring(0, i);
			}
		}
		return s;
	}

	protected ValidType createDetailedDatumFormatFehler(DateAndFormat datumMitFormat, String formatFuerFehlermeldungen,
			IValidierungParam param, boolean fullCheck, String formatFuerDatumBereichFehlermeldungen, FehlerBereich fehlerBereich) {
		int day;
		int month;
		int year;
		try {
			day = Integer.parseInt(getNumPart(PartiallyKnownDatesUtil.getValueOfDatePart(datumMitFormat, DatePart.DAY)));
			month = Integer.parseInt(getNumPart(PartiallyKnownDatesUtil.getValueOfDatePart(datumMitFormat, DatePart.MONTH)));
			year = Integer.parseInt(getNumPart(PartiallyKnownDatesUtil.getValueOfDatePart(datumMitFormat, DatePart.YEAR)));
		} catch (final NumberFormatException e) {
			if (fehlerBereich != null) {
				return ((FormatDefinitionDatumBereich) this).createFormatFehler(param, formatFuerDatumBereichFehlermeldungen);
			} else {
				return werfeAllgemeinenFormatFehler(formatFuerFehlermeldungen, param);
			}
		}
		if (day == 0 || month == 0 || year == 0) {
			if (fehlerBereich != null) {
				return ((FormatDefinitionDatumBereich) this).createFormatFehler(param, formatFuerDatumBereichFehlermeldungen);
			} else {
				return werfeAllgemeinenFormatFehler(formatFuerFehlermeldungen, param);
			}
		}
		if (month > 12) {
			List<Pair<String, String>> replacements = new ArrayList<>();
			replacements.add(new Pair<>(DATE_FORMAT_PARAM_NAME, formatFuerFehlermeldungen));
			replacements.add(new Pair<>(DATE_STRING_MONTH_PARAM_NAME, String.valueOf(month)));
			if (fehlerBereich != null) {
				replacements.add(new Pair<>(TIME_INTERVAL_FORMAT_PARAM_NAME, formatFuerDatumBereichFehlermeldungen));
				return createFehler(param, replacements,
						fehlerBereich == FehlerBereich.BEREICH_START_DATUM ?
								RuntimeFormalErrorEnum.DATUM_BEREICH_START_MONAT_FEHLER :
								RuntimeFormalErrorEnum.DATUM_BEREICH_END_MONAT_FEHLER,
						IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT);
			} else {
				return createFehler(param, replacements,
						RuntimeFormalErrorEnum.DATUM_MONAT_FEHLER,
						IFormaleFehlerConstants.FEHLER_DATUM_FORMAT);
			}
		}
		if (day == 29 && month == 2) {
			List<Pair<String, String>> replacements = new ArrayList<>();
			replacements.add(new Pair<>(DATE_FORMAT_PARAM_NAME, formatFuerFehlermeldungen));
			replacements.add(new Pair<>(DATE_STRING_PARAM_NAME, datumMitFormat.getDate()));
			replacements.add(new Pair<>(DATE_STRING_YEAR_PARAM_NAME, String.valueOf(year)));
			if (fehlerBereich != null) {
				replacements.add(new Pair<>(TIME_INTERVAL_FORMAT_PARAM_NAME, formatFuerDatumBereichFehlermeldungen));
				return createFehler(param, replacements,
						fehlerBereich == FehlerBereich.BEREICH_START_DATUM ?
								RuntimeFormalErrorEnum.DATUM_BEREICH_START_FEBRUAR_29_FEHLER :
								RuntimeFormalErrorEnum.DATUM_BEREICH_END_FEBRUAR_29_FEHLER,
						IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT);
			} else {
				return createFehler(param, replacements,
						RuntimeFormalErrorEnum.DATUM_FEBRUAR_29_FEHLER,
						IFormaleFehlerConstants.FEHLER_DATUM_FORMAT);
			}
		}
		List<Pair<String, String>> replacements = new ArrayList<>();
		replacements.add(new Pair<>(DATE_FORMAT_PARAM_NAME, formatFuerFehlermeldungen));
		replacements.add(new Pair<>(DATE_STRING_PARAM_NAME, datumMitFormat.getDate()));
		replacements.add(new Pair<>(DATE_STRING_DAY_PARAM_NAME, String.valueOf(day)));
		if (fehlerBereich != null) {
			replacements.add(new Pair<>(TIME_INTERVAL_FORMAT_PARAM_NAME, formatFuerDatumBereichFehlermeldungen));
			return createFehler(param, replacements,
					fehlerBereich == FehlerBereich.BEREICH_START_DATUM ?
							RuntimeFormalErrorEnum.DATUM_BEREICH_START_TAG_FEHLER :
							RuntimeFormalErrorEnum.DATUM_BEREICH_END_TAG_FEHLER,
					IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT);
		} else {
			return createFehler(param, replacements,
					RuntimeFormalErrorEnum.DATUM_TAG_FEHLER,
					IFormaleFehlerConstants.FEHLER_DATUM_FORMAT);
		}
	}

	private ValidType checkTeilbekanntSpezifischePruefungen(DateAndFormat datumMitFormat, IValidierungParam param,
			String formatFuerFehlermeldungen) {
		if (isTeilbekannt()) {
			if (!ValidationDateParser.isSyntacticallyCorrect(datumMitFormat)) {
				return createFehler(param, DATE_FORMAT_PARAM_NAME, formatFuerFehlermeldungen, RuntimeFormalErrorEnum.DATUM_FORMAT,
						IFormaleFehlerConstants.FEHLER_DATUM_FORMAT);
			}
			RuntimeFormalErrorEnum runtimeFormalError = TeilbekanntesDatumUtil.pruefeNichtBekannteDatumsteile(datumMitFormat,
					teilBekanntArt);
			String fehlertext = FehlerHandler.getFehlertext(runtimeFormalError, param.isExtmp(), param.getSprache());
			if (!StringUtils.isEmpty(fehlertext)) {
				return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_DATUM, fehlertext, param);
			}
		}
		return ValidType.Valid;
	}

	private ValidType werfeAllgemeinenFormatFehler(String formatFuerFehlermeldungen, IValidierungParam param) {
		return createFehler(param, DATE_FORMAT_PARAM_NAME, formatFuerFehlermeldungen, RuntimeFormalErrorEnum.DATUM_FORMAT,
				IFormaleFehlerConstants.FEHLER_DATUM_FORMAT);
	}

	/**
	 * Ersetzt die Zeichen "d" bzw "dd" durch "DD" (bzw. "TT" für Deutsch),
	 * "M" bzw. "MM" durch "MM", "y" durch "Y" (bzw. "J" für Deutsch) und "a" durch "AM/PM".
	 * In den Fehlermeldungen ist das Format nie mit "Nullen optional".<br>
	 * Bsp.: für die Sprache Deutsch wird "M/d" konvertiert zu "MM/TT", "yyyyMMdd" zu "JJJJMMTT"
	 * <p>
	 * ATTENTION: Should conform to CodeGenDateTypeImpl#convertPruefFormat2FehlermeldungFormat.
	 */
	protected String convertPruefFormat2FehlermeldungFormat(String s, Locale sprache) {
		s = s.replace("dd", "d").replace("d", "DD")
				.replace("MM", "M").replace("M", "MM")
				.replace("y", "Y").replace("a", "AM/PM");
		// Locale.GERMANY: Conversion for display of german dates
		if (Locale.GERMANY.equals(sprache)) {
			s = s.replace("D", "T").replace("Y", "J");
		}
		return s;
	}

	protected ValidType createFehler(IValidierungParam param, String paramName, String formatFuerFehlermeldungen,
			RuntimeFormalErrorEnum formalError, String fehlerId) {
		List<Pair<String, String>> replacements = new ArrayList<>();
		replacements.add(new Pair<>(paramName, formatFuerFehlermeldungen));
		return createFehler(param, replacements, formalError, fehlerId);
	}

	private ValidType createFehler(IValidierungParam param, List<Pair<String, String>> paramReplacements,
			RuntimeFormalErrorEnum formalError, String fehlerId) {
		String fehlerText = FehlerHandler.getFehlertext(formalError, param.isExtmp(), param.getSprache());
		for (var r : paramReplacements) {
			fehlerText = fehlerText.replace(r.getEntry1(), r.getEntry2());
		}
		return FehlerHandler.addFehler(fehlerId, fehlerText, param);
	}

	@Override
	public String getDefaultValue(IMetaDataValidierung metaData, PresentationInformation presentationInformation, Locale sprache) {
		return "";
	}

	public boolean isTeilbekannt() {
		return this.teilBekanntArt != DatumTeilbekanntArt.BEKANNT;
	}

	public DatumTeilbekanntArt getTeilbekanntArt() {
		return this.teilBekanntArt;
	}

	@Override
	public Object getWert(String key) {
		return switch (key) {
			case IMetaKeys.FIELD_MIN_LENGTH, IMetaKeys.FIELD_MAX_LENGTH -> laenge;
			case IMetaKeys.FIELD_FIELD_TYPE -> feldTyp;
			case IMetaKeys.FIELD_FORMAT -> metaFormat;
			case IMetaKeysInternal.FIELD_TEILBEKANNTART -> teilBekanntArt.toString();
			case null, default -> null;
		};
	}

	@Override
	public int compare(@NonNull String s1, @NonNull String s2, IMetaDataValidierung metaData) {
		return compare(format, s1, s2, metaData);
	}

	protected int compare(@NonNull String format, String s1, String s2, IMetaDataValidierung metaData) {
		Instant localDate1 = PartiallyKnownDatesUtil.createInstant(new DateAndFormat(s1, format, metaData.getTimeZone()));
		Instant localDate2 = PartiallyKnownDatesUtil.createInstant(new DateAndFormat(s2, format, metaData.getTimeZone()));
		return localDate1.compareTo(localDate2);
	}

	@Override
	public Object convertToBasicType(String internalValue, IMetaDataValidierung metaData) {
		if (StringUtils.isEmpty(internalValue)) {
			return null;
		}
		// partially known dates cannot be represented as a Date -> keep the raw internal value
		DateAndFormat datumMitFormat = new DateAndFormat(internalValue, format, metaData.getTimeZone());
		if (PartiallyKnownDatesUtil.isPartUnknown(datumMitFormat)) {
			return internalValue;
		}
		DateBasicConversionConfig config =
				createDateConversionConfig(format, metaData.getBaseYearOrDefault(), metaData.getTimeZone());
		return DateConversion.convertStringToDate(internalValue, config, internalDataFormats);
	}

	@Override
	public String convertFromBasicType(@NonNull Object value, @NonNull IMetaDataValidierung metaData) {
		DateBasicConversionConfig config =
				createDateConversionConfig(format, metaData.getBaseYearOrDefault(), metaData.getTimeZone());
		return DateConversion.convertDateToString(value, config, internalDataFormats).orElse(null);
	}
}
