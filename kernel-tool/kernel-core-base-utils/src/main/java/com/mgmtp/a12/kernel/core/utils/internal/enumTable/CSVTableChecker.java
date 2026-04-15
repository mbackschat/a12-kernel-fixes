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

package com.mgmtp.a12.kernel.core.utils.internal.enumTable;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.charset.LegalCharTester;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.Pair;

/**
 * Checks the consistency of a CSV table used for enumerations with table reference.
 */
public class CSVTableChecker {
	private static final int MAX_LENGTH_VALUE = 500;
	public static final String CSV_SEPARATOR = ";";
	public static final String DISPLAY_SUFFIX = "_Display";

	// Note: the pattern has to be consistent with the meta model fields
	// /MVK/Fields/FieldType/EnumerationType/columnName and /MVK/Fields/FieldType/EnumerationType/Categories/name
	private static final Pattern COLUMN_HEADER_PATTERN = Pattern.compile(
			"([a-zA-Z][0-9a-zA-Z]{0,100})((\\.[a-zA-Z][0-9a-zA-Z_]{0,99})|(" + DISPLAY_SUFFIX + "))?"); //$NON-NLS-2$

	// The column numbers of enumeration columns
	private List<Integer> enumColumns = new ArrayList<>();
	// The combinations of enumeration values used throughout the table with the line number
	private Map<List<String>, Integer> usedEnumValueCombinations = new HashMap<>();
	// The combination of column names that need to be unique (Display- <-> Enum-Value)
	private List<Pair<Integer, Integer>> uniquenessColumns = new ArrayList<>();
	// The values used in the uniqueness columns as map from left value to right values and as right value to left value.
	// A bidirectional map would be more efficient. Do we have one in the API?
	private List<Pair<Map<String, String>, Map<String, String>>> uniquenessValues = new ArrayList<>();
	// The column names
	private List<String> columnNames = null;
	private final CSVTableErrorLookup errorLookup;

	private CSVTableChecker() {
		this(Locale.GERMANY);
	}

	/**
	 * Construct the table checker with the given locale used for error reporting.
	 * Currently, only German properties are supported. If another locale is needed, the error
	 * messages in internal/kernel_core_errortext_error_messages need to be translated.
	 *
	 * @param locale
	 * 		the {@link Locale} used for error reporting.
	 */
	private CSVTableChecker(Locale locale) {
		errorLookup = new CSVTableErrorLookup(locale);
	}

	/**
	 * Check the given table.
	 *
	 * @param tableData
	 * 		The table data as an UTF-8 encoded String.
	 * @param tableName
	 * 		the name of the table for error reporting.
	 * @param allowedCharacters
	 * 		the allowed characters for the DV. This may be <code>null</code> if there is no
	 * 		restriction.
	 * @param pr
	 * 		the problem reporter receiving error messages.
	 * @return <code>true</code> if the table is valid, <code>false</code> otherwise.
	 */
	public static boolean checkCsvTable(byte[] tableData, String tableName, List<String> allowedCharacters, IProblemReporter pr) {
		return new CSVTableChecker().checkCsvTableIntern(tableData, tableName, allowedCharacters, pr);
	}

	private boolean checkCsvTableIntern(byte[] tableData, String tableName, List<String> allowedCharacters, IProblemReporter pr) {
		boolean isValid = true;
		// We want to get UTF-8 errors, so we have to use the constructor with explicit decoder
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(tableData), decoder))) {
			boolean first = true;
			String line;
			// The first line is not counted.
			int lineNo = 2;
			while ((line = br.readLine()) != null) {
				if (first) {
					if (!checkHeader(line, lineNo, tableName, allowedCharacters, pr)) {
						// Errors in headers are fatal and could lead to unexpected follow up errors
						return false;
					}
					first = false;
				} else {
					isValid = isValid && checkValueRow(line, lineNo, tableName, allowedCharacters, pr);
					lineNo++;
				}
			}
			if (lineNo == 2) {
				pr.reportProblem(new TableProblem(errorLookup.getErrorMessage(CSVTableErrorType.EMPTY_TABLE), tableName));
				isValid = false;
			}
		} catch (IOException e) {
			pr.reportProblem(new TableProblem(errorLookup.getErrorMessage(CSVTableErrorType.UTF8_ENCODING_WRONG), tableName));
			isValid = false;
		}
		return isValid;
	}

	private boolean checkHeader(String line, int lineNo, String tableName, List<String> allowedCharacters, IProblemReporter pr) {
		return checkLine(line, lineNo, tableName, allowedCharacters, pr, true);
	}

	private boolean checkValueRow(String line, int lineNo, String tableName, List<String> allowedCharacters, IProblemReporter pr) {
		return checkLine(line, lineNo, tableName, allowedCharacters, pr, false);
	}

	private boolean checkLine(String line, int lineNo, String tableName, List<String> allowedCharacters, IProblemReporter pr,
			boolean first) {
		if (StringUtils.isBlank(line)) {
			pr.reportProblem(new TableProblem(errorLookup.getErrorMessage(CSVTableErrorType.EMPTY_LINE, lineNo), tableName));
			return false;
		}
		String[] partsArr = line.split(CSV_SEPARATOR, -1);
		List<String> parts = Arrays.stream(partsArr).map(String::trim).collect(Collectors.toList());
		if (first) {
			return checkAndInitHeaderColumns(parts, pr, tableName);
		}

		if (parts.size() != columnNames.size()) {
			pr.reportProblem(new TableProblem(
					errorLookup.getErrorMessage(CSVTableErrorType.NUM_COLUMNS_DONT_MATCH, parts.size(), lineNo, columnNames.size()),
					tableName));
			return false;
		}
		return checkAndCollectTableValues(parts, lineNo, allowedCharacters, pr, tableName);
	}

	private boolean checkAndCollectTableValues(List<String> values, int lineNo, List<String> allowedCharacters, IProblemReporter pr,
			String tableName) {
		boolean isValid = IntStream.range(0, values.size())
				.mapToObj(c -> checkTableValue(values.get(c), lineNo, c + 1, allowedCharacters, pr, tableName))
				.allMatch(b -> b);
		List<String> enumValues = enumColumns.stream()
				.map(values::get)
				.collect(Collectors.toList());
		Integer prevLine = usedEnumValueCombinations.put(enumValues, lineNo);
		if (prevLine != null) {
			pr.reportProblem(
					new TableProblem(errorLookup.getErrorMessage(CSVTableErrorType.ENUM_VALUES_NOT_UNIQUE, prevLine, lineNo), pr));
			isValid = false;
		}
		isValid = isValid && IntStream.range(0, uniquenessColumns.size())
				.mapToObj(i -> addUniquenessColumns(uniquenessColumns.get(i), i, values, pr, tableName))
				.allMatch(b -> b);
		return isValid;
	}

	private boolean addUniquenessColumns(Pair<Integer, Integer> uniquePair, int idx, List<String> values, IProblemReporter pr,
			String tableName) {
		boolean isValid = true;
		String leftValue = values.get(uniquePair.getEntry1());
		String rightValue = values.get(uniquePair.getEntry2());
		Pair<Map<String, String>, Map<String, String>> uniqueValues = uniquenessValues.get(idx);
		String oldRightValue = uniqueValues.getEntry1().put(leftValue, rightValue);
		if (oldRightValue != null && !oldRightValue.equals(rightValue)) {
			pr.reportProblem(new TableProblem(errorLookup.getErrorMessage(CSVTableErrorType.XML_UI_VALUE_NOT_UNIQUE,
					columnNames.get(uniquePair.getEntry1()),
					leftValue,
					oldRightValue, rightValue,
					columnNames.get(uniquePair.getEntry2())), tableName));
			isValid = false;
		}
		String oldLeftValue = uniqueValues.getEntry2().put(rightValue, leftValue);
		if (oldLeftValue != null && !oldLeftValue.equals(leftValue)) {
			pr.reportProblem(new TableProblem(errorLookup.getErrorMessage(CSVTableErrorType.XML_UI_VALUE_NOT_UNIQUE,
					columnNames.get(uniquePair.getEntry2()),
					rightValue,
					oldLeftValue, leftValue,
					columnNames.get(uniquePair.getEntry1())), tableName));
			isValid = false;
		}
		return isValid;
	}

	private boolean checkTableValue(String value, int lineNo, int errColNo, List<String> allowedCharacters, IProblemReporter pr,
			String tableName) {
		if (value.isEmpty()) {
			pr.reportProblem(
					new TableProblem(errorLookup.getErrorMessage(CSVTableErrorType.EMPTY_VALUE, lineNo, errColNo), tableName));
			return false;
		}
		if (value.length() > MAX_LENGTH_VALUE) {
			pr.reportProblem(
					new TableProblem(errorLookup.getErrorMessage(CSVTableErrorType.VALUE_TOO_LONG, lineNo, errColNo), tableName));
			return false;
		}
		if (allowedCharacters.isEmpty()) {
			return true;
		}
		final Optional<LegalCharTester> legalCharTesterOpt = LegalCharTester.fromGraphemeSet(allowedCharacters);
		if (legalCharTesterOpt.isEmpty()) {
			return true; // should have been caught before or will be caught later
		}
		final LegalCharTester legalCharTester = legalCharTesterOpt.get();
		final List<Integer> errorPositions = legalCharTester.findErrorIndizes(value);
		errorPositions.forEach(i -> pr.reportProblem(
				new TableProblem(
						errorLookup.getErrorMessage(CSVTableErrorType.CHARACTERS_NOT_ALLOWED,
								lineNo, errColNo, i + 1, value),
						tableName)));
		return errorPositions.isEmpty();
	}

	private boolean checkAndInitHeaderColumns(List<String> headerParts, IProblemReporter pr, String tableName) {
		boolean isValid = true;
		for (int i = 0; i < headerParts.size(); i++) {
			String headerCol = headerParts.get(i);
			Matcher m = COLUMN_HEADER_PATTERN.matcher(headerCol);
			if (!m.matches()) {
				pr.reportProblem(
						new TableProblem(errorLookup.getErrorMessage(CSVTableErrorType.COLUMN_NAME_WRONG, headerCol), tableName));
				isValid = false;
				continue;
			}
			if (m.group(2) != null && !headerParts.contains(m.group(1))) {
				pr.reportProblem(
						new TableProblem(errorLookup.getErrorMessage(CSVTableErrorType.ENUM_COLUMN_MISSING, headerCol, m.group(1)),
								tableName));
				isValid = false;
			}
			if (m.group(2) == null) {
				enumColumns.add(i);
				String displayCol = headerCol + DISPLAY_SUFFIX;
				if (headerParts.contains(displayCol)) {
					int dispColNum = headerParts.indexOf(displayCol);
					uniquenessColumns.add(Pair.of(i, dispColNum));
				}
			}
		}
		columnNames = headerParts;
		// Initialize the uniquenessValues with empty maps.
		IntStream.range(0, uniquenessColumns.size()).forEach(i -> uniquenessValues.add(Pair.of(new HashMap<>(), new HashMap<>())));
		return isValid;
	}
}
