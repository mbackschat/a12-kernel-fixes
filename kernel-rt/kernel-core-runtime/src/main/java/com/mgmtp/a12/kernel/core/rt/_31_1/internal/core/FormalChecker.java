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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.core;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.FormatDefinitionString;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.ValidierungParam;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.ErrorCause;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.LegalCharTester;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RuntimeUtils;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.TrieNode;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult.ErrorType;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;

import lombok.NonNull;

/**
 * Provides methods for formal checks of the input data.
 */
public class FormalChecker {
	private static final int MAX_WERT_LAENGE = 99999; // Die technische Laengenbegrenzung einzelner Werte
	private static final Pattern UNICODE_PATTERN = Pattern.compile("\\X");

	private final @NonNull IMetaDataValidierung metaData;
	private final @NonNull DarstellungsInformationIntern presentationInfo;
	private final @NonNull ValidationMode modus;
	private final @NonNull IData data;
	private final boolean meldeFehler;

	private final @NonNull TrieNode legalGraphemeTrie;
	private final @NonNull LegalCharTester legalCharTester;
	private final @NonNull DarstellungsInformationIntern presentationInfoIntern;

	public FormalChecker(
			@NonNull IMetaDataValidierung metaData,
			@NonNull DarstellungsInformationIntern presentationInfo,
			@NonNull ValidationMode modus,
			@NonNull IData data,
			boolean meldeFehler
	) {
		this.metaData = metaData;
		this.presentationInfo = presentationInfo;
		this.modus = modus;
		this.meldeFehler = meldeFehler;
		this.data = data;

		this.legalGraphemeTrie = metaData.getLegalGraphemeTrie();
		this.legalCharTester = new LegalCharTester(metaData.getLegalCodepoints(), legalGraphemeTrie,
				metaData.isLegalCharCheckDeactivated());
		this.presentationInfoIntern = DarstellungsInformationIntern.createInternalRepresentation(metaData);

	}

	/**
	 * @return `false` if the number of levels does not match the number of indices (and reports an error in `result`), `true`
	 * 		otherwise.
	 */
	private boolean checkNumIndicesCorrect(
			IIdentifier idExtern,
			int numLevels,
			IResult result
	) {
		long[] indices = idExtern.getIndices();
		if (indices.length != numLevels) {
			String ft = getFehlertext(RuntimeFormalErrorEnum.FALSCHE_ANZAHL_INDICES_FM_TEXT)
					.replace("<fieldPathName>", idExtern.getName())
					.replace("<numberOfIndicesSpecified>", "" + indices.length)
					.replace("<numberOfIndicesExpected>", "" + numLevels);
			addFormalError(result, idExtern,
					IFormaleFehlerConstants.FEHLER_FALSCHE_ANZAHL_INDICES, ft);
			return false;
		}
		return true;
	}

	/**
	 * @return `false` if the indices were invalid (and reports an error in `result`), `true` otherwise
	 */
	public boolean checkIndices(
			IIdentifier idExtern,
			List<Long> maxIndices,
			IResult result
	) {
		if (!checkNumIndicesCorrect(idExtern, maxIndices.size(), result)) {
			return false;
		}
		if (!checkIndicesLargerThanZero(idExtern, result)) {
			return false;
		}
		if (!checkIndicesLessOrEqualThanMax(idExtern, maxIndices, result)) {
			return false;
		}
		return true;
	}

	/**
	 * @return `false` if any of the indices are smaller than `1` (and reports an error in `result`), `true` otherwise.
	 */
	private boolean checkIndicesLargerThanZero(IIdentifier idExtern, IResult result) {
		long[] indices = idExtern.getIndices();
		if (indices[0] < 1) {//erster Index ist die laufende Nummer Vordruck
			addFormalError(result, idExtern,
					IFormaleFehlerConstants.FEHLER_LAUFENDE_NR_ZU_KLEIN,
					getFehlertext(RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_FM_TEXT));
			return false;
		}
		for (int i = 1; i < indices.length; i++) {
			if (indices[i] < 1) {
				//letzter Index ist die Zeile
				if (i == indices.length - 1) {
					addFormalError(result, idExtern,
							IFormaleFehlerConstants.FEHLER_ZEILENNUMMER_ZU_KLEIN,
							getFehlertext(RuntimeFormalErrorEnum.FEHLER_ZEILE_FM_TEXT));
				} else {//wiederholbarer Kontext
					String fehlerText = getFehlertext(RuntimeFormalErrorEnum.KONTEXTNUMMER_ZU_KLEIN_FM_TEXT)
							.replace("<groupName>", EntityUtil.getPathOfHierarchyLevel(idExtern.getName(), i))
							.replace("<groupIndexSpecified>", "" + indices[i]);
					addFormalError(result, idExtern, IFormaleFehlerConstants.FEHLER_KONTEXTNUMMER_ZU_KLEIN, fehlerText);
				}
				return false;
			}
		}

		return true;
	}

	/**
	 * @return `false` if any of the USB is invalid (and reports an error in `result`), `true` otherwise.
	 */
	public boolean checkUSB(IIdentifier idExtern, long maxUsb, IResult result) {
		long usb = idExtern.getUsb();
		if (maxUsb != IIdentifier.USB_NOT_SPECIFIED) {
			if (usb == IIdentifier.USB_NOT_SPECIFIED) {
				addFormalError(result, idExtern,
						IFormaleFehlerConstants.FEHLER_KEIN_USB_SPEZIFIZIERT,
						getFehlertext(RuntimeFormalErrorEnum.FEHLER_KEIN_USB_FM_TEXT));
				return false;
			}
			if (usb < 1) {
				addFormalError(result, idExtern,
						IFormaleFehlerConstants.FEHLER_USB_ZU_KLEIN,
						getFehlertext(RuntimeFormalErrorEnum.FEHLER_USB_FM_TEXT));
				return false;
			}
			if (usb > maxUsb) {
				addFormalError(result, idExtern,
						IFormaleFehlerConstants.FEHLER_USB_ZU_GROSS,
						getFehlertext(RuntimeFormalErrorEnum.FEHLER_USB_ZU_GROSS_FM_TEXT));
				return false;
			}
		} else {
			if (usb != IIdentifier.USB_NOT_SPECIFIED) {
				addFormalError(result, idExtern,
						IFormaleFehlerConstants.FEHLER_USB_SPEZIFIZIERT,
						getFehlertext(RuntimeFormalErrorEnum.FEHLER_USB_ANGEGEBEN_FM_TEXT));
				return false;
			}
		}

		return true;
	}

	/**
	 * Helper method to report a formal error.
	 * Used in the formal validation and the preliminary rule validation.
	 *
	 * @param results
	 * 		the validation result storing errors.
	 * @param idExtern
	 * 		the field identifier, the error occurred for.
	 * @param key
	 * 		the error id of the formal error.
	 * @param errorMsg
	 * 		the error message.
	 * @param errorType
	 * 		the type of the error. This is only relevant for the preliminary rule checks,
	 * 		which can be omission errors as well.
	 */
	public void addFormalError(
			IResult results,
			IIdentifier idExtern,
			String key,
			String errorMsg,
			ErrorType errorType
	) {
		if (!meldeFehler) {
			return;
		}

		String ft = errorMsg.replace("$<fieldName>$", data.identifierToString(idExtern));

		Set<IIdentifier> refOmissionErrorResponsible = switch (errorType) {
			case OMISSION_ERROR -> Set.of(idExtern);
			case VALUE_ERROR -> Set.of();
		};

		results.addError(IFormaleFehlerConstants.FORMALE_PRUEFUNG, key, idExtern, ft, errorType,
				Set.of(idExtern), refOmissionErrorResponsible, Set.of());
		results.addMessageV2(IFormaleFehlerConstants.FORMALE_PRUEFUNG, key, ft, errorType, SeverityType.ERROR,
				createSingleFieldCause(idExtern));
	}

	private static ICause createSingleFieldCause(IIdentifier id) {
		return new ErrorCause(id, List.of(Set.of(id)));
	}

	public void addFormalError(IResult results, IIdentifier idExtern, String key, String errorMsg) {
		addFormalError(results, idExtern, key, errorMsg, ErrorType.VALUE_ERROR);
	}

	/**
	 * @param validity
	 *        {@link ValidType#Invalid} if the value provided to {@link #checkAndConvertFieldValue} was invalid (and reports an
	 * 		error in the {@code result} provided to that function), otherwise {@link ValidType#Valid}
	 * @param normalizedValue
	 *        {@code null} if the value provided to {@link #checkAndConvertFieldValue} was invalid or not validated, otherwise the
	 * 		provided {@code value} with normalized linebreaks.
	 * @param internalXmlValue
	 *        {@code null} if the value provided to {@link #checkAndConvertFieldValue} was invalid or not validated, otherwise the
	 * 		provided {@code value} in internal XML format
	 */
	public record FormalCheckResult(
			@NonNull ValidType validity,
			String normalizedValue, // nullable
			String internalXmlValue // nullable
	) {
		public static FormalCheckResult VALID_UNCHECKED = new FormalCheckResult(ValidType.Valid, null, null);
		public static FormalCheckResult INVALID = new FormalCheckResult(ValidType.Invalid, null, null);
	}

	/**
	 * Checks the provided value against the identifier's format definition, and converts it if the value was checked and valid.
	 */
	public @NonNull FormalCheckResult checkAndConvertFieldValue(
			@NonNull RtInternalIdentifier idIntern,
			@NonNull IIdentifier idExtern,
			@NonNull String value,
			@NonNull IResult result
	) {
		IFormatDefinition formatDefinition = metaData.getIFormatDefinition(idIntern.getName());
		if (formatDefinition instanceof FormatDefinitionString stringFormat
				&& stringFormat.isNoValueValidation()) {
			return FormalCheckResult.VALID_UNCHECKED;
		}

		if (!checkFieldValueTooLong(idExtern, value, result)) {
			return FormalCheckResult.INVALID;
		}

		String normalizedValue = RuntimeUtils.normalizeLinebreaks(value);

		ValidType vt = checkFieldValueFormat(result, value, idExtern, formatDefinition, presentationInfo);
		return switch (vt) {
			case Invalid -> FormalCheckResult.INVALID;
			case Valid -> new FormalCheckResult(
					ValidType.Valid,
					normalizedValue,
					formatDefinition.convert(
							normalizedValue, presentationInfo, false,
							RawParamHandler4Convert.of(
									new Object[] { modus.getLanguage(), modus.customFieldTypeFactory() })));
		};
	}

	/**
	 * Check the provided `value` against the provided `formatDefinition` (same as {@link #checkFieldValueFormatBasic}).
	 * <p>
	 * Additionally also checks:
	 * - `value` follows the legal charset
	 * - `value` does not start with a blank (" ")
	 * - `value` does not end with a blank (" ")
	 *
	 * @return {@link ValidType#Invalid} if the field value is not formally correct (and reports an error in `result`).
	 */
	private @NonNull ValidType checkFieldValueFormat(
			@NonNull IResult result,
			@NonNull String value,
			@NonNull IIdentifier idExtern,
			@NonNull IFormatDefinition formatDefinition,
			DarstellungsInformationIntern presentationInfoOrNull
	) {
		String valueWithMarker = checkForInvalidGraphemes(value, formatDefinition);
		if (!valueWithMarker.isEmpty()) {
			String fehlertext = getFehlertext(RuntimeFormalErrorEnum.ZEICHEN_NICHT_IM_ZEICHENSATZ_FM_TEXT)
					.replace("<valueWithMarker>", valueWithMarker);
			addFormalError(result, idExtern,
					IFormaleFehlerConstants.ZEICHEN_NICHT_IM_ZEICHENSATZ, fehlertext);
			return ValidType.Invalid;
		}
		if (value.startsWith(Constants.BLANK)) {
			addFormalError(result, idExtern,
					IFormaleFehlerConstants.FEHLER_FUEHRENDES_BLANK,
					getFehlertext(RuntimeFormalErrorEnum.FEHLER_FUEHRENDES_BLANK_FM_TEXT));
			return ValidType.Invalid;
		}
		if (value.endsWith(Constants.BLANK)) {
			addFormalError(result, idExtern,
					IFormaleFehlerConstants.FEHLER_NACHFOLGENDES_BLANK,
					getFehlertext(RuntimeFormalErrorEnum.FEHLER_NACHFOLGENDES_BLANK_FM_TEXT));
			return ValidType.Invalid;
		}
		return checkFieldValueFormatBasic(result, value, idExtern, formatDefinition, presentationInfoOrNull);
	}

	/**
	 * Check the provided `value` against the provided `formatDefinition`.
	 *
	 * @param presentationInfoOrNull
	 * 		the presentation info to use for the check. If {@code null}, defaults to the presentation info for the internal
	 * 		representation.
	 * @return {@link ValidType#Invalid} if the field value is not formally correct (and reports an error in `result`).
	 */
	public @NonNull ValidType checkFieldValueFormatBasic(
			@NonNull IResult result,
			@NonNull String value,
			@NonNull IIdentifier idExtern,
			@NonNull IFormatDefinition formatDefinition,
			DarstellungsInformationIntern presentationInfoOrNull
	) {
		ValidierungParam param = ValidierungParam.builder()
				.wert(value)
				.identifier(idExtern)
				.ergebnis(result)
				.nurPruefen(!meldeFehler)
				.darstellungsInfo(presentationInfoOrNull != null ? presentationInfoOrNull : presentationInfoIntern)
				.daten(data)
				.extmp(metaData.isExtmp())
				.sprache(modus.getLanguage())
				.nonLenientConversion(true) // always non-lenient with thousand separator placement in conversion
				.customFieldTypeFactory(modus.customFieldTypeFactory())
				.build();

		return formatDefinition.validiereFormat(param, metaData, true);
	}

	private boolean checkFieldValueTooLong(
			@NonNull IIdentifier idExtern,
			@NonNull String value,
			@NonNull IResult result
	) {
		if (value.length() > MAX_WERT_LAENGE) {
			addFormalError(result, idExtern,
					IFormaleFehlerConstants.FEHLER_WERT_ZU_LANG,
					getFehlertext(RuntimeFormalErrorEnum.FEHLER_WERT_ZU_LANG_FM_TEXT));
			return false;
		}
		return true;
	}

	/**
	 * Überprüft, ob der übergebene Wert nur Zeichen aus dem erlaubten Zeichensatz enthält.
	 *
	 * @return leerer String, wenn alle Zeichen korrekt sind, den Feldwert mit Markierungen ({{$—$}}) der fehlerhaften Zeichen
	 * 		sonst.
	 */
	private String checkForInvalidGraphemes(final String value, final IFormatDefinition formatDefinition) {
		// Oberflächenwerte von Enumerationen dürfen beliebige Zeichen enthalten
		if (formatDefinition.getWert(IMetaKeys.FIELD_FIELD_TYPE)
				.equals(IFormatDefinition.FeldFormatEnumType.ER_ENUMERATION.toString())
				&& presentationInfo.doConvertEnumerationen()) {
			return "";
		}
		if (legalCharTester.pruefeZeichensatz(value)) {
			return "";
		}
		// value contains invalid characters - try to mark them
		// the value may contain surrogate pair characters (i.e. characters consisting of 2 chars) or
		// combined characters (characters in the BMP but combined by multiple characters)
		// We build the grapheme clusters and try to mark invalid surrogates and combined characters
		return markInvalidGraphemes(UNICODE_PATTERN.matcher(value).results().map(MatchResult::group).toList());
	}

	private String markInvalidGraphemes(List<String> graphemes) {
		final StringBuilder valueWithMarker = new StringBuilder();
		boolean foundError = false;
		int i = 0;
		while (i < graphemes.size()) {
			final String grapheme = graphemes.get(i);
			if (!legalCharTester.pruefeZeichensatz(grapheme)) {
				if (grapheme.length() > 1) {
					// This could be the prefix of an allowed concatenation.
					final int nextIndex = skipValidConcatenations(i, graphemes, legalGraphemeTrie);
					if (nextIndex == i) {
						markInvalidGrapheme(valueWithMarker, grapheme);
						i = nextIndex + 1;
						foundError = true;
					} else {
						// valid concatenation found - include skipped graphemes as valid strings
						for (int k = i; k < nextIndex; k++) {
							valueWithMarker.append(graphemes.get(k));
						}
						i = nextIndex;
					}
				} else {
					// invalid grapheme of length 1
					markInvalidGrapheme(valueWithMarker, grapheme);
					foundError = true;
					i++;
				}
			} else {
				// grapheme was valid
				valueWithMarker.append(grapheme);
				i++;
			}
		}
		return foundError ? valueWithMarker.toString() : "";
	}

	/**
	 * Find the index (m+1) in the grapheme list if the concatenation of the graphemes [startIndex, m] is a valid prefix in the
	 * given Trie.
	 *
	 * @param startIndex
	 * 		starting index of the grapheme in the grapheme list
	 * @param graphemes
	 * 		the list of graphemes
	 * @param trieRoot
	 * 		the legal grapheme Trie
	 * @return The index of the grapheme that follows on a valid concatenation, otherwise the startIndex is returned.
	 */
	private static int skipValidConcatenations(final int startIndex, final List<String> graphemes, final TrieNode trieRoot) {
		int j = startIndex;
		int skipTo = startIndex;
		TrieNode trieNode = trieRoot;
		while (j < graphemes.size() && trieNode != null) {
			trieNode = matchGrapheme(graphemes.get(j), trieNode);
			j++;
			skipTo = (trieNode != null && trieNode.terminal()) ? j : skipTo; // only advance if a valid concatenation was found
		}
		return skipTo;
	}

	/**
	 * Try to prefix match the whole grapheme for the given Trie.
	 *
	 * @param grapheme
	 * 		the grapheme to match
	 * @param trieNode
	 * 		the {@link TrieNode} the grapheme should be a prefix of.
	 * @return The next {@link TrieNode} instance after the grapheme was matched, null otherwise.
	 */
	private static TrieNode matchGrapheme(final String grapheme, final TrieNode trieNode) {
		TrieNode partial = trieNode;
		for (int i = 0; i < grapheme.length(); i++) {
			if (partial != null) {
				partial = partial.findNext(grapheme.codePointAt(i));
			} else {
				return null;
			}
		}
		return partial;
	}

	private static void markInvalidGrapheme(final StringBuilder valueWithMarker, final String grapheme) {
		valueWithMarker.append("{{$");
		valueWithMarker.append(grapheme);
		valueWithMarker.append("$}}");
	}

	/**
	 * Private Hilfsmethode zur Überprüfung, ob Indizes mit zu grossen Werten übergeben wurden.
	 *
	 * @return false, falls ein Fehler gefunden wurde
	 */
	private boolean checkIndicesLessOrEqualThanMax(
			@NonNull IIdentifier idExtern,
			@NonNull List<Long> maxIndices,
			@NonNull IResult result
	) {

		long[] indices = idExtern.getIndices();

		Long maxLfdNr = maxIndices.get(0);
		//erster Index ist die laufende Nummer, Fehlermeldung muss ggf. entsprechend lauten
		if (indices[0] > maxLfdNr) {
			addFormalError(result, idExtern,
					IFormaleFehlerConstants.FEHLER_LAUFENDE_NR_ZU_GROSS,
					getFehlertext(RuntimeFormalErrorEnum.FEHLER_LAUFENDE_NR_ZU_GROSS_FM_TEXT));
			return false;
		}
		//Zeilenindex und Kontextindices
		for (int i = 1; i < indices.length; i++) {
			long maxIndex = maxIndices.get(i);
			if (indices[i] > maxIndex) {
				//der letze Index im Indexarray ist die Zeile, Fehlermeldung muss entsprechend lauten
				if (i == indices.length - 1) {
					addFormalError(result, idExtern,
							IFormaleFehlerConstants.FEHLER_ZEILENNUMMER_ZU_GROSS,
							getFehlertext(RuntimeFormalErrorEnum.FEHLER_ZEILENNUMMER_ZU_GROSS_FM_TEXT));
					return false;
				}
				String kontextPfad = EntityUtil.getPathOfHierarchyLevel(idExtern.getName(), i);
				String fehlertext = getFehlertext(RuntimeFormalErrorEnum.FEHLER_KONTEXTNUMMER_ZU_GROSS_FM_TEXT)
						.replace("<groupName>", kontextPfad)
						.replace("<groupIndexSpecified>", "" + indices[i])
						.replace("<maxGroupIndex>", "" + maxIndex);
				addFormalError(result, idExtern,
						IFormaleFehlerConstants.FEHLER_KONTEXTNUMMER_ZU_GROSS, fehlertext);
				return false;

			}
		}

		return true;
	}

	String getFehlertext(RuntimeFormalErrorEnum fehlerId) {
		Locale sprache = modus.getLanguage();
		return FehlerHandler.getFehlertext(fehlerId, metaData.isExtmp(), sprache);
	}
}
