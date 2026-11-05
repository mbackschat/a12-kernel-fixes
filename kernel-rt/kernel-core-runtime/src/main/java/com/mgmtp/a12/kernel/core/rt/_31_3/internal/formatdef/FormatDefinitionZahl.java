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

import static com.mgmtp.a12.utils.conversion.a12internal.util.NumberUtil.NUMBER_PATTERN;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.LocalizationHelper;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkBigDecimal;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.utils.conversion.a12internal.util.NumberUtil;
import com.mgmtp.a12.utils.conversion.config.NumberConversionConfig;
import com.mgmtp.a12.utils.conversion.internal.NumberConversion;
import com.mgmtp.a12.utils.conversion.internal.config.NumberConversionConfigBuilder;
import com.mgmtp.a12.utils.localization.DataFormats;
import com.mgmtp.a12.utils.localization.internal.DataFormatsBuilder;

import lombok.Getter;
import lombok.NonNull;

/**
 * Klasse modelliert zur Laufzeit ein Zahl-Feld.
 */
public class FormatDefinitionZahl implements IFormatDefinition {

	@Getter public enum ZahlArt {
		GELDBETRAG(PresentationInformation.TRAIT_AMOUNT),
		ZAHL(PresentationInformation.TRAIT_NONE);
		private final String trait;

		ZahlArt(String trait) {
			this.trait = trait;
		}
	}

	private static final int ZAHL_MIT_UNGUELTIGEN_ZEICHEN = 1;
	private static final int ZAHL_ZU_LANG = 2;
	private static final int ZAHL_ENTHAELT_VORZEICHEN = 3;
	private static final int ZAHL_ENTHAELT_PLUS = 4;
	private static final int ZAHL_IST_NULL = 5;
	private static final int ZAHL_HAT_FUEHRENDE_NULLEN = 6;
	private static final int ZAHL_HAT_DEZIMALTRENNER = 7;
	private static final int ZAHL_OHNE_DEZIMALTRENNER = 8;
	private static final int ZAHL_ZUVIELE_VORKOMMASTELLEN = 9;
	private static final int ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN = 10;
	private static final int ZAHL_NULL_ENTHAELT_VORZEICHEN = 11;
	private static final int ZAHL_ZU_KURZ = 12;
	private static final int ZAHL_KLEINER_MINWERT = 13;
	private static final int ZAHL_GROESSER_MAXWERT = 14;

	private static final String VORKOMMA_PATTERN = "[-]?[0-9]+";
	private static final String VORKOMMA_PATTERN_KLAMMER = "[-]?[0-9]+(";
	private static final String GKLAMMER_ZU = "}";
	private static final String GKLAMMER_ZU_OPT = "})?";
	private static final String ANGABE_ANZAHL = "[0-9]{";
	private static final String VORKOMMA_MAX = "[-]?[0-9]{1,";

	@Getter private final boolean signAllowed;
	private final boolean nullErlaubt;
	private final boolean fuehrendeNullenErlaubt;

	private Integer vorkommaStellenDisp = null;
	private int vorkommaStellen = -1;

	private Integer nachkommaStellenBisDisp = null;
	private int nachkommaStellenBis = -1;

	private Integer nachkommaStellenDisp = null;
	private int nachkommaStellen = -1;

	private final ZahlArt art;
	private Integer minLaenge = null;
	private final int minLaengeDisp;
	private Integer maxLaenge = null;
	private final int maxLaengeDisp;

	private VkBigDecimal minWert;
	private VkBigDecimal maxWert;
	// minWertDisp = der im ExtMP Editor angezeigte Wert für minWert
	private final String minWertDisp;
	// maxWertDisp = der im ExtMP Editor angezeigte Wert für maxWert
	private final String maxWertDisp;

	private String nullen = "";
	String feldTyp;

	// Patterns werden im Konstruktor gebildet, damit nachher die Überprüfung
	// schnell geht.
	private final Pattern[] anzahlNachkommastellenPattern = new Pattern[2];
	private final Pattern[] anzahlVarOptNachkommastellenPattern = new Pattern[2];
	private final Pattern[] anzahlVarNachkommastellenPattern = new Pattern[2];
	private final Pattern[] anzahlVorkommastellenPattern = new Pattern[2];

	private final Map<String, Pattern[]> thousandsSeparatorPatternCache = new HashMap<>();

	// Conversion config (min fractional digits + leading-zeros flag) shared by convert / convertToBasicType /
	// convertFromBasicType; built in init() once nachkommaStellen has its final value.
	private NumberConversionConfig conversionConfig;

	private static final Pattern[] zahlMitPlusPattern = {
			Pattern.compile("[+][0-9]+(\\.[0-9]+)?"),
			Pattern.compile("[+][0-9]+(,[0-9]+)?")
	};

	// Omitting backtracking hell (\d+(X?\d{3})*) -> \d+(X\d{3}(X?\d{3})*)?
	// We don't know what the thousands separator is, so we quote it with \Q\E
	// We want only one thousands separator parameter and one for the decimal separator,
	// so %1$s is the thousands separator and %2$s is the decimal separator
	// The pattern is similar to the DECIMAL_PLACES_OPTIONAL_PRECHECK_PATTERN
	private static final String THOUSANDS_SEPARATOR_PATTERN_TEMPLATE = "[-]?[0-9]+(%1$s[0-9]{3}((%1$s)?[0-9]{3})*)?(%2$s[0-9]*)?";

	public FormatDefinitionZahl(
			boolean vorzeichen, boolean nullErlaubt, boolean fuehrendeNullenErlaubt,
			int vorkomma, int nachkomma, int nachkommaBis, int minLaenge,
			int minLaengeDisp, int maxLaenge, int maxLaengeDisp,
			boolean istGeldbetrag, String minWert, String maxWert) {
		this.signAllowed = vorzeichen;
		this.nullErlaubt = nullErlaubt;
		this.fuehrendeNullenErlaubt = fuehrendeNullenErlaubt;

		this.vorkommaStellen = vorkomma;
		this.vorkommaStellenDisp = vorkomma;

		this.nachkommaStellen = nachkomma;
		this.nachkommaStellenDisp = nachkomma;

		this.nachkommaStellenBis = nachkommaBis;
		this.nachkommaStellenBisDisp = nachkommaBis;

		this.art = istGeldbetrag ? ZahlArt.GELDBETRAG : ZahlArt.ZAHL;
		this.minLaenge = minLaenge;
		this.minLaengeDisp = minLaengeDisp;
		this.maxLaenge = maxLaenge;
		this.maxLaengeDisp = maxLaengeDisp;

		this.minWertDisp = minWert;
		if (!this.minWertDisp.isEmpty()) {
			this.minWert = new VkBigDecimal(this.minWertDisp);
		}

		this.maxWertDisp = maxWert;
		if (!this.maxWertDisp.isEmpty()) {
			this.maxWert = new VkBigDecimal(this.maxWertDisp);
		}

		init();
	}

	public FormatDefinitionZahl(boolean hatVorzeichen2,
			boolean nullErlaubt2, boolean fuehrendeNullenErlaubt2,
			int vorkomma, int nachkomma, int nachkommaBis, ZahlArt zahl,
			int minLaenge2, int maxLaenge2, String minWert, String maxWert) {
		this.signAllowed = hatVorzeichen2;
		this.nullErlaubt = nullErlaubt2;
		this.fuehrendeNullenErlaubt = fuehrendeNullenErlaubt2;

		this.vorkommaStellen = vorkomma;
		this.vorkommaStellenDisp = -1;

		this.nachkommaStellen = nachkomma;
		this.nachkommaStellenDisp = -1;

		this.nachkommaStellenBis = nachkommaBis;
		this.nachkommaStellenBisDisp = -1;

		this.art = zahl;
		this.minLaenge = minLaenge2;
		this.minLaengeDisp = -1;
		this.maxLaenge = maxLaenge2;
		this.maxLaengeDisp = -1;

		this.minWertDisp = minWert;
		if (!StringUtils.isEmpty(this.minWertDisp)) {
			this.minWert = new VkBigDecimal(this.minWertDisp);
		}

		this.maxWertDisp = maxWert;
		if (!StringUtils.isEmpty(this.maxWertDisp)) {
			this.maxWert = new VkBigDecimal(this.maxWertDisp);
		}

		init();
	}

	private void init() {
		if (art == ZahlArt.GELDBETRAG) {
			if (nachkommaStellen == 0) {
				this.feldTyp = "GeldBetragOhneCent";
			} else {
				this.feldTyp = "GeldBetragMitCent";
			}
		} else {
			this.feldTyp = "Zahl";
		}

		//in diesem Fall muss nachkommaStellenBis > 0 sein und entspricht der oberen
		//Grenze der Nachkommastellen, vgl. EXT-5094
		if (nachkommaStellen == -1) {
			nachkommaStellenDisp = 0;
		}

		//in diesem Fall muss nachkommaStellen >= 0 sein und entspricht der exakten
		//Anzahl der Nachkommastellen, vgl. EXT-5094
		if (nachkommaStellenBis == -1) {
			nachkommaStellenBisDisp = nachkommaStellenDisp;
		}
		if (vorkommaStellen == -1) {
			vorkommaStellenDisp = null;
		}

		// Vorbelegung der patterns, damit dies nur einmal beim Einlesen gemacht
		// werden muss.
		if (nachkommaStellen > 0 && (nachkommaStellenBis == nachkommaStellen || nachkommaStellenBis == -1)) {
			for (int i = 0; i < 2; i++) {
				StringBuilder pBuf = new StringBuilder(VORKOMMA_PATTERN);

				pBuf.append(DarstellungsInformationIntern.getDezimalTrennerForPattern(i));
				pBuf.append(ANGABE_ANZAHL);
				pBuf.append(nachkommaStellen);
				pBuf.append(GKLAMMER_ZU);
				anzahlNachkommastellenPattern[i] = Pattern.compile(pBuf.toString());
			}
			StringBuilder buf = new StringBuilder();
			buf.append("0".repeat(nachkommaStellen));
			nullen = buf.toString();
		}

		if (nachkommaStellenBis > 0 && (nachkommaStellen == 0 || nachkommaStellen == -1)) {
			this.nachkommaStellen = 0;
			nachkommaStellen = 0;
			for (int i = 0; i < 2; i++) {
				StringBuilder pBuf = new StringBuilder(VORKOMMA_PATTERN_KLAMMER);
				pBuf.append(DarstellungsInformationIntern.getDezimalTrennerForPattern(i));
				pBuf.append(ANGABE_ANZAHL);
				pBuf.append(this.nachkommaStellen);
				pBuf.append(",");
				pBuf.append(nachkommaStellenBis);
				pBuf.append(GKLAMMER_ZU_OPT);
				anzahlVarOptNachkommastellenPattern[i] = Pattern.compile(pBuf.toString());
			}
		}
		if (nachkommaStellenBis > 0 && nachkommaStellen > 0) {
			for (int i = 0; i < 2; i++) {
				StringBuilder pBuf = new StringBuilder(VORKOMMA_PATTERN);
				pBuf.append(DarstellungsInformationIntern.getDezimalTrennerForPattern(i));
				pBuf.append(ANGABE_ANZAHL);
				pBuf.append(nachkommaStellen);
				pBuf.append(",");
				pBuf.append(nachkommaStellenBis);
				pBuf.append(GKLAMMER_ZU);
				anzahlVarNachkommastellenPattern[i] = Pattern.compile(pBuf.toString());
			}
		}
		if (vorkommaStellen > 0) {
			StringBuilder pBuf = new StringBuilder(VORKOMMA_MAX);
			pBuf.append(vorkommaStellen);
			pBuf.append(GKLAMMER_ZU);
			anzahlVorkommastellenPattern[0] = Pattern.compile(pBuf.toString());
			anzahlVorkommastellenPattern[1] = anzahlVorkommastellenPattern[0];
		}

		this.conversionConfig = new NumberConversionConfigBuilder()
				.withMinFractionalDigits(nachkommaStellen)
				.withLeadingZerosAllowed(fuehrendeNullenErlaubt)
				.build();
	}

	/**
	 * Konvertierungsmethode: es wird von der Oberflächendarstellung in die XML
	 * Darstellung konvertiert.
	 * Die Art des Dezimaltrenners ist zum Codegenerierungszeitpunkt bekannt und
	 * ist daher fest in der Konvertierungsmethode implementiert.
	 */
	@Override
	public String convert(String wert,
			DarstellungsInformationIntern darstellungsInfo,
			boolean vonXml, RawParamHandler4Convert additionalParameters) {

		if (darstellungsInfo == null) {
			return wert;
		}

		DataFormats internalDataFormats = numberDataFormats(darstellungsInfo.getDezimalTrennerIntern(), false, null);
		DataFormats displayDataFormats = numberDataFormats(darstellungsInfo.getDezimalTrenner(),
				darstellungsInfo.areDecimalPlacesOptional(),
				darstellungsInfo.getThousandsSeparatorForTrait(art.getTrait()));
		if (vonXml) {
			return NumberConversion.convert(wert, conversionConfig, internalDataFormats, conversionConfig, displayDataFormats);
		} else {
			return NumberConversion.convert(wert, conversionConfig, displayDataFormats, conversionConfig, internalDataFormats);
		}
	}

	/**
	 * Builds a {@link DataFormats} carrying the number layout (decimal / thousands separator, optional decimal
	 * places) the {@link NumberConversion} reads; the {@code minFractionalDigits} / leading-zeros flags live in
	 * {@link #conversionConfig}.
	 */
	private static DataFormats numberDataFormats(char decimalSeparator, boolean decimalPlacesOptional,
			String thousandsSeparator) {
		return new DataFormatsBuilder()
				.decimalSeparator(decimalSeparator)
				.thousandsSeparator(StringUtils.isEmpty(thousandsSeparator) ? null : thousandsSeparator)
				.decimalPlacesOptional(decimalPlacesOptional)
				.build();
	}

	/**
	 * Der Default wird verwendet, wenn in einem Fehlertext auf ein Wert zugegriffen wird,
	 * in dem Feld aber gar kein Wert übergeben wurde.
	 */
	@Override
	public String getDefaultValue(IMetaDataValidierung metaData, PresentationInformation presentationInformation, Locale sprache) {
		StringBuilder result = new StringBuilder("0");
		if (!nullen.isEmpty()) {
			DarstellungsInformationIntern darstellungsInformationIntern = new DarstellungsInformationIntern(
					metaData.getDezimalTrenner(), presentationInformation, metaData.getBaseYearOrDefault(),
					metaData.getDefaultPresentationInformation().get(sprache), metaData.getTimeZone());
			result.append(darstellungsInformationIntern.getDezimalTrenner());
			result.append(nullen);
		}

		return result.toString();
	}

	/**
	 * allgemeine Methode zum Validieren des Eingabewertes wert.
	 */
	private RuntimeFormalErrorEnum getFehlerIdFuerZahlen(int error) {
		return switch (error) {
			case ZAHL_ENTHAELT_PLUS -> isSignAllowed() ?
					RuntimeFormalErrorEnum.ZAHL_ENTHAELT_PLUS :
					RuntimeFormalErrorEnum.ZAHL_ENTHAELT_VORZEICHEN;
			case ZAHL_MIT_UNGUELTIGEN_ZEICHEN, ZAHL_OHNE_DEZIMALTRENNER, ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN -> {

				// ohne Nachkommastellen
				if (nachkommaStellen == 0 && (nachkommaStellenBis == nachkommaStellen || nachkommaStellenBis == -1)) {
					yield RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_ONK;
				}

				// variable Nachkommastellen
				if (nachkommaStellenBis > 0 && nachkommaStellenBis != nachkommaStellen) {
					yield RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_VNK;
				}

				// fixe Nachkommastellen
				if (nachkommaStellen > 0 && (nachkommaStellenBis == nachkommaStellen || nachkommaStellenBis == -1)) {
					yield isSignAllowed() ?
							RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKPVZ :
							RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN_FNKOVZ;
				}
				// über Nachkommastellen wurde keine Aussage getroffen.
				yield RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN;
			}
			case ZAHL_ENTHAELT_VORZEICHEN -> RuntimeFormalErrorEnum.ZAHL_ENTHAELT_VORZEICHEN;
			case ZAHL_HAT_DEZIMALTRENNER -> RuntimeFormalErrorEnum.ZAHL_HAT_DEZIMALTRENNER;
			case ZAHL_ZUVIELE_VORKOMMASTELLEN -> {
				if (nachkommaStellen == 0 && nachkommaStellenBis < 1) {
					yield isSignAllowed() ?
							RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZONK :
							RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZONK;
				} else {
					yield isSignAllowed() ?
							RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_MVZ :
							RuntimeFormalErrorEnum.ZAHL_ZUVIELE_VORKOMMASTELLEN_OVZ;
				}
			}
			case ZAHL_IST_NULL -> isSignAllowed() ? RuntimeFormalErrorEnum.ZAHL_IST_NULL : RuntimeFormalErrorEnum.ZAHL_IST_NULL_OVZ;
			case ZAHL_NULL_ENTHAELT_VORZEICHEN -> RuntimeFormalErrorEnum.ZAHL_NULL_ENTHAELT_VORZEICHEN;
			case ZAHL_HAT_FUEHRENDE_NULLEN -> RuntimeFormalErrorEnum.ZAHL_HAT_FUEHRENDE_NULLEN;
			case ZAHL_ZU_LANG -> RuntimeFormalErrorEnum.ZAHL_ZU_LANG;
			case ZAHL_ZU_KURZ -> RuntimeFormalErrorEnum.ZAHL_ZU_KURZ;
			case ZAHL_KLEINER_MINWERT -> RuntimeFormalErrorEnum.ZAHL_ZU_KLEIN;
			case ZAHL_GROESSER_MAXWERT -> RuntimeFormalErrorEnum.ZAHL_ZU_GROSS;
			default -> RuntimeFormalErrorEnum.ZAHL_MIT_UNGUELTIGEN_ZEICHEN;
		};
	}

	private RuntimeFormalErrorEnum getFehlerIdGBMC(int error) {
		return switch (error) {
			case ZAHL_ENTHAELT_PLUS -> isSignAllowed() ?
					RuntimeFormalErrorEnum.GBMC_ENTHAELT_PLUS :
					RuntimeFormalErrorEnum.GBMC_ENTHAELT_VORZEICHEN;
			case ZAHL_MIT_UNGUELTIGEN_ZEICHEN, ZAHL_OHNE_DEZIMALTRENNER, ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN -> isSignAllowed() ?
					RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_MVZ :
					RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_OVZ;
			case ZAHL_ENTHAELT_VORZEICHEN -> RuntimeFormalErrorEnum.GBMC_ENTHAELT_VORZEICHEN;
			case ZAHL_NULL_ENTHAELT_VORZEICHEN -> RuntimeFormalErrorEnum.GBMC_NULL_ENTHAELT_VORZEICHEN;
			case ZAHL_ZUVIELE_VORKOMMASTELLEN -> isSignAllowed() ?
					RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_MVZ :
					RuntimeFormalErrorEnum.GBMC_ZUVIELE_VORKOMMASTELLEN_OVZ;
			case ZAHL_IST_NULL -> isSignAllowed() ? RuntimeFormalErrorEnum.ZAHL_IST_NULL : RuntimeFormalErrorEnum.ZAHL_IST_NULL_OVZ;
			case ZAHL_HAT_FUEHRENDE_NULLEN -> RuntimeFormalErrorEnum.GBMC_HAT_FUEHRENDE_NULLEN;
			case ZAHL_ZU_LANG -> RuntimeFormalErrorEnum.GBMC_ZU_LANG;
			case ZAHL_ZU_KURZ -> RuntimeFormalErrorEnum.GBMC_ZU_KURZ;
			case ZAHL_KLEINER_MINWERT -> RuntimeFormalErrorEnum.GBMC_ZU_KLEIN;
			case ZAHL_GROESSER_MAXWERT -> RuntimeFormalErrorEnum.GBMC_ZU_GROSS;
			default -> RuntimeFormalErrorEnum.GBMC_MIT_UNGUELTIGEN_ZEICHEN_OVZ;
		};
	}

	private RuntimeFormalErrorEnum getFehlerIdGBOC(int error) {
		return switch (error) {
			case ZAHL_ENTHAELT_PLUS ->
					isSignAllowed() ? RuntimeFormalErrorEnum.GBOC_ENTHAELT_PLUS : RuntimeFormalErrorEnum.GBOC_ENTHAELT_VORZEICHEN;
			case ZAHL_MIT_UNGUELTIGEN_ZEICHEN, ZAHL_HAT_DEZIMALTRENNER -> isSignAllowed() ?
					RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_MVZ :
					RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_OVZ;
			case ZAHL_ENTHAELT_VORZEICHEN -> RuntimeFormalErrorEnum.GBOC_ENTHAELT_VORZEICHEN;
			case ZAHL_ZUVIELE_VORKOMMASTELLEN -> isSignAllowed() ?
					RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_MVZ :
					RuntimeFormalErrorEnum.GBOC_ZUVIELE_VORKOMMASTELLEN_OVZ;
			case ZAHL_IST_NULL -> isSignAllowed() ? RuntimeFormalErrorEnum.ZAHL_IST_NULL : RuntimeFormalErrorEnum.ZAHL_IST_NULL_OVZ;
			case ZAHL_NULL_ENTHAELT_VORZEICHEN -> RuntimeFormalErrorEnum.GBOC_NULL_ENTHAELT_VORZEICHEN;
			case ZAHL_HAT_FUEHRENDE_NULLEN -> RuntimeFormalErrorEnum.GBOC_HAT_FUEHRENDE_NULLEN;
			case ZAHL_ZU_LANG -> RuntimeFormalErrorEnum.GBOC_ZU_LANG;
			case ZAHL_ZU_KURZ -> RuntimeFormalErrorEnum.GBOC_ZU_KURZ;
			case ZAHL_KLEINER_MINWERT -> RuntimeFormalErrorEnum.GBOC_ZU_KLEIN;
			case ZAHL_GROESSER_MAXWERT -> RuntimeFormalErrorEnum.GBOC_ZU_GROSS;
			default -> isSignAllowed() ?
					RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_MVZ :
					RuntimeFormalErrorEnum.GBOC_MIT_UNGUELTIGEN_ZEICHEN_OVZ;
		};
	}

	private String getFehlerText(
			int error,
			IValidierungParam param) {

		RuntimeFormalErrorEnum fehlerId;
		//tempcobertura::0.66::tempcobertura
		switch (art) {
			case ZAHL -> fehlerId = getFehlerIdFuerZahlen(error);
			case GELDBETRAG -> fehlerId = (nachkommaStellen == 2) ? getFehlerIdGBMC(error) : getFehlerIdGBOC(error);

			//tempcobertura::0.00::tempcobertura
			case null, default -> throw new IllegalArgumentException("Not Implemented - " + art);
		}

		Locale sprache = param.getSprache();
		String fehlerText = FehlerHandler.getFehlertext(fehlerId, param.isExtmp(), param.getSprache());
		fehlerText = fehlerText.replace("<decimalSeparator>", String.valueOf(param.getDarstellungsInfo().getDezimalTrenner()));

		String decimalSeparatorInvalid = "";
		String oflMinWert = minWertDisp;
		String oflMaxWert = maxWertDisp;
		// if presentation info is . -> Error for ,
		if (param.getDarstellungsInfo().getDezimalTrenner() == '.') {
			decimalSeparatorInvalid = LocalizationHelper.getValue(LocalizationHelper.COMMA_PLURAL, sprache);
		} else {
			decimalSeparatorInvalid = LocalizationHelper.getValue(LocalizationHelper.POINT_PLURAL, sprache);
			oflMinWert = minWertDisp.replace('.', param.getDarstellungsInfo().getDezimalTrenner());
			oflMaxWert = maxWertDisp.replace('.', param.getDarstellungsInfo().getDezimalTrenner());
		}

		fehlerText = fehlerText.replace("<decimalSeparatorInvalid>", decimalSeparatorInvalid);
		fehlerText = fehlerText.replace("<zeros>", nullen);
		fehlerText = fehlerText.replace("<numberOfDecimalPlaces>", Integer.toString(nachkommaStellen));
		fehlerText = fehlerText.replace("<maxNumberOfDecimalPlaces>", Integer.toString(nachkommaStellenBis));
		fehlerText = fehlerText.replace("<numberOfDigitsBeforeDecimalPoint>", Integer.toString(vorkommaStellen));
		fehlerText = fehlerText.replace("<minLength>", Integer.toString(minLaenge));
		fehlerText = fehlerText.replace("<maxLength>", Integer.toString(maxLaenge));
		fehlerText = fehlerText.replace("<minValue>", oflMinWert);
		fehlerText = fehlerText.replace("<maxValue>", oflMaxWert);

		fehlerText = FehlerHandler.replaceFeldName(fehlerText, param.getDaten(), param.getIdentifier());

		return fehlerText;
	}

	@Override
	public ValidType validiereFormat(IValidierungParam param, IMetaDataValidierung metaData, boolean fullCheck) {
		if (!fullCheck && param.getWert().isEmpty()) {
			// this case is for the base type conversion -> an empty value will be converted to null later on
			return ValidType.Valid;
		}

		DarstellungsInformationIntern darstellungsInfo = param.getDarstellungsInfo();
		IIdentifier id = param.getIdentifier();
		IResult erg = param.getErgebnis();
		String wert = param.getWert();
		boolean nurPruefen = param.isNurPruefen();

		// Mit Hilfe des Parameters darstellungsInfo wird die Darstellungsinformation
		// zu dem Dezimaltrenner geholt.
		char decimalSeparatorChar = darstellungsInfo.getDezimalTrenner();
		String dezimalTrenner = String.valueOf(decimalSeparatorChar);

		// In den vereinbarten Patterns ist jeweils eine Option für Kommadarstellung
		// und eine Option für Punktdarstellung generiert.
		// Im Index wird nun gespeichert, ob die Komma oder Punkt Notation verwendet wird.
		// Dazu wird aus der Darstellungsinformation der richtige Index geholt.
		int index = darstellungsInfo.getDezimalIndexFuerValidierung();

		String thousandsSeparator = darstellungsInfo.getThousandsSeparatorForTrait(art.getTrait());
		if (!StringUtils.isEmpty(thousandsSeparator)) {
			Pattern thousandsSeparatorPattern = getThousandsSeparatorPattern(thousandsSeparator, index);
			if (param.isNonLenientConversion() && !thousandsSeparatorPattern.matcher(wert).matches()) {
				String fehlerText = getFehlerText(ZAHL_MIT_UNGUELTIGEN_ZEICHEN, param);
				return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_MIT_UNGUELTIGEN_ZEICHEN, fehlerText, param);
			}
			wert = wert.replaceAll(Pattern.quote(thousandsSeparator), "");
		}

		if (darstellungsInfo.areDecimalPlacesOptional()) {
			wert = NumberUtil.addDecimalPlacesIfNecessary(wert, dezimalTrenner, index, nachkommaStellen);
		}

		//Enthaelt Wert ungültiges Zeichen ?
		//zwar ist es eine Zahl, aber mit einem Plus-Vorzeichen
		if (zahlMitPlusPattern[index].matcher(wert).matches()) {
			String fehlerText = getFehlerText(ZAHL_ENTHAELT_PLUS, param);
			return FehlerHandler.addFehler(erg, IFormaleFehlerConstants.FEHLER_ZAHL_ENTHAELT_PLUS, id, fehlerText, nurPruefen);
		}
		if (!NUMBER_PATTERN[index].matcher(wert).matches()) {
			String fehlerText = getFehlerText(ZAHL_MIT_UNGUELTIGEN_ZEICHEN, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_MIT_UNGUELTIGEN_ZEICHEN, fehlerText, param);
		}

		// Wir wollen nur die Stelligkeit überprüfen, die 15 ist
		// Dezimaltrenner und Vorzeichen müssen rausgefiltert werden
		String stelligkeit = wert.replace(dezimalTrenner, "").replace("-", "");
		if (stelligkeit.length() > 15) {
			String fehlerText = FehlerHandler.getFehlertext(RuntimeFormalErrorEnum.STELLIGKEIT_ZU_LANG,
					param.isExtmp(), param.getSprache());
			return FehlerHandler.addFehler(IFormaleFehlerConstants.STELLIGKEIT_ZAHL_ZU_LANG, fehlerText, param);
		}

		if (!fullCheck) {
			return ValidType.Valid;
		}

		VkBigDecimal d = new VkBigDecimal(wert.replace(dezimalTrenner, "."));

		// Null enthält ein Vorzeichen
		if (VkBigDecimal.ZAHL_0.compareTo(d) == 0 && nullErlaubt && signAllowed && wert.contains("-")) {
			String fehlerText = getFehlerText(ZAHL_NULL_ENTHAELT_VORZEICHEN, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_NULL_ENTHAELT_VORZEICHEN, fehlerText, param);
		}

		//Zahl enthält Vorzeichen, obwohl ohne Vorzeichen definiert?
		if (!signAllowed && wert.contains("-")) {
			if (VkBigDecimal.ZAHL_0.compareTo(d) == 0 && !nullErlaubt && art.equals(ZahlArt.GELDBETRAG)) {
				String fehlerText = getFehlerText(ZAHL_IST_NULL, param);
				return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_IST_NULL, fehlerText, param);
			}
			String fehlerText = getFehlerText(ZAHL_ENTHAELT_VORZEICHEN, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_ENTHAELT_VORZEICHEN, fehlerText, param);
		}

		if (nachkommaStellen == 0 && nachkommaStellenBis <= 0 && wert.contains(dezimalTrenner)) {
			// darf kein "," im String sein
			String fehlerText = getFehlerText(ZAHL_HAT_DEZIMALTRENNER, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_HAT_DEZIMALTRENNER, fehlerText, param);
		}

		if (nachkommaStellen > 0 && !wert.contains(dezimalTrenner)) {
			// es muss ein "," im String sein
			String fehlerText = getFehlerText(ZAHL_OHNE_DEZIMALTRENNER, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_OHNE_DEZIMALTRENNER, fehlerText, param);
		}
		if (nachkommaStellen > 0 && nachkommaStellenBis == -1
				&& !anzahlNachkommastellenPattern[index].matcher(wert).matches()) {
			String fehlerText = getFehlerText(ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN, fehlerText, param);
		}

		if (nachkommaStellenBis > 0 && nachkommaStellen == 0
				&& !anzahlVarOptNachkommastellenPattern[index].matcher(wert).matches()) {
			String fehlerText = getFehlerText(ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN, fehlerText, param);

		}
		if (nachkommaStellenBis > 0 && nachkommaStellen > 0
				&& !anzahlVarNachkommastellenPattern[index].matcher(wert).matches()) {
			String fehlerText = getFehlerText(ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_FALSCHE_ANZAHL_NACHKOMMASTELLEN, fehlerText, param);
		}

		// Vorkommastellen Check
		if (vorkommaStellen > -1) {
			// bei einer komma-Zahl nimm nur den vorderen Wert zum Überprüfen
			int indexDezimalTrenner = wert.indexOf(dezimalTrenner);
			String w = (indexDezimalTrenner > -1) ? wert.substring(0, indexDezimalTrenner) : wert;
			if (!anzahlVorkommastellenPattern[index].matcher(w).matches()) {
				String fehlerText = getFehlerText(ZAHL_ZUVIELE_VORKOMMASTELLEN, param);
				return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_ZUVIELE_VORKOMMASTELLEN, fehlerText, param);
			}
		}
		if (VkBigDecimal.ZAHL_0.compareTo(d) == 0 && !nullErlaubt) {
			String fehlerText = getFehlerText(ZAHL_IST_NULL, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_IST_NULL, fehlerText, param);
		}

		// Fängt mit Null an, und ist länger als 1 Zeichen bzw. keine Null
		// vor der Vorkommastelle. -> Fehler
		if (!fuehrendeNullenErlaubt && NumberUtil.containsLeadingZeros(wert, decimalSeparatorChar)) {
			String fehlerText = getFehlerText(ZAHL_HAT_FUEHRENDE_NULLEN, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_HAT_FUEHRENDE_NULLEN, fehlerText, param);
		}

		if (minLaenge != -1 && wert.length() < minLaenge) {
			String fehlerText = getFehlerText(ZAHL_ZU_KURZ, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_ZU_KURZ, fehlerText, param);
		}

		if (maxLaenge != -1 && wert.length() > maxLaenge) {
			String fehlerText = getFehlerText(ZAHL_ZU_LANG, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_ZU_LANG, fehlerText, param);
		}
		if (minWert != null && minWert.compareTo(d) > 0) {
			String fehlerText = getFehlerText(ZAHL_KLEINER_MINWERT, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_ZU_KLEIN, fehlerText, param);
		}
		if (maxWert != null && maxWert.compareTo(d) < 0) {
			String fehlerText = getFehlerText(ZAHL_GROESSER_MAXWERT, param);
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_ZAHL_ZU_GROSS, fehlerText, param);
		}

		return ValidType.Valid;
	}

	private Pattern getThousandsSeparatorPattern(String thousandsSeparator, int index) {
		Pattern[] patterns = thousandsSeparatorPatternCache.get(thousandsSeparator);
		if (patterns == null) {
			patterns = new Pattern[2];
			thousandsSeparatorPatternCache.put(thousandsSeparator, patterns);
		}
		if (patterns[index] == null) {
			patterns[index] = createThousandsSeparatorPattern(thousandsSeparator, index);
			return patterns[index];
		} else {
			return patterns[index];
		}
	}

	private Pattern createThousandsSeparatorPattern(String thousandsSeparator, int index) {
		String thousandsSeparatorPatternString = Pattern.quote(thousandsSeparator);
		String decimalSeparatorPatternString = DarstellungsInformationIntern.getDezimalTrennerForPattern(index);
		return Pattern.compile(String.format(THOUSANDS_SEPARATOR_PATTERN_TEMPLATE,
				thousandsSeparatorPatternString,
				decimalSeparatorPatternString));
	}

	@Override
	public Object getWert(String key) {
		return switch (key) {
			case IMetaKeys.FIELD_MIN_LENGTH -> minLaengeDisp;
			case IMetaKeys.FIELD_MAX_LENGTH -> maxLaengeDisp;
			case IMetaKeys.FIELD_FIELD_TYPE -> feldTyp;
			case IMetaKeys.FIELD_DECIMAL_PLACES -> nachkommaStellenDisp;
			case IMetaKeys.FIELD_INTEGER_DIGIT -> vorkommaStellenDisp;
			case IMetaKeys.FIELD_MAX_DECIMAL_PLACES -> nachkommaStellenBisDisp;
			case IMetaKeys.FIELD_ZERO_ALLOWED -> nullErlaubt;
			case IMetaKeys.FIELD_LEADING_ZEROS -> fuehrendeNullenErlaubt;
			case IMetaKeys.FIELD_SIGN -> signAllowed;
			case null, default -> null;
		};
	}

	public boolean hasDecimalPlaces() {
		return nachkommaStellenBis > 0;
	}

	@Override
	public int compare(@NonNull String s1, @NonNull String s2, IMetaDataValidierung metaData) {
		// number fields are numerically sorted
		BigDecimal bd1 = new BigDecimal(s1);
		BigDecimal bd2 = new BigDecimal(s2);
		return bd1.compareTo(bd2);
	}

	@Override
	public Object convertToBasicType(String value, IMetaDataValidierung metaData) {
		return NumberConversion.convertStringToNumber(value, conversionConfig,
				numberDataFormats(metaData.getDezimalTrenner(), false, null));
	}

	@Override
	public String convertFromBasicType(@NonNull Object value, @NonNull IMetaDataValidierung metaData) {
		if (value instanceof String strVal && strVal.isEmpty()) {
			return null;
		}
		return NumberConversion.convertNumberToString(value, conversionConfig,
				numberDataFormats(metaData.getDezimalTrenner(), false, null));
	}
}
