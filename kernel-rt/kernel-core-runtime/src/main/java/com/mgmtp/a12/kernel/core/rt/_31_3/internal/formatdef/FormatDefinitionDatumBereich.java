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

import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DatumTeilbekanntArt;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidType;
import com.mgmtp.a12.utils.conversion.InterpretationOfYear;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateAndFormat;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil;
import com.mgmtp.a12.utils.conversion.a12internal.util.ValidationDateParser;
import com.mgmtp.a12.utils.conversion.config.DateRangeConversionConfig;
import com.mgmtp.a12.utils.conversion.internal.DateRangeConversion;
import com.mgmtp.a12.utils.conversion.internal.config.DateRangeConversionConfigBuilder;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.internal.DataFormatsBuilder;

import lombok.NonNull;

public class FormatDefinitionDatumBereich extends FormatDefinitionDatum implements IFormatDefinition {

	private String trenner;
	private final InterpretationOfYear interpretationOfYear;
	/** The single-endpoint internal (XML) {@link DataFormats} plus this range's XML separator. */
	private final DataFormats internalDataFormats;

	public FormatDefinitionDatumBereich(
			String javaFormat,
			String trenner,
			InterpretationOfYear interpretationOfYear) {
		super(javaFormat, DatumTeilbekanntArt.BEKANNT);
		this.trenner = trenner;
		this.laenge = javaFormat.length() * 2 + trenner.length();
		this.metaFormat = this.metaFormat + trenner + this.metaFormat;
		this.feldTyp = IFormatDefinition.FeldFormatEnumType.ER_DATUMBEREICH.toString();
		this.interpretationOfYear = interpretationOfYear;
		this.internalDataFormats = withDateRangeSeparator(getInternalDataFormats(javaFormat), trenner);
	}

	@Override
	public String convert(String wert,
			DarstellungsInformationIntern di,
			boolean vonXml, RawParamHandler4Convert additionalParameters) {
		if (wert.isEmpty()) {
			return wert;
		}
		DateRangeConversionConfig internalConfig = createDateRangeConversionConfig(format, di.getBaseYear(), di.getTimeZone());
		DateRangeConversionConfig displayConfig =
				createDateRangeConversionConfig(di.convertDatumFormatXml2Ofl(format), di.getBaseYear(), di.getTimeZone());
		DataFormats displayDataFormats = withDateRangeSeparator(di.getDateDisplayDataFormats(internalDataFormats),
				di.convertDatumBereichTrennerXML2Ofl(trenner));
		if (vonXml) {
			return DateRangeConversion.convert(wert, internalConfig, internalDataFormats, displayConfig, displayDataFormats);
		} else {
			return DateRangeConversion.convert(wert, displayConfig, displayDataFormats, internalConfig, internalDataFormats);
		}
	}

	@Override
	public ValidType validiereFormat(IValidierungParam param, IMetaDataValidierung metaData, boolean fullCheck) {
		if (!fullCheck && param.getWert().isEmpty()) {
			return ValidType.Valid;
		}

		// Anhand der Oberflächendarstellung und dem aktuellen Format
		// wird das Format ermittelt. Es muss unterschieden werden zwischen
		// dem Format, das für Fehlermeldungen verwendet wird und dem Format
		// das im Formatter zum prüfen verwendet wird. Die beiden Formate unterscheiden
		// sich in dem Fall, dass in dem Ursprungsformat keine Jahresangabe verwendet
		// wird.  Dann wird zum Prüfen eine Jahresangabe hinzugefügt, und diese Angabe
		// auch zum zu prüfenden Wert hinzugefügt.

		DarstellungsInformationIntern info = param.getDarstellungsInfo();
		final String pruefFormat = info.convertDatumFormatXml2Ofl(getFormat());

		String dbTrenner = info.convertDatumBereichTrennerXML2Ofl(trenner);
		String formatFuerFehlermeldungen = String.format("%s%s%s", pruefFormat, dbTrenner, pruefFormat);
		formatFuerFehlermeldungen = convertPruefFormat2FehlermeldungFormat(formatFuerFehlermeldungen, param.getSprache());
		String formatFuerDetailedDatumFehlermeldungen = convertPruefFormat2FehlermeldungFormat(pruefFormat, param.getSprache());

		String wert = param.getWert();
		if (!wert.contains(dbTrenner)) {
			return createTrennerFehltFehler(param, formatFuerFehlermeldungen);
		}

		//Trenne den Datumsbereich auf,  verwende den Oberflächentrenner.
		String[] datumBereich = DateUtil.splitStringBySeparator(wert, dbTrenner);
		if (datumBereich.length != 2 || datumBereich[0].length() == 0 || datumBereich[1].length() == 0) {
			return createFormatFehler(param, formatFuerFehlermeldungen);
		}

		DateAndFormat[] dates = DateUtil.getDateRange(wert, dbTrenner, pruefFormat, info.datumTrennerHintenOptional(format),
				metaData.getBaseYearOrDefault(), interpretationOfYear, metaData.getTimeZone());

		if (!ValidationDateParser.isSyntacticallyCorrect(dates[0]) ||
				!ValidationDateParser.isSyntacticallyCorrect(dates[1])) {
			return createFormatFehler(param, formatFuerFehlermeldungen);
		}
		Date dbStartDate;
		Date dbEndeDate;
		try {
			// feine Überprüfung durch Parsen
			dbStartDate = ValidationDateParser.parseDateWithoutSyntaxCheck(dates[0]);
		} catch (DateTimeParseException e) {
			return createDetailedDatumFormatFehler(dates[0], formatFuerDetailedDatumFehlermeldungen, param, fullCheck,
					formatFuerFehlermeldungen, FehlerBereich.BEREICH_START_DATUM);
		}
		try {
			// feine Überprüfung durch Parsen
			dbEndeDate = ValidationDateParser.parseDateWithoutSyntaxCheck(dates[1]);
		} catch (DateTimeParseException e) {
			return createDetailedDatumFormatFehler(dates[1], formatFuerDetailedDatumFehlermeldungen, param, fullCheck,
					formatFuerFehlermeldungen, FehlerBereich.BEREICH_END_DATUM);
		}

		if (!fullCheck) {
			return ValidType.Valid;
		}

		// gültiges Intervall ?
		if (DateUtil.dateBefore(dbEndeDate, dbStartDate)) {
			return createFehler(param, TIME_INTERVAL_FORMAT_PARAM_NAME, formatFuerFehlermeldungen,
					RuntimeFormalErrorEnum.DATUM_BEREICH_NICHT_GUELTIG,
					IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_NICHT_GUELTIG);
		}
		// check if date generally valid
		if (dateInvalid(dbStartDate, metaData.getTimeZone())) {
			return createFehler(param, TIME_INTERVAL_FORMAT_PARAM_NAME, formatFuerFehlermeldungen,
					RuntimeFormalErrorEnum.DATUM_BEREICH_DATUM,
					IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_DATUM);
		}
		return ValidType.Valid;
	}

	protected ValidType createFormatFehler(IValidierungParam param, String formatFuerFehlermeldungen) {
		return createFehler(param, TIME_INTERVAL_FORMAT_PARAM_NAME, formatFuerFehlermeldungen,
				RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT, IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_FORMAT);
	}

	private ValidType createTrennerFehltFehler(IValidierungParam param,
			String formatFuerFehlermeldungen) {
		return createFehler(param, TIME_INTERVAL_FORMAT_PARAM_NAME, formatFuerFehlermeldungen,
				RuntimeFormalErrorEnum.DATUM_BEREICH_FORMAT, IFormaleFehlerConstants.FEHLER_DATUM_BEREICH_TRENNER_FEHLT);
	}

	public String getTrenner() {
		return trenner;
	}

	public InterpretationOfYear getInterpretationOfYear() {
		return interpretationOfYear;
	}

	@Override
	public Object getWert(String key) {
		if (key.equals(IMetaKeysInternal.FIELD_TEILBEKANNTART)) {
			return null;
		}
		return super.getWert(key);
	}

	@Override
	public int compare(@NonNull String s1, @NonNull String s2, IMetaDataValidierung metaData) {
		String start1 = DateUtil.splitStringBySeparator(s1, trenner)[0];
		String start2 = DateUtil.splitStringBySeparator(s2, trenner)[0];
		return super.compare(start1, start2, metaData);
	}

	@Override
	public Object convertToBasicType(String internalValue, IMetaDataValidierung metaData) {
		if (StringUtils.isEmpty(internalValue)) {
			return null;
		}
		DateRangeConversionConfig config =
				createDateRangeConversionConfig(format, metaData.getBaseYearOrDefault(), metaData.getTimeZone());
		Object value = DateRangeConversion.convertStringToDateRange(internalValue, config, internalDataFormats, false)
				.value().orElse(null);
		return isArrayOfTwoDates(value) ? value : null;
	}

	private static boolean isArrayOfTwoDates(Object value) {
		return value instanceof Date[] dates && dates.length == 2 && dates[0] != null && dates[1] != null;
	}

	@Override
	public String convertFromBasicType(@NonNull Object value, @NonNull IMetaDataValidierung metaData) {
		DateRangeConversionConfig config =
				createDateRangeConversionConfig(format, metaData.getBaseYearOrDefault(), metaData.getTimeZone());
		return DateRangeConversion.convertDateRangeToString(value, config, internalDataFormats).orElse(null);
	}

	/**
	 * Builds the range conversion config describing this field (format + year interpretation) for the given base year
	 * and time zone. Mirrors the TypeScript {@code FormatDefinitionDatumBereich}.
	 */
	private DateRangeConversionConfig createDateRangeConversionConfig(String format, int baseYear, TimeZone timeZone) {
		DateRangeConversionConfigBuilder builder = new DateRangeConversionConfigBuilder();
		builder.withFormat(format);
		builder.withBaseYear(baseYear);
		builder.withTimeZone(timeZone);
		builder.withInterpretationOfYear(interpretationOfYear);
		return builder.build();
	}

	/**
	 * Copies {@code base} but overrides its date-range separator, so that the range converter reads the correct
	 * (internal vs. surface) separator. Mirrors the TypeScript {@code _getInternalDataFormats} / range display config.
	 */
	private static DataFormats withDateRangeSeparator(DataFormats base, String rangeSeparator) {
		return new DataFormatsBuilder(base).dateRangeSeparator(rangeSeparator).build();
	}

}
