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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.DynamicEnumerationCommand;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.Feld_t;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionDatum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionDatumBereich;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionString;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionVordefiniert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.ValidierungParam;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DatenImpl;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ErgebnisImpl;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.LocaleUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.MetaDataUtilValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.PresentationInformationValues;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.internal.utils.IExtmpMetaKeysBase;

import lombok.NonNull;

public class MetaField extends MetaEntity implements IMetaField {

	private static final LinkedHashSet<String> schluessel = new LinkedHashSet<>();
	private static final LinkedHashSet<String> internalKeys = new LinkedHashSet<>();

	final Set<IIdentifier> operandsSet = new HashSet<>();
	final Set<IIdentifier> targetSetForCalc = new HashSet<>();
	final Set<IIdentifier> operandSetDynamicEnum = new HashSet<>();
	final Map<Locale, String> errorTexts4Req;

	private final IFormatDefinition formatDef;
	private final MetaModelImpl metaModel;

	static {
		schluessel.add(IMetaKeys.FIELD_SHORT_NAME);
		schluessel.add(IMetaKeys.FIELD_FULLNAME);
		schluessel.add(IExtmpMetaKeysBase.FIELD_MAXZEILE);
		schluessel.add(IMetaKeys.FIELD_MANDATORY_FIELD);
		schluessel.add(IMetaKeys.FIELD_MIN_LENGTH);
		schluessel.add(IMetaKeys.FIELD_MAX_LENGTH);
		schluessel.add(IMetaKeys.FIELD_FIELD_TYPE);
		schluessel.add(IMetaKeys.FIELD_PATTERN);
		schluessel.add(IMetaKeys.FIELD_ERROR_MESSAGE);
		schluessel.add(IMetaKeys.FIELD_WORD_WRAP);
		schluessel.add(IMetaKeys.FIELD_SIGN);
		schluessel.add(IMetaKeys.FIELD_DECIMAL_PLACES);
		schluessel.add(IMetaKeys.FIELD_MAX_DECIMAL_PLACES);
		schluessel.add(IMetaKeys.FIELD_INTEGER_DIGIT);
		schluessel.add(IMetaKeys.FIELD_ZERO_ALLOWED);
		schluessel.add(IMetaKeys.FIELD_VALUE_FALSE);
		schluessel.add(IMetaKeys.FIELD_LEADING_ZEROS);
		schluessel.add(IMetaKeys.FIELD_FORMAT);
		schluessel.add(IMetaKeys.FIELD_VALUE_TRUE);
		schluessel.add(IMetaKeys.FIELD_DECIMAL_SEPARATOR);
		schluessel.add(IMetaKeys.FIELD_ENUMERATION_VALUES);
		schluessel.add(IMetaKeys.FIELD_ROOT_GROUP_MANDATORY_FIELD);
		schluessel.add(IMetaKeys.FIELD_ENUMERATION_UI_VALUES);
		schluessel.add(IMetaKeys.FIELD_OPERANDS_SET);
		schluessel.add(IMetaKeys.FIELD_TARGET_SET_FOR_CALC);
		schluessel.add(IMetaKeys.FIELD_S_VALUE);

		schluessel.add(IExtmpMetaKeysBase.FIELD_JSFUNKTIONSNAME);
		schluessel.add(IExtmpMetaKeysBase.FIELD_BERECHNUNG_UEBERTRAG);
		schluessel.add(IMetaKeys.FIELD_ENUMERATION_CATEGORY_VALUES);
		schluessel.add(IMetaKeys.FIELD_NO_VALUE_VALIDATION);

		schluessel.add(IMetaKeys.FIELD_DYNAMIC_ENUM_VALUES);
		schluessel.add(IMetaKeys.FIELD_SOURCE_FIELDS_FOR_DYNAMIC_ENUM);
		schluessel.add(IMetaKeys.FIELD_DYNAMIC_ENUMERATION);

		schluessel.add(IMetaKeys.FIELD_MANDATORY_GLOBAL_OR_IN_SURROUNDING_REPEATABLE_GROUP);
		schluessel.add(IMetaKeys.FIELD_META_FIELD_DATE_OF_DATE_RANGE);

		schluessel.add(IMetaKeys.FIELD_ANNOTATION);
		schluessel.add(IMetaKeys.FIELD_CUSTOM_ERROR_MESSAGE_FOR_REQUIRED);

		schluessel.add(IExtmpMetaKeysBase.FIELD_KENNZAHL);

		internalKeys.add(IMetaKeysInternal.FELD_INTERNENR);
		internalKeys.add(IMetaKeysInternal.FELD_UNIQUENAME);
		internalKeys.add(IMetaKeysInternal.FELD_MAXINDICES);
		internalKeys.add(IMetaKeysInternal.FELD_SB_KZ_REPETITION_1);
		internalKeys.add(IMetaKeysInternal.FELD_SB_KZ);
		internalKeys.add(IMetaKeysInternal.FIELD_TEILBEKANNTART);
	}

	private MetaField(final MetaModelImpl metaModel,
			final Feld_t f,
			final char dezimalTrenner,
			final IFormatDefinition formatDefinition) {
		super(schluessel, internalKeys);
		if (formatDefinition != null) {
			this.formatDef = formatDefinition;
		} else {
			this.formatDef = f.getFormatDefinition();
		}
		assert (formatDef != null);
		this.errorTexts4Req = f.getErrorTexts4Req();
		this.metaModel = metaModel;
		final String path = f.getPfadName();
		addWert(IMetaKeysInternal.FELD_INTERNENR, f.getInterneNr());
		addWert(IMetaKeys.FIELD_SHORT_NAME, path.substring(path.lastIndexOf('/') + 1));
		addWert(IMetaKeys.FIELD_FULLNAME, path);
		addWert(IExtmpMetaKeysBase.FIELD_MAXZEILE, f.getMaxZeile());
		addWert(IMetaKeys.FIELD_DECIMAL_SEPARATOR, String.valueOf(dezimalTrenner));
		addWert(IMetaKeys.FIELD_MANDATORY_FIELD, Arrays.asList(f.getPflichtFeldLfdNr()));
		addWert(IMetaKeys.FIELD_MANDATORY_GLOBAL_OR_IN_SURROUNDING_REPEATABLE_GROUP,
				f.isMandatoryForRepeatableGroup() || f.isPflichtFeld());
		addWert(IMetaKeys.FIELD_ROOT_GROUP_MANDATORY_FIELD, Arrays.asList(f.getVordruckPflichtFeldLfdNr()));
		addWert(IMetaKeysInternal.FELD_UNIQUENAME, f.getUniqueName());

		addWert(IExtmpMetaKeysBase.FIELD_JSFUNKTIONSNAME, f.getJavascriptName());
		addWert(IExtmpMetaKeysBase.FIELD_BERECHNUNG_UEBERTRAG, false);
		addWert(IMetaKeysInternal.FELD_SB_KZ_REPETITION_1, f.getSbKzRepetition1List());
		addWert(IMetaKeysInternal.FELD_SB_KZ, f.getSbKzMap());
		// Will be reset in MetaModelImpl#createRegel if necessary
		addWert(IMetaKeys.FIELD_DYNAMIC_ENUMERATION, false);

		if (this.formatDef instanceof FormatDefinitionDatumBereich) { // Feld_t is of type date range
			FormatDefinitionDatumBereich fddb = (FormatDefinitionDatumBereich) this.formatDef;
			FormatDefinitionDatum fdd = new FormatDefinitionDatum(
					fddb.getFormat(),
					fddb.getTeilbekanntArt());
			addWert(IMetaKeys.FIELD_META_FIELD_DATE_OF_DATE_RANGE, new MetaField(metaModel, f, dezimalTrenner, fdd));
		} else {
			addWert(IMetaKeys.FIELD_META_FIELD_DATE_OF_DATE_RANGE, null);
		}

		// "Annotation" is added to super.metadaten. This makes sure that super.pruefeSchluessel works fine. If any annotation is
		// provided in additional values, the 'null' value is overwritten.
		addWert(IMetaKeys.FIELD_ANNOTATION, null);

		Set<String> internalAdditionalKeys = f.getInternalAdditionalKeys();
		for (final String key : f.getAdditionalKeys()) {
			if (internalAdditionalKeys.contains(key)) {
				addAdditionalInternalKey(key);
			} else {
				addAdditionalKey(key);
			}
			addWert(key, f.getAdditionalValue(key));
		}

		addWert(IExtmpMetaKeysBase.FIELD_BERECHNUNG_UEBERTRAG, f.isBerechnungsUebertrag());
	}

	public MetaField(final MetaModelImpl metaModel,
			final Feld_t f,
			final char dezimalTrenner) {
		this(metaModel, f, dezimalTrenner, null);
	}

	void addToOperandsSet(IIdentifier operand) {
		operandsSet.add(operand);
	}

	void addToTargetSetForCalc(IIdentifier targetForCalc) {
		targetSetForCalc.add(targetForCalc);
	}

	void setMaxIndices(final MetaModelImpl metaModel) {
		final List<Long> maxIndices = getMaxIndices(metaModel);
		addWert(IMetaKeysInternal.FELD_MAXINDICES, maxIndices);
	}

	private List<Long> getMaxIndices(final MetaModelImpl meta) {
		final List<Long> result = new ArrayList<>();

		final String path = (String) getValue(IMetaKeys.FIELD_FULLNAME);
		int endIndex = path.indexOf('/', 1);
		while (endIndex != -1) {
			final String aktPfad = path.substring(0, endIndex);
			final MetaContainer metaCont = (MetaContainer) meta.getValue(IMetaKeys.MODEL_META_GROUP, aktPfad);
			final Long maxIndex = (Long) metaCont.getValue(IMetaKeys.GROUP_MAX_INDEX);
			if (maxIndex != null) {
				result.add(maxIndex);
			}
			endIndex = path.indexOf('/', endIndex + 1);
		}
		// der max. Zeilenindex fehlt noch
		final long maxZeile = (Long) getValue(IExtmpMetaKeysBase.FIELD_MAXZEILE);
		result.add(maxZeile);
		return result;
	}

	public MetaContainer getTopContainer() {
		MetaContainer result = null;
		String topContainerName = "";

		final String path = (String) getValue(IMetaKeys.FIELD_FULLNAME);
		final int endIndex = path.indexOf('/', 1);
		if (endIndex != -1) {
			topContainerName = path.substring(0, endIndex);
		} else {
			topContainerName = path;
		}
		if (!topContainerName.isEmpty()) {
			result = (MetaContainer) this.metaModel.getValue(IMetaKeys.MODEL_META_GROUP, topContainerName);
		}
		return result;
	}

	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (other.getClass() != getClass()) {
			return false;
		}

		final MetaField o = (MetaField) other;
		//der erste Parameter reicht für die Identifikation

		return (getValue(IMetaKeys.FIELD_FULLNAME).equals(o.getValue(IMetaKeys.FIELD_FULLNAME)));
	}

	public int hashCode() {
		return getValue(IMetaKeys.FIELD_FULLNAME).hashCode();
	}

	private boolean validateBeforeConvert(
			@NonNull final String data,
			final PresentationInformation presentationInfoOrNull,
			@NonNull final Locale sprache,
			final boolean fullCheck,
			final boolean nonLenient,
			@NonNull final IResult result,
			@NonNull final RawParamHandler4Convert additionalParameters) {

		final String name = (String) getValue(IMetaKeys.FIELD_FULLNAME);
		final long[] indices = { 1, 1 };
		final RtExternalIdentifier id = new RtExternalIdentifier(name, indices, 1);

		final PresentationInformationValues defaultPresentationInfoValues = metaModel.getMetaDataValidierung()
				.getDefaultPresentationInformation().get(sprache);
		final TimeZone timeZone = this.metaModel.getMetaDataValidierung().getTimeZone();
		final DarstellungsInformationIntern di = new DarstellungsInformationIntern(getDezimalTrenner(), presentationInfoOrNull,
				metaModel.getMetaDataValidierung().getBaseYearOrDefault(), defaultPresentationInfoValues, timeZone);
		final IData daten = new DatenImpl();
		final ValidierungParam param = ValidierungParam.builder()
				.wert(data)
				.identifier(id)
				.ergebnis(result)
				.nurPruefen(false)
				.darstellungsInfo(di)
				.daten(daten)
				.extmp(metaModel.getMetaDataValidierung().isExtmp())
				.sprache(sprache)
				.customFieldTypeFactory(additionalParameters.getCustomFieldTypeFactory())
				.nonLenientConversion(nonLenient)
				.build();
		final ValidType myValidType = formatDef.validiereFormat(param, metaModel.getMetaDataValidierung(), fullCheck);
		return !ValidType.Invalid.equals(myValidType);
	}

	public String convert(
			final String data,
			final PresentationInformation darstellungsInfo,
			final boolean vonXml,
			final Object... parameters) {
		if (data == null) {
			return null;
		}
		var additionalParams = RawParamHandler4Convert.of(parameters);

		IResult resultFromParams = additionalParams.getResultParam();
		IResult result = resultFromParams != null ? resultFromParams : new ErgebnisImpl();
		String converted = convertInternal(data, darstellungsInfo, vonXml, true, true, result, additionalParams);
		if (resultFromParams == null &&
				((ErgebnisImpl) result).getFehlerText() != null) {
			throw new IllegalArgumentException(((ErgebnisImpl) result).getFehlerText());
		}
		return converted;
	}

	// NOTE A12K-2678: NOTE: depends on 'isolate conversion logic'!
	@Override
	public Object convertWithOnlyBasicTypeCheck(final String value,
			final PresentationInformation presentationInformation,
			final @NonNull IResult result,
			final Object... parameters) {
		if (value == null) {
			// an empty value will be converted to null, except for string-, enumeration- and predefined types
			// that's why the conversion of an empty value is type specific
			return null;
		}
		var additionalParams = RawParamHandler4Convert.of(parameters);

		String internalValue = convertInternal(value, presentationInformation, false, false, false, result, additionalParams);

		return formatDef.convertToBasicType(internalValue, metaModel.getMetaDataValidierung());
	}

	// NOTE A12K-2678: NOTE: depends on 'isolate conversion logic'!
	@Override
	public String convertFromBasicType(final Object value,
			final PresentationInformation presentationInformation,
			final @NonNull IResult result,
			final Object... parameters) {
		if (value == null) {
			return null;
		}
		var additionalParams = RawParamHandler4Convert.of(parameters);
		String internalValue = formatDef.convertFromBasicType(value, metaModel.getMetaDataValidierung());
		if (internalValue == null) {
			return null;
		}
		return convertInternal(internalValue, presentationInformation, true, false, false, result, additionalParams);
	}

	private String convertInternal(
			@NonNull final String data, // we'd _not_ require non-null in case of isNoValueValidation, but all callers guarantee it
			// anyway, at the moment, so it should be safe to add. In cases other than isNoValueValidation, non-null is required
			// anyway due to the validateBeforeConvert call
			final PresentationInformation darstellungsInfo,
			final boolean vonXml,
			final boolean fullCheck,
			final boolean nonLenient,
			@NonNull final IResult result,
			@NonNull final RawParamHandler4Convert additionalParameters) {
		if (formatDef instanceof FormatDefinitionString formatDefString && formatDefString.isNoValueValidation()) {
			// No checks nor transformations for noValueValidation
			return data;
		}

		@SuppressWarnings("unchecked") Locale sprache = additionalParameters.getSprache(
				(List<Locale>) metaModel.getValue(IMetaKeys.MODEL_LANGUAGES));

		boolean isValid = validateBeforeConvert(
				data, vonXml ? null : darstellungsInfo, sprache, fullCheck, nonLenient, result, additionalParameters);
		if (!isValid) {
			return null;
		}

		final TimeZone timeZone = this.metaModel.getMetaDataValidierung().getTimeZone();
		final PresentationInformationValues defaultPresentationInfoValues = metaModel.getMetaDataValidierung()
				.getDefaultPresentationInformation().get(sprache);
		final DarstellungsInformationIntern di = new DarstellungsInformationIntern(getDezimalTrenner(), darstellungsInfo,
				metaModel.getMetaDataValidierung().getBaseYearOrDefault(), defaultPresentationInfoValues, timeZone);
		return formatDef.convert(data, di, vonXml, additionalParameters);
	}

	private Object getLaengeMitDarstellungsInfo(final PresentationInformation info,
			final String key, final Locale sprache) {

		final TimeZone timeZone = this.metaModel.getMetaDataValidierung().getTimeZone();
		final PresentationInformationValues defaultPresentationInformationValues = metaModel.getMetaDataValidierung()
				.getDefaultPresentationInformation().get(sprache);
		final DarstellungsInformationIntern di = new DarstellungsInformationIntern(getDezimalTrenner(), info,
				metaModel.getMetaDataValidierung().getBaseYearOrDefault(), defaultPresentationInformationValues, timeZone);
		final String typ = (String) getValue(IMetaKeys.FIELD_FIELD_TYPE);

		switch (typ) {
			case "Ja" -> {
				String wahrWert = di.getWahrWert();
				if (wahrWert == null) {//Nehme XMl Darstellung, wenn in DarstellungsInfo nicht gesetzt
					wahrWert = (String) getValue(IMetaKeys.FIELD_VALUE_TRUE);
				}
				return wahrWert.length();
			}
			case "JaNein" -> {
				String wahrWert = di.getWahrWert();
				String falschWert = di.getFalschWert();
				if (wahrWert == null) {//Nehme XMl Darstellung, wenn in DarstellungsInfo nicht gesetzt
					wahrWert = (String) getValue(IMetaKeys.FIELD_VALUE_TRUE);
				}
				if (falschWert == null) {//Nehme XMl Darstellung, wenn in DarstellungsInfo nicht gesetzt
					falschWert = (String) getValue(IMetaKeys.FIELD_VALUE_FALSE);
				}
				if (key.equals(IMetaKeys.FIELD_MAX_LENGTH)) {
					return Math.max(wahrWert.length(), falschWert.length());
				} else {
					return Math.min(wahrWert.length(), falschWert.length());
				}
			}
			case "DatumBereich" -> {
				final FormatDefinitionDatumBereich db = (FormatDefinitionDatumBereich) formatDef;
				final String xmlFormat = db.getFormat().replace("TT", "dd").replace("J", "y");
				final String xmlDatumBereichTrenner = db.getTrenner();
				if (IMetaKeys.FIELD_MAX_LENGTH.equals(key)) {
					return di.getMaxLaengeDatumBereich(xmlFormat, xmlDatumBereichTrenner);
				} else {
					return di.getMinLaengeDatumBereich(xmlFormat, xmlDatumBereichTrenner);
				}
			}
			case "Datum" -> {
				final String xmlFormat = ((String) getValue(IMetaKeys.FIELD_FORMAT)).replace("TT", "dd").replace("J", "y");
				if (IMetaKeys.FIELD_MAX_LENGTH.equals(key)) {
					return di.getMaxLaengeDatum(xmlFormat);
				} else {
					return di.getMinLaengeDatum(xmlFormat);
				}
			}
		}

		if (isNumber(typ)) {
			final String trait = isAmount(typ) ? PresentationInformation.TRAIT_AMOUNT : PresentationInformation.TRAIT_NONE;
			final String thousandsSeparatorForTrait = di.getThousandsSeparatorForTrait(trait);
			if (IMetaKeys.FIELD_MAX_LENGTH.equals(key) && !StringUtils.isEmpty(thousandsSeparatorForTrait)) {
				int xmlMaxLength = (int) getValue(key);
				Integer maxIntegerDigits = (Integer) getValue(IMetaKeys.FIELD_INTEGER_DIGIT);
				if (xmlMaxLength > 0 && maxIntegerDigits != null && maxIntegerDigits > 0) {
					int thousandsSepLength = thousandsSeparatorForTrait.length();
					return xmlMaxLength + ((maxIntegerDigits - 1) / 3) * thousandsSepLength;
				}
				return xmlMaxLength;
			}
		}
		if (this.formatDef instanceof FormatDefinitionVordefiniert) {
			if (IMetaKeys.FIELD_MAX_LENGTH.equals(key) && di.doConvertCustomFieldType(typ)) {
				/**
				 * Return the maximum length of the display value of the custom field type. At the meta data interface for a field, the maximum length
				 * of the internal value is returned by default. The length of the display value is only returned, if the user passes a
				 * <i>PresentationInformation</i> and sets parameter <i>CONVERT_CUSTOM_FIELD_TYPES_ALL</i> or adds the custom field type to parameter
				 * <i>CONVERT_CUSTOM_FIELD_TYPES_LIST</i>.
				 * projAlpha retrieves the maximum length at the meta data interface to determine the maximum length of an input field. This function
				 * is currently only implemented for the custom field types <i>IDNr</i> and <i>IBAN</i> where the display value is larger than the internal value.
				 * For all other custom field types the maximum length of the internal value is returned.
				 */
				if (typ.equals("IBAN") || typ.equals("IBANinternational")) {
					return 34 + 8;
				} else if (typ.equals("IDNr")) {
					return 11 + 3;
				}
			}
		}

		return getValue(key);

	}

	private boolean isNumber(String fieldType) {
		return fieldType.equals("Zahl") || isAmount(fieldType);
	}

	private boolean isAmount(String fieldType) {
		return fieldType.equals("GeldBetragMitCent") || fieldType.equals("GeldBetragOhneCent");
	}

	public Object getVordruckPflichtfeldIntern() {
		return super.getValue(IMetaKeys.FIELD_ROOT_GROUP_MANDATORY_FIELD);
	}

	public Object getPflichtfeldIntern() {
		return super.getValue(IMetaKeys.FIELD_MANDATORY_FIELD);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(final String key, final Object... parameter) {
		if (IMetaKeys.FIELD_ANNOTATION.equals(key)) {
			return getAnnotation(key, parameter);
		}
		if (IMetaKeys.FIELD_OPERANDS_SET.equals(key)) {
			return Collections.unmodifiableSet(operandsSet);
		}
		if (IMetaKeys.FIELD_TARGET_SET_FOR_CALC.equals(key)) {
			return Collections.unmodifiableSet(targetSetForCalc);
		}
		if (IMetaKeys.FIELD_SOURCE_FIELDS_FOR_DYNAMIC_ENUM.equals(key)) {
			checkMinNumberOfParameters(1, parameter);

			IIdentifier id = (IIdentifier) parameter[0];

			assert getValue(IMetaKeys.FIELD_FULLNAME).equals(id.getName()) : "The name of the MetaFeld (" + getValue(
					IMetaKeys.FIELD_FULLNAME) + " is not equal to the name of the given identifier (" + id.getName() + ").";

			HashSet<IIdentifier> result = new HashSet<>();
			for (IIdentifier op : operandSetDynamicEnum) {
				result.add(concretizeIndices(op, id));
			}
			return result;
		}
		if (IMetaKeys.FIELD_DYNAMIC_ENUM_VALUES.equals(key)) {
			checkMinNumberOfParameters(3, parameter);

			final IIdentifier field = (IIdentifier) parameter[0];

			assert getValue(IMetaKeys.FIELD_FULLNAME).equals(field.getName()) : "The name of the MetaFeld (" + getValue(
					IMetaKeys.FIELD_FULLNAME) + " is not equal to the name of the given identifier (" + field.getName() + ").";

			final IData data = (IData) parameter[1];
			final ValidationMode modeOrNull = (ValidationMode) parameter[2];

			return calculateEnumValues(field, data, modeOrNull);
		}
		if (key.equals(IMetaKeys.FIELD_ROOT_GROUP_MANDATORY_FIELD)
				|| key.equals(IMetaKeys.FIELD_MANDATORY_FIELD)) {
			checkNoParameters();
			final List<Integer> liste = (List<Integer>) super.getValue(key);

			return liste.contains(0);
		}
		if (IExtmpMetaKeysBase.FIELD_JSFUNKTIONSNAME.equals(key)) {
			final String jsFunktionsName = (String) super.getValue(IExtmpMetaKeysBase.FIELD_JSFUNKTIONSNAME);
			final Locale sprache = pruefeUndExtrahiereSprachParameter(IExtmpMetaKeysBase.FIELD_JSFUNKTIONSNAME, parameter);
			return String.format("%s_%s_%s", jsFunktionsName, sprache.getLanguage(), sprache.getCountry());
		}
		if (IMetaKeys.FIELD_LABEL.equals(key) || IExtmpMetaKeysBase.FIELD_OBERFLAECHENTEXT.equals(key)) {
			final Map<Locale, String> texte = (Map<Locale, String>) super.getValue(key);
			final Locale sprache = pruefeUndExtrahiereSprachParameter(key, parameter);
			return (texte.getOrDefault(sprache, ""));
		}
		if (IExtmpMetaKeysBase.FIELD_INDEX_FIELD_REQUIRED_4_SB_KZ_QUERY.equals(key)) {
			String indexFieldRequired4SbKzQueryUniqueName = (String) super.getValue(key);
			if (indexFieldRequired4SbKzQueryUniqueName == null) {
				return null;
			} else {
				return metaModel.getValue(IMetaKeys.MODEL_META_FIELD,
						indexFieldRequired4SbKzQueryUniqueName);
			}
		}
		if (IExtmpMetaKeysBase.FIELD_KENNZAHL.equals(key)) {
			checkNumberOfParameters(2, parameter);
			long[] indices = (long[]) parameter[0];
			String indexValue = (String) parameter[1];
			return getKennzahl(indices, indexValue);
		}
		if (IMetaKeys.FIELD_CUSTOM_ERROR_MESSAGE_FOR_REQUIRED.equals(key)) {
			if (!(boolean) this.getValue(IMetaKeys.FIELD_EXPLICITLY_MANDATORY)) {
				return null;
			}
			final Locale locale = pruefeUndExtrahiereSprachParameter(IMetaKeys.FIELD_CUSTOM_ERROR_MESSAGE_FOR_REQUIRED,
					parameter);
			final String errorText = errorTexts4Req.get(locale);
			if (errorText != null) {
				return errorText;
			}
			return FehlerHandler.getFehlertext(RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER,
					metaModel.getMetaDataValidierung().isExtmp(), locale);
		}
		if (informationIsInFeldTyp(key)) {
			switch (key) {
				case IMetaKeys.FIELD_ENUMERATION_VALUES, IMetaKeys.FIELD_ENUMERATION_UI_VALUES -> {
					final String typ = (String) getValue(IMetaKeys.FIELD_FIELD_TYPE);
					List<String> werte;
					if (IMetaKeys.FIELD_ENUMERATION_UI_VALUES.equals(key) && IMetaKeys.FIELD_TYPE_ENUMERATION.equals(typ)
							&& parameter != null && parameter.length != 0) {
						final Locale sprache = pruefeUndExtrahiereSprachParameter(key, parameter);
						// Enumeration mit Sprachparameter: es wird eine Liste von Pair (Oberflächen-/XML-Wert) zurückgegeben
						final List<Pair<String, String>> wertePaare = ((FormatDefinitionString) formatDef).getEnumOberflaechenWerte(
								sprache);
						if (wertePaare == null) {
							return new ArrayList<Pair<String, String>>();
						}
						// kopiere Werte:
						return copyListOfPairs(wertePaare);
					} else if (IMetaKeys.FIELD_ENUMERATION_VALUES.equals(key) && (IMetaKeys.FIELD_TYPE_EXT_ENUMERATION.equals(
							typ) || IMetaKeys.FIELD_TYPE_STRING.equals(typ))) {
						final Locale sprache = pruefeUndExtrahiereSprachParameter(key, parameter);
						werte = ((FormatDefinitionString) formatDef).getHintListValues(sprache);
						if (werte != null && !werte.isEmpty()) {
							return new ArrayList<>(werte);
						}
					}
					werte = (List<String>) formatDef.getWert(key);
					if (werte == null) {
						return new ArrayList<String>();
					}
					// kopiere Werte:
					return new ArrayList<>(werte);
				}
				case IMetaKeys.FIELD_ENUMERATION_CATEGORY_VALUES -> {
					final String typ = (String) getValue(IMetaKeys.FIELD_FIELD_TYPE);
					if (IMetaKeys.FIELD_TYPE_ENUMERATION.equals(typ)) {
						Map<String, Map<String, String>> categoryValueByXmlValue = (Map<String, Map<String, String>>) formatDef.getWert(
								key);
						final Map<String, Map<String, String>> result = new HashMap<>();
						//copy value to return
						for (Entry<String, Map<String, String>> entry : categoryValueByXmlValue.entrySet()) {
							String copiedXmlValue = entry.getKey();
							final Map<String, String> copiedCatValues = new HashMap<>(entry.getValue());
							result.put(copiedXmlValue, copiedCatValues);
						}
						return result;
					}
					return new HashMap<String, Map<String, String>>();
				}

				//Sonderbehandlung Länge
				case IMetaKeys.FIELD_MAX_LENGTH, IMetaKeys.FIELD_MIN_LENGTH -> {
					final String typ = (String) getValue(IMetaKeys.FIELD_FIELD_TYPE);
					if (typ.startsWith("Datum") || typ.startsWith("Ja") || isNumber(
							typ) || this.formatDef instanceof FormatDefinitionVordefiniert) {
						if (parameter != null && parameter.length > 0) {
							final Object o = parameter[0];
							assert o instanceof PresentationInformation : "The additional parameter must by of type 'PresentationInformation'.";
							final Locale sprache = ((parameter.length > 1) && parameter[1] instanceof Locale) ?
									(Locale) parameter[1] :
									null;
							if (sprache != null) {
								new LocaleUtil(
										(List<Locale>) metaModel.getValue(IMetaKeys.MODEL_LANGUAGES))
										.checkLocale(sprache);
							}
							try {
								final PresentationInformation info = (PresentationInformation) o;
								return getLaengeMitDarstellungsInfo(info, key, sprache);
							} catch (final ClassCastException e) {
								// bei falsch übergebenem Parameter keine extra
								// Behandlung
							}
						}
					}
					// Sonderbehandlung Enumeration
					if (IMetaKeys.FIELD_TYPE_ENUMERATION.equals(typ)) {
						final List<String> werte = (List<String>) getValue(IMetaKeys.FIELD_ENUMERATION_VALUES);
						int laenge = werte.get(0).length();
						for (int i = 1; i < werte.size(); i++) {
							final int actLaenge = werte.get(i).length();
							if (key.equals(IMetaKeys.FIELD_MIN_LENGTH) && laenge > actLaenge) {
								laenge = actLaenge;
							}
							if (key.equals(IMetaKeys.FIELD_MAX_LENGTH) && laenge < actLaenge) {
								laenge = actLaenge;
							}
						}
						return laenge;

					}
					// Sonderbehandlung sonstiger Strings
					else if ("String".equals(typ)) {
						final FormatDefinitionString sf = (FormatDefinitionString) formatDef;
						if (key.equals(IMetaKeys.FIELD_MIN_LENGTH)) {
							return sf.getMinLaengePrintable();
						} else {
							return sf.getMaxLaengePrintable();
						}
					}
				}
			}

			if (IMetaKeys.FIELD_ERROR_MESSAGE.equals(key)) {
				final String typ = (String) getValue(IMetaKeys.FIELD_FIELD_TYPE);
				if (IMetaKeys.FIELD_TYPE_ENUMERATION.equals(typ)) {
					final Locale sprache = pruefeUndExtrahiereSprachParameter(IMetaKeys.FIELD_ERROR_MESSAGE, parameter);
					// If no error message was specified, we use the default one for non-source-models
					return ((FormatDefinitionString) formatDef).getEnumFehlertext(
							metaModel.getMetaDataValidierung().isExtmp(), sprache);
				} else if ("String".equals(typ)) {
					final Map<Locale, String> fehlertexte = ((FormatDefinitionString) formatDef).getFehlertexte();
					final Locale sprache = pruefeUndExtrahiereSprachParameter(IMetaKeys.FIELD_ERROR_MESSAGE, parameter);
					return fehlertexte.get(sprache);
				}
			}
			return formatDef.getWert(key);
		}
		return super.getValue(key);

	}

	private IIdentifier concretizeIndices(IIdentifier operand, IIdentifier field) {
		long[] indices = operand.getIndices().clone();
		long usb = operand.getUsb();

		for (int i = 0; i < indices.length; i++) {
			if (indices[i] == IIdentifier.ITERATION) {
				assert field.getIndices().length > i : "The dynamic enum field has less levels than iterations in the operand?!? This should never be possible.";
				assert field.getIndices()[i] > 0 : "The given field identifier must be concrete!";
				indices[i] = field.getIndices()[i];
			}
		}
		if (usb == IIdentifier.ITERATION) {
			assert field.getUsb() > 0 : "The given field identifier must be concrete!";
			usb = field.getUsb();
		}
		return new RtExternalIdentifier(operand.getName(), indices, usb);
	}

	private @NonNull List<Pair<String, String>> calculateEnumValues(
			@NonNull IIdentifier field,
			@NonNull IData data,
			ValidationMode modeOrNull
	) {
		DynamicEnumerationCommand dec = new DynamicEnumerationCommand(modeOrNull, metaModel.getMetaDataValidierung(), data);
		return dec.calcEnumValues(field, data);
	}

	private List<Pair<String, String>> copyListOfPairs(final List<Pair<String, String>> origList) {
		final List<Pair<String, String>> copiedList = new ArrayList<>();
		for (final Pair<String, String> origPair : origList) {
			copiedList.add(new Pair<>(origPair));
		}
		return copiedList;
	}

	@SuppressWarnings("unchecked")
	private Locale pruefeUndExtrahiereSprachParameter(final String key, final Object[] parameter) {
		// Locale.GERMANY ExtMP fallback
		Locale sprache = Locale.GERMANY; // default ist deutsch (wenn keine Sprache als Parameter übergeben)
		if (parameter != null && parameter.length != 0) {
			if (!checkParameterAngegeben(key, 1, parameter)) {
				throw new IllegalArgumentException("The given parameter is not correct.");
			}
			sprache = new LocaleUtil((List<Locale>) metaModel.getValue(IMetaKeys.MODEL_LANGUAGES))
					.deriveLocaleFromFirstParam(parameter);
			if (sprache == null) {
				throw new IllegalArgumentException(
						String.format("The additional parameter for the key '%s' must be of type Locale.", key));
			}
		}
		return sprache;
	}

	private char getDezimalTrenner() {
		return ((String) this.getValue("DezimalTrenner")).charAt(0);
	}

	boolean informationIsInFeldTyp(final String key) {
		return (key.equals(IMetaKeys.FIELD_MIN_LENGTH) ||
				key.equals(IMetaKeys.FIELD_MAX_LENGTH) ||
				key.equals(IMetaKeys.FIELD_FIELD_TYPE) ||
				key.equals(IMetaKeys.FIELD_PATTERN) ||
				key.equals(IMetaKeys.FIELD_ERROR_MESSAGE) ||
				key.equals(IMetaKeys.FIELD_WORD_WRAP) ||
				key.equals(IMetaKeys.FIELD_DECIMAL_PLACES) ||
				key.equals(IMetaKeys.FIELD_MAX_DECIMAL_PLACES) ||
				key.equals(IMetaKeys.FIELD_SIGN) ||
				key.equals(IMetaKeys.FIELD_INTEGER_DIGIT) ||
				key.equals(IMetaKeys.FIELD_ZERO_ALLOWED) ||
				key.equals(IMetaKeys.FIELD_LEADING_ZEROS) ||
				key.equals(IMetaKeys.FIELD_FORMAT) ||
				key.equals(IMetaKeys.FIELD_VALUE_TRUE) ||
				key.equals(IMetaKeys.FIELD_VALUE_FALSE) ||
				key.equals(IMetaKeys.FIELD_ENUMERATION_VALUES) ||
				key.equals(IMetaKeys.FIELD_ENUMERATION_UI_VALUES) ||
				key.equals(IMetaKeysInternal.FIELD_TEILBEKANNTART) ||
				key.equals(IMetaKeys.FIELD_ENUMERATION_CATEGORY_VALUES)) ||
				key.equals(IMetaKeys.FIELD_NO_VALUE_VALIDATION) ||
				key.equals(IMetaKeys.FIELD_S_VALUE);
	}

	public String getKennzahl(long[] indices, String indexValue) {
		Pair<String, String> sbKz = getSbKz(indices, indexValue);
		return sbKz == null ? null : sbKz.getEntry2();
	}

	private Pair<String, String> getSbKz(long[] indices, String indexValue) {
		IMetaField indexField = MetaDataUtilValidierung.getIndexField4SbKzQuery(this);
		if (!areParametersOK(indices, indexValue, indexField)) {
			return null;
		}
		Map<List<Long>, Pair<String, String>> sbKzMap = MetaDataUtilValidierung.getSbKzMap(this);
		if (sbKzMap == null || sbKzMap.isEmpty()) {
			return null;
		}
		List<Long> key = createIndices(indices, indexValue, indexField);
		return sbKzMap.get(key);
	}

	private boolean areParametersOK(long[] indices, String indexValue, IMetaField indexField) {
		List<Long> maxIndices = metaModel.getMetaDataValidierung().getMaxFieldIndices(this);
		if (maxIndices.size() != indices.length) {
			return false;
		}
		for (int i = 0; i < indices.length; i++) {
			if (indices[i] < 1 || indices[i] > maxIndices.get(i)) {
				return false;
			}
		}
		if ((indexField != null && StringUtils.isEmpty(indexValue)) ||
				(indexField == null && !StringUtils.isEmpty(indexValue))) {
			return false;
		}
		if (indexField != null) {
			@SuppressWarnings("unchecked")
			List<String> enumValues = (List<String>) indexField.getValue(IMetaKeys.FIELD_ENUMERATION_VALUES);
			return enumValues.contains(indexValue);
		}
		return true;
	}

	/**
	 * In case the kennzahl flag was set to <code>true</code> the indices at the corresponding
	 * position
	 * (see key <code>SbKzRepetition1</code> for {@link IMetaField}) are set to 1.
	 * In case the field exists in a group which was mapped to a root group splitting, the right
	 * index
	 * for the splitting hierarchy level is set according to the given index value.
	 * Otherwise the given array of indices is converted to a list.
	 */
	private List<Long> createIndices(long[] indices, String indexValue, IMetaField indexField) {
		// we copy the array because we are going to change it
		long[] indicesCopy = Arrays.copyOf(indices, indices.length);
		if (indexField != null) {
			@SuppressWarnings("unchecked")
			List<String> enumValues = (List<String>) indexField.getValue(IMetaKeys.FIELD_ENUMERATION_VALUES);
			int hierarchyLevel4IndexField =
					EntityUtil.getIndexOfHierarchyLevel((String) indexField.getValue(IMetaKeys.FIELD_FULLNAME)) - 1;
			indicesCopy[hierarchyLevel4IndexField] = (long) enumValues.indexOf(indexValue) + 1;
		}
		List<Integer> sbKzRepetiton1List = MetaDataUtilValidierung.getSbKzRepetition1List(this);
		if (sbKzRepetiton1List != null) {
			for (Integer item : sbKzRepetiton1List) {
				indicesCopy[item] = 1;
			}
		}

		return Arrays.stream(indicesCopy).boxed().collect(Collectors.toList());
	}
}
