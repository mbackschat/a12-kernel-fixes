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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RuntimeUtils;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.utils.conversion.internal.StringConversion;

import lombok.Getter;
import lombok.NonNull;

/**
 * Klasse modelliert zur Laufzeit ein String Feld.
 */
public class FormatDefinitionString implements IFormatDefinition {

	private Pattern pattern = null;
	private final String feldTyp;
	private String muster = null;
	private String musterDisp = null;
	private Map<Locale, String> errorTexts = new HashMap<>();
	private int minLaenge;
	@Getter private final int maxLaenge;
	@Getter private int minLaengePrintable = -1;
	private int maxLaengePrintable = -1;
	private final boolean lineBreakAllowed;
	private final Boolean lineBreakAllowedDisp;
	@Getter private boolean noValueValidation = false;
	private List<String> enumWerte = new ArrayList<>();
	private Map<Locale, List<String>> values = new HashMap<>();
	private Map<Locale, List<Pair<String, String>>> enumOberflaechenWerte = new HashMap<>();
	private final Map<Locale, Map<String, String>> enumXML2OflMap = new HashMap<>();
	private final Map<Locale, Map<String, String>> enumOfl2XMLMap = new HashMap<>();
	private Map<String, Map<String, String>> enumCategoryValues = new HashMap<>();
	private final List<Locale> supportedLocales;
	private String sValue = "";
	private final Map<String, List<String>> table;
	private final Map<List<String>, List<Map<String, String>>> hierarchicalMap;
	@Getter private final List<String> dependentColumns;
	@Getter private final String columnName;
	private final String uiColumnName;
	private Set<String> distinctXmlValues = null;

	/**
	 * Konstruktor für String-Felder
	 */
	public FormatDefinitionString(int minLaenge, int maxLaenge, int minLaengePrintable, int maxLaengePrintable,
			String muster, Map<Locale, String> fehlertexte, boolean lba, boolean nvv, Map<Locale, List<String>> hintLists) {
		this.minLaenge = minLaenge;
		this.maxLaenge = maxLaenge;
		this.minLaengePrintable = minLaengePrintable;
		this.maxLaengePrintable = maxLaengePrintable;
		this.muster = muster;
		this.musterDisp = muster;
		this.errorTexts = Collections.unmodifiableMap((fehlertexte != null ? fehlertexte : new HashMap<Locale, String>()));
		this.lineBreakAllowed = lba;
		this.lineBreakAllowedDisp = lba;
		this.noValueValidation = nvv;
		feldTyp = IFormatDefinition.FeldFormatEnumType.ER_STRING.toString();
		if (!muster.isEmpty()) {
			pattern = Pattern.compile(muster);
		} else {
			this.musterDisp = null;
		}
		if (this.minLaenge == -1) {
			this.minLaenge = 1;
		}
		this.supportedLocales = new ArrayList<>();
		this.table = null;
		this.hierarchicalMap = null;
		this.dependentColumns = null;
		this.columnName = null;
		this.uiColumnName = null;
		if (hintLists != null) {
			this.values = copyHintList(hintLists);
		}
	}

	/**
	 * Konstruktor für Enumerationen
	 */
	public FormatDefinitionString(
			Map<Locale, String> fehlertexte,
			List<Locale> supportedLocales,
			String[] xmlWerte,
			Map<Locale, List<Pair<String, String>>> oberflaechenWerte,
			Map<String, Map<String, String>> categoryValues,
			String sValue) {
		this.minLaenge = 1;
		this.maxLaenge = -1;
		this.muster = "";
		musterDisp = null;
		lineBreakAllowed = false;
		lineBreakAllowedDisp = null;
		noValueValidation = false;
		this.errorTexts = Collections.unmodifiableMap((fehlertexte != null ? fehlertexte : new HashMap<Locale, String>()));
		feldTyp = IFormatDefinition.FeldFormatEnumType.ER_ENUMERATION.toString();

		Collections.addAll(enumWerte, xmlWerte);
		distinctXmlValues = new HashSet<>(enumWerte);

		Map<Locale, List<Pair<String, String>>> oflWerte = new HashMap<>();
		if (oberflaechenWerte != null) {
			for (Entry<Locale, List<Pair<String, String>>> entry : oberflaechenWerte.entrySet()) {
				Locale sprache = entry.getKey();
				List<Pair<String, String>> oflWerteFuerSprache = entry.getValue();
				oflWerte.put(sprache, Collections.unmodifiableList(oflWerteFuerSprache));
			}
		}
		this.enumOberflaechenWerte = Collections.unmodifiableMap(oflWerte);

		for (Entry<Locale, List<Pair<String, String>>> entry : enumOberflaechenWerte.entrySet()) {
			Locale sprache = entry.getKey();
			List<Pair<String, String>> oflXmlWerte = entry.getValue();
			enumXML2OflMap.put(sprache, new HashMap<>());
			enumOfl2XMLMap.put(sprache, new HashMap<>());
			for (Pair<String, String> oflXml : oflXmlWerte) {
				enumXML2OflMap.get(sprache).put(oflXml.getEntry2(), oflXml.getEntry1());
				enumOfl2XMLMap.get(sprache).put(oflXml.getEntry1(), oflXml.getEntry2());
			}
		}

		Map<String, Map<String, String>> catValues = new HashMap<>();
		if (categoryValues != null) {
			for (Entry<String, Map<String, String>> entry : categoryValues.entrySet()) {
				String xmlValue = entry.getKey();
				Map<String, String> categoryValuesForXmlValue = entry.getValue();
				catValues.put(xmlValue, Collections.unmodifiableMap(categoryValuesForXmlValue));
			}
		}
		this.enumCategoryValues = Collections.unmodifiableMap(catValues);
		this.sValue = sValue;
		this.supportedLocales = supportedLocales;
		this.table = null;
		this.hierarchicalMap = null;
		this.dependentColumns = null;
		this.columnName = null;
		this.uiColumnName = null;
	}

	/**
	 * Constructor for enumerations with table reference.
	 *
	 * @param tableName
	 * 		the name of the table.
	 * @param columnName
	 * 		the name of the column for the XML-values.
	 * @param dependentColumns
	 * 		the columns, the enumeration depends on.
	 */
	public FormatDefinitionString(Map<Locale, String> errorTexts, List<Locale> supportedLocales, String tableName,
			String columnName,
			String[] dependentColumns, ITableCache tableCache) {

		this.errorTexts = Collections.unmodifiableMap((errorTexts != null ? errorTexts : new HashMap<Locale, String>()));
		this.supportedLocales = supportedLocales;
		this.table = tableCache.getTableData(tableName);
		this.dependentColumns = Arrays.asList(dependentColumns);
		this.columnName = columnName;
		String uiName = columnName + "_Display";
		this.uiColumnName = table.containsKey(uiName) ? uiName : columnName;

		this.minLaenge = 1;
		this.maxLaenge = -1;
		this.muster = "";
		musterDisp = null;
		lineBreakAllowed = false;
		lineBreakAllowedDisp = null;
		noValueValidation = false;

		feldTyp = IFormatDefinition.FeldFormatEnumType.ER_ENUMERATION.toString();

		this.hierarchicalMap = createHierarchicalMap();
		this.enumWerte = table.get(this.columnName).stream()
				.distinct()
				.collect(Collectors.toList());
		this.distinctXmlValues = new HashSet<>(enumWerte);
		if (!uiColumnName.equals(this.columnName)) {
			calculateXmlUiMaps(supportedLocales);
		}
	}

	private void calculateXmlUiMaps(List<Locale> supportedLocales) {
		Map<String, String> xml2OflMap = new HashMap<>();
		Map<String, String> ofl2XmlMap = new HashMap<>();
		List<Pair<String, String>> enumUiValues = new ArrayList<>();
		List<String> xmlValues = table.get(columnName);
		List<String> uiValues = table.get(uiColumnName);
		Set<String> seenXmlValues = new HashSet<>();
		if (xmlValues.size() != uiValues.size()) {
			throw new IllegalArgumentException("Inconsistent table for columns " + columnName + " and " + uiColumnName);
		}
		int numRows = xmlValues.size();
		for (int i = 0; i < numRows; i++) {
			String xmlValue = xmlValues.get(i);
			String uiValue = uiValues.get(i);
			xml2OflMap.put(xmlValue, uiValue);
			ofl2XmlMap.put(uiValue, xmlValue);
			if (seenXmlValues.add(xmlValue)) {
				enumUiValues.add(new Pair<>(uiValue, xmlValue));
			}
		}
		for (Locale locale : supportedLocales) {
			enumXML2OflMap.put(locale, xml2OflMap);
			enumOfl2XMLMap.put(locale, ofl2XmlMap);
			enumOberflaechenWerte.put(locale, enumUiValues);
		}
		enumOberflaechenWerte = Collections.unmodifiableMap(enumOberflaechenWerte);
	}

	/**
	 * Build the hierarchical map.<br>
	 * This is a map to select all rows corresponding with a list of values for the dependent
	 * columns.
	 * <br>
	 * The hierarchical map contains a key for any combination of valid values for the dependent
	 * columns in a row. The corresponding value is a list with an entry for each row, where the
	 * values of the dependent columns are this combination. Each row is represented as a map
	 * containing the mapping from column name to value in that row for all column names starting
	 * with the column name of the current enumeration, i.e. the columns that correspond with this
	 * enumeration.
	 * <p>
	 * E.g. for an enumeration with column name 'a' and dependent columns 'b','c' and the following
	 * table<br>
	 * a,a_Display,a.cat1,a.cat2,b,c<br>
	 * v1,ad1,ac11,ac21,b1,c1<br>
	 * v2,ad2,ac11,ac22,b1,c1<br>
	 * v3,ad3,ac12,ac21,b1,c2<br>
	 * v4,ad4,ac11,ac22,b2,c2<br>
	 * the hierarchical map would contain following entries:<br>
	 * <code>
	 * {<br>
	 * (b1,c1):
	 * [{a:v1,a_Display:ad1,a.cat1:ac11,a.cat2:ac21},{a:v2,a_Display:ad2,a.cat1:ac11,a.cat2:ac22}],<br>
	 * (b1,c2): [{a:v3,a_Display:ad3,a.cat1:ac12,a.cat2:ac21}],<br>
	 * (b2,c2): [{a:v4,a_Display:ad4,a.cat1:ac11,a.cat2:ac22}]<br>}
	 * </code>
	 *
	 * @return the hierarchical map.
	 */
	private Map<List<String>, List<Map<String, String>>> createHierarchicalMap() {
		Map<List<String>, List<Map<String, String>>> result = new LinkedHashMap<>();
		for (int i = 0; i < table.get(columnName).size(); i++) {
			List<String> dependentValues = getRowValues(table, dependentColumns, i);
			Map<String, String> rowValues = new HashMap<>();
			for (Entry<String, List<String>> entry : table.entrySet()) {
				if (entry.getKey().startsWith(columnName)) {
					rowValues.put(entry.getKey(), entry.getValue().get(i));
				}
			}
			result.computeIfAbsent(dependentValues, k -> new ArrayList<>())
					.add(rowValues);
		}
		return result;
	}

	private List<String> getRowValues(Map<String, List<String>> table, List<String> columnNames, int i) {
		return columnNames.stream()
				.map(v -> table.get(v).get(i))
				.toList();
	}

	/**
	 * Konstruktor für nichtabgeschlossene Enumerationen
	 */
	public FormatDefinitionString(int maxLaenge, Map<Locale, List<String>> werte) {
		this.minLaenge = 1;
		this.maxLaenge = maxLaenge;
		this.muster = "";
		musterDisp = null;
		lineBreakAllowed = false;
		lineBreakAllowedDisp = null;
		noValueValidation = false;
		feldTyp = IFormatDefinition.FeldFormatEnumType.ER_ENUMERATIONN_NICHT_ABGESCHLOSSEN.toString();
		this.values = copyHintList(werte);
		this.supportedLocales = new ArrayList<>();
		this.table = null;
		this.hierarchicalMap = null;
		this.dependentColumns = null;
		this.columnName = null;
		this.uiColumnName = null;
	}

	private Map<Locale, List<String>> copyHintList(Map<Locale, List<String>> hintList) {
		Map<Locale, List<String>> hintListValues = new HashMap<>();
		if (hintList != null) {
			for (Entry<Locale, List<String>> entry : hintList.entrySet()) {
				Locale sprache = entry.getKey();
				List<String> enumValuesForLanguage = entry.getValue();
				hintListValues.put(sprache, Collections.unmodifiableList(enumValuesForLanguage));
			}
		}
		return Collections.unmodifiableMap(hintListValues);
	}

	@Override
	public String convert(String wert,
			DarstellungsInformationIntern darstellungsInfo, boolean vonXml, RawParamHandler4Convert additionalParameters) {
		if (!enumXML2OflMap.isEmpty() && darstellungsInfo.doConvertEnumerationen()) {
			Locale sprache = additionalParameters.getSprache(supportedLocales);
			if (enumXML2OflMap.get(sprache).isEmpty()) {
				throw new IllegalArgumentException(
						"Conversion without enumeration values. Was this code generated for the UXML mapper application?");
			}
			if (vonXml) {
				String oflWert = enumXML2OflMap.get(sprache).get(wert);
				if (oflWert != null) {
					return oflWert;
				}
			} else {
				String xmlWert = enumOfl2XMLMap.get(sprache).get(wert);
				if (xmlWert != null) {
					return xmlWert;
				}
			}
		}
		return wert;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(this.getClass().getSimpleName());
		if (minLaenge > 0) {
			buf.append("\nMinLaenge ");
			buf.append(minLaenge);
		}
		if (maxLaenge > 0) {
			buf.append("\nMaxLaenge ");
			buf.append(maxLaenge);
		}
		if (!StringUtils.isEmpty(muster)) {
			buf.append("\nMuster ");
			buf.append(muster);
		}
		if (lineBreakAllowed) {
			buf.append("\nZeilenumbrüche erlaubt. ");
		}
		if (noValueValidation) {
			buf.append("\nKeine Werteprüfung. ");
		}
		if (!errorTexts.isEmpty()) {
			buf.append("\nFehlertext ");
			List<String> mehrsprachigerFehlertext = new ArrayList<>();
			for (Entry<Locale, String> entry : errorTexts.entrySet()) {
				Locale sprache = entry.getKey();
				String fehlertext = entry.getValue();
				mehrsprachigerFehlertext.add(sprache + " " + fehlertext);
			}
			Collections.sort(mehrsprachigerFehlertext);
			buf.append(StringUtils.join(mehrsprachigerFehlertext, ", "));
		}

		return buf.toString();
	}

	/**
	 * allgemeine Methode zum Validieren des Eingabewertes.
	 */
	@Override
	public ValidType validiereFormat(IValidierungParam param, IMetaDataValidierung metaData, boolean fullCheck) {
		String wert = param.getWert();
		Locale sprache = param.getSprache();

		if (isNoValueValidation()) {
			return ValidType.Valid;
		}

		if (!fullCheck) {
			return ValidType.Valid;
		}

		if (!lineBreakAllowed && (wert.indexOf('\n') > -1 || wert.indexOf('\r') > -1)) {
			String fehler = FehlerHandler.getFehlertext(RuntimeFormalErrorEnum.STRING_ENTHAELT_NEWLINE,
					param.isExtmp(), param.getSprache());
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_STRING_MIT_NEWLINE, fehler, param);
		}

		if (!wert.isEmpty()) {
			// Normalisierung durchführen,
			wert = RuntimeUtils.normalizeLinebreaks(wert);
		}

		if (pattern != null) {
			if (!pattern.matcher(wert).matches()) {
				String fehler = FehlerHandler.transformFehlerText(errorTexts.get(sprache), param);
				return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_STRING_FALSCHES_MUSTER, fehler, param);
			}
		} else if (distinctXmlValues != null &&
				!distinctXmlValues.isEmpty() &&
				!feldTyp.equals(IFormatDefinition.FeldFormatEnumType.ER_ENUMERATIONN_NICHT_ABGESCHLOSSEN.toString())) {
			// falls nicht abgeschlossene enum darf nicht geprüft werden, ob Wert in enum enthalten ist
			boolean match = false;
			if (param.getDarstellungsInfo().doConvertEnumerationen() && !enumOberflaechenWerte.isEmpty()) {
				Set<String> oberflaechenwerte = enumOfl2XMLMap.get(sprache).keySet();
				if (oberflaechenwerte.contains(wert)) {
					match = true;
				}
			} else {
				if (distinctXmlValues.contains(wert)) {
					match = true;
				}
			}
			if (!match) {
				String fehler = FehlerHandler.transformFehlerText(
						getEnumFehlertext(param.isExtmp(), sprache), param);
				return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_STRING_FALSCHES_MUSTER, fehler, param);
			}
		}

		int wertLength = wert.length();
		if (minLaenge > 0 && wertLength < minLaenge) {
			String fehler = FehlerHandler.getFehlertext(RuntimeFormalErrorEnum.STRING_ZU_KURZ,
					param.isExtmp(), param.getSprache()).replace("<minLength>", Integer.toString(minLaenge));
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_STRING_ZU_KURZ, fehler, param);
		}

		if (maxLaenge > 0 && wertLength > maxLaenge) {
			String fehler = FehlerHandler.getFehlertext(RuntimeFormalErrorEnum.STRING_ZU_LANG,
					param.isExtmp(), param.getSprache()).replace("<maxLength>", Integer.toString(maxLaenge));
			return FehlerHandler.addFehler(IFormaleFehlerConstants.FEHLER_STRING_ZU_LANG, fehler, param);
		}

		return ValidType.Valid;
	}

	@Override
	public String getDefaultValue(IMetaDataValidierung metaData, PresentationInformation presentationInformation, Locale sprache) {
		return "";
	}

	public Object getMaxLaengePrintable() {
		return maxLaengePrintable;
	}

	public Map<Locale, String> getFehlertexte() {
		return errorTexts;
	}

	/**
	 * @param locale
	 * 		the locale to retreive the enumeration values for.
	 * @return the list of enumeration values as {@link Pair} of UI-value and XML-value in that
	 * 		order.
	 */
	public List<Pair<String, String>> getEnumOberflaechenWerte(Locale locale) {
		return enumOberflaechenWerte.get(locale);
	}

	/**
	 * @param locale
	 * 		the locale to retreive the enumeration values for.
	 * @return the list of hintList values.
	 */
	public List<String> getHintListValues(Locale locale) {
		return values.get(locale);
	}

	public String getValue4Category(String xmlValue, String category) {
		if (table != null) {
			List<String> columnNames = table.get(columnName);
			int idx = columnNames.indexOf(xmlValue);
			if (idx < 0) {
				return null;
			}
			String categoryColumnName = columnName + "." + category;
			List<String> categoryColumn = table.get(categoryColumnName);
			return categoryColumn == null ? null : categoryColumn.get(idx);
		} else {
			Map<String, String> categoryValues4XmlValue = enumCategoryValues.get(xmlValue);
			return categoryValues4XmlValue == null ? null : categoryValues4XmlValue.get(category);
		}
	}

	public List<Pair<String, String>> getOflXmlValuesForDependentColumnValues(List<String> depColValues) {
		return extractOflXmlPair(selectRows(depColValues));
	}

	private List<Pair<String, String>> extractOflXmlPair(List<Map<String, String>> rows) {
		return rows.stream()
				.map(m -> new Pair<>(m.get(uiColumnName), m.get(columnName)))
				.distinct()
				.toList();
	}

	public Set<String> getDistinctValuesForDependentColumns(List<String> depColValues) {
		return extractUniqueValues(selectRows(depColValues));
	}

	private Set<String> extractUniqueValues(List<Map<String, String>> rows) {
		return rows.stream()
				.map(m -> m.get(columnName))
				.collect(Collectors.toSet());
	}

	/**
	 * Get the values of the rows selected by the given dependent values.
	 *
	 * @param depColumnValues
	 * 		the values to select the row for. The values must be in the order of the dependent
	 * 		columns.
	 * @return a list of maps from column name to value for the selected rows or an empty list.
	 */
	private List<Map<String, String>> selectRows(List<String> depColumnValues) {
		if (hierarchicalMap == null) {
			return Collections.emptyList();
		}
		return hierarchicalMap.getOrDefault(depColumnValues, Collections.emptyList());
	}

	@Override
	public Object getWert(String key) {
		return switch (key) {
			case IMetaKeys.FIELD_MIN_LENGTH -> minLaenge;
			case IMetaKeys.FIELD_MAX_LENGTH -> maxLaenge;
			case IMetaKeys.FIELD_FIELD_TYPE -> feldTyp;
			case IMetaKeys.FIELD_PATTERN -> musterDisp;
			case IMetaKeys.FIELD_WORD_WRAP -> lineBreakAllowedDisp;
			case String s when s.equals(IMetaKeys.FIELD_ENUMERATION_VALUES) && (
					(
							feldTyp.equals(FeldFormatEnumType.ER_STRING.toString()) ||
									feldTyp.equals(FeldFormatEnumType.ER_ENUMERATIONN_NICHT_ABGESCHLOSSEN.toString())
					) &&
							values != null && !values.isEmpty() && values.get(Locale.GERMANY) != null) -> {
				// wird kein Parameter übergeben, so werden die deutschen Werte zurückgegeben
				// Locale.GERMANY ExtMP fallback
				@SuppressWarnings("null") // values is never null, the guard checks it, but the java compiler does not recognize it
				List<String> enumValues = new ArrayList<>(values.get(Locale.GERMANY));
				yield Collections.unmodifiableList(enumValues);
			}
			case IMetaKeys.FIELD_ENUMERATION_VALUES -> Collections.unmodifiableList(enumWerte);
			case String s when s.equals(IMetaKeys.FIELD_ENUMERATION_UI_VALUES) && !enumOberflaechenWerte.isEmpty() -> {
				List<String> oberflaechenWerte = new ArrayList<>();
				// Locale.GERMANY ExtMP fallback
				for (Pair<String, String> oflXmlWert : enumOberflaechenWerte.get(Locale.GERMANY)) {
					oberflaechenWerte.add(oflXmlWert.getEntry1());
				}
				yield oberflaechenWerte;
			}
			case IMetaKeys.FIELD_ENUMERATION_CATEGORY_VALUES -> enumCategoryValues;
			case IMetaKeys.FIELD_NO_VALUE_VALIDATION -> noValueValidation;
			case IMetaKeys.FIELD_S_VALUE -> sValue;
			case null, default -> null;
		};
	}

	@Override
	public int compare(@NonNull String s1, @NonNull String s2, IMetaDataValidierung metaData) {
		if (!enumWerte.isEmpty() && !feldTyp.equals(
				IFormatDefinition.FeldFormatEnumType.ER_ENUMERATIONN_NICHT_ABGESCHLOSSEN.toString())) {
			// enum fields are sorted according to the order of the xml values
			return enumWerte.indexOf(s1) - enumWerte.indexOf(s2);
		} else {
			// string and extensible enums are alphabetically sorted
			return s1.compareToIgnoreCase(s2);
		}
	}

	@Override
	public String convertToBasicType(String internalValue, IMetaDataValidierung metaData) {
		return internalValue;
	}

	@Override
	public @NonNull String convertFromBasicType(@NonNull Object value, @NonNull IMetaDataValidierung metaDat) {
		return StringConversion.convertStringTypeToString(value);
	}

	public String getEnumFehlertext(boolean isExtmp, Locale language) {
		String fehlertext = errorTexts.get(language);
		if (fehlertext != null) {
			return fehlertext;
		}
		return FehlerHandler.getFehlertext(RuntimeFormalErrorEnum.ENUMERATION_DEFAULT_ERROR, isExtmp, language);
	}
}
