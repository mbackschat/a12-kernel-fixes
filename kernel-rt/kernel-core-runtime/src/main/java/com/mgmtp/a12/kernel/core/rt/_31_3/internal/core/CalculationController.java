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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.core;

import java.math.RoundingMode;
import java.util.Collections;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionDatum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionDatumBereich;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionZahl;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ErrorCause;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkBigDecimal;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkDate;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkDateRange;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkString;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult.ErrorType;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;
import com.mgmtp.a12.utils.conversion.a12internal.util.ValidationDateParser;

import lombok.NonNull;

public class CalculationController extends RuntimeController {

	private static final int MAX_FRACTIONAL_DIGITS_TOTAL = 15;

	private String berechnetesFeld;
	private final @NonNull CalculationCache calcCache;
	private final @NonNull DarstellungsInformationIntern darstellungsInfo;
	private final @NonNull FormalChecker formalChecker;

	public CalculationController(
			@NonNull IResult ergebnisse,
			@NonNull CalculationCache calcCache,
			@NonNull ValidationMode modus,
			@NonNull IMetaDataValidierung metaData,
			@NonNull DarstellungsInformationIntern darstellungsInfo,
			@NonNull FormalChecker formalChecker
	) {
		super(ergebnisse, calcCache, modus, metaData);
		this.calcCache = calcCache;
		this.darstellungsInfo = darstellungsInfo;
		this.formalChecker = formalChecker;
	}

	public void initFuerBerechnung(String berechnetesFeld) {
		this.berechnetesFeld = berechnetesFeld;
	}

	@Override
	protected void logNichtAusgewertet(String fehlermeldung) {
		LOG.logBerechnetesFeldNichtAusgewertet(berechnetesFeld, fehlermeldung);
	}

	/**
	 * Set the calculated value in the calculation cache.
	 *
	 * @param idIntern
	 * 		the field to be set.
	 * @param valueIntern
	 * 		calculated value in internal format.
	 * @param isValueCorrect
	 * 		specifies the correctness of the value. If the value is correct, an
	 * 		error during
	 * 		conversion should never happen and will therefore be re-thrown, else
	 * 		the internal
	 * 		value will be used as UI value.
	 */
	public void setBerechnetenStringWert(RtInternalIdentifier idIntern, VkString valueIntern, boolean isValueCorrect) {
		IFormatDefinition formatDefinition = metaData.getIFormatDefinition(idIntern.getName());
		String uiValue;
		try {
			uiValue = formatDefinition.convert(
					valueIntern.getWert(), darstellungsInfo, true,
					RawParamHandler4Convert.of(new Object[] { modus.customFieldTypeFactory() }));
		} catch (IllegalArgumentException e) {
			if (isValueCorrect) {
				throw e;
			} else {
				uiValue = valueIntern.getWert();
			}
		}
		calcCache.addBerechnetenFeldWert(idIntern, uiValue, valueIntern);
	}

	/**
	 * Checks the given value of the given field and creates an error message in case of an error.
	 *
	 * @param idIntern
	 * 		the field to be tested.
	 * @param internValue
	 * 		the value of the field in internal format.
	 * @return <code>true</code>, in case of a correct value, <code>false</code> otherwise.
	 */
	public boolean checkField(@NonNull RtInternalIdentifier idIntern, @NonNull String internValue) {
		return formalChecker.checkFieldValueFormatBasic(
				pruefErgebnis,
				internValue,
				calcCache.convert2Extern(idIntern),
				metaData.getIFormatDefinition(idIntern.getName()),
				null
		) != ValidType.Invalid;
	}

	public void markiereAlsFehlerhaftBerechnet(RtInternalIdentifier id) {
		calcCache.markFieldAsInvalid(id);
	}

	/**
	 * see {@link CalculationCache#markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated}
	 * (used in generated code, see CodeGenCalculatedField.st)
	 */
	public void markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated(String uniqueFieldName, String[] parallelIterationGroups) {
		calcCache.markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated(uniqueFieldName, parallelIterationGroups);
	}

	/**
	 * erzeugt einen formalen Fehler für das Berechnete Feld.
	 */
	public void addBerechnungsFehler(RtInternalIdentifier idIntern) {
		String rName = IFormaleFehlerConstants.FORMALE_PRUEFUNG;
		ErrorType fehlerTyp = ErrorType.VALUE_ERROR;
		RtExternalIdentifier idExtern = validationCache.convert2Extern(idIntern);

		String fehlerText = FehlerHandler.getFehlertext(RuntimeFormalErrorEnum.BERECHNUNG_WERT_FEHLER, metaData.isExtmp(),
				modus.getLanguage());
		String key = IFormaleFehlerConstants.BERECHNUNG_WERT_FEHLER;

		String feldStringRepraesentation = validationCache.getDaten().identifierToString(idExtern);
		String ft = CheckCommand.ersetzeFeldParameter(fehlerText, feldStringRepraesentation);

		pruefErgebnis.addError(rName, key, idExtern, ft, fehlerTyp,
				Collections.<IIdentifier>singleton(idExtern), Collections.<IIdentifier>emptySet(),
				Collections.<IIdentifier>emptySet());
		pruefErgebnis.addMessageV2(rName, key, ft, fehlerTyp, SeverityType.ERROR, ErrorCause.createSingleFieldCause(idExtern));
	}

	public void handleBerechnetenWert(String value, RtInternalIdentifier id) {
		handleBerechnetenWert(new VkString(value), id);
	}

	public void handleBerechnetenWert(VkString value, RtInternalIdentifier id) {
		if (value.isNichtPruefungsrelevant() || !value.hasValue()) {
			// For fields that are not relevant or for empty values, do like no calculation was done.
			return;
		}
		boolean isValueCorrect = checkField(id,
				value.getWert()); // value.getWert() is known non-null because value.hasValue() has been checked above
		if (!isValueCorrect) {
			markiereAlsFehlerhaftBerechnet(id);
		}
		setBerechnetenStringWert(id, value, isValueCorrect);
	}

	public void handleBerechnetenWert(VkDate value, RtInternalIdentifier id) {
		if (value.isNichtPruefungsrelevant() || !value.hasValue()) {
			// For fields that are not relevant or for empty values, do like no calculation was done.
			return;
		}
		FormatDefinitionDatum formatDefinition = (FormatDefinitionDatum) metaData.getIFormatDefinition(id.getName());
		String formatString = formatDefinition.getFormat();
		String internalValue = ValidationDateParser.makeStringDayMonthTwoDigits(value.getDate(), formatString, timeZone);

		boolean isValueCorrect = checkField(id, internalValue);
		if (!isValueCorrect) {
			markiereAlsFehlerhaftBerechnet(id);
		}
		setBerechnetenStringWert(id, new VkString(internalValue, value.getOrigins()), isValueCorrect);

	}

	public void handleBerechnetenWert(VkDateRange value, RtInternalIdentifier id) {
		if (value.isNichtPruefungsrelevant() || !value.hasValue()) {
			// For fields that are not relevant or for empty values, do like no calculation was done.
			return;
		}
		FormatDefinitionDatumBereich formatDefinition = (FormatDefinitionDatumBereich) metaData.getIFormatDefinition(id.getName());
		String formatString = formatDefinition.getFormat();
		String internalStartValue = ValidationDateParser.makeStringDayMonthTwoDigits(value.getStart(), formatString, timeZone);
		String internalEndValue = ValidationDateParser.makeStringDayMonthTwoDigits(value.getEnd(), formatString, timeZone);
		String internalValue = internalStartValue + formatDefinition.getTrenner() + internalEndValue;

		boolean isValueCorrect = checkField(id, internalValue);
		if (!isValueCorrect) {
			markiereAlsFehlerhaftBerechnet(id);
		}
		setBerechnetenStringWert(id, new VkString(internalValue, value.getOrigins()), isValueCorrect);
	}

	/**
	 * Setzt den Berechneten Wert im Cache. Im Fehlerfall wird das Feld als fehlerhaft berechnet
	 * markiert und die entsprechende Fehlermeldung erzeugt.
	 *
	 * @param wert
	 * 		berechneter Wert
	 * @param id
	 * 		interne Id
	 */
	public void handleBerechnetenWert(@NonNull VkBigDecimal wert, @NonNull RtInternalIdentifier id) {
		// internally we calculate with precision VkBigDecimal#INTERNAL_PRECISION
		// Here we make a rounding to VkBigDecimal#DEFAULT_SCALE fractional digits
		VkBigDecimal valueWithDefaultScale = wert.setScale(VkBigDecimal.DEFAULT_SCALE, RoundingMode.HALF_UP);
		FormatDefinitionZahl formatDefinition = (FormatDefinitionZahl) metaData.getIFormatDefinition(id.getName());
		int minNachkommaMeta = (Integer) formatDefinition.getWert(IMetaKeys.FIELD_DECIMAL_PLACES);
		int maxNachkommaMeta = (Integer) formatDefinition.getWert(IMetaKeys.FIELD_MAX_DECIMAL_PLACES);
		if (valueWithDefaultScale.wertIstUngueltig()) {
			markiereAlsFehlerhaftBerechnet(id);
			addBerechnungsFehler(id);
		} else {
			int minScale = valueWithDefaultScale.getBigDecimalValue().stripTrailingZeros().scale();
			// Passt der Wert in die Nachkommastellen?
			if (minScale <= maxNachkommaMeta) {
				int destFractionalDigits = Math.max(minScale, minNachkommaMeta);
				String strWert = valueWithDefaultScale.getBigDecimalValue()
						.setScale(destFractionalDigits, RoundingMode.UNNECESSARY).toPlainString();
				String wertXML = strWert.replace('.', darstellungsInfo.getDezimalTrennerIntern());
				boolean isValueCorrect = checkField(id, wertXML);
				if (!isValueCorrect) {
					markiereAlsFehlerhaftBerechnet(id);
				}
				setBerechnetenStringWert(id, new VkString(wertXML, wert.getOrigins()), isValueCorrect);
			} else {
				// nein => nimm max. die max. möglichen Stellen + 1
				String strWert = BigDecimalUtils.toStringWertLaengenBeschraenkt(valueWithDefaultScale,
						MAX_FRACTIONAL_DIGITS_TOTAL + 1);
				String wertXML = strWert.replace('.', darstellungsInfo.getDezimalTrennerIntern());
				if (checkField(id, wertXML)) {
					// Der Wert sollte eigentlich immer fehlerhaft sein, also sollten wir hier nie landen.
					// Könnte auftreten, wenn die projektspezifische maximale Länge kleiner als 15 ist.
					// PruefeFeld dient eigentlich der Erzeugung einer spezifischeren Fehlermeldung.
					addBerechnungsFehler(id);
				}
				setBerechnetenStringWert(id, new VkString(wertXML, wert.getOrigins()), false);
				markiereAlsFehlerhaftBerechnet(id);
			}
		}
	}

	/**
	 * Is called from the generated code to check if the repetition is already set. Is used for
	 * parallel iteration because no additional repetitions should be created by calculation.
	 *
	 * @param id
	 * 		determines the indices
	 * @param levelWithoutUsb
	 * 		the level up to which it should be checked if the repetition is set
	 * 		(here this is
	 * 		the parallel iteration level on the calc field path)
	 * @return true if the repetition is set, false otherwise
	 */
	public boolean isWiederholungGesetzt(RtInternalIdentifier id, int levelWithoutUsb) {
		return validationCache.getGesetzteWiederholungen().istWiederholungGesetzt(getIndicesWithUsb(id, levelWithoutUsb + 1));
	}

	private long[] getIndicesWithUsb(RtInternalIdentifier id, int numLevelsWithoutUsb) {
		RtInternalIdentifier resolvedId = validationCache.resolveSemanticIndex(id);
		long[] indicesWithUsb = new long[numLevelsWithoutUsb + 1];
		indicesWithUsb[0] = id.getUsb();
		for (int i = 0; i < numLevelsWithoutUsb; i++) {
			indicesWithUsb[i + 1] = resolvedId.getIndices()[i];
		}
		return indicesWithUsb;
	}
}
