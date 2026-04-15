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

package com.mgmtp.a12.kernel.core.utils.internal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationDataTable;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.TableParsingException;
import com.mgmtp.a12.kernel.core.utils.internal.enumTable.CSVTableChecker;

/**
 * An implementation of <code>EnumerationDataTable</code> represents the data stored in a CSV table
 * used for enumerations.
 */
public class EnumerationDataTable implements IEnumerationDataTable {

	public static final String SEPARATOR = ";";

	protected final byte[] csvBytes;
	protected Map<String, List<String>> lazyColumns;
	private Map<String, Set<String>> columnsWithDistinctValues = new ConcurrentHashMap<>();
	protected final String name;

	/**
	 * @param name
	 * 		the name of the table
	 * @param csvBytes
	 * 		the UTF-8 encoded CSV-String.
	 */
	public EnumerationDataTable(String name, byte[] csvBytes) {
		this.name = name;
		this.csvBytes = csvBytes;
	}

	private Map<String, List<String>> parseCSV() {
		Map<String, List<String>> result = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(csvBytes), StandardCharsets.UTF_8))) {
			String header = reader.readLine();
			String[] colNames = header.split(SEPARATOR, -1);
			List<String> columnNames = Arrays.stream(colNames).map(String::trim).collect(Collectors.toList());
			for (String colName : columnNames) {
				result.put(colName, new ArrayList<>());
			}
			reader.lines().forEach(l -> parseRow(l, result, columnNames));
		} catch (IOException e) {
			throw new TableParsingException("The parsing of the CSV for table " + name + " was not successful.", e, this);
		}
		// result itself is not unmodifiable for testing reasons, see EnumerationDataTableForTest#getColumns()
		return result.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> Collections.unmodifiableList(e.getValue())));
	}

	private void parseRow(String line, Map<String, List<String>> table, List<String> columnNames) {
		String[] values = line.split(SEPARATOR, -1);
		if (values.length != columnNames.size()) {
			// Ignore errors, they are checked explicitly in CSVTableChecker
			values = Arrays.copyOfRange(values, 0, columnNames.size());
		}
		for (int i = 0; i < columnNames.size(); i++) {
			table.get(columnNames.get(i)).add(StringUtils.trimToEmpty(values[i]));
		}
	}

	/**
	 * Returns the table parsed from the CSV.
	 *
	 * @throws TableParsingException
	 * 		if the table could not be parsed successfully.
	 */
	@Override
	public Map<String, List<String>> getColumns() {
		if (lazyColumns == null) {
			lazyColumns = parseCSV();
		}
		return Collections.unmodifiableMap(lazyColumns);
	}

	/**
	 * Return the original UTF-8 encoded CSV string.
	 */
	@Override
	public byte[] getBytes() {
		return csvBytes;
	}

	@Override
	public boolean hasDisplayColumn(String columnName) {
		return getColumns().containsKey(columnName + CSVTableChecker.DISPLAY_SUFFIX);
	}

	@Override
	public Set<String> getDistinctCategoryValues(String enumColumnName, String category) {
		return getDistinctValues(enumColumnName + "." + category);
	}

	@Override
	public Set<String> getDistinctValues(String columnName) {
		return columnsWithDistinctValues.computeIfAbsent(columnName,
				n -> Collections.unmodifiableSet(getColumns().getOrDefault(n, Collections.emptyList()).stream()
						.collect(Collectors.toSet())));
	}
}
